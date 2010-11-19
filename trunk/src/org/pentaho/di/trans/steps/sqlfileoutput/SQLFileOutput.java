 /**********************************************************************
 **                                                                   **
 **               This code belongs to the KETTLE project.            **
 **                                                                   **
 ** Kettle, from version 2.2 on, is released into the public domain   **
 ** under the Lesser GNU Public License (LGPL).                       **
 **                                                                   **
 ** For more details, please read the document LICENSE.txt, included  **
 ** in this project                                                   **
 **                                                                   **
 ** http://www.kettle.be                                              **
 ** info@kettle.be                                                    **
 **                                                                   **
 **********************************************************************/
 
package org.pentaho.di.trans.steps.sqlfileoutput;

import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import org.pentaho.di.core.vfs.KettleVFS;
import org.apache.commons.vfs.FileObject;
import org.pentaho.di.core.ResultFile;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;




/**
 * Writes rows to a sql file.
 * 
 * @author Matt
 * @since 6-apr-2003
 */
public class SQLFileOutput extends BaseStep implements StepInterface
{
	private SQLFileOutputMeta meta;
	private SQLFileOutputData data;
	
	String schemaTable;
	String schemaName;
	String tableName;
        
		
	public SQLFileOutput(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		meta=(SQLFileOutputMeta)smi;
		data=(SQLFileOutputData)sdi;
		
		Object[] r=getRow();    // this also waits for a previous step to be finished.
		if (r==null)  // no more input to be expected...
		{
			return false;
		}
		if (first)
        {
            first=false;
            data.outputRowMeta = getInputRowMeta().clone();
            meta.getFields(data.outputRowMeta, getStepname(), null, null, this);
            data.insertRowMeta = getInputRowMeta().clone();
            
            
        	if(meta.isDoNotOpenNewFileInit())
			{
				if (!openNewFile())
				{
					logError("Couldn't open file [" + buildFilename() + "]");
					setErrors(1);
					return false;
				}
			}
               
            
        }

		boolean sendToErrorRow=false;
		String errorMessage = null;
		
		if ( r!=null && getLinesOutput()>0 && meta.getSplitEvery()>0 && ((getLinesOutput()+1)%meta.getSplitEvery())==0)   
		{
			
			// Done with this part or with everything.
			closeFile();
			
			// Not finished: open another file...
			if (r!=null)
			{
				if (!openNewFile())
				{
					logError("Unable to open new file (split #"+data.splitnr+"...");
					setErrors(1);
					return false;
				}
			}
		
		}
		
		if (r==null)  // no more input to be expected...
		{
			setOutputDone();
			return false;
		}

		try
		{
	        if (getLinesOutput()==0)
	        {
		        // Add creation table once to the top
		        if (meta.createTable())
		        {
		            String cr_table = data.db.getDDLCreationTable(schemaTable , data.insertRowMeta);	            
		            
		            if (log.isRowLevel()) logRowlevel(Messages.getString("SQLFileOutputLog.OutputSQL",cr_table));
			        // Write to file
		            data.writer.write(cr_table+ Const.CR + Const.CR) ;
		        }
    
	            // Truncate table
	            if (meta.truncateTable())
	            {
		            // Write to file
	            	String truncatetable=data.db.getDDLTruncateTable(schemaName, tableName+ ";" + Const.CR + Const.CR);
	            	data.writer.write(truncatetable);			
	            }
	            
	        }
	        
		}
		catch(Exception e)
		{
			throw new KettleStepException(e.getMessage());
		}
		
		try
		{
	        String sql = data.db.getSQLOutput(schemaName, tableName, data.insertRowMeta, r,meta.getDateFormat()) + ";" ;
	        
	        // Do we start a new line for this statement ?
	        if (meta.StartNewLine())  sql =sql + Const.CR;	

	        if (log.isRowLevel())  logRowlevel(Messages.getString("SQLFileOutputLog.OutputSQL",sql));
	            
	        try
	        {
		         // Write to file
		         data.writer.write(sql.toCharArray()) ;
	        }
	        catch(Exception e)
			{
	        	throw new KettleStepException(e.getMessage());
			}
            
	        putRow(data.outputRowMeta, r ); // in case we want it go further...
	        incrementLinesOutput();

            if (checkFeedback(getLinesRead())) 
            {
            	if(log.isBasic()) logBasic("linenr "+getLinesRead());
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
			
				logError(Messages.getString("SQLFileOutputMeta.Log.ErrorInStep")+e.getMessage()); //$NON-NLS-1$
				setErrors(1);
				stopAll();
				setOutputDone();  // signal end to receiver(s)
				return false;
	        }
			 if (sendToErrorRow)
	         {
				 // Simply add this row to the error row
	             putError(data.outputRowMeta, r, 1, errorMessage, null, "SFO001");
	             r=null;
	         }
		}	
		
		
		return true;
	}
	public String buildFilename()
	{
		return meta.buildFilename(environmentSubstitute(meta.getFileName()),  getCopy(), data.splitnr);
	}
	
	public boolean openNewFile()
	{
		boolean retval=false;
		data.writer=null;
		
		try
		{
         
			String filename = buildFilename();
			if (meta.AddToResult())
			{
				// Add this to the result file names...
				ResultFile resultFile = new ResultFile(ResultFile.FILE_TYPE_GENERAL, KettleVFS.getFileObject(filename), getTransMeta().getName(), getStepname());
				resultFile.setComment("This file was created with a text file output step");
	            addResultFile(resultFile);
			}
            OutputStream outputStream;
            
            if(log.isDetailed()) log.logDetailed(toString(), "Opening output stream in nocompress mode");
            OutputStream fos = KettleVFS.getOutputStream(filename, meta.isFileAppended());
            outputStream=fos;
			
            if(log.isDetailed()) log.logDetailed(toString(), "Opening output stream in default encoding");
            data.writer = new OutputStreamWriter(new BufferedOutputStream(outputStream, 5000));
        
            if (!Const.isEmpty(meta.getEncoding()))
            {
            	if(log.isBasic()) log.logDetailed(toString(), "Opening output stream in encoding: "+meta.getEncoding());
                data.writer = new OutputStreamWriter(new BufferedOutputStream(outputStream, 5000), environmentSubstitute(meta.getEncoding()));
            }
            else
            {
                if(log.isBasic()) log.logDetailed(toString(), "Opening output stream in default encoding");
                data.writer = new OutputStreamWriter(new BufferedOutputStream(outputStream, 5000));
            }
            
            if(log.isDetailed()) logDetailed("Opened new file with name ["+filename+"]");
            
            data.splitnr++;
			
			retval=true;
            
		}
		catch(Exception e)
		{
			logError("Error opening new file : "+e.toString());
		}

		return retval;
	}
	
	private boolean closeFile()
	{
		boolean retval=false;
		
		try
		{
			if(log.isDebug()) logDebug("Closing output stream");
			data.writer.close();
			if(log.isDebug()) logDebug("Closed output stream");
			data.writer = null;
		
			
			if(log.isDebug()) logDebug("Closing normal file ..");
		
            if (data.fos!=null)
            {
                data.fos.close();
                data.fos=null;
            }

			retval=true;
		}
		catch(Exception e)
		{
			logError("Exception trying to close file: " + e.toString());
			setErrors(1);
			retval = false;
		}

		return retval;
	}

    public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(SQLFileOutputMeta)smi;
		data=(SQLFileOutputData)sdi;

		if (super.init(smi, sdi))
		{
			try
			{
				if (meta.getDatabaseMeta() == null)
				{
		            throw new KettleStepException("The connection is not defined (empty)");
		        }	

				data.db=new Database(meta.getDatabaseMeta());
				data.db.shareVariablesWith(this);
                      
				logBasic("Connected to database ["+meta.getDatabaseMeta()+"]");
	
				
				if(meta.isCreateParentFolder())
				{
					// Check for parent folder
					FileObject parentfolder=null;
		    		try
		    		{
		    			// Get parent folder
		    			String filename=environmentSubstitute(meta.getFileName());
			    		parentfolder=KettleVFS.getFileObject(filename).getParent();	    		
			    		if(!parentfolder.exists())	
			    		{
			    			log.logBasic("Folder parent", "Folder parent " + parentfolder.getName() + " does not exist !");
			    			parentfolder.createFolder();
			    			log.logBasic("Folder parent", "Folder parent was created.");
			    		}
		    		}
		    		catch (Exception e) {
		    			logError("Couldn't created parent folder "+ parentfolder.getName());
		    			setErrors(1L);
						stopAll();
		    		}
		    		 finally {
		             	if ( parentfolder != null )
		             	{
		             		try  {
		             			parentfolder.close();
		             		}
		             		catch ( Exception ex ) {};
		             	}
		             }		
				}		
				
				
				if(!meta.isDoNotOpenNewFileInit())
				{
					if (!openNewFile())
					{
						logError("Couldn't open file [" + buildFilename() + "]");
						setErrors(1L);
						stopAll();
					}
				}
               
				tableName  = environmentSubstitute(meta.getTablename()); 
				schemaName  = environmentSubstitute(meta.getSchemaName()); 
			   
				if (Const.isEmpty(tableName))
		        {
		            throw new KettleStepException("The tablename is not defined (empty)");
		        }
		          
		        schemaTable = data.db.getDatabaseMeta().getQuotedSchemaTableCombination(schemaName, tableName);
	
			}
			catch(Exception e)
			{
				logError("An error occurred intialising this step: "+e.getMessage());
				stopAll();
				setErrors(1);
			}
			
			return true;
		}
		return false;
	}
		
	public void dispose(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(SQLFileOutputMeta)smi;
		data=(SQLFileOutputData)sdi;

		try
		{
            if(data.db!=null) data.db.closeInsert();
            if(data.fos!=null) data.fos.close();
            if(data.writer!=null) data.writer.close();
            closeFile();
		}
		
		catch(Exception dbe)
		{
			logError("Unexpected error committing the database connection: "+dbe.toString());
            logError(Const.getStackTracker(dbe));
			setErrors(1);
			stopAll();
		}
		finally
        {
            setOutputDone();

            if (data.db!=null) {
            	data.db.disconnect();
            }
            super.dispose(smi, sdi);
        }        
	}
	

	//
	// Run is were the action happens!
	public void run()
	{
    	BaseStep.runStepThread(this, meta, data);
	}
}
