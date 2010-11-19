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

package org.pentaho.di.ui.trans.steps.update;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
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
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.SQLStatement;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.ui.trans.step.TableItemInsertListener;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.update.Messages;
import org.pentaho.di.trans.steps.update.UpdateMeta;
import org.pentaho.di.ui.core.database.dialog.DatabaseExplorerDialog;
import org.pentaho.di.ui.core.database.dialog.SQLEditor;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.core.widget.TextVar;



public class UpdateDialog extends BaseStepDialog implements StepDialogInterface
{
	private CCombo       wConnection;

	private Label        wlKey;
	private TableView    wKey;
	private FormData     fdlKey, fdKey;

    private Label        wlSchema;
    private TextVar      wSchema;
    private FormData     fdlSchema, fdSchema;

	private Label        wlTable;
	private Button       wbTable;
	private TextVar      wTable;
	private FormData     fdlTable, fdbTable, fdTable;

	private Label        wlReturn;
	private TableView    wReturn;
	private FormData     fdlReturn, fdReturn;

	private Label        wlCommit;
	private Text         wCommit;
	private FormData     fdlCommit, fdCommit;
    
    private Label        wlErrorIgnored;
    private Button       wErrorIgnored;
    private FormData     fdlErrorIgnored, fdErrorIgnored;

    private Label        wlIgnoreFlagField;
    private Text         wIgnoreFlagField;
    private FormData     fdlIgnoreFlagField, fdIgnoreFlagField;
    
	private Button wGetLU;
	private FormData fdGetLU;
	private Listener lsGetLU;

	private UpdateMeta input;
	
	private ColumnInfo[] ciKey;
	
	private ColumnInfo[] ciReturn;
	
    private Map<String, Integer> inputFields;
    
    private Label        wlSkipLookup;
	private Button       wSkipLookup;
	private FormData     fdlSkipLookup, fdSkipLookup;
    
	/**
	 * List of ColumnInfo that should have the field names of the selected database table
	 */
	private List<ColumnInfo> tableFieldColumns = new ArrayList<ColumnInfo>();

	public UpdateDialog(Shell parent, Object in, TransMeta tr, String sname)
	{
		super(parent, (BaseStepMeta)in, tr, sname);
		input=(UpdateMeta)in;
        inputFields =new HashMap<String, Integer>();
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
		FocusListener lsFocusLost = new FocusAdapter() {
			public void focusLost(FocusEvent arg0) {
				setTableFieldCombo();
			}
		};
		changed = input.hasChanged();

		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText(Messages.getString("UpdateDialog.Shell.Title")); //$NON-NLS-1$
		
		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Stepname line
		wlStepname=new Label(shell, SWT.RIGHT);
		wlStepname.setText(Messages.getString("UpdateDialog.Stepname.Label")); //$NON-NLS-1$
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

		// Connection line
		wConnection = addConnectionLine(shell, wStepname, middle, margin);
		if (input.getDatabaseMeta()==null && transMeta.nrDatabases()==1) wConnection.select(0);
		wConnection.addModifyListener(lsMod);
		
        // Schema line...
        wlSchema=new Label(shell, SWT.RIGHT);
        wlSchema.setText(Messages.getString("UpdateDialog.TargetSchema.Label")); //$NON-NLS-1$
        props.setLook(wlSchema);
        fdlSchema=new FormData();
        fdlSchema.left = new FormAttachment(0, 0);
        fdlSchema.right= new FormAttachment(middle, -margin);
        fdlSchema.top  = new FormAttachment(wConnection, margin*2);
        wlSchema.setLayoutData(fdlSchema);

        wSchema=new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wSchema);
        wSchema.addModifyListener(lsMod);
        wSchema.addFocusListener(lsFocusLost);
        fdSchema=new FormData();
        fdSchema.left = new FormAttachment(middle, 0);
        fdSchema.top  = new FormAttachment(wConnection, margin*2);
        fdSchema.right= new FormAttachment(100, 0);
        wSchema.setLayoutData(fdSchema);

		// Table line...
		wlTable=new Label(shell, SWT.RIGHT);
		wlTable.setText(Messages.getString("UpdateDialog.TargetTable.Label")); //$NON-NLS-1$
 		props.setLook(wlTable);
		fdlTable=new FormData();
		fdlTable.left = new FormAttachment(0, 0);
		fdlTable.right= new FormAttachment(middle, -margin);
		fdlTable.top  = new FormAttachment(wSchema, margin);
		wlTable.setLayoutData(fdlTable);

		wbTable=new Button(shell, SWT.PUSH| SWT.CENTER);
 		props.setLook(wbTable);
		wbTable.setText(Messages.getString("UpdateDialog.Browse.Button")); //$NON-NLS-1$
		fdbTable=new FormData();
		fdbTable.right= new FormAttachment(100, 0);
		fdbTable.top  = new FormAttachment(wSchema, margin);
		wbTable.setLayoutData(fdbTable);

		wTable=new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wTable);
		wTable.addModifyListener(lsMod);
		wTable.addFocusListener(lsFocusLost);
		fdTable=new FormData();
		fdTable.left = new FormAttachment(middle, 0);
		fdTable.top  = new FormAttachment(wSchema, margin);
		fdTable.right= new FormAttachment(wbTable, -margin);
		wTable.setLayoutData(fdTable);

		// Commit line
		wlCommit = new Label(shell, SWT.RIGHT);
		wlCommit.setText(Messages.getString("UpdateDialog..Commit.Label")); //$NON-NLS-1$
 		props.setLook(wlCommit);
		fdlCommit = new FormData();
		fdlCommit.left = new FormAttachment(0, 0);
		fdlCommit.top = new FormAttachment(wTable, margin);
		fdlCommit.right = new FormAttachment(middle, -margin);
		wlCommit.setLayoutData(fdlCommit);
		wCommit = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wCommit);
		wCommit.addModifyListener(lsMod);
		fdCommit = new FormData();
		fdCommit.left = new FormAttachment(middle, 0);
		fdCommit.top = new FormAttachment(wTable, margin);
		fdCommit.right = new FormAttachment(100, 0);
		wCommit.setLayoutData(fdCommit);
        
		// UsePart update
		wlSkipLookup=new Label(shell, SWT.RIGHT);
		wlSkipLookup.setText(Messages.getString("UpdateDialog.SkipLookup.Label"));
 		props.setLook(wlSkipLookup);
		fdlSkipLookup=new FormData();
		fdlSkipLookup.left  = new FormAttachment(0, 0);
		fdlSkipLookup.top   = new FormAttachment(wCommit, margin);
		fdlSkipLookup.right = new FormAttachment(middle, -margin);
		wlSkipLookup.setLayoutData(fdlSkipLookup);
		wSkipLookup=new Button(shell, SWT.CHECK);
		wSkipLookup.setToolTipText(Messages.getString("UpdateDialog.SkipLookup.Tooltip"));
 		props.setLook(wSkipLookup);
		fdSkipLookup=new FormData();
		fdSkipLookup.left  = new FormAttachment(middle, 0);
		fdSkipLookup.top   = new FormAttachment(wCommit, margin);
		fdSkipLookup.right = new FormAttachment(100, 0);
		wSkipLookup.setLayoutData(fdSkipLookup);
		wSkipLookup.addSelectionListener(new SelectionAdapter() 
        {
            public void widgetSelected(SelectionEvent e) 
            {
                input.setChanged();
                setActiveIgnoreLookup();
            }
        }
    );		
		
        wlErrorIgnored=new Label(shell, SWT.RIGHT);
        wlErrorIgnored.setText(Messages.getString("UpdateDialog.ErrorIgnored.Label")); //$NON-NLS-1$
 		props.setLook(wlErrorIgnored);
        fdlErrorIgnored=new FormData();
        fdlErrorIgnored.left = new FormAttachment(0, 0);
        fdlErrorIgnored.top  = new FormAttachment(wSkipLookup, margin);
        fdlErrorIgnored.right= new FormAttachment(middle, -margin);
        wlErrorIgnored.setLayoutData(fdlErrorIgnored);
        
        wErrorIgnored=new Button(shell, SWT.CHECK );
 		props.setLook(        wErrorIgnored);
        wErrorIgnored.setToolTipText(Messages.getString("UpdateDialog.ErrorIgnored.ToolTip")); //$NON-NLS-1$
        fdErrorIgnored=new FormData();
        fdErrorIgnored.left = new FormAttachment(middle, 0);
        fdErrorIgnored.top  = new FormAttachment(wSkipLookup, margin);
        wErrorIgnored.setLayoutData(fdErrorIgnored);
        wErrorIgnored.addSelectionListener(new SelectionAdapter() 
            {
                public void widgetSelected(SelectionEvent e) 
                {
                    input.setChanged();
                    setFlags();
                }
            }
        );

        wlIgnoreFlagField=new Label(shell, SWT.LEFT);
        wlIgnoreFlagField.setText(Messages.getString("UpdateDialog.FlagField.Label")); //$NON-NLS-1$
 		props.setLook(        wlIgnoreFlagField);
        fdlIgnoreFlagField=new FormData();
        fdlIgnoreFlagField.left = new FormAttachment(wErrorIgnored, margin);
        fdlIgnoreFlagField.top  = new FormAttachment(wSkipLookup, margin);
        wlIgnoreFlagField.setLayoutData(fdlIgnoreFlagField);
        wIgnoreFlagField=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(        wIgnoreFlagField);
        wIgnoreFlagField.addModifyListener(lsMod);
        fdIgnoreFlagField=new FormData();
        fdIgnoreFlagField.left = new FormAttachment(wlIgnoreFlagField, margin);
        fdIgnoreFlagField.top  = new FormAttachment(wSkipLookup, margin);
        fdIgnoreFlagField.right= new FormAttachment(100, 0);
        wIgnoreFlagField.setLayoutData(fdIgnoreFlagField);


		wlKey=new Label(shell, SWT.NONE);
		wlKey.setText(Messages.getString("UpdateDialog.Key.Label")); //$NON-NLS-1$
 		props.setLook(wlKey);
		fdlKey=new FormData();
		fdlKey.left  = new FormAttachment(0, 0);
		fdlKey.top   = new FormAttachment(wIgnoreFlagField, margin);
		wlKey.setLayoutData(fdlKey);

		int nrKeyCols=4;
		int nrKeyRows=(input.getKeyStream()!=null?input.getKeyStream().length:1);
		
		ciKey=new ColumnInfo[nrKeyCols];
		ciKey[0]=new ColumnInfo(Messages.getString("UpdateDialog.ColumnInfo.TableField"),    ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] { "" }, false); //$NON-NLS-1$
		ciKey[1]=new ColumnInfo(Messages.getString("UpdateDialog.ColumnInfo.Comparator"),    ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] { "=", "<>", "<", "<=", ">", ">=", "LIKE", "BETWEEN", "IS NULL", "IS NOT NULL" } ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$ //$NON-NLS-11$
		ciKey[2]=new ColumnInfo(Messages.getString("UpdateDialog.ColumnInfo.StreamField1"),  ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] { "" }, false); //$NON-NLS-1$
		ciKey[3]=new ColumnInfo(Messages.getString("UpdateDialog.ColumnInfo.StreamField2"),  ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] { "" }, false); //$NON-NLS-1$
		tableFieldColumns.add(ciKey[0]);
		wKey=new TableView(transMeta, shell, 
						      SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL, 
						      ciKey, 
						      nrKeyRows,  
						      lsMod,
						      props
						      );

		wGet = new Button(shell, SWT.PUSH);
		wGet.setText(Messages.getString("UpdateDialog.GetFields.Button")); //$NON-NLS-1$
		fdGet = new FormData();
		fdGet.right = new FormAttachment(100, 0);
		fdGet.top = new FormAttachment(wlKey, margin);
		wGet.setLayoutData(fdGet);

		fdKey = new FormData();
		fdKey.left = new FormAttachment(0, 0);
		fdKey.top = new FormAttachment(wlKey, margin);
		fdKey.right = new FormAttachment(wGet, -margin);
		fdKey.bottom = new FormAttachment(wlKey, 190);
		wKey.setLayoutData(fdKey);

		// THE BUTTONS
		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(Messages.getString("System.Button.OK")); //$NON-NLS-1$
		wSQL=new Button(shell, SWT.PUSH);
		wSQL.setText(Messages.getString("UpdateDialog.SQL.Button")); //$NON-NLS-1$
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(Messages.getString("System.Button.Cancel")); //$NON-NLS-1$

		setButtonPositions(new Button[] { wOK, wCancel , wSQL }, margin, null);

		
		// THE UPDATE/INSERT TABLE
		wlReturn=new Label(shell, SWT.NONE);
		wlReturn.setText(Messages.getString("UpdateDialog.Return.Label")); //$NON-NLS-1$
 		props.setLook(wlReturn);
		fdlReturn=new FormData();
		fdlReturn.left  = new FormAttachment(0, 0);
		fdlReturn.top   = new FormAttachment(wKey, margin);
		wlReturn.setLayoutData(fdlReturn);
		
		int UpInsCols=2;
		int UpInsRows= (input.getUpdateLookup()!=null?input.getUpdateLookup().length:1);
		
		ciReturn=new ColumnInfo[UpInsCols];
		ciReturn[0]=new ColumnInfo(Messages.getString("UpdateDialog.ColumnInfo.TableField"),  ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] { "" }, false); //$NON-NLS-1$
		ciReturn[1]=new ColumnInfo(Messages.getString("UpdateDialog.ColumnInfo.StreamField"), ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] { "" }, false); //$NON-NLS-1$
		tableFieldColumns.add(ciReturn[0]);
		wReturn=new TableView(transMeta, shell, 
							  SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL, 
							  ciReturn, 
							  UpInsRows,  
							  lsMod,
							  props
							  );

		wGetLU = new Button(shell, SWT.PUSH);
		wGetLU.setText(Messages.getString("UpdateDialog.GetAndUpdateFields")); //$NON-NLS-1$
		fdGetLU = new FormData();
		fdGetLU.top   = new FormAttachment(wlReturn, margin);
		fdGetLU.right = new FormAttachment(100, 0);
		wGetLU.setLayoutData(fdGetLU);

		fdReturn = new FormData();
		fdReturn.left = new FormAttachment(0, 0);
		fdReturn.top = new FormAttachment(wlReturn, margin);
		fdReturn.right = new FormAttachment(wGetLU, -margin);
		fdReturn.bottom = new FormAttachment(wOK, -2*margin);
		wReturn.setLayoutData(fdReturn);
		
		
		
	    // 
        // Search the fields in the background
        //
        
        final Runnable runnable = new Runnable()
        {
            public void run()
            {
                StepMeta stepMeta = transMeta.findStep(stepname);
                if (stepMeta!=null)
                {
                    try
                    {
                    	RowMetaInterface row = transMeta.getPrevStepFields(stepMeta);
                        
                        // Remember these fields...
                        for (int i=0;i<row.size();i++)
                        {
                        	inputFields.put(row.getValueMeta(i).getName(), Integer.valueOf(i));
                        }
                        
                        setComboBoxes(); 
                    }
                    catch(KettleException e)
                    {
                        log.logError(toString(),Messages.getString("System.Dialog.GetFieldsFailed.Message"));
                    }
                }
            }
        };
        new Thread(runnable).start();


		// Add listeners
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();        } };
		lsGet      = new Listener() { public void handleEvent(Event e) { get();       } };
		lsGetLU    = new Listener() { public void handleEvent(Event e) { getUpdate(); } };
		lsSQL      = new Listener() { public void handleEvent(Event e) { create();    } };
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel();    } };
		
		wOK.addListener    (SWT.Selection, lsOK    );
		wGet.addListener   (SWT.Selection, lsGet   );
		wGetLU.addListener (SWT.Selection, lsGetLU );
		wSQL.addListener   (SWT.Selection, lsSQL   );
		wCancel.addListener(SWT.Selection, lsCancel);
		
		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
		
        wStepname.addSelectionListener( lsDef );
        wSchema.addSelectionListener( lsDef );
        wTable.addSelectionListener( lsDef );
        wCommit.addSelectionListener( lsDef );
		wIgnoreFlagField.addSelectionListener( lsDef );
        
		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );

		wbTable.addSelectionListener
		(
			new SelectionAdapter()
			{
				public void widgetSelected(SelectionEvent e) 
				{
					getTableName();
				}
			}
		);

		// Set the shell size, based upon previous time...
		setSize();
		getData();
		setActiveIgnoreLookup();
		setTableFieldCombo();
		input.setChanged(changed);

		shell.open();
		while (!shell.isDisposed())
		{
				if (!display.readAndDispatch()) display.sleep();
		}
		return stepname;
	}
	 
	 public void setActiveIgnoreLookup()
	 {
		 if(wSkipLookup.getSelection())
		 {
			 wErrorIgnored.setSelection(false);
			 wIgnoreFlagField.setText("");
		 }
		 wErrorIgnored.setEnabled(!wSkipLookup.getSelection());
		 wlErrorIgnored.setEnabled(!wSkipLookup.getSelection());
	     wlIgnoreFlagField.setEnabled(!wSkipLookup.getSelection() && wErrorIgnored.getSelection());
	     wIgnoreFlagField.setEnabled(!wSkipLookup.getSelection() && wErrorIgnored.getSelection());
	     
	 }
	protected void setComboBoxes()
    {
        // Something was changed in the row.
        //
		final Map<String, Integer> fields = new HashMap<String, Integer>();
        
        // Add the currentMeta fields...
        fields.putAll(inputFields);
        
        Set<String> keySet = fields.keySet();
        List<String> entries = new ArrayList<String>(keySet);
        
        String[] fieldNames= (String[]) entries.toArray(new String[entries.size()]);
        Const.sortStrings(fieldNames);
        // Key fields
        ciKey[2].setComboValues(fieldNames);
        ciKey[3].setComboValues(fieldNames);
        // return fields
        ciReturn[1].setComboValues(fieldNames);
    }
    public void setFlags()
    {
        wlIgnoreFlagField.setEnabled(wErrorIgnored.getSelection());
        wIgnoreFlagField.setEnabled(wErrorIgnored.getSelection());
    }
    private void setTableFieldCombo(){
		Runnable fieldLoader = new Runnable() {
			public void run() {
				//clear
				for (int i = 0; i < tableFieldColumns.size(); i++) {
					ColumnInfo colInfo = (ColumnInfo) tableFieldColumns.get(i);
					colInfo.setComboValues(new String[] {});
				}
				if (!Const.isEmpty(wTable.getText())) {
					DatabaseMeta ci = transMeta.findDatabase(wConnection.getText());
					if (ci != null) {
						Database db = new Database(ci);
						try {
							db.connect();

							String schemaTable = ci	.getQuotedSchemaTableCombination(transMeta.environmentSubstitute(wSchema
											.getText()), transMeta.environmentSubstitute(wTable.getText()));
							RowMetaInterface r = db.getTableFields(schemaTable);
							if (null != r) {
								String[] fieldNames = r.getFieldNames();
								if (null != fieldNames) {
									for (int i = 0; i < tableFieldColumns.size(); i++) {
										ColumnInfo colInfo = (ColumnInfo) tableFieldColumns.get(i);
										colInfo.setComboValues(fieldNames);
									}
								}
							}
						} catch (Exception e) {
							for (int i = 0; i < tableFieldColumns.size(); i++) {
								ColumnInfo colInfo = (ColumnInfo) tableFieldColumns	.get(i);
								colInfo.setComboValues(new String[] {});
							}
							// ignore any errors here. drop downs will not be
							// filled, but no problem for the user
						}
					}
				}
			}
		};
		shell.getDisplay().asyncExec(fieldLoader);
	}
	/**
	 * Copy information from the meta-data input to the dialog fields.
	 */ 
	public void getData()
	{
		int i;
		if(log.isDebug()) log.logDebug(toString(), Messages.getString("UpdateDialog.Log.GettingKeyInfo")); //$NON-NLS-1$
		
		wCommit.setText(""+input.getCommitSize()); //$NON-NLS-1$
		wSkipLookup.setSelection(input.isSkipLookup());
        wErrorIgnored.setSelection( input.isErrorIgnored() );
        if (input.getIgnoreFlagField()!=null) wIgnoreFlagField.setText( input.getIgnoreFlagField() );
		
		if (input.getKeyStream()!=null)
		for (i=0;i<input.getKeyStream().length;i++)
		{
			TableItem item = wKey.table.getItem(i);
			if (input.getKeyLookup()[i]   !=null) item.setText(1, input.getKeyLookup()[i]);
			if (input.getKeyCondition()[i]!=null) item.setText(2, input.getKeyCondition()[i]);
			if (input.getKeyStream()[i]         !=null) item.setText(3, input.getKeyStream()[i]);
			if (input.getKeyStream2()[i]        !=null) item.setText(4, input.getKeyStream2()[i]);
		}
		
		if (input.getUpdateLookup()!=null)
		for (i=0;i<input.getUpdateLookup().length;i++)
		{
			TableItem item = wReturn.table.getItem(i);
			if (input.getUpdateLookup()[i]!=null     ) item.setText(1, input.getUpdateLookup()[i]);
			if (input.getUpdateStream()[i]!=null ) item.setText(2, input.getUpdateStream()[i]);
		}
		
        if (input.getSchemaName()!=null)       wSchema.setText( input.getSchemaName() );
		if (input.getTableName()!=null)        wTable.setText( input.getTableName() );
		if (input.getDatabaseMeta()!=null)   wConnection.setText(input.getDatabaseMeta().getName());
		else if (transMeta.nrDatabases()==1)
		{
			wConnection.setText( transMeta.getDatabase(0).getName() );
		}

		wStepname.selectAll();
		wKey.setRowNums();
		wKey.optWidth(true);
		wReturn.setRowNums();
		wReturn.optWidth(true);	
        
        setFlags();
	}
	
	private void cancel()
	{
		stepname=null;
		input.setChanged(changed);
		dispose();
	}

	private void getInfo(UpdateMeta inf)
	{
		//Table ktable = wKey.table;
		int nrkeys = wKey.nrNonEmpty();
		int nrfields = wReturn.nrNonEmpty();
		
		inf.allocate(nrkeys, nrfields);
				
		inf.setCommitSize( Const.toInt( wCommit.getText(), 0) );
		inf.setSkipLookup(wSkipLookup.getSelection() );
		
		if(log.isDebug()) log.logDebug(toString(), Messages.getString("UpdateDialog.Log.FoundKeys",nrkeys+"")); //$NON-NLS-1$ //$NON-NLS-2$
		for (int i=0;i<nrkeys;i++)
		{
			TableItem item = wKey.getNonEmpty(i);
			inf.getKeyLookup()[i]    = item.getText(1);
			inf.getKeyCondition()[i] = item.getText(2);
			inf.getKeyStream()[i]          = item.getText(3);
			inf.getKeyStream2()[i]         = item.getText(4);
		}

		//Table ftable = wReturn.table;

		log.logDebug(toString(), Messages.getString("UpdateDialog.Log.FoundFields",nrfields+"")); //$NON-NLS-1$ //$NON-NLS-2$
		for (int i=0;i<nrfields;i++)
		{
			TableItem item  = wReturn.getNonEmpty(i);
			inf.getUpdateLookup()[i]        = item.getText(1);
			inf.getUpdateStream()[i]    = item.getText(2);
		}
		
        inf.setSchemaName( wSchema.getText() ); 
		inf.setTableName( wTable.getText() ); 
		inf.setDatabaseMeta( transMeta.findDatabase(wConnection.getText()) );
        
        inf.setErrorIgnored( wErrorIgnored.getSelection());
        inf.setIgnoreFlagField( wIgnoreFlagField.getText());

		stepname = wStepname.getText(); // return value
	}


	
	private void ok()
	{
		if (Const.isEmpty(wStepname.getText())) return;

		// Get the information for the dialog into the input structure.
		getInfo(input);
		
		if (input.getDatabaseMeta()==null)
		{
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
			mb.setMessage(Messages.getString("UpdateDialog.InvalidConnection.DialogMessage")); //$NON-NLS-1$
			mb.setText(Messages.getString("UpdateDialog.InvalidConnection.DialogTitle")); //$NON-NLS-1$
			mb.open();
		}
		
		dispose();
	}

	private void getTableName()
	{
		DatabaseMeta inf=null;
		// New class: SelectTableDialog
		int connr = wConnection.getSelectionIndex();
		if (connr>=0) inf = transMeta.getDatabase(connr);
		
		if (inf!=null)
		{
			log.logDebug(toString(), Messages.getString("UpdateDialog.Log.LookingAtConnection")+inf.toString()); //$NON-NLS-1$
		
			DatabaseExplorerDialog std = new DatabaseExplorerDialog(shell, SWT.NONE, inf, transMeta.getDatabases());
            std.setSelectedSchema(wSchema.getText());
            std.setSelectedTable(wTable.getText());
            std.setSplitSchemaAndTable(true);
			if (std.open() != null)
			{
                wSchema.setText(Const.NVL(std.getSchemaName(), ""));
                wTable.setText(Const.NVL(std.getTableName(), ""));
			}
		}
		else
		{
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
			mb.setMessage(Messages.getString("UpdateDialog.InvalidConnection.DialogMessage")); //$NON-NLS-1$
			mb.setText(Messages.getString("UpdateDialog.InvalidConnection.DialogTitle")); //$NON-NLS-1$
			mb.open(); 
		}
	}

	private void get()
	{
		try
		{
			RowMetaInterface r = transMeta.getPrevStepFields(stepname);
			if (r!=null && !r.isEmpty())
			{
                TableItemInsertListener listener = new TableItemInsertListener()
                {
                    public boolean tableItemInserted(TableItem tableItem, ValueMetaInterface v)
                    {
                        tableItem.setText(2, "=");
                        return true;
                    }
                };
                BaseStepDialog.getFieldsFromPrevious(r, wKey, 1, new int[] { 1, 3}, new int[] {}, -1, -1, listener);
			}
		}
		catch(KettleException ke)
		{
			new ErrorDialog(shell, Messages.getString("UpdateDialog.FailedToGetFields.DialogTitle"), Messages.getString("UpdateDialog.FailedToGetFields.DialogMessage"), ke); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	private void getUpdate()
	{
		try
		{
			RowMetaInterface r = transMeta.getPrevStepFields(stepname);
			if (r!=null && !r.isEmpty())
			{
                BaseStepDialog.getFieldsFromPrevious(r, wReturn, 1, new int[] { 1, 2}, new int[] {}, -1, -1, null);
			}
		}
		catch(KettleException ke)
		{
			new ErrorDialog(shell, Messages.getString("UpdateDialog.FailedToGetFields.DialogTitle"), Messages.getString("UpdateDialog.FailedToGetFields.DialogMessage"), ke); //$NON-NLS-1$ //$NON-NLS-2$
		}

	}
	
	// Generate code for create table...
	// Conversions done by Database
	private void create()
	{
		try
		{
			UpdateMeta info = new UpdateMeta();
			getInfo(info);
	
			String name = stepname;  // new name might not yet be linked to other steps! 
			StepMeta stepinfo = new StepMeta(Messages.getString("UpdateDialog.StepMeta.Title"), name, info); //$NON-NLS-1$
			RowMetaInterface prev = transMeta.getPrevStepFields(stepname);
			
			SQLStatement sql = info.getSQLStatements(transMeta, stepinfo, prev);
			if (!sql.hasError())
			{
				if (sql.hasSQL())
				{
					SQLEditor sqledit = new SQLEditor(shell, SWT.NONE, info.getDatabaseMeta(), transMeta.getDbCache(), sql.getSQL());
					sqledit.open();
				}
				else
				{
					MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_INFORMATION );
					mb.setMessage(Messages.getString("UpdateDialog.NoSQLNeeds.DialogMessage")); //$NON-NLS-1$
					mb.setText(Messages.getString("UpdateDialog.NoSQLNeeds.DialogTitle")); //$NON-NLS-1$
					mb.open(); 
				}
			}
			else
			{
				MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
				mb.setMessage(sql.getError());
				mb.setText(Messages.getString("UpdateDialog.SQLError.DialogTitle")); //$NON-NLS-1$
				mb.open(); 
			}
		}
		catch(KettleException ke)
		{
			new ErrorDialog(shell, Messages.getString("UpdateDialog.CouldNotBuildSQL.DialogTitle"), Messages.getString("UpdateDialog.CouldNotBuildSQL.DialogMessage"), ke); //$NON-NLS-1$ //$NON-NLS-2$
		}

	}
	
	public String toString()
	{
		return this.getClass().getName();
	}
}
