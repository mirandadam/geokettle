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

package org.pentaho.di.ui.trans.steps.filterrows;

import java.util.List;

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
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Condition;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.widget.ConditionEditor;
import org.pentaho.di.trans.steps.filterrows.FilterRowsMeta;
import org.pentaho.di.trans.steps.filterrows.Messages;


public class FilterRowsDialog extends BaseStepDialog implements StepDialogInterface
{
	private Label        wlTrueTo;
	private CCombo       wTrueTo;
	private FormData     fdlTrueTo, fdTrueTo;

	private Label        wlFalseTo;
	private CCombo       wFalseTo;
	private FormData     fdlFalseTo, fdFalseFrom;

	private Label           wlCondition;
	private ConditionEditor wCondition;
	private FormData        fdlCondition, fdCondition;

	private FilterRowsMeta input;
	private Condition      condition;
	
	private Condition      backupCondition;

	public FilterRowsDialog(Shell parent, Object in, TransMeta tr, String sname)
	{
		super(parent, (BaseStepMeta)in, tr, sname);
		input=(FilterRowsMeta)in;
        
        condition = (Condition)input.getCondition().clone();

    }

	public String open()
	{
		Shell parent = getParent();
		Display display = parent.getDisplay();

        shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MIN | SWT.MAX );
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
		backupCondition = (Condition)condition.clone(); 
			
		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText(Messages.getString("FilterRowsDialog.Shell.Title")); //$NON-NLS-1$
		
		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Stepname line
		wlStepname=new Label(shell, SWT.RIGHT);
		wlStepname.setText(Messages.getString("FilterRowsDialog.Stepname.Label")); //$NON-NLS-1$
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

		// Send 'True' data to...
		wlTrueTo=new Label(shell, SWT.RIGHT);
		wlTrueTo.setText(Messages.getString("FilterRowsDialog.SendTrueTo.Label")); //$NON-NLS-1$
 		props.setLook(wlTrueTo);
		fdlTrueTo=new FormData();
		fdlTrueTo.left = new FormAttachment(0, 0);
		fdlTrueTo.right= new FormAttachment(middle, -margin);
		fdlTrueTo.top  = new FormAttachment(wStepname, margin);
		wlTrueTo.setLayoutData(fdlTrueTo);
		wTrueTo=new CCombo(shell, SWT.BORDER );
 		props.setLook(wTrueTo);

		StepMeta stepinfo = transMeta.findStep(stepname);
		if (stepinfo!=null)
		{
			List<StepMeta> nextSteps = transMeta.findNextSteps(stepinfo);
			for (int i=0;i<nextSteps.size();i++)
			{
				StepMeta stepMeta = nextSteps.get(i);
				wTrueTo.add(stepMeta.getName());
			}
		}
		
		wTrueTo.addModifyListener(lsMod);
		fdTrueTo=new FormData();
		fdTrueTo.left = new FormAttachment(middle, 0);
		fdTrueTo.top  = new FormAttachment(wStepname, margin);
		fdTrueTo.right= new FormAttachment(100, 0);
		wTrueTo.setLayoutData(fdTrueTo);

		// Send 'False' data to...
		wlFalseTo=new Label(shell, SWT.RIGHT);
		wlFalseTo.setText(Messages.getString("FilterRowsDialog.SendFalseTo.Label")); //$NON-NLS-1$
 		props.setLook(wlFalseTo);
		fdlFalseTo=new FormData();
		fdlFalseTo.left = new FormAttachment(0, 0);
		fdlFalseTo.right= new FormAttachment(middle, -margin);
		fdlFalseTo.top  = new FormAttachment(wTrueTo, margin);
		wlFalseTo.setLayoutData(fdlFalseTo);
		wFalseTo=new CCombo(shell, SWT.BORDER );
 		props.setLook(wFalseTo);

		stepinfo = transMeta.findStep(stepname);
		if (stepinfo!=null)
		{
			List<StepMeta> nextSteps = transMeta.findNextSteps(stepinfo);
			for (int i=0;i<nextSteps.size();i++)
			{
				StepMeta stepMeta = nextSteps.get(i);
				wFalseTo.add(stepMeta.getName());
			}
		}
		
		wFalseTo.addModifyListener(lsMod);
		fdFalseFrom=new FormData();
		fdFalseFrom.left = new FormAttachment(middle, 0);
		fdFalseFrom.top  = new FormAttachment(wTrueTo, margin);
		fdFalseFrom.right= new FormAttachment(100, 0);
		wFalseTo.setLayoutData(fdFalseFrom);

		
		wlCondition=new Label(shell, SWT.NONE);
		wlCondition.setText(Messages.getString("FilterRowsDialog.Condition.Label")); //$NON-NLS-1$
 		props.setLook(wlCondition);
		fdlCondition=new FormData();
		fdlCondition.left  = new FormAttachment(0, 0);
		fdlCondition.top   = new FormAttachment(wFalseTo, margin);
		wlCondition.setLayoutData(fdlCondition);
		
		RowMetaInterface inputfields = null;
		try
		{
			inputfields = transMeta.getPrevStepFields(stepname);
		}
		catch(KettleException ke)
		{
			inputfields = new RowMeta();
			new ErrorDialog(shell, Messages.getString("FilterRowsDialog.FailedToGetFields.DialogTitle"), Messages.getString("FilterRowsDialog.FailedToGetFields.DialogMessage"), ke); //$NON-NLS-1$ //$NON-NLS-2$
		}

		// Some buttons
		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(Messages.getString("System.Button.OK")); //$NON-NLS-1$
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(Messages.getString("System.Button.Cancel")); //$NON-NLS-1$

		setButtonPositions(new Button[] { wOK, wCancel }, margin, null);

		wCondition = new ConditionEditor(shell, SWT.BORDER, condition, inputfields);
		
		fdCondition=new FormData();
		fdCondition.left  = new FormAttachment(0, 0);
		fdCondition.top   = new FormAttachment(wlCondition, margin);
		fdCondition.right = new FormAttachment(100, 0);
		fdCondition.bottom= new FormAttachment(wOK, -2*margin);
		wCondition.setLayoutData(fdCondition);
		wCondition.addModifyListener(lsMod);

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
		if (input.getSendTrueStepname() != null) wTrueTo.setText(input.getSendTrueStepname());
		if (input.getSendFalseStepname() != null) wFalseTo.setText(input.getSendFalseStepname());
		wStepname.selectAll();
	}
	
	private void cancel()
	{
		stepname=null;
		input.setChanged(backupChanged);
		// Also change the condition back to what it was...
		input.setCondition(backupCondition);
		dispose();
	}
	
	private void ok()
	{		
		if (Const.isEmpty(wStepname.getText())) return;

		if (wCondition.getLevel()>0) 
		{
			wCondition.goUp();
		}
		else
		{
            String trueStepname = wTrueTo.getText();
            if (trueStepname.length() == 0) trueStepname = null;
            String falseStepname = wFalseTo.getText();
            if (falseStepname.length() == 0) falseStepname = null;

            input.setSendTrueStepname(trueStepname);
			input.setSendTrueStep( transMeta.findStep( trueStepname ) );
            
			input.setSendFalseStepname(falseStepname);
			input.setSendFalseStep( transMeta.findStep( falseStepname ) );
			stepname = wStepname.getText(); // return value
			input.setCondition( condition );
			
			dispose();
		}
	}
}
