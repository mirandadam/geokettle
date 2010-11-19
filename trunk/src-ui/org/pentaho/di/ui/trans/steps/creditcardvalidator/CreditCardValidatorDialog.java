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

package org.pentaho.di.ui.trans.steps.creditcardvalidator;

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

import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.steps.creditcardvalidator.CreditCardValidatorMeta;
import org.pentaho.di.trans.steps.creditcardvalidator.Messages;

public class CreditCardValidatorDialog extends BaseStepDialog implements StepDialogInterface
{
	private boolean gotPreviousFields=false;

	private Label        wlFieldName;
	private CCombo       wFieldName;
	private FormData     fdlFieldName, fdFieldName;

	private Label        wlResult,wlCardType;
	private TextVar      wResult,wFileType;
	private FormData     fdlResult, fdResult,fdAdditionalFields,fdlCardType,fdCardType;
	
	private Label        wlNotValidMsg;
	private TextVar      wNotValidMsg;
	private FormData     fdlNotValidMsg,fdNotValidMsg;
	
	private Label        wlgetOnlyDigits;
	private Button       wgetOnlyDigits;
	private FormData     fdlgetOnlyDigits, fdgetOnlyDigits;
	
	private Group wOutputFields;
	

	private CreditCardValidatorMeta input;

	public CreditCardValidatorDialog(Shell parent, Object in, TransMeta transMeta, String sname)
	{
		super(parent, (BaseStepMeta)in, transMeta, sname);
		input=(CreditCardValidatorMeta)in;
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
		shell.setText(Messages.getString("CreditCardValidatorDialog.Shell.Title")); //$NON-NLS-1$
		
		int middle = props.getMiddlePct();
		int margin=Const.MARGIN;

		// Stepname line
		wlStepname=new Label(shell, SWT.RIGHT);
		wlStepname.setText(Messages.getString("CreditCardValidatorDialog.Stepname.Label")); //$NON-NLS-1$
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

		// filename field
		wlFieldName=new Label(shell, SWT.RIGHT);
		wlFieldName.setText(Messages.getString("CreditCardValidatorDialog.FieldName.Label")); //$NON-NLS-1$
 		props.setLook(wlFieldName);
		fdlFieldName=new FormData();
		fdlFieldName.left = new FormAttachment(0, 0);
		fdlFieldName.right= new FormAttachment(middle, -margin);
		fdlFieldName.top  = new FormAttachment(wStepname, margin);
		wlFieldName.setLayoutData(fdlFieldName);
		
		
		wFieldName=new CCombo(shell, SWT.BORDER | SWT.READ_ONLY);
 		props.setLook(wFieldName);
 		wFieldName.addModifyListener(lsMod);
		fdFieldName=new FormData();
		fdFieldName.left = new FormAttachment(middle, 0);
		fdFieldName.top  = new FormAttachment(wStepname, margin);
		fdFieldName.right= new FormAttachment(100, -margin);
		wFieldName.setLayoutData(fdFieldName);
		wFieldName.addFocusListener(new FocusListener()
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
		
		// get only digits?
		wlgetOnlyDigits=new Label(shell, SWT.RIGHT);
		wlgetOnlyDigits.setText(Messages.getString("CreditCardValidator.getOnlyDigits.Label"));
 		props.setLook(wlgetOnlyDigits);
		fdlgetOnlyDigits=new FormData();
		fdlgetOnlyDigits.left = new FormAttachment(0, 0);
		fdlgetOnlyDigits.top  = new FormAttachment(wFieldName, margin);
		fdlgetOnlyDigits.right= new FormAttachment(middle, -margin);
		wlgetOnlyDigits.setLayoutData(fdlgetOnlyDigits);
		wgetOnlyDigits=new Button(shell, SWT.CHECK );
 		props.setLook(wgetOnlyDigits);
		wgetOnlyDigits.setToolTipText(Messages.getString("CreditCardValidator.getOnlyDigits.Tooltip"));
		fdgetOnlyDigits=new FormData();
		fdgetOnlyDigits.left = new FormAttachment(middle, 0);
		fdgetOnlyDigits.top  = new FormAttachment(wFieldName, margin);
		wgetOnlyDigits.setLayoutData(fdgetOnlyDigits);
		
		///////////////////////////////// 
		// START OF Output Fields GROUP  //
		///////////////////////////////// 

		wOutputFields = new Group(shell, SWT.SHADOW_NONE);
		props.setLook(wOutputFields);
		wOutputFields.setText(Messages.getString("CreditCardValidatorDialog.OutputFields.Label"));
		
		FormLayout OutputFieldsgroupLayout = new FormLayout();
		OutputFieldsgroupLayout.marginWidth = 10;
		OutputFieldsgroupLayout.marginHeight = 10;
		wOutputFields.setLayout(OutputFieldsgroupLayout);
		
		
		// Result fieldname ...
		wlResult=new Label(wOutputFields, SWT.RIGHT);
		wlResult.setText(Messages.getString("CreditCardValidatorDialog.ResultField.Label")); //$NON-NLS-1$
 		props.setLook(wlResult);
		fdlResult=new FormData();
		fdlResult.left = new FormAttachment(0, -margin);
		fdlResult.right= new FormAttachment(middle, -2*margin);
		fdlResult.top  = new FormAttachment(wgetOnlyDigits, 2*margin);
		wlResult.setLayoutData(fdlResult);

		wResult=new TextVar(transMeta,wOutputFields, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wResult.setToolTipText(Messages.getString("CreditCardValidatorDialog.ResultField.Tooltip"));
 		props.setLook(wResult);
		wResult.addModifyListener(lsMod);
		fdResult=new FormData();
		fdResult.left = new FormAttachment(middle, -margin);
		fdResult.top  = new FormAttachment(wgetOnlyDigits, 2*margin);
		fdResult.right= new FormAttachment(100, 0);
		wResult.setLayoutData(fdResult);
		
		
		// FileType fieldname ...
		wlCardType=new Label(wOutputFields, SWT.RIGHT);
		wlCardType.setText(Messages.getString("CreditCardValidatorDialog.CardType.Label")); //$NON-NLS-1$
 		props.setLook(wlCardType);
 		fdlCardType=new FormData();
 		fdlCardType.left = new FormAttachment(0, -margin);
 		fdlCardType.right= new FormAttachment(middle, -2*margin);
		fdlCardType.top  = new FormAttachment(wResult, margin);
		wlCardType.setLayoutData(fdlCardType);

		wFileType=new TextVar(transMeta,wOutputFields, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wFileType.setToolTipText(Messages.getString("CreditCardValidatorDialog.CardType.Tooltip"));
 		props.setLook(wFileType);
		wFileType.addModifyListener(lsMod);
		fdCardType=new FormData();
		fdCardType.left = new FormAttachment(middle, -margin);
		fdCardType.top  = new FormAttachment(wResult, margin);
		fdCardType.right= new FormAttachment(100, 0);
		wFileType.setLayoutData(fdCardType);
		
		// UnvalidMsg fieldname ...
		wlNotValidMsg=new Label(wOutputFields, SWT.RIGHT);
		wlNotValidMsg.setText(Messages.getString("CreditCardValidatorDialog.NotValidMsg.Label")); //$NON-NLS-1$
 		props.setLook(wlNotValidMsg);
		fdlNotValidMsg=new FormData();
		fdlNotValidMsg.left = new FormAttachment(0, -margin);
		fdlNotValidMsg.right= new FormAttachment(middle, -2*margin);
		fdlNotValidMsg.top  = new FormAttachment(wFileType, margin);
		wlNotValidMsg.setLayoutData(fdlNotValidMsg);

		wNotValidMsg=new TextVar(transMeta,wOutputFields, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wNotValidMsg.setToolTipText(Messages.getString("CreditCardValidatorDialog.NotValidMsg.Tooltip"));
 		props.setLook(wNotValidMsg);
		wNotValidMsg.addModifyListener(lsMod);
		fdNotValidMsg=new FormData();
		fdNotValidMsg.left = new FormAttachment(middle, -margin);
		fdNotValidMsg.top  = new FormAttachment(wFileType, margin);
		fdNotValidMsg.right= new FormAttachment(100, 0);
		wNotValidMsg.setLayoutData(fdNotValidMsg);
		
		fdAdditionalFields = new FormData();
		fdAdditionalFields.left = new FormAttachment(0, margin);
		fdAdditionalFields.top = new FormAttachment(wgetOnlyDigits, 2*margin);
		fdAdditionalFields.right = new FormAttachment(100, -margin);
		wOutputFields.setLayoutData(fdAdditionalFields);
		
		///////////////////////////////// 
		// END OF Additional Fields GROUP  //
		///////////////////////////////// 

		// THE BUTTONS
		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(Messages.getString("System.Button.OK")); //$NON-NLS-1$
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(Messages.getString("System.Button.Cancel")); //$NON-NLS-1$

		setButtonPositions(new Button[] { wOK, wCancel }, margin, wOutputFields);

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

		input.setChanged(changed);

		shell.open();
		while (!shell.isDisposed())
		{
				if (!display.readAndDispatch()) display.sleep();
		}
		return stepname;
	}

	/**
	 * Copy information from the meta-data input to the dialog fields.
	 */ 
	public void getData()
	{
		if (input.getDynamicField() !=null)   wFieldName.setText(input.getDynamicField());
		wgetOnlyDigits.setSelection(input.isOnlyDigits());
		if (input.getResultFieldName()!=null)   wResult.setText(input.getResultFieldName());
		if (input.getCardType()!=null)   wFileType.setText(input.getCardType());
		if (input.getNotValidMsg()!=null)   wNotValidMsg.setText(input.getNotValidMsg());
		wStepname.selectAll();
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
		input.setDynamicField(wFieldName.getText() );
		input.setOnlyDigits(wgetOnlyDigits.getSelection());
		input.setResultFieldName(wResult.getText() );
		input.setCardType(wFileType.getText() );
		input.setNotValidMsg(wNotValidMsg.getText() );
		stepname = wStepname.getText(); // return value
		
		dispose();
	}
	private void get() {
		if(!gotPreviousFields)
		{
			try {
				String columnName = wFieldName.getText();
				wFieldName.removeAll();
				RowMetaInterface r = transMeta.getPrevStepFields(stepname);
				if (r != null) {
					r.getFieldNames();
	
					for (int i = 0; i < r.getFieldNames().length; i++) {
						wFieldName.add(r.getFieldNames()[i]);
					}
				}
				wFieldName.setText(columnName);
				gotPreviousFields=true;
			} catch (KettleException ke) {
				new ErrorDialog(shell, Messages.getString("CreditCardValidatorDialog.FailedToGetFields.DialogTitle"), Messages.getString("CreditCardValidatorDialog.FailedToGetFields.DialogMessage"), ke); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
	}
	public String toString()
	{
		return this.getClass().getName();
	}
}
