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
package org.pentaho.di.ui.spoon;

import org.pentaho.di.core.EngineMetaInterface;
import org.pentaho.di.core.LastUsedFile;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.w3c.dom.Node;

public class JobFileListener implements FileListener {

    public boolean open(Node jobNode, String fname, boolean importfile)
    {
    	Spoon spoon = Spoon.getInstance();
        try
        {
            JobMeta jobMeta = new JobMeta(spoon.getLog());
            jobMeta.loadXML(jobNode, spoon.getRepository(), spoon);
            spoon.setJobMetaVariables(jobMeta);
            spoon.getProperties().addLastFile(LastUsedFile.FILE_TYPE_JOB, fname, null, false, null);
            spoon.addMenuLast();
            if (!importfile) jobMeta.clearChanged();
            jobMeta.setFilename(fname);
            spoon.delegates.jobs.addJobGraph(jobMeta);
            
            spoon.refreshTree();
            spoon.refreshHistory();
            return true;
            
        }
        catch(KettleException e)
        {
            new ErrorDialog(spoon.getShell(), Messages.getString("Spoon.Dialog.ErrorOpening.Title"), Messages.getString("Spoon.Dialog.ErrorOpening.Message")+fname, e);
        }
        return false;
    }

    public boolean save(EngineMetaInterface meta, String fname,boolean export) {
    	Spoon spoon = Spoon.getInstance();
    	
    	EngineMetaInterface lmeta;
    	if (export)
    	{
    		lmeta = (JobMeta)((JobMeta)meta).realClone(false);
    	}
    	else
    		lmeta = meta;
    	
    	return spoon.saveMeta(lmeta, fname);
    }
    
    public void syncMetaName(EngineMetaInterface meta,String name) {
    	((JobMeta)meta).setName(name);
    }
}
