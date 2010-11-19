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


package org.pentaho.di.ui.core.database.wizard;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.ui.core.database.wizard.Messages;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.database.dialog.DatabaseDialog;

/**
 * 
 * On page one we select the username and password. We also provide a test button.
 * 
 * @author Matt
 * @since 04-apr-2005
 */
public class CreateDatabaseWizardPage2 extends WizardPage
{
	private Label			wlUsername;
	private Text			wUsername;
	private FormData		fdlUsername, fdUsername;

	private Label			wlPassword;
	private Text			wPassword;
	private FormData		fdlPassword, fdPassword;
	
	private Button          wTest;
	private FormData        fdTest;

	private PropsUI			props;
	private DatabaseMeta	info;

	public CreateDatabaseWizardPage2(String arg, PropsUI props, DatabaseMeta info)
	{
		super(arg);
		this.props = props;
		this.info = info;

		setTitle(Messages.getString("CreateDatabaseWizardPage2.DialogTitle")); //$NON-NLS-1$
		setDescription(Messages.getString("CreateDatabaseWizardPage2.DialogMessage")); //$NON-NLS-1$

		setPageComplete(false);
	}

	public void createControl(Composite parent)
	{
		int margin = Const.MARGIN;
		int middle = props.getMiddlePct();

		// create the composite to hold the widgets
		Composite composite = new Composite(parent, SWT.NONE);
 		props.setLook(composite);

		FormLayout compLayout = new FormLayout();
		compLayout.marginHeight = Const.FORM_MARGIN;
		compLayout.marginWidth = Const.FORM_MARGIN;
		composite.setLayout(compLayout);

		// USERNAME
		wlUsername = new Label(composite, SWT.RIGHT);
		wlUsername.setText(Messages.getString("CreateDatabaseWizardPage2.Username.Label")); //$NON-NLS-1$
 		props.setLook(wlUsername);
		fdlUsername = new FormData();
		fdlUsername.top = new FormAttachment(0, 0);
		fdlUsername.left = new FormAttachment(0, 0);
		fdlUsername.right = new FormAttachment(middle, 0);
		wlUsername.setLayoutData(fdlUsername);
		wUsername = new Text(composite, SWT.SINGLE | SWT.BORDER);
 		props.setLook(wUsername);
		fdUsername = new FormData();
		fdUsername.top = new FormAttachment(0, 0);
		fdUsername.left = new FormAttachment(middle, margin);
		fdUsername.right = new FormAttachment(100, 0);
		wUsername.setLayoutData(fdUsername);
		wUsername.addModifyListener(new ModifyListener()
		{
			public void modifyText(ModifyEvent arg0)
			{
				setPageComplete(false);
			}
		});

		// PASSWORD
		wlPassword = new Label(composite, SWT.RIGHT);
		wlPassword.setText(Messages.getString("CreateDatabaseWizardPage2.Password.Label")); //$NON-NLS-1$
 		props.setLook(wlPassword);
		fdlPassword = new FormData();
		fdlPassword.top = new FormAttachment(wUsername, margin);
		fdlPassword.left = new FormAttachment(0, 0);
		fdlPassword.right = new FormAttachment(middle, 0);
		wlPassword.setLayoutData(fdlPassword);
		wPassword = new Text(composite, SWT.SINGLE | SWT.BORDER);
 		props.setLook(wPassword);
		wPassword.setEchoChar('*');
		fdPassword = new FormData();
		fdPassword.top = new FormAttachment(wUsername, margin);
		fdPassword.left = new FormAttachment(middle, margin);
		fdPassword.right = new FormAttachment(100, 0);
		wPassword.setLayoutData(fdPassword);
		wPassword.addModifyListener(new ModifyListener()
		{
			public void modifyText(ModifyEvent arg0)
			{
				setPageComplete(false);
			}
		});
		
		wTest = new Button(composite, SWT.PUSH);
		wTest.setText(Messages.getString("CreateDatabaseWizardPage2.TestConnection.Button")); //$NON-NLS-1$
		fdTest = new FormData();
		fdTest.top = new FormAttachment(wPassword, margin*4);
		fdTest.left = new FormAttachment(50, 0);
		wTest.setLayoutData(fdTest);
		wTest.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent arg0)
			{
				test();
			}
		});

		// set the composite as the control for this page
		setControl(composite);
	}
	
	public void test()
	{
		Shell shell = getWizard().getContainer().getShell();
		DatabaseDialog.test(shell, info);
	}

	public boolean canFlipToNextPage()
	{
		return false;
	}

	public DatabaseMeta getDatabaseInfo()
	{
		if (wUsername.getText() != null && wUsername.getText().length() > 0)
		{
			info.setUsername(wUsername.getText());
		}

		if (wPassword.getText() != null && wPassword.getText().length() > 0)
		{
			info.setPassword(wPassword.getText());
		}

		wTest.setEnabled( info.getDatabaseType() != DatabaseMeta.TYPE_DATABASE_SAPR3 );
		
		return info;
	}

	public boolean canFinish()
	{
		getDatabaseInfo();

        String[] remarks = info.checkParameters(); 
		if (remarks.length == 0)
		{
			setErrorMessage(null);
			setMessage(Messages.getString("CreateDatabaseWizardPage2.Message.Finish")); //$NON-NLS-1$
			return true;
		}
		else
		{
			setErrorMessage(Messages.getString("CreateDatabaseWizardPage2.ErrorMessage.InvalidInput")); //$NON-NLS-1$
			// setMessage("Select 'Finish' to create the database connection");
			return false;
		}
	}
}