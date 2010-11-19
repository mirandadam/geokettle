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

package org.pentaho.di.trans.steps.textfileinput;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipInputStream;

import org.apache.commons.vfs.FileObject;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.ResultFile;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.fileinput.FileInputList;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.playlist.FilePlayListAll;
import org.pentaho.di.core.playlist.FilePlayListReplay;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.step.errorhandling.AbstractFileErrorHandler;
import org.pentaho.di.trans.step.errorhandling.CompositeFileErrorHandler;
import org.pentaho.di.trans.step.errorhandling.FileErrorHandler;
import org.pentaho.di.trans.step.errorhandling.FileErrorHandlerContentLineNumber;
import org.pentaho.di.trans.step.errorhandling.FileErrorHandlerMissingFiles;


/**
 * Read all sorts of text files, convert them to rows and writes these to one or
 * more output streams.
 * 
 * @author Matt
 * @since 4-apr-2003
 */
public class TextFileInput extends BaseStep implements StepInterface
{
	private static final int BUFFER_SIZE_INPUT_STREAM = 500;
    
    private static LogWriter log = LogWriter.getInstance();
    
    private TextFileInputMeta meta;

	private TextFileInputData data;

	private long lineNumberInFile;
    
    private TransMeta transmeta;

	public TextFileInput(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
        this.transmeta = transMeta;
	}

	public static final String getLine(LogWriter log, InputStreamReader reader, int formatNr, StringBuilder line) throws KettleFileException
	{
		int c = 0;
		line.setLength(0);
        
		try
		{
            switch(formatNr)
            {
            case TextFileInputMeta.FILE_FORMAT_DOS:
                {
                    while (c >= 0)
                    {
                        c = reader.read();
                        
                        if (c == '\r' || c == '\n' )
                        {
                            c = reader.read(); // skip \n and \r
                            if( c != '\r' && c != '\n' ) 
                            { 
                                // make sure its really a linefeed or cariage return
                                // raise an error this is not a DOS file
                                // so we have pulled a character from the next line
                                throw new KettleFileException(Messages.getString("TextFileInput.Log.SingleLineFound"));                                 
                            }
                            return line.toString();
                        }
                        if (c >= 0) line.append((char) c);
                    }
                }
                break;
            case TextFileInputMeta.FILE_FORMAT_UNIX:
                {
                    while (c >= 0)
                    {
                        c = reader.read();
                        
    					if (c == '\n' || c == '\r')
    					{
    						return line.toString();
    					}
    					if (c >= 0) line.append((char) c);
                    }
                }
                break;
            case TextFileInputMeta.FILE_FORMAT_MIXED:
        		 // in mixed mode we suppose the LF is the last char and CR is ignored
        			// not for MAC OS 9 but works for Mac OS X. Mac OS 9 can use UNIX-Format
        		{
                    while (c >= 0)
                    {
                        c = reader.read();
                        
        				if (c == '\n')
        				{
        					return line.toString();
        				}
        				else if (c != '\r')
        				{
        					if (c >= 0) line.append((char) c);
        				}
        			}
        		}
                break;
            }
		}
		catch(KettleFileException e)
		{
			throw e;
		}
		catch (Exception e)
		{
			if (line.length() == 0)
			{
				throw new KettleFileException(Messages.getString("TextFileInput.Log.Error.ExceptionReadingLine",e.toString()), e);
			}
			return line.toString();
		}
		if (line.length() > 0) return line.toString();

		return null;
	}
	
	public static final String[] guessStringsFromLine(String line, TextFileInputMeta inf) throws KettleException
	{
		List<String> strings = new ArrayList<String>();
        int fieldnr;
        
		String pol; // piece of line

		try
		{
			if (line == null) return null;

			if (inf.getFileType().equalsIgnoreCase("CSV"))
			{
				// Split string in pieces, only for CSV!

				fieldnr = 0;
				int pos = 0;
				int length = line.length();
				boolean dencl = false;

                int len_encl = (inf.getEnclosure() == null ? 0 : inf.getEnclosure().length());
                int len_esc = (inf.getEscapeCharacter() == null ? 0 : inf.getEscapeCharacter().length());

				while (pos < length)
				{
					int from = pos;
					int next;

					boolean encl_found;
					boolean contains_escaped_enclosures = false;
					boolean contains_escaped_separators = false;

					// Is the field beginning with an enclosure?
					// "aa;aa";123;"aaa-aaa";000;...
					if (len_encl > 0 && line.substring(from, from + len_encl).equalsIgnoreCase(inf.getEnclosure()))
					{
                        if (log.isRowLevel()) log.logRowlevel(Messages.getString("TextFileInput.Log.ConvertLineToRowTitle"), Messages.getString("TextFileInput.Log.ConvertLineToRow",line.substring(from, from + len_encl)));
						encl_found = true;
						int p = from + len_encl;

						boolean is_enclosure = len_encl > 0 && p + len_encl < length
								&& line.substring(p, p + len_encl).equalsIgnoreCase(inf.getEnclosure());
						boolean is_escape = len_esc > 0 && p + len_esc < length
								&& line.substring(p, p + len_esc).equalsIgnoreCase(inf.getEscapeCharacter());

						boolean enclosure_after = false;
						
						// Is it really an enclosure? See if it's not repeated twice or escaped!
						if ((is_enclosure || is_escape) && p < length - 1) 
						{
							String strnext = line.substring(p + len_encl, p + 2 * len_encl);
							if (strnext.equalsIgnoreCase(inf.getEnclosure()))
							{
								p++;
								enclosure_after = true;
								dencl = true;

								// Remember to replace them later on!
								if (is_escape) contains_escaped_enclosures = true; 
							}
						}

						// Look for a closing enclosure!
						while ((!is_enclosure || enclosure_after) && p < line.length())
						{
							p++;
							enclosure_after = false;
							is_enclosure = len_encl > 0 && p + len_encl < length && line.substring(p, p + len_encl).equals(inf.getEnclosure());
							is_escape = len_esc > 0 && p + len_esc < length && line.substring(p, p + len_esc).equals(inf.getEscapeCharacter());

							// Is it really an enclosure? See if it's not repeated twice or escaped!
							if ((is_enclosure || is_escape) && p < length - 1) // Is
							{
								String strnext = line.substring(p + len_encl, p + 2 * len_encl);
								if (strnext.equals(inf.getEnclosure()))
								{
									p++;
									enclosure_after = true;
									dencl = true;

									// Remember to replace them later on!
									if (is_escape) contains_escaped_enclosures = true; // remember
								}
							}
						}

						if (p >= length) next = p;
						else next = p + len_encl;

                        if (log.isRowLevel()) log.logRowlevel(Messages.getString("TextFileInput.Log.ConvertLineToRowTitle"), Messages.getString("TextFileInput.Log.EndOfEnclosure", ""+ p));
					}
					else
					{
						encl_found = false;
						boolean found = false;
						int startpoint = from;
						int tries = 1;
						do
						{
							next = line.indexOf(inf.getSeparator(), startpoint);

							// See if this position is preceded by an escape character.
							if (len_esc > 0 && next - len_esc > 0)
							{
								String before = line.substring(next - len_esc, next);

								if (inf.getEscapeCharacter().equals(before))
								{
									// take the next separator, this one is escaped...
									startpoint = next + 1; 
									tries++;
									contains_escaped_separators = true;
								}
								else
								{
									found = true;
								}
							}
							else
							{
								found = true;
							}
						}
						while (!found && next >= 0);
					}
					if (next == -1) next = length;

					if (encl_found)
					{
						pol = line.substring(from + len_encl, next - len_encl);
                        if (log.isRowLevel()) log.logRowlevel(Messages.getString("TextFileInput.Log.ConvertLineToRowTitle"), Messages.getString("TextFileInput.Log.EnclosureFieldFound", ""+ pol));
					}
					else
					{
						pol = line.substring(from, next);
                        if (log.isRowLevel()) log.logRowlevel(Messages.getString("TextFileInput.Log.ConvertLineToRowTitle"), Messages.getString("TextFileInput.Log.NormalFieldFound",""+ pol));
					}

					if (dencl)
					{
						StringBuilder sbpol = new StringBuilder(pol);
						int idx = sbpol.indexOf(inf.getEnclosure() + inf.getEnclosure());
						while (idx >= 0)
						{
							sbpol.delete(idx, idx + inf.getEnclosure().length());
							idx = sbpol.indexOf(inf.getEnclosure() + inf.getEnclosure());
						}
						pol = sbpol.toString();
					}

					//	replace the escaped enclosures with enclosures... 
					if (contains_escaped_enclosures) 
					{
						String replace = inf.getEscapeCharacter() + inf.getEnclosure();
						String replaceWith = inf.getEnclosure();

						pol = Const.replace(pol, replace, replaceWith);
					}

					//replace the escaped separators with separators... 
					if (contains_escaped_separators) 
					{
						String replace = inf.getEscapeCharacter() + inf.getSeparator();
						String replaceWith = inf.getSeparator();

						pol = Const.replace(pol, replace, replaceWith);
					}

					// Now add pol to the strings found!
					strings.add(pol);

					pos = next + inf.getSeparator().length();
					fieldnr++;
				}
				if ( pos == length )
				{
					if (log.isRowLevel()) log.logRowlevel(Messages.getString("TextFileInput.Log.ConvertLineToRowTitle"), Messages.getString("TextFileInput.Log.EndOfEmptyLineFound"));
					strings.add("");
                    fieldnr++;
				}
			}
			else
			{
				// Fixed file format: Simply get the strings at the required positions...
				for (int i = 0; i < inf.getInputFields().length; i++)
				{
					TextFileInputField field = inf.getInputFields()[i];

					int length = line.length();

					if (field.getPosition() + field.getLength() <= length)
					{
						strings.add( line.substring(field.getPosition(), field.getPosition() + field.getLength()) );
					}
					else
					{
						if (field.getPosition() < length)
						{
							strings.add( line.substring(field.getPosition()) );
						}
						else
						{
							strings.add( "" );
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			throw new KettleException(Messages.getString("TextFileInput.Log.Error.ErrorConvertingLine",e.toString()), e);
		}

		return strings.toArray(new String[strings.size()]);
	}
    
	
	public static final String[] convertLineToStrings(String line, InputFileMetaInterface inf) throws KettleException
	{
		String[] strings = new String[inf.getInputFields().length];
        int fieldnr;
        
		String pol; // piece of line

		try
		{
			if (line == null) return null;

			if (inf.getFileType().equalsIgnoreCase("CSV"))
			{
				// Split string in pieces, only for CSV!

				fieldnr = 0;
				int pos = 0;
				int length = line.length();
				boolean dencl = false;

                int len_encl = (inf.getEnclosure() == null ? 0 : inf.getEnclosure().length());
                int len_esc = (inf.getEscapeCharacter() == null ? 0 : inf.getEscapeCharacter().length());

				while (pos < length)
				{
					int from = pos;
					int next;

					boolean encl_found;
					boolean contains_escaped_enclosures = false;
					boolean contains_escaped_separators = false;

					// Is the field beginning with an enclosure?
					// "aa;aa";123;"aaa-aaa";000;...
					if (len_encl > 0 && line.substring(from, from + len_encl).equalsIgnoreCase(inf.getEnclosure()))
					{
                        if (log.isRowLevel()) log.logRowlevel(Messages.getString("TextFileInput.Log.ConvertLineToRowTitle"), Messages.getString("TextFileInput.Log.Encloruse", line.substring(from, from + len_encl)));
						encl_found = true;
						int p = from + len_encl;

						boolean is_enclosure = len_encl > 0 && p + len_encl < length
								&& line.substring(p, p + len_encl).equalsIgnoreCase(inf.getEnclosure());
						boolean is_escape = len_esc > 0 && p + len_esc < length
								&& line.substring(p, p + len_esc).equalsIgnoreCase(inf.getEscapeCharacter());

						boolean enclosure_after = false;
						
						// Is it really an enclosure? See if it's not repeated twice or escaped!
						if ((is_enclosure || is_escape) && p < length - 1) 
						{
							String strnext = line.substring(p + len_encl, p + 2 * len_encl);
							if (strnext.equalsIgnoreCase(inf.getEnclosure()))
							{
								p++;
								enclosure_after = true;
								dencl = true;

								// Remember to replace them later on!
								if (is_escape) contains_escaped_enclosures = true; 
							}
						}

						// Look for a closing enclosure!
						while ((!is_enclosure || enclosure_after) && p < line.length())
						{
							p++;
							enclosure_after = false;
							is_enclosure = len_encl > 0 && p + len_encl < length && line.substring(p, p + len_encl).equals(inf.getEnclosure());
							is_escape = len_esc > 0 && p + len_esc < length && line.substring(p, p + len_esc).equals(inf.getEscapeCharacter());

							// Is it really an enclosure? See if it's not repeated twice or escaped!
							if ((is_enclosure || is_escape) && p < length - 1) // Is
							{
								String strnext = line.substring(p + len_encl, p + 2 * len_encl);
								if (strnext.equals(inf.getEnclosure()))
								{
									p++;
									enclosure_after = true;
									dencl = true;

									// Remember to replace them later on!
									if (is_escape) contains_escaped_enclosures = true; // remember
								}
							}
						}

						if (p >= length) next = p;
						else next = p + len_encl;

                        if (log.isRowLevel()) log.logRowlevel(Messages.getString("TextFileInput.Log.ConvertLineToRowTitle"), Messages.getString("TextFileInput.Log.EndOfEnclosure",""+ p));
					}
					else
					{
						encl_found = false;
						boolean found = false;
						int startpoint = from;
						int tries = 1;
						do
						{
							next = line.indexOf(inf.getSeparator(), startpoint);

							// See if this position is preceded by an escape character.
							if (len_esc > 0 && next - len_esc > 0)
							{
								String before = line.substring(next - len_esc, next);

								if (inf.getEscapeCharacter().equals(before))
								{
									// take the next separator, this one is escaped...
									startpoint = next + 1; 
									tries++;
									contains_escaped_separators = true;
								}
								else
								{
									found = true;
								}
							}
							else
							{
								found = true;
							}
						}
						while (!found && next >= 0);
					}
					if (next == -1) next = length;

					if (encl_found && ((from + len_encl) <= (next - len_encl)))
					{
						pol = line.substring(from + len_encl, next - len_encl);
                        if (log.isRowLevel()) log.logRowlevel(Messages.getString("TextFileInput.Log.ConvertLineToRowTitle"), Messages.getString("TextFileInput.Log.EnclosureFieldFound",""+ pol ));
					}
					else
					{
						pol = line.substring(from, next);
                        if (log.isRowLevel()) log.logRowlevel(Messages.getString("TextFileInput.Log.ConvertLineToRowTitle"), Messages.getString("TextFileInput.Log.NormalFieldFound",""+ pol));
					}

					if (dencl && Const.isEmpty(inf.getEscapeCharacter()))
					{
						StringBuilder sbpol = new StringBuilder(pol);
						int idx = sbpol.indexOf(inf.getEnclosure() + inf.getEnclosure());
						while (idx >= 0)
						{
							sbpol.delete(idx, idx + inf.getEnclosure().length());
							idx = sbpol.indexOf(inf.getEnclosure() + inf.getEnclosure());
						}
						pol = sbpol.toString();
					}

					//	replace the escaped enclosures with enclosures... 
					if (contains_escaped_enclosures) 
					{
						String replace = inf.getEscapeCharacter() + inf.getEnclosure();
						String replaceWith = inf.getEnclosure();

						pol = Const.replace(pol, replace, replaceWith);
					}

					//replace the escaped separators with separators... 
					if (contains_escaped_separators) 
					{
						String replace = inf.getEscapeCharacter() + inf.getSeparator();
						String replaceWith = inf.getSeparator();

						pol = Const.replace(pol, replace, replaceWith);
					}

					// Now add pol to the strings found!
					try {
						strings[fieldnr]=pol;
					}
					catch(ArrayIndexOutOfBoundsException e) {
						// In case we didn't allocate enough space.
						// This happens when you have less header values specified than there are actual values in the rows.
						// As this is "the exception" we catch and resize here.
						//
						String[] newStrings = new String[strings.length];
						for (int x=0;x<strings.length;x++) newStrings[x] = strings[x];
						strings = newStrings;
					}

					pos = next + inf.getSeparator().length();
					fieldnr++;
				}
				if ( pos == length)
				{
					if (log.isRowLevel()) log.logRowlevel(Messages.getString("TextFileInput.Log.ConvertLineToRowTitle"), Messages.getString("TextFileInput.Log.EndOfEmptyLineFound"));
					if (fieldnr<strings.length) strings[fieldnr]= Const.EMPTY_STRING;
                    fieldnr++;
				}
			}
			else
			{
				// Fixed file format: Simply get the strings at the required positions...
				for (int i = 0; i < inf.getInputFields().length; i++)
				{
					TextFileInputField field = inf.getInputFields()[i];

					int length = line.length();

					if (field.getPosition() + field.getLength() <= length)
					{
						strings[i] = line.substring(field.getPosition(), field.getPosition() + field.getLength());
					}
					else
					{
						if (field.getPosition() < length)
						{
							strings[i] = line.substring(field.getPosition());
						}
						else
						{
							strings[i] = "";
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			throw new KettleException(Messages.getString("TextFileInput.Log.Error.ErrorConvertingLine",e.toString()), e);
		}

		return strings;
	}
    
    /**
     * @deprecated Use {@link #convertLineToRow(TextFileLine,InputFileMetaInterface,Object[],int,RowMetaInterface,RowMetaInterface,String,long, FileErrorHandler)} instead
     */
    public static final Object[] convertLineToRow(TextFileLine textFileLine, InputFileMetaInterface info, RowMetaInterface outputRowMeta, RowMetaInterface convertRowMeta, String fname, long rowNr, FileErrorHandler errorHandler) throws KettleException
    {
        return convertLineToRow(textFileLine, info, null, 0, outputRowMeta, convertRowMeta, fname,
                rowNr, errorHandler);
    }

    public static final Object[] convertLineToRow(TextFileLine textFileLine, InputFileMetaInterface info, Object[] passThruFields, int nrPassThruFields, RowMetaInterface outputRowMeta, RowMetaInterface convertRowMeta, String fname, long rowNr, FileErrorHandler errorHandler) throws KettleException
    {
      if (textFileLine == null || textFileLine.line == null /*|| textFileLine.line.length() == 0*/) return null;

        Object[] r = RowDataUtil.allocateRowData(outputRowMeta.size()); // over-allocate a bit in the row producing steps...
        
        int nrfields = info.getInputFields().length;
        int fieldnr;
        
        Long errorCount = null;
        if (info.isErrorIgnored() && info.getErrorCountField() != null && info.getErrorCountField().length() > 0)
        {
            errorCount = new Long(0L);
        }
        String errorFields = null;
        if (info.isErrorIgnored() && info.getErrorFieldsField() != null && info.getErrorFieldsField().length() > 0)
        {
            errorFields = "";
        }
        String errorText = null;
        if (info.isErrorIgnored() && info.getErrorTextField() != null && info.getErrorTextField().length() > 0)
        {
            errorText = "";
        }
        
        try
        {
            // System.out.println("Convertings line to string ["+line+"]");
            String[] strings = convertLineToStrings(textFileLine.line, info);

            for (fieldnr = 0; fieldnr < nrfields; fieldnr++)
            {
                TextFileInputField f = info.getInputFields()[fieldnr];
                ValueMetaInterface valueMeta = outputRowMeta.getValueMeta(fieldnr);
                ValueMetaInterface convertMeta = convertRowMeta.getValueMeta(fieldnr);
                
                Object value;

                String nullif = fieldnr < nrfields ? f.getNullString() : "";
                String ifnull = fieldnr < nrfields ? f.getIfNullValue() : "";
                int trim_type = fieldnr < nrfields ? f.getTrimType() : ValueMetaInterface.TRIM_TYPE_NONE;

                if (fieldnr < strings.length)
                {
                    String pol = strings[fieldnr];
                    try
                    {
                        value = valueMeta.convertDataFromString(pol, convertMeta, nullif, ifnull, trim_type);
                    }
                    catch (Exception e)
                    {                               	
                        // OK, give some feedback!
                        String message = Messages.getString("TextFileInput.Log.CoundNotParseField",valueMeta.toStringMeta(),"" + pol,valueMeta.getConversionMask(),""+ rowNr );
                        
                        if (info.isErrorIgnored())
                        {
                            log.logDetailed(fname, Messages.getString("TextFileInput.Log.Warning")+": "  + message+" : " + e.getMessage());

                            value = null;

                            if (errorCount != null)
                            {
                                errorCount=new Long( errorCount.longValue()+1L );
                            }
                            if (errorFields != null)
                            {
                                StringBuilder sb = new StringBuilder(errorFields);
                                if (sb.length() > 0) sb.append("\t"); // TODO document this change
                                sb.append(valueMeta.getName());
                                errorFields = sb.toString();
                            }
                            if (errorText != null)
                            {
                                StringBuilder sb = new StringBuilder(errorText);
                                if (sb.length() > 0) sb.append(Const.CR);
                                sb.append(message);
                                errorText=sb.toString();
                            }
                            if (errorHandler != null)
                            {
                                errorHandler.handleLineError(textFileLine.lineNumber, AbstractFileErrorHandler.NO_PARTS);
                            }

                            if (info.isErrorLineSkipped()) r=null; // compensates for stmt: r.setIgnore();
                        }
                        else
                        {
                            throw new KettleException(message, e);
                        }
                    }
                }
                else
                {
                    // No data found: TRAILING NULLCOLS: add null value...
                    value = null;
                }

                // Now add value to the row (if we're not skipping the row)
                if ( r != null )
                {
                    r[fieldnr] = value;
                }
            }

            // Support for trailing nullcols!
            // Should be OK at allocation time, but it doesn't hurt :-)
            if (fieldnr < nrfields)
            {
                for (int i = fieldnr; i < info.getInputFields().length; i++)
                {
                    r[i] = null;
                }
            }

            // Add the error handling fields...
            int index = nrfields;
            if (errorCount != null) 
            {
                r[index]=errorCount;
                index++;
            }
            if (errorFields != null)
            {
                r[index]=errorFields;
                index++;
            }
            if (errorText != null)
            {
                r[index]=errorText;
                index++;
            }
            
            // Possibly add a filename...
            if (info.includeFilename())
            {
                r[index]=fname;
                index++;
            }

            // Possibly add a row number...
            if (info.includeRowNumber())
            {
                r[index] = new Long(rowNr);
                index++;
            }
        }
        catch (Exception e)
        {
            throw new KettleException(Messages.getString("TextFileInput.Log.Error.ErrorConvertingLineText"), e);
        }

        if (passThruFields != null)
            return RowDataUtil.addRowData(passThruFields, nrPassThruFields, r);
        else
            return r;

    }
                    
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		Object[] r = null;
		boolean retval = true;
		boolean putrow = false;

		if (first) // we just got started
		{
            first = false;
            
            data.outputRowMeta = new RowMeta();
            RowMetaInterface[] infoStep = null;
            
            if (meta.isAcceptingFilenames())
            {
                // Read the files from the specified input stream...
            	//
                data.files.getFiles().clear();
                
                int idx = -1;
                data.rowSet = findInputRowSet(meta.getAcceptingStepName());
                
                Object[] fileRow = getRowFrom(data.rowSet);
                while (fileRow!=null)
                {
                    RowMetaInterface prevInfoFields = data.rowSet.getRowMeta();
                    if (idx<0)
                    {
                        if (meta.isPassingThruFields())
                        {
                            data.passThruFields = new HashMap<FileObject, Object[]>();
                            infoStep = new RowMetaInterface[] { prevInfoFields };
                            data.nrPassThruFields = prevInfoFields.size();
                        }
                        idx = prevInfoFields.indexOfValue(meta.getAcceptingField());
                        if (idx<0)
                        {
                            logError(Messages.getString("TextFileInput.Log.Error.UnableToFindFilenameField", meta.getAcceptingField()));
                            setErrors(1);
                            stopAll();
                            return false;
                        }
                    }
                    String fileValue = prevInfoFields.getString(fileRow, idx);
                    try
                    {
                        FileObject fileObject = KettleVFS.getFileObject(fileValue);
                        data.files.addFile(fileObject);
                        if (meta.isPassingThruFields())
                            data.passThruFields.put(fileObject, fileRow);
                    }
                    catch(IOException e)
                    {
                        logError(Messages.getString("TextFileInput.Log.Error.UnableToCreateFileObject", fileValue));
                    }
                    
                    // Grab another row
                    fileRow = getRowFrom(data.rowSet);
                }
                
                if (data.files.nrOfFiles()==0)
                {
                    if (log.isDetailed()) logDetailed(Messages.getString("TextFileInput.Log.Error.NoFilesSpecified"));
                    setOutputDone();
                    return false;
                }
            }

            meta.getFields(data.outputRowMeta, getStepname(), infoStep, null, this); // get the metadata populated.  Simple and easy.
            // Create convert meta-data objects that will contain Date & Number formatters
            //
            data.convertRowMeta = data.outputRowMeta.clone();
            for (int i=0;i<data.convertRowMeta.size();i++) data.convertRowMeta.getValueMeta(i).setType(ValueMetaInterface.TYPE_STRING);

            
            handleMissingFiles();
            
			// Open the first file & read the required rows in the buffer, stop
			// if it fails...
			if (!openNextFile())
			{
				closeLastFile();
				setOutputDone();
				return false;
			}

			// Count the number of repeat fields...
			for (int i = 0; i < meta.getInputFields().length; i++)
			{
				if (meta.getInputFields()[i].isRepeated()) data.nr_repeats++;
			}
		}
		else
		{
			if (!data.doneReading)
			{
				int repeats = 1;
				if (meta.isLineWrapped()) repeats = meta.getNrWraps() > 0 ? meta.getNrWraps() : repeats;

				// Read a number of lines...
				for (int i = 0; i < repeats && !data.doneReading; i++)
				{
					String line = getLine(log, data.isr, data.fileFormatType, data.lineStringBuilder); // Get one line of data;
					if (line != null)
					{
						// Filter row?
						boolean isFilterLastLine = false;
						boolean filterOK = checkFilterRow(line, isFilterLastLine);
						if (filterOK)
						{
							// logRowlevel("LINE READ: "+line);
							data.lineBuffer.add(new TextFileLine(line, lineNumberInFile, data.file));
						} 
						else
						{
							if (isFilterLastLine)
							{
								data.doneReading = true;
							}
							repeats++; // grab another line, this one got filtered
						}
					}
					else
					{
						data.doneReading = true;
					}
				}
			}
		}

		/* If the buffer is empty: open the next file. 
		 * (if nothing in there, open the next, etc.)
		 */
		while (data.lineBuffer.size() == 0)
		{
			if (!openNextFile()) // Open fails: done processing!
			{
				closeLastFile();
				setOutputDone(); // signal end to receiver(s)
				return false;
			}
		}

		/* Take the first line available in the buffer & remove the line from
		   the buffer
		*/
		TextFileLine textLine = (TextFileLine) data.lineBuffer.get(0);
		incrementLinesInput();
		lineNumberInFile++;

		data.lineBuffer.remove(0);

		if (meta.isLayoutPaged())
		{
			/* Different rules apply: on each page:
			   a header
			   a number of data lines
			   a footer
			*/ 
			if (!data.doneWithHeader && data.pageLinesRead == 0) // We are reading header lines
			{
				if (log.isRowLevel()) logRowlevel("P-HEADER (" + data.headerLinesRead + ") : " + textLine.line);
				data.headerLinesRead++;
				if (data.headerLinesRead >= meta.getNrHeaderLines())
				{
					data.doneWithHeader = true;
				}
			}
			else
			// data lines or footer on a page
			{
				if (data.pageLinesRead < meta.getNrLinesPerPage())
				{
					// See if we are dealing with wrapped lines:
					if (meta.isLineWrapped())
					{
						for (int i = 0; i < meta.getNrWraps(); i++)
						{
							String extra = "";
							if (data.lineBuffer.size() > 0)
							{
								extra = ((TextFileLine) data.lineBuffer.get(0)).line;
								data.lineBuffer.remove(0);
							}
							textLine.line += extra;
						}
					}

					if (log.isRowLevel()) logRowlevel("P-DATA: " + textLine.line);
					// Read a normal line on a page of data.
					data.pageLinesRead++;
					data.lineInFile ++;
					long useNumber = meta.isRowNumberByFile() ? data.lineInFile : getLinesWritten() + 1;
					r = convertLineToRow(textLine, meta, data.currentPassThruFieldsRow, data.nrPassThruFields, data.outputRowMeta, data.convertRowMeta, data.filename, useNumber, data.dataErrorLineHandler);
					if (r != null) putrow = true;
					
					// Possible fix for bug PDI-1121 - paged layout header and line count off by 1 
					// We need to reset these BEFORE the next header line is read, so that it 
					// is treated as a header ... obviously, only if there is no footer, and we are 
					// done reading data. 
          if (!meta.hasFooter() && (data.pageLinesRead == meta.getNrLinesPerPage()))
          {
            /* OK, we are done reading the footer lines, start again
               on 'next page' with the header
             */
            data.doneWithHeader = false;
            data.headerLinesRead = 0;
            data.pageLinesRead = 0;
            data.footerLinesRead = 0;
            if (log.isRowLevel()) logRowlevel("RESTART PAGE");
          }
				}
				else
				// done reading the data lines, skip the footer lines
				{
					if (meta.hasFooter() && data.footerLinesRead < meta.getNrFooterLines())
					{
						if (log.isRowLevel()) logRowlevel("P-FOOTER: " + textLine.line);
						data.footerLinesRead++;
					}

					if (!meta.hasFooter() || data.footerLinesRead >= meta.getNrFooterLines())
					{
						/* OK, we are done reading the footer lines, start again
						   on 'next page' with the header
						 */
						data.doneWithHeader = false;
						data.headerLinesRead = 0;
						data.pageLinesRead = 0;
						data.footerLinesRead = 0;
						if (log.isRowLevel()) logRowlevel("RESTART PAGE");
					}
				}
			}
		}
		else
		// A normal data line, can also be a header or a footer line
		{
			if (!data.doneWithHeader) // We are reading header lines
			{
				data.headerLinesRead++;
				if (data.headerLinesRead >= meta.getNrHeaderLines())
				{
					data.doneWithHeader = true;
				}
			}
			else
			{
				/* IF we are done reading and we have a footer
				   AND the number of lines in the buffer is smaller then the number of footer lines
				   THEN we can remove the remaining rows from the buffer: they are all footer rows.
				 */
				if (data.doneReading && meta.hasFooter() && data.lineBuffer.size() < meta.getNrFooterLines())
				{
					data.lineBuffer.clear();
				}
				else
				// Not yet a footer line: it's a normal data line.
				{
					// See if we are dealing with wrapped lines:
					if (meta.isLineWrapped())
					{
						for (int i = 0; i < meta.getNrWraps(); i++)
						{
							String extra = "";
							if (data.lineBuffer.size() > 0)
							{
								extra = ((TextFileLine) data.lineBuffer.get(0)).line;
								data.lineBuffer.remove(0);
							}
							textLine.line += extra;
						}
					}
					if (data.filePlayList.isProcessingNeeded(textLine.file, textLine.lineNumber, AbstractFileErrorHandler.NO_PARTS))
					{
						data.lineInFile ++;
						long useNumber = meta.isRowNumberByFile() ? data.lineInFile : getLinesWritten() + 1;
						r = convertLineToRow(textLine, meta, data.currentPassThruFieldsRow, data.nrPassThruFields, data.outputRowMeta, data.convertRowMeta, data.filename, useNumber, data.dataErrorLineHandler);
						if (r != null)
						{
							if (log.isRowLevel()) logRowlevel("Found data row: "+data.outputRowMeta.getString(r));
							putrow = true;
						}
					}
					else putrow = false;
				}
			}
		}

		if (putrow && r != null)
		{
			// See if the previous values need to be repeated!
			if (data.nr_repeats > 0)
			{
				if (data.previous_row == null) // First invocation...
				{
					data.previous_row = data.outputRowMeta.cloneRow(r);
				}
				else
				{
					int repnr = 0;
					for (int i = 0; i < meta.getInputFields().length; i++)
					{
						if (meta.getInputFields()[i].isRepeated())
						{
							if (r[i]==null) // if it is empty: take the previous value!
							{
								r[i] = data.previous_row[i];
							}
							else
							// not empty: change the previous_row entry!
							{
								data.previous_row[i] = r[i];
							}
							repnr++;
						}
					}
				}
			}

			if (log.isRowLevel()) logRowlevel("Putting row: " + data.outputRowMeta.getString(r));			
			putRow(data.outputRowMeta, r);

			if ( getLinesInput() >= meta.getRowLimit() && meta.getRowLimit() >0 )
			{
			    closeLastFile();
			    setOutputDone(); // signal end to receiver(s)
			    return false;
			}
		}

        if (checkFeedback(getLinesInput())) 
        {
        	if(log.isBasic()) logBasic("linenr " + getLinesInput());
        }

		return retval;
	}

    /**
	 * Check if the line should be taken.
	 * @param line
	 * @param isFilterLastLine (dummy input param, only set when return value is false)
	 * @return true when the line should be taken (when false, isFilterLastLine will be set)
	 */
	private boolean checkFilterRow(String line, boolean isFilterLastLine) {
		boolean filterOK=true;
		
		// check for noEmptyLines
		if (meta.noEmptyLines() && line.length() == 0)
		{
			filterOK=false;
		} else {
			// check the filters
			filterOK = data.filterProcessor.doFilters(line);
			if ( ! filterOK )
			{
				if ( data.filterProcessor.isStopProcessing())
				{
				    data.doneReading = true;
				}
			}
		}
		
		return filterOK;
	}

	private void handleMissingFiles() throws KettleException
	{
		List<FileObject> nonExistantFiles = data.files.getNonExistantFiles();

		if (nonExistantFiles.size() != 0)
		{
			String message = FileInputList.getRequiredFilesDescription(nonExistantFiles);
			if(log.isBasic()) log.logBasic("Required files", "WARNING: Missing " + message);
			if (meta.isErrorIgnored()) {
				for (FileObject fileObject : nonExistantFiles) {
					data.dataErrorLineHandler.handleNonExistantFile(fileObject);
				}
			}
			else {
				throw new KettleException("Following required files are missing: " + message);
			}
		}

		List<FileObject> nonAccessibleFiles = data.files.getNonAccessibleFiles();
		if (nonAccessibleFiles.size() != 0)
		{
			String message = FileInputList.getRequiredFilesDescription(nonAccessibleFiles);
			if(log.isBasic()) log.logBasic("Required files", "WARNING: Not accessible " + message);
			if (meta.isErrorIgnored()) {
				for (FileObject fileObject : nonAccessibleFiles) {
					data.dataErrorLineHandler.handleNonAccessibleFile(fileObject);
				}
			} else {
				throw new KettleException("Following required files are not accessible: " + message);
			}
		}
	}

	private boolean closeLastFile()
	{
		try
		{
			// Close previous file!
			if (data.filename != null)
			{
                String sFileCompression = meta.getFileCompression();
				if (sFileCompression != null && sFileCompression.equals("Zip"))
				{
					data.zi.closeEntry();
					data.zi.close();
				}
				else if (sFileCompression != null && sFileCompression.equals("GZip"))
				{
					data.gzi.close();
				}
				data.fr.close();
				data.isr.close();
				data.filename = null; // send it down the next time.
				if ( data.file != null )
				{
					data.file.close();
					data.file = null;
				}
			}
			data.dataErrorLineHandler.close();
		}
		catch (Exception e)
		{
			logError("Couldn't close file : " + data.filename + " --> " + e.toString());
			stopAll();
			setErrors(1);
			return false;
		}
		finally
		{
			// This is for bug #5797 : it tries to assure that the file handle
			// is actually freed/garbarge collected.
			System.gc();
		}

		return !data.isLastFile;
	}

	private boolean openNextFile()
	{
		try
		{
			lineNumberInFile = 0;
			if (!closeLastFile()) return false;

			if (data.files.nrOfFiles() == 0) return false;

			// Is this the last file?
			data.isLastFile = (data.filenr == data.files.nrOfFiles() - 1);
			data.file = data.files.getFile(data.filenr);
			data.filename = KettleVFS.getFilename( data.file );
			data.lineInFile = 0;
            if (meta.isPassingThruFields())
                data.currentPassThruFieldsRow = data.passThruFields.get(data.file);
			
            // Add this files to the result of this transformation.
            //
			if(meta.isAddResultFile())
			{
				ResultFile resultFile = new ResultFile(ResultFile.FILE_TYPE_GENERAL, data.file, getTransMeta().getName(), toString());
				resultFile.setComment("File was read by an Text File input step");
				addResultFile(resultFile);
			}
			if(log.isBasic()) logBasic("Opening file: " + data.filename);

			data.fr = KettleVFS.getInputStream(data.file);
			data.dataErrorLineHandler.handleFile(data.file);

            String sFileCompression = meta.getFileCompression();
			if (sFileCompression != null && sFileCompression.equals("Zip"))
			{
			    if (log.isDetailed()) logDetailed("This is a zipped file");
				data.zi = new ZipInputStream(data.fr);
				data.zi.getNextEntry();

				if (meta.getEncoding() != null && meta.getEncoding().length() > 0)
				{
					data.isr = new InputStreamReader(new BufferedInputStream(data.zi, BUFFER_SIZE_INPUT_STREAM), meta.getEncoding());
				}
				else
				{
					data.isr = new InputStreamReader(new BufferedInputStream(data.zi, BUFFER_SIZE_INPUT_STREAM));
				}
			}
			else if (sFileCompression != null && sFileCompression.equals("GZip"))
			{
			    if (log.isDetailed()) logDetailed("This is a gzipped file");
				data.gzi = new GZIPInputStream(data.fr);

				if (meta.getEncoding() != null && meta.getEncoding().length() > 0)
				{
					data.isr = new InputStreamReader(new BufferedInputStream(data.gzi, BUFFER_SIZE_INPUT_STREAM), meta.getEncoding());
				}
				else
				{
					data.isr = new InputStreamReader(new BufferedInputStream(data.gzi, BUFFER_SIZE_INPUT_STREAM));
				}
			}
			else
			{
				if (meta.getEncoding() != null && meta.getEncoding().length() > 0)
				{
					data.isr = new InputStreamReader(new BufferedInputStream(data.fr, BUFFER_SIZE_INPUT_STREAM), meta.getEncoding());
				}
				else
				{
					data.isr = new InputStreamReader(new BufferedInputStream(data.fr, BUFFER_SIZE_INPUT_STREAM));
				}
			}

			// Move file pointer ahead!
			data.filenr++;

			// /////////////////////////////////////////////////////////////////////////////
			// Read the first lines...

			/* Keep track of the status of the file: are there any lines left to read?
			 */
			data.doneReading = false;

			/* OK, read a number of lines in the buffer:
			   The header rows
			   The nr rows in the page : optional
			   The footer rows
			 */
			int bufferSize = 1; 
			bufferSize += meta.hasHeader() ? meta.getNrHeaderLines() : 0;
			bufferSize += meta.isLayoutPaged() ? meta.getNrLinesPerPage() : 0;
			bufferSize += meta.hasFooter() ? meta.getNrFooterLines() : 0;

			// See if we need to skip the document header lines...
			if (meta.isLayoutPaged())
			{
				for (int i = 0; i < meta.getNrLinesDocHeader(); i++)
				{
					// Just skip these...
					getLine(log, data.isr, data.fileFormatType, data.lineStringBuilder); // header and footer: not wrapped
					lineNumberInFile++;
				}
			}

			String line;
			for (int i = 0; i < bufferSize && !data.doneReading; i++)
			{
				line = getLine(log, data.isr, data.fileFormatType, data.lineStringBuilder);
				if (line != null)
				{
					// when there is no header, check the filter for the first line
					if (!meta.hasHeader())
					{
						// Filter row?
						boolean isFilterLastLine = false;
						boolean filterOK = checkFilterRow(line, isFilterLastLine);
						if (filterOK)
						{
							data.lineBuffer.add(new TextFileLine(line, lineNumberInFile, data.file)); // Store it in the
							// line buffer...
						} 
						else
						{
							bufferSize++; // grab another line, this one got filtered
						}
					}
					else //there is a header, so don't checkFilterRow
					{
						if (!meta.noEmptyLines() || line.length() != 0)
						{
						    data.lineBuffer.add(new TextFileLine(line, lineNumberInFile, data.file)); // Store it in the line buffer...
						}						
					}
				}
				else
				{
					data.doneReading = true;
				}
			}

			// Reset counters etc.
			data.headerLinesRead = 0;
			data.footerLinesRead = 0;
			data.pageLinesRead = 0;

			// Set a flags
			data.doneWithHeader = !meta.hasHeader();
		}
		catch (Exception e)
		{
			logError("Couldn't open file #" + data.filenr + " : " + data.filename + " --> " + e.toString());
			stopAll();
			setErrors(1);
			return false;
		}
		return true;
	}

	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta = (TextFileInputMeta) smi;
		data = (TextFileInputData) sdi;

		if (super.init(smi, sdi))
		{
			initErrorHandling();
			initReplayFactory();
				
			data.files = meta.getTextFileList(this);
			data.filterProcessor = new TextFileFilterProcessor(meta.getFilter());
            
            // If there are missing files, fail if we don't ignore errors
            //
			if ( (transmeta.getPreviousResult()==null || transmeta.getPreviousResult().getResultFiles()==null || transmeta.getPreviousResult().getResultFiles().size()==0) && 
                  data.files.nrOfMissingFiles() > 0 && !meta.isAcceptingFilenames() && !meta.isErrorIgnored()
               )
			{
				logError(Messages.getString("TextFileInput.Log.Error.NoFilesSpecified"));
				return false;
			}
            
			String clusterSize = getVariable(Const.INTERNAL_VARIABLE_CLUSTER_SIZE);
            if (!Const.isEmpty(clusterSize) && Integer.valueOf(clusterSize) > 1)
            {
                // TODO: add metadata to configure this.
                String nr = getVariable(Const.INTERNAL_VARIABLE_SLAVE_SERVER_NUMBER);
                if (log.isDetailed()) logDetailed("Running on slave server #"+nr+" : assuming that each slave reads a dedicated part of the same file(s)."); 
            }
            
            // If no nullif field is supplied, take the default.
            // String null_value = nullif;
            // if (null_value == null)
            // {
            // //     value="";
            // }
            // String null_cmp = Const.rightPad(new StringBuilder(null_value), pol.length());

            // calculate the file format type in advance so we can use a switch
            data.fileFormatType = meta.getFileFormatTypeNr();

            // calculate the file type in advance CSV or Fixed?
            data.fileType = meta.getFileTypeNr();
                
			return true;
		}
		return false;
	}

	private void initReplayFactory()
	{
		Date replayDate = getTrans().getReplayDate();
		if (replayDate == null) data.filePlayList = FilePlayListAll.INSTANCE;
		else data.filePlayList = new FilePlayListReplay(replayDate, meta.getLineNumberFilesDestinationDirectory(),
				meta.getLineNumberFilesExtension(), meta.getErrorFilesDestinationDirectory(), meta.getErrorLineFilesExtension(), meta.getEncoding());
	}

	private void initErrorHandling()
	{
		List<FileErrorHandler> dataErrorLineHandlers = new ArrayList<FileErrorHandler>(2);
		if (meta.getLineNumberFilesDestinationDirectory() != null)
			dataErrorLineHandlers.add(new FileErrorHandlerContentLineNumber(getTrans().getCurrentDate(), meta
					.getLineNumberFilesDestinationDirectory(), meta.getLineNumberFilesExtension(), meta.getEncoding(), this));
		if (meta.getErrorFilesDestinationDirectory() != null)
			dataErrorLineHandlers.add(new FileErrorHandlerMissingFiles(getTrans().getCurrentDate(), meta.getErrorFilesDestinationDirectory(), meta
					.getErrorLineFilesExtension(), meta.getEncoding(), this));
		data.dataErrorLineHandler = new CompositeFileErrorHandler(dataErrorLineHandlers);
	}

	public void dispose(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta = (TextFileInputMeta) smi;
		data = (TextFileInputData) sdi;

		if(data.file!=null) 
		{
			try{
			data.file.close();
			}catch (Exception e){}
		}
		super.dispose(smi, sdi);
		
		// This is for bug #5797 : it tries to assure that the file handle
		// is actually freed/garbarge collected.
		System.gc();
	}

	//
	// Run is were the action happens!
	public void run()
	{
    	BaseStep.runStepThread(this, meta, data);
	}
}
