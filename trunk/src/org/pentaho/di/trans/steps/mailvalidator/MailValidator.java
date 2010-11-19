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


package org.pentaho.di.trans.steps.mailvalidator;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

/**
 * Check if an email address is valid
 *  *  
 * @author Samatar
 * @since 03-Juin-2008
 *
 */

public class MailValidator extends BaseStep implements StepInterface
{
    private MailValidatorMeta meta;
    private MailValidatorData data;
    
    public MailValidator(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
    {
        super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
    }
    
   
    public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
    {
        meta=(MailValidatorMeta)smi;
        data=(MailValidatorData)sdi;

        Object[] r = getRow();      // Get row from input rowset & set row busy!
        if (r==null)  // no more input to be expected...
        {
            setOutputDone();
            return false;
        }
          
        if(first) 
        {
	    	first=false;
	    		
			// get the RowMeta
			data.previousRowMeta = getInputRowMeta().clone();
			data.NrPrevFields=data.previousRowMeta.size();
			data.outputRowMeta = getInputRowMeta().clone();
			meta.getFields(data.outputRowMeta, getStepname(), null, null, this);
    		
    		// check result fieldname
    		data.realResultFieldName=environmentSubstitute(meta.getResultFieldName());
    		if(Const.isEmpty(data.realResultFieldName))
    			throw new KettleException(Messages.getString("MailValidator.Error.ResultFieldNameMissing"));
    		
	    		
    		if(meta.isResultAsString()) 
    		{
    			if(Const.isEmpty(meta.getEMailValideMsg()))
    				throw new KettleException(Messages.getString("MailValidator.Error.EMailValidMsgMissing"));
    			
    			if(Const.isEmpty(meta.getEMailNotValideMsg()))
    				throw new KettleException(Messages.getString("MailValidator.Error.EMailNotValidMsgMissing"));
    			
    			data.msgValidMail=environmentSubstitute(meta.getEMailValideMsg());
    			data.msgNotValidMail=environmentSubstitute(meta.getEMailNotValideMsg());
    		}
    		
	    		
    		// Check is email address field is provided
			if (Const.isEmpty(meta.getEmailField()))
				throw new KettleException(Messages.getString("MailValidator.Error.FilenameFieldMissing"));
			
    		data.realResultErrorsFieldName=environmentSubstitute(meta.getErrorsField());
    		
			// cache the position of the field			
			if (data.indexOfeMailField<0) {	
				data.indexOfeMailField =data.previousRowMeta.indexOfValue(meta.getEmailField());
				if (data.indexOfeMailField<0) 
				{
					// The field is unreachable !
					throw new KettleException(Messages.getString("MailValidator.Exception.CouldnotFindField",meta.getEmailField())); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
				
			// SMTP check?
			if(meta.isSMTPCheck()){
				if(meta.isdynamicDefaultSMTP()){
					if (Const.isEmpty(meta.getDefaultSMTP()))
						throw new KettleException(Messages.getString("MailValidator.Error.DefaultSMTPFieldMissing"));
					
					if (data.indexOfdefaultSMTPField<0)
					{	
						data.indexOfdefaultSMTPField =data.previousRowMeta.indexOfValue(meta.getDefaultSMTP());
						if (data.indexOfdefaultSMTPField<0){
							// The field is unreachable !
							throw new KettleException(Messages.getString("MailValidator.Exception.CouldnotFindField",meta.getDefaultSMTP())); //$NON-NLS-1$ //$NON-NLS-2$
						}
					}
				}
				// get Timeout
				data.timeout=Const.toInt(environmentSubstitute(meta.getTimeOut()), 0);
				
				// get email sender
				data.realemailSender=environmentSubstitute(meta.geteMailSender());
				
				// get default SMTP server
				data.realdefaultSMTPServer=environmentSubstitute(meta.getDefaultSMTP());
			}
				
        }// End If first 
        	
        boolean sendToErrorRow=false;
        String errorMessage = null;
        boolean mailvalid=false;
        String mailerror=null;
        
    	Object[] outputRow = RowDataUtil.allocateRowData(data.outputRowMeta.size());
		for (int i = 0; i < data.NrPrevFields; i++) 
		{
			outputRow[i] = r[i];
		}
   
    	try 
    	{
	        // get dynamic email address
	    	String emailaddress= data.previousRowMeta.getString(r,data.indexOfeMailField);
	
	    	if(!Const.isEmpty(emailaddress)) 
	    	{
	    		if(meta.isdynamicDefaultSMTP())
	    			data.realdefaultSMTPServer= data.previousRowMeta.getString(r,data.indexOfdefaultSMTPField);
	    		
	    		// Check if address is valid
	    		MailValidationResult result=MailValidation.isAddressValid(emailaddress,data.realemailSender,	
	    				data.realdefaultSMTPServer,data.timeout,meta.isSMTPCheck());
	    		// return result
	    		mailvalid=result.isValide();
	    		mailerror=result.getErrorMessage();
	    		
	    	}else 
	    		mailerror=Messages.getString("MailValidator.Error.MailEmpty");
	    	if(meta.isResultAsString())
	    	{
	    		if(mailvalid) 
	    			outputRow[data.NrPrevFields]= data.msgValidMail;
	    		else
	    			outputRow[data.NrPrevFields]=data.msgNotValidMail;
	    	} else
	    	{
	        	// add boolean result field
	    		outputRow[data.NrPrevFields]= mailvalid;
	    	}
	    	int rowIndex=data.NrPrevFields;
	    	rowIndex++;
			// add errors field
			if(!Const.isEmpty(data.realResultErrorsFieldName))
	    		outputRow[rowIndex]= mailerror;
			
			putRow(data.outputRowMeta, outputRow);  // copy row to output rowset(s);

			if (log.isRowLevel()) log.logRowlevel(toString(), Messages.getString("MailValidator.LineNumber",getLinesRead()+" : "+getInputRowMeta().getString(r)));
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
	            logError(Messages.getString("MailValidator.ErrorInStepRunning")+e.getMessage()); //$NON-NLS-1$
	            setErrors(1);
	            stopAll();
	            setOutputDone();  // signal end to receiver(s)
	            return false;
	           
	    	}
	    	if (sendToErrorRow)
	    	{
	    	   // Simply add this row to the error row
	    		putError(getInputRowMeta(), r, 1, errorMessage, meta.getResultFieldName(), "MailValidator001");
	    	}
	    }
            
        return true;
    }
    
    public boolean init(StepMetaInterface smi, StepDataInterface sdi)
    {
        meta=(MailValidatorMeta)smi;
        data=(MailValidatorData)sdi;

        if (super.init(smi, sdi))
        {
        	if(Const.isEmpty(meta.getResultFieldName()))
        	{
        		log.logError(toString(), Messages.getString("MailValidator.Error.ResultFieldMissing"));
        		return false;
        	}
            return true;
        }
        return false;
    }
        
    public void dispose(StepMetaInterface smi, StepDataInterface sdi)
    {
        meta = (MailValidatorMeta)smi;
        data = (MailValidatorData)sdi;
        
        super.dispose(smi, sdi);
    }
	  //
    //
    // Run is were the action happens!
	//
	// Run is were the action happens!
	public void run()
	{
    	BaseStep.runStepThread(this, meta, data);
	}
    
    public String toString()
    {
        return this.getClass().getName();
    }
   
	
}
