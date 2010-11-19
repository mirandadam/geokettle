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
 

package org.pentaho.di.trans.steps.textfileoutput;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.ResultFile;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.util.EnvUtil;
import org.pentaho.di.core.util.StreamLogger;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;


/**
 * Converts input rows to text and then writes this text to one or more files.
 * 
 * @author Matt
 * @since 4-apr-2003
 */
public class TextFileOutput extends BaseStep implements StepInterface
{
    private static final String FILE_COMPRESSION_TYPE_NONE = TextFileOutputMeta.fileCompressionTypeCodes[TextFileOutputMeta.FILE_COMPRESSION_TYPE_NONE];
    private static final String FILE_COMPRESSION_TYPE_ZIP  = TextFileOutputMeta.fileCompressionTypeCodes[TextFileOutputMeta.FILE_COMPRESSION_TYPE_ZIP];
    private static final String FILE_COMPRESSION_TYPE_GZIP = TextFileOutputMeta.fileCompressionTypeCodes[TextFileOutputMeta.FILE_COMPRESSION_TYPE_GZIP];
    
	private TextFileOutputMeta meta;
	private TextFileOutputData data;
	 
	public TextFileOutput(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
    
	public synchronized boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException {
		meta = (TextFileOutputMeta) smi;
		data = (TextFileOutputData) sdi;

		boolean result = true;
		boolean bEndedLineWrote = false;
		Object[] r = getRow(); // This also waits for a row to be finished.

		if (r != null && first) {
			first = false;
			data.outputRowMeta = getInputRowMeta().clone();
			meta.getFields(data.outputRowMeta, getStepname(), null, null, this);

			// if file name in field is enabled then set field name and open file
			//
			if (meta.isFileNameInField()) {

				// find and set index of file name field in input stream
				//
				data.fileNameFieldIndex = getInputRowMeta().indexOfValue(meta.getFileNameField());

				// set the file name for this row
				//
				if (data.fileNameFieldIndex < 0) {
					throw new KettleStepException(Messages.getString("TextFileOutput.Exception.FileNameFieldNotFound", meta.getFileNameField())); // $NON-NLS-1$
				}
				
				data.fileNameMeta = getInputRowMeta().getValueMeta(data.fileNameFieldIndex);
				data.fileName = data.fileNameMeta.getString(r[data.fileNameFieldIndex]);
				setDataWriterForFilename(data.fileName);
			} 
			else  if (meta.isDoNotOpenNewFileInit() && !meta.isFileNameInField()) {
				// Open a new file here
				// 
				openNewFile(meta.getFileName());
				data.oneFileOpened = true;
				initBinaryDataFields();
			}

			if (!meta.isFileAppended() && (meta.isHeaderEnabled() || meta.isFooterEnabled())) // See if we have to write a header-line)
			{
				if (!meta.isFileNameInField() && meta.isHeaderEnabled() && data.outputRowMeta != null) {
					writeHeader();
				}
			}

			data.fieldnrs = new int[meta.getOutputFields().length];
			for (int i = 0; i < meta.getOutputFields().length; i++) {
				data.fieldnrs[i] = data.outputRowMeta.indexOfValue(meta.getOutputFields()[i].getName());
				if (data.fieldnrs[i] < 0) {
					throw new KettleStepException("Field [" + meta.getOutputFields()[i].getName() + "] couldn't be found in the input stream!");
				}
			}
		}

		if ((r == null && data.outputRowMeta != null && meta.isFooterEnabled()) || (r != null && getLinesOutput() > 0 && meta.getSplitEvery() > 0 && ((getLinesOutput() + 1) % meta.getSplitEvery()) == 0)) {
			if (data.outputRowMeta != null) {
				if (meta.isFooterEnabled()) {
					writeHeader();
				}
			}

			if (r == null) {
				// add tag to last line if needed
				writeEndedLine();
				bEndedLineWrote = true;
			}
			// Done with this part or with everything.
			closeFile();

			// Not finished: open another file...
			if (r != null) {
				openNewFile(meta.getFileName());

				if (meta.isHeaderEnabled() && data.outputRowMeta != null)
					if (writeHeader())
						incrementLinesOutput();
			}
		}

		if (r == null) // no more input to be expected...
		{
			if (false == bEndedLineWrote) {
				// add tag to last line if needed
				writeEndedLine();
				bEndedLineWrote = true;
			}

			setOutputDone();
			return false;
		}

		// First handle the file name in field
		// Write a header line as well if needed
		//
		if (meta.isFileNameInField()) {
			String baseFilename = data.fileNameMeta.getString(r[data.fileNameFieldIndex]);
			setDataWriterForFilename(baseFilename);
		}
		writeRowToFile(data.outputRowMeta, r);
		putRow(data.outputRowMeta, r); // in case we want it to go further...

		if (checkFeedback(getLinesOutput()))
			logBasic("linenr " + getLinesOutput());

		return result;
	}

	/**
	 * This method should only be used when you have a filename in the input stream.
	 * 
	 * @param filename the filename to set the data.writer field for
	 * @throws KettleException 
	 */
	private void setDataWriterForFilename(String filename) throws KettleException {
		// First handle the writers themselves.
		// If we didn't have a writer yet, we create one.
		// Basically we open a new file
		//
		data.writer = data.fileWriterMap.get(filename);
		if (data.writer==null) {
			openNewFile(filename);
			data.fileWriterMap.put(filename, data.writer);
		
			// If it's the first time we open it and we have a header, we write a header...
			//
			if (!meta.isFileAppended() && meta.isHeaderEnabled()) {
				if (writeHeader()) {
					incrementLinesOutput();
				}
			}
		}
	}

	private void writeRowToFile(RowMetaInterface rowMeta, Object[] r) throws KettleStepException
	{
		try
		{	
			if (meta.getOutputFields()==null || meta.getOutputFields().length==0)
			{
				/*
				 * Write all values in stream to text file.
				 */
				for (int i=0;i<rowMeta.size();i++)
				{
					if (i>0 && data.binarySeparator.length>0)
                    {
						data.writer.write(data.binarySeparator);
                    }
					ValueMetaInterface v=rowMeta.getValueMeta(i);
                    Object valueData = r[i];
                    
                    // no special null value default was specified since no fields are specified at all
                    // As such, we pass null
                    //
					writeField(v, valueData, null); 
				}
                data.writer.write(data.binaryNewline);
			}
			else
			{
				/*
				 * Only write the fields specified!
				 */
				for (int i=0;i<meta.getOutputFields().length;i++)
				{
					if (i>0 && data.binarySeparator.length>0)
						data.writer.write(data.binarySeparator);
	
					ValueMetaInterface v = rowMeta.getValueMeta(data.fieldnrs[i]);
					Object valueData = r[data.fieldnrs[i]];
					writeField(v, valueData, data.binaryNullValue[i]);
				}
                data.writer.write(data.binaryNewline);
			}

			incrementLinesOutput();
            
            // flush every 4k lines
            // if (linesOutput>0 && (linesOutput&0xFFF)==0) data.writer.flush();
		}
		catch(Exception e)
		{
			throw new KettleStepException("Error writing line", e);
		}
	}

    private byte[] formatField(ValueMetaInterface v, Object valueData) throws KettleValueException
    {
    	if( v.isString() )
    	{    	
    		String svalue = (valueData instanceof String)?(String)valueData:v.getString(valueData);
    		return convertStringToBinaryString(v,Const.trimToType(svalue, v.getTrimType()));
    	} 
    	else 
    	{
            return v.getBinaryString(valueData);
    	}
    }
    

    private byte[] convertStringToBinaryString(ValueMetaInterface v, String string) throws KettleValueException
    {
    	int length = v.getLength();
    	   	
    	if (string==null) return new byte[] {};
    	
    	if( length > -1 && length < string.length() ) {
    		// we need to truncate
    		String tmp = string.substring(0, length);
            if (Const.isEmpty(v.getStringEncoding()))
            {
            	return tmp.getBytes();
            }
            else
            {
                try
                {
                	return tmp.getBytes(v.getStringEncoding());
                }
                catch(UnsupportedEncodingException e)
                {
                    throw new KettleValueException("Unable to convert String to Binary with specified string encoding ["+v.getStringEncoding()+"]", e);
                }
            }
    	}
    	else {
    		byte text[];
            if (Const.isEmpty(v.getStringEncoding()))
            {
            	text = string.getBytes();
            }
            else
            {
                try
                {
                	text = string.getBytes(v.getStringEncoding());
                }
                catch(UnsupportedEncodingException e)
                {
                    throw new KettleValueException("Unable to convert String to Binary with specified string encoding ["+v.getStringEncoding()+"]", e);
                }
            }
        	if( length > string.length() ) 
        	{
        		// we need to pad this
        		
        		// Also for PDI-170: not all encoding use single characters, so we need to cope
        		// with this.
        		byte filler[] = " ".getBytes();
        		int size = text.length + filler.length*(length - string.length());
        		byte bytes[] = new byte[size];
        		System.arraycopy( text, 0, bytes, 0, text.length );
        		if( filler.length == 1 ) {
            		java.util.Arrays.fill( bytes, text.length, size, filler[0] );
        		} 
        		else 
        		{
        			// need to copy the filler array in lots of times
        			// TODO: this was not finished.
        		}        		        		
        		return bytes;
        	}
        	else
        	{
        		// do not need to pad or truncate
        		return text;
        	}
    	}
    }
    
    private byte[] getBinaryString(String string) throws KettleStepException {
    	try {
    		if (data.hasEncoding) {
        		return string.getBytes(meta.getEncoding());
    		}
    		else {
        		return string.getBytes();
    		}
    	}
    	catch(Exception e) {
    		throw new KettleStepException(e);
    	}
    }
    
    private void writeField(ValueMetaInterface v, Object valueData, byte[] nullString) throws KettleStepException
    {
        try
        {
        	byte[] str;
        	
        	// First check whether or not we have a null string set
        	// These values should be set when a null value passes
        	//
        	if (nullString!=null && v.isNull(valueData)) {
        		str = nullString;
        	}
        	else {
	        	if (meta.isFastDump()) {
	        		if( valueData instanceof byte[] )
	        		{
	            		str = (byte[]) valueData;
	        		} else {
	       				str = getBinaryString((valueData == null) ? "" : valueData.toString());
	        		}
	        	}
	        	else {
	    			str = formatField(v, valueData);
	        	}
        	}
        	
    		if (str!=null && str.length>0)
    		{
    			List<Integer> enclosures = null;
    			
        		if (v.isString() && meta.isEnclosureForced() && !meta.isPadded())
        		{
        			data.writer.write(data.binaryEnclosure);
        			
        			// Also check for the existence of the enclosure character.
        			// If needed we double (escape) the enclosure character.
        			//
        			enclosures = getEnclosurePositions(str);
        		}

        		if (enclosures == null) 
        		{
        			data.writer.write(str);
        		}
        		else
        		{
        			// Skip the enclosures, double them instead...
        			int from=0;
        			for (int i=0;i<enclosures.size();i++)
        			{
        				int position = enclosures.get(i);
        				data.writer.write(str, from, position + data.binaryEnclosure.length - from);
        				data.writer.write(data.binaryEnclosure); // write enclosure a second time
        				from=position+data.binaryEnclosure.length;
        			}
        			if (from<str.length)
        			{
        				data.writer.write(str, from, str.length-from);
        			}
        		}
        		
        		if (v.isString() && meta.isEnclosureForced() && !meta.isPadded())
        		{
        			data.writer.write(data.binaryEnclosure);
        		}
    		}
        }
        catch(Exception e)
        {
            throw new KettleStepException("Error writing field content to file", e);
        }
    }

	private List<Integer> getEnclosurePositions(byte[] str) {
		List<Integer> positions = null;
		if (data.binaryEnclosure!=null && data.binaryEnclosure.length>0)
		{
			for (int i=0;i<str.length - data.binaryEnclosure.length +1;i++)  //+1 because otherwise we will not find it at the end
			{
				// verify if on position i there is an enclosure
				// 
				boolean found = true;
				for (int x=0;found && x<data.binaryEnclosure.length;x++)
				{
					if (str[i+x] != data.binaryEnclosure[x]) found=false;
				}
				if (found)
				{
					if (positions==null) positions=new ArrayList<Integer>();
					positions.add(i);
				}
			}
		}
		return positions;
	}

	private boolean writeEndedLine()
	{
		boolean retval=false;
		try
		{
			String sLine = meta.getEndedLine();
			if (sLine!=null)
			{
				if (sLine.trim().length()>0)
				{
					data.writer.write(getBinaryString(sLine));
					incrementLinesOutput();
				}
			}
		}
		catch(Exception e)
		{
			logError("Error writing ended tag line: "+e.toString());
            logError(Const.getStackTracker(e));
			retval=true;
		}
		
		return retval;
	}
	
	private boolean writeHeader()
	{
		boolean retval=false;
		RowMetaInterface r=data.outputRowMeta;
		
		try
		{
			// If we have fields specified: list them in this order!
			if (meta.getOutputFields()!=null && meta.getOutputFields().length>0)
			{
				for (int i=0;i<meta.getOutputFields().length;i++)
				{
                    String fieldName = meta.getOutputFields()[i].getName();
                    ValueMetaInterface v = r.searchValueMeta(fieldName);
                    
					if (i>0 && data.binarySeparator.length>0)
					{
						data.writer.write(data.binarySeparator);
					}
                    if (meta.isEnclosureForced() && data.binaryEnclosure.length>0 && v!=null && v.isString())
                    {
                    	data.writer.write(data.binaryEnclosure);
                    }
                    data.writer.write(getBinaryString(fieldName));
                    if (meta.isEnclosureForced() && data.binaryEnclosure.length>0 && v!=null && v.isString())
                    {
                    	data.writer.write(data.binaryEnclosure);                    }
				}
				data.writer.write(data.binaryNewline);
			}
			else
			if (r!=null)  // Just put all field names in the header/footer
			{
				for (int i=0;i<r.size();i++)
				{
					if (i>0 && data.binarySeparator.length>0) 
					{
						data.writer.write(data.binarySeparator);
					}
					ValueMetaInterface v = r.getValueMeta(i);
					
                    if (meta.isEnclosureForced() && data.binaryEnclosure.length>0 && v.isString())
                    {
                        data.writer.write(data.binaryEnclosure);
                    }
                    data.writer.write(getBinaryString(v.getName()));
                    if (meta.isEnclosureForced() && data.binaryEnclosure.length>0 && v.isString())
                    {
                        data.writer.write(data.binaryEnclosure);
                    }
				}
                data.writer.write(data.binaryNewline);
			}
			else
			{
                data.writer.write(getBinaryString("no rows selected"+Const.CR));
			}
		}
		catch(Exception e)
		{
			logError("Error writing header line: "+e.toString());
            logError(Const.getStackTracker(e));
			retval=true;
		}
		incrementLinesOutput();
		return retval;
	}

	public String buildFilename(String filename, boolean ziparchive)
	{
		return TextFileOutputMeta.buildFilename(filename, meta.getExtension(), this, getCopy(), getPartitionID(), data.splitnr, ziparchive, meta);
	}
	
	public void openNewFile(String baseFilename) throws KettleException
	{
		data.writer=null;
		
		ResultFile resultFile = null;
		
		String filename = buildFilename(environmentSubstitute(baseFilename), true);
		
		try
		{
            if (meta.isFileAsCommand())
            {
            	if(log.isDebug()) logDebug("Spawning external process");
            	if (data.cmdProc != null)
            	{
            		logError("Previous command not correctly terminated");
            		setErrors(1);
            	}
            	String cmdstr = environmentSubstitute(meta.getFileName());
            	if (Const.getOS().equals("Windows 95"))
                {
            		cmdstr = "command.com /C " + cmdstr;
                }
            	else
                {
                    if (Const.getOS().startsWith("Windows"))
                    {
                        cmdstr = "cmd.exe /C " + cmdstr;
                    }
                }
            	if(log.isDetailed()) logDetailed("Starting: " + cmdstr);
            	Runtime r = Runtime.getRuntime();
            	data.cmdProc = r.exec(cmdstr, EnvUtil.getEnvironmentVariablesForRuntimeExec());
            	data.writer = data.cmdProc.getOutputStream();
            	StreamLogger stdoutLogger = new StreamLogger( data.cmdProc.getInputStream(), "(stdout)" );
            	StreamLogger stderrLogger = new StreamLogger( data.cmdProc.getErrorStream(), "(stderr)" );
            	new Thread(stdoutLogger).start();
            	new Thread(stderrLogger).start();
            }
            else
            {
            	// Add this to the result file names...
				resultFile = new ResultFile(ResultFile.FILE_TYPE_GENERAL, KettleVFS.getFileObject(filename), getTransMeta().getName(), getStepname());
				resultFile.setComment("This file was created with a text file output step");
	            addResultFile(resultFile);

	            OutputStream outputStream;
                
                if (!Const.isEmpty(meta.getFileCompression()) && !meta.getFileCompression().equals(FILE_COMPRESSION_TYPE_NONE))
                {
    				if (meta.getFileCompression().equals(FILE_COMPRESSION_TYPE_ZIP))
    				{
    					if(log.isDetailed()) log.logDetailed(toString(), "Opening output stream in zipped mode");
                        
    		            if(checkPreviouslyOpened(filename)){
    		            	data.fos = KettleVFS.getOutputStream(filename, true);
    		            }else{
    		            	data.fos = KettleVFS.getOutputStream(filename, meta.isFileAppended());
    		            }
                        data.zip = new ZipOutputStream(data.fos);
    					File entry = new File( filename );
    					ZipEntry zipentry = new ZipEntry(entry.getName());
    					zipentry.setComment("Compressed by Kettle");
    					data.zip.putNextEntry(zipentry);
    					outputStream=data.zip;
    				}
    				else if (meta.getFileCompression().equals(FILE_COMPRESSION_TYPE_GZIP))
    				{
    					if(log.isDetailed()) log.logDetailed(toString(), "Opening output stream in gzipped mode");
    		            if(checkPreviouslyOpened(filename)){
    		            	data.fos = KettleVFS.getOutputStream(filename, true);
    		            }else{
    		            	data.fos = KettleVFS.getOutputStream(filename, meta.isFileAppended());
    		            }
                        data.gzip = new GZIPOutputStream(data.fos);
    					outputStream=data.gzip;
    				}
                    else
                    {
                        throw new KettleFileException("No compression method specified!");
                    }
                }
				else
				{
					if(log.isDetailed()) log.logDetailed(toString(), "Opening output stream in nocompress mode");
                    if(checkPreviouslyOpened(filename)){
    		            	data.fos = KettleVFS.getOutputStream(filename, true);
    		            }else{
    		            	data.fos = KettleVFS.getOutputStream(filename, meta.isFileAppended());
    		            }
                    outputStream=data.fos;
				}
                
	            if (!Const.isEmpty(meta.getEncoding()))
	            {
	                if(log.isDetailed()) log.logDetailed(toString(), "Opening output stream in encoding: "+meta.getEncoding());
	                data.writer = new BufferedOutputStream(outputStream, 5000);
	            }
	            else
	            {
	                if(log.isDetailed()) log.logDetailed(toString(), "Opening output stream in default encoding");
	                data.writer = new BufferedOutputStream(outputStream, 5000);
	            }
	
	            if(log.isDetailed()) logDetailed("Opened new file with name ["+filename+"]");
            }
		}
		catch(Exception e)
		{
			throw new KettleException("Error opening new file : "+e.toString());
		}
		// System.out.println("end of newFile(), splitnr="+splitnr);

		data.splitnr++;

        if(resultFile!=null && meta.isAddToResultFiles())
        {
			// Add this to the result file names...
            addResultFile(resultFile);
        }
	}
	
	private boolean closeFile()
	{
		boolean retval=false;
		
		try
		{			
			if ( data.writer != null )
			{
				if(log.isDebug()) logDebug("Closing output stream");
			    data.writer.close();
			    if(log.isDebug()) logDebug("Closed output stream");
			}			
			data.writer = null;
			if (data.cmdProc != null)
			{
				if(log.isDebug()) logDebug("Ending running external command");
				int procStatus = data.cmdProc.waitFor();
				// close the streams
				// otherwise you get "Too many open files, java.io.IOException" after a lot of iterations
				try {
					data.cmdProc.getErrorStream().close();
					data.cmdProc.getOutputStream().close();				
					data.cmdProc.getInputStream().close();
				} catch (IOException e) {
					if(log.isDetailed()) logDetailed("Warning: Error closing streams: " + e.getMessage());
				}				
				data.cmdProc = null;
				if(log.isBasic() && procStatus != 0) logBasic("Command exit status: " + procStatus);
			}
			else
			{
				if(log.isDebug()) logDebug("Closing normal file ...");
				if (FILE_COMPRESSION_TYPE_ZIP.equals(meta.getFileCompression()))
				{
					data.zip.closeEntry();
					data.zip.finish();
					data.zip.close();
				}
				else if (FILE_COMPRESSION_TYPE_GZIP.equals(meta.getFileCompression()))
				{
					data.gzip.finish();
				}
                if (data.fos!=null)
                {
                    data.fos.close();
                    data.fos=null;
                }
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
	
	public boolean checkPreviouslyOpened(String filename){
		
		return data.previouslyOpenedFiles.contains(filename);
		
	}
	
	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(TextFileOutputMeta)smi;
		data=(TextFileOutputData)sdi;

		if (super.init(smi, sdi))
		{
			data.splitnr=0;
			// In case user want to create file at first row
			// In that case, DO NOT create file at Init
			if(!meta.isDoNotOpenNewFileInit())
			{
				try {
					if (!meta.isFileNameInField()) {
						openNewFile(meta.getFileName());
					}

					data.oneFileOpened=true;
				}	
				catch(Exception e)
				{
					logError("Couldn't open file "+meta.getFileName(), e);
					setErrors(1L);
					stopAll();
				}
			}

			try {
				initBinaryDataFields();
			} catch(Exception e)
			{
				logError("Couldn't initialize binary data fields", e);
				setErrors(1L);
				stopAll();
			}

			return true;
		}
	
		return false;
	}
	
	private void initBinaryDataFields() throws KettleException
	{
		try {
			data.hasEncoding = !Const.isEmpty(meta.getEncoding());
			data.binarySeparator = new byte[] {};
			data.binaryEnclosure = new byte[] {};
			data.binaryNewline   = new byte[] {};
			
			if (data.hasEncoding) {
				if (!Const.isEmpty(meta.getSeparator())) data.binarySeparator= environmentSubstitute(meta.getSeparator()).getBytes(meta.getEncoding());
				if (!Const.isEmpty(meta.getEnclosure())) data.binaryEnclosure = environmentSubstitute(meta.getEnclosure()).getBytes(meta.getEncoding());
				if (!Const.isEmpty(meta.getNewline()))   data.binaryNewline   = meta.getNewline().getBytes(meta.getEncoding());
			}
			else {
				if (!Const.isEmpty(meta.getSeparator())) data.binarySeparator= environmentSubstitute(meta.getSeparator()).getBytes();
				if (!Const.isEmpty(meta.getEnclosure())) data.binaryEnclosure = environmentSubstitute(meta.getEnclosure()).getBytes();
				if (!Const.isEmpty(meta.getNewline()))   data.binaryNewline   = environmentSubstitute(meta.getNewline()).getBytes();
			}
			
			data.binaryNullValue = new byte[meta.getOutputFields().length][];
			for (int i=0;i<meta.getOutputFields().length;i++)
			{
				data.binaryNullValue[i] = null;
				String nullString = meta.getOutputFields()[i].getNullString();
				if (!Const.isEmpty(nullString)) 
				{
					if (data.hasEncoding)
					{
						data.binaryNullValue[i] = nullString.getBytes(meta.getEncoding());
					}
					else
					{
						data.binaryNullValue[i] = nullString.getBytes();
					}
				}
			}
		}
		catch(Exception e) {
			throw new KettleException("Unexpected error while encoding binary fields", e);
		}
	}
	public void dispose(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(TextFileOutputMeta)smi;
		data=(TextFileOutputData)sdi;
		
		if(data.oneFileOpened) closeFile();
		
		try{
			if(data.fos!=null) { 
				data.fos.close();
			}
		}
		catch (Exception e)
		{
			
		}
		
		for (OutputStream outputStream : data.fileWriterMap.values()) {
			try {
				outputStream.close();
			} catch (IOException e) {
				// Eat exception.
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