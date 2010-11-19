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

package org.pentaho.di.job.entries.shell;

import static org.pentaho.di.job.entry.validator.AbstractFileValidator.putVariableSpace;
import static org.pentaho.di.job.entry.validator.AndValidator.putValidators;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.andValidator;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.fileExistsValidator;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.notBlankValidator;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.vfs.FileObject;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.logging.Log4jFileAppender;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.util.StreamLogger;
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
 * Shell type of Job Entry. You can define shell scripts to be executed in a
 * Job.
 * 
 * @author Matt
 * @since 01-10-2003, rewritten on 18-06-2004
 */
public class JobEntryShell extends JobEntryBase implements Cloneable, JobEntryInterface
{
	private String filename;

	private String workDirectory;

	public String arguments[];

	public boolean argFromPrevious;

	public boolean setLogfile;

	public String logfile, logext;

	public boolean addDate, addTime;

	public int loglevel;

	public boolean execPerRow;
	
	public boolean setAppendLogfile;
	
	public boolean insertScript;
	
	public String script;

	public JobEntryShell(String name)
	{
		super(name, "");
		setJobEntryType(JobEntryType.SHELL);
	}

	public JobEntryShell()
	{
		this("");
		clear();
	}

	public JobEntryShell(JobEntryBase jeb)
	{
		super(jeb);
		setJobEntryType(JobEntryType.SHELL);
	}

	public Object clone()
	{
		JobEntryShell je = (JobEntryShell) super.clone();
		return je;
	}

	public String getXML()
	{
		StringBuffer retval = new StringBuffer(300);

		retval.append(super.getXML());

		retval.append("      ").append(XMLHandler.addTagValue("filename", filename));
		retval.append("      ").append(XMLHandler.addTagValue("work_directory", workDirectory));
		retval.append("      ").append(XMLHandler.addTagValue("arg_from_previous", argFromPrevious));
		retval.append("      ").append(XMLHandler.addTagValue("exec_per_row", execPerRow));
		retval.append("      ").append(XMLHandler.addTagValue("set_logfile", setLogfile));
		retval.append("      ").append(XMLHandler.addTagValue("logfile", logfile));
		retval.append("      ").append(XMLHandler.addTagValue("set_append_logfile",     setAppendLogfile));
		retval.append("      ").append(XMLHandler.addTagValue("logext", logext));
		retval.append("      ").append(XMLHandler.addTagValue("add_date", addDate));
		retval.append("      ").append(XMLHandler.addTagValue("add_time", addTime));
		retval.append("      ").append(XMLHandler.addTagValue("insertScript", insertScript));
		retval.append("      ").append(XMLHandler.addTagValue("script", script));
		
		
		retval.append("      ").append(
				XMLHandler.addTagValue("loglevel", LogWriter.getLogLevelDesc(loglevel)));

		if (arguments != null)
			for (int i = 0; i < arguments.length; i++)
			{
				// THIS IS A VERY BAD WAY OF READING/SAVING AS IT MAKES
				// THE XML "DUBIOUS". DON'T REUSE IT. (Sven B)
				retval.append("      ").append(XMLHandler.addTagValue("argument" + i, arguments[i]));
			}

		return retval.toString();
	}

	public void loadXML(Node entrynode, List<DatabaseMeta> databases, List<SlaveServer> slaveServers,
			Repository rep) throws KettleXMLException
	{
		try
		{
			super.loadXML(entrynode, databases, slaveServers);
			setFileName(XMLHandler.getTagValue(entrynode, "filename"));
			setWorkDirectory(XMLHandler.getTagValue(entrynode, "work_directory"));
			argFromPrevious = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "arg_from_previous"));
			execPerRow = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "exec_per_row"));
			setLogfile = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "set_logfile"));
			setAppendLogfile = "Y".equalsIgnoreCase( XMLHandler.getTagValue(entrynode, "set_append_logfile") );
			addDate = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "add_date"));
			addTime = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "add_time"));
			logfile = XMLHandler.getTagValue(entrynode, "logfile");
			logext = XMLHandler.getTagValue(entrynode, "logext");
			loglevel = LogWriter.getLogLevel(XMLHandler.getTagValue(entrynode, "loglevel"));
			insertScript = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "insertScript"));
			
			script= XMLHandler.getTagValue(entrynode, "script");
			
			// How many arguments?
			int argnr = 0;
			while (XMLHandler.getTagValue(entrynode, "argument" + argnr) != null)
				argnr++;
			arguments = new String[argnr];

			// Read them all...
			// THIS IS A VERY BAD WAY OF READING/SAVING AS IT MAKES
			// THE XML "DUBIOUS". DON'T REUSE IT.
			for (int a = 0; a < argnr; a++)
				arguments[a] = XMLHandler.getTagValue(entrynode, "argument" + a);
		} catch (KettleException e)
		{
			throw new KettleXMLException("Unable to load job entry of type 'shell' from XML node", e);
		}
	}

	// Load the jobentry from repository
	public void loadRep(Repository rep, long id_jobentry, List<DatabaseMeta> databases,
			List<SlaveServer> slaveServers) throws KettleException
	{
		try
		{
			super.loadRep(rep, id_jobentry, databases, slaveServers);

			setFileName(rep.getJobEntryAttributeString(id_jobentry, "file_name"));
			setWorkDirectory(rep.getJobEntryAttributeString(id_jobentry, "work_directory"));
			argFromPrevious = rep.getJobEntryAttributeBoolean(id_jobentry, "arg_from_previous");
			execPerRow = rep.getJobEntryAttributeBoolean(id_jobentry, "exec_per_row");

			setLogfile = rep.getJobEntryAttributeBoolean(id_jobentry, "set_logfile");
			setAppendLogfile       = rep.getJobEntryAttributeBoolean(id_jobentry, "set_append_logfile");
			addDate = rep.getJobEntryAttributeBoolean(id_jobentry, "add_date");
			addTime = rep.getJobEntryAttributeBoolean(id_jobentry, "add_time");
			logfile = rep.getJobEntryAttributeString(id_jobentry, "logfile");
			logext = rep.getJobEntryAttributeString(id_jobentry, "logext");
			loglevel = LogWriter.getLogLevel(rep.getJobEntryAttributeString(id_jobentry, "loglevel"));
			insertScript = rep.getJobEntryAttributeBoolean(id_jobentry, "insertScript");
			
			script = rep.getJobEntryAttributeString(id_jobentry, "script");
			// How many arguments?
			int argnr = rep.countNrJobEntryAttributes(id_jobentry, "argument");
			arguments = new String[argnr];

			// Read them all...
			for (int a = 0; a < argnr; a++)
			{
				arguments[a] = rep.getJobEntryAttributeString(id_jobentry, a, "argument");
			}
		} catch (KettleDatabaseException dbe)
		{
			throw new KettleException(
					"Unable to load job entry of type 'shell' from the repository with id_jobentry="
							+ id_jobentry, dbe);
		}
	}

	// Save the attributes of this job entry
	//
	public void saveRep(Repository rep, long id_job) throws KettleException
	{
		try
		{
			super.saveRep(rep, id_job);

			rep.saveJobEntryAttribute(id_job, getID(), "file_name", filename);
			rep.saveJobEntryAttribute(id_job, getID(), "work_directory", workDirectory);
			rep.saveJobEntryAttribute(id_job, getID(), "arg_from_previous", argFromPrevious);
			rep.saveJobEntryAttribute(id_job, getID(), "exec_per_row", execPerRow);
			rep.saveJobEntryAttribute(id_job, getID(), "set_logfile", setLogfile);
			rep.saveJobEntryAttribute(id_job, getID(), "set_append_logfile", setAppendLogfile);
			rep.saveJobEntryAttribute(id_job, getID(), "add_date", addDate);
			rep.saveJobEntryAttribute(id_job, getID(), "add_time", addTime);
			rep.saveJobEntryAttribute(id_job, getID(), "logfile", logfile);
			rep.saveJobEntryAttribute(id_job, getID(), "logext", logext);
			rep.saveJobEntryAttribute(id_job, getID(), "loglevel", LogWriter.getLogLevelDesc(loglevel));
			rep.saveJobEntryAttribute(id_job, getID(), "insertScript", insertScript);
			rep.saveJobEntryAttribute(id_job, getID(), "script", script);
			
			// save the arguments...
			if (arguments != null)
			{
				for (int i = 0; i < arguments.length; i++)
				{
					rep.saveJobEntryAttribute(id_job, getID(), i, "argument", arguments[i]);
				}
			}
		} catch (KettleDatabaseException dbe)
		{
			throw new KettleException("Unable to save job entry of type 'shell' to the repository", dbe);
		}
	}

	public void clear()
	{
		super.clear();

		filename = null;
		workDirectory = null;
		arguments = null;
		argFromPrevious = false;
		addDate = false;
		addTime = false;
		logfile = null;
		logext = null;
		setLogfile = false;
		execPerRow = false;
		setAppendLogfile=false;
		insertScript=false;
		script=null;
	}

	public void setFileName(String n)
	{
		filename = n;
	}

	public String getFilename()
	{
		return filename;
	}

	public String getRealFilename()
	{
		return environmentSubstitute(getFilename());
	}

	public void setWorkDirectory(String n)
	{
		workDirectory = n;
	}

	public String getWorkDirectory()
	{
		return workDirectory;
	}
	
	public void setScript(String scriptin)
	{
		script=scriptin;
	}
	
	public String getScript()
	{
		return script;
	}

	public String getLogFilename()
	{
		String retval = "";
		if (setLogfile)
		{
			retval += logfile;
			Calendar cal = Calendar.getInstance();
			if (addDate)
			{
				SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
				retval += "_" + sdf.format(cal.getTime());
			}
			if (addTime)
			{
				SimpleDateFormat sdf = new SimpleDateFormat("HHmmss");
				retval += "_" + sdf.format(cal.getTime());
			}
			if (logext != null && logext.length() > 0)
			{
				retval += "." + logext;
			}
		}
		return retval;
	}

	public Result execute(Result result, int nr, Repository rep, Job parentJob) throws KettleException
	{
		LogWriter log = LogWriter.getInstance();

		Log4jFileAppender appender = null;
		int backupLogLevel = log.getLogLevel();
		if (setLogfile)
		{
			try
			{
				appender = LogWriter.createFileAppender(environmentSubstitute(getLogFilename()), true,setAppendLogfile);
			} catch (KettleException e)
			{
				log.logError(toString(),Messages.getString("JobEntryShell.Error.UnableopenAppenderFile",getLogFilename(), e.toString()));
				log.logError(toString(), Const.getStackTracker(e));
				result.setNrErrors(1);
				result.setResult(false);
				return result;
			}
			log.addAppender(appender);
			log.setLogLevel(loglevel);
		}

		result.setEntryNr(nr);

		// "Translate" the arguments for later
		String substArgs[] = null;
		if (arguments != null)
		{
			substArgs = new String[arguments.length];
			for (int idx = 0; idx < arguments.length; idx++)
			{
				substArgs[idx] = environmentSubstitute(arguments[idx]);
			}
		}

		int iteration = 0;
		String args[] = substArgs;
		RowMetaAndData resultRow = null;
		boolean first = true;
		List<RowMetaAndData> rows = result.getRows();
		
		if(log.isDetailed())
			log.logDetailed(toString(), Messages.getString("JobEntryShell.Log.FoundPreviousRows",""+(rows != null ? rows.size() : 0)));
		

		while ((first && !execPerRow)
				|| (execPerRow && rows != null && iteration < rows.size() && result.getNrErrors() == 0))
		{
			first = false;
			if (rows != null && execPerRow)
			{
				resultRow = (RowMetaAndData) rows.get(iteration);
			} else
			{
				resultRow = null;
			}

			List<RowMetaAndData> cmdRows = null;

			if (execPerRow) // Execute for each input row
			{
				if (argFromPrevious) // Copy the input row to the (command
										// line) arguments
				{
					if (resultRow != null)
					{
						args = new String[resultRow.size()];
						for (int i = 0; i < resultRow.size(); i++)
						{
							args[i] = resultRow.getString(i, null);
						}
					}
				} else
				{
					// Just pass a single row
					List<RowMetaAndData> newList = new ArrayList<RowMetaAndData>();
					newList.add(resultRow);
					cmdRows = newList;
				}
			} else
			{
				if (argFromPrevious)
				{
					// Only put the first Row on the arguments
					args = null;
					if (resultRow != null)
					{
						args = new String[resultRow.size()];
						for (int i = 0; i < resultRow.size(); i++)
						{
							args[i] = resultRow.getString(i, null);
						}
					} else
					{
						cmdRows = rows;
					}
				} else
				{
					// Keep it as it was...
					cmdRows = rows;
				}
			}

			executeShell(result, cmdRows, args);

			iteration++;
		}

		if (setLogfile)
		{
			if (appender != null)
			{
				log.removeAppender(appender);
				appender.close();
			}
			log.setLogLevel(backupLogLevel);

		}

		return result;
	}

	private void executeShell(Result result, List<RowMetaAndData> cmdRows, String[] args)
	{
		LogWriter log = LogWriter.getInstance();
		FileObject fileObject = null;
		String realScript=null;
		FileObject tempFile=null;
		
		try
		{
			// What's the exact command?
			String base[] = null;
			List<String> cmds = new ArrayList<String>();

			if(log.isBasic()) log.logBasic(toString(), Messages.getString("JobShell.RunningOn",Const.getOS()));

			if(insertScript)
			{
				realScript=environmentSubstitute(script);
			}else
			{
				String realFilename = environmentSubstitute(getFilename());
				fileObject = KettleVFS.getFileObject(realFilename);	
			}

			if (Const.getOS().equals("Windows 95"))
			{
				base = new String[] { "command.com", "/C" };
			} else if (Const.getOS().startsWith("Windows"))
			{
				base = new String[] { "cmd.exe", "/C" };
			} else
			{
				if (!insertScript) {
					// Just set the command to the script we need to execute...
					//
					base = new String[] { KettleVFS.getFilename(fileObject) };
				}
				else {
					// Create a unique new temporary filename in the working directory, put the script in there
					// Set the permissions to execute and then run it...
					//
					try {
						tempFile = KettleVFS.createTempFile("kettle", "shell", workDirectory);
						tempFile.createFile();
						OutputStream outputStream = tempFile.getContent().getOutputStream();
						outputStream.write(realScript.getBytes());
						outputStream.close();
						String tempFilename =  KettleVFS.getFilename(tempFile);
						// Now we have to make this file executable...
						// On Unix-like systems this is done using the command "/bin/chmod +x filename"
						//
						ProcessBuilder procBuilder = new ProcessBuilder("chmod", "+x", tempFilename);
						Process proc = procBuilder.start();
						// Eat/log stderr/stdout all messages in a different thread...
						StreamLogger errorLogger = new StreamLogger(proc.getErrorStream(), toString() + " (stderr)");
						StreamLogger outputLogger = new StreamLogger(proc.getInputStream(), toString() + " (stdout)");
						new Thread(errorLogger).start();
						new Thread(outputLogger).start();
						proc.waitFor();

						// Now set this filename as the base command...
						//
						base = new String[] { tempFilename };
					}
					catch(Exception e) {
						throw new Exception("Unable to create temporary file to execute script", e);
					}
				}
			}

			// Construct the arguments...
			if (argFromPrevious && cmdRows != null)
			{
				// Add the base command...
				for (int i = 0; i < base.length; i++)
					cmds.add(base[i]);

				if (Const.getOS().equals("Windows 95") || Const.getOS().startsWith("Windows"))
				{
					// for windows all arguments including the command itself
					// need to be
					// included in 1 argument to cmd/command.

					StringBuffer cmdline = new StringBuffer(300);

					cmdline.append('"');
					if(insertScript)
						cmdline.append(realScript);
					else
						cmdline.append(optionallyQuoteField(KettleVFS.getFilename(fileObject), "\""));
					// Add the arguments from previous results...
					for (int i = 0; i < cmdRows.size(); i++) // Normally just
																// one row, but
																// once in a
																// while to
																// remain
																// compatible we
																// have
																// multiple.
					{
						RowMetaAndData r = (RowMetaAndData) cmdRows.get(i);
						for (int j = 0; j < r.size(); j++)
						{
							cmdline.append(' ');
							cmdline.append(optionallyQuoteField(r.getString(j, null), "\""));
						}
					}
					cmdline.append('"');
					cmds.add(cmdline.toString());
				} else
				{
					// Add the arguments from previous results...
					for (int i = 0; i < cmdRows.size(); i++) // Normally just
																// one row, but
																// once in a
																// while to
																// remain
																// compatible we
																// have
																// multiple.
					{
						RowMetaAndData r = (RowMetaAndData) cmdRows.get(i);
						for (int j = 0; j < r.size(); j++)
						{
							cmds.add(optionallyQuoteField(r.getString(j, null), "\""));
						}
					}
				}
			} else if (args != null)
			{
				// Add the base command...
				for (int i = 0; i < base.length; i++)
					cmds.add(base[i]);

				if (Const.getOS().equals("Windows 95") || Const.getOS().startsWith("Windows"))
				{
					// for windows all arguments including the command itself
					// need to be
					// included in 1 argument to cmd/command.

					StringBuffer cmdline = new StringBuffer(300);

					cmdline.append('"');
					if(insertScript)
						cmdline.append(realScript);
					else
						cmdline.append(optionallyQuoteField(KettleVFS.getFilename(fileObject), "\""));

					for (int i = 0; i < args.length; i++)
					{
						cmdline.append(' ');
						cmdline.append(optionallyQuoteField(args[i], "\""));
					}
					cmdline.append('"');
					cmds.add(cmdline.toString());
				} else
				{
					for (int i = 0; i < args.length; i++)
					{
						cmds.add(args[i]);
					}
				}
			}

			StringBuffer command = new StringBuffer();

			Iterator<String> it = cmds.iterator();
			boolean first = true;
			while (it.hasNext())
			{
				if (!first)
					command.append(' ');
				else
					first = false;
				command.append((String) it.next());
			}
			if(log.isBasic()) log.logBasic(toString(), Messages.getString("JobShell.ExecCommand",command.toString()));
			

			// Build the environment variable list...
			ProcessBuilder procBuilder = new ProcessBuilder(cmds);
			Map<String, String> env = procBuilder.environment();
			String[] variables = listVariables();
			for (int i = 0; i < variables.length; i++)
			{
				env.put(variables[i], getVariable(variables[i]));
			}

			if (getWorkDirectory() != null && !Const.isEmpty(Const.rtrim(getWorkDirectory())))
			{
				String vfsFilename = environmentSubstitute(getWorkDirectory());
				File file = new File(KettleVFS.getFilename(KettleVFS.getFileObject(vfsFilename)));
				procBuilder.directory(file);
			}
			Process proc = procBuilder.start();

			// any error message?
			StreamLogger errorLogger = new StreamLogger(proc.getErrorStream(), toString() + " (stderr)");

			// any output?
			StreamLogger outputLogger = new StreamLogger(proc.getInputStream(), toString() + " (stdout)");

			// kick them off
			new Thread(errorLogger).start();
			new Thread(outputLogger).start();

			proc.waitFor();
			if(log.isDetailed()) log.logDetailed(toString(), Messages.getString("JobShell.CommandFinished",command.toString()));
			
			
			

			// What's the exit status?
			result.setExitStatus(proc.exitValue());
			if (result.getExitStatus() != 0)
			{
				if(log.isDetailed()) 
					log.logDetailed(toString(), Messages.getString("JobShell.ExitStatus",environmentSubstitute(getFilename()),""+result.getExitStatus()));

				result.setNrErrors(1);
			}
			
			// close the streams
			// otherwise you get "Too many open files, java.io.IOException" after a lot of iterations
			proc.getErrorStream().close();
			proc.getOutputStream().close();
	
		} catch (IOException ioe)
		{
			log.logError(toString(), Messages.getString("JobShell.ErrorRunningShell",environmentSubstitute(getFilename()),ioe.toString()));
			result.setNrErrors(1);
		} catch (InterruptedException ie)
		{
			log.logError(toString(), Messages.getString("JobShell.Shellinterupted",environmentSubstitute(getFilename()),ie.toString()));
			result.setNrErrors(1);
		} catch (Exception e)
		{
			log.logError(toString(), Messages.getString("JobShell.UnexpectedError",environmentSubstitute(getFilename()),e.toString()));
			result.setNrErrors(1);
		}
		finally {
			// If we created a temporary file, remove it...
			//
			if (tempFile!=null) {
				try {
					tempFile.delete();
				}
				catch(Exception e) {
					Messages.getString("JobShell.UnexpectedError",tempFile.toString(),e.toString());
				}
			}
		}

		if (result.getNrErrors() > 0)
		{
			result.setResult(false);
		} else
		{
			result.setResult(true);
		}
	}

	private String optionallyQuoteField(String field, String quote)
	{
		if (Const.isEmpty(field))
			return "\"\"";

		// If the field already contains quotes, we don't touch it anymore, just
		// return the same string...
		// also return it if no spaces are found
		if (field.indexOf(quote) >= 0 || (field.indexOf(' ') < 0 && field.indexOf('=') < 0))
		{
			return field;
		} else
		{
			return quote + field + quote;
		}
	}

	public boolean evaluates()
	{
		return true;
	}

	public boolean isUnconditional()
	{
		return true;
	}

	public List<ResourceReference> getResourceDependencies(JobMeta jobMeta)
	{
		List<ResourceReference> references = super.getResourceDependencies(jobMeta);
		if (!Const.isEmpty(filename))
		{
			String realFileName = jobMeta.environmentSubstitute(filename);
			ResourceReference reference = new ResourceReference(this);
			reference.getEntries().add(new ResourceEntry(realFileName, ResourceType.FILE));
			references.add(reference);
		}
		return references;
	}

	@Override
	public void check(List<CheckResultInterface> remarks, JobMeta jobMeta)
	{
		ValidatorContext ctx = new ValidatorContext();
		putVariableSpace(ctx, getVariables());
		putValidators(ctx, notBlankValidator(), fileExistsValidator());

		andValidator().validate(this, "workDirectory", remarks, ctx); //$NON-NLS-1$
		andValidator().validate(this, "filename", remarks, putValidators(notBlankValidator())); //$NON-NLS-1$

		if (setLogfile)
		{
			andValidator().validate(this, "logfile", remarks, putValidators(notBlankValidator())); //$NON-NLS-1$
		}
	}

	protected String getLogfile()
	{
		return logfile;
	}
}