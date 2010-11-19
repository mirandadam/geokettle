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

import java.util.Date;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectory;

public interface EngineMetaInterface {

	public void setFilename(String filename);
	
	public String getName();
	
	public void nameFromFilename();
	
	public void clearChanged();
	
	public String getXML() throws KettleException;
	
	public String getFileType();
	
    public String[] getFilterNames();

    public String[] getFilterExtensions();
    
    public String getDefaultExtension();
    
    public void setID( long id );
 
    public Date getCreatedDate();
    
    public void setCreatedDate(Date date);
    
    public String getCreatedUser();
    
    public void setCreatedUser(String createduser);
    
    public Date getModifiedDate();
    
    public void setModifiedDate(Date date);
    
    public void setModifiedUser( String user );
    
    public String getModifiedUser( );
    
    public RepositoryDirectory getDirectory();
    
    public boolean showReplaceWarning(Repository rep);
    
    public void saveRep(Repository rep, ProgressMonitorListener monitor) throws KettleException;
    
    public String getFilename();
    
    public boolean saveSharedObjects();
    
    public void setInternalKettleVariables();
}
