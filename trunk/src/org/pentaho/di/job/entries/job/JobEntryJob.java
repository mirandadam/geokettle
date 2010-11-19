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

package org.pentaho.di.job.entries.job;

import static org.pentaho.di.job.entry.validator.AndValidator.putValidators;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.andValidator;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.notBlankValidator;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.notNullValidator;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.ResultFile;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.SQLStatement;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.logging.Log4jFileAppender;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.parameters.DuplicateParamException;
import org.pentaho.di.core.parameters.NamedParams;
import org.pentaho.di.core.parameters.NamedParamsDefault;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobEntryType;
import org.pentaho.di.job.JobExecutionConfiguration;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectory;
import org.pentaho.di.resource.ResourceDefinition;
import org.pentaho.di.resource.ResourceEntry;
import org.pentaho.di.resource.ResourceNamingInterface;
import org.pentaho.di.resource.ResourceReference;
import org.pentaho.di.resource.ResourceEntry.ResourceType;
import org.pentaho.di.trans.StepLoader;
import org.pentaho.di.www.SlaveServerJobStatus;
import org.w3c.dom.Node;


/**
 * Recursive definition of a Job.  This step means that an entire Job has to be executed.
 * It can be the same Job, but just make sure that you don't get an endless loop.
 * Provide an escape routine using JobEval.
 *
 * @author Matt
 * @since 01-10-2003, Rewritten on 18-06-2004
 *
 */
public class JobEntryJob extends JobEntryBase implements Cloneable, JobEntryInterface
{
    private static final LogWriter log = LogWriter.getInstance();

	private String              jobname;
	private String              filename;
	private String directory;

	public  String  arguments[];
	public  boolean argFromPrevious;
	public  boolean paramsFromPrevious;
    public  boolean execPerRow;
    
    public  String  parameters[];
    public  String  parameterFieldNames[];
    public  String  parameterValues[];

	public  boolean setLogfile;
	public  String  logfile, logext;
	public  boolean addDate, addTime;
	public  int     loglevel;

	public  boolean parallel;
    private String directoryPath;
    public boolean setAppendLogfile;
    
	public  boolean waitingToFinish=true;
    public  boolean followingAbortRemotely;
    
    private String remoteSlaveServerName;
    public  boolean passingAllParameters=true;

    public JobEntryJob(String name)
	{
		super(name, "");
		setJobEntryType(JobEntryType.JOB);
	}

	public JobEntryJob()
	{
		this("");
		clear();
	}

    public Object clone()
    {
        JobEntryJob je = (JobEntryJob) super.clone();
        return je;
    }

	public JobEntryJob(JobEntryBase jeb)
	{
		super(jeb);
	}

	public void setFileName(String n)
	{
		filename=n;
	}

    /**
     * @deprecated use getFilename() instead.
     * @return the filename
     */
	public String getFileName()
	{
		return filename;
	}

    public String getFilename()
    {
        return filename;
    }

    public String getRealFilename()
    {
        return environmentSubstitute(getFilename());
    }

	public void setJobName(String jobname)
	{
		this.jobname=jobname;
	}

	public String getJobName()
	{
		return jobname;
	}

	public String getDirectory()
	{
		return directory;
	}

	public void setDirectory(String directory)
	{
		this.directory = directory;
	}

	public String getLogFilename()
	{
		String retval="";
		if (setLogfile)
		{
			retval+=logfile;
			Calendar cal = Calendar.getInstance();
			if (addDate)
			{
				SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
				retval+="_"+sdf.format(cal.getTime());
			}
			if (addTime)
			{
				SimpleDateFormat sdf = new SimpleDateFormat("HHmmss");
				retval+="_"+sdf.format(cal.getTime());
			}
			if (logext!=null && logext.length()>0)
			{
				retval+="."+logext;
			}
		}
		return retval;
	}

	public String getXML()
	{
        StringBuffer retval = new StringBuffer(200);

		retval.append(super.getXML());

		retval.append("      ").append(XMLHandler.addTagValue("filename",          filename));
		retval.append("      ").append(XMLHandler.addTagValue("jobname",           jobname));
		if (directory!=null)
		{
			retval.append("      ").append(XMLHandler.addTagValue("directory",         directory));
		}
		else
			if (directoryPath!=null)
			{
				retval.append("      ").append(XMLHandler.addTagValue("directory",         directoryPath)); // don't loose this info (backup/recovery)
			}
		retval.append("      ").append(XMLHandler.addTagValue("arg_from_previous", argFromPrevious));
		retval.append("      ").append(XMLHandler.addTagValue("params_from_previous", paramsFromPrevious));
		retval.append("      ").append(XMLHandler.addTagValue("exec_per_row",      execPerRow));
		retval.append("      ").append(XMLHandler.addTagValue("set_logfile",       setLogfile));
		retval.append("      ").append(XMLHandler.addTagValue("logfile",           logfile));
		retval.append("      ").append(XMLHandler.addTagValue("logext",            logext));
		retval.append("      ").append(XMLHandler.addTagValue("add_date",          addDate));
		retval.append("      ").append(XMLHandler.addTagValue("add_time",          addTime));
		retval.append("      ").append(XMLHandler.addTagValue("loglevel",          LogWriter.getLogLevelDesc(loglevel)));
		retval.append("      ").append(XMLHandler.addTagValue("slave_server_name", remoteSlaveServerName));
		retval.append("      ").append(XMLHandler.addTagValue("wait_until_finished",     waitingToFinish));
		retval.append("      ").append(XMLHandler.addTagValue("follow_abort_remote",     followingAbortRemotely));
		
		if (arguments!=null)  {
			for (int i=0;i<arguments.length;i++)
			{
				// This is a very very bad way of making an XML file, don't use it (or copy it). Sven Boden
				retval.append("      ").append(XMLHandler.addTagValue("argument"+i, arguments[i]));
			}
		}
		
		if (parameters!=null)  {
			retval.append("      ").append(XMLHandler.openTag("parameters"));
			
			retval.append("        ").append(XMLHandler.addTagValue("pass_all_parameters", passingAllParameters));
			
			for (int i=0;i<parameters.length;i++)
			{
				// This is a better way of making the XML file than the arguments.
				retval.append("            ").append(XMLHandler.openTag("parameter"));
				
				retval.append("            ").append(XMLHandler.addTagValue("name", parameters[i]));
				retval.append("            ").append(XMLHandler.addTagValue("stream_name", parameterFieldNames[i]));
				retval.append("            ").append(XMLHandler.addTagValue("value", parameterValues[i]));
				
				retval.append("            ").append(XMLHandler.closeTag("parameter"));
			}
			retval.append("      ").append(XMLHandler.closeTag("parameters"));
		}		
		retval.append("      ").append(XMLHandler.addTagValue("set_append_logfile",     setAppendLogfile));
		
		
		return retval.toString();
	}

	public void loadXML(Node entrynode, List<DatabaseMeta> databases, List<SlaveServer> slaveServers, Repository rep) throws KettleXMLException {
		try {
			super.loadXML(entrynode, databases, slaveServers);

			setFileName(XMLHandler.getTagValue(entrynode, "filename"));
			setJobName(XMLHandler.getTagValue(entrynode, "jobname"));
			argFromPrevious = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "arg_from_previous"));
			paramsFromPrevious = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "params_from_previous"));
			execPerRow = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "exec_per_row"));
			setLogfile = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "set_logfile"));
			addDate = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "add_date"));
			addTime = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "add_time"));
			logfile = XMLHandler.getTagValue(entrynode, "logfile");
			logext = XMLHandler.getTagValue(entrynode, "logext");
			loglevel = LogWriter.getLogLevel(XMLHandler.getTagValue(entrynode, "loglevel"));
			setAppendLogfile = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "set_append_logfile"));
			remoteSlaveServerName = XMLHandler.getTagValue(entrynode, "slave_server_name");
			directory = XMLHandler.getTagValue(entrynode, "directory");
			
			String wait = XMLHandler.getTagValue(entrynode, "wait_until_finished");
			if (Const.isEmpty(wait)) waitingToFinish=true;
			else waitingToFinish = "Y".equalsIgnoreCase( wait );

			followingAbortRemotely = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "follow_abort_remote"));

			// How many arguments?
			int argnr = 0;
			while (XMLHandler.getTagValue(entrynode, "argument" + argnr) != null)
				argnr++;
			arguments = new String[argnr];

			// Read them all... This is a very BAD way to do it by the way. Sven Boden.
			for (int a = 0; a < argnr; a++)  {
				arguments[a] = XMLHandler.getTagValue(entrynode, "argument" + a);
			}
			
			Node parametersNode = XMLHandler.getSubNode(entrynode, "parameters");   //$NON-NLS-1$
			
			String passAll = XMLHandler.getTagValue(parametersNode, "pass_all_parameters");
			passingAllParameters = Const.isEmpty(passAll) || "Y".equalsIgnoreCase(passAll);

			int nrParameters  = XMLHandler.countNodes(parametersNode, "parameter");       //$NON-NLS-1$
			
			parameters = new String[nrParameters];
			parameterFieldNames = new String[nrParameters];
			parameterValues = new String[nrParameters];
			
			for (int i=0;i<nrParameters;i++)
			{
				Node knode = XMLHandler.getSubNodeByNr(parametersNode, "parameter", i);         //$NON-NLS-1$
				
				parameters         [i] = XMLHandler.getTagValue(knode, "name");        //$NON-NLS-1$
				parameterFieldNames[i] = XMLHandler.getTagValue(knode, "stream_name"); //$NON-NLS-1$
				parameterValues    [i] = XMLHandler.getTagValue(knode, "value");       //$NON-NLS-1$
			}				
		} catch (KettleXMLException xe) {
			throw new KettleXMLException("Unable to load 'job' job entry from XML node", xe);
		}
	}

	/**
	 * Load the jobentry from repository
	 */
	public void loadRep(Repository rep, long id_jobentry, List<DatabaseMeta> databases, List<SlaveServer> slaveServers) throws KettleException {
		try {
			super.loadRep(rep, id_jobentry, databases, slaveServers);

			jobname = rep.getJobEntryAttributeString(id_jobentry, "name");
			directory = rep.getJobEntryAttributeString(id_jobentry, "dir_path");
			filename = rep.getJobEntryAttributeString(id_jobentry, "file_name");
			argFromPrevious = rep.getJobEntryAttributeBoolean(id_jobentry, "arg_from_previous");
			paramsFromPrevious = rep.getJobEntryAttributeBoolean(id_jobentry, "params_from_previous");			
			execPerRow = rep.getJobEntryAttributeBoolean(id_jobentry, "exec_per_row");
			setLogfile = rep.getJobEntryAttributeBoolean(id_jobentry, "set_logfile");
			addDate = rep.getJobEntryAttributeBoolean(id_jobentry, "add_date");
			addTime = rep.getJobEntryAttributeBoolean(id_jobentry, "add_time");
			logfile = rep.getJobEntryAttributeString(id_jobentry, "logfile");
			logext = rep.getJobEntryAttributeString(id_jobentry, "logext");
			loglevel = LogWriter.getLogLevel(rep.getJobEntryAttributeString(id_jobentry, "loglevel"));
			setAppendLogfile = rep.getJobEntryAttributeBoolean(id_jobentry, "set_append_logfile");
			remoteSlaveServerName = rep.getJobEntryAttributeString(id_jobentry, "slave_server_name");
			waitingToFinish = rep.getJobEntryAttributeBoolean(id_jobentry, "wait_until_finished", true);
			followingAbortRemotely = rep.getJobEntryAttributeBoolean(id_jobentry, "follow_abort_remote");

			// How many arguments?
			int argnr = rep.countNrJobEntryAttributes(id_jobentry, "argument");
			arguments = new String[argnr];

			// Read all arguments ...
			for (int a = 0; a < argnr; a++) {
				arguments[a] = rep.getJobEntryAttributeString(id_jobentry, a, "argument");
			}

			// How many arguments?
			int parameternr = rep.countNrJobEntryAttributes(id_jobentry, "parameter_name");
			parameters = new String[parameternr];
			parameterFieldNames = new String[parameternr];
			parameterValues = new String[parameternr];

			// Read all parameters ...
			for (int a = 0; a < parameternr; a++) {
				parameters[a] = rep.getJobEntryAttributeString(id_jobentry, a, "parameter_name");
				parameterFieldNames[a] = rep.getJobEntryAttributeString(id_jobentry, a, "parameter_stream_name");
				parameterValues[a] = rep.getJobEntryAttributeString(id_jobentry, a, "parameter_value");
			}
			
			passingAllParameters = rep.getJobEntryAttributeBoolean(id_jobentry, "pass_all_parameters", true);
			
		} catch (KettleDatabaseException dbe) {
			throw new KettleException("Unable to load job entry of type 'job' from the repository with id_jobentry=" + id_jobentry, dbe);
		}
	}

	// Save the attributes of this job entry
	//
	public void saveRep(Repository rep, long id_job) throws KettleException
	{
		try
		{
			super.saveRep(rep, id_job);

			if (rep.getImportBaseDirectory()!=null && !rep.getImportBaseDirectory().isRoot()) {
				directory = rep.getImportBaseDirectory().getPath() + directoryPath;
			}
			
			if (directory == null) {
				if (rep.getImportBaseDirectory()!=null) {
					directory = rep.getImportBaseDirectory().getPath();
				} else {
					directory = new RepositoryDirectory().getPath(); // just pick the root directory
				}
			}

			// Removed id_job as we do not know what it is if we are using variables in the path
			//	long id_job_attr = rep.getJobID(jobname, directory.getID());
			// rep.saveJobEntryAttribute(id_job, getID(), "id_job", id_job_attr);
			
			rep.saveJobEntryAttribute(id_job, getID(), "name", getJobName());
      		rep.saveJobEntryAttribute(id_job, getID(), "dir_path", getDirectory()!=null?getDirectory():"");
      		rep.saveJobEntryAttribute(id_job, getID(), "file_name", filename);
			rep.saveJobEntryAttribute(id_job, getID(), "arg_from_previous", argFromPrevious);
			rep.saveJobEntryAttribute(id_job, getID(), "params_from_previous", paramsFromPrevious);
			rep.saveJobEntryAttribute(id_job, getID(), "exec_per_row", execPerRow);
			rep.saveJobEntryAttribute(id_job, getID(), "set_logfile", setLogfile);
			rep.saveJobEntryAttribute(id_job, getID(), "add_date", addDate);
			rep.saveJobEntryAttribute(id_job, getID(), "add_time", addTime);
			rep.saveJobEntryAttribute(id_job, getID(), "logfile", logfile);
			rep.saveJobEntryAttribute(id_job, getID(), "logext", logext);
			rep.saveJobEntryAttribute(id_job, getID(), "set_append_logfile", setAppendLogfile);
			rep.saveJobEntryAttribute(id_job, getID(), "loglevel", LogWriter.getLogLevelDesc(loglevel));
			rep.saveJobEntryAttribute(id_job, getID(), "slave_server_name", remoteSlaveServerName);
			rep.saveJobEntryAttribute(id_job, getID(), "wait_until_finished", waitingToFinish);
			rep.saveJobEntryAttribute(id_job, getID(), "follow_abort_remote", followingAbortRemotely);

			// save the arguments...
			if (arguments!=null)
			{
				for (int i=0;i<arguments.length;i++)
				{
					rep.saveJobEntryAttribute(id_job, getID(), i, "argument", arguments[i]);
				}
			}
			
			// save the parameters...
			if (parameters!=null)
			{
				for (int i=0;i<parameters.length;i++)
				{
					rep.saveJobEntryAttribute(id_job, getID(), i, "parameter_name", parameters[i]);
					rep.saveJobEntryAttribute(id_job, getID(), i, "parameter_stream_name", Const.NVL(parameterFieldNames[i], ""));
					rep.saveJobEntryAttribute(id_job, getID(), i, "parameter_value", Const.NVL(parameterValues[i], ""));
				}
			}		
			
			rep.saveJobEntryAttribute(id_job, getID(), "pass_all_parameters", passingAllParameters);
		}
		catch(KettleDatabaseException dbe)
		{
			throw new KettleException("Unable to save job entry of type job to the repository with id_job="+id_job, dbe);
		}
	}

	public Result execute(Result result, int nr, Repository rep, Job parentJob) throws KettleException
	{
	    result.setEntryNr( nr );

        LogWriter logwriter = log;
        
        Log4jFileAppender appender = null;
        int backupLogLevel = log.getLogLevel();
        if (setLogfile)
        {
            try
            {
                appender = LogWriter.createFileAppender(environmentSubstitute(getLogFilename()), true,setAppendLogfile);
            }
            catch(KettleException e)
            {
                log.logError(toString(), "Unable to open file appender for file ["+getLogFilename()+"] : "+e.toString());
                log.logError(toString(), Const.getStackTracker(e));
                result.setNrErrors(1);
                result.setResult(false);
                return result;
            }
            log.addAppender(appender);
            log.setLogLevel(loglevel);

            logwriter = LogWriter.getInstance(environmentSubstitute(getLogFilename()), true, loglevel);
        }

        // Figure out the remote slave server...
        //
        SlaveServer remoteSlaveServer = null;
        if (!Const.isEmpty(remoteSlaveServerName)) {
        	String realRemoteSlaveServerName = environmentSubstitute(remoteSlaveServerName);
        	remoteSlaveServer = parentJob.getJobMeta().findSlaveServer(realRemoteSlaveServerName);
        	if (remoteSlaveServer==null) {
        		throw new KettleException(Messages.getString("JobJob.Exception.UnableToFindRemoteSlaveServer",realRemoteSlaveServerName));
        	}
        }
        try
        {
            // First load the job, outside of the loop...
        	if ( parentJob.getJobMeta() != null )
        	{
        		// reset the internal variables again.
        		// Maybe we should split up the variables even more like in UNIX shells.
        		// The internal variables need to be reset to be able use them properly in 2 sequential sub jobs.
        		parentJob.getJobMeta().setInternalKettleVariables();
        	}

            JobMeta jobMeta = null;
            boolean fromRepository = rep!=null && !Const.isEmpty(jobname) && directory!=null;
            boolean fromXMLFile = !Const.isEmpty(filename);
            if (fromRepository) // load from the repository...
            {
                if(log.isDetailed()) log.logDetailed(toString(), "Loading job from repository : ["+directory+" : "+environmentSubstitute(jobname)+"]");
                jobMeta = new JobMeta(logwriter, rep, environmentSubstitute(jobname), rep.getDirectoryTree().findDirectory(environmentSubstitute(directory)));
                jobMeta.setParentVariableSpace(parentJob);
            }
            else // Get it from the XML file
            if (fromXMLFile)
            {
            	if(log.isDetailed()) log.logDetailed(toString(), "Loading job from XML file : ["+environmentSubstitute(filename)+"]");
                jobMeta = new JobMeta(logwriter, environmentSubstitute(filename), rep, null);
                jobMeta.setParentVariableSpace(parentJob);
            }

            if (jobMeta==null)
            {
                throw new KettleException("Unable to load the job: please specify the name and repository directory OR a filename");
            }
            
            verifyRecursiveExecution(parentJob, jobMeta);
    		
            // Tell logging what job entry we are launching...
            if (fromRepository)
            {
                if(log.isBasic()) log.logBasic(toString(), "Starting job, loaded from repository : ["+directory+" : "+environmentSubstitute(jobname)+"]");
            }
            else
            if (fromXMLFile)
            {
            	if(log.isDetailed()) log.logDetailed(toString(), "Starting job, loaded from XML file : ["+environmentSubstitute(filename)+"]");
            }

            int iteration = 0;
            String args1[] = arguments;
            if (args1==null || args1.length==0) // no arguments?  Check the parent jobs arguments
            {
                args1 = parentJob.getJobMeta().getArguments();
            }

            copyVariablesFrom(parentJob);
            setParentVariableSpace(parentJob);

            //
            // For the moment only do variable translation at the start of a job, not
            // for every input row (if that would be switched on)
            //
            String args[] = null;
            if ( args1 != null )
            {
                args = new String[args1.length];
                for ( int idx = 0; idx < args1.length; idx++ )
                {
                	args[idx] = environmentSubstitute(args1[idx]);
                }
            }
            
            NamedParams namedParam = new NamedParamsDefault();
            if ( parameters != null )  {
            	for ( int idx = 0; idx < parameters.length; idx++ )
                {
            		if ( !Const.isEmpty(parameters[idx]) )  {
            			
            			// We have a parameter            			
            			try {
							namedParam.addParameterDefinition(parameters[idx], "", "Job entry runtime");
						} catch (DuplicateParamException e) {
							log.logError(toString(), "Duplicate parameter definition for " + parameters[idx]);
						}
						
            			if ( Const.isEmpty(Const.trim(parameterFieldNames[idx])) )  {
            				namedParam.setParameterValue(parameters[idx], 
				                     Const.NVL(environmentSubstitute(parameterValues[idx]), ""));            				
            			}            				            		
            			else  {
            				// something filled in, in the field column but we have no incoming stream. yet.
            				namedParam.setParameterValue(parameters[idx], "");
            			}
            		}                                
                }
            }

            RowMetaAndData resultRow = null;
            boolean first = true;
            List<RowMetaAndData> rows = new ArrayList<RowMetaAndData>(result.getRows());

            while( ( first && !execPerRow ) || ( execPerRow && rows!=null && iteration<rows.size() && result.getNrErrors()==0 ) )
            {
            	if (execPerRow)
            	{
            		result.getRows().clear();
            	}
                first=false;
                if (rows!=null && execPerRow)
                {
                	resultRow = (RowMetaAndData) rows.get(iteration);
                }
                else
                {
                	resultRow = null;
                }
                
                Result oneResult = new Result();
            	
            	List<RowMetaAndData> sourceRows = null;
                
                if (execPerRow) // Execute for each input row
                {
                    if (argFromPrevious) // Copy the input row to the (command line) arguments
                    {
                        args = null;
                        if (resultRow!=null)
                        {
                            args = new String[resultRow.size()];
                            for (int i=0;i<resultRow.size();i++)
                            {
                                args[i] = resultRow.getString(i, null);
                            }
                        }
                    }
                    else
                    {
                        // Just pass a single row
                        List<RowMetaAndData> newList = new ArrayList<RowMetaAndData>();
                        newList.add(resultRow);
                        sourceRows = newList;
                    }

                    if ( paramsFromPrevious )  { // Copy the input the parameters
                    	
                    	if ( parameters != null )  {
                    		for ( int idx = 0; idx < parameters.length; idx++ )
                    		{
                    			if ( !Const.isEmpty(parameters[idx]) )  {
                    				// We have a parameter
                    				if ( Const.isEmpty(Const.trim(parameterFieldNames[idx])) )  {
                    					namedParam.setParameterValue(parameters[idx], 
                    							Const.NVL(environmentSubstitute(parameterValues[idx]), ""));            				
                    				}            				            		
                    				else  {
                    					String fieldValue = "";
                    					
                    					if (resultRow!=null)  {
                    						fieldValue = resultRow.getString(parameterFieldNames[idx], "");
                    					}
                    					// Get the value from the input stream
                    					namedParam.setParameterValue(parameters[idx], 
                    							                     Const.NVL(fieldValue, ""));
                    				}
                    			}                                    	
                    		}
                    	}
                    }
                }
                else
                {
                    if (argFromPrevious)
                    {
                        // Only put the first Row on the arguments
                        args = null;
                        if (resultRow!=null)
                        {
                            args = new String[resultRow.size()];
                            for (int i=0;i<resultRow.size();i++)
                            {
                                args[i] = resultRow.getString(i, null);
                            }
                        }
                    }
                    else
                    {
                        // Keep it as it was...
                        sourceRows = result.getRows();
                    }
                    
                    if ( paramsFromPrevious )  { // Copy the input the parameters
                    	
                    	if ( parameters != null )  {
                    		for ( int idx = 0; idx < parameters.length; idx++ )
                    		{
                    			if ( !Const.isEmpty(parameters[idx]) )  {
                    				// We have a parameter
                    				if ( Const.isEmpty(Const.trim(parameterFieldNames[idx])) )  {
                    					namedParam.setParameterValue(parameters[idx], 
                    							Const.NVL(environmentSubstitute(parameterValues[idx]), ""));            				
                    				}            				            		
                    				else  {
                    					String fieldValue = "";
                    					
                    					if (resultRow!=null)  {
                    						fieldValue = resultRow.getString(parameterFieldNames[idx], "");
                    					}
                    					// Get the value from the input stream
                    					namedParam.setParameterValue(parameters[idx], 
                    							                     Const.NVL(fieldValue, ""));
                    				}
                    			}                                    	
                    		}
                    	}
                    }
                }

                if (remoteSlaveServer==null)
                {
                	// Local execution...
                	//
                	
	                // Create a new job
	                Job job = new Job(logwriter, StepLoader.getInstance(), rep, jobMeta);
	
	                job.shareVariablesWith(this);
	                job.setInternalKettleVariables(this);
	                job.copyParametersFrom(jobMeta);
	                
	                // Pass the socket repository all around.
	                //
	                job.setSocketRepository(parentJob.getSocketRepository());
	                
	                // Set the parameters calculated above on this instance.
	                //
	                job.clearParameters();
	                String[] parameterNames = job.listParameters();
	                for (int idx = 0; idx < parameterNames.length; idx++)  {
	                	// Grab the parameter value set in the job entry
	                	//
	                    String thisValue = namedParam.getParameterValue(parameterNames[idx]);
	                    if (!Const.isEmpty(thisValue)) {
	                    	// Set the value as specified by the user in the job entry
	                    	//
	                    	job.setParameterValue(parameterNames[idx], thisValue);
	                    } else {
	                    	// See if the parameter had a value set in the parent job...
	                    	// This value should pass down to the sub-job if that's what we opted to do.
	                    	//
	                    	if (isPassingAllParameters()) {
		                    	String parentValue = parentJob.getParameterValue(parameterNames[idx]);
		                    	if (!Const.isEmpty(parentValue)) {
		                    		job.setParameterValue(parameterNames[idx], parentValue);
		                    	}
	                    	}
	                    }
	                }
	                job.activateParameters();
	                
	                // Set the source rows we calculated above...
	                //
	                job.setSourceRows(sourceRows);
	
	                // Don't forget the logging...
	                job.beginProcessing();
	
	                // Link the job with the sub-job
	                parentJob.getJobTracker().addJobTracker(job.getJobTracker());
	
	                // Link both ways!
	                job.getJobTracker().setParentJobTracker(parentJob.getJobTracker());
	
	                // Tell this sub-job about its parent...
	                job.setParentJob(parentJob);
	
	                if (parentJob.getJobMeta().isBatchIdPassed())
	                {
	                    job.setPassedBatchId(parentJob.getBatchId());
	                }
	
	
	                job.getJobMeta().setArguments( args );	               
	
	                JobEntryJobRunner runner = new JobEntryJobRunner( job, result, nr);
	    			Thread jobRunnerThread = new Thread(runner);
	                jobRunnerThread.setName( Const.NVL(job.getJobMeta().getName(), job.getJobMeta().getFilename()) );
	                jobRunnerThread.start();
	
	                try
	                {
	        			while (!runner.isFinished() && !parentJob.isStopped())
	        			{
	        				try { Thread.sleep(0,1);}
	        				catch(InterruptedException e) { }
	        			}
	
	        			// if the parent-job was stopped, stop the sub-job too...
	        			if (parentJob.isStopped())
	        			{
	        				job.stopAll();
	        				runner.waitUntilFinished(); // Wait until finished!
	        				job.endProcessing("stop", new Result()); // dummy result
	        			}
	        			else
	        			{
	        				job.endProcessing(Database.LOG_STATUS_END, runner.getResult()); // the result of the execution to be stored in the log file.
	        			}
	                }
	        		catch(KettleException je)
	        		{
	        			log.logError(toString(), "Unable to open job entry job with name ["+getName()+"] : "+Const.CR+je.toString());
	        			result.setNrErrors(1);
	        		}
	        		
	        		oneResult = runner.getResult();
                }
                else
                {
                	// Remote execution...
                	//
                	JobExecutionConfiguration jobExecutionConfiguration = new JobExecutionConfiguration();
                	jobExecutionConfiguration.setPreviousResult(result.clone());
                	jobExecutionConfiguration.getPreviousResult().setRows(sourceRows);
                	jobExecutionConfiguration.setArgumentStrings(args);
                	jobExecutionConfiguration.setVariables(this);
                	jobExecutionConfiguration.setRemoteServer(remoteSlaveServer);
                	jobExecutionConfiguration.setRepository(rep);
                	jobExecutionConfiguration.setLogLevel(log.getLogLevel());

                	// Send the XML over to the slave server
                	// Also start the job over there...
                	//
                	try {
                		Job.sendToSlaveServer(jobMeta, jobExecutionConfiguration, rep);
                	} catch(KettleException e) {
                		// Perhaps the job exists on the remote server, carte is down, etc.
                		// This is an abort situation, stop the parent job...
                		// We want this in case we are running in parallel.  The other job entries can stop running now.
                		// 
                		parentJob.stopAll();
                		
                		// Pass the exception along
                		// 
                		throw e;
                	}
                	
                	// Now start the monitoring...
                	//
                	SlaveServerJobStatus jobStatus=null;
                	while (!parentJob.isStopped() && waitingToFinish)
                	{
                		try 
                		{
							jobStatus = remoteSlaveServer.getJobStatus(jobMeta.getName());
							if (jobStatus.getResult()!=null)
							{
								// The job is finished, get the result...
								//
								oneResult = jobStatus.getResult();
								break;
							}
						} 
                		catch (Exception e1) {
							log.logError(toString(), "Unable to contact slave server ["+remoteSlaveServer+"] to verify the status of job ["+jobMeta.getName()+"]");
							oneResult.setNrErrors(1L);
							break; // Stop looking too, chances are too low the server will come back on-line
						}
                		
                		try { Thread.sleep(10000); } catch(InterruptedException e) {} ; // sleep for 10 seconds
                	}
                	
                	if (!waitingToFinish) {
                		// Since the job was posted successfully, the result is true...
                		//
                		oneResult = new Result();
                		oneResult.setResult(true);
                	}
                	
                	if (parentJob.isStopped()) {
                		try 
                		{
	                		// See if we have a status and if we need to stop the remote execution here...
	                		// 
	                		if (jobStatus==null || jobStatus.isRunning()) {
	                			// Try a remote abort ...
	                			//
	                			remoteSlaveServer.stopJob(jobMeta.getName());
	                		}
                		}
                		catch (Exception e1) {
							log.logError(toString(), "Unable to contact slave server ["+remoteSlaveServer+"] to stop job ["+jobMeta.getName()+"]");
							oneResult.setNrErrors(1L);
							break; // Stop looking too, chances are too low the server will come back on-line
						}
                	}

                }
                
                if (iteration==0)
                {
                    result.clear();
                }
                
                result.add(oneResult);
                if (oneResult.getResult()==false) // if one of them fails, set the number of errors
                {
                    result.setNrErrors(result.getNrErrors()+1);
                }

                iteration++;
            }

        }
        catch(KettleException ke)
        {
            log.logError(toString(), "Error running job entry 'job' : "+ke.toString());
            log.logError(toString(), Const.getStackTracker(ke));

            result.setResult(false);
            result.setNrErrors(1L);
        }

        if (setLogfile)
        {
            if (appender!=null)
            {
                log.removeAppender(appender);
                appender.close();

                ResultFile resultFile = new ResultFile(ResultFile.FILE_TYPE_LOG, appender.getFile(), parentJob.getJobname(), getName());
                result.getResultFiles().put(resultFile.getFile().toString(), resultFile);
            }
            log.setLogLevel(backupLogLevel);

        }

        if (result.getNrErrors() > 0)
        {
            result.setResult( false );
        }
        else
        {
            result.setResult( true );
        }

        return result;
	}
	
	
	
	
	/**
	 * Make sure that we are not loading jobs recursively...
	 * 
	 * @param parentJobMeta the parent job metadata
	 * @param jobMeta the job metadata
	 * @throws KettleException in case both jobs are loaded from the same source
	 */
    private void verifyRecursiveExecution(Job parentJob, JobMeta jobMeta) throws KettleException {
    	
    	if (parentJob==null) return; // OK!
    	
    	JobMeta parentJobMeta = parentJob.getJobMeta();
    	
    	if (parentJobMeta.getName()==null && jobMeta.getName()!=null) return; // OK
    	if (parentJobMeta.getName()!=null && jobMeta.getName()==null) return; // OK as well.
    	
		// Not from the repository? just verify the filename
		//
		if (jobMeta.getFilename()!=null && jobMeta.getFilename().equals(parentJobMeta.getFilename()))
		{
			throw new KettleException(Messages.getString("JobJobError.Recursive", jobMeta.getFilename()));
		}

		// Different directories: OK
		if (parentJobMeta.getDirectory()==null && jobMeta.getDirectory()!=null) return; 
		if (parentJobMeta.getDirectory()!=null && jobMeta.getDirectory()==null) return; 
		if (jobMeta.getDirectory().getID() != parentJobMeta.getDirectory().getID()) return;
		
		// Same names, same directories : loaded from same location in the repository: 
		// --> recursive loading taking place!
		//
		if (parentJobMeta.getName().equals(jobMeta.getName()))
		{
			throw new KettleException(Messages.getString("JobJobError.Recursive", jobMeta.getFilename()));
		}
		
		// Also compare with the grand-parent (if there is any)
		verifyRecursiveExecution(parentJob.getParentJob(), jobMeta);
   	}

	public void clear()
	{
		super.clear();

		jobname=null;
		filename=null;
		directory = null;
		arguments=null;
		argFromPrevious=false;
		addDate=false;
		addTime=false;
		logfile=null;
		logext=null;
		setLogfile=false;
		setAppendLogfile=false;
	}

	public boolean evaluates()
	{
		return true;
	}

	public boolean isUnconditional()
	{
		return true;
	}

    public List<SQLStatement> getSQLStatements(Repository repository) throws KettleException
    {
        return getSQLStatements(repository, null);
    }

    public List<SQLStatement> getSQLStatements(Repository repository, VariableSpace space) throws KettleException
    {
    	this.copyVariablesFrom(space);
        JobMeta jobMeta = getJobMeta(repository, space);
        return jobMeta.getSQLStatements(repository, null);
    }
    
    
    private JobMeta getJobMeta(Repository rep, VariableSpace space) throws KettleException
    {   	
    	try
    	{
	        if (rep!=null && getDirectory()!=null)
	        {
	            return new JobMeta(LogWriter.getInstance(), 
	            		           rep, 
	            		           (space != null ? space.environmentSubstitute(getJobName()): getJobName()), 
                             rep.getDirectoryTree().findDirectory(environmentSubstitute(getDirectory())));
	        }
	        else
	        {
	            return new JobMeta(LogWriter.getInstance(), 
	            		           (space != null ? space.environmentSubstitute(getFilename()) : getFilename()), 
	            		           rep, null);
	        }
    	}
		catch(Exception e)
		{
			throw new KettleException("Unexpected error during job metadata load", e);
		}

    }

    /**
     * @return Returns the runEveryResultRow.
     */
    public boolean isExecPerRow()
    {
        return execPerRow;
    }

    /**
     * @param runEveryResultRow The runEveryResultRow to set.
     */
    public void setExecPerRow(boolean runEveryResultRow)
    {
        this.execPerRow = runEveryResultRow;
    }

    public List<ResourceReference> getResourceDependencies(JobMeta jobMeta) {
      List<ResourceReference> references = super.getResourceDependencies(jobMeta);
      if (!Const.isEmpty(filename)) {
        String realFileName = jobMeta.environmentSubstitute(filename);
        ResourceReference reference = new ResourceReference(this);
        reference.getEntries().add( new ResourceEntry(realFileName, ResourceType.ACTIONFILE));
        references.add(reference);
      }
      return references;
    }

    /**
     * We're going to load the transformation meta data referenced here.
     * Then we're going to give it a new filename, modify that filename in this entries.
     * The parent caller will have made a copy of it, so it should be OK to do so.
     */
    public String exportResources(VariableSpace space, Map<String, ResourceDefinition> definitions, ResourceNamingInterface namingInterface, Repository repository) throws KettleException {
		// Try to load the transformation from repository or file.
		// Modify this recursively too...
		//
		// AGAIN: there is no need to clone this job entry because the caller is responsible for this.
		//
		// First load the job meta data...
		//
		copyVariablesFrom(space);  // To make sure variables are available.
		JobMeta jobMeta = getJobMeta(repository, space);

		// Also go down into the job and export the files there. (going down recursively)
		//
		String proposedNewFilename = jobMeta.exportResources(jobMeta, definitions, namingInterface, repository);

		// To get a relative path to it, we inject ${Internal.Job.Filename.Directory} 
		//
		String newFilename = "${"+Const.INTERNAL_VARIABLE_JOB_FILENAME_DIRECTORY+"}/"+proposedNewFilename;

		// Set the filename in the job
		//
		jobMeta.setFilename(newFilename);

		// change it in the job entry
		//
		filename = newFilename;

		return proposedNewFilename;
    }

    @Override
    public void check(List<CheckResultInterface> remarks, JobMeta jobMeta)
    {
      if (setLogfile) {
        andValidator().validate(this, "logfile", remarks, putValidators(notBlankValidator())); //$NON-NLS-1$
      }

      if (null != directory) {
        // if from repo
        andValidator().validate(this, "directory", remarks, putValidators(notNullValidator())); //$NON-NLS-1$
        andValidator().validate(this, "jobName", remarks, putValidators(notBlankValidator())); //$NON-NLS-1$
      } else {
        // else from xml file
        andValidator().validate(this, "filename", remarks, putValidators(notBlankValidator())); //$NON-NLS-1$
      }
    }

      public static void main(String[] args) {
    List<CheckResultInterface> remarks = new ArrayList<CheckResultInterface>();
    new JobEntryJob().check(remarks, null);
    System.out.printf("Remarks: %s\n", remarks);
  }

    protected String getLogfile()
    {
      return logfile;
    }

	/**
	 * @return the remote slave server name
	 */
	public String getRemoteSlaveServerName() {
		return remoteSlaveServerName;
	}

	/**
	 * @param remoteSlaveServerName the remoteSlaveServer to set
	 */
	public void setRemoteSlaveServerName(String remoteSlaveServerName) {
		this.remoteSlaveServerName = remoteSlaveServerName;
	}

	/**
	 * @return the waitingToFinish
	 */
	public boolean isWaitingToFinish() {
		return waitingToFinish;
	}

	/**
	 * @param waitingToFinish the waitingToFinish to set
	 */
	public void setWaitingToFinish(boolean waitingToFinish) {
		this.waitingToFinish = waitingToFinish;
	}

	/**
	 * @return the followingAbortRemotely
	 */
	public boolean isFollowingAbortRemotely() {
		return followingAbortRemotely;
	}

	/**
	 * @param followingAbortRemotely the followingAbortRemotely to set
	 */
	public void setFollowingAbortRemotely(boolean followingAbortRemotely) {
		this.followingAbortRemotely = followingAbortRemotely;
	}

	/**
	 * @return the passingAllParameters
	 */
	public boolean isPassingAllParameters() {
		return passingAllParameters;
	}

	/**
	 * @param passingAllParameters the passingAllParameters to set
	 */
	public void setPassingAllParameters(boolean passingAllParameters) {
		this.passingAllParameters = passingAllParameters;
	}

}