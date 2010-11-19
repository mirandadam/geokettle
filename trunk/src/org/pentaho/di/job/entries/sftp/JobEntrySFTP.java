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

package org.pentaho.di.job.entries.sftp;

import static org.pentaho.di.job.entry.validator.AbstractFileValidator.putVariableSpace;
import static org.pentaho.di.job.entry.validator.AndValidator.putValidators;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.andValidator;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.fileExistsValidator;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.integerValidator;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.notBlankValidator;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.notNullValidator;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.ResultFile;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.encryption.Encr;
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

import org.apache.commons.vfs.FileObject;

/**
 * This defines a SFTP job entry.
 *
 * @author Matt
 * @since 05-11-2003
 *
 */
public class JobEntrySFTP extends JobEntryBase implements Cloneable, JobEntryInterface
{
	private String serverName;
	private String serverPort;
	private String userName;
	private String password;
	private String sftpDirectory;
	private String targetDirectory;
	private String wildcard;
	private boolean remove;
	private boolean isaddresult;
	private boolean createtargetfolder;
	private boolean copyprevious;

	public JobEntrySFTP(String n)
	{
		super(n, "");
		serverName=null;
        serverPort="22";
        isaddresult=true;
        createtargetfolder=false;
        copyprevious=false;
		setID(-1L);
		setJobEntryType(JobEntryType.SFTP);
	}

	public JobEntrySFTP()
	{
		this("");
	}

	public JobEntrySFTP(JobEntryBase jeb)
	{
		super(jeb);
	}

    public Object clone()
    {
        JobEntrySFTP je = (JobEntrySFTP) super.clone();
        return je;
    }

	public String getXML()
	{
        StringBuffer retval = new StringBuffer(200);

		retval.append(super.getXML());

		retval.append("      ").append(XMLHandler.addTagValue("servername",   serverName));
		retval.append("      ").append(XMLHandler.addTagValue("serverport",   serverPort));
		retval.append("      ").append(XMLHandler.addTagValue("username",     userName));
        retval.append("      ").append(XMLHandler.addTagValue("password",     Encr.encryptPasswordIfNotUsingVariables(getPassword())));
		retval.append("      ").append(XMLHandler.addTagValue("sftpdirectory", sftpDirectory));
		retval.append("      ").append(XMLHandler.addTagValue("targetdirectory", targetDirectory));
		retval.append("      ").append(XMLHandler.addTagValue("wildcard",     wildcard));
		retval.append("      ").append(XMLHandler.addTagValue("remove",       remove));
		retval.append("      ").append(XMLHandler.addTagValue("isaddresult",       isaddresult));
		retval.append("      ").append(XMLHandler.addTagValue("createtargetfolder",       createtargetfolder));
		retval.append("      ").append(XMLHandler.addTagValue("copyprevious",       copyprevious));
		
		
		

		return retval.toString();
	}

	public void loadXML(Node entrynode, List<DatabaseMeta> databases, List<SlaveServer> slaveServers, Repository rep) throws KettleXMLException
	{
		try
		{
			super.loadXML(entrynode, databases, slaveServers);
			serverName      = XMLHandler.getTagValue(entrynode, "servername");
			serverPort      = XMLHandler.getTagValue(entrynode, "serverport");
			userName        = XMLHandler.getTagValue(entrynode, "username");
			password        = Encr.decryptPasswordOptionallyEncrypted( XMLHandler.getTagValue(entrynode, "password") );
			sftpDirectory   = XMLHandler.getTagValue(entrynode, "sftpdirectory");
			targetDirectory = XMLHandler.getTagValue(entrynode, "targetdirectory");
			wildcard        = XMLHandler.getTagValue(entrynode, "wildcard");
			remove          = "Y".equalsIgnoreCase( XMLHandler.getTagValue(entrynode, "remove") );

			String addresult = XMLHandler.getTagValue(entrynode, "isaddresult");	
			
			if(Const.isEmpty(addresult)) 
				isaddresult = true;
			else
				isaddresult = "Y".equalsIgnoreCase(addresult);
			
			createtargetfolder          = "Y".equalsIgnoreCase( XMLHandler.getTagValue(entrynode, "createtargetfolder") );
			copyprevious          = "Y".equalsIgnoreCase( XMLHandler.getTagValue(entrynode, "copyprevious") );
	
		}
		catch(KettleXMLException xe)
		{
			throw new KettleXMLException("Unable to load job entry of type 'SFTP' from XML node", xe);
		}
	}

	public void loadRep(Repository rep, long id_jobentry, List<DatabaseMeta> databases, List<SlaveServer> slaveServers)
		throws KettleException
	{
		try
		{
			super.loadRep(rep, id_jobentry, databases, slaveServers);
			serverName      = rep.getJobEntryAttributeString(id_jobentry, "servername");
			int intServerPort = (int)rep.getJobEntryAttributeInteger(id_jobentry, "serverport");
            serverPort = rep.getJobEntryAttributeString(id_jobentry, "serverport"); // backward compatible.
            if (intServerPort>0 && Const.isEmpty(serverPort)) serverPort = Integer.toString(intServerPort);

			userName        = rep.getJobEntryAttributeString(id_jobentry, "username");
		    password        = Encr.decryptPasswordOptionallyEncrypted(rep.getJobEntryAttributeString(id_jobentry, "password"));
		    
			sftpDirectory   = rep.getJobEntryAttributeString(id_jobentry, "sftpdirectory");
			targetDirectory = rep.getJobEntryAttributeString(id_jobentry, "targetdirectory");
			wildcard        = rep.getJobEntryAttributeString(id_jobentry, "wildcard");
			remove          = rep.getJobEntryAttributeBoolean(id_jobentry, "remove");
		  
			String addToResult=rep.getStepAttributeString (id_jobentry, "add_to_result_filenames");
			if(Const.isEmpty(addToResult)) 
				isaddresult = true;
			else
				isaddresult =  rep.getStepAttributeBoolean(id_jobentry, "add_to_result_filenames");
			
			createtargetfolder          = rep.getJobEntryAttributeBoolean(id_jobentry, "createtargetfolder");
			copyprevious          = rep.getJobEntryAttributeBoolean(id_jobentry, "copyprevious");

		}
		catch(KettleException dbe)
		{
			throw new KettleException("Unable to load job entry of type 'SFTP' from the repository for id_jobentry="+id_jobentry, dbe);
		}
	}

	public void saveRep(Repository rep, long id_job)
		throws KettleException
	{
		try
		{
			super.saveRep(rep, id_job);

			rep.saveJobEntryAttribute(id_job, getID(), "servername",      serverName);
			rep.saveJobEntryAttribute(id_job, getID(), "serverport",      serverPort);
			rep.saveJobEntryAttribute(id_job, getID(), "username",        userName);
			rep.saveJobEntryAttribute(id_job, getID(), "password",        Encr.encryptPasswordIfNotUsingVariables(password)); //$NON-NLS-1$
			rep.saveJobEntryAttribute(id_job, getID(), "sftpdirectory",    sftpDirectory);
			rep.saveJobEntryAttribute(id_job, getID(), "targetdirectory", targetDirectory);
			rep.saveJobEntryAttribute(id_job, getID(), "wildcard",        wildcard);
			rep.saveJobEntryAttribute(id_job, getID(), "remove",          remove);
			rep.saveJobEntryAttribute(id_job, getID(), "isaddresult",          isaddresult);
			rep.saveJobEntryAttribute(id_job, getID(), "createtargetfolder",          createtargetfolder);
			rep.saveJobEntryAttribute(id_job, getID(), "copyprevious",          copyprevious);

		}
		catch(KettleDatabaseException dbe)
		{
			throw new KettleException("Unable to save job entry of type 'SFTP' to the repository for id_job="+id_job, dbe);
		}
	}

	/**
	 * @return Returns the directory.
	 */
	public String getScpDirectory()
	{
		return sftpDirectory;
	}

	/**
	 * @param directory The directory to set.
	 */
	public void setScpDirectory(String directory)
	{
		this.sftpDirectory = directory;
	}

	/**
	 * @return Returns the password.
	 */
	public String getPassword()
	{
		return password;
	}

	/**
	 * @param password The password to set.
	 */
	public void setPassword(String password)
	{
		this.password = password;
	}

	/**
	 * @return Returns the serverName.
	 */
	public String getServerName()
	{
		return serverName;
	}

	/**
	 * @param serverName The serverName to set.
	 */
	public void setServerName(String serverName)
	{
		this.serverName = serverName;
	}

	/**
	 * @return Returns the userName.
	 */
	public String getUserName()
	{
		return userName;
	}

	/**
	 * @param userName The userName to set.
	 */
	public void setUserName(String userName)
	{
		this.userName = userName;
	}

	/**
	 * @return Returns the wildcard.
	 */
	public String getWildcard()
	{
		return wildcard;
	}

	/**
	 * @param wildcard The wildcard to set.
	 */
	public void setWildcard(String wildcard)
	{
		this.wildcard = wildcard;
	}
	public void setAddToResult(boolean isaddresultin)
	   {
			this.isaddresult=isaddresultin;
	   }
		 
		public boolean isAddToResult()
		{
			return isaddresult;
		}

	/**
	 * @return Returns the targetDirectory.
	 */
	public String getTargetDirectory()
	{
		return targetDirectory;
	}

	public void setcreateTargetFolder(boolean createtargetfolder)
	{
		this.createtargetfolder=createtargetfolder;
	}
	
	public boolean iscreateTargetFolder()
	{
		return createtargetfolder;
	}
	public boolean isCopyPrevious()
	{
		return copyprevious;
	}
	
	public void setCopyPrevious(boolean copyprevious)
	{
		this.copyprevious=copyprevious;
	}
	/**
	 * @param targetDirectory The targetDirectory to set.
	 */
	public void setTargetDirectory(String targetDirectory)
	{
		this.targetDirectory = targetDirectory;
	}

	/**
	 * @param remove The remove to set.
	 */
	public void setRemove(boolean remove)
	{
		this.remove = remove;
	}

	/**
	 * @return Returns the remove.
	 */
	public boolean getRemove()
	{
		return remove;
	}

	public String getServerPort() {
		return serverPort;
	}

	public void setServerPort(String serverPort) {
		this.serverPort = serverPort;
	}


	public Result execute(Result previousResult, int nr, Repository rep, Job parentJob)
	{
		LogWriter log = LogWriter.getInstance();

        Result result = previousResult;
		List<RowMetaAndData> rows = result.getRows();
		RowMetaAndData resultRow = null;
		
		result.setResult( false );
		long filesRetrieved = 0;

		if(log.isDetailed()) log.logDetailed(toString(), Messages.getString("JobSFTP.Log.StartJobEntry"));
		HashSet<String> list_previous_filenames = new HashSet<String>();
		
		if(copyprevious)
		{
			if(rows.size()==0)
			{
				if(log.isDetailed()) log.logDetailed(toString(), Messages.getString("JobSFTP.ArgsFromPreviousNothing"));
				result.setResult(true);
				return result;
			}
			try{
				
				// Copy the input row to the (command line) arguments
				for (int iteration=0;iteration<rows.size();iteration++) 
				{			
					resultRow = rows.get(iteration);
				
					// Get file names
					String file_previous = resultRow.getString(0,null);
					if(!Const.isEmpty(file_previous))
					{
						list_previous_filenames.add(file_previous);
						if(log.isDebug()) log.logDebug(toString(), Messages.getString("JobSFTP.Log.FilenameFromResult",file_previous));
					}
				}
			}catch(Exception e)	{
				log.logError(toString(), Messages.getString("JobSFTP.Error.ArgFromPrevious"));
				result.setNrErrors(1);
				return result;
			}
		}
		
		
		SFTPClient sftpclient = null;

        // String substitution..
        String realServerName      = environmentSubstitute(serverName);
        String realServerPort      = environmentSubstitute(serverPort);
        String realUsername        = environmentSubstitute(userName);
        String realPassword        = environmentSubstitute(password);
        String realSftpDirString   = environmentSubstitute(sftpDirectory);
        String realWildcard        = environmentSubstitute(wildcard);
        String realTargetDirectory = environmentSubstitute(targetDirectory);
        
        FileObject TargetFolder=null;

        
		try
		{
			// Let's perform some checks before starting
			if(!Const.isEmpty(realTargetDirectory))
			{
				TargetFolder=KettleVFS.getFileObject(realTargetDirectory);
				boolean TargetFolderExists=TargetFolder.exists();
				if(TargetFolderExists)
				{
					if(log.isDetailed()) log.logDetailed(toString(), Messages.getString("JobSFTP.Log.TargetFolderExists", realTargetDirectory));
				}else
				{
					log.logError(toString(), Messages.getString("JobSFTP.Error.TargetFolderNotExists", realTargetDirectory));	
					if(!createtargetfolder)
					{
						// Error..Target folder can not be found !
						result.setNrErrors(1);
						return result;
					}else
					{
						// create target folder
						TargetFolder.createFolder();
						if(log.isDetailed()) log.logDetailed(toString(), Messages.getString("JobSFTP.Log.TargetFolderCreated", realTargetDirectory));
					}	
				}
			}
			
			if(TargetFolder!=null) 
			{
				TargetFolder.close();
				TargetFolder=null;
			}
			
			
			// Create sftp client to host ...
			sftpclient = new SFTPClient(InetAddress.getByName(realServerName), Const.toInt(realServerPort, 22), realUsername);
			if(log.isDetailed()) log.logDetailed(toString(), Messages.getString("JobSFTP.Log.OpenedConnection",realServerName,realServerPort,realUsername));

			// login to ftp host ...
			sftpclient.login(realPassword);
			// Passwords should not appear in log files.
			//log.logDetailed(toString(), "logged in using password "+realPassword); // Logging this seems a bad idea! Oh well.

			// move to spool dir ...
			if (!Const.isEmpty(realSftpDirString))
			{
				try
				{
					sftpclient.chdir(realSftpDirString);
				}catch(Exception e)
				{
					log.logError(toString(), Messages.getString("JobSFTP.Error.CanNotFindRemoteFolder",realSftpDirString));
					throw new Exception (e);
				}
				if(log.isDetailed()) log.logDetailed(toString(), Messages.getString("JobSFTP.Log.ChangedDirectory",realSftpDirString));
			}
			Pattern pattern = null;
			// Get all the files in the current directory...
			String[] filelist= sftpclient.dir();
			if(filelist==null)
			{
				// Nothing was found !!! exit
				result.setResult( true );
				if(log.isDetailed()) log.logDetailed(toString(), Messages.getString("JobSFTP.Log.Found",""+0));
				return result;
			}
			if(log.isDetailed()) log.logDetailed(toString(), Messages.getString("JobSFTP.Log.Found",""+filelist.length));

			if(!copyprevious)
			{
				if (!Const.isEmpty(realWildcard)) pattern = Pattern.compile(realWildcard);
			}
			
			
			// Get the files in the list...
			for (int i=0;i<filelist.length && !parentJob.isStopped();i++)
			{
				boolean getIt = true;
				// First see if the file matches the regular expression!
				if (!copyprevious && pattern!=null)
				{
					Matcher matcher = pattern.matcher(filelist[i]);
					getIt = matcher.matches();
				}

				if (getIt || list_previous_filenames.contains(filelist[i]))
				{
					if(log.isDebug()) log.logDebug(toString(), Messages.getString("JobSFTP.Log.GettingFiles",filelist[i],realTargetDirectory));

					String targetFilename = realTargetDirectory+Const.FILE_SEPARATOR+filelist[i];
					sftpclient.get(targetFilename, filelist[i]);
					filesRetrieved++;

					if(isaddresult)
					{
						// Add to the result files...
						ResultFile resultFile = new ResultFile(ResultFile.FILE_TYPE_GENERAL, KettleVFS.getFileObject(targetFilename), parentJob.getJobname(), toString());
						result.getResultFiles().put(resultFile.getFile().toString(), resultFile);
						if(log.isDetailed()) log.logDetailed(toString(), Messages.getString("JobSFTP.Log.FilenameAddedToResultFilenames",filelist[i]));
					}
                    if(log.isDetailed()) log.logDetailed(toString(), Messages.getString("JobSFTP.Log.TransferedFile",filelist[i]));

					// Delete the file if this is needed!
					if (remove)
					{
						sftpclient.delete(filelist[i]);
						if(log.isDetailed()) log.logDetailed(toString(), Messages.getString("JobSFTP.Log.DeletedFile",filelist[i]));
					}
				}
			}

			result.setResult( true );
			result.setNrFilesRetrieved(filesRetrieved);
		}
		catch(Exception e)
		{
			result.setNrErrors(1);
			log.logError(toString(), Messages.getString("JobSFTP.Error.GettingFiles",e.getMessage()));
            log.logError(toString(), Const.getStackTracker(e));
		} finally {
			// close connection, if possible
			try {
				if(sftpclient != null) sftpclient.disconnect();
			} catch (Exception e) {
				// just ignore this, makes no big difference
			}
			
			try{
				if (TargetFolder!=null){
					TargetFolder.close();
					TargetFolder=null;
				}
				if(list_previous_filenames!=null) list_previous_filenames=null;
			}catch (Exception e){}
			
		}

		return result;
	}

	public boolean evaluates()
	{
		return true;
	}

  public List<ResourceReference> getResourceDependencies(JobMeta jobMeta) {
    List<ResourceReference> references = super.getResourceDependencies(jobMeta);
    if (!Const.isEmpty(serverName)) {
      String realServerName = jobMeta.environmentSubstitute(serverName);
      ResourceReference reference = new ResourceReference(this);
      reference.getEntries().add( new ResourceEntry(realServerName, ResourceType.SERVER));
      references.add(reference);
    }
    return references;
  }

  @Override
  public void check(List<CheckResultInterface> remarks, JobMeta jobMeta)
  {
    andValidator().validate(this, "serverName", remarks, putValidators(notBlankValidator())); //$NON-NLS-1$

    ValidatorContext ctx = new ValidatorContext();
    putVariableSpace(ctx, getVariables());
    putValidators(ctx, notBlankValidator(), fileExistsValidator());
    andValidator().validate(this, "targetDirectory", remarks, ctx);//$NON-NLS-1$

    andValidator().validate(this, "userName", remarks, putValidators(notBlankValidator())); //$NON-NLS-1$
    andValidator().validate(this, "password", remarks, putValidators(notNullValidator())); //$NON-NLS-1$
    andValidator().validate(this, "serverPort", remarks, putValidators(integerValidator())); //$NON-NLS-1$
  }

  public static void main(String[] args) {
    List<CheckResultInterface> remarks = new ArrayList<CheckResultInterface>();
    new JobEntrySFTP().check(remarks, null);
    System.out.printf("Remarks: %s\n", remarks);
  }

}