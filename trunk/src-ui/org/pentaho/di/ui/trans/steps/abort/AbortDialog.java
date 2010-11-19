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
package org.pentaho.di.ui.trans.steps.abort;

import org.eclipse.swt.SWT;
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
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.steps.abort.AbortMeta;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.trans.steps.abort.Messages;


public class AbortDialog extends BaseStepDialog implements StepDialogInterface
{
	private Label        wlRowThreshold;
	private TextVar      wRowThreshold;
	private FormData     fdlRowThreshold, fdRowThreshold;

	private Label        wlMessage;
	private TextVar      wMessage;
	private FormData     fdlMessage, fdMessage;
	
    private Label        wlAlwaysLogRows;
    private Button       wAlwaysLogRows;
    private FormData     fdlAlwaysLogRows, fdAlwaysLogRows;
	
    private AbortMeta input;
    
    public AbortDialog(Shell parent, Object in, TransMeta transMeta, String sname)
    {
        super(parent, (BaseStepMeta)in, transMeta, sname);
        input=(AbortMeta)in;
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
        shell.setText(Messages.getString("AbortDialog.Shell.Title")); //$NON-NLS-1$
        
        int middle = props.getMiddlePct();
        int margin = Const.MARGIN;
        
        // Stepname line
        wlStepname=new Label(shell, SWT.RIGHT);
        wlStepname.setText(Messages.getString("AbortDialog.Stepname.Label")); //$NON-NLS-1$
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
        
		// RowThreshold line
		wlRowThreshold=new Label(shell, SWT.RIGHT);
		wlRowThreshold.setText(Messages.getString("AbortDialog.RowThreshold.Label")); //$NON-NLS-1$
 		props.setLook(wlRowThreshold);
		fdlRowThreshold=new FormData();
		fdlRowThreshold.left = new FormAttachment(0, 0);
		fdlRowThreshold.right= new FormAttachment(middle, -margin);
		fdlRowThreshold.top  = new FormAttachment(wStepname, margin);
		wlRowThreshold.setLayoutData(fdlRowThreshold);
		wRowThreshold=new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wRowThreshold.setText(""); //$NON-NLS-1$
 		props.setLook(wRowThreshold);
 		wRowThreshold.addModifyListener(lsMod);
 		wRowThreshold.setToolTipText(Messages.getString("AbortDialog.RowThreshold.Tooltip"));
		wRowThreshold.addModifyListener(lsMod);
		fdRowThreshold=new FormData();
		fdRowThreshold.left = new FormAttachment(middle, 0);
		fdRowThreshold.top  = new FormAttachment(wStepname, margin);
		fdRowThreshold.right= new FormAttachment(100, 0);
		wRowThreshold.setLayoutData(fdRowThreshold);
						
		// Message line
		wlMessage=new Label(shell, SWT.RIGHT);
		wlMessage.setText(Messages.getString("AbortDialog.AbortMessage.Label")); //$NON-NLS-1$
 		props.setLook(wlMessage);
		fdlMessage=new FormData();
		fdlMessage.left = new FormAttachment(0, 0);
		fdlMessage.right= new FormAttachment(middle, -margin);
		fdlMessage.top  = new FormAttachment(wRowThreshold, margin);
		wlMessage.setLayoutData(fdlMessage);
		wMessage=new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wMessage.setText(""); //$NON-NLS-1$
 		props.setLook(wMessage);
 		wMessage.addModifyListener(lsMod);
 		wMessage.setToolTipText(Messages.getString("AbortDialog.AbortMessage.Tooltip"));
		wMessage.addModifyListener(lsMod);
		fdMessage=new FormData();
		fdMessage.left = new FormAttachment(middle, 0);
		fdMessage.top  = new FormAttachment(wRowThreshold, margin);
		fdMessage.right= new FormAttachment(100, 0);
		wMessage.setLayoutData(fdMessage);
	
        wlAlwaysLogRows = new Label(shell, SWT.RIGHT);
        wlAlwaysLogRows.setText(Messages.getString("AbortDialog.AlwaysLogRows.Label"));
        props.setLook(wlAlwaysLogRows);
        fdlAlwaysLogRows = new FormData();
        fdlAlwaysLogRows.left = new FormAttachment(0, 0);
        fdlAlwaysLogRows.top = new FormAttachment(wMessage, margin);
        fdlAlwaysLogRows.right = new FormAttachment(middle, -margin);
        wlAlwaysLogRows.setLayoutData(fdlAlwaysLogRows);
        wAlwaysLogRows = new Button(shell, SWT.CHECK);
        props.setLook(wAlwaysLogRows);
        wAlwaysLogRows.setToolTipText(Messages.getString("AbortDialog.AlwaysLogRows.Tooltip"));
        fdAlwaysLogRows = new FormData();
        fdAlwaysLogRows.left = new FormAttachment(middle, 0);
        fdAlwaysLogRows.top = new FormAttachment(wMessage, margin);
        fdAlwaysLogRows.right = new FormAttachment(100, 0);
        wAlwaysLogRows.setLayoutData(fdAlwaysLogRows);
        wAlwaysLogRows.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent e)
            {
            	input.setChanged();
            }
        });		
		
        // Some buttons
        wOK=new Button(shell, SWT.PUSH);
        wOK.setText(BaseMessages.getString(getClass().getPackage().getName(), "System.Button.OK")); //$NON-NLS-1$
        wCancel=new Button(shell, SWT.PUSH);
        wCancel.setText(BaseMessages.getString(getClass().getPackage().getName(), "System.Button.Cancel")); //$NON-NLS-1$
        
        setButtonPositions(new Button[] { wOK, wCancel }, margin, wAlwaysLogRows);        

        // Add listeners
        lsCancel   = new Listener() { public void handleEvent(Event e) { cancel(); } };
        lsOK       = new Listener() { public void handleEvent(Event e) { ok();     } };
        
        wCancel.addListener(SWT.Selection, lsCancel);
        wOK.addListener    (SWT.Selection, lsOK    );
        
        lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
        
        wStepname.addSelectionListener( lsDef );
        wRowThreshold.addSelectionListener( lsDef );
        wMessage.addSelectionListener( lsDef );
        
        // Detect X or ALT-F4 or something that kills this window...
        shell.addShellListener( new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );

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
		if ( input.getRowThreshold() !=null ) wRowThreshold.setText(input.getRowThreshold());
		if ( input.getMessage() != null ) wMessage.setText(input.getMessage());
		wAlwaysLogRows.setSelection(input.isAlwaysLogRows());
		
		wStepname.selectAll();
	}
	
	private void getInfo(AbortMeta in)
	{
		input.setRowThreshold(wRowThreshold.getText());
		input.setMessage(wMessage.getText());
		input.setAlwaysLogRows(wAlwaysLogRows.getSelection());		
	}
    
    /**
     * Cancel the dialog.
     */
    private void cancel()
    {
        stepname=null;
        input.setChanged(changed);
        dispose();
    }
    
    private void ok()
    {
		if (Const.isEmpty(wStepname.getText())) return;
		    	
    	getInfo(input);
        stepname = wStepname.getText(); // return value
        dispose();
    }
}