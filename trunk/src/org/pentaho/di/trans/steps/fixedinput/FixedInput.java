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
 
package org.pentaho.di.trans.steps.fixedinput;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.apache.commons.vfs.FileObject;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.ResultFile;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

/**
 * Read a simple fixed width file
 * Just output fields found in the file...
 * 
 * @author Matt
 * @since 2007-07-06
 */
public class FixedInput extends BaseStep implements StepInterface
{
	private FixedInputMeta meta;
	private FixedInputData data;
	
	public FixedInput(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		meta=(FixedInputMeta)smi;
		data=(FixedInputData)sdi;

		if (first) {
			first=false;
			
			data.outputRowMeta = new RowMeta();
			meta.getFields(data.outputRowMeta, getStepname(), null, null, this);

			// The conversion logic for when the lazy conversion is turned of is simple:
			// Pretend it's a lazy conversion object anyway and get the native type during conversion.
			//
			data.convertRowMeta = data.outputRowMeta.clone();
			for (ValueMetaInterface valueMeta : data.convertRowMeta.getValueMetaList())
			{
				valueMeta.setStorageType(ValueMetaInterface.STORAGE_TYPE_BINARY_STRING);
			}
			
			if (meta.isHeaderPresent()) {
				readOneRow(false); // skip this row.
			}
		}
		
		Object[] outputRowData=readOneRow(true);
		if (outputRowData==null)  // no more input to be expected...
		{
			setOutputDone();
			return false;
		}
		
		putRow(data.outputRowMeta, outputRowData);     // copy row to possible alternate rowset(s).

        if (checkFeedback(getLinesInput())) logBasic(Messages.getString("FixedInput.Log.LineNumber", Long.toString(getLinesInput()))); //$NON-NLS-1$
        
		return true;
	}

	
	/** Read a single row of data from the file... 
	 * 
	 * @param doConversions if you want to do conversions, set to false for the header row.
	 * @return a row of data...
	 * @throws KettleException
	 */
	private Object[] readOneRow(boolean doConversions) throws KettleException {

		try {
			
	        // See if we need to call it a day...
	        //
	        if (meta.isRunningInParallel()) {
	        	if (getLinesInput()>=data.rowsToRead) {
	        		return null; // We're done.  The rest is for the other steps in the cluster
	        	}
	        }

			Object[] outputRowData = RowDataUtil.allocateRowData(data.convertRowMeta.size());
			int outputIndex=0;
			
			// The strategy is as follows...
			// We read a block of byte[] from the file.
			// 
			// Then we scan that block of data.
			// We keep a byte[] that we extend if needed..
			// At the end of the block we read another, etc.
			//
			// Let's start by looking where we left off reading.
			//

			if (data.stopReading) {
				return null;
			}
			
			FixedFileInputField[] fieldDefinitions = meta.getFieldDefinition();
            for (int i=0;i<fieldDefinitions.length;i++) {
				
				int fieldWidth = fieldDefinitions[i].getWidth();
				data.endBuffer = data.startBuffer+fieldWidth; 
				if (data.endBuffer>data.bufferSize) {
					// Oops, we need to read more data...
					// Better resize this before we read other things in it...
					//
					data.resizeByteBuffer();
					
					// Also read another chunk of data, now that we have the space for it...
					// Ignore EOF, there might be other stuff in the buffer.
					//
					data.readBufferFromFile();
				}

				// re-verify the buffer after we tried to read extra data from file...
				//
				if (data.endBuffer>data.bufferSize) {
					// still a problem?
					// We hit an EOF and are trying to read beyond the EOF...
					
				    // If we are on the first field and there
				    // is nothing left in the buffer, don't return
				    // a row because we're done.
				    if ((0 == i) && data.bufferSize <= 0)
				    {
				        return null;
				    }
				    
				    
				    // This is the last record of data in the file.
			        data.stopReading = true;

	                // Just take what's left for the current field.
				    fieldWidth=data.bufferSize;
				}
				byte[] field = new byte[fieldWidth];
				System.arraycopy(data.byteBuffer, data.startBuffer, field, 0, fieldWidth);
				
				if (doConversions) {
					if (meta.isLazyConversionActive()) {
						outputRowData[outputIndex++] = field;
					}
					else {
						// We're not lazy so we convert the data right here and now.
						// The convert object uses binary storage as such we just have to ask the native type from it.
						// That will do the actual conversion.
						//
						ValueMetaInterface sourceValueMeta = data.convertRowMeta.getValueMeta(outputIndex);
						outputRowData[outputIndex++] = sourceValueMeta.convertBinaryStringToNativeType(field);
					}
				}
				else {
					outputRowData[outputIndex++] = null; // nothing for the header, no conversions here.
				}
				
				// OK, onto the next field...
				// 
				data.startBuffer=data.endBuffer;
			}
			
			// Now that we have all the data, see if there are any linefeed characters to remove from the buffer...
			//
			if (meta.isLineFeedPresent()) {

				data.endBuffer+=2;
				
				if (data.endBuffer>=data.bufferSize) {
					// Oops, we need to read more data...
					// Better resize this before we read other things in it...
					//
					data.resizeByteBuffer();
					
					// Also read another chunk of data, now that we have the space for it...
					data.readBufferFromFile();
				}

				// CR + Line feed in the worst case.
				//
				if (data.byteBuffer[data.startBuffer]=='\n' || data.byteBuffer[data.startBuffer]=='\r') {

					data.startBuffer++;

					if (data.byteBuffer[data.startBuffer]=='\n' || data.byteBuffer[data.startBuffer]=='\r') {

						data.startBuffer++;
					}
				}
				data.endBuffer = data.startBuffer;
			}
		
			incrementLinesInput();
			return outputRowData;
		}
		catch (Exception e)
		{
			throw new KettleFileException("Exception reading line using NIO: " + e.toString(), e);
		}
		
	}

	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(FixedInputMeta)smi;
		data=(FixedInputData)sdi;
		
		if (super.init(smi, sdi)) {
			try {
				data.preferredBufferSize = Integer.parseInt(environmentSubstitute(meta.getBufferSize()));
				data.lineWidth = Integer.parseInt(environmentSubstitute(meta.getLineWidth()));
				data.filename = environmentSubstitute(meta.getFilename());
				
				if (Const.isEmpty(data.filename)) {
					logError(Messages.getString("FixedInput.MissingFilename.Message"));
					return false;
				}
				
				FileObject fileObject = KettleVFS.getFileObject(data.filename);
				try
				{
					FileInputStream fileInputStream = new FileInputStream(fileObject.getName().getPathDecoded());
					data.fc = fileInputStream.getChannel();
					data.bb = ByteBuffer.allocateDirect( data.preferredBufferSize );
				}
				catch(IOException e) {
					logError(e.toString());
					return false;
				}
				
				// Add filename to result filenames ?
				if(meta.isAddResultFile())
				{
					ResultFile resultFile = new ResultFile(ResultFile.FILE_TYPE_GENERAL, fileObject, getTransMeta().getName(), toString());
					resultFile.setComment("File was read by a Fixed input step");
					addResultFile(resultFile);
				}
				
				logBasic("Opened file with name ["+data.filename+"]");
				
				data.stopReading = false;
				
				if (meta.isRunningInParallel()) {
					data.stepNumber = getUniqueStepNrAcrossSlaves();
					data.totalNumberOfSteps = getUniqueStepCountAcrossSlaves();
		            data.fileSize = fileObject.getContent().getSize();
				}
				
				// OK, now we need to skip a number of bytes in case we're doing a parallel read.
				//
				if (meta.isRunningInParallel()) {
					
					int totalLineWidth = data.lineWidth + meta.getLineSeparatorLength(); // including line separator bytes
	                long nrRows = data.fileSize / totalLineWidth; // 100.000 / 100 = 1000 rows
	                long rowsToSkip = Math.round( data.stepNumber * nrRows / (double)data.totalNumberOfSteps );  // 0, 333, 667
	                long nextRowsToSkip = Math.round( (data.stepNumber+1) * nrRows / (double)data.totalNumberOfSteps );  // 333, 667, 1000
	                data.rowsToRead = nextRowsToSkip - rowsToSkip;
	                long bytesToSkip = rowsToSkip * totalLineWidth;
	             
	                logBasic("Step #"+data.stepNumber+" is skipping "+bytesToSkip+" to position in file, then it's reading "+data.rowsToRead+" rows.");

                    data.fc.position(bytesToSkip);
				}
								
				return true;
			} catch (IOException e) {
				logError("Error opening file '"+meta.getFilename()+"' : "+e.toString());
				logError(Const.getStackTracker(e));
			}
		}
		return false;
	}
	
	@Override
	public void dispose(StepMetaInterface smi, StepDataInterface sdi) {
		
		try {
			if (data.fc!=null) {
				data.fc.close();
			}
		} catch (IOException e) {
			logError("Unable to close file channel for file '"+meta.getFilename()+"' : "+e.toString());
			logError(Const.getStackTracker(e));
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