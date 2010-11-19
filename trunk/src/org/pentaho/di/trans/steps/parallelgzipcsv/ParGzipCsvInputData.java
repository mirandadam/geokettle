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
 

package org.pentaho.di.trans.steps.parallelgzipcsv;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * @author Matt
 * @since 3.2
 */
public class ParGzipCsvInputData extends BaseStepData implements StepDataInterface
{
	public RowMetaInterface convertRowMeta;
	public RowMetaInterface outputRowMeta;
	
	public String[] filenames;
	public int      filenr;
	public int      startFilenr;
	public byte[]   binaryFilename;
	
	public InputStream fis;
	
	public boolean  isAddingRowNumber;
	public long     rowNumber;
	
	public int stepNumber;
	public int totalNumberOfSteps;

			
	public boolean parallel;
	public int filenameFieldIndex;
	public int rownumFieldIndex;
	public GZIPInputStream	gzis;
	public int	bufferSize;
	public byte[]	delimiter;
	public byte[]	enclosure;
	
	public int startBuffer;
	public int endBuffer;
	public int maxBuffer;
	
	/** 
	 * This is the main byte buffer into which we're going to read chunks of data...
	 */
	public byte[]   byteBuffer;
	
	public long	totalBytesRead;
	public long	blockSize;
	public boolean	eofReached;
	public long	fileReadPosition;
	public int	blockNr;
	
	/**
	 * 
	 */
	public ParGzipCsvInputData()
	{
		super();
		startBuffer=0;
		endBuffer=0;
		maxBuffer=0;
		fileReadPosition=0L;
	}
	
	/**
    @return the byte array with escaped enclosures escaped.
	*/
	public byte[] removeEscapedEnclosures(byte[] field, int nrEnclosuresFound) {
		byte[] result = new byte[field.length-nrEnclosuresFound];
		int resultIndex=0;
		for (int i=0;i<field.length;i++)
		{
			if (field[i]==enclosure[0])
			{
				if (i+1<field.length && field[i+1]==enclosure[0])
				{
					// field[i]+field[i+1] is an escaped enclosure...
					// so we ignore this one
					// field[i+1] will be picked up on the next iteration.
				}
				else
				{
					// Not an escaped enclosure...
					result[resultIndex++] = field[i];
				}
			}
			else
			{
				result[resultIndex++] = field[i];
			}
		}
		return result;
	}

	/**
	 * Read more data from our current file... 
	 * @return
	 */
	public boolean getMoreData() throws KettleException {
		// See if the buffer is completely full (very long lines of data...
		// In that situation, we need to re-size the byte buffer...
		// We make it half as long...
		//
		if (startBuffer==0 && endBuffer>=byteBuffer.length) {
			int newSize;
			if (byteBuffer.length==0) { // initial
				newSize = bufferSize;
			} else {
				newSize = (int)((byteBuffer.length*3)/2); // increase by 50%
			}
			byte[] newByteBuffer = new byte[newSize];
			// Copy over the data into the new buffer.
			//
			maxBuffer = byteBuffer.length-startBuffer;
			System.arraycopy(byteBuffer, startBuffer, newByteBuffer, 0, maxBuffer);
			byteBuffer = newByteBuffer;
		}
		else
		{
			// Copy The old data to the start of the buffer...
			//
			if (startBuffer>0) {
				maxBuffer=byteBuffer.length-startBuffer;
				System.arraycopy(byteBuffer, startBuffer, byteBuffer, 0, maxBuffer);
				endBuffer=maxBuffer;
				startBuffer=0;
			}
		}			
		
		// Read from our file...
		//
		int size = byteBuffer.length-maxBuffer;
		int bytesRead = 0;
		int leftToRead = size;
		try {
			while (bytesRead<size) {
				int n = gzis.read(byteBuffer, maxBuffer, leftToRead);
				if (n<0) {
					// EOF, nothing more to read in combination with the need to get more data means we're done.
					//
					eofReached=true;
					fileReadPosition+=bytesRead;
					return bytesRead==0; 
				}
				bytesRead+=n; // bytes read so far
				maxBuffer+=n; // that's where we ended up so far
				leftToRead-=n; // a little bit less to read
			}
			fileReadPosition+=bytesRead; // keep track of where we are in the file...

			return false; // all OK
			
		} catch (IOException e) {
			throw new KettleException("Unable to read "+size+" bytes from the gzipped input file", e);
		}
	}
}
