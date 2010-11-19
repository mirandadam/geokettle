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
package org.pentaho.di.core;

import org.pentaho.di.repository.RepositoryDirectory;


public class LastUsedFile
{
    public static final String FILE_TYPE_TRANSFORMATION = "Trans";
    public static final String FILE_TYPE_JOB            = "Job";
    public static final String FILE_TYPE_SCHEMA         = "Schema";
    
    public static final int OPENED_ITEM_TYPE_MASK_NONE    = 0;
    public static final int OPENED_ITEM_TYPE_MASK_GRAPH   = 1;
    public static final int OPENED_ITEM_TYPE_MASK_LOG     = 2;
    public static final int OPENED_ITEM_TYPE_MASK_HISTORY = 4;
    
    private String  fileType;
    private String  filename;
    private String  directory;
    private boolean sourceRepository;
    private String  repositoryName;
    
    private boolean opened;
    private int     openItemTypes;
    
    /**
     * @param fileType The type of file to use (FILE_TYPE_TRANSFORMATION, FILE_TYPE_JOB, ...)
     * @param filename
     * @param directory
     * @param sourceRepository
     * @param repositoryName
     * @param opened
     * @param openItemTypes
     */
    public LastUsedFile(String fileType, String filename, String directory, boolean sourceRepository, String repositoryName, boolean opened, int openItemTypes)
    {
        this.fileType = fileType;
        this.filename = filename;
        this.directory = directory;
        this.sourceRepository = sourceRepository;
        this.repositoryName = repositoryName;
        this.opened = opened;
        this.openItemTypes = openItemTypes;
    }
    
    public String toString()
    {
        String string = "";
        
        if (sourceRepository && !Const.isEmpty(directory) && !Const.isEmpty(repositoryName))
        {
            string+="["+repositoryName+"] "; 
            
            if (directory.endsWith(RepositoryDirectory.DIRECTORY_SEPARATOR))
            {
                string+=": "+directory+filename;
            }
            else
            {
                string+=": "+RepositoryDirectory.DIRECTORY_SEPARATOR+filename;
            }
        }
        else
        {
            string+=filename;
        }
            
        return string;
    }
    
    public int hashCode()
    {
        return (getFileType()+toString()).hashCode();
    }
    
    public boolean equals(Object obj)
    {
        LastUsedFile file = (LastUsedFile) obj;
        return getFileType().equals(file.getFileType()) && toString().equals(file.toString());
    }

    /**
     * @return the directory
     */
    public String getDirectory()
    {
        return directory;
    }

    /**
     * @param directory the directory to set
     */
    public void setDirectory(String directory)
    {
        this.directory = directory;
    }

    /**
     * @return the filename
     */
    public String getFilename()
    {
        return filename;
    }

    /**
     * @param filename the filename to set
     */
    public void setFilename(String filename)
    {
        this.filename = filename;
    }

    /**
     * @return the repositoryName
     */
    public String getRepositoryName()
    {
        return repositoryName;
    }

    /**
     * @param repositoryName the repositoryName to set
     */
    public void setRepositoryName(String repositoryName)
    {
        this.repositoryName = repositoryName;
    }

    /**
     * @return the sourceRepository
     */
    public boolean isSourceRepository()
    {
        return sourceRepository;
    }

    /**
     * @param sourceRepository the sourceRepository to set
     */
    public void setSourceRepository(boolean sourceRepository)
    {
        this.sourceRepository = sourceRepository;
    }

    /**
     * @return the fileType
     */
    public String getFileType()
    {
        return fileType;
    }

    /**
     * @param fileType the fileType to set
     */
    public void setFileType(String fileType)
    {
        this.fileType = fileType;
    }
    
    public boolean isTransformation()
    {
        return FILE_TYPE_TRANSFORMATION.equalsIgnoreCase(fileType);
    }

    public boolean isJob()
    {
        return FILE_TYPE_JOB.equalsIgnoreCase(fileType);
    }
    
    public boolean isSchema()
    {
        return FILE_TYPE_SCHEMA.equalsIgnoreCase(fileType);
    }

	/**
	 * @return the opened
	 */
	public boolean isOpened() {
		return opened;
	}

	/**
	 * @param opened the opened to set
	 */
	public void setOpened(boolean opened) {
		this.opened = opened;
	}

	/**
	 * @return the openItemTypes
	 */
	public int getOpenItemTypes() {
		return openItemTypes;
	}

	/**
	 * @param openItemTypes the openItemTypes to set
	 */
	public void setOpenItemTypes(int openItemTypes) {
		this.openItemTypes = openItemTypes;
	}



}
