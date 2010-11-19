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
 * Created on 18-mei-2003
 *
 */

package org.pentaho.di.ui.trans.steps.mappingoutput;

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
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.steps.mappingoutput.MappingOutputMeta;
import org.pentaho.di.trans.steps.mappingoutput.Messages;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

public class MappingOutputDialog extends BaseStepDialog implements StepDialogInterface
{
	/*
    private Label             wlFields;

    private TableView         wFields;

    private FormData          fdlFields, fdFields;
	*/
	
    private MappingOutputMeta input;

    public MappingOutputDialog(Shell parent, Object in, TransMeta tr, String sname)
    {
        super(parent, (BaseStepMeta) in, tr, sname);
        input = (MappingOutputMeta) in;
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

        FormLayout formLayout = new FormLayout();
        formLayout.marginWidth = Const.FORM_MARGIN;
        formLayout.marginHeight = Const.FORM_MARGIN;

        shell.setLayout(formLayout);
        shell.setText(Messages.getString("MappingOutputDialog.Shell.Title")); //$NON-NLS-1$

        int middle = props.getMiddlePct();
        int margin = Const.MARGIN;

        // Stepname line
        wlStepname = new Label(shell, SWT.RIGHT);
        wlStepname.setText(Messages.getString("MappingOutputDialog.Stepname.Label")); //$NON-NLS-1$
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

        /* NO LONGER NEEDED IN VERSION 3.x
        
        wlFields = new Label(shell, SWT.NONE);
        wlFields.setText(Messages.getString("MappingOutputDialog.Fields.Label")); //$NON-NLS-1$
        props.setLook(wlFields);
        fdlFields = new FormData();
        fdlFields.left = new FormAttachment(0, 0);
        fdlFields.top = new FormAttachment(wStepname, margin);
        wlFields.setLayoutData(fdlFields);

        final int FieldsRows = input.getFieldName().length;

        ColumnInfo[] colinf = new ColumnInfo[] 
        { 
            new ColumnInfo(Messages.getString("MappingOutputDialog.ColumnInfo.Name"), ColumnInfo.COLUMN_TYPE_TEXT, false), //$NON-NLS-1$
            new ColumnInfo(Messages.getString("MappingOutputDialog.ColumnInfo.Type"), ColumnInfo.COLUMN_TYPE_CCOMBO, ValueMeta.getTypes()), //$NON-NLS-1$
            new ColumnInfo(Messages.getString("MappingOutputDialog.ColumnInfo.Length"), ColumnInfo.COLUMN_TYPE_TEXT, false),  //$NON-NLS-1$
            new ColumnInfo(Messages.getString("MappingOutputDialog.ColumnInfo.Precision"), ColumnInfo.COLUMN_TYPE_TEXT, false), //$NON-NLS-1$
            new ColumnInfo(Messages.getString("MappingOutputDialog.ColumnInfo.Added"), ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] {"Y", "N"}, true)  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        };

        wFields = new TableView(transMeta, shell, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, colinf, FieldsRows, lsMod, props);

        fdFields = new FormData();
        fdFields.left = new FormAttachment(0, 0);
        fdFields.top = new FormAttachment(wlFields, margin);
        fdFields.right = new FormAttachment(100, 0);
        fdFields.bottom = new FormAttachment(100, -50);
        wFields.setLayoutData(fdFields);
		*/
        
        // Some buttons
        wOK = new Button(shell, SWT.PUSH);
        wOK.setText(Messages.getString("System.Button.OK")); //$NON-NLS-1$
        
        //wGet = new Button(shell, SWT.PUSH);
        //wGet.setText(Messages.getString("MappingOutputDialog.GetFields.Button")); //$NON-NLS-1$
        
        wCancel = new Button(shell, SWT.PUSH);
        wCancel.setText(Messages.getString("System.Button.Cancel")); //$NON-NLS-1$

        setButtonPositions(new Button[] { wOK, wCancel, }, margin, wStepname);

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
        /*
        lsGet = new Listener()
        {
            public void handleEvent(Event e)
            {
                get();
            }
        };
        */

        wCancel.addListener(SWT.Selection, lsCancel);
        /*wGet.addListener(SWT.Selection, lsGet);*/
        wOK.addListener(SWT.Selection, lsOK);

        lsDef = new SelectionAdapter()
        {
            public void widgetDefaultSelected(SelectionEvent e)
            {
                ok();
            }
        };

        wStepname.addSelectionListener(lsDef);

        // Detect X or ALT-F4 or something that kills this window...
        shell.addShellListener(new ShellAdapter()
        {
            public void shellClosed(ShellEvent e)
            {
                cancel();
            }
        });

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
    	/*
        for (int i=0;i<input.getFieldName().length;i++)
        {
            if (input.getFieldName()[i]!=null)
            {
                TableItem item = wFields.table.getItem(i);
                item.setText(1, input.getFieldName()[i]);
                String type   = ValueMeta.getTypeDesc(input.getFieldType()[i]);
                int length    = input.getFieldLength()[i];
                int prec      = input.getFieldPrecision()[i];
                if (type  !=null) item.setText(2, type  );
                if (length>=0   ) item.setText(3, ""+length); //$NON-NLS-1$
                if (prec>=0     ) item.setText(4, ""+prec  ); //$NON-NLS-1$
                item.setText(5, input.getFieldAdded()[i]?"Y":"N"); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
        
        wFields.setRowNums();
        wFields.optWidth(true);
        */
    	
        wStepname.selectAll();
    }

    private void cancel()
    {
        stepname = null;
        input.setChanged(changed);
        dispose();
    }

    private void ok()
    {
		if (Const.isEmpty(wStepname.getText())) return;

        stepname = wStepname.getText(); // return value
        
        /*
        int nrfields = wFields.nrNonEmpty();

        input.allocate(nrfields);

        for (int i=0;i<nrfields;i++)
        {
            TableItem item = wFields.getNonEmpty(i);
            input.getFieldName()[i]   = item.getText(1);
            input.getFieldType()[i]   = ValueMeta.getType(item.getText(2));
            String slength = item.getText(3);
            String sprec   = item.getText(4);
            
            input.getFieldLength()[i]    = Const.toInt(slength, -1); 
            input.getFieldPrecision()[i] = Const.toInt(sprec  , -1); 
            
            input.getFieldAdded()[i] = "Y".equalsIgnoreCase(item.getText(5)); //$NON-NLS-1$
        }
		*/
        
        dispose();
    }
    
    /*
    private void get()
    {
        try
        {
            RowMetaInterface r = transMeta.getPrevStepFields(stepname);
            if (r!=null && !r.isEmpty())
            {
                BaseStepDialog.getFieldsFromPrevious(r, wFields, 1, new int[] { 1 }, new int[] {2}, 3, 4, new TableItemInsertListener()
                    {
                        public boolean tableItemInserted(TableItem tableItem, ValueMetaInterface v)
                        {
                            tableItem.setText(5, "Y");
                            return true;
                        }
                    }
                );
            }
        }
        catch(KettleException ke)
        {
            new ErrorDialog(shell, Messages.getString("MappingOutputDialog.FailedToGetFields.DiaogTitle"), Messages.getString("MappingOutputDialog.FailedToGetFields.DiaogMessage"), ke); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }
    */
    
}