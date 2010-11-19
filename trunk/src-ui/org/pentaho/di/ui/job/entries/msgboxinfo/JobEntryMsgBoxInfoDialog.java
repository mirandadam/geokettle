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

package org.pentaho.di.ui.job.entries.msgboxinfo;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox; 
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
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entries.msgboxinfo.JobEntryMsgBoxInfo;
import org.pentaho.di.job.entries.msgboxinfo.Messages;
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
 * This dialog allows you to edit a JobEntryEval object.
 * 
 * @author Matt
 * @since 19-06-2003
 */
public class JobEntryMsgBoxInfoDialog extends JobEntryDialog implements JobEntryDialogInterface
{
    private Label wlName;

    private Text wName;

    private FormData fdlName, fdName;

    private Label wlBodyMessage;

    private Text wBodyMessage;

    private FormData fdlBodyMessage, fdBodyMessage;

    private Button wOK, wCancel;

    private Listener lsOK, lsCancel;

    private JobEntryMsgBoxInfo jobEntry;

    private Shell shell;

    private SelectionAdapter lsDef;
    
    private boolean changed;

	//TitleMessage
	private Label wlTitleMessage;

	private TextVar wTitleMessage;

	private FormData fdlTitleMessage, fdTitleMessage;

    public JobEntryMsgBoxInfoDialog(Shell parent, JobEntryInterface jobEntryInt, Repository rep, JobMeta jobMeta)
    {
        super(parent, jobEntryInt, rep, jobMeta);
        jobEntry = (JobEntryMsgBoxInfo) jobEntryInt;
        if (this.jobEntry.getName() == null)
            this.jobEntry.setName(Messages.getString("MsgBoxInfo.Name.Default"));
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
        shell.setText(Messages.getString("MsgBoxInfo.Title"));

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
        wlName.setText(Messages.getString("MsgBoxInfo.Label"));
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


		// Title Msgbox
		wlTitleMessage = new Label(shell, SWT.RIGHT);
		wlTitleMessage.setText(Messages.getString("MsgBoxInfo.TitleMessage.Label"));
		props.setLook(wlTitleMessage);
		fdlTitleMessage = new FormData();
		fdlTitleMessage.left = new FormAttachment(0, 0);
		fdlTitleMessage.top = new FormAttachment(wName, margin);
		fdlTitleMessage.right = new FormAttachment(middle, -margin);
		wlTitleMessage.setLayoutData(fdlTitleMessage);

		wTitleMessage = new TextVar(jobMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wTitleMessage);
		wTitleMessage.addModifyListener(lsMod);
		fdTitleMessage = new FormData();
		fdTitleMessage.left = new FormAttachment(middle, 0);
		fdTitleMessage.top = new FormAttachment(wName, margin);
		fdTitleMessage.right = new FormAttachment(100, 0);
		wTitleMessage.setLayoutData(fdTitleMessage);

        // Body Msgbox
		wlBodyMessage = new Label(shell, SWT.RIGHT);
        wlBodyMessage.setText(Messages.getString("MsgBoxInfo.BodyMessage.Label"));
        props.setLook(wlBodyMessage);
        fdlBodyMessage = new FormData();
        fdlBodyMessage.left = new FormAttachment(0, 0);
        fdlBodyMessage.top = new FormAttachment(wTitleMessage, margin);
		fdlBodyMessage.right = new FormAttachment(middle, -margin);
        wlBodyMessage.setLayoutData(fdlBodyMessage);

        wBodyMessage = new Text(shell, SWT.MULTI | SWT.LEFT | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        wBodyMessage.setText(Messages.getString("MsgBoxInfo.Name.Default"));
        props.setLook(wBodyMessage,Props.WIDGET_STYLE_FIXED);
        wBodyMessage.addModifyListener(lsMod);
        fdBodyMessage = new FormData();
        fdBodyMessage.left = new FormAttachment(middle, 0);
        fdBodyMessage.top = new FormAttachment(wTitleMessage, margin);
        fdBodyMessage.right = new FormAttachment(100, 0);
		fdBodyMessage.bottom =new FormAttachment(wOK, -margin);
        wBodyMessage.setLayoutData(fdBodyMessage);

		// SelectionAdapter lsVar = VariableButtonListenerFactory.getSelectionAdapter(shell, wBodyMessage, jobMeta);
		wBodyMessage.addKeyListener(new ControlSpaceKeyAdapter(jobMeta, wBodyMessage));

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
        if (jobEntry.getBodyMessage() != null)
            wBodyMessage.setText(jobEntry.getBodyMessage());

		if (jobEntry.getTitleMessage() != null)
			wTitleMessage.setText(jobEntry.getTitleMessage());
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
		jobEntry.setTitleMessage(wTitleMessage.getText());
        jobEntry.setBodyMessage(wBodyMessage.getText());
        dispose();
    }

    public String toString()
    {
        return this.getClass().getName();
    }
}
