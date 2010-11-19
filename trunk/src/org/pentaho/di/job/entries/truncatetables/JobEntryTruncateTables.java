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
package org.pentaho.di.job.entries.truncatetables;

import static org.pentaho.di.job.entry.validator.AbstractFileValidator.putVariableSpace;
import static org.pentaho.di.job.entry.validator.AndValidator.putValidators;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.andValidator;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.fileExistsValidator;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.notNullValidator;

import java.util.List;
import org.w3c.dom.Node;

import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.logging.LogWriter;
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

/**
 * This defines a Truncate Tables job entry.
 * 
 * @author Samatar
 * @since 22-07-2008
 *
 */
public class JobEntryTruncateTables extends JobEntryBase implements Cloneable, JobEntryInterface
{
	public boolean argFromPrevious;
	  
	private DatabaseMeta connection;
	
	public String arguments[];

	public String schemaname[];
	
	private int NrErrors=0;
	private int NrSuccess=0;
	boolean continueProcess=true;

	public JobEntryTruncateTables(String n)
	{
	    super(n, "");
	    this.argFromPrevious=false;
	    this.arguments = null;
	    this.schemaname=null;
	    this.connection=null;
		setID(-1L);
		setJobEntryType(JobEntryType.TRUNCATE_TABLES);
	}

	public JobEntryTruncateTables()
	{
		this("");
	}

	public JobEntryTruncateTables(JobEntryBase jeb)
	{
		super(jeb);
	}
    
    public Object clone()
    {
        JobEntryTruncateTables je = (JobEntryTruncateTables) super.clone();
        return je;
    }

	public String getXML()
	{
        StringBuffer retval = new StringBuffer(200);
		
		retval.append(super.getXML());
		retval.append("      ").append(XMLHandler.addTagValue("connection", this.connection==null?null:this.connection.getName()));
		retval.append("      ").append(XMLHandler.addTagValue("arg_from_previous", this.argFromPrevious));
		retval.append("      <fields>").append(Const.CR); //$NON-NLS-1$
	    if (arguments != null) {
	      for (int i = 0; i < this.arguments.length; i++) {
	        retval.append("        <field>").append(Const.CR); //$NON-NLS-1$
	        retval.append("          ").append(XMLHandler.addTagValue("name", this.arguments[i])); //$NON-NLS-1$ //$NON-NLS-2$
	        retval.append("          ").append(XMLHandler.addTagValue("schemaname", this.schemaname[i])); //$NON-NLS-1$ //$NON-NLS-2$
	        retval.append("        </field>").append(Const.CR); //$NON-NLS-1$
	      }
	    }
	    retval.append("      </fields>").append(Const.CR); //$NON-NLS-1$
		return retval.toString();
	}
	
	public void loadXML(Node entrynode, List<DatabaseMeta>  databases, List<SlaveServer> slaveServers, Repository rep) throws KettleXMLException
	{
		try
		{
			super.loadXML(entrynode, databases, slaveServers);

			String dbname = XMLHandler.getTagValue(entrynode, "connection");
			this.connection    = DatabaseMeta.findDatabase(databases, dbname);
			this.argFromPrevious = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "arg_from_previous")); 
			  
		    Node fields = XMLHandler.getSubNode(entrynode, "fields"); //$NON-NLS-1$

		      // How many field arguments?
		      int nrFields = XMLHandler.countNodes(fields, "field"); //$NON-NLS-1$
		      this.arguments = new String[nrFields];
		      this.schemaname = new String[nrFields];

		      // Read them all...
		      for (int i = 0; i < nrFields; i++) {
		        Node fnode = XMLHandler.getSubNodeByNr(fields, "field", i); //$NON-NLS-1$
		        this.arguments[i] = XMLHandler.getTagValue(fnode, "name"); //$NON-NLS-1$
		        this.schemaname[i] = XMLHandler.getTagValue(fnode, "schemaname"); //$NON-NLS-1$
		      }
		}
		catch(KettleException e)
		{
			throw new KettleXMLException(Messages.getString("JobEntryTruncateTables.UnableLoadXML"),e);
		}
	}

	public void loadRep(Repository rep, long id_jobentry, List<DatabaseMeta> databases, List<SlaveServer> slaveServers)
	throws KettleException
	{
	try
	{
		super.loadRep(rep, id_jobentry, databases, slaveServers);

			long id_db = rep.getJobEntryAttributeInteger(id_jobentry, "id_database");
			if (id_db>0)
			{
				this.connection = DatabaseMeta.findDatabase(databases, id_db);
			}
			else
			{
				// This is were we end up in normally, the previous lines are for backward compatibility.
				this.connection = DatabaseMeta.findDatabase(databases, rep.getJobEntryAttributeString(id_jobentry, "connection"));
			}
			this.argFromPrevious = rep.getJobEntryAttributeBoolean(id_jobentry, "arg_from_previous");
			 // How many arguments?
		      int argnr = rep.countNrJobEntryAttributes(id_jobentry, "name"); //$NON-NLS-1$
		      this.arguments = new String[argnr];
		      this.schemaname = new String[argnr];

		      // Read them all...
		      for (int a = 0; a < argnr; a++) {
		    	  this.arguments[a] = rep.getJobEntryAttributeString(id_jobentry, a, "name"); //$NON-NLS-1$
		    	  this.schemaname[a] = rep.getJobEntryAttributeString(id_jobentry, a, "schemaname"); //$NON-NLS-1$
		      }

		}
		catch(KettleDatabaseException dbe)
		{
			throw new KettleException(Messages.getString("JobEntryTruncateTables.UnableLoadRep",""+id_jobentry), dbe);
		}
	}
	
	public void saveRep(Repository rep, long id_job)
		throws KettleException
	{
		try
		{
			super.saveRep(rep, id_job);
			
			if (this.connection!=null) rep.saveJobEntryAttribute(id_job, getID(), "connection", this.connection.getName());
			rep.saveJobEntryAttribute(id_job, getID(), "arg_from_previous", this.argFromPrevious);
		      // save the arguments...
		      if (this.arguments != null) {
		        for (int i = 0; i < this.arguments.length; i++) {
		          rep.saveJobEntryAttribute(id_job, getID(), i, "name", this.arguments[i]); //$NON-NLS-1$
		          rep.saveJobEntryAttribute(id_job, getID(), i, "schemaname", this.schemaname[i]); //$NON-NLS-1$
		        }
		      }
		}
		catch(KettleDatabaseException dbe)
		{
			throw new KettleException(Messages.getString("JobEntryTruncateTables.UnableSaveRep",""+id_job), dbe);
		}
	}
	
	public void setDatabase(DatabaseMeta database)
	{
		this.connection = database;
	}
	
	public DatabaseMeta getDatabase()
	{
		return this.connection;
	}
	
	public boolean evaluates()
	{
		return true;
	}

	public boolean isUnconditional()
	{
		return true;
	}
	private boolean TruncateTables(LogWriter log,String tablename, String schemaname, Database db)
	{
		boolean retval=false;
		String realSchemaname=schemaname;
		String realTablename=tablename;
		try{

			if(!Const.isEmpty(realSchemaname))
                	realTablename = db.getDatabaseMeta().getQuotedSchemaTableCombination(realSchemaname, realTablename);
                
			// check if table exists!
			if(db.checkTableExists(realTablename)){
				if(!Const.isEmpty(realSchemaname))
					db.truncateTable(realSchemaname, tablename);
				else
					db.truncateTable(tablename);
		
				if(log.isDetailed()) log.logDetailed(toString(), Messages.getString("JobEntryTruncateTables.Log.TableTruncated",realTablename));
				
				retval=true;
			}else{
				log.logError(toString(), Messages.getString("JobEntryTruncateTables.Error.CanNotFindTable",realTablename));
			}
		}catch (Exception e)
		{
			log.logError(toString(), Messages.getString("JobEntryTruncateTables.Error.CanNotTruncateTables",realTablename,e.toString()));
		}
		return retval;
	}

	public Result execute(Result previousResult, int nr, Repository rep, Job parentJob)
	{
		LogWriter log = LogWriter.getInstance();
		Result result = previousResult;
	    List<RowMetaAndData> rows = result.getRows();
	    RowMetaAndData resultRow = null;
		
		result.setResult(true);
		NrErrors=0;
		continueProcess=true;
		NrSuccess=0;
		
	    if (argFromPrevious) {
		      if(log.isDetailed()) 
		    	  log.logDetailed(toString(), Messages.getString("JobEntryTruncateTables.FoundPreviousRows", String.valueOf((rows != null ? rows.size() : 0)))); //$NON-NLS-1$
		      if(rows.size()==0) return result;
	    }
		if (connection!=null)
		{ 
			Database db = new Database(connection);
			db.shareVariablesWith(this);
			try
			{
				db.connect();
			    if (argFromPrevious && rows != null) // Copy the input row to the (command line) arguments
			    {

			      for (int iteration = 0; iteration < rows.size() && !parentJob.isStopped() && continueProcess; iteration++) {  
			    		resultRow = rows.get(iteration);

			    		// Get values from previous result 
			    		String tablename_previous = resultRow.getString(0, null);
			    		String schemaname_previous = resultRow.getString(1, null);
			        
				        if(!Const.isEmpty(tablename_previous))
				        {
				            if(log.isDetailed()) 
				          	  log.logDetailed(toString(), Messages.getString("JobEntryTruncateTables.ProcessingRow", tablename_previous, schemaname_previous)); //$NON-NLS-1$
				        }else{
				      	  log.logError(toString(), Messages.getString("JobEntryTruncateTables.RowEmpty")); //$NON-NLS-1$ 
				        }
			      }
			        
			      }else if (arguments!=null) {
	        		 for (int i = 0; i < arguments.length && !parentJob.isStopped() && continueProcess; i++) {
	                     String realTablename = environmentSubstitute(arguments[i]);                
	             		 String realSchemaname = environmentSubstitute(schemaname[i]); 
	        			 if(!Const.isEmpty(realTablename)) {
		        	    	  if(log.isDetailed()) 
		        	    		  log.logDetailed(toString(), Messages.getString("JobEntryTruncateTables.ProcessingArg", arguments[i], schemaname[i])); //$NON-NLS-1$
		        			
		        	    	  // let's truncate table
		        	    	  if(TruncateTables(log, realTablename, realSchemaname, db)) 
		        	    		  updateSuccess();
		        	    	  else
		        	    		  updateErrors();
	        			 }else{
	        				  log.logError(toString(), Messages.getString("JobEntryTruncateTables.ArgEmpty", arguments[i], schemaname[i])); //$NON-NLS-1$ 
	        			 }
	        	      }	
			      }
			}
			catch(Exception dbe){
				result.setNrErrors(1);
				log.logError(toString(), Messages.getString("JobEntryTruncateTables.Error.RunningEntry",dbe.getMessage()));
			}finally{
				if(db!=null) db.disconnect();
			}
		}
		else {
			result.setNrErrors(1);
			log.logError(toString(),Messages.getString("JobEntryTruncateTables.NoDbConnection"));
		}
		
		result.setNrErrors(NrErrors);
		result.setResult(NrErrors==0);
		return result;
	}
	private void updateErrors()
	{
		NrErrors++;
		continueProcess=false;
	}
	private void updateSuccess()
	{
		NrSuccess++;
	}
    
    public DatabaseMeta[] getUsedDatabaseConnections()
    {
        return new DatabaseMeta[] { connection, };
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

}