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

package org.pentaho.di.ui.core.database.dialog;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.ProgressMonitorAdapter;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.ui.core.database.dialog.Messages;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.ui.core.dialog.ErrorDialog;


/**
 * Takes care of displaying a dialog that will handle the wait while 
 * we're getting rows for a certain SQL query on a database.
 * 
 * @author Matt
 * @since  12-may-2005
 */
public class GetPreviewTableProgressDialog
{
	private Shell shell;
	private DatabaseMeta dbMeta;
	private String tableName;
	private int limit;
	private List<Object[]> rows;
    private RowMetaInterface rowMeta;
	
	private Database db;    

	/**
	 * Creates a new dialog that will handle the wait while we're doing the hard work.
	 */
	public GetPreviewTableProgressDialog(Shell shell, DatabaseMeta dbInfo, String tableName, int limit)
	{
		this.shell = shell;
		this.dbMeta = dbInfo;
		this.tableName = tableName;
		this.limit = limit;
    }
	
	public List<Object[]> open()
	{
		IRunnableWithProgress op = new IRunnableWithProgress()
		{
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
			{
				db = new Database(dbMeta);
				try 
				{
					db.connect();
					
					rows =  db.getFirstRows(tableName, limit, new ProgressMonitorAdapter(monitor));
                    rowMeta = db.getReturnRowMeta();
					
					if (monitor.isCanceled()) 
						throw new InvocationTargetException(new Exception("This operation was cancelled!"));
				}
				catch(KettleException e)
				{
					throw new InvocationTargetException(e, "Couldn't find any rows because of an error :"+e.toString());
				}
				finally
				{
					db.disconnect();
				}
			}
		};
		
		try
		{
			final ProgressMonitorDialog pmd = new ProgressMonitorDialog(shell);
			// Run something in the background to cancel active database queries, forecably if needed!
			Runnable run = new Runnable()
            {
                public void run()
                {
                    IProgressMonitor monitor = pmd.getProgressMonitor();
                    while (pmd.getShell()==null || ( !pmd.getShell().isDisposed() && !monitor.isCanceled() ))
                    {
                        try { Thread.sleep(100); } catch(InterruptedException e) { };
                    }
                    
                    if (monitor.isCanceled()) // Disconnect and see what happens!
                    {
                        try { db.cancelQuery(); } catch(Exception e) {};
                    }
                }
            };
            // Start the cancel tracker in the background!
            new Thread(run).start();
            
			pmd.run(true, true, op);
		}
		catch (InvocationTargetException e)
		{
		    showErrorDialog(e);
			return null;
		}
		catch (InterruptedException e)
		{
		    showErrorDialog(e);
			return null;
		}
		
		return rows;
	}

    /**
     * Showing an error dialog
     * 
     * @param e
    */
    private void showErrorDialog(Exception e)
    {
        new ErrorDialog(shell, Messages.getString("GetPreviewTableProgressDialog.Error.Title"),
            Messages.getString("GetPreviewTableProgressDialog.Error.Message"), e);
    }

    /**
     * @return the rowMeta
     */
    public RowMetaInterface getRowMeta()
    {
        return rowMeta;
    }
}