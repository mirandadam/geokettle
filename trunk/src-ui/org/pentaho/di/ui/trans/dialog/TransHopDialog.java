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
 * Created on 19-jun-2003
 *
 */

package org.pentaho.di.ui.trans.dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.Const;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.trans.TransHopMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.ui.trans.dialog.Messages;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.ui.core.PropsUI;




public class TransHopDialog extends Dialog
{
	private Label        wlFrom;
	private CCombo       wFrom;
    private FormData     fdlFrom, fdFrom;

	private Label        wlTo;
	private Button       wFlip;
	private CCombo       wTo;
	private FormData     fdlTo, fdFlip, fdTo;

	private Label        wlEnabled;
	private Button       wEnabled;
	private FormData     fdlEnabled, fdEnabled;

	private Button wOK, wCancel;
	private FormData fdOK, fdCancel;
	private Listener lsOK, lsCancel, lsFlip;

	private TransHopMeta input;
	private Shell  shell;
	private TransMeta transMeta;
	private PropsUI props;

	private ModifyListener lsMod;

	private boolean changed;

	public TransHopDialog(Shell parent, int style, TransHopMeta transHopMeta, TransMeta tr)
	{
		super(parent, style);
		this.props=PropsUI.getInstance();
		input=transHopMeta;
		transMeta=tr;
	}

	public Object open()
	{
		Shell parent = getParent();
		Display display = parent.getDisplay();

		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE);
 		props.setLook(shell);
		shell.setImage(GUIResource.getInstance().getImageHop());

		lsMod = new ModifyListener()
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
		shell.setText(Messages.getString("TransHopDialog.Shell.Label")); //$NON-NLS-1$

		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;
        int width = 0;

		// From step line
		wlFrom=new Label(shell, SWT.RIGHT);
		wlFrom.setText(Messages.getString("TransHopDialog.FromStep.Label")); //$NON-NLS-1$
 		props.setLook(wlFrom);
		fdlFrom=new FormData();
		fdlFrom.left = new FormAttachment(0, 0);
		fdlFrom.right= new FormAttachment(middle, -margin);
		fdlFrom.top  = new FormAttachment(0, margin);
		wlFrom.setLayoutData(fdlFrom);
		wFrom=new CCombo(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wFrom.setText(Messages.getString("TransHopDialog.FromStepDropdownList.Label")); //$NON-NLS-1$
 		props.setLook(wFrom);

		for (int i=0;i<transMeta.nrSteps();i++)
		{
			StepMeta stepMeta = transMeta.getStep(i);
			wFrom.add(stepMeta.getName());
		}
		wFrom.addModifyListener(lsMod);

		fdFrom=new FormData();
		fdFrom.left = new FormAttachment(middle, 0);
		fdFrom.top  = new FormAttachment(0, margin);
		fdFrom.right= new FormAttachment(100, 0);
		wFrom.setLayoutData(fdFrom);

		// To line
		wlTo=new Label(shell, SWT.RIGHT);
		wlTo.setText(Messages.getString("TransHopDialog.TargetStep.Label")); //$NON-NLS-1$
 		props.setLook(wlTo);
		fdlTo=new FormData();
		fdlTo.left = new FormAttachment(0, 0);
		fdlTo.right= new FormAttachment(middle, -margin);
		fdlTo.top  = new FormAttachment(wFrom, margin);
		wlTo.setLayoutData(fdlTo);
		wTo=new CCombo(shell, SWT.BORDER | SWT.READ_ONLY);
		wTo.setText(Messages.getString("TransHopDialog.TargetStepDropdownList.Label")); //$NON-NLS-1$
 		props.setLook(wTo);

		for (int i=0;i<transMeta.nrSteps();i++)
		{
			StepMeta stepMeta = transMeta.getStep(i);
			wTo.add(stepMeta.getName());
		}
		wTo.addModifyListener(lsMod);

		fdTo=new FormData();
		fdTo.left = new FormAttachment(middle, 0);
		fdTo.top  = new FormAttachment(wFrom, margin);
		fdTo.right= new FormAttachment(100, 0);
		wTo.setLayoutData(fdTo);

		// Enabled?
		wlEnabled=new Label(shell, SWT.RIGHT);
		wlEnabled.setText(Messages.getString("TransHopDialog.EnableHop.Label")); //$NON-NLS-1$
 		props.setLook(wlEnabled);
		fdlEnabled=new FormData();
		fdlEnabled.left = new FormAttachment(0, 0);
		fdlEnabled.right= new FormAttachment(middle, -margin);
		fdlEnabled.top  = new FormAttachment(wlTo, margin*5);
		wlEnabled.setLayoutData(fdlEnabled);
		wEnabled=new Button(shell, SWT.CHECK);
 		props.setLook(wEnabled);
		fdEnabled=new FormData();
		fdEnabled.left = new FormAttachment(middle, 0);
		fdEnabled.top  = new FormAttachment(wlTo, margin*5);
		wEnabled.setLayoutData(fdEnabled);
		wEnabled.addSelectionListener(new SelectionAdapter()
			{
				public void widgetSelected(SelectionEvent e)
				{
					input.setEnabled( !input.isEnabled());
					input.setChanged();
				}
			}
		);

		wFlip = new Button(shell, SWT.PUSH);
		wFlip.setText(Messages.getString("TransHopDialog.FromTo.Button")); //$NON-NLS-1$
		fdFlip = new FormData();
		fdFlip.right = new FormAttachment(100, 0);
		fdFlip.top  = new FormAttachment(wlTo, 20);
		wFlip.setLayoutData(fdFlip);

		// Some buttons
		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(Messages.getString("System.Button.OK")); //$NON-NLS-1$
		wOK.pack(true);
		Rectangle rOK = wOK.getBounds();

		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(Messages.getString("System.Button.Cancel")); //$NON-NLS-1$
		wCancel.pack(true);
		Rectangle rCancel = wCancel.getBounds();

		width = (rOK.width > rCancel.width ? rOK.width : rCancel.width);
		width += margin;

		fdOK=new FormData();
		fdOK.top        = new FormAttachment(wFlip, margin*5);
		fdOK.left       = new FormAttachment(50, -width);
		fdOK.right      = new FormAttachment(50, -(margin/2));
		//fdOK.bottom     = new FormAttachment(100, 0);
		wOK.setLayoutData(fdOK);

		fdCancel=new FormData();
		fdCancel.top    = new FormAttachment(wFlip, margin*5);
		fdCancel.left   = new FormAttachment(50, margin/2);
		fdCancel.right  = new FormAttachment(50, width);
		//fdCancel.bottom = new FormAttachment(100, 0);
		wCancel.setLayoutData(fdCancel);

		// Add listeners
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel(); } };
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();     } };
		lsFlip     = new Listener() { public void handleEvent(Event e) { flip();   } };

		wOK.addListener    (SWT.Selection, lsOK     );
		wCancel.addListener(SWT.Selection, lsCancel );
		wFlip.addListener  (SWT.Selection, lsFlip );

		// Detect [X] or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );

		getData();

		BaseStepDialog.setSize(shell);

		input.setChanged(changed);

		shell.open();
		while (!shell.isDisposed())
		{
				if (!display.readAndDispatch()) display.sleep();
		}
		return input;
	}

	public void dispose()
	{
		props.setScreen(new WindowProperty(shell));
		shell.dispose();
	}

	/**
	 * Copy information from the meta-data input to the dialog fields.
	 */
	public void getData()
	{
		if (input.getFromStep() != null) wFrom.setText(input.getFromStep().getName());
		if (input.getToStep()   != null) wTo.setText(input.getToStep().getName());
		wEnabled.setSelection(input.isEnabled());
	}

	private void cancel()
	{
		input.setChanged(changed);
		input=null;
		dispose();
	}

	private void ok()
	{
		StepMeta fromBackup = input.getFromStep();
		StepMeta toBackup = input.getToStep();
		input.setFromStep( transMeta.findStep( wFrom.getText() ));
		input.setToStep  ( transMeta.findStep( wTo.getText()   ));

		if (transMeta.hasLoop(input.getFromStep()))
		{
			input.setFromStep(fromBackup);
			input.setToStep(toBackup);
			MessageBox mb = new MessageBox(shell, SWT.YES | SWT.ICON_WARNING );
			mb.setMessage(Messages.getString("TransHopDialog.LoopsNotAllowed.DialogMessage")); //$NON-NLS-1$
			mb.setText(Messages.getString("TransHopDialog.LoopsNotAllowed.DialogTitle")); //$NON-NLS-1$
			mb.open();
		}
		else
		{
			if (input.getFromStep()==null)
			{
				MessageBox mb = new MessageBox(shell, SWT.YES | SWT.ICON_WARNING );
				mb.setMessage(Messages.getString("TransHopDialog.StepDoesNotExist.DialogMessage",wFrom.getText())); //$NON-NLS-1$ //$NON-NLS-2$
				mb.setText(Messages.getString("TransHopDialog.StepDoesNotExist.DialogTitle")); //$NON-NLS-1$
				mb.open();
			}
			else
			{
				if (input.getToStep()==null)
				{
					MessageBox mb = new MessageBox(shell, SWT.YES | SWT.ICON_WARNING );
					mb.setMessage(Messages.getString("TransHopDialog.StepDoesNotExist.DialogMessage",wTo.getText())); //$NON-NLS-1$ //$NON-NLS-2$
					mb.setText(Messages.getString("TransHopDialog.StepDoesNotExist.DialogTitle")); //$NON-NLS-1$
					mb.open();
				}
				else
				{
					if (input.getFromStep().equals(input.getToStep()))
					{
						MessageBox mb = new MessageBox(shell, SWT.YES | SWT.ICON_WARNING );
						mb.setMessage(Messages.getString("TransHopDialog.CannotGoToSameStep.DialogMessage")); //$NON-NLS-1$
						mb.setText(Messages.getString("TransHopDialog.CannotGoToSameStep.DialogTitle")); //$NON-NLS-1$
						mb.open();
					}
					else
					{
						dispose();
					}
				}
			}
		}
	}

	private void flip()
	{
		String dummy;
		dummy = wFrom.getText();
		wFrom.setText(wTo.getText());
		wTo.setText(dummy);
		input.setChanged();
	}
}
