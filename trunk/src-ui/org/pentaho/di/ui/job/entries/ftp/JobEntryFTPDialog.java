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

package org.pentaho.di.ui.job.entries.ftp;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
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
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.util.StringUtil;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.core.widget.LabelText;
import org.pentaho.di.ui.core.widget.LabelTextVar;
import org.pentaho.di.ui.job.dialog.JobDialog;
import org.pentaho.di.ui.job.entry.JobEntryDialog;
import org.pentaho.di.job.entry.JobEntryDialogInterface;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.job.entries.ftp.JobEntryFTP;
import org.pentaho.di.job.entries.ftp.Messages;
import org.pentaho.di.ui.core.widget.TextVar;



import com.enterprisedt.net.ftp.FTPClient;


/**
 * This dialog allows you to edit the FTP Get job entry settings. 
 * 
 * @author Matt
 * @since 19-06-2003
 */

public class JobEntryFTPDialog extends JobEntryDialog implements JobEntryDialogInterface
{
    private LabelText wName;

    private FormData fdName;

    private LabelTextVar wServerName;

    private FormData fdServerName;

    private LabelTextVar wUserName;

    private FormData fdUserName;

    private LabelTextVar wPassword;

    private FormData fdPassword;

    private TextVar wFtpDirectory;
    private Label wlFtpDirectory;

    private FormData fdFtpDirectory;
    private FormData fdlFtpDirectory;

    private LabelTextVar wWildcard;

    private FormData fdWildcard;

    private Label wlBinaryMode;

    private Button wBinaryMode;

    private FormData fdlBinaryMode, fdBinaryMode;

    private LabelTextVar wTimeout;

    private FormData fdTimeout;

    private Label wlRemove;

    private Button wRemove;

    private FormData fdlRemove, fdRemove;

    private Label wlOnlyNew;

    private Button wOnlyNew;

    private FormData fdlOnlyNew, fdOnlyNew;

    private Label wlActive;

    private Button wActive;

    private FormData fdlActive, fdActive;

    private Button wOK, wCancel;

    private Listener lsOK, lsCancel;

    private JobEntryFTP jobEntry;

    private Shell shell;

    private SelectionAdapter lsDef;
    
    private Label        wlControlEncoding;
    
    private Combo        wControlEncoding;
    
    private FormData     fdlControlEncoding, fdControlEncoding;

    private boolean changed;
    
    private Label wlMove;

    private Button wMove;
    
    private Label wlMoveToDirectory;
    
    private FormData fdMove, fdlMove;
    

    private FormData fdMoveToDirectory,fdlMoveToDirectory;
    
    private TextVar wMoveToDirectory;
    
    private Label        wlSpecifyFormat;
    private Button       wSpecifyFormat;
    private FormData     fdlSpecifyFormat, fdSpecifyFormat;

    private Label        wlDateTimeFormat;
    private CCombo       wDateTimeFormat;
    private FormData     fdlDateTimeFormat, fdDateTimeFormat; 
    
    private Label        wlAddDate;
    private Button       wAddDate;
    private FormData     fdlAddDate, fdAddDate;

    private Label        wlAddTime;
    private Button       wAddTime;
    private FormData     fdlAddTime, fdAddTime;
    
	private Label        wlAddDateBeforeExtension;
	private Button       wAddDateBeforeExtension;
	private FormData     fdlAddDateBeforeExtension, fdAddDateBeforeExtension;

	
    
	private Group wServerSettings;
    private FormData fdServerSettings;
    
	private Group wLocalSettings;
    private FormData fdLocalSettings;
    
	private CTabFolder   wTabFolder;
	private Composite    wGeneralComp,wFilesComp,wAdvancedComp;	
	private CTabItem     wGeneralTab,wFilesTab,wAdvancedTab;
	private FormData	 fdGeneralComp,fdFilesComp,fdAdvancedComp;
	private FormData     fdTabFolder;
	
    private LabelTextVar wProxyHost;
    
    private FormData     fdPort;

    private LabelTextVar wPort;


    private FormData     fdProxyHost;

    private LabelTextVar wProxyPort;

    private FormData     fdProxyPort;

    private LabelTextVar wProxyUsername;

    private FormData     fdProxyUsername;
    
    private LabelTextVar wProxyPassword;
    
    private FormData     fdProxyPasswd;
    
	private Button wTest;
	
	private FormData fdTest;
	
	private Listener lsTest;
	
	private Listener lsCheckFolder;
	
	private Listener lsCheckChangeFolder;
	
	private Group wAdvancedSettings;
    private FormData fdAdvancedSettings;
    
	private Group wRemoteSettings;
    private FormData fdRemoteSettings;
    
    private Label wlAddFilenameToResult;

    private Button wAddFilenameToResult;

    private FormData fdlAddFilenameToResult, fdAddFilenameToResult;
    
    private Label wlTargetDirectory;
    private TextVar wTargetDirectory;
    private FormData fdlTargetDirectory, fdTargetDirectory;
    
    private Button wbTargetDirectory;
    private FormData fdbTargetDirectory;   
    
	private Label wlIfFileExists;
	private  CCombo wIfFileExists;
	private FormData fdlIfFileExists, fdIfFileExists;
	
	private Button wbTestFolderExists;
	private FormData fdbTestFolderExists;
	
	private Button wCreateMoveFolder;
	private Label wlCreateMoveFolder;
	private FormData fdCreateMoveFolder;
	private FormData fdlCreateMoveFolder;
	
	private Button wbTestChangeFolderExists;
	private FormData fdbTestChangeFolderExists;
	
	private Group wSuccessOn;
    private FormData fdSuccessOn;
    
	
	private Label wlNrErrorsLessThan;
	private TextVar wNrErrorsLessThan;
	private FormData fdlNrErrorsLessThan, fdNrErrorsLessThan;
	
	private Label wlSuccessCondition;
	private CCombo wSuccessCondition;
	private FormData fdlSuccessCondition, fdSuccessCondition;
	
	private FTPClient ftpclient = null;
	private String pwdFolder=null;
    
    // These should not be translated, they are required to exist on all
    // platforms according to the documentation of "Charset".
    private static String[] encodings = { "US-ASCII",
    	                                  "ISO-8859-1",
    	                                  "UTF-8",
    	                                  "UTF-16BE",
    	                                  "UTF-16LE",
    	                                  "UTF-16" }; 

    //
    // Original code used to fill encodings, this display all possibilities but
    // takes 10 seconds on my pc to fill.
    //
    // static {
    //     SortedMap charsetMap = Charset.availableCharsets();
    //    Set charsetSet = charsetMap.keySet();
    //    encodings = (String [])charsetSet.toArray(new String[0]);
    // }

    public JobEntryFTPDialog(Shell parent, JobEntryInterface jobEntryInt, Repository rep, JobMeta jobMeta)
    {
        super(parent, jobEntryInt, rep, jobMeta);
        jobEntry = (JobEntryFTP) jobEntryInt;
        if (this.jobEntry.getName() == null)
            this.jobEntry.setName(Messages.getString("JobFTP.Name.Default"));
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
            	pwdFolder=null;
            	ftpclient=null;
                jobEntry.setChanged();
            }
        };
        changed = jobEntry.hasChanged();

        FormLayout formLayout = new FormLayout();
        formLayout.marginWidth = Const.FORM_MARGIN;
        formLayout.marginHeight = Const.FORM_MARGIN;

        shell.setLayout(formLayout);
        shell.setText(Messages.getString("JobFTP.Title"));

        int middle = props.getMiddlePct();
        int margin = Const.MARGIN;

        // Job entry name line
        wName = new LabelText(shell, Messages.getString("JobFTP.Name.Label"), Messages
            .getString("JobFTP.Name.Tooltip"));
        wName.addModifyListener(lsMod);
        fdName = new FormData();
        fdName.top = new FormAttachment(0, 0);
        fdName.left = new FormAttachment(0, 0);
        fdName.right = new FormAttachment(100, 0);
        wName.setLayoutData(fdName);
        
        
        wTabFolder = new CTabFolder(shell, SWT.BORDER);
 		props.setLook(wTabFolder, Props.WIDGET_STYLE_TAB);
 		
 		//////////////////////////
		// START OF GENERAL TAB   ///
		//////////////////////////
		
		
		
		wGeneralTab=new CTabItem(wTabFolder, SWT.NONE);
		wGeneralTab.setText(Messages.getString("JobFTP.Tab.General.Label"));
		
		wGeneralComp = new Composite(wTabFolder, SWT.NONE);
 		props.setLook(wGeneralComp);

		FormLayout generalLayout = new FormLayout();
		generalLayout.marginWidth  = 3;
		generalLayout.marginHeight = 3;
		wGeneralComp.setLayout(generalLayout);
        
	     // ////////////////////////
	     // START OF SERVER SETTINGS GROUP///
	     // /
	    wServerSettings = new Group(wGeneralComp, SWT.SHADOW_NONE);
	    props.setLook(wServerSettings);
	    wServerSettings.setText(Messages.getString("JobFTP.ServerSettings.Group.Label"));

	    FormLayout ServerSettingsgroupLayout = new FormLayout();
	    ServerSettingsgroupLayout.marginWidth = 10;
	    ServerSettingsgroupLayout.marginHeight = 10;

	    wServerSettings.setLayout(ServerSettingsgroupLayout);

        // ServerName line
        wServerName = new LabelTextVar(jobMeta,wServerSettings, Messages.getString("JobFTP.Server.Label"), Messages
            .getString("JobFTP.Server.Tooltip"));
        props.setLook(wServerName);
        wServerName.addModifyListener(lsMod);
        fdServerName = new FormData();
        fdServerName.left = new FormAttachment(0, 0);
        fdServerName.top = new FormAttachment(wName, margin);
        fdServerName.right = new FormAttachment(100, 0);
        wServerName.setLayoutData(fdServerName);
        
        // Server port line
        wPort = new LabelTextVar(jobMeta,wServerSettings, Messages.getString("JobFTP.Port.Label"), Messages.getString("JobFTP.Port.Tooltip"));
        props.setLook(wPort);
        wPort.addModifyListener(lsMod);
        fdPort = new FormData();
        fdPort.left 	= new FormAttachment(0, 0);
        fdPort.top  	= new FormAttachment(wServerName, margin);
        fdPort.right	= new FormAttachment(100, 0);
        wPort.setLayoutData(fdPort);

        // UserName line
        wUserName = new LabelTextVar(jobMeta,wServerSettings, Messages.getString("JobFTP.User.Label"), Messages
            .getString("JobFTP.User.Tooltip"));
        props.setLook(wUserName);
        wUserName.addModifyListener(lsMod);
        fdUserName = new FormData();
        fdUserName.left = new FormAttachment(0, 0);
        fdUserName.top = new FormAttachment(wPort, margin);
        fdUserName.right = new FormAttachment(100, 0);
        wUserName.setLayoutData(fdUserName);

        // Password line
        wPassword = new LabelTextVar(jobMeta,wServerSettings, Messages.getString("JobFTP.Password.Label"), Messages
            .getString("JobFTP.Password.Tooltip"));
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

        
        // Proxy host line
        wProxyHost = new LabelTextVar(jobMeta,wServerSettings, Messages.getString("JobFTP.ProxyHost.Label"), Messages.getString("JobFTP.ProxyHost.Tooltip"));
        props.setLook(wProxyHost);
        wProxyHost.addModifyListener(lsMod);
        fdProxyHost = new FormData();
        fdProxyHost.left 	= new FormAttachment(0, 0);
        fdProxyHost.top		= new FormAttachment(wPassword, 2*margin);
        fdProxyHost.right	= new FormAttachment(100, 0);
        wProxyHost.setLayoutData(fdProxyHost);

        // Proxy port line
        wProxyPort = new LabelTextVar(jobMeta,wServerSettings, Messages.getString("JobFTP.ProxyPort.Label"), Messages.getString("JobFTP.ProxyPort.Tooltip"));
        props.setLook(wProxyPort);
        wProxyPort.addModifyListener(lsMod);
        fdProxyPort = new FormData();
        fdProxyPort.left 	= new FormAttachment(0, 0);
        fdProxyPort.top  	= new FormAttachment(wProxyHost, margin);
        fdProxyPort.right	= new FormAttachment(100, 0);
        wProxyPort.setLayoutData(fdProxyPort);

        // Proxy username line
        wProxyUsername = new LabelTextVar(jobMeta,wServerSettings, Messages.getString("JobFTP.ProxyUsername.Label"), Messages.getString("JobFTP.ProxyUsername.Tooltip"));
        props.setLook(wProxyUsername);
        wProxyUsername.addModifyListener(lsMod);
        fdProxyUsername = new FormData();
        fdProxyUsername.left = new FormAttachment(0, 0);
        fdProxyUsername.top  = new FormAttachment(wProxyPort, margin);
        fdProxyUsername.right= new FormAttachment(100, 0);
        wProxyUsername.setLayoutData(fdProxyUsername);
        
        // Proxy password line
        wProxyPassword = new LabelTextVar(jobMeta,wServerSettings, Messages.getString("JobFTP.ProxyPassword.Label"), Messages.getString("JobFTP.ProxyPassword.Tooltip"));
        props.setLook(wProxyPassword);
        wProxyPassword.addModifyListener(lsMod);
        fdProxyPasswd=new FormData();
        fdProxyPasswd.left = new FormAttachment(0, 0);
        fdProxyPasswd.top  = new FormAttachment(wProxyUsername, margin);
        fdProxyPasswd.right= new FormAttachment(100, 0);
        wProxyPassword.setLayoutData(fdProxyPasswd);
        
		// Test connection button
		wTest=new Button(wServerSettings,SWT.PUSH);
		wTest.setText(Messages.getString("JobFTP.TestConnection.Label"));
 		props.setLook(wTest);
		fdTest=new FormData();
		wTest.setToolTipText(Messages.getString("JobFTP.TestConnection.Tooltip"));
		//fdTest.left = new FormAttachment(middle, 0);
		fdTest.top  = new FormAttachment(wProxyPassword, margin);
		fdTest.right= new FormAttachment(100, 0);
		wTest.setLayoutData(fdTest);
        
        
	     fdServerSettings = new FormData();
	     fdServerSettings.left = new FormAttachment(0, margin);
	     fdServerSettings.top = new FormAttachment(wName, margin);
	     fdServerSettings.right = new FormAttachment(100, -margin);
	     wServerSettings.setLayoutData(fdServerSettings);
	     // ///////////////////////////////////////////////////////////
	     // / END OF SERVER SETTINGS GROUP
	     // ///////////////////////////////////////////////////////////
        

	     // ////////////////////////
	     // START OF Advanced SETTINGS GROUP///
	     // /
	     wAdvancedSettings = new Group(wGeneralComp, SWT.SHADOW_NONE);
	     props.setLook(wAdvancedSettings);
	     wAdvancedSettings.setText(Messages.getString("JobFTP.AdvancedSettings.Group.Label"));
	     FormLayout AdvancedSettingsgroupLayout = new FormLayout();
	     AdvancedSettingsgroupLayout.marginWidth = 10;
	     AdvancedSettingsgroupLayout.marginHeight = 10;
	     wAdvancedSettings.setLayout(AdvancedSettingsgroupLayout);

	    
	     // Binary mode selection...
        wlBinaryMode = new Label(wAdvancedSettings, SWT.RIGHT);
        wlBinaryMode.setText(Messages.getString("JobFTP.BinaryMode.Label"));
        props.setLook(wlBinaryMode);
        fdlBinaryMode = new FormData();
        fdlBinaryMode.left = new FormAttachment(0, 0);
        fdlBinaryMode.top = new FormAttachment(wServerSettings, margin);
        fdlBinaryMode.right = new FormAttachment(middle, 0);
        wlBinaryMode.setLayoutData(fdlBinaryMode);
        wBinaryMode = new Button(wAdvancedSettings, SWT.CHECK);
        props.setLook(wBinaryMode);
        wBinaryMode.setToolTipText(Messages.getString("JobFTP.BinaryMode.Tooltip"));
        fdBinaryMode = new FormData();
        fdBinaryMode.left = new FormAttachment(middle, margin);
        fdBinaryMode.top = new FormAttachment(wServerSettings, margin);
        fdBinaryMode.right = new FormAttachment(100, 0);
        wBinaryMode.setLayoutData(fdBinaryMode);

        // Timeout line
        wTimeout = new LabelTextVar(jobMeta,wAdvancedSettings, Messages.getString("JobFTP.Timeout.Label"), Messages
            .getString("JobFTP.Timeout.Tooltip"));
        props.setLook(wTimeout);
        wTimeout.addModifyListener(lsMod);
        fdTimeout = new FormData();
        fdTimeout.left = new FormAttachment(0, 0);
        fdTimeout.top = new FormAttachment(wlBinaryMode, margin);
        fdTimeout.right = new FormAttachment(100, 0);
        wTimeout.setLayoutData(fdTimeout);


        // active connection?
        wlActive = new Label(wAdvancedSettings, SWT.RIGHT);
        wlActive.setText(Messages.getString("JobFTP.ActiveConns.Label"));
        props.setLook(wlActive);
        fdlActive = new FormData();
        fdlActive.left = new FormAttachment(0, 0);
        fdlActive.top = new FormAttachment(wTimeout, margin);
        fdlActive.right = new FormAttachment(middle, 0);
        wlActive.setLayoutData(fdlActive);
        wActive = new Button(wAdvancedSettings, SWT.CHECK);
        wActive.setToolTipText(Messages.getString("JobFTP.ActiveConns.Tooltip"));
        props.setLook(wActive);
        fdActive = new FormData();
        fdActive.left = new FormAttachment(middle, margin);
        fdActive.top = new FormAttachment(wTimeout, margin);
        fdActive.right = new FormAttachment(100, 0);
        wActive.setLayoutData(fdActive);
        
        // Control encoding line
        //
        // The drop down is editable as it may happen an encoding may not be present
        // on one machine, but you may want to use it on your execution server
        //
        wlControlEncoding=new Label(wAdvancedSettings, SWT.RIGHT);
        wlControlEncoding.setText(Messages.getString("JobFTP.ControlEncoding.Label"));
        props.setLook(wlControlEncoding);
        fdlControlEncoding=new FormData();
        fdlControlEncoding.left  = new FormAttachment(0, 0);
        fdlControlEncoding.top   = new FormAttachment(wActive, margin);
        fdlControlEncoding.right = new FormAttachment(middle, 0);
        wlControlEncoding.setLayoutData(fdlControlEncoding);
        wControlEncoding=new Combo(wAdvancedSettings, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        wControlEncoding.setToolTipText(Messages.getString("JobFTP.ControlEncoding.Tooltip"));
        wControlEncoding.setItems(encodings);
        props.setLook(wControlEncoding);
        fdControlEncoding=new FormData();
        fdControlEncoding.left = new FormAttachment(middle, margin);
        fdControlEncoding.top  = new FormAttachment(wActive, margin);
        fdControlEncoding.right= new FormAttachment(100, 0);        
        wControlEncoding.setLayoutData(fdControlEncoding);
    
	    
	     fdAdvancedSettings = new FormData();
	     fdAdvancedSettings.left = new FormAttachment(0, margin);
	     fdAdvancedSettings.top = new FormAttachment(wServerSettings, margin);
	     fdAdvancedSettings.right = new FormAttachment(100, -margin);
	     wAdvancedSettings.setLayoutData(fdAdvancedSettings);
	     // ///////////////////////////////////////////////////////////
	     // / END OF Advanced SETTINGS GROUP
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
		
 		//////////////////////////
		// START OF Files TAB   ///
		//////////////////////////
		
 		wFilesTab=new CTabItem(wTabFolder, SWT.NONE);
		wFilesTab.setText(Messages.getString("JobFTP.Tab.Files.Label"));
		
		wFilesComp = new Composite(wTabFolder, SWT.NONE);
 		props.setLook(wFilesComp);

		FormLayout FilesLayout = new FormLayout();
		FilesLayout.marginWidth  = 3;
		FilesLayout.marginHeight = 3;
		wFilesComp.setLayout(FilesLayout);
 		
		 // ////////////////////////
	     // START OF Remote SETTINGS GROUP///
	     // /
	    wRemoteSettings = new Group(wFilesComp, SWT.SHADOW_NONE);
	    props.setLook(wRemoteSettings);
	    wRemoteSettings.setText(Messages.getString("JobFTP.RemoteSettings.Group.Label"));

	    FormLayout RemoteSettinsgroupLayout = new FormLayout();
	    RemoteSettinsgroupLayout.marginWidth = 10;
	    RemoteSettinsgroupLayout.marginHeight = 10;

	    wRemoteSettings.setLayout(RemoteSettinsgroupLayout);
	    
	    
        // Move to directory
        wlFtpDirectory = new Label(wRemoteSettings, SWT.RIGHT);
        wlFtpDirectory.setText(Messages.getString("JobFTP.RemoteDir.Label"));
        props.setLook(wlFtpDirectory);
        fdlFtpDirectory= new FormData();
        fdlFtpDirectory.left = new FormAttachment(0, 0);
        fdlFtpDirectory.top = new FormAttachment(0, margin);
        fdlFtpDirectory.right = new FormAttachment(middle, 0);
        wlFtpDirectory.setLayoutData(fdlFtpDirectory);
	    
	    // Test remote folder  button ...
		wbTestChangeFolderExists=new Button(wRemoteSettings, SWT.PUSH| SWT.CENTER);
		props.setLook(wbTestChangeFolderExists);
		wbTestChangeFolderExists.setText(Messages.getString("JobFTP.TestFolderExists.Label"));
		fdbTestChangeFolderExists=new FormData();
		fdbTestChangeFolderExists.right= new FormAttachment(100, 0);
		fdbTestChangeFolderExists.top  = new FormAttachment(0, margin);
		wbTestChangeFolderExists.setLayoutData(fdbTestChangeFolderExists);

        wFtpDirectory = new TextVar(jobMeta,wRemoteSettings, SWT.SINGLE | SWT.LEFT | SWT.BORDER, Messages
           .getString("JobFTP.RemoteDir.Tooltip"));
       props.setLook(wFtpDirectory);
       wFtpDirectory.addModifyListener(lsMod);
       fdFtpDirectory = new FormData();
       fdFtpDirectory.left = new FormAttachment(middle, margin);
       fdFtpDirectory.top = new FormAttachment(0, margin);
       fdFtpDirectory.right = new FormAttachment(wbTestChangeFolderExists, -margin);
       wFtpDirectory.setLayoutData(fdFtpDirectory);
       
        
        // Wildcard line
        wWildcard = new LabelTextVar(jobMeta,wRemoteSettings, Messages.getString("JobFTP.Wildcard.Label"), Messages
            .getString("JobFTP.Wildcard.Tooltip"));
        props.setLook(wWildcard);
        wWildcard.addModifyListener(lsMod);
        fdWildcard = new FormData();
        fdWildcard.left = new FormAttachment(0, 0);
        fdWildcard.top = new FormAttachment(wFtpDirectory, margin);
        fdWildcard.right = new FormAttachment(100, 0);
        wWildcard.setLayoutData(fdWildcard);
        
        // Remove files after retrieval...
        wlRemove = new Label(wRemoteSettings, SWT.RIGHT);
        wlRemove.setText(Messages.getString("JobFTP.RemoveFiles.Label"));
        props.setLook(wlRemove);
        fdlRemove = new FormData();
        fdlRemove.left = new FormAttachment(0, 0);
        fdlRemove.top = new FormAttachment(wWildcard, margin);
        fdlRemove.right = new FormAttachment(middle, 0);
        wlRemove.setLayoutData(fdlRemove);
        wRemove = new Button(wRemoteSettings, SWT.CHECK);
        wRemove.setToolTipText(Messages.getString("JobFTP.RemoveFiles.Tooltip"));
        props.setLook(wRemove);
        fdRemove = new FormData();
        fdRemove.left = new FormAttachment(middle, margin);
        fdRemove.top = new FormAttachment(wWildcard, margin);
        fdRemove.right = new FormAttachment(100, 0);
        wRemove.setLayoutData(fdRemove);
        
        wRemove.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				if(wRemove.getSelection())
				{
					wMove.setSelection(false);
					activateMoveTo();
				}
				
			}
		});
        
        // Move files after the transfert?...
        wlMove = new Label(wRemoteSettings, SWT.RIGHT);
        wlMove.setText(Messages.getString("JobFTP.MoveFiles.Label"));
        props.setLook(wlMove);
        fdlMove = new FormData();
        fdlMove.left = new FormAttachment(0, 0);
        fdlMove.top = new FormAttachment(wRemove, margin);
        fdlMove.right = new FormAttachment(middle, -margin);
        wlMove.setLayoutData(fdlMove);
        wMove = new Button(wRemoteSettings, SWT.CHECK);
        props.setLook(wMove);
        wMove.setToolTipText(Messages.getString("JobFTP.MoveFiles.Tooltip"));
        fdMove = new FormData();
        fdMove.left = new FormAttachment(middle, margin);
        fdMove.top = new FormAttachment(wRemove, margin);
        fdMove.right = new FormAttachment(100, 0);
        wMove.setLayoutData(fdMove);
        wMove.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				activateMoveTo();
				if(wMove.getSelection())
				{
					wRemove.setSelection(false);
				}
				
			}
		});
        
        // Move to directory
        wlMoveToDirectory = new Label(wRemoteSettings, SWT.RIGHT);
        wlMoveToDirectory.setText(Messages.getString("JobFTP.MoveFolder.Label"));
        props.setLook(wlMoveToDirectory);
        fdlMoveToDirectory= new FormData();
        fdlMoveToDirectory.left = new FormAttachment(0, 0);
        fdlMoveToDirectory.top = new FormAttachment(wMove, margin);
        fdlMoveToDirectory.right = new FormAttachment(middle, 0);
        wlMoveToDirectory.setLayoutData(fdlMoveToDirectory);
        
	    // Test remote folder  button ...
		wbTestFolderExists=new Button(wRemoteSettings, SWT.PUSH| SWT.CENTER);
		props.setLook(wbTestFolderExists);
		wbTestFolderExists.setText(Messages.getString("JobFTP.TestFolderExists.Label"));
		fdbTestFolderExists=new FormData();
		fdbTestFolderExists.right= new FormAttachment(100, 0);
		fdbTestFolderExists.top  = new FormAttachment(wMove, margin);
		wbTestFolderExists.setLayoutData(fdbTestFolderExists);
        
        wMoveToDirectory = new TextVar(jobMeta,wRemoteSettings, SWT.SINGLE | SWT.LEFT | SWT.BORDER, Messages
            .getString("JobFTP.MoveToDirectory.Tooltip"));
        wMoveToDirectory.setToolTipText(Messages.getString("JobFTP.MoveFolder.Tooltip"));
        props.setLook(wMoveToDirectory);
        wMoveToDirectory.addModifyListener(lsMod);
        fdMoveToDirectory = new FormData();
        fdMoveToDirectory.left = new FormAttachment(middle, margin);
        fdMoveToDirectory.top = new FormAttachment(wMove, margin);
        fdMoveToDirectory.right = new FormAttachment(wbTestFolderExists, -margin);
        wMoveToDirectory.setLayoutData(fdMoveToDirectory);
        
        
        // create destination folder?...
        wlCreateMoveFolder = new Label(wRemoteSettings, SWT.RIGHT);
        wlCreateMoveFolder.setText(Messages.getString("JobFTP.CreateMoveFolder.Label"));
        props.setLook(wlCreateMoveFolder);
        fdlCreateMoveFolder = new FormData();
        fdlCreateMoveFolder.left = new FormAttachment(0, 0);
        fdlCreateMoveFolder.top = new FormAttachment(wMoveToDirectory, margin);
        fdlCreateMoveFolder.right = new FormAttachment(middle, 0);
        wlCreateMoveFolder.setLayoutData(fdlCreateMoveFolder);
        wCreateMoveFolder = new Button(wRemoteSettings, SWT.CHECK);
        wCreateMoveFolder.setToolTipText(Messages.getString("JobFTP.CreateMoveFolder.Tooltip"));
        props.setLook(wCreateMoveFolder);
        fdCreateMoveFolder = new FormData();
        fdCreateMoveFolder.left = new FormAttachment(middle, margin);
        fdCreateMoveFolder.top = new FormAttachment(wMoveToDirectory, margin);
        fdCreateMoveFolder.right = new FormAttachment(100, 0);
        wCreateMoveFolder.setLayoutData(fdCreateMoveFolder);
      
        fdRemoteSettings = new FormData();
        fdRemoteSettings.left = new FormAttachment(0, margin);
        fdRemoteSettings.top = new FormAttachment(0, 2*margin);
        fdRemoteSettings.right = new FormAttachment(100, -margin);
        wRemoteSettings.setLayoutData(fdRemoteSettings);
       // ///////////////////////////////////////////////////////////
       // / END OF Remote SETTINGSGROUP
       // ///////////////////////////////////////////////////////////
		
		
		
		
		 // ////////////////////////
	     // START OF LOCAL SETTINGS GROUP///
	     // /
	    wLocalSettings = new Group(wFilesComp, SWT.SHADOW_NONE);
	    props.setLook(wLocalSettings);
	    wLocalSettings.setText(Messages.getString("JobFTP.LocalSettings.Group.Label"));

	    FormLayout LocalSettinsgroupLayout = new FormLayout();
	    LocalSettinsgroupLayout.marginWidth = 10;
	    LocalSettinsgroupLayout.marginHeight = 10;

	    wLocalSettings.setLayout(LocalSettinsgroupLayout);
	      
	     
	     
	    // TargetDirectory
		wlTargetDirectory = new Label(wLocalSettings, SWT.RIGHT);
		wlTargetDirectory.setText(Messages.getString("JobFTP.TargetDir.Label"));
		props.setLook(wlTargetDirectory);
		fdlTargetDirectory = new FormData();
		fdlTargetDirectory.left = new FormAttachment(0, 0);
		fdlTargetDirectory.top = new FormAttachment(wRemoteSettings, margin);
		fdlTargetDirectory.right = new FormAttachment(middle, -margin);
		wlTargetDirectory.setLayoutData(fdlTargetDirectory);
		
	    // Browse folders button ...
		wbTargetDirectory=new Button(wLocalSettings, SWT.PUSH| SWT.CENTER);
		props.setLook(wbTargetDirectory);
		wbTargetDirectory.setText(Messages.getString("JobFTP.BrowseFolders.Label"));
		fdbTargetDirectory=new FormData();
		fdbTargetDirectory.right= new FormAttachment(100, 0);
		fdbTargetDirectory.top  = new FormAttachment(wRemoteSettings, margin);
		wbTargetDirectory.setLayoutData(fdbTargetDirectory);
		wbTargetDirectory.addSelectionListener
			(
				new SelectionAdapter()
				{
					public void widgetSelected(SelectionEvent e)
					{
						DirectoryDialog ddialog = new DirectoryDialog(shell, SWT.OPEN);
						if (wTargetDirectory.getText()!=null)
						{
							ddialog.setFilterPath(jobMeta.environmentSubstitute(wTargetDirectory.getText()) );
						}
						
						 // Calling open() will open and run the dialog.
				        // It will return the selected directory, or
				        // null if user cancels
				        String dir = ddialog.open();
				        if (dir != null) {
				          // Set the text box to the new selection
				        	wTargetDirectory.setText(dir);
				        }
						
					}
				}
			);
			
		
		wTargetDirectory = new TextVar(jobMeta,wLocalSettings, SWT.SINGLE | SWT.LEFT | SWT.BORDER, Messages
			.getString("JobFTP.TargetDir.Tooltip"));
		props.setLook(wTargetDirectory);
		wTargetDirectory.addModifyListener(lsMod);
		fdTargetDirectory = new FormData();
		fdTargetDirectory.left = new FormAttachment(middle, margin);
		fdTargetDirectory.top = new FormAttachment(wRemoteSettings, margin);
		fdTargetDirectory.right = new FormAttachment(wbTargetDirectory, -margin);
		wTargetDirectory.setLayoutData(fdTargetDirectory);
		

       
       
	   	// Create multi-part file?
	   	wlAddDate=new Label(wLocalSettings, SWT.RIGHT);
	   	wlAddDate.setText(Messages.getString("JobFTP.AddDate.Label"));
	   		props.setLook(wlAddDate);
	   	fdlAddDate=new FormData();
	   	fdlAddDate.left = new FormAttachment(0, 0);
	   	fdlAddDate.top  = new FormAttachment(wTargetDirectory, margin);
	   	fdlAddDate.right= new FormAttachment(middle, -margin);
	   	wlAddDate.setLayoutData(fdlAddDate);
	   	wAddDate=new Button(wLocalSettings, SWT.CHECK);
   		props.setLook(wAddDate);
   		wAddDate.setToolTipText(Messages.getString("JobFTP.AddDate.Tooltip"));
	   	fdAddDate=new FormData();
	   	fdAddDate.left = new FormAttachment(middle, margin);
	   	fdAddDate.top  = new FormAttachment(wTargetDirectory, margin);
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
	   	wlAddTime=new Label(wLocalSettings, SWT.RIGHT);
	   	wlAddTime.setText(Messages.getString("JobFTP.AddTime.Label"));
   		props.setLook(wlAddTime);
	   	fdlAddTime=new FormData();
	   	fdlAddTime.left = new FormAttachment(0, 0);
	   	fdlAddTime.top  = new FormAttachment(wAddDate, margin);
	   	fdlAddTime.right= new FormAttachment(middle, -margin);
	   	wlAddTime.setLayoutData(fdlAddTime);
	   	wAddTime=new Button(wLocalSettings, SWT.CHECK);
   		props.setLook(wAddTime);
   		wAddTime.setToolTipText(Messages.getString("JobFTP.AddTime.Tooltip"));
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
	   	wlSpecifyFormat=new Label(wLocalSettings, SWT.RIGHT);
	   	wlSpecifyFormat.setText(Messages.getString("JobFTP.SpecifyFormat.Label"));
	   	props.setLook(wlSpecifyFormat);
	   	fdlSpecifyFormat=new FormData();
	   	fdlSpecifyFormat.left = new FormAttachment(0, 0);
	   	fdlSpecifyFormat.top  = new FormAttachment(wAddTime, margin);
	   	fdlSpecifyFormat.right= new FormAttachment(middle, -margin);
	   	wlSpecifyFormat.setLayoutData(fdlSpecifyFormat);
	   	wSpecifyFormat=new Button(wLocalSettings, SWT.CHECK);
	   	props.setLook(wSpecifyFormat);
	   	wSpecifyFormat.setToolTipText(Messages.getString("JobFTP.SpecifyFormat.Tooltip"));
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
	   				setAddDateBeforeExtension();
	   			}
	   		}
	   	);

   	
	   	//	Prepare a list of possible DateTimeFormats...
	   	String dats[] = Const.getDateFormats();
   	
   		// DateTimeFormat
	   	wlDateTimeFormat=new Label(wLocalSettings, SWT.RIGHT);
       wlDateTimeFormat.setText(Messages.getString("JobFTP.DateTimeFormat.Label"));
       props.setLook(wlDateTimeFormat);
       fdlDateTimeFormat=new FormData();
       fdlDateTimeFormat.left = new FormAttachment(0, 0);
       fdlDateTimeFormat.top  = new FormAttachment(wSpecifyFormat, margin);
       fdlDateTimeFormat.right= new FormAttachment(middle, -margin);
       wlDateTimeFormat.setLayoutData(fdlDateTimeFormat);
       wDateTimeFormat=new CCombo(wLocalSettings, SWT.BORDER | SWT.READ_ONLY);
       wDateTimeFormat.setEditable(true);
       props.setLook(wDateTimeFormat);
       wDateTimeFormat.addModifyListener(lsMod);
       fdDateTimeFormat=new FormData();
       fdDateTimeFormat.left = new FormAttachment(middle, margin);
       fdDateTimeFormat.top  = new FormAttachment(wSpecifyFormat, margin);
       fdDateTimeFormat.right= new FormAttachment(100, 0);
       wDateTimeFormat.setLayoutData(fdDateTimeFormat);
       for (int x=0;x<dats.length;x++) wDateTimeFormat.add(dats[x]);
       
       // Add Date before extension?
       wlAddDateBeforeExtension = new Label(wLocalSettings, SWT.RIGHT);
       wlAddDateBeforeExtension.setText(Messages.getString("JobFTP.AddDateBeforeExtension.Label"));
       props.setLook(wlAddDateBeforeExtension);
       fdlAddDateBeforeExtension = new FormData();
       fdlAddDateBeforeExtension.left = new FormAttachment(0, 0);
       fdlAddDateBeforeExtension.top = new FormAttachment(wDateTimeFormat, margin);
       fdlAddDateBeforeExtension.right = new FormAttachment(middle, -margin);
       wlAddDateBeforeExtension.setLayoutData(fdlAddDateBeforeExtension);
       wAddDateBeforeExtension = new Button(wLocalSettings, SWT.CHECK);
       props.setLook(wAddDateBeforeExtension);
       wAddDateBeforeExtension.setToolTipText(Messages.getString("JobFTP.AddDateBeforeExtension.Tooltip"));
       fdAddDateBeforeExtension = new FormData();
       fdAddDateBeforeExtension.left = new FormAttachment(middle, margin);
       fdAddDateBeforeExtension.top = new FormAttachment(wDateTimeFormat, margin);
       fdAddDateBeforeExtension.right = new FormAttachment(100, 0);
       wAddDateBeforeExtension.setLayoutData(fdAddDateBeforeExtension);
       wAddDateBeforeExtension.addSelectionListener(new SelectionAdapter()
       {
           public void widgetSelected(SelectionEvent e)
           {
               jobEntry.setChanged();
           }
       });

       
       // OnlyNew files after retrieval...
       wlOnlyNew = new Label(wLocalSettings, SWT.RIGHT);
       wlOnlyNew.setText(Messages.getString("JobFTP.DontOverwrite.Label"));
       props.setLook(wlOnlyNew);
       fdlOnlyNew = new FormData();
       fdlOnlyNew.left = new FormAttachment(0, 0);
       fdlOnlyNew.top = new FormAttachment(wAddDateBeforeExtension, margin);
       fdlOnlyNew.right = new FormAttachment(middle, 0);
       wlOnlyNew.setLayoutData(fdlOnlyNew);
       wOnlyNew = new Button(wLocalSettings, SWT.CHECK);
       wOnlyNew.setToolTipText(Messages.getString("JobFTP.DontOverwrite.Tooltip"));
       props.setLook(wOnlyNew);
       fdOnlyNew = new FormData();
       fdOnlyNew.left = new FormAttachment(middle, margin);
       fdOnlyNew.top = new FormAttachment(wAddDateBeforeExtension, margin);
       fdOnlyNew.right = new FormAttachment(100, 0);
       wOnlyNew.setLayoutData(fdOnlyNew);
       wOnlyNew.addSelectionListener(new SelectionAdapter()
       {
           public void widgetSelected(SelectionEvent e)
           {
        	   activeIfExists();
               jobEntry.setChanged();
           }
       });
       
       // Add filenames to result filenames...
       wlAddFilenameToResult = new Label(wLocalSettings, SWT.RIGHT);
       wlAddFilenameToResult.setText(Messages.getString("JobFTP.AddFilenameToResult.Label"));
       props.setLook(wlAddFilenameToResult);
       fdlAddFilenameToResult = new FormData();
       fdlAddFilenameToResult.left = new FormAttachment(0, 0);
       fdlAddFilenameToResult.top = new FormAttachment(wAddDateBeforeExtension, margin);
       fdlAddFilenameToResult.right = new FormAttachment(middle, 0);
       wlAddFilenameToResult.setLayoutData(fdlAddFilenameToResult);
       wAddFilenameToResult = new Button(wLocalSettings, SWT.CHECK);
       wAddFilenameToResult.setToolTipText(Messages.getString("JobFTP.AddFilenameToResult.Tooltip"));
       props.setLook(wAddFilenameToResult);
       fdAddFilenameToResult = new FormData();
       fdAddFilenameToResult.left = new FormAttachment(middle, margin);
       fdAddFilenameToResult.top = new FormAttachment(wAddDateBeforeExtension, margin);
       fdAddFilenameToResult.right = new FormAttachment(100, 0);
       wAddFilenameToResult.setLayoutData(fdAddFilenameToResult);
       
       // If File Exists
		wlIfFileExists = new Label(wLocalSettings, SWT.RIGHT);
		wlIfFileExists.setText(Messages.getString("JobFTP.IfFileExists.Label"));
		props.setLook(wlIfFileExists);
		fdlIfFileExists = new FormData();
		fdlIfFileExists.left = new FormAttachment(0, 0);
		fdlIfFileExists.right = new FormAttachment(middle, 0);
		fdlIfFileExists.top = new FormAttachment(wOnlyNew, margin);
		wlIfFileExists.setLayoutData(fdlIfFileExists);
		wIfFileExists = new CCombo(wLocalSettings, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER);
		wIfFileExists.add(Messages.getString("JobFTP.Skip.Label"));
		wIfFileExists.add(Messages.getString("JobFTP.Give_Unique_Name.Label"));
		wIfFileExists.add(Messages.getString("JobFTP.Fail.Label"));
		wIfFileExists.select(0); // +1: starts at -1

		props.setLook(wIfFileExists);
		fdIfFileExists= new FormData();
		fdIfFileExists.left = new FormAttachment(middle, 0);
		fdIfFileExists.top = new FormAttachment(wOnlyNew, margin);
		fdIfFileExists.right = new FormAttachment(100, 0);
		wIfFileExists.setLayoutData(fdIfFileExists);

		fdIfFileExists = new FormData();
		fdIfFileExists.left = new FormAttachment(middle, margin);
		fdIfFileExists.top = new FormAttachment(wOnlyNew, margin);
		fdIfFileExists.right = new FormAttachment(100, 0);
		wIfFileExists.setLayoutData(fdIfFileExists);
		
		wIfFileExists.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				
				
			}
		});


       // Add filenames to result filenames...
       wlAddFilenameToResult = new Label(wLocalSettings, SWT.RIGHT);
       wlAddFilenameToResult.setText(Messages.getString("JobFTP.AddFilenameToResult.Label"));
       props.setLook(wlAddFilenameToResult);
       fdlAddFilenameToResult = new FormData();
       fdlAddFilenameToResult.left = new FormAttachment(0, 0);
       fdlAddFilenameToResult.top = new FormAttachment(wIfFileExists, 2*margin);
       fdlAddFilenameToResult.right = new FormAttachment(middle, 0);
       wlAddFilenameToResult.setLayoutData(fdlAddFilenameToResult);
       wAddFilenameToResult = new Button(wLocalSettings, SWT.CHECK);
       wAddFilenameToResult.setToolTipText(Messages.getString("JobFTP.AddFilenameToResult.Tooltip"));
       props.setLook(wAddFilenameToResult);
       fdAddFilenameToResult = new FormData();
       fdAddFilenameToResult.left = new FormAttachment(middle, margin);
       fdAddFilenameToResult.top = new FormAttachment(wIfFileExists, 2*margin);
       fdAddFilenameToResult.right = new FormAttachment(100, 0);
       wAddFilenameToResult.setLayoutData(fdAddFilenameToResult);
       
       
      fdLocalSettings = new FormData();
      fdLocalSettings.left = new FormAttachment(0, margin);
      fdLocalSettings.top = new FormAttachment(wRemoteSettings, margin);
      fdLocalSettings.right = new FormAttachment(100, -margin);
      wLocalSettings.setLayoutData(fdLocalSettings);
      // ///////////////////////////////////////////////////////////
      // / END OF LOCAL SETTINGSGROUP
      // ///////////////////////////////////////////////////////////
		
		fdFilesComp=new FormData();
		fdFilesComp.left  = new FormAttachment(0, 0);
		fdFilesComp.top   = new FormAttachment(0, 0);
		fdFilesComp.right = new FormAttachment(100, 0);
		fdFilesComp.bottom= new FormAttachment(100, 0);
		wFilesComp.setLayoutData(fdFilesComp);
		
		wFilesComp.layout();
		wFilesTab.setControl(wFilesComp);
 		props.setLook(wFilesComp);
 	
 		
		/////////////////////////////////////////////////////////////
		/// END OF Files TAB
		/////////////////////////////////////////////////////////////
		
 		//////////////////////////
		// START OF Advanced TAB   ///
		//////////////////////////
		

		wAdvancedTab=new CTabItem(wTabFolder, SWT.NONE);
		wAdvancedTab.setText(Messages.getString("JobFTP.Tab.Advanced.Label"));
		
		wAdvancedComp = new Composite(wTabFolder, SWT.NONE);
 		props.setLook(wAdvancedComp);

		FormLayout AdvancedLayout = new FormLayout();
		AdvancedLayout.marginWidth  = 3;
		AdvancedLayout.marginHeight = 3;
		wAdvancedComp.setLayout(AdvancedLayout);
		
		 // SuccessOngrouping?
	     // ////////////////////////
	     // START OF SUCCESS ON GROUP///
	     // /
	    wSuccessOn= new Group(wAdvancedComp, SWT.SHADOW_NONE);
	    props.setLook(wSuccessOn);
	    wSuccessOn.setText(Messages.getString("JobFTP.SuccessOn.Group.Label"));

	    FormLayout successongroupLayout = new FormLayout();
	    successongroupLayout.marginWidth = 10;
	    successongroupLayout.marginHeight = 10;

	    wSuccessOn.setLayout(successongroupLayout);
	    

	    //Success Condition
	  	wlSuccessCondition = new Label(wSuccessOn, SWT.RIGHT);
	  	wlSuccessCondition.setText(Messages.getString("JobFTP.SuccessCondition.Label") + " ");
	  	props.setLook(wlSuccessCondition);
	  	fdlSuccessCondition = new FormData();
	  	fdlSuccessCondition.left = new FormAttachment(0, 0);
	  	fdlSuccessCondition.right = new FormAttachment(middle, 0);
	  	fdlSuccessCondition.top = new FormAttachment(0, margin);
	  	wlSuccessCondition.setLayoutData(fdlSuccessCondition);
	  	wSuccessCondition = new CCombo(wSuccessOn, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER);
	  	wSuccessCondition.add(Messages.getString("JobFTP.SuccessWhenAllWorksFine.Label"));
	  	wSuccessCondition.add(Messages.getString("JobFTP.SuccessWhenAtLeat.Label"));
	  	wSuccessCondition.add(Messages.getString("JobFTP.SuccessWhenNrErrorsLessThan.Label"));
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
		wlNrErrorsLessThan= new Label(wSuccessOn, SWT.RIGHT);
		wlNrErrorsLessThan.setText(Messages.getString("JobFTP.NrBadFormedLessThan.Label") + " ");
		props.setLook(wlNrErrorsLessThan);
		fdlNrErrorsLessThan= new FormData();
		fdlNrErrorsLessThan.left = new FormAttachment(0, 0);
		fdlNrErrorsLessThan.top = new FormAttachment(wSuccessCondition, margin);
		fdlNrErrorsLessThan.right = new FormAttachment(middle, -margin);
		wlNrErrorsLessThan.setLayoutData(fdlNrErrorsLessThan);
		
		
		wNrErrorsLessThan= new TextVar(jobMeta,wSuccessOn, SWT.SINGLE | SWT.LEFT | SWT.BORDER, Messages
			.getString("JobFTP.NrBadFormedLessThan.Tooltip"));
		props.setLook(wNrErrorsLessThan);
		wNrErrorsLessThan.addModifyListener(lsMod);
		fdNrErrorsLessThan= new FormData();
		fdNrErrorsLessThan.left = new FormAttachment(middle, 0);
		fdNrErrorsLessThan.top = new FormAttachment(wSuccessCondition, margin);
		fdNrErrorsLessThan.right = new FormAttachment(100, -margin);
		wNrErrorsLessThan.setLayoutData(fdNrErrorsLessThan);
		
	
	    fdSuccessOn= new FormData();
	    fdSuccessOn.left = new FormAttachment(0, margin);
	    fdSuccessOn.top = new FormAttachment(0, margin);
	    fdSuccessOn.right = new FormAttachment(100, -margin);
	    wSuccessOn.setLayoutData(fdSuccessOn);
	     // ///////////////////////////////////////////////////////////
	     // / END OF Success ON GROUP
	     // ///////////////////////////////////////////////////////////

		fdAdvancedComp=new FormData();
		fdAdvancedComp.left  = new FormAttachment(0, 0);
		fdAdvancedComp.top   = new FormAttachment(0, 0);
		fdAdvancedComp.right = new FormAttachment(100, 0);
		fdAdvancedComp.bottom= new FormAttachment(100, 0);
		wAdvancedComp.setLayoutData(fdAdvancedComp);
		
		wAdvancedComp.layout();
		wAdvancedTab.setControl(wAdvancedComp);
 		props.setLook(wAdvancedComp);
 		
 		
 		
		/////////////////////////////////////////////////////////////
		/// END OF Advanced TAB
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
        lsTest     = new Listener() { public void handleEvent(Event e) { test(); } };
        lsCheckFolder     = new Listener() { public void handleEvent(Event e) { checkRemoteFolder(false,true,wMoveToDirectory.getText()); } };
        lsCheckChangeFolder     = new Listener() { public void handleEvent(Event e) { checkRemoteFolder(true,false,wFtpDirectory.getText()); } };
        
        wCancel.addListener(SWT.Selection, lsCancel);
        wOK.addListener(SWT.Selection, lsOK);
        wTest.addListener    (SWT.Selection, lsTest    );
        wbTestFolderExists.addListener    (SWT.Selection, lsCheckFolder    );
        wbTestChangeFolderExists.addListener    (SWT.Selection, lsCheckChangeFolder    );
        
        lsDef = new SelectionAdapter()
        {
            public void widgetDefaultSelected(SelectionEvent e)
            {
                ok();
            }
        };

        wName.addSelectionListener(lsDef);
        wServerName.addSelectionListener(lsDef);
        wUserName.addSelectionListener(lsDef);
        wPassword.addSelectionListener(lsDef);
        wFtpDirectory.addSelectionListener(lsDef);
        wTargetDirectory.addSelectionListener(lsDef);
        wFtpDirectory.addSelectionListener(lsDef);
        wWildcard.addSelectionListener(lsDef);
        wTimeout.addSelectionListener(lsDef);

        // Detect X or ALT-F4 or something that kills this window...
        shell.addShellListener(new ShellAdapter()
        {
            public void shellClosed(ShellEvent e)
            {
                cancel();
            }
        });

        getData();
        activateMoveTo();
        setDateTimeFormat();
        setAddDateBeforeExtension();
        activeSuccessCondition();
        activeIfExists();
        
        wTabFolder.setSelection(0);
        BaseStepDialog.setSize(shell);

        shell.open();
        props.setDialogSize(shell, "JobFTPDialogSize");
        while (!shell.isDisposed())
        {
            if (!display.readAndDispatch())
                display.sleep();
        }
        return jobEntry;
    }
    private void activeIfExists()
    {
    	wIfFileExists.setEnabled(wOnlyNew.getSelection());
    	wlIfFileExists.setEnabled(wOnlyNew.getSelection());
    }
    private void test()
    {
    	if(connectToFTP(false,false))
    	{
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_INFORMATION );
			mb.setMessage(Messages.getString("JobFTP.Connected.OK",wServerName.getText()) +Const.CR);
			mb.setText(Messages.getString("JobFTP.Connected.Title.Ok"));
			mb.open();
		}else
		{
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
			mb.setMessage(Messages.getString("JobFTP.Connected.NOK.ConnectionBad",wServerName.getText()) +Const.CR);
			mb.setText(Messages.getString("JobFTP.Connected.Title.Bad"));
			mb.open(); 
	    }
	   
    }
    private void checkRemoteFolder(boolean FTPFolfer,boolean checkMoveFolder,String foldername)
    {
    	if(!Const.isEmpty(foldername))
    	{
	    	if(connectToFTP(FTPFolfer,checkMoveFolder))
	    	{
				MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_INFORMATION );
				mb.setMessage(Messages.getString("JobFTP.FolderExists.OK",foldername) +Const.CR);
				mb.setText(Messages.getString("JobFTP.FolderExists.Title.Ok"));
				mb.open();
			}else
			{
				MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
				mb.setMessage(Messages.getString("JobFTP.FolderExists.NOK",foldername) +Const.CR);
				mb.setText(Messages.getString("JobFTP.FolderExists.Title.Bad"));
				mb.open(); 
		    }
    	}
    }
    private boolean connectToFTP(boolean checkfolder,boolean checkmoveToFolder)
    {
    	boolean retval=false;
		try
		{
			if(ftpclient==null || !ftpclient.connected())
			{
		    	 // Create ftp client to host:port ...
		        ftpclient = new FTPClient();
		        String realServername = jobMeta.environmentSubstitute(wServerName.getText());
		        ftpclient.setRemoteAddr(InetAddress.getByName(realServername));
		        
		        if (!Const.isEmpty(wProxyHost.getText())) 
		        {
		      	  String realProxy_host = jobMeta.environmentSubstitute(wProxyHost.getText());
		      	  ftpclient.setRemoteAddr(InetAddress.getByName(realProxy_host));
		
		      	  // FIXME: Proper default port for proxy    	  
		      	  int port = Const.toInt(jobMeta.environmentSubstitute(wProxyHost.getText()), 21);
		      	  if (port != 0) 
		      	  {
		      	     ftpclient.setRemotePort(port);
		      	  }
		        } 
		        else 
		        {
		            ftpclient.setRemoteAddr(InetAddress.getByName(realServername));
		                           
		        }
	
		        // login to ftp host ...
		        ftpclient.connect();     
		        String realUsername = jobMeta.environmentSubstitute(wUserName.getText()) +
		                              (!Const.isEmpty(wProxyHost.getText()) ? "@" + realServername : "") + 
		                              (!Const.isEmpty(wProxyUsername.getText()) ? " " + jobMeta.environmentSubstitute(wProxyUsername.getText()) 
		                          		                           : ""); 
		           		            
		        String realPassword = jobMeta.environmentSubstitute(wPassword.getText()) + 
		                              (!Const.isEmpty(wProxyPassword.getText()) ? " " + jobMeta.environmentSubstitute(wProxyPassword.getText()) : "" );
		        // login now ...
		        ftpclient.login(realUsername, realPassword);
		        pwdFolder=ftpclient.pwd();
			}  
			
			
			String realFtpDirectory ="";
			if (!Const.isEmpty(wFtpDirectory.getText()))
				realFtpDirectory = jobMeta.environmentSubstitute(wFtpDirectory.getText());
			
	        if(checkfolder)
	        {
	        	if(pwdFolder!=null) ftpclient.chdir(pwdFolder);
	        	// move to spool dir ...
				if (!Const.isEmpty(realFtpDirectory))
					ftpclient.chdir(realFtpDirectory);
	        }
	        if(checkmoveToFolder)
	        {	   
	        	if(pwdFolder!=null) ftpclient.chdir(pwdFolder);
	        	
	        	// move to folder ...
	        	
				if (!Const.isEmpty(wMoveToDirectory.getText()))
				{
	                String realMoveDirectory = jobMeta.environmentSubstitute(wMoveToDirectory.getText());
	                realMoveDirectory=realFtpDirectory+"/"+realMoveDirectory;
	                ftpclient.chdir(realMoveDirectory);
				}	
	        }

	        retval=true;
		}
	     catch (Exception e)
	    {
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
			mb.setMessage(Messages.getString("JobFTP.ErrorConnect.NOK",e.getMessage()) +Const.CR);
			mb.setText(Messages.getString("JobFTP.ErrorConnect.Title.Bad"));
			mb.open(); 
	    } 
	    return retval;
    }
	private void activeSuccessCondition()
	{
		wlNrErrorsLessThan.setEnabled(wSuccessCondition.getSelectionIndex()!=0);
		wNrErrorsLessThan.setEnabled(wSuccessCondition.getSelectionIndex()!=0);	
	}
	private void setAddDateBeforeExtension()
	{
		wlAddDateBeforeExtension.setEnabled(wAddDate.getSelection()||wAddTime.getSelection()||wSpecifyFormat.getSelection() );
		wAddDateBeforeExtension.setEnabled(wAddDate.getSelection()||wAddTime.getSelection()||wSpecifyFormat.getSelection() );
		if(!wAddDate.getSelection()&& !wAddTime.getSelection()&& !wSpecifyFormat.getSelection())
			wAddDateBeforeExtension.setSelection(false);
	}
    public void activateMoveTo()
	{
		wMoveToDirectory.setEnabled(wMove.getSelection());
		wlMoveToDirectory.setEnabled(wMove.getSelection());
		wCreateMoveFolder.setEnabled(wMove.getSelection());
		wlCreateMoveFolder.setEnabled(wMove.getSelection());
		wbTestFolderExists.setEnabled(wMove.getSelection());
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
    	closeFTPConnection();
        WindowProperty winprop = new WindowProperty(shell);
        props.setScreen(winprop);
        shell.dispose();
    }

    /**
     * Copy information from the meta-data input to the dialog fields.
     */
    public void getData()
    {
        if (jobEntry.getName() != null)
            wName.setText(jobEntry.getName());
        wName.getTextWidget().selectAll();

        wServerName.setText(Const.NVL(jobEntry.getServerName(), ""));
        wPort.setText(Const.NVL(jobEntry.getPort(), "21"));
        wUserName.setText(Const.NVL(jobEntry.getUserName(), ""));
        wPassword.setText(Const.NVL(jobEntry.getPassword(), ""));
        wFtpDirectory.setText(Const.NVL(jobEntry.getFtpDirectory(), ""));
        wTargetDirectory.setText(Const.NVL(jobEntry.getTargetDirectory(), ""));
        wWildcard.setText(Const.NVL(jobEntry.getWildcard(), ""));
        wBinaryMode.setSelection(jobEntry.isBinaryMode());
        wTimeout.setText("" + jobEntry.getTimeout());
        wRemove.setSelection(jobEntry.getRemove());
        wOnlyNew.setSelection(jobEntry.isOnlyGettingNewFiles());
        wActive.setSelection(jobEntry.isActiveConnection());
        wControlEncoding.setText(jobEntry.getControlEncoding());
        wMove.setSelection(jobEntry.isMoveFiles());
        wMoveToDirectory.setText(Const.NVL(jobEntry.getMoveToDirectory(), ""));
        
    	wAddDate.setSelection(jobEntry.isDateInFilename());
    	wAddTime.setSelection(jobEntry.isTimeInFilename());
    	
    	if (jobEntry.getDateTimeFormat()!= null) wDateTimeFormat.setText( jobEntry.getDateTimeFormat() );
    	wSpecifyFormat.setSelection(jobEntry.isSpecifyFormat());
    	
    	wAddDateBeforeExtension.setSelection(jobEntry.isAddDateBeforeExtension());
    	wAddFilenameToResult.setSelection(jobEntry.isAddToResult());
    	wCreateMoveFolder.setSelection(jobEntry.isCreateMoveFolder());
    	
        wProxyHost.setText(Const.NVL(jobEntry.getProxyHost(), ""));       
        wProxyPort.setText(Const.NVL(jobEntry.getProxyPort(), ""));
        wProxyUsername.setText(Const.NVL(jobEntry.getProxyUsername(), ""));
        wProxyPassword.setText(Const.NVL(jobEntry.getProxyPassword(), ""));
        
        wIfFileExists.select(jobEntry.ifFileExists);
        
    	if (jobEntry.getLimit()!= null) 
			wNrErrorsLessThan.setText( jobEntry.getLimit());
		else
			wNrErrorsLessThan.setText("10");
		
		
		if(jobEntry.getSuccessCondition()!=null)
		{
			if(jobEntry.getSuccessCondition().equals(jobEntry.SUCCESS_IF_AT_LEAST_X_FILES_DOWNLOADED))
				wSuccessCondition.select(1);
			else if(jobEntry.getSuccessCondition().equals(jobEntry.SUCCESS_IF_ERRORS_LESS))
				wSuccessCondition.select(2);
			else
				wSuccessCondition.select(0);	
		}else wSuccessCondition.select(0);
		
		
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
        jobEntry.setPort(wPort.getText());
        jobEntry.setServerName(wServerName.getText());
        jobEntry.setUserName(wUserName.getText());
        jobEntry.setPassword(wPassword.getText());
        jobEntry.setFtpDirectory(wFtpDirectory.getText());
        jobEntry.setTargetDirectory(wTargetDirectory.getText());
        jobEntry.setWildcard(wWildcard.getText());
        jobEntry.setBinaryMode(wBinaryMode.getSelection());
        jobEntry.setTimeout(Const.toInt(wTimeout.getText(), 10000));
        jobEntry.setRemove(wRemove.getSelection());
        jobEntry.setOnlyGettingNewFiles(wOnlyNew.getSelection());
        jobEntry.setActiveConnection(wActive.getSelection());
        jobEntry.setControlEncoding(wControlEncoding.getText());
        jobEntry.setMoveFiles(wMove.getSelection());
        jobEntry.setMoveToDirectory(wMoveToDirectory.getText());
        
    	jobEntry.setDateInFilename( wAddDate.getSelection() );
    	jobEntry.setTimeInFilename( wAddTime.getSelection() );
    	jobEntry.setSpecifyFormat(wSpecifyFormat.getSelection());
    	jobEntry.setDateTimeFormat(wDateTimeFormat.getText());
    	
    	jobEntry.setAddDateBeforeExtension(wAddDateBeforeExtension.getSelection());
    	jobEntry.setAddToResult(wAddFilenameToResult.getSelection());
    	jobEntry.setCreateMoveFolder(wCreateMoveFolder.getSelection());
    	
        jobEntry.setProxyHost(wProxyHost.getText()); 
        jobEntry.setProxyPort(wProxyPort.getText());
        jobEntry.setProxyUsername(wProxyUsername.getText());
        jobEntry.setProxyPassword(wProxyPassword.getText());
        
        if(wIfFileExists.getSelectionIndex()==1)
        {
        	jobEntry.ifFileExists=jobEntry.ifFileExistsCreateUniq;
        	jobEntry.SifFileExists=jobEntry.SifFileExistsCreateUniq;
        }
        else if(wIfFileExists.getSelectionIndex()==2)
        {
        	jobEntry.ifFileExists=jobEntry.ifFileExistsFail;
        	jobEntry.SifFileExists=jobEntry.SifFileExistsFail;
        }
        else
        {
        	jobEntry.ifFileExists=jobEntry.ifFileExistsSkip;
        	jobEntry.SifFileExists=jobEntry.SifFileExistsSkip;
        }
        
        
        jobEntry.setLimit(wNrErrorsLessThan.getText());

		
		if(wSuccessCondition.getSelectionIndex()==1)
			jobEntry.setSuccessCondition(jobEntry.SUCCESS_IF_AT_LEAST_X_FILES_DOWNLOADED);
		else if(wSuccessCondition.getSelectionIndex()==2)
			jobEntry.setSuccessCondition(jobEntry.SUCCESS_IF_ERRORS_LESS);
		else
			jobEntry.setSuccessCondition(jobEntry.SUCCESS_IF_NO_ERRORS);	
		
		
        dispose();
    }
    
    private void closeFTPConnection()
    {
    	// Close FTP connection if necessary
		if (ftpclient != null && ftpclient.connected())
	      {
	        try
	        {
	          ftpclient.quit();
	          ftpclient=null;
	        } catch (Exception e) {}
	      }
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
}