/*************************************************************************************** 
 * Copyright (C) 2007 Samatar, Brahim.  All rights reserved. 
 * This software was developed by Samatar, Brahim and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. A copy of the license, 
 * is included with the binaries and source code. The Original Code is Samatar, Brahim.  
 * The Initial Developer is Samatar, Brahim.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an 
 * "AS IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. 
 * Please refer to the license for the specific language governing your rights 
 * and limitations.
 ***************************************************************************************/
 

package org.pentaho.di.trans.steps.ldapinput;

import java.util.Hashtable;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.InitialDirContext;

import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
/**
 * Read LDAP Host, convert them to rows and writes these to one or more output streams.
 * 
 * @author Samatar
 * @since 21-09-2007
 */
public class LDAPInput extends BaseStep implements StepInterface
{
	private LDAPInputMeta meta;
	private LDAPInputData data;

	
	public LDAPInput(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
        if (first)
        {
            first = false;
            // Create the output row meta-data
            data.outputRowMeta = new RowMeta();
            meta.getFields(data.outputRowMeta, getStepname(), null, null, this); // get the metadata populated
            
            // Create convert meta-data objects that will contain Date & Number formatters
            //
            data.convertRowMeta = data.outputRowMeta.clone();
            for (int i=0;i<data.convertRowMeta.size();i++) data.convertRowMeta.getValueMeta(i).setType(ValueMetaInterface.TYPE_STRING);


            // For String to <type> conversions, we allocate a conversion meta data row as well...
			//
			data.convertRowMeta = data.outputRowMeta.clone();
			for (int i=0;i<data.convertRowMeta.size();i++) {
				data.convertRowMeta.getValueMeta(i).setType(ValueMetaInterface.TYPE_STRING);           
			}
   
			// Try to connect to LDAP server
            connectServerLdap();
           
	        // Get multi valued field separator
	        data.multi_valuedFieldSeparator=environmentSubstitute(meta.getMultiValuedSeparator()) ;
	        
	        data.nrfields = meta.getInputFields().length;
	 
        }
	
        Object[] outputRowData=null;
        boolean sendToErrorRow=false;
		String errorMessage = null;
		try 
		{	
			outputRowData =getOneRow();
			
			if(outputRowData==null)
			{
				setOutputDone();
				return false;
			}
		
			putRow(data.outputRowMeta, outputRowData);  // copy row to output rowset(s);
		    
			if(log.isDebug()) log.logDebug(Messages.getString("LDAPInput.log.ReadRow"), outputRowData.toString());
			
		    if (checkFeedback(getLinesInput()))
		    {
		    	if(log.isDetailed()) logDetailed(Messages.getString("LDAPInput.log.LineRow") + getLinesInput());
		    }
		    
		    return true; 
		} 
		catch(Exception e)
		{
			if (getStepMeta().isDoingErrorHandling())
			{
		         sendToErrorRow = true;
		         errorMessage = e.toString();
			}
			else
			{
				logError(Messages.getString("LDAPInput.log.Exception", e.getMessage()));
				setErrors(1);
				stopAll();
				setOutputDone();  // signal end to receiver(s)
				return false;				
			}
			if (sendToErrorRow)
			{
			   // Simply add this row to the error row
			   putError(getInputRowMeta(), outputRowData, 1, errorMessage, null, "LDAPINPUT001");
			}
		} 
		return true;
	}		
	private Object[] getOneRow()  throws KettleException
	{

		while (!data.results.hasMoreElements())
		{
	      return null;
		} 

		 // Build an empty row based on the meta-data		  
		 Object[] outputRowData=buildEmptyRow();

		 try{	
			 SearchResult searchResult = (SearchResult) data.results.next();
             Attributes attributes = searchResult.getAttributes();     	
             
             if(attributes!=null)
             {
				// Execute for each Input field...
				for (int i=0;i<meta.getInputFields().length;i++)
				{
					// Get attribute value
					
					String attrvalue =null;
					Attribute attr = attributes.get(environmentSubstitute(meta.getInputFields()[i].getAttribute())); 
	                if (attr!=null) 
	                {
	                	//Let's try to get all values of this attribute
	                	
	                	StringBuilder attrStr = new StringBuilder();
						for (NamingEnumeration<?> eattr = attr.getAll() ; eattr.hasMore();) 
						{
							if (attrStr.length() > 0) {
								attrStr.append(data.multi_valuedFieldSeparator);
							}
							attrStr.append(eattr.next().toString());
						}
						attrvalue=attrStr.toString(); 	
						
						// DO Trimming!
						switch (meta.getInputFields()[i].getTrimType())
						{
						case LDAPInputField.TYPE_TRIM_LEFT:
							attrvalue = Const.ltrim(attrvalue);
							break;
						case LDAPInputField.TYPE_TRIM_RIGHT:
							attrvalue = Const.rtrim(attrvalue);
							break;
						case LDAPInputField.TYPE_TRIM_BOTH:
							attrvalue = Const.trim(attrvalue);
							break;
						default:
							break;
						}
							      
	                }
					// DO CONVERSIONS...
					//
					ValueMetaInterface targetValueMeta = data.outputRowMeta.getValueMeta(i);
					ValueMetaInterface sourceValueMeta = data.convertRowMeta.getValueMeta(i);
					outputRowData[i] = targetValueMeta.convertData(sourceValueMeta, attrvalue);
            
                
					// Do we need to repeat this field if it is null?
					if (meta.getInputFields()[i].isRepeated())
					{
						if (data.previousRow!=null && Const.isEmpty(attrvalue))
						{
							outputRowData[i] = data.previousRow[i];
						}
					}
	
				}    // End of loop over fields...

		        // See if we need to add the row number to the row...  
		        if (meta.includeRowNumber() && !Const.isEmpty(meta.getRowNumberField()))
		        {
		            outputRowData[ data.nrfields] = new Long(data.rownr);
		        }
		        
				RowMetaInterface irow = getInputRowMeta();
				
				data.previousRow = irow==null?outputRowData:(Object[])irow.cloneRow(outputRowData); // copy it to make
				// surely the next step doesn't change it in between...
				data.rownr++;
				
				incrementLinesInput();
             }         
			
		 }
		 catch (Exception e)
		 {
			throw new KettleException(Messages.getString("LDAPInput.Exception.CanNotReadLDAP"), e);
		 }
		return outputRowData;
	}
	 
	 public void connectServerLdap() throws KettleException {
		 //TODO : Add SSL Authentication
			/*
			//---
			//SSL
	
			// Dynamically set JSSE as a security provider
			Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
	
			// Dynamically set the property that JSSE uses to identify
			// the keystore that holds trusted root certificates
			String certifPath=System.getProperty("user.dir")+"\\certificats\\"+rb.getString("certificat").trim();
			System.setProperty("javax.net.ssl.trustStore", certifPath);*/
		 try
		 {
			String port=environmentSubstitute(meta.getPort());
			int portint=Const.toInt(port, 389);
			String hostname=environmentSubstitute(meta.getHost());
			String username=environmentSubstitute(meta.getUserName());
			String password=environmentSubstitute(meta.getPassword());
	        //Set the filter string.  The more exact of the search string
			String filter=environmentSubstitute(meta.getFilterString());
			//Set the Search base.This is the place where the search will
			String searchbase=environmentSubstitute(meta.getSearchBase());
			 
			 
	        Hashtable<String, String> env = new Hashtable<String, String>();

	        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
	        if(hostname.indexOf("ldap://")>=0)
	        	env.put(Context.PROVIDER_URL,hostname + ":" + portint);
	        else
	        	env.put(Context.PROVIDER_URL, "ldap://"+hostname + ":" + portint);
	        env.put(Context.SECURITY_AUTHENTICATION, "simple" );
	        // TODO : Add referral handling
	        if (meta.UseAuthentication())
	        {
	        	env.put(Context.SECURITY_PRINCIPAL, username);
	        	env.put(Context.SECURITY_CREDENTIALS, password); 
	        }

	       data.ctx=new InitialDirContext(env);
		  
	       if (data.ctx==null)
		   {
			   logError(Messages.getString("LDAPInput.Error.UnableToConnectToServer"));
			   throw new KettleException(Messages.getString("LDAPInput.Error.UnableToConnectToServer"));
		   }
	       if(log.isBasic()) logBasic(Messages.getString("LDAPInput.Log.ConnectedToServer",hostname,username));
		   if (log.isDetailed()) logDetailed(Messages.getString("LDAPInput.ClassUsed.Message",data.ctx.getClass().getName()));
		   // Get the schema tree root
		   //DirContext schema = data.ctx.getSchema("");  
		   //if (log.isDetailed()) logDetailed(Messages.getString("LDAPInput.SchemaList.Message",""+schema.list(""))); 
	       
		   SearchControls controls = new SearchControls();
		   controls.setCountLimit(meta.getRowLimit());
		    
		   // Time Limit
		   if(meta.getTimeLimit()>0)   controls.setTimeLimit(meta.getTimeLimit() * 1000);

		   // Limit returned attributes to user selection
		   String[] attrReturned = new String [meta.getInputFields().length];
		   // Get user selection attributes
		   for (int i=0;i<meta.getInputFields().length;i++)
		   {
		    	attrReturned[i]=environmentSubstitute(meta.getInputFields()[i].getAttribute());
			
		   }
		   
		   controls.setReturningAttributes(attrReturned);
		     
		     
		   if(Const.isEmpty(searchbase))
		   {
			    // get Search Base
			    Attributes attrs = data.ctx.getAttributes("", new String[] { "namingContexts" });
				Attribute attr = attrs.get("namingContexts");
				  
				searchbase=attr.get().toString();
				if(log.isBasic()) logBasic(Messages.getString("LDAPInput.SearchBaseFound",searchbase) );
		    } 
		
	        controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
	         
	        data.results = data.ctx.search(searchbase,filter.replace("\n\r", "").replace("\n", ""), controls);
	        
		 }catch (Exception e)
		 {
			 throw new KettleException(Messages.getString("LDAPinput.Exception.ErrorConnecting", e.getMessage()));
		 }
	    }
	
	/**
	 * Build an empty row based on the meta-data...
	 * 
	 * @return
	 */

	private Object[] buildEmptyRow()
	{
        Object[] rowData = RowDataUtil.allocateRowData(data.outputRowMeta.size());
 
		 return rowData;
	}


	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(LDAPInputMeta)smi;
		data=(LDAPInputData)sdi;
		
		if (super.init(smi, sdi))
		{
			data.rownr = 1L;
			
			return true;
		}
		return false;
	}
	
	public void dispose(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(LDAPInputMeta)smi;
		data=(LDAPInputData)sdi;
		if(data.ctx!=null)
		{
			try
			{
				data.ctx.close();
				if(data.results!=null) data.results=null;
				if(log.isBasic()) log.logBasic(toString(),Messages.getString("LDAPInput.log.Disconnection.Done"));
			}
			catch (Exception e)
			{
	             logError(Messages.getString("LDAPInput.Exception.ErrorDisconecting",e.toString()));
	             logError(Const.getStackTracker(e));
			}
			
		}
		super.dispose(smi, sdi);
	}
	
	//
	// Run is were the action happens!
	public void run()
	{
    	BaseStep.runStepThread(this, meta, data);
	}	
}
