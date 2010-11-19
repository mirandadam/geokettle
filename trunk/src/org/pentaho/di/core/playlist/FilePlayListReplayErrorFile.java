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
package org.pentaho.di.core.playlist;

import java.io.IOException;

import org.apache.commons.vfs.FileObject;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.trans.step.errorhandling.AbstractFileErrorHandler;


public class FilePlayListReplayErrorFile extends FilePlayListReplayFile {

	private FileObject errorFile;

	public FilePlayListReplayErrorFile(FileObject errorFile, FileObject processingFile) {
		super(processingFile, AbstractFileErrorHandler.NO_PARTS);
		this.errorFile = errorFile;
	}

	public boolean isProcessingNeeded(FileObject file, long lineNr, String filePart)
			throws KettleException {
        try
        {
            return errorFile.exists();
        }
        catch(IOException e)
        {
            throw new KettleException(e);
        }
	}

}
