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

package org.pentaho.di.ui.trans.steps.filestoresult;

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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.ResultFile;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.trans.steps.filestoresult.FilesToResultMeta;
import org.pentaho.di.trans.steps.filestoresult.Messages;


public class FilesToResultDialog extends BaseStepDialog implements StepDialogInterface
{
	private Label        wlFilenameField;
	private CCombo       wFilenameField;
	private FormData     fdlFilenameField, fdFilenameField;

	private Label        wlTypes;
	private List         wTypes;
	private FormData     fdlTypes, fdTypes;

	private FilesToResultMeta input;

	public FilesToResultDialog(Shell parent, Object in, TransMeta tr, String sname)
	{
		super(parent, (BaseStepMeta)in, tr, sname);
		input=(FilesToResultMeta)in;
	}

	public String open()
	{
		Shell parent = getParent();
		Display display = parent.getDisplay();

		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.MIN | SWT.MAX | SWT.RESIZE);
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
		shell.setText(Messages.getString("FilesToResultDialog.Shell.Title")); //$NON-NLS-1$
		
		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Stepname line
		wlStepname=new Label(shell, SWT.RIGHT);
		wlStepname.setText(Messages.getString("FilesToResultDialog.Stepname.Label")); //$NON-NLS-1$
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
		
		// The rest...
		
		// FilenameField line
		wlFilenameField=new Label(shell, SWT.RIGHT);
		wlFilenameField.setText(Messages.getString("FilesToResultDialog.FilenameField.Label")); //$NON-NLS-1$
 		props.setLook(wlFilenameField);
		fdlFilenameField=new FormData();
		fdlFilenameField.left = new FormAttachment(0, 0);
		fdlFilenameField.top  = new FormAttachment(wStepname, margin);
		fdlFilenameField.right= new FormAttachment(middle, -margin);
		wlFilenameField.setLayoutData(fdlFilenameField);

		wFilenameField=new CCombo(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wFilenameField.setToolTipText(Messages.getString("FilesToResultDialog.FilenameField.Tooltip")); //$NON-NLS-1$
 		props.setLook(wFilenameField);
		wFilenameField.addModifyListener(lsMod);
		fdFilenameField=new FormData();
		fdFilenameField.left = new FormAttachment(middle, 0);
		fdFilenameField.top  = new FormAttachment(wStepname, margin);
		fdFilenameField.right= new FormAttachment(100, 0);
		wFilenameField.setLayoutData(fdFilenameField);
		
		/*
		 * Get the field names from the previous steps, in the background though
		 */
		Runnable runnable = new Runnable()
		{
			public void run()
			{
				try
				{
					RowMetaInterface inputfields = transMeta.getPrevStepFields(stepname);
                    if (inputfields!=null)
                    {
    					for (int i=0;i<inputfields.size();i++)
    					{
    						wFilenameField.add( inputfields.getValueMeta(i).getName() );
    					}
                    }
				}
				catch(Exception ke)
				{
					new ErrorDialog(shell, Messages.getString("FilesToResultDialog.FailedToGetFields.DialogTitle"), Messages.getString("FilesToResultDialog.FailedToGetFields.DialogMessage"), ke); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
		};
		display.asyncExec(runnable);
		
		// Some buttons
		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(Messages.getString("System.Button.OK")); //$NON-NLS-1$
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(Messages.getString("System.Button.Cancel")); //$NON-NLS-1$

		setButtonPositions(new Button[] { wOK, wCancel }, margin, null);
		
		// Include Files?
		wlTypes=new Label(shell, SWT.RIGHT);
		wlTypes.setText(Messages.getString("FilesToResultDialog.TypeOfFile.Label"));
 		props.setLook(wlTypes);
		fdlTypes=new FormData();
		fdlTypes.left = new FormAttachment(0, 0);
		fdlTypes.top  = new FormAttachment(wFilenameField, margin);
		fdlTypes.right= new FormAttachment(middle, -margin);
		wlTypes.setLayoutData(fdlTypes);
		wTypes=new List(shell, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		wTypes.setToolTipText(Messages.getString("FilesToResultDialog.TypeOfFile.Tooltip"));
 		props.setLook(wTypes);
		fdTypes=new FormData();
		fdTypes.left   = new FormAttachment(middle, 0);
		fdTypes.top    = new FormAttachment(wFilenameField, margin);
		fdTypes.bottom = new FormAttachment(wOK, -margin*3);
		fdTypes.right  = new FormAttachment(100, 0);
		wTypes.setLayoutData(fdTypes);
		for (int i=0;i<ResultFile.getAllTypeDesc().length;i++)
		{
			wTypes.add(ResultFile.getAllTypeDesc()[i]);
		}
		

		// Add listeners
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel(); } };
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();     } };
		
		wCancel.addListener(SWT.Selection, lsCancel);
		wOK.addListener    (SWT.Selection, lsOK    );
		
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
	
	/**
	 * Copy information from the meta-data input to the dialog fields.
	 */ 
	public void getData()
	{
		wStepname.selectAll();
		
		wTypes.select(input.getFileType());
		if (input.getFilenameField()!=null) wFilenameField.setText(input.getFilenameField());
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
		
		input.setFilenameField(wFilenameField.getText());
		if (wTypes.getSelectionIndex()>=0) 
		{
			input.setFileType( wTypes.getSelectionIndex() );
		}
		else
		{
			input.setFileType( ResultFile.FILE_TYPE_GENERAL );
		}
		
		dispose();
	}
}
