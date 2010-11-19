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

package org.pentaho.di.job.entries.ping;

import static org.pentaho.di.job.entry.validator.AndValidator.putValidators;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.andValidator;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.notBlankValidator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.net.InetAddress;

import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Result;
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
import org.pentaho.di.repository.Repository;
import org.pentaho.di.resource.ResourceEntry;
import org.pentaho.di.resource.ResourceReference;
import org.pentaho.di.resource.ResourceEntry.ResourceType;
import org.w3c.dom.Node;


/**
 * This defines a ping job entry.
 *
 * @author Samatar Hassan
 * @since Mar-2007
 *
 */
public class JobEntryPing extends JobEntryBase implements Cloneable, JobEntryInterface
{
	private String hostname;
	private String timeout;
	public String defaultTimeOut="3000";
	private String nbrPackets;
	private String Windows_CHAR="-n";
	private String NIX_CHAR="-c";
	
 	public String classicPing="classicPing";
 	public int iclassicPing=0;
 	public String systemPing="systemPing";
	public int isystemPing=1;
 	public String bothPings="bothPings";
	public int ibothPings=2;
	
	public String pingtype;
	public int ipingtype;

	public JobEntryPing(String n)
	{
		super(n, "");
		pingtype=classicPing;
		hostname=null;
		nbrPackets="2";
		timeout=defaultTimeOut;
		setID(-1L);
		setJobEntryType(JobEntryType.PING);
	}

	public JobEntryPing()
	{
		this("");
	}

	public JobEntryPing(JobEntryBase jeb)
	{
		super(jeb);
	}

    public Object clone()
    {
        JobEntryPing je = (JobEntryPing) super.clone();
        return je;
    }

	public String getXML()
	{
        StringBuffer retval = new StringBuffer(100);

		retval.append(super.getXML());
		retval.append("      ").append(XMLHandler.addTagValue("hostname",    hostname));
		retval.append("      ").append(XMLHandler.addTagValue("nbr_packets", nbrPackets));

		// TODO: The following line may be removed 3 versions after 2.5.0
		retval.append("      ").append(XMLHandler.addTagValue("nbrpaquets",  nbrPackets));
		retval.append("      ").append(XMLHandler.addTagValue("timeout",   timeout));
		
		retval.append("      ").append(XMLHandler.addTagValue("pingtype",   pingtype));

		return retval.toString();
	}

	public void loadXML(Node entrynode, List<DatabaseMeta> databases, List<SlaveServer> slaveServers, Repository rep)
		throws KettleXMLException
	{
		try
		{
			String nbrPaquets;
			super.loadXML(entrynode, databases, slaveServers);
			hostname   = XMLHandler.getTagValue(entrynode, "hostname");
			nbrPackets = XMLHandler.getTagValue(entrynode, "nbr_packets");

			// TODO: The following lines may be removed 3 versions after 2.5.0
			nbrPaquets = XMLHandler.getTagValue(entrynode, "nbrpaquets");
			if ( nbrPackets == null && nbrPaquets != null )
			{
				// if only nbrpaquets exists this means that the file was
				// save by a version 2.5.0 ping job entry
				nbrPackets = nbrPaquets;
			}
			timeout     = XMLHandler.getTagValue(entrynode, "timeout");
			pingtype     = XMLHandler.getTagValue(entrynode, "pingtype");
			if(Const.isEmpty(pingtype))
			{
				pingtype=classicPing;
				ipingtype=iclassicPing;
			}else
			{
				if(pingtype.equals(systemPing))
					ipingtype=isystemPing;
				else if(pingtype.equals(bothPings))
					ipingtype=ibothPings;
				else
					ipingtype=iclassicPing;
				
			}
		}
		catch(KettleXMLException xe)
		{
			throw new KettleXMLException("Unable to load job entry of type 'ping' from XML node", xe);
		}
	}

	public void loadRep(Repository rep, long id_jobentry, List<DatabaseMeta> databases, List<SlaveServer> slaveServers)
		throws KettleException
	{
		try
		{
			String nbrPaquets;
			super.loadRep(rep, id_jobentry, databases, slaveServers);
			hostname   = rep.getJobEntryAttributeString(id_jobentry, "hostname");
			nbrPackets = rep.getJobEntryAttributeString(id_jobentry, "nbr_packets");

			// TODO: The following lines may be removed 3 versions after 2.5.0
			nbrPaquets = rep.getJobEntryAttributeString(id_jobentry, "nbrpaquets");
			if ( nbrPackets == null && nbrPaquets != null )
			{
				// if only nbrpaquets exists this means that the file was
				// save by a version 2.5.0 ping job entry
				nbrPackets = nbrPaquets;
			}
			timeout = rep.getJobEntryAttributeString(id_jobentry, "timeout");
			
			pingtype  = rep.getJobEntryAttributeString(id_jobentry, "pingtype");
			if(Const.isEmpty(pingtype))
			{
				pingtype=classicPing;
				ipingtype=iclassicPing;
			}else{
				if(pingtype.equals(systemPing))
					ipingtype=isystemPing;
				else if(pingtype.equals(bothPings))
					ipingtype=ibothPings;
				else
					ipingtype=iclassicPing;
			}
		}
		catch(KettleException dbe)
		{
			throw new KettleException("Unable to load job entry of type 'ping' exists from the repository for id_jobentry="+id_jobentry, dbe);
		}
	}

	public void saveRep(Repository rep, long id_job)
		throws KettleException
	{
		try
		{
			super.saveRep(rep, id_job);

			rep.saveJobEntryAttribute(id_job, getID(), "hostname",    hostname);
			rep.saveJobEntryAttribute(id_job, getID(), "nbr_packets", nbrPackets);
			// TODO: The following line may be removed 3 versions after 2.5.0
			rep.saveJobEntryAttribute(id_job, getID(), "nbrpaquets",  nbrPackets);
			rep.saveJobEntryAttribute(id_job, getID(), "timeout",      timeout);
			rep.saveJobEntryAttribute(id_job, getID(), "pingtype",      pingtype);
		}
		catch(KettleDatabaseException dbe)
		{
			throw new KettleException("Unable to save job entry of type 'ping' to the repository for id_job="+id_job, dbe);
		}
	}
	public String getNbrPackets()
	{
		return nbrPackets;
	}

	public String getRealNbrPackets()
	{
		return environmentSubstitute(getNbrPackets());
	}

	public void setNbrPackets(String nbrPackets)
	{
		this.nbrPackets = nbrPackets;
	}
	public void setHostname(String hostname)
	{
		this.hostname = hostname;
	}

	public String getHostname()
	{
		return hostname;
	}

    public String getRealHostname()
    {
        return environmentSubstitute(getHostname());
    }

	public String getTimeOut()
	{
		return timeout;
	}

	public String getRealTimeOut()
	{
		return environmentSubstitute(getTimeOut());
	}

	public void setTimeOut(String timeout)
	{
		this.timeout = timeout;
	}

	public Result execute(Result previousResult, int nr, Repository rep, Job parentJob)
    {
        LogWriter log = LogWriter.getInstance();
        Result result = previousResult;
        
        result.setNrErrors(1);
        result.setResult(false);

        String hostname = getRealHostname();
    	int timeoutInt = Const.toInt(getRealTimeOut(), 300);
    	int packets = Const.toInt(getRealNbrPackets(), 2);
        boolean status =false;
        
        if (Const.isEmpty(hostname))
        {
            // No Host was specified
            log.logError(toString(), Messages.getString("JobPing.SpecifyHost.Label"));
            return result;
        }

        try
        {
        	if(ipingtype==isystemPing || ipingtype==ibothPings)
        	{
        		// Perform a system (Java) ping ...
	        	status=systemPing(hostname, timeoutInt,log);
	        	if(status)
	        	{
	                if(log.isDetailed())
	                	log.logDetailed(Messages.getString("JobPing.SystemPing"), Messages.getString("JobPing.OK.Label",hostname));
	        	}else
	        		log.logError(Messages.getString("JobPing.SystemPing"),Messages.getString("JobPing.NOK.Label",hostname));
        	}
        	if((ipingtype==iclassicPing) || (ipingtype==ibothPings && !status))
        	{
        		// Perform a classic ping ..
        		status=classicPing(hostname, packets,log);
        		if(status)
        		{
                    if(log.isDetailed())
                    	log.logDetailed(Messages.getString("JobPing.ClassicPing"), Messages.getString("JobPing.OK.Label",hostname));
        		}else
        			log.logError(Messages.getString("JobPing.ClassicPing"),Messages.getString("JobPing.NOK.Label",hostname));
        	}	
        }

        catch (Exception ex)
        {
            log.logError(toString(), Messages.getString("JobPing.Error.Label") + ex.getMessage());
        }
    	if (status)
        {
        	if(log.isDetailed())
        		log.logDetailed(toString(), Messages.getString("JobPing.OK.Label",hostname));
            result.setNrErrors(0);
            result.setResult(true);
        }else
        	log.logError(toString(), Messages.getString("JobPing.NOK.Label",hostname));
        return result;
    }

	public boolean evaluates()
	{
		return true;
	}
	private boolean systemPing(String hostname, int timeout,LogWriter log)
	{
		boolean retval=false;
		
		InetAddress address=null;
    	try{
    		address=InetAddress.getByName(hostname);
	    	if(address==null)
	    	{
	    		log.logError(toString(),Messages.getString("JobPing.CanNotGetAddress",hostname));
	    		return retval;
	    	}
    	
	        if(log.isDetailed()) 
	        {
	        	log.logDetailed(toString(),Messages.getString("JobPing.HostName",address.getHostName()));
	        	log.logDetailed(toString(),Messages.getString("JobPing.HostAddress",address.getHostAddress()));
	        }
        	
	        retval = address.isReachable(timeout);
	    	}catch(Exception e)
	    	{
	    		log.logError(toString(),Messages.getString("JobPing.ErrorSystemPing",hostname,e.getMessage()));
	    	}
			return retval;
	}
	private boolean classicPing(String hostname, int nrpackets,LogWriter log)
	{
		boolean retval=false;
		  try
          {
              String lignePing = "";
              String CmdPing="ping " ;
              if(Const.isWindows())
            	  CmdPing+= hostname + " " + Windows_CHAR + " " + nrpackets;
              else
            	  CmdPing+= hostname + " " + NIX_CHAR + " " + nrpackets;
              
        	  if(log.isDetailed()) 
        	  {
        		  log.logDetailed(toString(), Messages.getString("JobPing.NbrPackets.Label", ""+nrpackets));
        		  log.logDetailed(toString(), Messages.getString("JobPing.ExecClassicPing.Label", CmdPing));
        	  }
        	  Process processPing = Runtime.getRuntime().exec(CmdPing);
        	  if(log.isDetailed()) log.logDetailed(toString(), Messages.getString("JobPing.Gettingresponse.Label",hostname));
        	  // Get ping response
              BufferedReader br = new BufferedReader(new InputStreamReader(processPing.getInputStream()));

              // Read response lines
              while ((lignePing = br.readLine()) != null)
              {
                  if(log.isDetailed()) log.logDetailed(toString(), lignePing);
              }
              // We succeed only when 0% lost of data
              if (processPing.exitValue()==0)
              {
                  retval=true;
              }
          }

          catch (IOException ex)
          {
              log.logError(toString(), Messages.getString("JobPing.Error.Label") + ex.getMessage());
          }
          return retval;
	}
  public List<ResourceReference> getResourceDependencies(JobMeta jobMeta) {
    List<ResourceReference> references = super.getResourceDependencies(jobMeta);
    if (!Const.isEmpty(hostname)) {
      String realServername = jobMeta.environmentSubstitute(hostname);
      ResourceReference reference = new ResourceReference(this);
      reference.getEntries().add( new ResourceEntry(realServername, ResourceType.SERVER));
      references.add(reference);
    }
    return references;
  }

  @Override
  public void check(List<CheckResultInterface> remarks, JobMeta jobMeta)
  {
    andValidator().validate(this, "hostname", remarks, putValidators(notBlankValidator())); //$NON-NLS-1$
  }



}