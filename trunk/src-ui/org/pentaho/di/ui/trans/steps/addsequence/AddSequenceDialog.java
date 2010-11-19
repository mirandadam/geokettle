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
 * Created on 2-jul-2003
 *
 */

package org.pentaho.di.ui.trans.steps.addsequence;

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
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.steps.addsequence.AddSequenceMeta;
import org.pentaho.di.trans.steps.addsequence.Messages;


public class AddSequenceDialog extends BaseStepDialog implements StepDialogInterface
{
	private Label        wlValuename;
	private Text         wValuename;

    private Group        gDatabase, gCounter;
    private FormData     fdDatabase, fdCounter;
    
	private Label        wlUseDatabase;
	private Button       wUseDatabase;

    private Label        wlConnection;
	private CCombo       wConnection;
    private Button       wbnConnection, wbeConnection;

    private Label        wlSchema;
    private TextVar      wSchema;

	private Label        wlSeqname;
	private TextVar      wSeqname;

	private Label        wlUseCounter;
	private Button       wUseCounter;

    private Label        wlCounterName;
    private Text         wCounterName;

	private Label        wlStartAt;
	private TextVar      wStartAt;

	private Label        wlIncrBy;
	private TextVar      wIncrBy;

	private Label        wlMaxVal;
	private TextVar      wMaxVal;

	private AddSequenceMeta input;
	
	public AddSequenceDialog(Shell parent, Object in, TransMeta transMeta, String sname)
	{
		super(parent, (BaseStepMeta)in, transMeta, sname);
		input=(AddSequenceMeta)in;
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
		shell.setText(Messages.getString("AddSequenceDialog.Shell.Title")); //$NON-NLS-1$
		
		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Stepname line
		wlStepname=new Label(shell, SWT.RIGHT);
		wlStepname.setText(Messages.getString("AddSequenceDialog.StepName.Label")); //$NON-NLS-1$
 		props.setLook(		wlStepname);
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

		// Valuename line
		wlValuename=new Label(shell, SWT.RIGHT);
		wlValuename.setText(Messages.getString("AddSequenceDialog.Valuename.Label")); //$NON-NLS-1$
 		props.setLook(wlValuename);
		FormData fdlValuename = new FormData();
		fdlValuename.left = new FormAttachment(0, 0);
		fdlValuename.top  = new FormAttachment(wStepname, margin);
		fdlValuename.right= new FormAttachment(middle, -margin);
		wlValuename.setLayoutData(fdlValuename);
		wValuename=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wValuename.setText(""); //$NON-NLS-1$
 		props.setLook(wValuename);
		wValuename.addModifyListener(lsMod);
		FormData fdValuename = new FormData();
		fdValuename.left = new FormAttachment(middle, 0);
		fdValuename.top  = new FormAttachment(wStepname, margin);
		fdValuename.right= new FormAttachment(100, 0);
		wValuename.setLayoutData(fdValuename);
        
        gDatabase = new Group(shell, SWT.NONE);
        gDatabase.setText(Messages.getString("AddSequenceDialog.UseDatabaseGroup.Label"));
        FormLayout databaseLayout = new FormLayout();
        databaseLayout.marginHeight = margin;
        databaseLayout.marginWidth = margin;
        gDatabase.setLayout(databaseLayout);
        props.setLook(gDatabase);
        fdDatabase = new FormData();
        fdDatabase.left  = new FormAttachment(0,0);
        fdDatabase.right = new FormAttachment(100,0);
        fdDatabase.top   = new FormAttachment(wValuename, 2*margin);
        gDatabase.setLayoutData(fdDatabase);

		wlUseDatabase=new Label(gDatabase, SWT.RIGHT);
		wlUseDatabase.setText(Messages.getString("AddSequenceDialog.UseDatabase.Label")); //$NON-NLS-1$
 		props.setLook(wlUseDatabase);
		FormData fdlUseDatabase = new FormData();
		fdlUseDatabase.left = new FormAttachment(0, 0);
		fdlUseDatabase.top  = new FormAttachment(0, 0);
		fdlUseDatabase.right= new FormAttachment(middle, -margin);
		wlUseDatabase.setLayoutData(fdlUseDatabase);
		wUseDatabase=new Button(gDatabase, SWT.CHECK );
 		props.setLook(wUseDatabase);
		wUseDatabase.setToolTipText(Messages.getString("AddSequenceDialog.UseDatabase.Tooltip")); //$NON-NLS-1$
		FormData fdUseDatabase = new FormData();
		fdUseDatabase.left = new FormAttachment(middle, 0);
		fdUseDatabase.top  = new FormAttachment(0, 0);
		wUseDatabase.setLayoutData(fdUseDatabase);
		wUseDatabase.addSelectionListener(new SelectionAdapter() 
			{
				public void widgetSelected(SelectionEvent e) 
				{
                    wUseCounter.setSelection(!wUseDatabase.getSelection());
					enableFields();
				}
			}
		);

		// Connection line
		wlConnection = new Label(gDatabase, SWT.RIGHT);
        wbnConnection = new Button(gDatabase, SWT.PUSH);
		wbeConnection = new Button(gDatabase, SWT.PUSH);
        wConnection = addConnectionLine(gDatabase, wUseDatabase, middle, margin, wlConnection, wbnConnection, wbeConnection);
		if (input.getDatabase()==null && transMeta.nrDatabases()==1) wConnection.select(0);
		wConnection.addModifyListener(lsMod);

        // Schema line...
        wlSchema=new Label(gDatabase, SWT.RIGHT);
        wlSchema.setText(Messages.getString("AddSequenceDialog.TargetSchema.Label")); //$NON-NLS-1$
        props.setLook(wlSchema);
        FormData fdlSchema = new FormData();
        fdlSchema.left = new FormAttachment(0, 0);
        fdlSchema.right= new FormAttachment(middle, -margin);
        fdlSchema.top  = new FormAttachment(wConnection, margin);
        wlSchema.setLayoutData(fdlSchema);

        wSchema=new TextVar(transMeta, gDatabase, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wSchema);
        wSchema.addModifyListener(lsMod);
        FormData fdSchema = new FormData();
        fdSchema.left = new FormAttachment(middle, 0);
        fdSchema.top  = new FormAttachment(wConnection, margin);
        fdSchema.right= new FormAttachment(100, 0);
        wSchema.setLayoutData(fdSchema);


		// Seqname line
		wlSeqname=new Label(gDatabase, SWT.RIGHT);
		wlSeqname.setText(Messages.getString("AddSequenceDialog.Seqname.Label")); //$NON-NLS-1$
 		props.setLook(wlSeqname);
		FormData fdlSeqname = new FormData();
		fdlSeqname.left = new FormAttachment(0, 0);
		fdlSeqname.right= new FormAttachment(middle, -margin);
		fdlSeqname.top  = new FormAttachment(wSchema, margin);
		wlSeqname.setLayoutData(fdlSeqname);
		wSeqname=new TextVar(transMeta, gDatabase, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wSeqname.setText(""); //$NON-NLS-1$
 		props.setLook(wSeqname);
		wSeqname.addModifyListener(lsMod);
		FormData fdSeqname = new FormData();
		fdSeqname.left = new FormAttachment(middle, 0);
		fdSeqname.top  = new FormAttachment(wSchema, margin);
		fdSeqname.right= new FormAttachment(100, 0);
		wSeqname.setLayoutData(fdSeqname);

        
        gCounter = new Group(shell, SWT.NONE);
        gCounter.setText(Messages.getString("AddSequenceDialog.UseCounterGroup.Label"));
        FormLayout counterLayout = new FormLayout();
        counterLayout.marginHeight = margin;
        counterLayout.marginWidth = margin;
        gCounter.setLayout(counterLayout);
        props.setLook(gCounter);
        fdCounter = new FormData();
        fdCounter.left  = new FormAttachment(0,0);
        fdCounter.right = new FormAttachment(100,0);
        fdCounter.top   = new FormAttachment(gDatabase, 2*margin);
        gCounter.setLayoutData(fdCounter);

		wlUseCounter=new Label(gCounter, SWT.RIGHT);
		wlUseCounter.setText(Messages.getString("AddSequenceDialog.UseCounter.Label")); //$NON-NLS-1$
 		props.setLook(wlUseCounter);
		FormData fdlUseCounter = new FormData();
		fdlUseCounter.left = new FormAttachment(0, 0);
		fdlUseCounter.top  = new FormAttachment(wSeqname, margin);
		fdlUseCounter.right= new FormAttachment(middle, -margin);
		wlUseCounter.setLayoutData(fdlUseCounter);
		wUseCounter=new Button(gCounter, SWT.CHECK );
 		props.setLook(wUseCounter);
		wUseCounter.setToolTipText(Messages.getString("AddSequenceDialog.UseCounter.Tooltip")); //$NON-NLS-1$
		FormData fdUseCounter = new FormData();
		fdUseCounter.left = new FormAttachment(middle, 0);
		fdUseCounter.top  = new FormAttachment(wSeqname, margin);
		wUseCounter.setLayoutData(fdUseCounter);
		wUseCounter.addSelectionListener(new SelectionAdapter() 
			{
				public void widgetSelected(SelectionEvent e) 
				{
                    wUseDatabase.setSelection(!wUseCounter.getSelection());
					enableFields();
				}
			}
		);

        // CounterName line
        wlCounterName=new Label(gCounter, SWT.RIGHT);
        wlCounterName.setText(Messages.getString("AddSequenceDialog.CounterName.Label")); //$NON-NLS-1$
        props.setLook(wlCounterName);
        FormData fdlCounterName = new FormData();
        fdlCounterName.left = new FormAttachment(0, 0);
        fdlCounterName.right= new FormAttachment(middle, -margin);
        fdlCounterName.top  = new FormAttachment(wUseCounter, margin);
        wlCounterName.setLayoutData(fdlCounterName);
        wCounterName=new Text(gCounter, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        wCounterName.setText(""); //$NON-NLS-1$
        props.setLook(wCounterName);
        wCounterName.addModifyListener(lsMod);
        FormData fdCounterName = new FormData();
        fdCounterName.left = new FormAttachment(middle, 0);
        fdCounterName.top  = new FormAttachment(wUseCounter, margin);
        fdCounterName.right= new FormAttachment(100, 0);
        wCounterName.setLayoutData(fdCounterName);

		// StartAt line
		wlStartAt=new Label(gCounter, SWT.RIGHT);
		wlStartAt.setText(Messages.getString("AddSequenceDialog.StartAt.Label")); //$NON-NLS-1$
 		props.setLook(wlStartAt);
		FormData fdlStartAt = new FormData();
		fdlStartAt.left = new FormAttachment(0, 0);
		fdlStartAt.right= new FormAttachment(middle, -margin);
		fdlStartAt.top  = new FormAttachment(wCounterName, margin);
		wlStartAt.setLayoutData(fdlStartAt);
		wStartAt=new TextVar(transMeta, gCounter, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wStartAt.setText(""); //$NON-NLS-1$
 		props.setLook(wStartAt);
		wStartAt.addModifyListener(lsMod);
		FormData fdStartAt = new FormData();
		fdStartAt.left = new FormAttachment(middle, 0);
		fdStartAt.top  = new FormAttachment(wCounterName, margin);
		fdStartAt.right= new FormAttachment(100, 0);
		wStartAt.setLayoutData(fdStartAt);

		// IncrBy line
		wlIncrBy=new Label(gCounter, SWT.RIGHT);
		wlIncrBy.setText(Messages.getString("AddSequenceDialog.IncrBy.Label")); //$NON-NLS-1$
 		props.setLook(wlIncrBy);
		FormData fdlIncrBy = new FormData();
		fdlIncrBy.left = new FormAttachment(0, 0);
		fdlIncrBy.right= new FormAttachment(middle, -margin);
		fdlIncrBy.top  = new FormAttachment(wStartAt, margin);
		wlIncrBy.setLayoutData(fdlIncrBy);
		wIncrBy=new TextVar(transMeta, gCounter, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wIncrBy.setText(""); //$NON-NLS-1$
 		props.setLook(wIncrBy);
		wIncrBy.addModifyListener(lsMod);
		FormData fdIncrBy = new FormData();
		fdIncrBy.left = new FormAttachment(middle, 0);
		fdIncrBy.top  = new FormAttachment(wStartAt, margin);
		fdIncrBy.right= new FormAttachment(100, 0);
		wIncrBy.setLayoutData(fdIncrBy);

		// MaxVal line
		wlMaxVal=new Label(gCounter, SWT.RIGHT);
		wlMaxVal.setText(Messages.getString("AddSequenceDialog.MaxVal.Label")); //$NON-NLS-1$
 		props.setLook(wlMaxVal);
		FormData fdlMaxVal = new FormData();
		fdlMaxVal.left = new FormAttachment(0, 0);
		fdlMaxVal.right= new FormAttachment(middle, -margin);
		fdlMaxVal.top  = new FormAttachment(wIncrBy, margin);
		wlMaxVal.setLayoutData(fdlMaxVal);
		wMaxVal=new TextVar(transMeta, gCounter, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wMaxVal.setText(""); //$NON-NLS-1$
 		props.setLook(wMaxVal);
		wMaxVal.addModifyListener(lsMod);
		FormData fdMaxVal = new FormData();
		fdMaxVal.left = new FormAttachment(middle, 0);
		fdMaxVal.top  = new FormAttachment(wIncrBy, margin);
		fdMaxVal.right= new FormAttachment(100, 0);
		wMaxVal.setLayoutData(fdMaxVal);

		
		// THE BUTTONS
		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(Messages.getString("System.Button.OK")); //$NON-NLS-1$
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(Messages.getString("System.Button.Cancel")); //$NON-NLS-1$

		setButtonPositions(new Button[] { wOK, wCancel }, margin, gCounter);

		// Add listeners
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();        } };
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel();    } };
		
		wOK.addListener    (SWT.Selection, lsOK    );
		wCancel.addListener(SWT.Selection, lsCancel);
		
		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
		
		wStepname.addSelectionListener( lsDef );
		wValuename.addSelectionListener( lsDef);
        wSchema.addSelectionListener( lsDef );
		wSeqname.addSelectionListener( lsDef );
		wStartAt.addSelectionListener( lsDef );
		wIncrBy.addSelectionListener( lsDef );
		wMaxVal.addSelectionListener( lsDef );
        wCounterName.addSelectionListener( lsDef );
		
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
	
	public void enableFields()
	{
        boolean useDatabase = wUseDatabase.getSelection();
        boolean useCounter = wUseCounter.getSelection();
        
		wlConnection.setEnabled(useDatabase);
		wConnection.setEnabled(useDatabase);
        wbnConnection.setEnabled(useDatabase);
		wbeConnection.setEnabled(useDatabase);
        wlSchema.setEnabled(useDatabase);
        wSchema.setEnabled(useDatabase);
        wlSeqname.setEnabled(useDatabase);
		wSeqname.setEnabled(useDatabase);
		
        wlCounterName.setEnabled(useCounter);
        wCounterName.setEnabled(useCounter);
		wlStartAt.setEnabled(useCounter);
		wStartAt.setEnabled(useCounter);
		wlIncrBy.setEnabled(useCounter);
		wIncrBy.setEnabled(useCounter);
		wlMaxVal.setEnabled(useCounter);
		wMaxVal.setEnabled(useCounter);
	}

	/**
	 * Copy information from the meta-data input to the dialog fields.
	 */ 
	public void getData()
	{
		log.logDebug(toString(), Messages.getString("AddSequenceDialog.Log.GettingKeyInfo")); //$NON-NLS-1$

		if (input.getValuename()!=null) wValuename.setText(input.getValuename());
		
		wUseDatabase.setSelection(input.isDatabaseUsed());
		if (input.getDatabase()!=null) wConnection.setText(input.getDatabase().getName());
		else if (transMeta.nrDatabases()==1)
		{
			wConnection.setText( transMeta.getDatabase(0).getName() );
		}
        if (input.getSchemaName()!=null) wSchema.setText(input.getSchemaName());
        if (input.getSequenceName()!=null) wSeqname.setText(input.getSequenceName());
		
		wUseCounter.setSelection(input.isCounterUsed());
        wCounterName.setText( Const.NVL(input.getCounterName(), "") );
		wStartAt.setText(input.getStartAt()); //$NON-NLS-1$
		wIncrBy.setText(input.getIncrementBy()); //$NON-NLS-1$
		wMaxVal.setText(input.getMaxValue()); //$NON-NLS-1$
		
		enableFields();
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
		
		stepname = wStepname.getText(); // return value

        input.setUseCounter(wUseCounter.getSelection());
        input.setUseDatabase(wUseDatabase.getSelection());
        
		String connection=wConnection.getText();
		input.setDatabase(transMeta.findDatabase(connection));
        input.setSchemaName(wSchema.getText());
		input.setSequenceName(wSeqname.getText());
		input.setValuename(wValuename.getText());
		
        input.setCounterName( wCounterName.getText() );
		input.setStartAt    ( wStartAt.getText() );
		input.setIncrementBy( wIncrBy.getText() );
		input.setMaxValue   ( wMaxVal.getText() );
		
		if (input.isDatabaseUsed() && transMeta.findDatabase(connection)==null)
		{
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
			mb.setMessage(Messages.getString("AddSequenceDialog.NoValidConnectionError.DialogMessage")); //$NON-NLS-1$
			mb.setText(Messages.getString("AddSequenceDialog.NoValidConnectionError.DialogTitle")); //$NON-NLS-1$
			mb.open();
		}
		
		dispose();
	}
	
	public String toString()
	{
		return this.getClass().getName();
	}
}