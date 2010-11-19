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

package org.pentaho.di.ui.trans.steps.fileexists;

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
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Group;

import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.steps.fileexists.Messages;
import org.pentaho.di.trans.steps.fileexists.FileExistsMeta;
import org.pentaho.di.ui.core.dialog.ErrorDialog;

public class FileExistsDialog extends BaseStepDialog implements StepDialogInterface
{

	private boolean gotPreviousFields=false;
	private Label        wlFileName;
	private CCombo       wFileName;
	private FormData     fdlFileName, fdFileName;

	private Label        wlResult,wlFileType;
	private TextVar         wResult,wFileType;
	private FormData     fdlResult, fdResult,fdAdditionalFields,fdlFileType,fdFileType;
	
	private Label        wlInclFileType;
	private Button       wInclFileType;
	private FormData     fdlInclFileType, fdInclFileType;
	
	private Group wAdditionalFields;
	
	private Button       wAddResult;
	private FormData     fdAddResult,fdlAddResult;
	private Label        wlAddResult;

	private FileExistsMeta input;

	public FileExistsDialog(Shell parent, Object in, TransMeta transMeta, String sname)
	{
		super(parent, (BaseStepMeta)in, transMeta, sname);
		input=(FileExistsMeta)in;
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
		shell.setText(Messages.getString("FileExistsDialog.Shell.Title")); //$NON-NLS-1$
		
		int middle = props.getMiddlePct();
		int margin=Const.MARGIN;

		// Stepname line
		wlStepname=new Label(shell, SWT.RIGHT);
		wlStepname.setText(Messages.getString("FileExistsDialog.Stepname.Label")); //$NON-NLS-1$
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

		// filename field
		wlFileName=new Label(shell, SWT.RIGHT);
		wlFileName.setText(Messages.getString("FileExistsDialog.FileName.Label")); //$NON-NLS-1$
 		props.setLook(wlFileName);
		fdlFileName=new FormData();
		fdlFileName.left = new FormAttachment(0, 0);
		fdlFileName.right= new FormAttachment(middle, -margin);
		fdlFileName.top  = new FormAttachment(wStepname, margin);
		wlFileName.setLayoutData(fdlFileName);
		
		
		wFileName=new CCombo(shell, SWT.BORDER | SWT.READ_ONLY);
 		props.setLook(wFileName);
		wFileName.addModifyListener(lsMod);
		fdFileName=new FormData();
		fdFileName.left = new FormAttachment(middle, 0);
		fdFileName.top  = new FormAttachment(wStepname, margin);
		fdFileName.right= new FormAttachment(100, -margin);
		wFileName.setLayoutData(fdFileName);
		wFileName.addFocusListener(new FocusListener()
        {
            public void focusLost(org.eclipse.swt.events.FocusEvent e)
            {
            }
        
            public void focusGained(org.eclipse.swt.events.FocusEvent e)
            {
                Cursor busy = new Cursor(shell.getDisplay(), SWT.CURSOR_WAIT);
                shell.setCursor(busy);
                get();
                shell.setCursor(null);
                busy.dispose();
            }
        }
    );
		
		// Result fieldname ...
		wlResult=new Label(shell, SWT.RIGHT);
		wlResult.setText(Messages.getString("FileExistsDialog.ResultField.Label")); //$NON-NLS-1$
 		props.setLook(wlResult);
		fdlResult=new FormData();
		fdlResult.left = new FormAttachment(0, 0);
		fdlResult.right= new FormAttachment(middle, -margin);
		fdlResult.top  = new FormAttachment(wFileName, margin*2);
		wlResult.setLayoutData(fdlResult);

		wResult=new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wResult.setToolTipText(Messages.getString("FileExistsDialog.ResultField.Tooltip"));
 		props.setLook(wResult);
		wResult.addModifyListener(lsMod);
		fdResult=new FormData();
		fdResult.left = new FormAttachment(middle, 0);
		fdResult.top  = new FormAttachment(wFileName, margin*2);
		fdResult.right= new FormAttachment(100, 0);
		wResult.setLayoutData(fdResult);
		
		// Add filename to result filenames?
		wlAddResult=new Label(shell, SWT.RIGHT);
		wlAddResult.setText(Messages.getString("FileExistsDialog.AddResult.Label"));
 		props.setLook(wlAddResult);
		fdlAddResult=new FormData();
		fdlAddResult.left = new FormAttachment(0, 0);
		fdlAddResult.top  = new FormAttachment(wResult, margin);
		fdlAddResult.right= new FormAttachment(middle, -margin);
		wlAddResult.setLayoutData(fdlAddResult);
		wAddResult=new Button(shell, SWT.CHECK );
 		props.setLook(wAddResult);
		wAddResult.setToolTipText(Messages.getString("FileExistsDialog.AddResult.Tooltip"));
		fdAddResult=new FormData();
		fdAddResult.left = new FormAttachment(middle, 0);
		fdAddResult.top  = new FormAttachment(wResult, margin);
		wAddResult.setLayoutData(fdAddResult);
		
		///////////////////////////////// 
		// START OF Additional Fields GROUP  //
		///////////////////////////////// 

		wAdditionalFields = new Group(shell, SWT.SHADOW_NONE);
		props.setLook(wAdditionalFields);
		wAdditionalFields.setText(Messages.getString("FileExistsDialog.wAdditionalFields.Label"));
		
		FormLayout AdditionalFieldsgroupLayout = new FormLayout();
		AdditionalFieldsgroupLayout.marginWidth = 10;
		AdditionalFieldsgroupLayout.marginHeight = 10;
		wAdditionalFields.setLayout(AdditionalFieldsgroupLayout);
		
		// include filetype?
		wlInclFileType=new Label(wAdditionalFields, SWT.RIGHT);
		wlInclFileType.setText(Messages.getString("FileExistsDialog.InclFileType.Label"));
 		props.setLook(wlInclFileType);
		fdlInclFileType=new FormData();
		fdlInclFileType.left = new FormAttachment(0, 0);
		fdlInclFileType.top  = new FormAttachment(wResult, margin);
		fdlInclFileType.right= new FormAttachment(middle, -margin);
		wlInclFileType.setLayoutData(fdlInclFileType);
		wInclFileType=new Button(wAdditionalFields, SWT.CHECK );
 		props.setLook(wInclFileType);
		wInclFileType.setToolTipText(Messages.getString("FileExistsDialog.InclFileType.Tooltip"));
		fdInclFileType=new FormData();
		fdInclFileType.left = new FormAttachment(middle, 0);
		fdInclFileType.top  = new FormAttachment(wResult, margin);
		wInclFileType.setLayoutData(fdInclFileType);
		
		// Enable/disable the right fields to allow a filename to be added to each row...
		wInclFileType.addSelectionListener(new SelectionAdapter() 
			{
				public void widgetSelected(SelectionEvent e) 
				{
					activeFileType();
				}
			}
		);
		
		// FileType fieldname ...
		wlFileType=new Label(wAdditionalFields, SWT.RIGHT);
		wlFileType.setText(Messages.getString("FileExistsDialog.FileTypeField.Label")); //$NON-NLS-1$
 		props.setLook(wlFileType);
		fdlFileType=new FormData();
		fdlFileType.left = new FormAttachment(wInclFileType, 2*margin);
		fdlFileType.top  = new FormAttachment(wResult, margin);
		wlFileType.setLayoutData(fdlFileType);

		wFileType=new TextVar(transMeta, wAdditionalFields, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wFileType.setToolTipText(Messages.getString("FileExistsDialog.FileTypeField.Tooltip"));
 		props.setLook(wFileType);
		wFileType.addModifyListener(lsMod);
		fdFileType=new FormData();
		fdFileType.left = new FormAttachment(wlFileType, margin);
		fdFileType.top  = new FormAttachment(wResult, margin);
		fdFileType.right= new FormAttachment(100, 0);
		wFileType.setLayoutData(fdFileType);
		
		fdAdditionalFields = new FormData();
		fdAdditionalFields.left = new FormAttachment(0, margin);
		fdAdditionalFields.top = new FormAttachment(wAddResult, margin);
		fdAdditionalFields.right = new FormAttachment(100, -margin);
		wAdditionalFields.setLayoutData(fdAdditionalFields);
		
		///////////////////////////////// 
		// END OF Additional Fields GROUP  //
		///////////////////////////////// 

		// THE BUTTONS
		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(Messages.getString("System.Button.OK")); //$NON-NLS-1$
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(Messages.getString("System.Button.Cancel")); //$NON-NLS-1$

		setButtonPositions(new Button[] { wOK, wCancel }, margin, wAdditionalFields);

		// Add listeners
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();        } };

		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel();    } };
		
		wOK.addListener    (SWT.Selection, lsOK    );
		wCancel.addListener(SWT.Selection, lsCancel);
		
		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
		
		wStepname.addSelectionListener( lsDef );
		
		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );

		// Set the shell size, based upon previous time...
		setSize();
		
		getData();
		activeFileType();
		input.setChanged(changed);

		shell.open();
		while (!shell.isDisposed())
		{
				if (!display.readAndDispatch()) display.sleep();
		}
		return stepname;
	}
	private void activeFileType()
	{
		wlFileType.setEnabled(wInclFileType.getSelection());
		wFileType.setEnabled(wInclFileType.getSelection());
	}
	/**
	 * Copy information from the meta-data input to the dialog fields.
	 */ 
	public void getData()
	{
		if (input.getDynamicFilenameField() !=null)   wFileName.setText(input.getDynamicFilenameField());
		if (input.getResultFieldName()!=null)   wResult.setText(input.getResultFieldName());
		wInclFileType.setSelection(input.includeFileType());
		if (input.getFileTypeFieldName()!=null)   wFileType.setText(input.getFileTypeFieldName());
		wAddResult.setSelection(input.addResultFilenames());
		
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
		input.setDynamicFilenameField(wFileName.getText() );
		input.setResultFieldName(wResult.getText() );
		input.setincludeFileType(wInclFileType.getSelection());
		input.setFileTypeFieldName(wFileType.getText() );
		input.setaddResultFilenames(wAddResult.getSelection());
		stepname = wStepname.getText(); // return value
		
		dispose();
	}
	 private void get()
	 {
		 if(!gotPreviousFields) {
		 try{
	            String fieldvalue=wFileName.getText();
			    wFileName.removeAll();
				RowMetaInterface r = transMeta.getPrevStepFields(stepname);
				if (r!=null)
				{
					wFileName.setItems(r.getFieldNames());
				}
				if(fieldvalue!=null) wFileName.setText(fieldvalue);
				gotPreviousFields=true;
		 }catch(KettleException ke){
				new ErrorDialog(shell, Messages.getString("FileExistsDialog.FailedToGetFields.DialogTitle"), Messages.getString("FileExistsDialog.FailedToGetFields.DialogMessage"), ke); //$NON-NLS-1$ //$NON-NLS-2$
			}
		 }
	 }
	public String toString()
	{
		return this.getClass().getName();
	}
}
