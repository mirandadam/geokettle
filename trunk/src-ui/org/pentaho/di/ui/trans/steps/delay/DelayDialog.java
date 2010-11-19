 /* Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Samatar Hassan.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.*/

/*
 * Created on 18-06-2008
 *
 */

package org.pentaho.di.ui.trans.steps.delay;

import org.eclipse.swt.SWT;
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

import org.pentaho.di.ui.core.widget.LabelTextVar;
import org.pentaho.di.core.Const;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.steps.delay.DelayMeta;
import org.pentaho.di.trans.steps.delay.Messages;

public class DelayDialog extends BaseStepDialog implements StepDialogInterface
{
	private DelayMeta input;
	private CCombo   wScaleTime;
	private FormData fdScaleTime;

	private LabelTextVar wTimeout;
	private FormData     fdTimeout;
	
	public DelayDialog(Shell parent, Object in, TransMeta tr, String sname)
	{
		super(parent, (BaseStepMeta)in, tr, sname);
		input=(DelayMeta)in;
	}

	public String open()
	{
		Shell parent = getParent();
		Display display = parent.getDisplay();

		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MIN | SWT.MAX);
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
		shell.setText(Messages.getString("DelayDialog.Shell.Title"));
		
		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Stepname line
		wlStepname=new Label(shell, SWT.RIGHT);
		wlStepname.setText(Messages.getString("DelayDialog.Stepname.Label"));
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
		
		// Timeout line
		wTimeout = new LabelTextVar(transMeta, shell, Messages.getString("DelayDialog.Timeout.Label"), Messages.getString("DelayDialog.Timeout.Tooltip"));
		props.setLook(wTimeout);
		wTimeout.addModifyListener(lsMod);
		fdTimeout = new FormData();
		fdTimeout.left = new FormAttachment(0, -margin);
		fdTimeout.top = new FormAttachment(wStepname, margin);
		fdTimeout.right = new FormAttachment(100, -margin);
		wTimeout.setLayoutData(fdTimeout);

		// Whenever something changes, set the tooltip to the expanded version:
		wTimeout.addModifyListener(new ModifyListener()
	    {
		    public void modifyText(ModifyEvent e)
		    {
			    wTimeout.setToolTipText(transMeta.environmentSubstitute( wTimeout.getText() ) );
		    }
	    }
		);
		
		wScaleTime = new CCombo(shell, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER);
		wScaleTime.add(Messages.getString("DelayDialog.MSScaleTime.Label"));
		wScaleTime.add(Messages.getString("DelayDialog.SScaleTime.Label"));
		wScaleTime.add(Messages.getString("DelayDialog.MnScaleTime.Label"));
		wScaleTime.add(Messages.getString("DelayDialog.HrScaleTime.Label"));
		wScaleTime.select(0); // +1: starts at -1
		props.setLook(wScaleTime);
		fdScaleTime= new FormData();
		fdScaleTime.left = new FormAttachment(middle, 0);
		fdScaleTime.top = new FormAttachment(wTimeout, margin);
		fdScaleTime.right = new FormAttachment(100, 0);
		wScaleTime.setLayoutData(fdScaleTime);
		wScaleTime.addModifyListener(lsMod);
		
		// Some buttons
		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(Messages.getString("System.Button.OK")); //$NON-NLS-1$
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(Messages.getString("System.Button.Cancel")); //$NON-NLS-1$

		setButtonPositions(new Button[] { wOK, wCancel }, margin, wScaleTime);

		// Add listeners
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel(); } };
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();     } };
		
		wCancel.addListener(SWT.Selection, lsCancel);
		wOK.addListener    (SWT.Selection, lsOK    );
		
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
		wStepname.selectAll();
		if(input.getTimeOut()!=null) wTimeout.setText(input.getTimeOut());
		wScaleTime.select(input.getScaleTimeCode());
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
		stepname = wStepname.getText(); // return value
		input.setTimeOut(Const.toInt(wTimeout.getText(), 0)+"");
		input.setScaleTimeCode(wScaleTime.getSelectionIndex());
		dispose();
	}
}
