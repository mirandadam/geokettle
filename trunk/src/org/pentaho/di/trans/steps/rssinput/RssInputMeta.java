/*************************************************************************************** 
 * Copyright (C) 2007 Samatar.  All rights reserved. 
 * This software was developed by Samatar and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. A copy of the license, 
 * is included with the binaries and source code. The Original Code is Samatar.  
 * The Initial Developer is Samatar.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an 
 * "AS IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. 
 * Please refer to the license for the specific language governing your rights 
 * and limitations.
 ***************************************************************************************/

/* 
 * 
 * Created on 4-apr-2003
 * 
 */

package org.pentaho.di.trans.steps.rssinput;

import java.util.List;
import java.util.Map;

import org.w3c.dom.Node;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

public class RssInputMeta extends BaseStepMeta implements StepMetaInterface
{	
	
	/** Flag indicating that a row number field should be included in the output */
	private  boolean includeRowNumber;
	
	/** The name of the field in the output containing the row number*/
	private  String  rowNumberField;
	
	/** Flag indicating that url field should be included in the output */
	private  boolean includeUrl;
	
	/** The name of the field in the output containing the url*/
	private  String  urlField;
	
	/** The maximum number or lines to read */
	private  long  rowLimit;

	/** The fields to import... */
	private RssInputField inputFields[];
	
	/** The url **/
	private String url[];
	
	/** read rss from */
	private String readfrom;
	
	/** if URL defined in a field? */
	private boolean urlInField;
	
	/** URL field name */
	private String urlFieldname;
	
	
	public RssInputMeta()
	{
		super(); // allocate BaseStepMeta
	}
		
	/**
     * @return Returns the input fields.
     */
    public RssInputField[] getInputFields()
    {
        return inputFields;
    }
    
    /**
     * @param inputFields The input fields to set.
     */
    public void setInputFields(RssInputField[] inputFields)
    {
        this.inputFields = inputFields;
    }

    
    
    /**
     * @return Returns the urlInField.
     */
    public boolean urlInField()
    {
        return urlInField;
    }
    
    
    
    /**
     *  @param inputFields The urlInField to set.
     */
    public void seturlInField(boolean urlInFieldin)
    {
        this.urlInField=urlInFieldin;
    }
    
    
    /**
     * @return Returns the includeRowNumber.
     */
    public boolean includeRowNumber()
    {
        return includeRowNumber;
    }
    
    public void setReadFrom(String readfrom)
    {
    	this.readfrom=readfrom;
    }
    
    public String getReadFrom()
    {
    	return readfrom;
    }
    
    public String getRealReadFrom()
    {
    	return getReadFrom();
    }
    
    
    /**
     * @return Returns the includeUrl.
     */
    public boolean includeUrl()
    {
        return includeUrl;
    }
    
    /**
     * @param includeRowNumber The includeRowNumber to set.
     */
    public void setIncludeRowNumber(boolean includeRowNumber)
    {
        this.includeRowNumber = includeRowNumber;
    }
    
    
    /**
     * @param includeUrl The includeUrl to set.
     */
    public void setIncludeUrl(boolean includeUrl)
    {
        this.includeUrl = includeUrl;
    }
    
    
    
    /**
     * @return Returns the rowLimit.
     */
    public long getRowLimit()
    {
        return rowLimit;
    }
    
    /**
     * @param rowLimit The rowLimit to set.
     */
    public void setRowLimit(long rowLimit)
    {
        this.rowLimit = rowLimit;
    }

    /**
     * @return Returns the rowNumberField.
     */
    public String getRowNumberField()
    {
        return rowNumberField;
    }
    
    /**
     * @return Returns the urlField.
     */
    public String geturlField()
    {
        return urlField;
    }
    
    /**
     * @param urlField The urlField to set.
     */
    public void seturlField(String urlField)
    {
        this.urlField = urlField;
    }

    /**
     * @param urlFieldname The urlFieldname to set.
     */
    public void setUrlFieldname(String urlFieldname)
    {
        this.urlFieldname = urlFieldname;
    }

    /**
     *@return Returns the urlFieldname.
     */
    public String getUrlFieldname()
    {
        return urlFieldname;
    }
    
    /**
     * @param url The url to set.
     */
    public void setUrl(String[] url)
    {
        this.url = url;
    }
    
    public String[] getUrl()
    {
    	return url;
    }
    
    
    /**
     * @param rowNumberField The rowNumberField to set.
     */
    public void setRowNumberField(String rowNumberField)
    {
        this.rowNumberField = rowNumberField;
    }
    

   
    public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters)
	    throws KettleXMLException
	{
		readData(stepnode);
	}

	public Object clone()
	{
		RssInputMeta retval = (RssInputMeta)super.clone();

		int nrFields = inputFields.length;
		int nrUrl  = url.length;

		retval.allocate(nrUrl, nrFields);
		for (int i=0;i<nrUrl;i++)
		{
			retval.url[i]     = url[i];
		}
		for (int i=0;i<nrFields;i++)
		{
            if (inputFields[i]!=null)
            {
                retval.inputFields[i] = (RssInputField)inputFields[i].clone();
            }
		}
		
		return retval;
	}
    
    public String getXML()
    {
        StringBuffer retval=new StringBuffer();

        
        retval.append("    "+XMLHandler.addTagValue("url_in_field",      urlInField));
        retval.append("    "+XMLHandler.addTagValue("url_field_name",    urlFieldname));
        retval.append("    "+XMLHandler.addTagValue("rownum",          includeRowNumber));
        retval.append("    "+XMLHandler.addTagValue("rownum_field",    rowNumberField));
        retval.append("    "+XMLHandler.addTagValue("include_url",          includeUrl));
        retval.append("    "+XMLHandler.addTagValue("url_Field",    urlField));
        retval.append("    "+XMLHandler.addTagValue("read_from",    readfrom));
        retval.append("    <urls>"+Const.CR);
        for (int i=0;i<url.length;i++)
        {
            retval.append("      "+XMLHandler.addTagValue("url",     url[i]));
        }
        retval.append("    </urls>"+Const.CR);
        retval.append("    <fields>"+Const.CR);
        for (int i=0;i<inputFields.length;i++)
        {
            RssInputField field = inputFields[i];
            retval.append(field.getXML());
        }
        retval.append("      </fields>"+Const.CR);
        retval.append("    "+XMLHandler.addTagValue("limit", rowLimit));
        return retval.toString();
    }

	private void readData(Node stepnode) throws KettleXMLException
	{
		try
		{

			urlInField  = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "url_in_field"));
			urlFieldname    = XMLHandler.getTagValue(stepnode, "url_field_name");
			includeRowNumber  = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "rownum"));
			rowNumberField    = XMLHandler.getTagValue(stepnode, "rownum_field");
			includeUrl  = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "include_url"));
			urlField    = XMLHandler.getTagValue(stepnode, "url_Field");
			readfrom    = XMLHandler.getTagValue(stepnode, "read_from");
			Node fields     = XMLHandler.getSubNode(stepnode,  "fields");
			int nrFields    = XMLHandler.countNodes(fields,    "field");
			Node urlnode  = XMLHandler.getSubNode(stepnode,   "urls");
			int nrUrls     = XMLHandler.countNodes(urlnode,  "url");
			allocate(nrUrls,nrFields);
			for (int i=0;i<nrUrls;i++)
			{
				Node urlnamenode = XMLHandler.getSubNodeByNr(urlnode, "url", i); 
				url[i] = XMLHandler.getNodeValue(urlnamenode);
			}

			for (int i=0;i<nrFields;i++)
			{
				Node fnode = XMLHandler.getSubNodeByNr(fields, "field", i);
				RssInputField field = new RssInputField(fnode);
				inputFields[i] = field;
			}
			
			// Is there a limit on the number of rows we process?
			rowLimit = Const.toLong(XMLHandler.getTagValue(stepnode, "limit"), 0L);
		}
		catch(Exception e)
		{
			throw new KettleXMLException("Unable to load step info from XML", e);
		}
	}
	
	public void allocate(int nrUrl,int nrfields)
	{
		inputFields = new RssInputField[nrfields]; 
		url   = new String [nrUrl];
	}
	
	public void setDefault()
	{
		urlInField=false;
		urlFieldname = "";
		includeRowNumber = false;
		rowNumberField   = "";		
		includeUrl = false;
		urlField   = "";
		readfrom="";
		
		int nrFields =0;
		int nrUrl =0;

		allocate(nrUrl,nrFields);	

		for (int i=0;i<nrUrl;i++) 
		{
			url[i]="";
			
		}
		
		
		for (int i=0;i<nrFields;i++)
		{
		    inputFields[i] = new RssInputField("field"+(i+1));
		}

		rowLimit=0;
	}
	public void getFields(RowMetaInterface r, String name, RowMetaInterface info[], StepMeta nextStep, VariableSpace space) throws KettleStepException
	{		
		int i;
		for (i=0;i<inputFields.length;i++)
		{
			RssInputField field = inputFields[i];       
	        
			int type=field.getType();
			if (type==ValueMeta.TYPE_NONE) type=ValueMeta.TYPE_STRING;
			ValueMetaInterface v=new ValueMeta(space.environmentSubstitute(field.getName()), type);
			v.setLength(field.getLength(), field.getPrecision());
			v.setOrigin(name);
			r.addValueMeta(v);
	        
		}
		
		if (includeUrl)
		{
			ValueMetaInterface v = new ValueMeta(space.environmentSubstitute(urlField), ValueMeta.TYPE_STRING);
			v.setLength(100, -1);
			v.setOrigin(name);
			r.addValueMeta(v);
		}
		
		if (includeRowNumber)
		{
			ValueMetaInterface v = new ValueMeta(space.environmentSubstitute(rowNumberField), ValueMeta.TYPE_INTEGER);
			v.setLength(ValueMetaInterface.DEFAULT_INTEGER_LENGTH, 0);
			v.setOrigin(name);
			r.addValueMeta(v);
		}
	}
	
	
	
	public void readRep(Repository rep, long id_step, List<DatabaseMeta> databases, Map<String, Counter> counters)
    throws KettleException
    {
		try
		{

			
			urlInField  = rep.getStepAttributeBoolean(id_step, "url_in_field");
			urlFieldname    = rep.getStepAttributeString (id_step, "url_field_name");
			includeRowNumber  = rep.getStepAttributeBoolean(id_step, "rownum");
			rowNumberField    = rep.getStepAttributeString (id_step, "rownum_field");
			includeUrl  = rep.getStepAttributeBoolean(id_step, "include_url");
			urlField    = rep.getStepAttributeString (id_step, "url_Field");
			readfrom    = rep.getStepAttributeString (id_step, "read_from");
			rowLimit          = rep.getStepAttributeInteger(id_step, "limit");

			int nrFields      = rep.countNrStepAttributes(id_step, "field_name");
			int nrUrls      = rep.countNrStepAttributes(id_step, "url_name");
            
			allocate(nrUrls,nrFields);
			
			for (int i=0;i<nrUrls;i++)
			{
				url[i] =      rep.getStepAttributeString (id_step, i, "url_name");
			
			}

			for (int i=0;i<nrFields;i++)
			{
			    RssInputField field = new RssInputField();
			    
				field.setName( rep.getStepAttributeString (id_step, i, "field_name") );
				field.setColumn( RssInputField.getColumnByCode( rep.getStepAttributeString (id_step, i, "field_column") ));
				field.setType( ValueMeta.getType( rep.getStepAttributeString (id_step, i, "field_type") ) );
				field.setFormat( rep.getStepAttributeString (id_step, i, "field_format") );
				field.setCurrencySymbol( rep.getStepAttributeString (id_step, i, "field_currency") );
				field.setDecimalSymbol( rep.getStepAttributeString (id_step, i, "field_decimal") );
				field.setGroupSymbol( rep.getStepAttributeString (id_step, i, "field_group") );
				field.setLength( (int)rep.getStepAttributeInteger(id_step, i, "field_length") );
				field.setPrecision( (int)rep.getStepAttributeInteger(id_step, i, "field_precision") );
				field.setTrimType( RssInputField.getTrimTypeByCode( rep.getStepAttributeString (id_step, i, "field_trim_type") ));
				field.setRepeated( rep.getStepAttributeBoolean(id_step, i, "field_repeat") );

				inputFields[i] = field;
			}
        }
		catch(Exception e)
		{
			throw new KettleException(Messages.getString("RssInputMeta.Exception.ErrorReadingRepository"), e);
		}
	}
	
	public void saveRep(Repository rep, long id_transformation, long id_step)
		throws KettleException
	{
		try
		{
			
			rep.saveStepAttribute(id_transformation, id_step, "url_in_field",          urlInField);
			rep.saveStepAttribute(id_transformation, id_step, "url_field_name",    urlFieldname);			
			rep.saveStepAttribute(id_transformation, id_step, "rownum",          includeRowNumber);
			rep.saveStepAttribute(id_transformation, id_step, "rownum_field",    rowNumberField);
			rep.saveStepAttribute(id_transformation, id_step, "include_url",          includeUrl);
			rep.saveStepAttribute(id_transformation, id_step, "url_Field",    urlField);
			rep.saveStepAttribute(id_transformation, id_step, "read_from",    readfrom);
			rep.saveStepAttribute(id_transformation, id_step, "limit",           rowLimit);

			for (int i=0;i<inputFields.length;i++)
			{
			    RssInputField field = inputFields[i];
			    
				rep.saveStepAttribute(id_transformation, id_step, i, "field_name",          field.getName());
				rep.saveStepAttribute(id_transformation, id_step, i, "fied_column",         field.getColumnCode());
				rep.saveStepAttribute(id_transformation, id_step, i, "field_type",          field.getTypeDesc());
				rep.saveStepAttribute(id_transformation, id_step, i, "field_format",        field.getFormat());
				rep.saveStepAttribute(id_transformation, id_step, i, "field_currency",      field.getCurrencySymbol());
				rep.saveStepAttribute(id_transformation, id_step, i, "field_decimal",       field.getDecimalSymbol());
				rep.saveStepAttribute(id_transformation, id_step, i, "field_group",         field.getGroupSymbol());
				rep.saveStepAttribute(id_transformation, id_step, i, "field_length",        field.getLength());
				rep.saveStepAttribute(id_transformation, id_step, i, "field_precision",     field.getPrecision());
				rep.saveStepAttribute(id_transformation, id_step, i, "field_trim_type",     field.getTrimTypeCode());
				rep.saveStepAttribute(id_transformation, id_step, i, "field_repeat",        field.isRepeated());

			}
		}
		catch(Exception e)
		{
			throw new KettleException(Messages.getString("RssInputMeta.Exception.ErrorSavingToRepository", ""+id_step), e);
		}
	}
	


	public StepDataInterface getStepData()
	{
		return new RssInputData();
	}
    public boolean supportsErrorHandling()
    {
        return true;
    }
    public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev, String input[], String output[], RowMetaInterface info)
	{
		CheckResult cr;
		
		if(urlInField)
		{
			 if (Const.isEmpty(getUrlFieldname()))
			 {
				 cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("RssInputMeta.CheckResult.NoField"), stepMeta);
				 remarks.add(cr); 
			 }
			 else
			 {
				 cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("RssInputMeta.CheckResult.FieldOk"), stepMeta);
				 remarks.add(cr); 
			 }		 
		}
		else
		{
			if (getUrl()==null || getUrl().length==0)
			{
				cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("RssInputMeta.CheckResult.NoUrl"), stepMeta);
				remarks.add(cr);
			}
			else
			{
				cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("RssInputMeta.CheckResult.UrlOk", ""+ getUrl().length), stepMeta);
				remarks.add(cr);
			}	
		}	

		
	}
    
    public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta tr, Trans trans)
	{
		return new RssInput(stepMeta, stepDataInterface, cnr, tr, trans);
	}


}