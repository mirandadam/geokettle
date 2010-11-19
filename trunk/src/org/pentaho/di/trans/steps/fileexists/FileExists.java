/*
 * Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
*/
package org.pentaho.di.trans.steps.fileexists;

import org.apache.commons.vfs.FileType;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.ResultFile;
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
 * Check if a table exists in a Database
 *  *  
 * @author Samatar
 * @since 03-Juin-2008
 *
 */

public class FileExists extends BaseStep implements StepInterface
{
    private FileExistsMeta meta;
    private FileExistsData data;
    
    public FileExists(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
    {
        super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
    }
    
   
    public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
    {
        meta=(FileExistsMeta)smi;
        data=(FileExistsData)sdi;
        
        boolean sendToErrorRow=false;
        String errorMessage = null;

        Object[] r = getRow();      // Get row from input rowset & set row busy!
        if (r==null)  // no more input to be expected...
        {
            setOutputDone();
            return false;
        }
           
        boolean fileexists=false;
        String filetype=null;
        
        try
        {
        	if(first)
        	{
        		first=false;
    			// get the RowMeta
    			data.previousRowMeta = getInputRowMeta().clone();
    			data.NrPrevFields=data.previousRowMeta.size();
    			data.outputRowMeta = data.previousRowMeta;
    			meta.getFields(data.outputRowMeta, getStepname(), null, null, this);
    			
        		// Check is tablename field is provided
				if (Const.isEmpty(meta.getDynamicFilenameField()))
				{
					logError(Messages.getString("FileExists.Error.FilenameFieldMissing"));
					throw new KettleException(Messages.getString("FileExists.Error.FilenameFieldMissing"));
				}
				
				// cache the position of the field			
				if (data.indexOfFileename<0)
				{	
					data.indexOfFileename =data.previousRowMeta.indexOfValue(meta.getDynamicFilenameField());
					if (data.indexOfFileename<0)
					{
						// The field is unreachable !
						logError(Messages.getString("FileExists.Exception.CouldnotFindField")+ "[" + meta.getDynamicFilenameField()+"]"); //$NON-NLS-1$ //$NON-NLS-2$
						throw new KettleException(Messages.getString("FileExists.Exception.CouldnotFindField",meta.getDynamicFilenameField())); //$NON-NLS-1$ //$NON-NLS-2$
					}
				}
        	}// End If first 
        	
        	Object[] outputRow = RowDataUtil.allocateRowData(data.outputRowMeta.size());
    		for (int i = 0; i < data.NrPrevFields; i++)
    		{
    			outputRow[i] = r[i];
    		}
        	// get filename
        	String filename= data.previousRowMeta.getString(r,data.indexOfFileename);
        	if(!Const.isEmpty(filename))
        	{
        		data.file=KettleVFS.getFileObject(filename);
        	
        		// Check if file
        		fileexists=data.file.exists();
        		
        		// include file type?
        		if(meta.includeFileType() && fileexists && !Const.isEmpty(meta.getFileTypeFieldName())) 
        			filetype=data.file.getType().toString();
        		
        		// add filename to result filenames?
        		if(meta.addResultFilenames() && fileexists && data.file.getType()==FileType.FILE)
        		{
        			// Add this to the result file names...
        			ResultFile resultFile = new ResultFile(ResultFile.FILE_TYPE_GENERAL, data.file, getTransMeta().getName(), getStepname());
        			resultFile.setComment(Messages.getString("FileExists.Log.FileAddedResult"));
        			addResultFile(resultFile);
        			
        			if(log.isDetailed()) log.logDetailed(toString(), Messages.getString("FileExists.Log.FilenameAddResult",data.file.toString()));
        		}
        	}
        	
        	// Add result field to input stream
    		outputRow[data.NrPrevFields]= fileexists;
    		int rowIndex=data.NrPrevFields;
    		rowIndex++;
        	
            if(meta.includeFileType() && !Const.isEmpty(meta.getFileTypeFieldName())) 
            	outputRow[rowIndex]=filetype;

			 //	add new values to the row.
	        putRow(data.outputRowMeta, outputRow);  // copy row to output rowset(s);

	        if (log.isRowLevel()) log.logRowlevel(toString(), Messages.getString("FileExists.LineNumber",getLinesRead()+" : "+getInputRowMeta().getString(r)));
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
	            logError(Messages.getString("FileExists.ErrorInStepRunning")+e.getMessage()); //$NON-NLS-1$
	            setErrors(1);
	            stopAll();
	            setOutputDone();  // signal end to receiver(s)
	            return false;
        	}
        	if (sendToErrorRow)
        	{
        	   // Simply add this row to the error row
        	   putError(getInputRowMeta(), r, 1, errorMessage, meta.getResultFieldName(), "FileExistsO01");
        	}
        }
            
        return true;
    }
    
    public boolean init(StepMetaInterface smi, StepDataInterface sdi)
    {
        meta=(FileExistsMeta)smi;
        data=(FileExistsData)sdi;

        if (super.init(smi, sdi))
        {
        	if(Const.isEmpty(meta.getResultFieldName()))
        	{
        		log.logError(toString(), Messages.getString("FileExists.Error.ResultFieldMissing"));
        		return false;
        	}
            return true;
        }
        return false;
    }
        
    public void dispose(StepMetaInterface smi, StepDataInterface sdi)
    {
        meta = (FileExistsMeta)smi;
        data = (FileExistsData)sdi;
        if(data.file!=null)
        {
        	try{
        	    	data.file.close();
        	    	data.file=null;
        	}catch(Exception e){}
        	
        }
        super.dispose(smi, sdi);
    }

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
