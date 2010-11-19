/*************************************************************************************** 
 * Copyright (C) 2007 Samatar.  All rights reserved. 
 * This software was developed by Samatar and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. A copy of the license, 
 * is included with the binaries and source code. The Original Code is Samatar.  
 * The Initial Developer is Samatar.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an 
 * "AS IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. 
 * Please refer to the license for the specific language governing your rights 
 * and limitations.
 ***************************************************************************************/

/*
 * Created on 23-07-2008
 *
 */

package org.pentaho.di.ui.trans.steps.mailvalidator;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Cursor;
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
import org.eclipse.swt.widgets.Group;

import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.steps.mailvalidator.Messages;
import org.pentaho.di.trans.steps.mailvalidator.MailValidatorMeta;
import org.pentaho.di.ui.core.dialog.ErrorDialog;

public class MailValidatorDialog extends BaseStepDialog implements StepDialogInterface
{
	private boolean gotPreviousFields=false;

	private Label        wlemailFieldName;
	private CCombo       wemailFieldName;
	private FormData     fdlemailFieldName, fdemailFieldName;

	private Label        wldefaultSMTPField;
	private CCombo       wdefaultSMTPField;
	private FormData     fdldefaultSMTPField, fddefaultSMTPField;

	private Label        wlResult;
	private TextVar      wResult;
	private FormData     fdlResult, fdResult;
	
	private Label        wleMailSender;
	private TextVar      weMailSender;
	private FormData     fdleMailSender, fdeMailSender;
	
	private Label        wlTimeOut;
	private TextVar      wTimeOut;
	private FormData     fdlTimeOut, fdTimeOut;
	
	private Label        wlDefaultSMTP;
	private TextVar      wDefaultSMTP;
	private FormData     fdlDefaultSMTP, fdDefaultSMTP;
	
	private Label 		wldynamicDefaultSMTP;
	private Button 		wdynamicDefaultSMTP;
	private FormData 	fdldynamicDefaultSMTP;
	private FormData 	fddynamicDefaultSMTP;
	
	private Group wResultGroup;
	private FormData fdResultGroup;
	
	private Group wSettingsGroup;
	private FormData fdSettingsGroup;
	
	private Label wlResultAsString;
	private FormData fdlResultAsString;
	private Button wResultAsString;
	private FormData fdResultAsString;
	
	private Label wlSMTPCheck;
	private FormData fdlSMTPCheck;
	private Button wSMTPCheck;
	private FormData fdSMTPCheck;
	
	private Label wlResultStringFalse;
	private FormData fdlResultStringFalse;
	private Label wlResultStringTrue;
	private FormData fdlResultStringTrue;
	private FormData fdResultStringTrue;
	private FormData fdResultStringFalse;
	private TextVar wResultStringFalse;
	private TextVar wResultStringTrue;
	
	private TextVar wErrorMsg;
	private Label wlErrorMsg;
	private FormData fdlErrorMsg;
	private FormData fdErrorMsg;
	
	private MailValidatorMeta input;

	public MailValidatorDialog(Shell parent, Object in, TransMeta transMeta, String sname)
	{
		super(parent, (BaseStepMeta)in, transMeta, sname);
		input=(MailValidatorMeta)in;
	}

	public String open()
	{
		Shell parent = getParent();
		Display display = parent.getDisplay();

		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN);
 		props.setLook(shell);
        setShellImage(shell, input);

		ModifyListener lsMod = new ModifyListener() 
		{
			public void modifyText(ModifyEvent e) 
			{
				input.setChanged();
			}
		};

        
		changed = input.hasChanged();

		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText(Messages.getString("MailValidatorDialog.Shell.Title")); //$NON-NLS-1$
		
		int middle = props.getMiddlePct();
		int margin=Const.MARGIN;

		// Stepname line
		wlStepname=new Label(shell, SWT.RIGHT);
		wlStepname.setText(Messages.getString("MailValidatorDialog.Stepname.Label")); //$NON-NLS-1$
 		props.setLook(wlStepname);
		fdlStepname=new FormData();
		fdlStepname.left = new FormAttachment(0, 0);
		fdlStepname.right= new FormAttachment(middle, -margin);
		fdlStepname.top  = new FormAttachment(0, margin);
		wlStepname.setLayoutData(fdlStepname);
		wStepname=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wStepname.setText(stepname);
 		props.setLook(wStepname);
		wStepname.addModifyListener(lsMod);
		fdStepname=new FormData();
		fdStepname.left = new FormAttachment(middle, 0);
		fdStepname.top  = new FormAttachment(0, margin);
		fdStepname.right= new FormAttachment(100, 0);
		wStepname.setLayoutData(fdStepname);

		// emailFieldName field
		wlemailFieldName=new Label(shell, SWT.RIGHT);
		wlemailFieldName.setText(Messages.getString("MailValidatorDialog.emailFieldName.Label")); //$NON-NLS-1$
 		props.setLook(wlemailFieldName);
		fdlemailFieldName=new FormData();
		fdlemailFieldName.left = new FormAttachment(0, 0);
		fdlemailFieldName.right= new FormAttachment(middle, -margin);
		fdlemailFieldName.top  = new FormAttachment(wStepname, margin);
		wlemailFieldName.setLayoutData(fdlemailFieldName);
		
		wemailFieldName=new CCombo(shell, SWT.BORDER | SWT.READ_ONLY);
 		props.setLook(wemailFieldName);
 		wemailFieldName.addModifyListener(lsMod);
		fdemailFieldName=new FormData();
		fdemailFieldName.left = new FormAttachment(middle, 0);
		fdemailFieldName.top  = new FormAttachment(wStepname, margin);
		fdemailFieldName.right= new FormAttachment(100, -margin);
		wemailFieldName.setLayoutData(fdemailFieldName);
		wemailFieldName.addFocusListener(new FocusListener()
        {
            public void focusLost(org.eclipse.swt.events.FocusEvent e)
            {
            }
        
            public void focusGained(org.eclipse.swt.events.FocusEvent e)
            {
                Cursor busy = new Cursor(shell.getDisplay(), SWT.CURSOR_WAIT);
                shell.setCursor(busy);
                get();
                shell.setCursor(null);
                busy.dispose();
            }
        }
    );
		
		// ////////////////////////
		// START OF Settings GROUP
		// 

		wSettingsGroup = new Group(shell, SWT.SHADOW_NONE);
		props.setLook(wSettingsGroup);
		wSettingsGroup.setText(Messages.getString("MailValidatorDialog.SettingsGroup.Label"));
		
		FormLayout groupSettings = new FormLayout();
		groupSettings .marginWidth = 10;
		groupSettings .marginHeight = 10;
		wSettingsGroup.setLayout(groupSettings );

		
	    // perform SMTP check?
        wlSMTPCheck = new Label(wSettingsGroup, SWT.RIGHT);
        wlSMTPCheck.setText(Messages.getString("MailValidatorDialog.SMTPCheck.Label"));
		props.setLook(wlSMTPCheck);
		fdlSMTPCheck = new FormData();
		fdlSMTPCheck.left = new FormAttachment(0, 0);
		fdlSMTPCheck.top = new FormAttachment(wResult, margin);
		fdlSMTPCheck.right = new FormAttachment(middle, -2*margin);
		wlSMTPCheck.setLayoutData(fdlSMTPCheck);
		wSMTPCheck = new Button(wSettingsGroup, SWT.CHECK);
		props.setLook(wSMTPCheck);
		wSMTPCheck.setToolTipText(Messages.getString("MailValidatorDialog.SMTPCheck.Tooltip"));
		fdSMTPCheck = new FormData();
		fdSMTPCheck.left = new FormAttachment(middle, -margin);
		fdSMTPCheck.top = new FormAttachment(wemailFieldName, margin);
		wSMTPCheck.setLayoutData(fdSMTPCheck);
		wSMTPCheck.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent e)
            {
            	activeSMTPCheck();
            }
        });
		

		// TimeOut fieldname ...
		wlTimeOut=new Label(wSettingsGroup, SWT.RIGHT);
		wlTimeOut.setText(Messages.getString("MailValidatorDialog.TimeOutField.Label")); //$NON-NLS-1$
 		props.setLook(wlTimeOut);
		fdlTimeOut=new FormData();
		fdlTimeOut.left = new FormAttachment(0, 0);
		fdlTimeOut.right= new FormAttachment(middle, -2*margin);
		fdlTimeOut.top  = new FormAttachment(wSMTPCheck, margin);
		wlTimeOut.setLayoutData(fdlTimeOut);

		wTimeOut=new TextVar(transMeta,wSettingsGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wTimeOut.setToolTipText(Messages.getString("MailValidatorDialog.TimeOutField.Tooltip"));
 		props.setLook(wTimeOut);
		wTimeOut.addModifyListener(lsMod);
		fdTimeOut=new FormData();
		fdTimeOut.left = new FormAttachment(middle, -margin);
		fdTimeOut.top  = new FormAttachment(wSMTPCheck, margin);
		fdTimeOut.right= new FormAttachment(100, 0);
		wTimeOut.setLayoutData(fdTimeOut);
		
		// eMailSender fieldname ...
		wleMailSender=new Label(wSettingsGroup, SWT.RIGHT);
		wleMailSender.setText(Messages.getString("MailValidatorDialog.eMailSenderField.Label")); //$NON-NLS-1$
 		props.setLook(wleMailSender);
		fdleMailSender=new FormData();
		fdleMailSender.left = new FormAttachment(0, 0);
		fdleMailSender.right= new FormAttachment(middle, -2*margin);
		fdleMailSender.top  = new FormAttachment(wTimeOut, margin);
		wleMailSender.setLayoutData(fdleMailSender);

		weMailSender=new TextVar(transMeta,wSettingsGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		weMailSender.setToolTipText(Messages.getString("MailValidatorDialog.eMailSenderField.Tooltip"));
 		props.setLook(weMailSender);
		weMailSender.addModifyListener(lsMod);
		fdeMailSender=new FormData();
		fdeMailSender.left = new FormAttachment(middle, -margin);
		fdeMailSender.top  = new FormAttachment(wTimeOut, margin);
		fdeMailSender.right= new FormAttachment(100, 0);
		weMailSender.setLayoutData(fdeMailSender);
		

		// DefaultSMTP fieldname ...
		wlDefaultSMTP=new Label(wSettingsGroup, SWT.RIGHT);
		wlDefaultSMTP.setText(Messages.getString("MailValidatorDialog.DefaultSMTPField.Label")); //$NON-NLS-1$
 		props.setLook(wlDefaultSMTP);
		fdlDefaultSMTP=new FormData();
		fdlDefaultSMTP.left = new FormAttachment(0, 0);
		fdlDefaultSMTP.right= new FormAttachment(middle, -2*margin);
		fdlDefaultSMTP.top  = new FormAttachment(weMailSender, margin);
		wlDefaultSMTP.setLayoutData(fdlDefaultSMTP);

		wDefaultSMTP=new TextVar(transMeta,wSettingsGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wDefaultSMTP.setToolTipText(Messages.getString("MailValidatorDialog.DefaultSMTPField.Tooltip"));
 		props.setLook(wDefaultSMTP);
		wDefaultSMTP.addModifyListener(lsMod);
		fdDefaultSMTP=new FormData();
		fdDefaultSMTP.left = new FormAttachment(middle, -margin);
		fdDefaultSMTP.top  = new FormAttachment(weMailSender, margin);
		fdDefaultSMTP.right= new FormAttachment(100, 0);
		wDefaultSMTP.setLayoutData(fdDefaultSMTP);
		
		 // dynamic SMTP server?
        wldynamicDefaultSMTP = new Label(wSettingsGroup, SWT.RIGHT);
        wldynamicDefaultSMTP.setText(Messages.getString("MailValidatorDialog.dynamicDefaultSMTP.Label"));
		props.setLook(wldynamicDefaultSMTP);
		fdldynamicDefaultSMTP = new FormData();
		fdldynamicDefaultSMTP.left = new FormAttachment(0, 0);
		fdldynamicDefaultSMTP.top = new FormAttachment(wDefaultSMTP, margin);
		fdldynamicDefaultSMTP.right = new FormAttachment(middle, -2*margin);
		wldynamicDefaultSMTP.setLayoutData(fdldynamicDefaultSMTP);
		wdynamicDefaultSMTP = new Button(wSettingsGroup, SWT.CHECK);
		props.setLook(wdynamicDefaultSMTP);
		wdynamicDefaultSMTP.setToolTipText(Messages.getString("MailValidatorDialog.dynamicDefaultSMTP.Tooltip"));
		fddynamicDefaultSMTP = new FormData();
		fddynamicDefaultSMTP.left = new FormAttachment(middle, -margin);
		fddynamicDefaultSMTP.top = new FormAttachment(wDefaultSMTP, margin);
		wdynamicDefaultSMTP.setLayoutData(fddynamicDefaultSMTP);
		wdynamicDefaultSMTP.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent e)
            {
            	activedynamicDefaultSMTP();
            }
        });
		
		// defaultSMTPField field
		wldefaultSMTPField=new Label(wSettingsGroup, SWT.RIGHT);
		wldefaultSMTPField.setText(Messages.getString("MailValidatorDialog.defaultSMTPField.Label")); //$NON-NLS-1$
 		props.setLook(wldefaultSMTPField);
		fdldefaultSMTPField=new FormData();
		fdldefaultSMTPField.left = new FormAttachment(0, 0);
		fdldefaultSMTPField.right= new FormAttachment(middle, -2*margin);
		fdldefaultSMTPField.top  = new FormAttachment(wdynamicDefaultSMTP, margin);
		wldefaultSMTPField.setLayoutData(fdldefaultSMTPField);
		
		wdefaultSMTPField=new CCombo(wSettingsGroup, SWT.BORDER | SWT.READ_ONLY);
 		props.setLook(wdefaultSMTPField);
 		wdefaultSMTPField.addModifyListener(lsMod);
		fddefaultSMTPField=new FormData();
		fddefaultSMTPField.left = new FormAttachment(middle, -margin);
		fddefaultSMTPField.top  = new FormAttachment(wdynamicDefaultSMTP, margin);
		fddefaultSMTPField.right= new FormAttachment(100, -margin);
		wdefaultSMTPField.setLayoutData(fddefaultSMTPField);
		wdefaultSMTPField.addFocusListener(new FocusListener()
        {
            public void focusLost(org.eclipse.swt.events.FocusEvent e)
            {
            }
        
            public void focusGained(org.eclipse.swt.events.FocusEvent e)
            {
                Cursor busy = new Cursor(shell.getDisplay(), SWT.CURSOR_WAIT);
                shell.setCursor(busy);
                get();
                shell.setCursor(null);
                busy.dispose();
            }
        }
    );
		
		fdSettingsGroup = new FormData();
    	fdSettingsGroup.left = new FormAttachment(0, margin);
    	fdSettingsGroup.top = new FormAttachment(wemailFieldName, margin);
    	fdSettingsGroup.right = new FormAttachment(100, -margin);
    	wSettingsGroup.setLayoutData(fdSettingsGroup);
		
		// ///////////////////////////////////////////////////////////
		// / END OF Settings GROUP
		// ///////////////////////////////////////////////////////////
        
		
		
		// ////////////////////////
		// START OF Result GROUP
		// 

		wResultGroup = new Group(shell, SWT.SHADOW_NONE);
		props.setLook(wResultGroup);
		wResultGroup.setText(Messages.getString("MailValidatorDialog.ResultGroup.label"));
		
		FormLayout groupResult = new FormLayout();
		groupResult.marginWidth = 10;
		groupResult.marginHeight = 10;
		wResultGroup.setLayout(groupResult);

		
		// Result fieldname ...
		wlResult=new Label(wResultGroup, SWT.RIGHT);
		wlResult.setText(Messages.getString("MailValidatorDialog.ResultField.Label")); //$NON-NLS-1$
 		props.setLook(wlResult);
		fdlResult=new FormData();
		fdlResult.left = new FormAttachment(0, 0);
		fdlResult.right= new FormAttachment(middle, -2*margin);
		fdlResult.top  = new FormAttachment(wSettingsGroup, margin*2);
		wlResult.setLayoutData(fdlResult);

		wResult=new TextVar(transMeta,wResultGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wResult.setToolTipText(Messages.getString("MailValidatorDialog.ResultField.Tooltip"));
 		props.setLook(wResult);
		wResult.addModifyListener(lsMod);
		fdResult=new FormData();
		fdResult.left = new FormAttachment(middle, -margin);
		fdResult.top  = new FormAttachment(wSettingsGroup, margin*2);
		fdResult.right= new FormAttachment(100, 0);
		wResult.setLayoutData(fdResult);
		
	      // is Result as String
        wlResultAsString = new Label(wResultGroup, SWT.RIGHT);
        wlResultAsString.setText(Messages.getString("MailValidatorDialog.ResultAsString.Label"));
		props.setLook(wlResultAsString);
		fdlResultAsString = new FormData();
		fdlResultAsString.left = new FormAttachment(0, 0);
		fdlResultAsString.top = new FormAttachment(wResult, margin);
		fdlResultAsString.right = new FormAttachment(middle, -2*margin);
		wlResultAsString.setLayoutData(fdlResultAsString);
		wResultAsString = new Button(wResultGroup, SWT.CHECK);
		props.setLook(wResultAsString);
		wResultAsString.setToolTipText(Messages.getString("MailValidatorDialog.ResultAsString.Tooltip"));
		fdResultAsString = new FormData();
		fdResultAsString.left = new FormAttachment(middle, -margin);
		fdResultAsString.top = new FormAttachment(wResult, margin);
		wResultAsString.setLayoutData(fdResultAsString);
		wResultAsString.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent e)
            {
            	activeResultAsString();
            }
        });
		
		// ResultStringTrue fieldname ...
		wlResultStringTrue=new Label(wResultGroup, SWT.RIGHT);
		wlResultStringTrue.setText(Messages.getString("MailValidatorDialog.ResultStringTrueField.Label")); //$NON-NLS-1$
 		props.setLook(wlResultStringTrue);
		fdlResultStringTrue=new FormData();
		fdlResultStringTrue.left = new FormAttachment(0, 0);
		fdlResultStringTrue.right= new FormAttachment(middle, -2*margin);
		fdlResultStringTrue.top  = new FormAttachment(wResultAsString, margin);
		wlResultStringTrue.setLayoutData(fdlResultStringTrue);

		wResultStringTrue=new TextVar(transMeta,wResultGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wResultStringTrue.setToolTipText(Messages.getString("MailValidatorDialog.ResultStringTrueField.Tooltip"));
 		props.setLook(wResultStringTrue);
		wResultStringTrue.addModifyListener(lsMod);
		fdResultStringTrue=new FormData();
		fdResultStringTrue.left = new FormAttachment(middle, -margin);
		fdResultStringTrue.top  = new FormAttachment(wResultAsString, margin);
		fdResultStringTrue.right= new FormAttachment(100, 0);
		wResultStringTrue.setLayoutData(fdResultStringTrue);
		
		// ResultStringFalse fieldname ...
		wlResultStringFalse=new Label(wResultGroup, SWT.RIGHT);
		wlResultStringFalse.setText(Messages.getString("MailValidatorDialog.ResultStringFalseField.Label")); //$NON-NLS-1$
 		props.setLook(wlResultStringFalse);
		fdlResultStringFalse=new FormData();
		fdlResultStringFalse.left = new FormAttachment(0, 0);
		fdlResultStringFalse.right= new FormAttachment(middle, -2*margin);
		fdlResultStringFalse.top  = new FormAttachment(wResultStringTrue, margin);
		wlResultStringFalse.setLayoutData(fdlResultStringFalse);

		wResultStringFalse=new TextVar(transMeta,wResultGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wResultStringFalse.setToolTipText(Messages.getString("MailValidatorDialog.ResultStringFalseField.Tooltip"));
 		props.setLook(wResultStringFalse);
		wResultStringFalse.addModifyListener(lsMod);
		fdResultStringFalse=new FormData();
		fdResultStringFalse.left = new FormAttachment(middle, -margin);
		fdResultStringFalse.top  = new FormAttachment(wResultStringTrue, margin);
		fdResultStringFalse.right= new FormAttachment(100, 0);
		wResultStringFalse.setLayoutData(fdResultStringFalse);
		
		
		// ErrorMsg fieldname ...
		wlErrorMsg=new Label(wResultGroup, SWT.RIGHT);
		wlErrorMsg.setText(Messages.getString("MailValidatorDialog.ErrorMsgField.Label")); //$NON-NLS-1$
 		props.setLook(wlErrorMsg);
		fdlErrorMsg=new FormData();
		fdlErrorMsg.left = new FormAttachment(0, 0);
		fdlErrorMsg.right= new FormAttachment(middle, -2*margin);
		fdlErrorMsg.top  = new FormAttachment(wResultStringFalse, margin);
		wlErrorMsg.setLayoutData(fdlErrorMsg);

		wErrorMsg=new TextVar(transMeta,wResultGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wErrorMsg.setToolTipText(Messages.getString("MailValidatorDialog.ErrorMsgField.Tooltip"));
 		props.setLook(wErrorMsg);
		wErrorMsg.addModifyListener(lsMod);
		fdErrorMsg=new FormData();
		fdErrorMsg.left = new FormAttachment(middle, -margin);
		fdErrorMsg.top  = new FormAttachment(wResultStringFalse, margin);
		fdErrorMsg.right= new FormAttachment(100, 0);
		wErrorMsg.setLayoutData(fdErrorMsg);
		
		
		fdResultGroup = new FormData();
    	fdResultGroup.left = new FormAttachment(0, margin);
    	fdResultGroup.top = new FormAttachment(wSettingsGroup, 2*margin);
    	fdResultGroup.right = new FormAttachment(100, -margin);
    	wResultGroup.setLayoutData(fdResultGroup);
		
		// ///////////////////////////////////////////////////////////
		// / END OF Result GROUP
		// ///////////////////////////////////////////////////////////
        
    	
		// THE BUTTONS
		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(Messages.getString("System.Button.OK")); //$NON-NLS-1$
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(Messages.getString("System.Button.Cancel")); //$NON-NLS-1$

		setButtonPositions(new Button[] { wOK, wCancel }, margin, wResultGroup);

		// Add listeners
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();        } };

		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel();    } };
		
		wOK.addListener    (SWT.Selection, lsOK    );
		wCancel.addListener(SWT.Selection, lsCancel);
		
		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
		
		wStepname.addSelectionListener( lsDef );
		
		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );

		// Set the shell size, based upon previous time...
		setSize();
		
		getData();
		activeSMTPCheck();
		activeResultAsString();
		input.setChanged(changed);

		shell.open();
		while (!shell.isDisposed())
		{
				if (!display.readAndDispatch()) display.sleep();
		}
		return stepname;
	}
	private void activedynamicDefaultSMTP()
	{
		wldefaultSMTPField.setEnabled(wSMTPCheck.getSelection()&& wdynamicDefaultSMTP.getSelection());
		wdefaultSMTPField.setEnabled(wSMTPCheck.getSelection()&&wdynamicDefaultSMTP.getSelection());
	}
	private void activeSMTPCheck()
	{
		wlTimeOut.setEnabled(wSMTPCheck.getSelection());
		wTimeOut.setEnabled(wSMTPCheck.getSelection());
		wlDefaultSMTP.setEnabled(wSMTPCheck.getSelection());
		wDefaultSMTP.setEnabled(wSMTPCheck.getSelection());
		wleMailSender.setEnabled(wSMTPCheck.getSelection());
		weMailSender.setEnabled(wSMTPCheck.getSelection());
		wdynamicDefaultSMTP.setEnabled(wSMTPCheck.getSelection());
		wldynamicDefaultSMTP.setEnabled(wSMTPCheck.getSelection());
		activedynamicDefaultSMTP();
	}
	
	/**
	 * Copy information from the meta-data input to the dialog fields.
	 */ 
	public void getData()
	{
		if (input.getEmailField()!=null)   wemailFieldName.setText(input.getEmailField());
		if (input.getResultFieldName()!=null)   wResult.setText(input.getResultFieldName());
		
		wResultAsString.setSelection(input.isResultAsString());
		if (input.getEMailValideMsg()!=null)   wResultStringTrue.setText(input.getEMailValideMsg());
		if (input.getEMailNotValideMsg()!=null)   wResultStringFalse.setText(input.getEMailNotValideMsg());
		if (input.getErrorsField()!=null)   wErrorMsg.setText(input.getErrorsField());
		if (input.getTimeOut()!=null) {
			int i=Const.toInt(input.getTimeOut(), 0);
			if(i==0) wTimeOut.setText("0");
			else wTimeOut.setText(input.getTimeOut());
		}
		wSMTPCheck.setSelection(input.isSMTPCheck());
		if (input.getDefaultSMTP()!=null)   wDefaultSMTP.setText(input.getDefaultSMTP());
		if (input.geteMailSender()!=null)   weMailSender.setText(input.geteMailSender());
		wdynamicDefaultSMTP.setSelection(input.isdynamicDefaultSMTP());
		if(input.getDefaultSMTPField()!=null) wdefaultSMTPField.setText(input.getDefaultSMTPField());
		
		wStepname.selectAll();
	}
	private void activeResultAsString()
	{
		wlResultStringFalse.setEnabled(wResultAsString.getSelection());
		wResultStringFalse.setEnabled(wResultAsString.getSelection());
		wlResultStringTrue.setEnabled(wResultAsString.getSelection());
		wResultStringTrue.setEnabled(wResultAsString.getSelection());
	}
	private void cancel()
	{
		stepname=null;
		input.setChanged(changed);
		dispose();
	}
	
	private void ok()
	{
		if (Const.isEmpty(wStepname.getText())) return;
		input.setEmailfield(wemailFieldName.getText() );
		input.setResultFieldName(wResult.getText() );
		stepname = wStepname.getText(); // return value
		
		input.setResultAsString(wResultAsString.getSelection());
		input.setEmailValideMsg(wResultStringTrue.getText() );
		input.setEmailNotValideMsg(wResultStringFalse.getText() );
		input.setErrorsField(wErrorMsg.getText() );
		input.setTimeOut(wTimeOut.getText() );
		input.setDefaultSMTP(wDefaultSMTP.getText() );
		input.seteMailSender(weMailSender.getText() );
		input.setSMTPCheck(wSMTPCheck.getSelection());
		input.setdynamicDefaultSMTP(wdynamicDefaultSMTP.getSelection());
		input.setDefaultSMTPField(wdefaultSMTPField.getText());
		
		dispose();
	}

	 private void get()
		{
		 if(!gotPreviousFields){
			try{
				String emailField=null;
				String smtpdefaultField=null;
				if(wemailFieldName.getText()!=null) emailField=wemailFieldName.getText();
				if(wdefaultSMTPField.getText()!=null) smtpdefaultField=wdefaultSMTPField.getText();
				
				wemailFieldName.removeAll();
				wdefaultSMTPField.removeAll();

				RowMetaInterface r = transMeta.getPrevStepFields(stepname);
				if (r!=null){
					wemailFieldName.setItems(r.getFieldNames());
					wdefaultSMTPField.setItems(r.getFieldNames());
				}
				if(emailField!=null) wemailFieldName.setText(emailField);
				if(smtpdefaultField!=null) wdefaultSMTPField.setText(smtpdefaultField);
				gotPreviousFields=true;
			}
			catch(KettleException ke){
				new ErrorDialog(shell, Messages.getString("MailValidatorDialog.FailedToGetFields.DialogTitle"), Messages.getString("MailValidatorDialog.FailedToGetFields.DialogMessage"), ke); //$NON-NLS-1$ //$NON-NLS-2$
			}
		 }
		}
	public String toString()
	{
		return this.getClass().getName();
	}
}
