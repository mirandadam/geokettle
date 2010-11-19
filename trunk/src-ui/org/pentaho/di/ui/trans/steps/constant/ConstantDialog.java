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

package org.pentaho.di.ui.trans.steps.constant;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Point;
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
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.trans.steps.constant.ConstantMeta;
import org.pentaho.di.trans.steps.constant.Messages;


public class ConstantDialog extends BaseStepDialog implements StepDialogInterface
{
	private Label        wlFields;
	private TableView    wFields;
	private FormData     fdlFields, fdFields;

	private ConstantMeta input;

	public ConstantDialog(Shell parent, Object in, TransMeta transMeta, String sname)
	{
		super(parent, (BaseStepMeta)in, transMeta, sname);
		input=(ConstantMeta)in;
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
		shell.setText(Messages.getString("ConstantDialog.DialogTitle"));
		
		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Filename line
		wlStepname=new Label(shell, SWT.RIGHT);
		wlStepname.setText(Messages.getString("System.Label.StepName"));
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

		wlFields=new Label(shell, SWT.NONE);
		wlFields.setText(Messages.getString("ConstantDialog.Fields.Label"));
 		props.setLook(wlFields);
		fdlFields=new FormData();
		fdlFields.left = new FormAttachment(0, 0);
		fdlFields.top  = new FormAttachment(wStepname, margin);
		wlFields.setLayoutData(fdlFields);
		
		final int FieldsCols=9;
		final int FieldsRows=input.getFieldName().length;
		
		ColumnInfo[] colinf=new ColumnInfo[FieldsCols];
		colinf[0]=new ColumnInfo(Messages.getString("ConstantDialog.Name.Column"),       ColumnInfo.COLUMN_TYPE_TEXT,   false);
		colinf[1]=new ColumnInfo(Messages.getString("ConstantDialog.Type.Column"),       ColumnInfo.COLUMN_TYPE_CCOMBO, ValueMeta.getTypes() );
		colinf[2]=new ColumnInfo(Messages.getString("ConstantDialog.Format.Column"),     ColumnInfo.COLUMN_TYPE_CCOMBO,Const.getConversionFormats());
		colinf[3]=new ColumnInfo(Messages.getString("ConstantDialog.Length.Column"),     ColumnInfo.COLUMN_TYPE_TEXT,   false);
		colinf[4]=new ColumnInfo(Messages.getString("ConstantDialog.Precision.Column"),  ColumnInfo.COLUMN_TYPE_TEXT,   false);
		colinf[5]=new ColumnInfo(Messages.getString("ConstantDialog.Currency.Column"),   ColumnInfo.COLUMN_TYPE_TEXT,   false);
		colinf[6]=new ColumnInfo(Messages.getString("ConstantDialog.Decimal.Column"),    ColumnInfo.COLUMN_TYPE_TEXT,   false);
		colinf[7]=new ColumnInfo(Messages.getString("ConstantDialog.Group.Column"),      ColumnInfo.COLUMN_TYPE_TEXT,   false);
		colinf[8]=new ColumnInfo(Messages.getString("ConstantDialog.Value.Column"),      ColumnInfo.COLUMN_TYPE_TEXT,   false);
		
		wFields=new TableView(transMeta, shell, 
						      SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, 
						      colinf, 
						      FieldsRows,  
						      lsMod,
							  props
						      );

		fdFields=new FormData();
		fdFields.left  = new FormAttachment(0, 0);
		fdFields.top   = new FormAttachment(wlFields, margin);
		fdFields.right = new FormAttachment(100, 0);
		fdFields.bottom= new FormAttachment(100, -50);
		wFields.setLayoutData(fdFields);

		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(Messages.getString("System.Button.OK"));
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(Messages.getString("System.Button.Cancel"));
		
		setButtonPositions(new Button[] { wOK, wCancel }, margin, wFields);

		// Add listeners
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();     } };
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel(); } };
		
		wOK.addListener    (SWT.Selection, lsOK    );
		wCancel.addListener(SWT.Selection, lsCancel);
		
		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
		
		wStepname.addSelectionListener( lsDef );
		
		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );


		lsResize = new Listener() 
		{
			public void handleEvent(Event event) 
			{
				Point size = shell.getSize();
				wFields.setSize(size.x-10, size.y-50);
				wFields.table.setSize(size.x-10, size.y-50);
				wFields.redraw();
			}
		};
		shell.addListener(SWT.Resize, lsResize);
		
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
		int i;
		log.logDebug(toString(), "getting fields info...");

		for (i=0;i<input.getFieldName().length;i++)
		{
			if (input.getFieldName()[i]!=null)
			{
				TableItem item = wFields.table.getItem(i);
				item.setText(1, input.getFieldName()[i]);
				log.logDebug(toString(), "field #"+i+" --> fieldType[i]="+input.getFieldType()[i]);
				String type   = input.getFieldType()[i];
				String format = input.getFieldFormat()[i];
                String length = input.getFieldLength()[i]<0?"":(""+input.getFieldLength()[i]);
                String prec   = input.getFieldPrecision()[i]<0?"":(""+input.getFieldPrecision()[i]);;
				String curr   = input.getCurrency()[i];
				String group  = input.getGroup()[i];
				String decim  = input.getDecimal()[i];
				String def    = input.getValue()[i];
				if (type  !=null) item.setText(2, type  ); else item.setText(2, "");
				if (format!=null) item.setText(3, format); else item.setText(3, "");
				if (length!=null) item.setText(4, length); else item.setText(4, "");
				if (prec  !=null) item.setText(5, prec  ); else item.setText(5, "");
				if (curr  !=null) item.setText(6, curr  ); else item.setText(6, "");
				if (decim !=null) item.setText(7, decim ); else item.setText(7, "");
				if (group !=null) item.setText(8, group ); else item.setText(8, "");
				if (def   !=null) item.setText(9, def   ); else item.setText(9, "");
			}
		}
        
        wFields.setRowNums();
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
        
		int i;
		//Table table = wFields.table;
		
		int nrfields = wFields.nrNonEmpty();

		input.allocate(nrfields);

		for (i=0;i<nrfields;i++)
		{
			TableItem item = wFields.getNonEmpty(i);
			input.getFieldName()[i]   = item.getText(1);
			input.getFieldType()[i]   = item.getText(2);
			input.getFieldFormat()[i] = item.getText(3);
			String slength = item.getText(4);
			String sprec   = item.getText(5);
			input.getCurrency()[i] = item.getText(6);
			input.getDecimal()[i]  = item.getText(7);
			input.getGroup()[i]    = item.getText(8);
			input.getValue()[i]        = item.getText(9);
			
			try { input.getFieldLength()[i]    = Integer.parseInt(slength); } 
			  catch(Exception e) { input.getFieldLength()[i]    = -1; }
			try { input.getFieldPrecision()[i] = Integer.parseInt(sprec  ); } 
			  catch(Exception e) { input.getFieldPrecision()[i] = -1; }

		}
		
		dispose();
	}
	
	public String toString()
	{
		return this.getClass().getName();
	}

}
