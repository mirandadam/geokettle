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

package org.pentaho.di.job.entries.abort;

import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.addOkRemark;

import java.util.List;

import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.CheckResultInterface;
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
 * Job entry type to abort a job.
 *
 * @author Samatar
 * @since 12-02-2007
 */
public class JobEntryAbort extends JobEntryBase implements Cloneable, JobEntryInterface {
  private String messageAbort;

  public JobEntryAbort(String n, String scr) {
    super(n, ""); //$NON-NLS-1$
    messageAbort = null;
    setJobEntryType(JobEntryType.ABORT);
  }

  public JobEntryAbort() {
    this("", ""); //$NON-NLS-1$//$NON-NLS-2$
  }

  public JobEntryAbort(JobEntryBase jeb) {
    super(jeb);
  }

  public Object clone() {
    JobEntryAbort je = (JobEntryAbort) super.clone();
    return je;
  }

  public String getXML() {
    StringBuffer retval = new StringBuffer();

    retval.append(super.getXML());
    retval.append("      ").append(XMLHandler.addTagValue("message", messageAbort)); //$NON-NLS-1$//$NON-NLS-2$

    return retval.toString();
  }

  public void loadXML(Node entrynode, List<DatabaseMeta> databases, List<SlaveServer> slaveServers, Repository rep) throws KettleXMLException {
    try {
      super.loadXML(entrynode, databases, slaveServers);
      messageAbort = XMLHandler.getTagValue(entrynode, "message"); //$NON-NLS-1$
    } catch (Exception e) {
      throw new KettleXMLException(Messages.getString("JobEntryAbort.UnableToLoadFromXml.Label"), e); //$NON-NLS-1$
    }
  }

  public void loadRep(Repository rep, long id_jobentry, List<DatabaseMeta> databases, List<SlaveServer> slaveServers) throws KettleException {
    try {
      super.loadRep(rep, id_jobentry, databases, slaveServers);
      messageAbort = rep.getJobEntryAttributeString(id_jobentry, "message"); //$NON-NLS-1$
    } catch (KettleDatabaseException dbe) {
      throw new KettleException(Messages.getString("JobEntryAbort.UnableToLoadFromRepo.Label", String.valueOf(id_jobentry)), dbe); //$NON-NLS-1$
    }
  }

  // Save the attributes of this job entry
  //
  public void saveRep(Repository rep, long id_job) throws KettleException {
    try {
      super.saveRep(rep, id_job);
      rep.saveJobEntryAttribute(id_job, getID(), "message", messageAbort); //$NON-NLS-1$

    } catch (KettleDatabaseException dbe) {
      throw new KettleException(
          Messages.getString("JobEntryAbort.UnableToSaveToRepo.Label", String.valueOf(id_job)), dbe); //$NON-NLS-1$
    }
  }

  public boolean evaluate(Result result) {
    LogWriter log = LogWriter.getInstance();
    String Returnmessage = null;
    String RealMessageabort = environmentSubstitute(getMessageabort());

    try {
      // Return False
      if (RealMessageabort == null) {
        Returnmessage = Messages.getString("JobEntryAbort.Meta.CheckResult.Label"); //$NON-NLS-1$
      } else {
        Returnmessage = RealMessageabort;

      }
      log.logError(toString(), Returnmessage);
      result.setNrErrors(1);
      return false;
    } catch (Exception e) {
      result.setNrErrors(1);
      log.logError(toString(), Messages.getString("JobEntryAbort.Meta.CheckResult.CouldNotExecute") + e.toString()); //$NON-NLS-1$
      return false;
    }
  }

  /**
   * Execute this job entry and return the result.
   * In this case it means, just set the result boolean in the Result class.
   * @param previousResult The result of the previous execution
   * @return The Result of the execution.
   */
  public Result execute(Result previousResult, int nr, Repository rep, Job parentJob) {
    previousResult.setResult(evaluate(previousResult));
    // we fail so stop 
    // job execution
    parentJob.stopAll();
    return previousResult;
  }

  public boolean resetErrorsBeforeExecution() {
    // we should be able to evaluate the errors in
    // the previous jobentry.
    return false;
  }

  public boolean evaluates() {
    return true;
  }

  public boolean isUnconditional() {
    return false;
  }

  public void setMessageabort(String messageabort) {
    this.messageAbort = messageabort;
  }

  public String getMessageabort() {
    return messageAbort;
  }

  public void check(List<CheckResultInterface> remarks, JobMeta jobMeta) {
    addOkRemark(this, "messageabort", remarks); //$NON-NLS-1$
  }
}