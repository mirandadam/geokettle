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
 package org.pentaho.di.ui.trans.steps.denormaliser;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Cursor;
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
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.trans.steps.denormaliser.DenormaliserMeta;
import org.pentaho.di.trans.steps.denormaliser.DenormaliserTargetField;
import org.pentaho.di.trans.steps.denormaliser.Messages;

public class DenormaliserDialog extends BaseStepDialog implements StepDialogInterface
{
    public static final String STRING_SORT_WARNING_PARAMETER = "PivotSortWarning"; //$NON-NLS-1$
    
	private Label        wlGroup;
	private TableView    wGroup;
	private FormData     fdlGroup, fdGroup;

	private Label        wlTarget;
	private TableView    wTarget;
	private FormData     fdlTarget, fdTarget;

    private Label        wlKeyField;
   	private CCombo       wKeyField;
    private FormData     fdlKeyField, fdKeyField;

	private Button wGet, wGetAgg;
	private FormData fdGet, fdGetAgg;
	private Listener lsGet, lsGetAgg;

	private DenormaliserMeta input;

	private boolean gotPreviousFields=false;
	
	public DenormaliserDialog(Shell parent, Object in, TransMeta transMeta, String sname)
	{
		super(parent, (BaseStepMeta)in, transMeta, sname);
		input=(DenormaliserMeta)in;
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
		backupChanged = input.hasChanged();

		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText(Messages.getString("DenormaliserDialog.Shell.Title")); //$NON-NLS-1$
		
		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;
		
		// Stepname line
		wlStepname=new Label(shell, SWT.RIGHT);
		wlStepname.setText(Messages.getString("DenormaliserDialog.Stepname.Label")); //$NON-NLS-1$
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


        // Key field...
        wlKeyField=new Label(shell, SWT.RIGHT);
        wlKeyField.setText(Messages.getString("DenormaliserDialog.KeyField.Label")); //$NON-NLS-1$
        props.setLook(wlKeyField);
        fdlKeyField=new FormData();
        fdlKeyField.left = new FormAttachment(0, 0);
        fdlKeyField.right= new FormAttachment(middle, -margin);
        fdlKeyField.top  = new FormAttachment(wStepname, margin);
        wlKeyField.setLayoutData(fdlKeyField);

        wKeyField=new CCombo(shell, SWT.BORDER | SWT.READ_ONLY);
        props.setLook(wKeyField);
        wKeyField.addModifyListener(lsMod);
        fdKeyField=new FormData();
        fdKeyField.left  = new FormAttachment(middle, 0);
        fdKeyField.top   = new FormAttachment(wStepname, margin);
        fdKeyField.right = new FormAttachment(100, 0);
        wKeyField.setLayoutData(fdKeyField);
        wKeyField.addFocusListener(new FocusListener()
        {
            public void focusLost(org.eclipse.swt.events.FocusEvent e)
            {
            }
        
            public void focusGained(org.eclipse.swt.events.FocusEvent e)
            {
                Cursor busy = new Cursor(shell.getDisplay(), SWT.CURSOR_WAIT);
                shell.setCursor(busy);
                getPreviousFieldNames();
                shell.setCursor(null);
                busy.dispose();
            }
        }
    );

		wlGroup=new Label(shell, SWT.NONE);
		wlGroup.setText(Messages.getString("DenormaliserDialog.Group.Label")); //$NON-NLS-1$
 		props.setLook(wlGroup);
		fdlGroup=new FormData();
		fdlGroup.left  = new FormAttachment(0, 0);
		fdlGroup.top   = new FormAttachment(wKeyField, margin);
		wlGroup.setLayoutData(fdlGroup);

		int nrKeyCols=1;
		int nrKeyRows=(input.getGroupField()!=null?input.getGroupField().length:1);
		
		ColumnInfo[] ciKey=new ColumnInfo[nrKeyCols];
		ciKey[0]=new ColumnInfo(Messages.getString("DenormaliserDialog.ColumnInfo.GroupField"),  ColumnInfo.COLUMN_TYPE_TEXT,   false); //$NON-NLS-1$
		
		wGroup=new TableView(transMeta, shell, 
						      SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL, 
						      ciKey, 
						      nrKeyRows,  
						      lsMod,
							  props
						      );

		wGet=new Button(shell, SWT.PUSH);
		wGet.setText(Messages.getString("DenormaliserDialog.GetFields.Button")); //$NON-NLS-1$
		fdGet = new FormData();
		fdGet.top   = new FormAttachment(wlGroup, margin);
		fdGet.right = new FormAttachment(100, 0);
		wGet.setLayoutData(fdGet);
		
		fdGroup=new FormData();
		fdGroup.left  = new FormAttachment(0, 0);
		fdGroup.top   = new FormAttachment(wlGroup, margin);
		fdGroup.right = new FormAttachment(wGet, -margin);
		fdGroup.bottom= new FormAttachment(30, 0);
		wGroup.setLayoutData(fdGroup);

		// THE unpivot target field fields
		wlTarget=new Label(shell, SWT.NONE);
		wlTarget.setText(Messages.getString("DenormaliserDialog.Target.Label")); //$NON-NLS-1$
 		props.setLook(wlTarget);
		fdlTarget=new FormData();
		fdlTarget.left  = new FormAttachment(0, 0);
		fdlTarget.top   = new FormAttachment(wGroup, margin);
		wlTarget.setLayoutData(fdlTarget);
		
		int UpInsRows= (input.getDenormaliserTargetField()!=null?input.getDenormaliserTargetField().length:1);
		
        String formats[] = Const.getConversionFormats();
        
		ColumnInfo[] ciTarget=new ColumnInfo[]
          {
    		 new ColumnInfo(Messages.getString("DenormaliserDialog.ColumnInfo.TargetFieldname"),     ColumnInfo.COLUMN_TYPE_TEXT,   false), //$NON-NLS-1$
    		 new ColumnInfo(Messages.getString("DenormaliserDialog.ColumnInfo.ValueFieldname"),      ColumnInfo.COLUMN_TYPE_TEXT,   false), //$NON-NLS-1$
    		 new ColumnInfo(Messages.getString("DenormaliserDialog.ColumnInfo.Keyvalue"),            ColumnInfo.COLUMN_TYPE_TEXT,   false), //$NON-NLS-1$
             new ColumnInfo(Messages.getString("DenormaliserDialog.ColumnInfo.Type"),                 ColumnInfo.COLUMN_TYPE_CCOMBO, ValueMeta.getAllTypes(), false), //$NON-NLS-1$
             new ColumnInfo(Messages.getString("DenormaliserDialog.ColumnInfo.Format"),               ColumnInfo.COLUMN_TYPE_CCOMBO,  formats), //$NON-NLS-1$
             new ColumnInfo(Messages.getString("DenormaliserDialog.ColumnInfo.Length"),               ColumnInfo.COLUMN_TYPE_TEXT,    false), //$NON-NLS-1$
             new ColumnInfo(Messages.getString("DenormaliserDialog.ColumnInfo.Precision"),            ColumnInfo.COLUMN_TYPE_TEXT,    false), //$NON-NLS-1$
             new ColumnInfo(Messages.getString("DenormaliserDialog.ColumnInfo.Currency"),             ColumnInfo.COLUMN_TYPE_TEXT,    false), //$NON-NLS-1$
             new ColumnInfo(Messages.getString("DenormaliserDialog.ColumnInfo.Decimal"),              ColumnInfo.COLUMN_TYPE_TEXT,    false), //$NON-NLS-1$
             new ColumnInfo(Messages.getString("DenormaliserDialog.ColumnInfo.Group"),                ColumnInfo.COLUMN_TYPE_TEXT,    false), //$NON-NLS-1$
             new ColumnInfo(Messages.getString("DenormaliserDialog.ColumnInfo.NullIf"),              ColumnInfo.COLUMN_TYPE_TEXT,    false), //$NON-NLS-1$
             new ColumnInfo(Messages.getString("DenormaliserDialog.ColumnInfo.Aggregation"),          ColumnInfo.COLUMN_TYPE_CCOMBO,  DenormaliserTargetField.typeAggrLongDesc, false), //$NON-NLS-1$
          };
        
        ciTarget[ciTarget.length-1].setToolTip(Messages.getString("DenormaliserDialog.CiTarget.Title")); //$NON-NLS-1$
        ciTarget[2].setUsingVariables(true);
		
		wTarget=new TableView(transMeta, shell, 
							  SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL, 
							  ciTarget, 
							  UpInsRows,  
							  lsMod,
							  props
							  );

		wGetAgg=new Button(shell, SWT.PUSH);
		wGetAgg.setText(Messages.getString("DenormaliserDialog.GetLookupFields.Button")); //$NON-NLS-1$
		fdGetAgg = new FormData();
		fdGetAgg.top   = new FormAttachment(wlTarget, margin);
		fdGetAgg.right = new FormAttachment(100, 0);
		wGetAgg.setLayoutData(fdGetAgg);

		// THE BUTTONS
		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(Messages.getString("System.Button.OK")); //$NON-NLS-1$
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(Messages.getString("System.Button.Cancel")); //$NON-NLS-1$

		setButtonPositions(new Button[] { wOK, wCancel }, margin, null);

		fdTarget=new FormData();
		fdTarget.left  = new FormAttachment(0, 0);
		fdTarget.top   = new FormAttachment(wlTarget, margin);
		fdTarget.right = new FormAttachment(wGetAgg, -margin);
		fdTarget.bottom= new FormAttachment(wOK, -margin);
		wTarget.setLayoutData(fdTarget);

		// Add listeners
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();        } };
		lsGet      = new Listener() { public void handleEvent(Event e) { get();       } };
		lsGetAgg   = new Listener() { public void handleEvent(Event e) { getAgg(); } };
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel();    } };
		
		wOK.addListener    (SWT.Selection, lsOK    );
		wGet.addListener   (SWT.Selection, lsGet   );
		wGetAgg.addListener (SWT.Selection, lsGetAgg );
		wCancel.addListener(SWT.Selection, lsCancel);
		
		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
		
		wStepname.addSelectionListener( lsDef );
		
		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );



		// Set the shell size, based upon previous time...
		setSize();
				
		getData();
		input.setChanged(backupChanged);

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
		log.logDebug(toString(), Messages.getString("DenormaliserDialog.Log.Getting.KeyInfo")); //$NON-NLS-1$
		
        if (input.getKeyField()!= null) wKeyField.setText(input.getKeyField());

		if (input.getGroupField()!=null)
		for (i=0;i<input.getGroupField().length;i++)
		{
			TableItem item = wGroup.table.getItem(i);
			if (input.getGroupField()[i]   !=null) item.setText(1, input.getGroupField()[i]);
		}
		
		if (input.getDenormaliserTargetField()!=null)
		for (i=0;i<input.getDenormaliserTargetField().length;i++)
		{
            DenormaliserTargetField field = input.getDenormaliserTargetField()[i];
            
			TableItem item = wTarget.table.getItem(i);
            
			if (field.getTargetName() != null )           item.setText( 1, field.getTargetName());
            if (field.getFieldName() != null )            item.setText( 2, field.getFieldName());
            if (field.getKeyValue() != null )             item.setText( 3, field.getKeyValue());
            if (field.getTargetTypeDesc() != null )       item.setText( 4, field.getTargetTypeDesc());
            if (field.getTargetFormat() != null )         item.setText( 5, field.getTargetFormat());
            if (field.getTargetLength()>=0)               item.setText( 6, ""+field.getTargetLength()); //$NON-NLS-1$
            if (field.getTargetPrecision()>=0)            item.setText( 7, ""+field.getTargetPrecision()); //$NON-NLS-1$
            if (field.getTargetCurrencySymbol() != null ) item.setText( 8, field.getTargetCurrencySymbol());
            if (field.getTargetDecimalSymbol() != null )  item.setText( 9, field.getTargetDecimalSymbol());
            if (field.getTargetGroupingSymbol() != null ) item.setText(10, field.getTargetGroupingSymbol());
            if (field.getTargetNullString() != null )     item.setText(11, field.getTargetNullString());
            if (field.getTargetAggregationType()>=0 )     item.setText(12, field.getTargetAggregationTypeDescLong());
        }
		
		wStepname.selectAll();
		wGroup.setRowNums();
		wGroup.optWidth(true);
		wTarget.setRowNums();
		wTarget.optWidth(true);
	}
	
	private void cancel()
	{
		stepname=null;
		dispose();
	}
	
	private void ok()
	{
		if (Const.isEmpty(wStepname.getText())) return;

		int sizegroup = wGroup.nrNonEmpty();
		int nrfields = wTarget.nrNonEmpty();
        
        input.setKeyField( wKeyField.getText() );

		input.allocate(sizegroup, nrfields);
				
		for (int i=0;i<sizegroup;i++)
		{
			TableItem item = wGroup.getNonEmpty(i);
			input.getGroupField()[i]    = item.getText(1);
		}
		
		for (int i=0;i<nrfields;i++)
		{
            DenormaliserTargetField field = new DenormaliserTargetField();
            
			TableItem item      = wTarget.getNonEmpty(i);
            field.setTargetName( item.getText(1) );
            field.setFieldName( item.getText(2) );
            field.setKeyValue( item.getText(3) );
            field.setTargetType( item.getText(4) );
            field.setTargetFormat( item.getText(5) );
            field.setTargetLength( Const.toInt( item.getText(6), -1) );
            field.setTargetPrecision( Const.toInt( item.getText(7), -1) );
            field.setTargetCurrencySymbol( item.getText(8) );
            field.setTargetDecimalSymbol( item.getText(9) );
            field.setTargetGroupingSymbol( item.getText(10) );
            field.setTargetNullString( item.getText(11) );
            field.setTargetAggregationType( item.getText(12) );
            
            input.getDenormaliserTargetField()[i] = field;
		}
		
		stepname = wStepname.getText();
        
        if ( "Y".equalsIgnoreCase( props.getCustomParameter(STRING_SORT_WARNING_PARAMETER, "Y") )) //$NON-NLS-1$ //$NON-NLS-2$
        {
            MessageDialogWithToggle md = new MessageDialogWithToggle(shell, 
                 Messages.getString("DenormaliserDialog.Unpivot.DialogTitle"),  //$NON-NLS-1$
                 null,
                 Messages.getString("DenormaliserDialog.Unpivot.DialogMessage",Const.CR,Const.CR), //$NON-NLS-1$ //$NON-NLS-2$
                 MessageDialog.WARNING,
                 new String[] { Messages.getString("DenormaliserDialog.WarningMessage.Option.1") }, //$NON-NLS-1$
                 0,
                 Messages.getString("DenormaliserDialog.WarningMessage.Option.2"), //$NON-NLS-1$
                 "N".equalsIgnoreCase( props.getCustomParameter(STRING_SORT_WARNING_PARAMETER, "Y") ) //$NON-NLS-1$ //$NON-NLS-2$
            );
            MessageDialogWithToggle.setDefaultImage(GUIResource.getInstance().getImageSpoon());
            md.open();
            props.setCustomParameter(STRING_SORT_WARNING_PARAMETER, md.getToggleState()?"N":"Y"); //$NON-NLS-1$ //$NON-NLS-2$
            props.saveProps();
        }
					
		dispose();
	}

	private void get()
	{
		try
		{
			RowMetaInterface r = transMeta.getPrevStepFields(stepname);
			if (r!=null && !r.isEmpty())
			{
                BaseStepDialog.getFieldsFromPrevious(r, wGroup, 1, new int[] { 1 }, new int[] { }, -1, -1, null);
			}
		}
		catch(KettleException ke)
		{
			new ErrorDialog(shell, Messages.getString("DenormaliserDialog.FailedToGetFields.DialogTitle"), Messages.getString("DenormaliserDialog.FailedToGetFields.DialogMessage"), ke); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	private void getAgg()
	{
        // The grouping fields: ignore those.
        wGroup.removeEmptyRows();
        final String[] groupingFields = wGroup.getItems(0);
		try
		{
			RowMetaInterface r = transMeta.getPrevStepFields(stepname);
			if (r!=null && !r.isEmpty())
			{
                BaseStepDialog.getFieldsFromPrevious(r, wTarget, 2, new int[] {}, new int[] {}, -1, -1, new TableItemInsertListener()
                    {
                        public boolean tableItemInserted(TableItem tableItem, ValueMetaInterface v)
                        {
                            if (Const.indexOfString(v.getName(), groupingFields)<0) // Not a grouping field
                            {
                                if (!wKeyField.getText().equalsIgnoreCase(v.getName())) // Not the key field
                                {
                                    int nr = tableItem.getParent().indexOf(tableItem)+1;
                                    tableItem.setText(1, Messages.getString("DenormaliserDialog.TargetFieldname.Label")+nr); // the target fieldname //$NON-NLS-1$
                                    tableItem.setText(2, v.getName());
                                    tableItem.setText(4, v.getTypeDesc());
                                    if (v.getLength()>=0) tableItem.setText(6, ""+v.getLength()); //$NON-NLS-1$
                                    if (v.getPrecision()>=0) tableItem.setText(7, ""+v.getPrecision()); //$NON-NLS-1$
                                }
                            }
                            return true;
                        }
                    }
                );
			}
		}
		catch(KettleException ke)
		{
			new ErrorDialog(shell, Messages.getString("DenormaliserDialog.FailedToGetFields.DialogTitle"), Messages.getString("DenormaliserDialog.FailedToGetFields.DialogMessage"), ke); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
	 private void getPreviousFieldNames()
	 {
		 if(!gotPreviousFields) 
		 {
			try
			{
				String keyValue=wKeyField.getText();
				wKeyField.removeAll();
				RowMetaInterface r = transMeta.getPrevStepFields(stepname);
		
				if (r != null) {
					r.getFieldNames();
	
					for (int i = 0; i < r.getFieldNames().length; i++) {
						wKeyField.add(r.getFieldNames()[i]);
					}
				}
				if(keyValue!=null) wKeyField.setText(keyValue);
				gotPreviousFields=true;
			}
			catch(KettleException ke)
			{
				new ErrorDialog(shell, Messages.getString("DenormaliserDialog.FailedToGetFields.DialogTitle"), Messages.getString("DenormaliserDialog.FailedToGetFields.DialogMessage"), ke); //$NON-NLS-1$ //$NON-NLS-2$
			}
		 }
		}
	public String toString()
	{
		return this.getClass().getName();
	}
}
