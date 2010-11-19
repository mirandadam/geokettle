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

package org.pentaho.di.job.entries.deletefolders;

import static org.pentaho.di.job.entry.validator.AbstractFileValidator.putVariableSpace;
import static org.pentaho.di.job.entry.validator.AndValidator.putValidators;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.andValidator;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.fileExistsValidator;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.notNullValidator;

import java.io.IOException;
import java.util.List;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSelectInfo;
import org.apache.commons.vfs.FileSelector;
import org.apache.commons.vfs.FileType;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobEntryType;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.job.entry.validator.ValidatorContext;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.resource.ResourceEntry;
import org.pentaho.di.resource.ResourceReference;
import org.pentaho.di.resource.ResourceEntry.ResourceType;
import org.w3c.dom.Node;

/**
 * This defines a 'delete folders' job entry.
 *
 * @author Samatar Hassan
 * @since 13-05-2008
 */
public class JobEntryDeleteFolders extends JobEntryBase implements Cloneable, JobEntryInterface {

  public boolean argFromPrevious;

  public String arguments[];
  
  private String success_condition;
  public  String SUCCESS_IF_AT_LEAST_X_FOLDERS_DELETED="success_when_at_least";
  public  String SUCCESS_IF_ERRORS_LESS="success_if_errors_less";
  public  String SUCCESS_IF_NO_ERRORS="success_if_no_errors";
  
  private String limit_folders;
  
	
	int NrErrors=0;
	int NrSuccess=0;
	boolean successConditionBroken=false;
	boolean successConditionBrokenExit=false;
	int limitFolders=0;


  public JobEntryDeleteFolders(String n) {
    super(n, ""); //$NON-NLS-1$
    argFromPrevious = false;
    arguments = null;

    success_condition=SUCCESS_IF_NO_ERRORS;
    limit_folders="10";
    setID(-1L);
    setJobEntryType(JobEntryType.DELETE_FOLDERS);
  }

  public JobEntryDeleteFolders() {
    this(""); //$NON-NLS-1$
  }

  public JobEntryDeleteFolders(JobEntryBase jeb) {
    super(jeb);
  }

  public Object clone() {
    JobEntryDeleteFolders je = (JobEntryDeleteFolders) super.clone();
    return je;
  }

  public String getXML() {
    StringBuffer retval = new StringBuffer(300);

    retval.append(super.getXML());
    retval.append("      ").append(XMLHandler.addTagValue("arg_from_previous", argFromPrevious)); //$NON-NLS-1$ //$NON-NLS-2$
    retval.append("      ").append(XMLHandler.addTagValue("success_condition", success_condition)); //$NON-NLS-1$ //$NON-NLS-2$
	retval.append("      ").append(XMLHandler.addTagValue("limit_folders", limit_folders));
	
    retval.append("      <fields>").append(Const.CR); //$NON-NLS-1$
    if (arguments != null) {
      for (int i = 0; i < arguments.length; i++) {
        retval.append("        <field>").append(Const.CR); //$NON-NLS-1$
        retval.append("          ").append(XMLHandler.addTagValue("name", arguments[i])); //$NON-NLS-1$ //$NON-NLS-2$
        retval.append("        </field>").append(Const.CR); //$NON-NLS-1$
      }
    }
    retval.append("      </fields>").append(Const.CR); //$NON-NLS-1$

    return retval.toString();
  }

  public void loadXML(Node entrynode, List<DatabaseMeta> databases, List<SlaveServer> slaveServers, Repository rep) throws KettleXMLException {
    try {
      super.loadXML(entrynode, databases, slaveServers);
      argFromPrevious = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "arg_from_previous")); //$NON-NLS-1$ //$NON-NLS-2$
      success_condition          = XMLHandler.getTagValue(entrynode, "success_condition");
      limit_folders          = XMLHandler.getTagValue(entrynode, "limit_folders");
      
      Node fields = XMLHandler.getSubNode(entrynode, "fields"); //$NON-NLS-1$

      // How many field arguments?
      int nrFields = XMLHandler.countNodes(fields, "field"); //$NON-NLS-1$
      arguments = new String[nrFields];

      // Read them all...
      for (int i = 0; i < nrFields; i++) {
        Node fnode = XMLHandler.getSubNodeByNr(fields, "field", i); //$NON-NLS-1$

        arguments[i] = XMLHandler.getTagValue(fnode, "name"); //$NON-NLS-1$
      }
    } catch (KettleXMLException xe) {
      throw new KettleXMLException(Messages.getString("JobEntryDeleteFolders.UnableToLoadFromXml"), xe); //$NON-NLS-1$
    }
  }

  public void loadRep(Repository rep, long id_jobentry, List<DatabaseMeta> databases, List<SlaveServer> slaveServers) throws KettleException {
    try {
      super.loadRep(rep, id_jobentry, databases, slaveServers);
      argFromPrevious = rep.getJobEntryAttributeBoolean(id_jobentry, "arg_from_previous"); //$NON-NLS-1$
      limit_folders  = rep.getJobEntryAttributeString(id_jobentry, "limit_folders");
		success_condition  = rep.getJobEntryAttributeString(id_jobentry, "success_condition");

      // How many arguments?
      int argnr = rep.countNrJobEntryAttributes(id_jobentry, "name"); //$NON-NLS-1$
      arguments = new String[argnr];

      // Read them all...
      for (int a = 0; a < argnr; a++) {
        arguments[a] = rep.getJobEntryAttributeString(id_jobentry, a, "name"); //$NON-NLS-1$
      }
    } catch (KettleException dbe) {
      throw new KettleException(Messages.getString("JobEntryDeleteFolders.UnableToLoadFromRepo", String.valueOf(id_jobentry)), dbe); //$NON-NLS-1$
    }
  }

  public void saveRep(Repository rep, long id_job) throws KettleException {
    try {
      super.saveRep(rep, id_job);

      rep.saveJobEntryAttribute(id_job, getID(), "arg_from_previous", argFromPrevious); //$NON-NLS-1$
	  rep.saveJobEntryAttribute(id_job, getID(), "limit_folders",      limit_folders);
	  rep.saveJobEntryAttribute(id_job, getID(), "success_condition",      success_condition);

      // save the arguments...
      if (arguments != null) {
        for (int i = 0; i < arguments.length; i++) {
          rep.saveJobEntryAttribute(id_job, getID(), i, "name", arguments[i]); //$NON-NLS-1$
        }
      }
    } catch (KettleDatabaseException dbe) {
      throw new KettleException(
          Messages.getString("JobEntryDeleteFolders.UnableToSaveToRepo", String.valueOf(id_job)), dbe); //$NON-NLS-1$
    }
  }

  public Result execute(Result result, int nr, Repository rep, Job parentJob) throws KettleException {
    LogWriter log = LogWriter.getInstance();

    List<RowMetaAndData> rows = result.getRows();
    RowMetaAndData resultRow = null;

    result.setNrErrors(1);
    result.setResult(false);
    
	NrErrors=0;
	NrSuccess=0;
	successConditionBroken=false;
	successConditionBrokenExit=false;
	limitFolders=Const.toInt(environmentSubstitute(getLimitFolders()),10);


    if (argFromPrevious) {
      if(log.isDetailed())	
    	  log.logDetailed(toString(), Messages.getString("JobEntryDeleteFolders.FoundPreviousRows", String.valueOf((rows != null ? rows.size() : 0)))); //$NON-NLS-1$
    }

    if (argFromPrevious && rows != null){
      for (int iteration = 0; iteration < rows.size() && !parentJob.isStopped(); iteration++) {
		if(successConditionBroken){
			log.logError(toString(), Messages.getString("JobEntryDeleteFolders.Error.SuccessConditionbroken",""+NrErrors));
			result.setNrErrors(NrErrors);
			result.setNrLinesDeleted(NrSuccess);
			return result;
		}
    	resultRow = rows.get(iteration);
        String args_previous = resultRow.getString(0, null);
        if(!Const.isEmpty(args_previous)){
	        if(deleteFolder(args_previous)){
	        	updateSuccess();
	        }else {
	        	updateErrors();	
	        }
        }else{
        	// empty filename !
        	log.logError(toString(), Messages.getString("JobEntryDeleteFolders.Error.EmptyLine"));
        }
      }
    } else if (arguments != null) {
      for (int i = 0; i < arguments.length && !parentJob.isStopped(); i++) {
  		if(successConditionBroken)
		{
			log.logError(toString(), Messages.getString("JobEntryDeleteFolders.Error.SuccessConditionbroken",""+NrErrors));
			result.setNrErrors(NrErrors);
			result.setNrLinesDeleted(NrSuccess);
			return result;
		}
  		String realfilename=environmentSubstitute(arguments[i]);
	    if(!Const.isEmpty(realfilename))
	    {
    	  if(deleteFolder(realfilename)){
          	updateSuccess();
          }else {
        	  updateErrors();
          }  
	    }else{
         // empty filename !
         log.logError(toString(), Messages.getString("JobEntryDeleteFolders.Error.EmptyLine"));
	   }
      }
    }
   
	if(log.isDetailed()){
		log.logDetailed(toString(), "=======================================");
		log.logDetailed(toString(), Messages.getString("JobEntryDeleteFolders.Log.Info.NrError","" + NrErrors));
		log.logDetailed(toString(), Messages.getString("JobEntryDeleteFolders.Log.Info.NrDeletedFolders","" + NrSuccess));
		log.logDetailed(toString(), "=======================================");
	}
    
	result.setNrErrors(NrErrors);
	result.setNrLinesDeleted(NrSuccess);
	if(getSuccessStatus())	result.setResult(true);
	
    return result;
  }
	private void updateErrors()
	{
		NrErrors++;
		if(checkIfSuccessConditionBroken()){
			// Success condition was broken
			successConditionBroken=true;
		}
	}
	private boolean checkIfSuccessConditionBroken()
	{
		boolean retval=false;
		if ((NrErrors>0 && getSuccessCondition().equals(SUCCESS_IF_NO_ERRORS))
				|| (NrErrors>=limitFolders && getSuccessCondition().equals(SUCCESS_IF_ERRORS_LESS))){
			retval=true;	
		}
		return retval;
	}
	private void updateSuccess()
	{
		NrSuccess++;
	}
	private boolean getSuccessStatus()
	{
		boolean retval=false;
		
		if ((NrErrors==0 && getSuccessCondition().equals(SUCCESS_IF_NO_ERRORS))
				|| (NrSuccess>=limitFolders && getSuccessCondition().equals(SUCCESS_IF_AT_LEAST_X_FOLDERS_DELETED))
				|| (NrErrors<=limitFolders && getSuccessCondition().equals(SUCCESS_IF_ERRORS_LESS))){
				retval=true;	
			}
		
		return retval;
	}
  private boolean deleteFolder(String foldername) {
    LogWriter log = LogWriter.getInstance();

    boolean rcode = false;
    FileObject filefolder = null;

    try {
      filefolder = KettleVFS.getFileObject(foldername);

      // Here gc() is explicitly called if e.g. createfile is used in the same
      // job for the same file. The problem is that after creating the file the
      // file object is not properly garbaged collected and thus the file cannot
      // be deleted anymore. This is a known problem in the JVM.

      System.gc();

      if (filefolder.exists()) {
        // the file or folder exists
        if (filefolder.getType() == FileType.FOLDER) {
          // It's a folder
          if (log.isDetailed())
            log.logDetailed(toString(), Messages.getString("JobEntryDeleteFolders.ProcessingFolder", foldername)); //$NON-NLS-1$
          // Delete Files
          int Nr = filefolder.delete(new TextFileSelector());

          if (log.isDetailed())
            log.logDetailed(toString(), Messages.getString("JobEntryDeleteFolders.TotalDeleted", foldername,String.valueOf(Nr))); //$NON-NLS-1$
          rcode = true;
        } else {
        	// Error...This file is not a folder!
        	log.logError(toString(), Messages.getString("JobEntryDeleteFolders.Error.NotFolder"));
        }
      } else {
        // File already deleted, no reason to try to delete it
    	  if(log.isBasic()) log.logBasic(toString(), Messages.getString("JobEntryDeleteFolders.FolderAlreadyDeleted", foldername)); //$NON-NLS-1$
        rcode = true;
      }
    } catch (IOException e) {
      log.logError(toString(), Messages.getString("JobEntryDeleteFolders.CouldNotDelete", foldername, e.getMessage())); //$NON-NLS-1$
    } finally {
      if (filefolder != null) {
        try {
          filefolder.close();
        } catch (IOException ex) {
        };
      }
    }

    return rcode;
  }


	private class TextFileSelector implements FileSelector 
	{	
		public boolean includeFile(FileSelectInfo info) {
			return true;
		}

		public boolean traverseDescendents(FileSelectInfo info)	{
			return true;
		}
	}
  

  public void setPrevious(boolean argFromPrevious) {
	    this.argFromPrevious = argFromPrevious;
	  }

  
  
  public boolean evaluates() {
    return true;
  }

  public void check(List<CheckResultInterface> remarks, JobMeta jobMeta) {
    boolean res = andValidator().validate(this, "arguments", remarks, putValidators(notNullValidator())); //$NON-NLS-1$

    if (res == false) {
      return;
    }

    ValidatorContext ctx = new ValidatorContext();
    putVariableSpace(ctx, getVariables());
    putValidators(ctx, notNullValidator(), fileExistsValidator());

    for (int i = 0; i < arguments.length; i++) {
      andValidator().validate(this, "arguments[" + i + "]", remarks, ctx); //$NON-NLS-1$ //$NON-NLS-2$
    }
  }

  public List<ResourceReference> getResourceDependencies(JobMeta jobMeta) {
    List<ResourceReference> references = super.getResourceDependencies(jobMeta);
    if (arguments != null) {
      ResourceReference reference = null;
      for (int i=0; i<arguments.length; i++) {
        String filename = jobMeta.environmentSubstitute(arguments[i]);
        if (reference == null) {
          reference = new ResourceReference(this);
          references.add(reference);
        }
        reference.getEntries().add( new ResourceEntry(filename, ResourceType.FILE));
     }
    }
    return references;
  }

  public boolean isArgFromPrevious()
  {
    return argFromPrevious;
  }

  public String[] getArguments()
  {
    return arguments;
  }

	public void setSuccessCondition(String success_condition)
	{
		this.success_condition=success_condition;
	}
	public String getSuccessCondition()
	{
		return success_condition;
	}
	public void setLimitFolders(String limit_folders)
	{
		this.limit_folders=limit_folders;
	}
	
	public String getLimitFolders()
	{
		return limit_folders;
	}

}