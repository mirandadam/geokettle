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

package org.pentaho.di.ui.trans.steps.stringcut;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.ui.trans.step.TableItemInsertListener;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.steps.stringcut.Messages;
import org.pentaho.di.trans.steps.stringcut.StringCutMeta;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.trans.step.StepMeta;


public class StringCutDialog extends BaseStepDialog implements StepDialogInterface {
	

	private Label wlKey;

	private TableView wFields;

	private FormData fdlKey, fdKey;

	private StringCutMeta input;
	
    private Map<String, Integer> inputFields;
    
    private ColumnInfo[] ciKey;

	public StringCutDialog(Shell parent, Object in, TransMeta tr, String sname) {
		super(parent, (BaseStepMeta) in, tr, sname);
		input = (StringCutMeta) in;
        inputFields =new HashMap<String, Integer>();
	}

	public String open() {
		Shell parent = getParent();
		Display display = parent.getDisplay();

		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX
				| SWT.MIN);
		props.setLook(shell);
		setShellImage(shell, input);

		ModifyListener lsMod = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				input.setChanged();
			}
		};
		changed = input.hasChanged();

		FormLayout formLayout = new FormLayout();
		formLayout.marginWidth = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText(Messages.getString("StringCutDialog.Shell.Title")); //$NON-NLS-1$

		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Stepname line
		wlStepname = new Label(shell, SWT.RIGHT);
		wlStepname.setText(Messages.getString("StringCutDialog.Stepname.Label")); //$NON-NLS-1$
		props.setLook(wlStepname);
		fdlStepname = new FormData();
		fdlStepname.left = new FormAttachment(0, 0);
		fdlStepname.right = new FormAttachment(middle, -margin);
		fdlStepname.top = new FormAttachment(0, margin);
		wlStepname.setLayoutData(fdlStepname);
		wStepname = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wStepname.setText(stepname);
		props.setLook(wStepname);
		wStepname.addModifyListener(lsMod);
		fdStepname = new FormData();
		fdStepname.left = new FormAttachment(middle, 0);
		fdStepname.top = new FormAttachment(0, margin);
		fdStepname.right = new FormAttachment(100, 0);
		wStepname.setLayoutData(fdStepname);
		

		wlKey = new Label(shell, SWT.NONE);
		wlKey.setText(Messages.getString("StringCutDialog.Fields.Label")); //$NON-NLS-1$
		props.setLook(wlKey);
		fdlKey = new FormData();
		fdlKey.left = new FormAttachment(0, 0);
		fdlKey.top = new FormAttachment(wStepname, 2*margin);
		wlKey.setLayoutData(fdlKey);
		
	   
		int nrFieldCols = 4;
		int nrFieldRows = (input.getFieldInStream() != null ? input.getFieldInStream().length : 1);

		ciKey = new ColumnInfo[nrFieldCols];
		ciKey[0] = new ColumnInfo(
				Messages.getString("StringCutDialog.ColumnInfo.InStreamField"), ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] { "" }, false); //$NON-NLS-1$
		ciKey[1] = new ColumnInfo(
				Messages.getString("StringCutDialog.ColumnInfo.OutStreamField"), ColumnInfo.COLUMN_TYPE_TEXT, false); //$NON-NLS-1$
		ciKey[2] = new ColumnInfo(
				Messages.getString("StringCutDialog.ColumnInfo.CutFrom"), ColumnInfo.COLUMN_TYPE_TEXT, false); //$NON-NLS-1$
		ciKey[3] = new ColumnInfo(
				Messages.getString("StringCutDialog.ColumnInfo.CutTo"), ColumnInfo.COLUMN_TYPE_TEXT, false); //$NON-NLS-1$

		
		
		ciKey[2].setUsingVariables(true);
		ciKey[1].setToolTip(Messages.getString("StringCutDialog.ColumnInfo.OutStreamField.Tooltip"));
		ciKey[3].setUsingVariables(true);

		
		wFields = new TableView(transMeta,shell, SWT.BORDER
				| SWT.FULL_SELECTION | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL,
				ciKey, nrFieldRows, lsMod, props);

		fdKey = new FormData();
		fdKey.left = new FormAttachment(0, 0);
		fdKey.top = new FormAttachment(wlKey, margin);
		fdKey.right = new FormAttachment(100, -margin);
		fdKey.bottom = new FormAttachment(100, -30);
		wFields.setLayoutData(fdKey);
		
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
                        	inputFields.put(row.getValueMeta(i).getName(), new Integer(i));
                        }
                        
                        setComboBoxes();
                    }
                    catch(KettleException e)
                    {
                    	log.logError(toString(), "It was not possible to get the fields from the previous step(s).");
                    }
                }
            }
        };
        new Thread(runnable).start();
      
		
		// THE BUTTONS
		wOK = new Button(shell, SWT.PUSH);
		wOK.setText(Messages.getString("System.Button.OK")); //$NON-NLS-1$
		wCancel = new Button(shell, SWT.PUSH);
		wCancel.setText(Messages.getString("System.Button.Cancel")); //$NON-NLS-1$

		wGet = new Button(shell, SWT.PUSH);
		wGet.setText(Messages.getString("StringCutDialog.GetFields.Button")); //$NON-NLS-1$
		fdGet = new FormData();
		fdGet.right = new FormAttachment(100, 0);
		fdGet.top = new FormAttachment(wStepname, 3*middle);
		wGet.setLayoutData(fdGet);
		
		setButtonPositions(new Button[] { wOK, wGet, wCancel }, margin, null);

		// Add listeners
		lsOK = new Listener() {
			public void handleEvent(Event e) {
				ok();
			}
		};
		lsGet = new Listener() {
			public void handleEvent(Event e) {
				get();
			}
		};
		lsCancel = new Listener() {
			public void handleEvent(Event e) {
				cancel();
			}
		};


		wOK.addListener(SWT.Selection, lsOK);
		wGet.addListener(SWT.Selection, lsGet);
		wCancel.addListener(SWT.Selection, lsCancel);

		lsDef = new SelectionAdapter() {
			public void widgetDefaultSelected(SelectionEvent e) {
				ok();
			}
		};

		wStepname.addSelectionListener(lsDef);

		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(new ShellAdapter() {
			public void shellClosed(ShellEvent e) {
				cancel();
			}
		});

		// Set the shell size, based upon previous time...
		setSize();

		getData();
		input.setChanged(changed);

		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		return stepname;
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
        
        String fieldNames[] = (String[]) entries.toArray(new String[entries.size()]);

        Const.sortStrings(fieldNames);
        ciKey[0].setComboValues(fieldNames);
    }
	/**
	 * Copy information from the meta-data input to the dialog fields.
	 */
	public void getData() {
		int i;
		if (input.getFieldInStream() != null) {
			for (i = 0; i < input.getFieldInStream().length; i++) {
				TableItem item = wFields.table.getItem(i);
				if (input.getFieldInStream()[i] != null) item.setText(1, input.getFieldInStream()[i]);
				if (input.getFieldOutStream()[i] != null) item.setText(2, input.getFieldOutStream()[i]);
				if (input.getCutFrom()[i] != null) item.setText(3, input.getCutFrom()[i]);
				if (input.getCutTo()[i] != null) item.setText(4, input.getCutTo()[i]);
			}
		}

		wStepname.selectAll();
		wFields.setRowNums();
		wFields.optWidth(true);
	}

	private void cancel() {
		stepname = null;
		input.setChanged(changed);
		dispose();
	}

	
	private void getInfo(StringCutMeta inf) {
		int nrkeys = wFields.nrNonEmpty();

		inf.allocate(nrkeys);
		if(log.isDebug())
			log.logDebug(toString(), Messages.getString("StringCutDialog.Log.FoundFields", String.valueOf(nrkeys))); //$NON-NLS-1$ //$NON-NLS-2$
		for (int i = 0; i < nrkeys; i++) {
			TableItem item = wFields.getNonEmpty(i);
			inf.getFieldInStream()[i] = item.getText(1);
			inf.getFieldOutStream()[i] = item.getText(2);
			inf.getCutFrom()[i] = item.getText(3);
			inf.getCutTo()[i] = item.getText(4);
		}

		stepname = wStepname.getText(); // return value
	}

	private void ok() {
		if (Const.isEmpty(wStepname.getText()))
			return;

		// Get the information for the dialog into the input structure.
		getInfo(input);

		dispose();
	}


	private void get() {
		try {
			RowMetaInterface r = transMeta.getPrevStepFields(stepname);
			if (r != null) {
				TableItemInsertListener listener = new TableItemInsertListener() {
					public boolean tableItemInserted(TableItem tableItem, ValueMetaInterface v) {
						if (v.getType() == ValueMeta.TYPE_STRING) {
							// Only process strings
							return true;
						} else {
							return false;
						}
					}
				};

				BaseStepDialog.getFieldsFromPrevious(r, wFields, 1, new int[] { 1 }, new int[] {}, -1, -1, listener);

			}
		} catch (KettleException ke) {
			new ErrorDialog(
					shell,Messages.getString("StringCutDialog.FailedToGetFields.DialogTitle"), Messages.getString("StringCutDialog.FailedToGetFields.DialogMessage"), ke); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	public String toString() {
		return this.getClass().getName();
	}
}