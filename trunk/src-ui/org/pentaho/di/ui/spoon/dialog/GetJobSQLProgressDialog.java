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
/*
 *
 *
 */

package org.pentaho.di.ui.spoon.dialog;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.ProgressMonitorAdapter;
import org.pentaho.di.core.SQLStatement;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.ui.spoon.dialog.Messages;
import org.pentaho.di.ui.core.dialog.ErrorDialog;




/**
 * Takes care of displaying a dialog that will handle the wait while getting the SQL for a job...
 * 
 * @author Matt
 * @since  29-mrt-2006
 */
public class GetJobSQLProgressDialog
{
	private Shell shell;
	private JobMeta jobMeta;
	private List<SQLStatement> stats;
    private Repository repository;

	
	/**
	 * Creates a new dialog that will handle the wait while getting the SQL for a job...
	 */
	public GetJobSQLProgressDialog(Shell shell, JobMeta jobMeta, Repository repository)
	{
		this.shell = shell;
		this.jobMeta = jobMeta;
        this.repository = repository;

	}
	
	public List<SQLStatement> open()
	{
		IRunnableWithProgress op = new IRunnableWithProgress()
		{
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
			{
                // This is running in a new process: copy some KettleVariables info
                // LocalVariables.getInstance().createKettleVariables(Thread.currentThread(), kettleVariables.getLocalThread(), true);
                // --> don't set variables if not running in different thread --> pmd.run(true,true, op);

				try
				{
					stats = jobMeta.getSQLStatements(repository, new ProgressMonitorAdapter(monitor));
				}
				catch(KettleException e)
				{
					throw new InvocationTargetException(e, Messages.getString("GetJobSQLProgressDialog.RuntimeError.UnableToGenerateSQL.Exception", e.getMessage())); //Error generating SQL for job: \n{0}
				}
			}
		};
		
		try
		{
			ProgressMonitorDialog pmd = new ProgressMonitorDialog(shell);
			pmd.run(false, false, op);
		}
		catch (InvocationTargetException e)
		{
			new ErrorDialog(shell, Messages.getString("GetJobSQLProgressDialog.Dialog.UnableToGenerateSQL.Title"), Messages.getString("GetJobSQLProgressDialog.Dialog.UnableToGenerateSQL.Message"), e); //"Error generating SQL for job","An error occured generating the SQL for this job\!"
			stats = null;
		}
		catch (InterruptedException e)
		{
			new ErrorDialog(shell, Messages.getString("GetJobSQLProgressDialog.Dialog.UnableToGenerateSQL.Title"), Messages.getString("GetJobSQLProgressDialog.Dialog.UnableToGenerateSQL.Message"), e); //"Error generating SQL for job","An error occured generating the SQL for this job\!"
			stats = null;
		}

		return stats;
	}
}
