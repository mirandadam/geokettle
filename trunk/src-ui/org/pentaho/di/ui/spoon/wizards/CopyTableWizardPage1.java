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

package org.pentaho.di.ui.spoon.wizards;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.List;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.ui.spoon.wizards.CopyTableWizardPage2;
import org.pentaho.di.ui.spoon.wizards.Messages;
import org.pentaho.di.ui.core.PropsUI;


/**
 * 
 * On page one we select the source and target databases...
 * 
 * @author Matt
 * @since  29-mar-05
 */
public class CopyTableWizardPage1 extends WizardPage
{
	private List     wSourceDB, wTargetDB;
	private FormData fdSourceDB, fdTargetDB;

	private PropsUI props;
	private java.util.List<DatabaseMeta> databases;
	
    /** @deprecated */
    public CopyTableWizardPage1(String arg, PropsUI props, java.util.List<DatabaseMeta> databases)
    {
        this(arg, databases);
    }

	public CopyTableWizardPage1(String arg, java.util.List<DatabaseMeta> databases)
	{
		super(arg);
		this.props=PropsUI.getInstance();
		this.databases=databases;
		
		setTitle(Messages.getString("CopyTableWizardPage1.Dialog.Title")); //$NON-NLS-1$
		setDescription(Messages.getString("CopyTableWizardPage1.Dialog.Description")); //$NON-NLS-1$
		
		setPageComplete(false);
	}
	
	public void createControl(Composite parent)
	{
		int margin = Const.MARGIN;
		
		// create the composite to hold the widgets
		Composite composite = new Composite(parent, SWT.NONE);
 		props.setLook(composite);
	    
	    FormLayout compLayout = new FormLayout();
	    compLayout.marginHeight = Const.FORM_MARGIN;
	    compLayout.marginWidth  = Const.FORM_MARGIN;
		composite.setLayout(compLayout);

		wSourceDB = new List(composite, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
 		props.setLook(wSourceDB);
		for (int i=0;i<databases.size();i++)
		{
			DatabaseMeta dbInfo = (DatabaseMeta)databases.get(i);
			wSourceDB.add(dbInfo.getName());
		}
		fdSourceDB = new FormData();
		fdSourceDB.top    = new FormAttachment(0,0);
		fdSourceDB.left   = new FormAttachment(0,0);
		fdSourceDB.bottom = new FormAttachment(100,0);
		fdSourceDB.right  = new FormAttachment(50,0);
		wSourceDB.setLayoutData(fdSourceDB);
		wSourceDB.addSelectionListener
			(
				new SelectionAdapter()
				{
					public void widgetSelected(SelectionEvent e)
					{
						setPageComplete(false);
					}
				}
			);
		
		wTargetDB = new List(composite, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
 		props.setLook(wTargetDB);
		for (int i=0;i<databases.size();i++)
		{
			DatabaseMeta dbInfo = (DatabaseMeta)databases.get(i);
			wTargetDB.add(dbInfo.getName());
		}
		fdTargetDB = new FormData();
		fdTargetDB.top    = new FormAttachment(0,0);
		fdTargetDB.left   = new FormAttachment(50,margin);
		fdTargetDB.bottom = new FormAttachment(100,0);
		fdTargetDB.right  = new FormAttachment(100,0);
		wTargetDB.setLayoutData(fdTargetDB);
		wTargetDB.addSelectionListener
			(
				new SelectionAdapter()
				{
					public void widgetSelected(SelectionEvent e)
					{
						setPageComplete(false);
					}
				}
			);
		
		// set the composite as the control for this page
		setControl(composite);
	}
		
	public boolean canFlipToNextPage()
	{
		DatabaseMeta source = getSourceDatabase();
		DatabaseMeta target = getTargetDatabase();
		
		if (source==null && target==null)
		{
			setErrorMessage(Messages.getString("CopyTableWizardPage1.SourceAndTargetIsNull.DialogMessage")); //$NON-NLS-1$
			return false;
		}
		else
		if (source==null && target!=null)
		{
			setErrorMessage(Messages.getString("CopyTableWizardPage1.SourceIsNull.DialogMessage")); //$NON-NLS-1$
			return false;
		}
		else
		if (source!=null && target==null)
		{
			setErrorMessage(Messages.getString("CopyTableWizardPage1.TargetIsNull.DialogMessage")); //$NON-NLS-1$
			return false;
		}
		else
		if (source!=null && target!=null && source.equals(target))
		{
			setErrorMessage(Messages.getString("CopyTableWizardPage1.SourceAndTargetIsSame.DialogMessage")); //$NON-NLS-1$
			return false;
		}
		else
		{
			setErrorMessage(null);
			setMessage(Messages.getString("CopyTableWizardPage1.GoOnNext.DialogMessage")); //$NON-NLS-1$
			return true;
		}
	}	
	
	public DatabaseMeta getSourceDatabase()
	{
		if (wSourceDB.getSelection().length==1)
		{
			String sourceDbName = wSourceDB.getSelection()[0];
			return DatabaseMeta.findDatabase(databases, sourceDbName);
		}
		return null;
	}
	
	public DatabaseMeta getTargetDatabase()
	{
		if (wTargetDB.getSelection().length==1)
		{
			String targetDbName = wTargetDB.getSelection()[0];
			return DatabaseMeta.findDatabase(databases, targetDbName);
		}
		return null;
	}	
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.WizardPage#getNextPage()
	 */
	public IWizardPage getNextPage()
	{
		CopyTableWizardPage2 page2 = (CopyTableWizardPage2)super.getNextPage();
		if (page2.getInputData())
		{
			page2.getData();
			return page2;
		}
		return this;
	}
}
