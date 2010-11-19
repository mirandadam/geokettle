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

package org.pentaho.di.ui.trans.steps.rowgenerator;

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
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.TransPreviewFactory;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.steps.rowgenerator.Messages;
import org.pentaho.di.trans.steps.rowgenerator.RowGeneratorMeta;
import org.pentaho.di.ui.core.dialog.EnterNumberDialog;
import org.pentaho.di.ui.core.dialog.EnterTextDialog;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.dialog.PreviewRowsDialog;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.dialog.TransPreviewProgressDialog;
import org.pentaho.di.ui.trans.step.BaseStepDialog;



public class RowGeneratorDialog extends BaseStepDialog implements StepDialogInterface
{
	private Label        wlLimit;
	private TextVar      wLimit;
	private FormData     fdlLimit, fdLimit;
	
	private Label        wlFields;
	private TableView    wFields;
	private FormData     fdlFields, fdFields;

	private RowGeneratorMeta input;

	public RowGeneratorDialog(Shell parent, Object in, TransMeta transMeta, String sname)
	{
		super(parent, (BaseStepMeta)in, transMeta, sname);
		input=(RowGeneratorMeta)in;
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
		shell.setText(Messages.getString("RowGeneratorDialog.DialogTitle"));
		
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

		wlLimit=new Label(shell, SWT.RIGHT);
		wlLimit.setText(Messages.getString("RowGeneratorDialog.Limit.Label"));
 		props.setLook(wlLimit);
		fdlLimit=new FormData();
		fdlLimit.left = new FormAttachment(0, 0);
		fdlLimit.right= new FormAttachment(middle, -margin);
		fdlLimit.top  = new FormAttachment(wStepname, margin);
		wlLimit.setLayoutData(fdlLimit);
		wLimit=new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wLimit);
		wLimit.addModifyListener(lsMod);
		fdLimit=new FormData();
		fdLimit.left = new FormAttachment(middle, 0);
		fdLimit.top  = new FormAttachment(wStepname, margin);
		fdLimit.right= new FormAttachment(100, 0);
		wLimit.setLayoutData(fdLimit);

		wlFields=new Label(shell, SWT.NONE);
		wlFields.setText(Messages.getString("RowGeneratorDialog.Fields.Label"));
 		props.setLook(wlFields);
		fdlFields=new FormData();
		fdlFields.left = new FormAttachment(0, 0);
		fdlFields.top  = new FormAttachment(wLimit, margin);
		wlFields.setLayoutData(fdlFields);
		
		final int FieldsRows=input.getFieldName().length;
		
		ColumnInfo[] colinf=new ColumnInfo[] { 
		 new ColumnInfo(Messages.getString("System.Column.Name"),       ColumnInfo.COLUMN_TYPE_TEXT,   false),
		 new ColumnInfo(Messages.getString("System.Column.Type"),       ColumnInfo.COLUMN_TYPE_CCOMBO, ValueMeta.getTypes() ),
		 new ColumnInfo(Messages.getString("System.Column.Format"),     ColumnInfo.COLUMN_TYPE_CCOMBO, Const.getConversionFormats()),
		 new ColumnInfo(Messages.getString("System.Column.Length"),     ColumnInfo.COLUMN_TYPE_TEXT,   false),
		 new ColumnInfo(Messages.getString("System.Column.Precision"),  ColumnInfo.COLUMN_TYPE_TEXT,   false),
		 new ColumnInfo(Messages.getString("System.Column.Currency"),   ColumnInfo.COLUMN_TYPE_TEXT,   false),
		 new ColumnInfo(Messages.getString("System.Column.Decimal"),    ColumnInfo.COLUMN_TYPE_TEXT,   false),
		 new ColumnInfo(Messages.getString("System.Column.Group"),      ColumnInfo.COLUMN_TYPE_TEXT,   false),
		 new ColumnInfo(Messages.getString("System.Column.Value"),      ColumnInfo.COLUMN_TYPE_TEXT,   false),
		};
		
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
        wPreview=new Button(shell, SWT.PUSH);
        wPreview.setText(Messages.getString("System.Button.Preview"));
        wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(Messages.getString("System.Button.Cancel"));
		
        setButtonPositions(new Button[] { wOK, wCancel , wPreview }, margin, null);
        
		// Add listeners
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();     } };
        lsPreview  = new Listener() { public void handleEvent(Event e) { preview(); } };
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel(); } };
		
		wOK.addListener      (SWT.Selection, lsOK    );
        wPreview.addListener (SWT.Selection, lsPreview);
		wCancel.addListener  (SWT.Selection, lsCancel);
		
		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
		
		wStepname.addSelectionListener( lsDef );
		wLimit.addSelectionListener(lsDef);
		
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
		log.logDebug(toString(), "getting fields info...");
		
		wLimit.setText(input.getRowLimit());

		for (int i=0;i<input.getFieldName().length;i++)
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
        try
        {
            getInfo(new RowGeneratorMeta()); // to see if there is an exception
            getInfo(input);                  // to put the content on the input structure for real if all is well.
            dispose();
        }
        catch(KettleException e)
        {
            new ErrorDialog(shell, Messages.getString("RowGeneratorDialog.Illegal.Dialog.Settings.Title"), Messages.getString("RowGeneratorDialog.Illegal.Dialog.Settings.Message"), e);
        }
	}
	
	private void getInfo(RowGeneratorMeta meta) throws KettleException
    {
        meta.setRowLimit( wLimit.getText() );
        
        int nrfields = wFields.nrNonEmpty();

        meta.allocate(nrfields);

        for (int i=0;i<nrfields;i++)
        {
            TableItem item = wFields.getNonEmpty(i);
            
            meta.getFieldName()[i]   = item.getText(1);
            meta.getFieldType()[i]   = item.getText(2);
            meta.getFieldFormat()[i] = item.getText(3);
            String slength           = item.getText(4);
            String sprec             = item.getText(5);
            meta.getCurrency()[i]    = item.getText(6);
            meta.getDecimal()[i]     = item.getText(7);
            meta.getGroup()[i]       = item.getText(8);
            meta.getValue()[i]       = item.getText(9);
            
            meta.getFieldLength()[i]    = Const.toInt( slength, -1);
            meta.getFieldPrecision()[i] = Const.toInt( sprec  , -1);
        }
        
        // Performs checks...
        /*
         * Commented out verification : if variables are used, this check is a pain!
         * 
        long longLimit = Const.toLong(transMeta.environmentSubstitute( wLimit.getText()), -1L );
        if (longLimit<0)
        {
            throw new KettleException( Messages.getString("RowGeneratorDialog.Wrong.RowLimit.Number") );
        }
        */
    }

    public String toString()
	{
		return this.getClass().getName();
	}

    /**
     * Preview the data generated by this step.
     * This generates a transformation using this step & a dummy and previews it.
     *
     */
    private void preview()
    {
        RowGeneratorMeta oneMeta = new RowGeneratorMeta();
        try
        {
            getInfo(oneMeta);
        }
        catch(KettleException e)
        {
            new ErrorDialog(shell, Messages.getString("RowGeneratorDialog.Illegal.Dialog.Settings.Title"), Messages.getString("RowGeneratorDialog.Illegal.Dialog.Settings.Message"), e);
            return;
        }
        
        TransMeta previewMeta = TransPreviewFactory.generatePreviewTransformation(transMeta, oneMeta, wStepname.getText());
        
        EnterNumberDialog numberDialog = new EnterNumberDialog(shell, props.getDefaultPreviewSize(), Messages.getString("System.Dialog.EnterPreviewSize.Title"), Messages.getString("System.Dialog.EnterPreviewSize.Message"));
        int previewSize = numberDialog.open();
        if (previewSize>0)
        {
            TransPreviewProgressDialog progressDialog = new TransPreviewProgressDialog(shell, previewMeta, new String[] { wStepname.getText() }, new int[] { previewSize } );
            progressDialog.open();

            Trans trans = progressDialog.getTrans();
            String loggingText = progressDialog.getLoggingText();

            if (!progressDialog.isCancelled())
            {
                if (trans.getResult()!=null && trans.getResult().getNrErrors()>0)
                {
                	EnterTextDialog etd = new EnterTextDialog(shell, Messages.getString("System.Dialog.PreviewError.Title"),  
                			Messages.getString("System.Dialog.PreviewError.Message"), loggingText, true );
                	etd.setReadOnly();
                	etd.open();
                }
            }
            
            PreviewRowsDialog prd =new PreviewRowsDialog(shell, transMeta, SWT.NONE, wStepname.getText(), progressDialog.getPreviewRowsMeta(wStepname.getText()), progressDialog.getPreviewRows(wStepname.getText()), loggingText);
            prd.open();
        }
    }
}