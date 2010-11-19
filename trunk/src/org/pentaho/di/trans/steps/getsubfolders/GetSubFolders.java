/* Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Samatar.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.*/

package org.pentaho.di.trans.steps.getsubfolders;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.apache.commons.vfs.FileObject;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.fileinput.FileInputList;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

/**
 * Read all subfolder inside a specified folder and convert them to rows and writes these to one or more output streams.
 * 
 * @author Samatar
 * @since 18-July-2008
 */
public class GetSubFolders extends BaseStep implements StepInterface
{
    private GetSubFoldersMeta meta;

    private GetSubFoldersData data;

    public GetSubFolders(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
    {
        super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
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

    public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
    {

		if(meta.isFoldernameDynamic() &&  (data.filenr >= data.filessize)){
			// Grab one row from previous step ...
			data.readrow=getRow();
		}
			
       if (first)
       {	        	
        first = false;
            
        if(meta.isFoldernameDynamic())
        {
			data.inputRowMeta = getInputRowMeta();
			data.outputRowMeta = data.inputRowMeta.clone();
	        meta.getFields(data.outputRowMeta, getStepname(), null, null, this);

            // Get total previous fields
            data.totalpreviousfields=data.inputRowMeta.size();

        	// Check is filename field is provided
			if (Const.isEmpty(meta.getDynamicFoldernameField()))
			{
				logError(Messages.getString("GetSubFolders.Log.NoField"));
				throw new KettleException(Messages.getString("GetSubFolders.Log.NoField"));
			}
			
			// cache the position of the field			
			if (data.indexOfFoldernameField<0)
			{	
				String realDynamicFoldername=environmentSubstitute(meta.getDynamicFoldernameField());
				data.indexOfFoldernameField =data.inputRowMeta.indexOfValue(realDynamicFoldername);
				if (data.indexOfFoldernameField<0)
				{
					// The field is unreachable !
					logError(Messages.getString("GetSubFolders.Log.ErrorFindingField")+ "[" + realDynamicFoldername+"]"); //$NON-NLS-1$ //$NON-NLS-2$
					throw new KettleException(Messages.getString("GetSubFolders.Exception.CouldnotFindField",realDynamicFoldername)); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}  
        }else
        {
			 // Create the output row meta-data
            data.outputRowMeta = new RowMeta();
            meta.getFields(data.outputRowMeta, getStepname(), null, null, this); // get the metadata populated
            //data.nrStepFields=  data.outputRowMeta.size();

            data.files = meta.getFolderList(this);
            data.filessize=data.files.nrOfFiles();
			handleMissingFiles();
            
        }
        data.nrStepFields=  data.outputRowMeta.size();
        
	  }// end if first
	 if(meta.isFoldernameDynamic())
	 {
		if (data.readrow==null) {
            setOutputDone();
            return false;
        }
	 }else{
		if (data.filenr >= data.filessize) {
            setOutputDone();
            return false;
        }
	 }
		
       try
       {
        	Object[] outputRow = buildEmptyRow();
        	int outputIndex = 0;
			Object extraData[] = new Object[data.nrStepFields];
        	if(meta.isFoldernameDynamic())
        	{
    			if (data.filenr >= data.filessize)
    		    {
    				// Get value of dynamic filename field ...
    	    		String filename=getInputRowMeta().getString(data.readrow,data.indexOfFoldernameField);

    	    		String[] filesname={filename};
    		      	String[] filesrequired={GetSubFoldersMeta.NO};
    		      	// Get files list
    		      	data.files = meta.getDynamicFolderList(getTransMeta(), filesname, filesrequired);
    		      	data.filessize=data.files.nrOfFiles();
    		      	data.filenr=0;
    		     }
        		
        		// Clone current input row
    			outputRow = data.readrow.clone();
        	}
        	if(data.filessize>0)
        	{
	        	data.file = data.files.getFile(data.filenr);
            	
                // filename
        		extraData[outputIndex++]=KettleVFS.getFilename(data.file);

                // short_filename
        		extraData[outputIndex++]=data.file.getName().getBaseName();

                try
                {
    				 // Path
                	 extraData[outputIndex++]=KettleVFS.getFilename(data.file.getParent());
                    
                     // ishidden
    				 extraData[outputIndex++]=Boolean.valueOf(data.file.isHidden());

                     // isreadable
    				 extraData[outputIndex++]=Boolean.valueOf(data.file.isReadable());
    				
                     // iswriteable
    				 extraData[outputIndex++]=Boolean.valueOf(data.file.isWriteable());

                     // lastmodifiedtime
    				 extraData[outputIndex++]=new Date( data.file.getContent().getLastModifiedTime() );
   				 	
                }
                catch (IOException e)
                {
                    throw new KettleException(e);
                }

                 // uri	
				 extraData[outputIndex++]= data.file.getName().getURI();
   	
                 // rooturi	
				 extraData[outputIndex++]= data.file.getName().getRootURI();
				 
	            // childrens files
	        	extraData[outputIndex++]=new Long(data.file.getChildren().length);
		
		         // See if we need to add the row number to the row...  
		         if (meta.includeRowNumber() && !Const.isEmpty(meta.getRowNumberField()))
		         {
					  extraData[outputIndex++]= new Long(data.rownr);
		         }
		
		         data.rownr++;
		        // Add row data
		        outputRow = RowDataUtil.addRowData(outputRow,data.totalpreviousfields, extraData);
                // Send row
		        putRow(data.outputRowMeta, outputRow);
		        
	      		if (meta.getRowLimit()>0 && data.rownr>=meta.getRowLimit())  // limit has been reached: stop now.
	      		{
	   	           setOutputDone();
	   	           return false;
	      		}
	      		
            }
        }
        catch (Exception e)
        {
            throw new KettleStepException(e);
        }

        data.filenr++;

        if (checkFeedback(getLinesInput())) 	
        {
        	if(log.isBasic()) logBasic(Messages.getString("GetSubFolders.Log.NrLine",""+getLinesInput()));
        }

        return true;
    }

    private void handleMissingFiles() throws KettleException
    {
        List<FileObject> nonExistantFiles = data.files.getNonExistantFiles();

        if (nonExistantFiles.size() != 0)
        {
            String message = FileInputList.getRequiredFilesDescription(nonExistantFiles);
            logError(Messages.getString("GetSubFolders.Error.MissingFiles",message));
            throw new KettleException(Messages.getString("GetSubFolders.Exception.MissingFiles",message));
        }

        List<FileObject> nonAccessibleFiles = data.files.getNonAccessibleFiles();
        if (nonAccessibleFiles.size() != 0)
        {
            String message = FileInputList.getRequiredFilesDescription(nonAccessibleFiles);
            logError(Messages.getString("GetSubFolders.Error.NoAccessibleFiles",message));
            throw new KettleException(Messages.getString("GetSubFolders.Exception.NoAccessibleFiles",message));
        }
    }

    public boolean init(StepMetaInterface smi, StepDataInterface sdi)
    {
        meta = (GetSubFoldersMeta) smi;
        data = (GetSubFoldersData) sdi;

        if (super.init(smi, sdi))
        {
			try
			{
	            data.filessize=0;
	            data.rownr = 1L;
				data.filenr = 0;
				data.totalpreviousfields=0;   
			}
			catch(Exception e)
			{
				logError("Error initializing step: "+e.toString());
				logError(Const.getStackTracker(e));
				return false;
			}
		
            return true;
          
        }
        return false;
    }

    public void dispose(StepMetaInterface smi, StepDataInterface sdi)
    {
        meta = (GetSubFoldersMeta) smi;
        data = (GetSubFoldersData) sdi;
        if(data.file!=null){
        	try{
        	    	data.file.close();
        	    	data.file=null;
        	}catch(Exception e){}
        	
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