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
package org.pentaho.di.ui.repository;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.TreeItem;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectory;
import org.pentaho.di.repository.RepositoryObject;
import org.pentaho.di.ui.core.gui.GUIResource;

public class RepositoryDirectoryUI {

    /**
     * Set the name of this directory on a TreeItem. 
     * Also, create children on this TreeItem to reflect the subdirectories.
     * In these sub-directories, fill in the available transformations from the repository.

     * @param ti The TreeItem to set the name on and to create the subdirectories
     * @param rep The repository
     * @param dircolor The color in which the directories will be drawn.
     * @param sortPosition The sort position
     * @param ascending The ascending flag
     * @param getTransformations Include transformations in the tree or not
     * @param getJobs Include jobs in the tree or not
     * @throws KettleDatabaseException
     */
	public static void getTreeWithNames(TreeItem ti, Repository rep, Color dircolor, int sortPosition, boolean ascending, boolean getTransformations, boolean getJobs, RepositoryDirectory dir) throws KettleDatabaseException
	{
		ti.setText(dir.getDirectoryName());
		ti.setForeground(dircolor);
		
		// First, we draw the directories
		for (int i=0;i<dir.getNrSubdirectories();i++)
		{
			RepositoryDirectory subdir = dir.getSubdirectory(i);
			TreeItem subti = new TreeItem(ti, SWT.NONE);
			subti.setImage(GUIResource.getInstance().getImageArrow());
			getTreeWithNames(subti, rep, dircolor, sortPosition, ascending, getTransformations, getJobs, subdir);
		}
		
		try
		{
            List<RepositoryObject> repositoryObjects = new ArrayList<RepositoryObject>();
            
			// Then show the transformations & jobs in that directory...
            if (getTransformations)
            {
                List<RepositoryObject> repositoryTransformations = rep.getTransformationObjects(dir.getID());
                if (repositoryTransformations!=null)
                {
                    repositoryObjects.addAll(repositoryTransformations);
                }
            }
            if (getJobs)
            {
                List<RepositoryObject> repositoryJobs = rep.getJobObjects(dir.getID());
                if (repositoryJobs!=null)
                {
                    repositoryObjects.addAll(repositoryJobs);
                }
            }
            
            // Sort the directory list appropriately...
            //
            RepositoryObject.sortRepositoryObjects(repositoryObjects, sortPosition, ascending);
            
            for (int i=0;i<repositoryObjects.size();i++)
            {
                TreeItem tiObject = new TreeItem(ti, SWT.NONE);
                RepositoryObject repositoryObject = (RepositoryObject)repositoryObjects.get(i);
                if(repositoryObject.getObjectType()==RepositoryObject.STRING_OBJECT_TYPE_TRANSFORMATION)
                	tiObject.setImage(GUIResource.getInstance().getImageTransGraph());
                else if(repositoryObject.getObjectType()==RepositoryObject.STRING_OBJECT_TYPE_JOB)
                	tiObject.setImage(GUIResource.getInstance().getImageJobGraph());
                
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                tiObject.setText(0, Const.NVL(repositoryObject.getName(), ""));
                tiObject.setText(1, Const.NVL(repositoryObject.getObjectType(), ""));
                tiObject.setText(2, Const.NVL(repositoryObject.getModifiedUser(), ""));
                tiObject.setText(3, repositoryObject.getModifiedDate()!=null ? simpleDateFormat.format(repositoryObject.getModifiedDate()) : "");
                tiObject.setText(4, Const.NVL(repositoryObject.getDescription(), ""));

            }

		}
		catch(KettleException dbe)
		{
            throw new KettleDatabaseException("Unable to populate tree with repository objects", dbe);
		}

		ti.setExpanded(dir.isRoot());
	}
	
	/**
	 * Gets a directory tree on a TreeItem to work with.
	 * @param ti The TreeItem to set the directory tree on
	 * @param dircolor The color of the directory tree item.
	 */
	public static void getDirectoryTree(TreeItem ti, Color dircolor, RepositoryDirectory dir)
	{
		ti.setText(dir.getDirectoryName());
		ti.setForeground(dircolor);
		
		// First, we draw the directories
		for (int i=0;i<dir.getNrSubdirectories();i++)
		{
			RepositoryDirectory subdir = dir.getSubdirectory(i);
			TreeItem subti = new TreeItem(ti, SWT.NONE);
			subti.setImage(GUIResource.getInstance().getImageArrow());
			getDirectoryTree(subti, dircolor, subdir);
		}
	}
	

}
