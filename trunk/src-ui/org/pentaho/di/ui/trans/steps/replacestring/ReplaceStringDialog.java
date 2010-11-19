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

package org.pentaho.di.ui.trans.steps.replacestring;
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
import org.pentaho.di.trans.steps.replacestring.Messages;
import org.pentaho.di.trans.steps.replacestring.ReplaceStringMeta;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.trans.step.StepMeta;


/**
 * Search and replace in string.
 * 
 * @author Samatar Hassan
 * @since 28 September 2007
 */
public class ReplaceStringDialog extends BaseStepDialog implements StepDialogInterface {
	

	private Label wlKey;

	private TableView wFields;

	private FormData fdlKey, fdKey;

	private ReplaceStringMeta input;
	
    private Map<String, Integer> inputFields;
    
    private ColumnInfo[] ciKey;

	public ReplaceStringDialog(Shell parent, Object in, TransMeta tr, String sname) {
		super(parent, (BaseStepMeta) in, tr, sname);
		input = (ReplaceStringMeta) in;
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
		shell.setText(Messages.getString("ReplaceStringDialog.Shell.Title")); //$NON-NLS-1$

		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Stepname line
		wlStepname = new Label(shell, SWT.RIGHT);
		wlStepname.setText(Messages.getString("ReplaceStringDialog.Stepname.Label")); //$NON-NLS-1$
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
		wlKey.setText(Messages.getString("ReplaceStringDialog.Fields.Label")); //$NON-NLS-1$
		props.setLook(wlKey);
		fdlKey = new FormData();
		fdlKey.left = new FormAttachment(0, 0);
		fdlKey.top = new FormAttachment(wStepname, 2*margin);
		wlKey.setLayoutData(fdlKey);
		
	   
		int nrFieldCols = 7;
		int nrFieldRows = (input.getFieldInStream() != null ? input.getFieldInStream().length : 1);

		ciKey = new ColumnInfo[nrFieldCols];
		ciKey[0] = new ColumnInfo(Messages.getString("ReplaceStringDialog.ColumnInfo.InStreamField"), ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] { "" }, false); //$NON-NLS-1$
		ciKey[1] = new ColumnInfo(Messages.getString("ReplaceStringDialog.ColumnInfo.OutStreamField"), ColumnInfo.COLUMN_TYPE_TEXT, false); //$NON-NLS-1$
		ciKey[2] =  new ColumnInfo(Messages.getString("ReplaceStringDialog.ColumnInfo.useRegEx"), ColumnInfo.COLUMN_TYPE_CCOMBO,  ReplaceStringMeta.useRegExDesc);
		ciKey[3] = new ColumnInfo(Messages.getString("ReplaceStringDialog.ColumnInfo.Replace"), ColumnInfo.COLUMN_TYPE_TEXT, false); //$NON-NLS-1$
		ciKey[4] = new ColumnInfo(Messages.getString("ReplaceStringDialog.ColumnInfo.By"), ColumnInfo.COLUMN_TYPE_TEXT, false); //$NON-NLS-1$
		ciKey[5] =  new ColumnInfo(Messages.getString("ReplaceStringDialog.ColumnInfo.WholeWord"), ColumnInfo.COLUMN_TYPE_CCOMBO,  ReplaceStringMeta.wholeWordDesc);
		ciKey[6] =  new ColumnInfo(Messages.getString("ReplaceStringDialog.ColumnInfo.CaseSensitive"), ColumnInfo.COLUMN_TYPE_CCOMBO,  ReplaceStringMeta.caseSensitiveDesc);

		ciKey[1].setToolTip(Messages.getString("ReplaceStringDialog.ColumnInfo.OutStreamField.Tooltip"));
		ciKey[1].setUsingVariables(true);
		ciKey[3].setUsingVariables(true);
		ciKey[4].setUsingVariables(true);
		
		wFields = new TableView(transMeta, shell, SWT.BORDER
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
                    	log.logError(toString(), Messages.getString("ReplaceString.Error.CanNotGetFields"));
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
		wGet.setText(Messages.getString("ReplaceStringDialog.GetFields.Button")); //$NON-NLS-1$
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
				item.setText(3, ReplaceStringMeta.getUseRegExDesc(input.getUseRegEx()[i]));
				if (input.getReplaceString()[i] != null) item.setText(4, input.getReplaceString()[i]);
				if (input.getReplaceByString()[i] != null) item.setText(5, input.getReplaceByString()[i]);
				item.setText(6, ReplaceStringMeta.getWholeWordDesc(input.getWholeWord()[i]));
				item.setText(7, ReplaceStringMeta.getCaseSensitiveDesc(input.getCaseSensitive()[i]));
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

	
	private void getInfo(ReplaceStringMeta inf) {

		int nrkeys = wFields.nrNonEmpty();

		inf.allocate(nrkeys);
		if(log.isDebug())
			log.logDebug(toString(), Messages.getString("ReplaceStringDialog.Log.FoundFields", String.valueOf(nrkeys))); //$NON-NLS-1$ //$NON-NLS-2$
		for (int i = 0; i < nrkeys; i++) {
			TableItem item = wFields.getNonEmpty(i);
			inf.getFieldInStream()[i] = item.getText(1);
			inf.getFieldOutStream()[i] = item.getText(2);
			inf.getUseRegEx()[i] =ReplaceStringMeta.getUseRegExByDesc(item.getText(3));
			inf.getReplaceString()[i] = item.getText(4);
			inf.getReplaceByString()[i] = item.getText(5);
			inf.getWholeWord()[i] = ReplaceStringMeta.getWholeWordByDesc(item.getText(6));
			inf.getCaseSensitive()[i] = ReplaceStringMeta.getCaseSensitiveByDesc(item.getText(7));
		}

		stepname = wStepname.getText(); // return value
	}

	private void ok() {
		if (Const.isEmpty(wStepname.getText()))	return;

		// Get the information for the dialog into the input structure.
		getInfo(input);

		dispose();
	}


	private void get() {
		try {
			RowMetaInterface r = transMeta.getPrevStepFields(stepname);
			if (r != null) {
				TableItemInsertListener listener = new TableItemInsertListener()  {
					public boolean tableItemInserted(TableItem tableItem, ValueMetaInterface v) {
						if (v.getType() == ValueMeta.TYPE_STRING) {
							// Only process strings
							tableItem.setText(3, Messages.getString("System.Combo.No"));
							tableItem.setText(6, Messages.getString("System.Combo.No"));
							tableItem.setText(7, Messages.getString("System.Combo.No"));
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
					shell,Messages.getString("ReplaceStringDialog.FailedToGetFields.DialogTitle"), Messages.getString("ReplaceStringDialog.FailedToGetFields.DialogMessage"), ke); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
}