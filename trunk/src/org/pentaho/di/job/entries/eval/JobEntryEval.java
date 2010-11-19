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

package org.pentaho.di.job.entries.eval;

import static org.pentaho.di.job.entry.validator.AndValidator.putValidators;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.andValidator;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.notBlankValidator;

import java.util.List;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Scriptable;
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
 * Job entry type to evaluate the result of a previous job entry.
 * It uses a piece of javascript to do this.
 *
 * @author Matt
 * @since 5-11-2003
 */
public class JobEntryEval extends JobEntryBase implements Cloneable, JobEntryInterface {
  private String script;

  public JobEntryEval(String n, String scr) {
    super(n, ""); //$NON-NLS-1$
    script = scr;
    setID(-1L);
    setJobEntryType(JobEntryType.EVAL);
  }

  public JobEntryEval() {
    this("", ""); //$NON-NLS-1$ //$NON-NLS-2$
  }

  public JobEntryEval(JobEntryBase jeb) {
    super(jeb);
  }

  public Object clone() {
    JobEntryEval je = (JobEntryEval) super.clone();
    return je;
  }

  public String getXML() {
    StringBuffer retval = new StringBuffer();

    retval.append(super.getXML());
    retval.append("      ").append(XMLHandler.addTagValue("script", script)); //$NON-NLS-1$ //$NON-NLS-2$

    return retval.toString();
  }

  public void loadXML(Node entrynode, List<DatabaseMeta> databases, List<SlaveServer> slaveServers, Repository rep) throws KettleXMLException {
    try {
      super.loadXML(entrynode, databases, slaveServers);
      script = XMLHandler.getTagValue(entrynode, "script"); //$NON-NLS-1$
    } catch (Exception e) {
      throw new KettleXMLException(Messages.getString("JobEntryEval.UnableToLoadFromXml"), e); //$NON-NLS-1$
    }
  }

  public void loadRep(Repository rep, long id_jobentry, List<DatabaseMeta> databases, List<SlaveServer> slaveServers) throws KettleException {
    try {
      super.loadRep(rep, id_jobentry, databases, slaveServers);

      script = rep.getJobEntryAttributeString(id_jobentry, "script"); //$NON-NLS-1$
    } catch (KettleDatabaseException dbe) {
      throw new KettleException(
          Messages.getString("JobEntryEval.UnableToLoadFromRepo", String.valueOf(id_jobentry)), dbe); //$NON-NLS-1$
    }
  }

  // Save the attributes of this job entry
  //
  public void saveRep(Repository rep, long id_job) throws KettleException {
    try {
      super.saveRep(rep, id_job);

      rep.saveJobEntryAttribute(id_job, getID(), "script", script); //$NON-NLS-1$
    } catch (KettleDatabaseException dbe) {
      throw new KettleException(Messages.getString("JobEntryEval.UnableToSaveToRepo", String.valueOf(id_job)), //$NON-NLS-1$
          dbe);
    }
  }

  public void setScript(String s) {
    script = s;
  }

  public String getScript() {
    return script;
  }

  /**
   * Evaluate the result of the execution of previous job entry.
   * @param result The result to evaulate.
 * @param prev_result the previous result
 * @param parentJob the parent job
   * @return The boolean result of the evaluation script.
   */
  public boolean evaluate(Result result, Job parentJob, Result prev_result) {
    LogWriter log = LogWriter.getInstance();
    Context cx;
    Scriptable scope;

    cx = ContextFactory.getGlobal().enterContext();
	
    try {
      scope = cx.initStandardObjects(null);

      Long errors = new Long(result.getNrErrors());
      Long lines_input = new Long(result.getNrLinesInput());
      Long lines_output = new Long(result.getNrLinesOutput());
      Long lines_updated = new Long(result.getNrLinesUpdated());
      Long lines_rejected = new Long(result.getNrLinesRejected());
      Long lines_read = new Long(result.getNrLinesRead());
      Long lines_written = new Long(result.getNrLinesWritten());
      Long exit_status = new Long(result.getExitStatus());
      Long files_retrieved = new Long(result.getNrFilesRetrieved());
      Long nr = new Long(result.getEntryNr());

      scope.put("errors", scope, errors); //$NON-NLS-1$
      scope.put("lines_input", scope, lines_input); //$NON-NLS-1$
      scope.put("lines_output", scope, lines_output); //$NON-NLS-1$
      scope.put("lines_updated", scope, lines_updated); //$NON-NLS-1$
      scope.put("lines_rejected", scope, lines_rejected); //$NON-NLS-1$
      scope.put("lines_read", scope, lines_read); //$NON-NLS-1$
      scope.put("lines_written", scope, lines_written); //$NON-NLS-1$
      scope.put("files_retrieved", scope, files_retrieved); //$NON-NLS-1$
      scope.put("exit_status", scope, exit_status); //$NON-NLS-1$
      scope.put("nr", scope, nr); //$NON-NLS-1$
      scope.put("is_windows", scope, Boolean.valueOf(Const.isWindows())); //$NON-NLS-1$

      Object array[] = null;
      if (result.getRows() != null) {
        array = result.getRows().toArray();
      }

      scope.put("rows", scope, array); //$NON-NLS-1$
      scope.put("parent_job", scope, parentJob); //$NON-NLS-1$
      scope.put("previous_result", scope, prev_result); //$NON-NLS-1$

      try {
        Object res = cx.evaluateString(scope, this.script, "<cmd>", 1, null); //$NON-NLS-1$
        boolean retval = Context.toBoolean(res);
        // System.out.println(result.toString()+" + ["+this.script+"] --> "+retval);
        result.setNrErrors(0);

        return retval;
      } catch (Exception e) {
        result.setNrErrors(1);
        log.logError(toString(), Messages.getString("JobEntryEval.CouldNotCompile", e.toString())); //$NON-NLS-1$
        return false;
      }
    } catch (Exception e) {
      result.setNrErrors(1);
      log.logError(toString(), Messages.getString("JobEntryEval.ErrorEvaluating", e.toString())); //$NON-NLS-1$
      return false;
    } finally {
      Context.exit();
    }
  }

  /**
   * Execute this job entry and return the result.
   * In this case it means, just set the result boolean in the Result class.
   * @param prev_result The result of the previous execution
   * @return The Result of the execution.
   */
  public Result execute(Result prev_result, int nr, Repository rep, Job parentJob) {
    prev_result.setResult(evaluate(prev_result, parentJob, prev_result));

    return prev_result;
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

  public void check(List<CheckResultInterface> remarks, JobMeta jobMeta)
  {
    andValidator().validate(this, "script", remarks, putValidators(notBlankValidator())); //$NON-NLS-1$
  }

}