/* * Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
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

import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.database.GenericDatabaseMeta;
import org.pentaho.di.ui.core.database.wizard.Messages;
import org.pentaho.di.ui.core.PropsUI;


/**
 * 
 * On page one we select the database connection SAP/R3 specific settings
 * 1) The data tablespace
 * 2) The index tablespace
 * 
 * @author Jens Bleuel
 * @since  22-mar-2006
 */
public class CreateDatabaseWizardPageGeneric extends WizardPage
{
	private Label    wlURL;
	private Text     wURL;
	private FormData fdlURL, fdURL;

	private Label    wlDriverClass;
	private Text     wDriverClass;
	private FormData fdlDriverClass, fdDriverClass;
    
	private PropsUI props;
	private DatabaseMeta info;
	
	public CreateDatabaseWizardPageGeneric(String arg, PropsUI props, DatabaseMeta info)
	{
		super(arg);
		this.props=props;
		this.info = info;
		
		setTitle(Messages.getString("CreateDatabaseWizardPageGeneric.DialogTitle")); //$NON-NLS-1$
		setDescription(Messages.getString("CreateDatabaseWizardPageGeneric.DialogMessage")); //$NON-NLS-1$
		
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
	    compLayout.marginWidth  = Const.FORM_MARGIN;
		composite.setLayout(compLayout);

		// URL
		wlURL = new Label(composite, SWT.RIGHT);
		wlURL.setText(Messages.getString("CreateDatabaseWizardPageGeneric.URL.Label")); //$NON-NLS-1$
 		props.setLook(wlURL);
		fdlURL = new FormData();
		fdlURL.top    = new FormAttachment(0, 0);
		fdlURL.left   = new FormAttachment(0, 0);
		fdlURL.right  = new FormAttachment(middle,0);
		wlURL.setLayoutData(fdlURL);
		wURL = new Text(composite, SWT.SINGLE | SWT.BORDER);
 		props.setLook(wURL);
		fdURL = new FormData();
		fdURL.top     = new FormAttachment(0, 0);
		fdURL.left    = new FormAttachment(middle, margin);
		fdURL.right   = new FormAttachment(100, 0);
		wURL.setLayoutData(fdURL);
		wURL.addModifyListener(new ModifyListener()
		{
			public void modifyText(ModifyEvent arg0)
			{
				setPageComplete(false);
			}
		});
		
		// DRIVER CLASS
		wlDriverClass = new Label(composite, SWT.RIGHT);
		wlDriverClass.setText(Messages.getString("CreateDatabaseWizardPageGeneric.DriverClass.Label")); //$NON-NLS-1$
 		props.setLook(wlDriverClass);
		fdlDriverClass = new FormData();
		fdlDriverClass.top    = new FormAttachment(wURL, margin);
		fdlDriverClass.left   = new FormAttachment(0, 0);
		fdlDriverClass.right  = new FormAttachment(middle,0);
		wlDriverClass.setLayoutData(fdlDriverClass);
		wDriverClass = new Text(composite, SWT.SINGLE | SWT.BORDER);
 		props.setLook(wDriverClass);
		fdDriverClass = new FormData();
		fdDriverClass.top     = new FormAttachment(wURL, margin);
		fdDriverClass.left    = new FormAttachment(middle, margin);
		fdDriverClass.right   = new FormAttachment(100, 0);
		wDriverClass.setLayoutData(fdDriverClass);
		wDriverClass.addModifyListener(new ModifyListener()
		{
			public void modifyText(ModifyEvent arg0)
			{
				setPageComplete(false);
			}
		});
		
		
		// set the composite as the control for this page
		setControl(composite);
	}

	public boolean canFlipToNextPage()
	{
		String url = wURL.getText()!=null?wURL.getText().length()>0?wURL.getText():null:null;
		String driverClass   = wDriverClass.getText()!=null?wDriverClass.getText().length()>0?wDriverClass.getText():null:null;
		
		if (url==null || driverClass==null)
		{
			setErrorMessage(Messages.getString("CreateDatabaseWizardPageGeneric.ErrorMessage.URLAndDriverClassRequired")); //$NON-NLS-1$
			return false;
		}
		else
		{
			getDatabaseInfo();
			setErrorMessage(null);
			setMessage(Messages.getString("CreateDatabaseWizardPageGeneric.Message.Next")); //$NON-NLS-1$
			return true;
		}

	}	
	
	public DatabaseMeta getDatabaseInfo()
	{

		if (wURL.getText()!=null && wURL.getText().length()>0) 
		{
	        info.getAttributes().put(GenericDatabaseMeta.ATRRIBUTE_CUSTOM_URL,     wURL.getText());
		}
		
		if (wDriverClass.getText()!=null && wDriverClass.getText().length()>0)
		{
			info.getAttributes().put(GenericDatabaseMeta.ATRRIBUTE_CUSTOM_DRIVER_CLASS, wDriverClass.getText());
		}

		return info;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.WizardPage#getNextPage()
	 */
	public IWizardPage getNextPage()
	{
		IWizard wiz = getWizard();
		return wiz.getPage("2"); //$NON-NLS-1$
	}
	
}
