/*
 * Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Samatar Hassan.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
*/

package org.pentaho.di.ui.trans.steps.setvalueconstant;

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
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.setvalueconstant.SetValueConstantMeta;
import org.pentaho.di.trans.steps.setvalueconstant.Messages;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.trans.step.TableItemInsertListener;


public class SetValueConstantDialog extends BaseStepDialog implements StepDialogInterface
{
	
	private SetValueConstantMeta input;
	
	private ModifyListener lsMod ;
	private ModifyListener oldlsMod ;
	private int middle;
	private int margin;
	
	/**
	 * all fields from the previous steps
	 */
    private Map<String, Integer> inputFields;


	private Label        wlFields;
	private TableView    wFields;
	private FormData     fdlFields, fdFields;

	private ColumnInfo[] colinf;
	
	private Label        wluseVars;
	private Button       wuseVars;
	
	public SetValueConstantDialog(Shell parent, Object in, TransMeta tr, String sname)
	{
		super(parent, (BaseStepMeta)in, tr, sname);
		input=(SetValueConstantMeta)in;
        inputFields =new HashMap<String, Integer>();
	}

	public String open()
	{
		Shell parent = getParent();
		Display display = parent.getDisplay();

		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MIN | SWT.MAX);
 		props.setLook(shell);
 		setShellImage(shell, input);
        
		lsMod = new ModifyListener() 
		{
			public void modifyText(ModifyEvent e) 
			{
				input.setChanged();
			}
		};
		changed = input.hasChanged();
		oldlsMod=lsMod;
		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;
		
		middle = props.getMiddlePct();
		margin = Const.MARGIN;

		shell.setLayout(formLayout);
		shell.setText(Messages.getString("SetValueConstantDialog.Shell.Title")); //$NON-NLS-1$

		// Stepname line
		wlStepname=new Label(shell, SWT.RIGHT);
		wlStepname.setText(Messages.getString("SetValueConstantDialog.Stepname.Label")); //$NON-NLS-1$
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
		
		 // Use variable?
        wluseVars=new Label(shell, SWT.RIGHT);
        wluseVars.setText(Messages.getString("SetValueConstantDialog.useVars.Label")); //$NON-NLS-1$
        props.setLook(wluseVars);
        FormData fdlUpdate=new FormData();
        fdlUpdate.left   = new FormAttachment(0, 0);
        fdlUpdate.right  = new FormAttachment(middle, -margin);
        fdlUpdate.top    = new FormAttachment(wStepname, 2*margin);
        wluseVars.setLayoutData(fdlUpdate);
        wuseVars=new Button(shell, SWT.CHECK);
        wuseVars.setToolTipText(Messages.getString("SetValueConstantDialog.useVars.Tooltip")); //$NON-NLS-1$
        props.setLook(wuseVars);
        FormData fdUpdate=new FormData();
        fdUpdate.left = new FormAttachment(middle, 0);
        fdUpdate.top  = new FormAttachment(wStepname, 2*margin);
        fdUpdate.right= new FormAttachment(100, 0);
        wuseVars.setLayoutData(fdUpdate);
        
   
		
        // Table with fields
		wlFields=new Label(shell, SWT.NONE);
		wlFields.setText(Messages.getString("SetValueConstantDialog.Fields.Label"));
 		props.setLook(wlFields);
		fdlFields=new FormData();
		fdlFields.left = new FormAttachment(0, 0);
		fdlFields.top  = new FormAttachment(wuseVars, margin);
		wlFields.setLayoutData(fdlFields);
		
		int FieldsCols=3;
		final int FieldsRows=input.getFieldName().length;
		colinf=new ColumnInfo[FieldsCols];
		colinf[0]=new ColumnInfo(Messages.getString("SetValueConstantDialog.Fieldname.Column"),  ColumnInfo.COLUMN_TYPE_CCOMBO, new String[]{},false);
		colinf[1]=new ColumnInfo(Messages.getString("SetValueConstantDialog.Value.Column"), ColumnInfo.COLUMN_TYPE_TEXT , false);
		colinf[2]=new ColumnInfo(Messages.getString("SetValueConstantDialog.Value.ConversionMask"), ColumnInfo.COLUMN_TYPE_CCOMBO, Const.getDateFormats());
		
		wFields=new TableView(transMeta,shell, 
				  SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, 
				  colinf, 
				  FieldsRows,  
				  oldlsMod,
				  props
				  );

		fdFields=new FormData();
		fdFields.left  = new FormAttachment(0, 0);
		fdFields.top   = new FormAttachment(wlFields, margin);
		fdFields.right = new FormAttachment(100, 0);
		fdFields.bottom= new FormAttachment(100, -50);
	
		wFields.setLayoutData(fdFields);
		
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
                    	log.logError(toString(), Messages.getString("System.Dialog.GetFieldsFailed.Message"));
                    }
                }
            }
        };
        new Thread(runnable).start();
      

		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(Messages.getString("System.Button.OK"));
		wGet=new Button(shell, SWT.PUSH);
		wGet.setText(Messages.getString("System.Button.GetFields"));
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(Messages.getString("System.Button.Cancel"));
		
		
		setButtonPositions(new Button[] { wOK, wGet, wCancel }, margin, wFields);

		// Add listeners
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel(); } };
		lsGet      = new Listener() { public void handleEvent(Event e) { get();    } };
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();     } };
		
		
		wCancel.addListener(SWT.Selection, lsCancel);
		wOK.addListener    (SWT.Selection, lsOK    );
		wGet.addListener   (SWT.Selection, lsGet   );
		
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
	protected void setComboBoxes()
    {
        // Something was changed in the row.
        //
		final Map<String, Integer> fields = new HashMap<String, Integer>();
        
        // Add the currentMeta fields...
        fields.putAll(inputFields);
        
        Set<String> keySet = fields.keySet();
        List<String> entries = new ArrayList<String>(keySet);
        
        String[] fieldNames = (String[]) entries.toArray(new String[entries.size()]);

        Const.sortStrings(fieldNames);
        colinf[0].setComboValues(fieldNames);
    }
	private void get()
	{
		try
		{
			RowMetaInterface r = transMeta.getPrevStepFields(stepMeta);
			if (r!=null)
			{
				TableItemInsertListener insertListener = new TableItemInsertListener()  
                    {   
                    	public boolean tableItemInserted(TableItem tableItem, ValueMetaInterface v) 
                        { 
                            return true;
                        } 
                    };
                    
                BaseStepDialog.getFieldsFromPrevious(r, wFields, 1, new int[] { 1 }, new int[] {}, -1, -1, insertListener);
			}
		}
		catch(KettleException ke)
		{
			new ErrorDialog(shell, Messages.getString("System.Dialog.GetFieldsFailed.Title"), Messages.getString("System.Dialog.GetFieldsFailed.Message"), ke);
		}
	}
	
	
 
	/**
	 * Copy information from the meta-data input to the dialog fields.
	 */ 
	public void getData()
	{
		wuseVars.setSelection(input.isUseVars());
		Table table = wFields.table;
		if (input.getFieldName().length>0) table.removeAll();
		for (int i=0;i<input.getFieldName().length;i++)
		{
			TableItem ti = new TableItem(table, SWT.NONE);
			ti.setText(0, ""+(i+1));
			if(input.getFieldName()[i]!=null)    ti.setText(1, input.getFieldName()[i]);
			if(input.getReplaceValue()[i]!=null) ti.setText(2, input.getReplaceValue()[i]);
			if(input.getReplaceMask()[i]!=null)  ti.setText(3, input.getReplaceMask()[i]);
		}

        wFields.setRowNums();
        wFields.removeEmptyRows();
		wFields.optWidth(true);
		
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

		input.setUseVars(wuseVars.getSelection());
		int count = wFields.nrNonEmpty();
		input.allocate(count);
		
		for (int i=0;i<count;i++)
		{
			TableItem ti = wFields.getNonEmpty(i);
			input.getFieldName()[i] = ti.getText(1);
			input.getReplaceValue()[i] = ti.getText(2);
			input.getReplaceMask()[i] = ti.getText(3);
		}
		
		dispose();
	}
}
