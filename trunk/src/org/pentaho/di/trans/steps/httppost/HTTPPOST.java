 /* Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Samatar Hassan and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Samatar Hassan.
 * The Initial Developer is Samatar Hassan.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.*/
 
package org.pentaho.di.trans.steps.httppost;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import java.io.ByteArrayInputStream;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.NameValuePair;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.core.Const;



/**
 * Retrieves values from a database by calling database stored procedures or functions
 *  
 * @author Samatar
 * @since 15-jan-2009
 *
 */

public class HTTPPOST extends BaseStep implements StepInterface
{
	private HTTPPOSTMeta meta;
	private HTTPPOSTData data;
	
	public HTTPPOST(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
	

	
	private Object[] callHTTPPOST(Object[] rowData) throws KettleException
    {
		// get dynamic url ?
		if(meta.isUrlInField()) data.realUrl=data.inputRowMeta.getString(rowData,data.indexOfUrlField);
 
      	try
        {
      		if(log.isDetailed()) logDetailed(Messages.getString("HTTPPOST.Log.ConnectingToURL",data.realUrl));
      		
            // Prepare HTTP POST
            // 
            HttpClient HTTPPOSTclient = new HttpClient();
            PostMethod post = new PostMethod(data.realUrl);
            //post.setFollowRedirects(false); 
            
            // Specify content type and encoding
            // If content encoding is not explicitly specified
            // ISO-8859-1 is assumed
            if(Const.isEmpty(data.realEncoding))
            	post.setRequestHeader("Content-type", "text/xml");
            else
            	post.setRequestHeader("Content-type", "text/xml; "+data.realEncoding);
            
            
            // BODY PARAMETERS
            if(data.useBodyParameters)
	         {
	            // set body parameters that we want to send 
		        for (int i=0;i<data.body_parameters_nrs.length;i++)
		        {
		        	data.bodyParameters[i].setValue(data.inputRowMeta.getString(rowData,data.body_parameters_nrs[i]));
		        }
	            post.setRequestBody(data.bodyParameters);
	         }

            // QUERY PARAMETERS
            if(data.useQueryParameters)
            {
            	 for (int i=0;i<data.query_parameters_nrs.length;i++)
 		         {
 		        	data.queryParameters[i].setValue(data.inputRowMeta.getString(rowData,data.query_parameters_nrs[i]));
 		         }
            	 post.setQueryString(data.queryParameters); 
            }

            // Set request entity?
            if(data.indexOfRequestEntity>=0)
            {
            	String tmp=data.inputRowMeta.getString(rowData,data.indexOfRequestEntity);
                // Request content will be retrieved directly
                // from the input stream
                // Per default, the request content needs to be buffered
                // in order to determine its length.
                // Request body buffering can be avoided when
                // content length is explicitly specified
            	
            	if(meta.isPostAFile())
            	{
     		       File input = new File(tmp);
     		       post.setRequestEntity(new InputStreamRequestEntity(new FileInputStream(input), input.length()));
            	}
            	else
            	{
            		post.setRequestEntity(new InputStreamRequestEntity(new ByteArrayInputStream(tmp.getBytes()), tmp.length())); 
            	}
            }
            
            // Execute request
            // 
            InputStream inputStream=null;
            try
            {
            	// Execute the POST method
                int statusCode = HTTPPOSTclient.executeMethod(post);
                
                // Display status code
                if(log.isDebug()) log.logDebug(toString(), Messages.getString("HTTPPOST.Log.ResponseCode",""+statusCode));
                String body=null;
                if( statusCode != -1 )
                {
	                // the response
	                inputStream = post.getResponseBodyAsStream();
	                StringBuffer bodyBuffer = new StringBuffer();
	                int c;
	                while ( (c=inputStream.read())!=-1) bodyBuffer.append((char)c);
	                inputStream.close();
	                
	                // Display response
	                body = bodyBuffer.toString();
	                
	                if(log.isDebug()) log.logDebug(toString(), Messages.getString("HTTPPOST.Log.ResponseBody",body));
                }
                //return new Value(meta.getFieldName(), body);
                return RowDataUtil.addValueData(rowData, data.inputRowMeta.size(), body);
            }
            finally
            {
            	if(inputStream!=null) inputStream.close(); 
                // Release current connection to the connection pool once you are done
            	post.releaseConnection();
            }
        }
        catch(Exception e)
        {
            throw new KettleException(Messages.getString("HTTPPOST.Error.CanNotReadURL",data.realUrl), e);

        }
    }
    public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		meta=(HTTPPOSTMeta)smi;
		data=(HTTPPOSTData)sdi;
		
		 boolean sendToErrorRow=false;
		 String errorMessage = null;

		Object[] r=getRow();       // Get row from input rowset & set row busy!
		if (r==null)  // no more input to be expected...
		{
			setOutputDone();
			return false;
		}
		if ( first )
		{
			first=false;
			data.inputRowMeta = getInputRowMeta();
			data.outputRowMeta=getInputRowMeta().clone();
			meta.getFields(data.outputRowMeta, getStepname(), null, null, this);
			
			if(meta.isUrlInField())
			{
				if(Const.isEmpty(meta.getUrlField()))
				{
					logError(Messages.getString("HTTPPOST.Log.NoField"));
					throw new KettleException(Messages.getString("HTTPPOST.Log.NoField"));
				}
				
				// cache the position of the field			
				if (data.indexOfUrlField<0)
				{	
					String realUrlfieldName=environmentSubstitute(meta.getUrlField());
					data.indexOfUrlField =data.inputRowMeta.indexOfValue((realUrlfieldName));
					if (data.indexOfUrlField<0)
					{
						// The field is unreachable !
						logError(Messages.getString("HTTPPOST.Log.ErrorFindingField",realUrlfieldName)); 
						throw new KettleException(Messages.getString("HTTPPOST.Exception.ErrorFindingField",realUrlfieldName)); 
					}
				}
			}else
			{
				data.realUrl=environmentSubstitute(meta.getUrl());
			}
			// set body parameters
			int nrargs=meta.getArgumentField().length;
			if(nrargs>0)
			{
				data.useBodyParameters=true;
				data.body_parameters_nrs=new int[nrargs];
				data.bodyParameters = new NameValuePair[nrargs];
				for (int i=0;i<nrargs;i++)
				{
					data.body_parameters_nrs[i]=data.inputRowMeta.indexOfValue(meta.getArgumentField()[i]);
					if (data.body_parameters_nrs[i]<0)
					{
						logError(Messages.getString("HTTPPOST.Log.ErrorFindingField")+meta.getArgumentField()[i]+"]"); //$NON-NLS-1$ //$NON-NLS-2$
						throw new KettleStepException(Messages.getString("HTTPPOST.Exception.CouldnotFindField",meta.getArgumentField()[i])); //$NON-NLS-1$ //$NON-NLS-2$
					}
					data.bodyParameters[i]= new NameValuePair(environmentSubstitute(meta.getArgumentParameter()[i]),
							data.outputRowMeta.getString(r,data.body_parameters_nrs[i]));
				}
			}
			// set query parameters
			int nrQuery=meta.getQueryField().length;
			if(nrQuery>0)
			{
				data.useQueryParameters=true;
				data.query_parameters_nrs=new int[nrQuery];
				data.queryParameters = new NameValuePair[nrQuery];
				for (int i=0;i<nrQuery;i++)
				{
					data.query_parameters_nrs[i]=data.inputRowMeta.indexOfValue(meta.getQueryField()[i]);
					if (data.query_parameters_nrs[i]<0)
					{
						logError(Messages.getString("HTTPPOST.Log.ErrorFindingField")+meta.getQueryField()[i]+"]"); //$NON-NLS-1$ //$NON-NLS-2$
						throw new KettleStepException(Messages.getString("HTTPPOST.Exception.CouldnotFindField",meta.getQueryField()[i])); //$NON-NLS-1$ //$NON-NLS-2$
					}
					data.queryParameters[i]= new NameValuePair(environmentSubstitute(meta.getQueryParameter()[i]),
							data.outputRowMeta.getString(r,data.query_parameters_nrs[i]));
				}
			}
			// set request entity?
			if(!Const.isEmpty(meta.getRequestEntity()))
			{
				data.indexOfRequestEntity=data.inputRowMeta.indexOfValue(environmentSubstitute(meta.getRequestEntity()));
				if (data.indexOfRequestEntity<0)
				{
					throw new KettleStepException(Messages.getString("HTTPPOST.Exception.CouldnotFindRequestEntityField",meta.getRequestEntity())); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
			data.realEncoding=environmentSubstitute(meta.getEncoding());	
		} // end if first
		
		try
		{
	        Object[] outputRowData = callHTTPPOST(r);
        	putRow(data.outputRowMeta, outputRowData);  // copy row to output rowset(s);
			
            if (checkFeedback(getLinesRead())) 
            {
            	if(log.isDetailed()) logDetailed(Messages.getString("HTTPPOST.LineNumber")+getLinesRead()); //$NON-NLS-1$
            }
		}
		catch(KettleException e)
		{
			if (getStepMeta().isDoingErrorHandling())
			{
		         sendToErrorRow = true;
		         errorMessage = e.toString();
			}
			else
			{
				logError(Messages.getString("HTTPPOST.ErrorInStepRunning")+e.getMessage()); //$NON-NLS-1$
				setErrors(1);
                logError(Const.getStackTracker(e));
				stopAll();
				setOutputDone();  // signal end to receiver(s)
				return false;
			}

			if (sendToErrorRow)
			{
			   // Simply add this row to the error row
			   putError(getInputRowMeta(), r, 1, errorMessage, null, "HTTPPOST001");
			}

		}
			
		return true;
	}
	
	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(HTTPPOSTMeta)smi;
		data=(HTTPPOSTData)sdi;

		if (super.init(smi, sdi))
		{
		    return true;
		}
		return false;
	}
		
	public void dispose(StepMetaInterface smi, StepDataInterface sdi)
	{
	    meta = (HTTPPOSTMeta)smi;
	    data = (HTTPPOSTData)sdi;
	    
	    super.dispose(smi, sdi);
	}

	//
	// Run is were the action happens!
	public void run()
	{
    	BaseStep.runStepThread(this, meta, data);
	}
}
