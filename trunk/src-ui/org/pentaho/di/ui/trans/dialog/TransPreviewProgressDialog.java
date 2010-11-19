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

package org.pentaho.di.ui.trans.dialog;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.Log4jStringAppender;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.debug.BreakPointListener;
import org.pentaho.di.trans.debug.StepDebugMeta;
import org.pentaho.di.trans.debug.TransDebugMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.ui.core.dialog.ErrorDialog;


/**
 * Takes care of displaying a dialog that will handle the wait while previewing a transformation...
 * 
 * @author Matt
 * @since  13-jan-2006
 */
public class TransPreviewProgressDialog
{
    private Shell shell;
    private TransMeta transMeta;
    private String[] previewStepNames;
    private int[] previewSize;
    private Trans trans;
    
    private boolean cancelled;
    private String loggingText;
	private TransDebugMeta transDebugMeta;
    
    /**
     * Creates a new dialog that will handle the wait while previewing a transformation...
     */
    public TransPreviewProgressDialog(Shell shell, TransMeta transMeta, String previewStepNames[], int previewSize[])
    {
        this.shell = shell;
        this.transMeta = transMeta;
        this.previewStepNames = previewStepNames;
        this.previewSize = previewSize;
        
        cancelled = false;
    }
    
    public TransMeta open()
    {
        IRunnableWithProgress op = new IRunnableWithProgress()
        {
            public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
            {
                doPreview(monitor);
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
                        try { trans.stopAll(); } catch(Exception e) {};
                    }
                }
            };
            
            // Start the cancel tracker in the background!
            new Thread(run).start();
            
            pmd.run(true, true, op);
        }
        catch (InvocationTargetException e)
        {
            new ErrorDialog(shell, Messages.getString("TransPreviewProgressDialog.ErrorLoadingTransformation.DialogTitle"), Messages.getString("TransPreviewProgressDialog.ErrorLoadingTransformation.DialogMessage"), e); //$NON-NLS-1$ //$NON-NLS-2$
            transMeta = null;
        }
        catch (InterruptedException e)
        {
            new ErrorDialog(shell, Messages.getString("TransPreviewProgressDialog.ErrorLoadingTransformation.DialogTitle"), Messages.getString("TransPreviewProgressDialog.ErrorLoadingTransformation.DialogMessage"), e); //$NON-NLS-1$ //$NON-NLS-2$
            transMeta = null;
        }

        return transMeta;
    }
    
    private void doPreview(final IProgressMonitor progressMonitor)
    {
        LogWriter log = LogWriter.getInstance();
        
        progressMonitor.beginTask(Messages.getString("TransPreviewProgressDialog.Monitor.BeginTask.Title"), 100); //$NON-NLS-1$
        
        // Log preview activity to a String:
        Log4jStringAppender stringAppender = LogWriter.createStringAppender();
        log.addAppender(stringAppender);
        
        // This transformation is ready to run in preview!
        trans = new Trans(transMeta);
        
        // Prepare the execution...
        //
        try {
			trans.prepareExecution(null);
		} catch (final KettleException e) {
			shell.getDisplay().asyncExec(new Runnable() {
				public void run() {
					new ErrorDialog(shell, Messages.getString("System.Dialog.Error.Title"), Messages
							.getString("TransPreviewProgressDialog.Exception.ErrorPreparingTransformation"), e);
				}
			});
			
			
			// It makes no sense to continue, so just stop running...
			//
			return;
		}
        
        // Add the preview / debugging information...
        //
        transDebugMeta = new TransDebugMeta(transMeta);
        for (int i=0;i<previewStepNames.length;i++) {
        	StepMeta stepMeta = transMeta.findStep(previewStepNames[i]);
        	StepDebugMeta stepDebugMeta = new StepDebugMeta(stepMeta);
        	stepDebugMeta.setReadingFirstRows(true);
        	stepDebugMeta.setRowCount(previewSize[i]);
        	transDebugMeta.getStepDebugMetaMap().put(stepMeta, stepDebugMeta);
        }
        
        // set the appropriate listeners on the transformation...
        //
        transDebugMeta.addRowListenersToTransformation(trans);
        
        // Fire off the step threads... start running!
        //
        try {
            trans.startThreads();
		} catch (final KettleException e) {
			shell.getDisplay().asyncExec(new Runnable() {
				public void run() {
					new ErrorDialog(shell, Messages.getString("System.Dialog.Error.Title"), Messages
							.getString("TransPreviewProgressDialog.Exception.ErrorPreparingTransformation"), e);
				}
			});
			
			// It makes no sense to continue, so just stop running...
			//
			return;
		}
        
        int previousPct = 0;
        final List<String> previewComplete = new ArrayList<String>();
        
        while (previewComplete.size()<previewStepNames.length && !trans.isFinished() && !progressMonitor.isCanceled())
        {
			// We add a break-point that is called every time we have a step with a full preview row buffer
        	// That makes it easy and fast to see if we have all the rows we need
        	//
			transDebugMeta.addBreakPointListers(new BreakPointListener() {
					public void breakPointHit(TransDebugMeta transDebugMeta, StepDebugMeta stepDebugMeta, RowMetaInterface rowBufferMeta, List<Object[]> rowBuffer) {
						String stepName =  stepDebugMeta.getStepMeta().getName();
						previewComplete.add(stepName);
						progressMonitor.subTask( Messages.getString("TransPreviewProgressDialog.SubTask.StepPreviewFinished", stepName) );
					}
				}
			);
			
            // How many rows are done?
            int nrDone = 0;
            int nrTotal = 0;
            for (StepDebugMeta stepDebugMeta : transDebugMeta.getStepDebugMetaMap().values()) {
            	nrDone+=stepDebugMeta.getRowBuffer().size();
            	nrTotal+=stepDebugMeta.getRowCount();
            }
            
            int pct = 100*nrDone/nrTotal;
            
            int worked = pct - previousPct;
            
            if (worked>0) progressMonitor.worked(worked);
            previousPct = pct;
            
            // Change the percentage...
            try { Thread.sleep(500); } catch(InterruptedException e) {}
            
            if (progressMonitor.isCanceled())
            {
                cancelled=true;
                trans.stopAll();
            }
        }
        
        trans.stopAll();
        
        // Log preview activity to a String:
        log.removeAppender(stringAppender);
        loggingText = stringAppender.getBuffer().toString();
        
        progressMonitor.done();
    }
    
    /**
     * @param stepname the name of the step to get the preview rows for
     * @return A list of rows as the result of the preview run.
     */
    public List<Object[]> getPreviewRows(String stepname)
    {
    	if (transDebugMeta==null) return null;
    	
    	for (StepMeta stepMeta : transDebugMeta.getStepDebugMetaMap().keySet()) {
    		if (stepMeta.getName().equals(stepname)) {
    			StepDebugMeta stepDebugMeta = transDebugMeta.getStepDebugMetaMap().get(stepMeta);
    			return stepDebugMeta.getRowBuffer();
    		}
    	}
        return null;
    }
    
    /**
     * @param stepname the name of the step to get the preview rows for
     * @return A description of the row (metadata)
     */
    public RowMetaInterface getPreviewRowsMeta(String stepname)
    {
    	if (transDebugMeta==null) return null;
    	
    	for (StepMeta stepMeta : transDebugMeta.getStepDebugMetaMap().keySet()) {
    		if (stepMeta.getName().equals(stepname)) {
    			StepDebugMeta stepDebugMeta = transDebugMeta.getStepDebugMetaMap().get(stepMeta);
    			return stepDebugMeta.getRowBufferMeta();
    		}
    	}
        return null;
    }

    /**
     * @return true is the preview was canceled by the user
     */
    public boolean isCancelled()
    {
        return cancelled;
    }
    
    /**
     * @return The logging text from the latest preview run
     */
    public String getLoggingText()
    {
        return loggingText;
    }
    
    /**
     * 
     * @return The transformation object that executed the preview TransMeta
     */
    public Trans getTrans()
    {
       return trans; 
    }

	/**
	 * @return the transDebugMeta
	 */
	public TransDebugMeta getTransDebugMeta() {
		return transDebugMeta;
	}
}