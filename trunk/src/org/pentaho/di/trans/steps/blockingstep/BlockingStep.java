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
 
package org.pentaho.di.trans.steps.blockingstep;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketTimeoutException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.vfs.FileObject;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;


/**
 *  A step that blocks throughput until the input ends, then it will either output
 *  the last row or the complete input. 
 */
public class BlockingStep extends BaseStep implements StepInterface {

    private BlockingStepMeta meta;
    private BlockingStepData data;
    private Object[] lastRow;
    
    public BlockingStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
    {
        super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
    }
      
	private boolean addBuffer(RowMetaInterface rowMeta, Object[] r)    
	{
		if (r!=null)
		{
			data.buffer.add(r);     // Save row
		}
		
		// Time to write to disk: buffer in core is full!
		if (   data.buffer.size()==meta.getCacheSize()                 // Buffer is full: dump to disk 
		   || (data.files.size()>0 && r==null && data.buffer.size()>0) // No more records: join from disk 
		   )
		{		
			// Then write them to disk...
			DataOutputStream dos;
			GZIPOutputStream gzos;
			int p;
			
			try
			{
				FileObject fileObject=KettleVFS.createTempFile(meta.getPrefix(), ".tmp", environmentSubstitute(meta.getDirectory()));
				
				data.files.add(fileObject); // Remember the files!
				OutputStream outputStream = KettleVFS.getOutputStream(fileObject,false);
				if (meta.getCompress())
				{
					gzos = new GZIPOutputStream(new BufferedOutputStream(outputStream));
					dos=new DataOutputStream(gzos);
				}
				else
				{
					dos = new DataOutputStream(outputStream);
					gzos = null;
				}
			
				// How many records do we have?
				dos.writeInt(data.buffer.size());
                
                for (p=0;p<data.buffer.size();p++)
				{
                    // Just write the data, nothing else
                    rowMeta.writeData(dos, (Object[])data.buffer.get(p));
				}
				// Close temp-file
				dos.close();  // close data stream
				if (gzos != null)
                {
					gzos.close(); // close gzip stream
                }
                outputStream.close();  // close file stream
			}
			catch(Exception e)
			{
				logError("Error processing tmp-file: "+e.toString());
				return false;
			}
			
			data.buffer.clear();
		}		
		
		return true; 
	}
	
	private Object[] getBuffer()
	{
		Object[] retval;
		
		// Open all files at once and read one row from each file...
		if (data.files.size()>0 && ( data.dis.size()==0 || data.fis.size()==0 ))
		{
			if(log.isBasic()) logBasic(Messages.getString("BlockingStep.Log.Openfiles"));	
	
			try
			{
				FileObject fileObject = (FileObject)data.files.get(0);
				String filename = KettleVFS.getFilename(fileObject);
				if (log.isDetailed()) logDetailed(Messages.getString("BlockingStep.Log.Openfilename1")+filename+Messages.getString("BlockingStep.Log.Openfilename2"));
				InputStream fi=KettleVFS.getInputStream(fileObject);
				DataInputStream di;
				data.fis.add(fi);
				if (meta.getCompress())
				{
					GZIPInputStream gzfi = new GZIPInputStream(new BufferedInputStream(fi));
					di =new DataInputStream(gzfi);
					data.gzis.add(gzfi);
				}
				else
				{
					di=new DataInputStream(fi);
				}
				data.dis.add(di);

				// How long is the buffer?
				int buffersize=di.readInt();

				if (log.isDetailed()) logDetailed(Messages.getString("BlockingStep.Log.BufferSize1")+filename+
										  Messages.getString("BlockingStep.Log.BufferSize2")+ buffersize+ " " + Messages.getString("BlockingStep.Log.BufferSize3"));

				if (buffersize>0)
				{
					// Read a row from temp-file
                    data.rowbuffer.add(data.outputRowMeta.readData(di));
				}
			}
			catch(Exception e)
			{
				logError(Messages.getString("BlockingStepMeta.ErrorReadingFile")+e.toString());
                logError(Const.getStackTracker(e));
			}
		}
		
		if (data.files.size()==0)
		{
			if (data.buffer.size()>0)
			{
				retval=(Object[])data.buffer.get(0);
				data.buffer.remove(0);
			}
			else
			{
				retval=null;
			}
		}
		else
		{
			if (data.rowbuffer.size()==0)
            {
                retval=null;
            }
			else
			{		
				retval=(Object[])data.rowbuffer.get(0);
		
				data.rowbuffer.remove(0);
				
				// now get another 
				FileObject    file = (FileObject)data.files.get(0);
				DataInputStream di = (DataInputStream)data.dis.get(0); 
				InputStream     fi = (InputStream)data.fis.get(0);
				GZIPInputStream gzfi = (meta.getCompress()) ? (GZIPInputStream)data.gzis.get(0) : null;

				try
				{
					data.rowbuffer.add(0, data.outputRowMeta.readData(di));
				}
				catch(SocketTimeoutException e)
				{
		            logError(Messages.getString("System.Log.UnexpectedError")+" : "+e.toString()); //$NON-NLS-1$ //$NON-NLS-2$
		            logError(Const.getStackTracker(e));
		            setErrors(1);
		            stopAll();
				}
				catch(KettleFileException fe) // empty file or EOF mostly
				{
					try
					{
						di.close();
						fi.close();
						if (gzfi != null) gzfi.close();
						file.delete();
					}
					catch(IOException e)
					{
						logError(Messages.getString("BlockingStepMeta.UnableDeleteFile")+file.toString());
						setErrors(1);
						stopAll();
						return null;
					}
					
					data.files.remove(0);
					data.dis.remove(0);
					data.fis.remove(0);
					if (gzfi != null) data.gzis.remove(0);
				}
			}
		}
		return retval;
	}
    
	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(BlockingStepMeta)smi;
		data=(BlockingStepData)sdi;
		
		if (super.init(smi, sdi))
		{
		    // Add init code here.
		    return true;
		}
		return false;
	} 
    
    public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException {
    	
    	boolean err=true;
        Object[] r=getRow();       // Get row from input rowset & set row busy!
        
		// initialize 
		if (first && r!=null)
		{
            data.outputRowMeta = getInputRowMeta().clone();
		}
        
        if ( ! meta.isPassAllRows())
        {
        	if (r==null)  // no more input to be expected...
        	{
        		if(lastRow != null) {
        			putRow(data.outputRowMeta, lastRow);
        		}
        		setOutputDone();
        		return false;
        	}

        	lastRow = r;
        	return true;
        }
        else
        {
        	//  The mode in which we pass all rows to the output.        	
    		err=addBuffer(getInputRowMeta(), r);
    		if (!err) 
    		{
    			setOutputDone(); // signal receiver we're finished.
    			return false;
    		}		
    		
    		if (r==null)  // no more input to be expected...
    		{
    			// Now we can start the output!
    			r=getBuffer();
    			while (r!=null  && !isStopped())
    			{
    				if (log.isRowLevel()) logRowlevel("Read row: "+getInputRowMeta().getString(r));
    				
    				putRow(data.outputRowMeta, r); // copy row to possible alternate rowset(s).

    				r=getBuffer();
    			}
    			
    			setOutputDone(); // signal receiver we're finished.
    			return false;
    		}
    		
        	return true;
        }        
    }

    //
    // Run is were the action happens!
    public void run()
    {
    	BaseStep.runStepThread(this, meta, data);
    }
}