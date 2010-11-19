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
package org.pentaho.di.core.logging;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.vfs.FileObject;
import org.apache.log4j.Appender;
import org.apache.log4j.Layout;
import org.apache.log4j.spi.ErrorHandler;
import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LoggingEvent;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.vfs.KettleVFS;

public class Log4jFileAppender implements Appender
{
    private Layout layout;
    private Filter filter;
    
    private FileObject file;
    
    private String  name;
    
    private OutputStream fileOutputStream;
    
    public Log4jFileAppender(FileObject file) throws IOException
    {
        this.file = file;
        
        fileOutputStream = KettleVFS.getOutputStream(file, false);
    }
    public Log4jFileAppender(FileObject file,boolean append) throws IOException
    {
        this.file = file;
        
        fileOutputStream = KettleVFS.getOutputStream(file, append);
    }
    public void addFilter(Filter filter)
    {
        this.filter = filter;
    }

    public Filter getFilter()
    {
        return filter;
    }

    public void clearFilters()
    {
        filter=null;
    }

    public void close()
    {
        try
        {
            fileOutputStream.close();
        }
        catch(IOException e)
        {
            System.out.println("Unable to close Logging file ["+file.getName()+"] : "+e.getMessage());
        }
    }

    public void doAppend(LoggingEvent event)
    {
        String line = layout.format(event)+Const.CR;
        try
        {
            fileOutputStream.write(line.getBytes(Const.XML_ENCODING));
        }
        catch(IOException e)
        {
            System.out.println("Unable to close Logging file ["+file.getName()+"] : "+e.getMessage());
        }
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    public void setErrorHandler(ErrorHandler arg0)
    {
    }

    public ErrorHandler getErrorHandler()
    {
        return null;
    }

    public void setLayout(Layout layout)
    {
        this.layout = layout;
    }

    public Layout getLayout()
    {
        return layout;
    }

    public boolean requiresLayout()
    {
        return true;
    }

    public FileObject getFile()
    {
        return file;
    }

    public void setFilename(FileObject file)
    {
        this.file = file;
    }

    public OutputStream getFileOutputStream()
    {
        return fileOutputStream;
    }

    public void setFileOutputStream(OutputStream fileOutputStream)
    {
        this.fileOutputStream = fileOutputStream;
    }

    public void setFilter(Filter filter)
    {
        this.filter = filter;
    }
}
