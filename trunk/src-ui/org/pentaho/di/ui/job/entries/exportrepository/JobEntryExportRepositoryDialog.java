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
 /**********************************************************************
 **                                                                   **
 **               This code belongs to the KETTLE project.            **
 **                                                                   **
 **                                                                   **
 **                                                                   **
 **********************************************************************/

package org.pentaho.di.ui.job.entries.exportrepository;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox; 
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Group;

import org.pentaho.di.core.util.StringUtil;
import org.pentaho.di.ui.repository.dialog.SelectDirectoryDialog;
import org.pentaho.di.ui.core.dialog.EnterSelectionDialog;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.Const;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.job.dialog.JobDialog;
import org.pentaho.di.ui.core.widget.LabelTextVar;
import org.pentaho.di.ui.job.entry.JobEntryDialog;
import org.pentaho.di.job.entry.JobEntryDialogInterface;
import org.pentaho.di.repository.RepositoryDirectory;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoriesMeta;
import org.pentaho.di.repository.RepositoryMeta;
import org.pentaho.di.repository.UserInfo;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.job.entries.exportrepository.JobEntryExportRepository;
import org.pentaho.di.job.entries.exportrepository.Messages;


/**
 * This dialog allows you to edit the Export repository job entry settings.
 *
 * @author Samatar
 * @since  04-06-2008
 */
public class JobEntryExportRepositoryDialog extends JobEntryDialog implements JobEntryDialogInterface
{
	private static final String[] FILETYPES = new String[] 
	                                     {Messages.getString("JobExportRepository.Filetype.XmlFiles"),
	                                       Messages.getString("JobExportRepository.Filetype.AllFiles") };
	
	private Label        wlName;
	private Text         wName;
    private FormData     fdlName, fdName;

	private Label        wlRepositoryname;
	private Button       wbRepositoryname;
	private TextVar      wRepositoryname;
	private FormData     fdlRepositoryname, fdbRepositoryname, fdRepositoryname;
	
	private Label        wlFoldername;
	private Button       wbFoldername;
	private TextVar      wFoldername;
	private FormData     fdlFoldername, fdbFoldername, fdFoldername;
	
	private Label wlTargetFilename;
	private FormData fdlTargetFilename;
	
    private LabelTextVar wUserName;

    private FormData fdUserName;

    private LabelTextVar wPassword;

    private FormData fdPassword;
    
    private TextVar wTargetFilename;

    private FormData fdTargetFilename;
    
    private Button wbTargetFilename;
    private FormData fdbTargetFilename;
    private Button wbTargetFoldername;
    private FormData fdbTargetFoldername;
    
	private Group wRepositoryInfos,wTarget,wSettings;
    private FormData fdRepositoryInfos,fdTarget,fdSettings;
    
	private Label wlIfFileExists;
	private  CCombo wIfFileExists;
	private FormData fdlIfFileExists, fdIfFileExists;
	
	private Label wlExportType;
	private  CCombo wExportType;
	private FormData fdlExportType, fdExportType;

	private Button wOK, wCancel;
	private Listener lsOK, lsCancel;

	private JobEntryExportRepository jobEntry;
	private Shell       	shell;
	private Button wTest;
	
	private FormData fdTest;
	
	private Listener lsTest;

	private SelectionAdapter lsDef;

	private boolean changed;
	
	private Label        wlAddDate;
	private Button       wAddDate;
	private FormData     fdlAddDate, fdAddDate;
	
	private Label        wlcreateFolder;
	private Button       wcreateFolder;
	private FormData     fdlcreateFolder, fdcreateFolder;

	private Label        wlAddTime;
	private Button       wAddTime;
	private FormData     fdlAddTime, fdAddTime;
	
	private Label        wlNewFolder;
	private Button       wNewFolder;
	private FormData     fdlNewFolder, fdNewFolder;
	
	
	private Label        wlSpecifyFormat;
	private Button       wSpecifyFormat;
	private FormData     fdlSpecifyFormat, fdSpecifyFormat;

  	private Label        wlDateTimeFormat;
	private CCombo       wDateTimeFormat;
	private FormData     fdlDateTimeFormat, fdDateTimeFormat; 
	
	private Label        wlAddFileToResult;
	private Button       wAddFileToResult;
	private FormData     fdlAddFileToResult, fdAddFileToResult;

	private CTabFolder   wTabFolder;
	private Composite    wGeneralComp,wAdvancedComp;	
	private CTabItem     wGeneralTab,wAdvancedTab;
	private FormData	 fdGeneralComp,fdAdvancedComp;
	private FormData     fdTabFolder;
	
	private Label wlSuccessCondition;
	private CCombo wSuccessCondition;
	private FormData fdlSuccessCondition, fdSuccessCondition;
	
	
	private Label wlLimit;
	private TextVar wLimit;
	private FormData fdlLimit, fdLimit;
	
	private Group wSuccessOn;
    private FormData fdSuccessOn;
	

    public JobEntryExportRepositoryDialog(Shell parent, JobEntryInterface jobEntryInt, Repository rep, JobMeta jobMeta)
    {
        super(parent, jobEntryInt, rep, jobMeta);
        jobEntry = (JobEntryExportRepository) jobEntryInt;
        if (this.jobEntry.getName() == null)
            this.jobEntry.setName(Messages.getString("JobExportRepository.Name.Default"));
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

		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText(Messages.getString("JobExportRepository.Title"));

		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Repositoryname line
		wlName=new Label(shell, SWT.RIGHT);
		wlName.setText(Messages.getString("JobExportRepository.Name.Label"));
 		props.setLook(wlName);
		fdlName=new FormData();
		fdlName.left = new FormAttachment(0, 0);
		fdlName.right= new FormAttachment(middle, -margin);
		fdlName.top  = new FormAttachment(0, margin);
		wlName.setLayoutData(fdlName);
		wName=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wName);
		wName.addModifyListener(lsMod);
		fdName=new FormData();
		fdName.left = new FormAttachment(middle, 0);
		fdName.top  = new FormAttachment(0, margin);
		fdName.right= new FormAttachment(100, 0);
		wName.setLayoutData(fdName);
		
        wTabFolder = new CTabFolder(shell, SWT.BORDER);
 		props.setLook(wTabFolder, Props.WIDGET_STYLE_TAB);
		
		//////////////////////////
		// START OF GENERAL TAB   ///
		//////////////////////////
		
		wGeneralTab=new CTabItem(wTabFolder, SWT.NONE);
		wGeneralTab.setText(Messages.getString("JobExportRepository.Tab.General.Label"));
		
		wGeneralComp = new Composite(wTabFolder, SWT.NONE);
 		props.setLook(wGeneralComp);

		FormLayout generalLayout = new FormLayout();
		generalLayout.marginWidth  = 3;
		generalLayout.marginHeight = 3;
		wGeneralComp.setLayout(generalLayout);
		
	    // ////////////////////////
	     // START OF Repository Infos GROUP///
	     // /
	    wRepositoryInfos = new Group(wGeneralComp, SWT.SHADOW_NONE);
	    props.setLook(wRepositoryInfos);
	    wRepositoryInfos.setText(Messages.getString("JobExportRepository.RepositoryInfos.Group.Label"));

	    FormLayout RepositoryInfosgroupLayout = new FormLayout();
	    RepositoryInfosgroupLayout.marginWidth = 10;
	    RepositoryInfosgroupLayout.marginHeight = 10;

	    wRepositoryInfos.setLayout(RepositoryInfosgroupLayout);

		// Repositoryname line
		wlRepositoryname=new Label(wRepositoryInfos, SWT.RIGHT);
		wlRepositoryname.setText(Messages.getString("JobExportRepository.Repositoryname.Label"));
 		props.setLook(wlRepositoryname);
		fdlRepositoryname=new FormData();
		fdlRepositoryname.left = new FormAttachment(0, margin);
		fdlRepositoryname.top  = new FormAttachment(wName, margin);
		fdlRepositoryname.right= new FormAttachment(middle, -margin);
		wlRepositoryname.setLayoutData(fdlRepositoryname);

		wbRepositoryname=new Button(wRepositoryInfos, SWT.PUSH| SWT.CENTER);
 		props.setLook(wbRepositoryname);
		wbRepositoryname.setText(Messages.getString("JobExportRepository.ListRepositories.Label"));
		wbRepositoryname.setToolTipText(Messages.getString("JobExportRepository.ListRepositories.Tooltip"));
		fdbRepositoryname=new FormData();
		fdbRepositoryname.right= new FormAttachment(100, 0);
		fdbRepositoryname.top  = new FormAttachment(wName, 0);
		wbRepositoryname.setLayoutData(fdbRepositoryname);
		wbRepositoryname.addSelectionListener( new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { getListRepositories(); } } );
		
		wRepositoryname=new TextVar(jobMeta,wRepositoryInfos, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wRepositoryname);
		wRepositoryname.addModifyListener(lsMod);
		fdRepositoryname=new FormData();
		fdRepositoryname.left = new FormAttachment(middle, margin);
		fdRepositoryname.top  = new FormAttachment(wName, margin);
		fdRepositoryname.right= new FormAttachment(wbRepositoryname, -margin);
		wRepositoryname.setLayoutData(fdRepositoryname);

		// Whenever something changes, set the tooltip to the expanded version:
		wRepositoryname.addModifyListener(new ModifyListener()
			{
				public void modifyText(ModifyEvent e)
				{
					wRepositoryname.setToolTipText(jobMeta.environmentSubstitute( wRepositoryname.getText() ) );
				}
			}
		);

		  // UserName line
        wUserName = new LabelTextVar(jobMeta,wRepositoryInfos, Messages.getString("JobExportRepository.User.Label"), Messages
            .getString("JobExportRepository.User.Tooltip"));
        props.setLook(wUserName);
        wUserName.addModifyListener(lsMod);
        fdUserName = new FormData();
        fdUserName.left = new FormAttachment(0, 0);
        fdUserName.top = new FormAttachment(wRepositoryname, margin);
        fdUserName.right = new FormAttachment(100, 0);
        wUserName.setLayoutData(fdUserName);

        // Password line
        wPassword = new LabelTextVar(jobMeta,wRepositoryInfos, Messages.getString("JobExportRepository.Password.Label"), Messages
            .getString("JobExportRepository.Password.Tooltip"));
        props.setLook(wPassword);
        wPassword.setEchoChar('*');
        wPassword.addModifyListener(lsMod);
        fdPassword = new FormData();
        fdPassword.left = new FormAttachment(0, 0);
        fdPassword.top = new FormAttachment(wUserName, margin);
        fdPassword.right = new FormAttachment(100, 0);
        wPassword.setLayoutData(fdPassword);
        // OK, if the password contains a variable, we don't want to have the password hidden...
        wPassword.getTextWidget().addModifyListener(new ModifyListener()
        {
            public void modifyText(ModifyEvent e)
            {
                checkPasswordVisible();
            }
        });

        
		// Test connection button
		wTest=new Button(wRepositoryInfos,SWT.PUSH);
		wTest.setText(Messages.getString("JobExportRepository.TestConnection.Label"));
 		props.setLook(wTest);
		fdTest=new FormData();
		wTest.setToolTipText(Messages.getString("JobExportRepository.TestConnection.Tooltip"));
		//fdTest.left = new FormAttachment(middle, 0);
		fdTest.top  = new FormAttachment(wPassword, 2*margin);
		fdTest.right= new FormAttachment(100, 0);
		wTest.setLayoutData(fdTest);
		

	     fdRepositoryInfos = new FormData();
	     fdRepositoryInfos.left = new FormAttachment(0, margin);
	     fdRepositoryInfos.top = new FormAttachment(wName, margin);
	     fdRepositoryInfos.right = new FormAttachment(100, -margin);
	     wRepositoryInfos.setLayoutData(fdRepositoryInfos);
	     // ///////////////////////////////////////////////////////////
	     // / END OF Repository Infos GROUP
	     // ///////////////////////////////////////////////////////////
		 
	     
	     // ////////////////////////
	     // START OF Settings GROUP///
	     // //////////////////////////////
	   
	     wSettings = new Group(wGeneralComp, SWT.SHADOW_NONE);
	     props.setLook(wSettings);
	     wSettings.setText(Messages.getString("JobExportRepository.Settings.Group.Label"));

	    FormLayout SettingsgroupLayout = new FormLayout();
	    SettingsgroupLayout.marginWidth = 10;
	    SettingsgroupLayout.marginHeight = 10;
	    wSettings.setLayout(SettingsgroupLayout);
	     
	    
	    wlExportType = new Label(wSettings, SWT.RIGHT);
		wlExportType.setText(Messages.getString("JobExportRepository.ExportType.Label"));
		props.setLook(wlExportType);
		fdlExportType = new FormData();
		fdlExportType.left = new FormAttachment(0, 0);
		fdlExportType.right = new FormAttachment(middle, 0);
		fdlExportType.top = new FormAttachment(wRepositoryInfos, margin);
		wlExportType.setLayoutData(fdlExportType);
		wExportType = new CCombo(wSettings, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER);
		wExportType.add(Messages.getString("JobExportRepository.Export_All.Label"));
		wExportType.add(Messages.getString("JobExportRepository.Export_Jobs.Label"));
		wExportType.add(Messages.getString("JobExportRepository.Export_Trans.Label"));
		wExportType.add(Messages.getString("JobExportRepository.Export_By_Folder.Label"));
		wExportType.add(Messages.getString("JobExportRepository.Export_One_Folder.Label"));
		wExportType.select(0); // +1: starts at -1

		props.setLook(wExportType);
		fdExportType= new FormData();
		fdExportType.left = new FormAttachment(middle, margin);
		fdExportType.top = new FormAttachment(wTargetFilename, margin);
		fdExportType.right = new FormAttachment(100, 0);
		wExportType.setLayoutData(fdExportType);
		wExportType.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				 activeOneFolder();		
			}
		});

		
		
		
		// Foldername line
		wlFoldername=new Label(wSettings, SWT.RIGHT);
		wlFoldername.setText(Messages.getString("JobExportRepository.Foldername.Label"));
 		props.setLook(wlFoldername);
		fdlFoldername=new FormData();
		fdlFoldername.left = new FormAttachment(0, margin);
		fdlFoldername.top  = new FormAttachment(wExportType, margin);
		fdlFoldername.right= new FormAttachment(middle, -margin);
		wlFoldername.setLayoutData(fdlFoldername);

		wbFoldername=new Button(wSettings, SWT.PUSH| SWT.CENTER);
 		props.setLook(wbFoldername);
		//wbFoldername.setText(Messages.getString("JobExportRepository.ListFolders.Label"));
		wbFoldername.setToolTipText(Messages.getString("JobExportRepository.ListFolders.Tooltip"));
		wbFoldername.setImage(GUIResource.getInstance().getImageBol());
		fdbFoldername=new FormData();
		fdbFoldername.right= new FormAttachment(100, 0);
		fdbFoldername.top  = new FormAttachment(wExportType, 0);
		wbFoldername.setLayoutData(fdbFoldername);
		wbFoldername.addSelectionListener( new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { displaydirectoryList(); } } );


		wFoldername=new TextVar(jobMeta,wSettings, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wFoldername);
		wFoldername.addModifyListener(lsMod);
		fdFoldername=new FormData();
		fdFoldername.left = new FormAttachment(middle, margin);
		fdFoldername.top  = new FormAttachment(wExportType, margin);
		fdFoldername.right= new FormAttachment(wbFoldername, -margin);
		wFoldername.setLayoutData(fdFoldername);

		// Whenever something changes, set the tooltip to the expanded version:
		wFoldername.addModifyListener(new ModifyListener()
			{
				public void modifyText(ModifyEvent e)
				{
					wFoldername.setToolTipText(jobMeta.environmentSubstitute( wFoldername.getText() ) );
				}
			}
		);

		  // export each directory to a new folder?
		wlNewFolder=new Label(wSettings, SWT.RIGHT);
		wlNewFolder.setText(Messages.getString("JobExportRepository.NewFolder.Label"));
 		props.setLook(wlNewFolder);
		fdlNewFolder=new FormData();
		fdlNewFolder.left = new FormAttachment(0, 0);
		fdlNewFolder.top  = new FormAttachment(wFoldername, margin);
		fdlNewFolder.right= new FormAttachment(middle, -margin);
		wlNewFolder.setLayoutData(fdlNewFolder);
		wNewFolder=new Button(wSettings, SWT.CHECK);
 		props.setLook(wNewFolder);
 		wNewFolder.setToolTipText(Messages.getString("JobExportRepository.NewFolder.Tooltip"));
		fdNewFolder=new FormData();
		fdNewFolder.left = new FormAttachment(middle, margin);
		fdNewFolder.top  = new FormAttachment(wFoldername, margin);
		fdNewFolder.right= new FormAttachment(100, 0);
		wNewFolder.setLayoutData(fdNewFolder);
		wNewFolder.addSelectionListener(new SelectionAdapter() 
			{
				public void widgetSelected(SelectionEvent e) 
				{
					jobEntry.setChanged();
				}
			}
		);
	    
	     fdSettings = new FormData();
	     fdSettings.left = new FormAttachment(0, margin);
	     fdSettings.top = new FormAttachment(wRepositoryInfos, margin);
	     fdSettings.right = new FormAttachment(100, -margin);
	     wSettings.setLayoutData(fdSettings);
	     // ///////////////////////////////////////////////////////////
	     // / END OF Settings GROUP
	     // ///////////////////////////////////////////////////////////
		 
	     
	     // ////////////////////////
	     // START OF Target Filename GROUP///
	     // //////////////////////////////
	   
	     wTarget = new Group(wGeneralComp, SWT.SHADOW_NONE);
	     props.setLook(wTarget);
	     wTarget.setText(Messages.getString("JobExportRepository.Target.Group.Label"));

	    FormLayout TargetgroupLayout = new FormLayout();
	    TargetgroupLayout.marginWidth = 10;
	    TargetgroupLayout.marginHeight = 10;
	    wTarget.setLayout(TargetgroupLayout);
	     
        // Target filename line
		wlTargetFilename=new Label(wTarget, SWT.RIGHT);
		wlTargetFilename.setText(Messages.getString("JobExportRepository.TargetFilename.Label"));
		props.setLook(wlTargetFilename);
		fdlTargetFilename=new FormData();
		fdlTargetFilename.left = new FormAttachment(0, 0);
		fdlTargetFilename.top  = new FormAttachment(wSettings, margin);
		fdlTargetFilename.right= new FormAttachment(middle, -margin);
		wlTargetFilename.setLayoutData(fdlTargetFilename);
		
        // Browse Source folders button ...
		wbTargetFilename=new Button(wTarget, SWT.PUSH| SWT.CENTER);
		props.setLook(wbTargetFilename);
		wbTargetFilename.setText(Messages.getString("JobExportRepository.BrowseFolders.Label"));
		fdbTargetFilename=new FormData();
		fdbTargetFilename.right= new FormAttachment(100, 0);
		fdbTargetFilename.top  = new FormAttachment(wSettings, margin);
		wbTargetFilename.setLayoutData(fdbTargetFilename);
		
		wbTargetFilename.addSelectionListener
		(
			new SelectionAdapter()
			{
				public void widgetSelected(SelectionEvent e)
				{
					DirectoryDialog ddialog = new DirectoryDialog(shell, SWT.OPEN);
					if (wTargetFilename.getText()!=null)
					{
						ddialog.setFilterPath(jobMeta.environmentSubstitute(wTargetFilename.getText()) );
					}
					
					 // Calling open() will open and run the dialog.
			        // It will return the selected directory, or
			        // null if user cancels
			        String dir = ddialog.open();
			        if (dir != null) {
			          // Set the text box to the new selection
			        	wTargetFilename.setText(dir);
			        }
					
				}
			}
		);
		
		// Browse Source files button ...
		wbTargetFoldername=new Button(wTarget, SWT.PUSH| SWT.CENTER);
		props.setLook(wbTargetFoldername);
		wbTargetFoldername.setText(Messages.getString("JobExportRepository.BrowseFiles.Label"));
		fdbTargetFoldername=new FormData();
		fdbTargetFoldername.right= new FormAttachment(wbTargetFilename, -margin);
		fdbTargetFoldername.top  = new FormAttachment(wSettings, margin);
		wbTargetFoldername.setLayoutData(fdbTargetFoldername);
		wbTargetFoldername.addSelectionListener
		(
				new SelectionAdapter()
			{
				public void widgetSelected(SelectionEvent e)
				{
					FileDialog dialog = new FileDialog(shell, SWT.OPEN);
					dialog.setFilterExtensions(new String[] {"*.xml",".*"});
					if (wTargetFilename.getText()!=null)
					{
						dialog.setFileName(jobMeta.environmentSubstitute(wTargetFilename.getText()) );
					}
					dialog.setFilterNames(FILETYPES);
					if (dialog.open()!=null)
					{
						wTargetFilename.setText(dialog.getFilterPath()+Const.FILE_SEPARATOR+dialog.getFileName());
					}
				}
			}
				);
	     // Target filename line
	    wTargetFilename = new TextVar(jobMeta,wTarget, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wTargetFilename);
        wTargetFilename.setToolTipText(Messages.getString("JobExportRepository.TargetFilename.Tooltip"));
        wTargetFilename.addModifyListener(lsMod);
        fdTargetFilename = new FormData();
        fdTargetFilename.left = new FormAttachment(middle, margin);
        fdTargetFilename.top = new FormAttachment(wSettings, margin);
        fdTargetFilename.right = new FormAttachment(wbTargetFoldername, -margin);
        wTargetFilename.setLayoutData(fdTargetFilename);
        
        // create folder or parent folder?
		wlcreateFolder=new Label(wTarget, SWT.RIGHT);
		wlcreateFolder.setText(Messages.getString("JobExportRepository.createFolder.Label"));
 		props.setLook(wlcreateFolder);
		fdlcreateFolder=new FormData();
		fdlcreateFolder.left = new FormAttachment(0, 0);
		fdlcreateFolder.top  = new FormAttachment(wTargetFilename, margin);
		fdlcreateFolder.right= new FormAttachment(middle, -margin);
		wlcreateFolder.setLayoutData(fdlcreateFolder);
		wcreateFolder=new Button(wTarget, SWT.CHECK);
 		props.setLook(wcreateFolder);
 		wcreateFolder.setToolTipText(Messages.getString("JobExportRepository.createFolder.Tooltip"));
		fdcreateFolder=new FormData();
		fdcreateFolder.left = new FormAttachment(middle, margin);
		fdcreateFolder.top  = new FormAttachment(wTargetFilename, margin);
		fdcreateFolder.right= new FormAttachment(100, 0);
		wcreateFolder.setLayoutData(fdcreateFolder);
		wcreateFolder.addSelectionListener(new SelectionAdapter() 
			{
				public void widgetSelected(SelectionEvent e) 
				{
					jobEntry.setChanged();
				}
			}
		);
        
        // Create multi-part file?
		wlAddDate=new Label(wTarget, SWT.RIGHT);
		wlAddDate.setText(Messages.getString("JobExportRepository.AddDate.Label"));
 		props.setLook(wlAddDate);
		fdlAddDate=new FormData();
		fdlAddDate.left = new FormAttachment(0, 0);
		fdlAddDate.top  = new FormAttachment(wcreateFolder, margin);
		fdlAddDate.right= new FormAttachment(middle, -margin);
		wlAddDate.setLayoutData(fdlAddDate);
		wAddDate=new Button(wTarget, SWT.CHECK);
 		props.setLook(wAddDate);
 		wAddDate.setToolTipText(Messages.getString("JobExportRepository.AddDate.Tooltip"));
		fdAddDate=new FormData();
		fdAddDate.left = new FormAttachment(middle, margin);
		fdAddDate.top  = new FormAttachment(wcreateFolder, margin);
		fdAddDate.right= new FormAttachment(100, 0);
		wAddDate.setLayoutData(fdAddDate);
		wAddDate.addSelectionListener(new SelectionAdapter() 
			{
				public void widgetSelected(SelectionEvent e) 
				{
					jobEntry.setChanged();
				}
			}
		);
		// Create multi-part file?
		wlAddTime=new Label(wTarget, SWT.RIGHT);
		wlAddTime.setText(Messages.getString("JobExportRepository.AddTime.Label"));
 		props.setLook(wlAddTime);
		fdlAddTime=new FormData();
		fdlAddTime.left = new FormAttachment(0, 0);
		fdlAddTime.top  = new FormAttachment(wAddDate, margin);
		fdlAddTime.right= new FormAttachment(middle, -margin);
		wlAddTime.setLayoutData(fdlAddTime);
		wAddTime=new Button(wTarget, SWT.CHECK);
 		props.setLook(wAddTime);
 		wAddTime.setToolTipText(Messages.getString("JobExportRepository.AddTime.Tooltip"));
		fdAddTime=new FormData();
		fdAddTime.left = new FormAttachment(middle, margin);
		fdAddTime.top  = new FormAttachment(wAddDate, margin);
		fdAddTime.right= new FormAttachment(100, 0);
		wAddTime.setLayoutData(fdAddTime);
		wAddTime.addSelectionListener(new SelectionAdapter() 
			{
				public void widgetSelected(SelectionEvent e) 
				{
					jobEntry.setChanged();
				}
			}
		);

		// Specify date time format?
		wlSpecifyFormat=new Label(wTarget, SWT.RIGHT);
		wlSpecifyFormat.setText(Messages.getString("JobExportRepository.SpecifyFormat.Label"));
		props.setLook(wlSpecifyFormat);
		fdlSpecifyFormat=new FormData();
		fdlSpecifyFormat.left = new FormAttachment(0, 0);
		fdlSpecifyFormat.top  = new FormAttachment(wAddTime, margin);
		fdlSpecifyFormat.right= new FormAttachment(middle, -margin);
		wlSpecifyFormat.setLayoutData(fdlSpecifyFormat);
		wSpecifyFormat=new Button(wTarget, SWT.CHECK);
		props.setLook(wSpecifyFormat);
		wSpecifyFormat.setToolTipText(Messages.getString("JobExportRepository.SpecifyFormat.Tooltip"));
	    fdSpecifyFormat=new FormData();
		fdSpecifyFormat.left = new FormAttachment(middle, margin);
		fdSpecifyFormat.top  = new FormAttachment(wAddTime, margin);
		fdSpecifyFormat.right= new FormAttachment(100, 0);
		wSpecifyFormat.setLayoutData(fdSpecifyFormat);
		wSpecifyFormat.addSelectionListener(new SelectionAdapter() 
			{
				public void widgetSelected(SelectionEvent e) 
				{
					jobEntry.setChanged();
					setDateTimeFormat();
				}
			}
		);

		
 		// DateTimeFormat
		wlDateTimeFormat=new Label(wTarget, SWT.RIGHT);
        wlDateTimeFormat.setText(Messages.getString("JobExportRepository.DateTimeFormat.Label"));
        props.setLook(wlDateTimeFormat);
        fdlDateTimeFormat=new FormData();
        fdlDateTimeFormat.left = new FormAttachment(0, 0);
        fdlDateTimeFormat.top  = new FormAttachment(wSpecifyFormat, margin);
        fdlDateTimeFormat.right= new FormAttachment(middle, -margin);
        wlDateTimeFormat.setLayoutData(fdlDateTimeFormat);
        wDateTimeFormat=new CCombo(wTarget, SWT.BORDER | SWT.READ_ONLY);
        wDateTimeFormat.setEditable(true);
        props.setLook(wDateTimeFormat);
        wDateTimeFormat.addModifyListener(lsMod);
        fdDateTimeFormat=new FormData();
        fdDateTimeFormat.left = new FormAttachment(middle, margin);
        fdDateTimeFormat.top  = new FormAttachment(wSpecifyFormat, margin);
        fdDateTimeFormat.right= new FormAttachment(100, 0);
        wDateTimeFormat.setLayoutData(fdDateTimeFormat);
		//	Prepare a list of possible DateTimeFormats...
		String dats[] = Const.getDateFormats();
        for (int x=0;x<dats.length;x++) wDateTimeFormat.add(dats[x]);
        
        
        // If File Exists
		wlIfFileExists = new Label(wTarget, SWT.RIGHT);
		wlIfFileExists.setText(Messages.getString("JobExportRepository.IfFileExists.Label"));
		props.setLook(wlIfFileExists);
		fdlIfFileExists = new FormData();
		fdlIfFileExists.left = new FormAttachment(0, 0);
		fdlIfFileExists.right = new FormAttachment(middle, 0);
		fdlIfFileExists.top = new FormAttachment(wDateTimeFormat, margin);
		wlIfFileExists.setLayoutData(fdlIfFileExists);
		wIfFileExists = new CCombo(wTarget, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER);
		wIfFileExists.add(Messages.getString("JobExportRepository.Do_Nothing_IfFileExists.Label"));
		wIfFileExists.add(Messages.getString("JobExportRepository.Overwrite_File_IfFileExists.Label"));
		wIfFileExists.add(Messages.getString("JobExportRepository.Unique_Name_IfFileExists.Label"));;
		wIfFileExists.add(Messages.getString("JobExportRepository.Fail_IfFileExists.Label"));
		wIfFileExists.select(0); // +1: starts at -1

		props.setLook(wIfFileExists);
		fdIfFileExists= new FormData();
		fdIfFileExists.left = new FormAttachment(middle, margin);
		fdIfFileExists.top = new FormAttachment(wDateTimeFormat, margin);
		fdIfFileExists.right = new FormAttachment(100, 0);
		wIfFileExists.setLayoutData(fdIfFileExists);
		
		//Add file to result
		wlAddFileToResult = new Label(wTarget, SWT.RIGHT);
		wlAddFileToResult.setText(Messages.getString("JobExportRepository.AddFileToResult.Label"));
		props.setLook(wlAddFileToResult);
		fdlAddFileToResult = new FormData();
		fdlAddFileToResult.left = new FormAttachment(0, 0);
		fdlAddFileToResult.top = new FormAttachment(wIfFileExists, margin);
		fdlAddFileToResult.right = new FormAttachment(middle, -margin);
		wlAddFileToResult.setLayoutData(fdlAddFileToResult);
		wAddFileToResult = new Button(wTarget, SWT.CHECK);
		props.setLook(wAddFileToResult);
		wAddFileToResult.setToolTipText(Messages.getString("JobExportRepository.AddFileToResult.Tooltip"));
		fdAddFileToResult = new FormData();
		fdAddFileToResult.left = new FormAttachment(middle, margin);
		fdAddFileToResult.top = new FormAttachment(wIfFileExists, margin);
		fdAddFileToResult.right = new FormAttachment(100, 0);
		wAddFileToResult.setLayoutData(fdAddFileToResult);
		wAddFileToResult.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				jobEntry.setChanged();
			}
		});
	      
		
	     fdTarget = new FormData();
	     fdTarget.left = new FormAttachment(0, margin);
	     fdTarget.top = new FormAttachment(wSettings, margin);
	     fdTarget.right = new FormAttachment(100, -margin);
	     wTarget.setLayoutData(fdTarget);
	     // ///////////////////////////////////////////////////////////
	     // / END OF Target GROUP
	     // ///////////////////////////////////////////////////////////
		 

		fdGeneralComp=new FormData();
		fdGeneralComp.left  = new FormAttachment(0, 0);
		fdGeneralComp.top   = new FormAttachment(0, 0);
		fdGeneralComp.right = new FormAttachment(100, 0);
		fdGeneralComp.bottom= new FormAttachment(100, 0);
		wGeneralComp.setLayoutData(fdGeneralComp);
		
		wGeneralComp.layout();
		wGeneralTab.setControl(wGeneralComp);
 		props.setLook(wGeneralComp);
 		
 		
 		
		/////////////////////////////////////////////////////////////
		/// END OF GENERAL TAB
		/////////////////////////////////////////////////////////////
		
 		
 		//////////////////////////////////////
		// START OF ADVANCED  TAB   ///
		/////////////////////////////////////
		
		wAdvancedTab=new CTabItem(wTabFolder, SWT.NONE);
		wAdvancedTab.setText(Messages.getString("JobExportRepository.Tab.Advanced.Label"));

		FormLayout contentLayout = new FormLayout ();
		contentLayout.marginWidth  = 3;
		contentLayout.marginHeight = 3;
		
		wAdvancedComp = new Composite(wTabFolder, SWT.NONE);
 		props.setLook(wAdvancedComp);
 		wAdvancedComp.setLayout(contentLayout);
 		
 		
 		 // SuccessOngrouping?
	     // ////////////////////////
	     // START OF SUCCESS ON GROUP///
	     // /
	    wSuccessOn= new Group(wAdvancedComp, SWT.SHADOW_NONE);
	    props.setLook(wSuccessOn);
	    wSuccessOn.setText(Messages.getString("JobExportRepository.SuccessOn.Group.Label"));

	    FormLayout successongroupLayout = new FormLayout();
	    successongroupLayout.marginWidth = 10;
	    successongroupLayout.marginHeight = 10;

	    wSuccessOn.setLayout(successongroupLayout);

	    //Success Condition
	  	wlSuccessCondition = new Label(wSuccessOn, SWT.RIGHT);
	  	wlSuccessCondition.setText(Messages.getString("JobExportRepository.SuccessCondition.Label"));
	  	props.setLook(wlSuccessCondition);
	  	fdlSuccessCondition = new FormData();
	  	fdlSuccessCondition.left = new FormAttachment(0, 0);
	  	fdlSuccessCondition.right = new FormAttachment(middle, 0);
	  	fdlSuccessCondition.top = new FormAttachment(0, margin);
	  	wlSuccessCondition.setLayoutData(fdlSuccessCondition);
	  	wSuccessCondition = new CCombo(wSuccessOn, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER);
	  	wSuccessCondition.add(Messages.getString("JobExportRepository.SuccessWhenAllWorksFine.Label"));
	  	wSuccessCondition.add(Messages.getString("JobExportRepository.SuccessWhenErrorsLessThan.Label"));
	  	
	  	wSuccessCondition.select(0); // +1: starts at -1
	  	
		props.setLook(wSuccessCondition);
		fdSuccessCondition= new FormData();
		fdSuccessCondition.left = new FormAttachment(middle, 0);
		fdSuccessCondition.top = new FormAttachment(0, margin);
		fdSuccessCondition.right = new FormAttachment(100, 0);
		wSuccessCondition.setLayoutData(fdSuccessCondition);
		wSuccessCondition.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				activeSuccessCondition();
				
			}
		});

		// Success when number of errors less than
		wlLimit= new Label(wSuccessOn, SWT.RIGHT);
		wlLimit.setText(Messages.getString("JobExportRepository.Limit.Label"));
		props.setLook(wlLimit);
		fdlLimit= new FormData();
		fdlLimit.left = new FormAttachment(0, 0);
		fdlLimit.top = new FormAttachment(wSuccessCondition, margin);
		fdlLimit.right = new FormAttachment(middle, -margin);
		wlLimit.setLayoutData(fdlLimit);
		
		
		wLimit= new TextVar(jobMeta,wSuccessOn, SWT.SINGLE | SWT.LEFT | SWT.BORDER, Messages
			.getString("JobExportRepository.NrLimit.Tooltip"));
		props.setLook(wLimit);
		wLimit.addModifyListener(lsMod);
		fdLimit= new FormData();
		fdLimit.left = new FormAttachment(middle, 0);
		fdLimit.top = new FormAttachment(wSuccessCondition, margin);
		fdLimit.right = new FormAttachment(100, -margin);
		wLimit.setLayoutData(fdLimit);
		
	    fdSuccessOn= new FormData();
	    fdSuccessOn.left = new FormAttachment(0, margin);
	    fdSuccessOn.top = new FormAttachment(0, margin);
	    fdSuccessOn.right = new FormAttachment(100, -margin);
	    wSuccessOn.setLayoutData(fdSuccessOn);
	     // ///////////////////////////////////////////////////////////
	     // / END OF Success ON GROUP
	     // ///////////////////////////////////////////////////////////

	    
 		
	    fdAdvancedComp = new FormData();
		fdAdvancedComp.left  = new FormAttachment(0, 0);
 		fdAdvancedComp.top   = new FormAttachment(0, 0);
 		fdAdvancedComp.right = new FormAttachment(100, 0);
 		fdAdvancedComp.bottom= new FormAttachment(100, 0);
 		wAdvancedComp.setLayoutData(wAdvancedComp);

 		wAdvancedComp.layout();
		wAdvancedTab.setControl(wAdvancedComp);


		/////////////////////////////////////////////////////////////
		/// END OF ADVANCED TAB
		/////////////////////////////////////////////////////////////
 		
 		
 		
 		
		fdTabFolder = new FormData();
		fdTabFolder.left  = new FormAttachment(0, 0);
		fdTabFolder.top   = new FormAttachment(wName, margin);
		fdTabFolder.right = new FormAttachment(100, 0);
		fdTabFolder.bottom= new FormAttachment(100, -50);
		wTabFolder.setLayoutData(fdTabFolder); 
 		
 		
        wOK = new Button(shell, SWT.PUSH);
        wOK.setText(Messages.getString("System.Button.OK"));
        wCancel = new Button(shell, SWT.PUSH);
        wCancel.setText(Messages.getString("System.Button.Cancel"));
        
		BaseStepDialog.positionBottomButtons(shell, new Button[] { wOK, wCancel }, margin, wTabFolder);

		// Add listeners
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel(); } };
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();     } };
		lsTest     = new Listener() { public void handleEvent(Event e) { RepConnect(true); } };

		wCancel.addListener(SWT.Selection, lsCancel);
		wOK.addListener    (SWT.Selection, lsOK    );
		wTest.addListener    (SWT.Selection, lsTest    );

		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };

		wName.addSelectionListener( lsDef );
		wRepositoryname.addSelectionListener( lsDef );

		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );

		getData();
		activeOneFolder();
		setDateTimeFormat();
		activeSuccessCondition();
		wTabFolder.setSelection(0);
		BaseStepDialog.setSize(shell);

		shell.open();
		while (!shell.isDisposed())
		{
				if (!display.readAndDispatch()) display.sleep();
		}
		return jobEntry;
	}

	private void activeSuccessCondition()
	{
		wlLimit.setEnabled(wSuccessCondition.getSelectionIndex()!=0);
		wLimit.setEnabled(wSuccessCondition.getSelectionIndex()!=0);	
	}
    public void checkPasswordVisible()
    {
        String password = wPassword.getText();
        List<String> list = new ArrayList<String>();
        StringUtil.getUsedVariables(password, list, true);
        if (list.size() == 0)
        {
            wPassword.setEchoChar('*');
        }
        else
        {
            wPassword.setEchoChar('\0'); // Show it all...
        }
    }
    private void setDateTimeFormat()
	{
		if(wSpecifyFormat.getSelection())
		{
			wAddDate.setSelection(false);	
			wAddTime.setSelection(false);
		}

		wDateTimeFormat.setEnabled(wSpecifyFormat.getSelection());
		wlDateTimeFormat.setEnabled(wSpecifyFormat.getSelection());
		wAddDate.setEnabled(!wSpecifyFormat.getSelection());
		wlAddDate.setEnabled(!wSpecifyFormat.getSelection());
		wAddTime.setEnabled(!wSpecifyFormat.getSelection());
		wlAddTime.setEnabled(!wSpecifyFormat.getSelection());
		
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
		if (jobEntry.getName()    != null) wName.setText( jobEntry.getName() );
		wName.selectAll();
		if (jobEntry.getRepositoryname()!= null) wRepositoryname.setText( jobEntry.getRepositoryname() );
		if (jobEntry.getUsername()!= null) wUserName.setText( jobEntry.getUsername() );
		wPassword.setText(Const.NVL(jobEntry.getPassword(), ""));
		if (jobEntry.getTargetfilename()!= null) wTargetFilename.setText( jobEntry.getTargetfilename() );
		
		if(jobEntry.getIfFileExists()!=null)
		{
			if(jobEntry.getIfFileExists().equals(jobEntry.If_FileExists_Overwrite))
				wIfFileExists.select(1);
			else if(jobEntry.getIfFileExists().equals(jobEntry.If_FileExists_Uniquename))
				wIfFileExists.select(2);
			else if(jobEntry.getIfFileExists().equals(jobEntry.If_FileExists_Fail))
				wIfFileExists.select(3);
			else wIfFileExists.select(0);
			
		}else wIfFileExists.select(0);
		
		if(jobEntry.getExportType()!=null)
		{
			if(jobEntry.getExportType().equals(jobEntry.Export_Jobs))
				wExportType.select(1);
			else if(jobEntry.getExportType().equals(jobEntry.Export_Trans))
				wExportType.select(2);
			else if(jobEntry.getExportType().equals(jobEntry.Export_By_Folder))
				wExportType.select(3);
			else if(jobEntry.getExportType().equals(jobEntry.Export_One_Folder))
				wExportType.select(4);
			else 
				wExportType.select(0);
			
		}else wExportType.select(0);
		
        if (jobEntry.getDirectory() != null)
            wFoldername.setText(jobEntry.getDirectory());
        
    	wAddDate.setSelection(jobEntry.isAddDate());
		wAddTime.setSelection(jobEntry.isAddTime());
		wSpecifyFormat.setSelection(jobEntry.isSpecifyFormat());
		if (jobEntry.getDateTimeFormat()!= null) wDateTimeFormat.setText( jobEntry.getDateTimeFormat() );
		wcreateFolder.setSelection(jobEntry.isCreateFolder());
		wNewFolder.setSelection(jobEntry.isNewFolder());
		wAddFileToResult.setSelection(jobEntry.isAddresultfilesname());
		
		if (jobEntry.getNrLimit()!= null) 
			wLimit.setText( jobEntry.getNrLimit() );
		else
			wLimit.setText("10");
		
		if(jobEntry.getSuccessCondition()!=null)
		{
			if(jobEntry.getSuccessCondition().equals(jobEntry.SUCCESS_IF_ERRORS_LESS))
				wSuccessCondition.select(1);
			else
				wSuccessCondition.select(0);	
		}else wSuccessCondition.select(0);
		
	}

	private void cancel()
	{
		jobEntry.setChanged(changed);
		jobEntry=null;
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
		jobEntry.setRepositoryname(wRepositoryname.getText());
		jobEntry.setUsername(wUserName.getText());
		jobEntry.setPassword(wPassword.getText());
		jobEntry.setTargetfilename(wTargetFilename.getText());
        
		if(wIfFileExists.getSelectionIndex()==1)
			jobEntry.setIfFileExists(jobEntry.If_FileExists_Overwrite);
		else if(wIfFileExists.getSelectionIndex()==2)
			jobEntry.setIfFileExists(jobEntry.If_FileExists_Uniquename);
		else if(wIfFileExists.getSelectionIndex()==3)
			jobEntry.setIfFileExists(jobEntry.If_FileExists_Fail);	
		else
			jobEntry.setIfFileExists(jobEntry.If_FileExists_Skip);	
		
		
		if(wExportType.getSelectionIndex()==1)
			jobEntry.setExportType(jobEntry.Export_Jobs);
		else if(wExportType.getSelectionIndex()==2)
			jobEntry.setExportType(jobEntry.Export_Trans);
		else if(wExportType.getSelectionIndex()==3)
			jobEntry.setExportType(jobEntry.Export_By_Folder);	
		else if(wExportType.getSelectionIndex()==4)
			jobEntry.setExportType(jobEntry.Export_One_Folder);	
		else
			jobEntry.setExportType(jobEntry.Export_All);	
		
		jobEntry.setDirectory(wFoldername.getText());
		
		
		jobEntry.setAddDate(wAddDate.getSelection());
		jobEntry.setAddTime(wAddTime.getSelection());
		jobEntry.setSpecifyFormat(wSpecifyFormat.getSelection());
		jobEntry.setDateTimeFormat(wDateTimeFormat.getText());
		jobEntry.setAddDate(wAddDate.getSelection());
		jobEntry.setCreateFolder(wcreateFolder.getSelection());
		jobEntry.setNewFolder(wNewFolder.getSelection());
		jobEntry.setAddresultfilesname(wAddFileToResult.getSelection());
		

		if(wSuccessCondition.getSelectionIndex()==1)
			jobEntry.setSuccessCondition(jobEntry.SUCCESS_IF_ERRORS_LESS);
		else
			jobEntry.setSuccessCondition(jobEntry.SUCCESS_IF_NO_ERRORS);	
		jobEntry.setNrLimit(wLimit.getText());
		
		dispose();
	}
	private void activeOneFolder()
	{
		wlFoldername.setEnabled(wExportType.getSelectionIndex()==4);
		wFoldername.setEnabled(wExportType.getSelectionIndex()==4);
		wbFoldername.setEnabled(wExportType.getSelectionIndex()==4);
		
		wlNewFolder.setEnabled(wExportType.getSelectionIndex()==3);
		wNewFolder.setEnabled(wExportType.getSelectionIndex()==3);
	}
	public String toString()
	{
		return this.getClass().getName();
	}

	public boolean evaluates()
	{
		return true;
	}

	public boolean isUnconditional()
	{
		return false;
	}
	
	private boolean RepConnect(boolean displaySuccess)
	{
		boolean retval=false;
		LogWriter log = LogWriter.getInstance();
		RepositoriesMeta reps_info =null;
		RepositoryMeta rep_info  = null;
		UserInfo       user_info = null;
		Repository repos		= null;
		
		try
		{
			reps_info = new RepositoriesMeta(log);
			if (!reps_info.readData())
			{
				displayMsg(Messages.getString("JobExportRepository.Error.NoRepsDefined"),Messages.getString("JobExportRepository.Error.NoRepsDefinedMsg"),true);
			}
			rep_info = reps_info.findRepository( jobMeta.environmentSubstitute(wRepositoryname.getText()));
	
			if (rep_info==null)
			{
				// Can not find repository
				displayMsg(Messages.getString("JobExportRepository.Error.CanNotFindRep"),Messages.getString("JobExportRepository.Error.CanNotFindRepMsg",wRepositoryname.getText()),true);
				return false;
			}
			
			repos = new Repository(log, rep_info, user_info);
			if (!repos.connect("Export job entry"))
			{
				displayMsg(Messages.getString("JobExportRepository.Error.CanNotConnect"),Messages.getString("JobExportRepository.Error.CanNotConnectMsg",wRepositoryname.getText()),true);
				return false;
			}	
			// Check username, password
			user_info = new UserInfo(repos, jobMeta.environmentSubstitute(wUserName.getText()),jobMeta.environmentSubstitute(wPassword.getText()));
				
			if (user_info.getID()>0)
			{
				repos.disconnect();
				repos=null;
				
				if(displaySuccess)
					displayMsg(Messages.getString("JobExportRepository.Connected.Title.Ok"),
							Messages.getString("JobExportRepository.Connected.OK",wRepositoryname.getText()),false);
			}else
			{
					displayMsg(Messages.getString("JobExportRepository.Connected.Title.Bad"),
							Messages.getString("JobExportRepository.CanNotVerifyUser",wUserName.getText(),wRepositoryname.getText()),true);
					return false;
			}

			// We are connected
			retval=true;

		}catch(Exception e)
		{
			displayMsg(Messages.getString("System.Dialog.Error.Title"),
					Messages.getString("JobExportRepository.ErrorConnecting",wRepositoryname.getText()),true);
		}finally
		{
			if(repos!=null) 
			{
				repos.disconnect();
				repos=null;
			}
			if(rep_info!=null) rep_info=null;
			if(user_info!=null) user_info=null;
			reps_info.clear();
		}
		return retval;
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
				displayMsg(Messages.getString("JobExportRepository.Error.NoRepsDefined"),Messages.getString("JobExportRepository.Error.NoRepsDefinedMsg"),true);
			}else
			{
				int nrRepositories=reps_info.nrRepositories();
				if(nrRepositories==0)
				{
					displayMsg(Messages.getString("System.Dialog.Error.Title"), Messages.getString("JobExportRepository.Error.NoRep.DialogMessage"), true);
				}else
				{
					String available[] = new String[nrRepositories];
	                
					for (int i=0;i<nrRepositories;i++)
					{
						RepositoryMeta ri = reps_info.getRepository(i);
						available[i]=ri.getName();
					}
					
					String[] source = new String[1];
					source[0]=wRepositoryname.getText();
					int idxSource[] = Const.indexsOfStrings(source, available);
					EnterSelectionDialog dialog = new EnterSelectionDialog(shell, available, Messages.getString("JobExportRepository.SelectRepository.Title"), Messages.getString("JobExportRepository.SelectRepository.Message"));
					dialog.setMulti(false);
					dialog.setSelectedNrs(idxSource);
					if (dialog.open()!=null)
					{
						int idx[] = dialog.getSelectionIndeces();
						wRepositoryname.setText(available[idx[0]]);
					}
				}
			}
		}
		catch(Exception e)
		{
			displayMsg(Messages.getString("System.Dialog.Error.Title"), Messages.getString("JobExportRepository.ErrorGettingRepositories.DialogMessage")+Const.CR+":"+e.getMessage(), true);
		}finally
		{
			reps_info.clear();
		}
	}
	private void displaydirectoryList()
	{
		LogWriter log = LogWriter.getInstance();
		RepositoriesMeta reps_info =null;
		RepositoryMeta rep_info  = null;
		UserInfo       user_info = null;
		Repository repos		= null;
		try{
			reps_info = new RepositoriesMeta(log);
			if (!reps_info.readData())
			{
				displayMsg(Messages.getString("JobExportRepository.Error.NoRepsDefined"),Messages.getString("JobExportRepository.Error.NoRepsDefinedMsg"),true);
			}
			rep_info = reps_info.findRepository( jobMeta.environmentSubstitute(wRepositoryname.getText()));
	
			if (rep_info==null)
			{
				// Can not find repository
				displayMsg(Messages.getString("JobExportRepository.Error.CanNotFindRep"),Messages.getString("JobExportRepository.Error.CanNotFindRepMsg",wRepositoryname.getText()),true);
			}
			
			repos = new Repository(log, rep_info, user_info);
			try{
			if (!repos.connect("Export job entry"))
			{
				displayMsg(Messages.getString("JobExportRepository.Error.CanNotConnect"),Messages.getString("JobExportRepository.Error.CanNotConnectMsg",wRepositoryname.getText()),true);
			}
			}catch(Exception e){}
	
			
			SelectDirectoryDialog sdd = new SelectDirectoryDialog(shell, SWT.NONE, repos);
			RepositoryDirectory rd = sdd.open();
			if (rd!=null)
			{
	            wFoldername.setText(rd.getPath());
			}
	
		}catch(Exception e)
		{
			displayMsg(Messages.getString("System.Dialog.Error.Title"), Messages.getString("JobExportRepository.ErrorGettingFolderds.DialogMessage")+Const.CR+":"+e.getMessage(), true);
		}
		finally
		{
			if(repos!=null) 
			{
				repos.disconnect();
				repos=null;
			}
			if(rep_info!=null) rep_info=null;
			if(user_info!=null) user_info=null;
			reps_info.clear();
		}
	}
}