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
package org.pentaho.di.core.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Vector;

import org.pentaho.di.core.logging.LogWriter;


public class SortedFileOutputStream extends FileOutputStream
{
    /** Internal buffer to catch output. Before really writing output, the properties get sorted. */
    private StringBuffer sb = null;

    /** Logger, for the few errors that may occur. */
    private LogWriter log = null;

    /**
     * CT
     * 
     * @param file
     * @throws FileNotFoundException
     */
    public SortedFileOutputStream(File file) throws FileNotFoundException
    {
        super(file);
    }

    /**
     * Setter
     * 
     * @param log
     */
    public void setLogger(LogWriter log)
    {
        this.log = log;
    }

    /**
     * Appending to internal StringBuffer, instead of immediately writing to the file
     */
    public void write(byte[] b, int off, int len) throws IOException
    {
        if (sb == null)
            sb = new StringBuffer();
        sb.append(new String(b, off, len));
    }

    /**
     * Appending to internal StringBuffer, instead of immediately writing to the file
     */
    public void write(byte[] b) throws IOException
    {
        if (sb == null)
            sb = new StringBuffer();
        sb.append(new String(b));
    }

    /**
     * Appending to internal StringBuffer, instead of immediately writing to the file
     */
    public void write(int b) throws IOException
    {
        if (sb == null)
            sb = new StringBuffer();
        sb.append(b);
    }

    /**
     * Catch <code>flush</code> method, don't do nothing
     */
    public void flush() throws IOException
    {
    }

    /**
     * If internally stored content is available, sorting keys of content, then sending content to
     * file. Then calling {@link FileOutputStream#close()} method.
     */
    public void close() throws IOException
    {
        if (sb == null || sb.length() == 0)
        {
            super.flush();
            super.close();
        }

        int[] iPos = new int[1];
        iPos[0] = 0;
        String sLine = nextLine(iPos);

        Vector<String> lines = new Vector<String>();
        while (sLine != null)
        {
            // Length 0 -> do nothing
            if (sLine.length() == 0)
            {
                sLine = nextLine(iPos);
                continue;
            }

            // Character at first position is a '#' -> this is a comment
            if (sLine.charAt(0) == '#')
            {
                super.write(sLine.getBytes());
                sLine = nextLine(iPos);
                continue;
            }

            // Get first occurrence of '=' character, that is not a position 0 and not
            // escaped by a '\\'
            int idx = sLine.indexOf('=');
            if (idx <= 0)
            {
                // '=' either does not exist or is at first position (that should never happen!).
                // Write line immediately
                log.logError(this.getClass().getName(),
                    "Unexpected: '=' character not found or found at first position.");
                super.write(sLine.getBytes());
            }
            else
            {
                while (idx != -1 && sLine.charAt(idx - 1) == '\\')
                    idx = sLine.indexOf('=', idx + 1);

                if (idx == -1)
                {
                    log.logError(this.getClass().getName(),
                        "Unexpected: No '=' character found that is not escaped by a '\\'.");
                    super.write(sLine.getBytes());
                }
                else
                {
                    lines.add(sLine);
                }
            }
            sLine = nextLine(iPos);
        }

        Collections.sort(lines);
        for (String line : lines)
            super.write( line.getBytes() );

        super.flush();
        super.close();
    }

    /**
     * Get next line. The line end is marked at the first occurrence of an unescaped '\n' or '\r'
     * character. All following '\n' or '\r' characters after the first unescaped '\n' or '\r'
     * character are included in the line.
     * 
     * @param iPos The position from where to start at. This is passed as array of size one to
     *            <i>pass back</i> the parsing position (kind of C++ reference pass)
     * @return
     */
    private String nextLine(int[] iPos)
    {
        // End of StringBuffer reached?
        if (iPos[0] >= sb.length())
            return null;

        // Remember start
        int iStart = iPos[0];
        char c = sb.charAt(iPos[0]);

        // Read until end of stream reached or first '\n' or '\r' character found
        while (iPos[0] < sb.length() && c != '\n' && c != '\r')
        {
            c = sb.charAt(iPos[0]++);

            // If now we have '\r' or '\n' and they are escaped, we just read the next
            // character. For this at least two characters must have been read.
            if (iPos[0] >= 2)
            {
                // Is it an escaped '\r' or '\n'?
                if ((c == '\n' || c == '\r') && (iPos[0] - 2 == '\\'))
                {
                    // Yes! Just read next character, if not end of stream reached
                    if (iPos[0] < sb.length())
                        c = sb.charAt(iPos[0]++);
                }
            }
        }

        // Either we've found a '\r' or '\n' character or we are at the end of the stream.
        // In either case return.
        if (iPos[0] == sb.length())
        {
            // Return complete remainder
            return sb.substring(iStart);
        }
        else
        {
            // Consume characters as long as '\r' or '\n' is found.
            while (iPos[0] < sb.length() && (c == '\n' || c == '\r'))
                c = sb.charAt(iPos[0]++);

            // Return complete remainder or part of stream
            if (iPos[0] == sb.length())
                return sb.substring(iStart);
            else
            {
                iPos[0]--;
                return sb.substring(iStart, iPos[0]);
            }
        }
    }
}
