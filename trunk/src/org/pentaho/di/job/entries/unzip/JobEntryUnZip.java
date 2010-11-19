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
 
package org.pentaho.di.job.entries.unzip;


import static org.pentaho.di.job.entry.validator.AbstractFileValidator.putVariableSpace;
import static org.pentaho.di.job.entry.validator.AndValidator.putValidators;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.andValidator;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.fileDoesNotExistValidator;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.notBlankValidator;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.vfs.AllFileSelector;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSelectInfo;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.ResultFile;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.util.StringUtil;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobEntryType;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.job.entry.validator.ValidatorContext;
import org.pentaho.di.repository.Repository;
import org.w3c.dom.Node;


/**
 * This defines a 'unzip' job entry. Its main use would be to 
 * unzip files in a directory
 * 
 * @author Samatar Hassan
 * @since 25-09-2007
 *
 */

public class JobEntryUnZip extends JobEntryBase implements Cloneable, JobEntryInterface
{

	private String zipFilename;
	public int afterunzip;
	private String wildcard;
	private String wildcardexclude;
	private String targetdirectory;
	private String movetodirectory;
	private boolean addfiletoresult;
	private boolean isfromprevious;
	private boolean adddate;
	private boolean addtime;
	private boolean SpecifyFormat;
	private String date_time_format;
	private boolean rootzip;
	private boolean createfolder;
	private String nr_limit;
	private String wildcardSource;
	private int     iffileexist;
	private boolean createMoveToDirectory;
	
	public  String SUCCESS_IF_AT_LEAST_X_FILES_UN_ZIPPED="success_when_at_least";
	public  String SUCCESS_IF_ERRORS_LESS="success_if_errors_less";
	public  String SUCCESS_IF_NO_ERRORS="success_if_no_errors";
	private String success_condition;
	

	public static final int IF_FILE_EXISTS_SKIP              		 =  0;
	public static final int IF_FILE_EXISTS_OVERWRITE               	 =  1;
	public static final int IF_FILE_EXISTS_UNIQ               		 =  2;
	public static final int IF_FILE_EXISTS_FAIL            			 =  3;
	public static final int IF_FILE_EXISTS_OVERWRITE_DIFF_SIZE       =  4;
	public static final int IF_FILE_EXISTS_OVERWRITE_EQUAL_SIZE      =  5;
	public static final int IF_FILE_EXISTS_OVERWRITE_ZIP_BIG         =  6;
	public static final int IF_FILE_EXISTS_OVERWRITE_ZIP_BIG_EQUAL   =  7;
	public static final int IF_FILE_EXISTS_OVERWRITE_ZIP_SMALL       =  8;
	public static final int IF_FILE_EXISTS_OVERWRITE_ZIP_SMALL_EQUAL =  9;
	
	public static final String typeIfFileExistsCode[] =  /* WARNING: DO NOT TRANSLATE THIS. */ 
	{
		"SKIP", "OVERWRITE", "UNIQ", "FAIL", "OVERWRITE_DIFF_SIZE", 
		"OVERWRITE_EQUAL_SIZE", "OVERWRITE_ZIP_BIG", "OVERWRITE_ZIP_BIG_EQUAL", "OVERWRITE_ZIP_BIG_SMALL", 
		"OVERWRITE_ZIP_BIG_SMALL_EQUAL",  
	};
	
	public static final String typeIfFileExistsDesc[] = 
	{
		Messages.getString("JobUnZip.Skip.Label"),
		Messages.getString("JobUnZip.Overwrite.Label"),
        Messages.getString("JobUnZip.Give_Unique_Name.Label"),
        Messages.getString("JobUnZip.Fail.Label"),
        Messages.getString("JobUnZip.OverwriteIfSizeDifferent.Label"),
        Messages.getString("JobUnZip.OverwriteIfSizeEquals.Label"),
        Messages.getString("JobUnZip.OverwriteIfZipBigger.Label"),
        Messages.getString("JobUnZip.OverwriteIfZipBiggerOrEqual.Label"),
        Messages.getString("JobUnZip.OverwriteIfZipSmaller.Label"),
        Messages.getString("JobUnZip.OverwriteIfZipSmallerOrEqual.Label"),
	};
	
	private int NrErrors=0;
	private int NrSuccess=0;
	boolean successConditionBroken=false;
	boolean successConditionBrokenExit=false;
	int limitFiles=0;
	
	public JobEntryUnZip(String n)
	{
		super(n, "");
		zipFilename=null;
		afterunzip=0;
		wildcard=null;
		wildcardexclude=null;
		targetdirectory=null;
		movetodirectory=null;
		addfiletoresult = false;
		isfromprevious = false;
		adddate=false;
		addtime=false;
		SpecifyFormat=false;
		rootzip=false;
		createfolder=false;
		nr_limit="10";
		wildcardSource=null;
		iffileexist=IF_FILE_EXISTS_SKIP;
		success_condition=SUCCESS_IF_NO_ERRORS;
		createMoveToDirectory=false;
		setID(-1L);
		setJobEntryType(JobEntryType.UNZIP);
	}

	public JobEntryUnZip()
	{
		this("");
	}

	public JobEntryUnZip(JobEntryBase jeb)
	{
		super(jeb);
	}

    public Object clone()
    {
        JobEntryUnZip je = (JobEntryUnZip) super.clone();
        return je;
    }
    
	public String getXML()
	{
        StringBuffer retval = new StringBuffer(50);
		
		retval.append(super.getXML());		
		retval.append("      ").append(XMLHandler.addTagValue("zipfilename",      zipFilename));
		retval.append("      ").append(XMLHandler.addTagValue("wildcard",         wildcard));
		retval.append("      ").append(XMLHandler.addTagValue("wildcardexclude",  wildcardexclude));
		retval.append("      ").append(XMLHandler.addTagValue("targetdirectory",  targetdirectory));
		retval.append("      ").append(XMLHandler.addTagValue("movetodirectory",  movetodirectory));
		retval.append("      ").append(XMLHandler.addTagValue("afterunzip",  afterunzip));
		retval.append("      ").append(XMLHandler.addTagValue("addfiletoresult",  addfiletoresult));
		retval.append("      ").append(XMLHandler.addTagValue("isfromprevious",  isfromprevious));
		retval.append("      ").append(XMLHandler.addTagValue("adddate",  adddate));
		retval.append("      ").append(XMLHandler.addTagValue("addtime",  addtime));
		retval.append("      ").append(XMLHandler.addTagValue("SpecifyFormat",  SpecifyFormat));
		retval.append("      ").append(XMLHandler.addTagValue("date_time_format",  date_time_format));
		retval.append("      ").append(XMLHandler.addTagValue("rootzip",  rootzip));
		retval.append("      ").append(XMLHandler.addTagValue("createfolder",  createfolder));
		retval.append("      ").append(XMLHandler.addTagValue("nr_limit",  nr_limit));
		retval.append("      ").append(XMLHandler.addTagValue("wildcardSource",  wildcardSource));
		retval.append("      ").append(XMLHandler.addTagValue("success_condition", success_condition));
		retval.append("      ").append(XMLHandler.addTagValue("iffileexists", getIfFileExistsCode(iffileexist))); 
		retval.append("      ").append(XMLHandler.addTagValue("create_move_to_directory",  createMoveToDirectory));
		
		return retval.toString();
	}
	
	public void loadXML(Node entrynode, List<DatabaseMeta> databases, List<SlaveServer> slaveServers, Repository rep)
	throws KettleXMLException
 {
		try
		{
			super.loadXML(entrynode, databases, slaveServers);
			zipFilename = XMLHandler.getTagValue(entrynode, "zipfilename");
			afterunzip        = Const.toInt(XMLHandler.getTagValue(entrynode, "afterunzip"), -1);

    		wildcard = XMLHandler.getTagValue(entrynode, "wildcard");
			wildcardexclude = XMLHandler.getTagValue(entrynode, "wildcardexclude");
			targetdirectory = XMLHandler.getTagValue(entrynode, "targetdirectory");
			movetodirectory = XMLHandler.getTagValue(entrynode, "movetodirectory");
			addfiletoresult = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "addfiletoresult"));
			isfromprevious = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "isfromprevious"));	
			adddate = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "adddate"));	
			addtime = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "addtime"));	
			SpecifyFormat = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "SpecifyFormat"));	
			date_time_format = XMLHandler.getTagValue(entrynode, "date_time_format");
			rootzip = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "rootzip"));
			createfolder = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "createfolder"));
			nr_limit = XMLHandler.getTagValue(entrynode, "nr_limit");
			wildcardSource = XMLHandler.getTagValue(entrynode, "wildcardSource");
			success_condition          = XMLHandler.getTagValue(entrynode, "success_condition");
			if(Const.isEmpty(success_condition)) success_condition=SUCCESS_IF_NO_ERRORS;
			iffileexist   = getIfFileExistsInt(XMLHandler.getTagValue(entrynode, "iffileexists"));	
			createMoveToDirectory = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "create_move_to_directory"));
			
		}
		catch(KettleXMLException xe)
		{
			throw new KettleXMLException("Unable to load job entry of type 'unzip' from XML node", xe);
		}
	}

	public void loadRep(Repository rep, long id_jobentry, List<DatabaseMeta> databases, List<SlaveServer> slaveServers)
	throws KettleException
  {
		try
		{
			super.loadRep(rep, id_jobentry, databases, slaveServers);
			zipFilename = rep.getJobEntryAttributeString(id_jobentry, "zipfilename");
			afterunzip=(int) rep.getJobEntryAttributeInteger(id_jobentry, "afterunzip");
			wildcard = rep.getJobEntryAttributeString(id_jobentry, "wildcard");
			wildcardexclude = rep.getJobEntryAttributeString(id_jobentry, "wildcardexclude");
			targetdirectory = rep.getJobEntryAttributeString(id_jobentry, "targetdirectory");
			movetodirectory = rep.getJobEntryAttributeString(id_jobentry, "movetodirectory");
			addfiletoresult=rep.getJobEntryAttributeBoolean(id_jobentry, "addfiletoresult");
			isfromprevious=rep.getJobEntryAttributeBoolean(id_jobentry, "isfromprevious");
			adddate=rep.getJobEntryAttributeBoolean(id_jobentry, "adddate");
			addtime=rep.getJobEntryAttributeBoolean(id_jobentry, "adddate");
			SpecifyFormat=rep.getJobEntryAttributeBoolean(id_jobentry, "SpecifyFormat");
			date_time_format = rep.getJobEntryAttributeString(id_jobentry, "date_time_format");
			rootzip=rep.getJobEntryAttributeBoolean(id_jobentry, "rootzip");
			createfolder=rep.getJobEntryAttributeBoolean(id_jobentry, "createfolder");
			nr_limit=rep.getJobEntryAttributeString(id_jobentry, "nr_limit");
			wildcardSource=rep.getJobEntryAttributeString(id_jobentry, "wildcardSource");
			success_condition  = rep.getJobEntryAttributeString(id_jobentry, "success_condition");
			if(Const.isEmpty(success_condition)) success_condition=SUCCESS_IF_NO_ERRORS;
			iffileexist    = getIfFileExistsInt(rep.getJobEntryAttributeString(id_jobentry,"iffileexists") );
			createMoveToDirectory=rep.getJobEntryAttributeBoolean(id_jobentry, "create_move_to_directory");
		}


		catch(KettleException dbe)
		{
			throw new KettleException("Unable to load job entry of type 'unzip' from the repository for id_jobentry="+id_jobentry, dbe);
		}
	}
	
	public void saveRep(Repository rep, long id_job)
		throws KettleException
	{
		try
		{
			super.saveRep(rep, id_job);
			
			rep.saveJobEntryAttribute(id_job, getID(), "zipfilename", zipFilename);
			rep.saveJobEntryAttribute(id_job, getID(), "afterunzip", afterunzip);
			rep.saveJobEntryAttribute(id_job, getID(), "wildcard", wildcard);
			rep.saveJobEntryAttribute(id_job, getID(), "wildcardexclude", wildcardexclude);
			rep.saveJobEntryAttribute(id_job, getID(), "targetdirectory", targetdirectory);
			rep.saveJobEntryAttribute(id_job, getID(), "movetodirectory", movetodirectory);
			rep.saveJobEntryAttribute(id_job, getID(), "addfiletoresult", addfiletoresult);
			rep.saveJobEntryAttribute(id_job, getID(), "isfromprevious", isfromprevious);
			rep.saveJobEntryAttribute(id_job, getID(), "addtime", addtime);
			rep.saveJobEntryAttribute(id_job, getID(), "adddate", adddate);
			rep.saveJobEntryAttribute(id_job, getID(), "SpecifyFormat", SpecifyFormat);
			rep.saveJobEntryAttribute(id_job, getID(), "date_time_format", date_time_format);
			rep.saveJobEntryAttribute(id_job, getID(), "rootzip", rootzip);
			rep.saveJobEntryAttribute(id_job, getID(), "createfolder", createfolder);
			rep.saveJobEntryAttribute(id_job, getID(), "nr_limit", nr_limit);
			rep.saveJobEntryAttribute(id_job, getID(), "wildcardSource", wildcardSource);
			rep.saveJobEntryAttribute(id_job, getID(), "success_condition",    success_condition);
			rep.saveJobEntryAttribute(id_job, getID(), "iffileexists", getIfFileExistsCode(iffileexist));
			rep.saveJobEntryAttribute(id_job, getID(), "create_move_to_directory", createMoveToDirectory);
		}
		catch(KettleDatabaseException dbe)
		{
			throw new KettleException("Unable to save job entry of type 'unzip' to the repository for id_job="+id_job, dbe);
		}
	}

	
	public Result execute(Result previousResult, int nr, Repository rep, Job parentJob)
	{
		LogWriter log = LogWriter.getInstance();
		Result result = previousResult;
		result.setResult( false );
		result.setEntryNr(1);

		 List<RowMetaAndData> rows = result.getRows();
		 RowMetaAndData resultRow = null;
		
		String realFilenameSource    = environmentSubstitute(zipFilename);
		String realWildcardSource    = environmentSubstitute(wildcardSource);
		String realWildcard          = environmentSubstitute(wildcard);
		String realWildcardExclude   = environmentSubstitute(wildcardexclude);
		String realTargetdirectory   = environmentSubstitute(targetdirectory);
		String realMovetodirectory   = environmentSubstitute(movetodirectory);

		limitFiles=Const.toInt(environmentSubstitute(getLimit()),10);
		NrErrors=0;
		NrSuccess=0;
		successConditionBroken=false;
		successConditionBrokenExit=false;
		
		if(isfromprevious)
		{
			if(log.isDetailed())	
				log.logDetailed(toString(), Messages.getString("JobUnZip.Log.ArgFromPrevious.Found",(rows!=null?rows.size():0)+ ""));	
			
			if(rows.size()==0)	return result;	
		}else
		{
			if(Const.isEmpty(zipFilename))
			{
				// Zip file/folder is missing
				log.logError(toString(), Messages.getString("JobUnZip.No_ZipFile_Defined.Label"));
				return result;
			}
		}
		
	
		FileObject fileObject = null;
		FileObject targetdir=null;
		FileObject movetodir=null;
	
		
		try 
		{
			
			// Let's make some checks here, before running job entry ...	
			
			boolean exitjobentry=false;
			// Target folder
			targetdir = KettleVFS.getFileObject(realTargetdirectory);	
			if (!targetdir.exists())
			{
				if(createfolder)
				{
					targetdir.createFolder();
					if(log.isDetailed()) log.logDetailed(toString(),Messages.getString("JobUnZip.Log.TargetFolderCreated",realTargetdirectory));
						
				}else
				{
					log.logError(Messages.getString("JobUnZip.Error.Label"), Messages.getString("JobUnZip.TargetFolderNotFound.Label"));
					exitjobentry=true;
				}
			}else{
				if (!(targetdir.getType() == FileType.FOLDER))
				{
					log.logError(Messages.getString("JobUnZip.Error.Label"), Messages.getString("JobUnZip.TargetFolderNotFolder.Label",realTargetdirectory));
					exitjobentry=true;
				}else
				{
					if (log.isDetailed()) log.logDetailed(toString(),Messages.getString("JobUnZip.TargetFolderExists.Label",realTargetdirectory));
				}
			}
			
			// If user want to move zip files after process
			// movetodirectory must be provided 
			if(afterunzip==2)
			{
				if(Const.isEmpty(movetodirectory))
				{
					log.logError(Messages.getString("JobUnZip.Error.Label"), Messages.getString("JobUnZip.MoveToDirectoryEmpty.Label"));
					exitjobentry=true;
				}else
				{
					movetodir = KettleVFS.getFileObject(realMovetodirectory);
					if (!(movetodir.exists()) || !(movetodir.getType() == FileType.FOLDER))
					{
						if(createMoveToDirectory)
						{
							movetodir.createFolder();
							if(log.isDetailed()) log.logDetailed(toString(),Messages.getString("JobUnZip.Log.MoveToFolderCreated",realMovetodirectory));
						}else
						{
							log.logError(Messages.getString("JobUnZip.Error.Label"), Messages.getString("JobUnZip.MoveToDirectoryNotExists.Label"));
							exitjobentry=true;
						}
					}
				}
			}
			
			// We found errors...now exit
			if(exitjobentry) return result;
			
			if(isfromprevious)
			{
				if (rows!=null) // Copy the input row to the (command line) arguments
				{
					for (int iteration=0;iteration<rows.size() && !parentJob.isStopped();iteration++) 
					{
						if(successConditionBroken){
							if(!successConditionBrokenExit){
								log.logError(toString(), Messages.getString("JobUnZip.Error.SuccessConditionbroken",""+NrErrors));
								successConditionBrokenExit=true;
							}
							result.setEntryNr(NrErrors);
							return result;
						}
						
						resultRow = rows.get(iteration);
						
						// Get sourcefile/folder and wildcard
						realFilenameSource = resultRow.getString(0,null);
						realWildcardSource = resultRow.getString(1,null);
			
						fileObject = KettleVFS.getFileObject(realFilenameSource);
						if(fileObject.exists())
						{
							processOneFile(log, result,parentJob, 
									fileObject,realTargetdirectory,
									realWildcard,realWildcardExclude, movetodir,realMovetodirectory,
									realWildcardSource);	
						}else
						{
							updateErrors();
							log.logError(toString(),  Messages.getString("JobUnZip.Error.CanNotFindFile", realFilenameSource));
						}
					}
				}
			}else{
				fileObject = KettleVFS.getFileObject(realFilenameSource);
				if (!fileObject.exists())
				{
					log.logError(Messages.getString("JobUnZip.Error.Label"), Messages.getString("JobUnZip.ZipFile.NotExists.Label",realFilenameSource));
					return result;
				}
		
				if (log.isDetailed()) log.logDetailed(toString(),Messages.getString("JobUnZip.Zip_FileExists.Label",realFilenameSource));
				if(Const.isEmpty(targetdirectory))
				{
					log.logError(Messages.getString("JobUnZip.Error.Label"), Messages.getString("JobUnZip.TargetFolderNotFound.Label"));
					return result;
				}
				
				processOneFile(log, result,parentJob, 
						fileObject,realTargetdirectory,
						realWildcard,realWildcardExclude, movetodir,realMovetodirectory,
						realWildcardSource);
			}	
		}
		catch (Exception e) 
		{
   			log.logError(Messages.getString("JobUnZip.Error.Label"), Messages.getString("JobUnZip.ErrorUnzip.Label",realFilenameSource,e.getMessage()));
   			updateErrors();			
		}
		finally 
		{
			if ( fileObject != null ){
				try{
					fileObject.close();
				}catch ( IOException ex ) {};
			}
			if ( targetdir != null ){
				try{
					targetdir.close();
				}catch ( IOException ex ) {};
			}
			if ( movetodir != null ){
				try{
					movetodir.close();
				}catch ( IOException ex ) {};
			}
		}
	
		result.setNrErrors(NrErrors);
		result.setNrLinesWritten(NrSuccess);
		if(getSuccessStatus())	result.setResult(true);
		displayResults(log);
		
		return result;
	}
	private void displayResults(LogWriter log)
	{
		if(log.isDetailed()){
			log.logDetailed(toString(), "=======================================");
			log.logDetailed(toString(), Messages.getString("JobUnZip.Log.Info.FilesInError","" + NrErrors));
			log.logDetailed(toString(), Messages.getString("JobUnZip.Log.Info.FilesInSuccess","" + NrSuccess));
			log.logDetailed(toString(), "=======================================");
		}
	}
	
	private boolean processOneFile(LogWriter log, Result result,Job parentJob, 
			FileObject fileObject,String realTargetdirectory,
			String realWildcard,String realWildcardExclude, FileObject movetodir,String realMovetodirectory,
			String realWildcardSource)
	{
		boolean retval=false;
		
		try{
			if(fileObject.getType().equals(FileType.FILE))
			{
				// We have to unzip one zip file
				if(!unzipFile(log, fileObject, realTargetdirectory,realWildcard,
					realWildcardExclude,result, parentJob, fileObject, movetodir,realMovetodirectory))
					updateErrors();
				else
					updateSuccess();
			}else
			{
				// Folder..let's see wildcard
				FileObject[] children = fileObject.getChildren();
				
				for (int i=0; i<children.length && !parentJob.isStopped(); i++) 
				{
					if(successConditionBroken){
						if(!successConditionBrokenExit){
							log.logError(toString(), Messages.getString("JobUnZip.Error.SuccessConditionbroken",""+NrErrors));
							successConditionBrokenExit=true;
						}
						return false;
					}
		            // Get only file!
					if (!children[i].getType().equals(FileType.FOLDER)) 
					{
						boolean unzip=true;
						
						String filename=children[i].getName().getPath();
						
						Pattern patternSource = null;
	
						if (!Const.isEmpty(realWildcardSource))  
							patternSource = Pattern.compile(realWildcardSource);
						
						// First see if the file matches the regular expression!
						if (patternSource!=null)
						{
							Matcher matcher = patternSource.matcher(filename);
							unzip = matcher.matches();
						}
						if(unzip)
						{	
							if(!unzipFile(log,children[i],realTargetdirectory,realWildcard,
									realWildcardExclude,result, parentJob, fileObject,movetodir,
									realMovetodirectory))
								updateErrors();
							else
								updateSuccess();
						}
					}
				}
			}
		}catch(Exception e)
		{
			updateErrors();
			log.logError(toString(), Messages.getString("JobUnZip.Error.Label",e.getMessage()));
		}finally 
		{	
			if ( fileObject != null )
			{
				try {
					fileObject.close();
				}catch ( IOException ex ) {};
			}
		}
		return retval;
	}
	private boolean unzipFile(LogWriter log, FileObject sourceFileObject, String realTargetdirectory, String realWildcard,
			String realWildcardExclude, Result result, Job parentJob, FileObject fileObject, FileObject movetodir,
			String realMovetodirectory)
	{
		boolean retval=false;
		
		try {
			
			 if(log.isDetailed()) log.logDetailed(toString(), Messages.getString("JobUnZip.Log.ProcessingFile",sourceFileObject.toString()));
		
			 // Do you create a root folder?
			 //
			 if(rootzip)
			 {
				String shortSourceFilename = sourceFileObject.getName().getBaseName();
	        	int lenstring=shortSourceFilename.length();
	        	int lastindexOfDot=shortSourceFilename.lastIndexOf('.');
	        	if(lastindexOfDot==-1) lastindexOfDot=lenstring;
	        		
	        	String foldername=realTargetdirectory + "/" + shortSourceFilename.substring(0, lastindexOfDot);
				FileObject rootfolder=KettleVFS.getFileObject(foldername);
				if(!rootfolder.exists())
				{
					try {
					  rootfolder.createFolder();
					  if(log.isDetailed()) log.logDetailed(toString(), Messages.getString("JobUnZip.Log.RootFolderCreated",foldername));
					} catch(Exception e) {
						 throw new Exception(Messages.getString("JobUnZip.Error.CanNotCreateRootFolder",foldername), e);
					}
				}
			}
			 
		    // Try to read the entries from the VFS object...
			//
			String zipFilename = "zip:"+sourceFileObject.getName().getFriendlyURI();
			FileObject zipFile = KettleVFS.getFileObject(zipFilename);
			FileObject[] items = zipFile.findFiles(
					new AllFileSelector()
                        {
                            public boolean traverseDescendents(FileSelectInfo info)
                            {
                                return true;
                            }
                            
                            public boolean includeFile(FileSelectInfo info)
                            {
                                // Never return the parent directory of a file list.
                            	if (info.getDepth() == 0) {
                                    return false;
                                }
                                
                            	FileObject fileObject = info.getFile();
                            	return fileObject!=null;
                            }
                        }
                    );

			Pattern pattern = null;
			if (!Const.isEmpty(realWildcard)) 
			{
				pattern = Pattern.compile(realWildcard);
				
			}
			Pattern patternexclude = null;
			if (!Const.isEmpty(realWildcardExclude)) 
			{
				patternexclude = Pattern.compile(realWildcardExclude);
				
			}

			for (FileObject item : items) {
				
					if(successConditionBroken){
					  if(!successConditionBrokenExit){
						log.logError(toString(), Messages.getString("JobUnZip.Error.SuccessConditionbroken",""+NrErrors));
						successConditionBrokenExit=true;
					  }
					  return false;
				  }
					
				  try{
					  if(log.isDetailed()) log.logDetailed(toString(), Messages.getString("JobUnZip.Log.ProcessingZipEntry",item.getName().getURI(), sourceFileObject.toString()));
						
					  // get real destination filename
					  //
					  String newFileName = realTargetdirectory + Const.FILE_SEPARATOR + getTargetFilename(item.getName().getPath());
					  FileObject newFileObject = KettleVFS.getFileObject(newFileName);
						
					  if( item.getType().equals(FileType.FOLDER))
					  {
						 // Directory 
						 //
			             if(log.isDetailed()) log.logDetailed(toString(),Messages.getString("JobUnZip.CreatingDirectory.Label",newFileName));
		
			             // Create Directory if necessary ...
			             //
			             if(!newFileObject.exists()) newFileObject.createFolder();
					  }
					  else
					  {
						// File
						//
						boolean getIt = true;
						boolean getItexclude = false;
							
					    // First see if the file matches the regular expression!
						//
						if (pattern!=null)
						{
							Matcher matcher = pattern.matcher(item.getName().getURI());
							getIt = matcher.matches();
						}
		
						if (patternexclude!=null)
						{
							Matcher matcherexclude = patternexclude.matcher(item.getName().getURI());
							getItexclude = matcherexclude.matches();
						}
	
						boolean take=takeThisFile(log, item, newFileName);
						
						if (getIt && !getItexclude && take)
						{
							if(log.isDetailed()) log.logDetailed(toString(),Messages.getString("JobUnZip.ExtractingEntry.Label",item.getName().getURI(),newFileName));
							
							if(iffileexist==IF_FILE_EXISTS_UNIQ)
							{
				        		// Create file with unique name
				        		
				        		int lenstring=newFileName.length();
				        		int lastindexOfDot=newFileName.lastIndexOf('.');
				        		if(lastindexOfDot==-1) lastindexOfDot=lenstring;
				        		
				        		newFileName=newFileName.substring(0, lastindexOfDot)
				        		+ StringUtil.getFormattedDateTimeNow(true) 
				        		+ newFileName.substring(lastindexOfDot, lenstring);
				        		
				        		if(log.isDebug()) log.logDebug(toString(), Messages.getString("JobUnZip.Log.CreatingUniqFile",newFileName));
							}
							
							// See if the folder to the target file exists...
							//
							if (!newFileObject.getParent().exists()) {
								newFileObject.getParent().createFolder(); // creates the whole path.
							}
							InputStream is = null;
							OutputStream os = null;
							
							try {
			                  is = KettleVFS.getInputStream(item);
			                  os = KettleVFS.getOutputStream(newFileObject, false);
			                
				              if(is!=null)
				              {
					                byte[] buff=new byte[2048];
				                	int len;
				                	
				                	while((len=is.read(buff))>0)
				                	{
				                		os.write(buff,0,len);
				                	}
				                  
					                // Add filename to result filenames
					                addFilenameToResultFilenames(result, parentJob, newFileName);
				              }
							} finally {
			                    if(is!=null) is.close();
				                if(os!=null) os.close();
							}
						}// end if take    		
					 }
				  } catch(Exception e)
				  {
					  updateErrors();
					  log.logError(toString(), Messages.getString("JobUnZip.Error.CanNotProcessZipEntry",item.getName().getURI(), sourceFileObject.toString()), e);
				  }
	         }// End while

		     // Here gc() is explicitly called if e.g. createfile is used in the same
			 // job for the same file. The problem is that after creating the file the
			 // file object is not properly garbaged collected and thus the file cannot
			 // be deleted anymore. This is a known problem in the JVM.
				
			 //System.gc();
			  
			  // Unzip done...
			  if (afterunzip==1)
			  {
				  // delete zip file
				  boolean deleted = fileObject.delete();
				  if ( ! deleted )	
				  {	
					  updateErrors();
					  log.logError(toString(), Messages.getString("JobUnZip.Cant_Delete_File.Label", sourceFileObject.toString()));
				   }
				   // File deleted
				   if(log.isDebug()) log.logDebug(toString(), Messages.getString("JobUnZip.File_Deleted.Label", sourceFileObject.toString()));
			  }
			  else if(afterunzip == 2)
			  {
				   FileObject destFile=null;
				   // Move File	
					try
					{
						String destinationFilename=movetodir+Const.FILE_SEPARATOR+ fileObject.getName().getBaseName();
						destFile=KettleVFS.getFileObject(destinationFilename);
						
						fileObject.moveTo(destFile);

						// File moved
						if(log.isDetailed()) log.logDetailed(toString(), Messages.getString("JobUnZip.Log.FileMovedTo",sourceFileObject.toString(),realMovetodirectory));
					}
					catch (Exception e) 
					{
						updateErrors();
						log.logError(toString(), Messages.getString("JobUnZip.Cant_Move_File.Label",sourceFileObject.toString(),realMovetodirectory,e.getMessage()));
					}finally
					{
						if ( destFile != null ){
							try{
								destFile.close();
							}catch ( IOException ex ) {};
						}
					}
			 }
			  
			 retval=true;
		}
		catch (Exception e) 
		{
			updateErrors();
   			log.logError(Messages.getString("JobUnZip.Error.Label"), Messages.getString("JobUnZip.ErrorUnzip.Label",sourceFileObject.toString(),e.getMessage()), e);
		}

		return retval;
	}

	private void addFilenameToResultFilenames(Result result, Job parentJob, String newfile) throws Exception
	{
		if (addfiletoresult)
	 	{
			// Add file to result files name
			ResultFile resultFile = new ResultFile(ResultFile.FILE_TYPE_GENERAL , KettleVFS.getFileObject(newfile), parentJob.getJobname(), toString());
			result.getResultFiles().put(resultFile.getFile().toString(), resultFile);
	 	}
	}
	private void updateErrors()
	{
		NrErrors++;
		if(checkIfSuccessConditionBroken())
		{
			// Success condition was broken
			successConditionBroken=true;
		}
	}
	
	private void updateSuccess()
	{
		NrSuccess++;
	}
	private boolean checkIfSuccessConditionBroken()
	{
		boolean retval=false;
		if ((NrErrors>0 && getSuccessCondition().equals(SUCCESS_IF_NO_ERRORS))
				|| (NrErrors>=limitFiles && getSuccessCondition().equals(SUCCESS_IF_ERRORS_LESS)))
		{
			retval=true;	
		}
		return retval;
	}
	private boolean getSuccessStatus()
	{
		boolean retval=false;
		
		if ((NrErrors==0 && getSuccessCondition().equals(SUCCESS_IF_NO_ERRORS))
				|| (NrSuccess>=limitFiles && getSuccessCondition().equals(SUCCESS_IF_AT_LEAST_X_FILES_UN_ZIPPED))
				|| (NrErrors<=limitFiles && getSuccessCondition().equals(SUCCESS_IF_ERRORS_LESS)))
			{
				retval=true;	
			}
		
		return retval;
	}
	
	private boolean takeThisFile(LogWriter log, FileObject sourceFile, String destinationFile) throws FileSystemException
	{
		boolean retval=false;
		File destination= new File(destinationFile);
		if(!destination.exists())
		{
			if(log.isDebug()) log.logDebug(toString(), Messages.getString("JobUnZip.Log.CanNotFindFile",destinationFile));
			return true;
		}
		if(log.isDebug()) log.logDebug(toString(), Messages.getString("JobUnZip.Log.FileExists",destinationFile));
		if(iffileexist==IF_FILE_EXISTS_SKIP)
		{
			if(log.isDebug()) log.logDebug(toString(), Messages.getString("JobUnZip.Log.FileSkip",destinationFile));
			return false;
		}
		if(iffileexist==IF_FILE_EXISTS_FAIL)
		{
			updateErrors();
			log.logError(toString(), Messages.getString("JobUnZip.Log.FileError",destinationFile,""+NrErrors));
			return false;
		}
		
		if(iffileexist==IF_FILE_EXISTS_OVERWRITE)
		{
			if(log.isDebug()) log.logDebug(toString(), Messages.getString("JobUnZip.Log.FileOverwrite",destinationFile));
			return true;
		}
		
		Long entrySize=sourceFile.getContent().getSize();
		Long destinationSize=destination.length();
		
		if(iffileexist==IF_FILE_EXISTS_OVERWRITE_DIFF_SIZE)
		{
			if(entrySize!=destinationSize)
			{
				if(log.isDebug()) log.logDebug(toString(), Messages.getString("JobUnZip.Log.FileDiffSize.Diff",
						sourceFile.getName().getURI(),""+entrySize,destinationFile,""+destinationSize));
				return true;
			}
			else
			{
				if(log.isDebug()) log.logDebug(toString(), Messages.getString("JobUnZip.Log.FileDiffSize.Same",
						sourceFile.getName().getURI(),""+entrySize,destinationFile,""+destinationSize));
				return false;
			}
		}
		if(iffileexist==IF_FILE_EXISTS_OVERWRITE_EQUAL_SIZE)
		{
			if(entrySize==destinationSize)
			{
				if(log.isDebug()) log.logDebug(toString(), Messages.getString("JobUnZip.Log.FileEqualSize.Same",
						sourceFile.getName().getURI(),""+entrySize,destinationFile,""+destinationSize));
				return true;
			}
			else
			{
				if(log.isDebug()) log.logDebug(toString(), Messages.getString("JobUnZip.Log.FileEqualSize.Diff",
						sourceFile.getName().getURI(),""+entrySize,destinationFile,""+destinationSize));
				return false;
			}
		}
		if(iffileexist==IF_FILE_EXISTS_OVERWRITE_ZIP_BIG)
		{
			if(entrySize>destinationSize)
			{
				if(log.isDebug()) log.logDebug(toString(), Messages.getString("JobUnZip.Log.FileBigSize.Big",
						sourceFile.getName().getURI(),""+entrySize,destinationFile,""+destinationSize));
				return true;
			}
			else
			{
				if(log.isDebug()) log.logDebug(toString(), Messages.getString("JobUnZip.Log.FileBigSize.Small",
						sourceFile.getName().getURI(),""+entrySize,destinationFile,""+destinationSize));
				return false;
			}
		}
		if(iffileexist==IF_FILE_EXISTS_OVERWRITE_ZIP_BIG_EQUAL)
		{
			if(entrySize>=destinationSize)
			{
				if(log.isDebug()) log.logDebug(toString(), 
						Messages.getString("JobUnZip.Log.FileBigEqualSize.Big",	
								sourceFile.getName().getURI(),""+entrySize,destinationFile,""+destinationSize));
				return true;
			}
			else
			{
				if(log.isDebug()) log.logDebug(toString(), 
						Messages.getString("JobUnZip.Log.FileBigEqualSize.Small",	
								sourceFile.getName().getURI(),""+entrySize,destinationFile,""+destinationSize));
				return false;
			}
		}
		if(iffileexist==IF_FILE_EXISTS_OVERWRITE_ZIP_SMALL)
		{
			if(entrySize<destinationSize)
			{
				if(log.isDebug()) log.logDebug(toString(), 
						Messages.getString("JobUnZip.Log.FileSmallSize.Small",	
								sourceFile.getName().getURI(),""+entrySize,destinationFile,""+destinationSize));
				return true;
			}
			else
			{
				if(log.isDebug()) log.logDebug(toString(), 
						Messages.getString("JobUnZip.Log.FileSmallSize.Big",	
								sourceFile.getName().getURI(),""+entrySize,destinationFile,""+destinationSize));
				return false;
			}
		}
		if(iffileexist==IF_FILE_EXISTS_OVERWRITE_ZIP_SMALL_EQUAL)
		{
			if(entrySize<=destinationSize)
			{
				if(log.isDebug()) log.logDebug(toString(), 
						Messages.getString("JobUnZip.Log.FileSmallEqualSize.Small",	
								sourceFile.getName().getURI(),""+entrySize,destinationFile,""+destinationSize));
				return true;
			}
			else
			{
				if(log.isDebug()) log.logDebug(toString(), 
						Messages.getString("JobUnZip.Log.FileSmallEqualSize.Big",	
								sourceFile.getName().getURI(),""+entrySize,destinationFile,""+destinationSize));
				return false;
			}
		}
		if(iffileexist==IF_FILE_EXISTS_UNIQ)
		{
    		// Create file with unique name
			return true;
		}
		
		return retval;
	}
	
	public boolean evaluates()
	{
		return true;
	}
    
	public static final int getIfFileExistsInt(String desc)
	{
		for (int i=0;i<typeIfFileExistsCode.length;i++)
		{
			if (typeIfFileExistsCode[i].equalsIgnoreCase(desc)) return i;
		}
		return 0;
	}
	public static final String getIfFileExistsCode(int i)
	{
		if (i<0 || i>=typeIfFileExistsCode.length) return null;
		return typeIfFileExistsCode[i];
	}
	   /**
     * @return Returns the iffileexist.
     */
    public int getIfFileExist()
    {
        return iffileexist;
    }
    
    /**
     * @param setIfFileExist The iffileexist to set.
     */
    public void setIfFileExists(int iffileexist)
    {
        this.iffileexist = iffileexist;
    }
    public boolean isCreateMoveToDirectory()
    {
    	return createMoveToDirectory;
    }
    public void setCreateMoveToDirectory(boolean createMoveToDirectory)
    {
    	this.createMoveToDirectory=createMoveToDirectory;
    }
	public void setZipFilename(String zipFilename)
	{
		this.zipFilename = zipFilename;
	}

	public void setWildcard(String wildcard)
	{
		this.wildcard = wildcard;
	}
	public void setWildcardExclude(String wildcardexclude)
	{
		this.wildcardexclude = wildcardexclude;
	}
	
	public void setSourceDirectory(String targetdirectoryin)
	{
		this.targetdirectory = targetdirectoryin;
	}
	
	public void setMoveToDirectory(String movetodirectory)
	{
		this.movetodirectory = movetodirectory;
	}
	
	public String getSourceDirectory()
	{
		return targetdirectory;
	}

	public String getMoveToDirectory()
	{
		return movetodirectory;
	}

	public String getZipFilename()
	{
		return zipFilename;
	}
	
	public String getWildcardSource()
	{
		return wildcardSource;
	}
	public void setWildcardSource(String wildcardSource)
	{
		this.wildcardSource=wildcardSource;
	}
	
	public String getWildcard()
	{
		return wildcard;
	}
	
	public String getWildcardExclude()
	{
		return wildcardexclude;
	}
	public void setAddFileToResult(boolean addfiletoresultin) 
	{
		this.addfiletoresult = addfiletoresultin;
	}
	
	public boolean isAddFileToResult() 
	{
		return addfiletoresult;
	}
	public void setDateInFilename(boolean adddate) 
	{
		this.adddate= adddate;
	}
	
	public boolean isDateInFilename() 
	{
		return adddate;
	}
	public void setTimeInFilename(boolean addtime) 
	{
		this.addtime= addtime;
	}
	public boolean isTimeInFilename() 
	{
		return addtime;
	}
	 public boolean  isSpecifyFormat()
	 {
	   	return SpecifyFormat;
	 }
	 public void setSpecifyFormat(boolean SpecifyFormat)
	 {
	   	this.SpecifyFormat=SpecifyFormat;
	 }
	 public String getDateTimeFormat()
	 {
		return date_time_format;
	 }
	 public void setDateTimeFormat(String date_time_format)
	 {
		this.date_time_format=date_time_format;
	 }
	public void setDatafromprevious(boolean isfromprevious) 
	{
		this.isfromprevious = isfromprevious;
	}
	
	public boolean getDatafromprevious() 
	{
		return isfromprevious;
	}	
	
	public void setCreateRootFolder(boolean rootzip)
	{
		this.rootzip=rootzip;
	}
	public boolean isCreateRootFolder() 
	{
		return rootzip;
	}	
	
	public void setCreateFolder(boolean createfolder)
	{
		this.createfolder=createfolder;
	}
	public boolean isCreateFolder() 
	{
		return createfolder;
	}	
	
	public void setLimit(String nr_limitin)
	{
		this.nr_limit=nr_limitin;
	}
	
	public String getLimit()
	{
		return nr_limit;
	}
	public void setSuccessCondition(String success_condition)
	{
		this.success_condition=success_condition;
	}
	public String getSuccessCondition()
	{
		return success_condition;
	}
	
	
    /**
     * @param string the filename from the FTP server
     * 
     * @return the calculated target filename
     */
	protected String getTargetFilename(String filename)
    {
		
        String retval="";
		// Replace possible environment variables...
		if(filename!=null) retval=filename;
		
		int lenstring=retval.length();
		int lastindexOfDot=retval.lastIndexOf('.');
		if(lastindexOfDot==-1) lastindexOfDot=lenstring;
		
		retval=retval.substring(0, lastindexOfDot);
		
		SimpleDateFormat daf     = new SimpleDateFormat();
		Date now = new Date();
		
		if(SpecifyFormat && !Const.isEmpty(date_time_format))
		{
			daf.applyPattern(date_time_format);
			String dt = daf.format(now);
			retval+=dt;
		}else
		{
		
			if (adddate)
			{
				daf.applyPattern("yyyyMMdd");
				String d = daf.format(now);
				retval+="_"+d;
			}
			if (addtime)
			{
				daf.applyPattern("HHmmssSSS");
				String t = daf.format(now);
				retval+="_"+t;
			}
		}
		
		retval+=filename.substring(lastindexOfDot, lenstring);

		return retval;

        
    }
	
	 @Override
	  public void check(List<CheckResultInterface> remarks, JobMeta jobMeta)
	  {
	    ValidatorContext ctx1 = new ValidatorContext();
	    putVariableSpace(ctx1, getVariables());
	    putValidators(ctx1, notBlankValidator(), fileDoesNotExistValidator());
	   
	    andValidator().validate(this, "zipFilename", remarks, ctx1);//$NON-NLS-1$

	    if (2 == afterunzip) {
	      // setting says to move
	      andValidator().validate(this, "moveToDirectory", remarks, putValidators(notBlankValidator())); //$NON-NLS-1$
	    }

	    andValidator().validate(this, "sourceDirectory", remarks, putValidators(notBlankValidator())); //$NON-NLS-1$

	  }
	
}