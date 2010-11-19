 /* Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.*/
 

package org.pentaho.di.trans.steps.getfilesrowscount;

import java.io.BufferedInputStream;
import java.io.InputStreamReader;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileType;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.ResultFile;
import org.pentaho.di.core.exception.KettleException;
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
 * Read all files, count rows number
 * 
 * @author Samatar
 * @since 24-05-2007
 */
public class GetFilesRowsCount extends BaseStep implements StepInterface
{
	private GetFilesRowsCountMeta meta;
	private GetFilesRowsCountData data;
	
	private static final int BUFFER_SIZE_INPUT_STREAM = 500;
	
	public GetFilesRowsCount(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}


	private Object[] getOneRow() throws KettleException
	{
		if (!openNextFile()) return null;
			
		// Build an empty row based on the meta-data		  
		Object[] r;
		try
		{ 
			// Create new row or clone
			if (meta.isFileField())
			{
				r = data.readrow.clone();
				r = RowDataUtil.resizeArray(r, data.outputRowMeta.size());
			}else
			{
				r = RowDataUtil.allocateRowData(data.outputRowMeta.size());
			}
			
			r[data.totalpreviousfields]=data.rownr;
			
			if (meta.includeCountFiles()) r[data.totalpreviousfields+1]= data.filenr;
			
			incrementLinesInput();
			
		}
		 catch (Exception e)
		 {
			 
			throw new KettleException("Unable to read row from file", e);
			
		 }
		 
		return r;
	}
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		 
		try{
			 // Grab one row
			 Object[] outputRowData=getOneRow();
			 if (outputRowData==null)
		     {
		        setOutputDone();  // signal end to receiver(s)
		        return false; // end of data or error.
		     }
			 if((!meta.isFileField() && data.last_file)||meta.isFileField())
			 {
				 putRow(data.outputRowMeta, outputRowData);  // copy row to output rowset(s);
				 if (log.isDetailed()) log.logDetailed(toString(),Messages.getString("GetFilesRowsCount.Log.TotalRowsFiles"), data.rownr,data.filenr);
			 }	

		}catch(KettleException e)
		{
			
			logError(Messages.getString("GetFilesRowsCount.ErrorInStepRunning",e.getMessage())); //$NON-NLS-1$
			setErrors(1);
			stopAll();
			setOutputDone();  // signal end to receiver(s)
			return false;
		}
		 return true;
	
	}		
	private void getRowNumber() throws KettleException
	{
		try
		{
			if (data.file.getType() == FileType.FILE)
			{
				data.fr = KettleVFS.getInputStream(data.file);
				data.isr = new InputStreamReader(new BufferedInputStream(data.fr, BUFFER_SIZE_INPUT_STREAM));
					
				int c = 0;				
				data.lineStringBuffer.setLength(0);
				
				 while (c >= 0)
		         {
				     c = data.isr.read();
		
		             if (c == data.separator)
		             {
		                 // Move Row number pointer ahead
		               	 data.rownr ++;	
					 }	                        
		         }
			}
			if(log.isDetailed()) log.logDetailed(toString(),Messages.getString("GetFilesRowsCount.Log.RowsInFile", data.file.toString(), ""+data.rownr));
		}
		catch (Exception e)
		{
			throw new KettleException(e);
		}
	  
	}

	private boolean openNextFile()
	{
		if (data.last_file) return false; // Done!

		try
		{
			if(!meta.isFileField())
			{
	            if (data.filenr>=data.files.nrOfFiles()) // finished processing!
	            {
	            	if (log.isDetailed()) logDetailed(Messages.getString("GetFilesRowsCount.Log.FinishedProcessing"));
	                return false;
	            }
	            
			    // Is this the last file?
				data.last_file = ( data.filenr==data.files.nrOfFiles()-1);
				data.file = (FileObject) data.files.getFile((int)data.filenr);
				

			}else
			{
				data.readrow=getRow();     // Get row from input rowset & set row busy!
				if (data.readrow==null)
			    {
					if (log.isDetailed()) logDetailed(Messages.getString("GetFilesRowsCount.Log.FinishedProcessing"));
			         return false;
			    }
				
				if (first)
		        {
		            first = false;
		            
	            	data.inputRowMeta = getInputRowMeta();
		            data.outputRowMeta = data.inputRowMeta.clone();
		            meta.getFields(data.outputRowMeta, getStepname(), null, null, this);
		            
		            // Get total previous fields
		            data.totalpreviousfields=data.inputRowMeta.size();

					// Check is filename field is provided
					if (Const.isEmpty(meta.setOutputFilenameField()))
					{
						logError(Messages.getString("GetFilesRowsCount.Log.NoField"));
						throw new KettleException(Messages.getString("GetFilesRowsCount.Log.NoField"));
					}
					
					// cache the position of the field			
					if (data.indexOfFilenameField<0)
					{	
						data.indexOfFilenameField =getInputRowMeta().indexOfValue(meta.setOutputFilenameField());
						if (data.indexOfFilenameField<0)
						{
							// The field is unreachable !
							logError(Messages.getString("GetFilesRowsCount.Log.ErrorFindingField", meta.setOutputFilenameField())); //$NON-NLS-1$ //$NON-NLS-2$
							throw new KettleException(Messages.getString("GetFilesRowsCount.Exception.CouldnotFindField",meta.setOutputFilenameField())); //$NON-NLS-1$ //$NON-NLS-2$
						}
					}
	            	
		            
		        }  // End if first
				
				
				String filename=getInputRowMeta().getString(data.readrow,data.indexOfFilenameField);
				if(log.isDetailed()) log.logDetailed(toString(),Messages.getString("GetFilesRowsCount.Log.FilenameInStream", meta.setOutputFilenameField(),filename));

				data.file= KettleVFS.getFileObject(filename);
               
				// Init Row number
              	if(meta.isFileField()) data.rownr =0;	
			}
			
			// Move file pointer ahead!
			data.filenr++;
			
			if(meta.isAddResultFile())
			{
				// Add this to the result file names...
				ResultFile resultFile = new ResultFile(ResultFile.FILE_TYPE_GENERAL, data.file, getTransMeta().getName(), getStepname());
				resultFile.setComment(Messages.getString("GetFilesRowsCount.Log.FileAddedResult"));
				addResultFile(resultFile);
			}
			
			if (log.isDetailed()) logDetailed(Messages.getString("GetFilesRowsCount.Log.OpeningFile", data.file.toString()));
			getRowNumber();	
			if (log.isDetailed()) logDetailed(Messages.getString("GetFilesRowsCount.Log.FileOpened", data.file.toString()));
						
			
		}
		catch(Exception e)
		{
			logError(Messages.getString("GetFilesRowsCount.Log.UnableToOpenFile", ""+data.filenr, data.file.toString(), e.toString()));
			stopAll();
			setErrors(1);
			return false;
		}
		return true;
	}
	
	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(GetFilesRowsCountMeta)smi;
		data=(GetFilesRowsCountData)sdi;
		
		if (super.init(smi, sdi))
		{
			  if((meta.getRowSeparatorFormat().equals("CUSTOM")) && (Const.isEmpty(meta.getRowSeparator())))
	            {
	            	log.logError(Messages.getString("GetFilesRowsCount.Error.NoSeparator.Title"), Messages.getString("GetFilesRowsCount.Error.NoSeparator.Msg"));
	            	setErrors(1);
	                stopAll(); 
	            }
	            else
	            {
		            if (meta.getRowSeparatorFormat().equals("CR"))
		    		{
		    			data.separator='\n';
		    			if (log.isDetailed()) log.logDetailed(Messages.getString("GetFilesRowsCount.Log.Separator.Title"), Messages.getString("GetFilesRowsCount.Log.Separatoris.Infos") + " \\n");
		    		}
		            else if (meta.getRowSeparatorFormat().equals("LF"))
		    		{
		            	data.separator='\r';
		    			if (log.isDetailed()) log.logDetailed(Messages.getString("GetFilesRowsCount.Log.Separator.Title"), Messages.getString("GetFilesRowsCount.Log.Separatoris.Infos") + " \\r");
		    		}
		            else if (meta.getRowSeparatorFormat().equals("TAB"))
		    		{
		            	data.separator='\t';
		            	if (log.isDetailed()) log.logDetailed(Messages.getString("GetFilesRowsCount.Log.Separator.Title"), Messages.getString("GetFilesRowsCount.Log.Separatoris.Infos") + " \\t");
		    		}
		            else
		    		{
		            	
		            	data.separator=environmentSubstitute(meta.getRowSeparator()).charAt(0);
		            	 
		            	if (log.isDetailed()) log.logDetailed(Messages.getString("GetFilesRowsCount.Log.Separator.Title"), Messages.getString("GetFilesRowsCount.Log.Separatoris.Infos") + " " +data.separator);
		    		}
	            }
			  
			if(!meta.isFileField())
			{
				data.files = meta.getFiles(this);
				if (data.files==null || data.files.nrOfFiles()==0)
				{
					logError(Messages.getString("GetFilesRowsCount.Log.NoFiles"));
					return false;
				}
				try{
					  // Create the output row meta-data
		            data.outputRowMeta = new RowMeta();
		            meta.getFields(data.outputRowMeta, getStepname(), null, null, this); // get the metadata populated
   
				}
				catch(Exception e)
				{
					logError("Error initializing step: "+e.toString());
					logError(Const.getStackTracker(e));
					return false;
				}
			} 
			data.rownr = 0;
			data.filenr = 0;
			data.totalpreviousfields=0;
			
			return true;
		}
		return false;
	}
	
	public void dispose(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta = (GetFilesRowsCountMeta)smi;
		data = (GetFilesRowsCountData)sdi;
		if(data.file!=null) 
		{
			try
			{
				data.file.close();
				data.file=null;
			}catch  (Exception e)
			{
			}
		}
		if(data.is!=null) 
		{
			try
			{
				data.is.close();
				data.is=null;
			}catch  (Exception e)
			{
			}
		}
		if(data.isr!=null) 
		{
			try
			{
				data.isr.close();
				data.isr=null;
			}catch  (Exception e)
			{
			}
		}
		if(data.lineStringBuffer!=null) data.lineStringBuffer=null;

		super.dispose(smi, sdi);
	}
	
	//
	// Run is were the action happens!
	public void run()
	{	
    	BaseStep.runStepThread(this, meta, data);
	}
}