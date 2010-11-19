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

package org.pentaho.di.job.entries.delay;

import static org.pentaho.di.job.entry.validator.AndValidator.putValidators;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.andValidator;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.integerValidator;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.longValidator;

import java.util.List;

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
import org.w3c.dom.Node;

/**
 * Job entry type to sleep for a time. It uses a piece of javascript to do this.
 *
 * @author Samatar
 * @since 21-02-2007
 */
public class JobEntryDelay extends JobEntryBase implements Cloneable, JobEntryInterface
{
  static private String DEFAULT_MAXIMUM_TIMEOUT = "0"; //$NON-NLS-1$

  private String maximumTimeout; // maximum timeout in seconds

  public int scaleTime;

  public JobEntryDelay(String n)
  {
    super(n, ""); //$NON-NLS-1$
    setID(-1L);
    setJobEntryType(JobEntryType.DELAY);
  }

  public JobEntryDelay()
  {
    this(""); //$NON-NLS-1$
  }

  public JobEntryDelay(JobEntryBase jeb)
  {
    super(jeb);
  }

  public Object clone()
  {
    JobEntryDelay je = (JobEntryDelay) super.clone();
    return je;
  }

  public String getXML()
  {
    StringBuffer retval = new StringBuffer(200);

    retval.append(super.getXML());
    retval.append("      ").append(XMLHandler.addTagValue("maximumTimeout", maximumTimeout)); //$NON-NLS-1$//$NON-NLS-2$
    retval.append("      ").append(XMLHandler.addTagValue("scaletime", scaleTime)); //$NON-NLS-1$ //$NON-NLS-2$

    return retval.toString();
  }

  public void loadXML(Node entrynode, List<DatabaseMeta> databases, List<SlaveServer> slaveServers, Repository rep) throws KettleXMLException
  {
    try
    {
      super.loadXML(entrynode, databases, slaveServers);
      maximumTimeout = XMLHandler.getTagValue(entrynode, "maximumTimeout"); //$NON-NLS-1$
      scaleTime = Integer.parseInt(XMLHandler.getTagValue(entrynode, "scaletime")); //$NON-NLS-1$
    } catch (Exception e)
    {
      throw new KettleXMLException(Messages.getString("JobEntryDelay.UnableToLoadFromXml.Label"), e); //$NON-NLS-1$
    }
  }

  public void loadRep(Repository rep, long id_jobentry, List<DatabaseMeta> databases, List<SlaveServer> slaveServers) throws KettleException
  {
    try
    {
      super.loadRep(rep, id_jobentry, databases, slaveServers);

      maximumTimeout = rep.getJobEntryAttributeString(id_jobentry, "maximumTimeout"); //$NON-NLS-1$
      scaleTime = (int) rep.getJobEntryAttributeInteger(id_jobentry, "scaletime"); //$NON-NLS-1$
    } catch (KettleDatabaseException dbe)
    {
      throw new KettleException(Messages.getString("JobEntryDelay.UnableToLoadFromRepo.Label") //$NON-NLS-1$
          + id_jobentry, dbe);
    }
  }

  //
  // Save the attributes of this job entry
  //
  public void saveRep(Repository rep, long id_job) throws KettleException
  {
    try
    {
      super.saveRep(rep, id_job);

      rep.saveJobEntryAttribute(id_job, getID(), "maximumTimeout", maximumTimeout); //$NON-NLS-1$
      rep.saveJobEntryAttribute(id_job, getID(), "scaletime", scaleTime); //$NON-NLS-1$
    } catch (KettleDatabaseException dbe)
    {
      throw new KettleException(Messages.getString("JobEntryDelay.UnableToSaveToRepo.Label") + id_job, dbe); //$NON-NLS-1$
    }
  }

  /**
   * Execute this job entry and return the result.
   * In this case it means, just set the result boolean in the Result class.
   * @param previousResult The result of the previous execution
   * @return The Result of the execution.
   */
  public Result execute(Result previousResult, int nr, Repository rep, Job parentJob)
  {
    LogWriter log = LogWriter.getInstance();
    Result result = previousResult;
    result.setResult(false);
    int Multiple;
    String Waitscale;

    // Scale time
    if (scaleTime == 0)
    {
      // Second
      Multiple = 1000;
      Waitscale = Messages.getString("JobEntryDelay.SScaleTime.Label"); //$NON-NLS-1$

    } else if (scaleTime == 1)
    {
      // Minute
      Multiple = 60000;
      Waitscale = Messages.getString("JobEntryDelay.MnScaleTime.Label"); //$NON-NLS-1$
    } else
    {
      // Hour
      Multiple = 3600000;
      Waitscale = Messages.getString("JobEntryDelay.HrScaleTime.Label"); //$NON-NLS-1$
    }
    try
    {
      // starttime (in seconds ,Minutes or Hours)
      long timeStart = System.currentTimeMillis() / Multiple;

      long iMaximumTimeout = Const.toInt(getrealMaximumTimeout(), Const.toInt(DEFAULT_MAXIMUM_TIMEOUT, 0));

      if (log.isDetailed())
      {
        log.logDetailed(toString(), Messages.getString("JobEntryDelay.LetsWaitFor.Label", String //$NON-NLS-1$
            .valueOf(iMaximumTimeout), String.valueOf(Waitscale)));
      }

      boolean continueLoop = true;
      //
      // Sanity check on some values, and complain on insanity
      //
      if (iMaximumTimeout < 0)
      {
        iMaximumTimeout = Const.toInt(DEFAULT_MAXIMUM_TIMEOUT, 0);
        log.logBasic(toString(), Messages.getString("JobEntryDelay.MaximumTimeReset.Label", String.valueOf(iMaximumTimeout), String.valueOf(Waitscale))); //$NON-NLS-1$
      }

      // Loop until the delay time has expired.
      //
      while (continueLoop && !parentJob.isStopped())
      {
        // Update Time value
        long now = System.currentTimeMillis() / Multiple;

        // Let's check the limit time
        if ((iMaximumTimeout > 0) && (now >= (timeStart + iMaximumTimeout)))
        {
          // We have reached the time limit
          if (log.isDetailed())
          {
            log.logDetailed(toString(), Messages.getString("JobEntryDelay.WaitTimeIsElapsed.Label")); //$NON-NLS-1$
          }
          continueLoop = false;
          result.setResult(true);
        }
        else
        {
			Thread.sleep(100);
        }
      }
    } 
    catch (Exception e)
    {
      // We get an exception
      result.setResult(false);
      log.logError(toString(), "Error  : " + e.getMessage()); //$NON-NLS-1$
    }

    return result;
  }

  public boolean resetErrorsBeforeExecution()
  {
    // we should be able to evaluate the errors in
    // the previous jobentry.
    return false;
  }

  public boolean evaluates()
  {
    return true;
  }

  public boolean isUnconditional()
  {
    return false;
  }

  public String getMaximumTimeout()
  {
    return maximumTimeout;
  }
  public String getrealMaximumTimeout()
  {
    return environmentSubstitute(getMaximumTimeout());
  }
  public void setMaximumTimeout(String s)
  {
    maximumTimeout = s;
  }

  public void check(List<CheckResultInterface> remarks, JobMeta jobMeta)
  {
    andValidator().validate(this, "maximumTimeout", remarks, putValidators(longValidator())); //$NON-NLS-1$
    andValidator().validate(this, "scaleTime", remarks, putValidators(integerValidator())); //$NON-NLS-1$
  }

  public int getScaleTime()
  {
    return scaleTime;
  }

  public void setScaleTime(int scaleTime)
  {
    this.scaleTime = scaleTime;
  }
}