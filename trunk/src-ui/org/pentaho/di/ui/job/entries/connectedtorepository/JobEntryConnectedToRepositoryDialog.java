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
 * Created on 10-03-2007
 *
 */

package org.pentaho.di.ui.job.entries.connectedtorepository;

import org.eclipse.swt.SWT;
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
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.ui.core.dialog.EnterSelectionDialog;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.job.dialog.JobDialog;
import org.pentaho.di.ui.job.entry.JobEntryDialog; 
import org.pentaho.di.job.entry.JobEntryDialogInterface;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.RepositoriesMeta;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryMeta;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.job.entries.connectedtorepository.JobEntryConnectedToRepository;
import org.pentaho.di.job.entries.connectedtorepository.Messages;

/**
 * This dialog allows you to edit a JobEntry Connected to repository object.
 * 
 * @author Samatar
 * @since 23-06-2008
 */
public class JobEntryConnectedToRepositoryDialog extends JobEntryDialog implements JobEntryDialogInterface
{
    private Label wlName;

    private Text wName;

    private FormData fdlName, fdName;

  
    private Button wOK, wCancel;

    private Listener lsOK, lsCancel;

    private JobEntryConnectedToRepository jobEntry;

    private Shell shell;

    private SelectionAdapter lsDef;

    private boolean changed;
    
    private Label        wlspecificRep;
    private Button       wspecificRep;
    private FormData     fdlspecificRep, fdspecificRep;
    
    private Label        wlspecificUser;
    private Button       wspecificUser;
    private FormData     fdlspecificUser, fdspecificUser;
    
	private Label wlRepName;
	private TextVar wRepName;
	private FormData fdlRepName, fdRepName;
	
	private Label wlUserName;
	private TextVar wUserName;
	private FormData fdlUserName, fdUserName;
	
	private Button       wbRepositoryname;
	private FormData     fdbRepositoryname;


    public JobEntryConnectedToRepositoryDialog(Shell parent, JobEntryInterface jobEntryInt, Repository rep, JobMeta jobMeta)
    {
        super(parent, jobEntryInt, rep, jobMeta);
        jobEntry = (JobEntryConnectedToRepository) jobEntryInt;
        if (this.jobEntry.getName() == null)
            this.jobEntry.setName(Messages.getString("JobEntryConnectedToRepositoryDialog.Jobname.Label"));
    }

    public JobEntryInterface open()
    {
        Shell parent = getParent();
        Display display = parent.getDisplay();

        shell = new Shell(parent, props.getJobsDialogStyle());
        props.setLook(shell);
        JobDialog.setShellImage(shell, jobEntry);

        ModifyListener lsMod = new ModifyListener()
        {
            public void modifyText(ModifyEvent e)
            {
                jobEntry.setChanged();
            }
        };
        changed = jobEntry.hasChanged();

        FormLayout formLayout = new FormLayout();
        formLayout.marginWidth = Const.FORM_MARGIN;
        formLayout.marginHeight = Const.FORM_MARGIN;

        shell.setLayout(formLayout);
        shell.setText(Messages.getString("JobEntryConnectedToRepositoryDialog.Title"));

        int middle = props.getMiddlePct();
        int margin = Const.MARGIN;
        
        // Filename line
        wlName = new Label(shell, SWT.RIGHT);
        wlName.setText(Messages.getString("JobEntryConnectedToRepositoryDialog.Label"));
        props.setLook(wlName);
        fdlName = new FormData();
        fdlName.left = new FormAttachment(0, 0);
		fdlName.right = new FormAttachment(middle, -margin);
        fdlName.top = new FormAttachment(0, margin);
        wlName.setLayoutData(fdlName);
        wName = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wName);
        wName.addModifyListener(lsMod);
        fdName = new FormData();
        fdName.left = new FormAttachment(middle, 0);
        fdName.top = new FormAttachment(0, margin);
        fdName.right = new FormAttachment(100, 0);
        wName.setLayoutData(fdName);
        
    	
		// Connected to a specific rep?
        wlspecificRep = new Label(shell, SWT.RIGHT);
        wlspecificRep.setText(Messages.getString("JobEntryConnectedToRepositoryDialog.specificRep.Label"));
        props.setLook(wlspecificRep);
        fdlspecificRep = new FormData();
        fdlspecificRep.left = new FormAttachment(0, 0);
        fdlspecificRep.top = new FormAttachment(wName, margin);
        fdlspecificRep.right = new FormAttachment(middle, -margin);
        wlspecificRep.setLayoutData(fdlspecificRep);
        wspecificRep = new Button(shell, SWT.CHECK);
        props.setLook(wspecificRep);
        wspecificRep.setToolTipText(Messages.getString("JobEntryConnectedToRepositoryDialog.specificRep.Tooltip"));
        fdspecificRep = new FormData();
        fdspecificRep.left = new FormAttachment(middle, 0);
        fdspecificRep.top = new FormAttachment(wName, margin);
        fdspecificRep.right = new FormAttachment(100, 0);
        wspecificRep.setLayoutData(fdspecificRep);
        wspecificRep.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent e)
            {
                jobEntry.setChanged();
                activeSpecRep();
            }
        });
        
		// Repository name
		wlRepName = new Label(shell, SWT.RIGHT);
		wlRepName.setText(Messages.getString("JobEntryConnectedToRepositoryDialog.RepName.Label"));
		props.setLook(wlRepName);
		fdlRepName = new FormData();
		fdlRepName.left = new FormAttachment(0, 0);
		fdlRepName.right = new FormAttachment(middle, -margin);
		fdlRepName.top = new FormAttachment(wspecificRep, margin);
		wlRepName.setLayoutData(fdlRepName);
		
		wbRepositoryname=new Button(shell, SWT.PUSH| SWT.CENTER);
 		props.setLook(wbRepositoryname);
		wbRepositoryname.setText(Messages.getString("JobEntryConnectedToRepositoryDialog.ListRepositories.Label"));
		wbRepositoryname.setToolTipText(Messages.getString("JobEntryConnectedToRepositoryDialog.ListRepositories.Tooltip"));
		fdbRepositoryname=new FormData();
		fdbRepositoryname.right= new FormAttachment(100, 0);
		fdbRepositoryname.top  = new FormAttachment(wspecificRep, 0);
		wbRepositoryname.setLayoutData(fdbRepositoryname);
		wbRepositoryname.addSelectionListener( new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { getListRepositories(); } } );
		

		wRepName = new TextVar(jobMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wRepName);
		wRepName.setToolTipText(Messages.getString("JobEntryConnectedToRepositoryDialog.RepName.Tooltip"));
		wRepName.addModifyListener(lsMod);
		fdRepName = new FormData();
		fdRepName.left = new FormAttachment(middle, 0);
		fdRepName.top = new FormAttachment(wspecificRep, margin);
		fdRepName.right = new FormAttachment(wbRepositoryname, -margin);
		wRepName.setLayoutData(fdRepName);
		

		// Connected to a specific user?
        wlspecificUser = new Label(shell, SWT.RIGHT);
        wlspecificUser.setText(Messages.getString("JobEntryConnectedToRepositoryDialog.specificUser.Label"));
        props.setLook(wlspecificUser);
        fdlspecificUser = new FormData();
        fdlspecificUser.left = new FormAttachment(0, 0);
        fdlspecificUser.top = new FormAttachment(wRepName, margin);
        fdlspecificUser.right = new FormAttachment(middle, -margin);
        wlspecificUser.setLayoutData(fdlspecificUser);
        wspecificUser = new Button(shell, SWT.CHECK);
        props.setLook(wspecificUser);
        wspecificUser.setToolTipText(Messages.getString("JobEntryConnectedToRepositoryDialog.specificUser.Tooltip"));
        fdspecificUser = new FormData();
        fdspecificUser.left = new FormAttachment(middle, 0);
        fdspecificUser.top = new FormAttachment(wRepName, margin);
        fdspecificUser.right = new FormAttachment(100, 0);
        wspecificUser.setLayoutData(fdspecificUser);
        wspecificUser.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent e)
            {
                jobEntry.setChanged();
                activeSpecUser();
            }
        });
        
		// Username
		wlUserName = new Label(shell, SWT.RIGHT);
		wlUserName.setText(Messages.getString("JobEntryConnectedToUserositoryDialog.UserName.Label"));
		props.setLook(wlUserName);
		fdlUserName = new FormData();
		fdlUserName.left = new FormAttachment(0, 0);
		fdlUserName.right = new FormAttachment(middle, -margin);
		fdlUserName.top = new FormAttachment(wspecificUser, margin);
		wlUserName.setLayoutData(fdlUserName);
        // Username
		wUserName = new TextVar(jobMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wUserName);
		wUserName.setToolTipText(Messages.getString("JobEntryConnectedToUserositoryDialog.UserName.Tooltip"));
		wUserName.addModifyListener(lsMod);
		fdUserName = new FormData();
		fdUserName.left = new FormAttachment(middle, 0);
		fdUserName.top = new FormAttachment(wspecificUser, margin);
		fdUserName.right = new FormAttachment(100, -margin);
		wUserName.setLayoutData(fdUserName);
		
        wOK = new Button(shell, SWT.PUSH);
        wOK.setText(Messages.getString("System.Button.OK"));
        wCancel = new Button(shell, SWT.PUSH);
        wCancel.setText(Messages.getString("System.Button.Cancel"));

        // at the bottom
        BaseStepDialog.positionBottomButtons(shell, new Button[] { wOK, wCancel }, margin, wUserName);
	
	
		// Add listeners
        lsCancel = new Listener()
        {
            public void handleEvent(Event e)
            {
                cancel();
            }
        };

        lsOK = new Listener()
        {
            public void handleEvent(Event e)
            {
                ok();
            }
        };

        wCancel.addListener(SWT.Selection, lsCancel);
        wOK.addListener(SWT.Selection, lsOK);

        lsDef = new SelectionAdapter()
        {
            public void widgetDefaultSelected(SelectionEvent e)
            {
                ok();
            }
        };

        wName.addSelectionListener(lsDef);

        // Detect X or ALT-F4 or something that kills this window...
        shell.addShellListener(new ShellAdapter()
        {
            public void shellClosed(ShellEvent e)
            {
                cancel();
            }
        });


        getData();
        activeSpecRep();
        activeSpecUser();
        BaseStepDialog.setSize(shell);

        shell.open();
        props.setDialogSize(shell, "JobSuccessDialogSize");
        while (!shell.isDisposed())
        {
            if (!display.readAndDispatch())
                display.sleep();
        }
        return jobEntry;
    }
    private void activeSpecRep()
    {
    	wlRepName.setEnabled(wspecificRep.getSelection());
    	wRepName.setEnabled(wspecificRep.getSelection());
    	wbRepositoryname.setEnabled(wspecificRep.getSelection());
    }
    private void activeSpecUser()
    {
    	wlUserName.setEnabled(wspecificUser.getSelection());
    	wUserName.setEnabled(wspecificUser.getSelection());
    }
    public void dispose()
    {
        WindowProperty winprop = new WindowProperty(shell);
        props.setScreen(winprop);
        shell.dispose();
    }

    /**
     * Copy information from the meta-data input to the dialog fields.
     */
    public void getData()
    {
        if (jobEntry.getName() != null) wName.setText(jobEntry.getName());
        wspecificRep.setSelection(jobEntry.isSpecificRep());
        if (jobEntry.getRepName() != null)  wRepName.setText(jobEntry.getRepName());
        wspecificUser.setSelection(jobEntry.isSpecificUser());
        if (jobEntry.getUserName() != null)  wUserName.setText(jobEntry.getUserName());
        wName.selectAll();
		
    }

    private void cancel()
    {
        jobEntry.setChanged(changed);
        jobEntry = null;
        dispose();
    }

    private void ok()
    {
	   if(Const.isEmpty(wName.getText())) 
        {
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
			mb.setText(Messages.getString("System.StepJobEntryNameMissing.Title"));
			mb.setMessage(Messages.getString("System.JobEntryNameMissing.Msg"));
			mb.open(); 
			return;
        }
        jobEntry.setName(wName.getText());
        jobEntry.setSpecificRep(wspecificRep.getSelection());
        jobEntry.setRepName(wRepName.getText());
        jobEntry.setSpecificUser(wspecificUser.getSelection());
        jobEntry.setUserName(wUserName.getText());
        dispose();
    }

    public String toString()
    {
        return this.getClass().getName();
    }
	private void displayMsg(String title, String message, boolean error)
	{
		if(error)
		{
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
			mb.setMessage(message +Const.CR);
			mb.setText(title);
			mb.open();
		}else
		{
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_INFORMATION );
			mb.setMessage(message +Const.CR);
			mb.setText(title);
			mb.open();
		}
	}
    /**
	 * Get a list of repositories defined in this system, allow the user to select from it.
	 *
	 */
	private void getListRepositories()
	{
		RepositoriesMeta reps_info =null;
		try
		{
			LogWriter log = LogWriter.getInstance();
			reps_info = new RepositoriesMeta(log);
			if (!reps_info.readData())
			{
				displayMsg(Messages.getString("JobEntryConnectedToRepositoryDialog.Error.NoRepsDefined"),Messages.getString("JobEntryConnectedToRepositoryDialog.Error.NoRepsDefinedMsg"),true);
			}else
			{
				int nrRepositories=reps_info.nrRepositories();
				if(nrRepositories==0)
				{
					displayMsg(Messages.getString("System.Dialog.Error.Title"), Messages.getString("JobEntryConnectedToRepositoryDialog.Error.NoRep.DialogMessage"), true);
				}else
				{
					String available[] = new String[nrRepositories];
	                
					for (int i=0;i<nrRepositories;i++)
					{
						RepositoryMeta ri = reps_info.getRepository(i);
						available[i]=ri.getName();
					}
					
					String[] source = new String[1];
					source[0]=wRepName.getText();
					int idxSource[] = Const.indexsOfStrings(source, available);
					EnterSelectionDialog dialog = new EnterSelectionDialog(shell, available, Messages.getString("JobEntryConnectedToRepositoryDialog.SelectRepository.Title"), Messages.getString("JobEntryConnectedToRepositoryDialog.SelectRepository.Message"));
					dialog.setMulti(false);
					dialog.setSelectedNrs(idxSource);
					if (dialog.open()!=null)
					{
						int idx[] = dialog.getSelectionIndeces();
						wRepName.setText(available[idx[0]]);
					}
				}
			}
		}
		catch(Exception e)
		{
			displayMsg(Messages.getString("System.Dialog.Error.Title"), Messages.getString("JobEntryConnectedToRepositoryDialog.ErrorGettingRepositories.DialogMessage")+Const.CR+":"+e.getMessage(), true);
		}finally
		{
			reps_info.clear();
		}
	}
}