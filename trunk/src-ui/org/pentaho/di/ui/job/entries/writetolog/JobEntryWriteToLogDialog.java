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

/*
 * Created on 19-jun-2003
 *
 */

package org.pentaho.di.ui.job.entries.writetolog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox; 
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entries.writetolog.JobEntryWriteToLog;
import org.pentaho.di.job.entries.writetolog.Messages;
import org.pentaho.di.job.entry.JobEntryDialogInterface;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.core.widget.ControlSpaceKeyAdapter;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.job.dialog.JobDialog;
import org.pentaho.di.ui.job.entry.JobEntryDialog;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

/**
 * This dialog allows you to edit a JobEntryWriteToLog object.
 * 
 * @author Samatar
 * @since 08-08-2007
 */

public class JobEntryWriteToLogDialog extends JobEntryDialog implements JobEntryDialogInterface
{
    private Label wlName;

    private Text wName;

    private FormData fdlName, fdName;

    private Label wlLogMessage;

    private Text wLogMessage;

    private FormData fdlLogMessage, fdLogMessage;

    private Button wOK, wCancel;

    private Listener lsOK, lsCancel;

    private JobEntryWriteToLog jobEntry;

    private Shell shell;

    private SelectionAdapter lsDef;

    private boolean changed;

	//Log subject
	private Label wlLogSubject;

	private TextVar wLogSubject;

	private FormData fdlLogSubject, fdLogSubject;
	
    private Label wlLoglevel;

    private CCombo wLoglevel;

    private FormData fdlLoglevel, fdLoglevel;
    
    
    public JobEntryWriteToLogDialog(Shell parent, JobEntryInterface jobEntryInt, Repository rep, JobMeta jobMeta)
    {
        super(parent, jobEntryInt, rep, jobMeta);
        jobEntry = (JobEntryWriteToLog) jobEntryInt;
        if (this.jobEntry.getName() == null)
            this.jobEntry.setName(Messages.getString("WriteToLog.Name.Default"));
    }
    
    
    public JobEntryInterface open()
    {
        Shell parent = getParent();
        Display display = parent.getDisplay();

        shell = new Shell(parent, props.getJobsDialogStyle());
        props.setLook(shell);
        JobDialog.setShellImage(shell, jobEntry);

        ModifyListener lsMod = new ModifyListener()
        {
            public void modifyText(ModifyEvent e)
            {
                jobEntry.setChanged();
            }
        };
        changed = jobEntry.hasChanged();

        FormLayout formLayout = new FormLayout();
        formLayout.marginWidth = Const.FORM_MARGIN;
        formLayout.marginHeight = Const.FORM_MARGIN;

        shell.setLayout(formLayout);
        shell.setText(Messages.getString("WriteToLog.Title"));

        int middle = props.getMiddlePct();
        int margin = Const.MARGIN;

        wOK = new Button(shell, SWT.PUSH);
        wOK.setText(Messages.getString("System.Button.OK"));
        wCancel = new Button(shell, SWT.PUSH);
        wCancel.setText(Messages.getString("System.Button.Cancel"));

        // at the bottom
        BaseStepDialog.positionBottomButtons(shell, new Button[] { wOK, wCancel }, margin, null);

        // Filename line
        wlName = new Label(shell, SWT.RIGHT);
        wlName.setText(Messages.getString("WriteToLog.Jobname.Label"));
        props.setLook(wlName);
        fdlName = new FormData();
        fdlName.left = new FormAttachment(0, 0);
		fdlName.right = new FormAttachment(middle, 0);
        fdlName.top = new FormAttachment(0, margin);
        wlName.setLayoutData(fdlName);
        wName = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wName);
        wName.addModifyListener(lsMod);
        fdName = new FormData();
        fdName.left = new FormAttachment(middle, 0);
        fdName.top = new FormAttachment(0, margin);
        fdName.right = new FormAttachment(100, 0);
        wName.setLayoutData(fdName);

        // Log Level
        wlLoglevel = new Label(shell, SWT.RIGHT);
        wlLoglevel.setText(Messages.getString("WriteToLog.Loglevel.Label"));
        props.setLook(wlLoglevel);
        fdlLoglevel = new FormData();
        fdlLoglevel.left = new FormAttachment(0, 0);
        fdlLoglevel.right = new FormAttachment(middle, -margin);
        fdlLoglevel.top = new FormAttachment(wName, margin);
        wlLoglevel.setLayoutData(fdlLoglevel);
        wLoglevel = new CCombo(shell, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER);
        for (int i = 0; i < LogWriter.log_level_desc_long.length; i++)
            wLoglevel.add(LogWriter.log_level_desc_long[i]);
        wLoglevel.select(jobEntry.loglevel + 1); // +1: starts at -1

        props.setLook(wLoglevel);
        fdLoglevel = new FormData();
        fdLoglevel.left = new FormAttachment(middle, 0);
        fdLoglevel.top = new FormAttachment(wName, margin);
        fdLoglevel.right = new FormAttachment(100, 0);
        wLoglevel.setLayoutData(fdLoglevel);

        // Subject
		wlLogSubject = new Label(shell, SWT.RIGHT);
		wlLogSubject.setText(Messages.getString("WriteToLog.LogSubject.Label"));
        props.setLook(wlLogSubject);
        fdlLogSubject = new FormData();
        fdlLogSubject.left = new FormAttachment(0, 0);
        fdlLogSubject.top = new FormAttachment(wLoglevel, margin);
        fdlLogSubject.right = new FormAttachment(middle, -margin);
        wlLogSubject.setLayoutData(fdlLogSubject);

        wLogSubject = new TextVar(jobMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        wLogSubject.setText(Messages.getString("WriteToLog.Name.Default"));
        props.setLook(wLogSubject);
        wLogSubject.addModifyListener(lsMod);
        fdLogSubject = new FormData();
        fdLogSubject.left = new FormAttachment(middle, 0);
        fdLogSubject.top = new FormAttachment(wLoglevel, margin);
        fdLogSubject.right = new FormAttachment(100, 0);
		wLogSubject.setLayoutData(fdLogSubject);
		
			

        // Log message to display
		wlLogMessage = new Label(shell, SWT.RIGHT);
        wlLogMessage.setText(Messages.getString("WriteToLog.LogMessage.Label"));
        props.setLook(wlLogMessage);
        fdlLogMessage = new FormData();
        fdlLogMessage.left = new FormAttachment(0, 0);
        fdlLogMessage.top = new FormAttachment(wLogSubject, margin);
		fdlLogMessage.right = new FormAttachment(middle, -margin);
        wlLogMessage.setLayoutData(fdlLogMessage);

        wLogMessage = new Text(shell, SWT.MULTI | SWT.LEFT | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        wLogMessage.setText(Messages.getString("WriteToLog.Name.Default"));
        props.setLook(wLogMessage,Props.WIDGET_STYLE_FIXED);
        wLogMessage.addModifyListener(lsMod);
        fdLogMessage = new FormData();
        fdLogMessage.left = new FormAttachment(middle, 0);
        fdLogMessage.top = new FormAttachment(wLogSubject, margin);
        fdLogMessage.right = new FormAttachment(100, 0);
		fdLogMessage.bottom =new FormAttachment(wOK, -margin);
        wLogMessage.setLayoutData(fdLogMessage);

		// SelectionAdapter lsVar = VariableButtonListenerFactory.getSelectionAdapter(shell, wLogMessage, jobMeta);
		wLogMessage.addKeyListener(new ControlSpaceKeyAdapter(jobMeta, wLogMessage));

        // Add listeners
        lsCancel = new Listener()
        {
            public void handleEvent(Event e)
            {
                cancel();
            }
        };



        lsOK = new Listener()
        {
            public void handleEvent(Event e)
            {
                ok();
            }
        };

        wCancel.addListener(SWT.Selection, lsCancel);
        wOK.addListener(SWT.Selection, lsOK);

        lsDef = new SelectionAdapter()
        {
            public void widgetDefaultSelected(SelectionEvent e)
            {
                ok();
            }
        };

        wName.addSelectionListener(lsDef);

        // Detect X or ALT-F4 or something that kills this window...
        shell.addShellListener(new ShellAdapter()
        {
            public void shellClosed(ShellEvent e)
            {
                cancel();
            }
        });


        getData();

        BaseStepDialog.setSize(shell, 250, 250, false);

        shell.open();
        props.setDialogSize(shell, "JobEvalDialogSize");
        while (!shell.isDisposed())
        {
            if (!display.readAndDispatch())
                display.sleep();
        }
        return jobEntry;
    }

    public void dispose()
    {
        WindowProperty winprop = new WindowProperty(shell);
        props.setScreen(winprop);
        shell.dispose();
    }

    /**
     * Copy information from the meta-data input to the dialog fields.
     */
    public void getData()
    {
        if (jobEntry.getName() != null)
            wName.setText(jobEntry.getName());
        wName.selectAll();
        if (jobEntry.getLogMessage() != null)
            wLogMessage.setText(jobEntry.getLogMessage());
        
        if (jobEntry.getLogSubject() != null)
            wLogSubject.setText(jobEntry.getLogSubject());

        wLoglevel.select(jobEntry.loglevel + 1);
    }

    private void cancel()
    {
        jobEntry.setChanged(changed);
        jobEntry = null;
        dispose();
    }

    private void ok()
    {
 	   if(Const.isEmpty(wName.getText())) 
       {
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
			mb.setText(Messages.getString("System.StepJobEntryNameMissing.Title"));
			mb.setMessage(Messages.getString("System.JobEntryNameMissing.Msg"));
			mb.open(); 
			return;
       }
        jobEntry.setName(wName.getText());
        jobEntry.setLogMessage(wLogMessage.getText());
        jobEntry.setLogSubject(wLogSubject.getText());
        jobEntry.loglevel = wLoglevel.getSelectionIndex() - 1;
        dispose();
    }

    public String toString()
    {
        return this.getClass().getName();
    }
}
