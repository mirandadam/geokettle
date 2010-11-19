package org.pentaho.di.trans.steps.webservices;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.webservices.wsdl.Wsdl;
import org.pentaho.di.trans.steps.webservices.wsdl.XsdType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ctc.wstx.exc.WstxParsingException;

public class WebService extends BaseStep implements StepInterface
{
    public static final String NS_PREFIX = "ns";

    private WebServiceData data;

    private WebServiceMeta meta;

    private StringBuffer xml;

    private int nbRowProcess;

    private long requestTime;

    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    private SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    private DecimalFormat decFormat = new DecimalFormat("00");

    private Date dateRef;

    public WebService(StepMeta aStepMeta, StepDataInterface aStepData, int value, TransMeta aTransMeta, Trans aTrans)
    {
        super(aStepMeta, aStepData, value, aTransMeta, aTrans);

        // Reference date used to format hours
        try
        {
            dateRef = timeFormat.parse("00:00:00");
        }
        catch (ParseException e)
        {
            logError("Unexpected error in WebService constructor: ", e);
            setErrors(1);
            stopAll();
        }
    }

    public boolean processRow(StepMetaInterface metaInterface, StepDataInterface dataInterface) throws KettleException
    {
        meta = (WebServiceMeta) metaInterface;
        data = (WebServiceData) dataInterface;

        Object[] vCurrentRow = getRow();

    	if (first)
    	{
    		first=false;
    	
    		if (getInputRowMeta()!=null) {
    			data.outputRowMeta = getInputRowMeta().clone();
    		} else {
    			data.outputRowMeta = new RowMeta();
    		}
    		meta.getFields(data.outputRowMeta, getStepname(), null, null, this);
    		
    		defineIndexList(getInputRowMeta(), vCurrentRow);
    		startXML();
    	}
    	else
    	{
    		// Input from previous steps, no longer getting any rows, call it a day...
    		//
    		if (vCurrentRow==null) {
    			setOutputDone();
    			return false;
    		}
    	}
    	
        if (vCurrentRow != null)
        {
            parseRow(getInputRowMeta(), vCurrentRow);

            nbRowProcess++;
        }

        if ((vCurrentRow == null && (nbRowProcess % meta.getCallStep() != 0)) || (vCurrentRow != null && ((nbRowProcess > 0 && nbRowProcess % meta.getCallStep() == 0)))
            || (vCurrentRow == null && (!meta.hasFieldsIn())))
        {
            endXML();
            requestSOAP(vCurrentRow, getInputRowMeta());

            startXML();
        }

        // No input received, this one lookup execution is all we're going to do.
        //
        if (vCurrentRow == null)
        {
            setOutputDone();
        }
        return vCurrentRow != null;
    }

    private List<Integer> indexList;
    
    private void defineIndexList(RowMetaInterface rowMeta, Object[] vCurrentRow)
    {
    	// Create an index list for the input fields
    	//
        indexList = new ArrayList<Integer>();
        if (rowMeta!=null) {
	        for (WebServiceField curField : meta.getFieldsIn())
	        {
	            int index = rowMeta.indexOfValue(curField.getName());
	            if (index>=0)
	            {
	            	indexList.add(index);
	            }
	        }
        }
        
        // Create a map for the output values too
        //
        for (WebServiceField curField : meta.getFieldsOut())
        {
            int index = data.outputRowMeta.indexOfValue(curField.getName());
            if (index>=0)
            {
            	// Keep a mapping between the web service name and the index of the target field.
            	// This makes it easier to populate the fields later on, reading back the result.
            	//
            	data.indexMap.put(curField.getWsName(), index); 
            }
        }
    	
    }
    
    private void parseRow(RowMetaInterface rowMeta, Object[] vCurrentRow) throws KettleValueException
    {
        if (meta.getInFieldArgumentName() != null)
        {
            xml.append("        <" + NS_PREFIX + ":").append(meta.getInFieldArgumentName()).append(">\n");
        }

        for (Integer index : indexList)
        {
            ValueMetaInterface vCurrentValue = rowMeta.getValueMeta(index);
            Object data = vCurrentRow[index];
            
            WebServiceField field = meta.getFieldInFromName(vCurrentValue.getName());
            if (field != null)
            {
                if (!vCurrentValue.isNull(data))
                {
                    xml.append("          <").append(NS_PREFIX).append(":").append(field.getWsName()).append(">");
                    if (XsdType.TIME.equals(field.getXsdType()))
                    {
                        // Allow to deal with hours like 36:12:12 (> 24h)
                        long millis = vCurrentValue.getDate(data).getTime() - dateRef.getTime();
                        xml.append(decFormat.format(millis / 3600000) + ":"
                                   + decFormat.format((millis % 3600000) / 60000)
                                   + ":"
                                   + decFormat.format(((millis % 60000) / 1000)));
                    }
                    else if (XsdType.DATE.equals(field.getXsdType()))
                    {
                        xml.append(dateFormat.format(vCurrentValue.getDate(data)));
                    }
                    else if (XsdType.BOOLEAN.equals(field.getXsdType()))
                    {
                        xml.append(vCurrentValue.getBoolean(data) ? "true" : "false");
                    }
                    else if (XsdType.DATE_TIME.equals(field.getXsdType()))
                    {
                        xml.append(dateTimeFormat.format(vCurrentValue.getDate(data)));
                    }
                    else if (vCurrentValue.isNumber())
                    {
                    	// TODO: To Fix !! This is very bad coding...
                    	//
                        xml.append(vCurrentValue.getString(data).trim().replace(',', '.'));
                    }
                    else
                    {
                        xml.append(Const.trim(vCurrentValue.getString(data)));
                    }
                    xml.append("</").append(NS_PREFIX).append(":").append(field.getWsName()).append(">\n");
                }
                else
                {
                    xml.append("          <").append(NS_PREFIX).append(":").append(field.getWsName()).append(" xsi:nil=\"true\"/>\n");
                }
            }
        }
        if (meta.getInFieldArgumentName() != null)
        {
            xml.append("        </" + NS_PREFIX + ":").append(meta.getInFieldArgumentName()).append(">\n");
        }
    }

    private void startXML()
    {
        xml = new StringBuffer();

        // TODO We only manage one name space for all the elements. See in the
        // future how to manage multiple name spaces
        //
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        xml.append("<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:ns=\"");
        xml.append(meta.getOperationNamespace());
        xml.append("\">\n");

        xml.append("  <soapenv:Header/>\n");
        xml.append("  <soapenv:Body>\n");

        xml.append("    <" + NS_PREFIX + ":").append(meta.getOperationName()).append(">\n");
        if (meta.getInFieldContainerName() != null)
        {
            xml.append("      <" + NS_PREFIX + ":" + meta.getInFieldContainerName() + ">\n");
        }

    }

    private void endXML()
    {
    	if (xml==null) startXML();
    	
        if (meta.getInFieldContainerName() != null)
        {
            xml.append("      </" + NS_PREFIX + ":" + meta.getInFieldContainerName() + ">\n");
        }
        xml.append("    </" + NS_PREFIX + ":").append(meta.getOperationName()).append(">\n");
        xml.append("  </soapenv:Body>\n");
        xml.append("</soapenv:Envelope>\n");
    }

    private synchronized void requestSOAP(Object[] rowData, RowMetaInterface rowMeta) throws KettleException
    {
        Wsdl wsdl;
        try{
         wsdl = new Wsdl(new java.net.URI(data.realUrl), null, null);
        }
        catch(Exception e){
         throw new KettleStepException(Messages.getString("WebServices.ERROR0013.ExceptionLoadingWSDL"), e);
        }
        String vURLService = wsdl.getServiceEndpoint();
        
        HttpClient vHttpClient = new HttpClient();
        PostMethod vHttpMethod = new PostMethod(vURLService);
        HostConfiguration vHostConfiguration = new HostConfiguration();

        String httpLogin = environmentSubstitute(meta.getHttpLogin());
        if (httpLogin != null && !"".equals(httpLogin))
        {
            vHttpClient.getParams().setAuthenticationPreemptive(true);
            Credentials defaultcreds = new UsernamePasswordCredentials(httpLogin, environmentSubstitute(meta.getHttpPassword()));
            vHttpClient.getState().setCredentials(AuthScope.ANY, defaultcreds);
        }

        String proxyHost = environmentSubstitute(meta.getProxyHost());
        if (proxyHost != null && !"".equals(proxyHost))
        {
            vHostConfiguration.setProxy(proxyHost, Const.toInt(environmentSubstitute(meta.getProxyPort()), 8080));
        }

        try
        {
        	URI uri = new URI(vURLService, false);
            vHttpMethod.setURI(uri);
            vHttpMethod.setRequestHeader("Content-Type", "text/xml;charset=UTF-8");
            
            String soapAction = "\"" + meta.getOperationNamespace(); 
            if (!meta.getOperationNamespace().endsWith("/")) {
            	soapAction += "/";
            }
            soapAction+=meta.getOperationName()+"\"";            
            logDetailed(Messages.getString("WebServices.Log.UsingRequestHeaderSOAPAction", soapAction));
            vHttpMethod.setRequestHeader("SOAPAction", soapAction);

            RequestEntity requestEntity = new ByteArrayRequestEntity(xml.toString().getBytes("UTF-8"), "UTF-8");
            vHttpMethod.setRequestEntity(requestEntity);
            long currentRequestTime = Const.nanoTime();
            int responseCode = vHttpClient.executeMethod(vHostConfiguration, vHttpMethod);
            if (responseCode == 200)
            {
                processRows(vHttpMethod.getResponseBodyAsStream(), rowData, rowMeta);
            }
            else if (responseCode == 401)
            {
                throw new KettleStepException(Messages.getString("WebServices.ERROR0011.Authentication", vURLService));
            }
            else if (responseCode == 404)
            {
            	throw new KettleStepException(Messages.getString("WebServices.ERROR0012.NotFound", vURLService));
            }
            else
            {
            	throw new KettleStepException(Messages.getString("WebServices.ERROR0001.ServerError", Integer.toString(responseCode), Const.NVL(new String(vHttpMethod.getResponseBody()), ""), vURLService) );
            }
            requestTime += Const.nanoTime() - currentRequestTime;
        }
        catch (URIException e)
        {
            throw new KettleStepException(Messages.getString("WebServices.ERROR0002.InvalidURI", vURLService), e);
        }
        catch (UnsupportedEncodingException e)
        {
            throw new KettleStepException(Messages.getString("WebServices.ERROR0003.UnsupportedEncoding", vURLService), e);
        }
        catch (HttpException e)
        {
            throw new KettleStepException(Messages.getString("WebServices.ERROR0004.HttpException", vURLService), e);
        }
        catch (UnknownHostException e)
        {
            throw new KettleStepException(Messages.getString("WebServices.ERROR0013.UnknownHost", vURLService), e);
        }
        catch (IOException e)
        {
            throw new KettleStepException(Messages.getString("WebServices.ERROR0005.IOException", vURLService), e);
        }
        finally
        {
            vHttpMethod.releaseConnection();
        }
    }

    public boolean init(StepMetaInterface smi, StepDataInterface sdi)
    {
        meta = (WebServiceMeta) smi;
        data = (WebServiceData) sdi;
        
        data.indexMap = new Hashtable<String,Integer>();
        data.realUrl=environmentSubstitute(meta.getUrl());
        return super.init(smi, sdi);
    }

    public void dispose(StepMetaInterface smi, StepDataInterface sdi)
    {
        meta = (WebServiceMeta) smi;
        data = (WebServiceData) sdi;

        super.dispose(smi, sdi);
    }
    
    private String readStringFromInputStream(InputStream anXml) throws KettleStepException {
		StringBuffer response = new StringBuffer();
		try
    	{
    		int c=anXml.read();
    		while (c>=0)
    		{
    			response.append((char)c);
    			c=anXml.read();
    		}
    		anXml.close();
    		
    		return response.toString();
    	}
    	catch(Exception e)
    	{
    		throw new KettleStepException("Unable to read web service response data from input stream", e);
    	}
    }

    private void processRows(InputStream anXml, Object[] rowData, RowMetaInterface rowMeta) throws KettleException
    {
    	// Just to make sure the old transformations keep working...
    	//
    	if (meta.isCompatible()) {
    		compatibleProcessRows(anXml, rowData, rowMeta);
    		return;
    	}
    	
    	// First we should get the complete string
    	// The problem is that the string can contain XML or any other format such as HTML saying the service is no longer available.
    	// We're talking about a WEB service here.
    	// As such, to keep the original parsing scheme, we first read the content.
    	// Then we create an input stream from the content again.
    	// It's elaborate, but that way we can report on the failure more correctly.
    	//
    	String response = readStringFromInputStream(anXml);

    	try {

	    	// What is the expected response object for the operation?
	    	//
	    	Document doc = XMLHandler.loadXMLString(response);
	    	Node enveloppeNode = XMLHandler.getSubNode(doc, "soapenv:Envelope");
	    	if (enveloppeNode==null) enveloppeNode = XMLHandler.getSubNode(doc, "soap:Envelope"); // retry
	    	Node bodyNode = XMLHandler.getSubNode(enveloppeNode, "soapenv:body");
	    	if (bodyNode==null) bodyNode = XMLHandler.getSubNode(enveloppeNode, "soap:body");// retry
	    	
	    	// Create a few objects to help do the layout of XML snippets we find along the way
	    	// 
	    	TransformerFactory transformerFactory = TransformerFactory.newInstance();
	    	Transformer transformer = transformerFactory.newTransformer();
	    	transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");

            if (log.isDetailed()) {
		    	StringWriter bodyXML = new StringWriter();
				transformer.transform(new DOMSource(bodyNode), new StreamResult(bodyXML));
				
				logDetailed(bodyXML.toString());
            }			

	    	// The node directly below the body is the response node
	    	// It's apparently a hassle to get the name in a consistent way, but we know it's the first element node
	    	//
	    	Node responseNode = null;
	    	NodeList nodeList = null;
	    	if (!Const.isEmpty(meta.getRepeatingElementName())) {
	    	
	    		// We have specified the repeating element name : use it
	    		//
	    		nodeList = ((Element)bodyNode).getElementsByTagName(meta.getRepeatingElementName());
	    		
	    	} else {
	    		
	    		if (meta.isReturningReplyAsString()) {
	    			
	    			// Just return the body node as an XML string...
	    			//
	    			StringWriter nodeXML = new StringWriter();
	    			transformer.transform(new DOMSource(bodyNode), new StreamResult(nodeXML));
	    			String xml = response; // nodeXML.toString();
	    			Object[] outputRowData = createNewRow(rowData);
	    			int index = rowData==null ? 0 : getInputRowMeta().size();
	    			outputRowData[index++] = xml;
	    			putRow(data.outputRowMeta, outputRowData);
	    			
	    		} else {
	    			
		    		// We just grab the list of nodes from the children of the body
		    		// Look for the first element node (first real child) and take that one.
		    		// For that child-element, we consider all the children below
		    		//
			    	NodeList responseChildren = bodyNode.getChildNodes();
			    	for (int i=0;i<responseChildren.getLength();i++) {
			    		Node responseChild = responseChildren.item(i);
			    		if (responseChild.getNodeType()==Node.ELEMENT_NODE) {
			    			responseNode = responseChild;
			    			break;
			    		}
			    	}
			    	if (responseNode!=null) {
			    		nodeList = responseNode.getChildNodes();
			    	}
	    		}
	    	}
	    	
	    	// The section below is just for repeating nodes.  If we don't have those it ends here.
	    	//
	    	if (nodeList==null || meta.isReturningReplyAsString()) return;

            // Allocate a result row in case we are dealing with a single result row
            //
            Object[] outputRowData = createNewRow(rowData);
            
	    	// Now loop over the node list found above...
	    	//
            boolean singleRow = false;
            int fieldsFound = 0;
	    	for (int i=0;i<nodeList.getLength();i++) {
	    		Node node = nodeList.item(i);
	    		
	    		if (meta.isReturningReplyAsString()) {
	    			
	    			// Just return the body node as an XML string...
	    			//
	    			StringWriter nodeXML = new StringWriter();
	    			transformer.transform(new DOMSource(bodyNode), new StreamResult(nodeXML));
	    			String xml = nodeXML.toString();
	    			outputRowData = createNewRow(rowData);
	    			int index = rowData==null ? 0 : getInputRowMeta().size();
	    			outputRowData[index++] = xml;
	    			putRow(data.outputRowMeta, outputRowData);
	    			
	    		} else {
	    		
	    			// This node either contains the data for a single row or it contains the first element of a single result response
	    			// If we find the node name in out output result fields list, we are going to consider it a single row result.
	    			//
	    			WebServiceField field = meta.getFieldOutFromWsName(node.getNodeName());
	    			if (field!=null) {
	    				if (getNodeValue(outputRowData, node, field, transformer, true)) {
	    					// We found a match.
	    					// This means that we are dealing with a single row
	    					// It also means that we need to update the output index pointer
	    					//
	    					singleRow=true;
	    					fieldsFound++;
	    				}
	    			} else {
	    				// If we didn't already get data in the previous block we'll assume multiple rows coming back.
	    				//
	    				if (!singleRow) {
		    				// Sticking with the multiple-results scenario...
		    				//
		    				
		    				// TODO: remove next 2 lines, added for debug reasons.
		    				//
		    				if (log.isDetailed()) {
				    			StringWriter nodeXML = new StringWriter();
				    			transformer.transform(new DOMSource(node), new StreamResult(nodeXML));
				    			logDetailed(Messages.getString("WebServices.Log.ResultRowDataFound", nodeXML.toString()));
		    				}
			    			
				    		// Allocate a new row...
			    			//
			    			outputRowData = createNewRow(rowData);
				            
				            // Let's see what's in there...
				            //
				    		NodeList childNodes = node.getChildNodes();
				    		for (int j=0;j<childNodes.getLength();j++) {
				    			Node childNode = childNodes.item(j);
				    			
				    			field = meta.getFieldOutFromWsName(childNode.getNodeName());
				    			if (field!=null) {
				    			
					    			if (getNodeValue(outputRowData, childNode, field, transformer, false)) {
				    					// We found a match.
				    					// This means that we are dealing with a single row
				    					// It also means that we need to update the output index pointer
				    					//
				    					fieldsFound++;
				    				}
				    			}
				    		}
				    		
				    		// Prevent empty rows from being sent out.
				    		//
				    		if (fieldsFound>0) {
				    			// Send a row in a series of rows on its way.
				    			//
				    			putRow(data.outputRowMeta, outputRowData);
				    		}
	    				}
	    			}
	    		}
    		}

    		if (singleRow && fieldsFound>0) {
    			// Send the single row on its way.
    			//
    			putRow(data.outputRowMeta, outputRowData);
    		}
        }
        catch (Exception e)
        {
            throw new KettleStepException(Messages.getString("WebServices.ERROR0010.OutputParsingError", response.toString()), e);
        }
    }
    
    private Object[] createNewRow(Object[] inputRowData) {
    	return inputRowData==null ? RowDataUtil.allocateRowData(data.outputRowMeta.size()) : RowDataUtil.createResizedCopy(inputRowData, data.outputRowMeta.size());
	}

	private void compatibleProcessRows(InputStream anXml, Object[] rowData, RowMetaInterface rowMeta) throws KettleException {

		// First we should get the complete string
		// The problem is that the string can contain XML or any other format such as HTML saying the service is no longer available.
		// We're talking about a WEB service here.
		// As such, to keep the original parsing scheme, we first read the content.
		// Then we create an input stream from the content again.
		// It's elaborate, but that way we can report on the failure more correctly.
		//
		String response = readStringFromInputStream(anXml);

		// Create a new reader to feed into the XML Input Factory below...
		//
		StringReader stringReader = new StringReader(response.toString());

		// TODO Very empirical : see if we can do something better here
		try {
			XMLInputFactory vFactory = XMLInputFactory.newInstance();
			XMLStreamReader vReader = vFactory.createXMLStreamReader(stringReader);

			Object[] outputRowData = RowDataUtil.allocateRowData(data.outputRowMeta.size());
			int outputIndex = 0;

			boolean processing = false;
			boolean oneValueRowProcessing = false;
			for (int event = vReader.next(); vReader.hasNext(); event = vReader.next()) {
				switch (event) {
				case XMLStreamConstants.START_ELEMENT:

					// Start new code
					//START_ELEMENT= 1
					//
					if (log.isRowLevel())
						logRowlevel("START_ELEMENT / " + vReader.getAttributeCount() + " / " + vReader.getNamespaceCount());

					// If we start the xml element named like the return type,
					// we start a new row
					//
					if (log.isRowLevel())
						logRowlevel("vReader.getLocalName = " + vReader.getLocalName());
					if (Const.isEmpty(meta.getOutFieldArgumentName())) {
						//getOutFieldArgumentName() == null
						if (oneValueRowProcessing) {
							WebServiceField field = meta.getFieldOutFromWsName(vReader.getLocalName());
							if (field != null) {
								outputRowData[outputIndex++] = getValue(vReader.getElementText(), field);
								putRow(data.outputRowMeta, outputRowData);
								oneValueRowProcessing = false;
							} else {
								if (meta.getOutFieldContainerName().equals(vReader.getLocalName())) {
									// meta.getOutFieldContainerName() = vReader.getLocalName()
									if (log.isRowLevel())
										logRowlevel("OutFieldContainerName = " + meta.getOutFieldContainerName());
									oneValueRowProcessing = true;
								}
							}
						}
					} else {
						//getOutFieldArgumentName() != null
						if (log.isRowLevel())
							logRowlevel("OutFieldArgumentName = " + meta.getOutFieldArgumentName());
						if (meta.getOutFieldArgumentName().equals(vReader.getLocalName())) {
							if (log.isRowLevel())
								logRowlevel("vReader.getLocalName = " + vReader.getLocalName());
							if (log.isRowLevel())
								logRowlevel("OutFieldArgumentName = ");
							if (processing) {
								WebServiceField field = meta.getFieldOutFromWsName(vReader.getLocalName());
								if (field != null) {
									int index = data.outputRowMeta.indexOfValue(field.getName());
									if (index >= 0) {
										outputRowData[index] = getValue(vReader.getElementText(), field);
									}
								}
								processing = false;
							} else {
								WebServiceField field = meta.getFieldOutFromWsName(vReader.getLocalName());
								if (meta.getFieldsOut().size() == 1 && field != null) {
									// This can be either a simple return element, or a complex type...
									//
									try {
										outputRowData[outputIndex++] = getValue(vReader.getElementText(), field);
										putRow(data.outputRowMeta, outputRowData);
									} catch (WstxParsingException e) {
										throw new KettleStepException("Unable to get value for field [" + field.getName() + "].  Verify that this is not a complex data type by looking at the response XML.", e);
									}
								} else {
									for (WebServiceField curField : meta.getFieldsOut()) {
										if (!Const.isEmpty(curField.getName())) {
											outputRowData[outputIndex++] = getValue(vReader.getElementText(), curField);
										}
									}
									processing = true;
								}
							}

						} else {
							if (log.isRowLevel())
								logRowlevel("vReader.getLocalName = " + vReader.getLocalName());
							if (log.isRowLevel())
								logRowlevel("OutFieldArgumentName = " + meta.getOutFieldArgumentName());
						}
					}
					break;

				case XMLStreamConstants.END_ELEMENT:
					//END_ELEMENT= 2
					if (log.isRowLevel())
						logRowlevel("END_ELEMENT");
					// If we end the xml element named as the return type, we
					// finish a row
					if ((meta.getOutFieldArgumentName() == null && meta.getOperationName().equals(vReader.getLocalName()))) {
						oneValueRowProcessing = false;
					} else if (meta.getOutFieldArgumentName() != null && meta.getOutFieldArgumentName().equals(vReader.getLocalName())) {
						putRow(data.outputRowMeta, outputRowData);
						processing = false;
					}
					break;
				case XMLStreamConstants.PROCESSING_INSTRUCTION:
					//PROCESSING_INSTRUCTION= 3
					if (log.isRowLevel())
						logRowlevel("PROCESSING_INSTRUCTION");
					break;
				case XMLStreamConstants.CHARACTERS:
					//CHARACTERS= 4
					if (log.isRowLevel())
						logRowlevel("CHARACTERS");
					break;
				case XMLStreamConstants.COMMENT:
					//COMMENT= 5
					if (log.isRowLevel())
						logRowlevel("COMMENT");
					break;
				case XMLStreamConstants.SPACE:
					//PROCESSING_INSTRUCTION= 6
					if (log.isRowLevel())
						logRowlevel("PROCESSING_INSTRUCTION");
					break;
				case XMLStreamConstants.START_DOCUMENT:
					//START_DOCUMENT= 7
					if (log.isRowLevel())
						logRowlevel("START_DOCUMENT");
					if (log.isRowLevel())
						logRowlevel(vReader.getText());
					break;
				case XMLStreamConstants.END_DOCUMENT:
					//END_DOCUMENT= 8
					if (log.isRowLevel())
						logRowlevel("END_DOCUMENT");
					break;
				case XMLStreamConstants.ENTITY_REFERENCE:
					//ENTITY_REFERENCE= 9
					if (log.isRowLevel())
						logRowlevel("ENTITY_REFERENCE");
					break;
				case XMLStreamConstants.ATTRIBUTE:
					//ATTRIBUTE= 10
					if (log.isRowLevel())
						logRowlevel("ATTRIBUTE");
					break;
				case XMLStreamConstants.DTD:
					//DTD= 11
					if (log.isRowLevel())
						logRowlevel("DTD");
					break;
				case XMLStreamConstants.CDATA:
					//CDATA= 12
					if (log.isRowLevel())
						logRowlevel("CDATA");
					break;
				case XMLStreamConstants.NAMESPACE:
					//NAMESPACE= 13
					if (log.isRowLevel())
						logRowlevel("NAMESPACE");
					break;
				case XMLStreamConstants.NOTATION_DECLARATION:
					//NOTATION_DECLARATION= 14
					if (log.isRowLevel())
						logRowlevel("NOTATION_DECLARATION");
					break;
				case XMLStreamConstants.ENTITY_DECLARATION:
					//ENTITY_DECLARATION= 15
					if (log.isRowLevel())
						logRowlevel("ENTITY_DECLARATION");
					break;
				default:
					break;
				}
			}
		} catch (Exception e) {
			throw new KettleStepException(Messages.getString("WebServices.ERROR0010.OutputParsingError", response.toString()), e);
		}
	}
    
    private boolean getNodeValue(Object[] outputRowData, Node node, WebServiceField field, Transformer transformer, boolean singleRowScenario) throws KettleException {
		
    	Integer outputIndex = data.indexMap.get(field.getWsName());
    	if (outputIndex==null) {
    		// Unknown field : don't look any further, it's not a field we want to use.
    		//
    		return false;
    	}
    	
    	// if it's a text node or if we recognize the field type, we just grab the value 
    	//
    	if (node.getNodeType()==Node.TEXT_NODE || !field.isComplex()) {
			Object rowValue = null;
			
			// See if this is a node we expect as a return value...
			//
			String textContent = node.getTextContent();
        	try {
        		rowValue = getValue(textContent, field);
        		outputRowData[outputIndex] = rowValue;
				return true;
        	}
        	catch(Exception e) {
        		throw new KettleException("Unable to convert value ["+textContent+"] for field ["+field.getWsName()+"], type ["+field.getXsdType()+"]", e);
        	}
		} else if (node.getNodeType()==Node.ELEMENT_NODE) {
			// Perhaps we're dealing with complex data types.
			// Perhaps we can just ship the XML snippet over to the next steps.
			//
			try {
				StringWriter childNodeXML = new StringWriter();
    			transformer.transform(new DOMSource(node), new StreamResult(childNodeXML));
    			outputRowData[outputIndex] = childNodeXML.toString();
				return true;
			}
			catch(Exception e) {
        		throw new KettleException("Unable to transform DOM node with name ["+node.getNodeName()+"] to XML", e);
			}
		}
    	
    	// Nothing found, return false
    	//
		return false;
	}

	private Object getValue(String vNodeValue, WebServiceField field) throws XMLStreamException, ParseException
    {
        if (vNodeValue == null)
        {
            return null;
        }
        else
        {
            if (XsdType.BOOLEAN.equals(field.getXsdType()))
            {
                return Boolean.valueOf(vNodeValue);
            }
            else if (XsdType.DATE.equals(field.getXsdType()))
            {
                try
                {
                    return dateFormat.parse(vNodeValue);
                }
                catch (ParseException e)
                {
                	logError(Const.getStackTracker(e));
                    setErrors(1);
                    stopAll();
                    return null;
                }
            }
            else if (XsdType.TIME.equals(field.getXsdType()))
            {
                try
                {
                    return timeFormat.parse(vNodeValue);
                }
                catch (ParseException e)
                {
                	logError(Const.getStackTracker(e));
                    setErrors(1);
                    stopAll();
                	return null;
                }
            }
            else if (XsdType.DATE_TIME.equals(field.getXsdType()))
            {
                try
                {
                    return dateTimeFormat.parse(vNodeValue);
                }
                catch (ParseException e)
                {
                	logError(Const.getStackTracker(e));
                    setErrors(1);
                    stopAll();
                	return null;
                }
            }
            else if (XsdType.INTEGER.equals(field.getXsdType()) || XsdType.SHORT.equals(field.getXsdType()) || XsdType.INTEGER_DESC.equals(field.getXsdType()))
            {
                try
                {
                    return Long.parseLong(vNodeValue);
                }
                catch (NumberFormatException e)
                {
                	logError(Const.getStackTracker(e));
                    setErrors(1);
                    stopAll();
                	return null;
                }
            }
            else if (XsdType.FLOAT.equals(field.getXsdType()) || XsdType.DOUBLE.equals(field.getXsdType()))
            {
                try
                {
                    return Double.parseDouble(vNodeValue);
                }
                catch (NumberFormatException e)
                {
                	logError(Const.getStackTracker(e));
                    setErrors(1);
                    stopAll();
                    return null;
                }
            }
            else if (XsdType.BINARY.equals(field.getXsdType()))
            {
                return Base64.decodeBase64(vNodeValue.getBytes());
            }
            else if (XsdType.DECIMAL.equals(field.getXsdType()))
            {
                return new BigDecimal(vNodeValue);
            }
            else
            {
                return vNodeValue;
            }
        }
    }
 
	//
	// Run is were the action happens!
	public void run()
	{
    	BaseStep.runStepThread(this, meta, data);
	}
    
}