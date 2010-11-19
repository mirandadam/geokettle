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

package org.pentaho.di.ui.job.entries.ftpput;

import java.net.InetAddress;

import org.eclipse.swt.SWT;
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
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.ui.core.widget.LabelTextVar;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Props;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.job.dialog.JobDialog;
import org.pentaho.di.ui.job.entry.JobEntryDialog;
import org.pentaho.di.job.entry.JobEntryDialogInterface;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.job.entries.ftpput.JobEntryFTPPUT;
import org.pentaho.di.job.entries.ftpput.Messages;

import com.enterprisedt.net.ftp.FTPClient;



/**
 * This dialog allows you to edit the FTP Put job entry settings
 * 
 * @author Samatar
 * @since 15-09-2007
 */

public class JobEntryFTPPUTDialog extends JobEntryDialog implements JobEntryDialogInterface
{
    private Label wlName;

    private Text wName;

    private FormData fdlName, fdName;

    private Label wlServerName;

    private TextVar wServerName;

    private FormData fdlServerName, fdServerName;

    private Label wlServerPort;

    private TextVar wServerPort;

    private FormData fdlServerPort, fdServerPort;

    private Label wlUserName;

    private TextVar wUserName;

    private FormData fdlUserName, fdUserName;

    private Label wlPassword;

    private TextVar wPassword;

    private FormData fdlPassword, fdPassword;

    private Label wlLocalDirectory;

    private TextVar wLocalDirectory;

    private FormData fdlLocalDirectory, fdLocalDirectory;

    private Label wlRemoteDirectory;

    private TextVar wRemoteDirectory;

    private FormData fdlRemoteDirectory, fdRemoteDirectory;

    private Label wlWildcard;

    private TextVar wWildcard;

    private FormData fdlWildcard, fdWildcard;

    private Label wlRemove;

    private Button wRemove;

    private FormData fdlRemove, fdRemove;

    private Button wOK, wCancel;

    private Listener lsOK, lsCancel;
    
	private Listener lsCheckRemoteFolder;

    private JobEntryFTPPUT jobEntry;

    private Shell shell;

	private Button wbTestRemoteDirectoryExists;
	
	private FormData fdbTestRemoteDirectoryExists;
	
	private Button wTest;
	
	private FormData fdTest;
	
	private Listener lsTest;

    private SelectionAdapter lsDef;

    private boolean changed;
    
    private Label wlBinaryMode;

    private Button wBinaryMode;

    private FormData fdlBinaryMode, fdBinaryMode;

    private TextVar wTimeout;
    
    private Label wlTimeout;

    private FormData fdTimeout,fdlTimeout;
    
    private Label wlOnlyNew;

    private Button wOnlyNew;

    private FormData fdlOnlyNew, fdOnlyNew;

    private Label wlActive;

    private Button wActive;

    private FormData fdlActive, fdActive;
    
    private Label        wlControlEncoding;
    
    private Combo        wControlEncoding;
    
    private FormData     fdlControlEncoding, fdControlEncoding;
    
	private CTabFolder   wTabFolder;
	
	private Composite    wGeneralComp,wFilesComp;	
	
	private CTabItem     wGeneralTab,wFilesTab;
	
	private FormData	 fdGeneralComp,fdFilesComp;
	
	private FormData     fdTabFolder;
	
	private Group wSourceSettings,wTargetSettings;
	
    private FormData fdSourceSettings,fdTargetSettings;
	
	private Group wServerSettings;
	
    private FormData fdServerSettings;
    
	private Group wAdvancedSettings;
	
    private FormData fdAdvancedSettings;
    
    private FormData     fdProxyHost;

    private LabelTextVar wProxyPort;

    private FormData     fdProxyPort;

    private LabelTextVar wProxyUsername;

    private FormData     fdProxyUsername;
    
    private LabelTextVar wProxyPassword;
    
    private FormData     fdProxyPasswd;
    
    private LabelTextVar wProxyHost;
    
    // These should not be translated, they are required to exist on all
    // platforms according to the documentation of "Charset".
    private static String[] encodings = { "US-ASCII",
    	                                  "ISO-8859-1",
    	                                  "UTF-8",
    	                                  "UTF-16BE",
    	                                  "UTF-16LE",
    	                                  "UTF-16" }; 
    
    private Button wbLocalDirectory;
    private FormData fdbLocalDirectory;

	private FTPClient ftpclient = null;
	private String  pwdFolder=null;
    
    public JobEntryFTPPUTDialog(Shell parent, JobEntryInterface jobEntryInt, Repository rep, JobMeta jobMeta)
    {
        super(parent, jobEntryInt, rep, jobMeta);
        jobEntry = (JobEntryFTPPUT) jobEntryInt;
        if (this.jobEntry.getName() == null)
            this.jobEntry.setName(Messages.getString("JobFTPPUT.Name.Default"));
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
            	ftpclient=null;
            	pwdFolder=null;
                jobEntry.setChanged();
            }
        };
        changed = jobEntry.hasChanged();

        FormLayout formLayout = new FormLayout();
        formLayout.marginWidth = Const.FORM_MARGIN;
        formLayout.marginHeight = Const.FORM_MARGIN;

        shell.setLayout(formLayout);
        shell.setText(Messages.getString("JobFTPPUT.Title"));

        int middle = props.getMiddlePct();
        int margin = Const.MARGIN;

        // Filename line
        wlName = new Label(shell, SWT.RIGHT);
        wlName.setText(Messages.getString("JobFTPPUT.Name.Label"));
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
        
        wTabFolder = new CTabFolder(shell, SWT.BORDER);
 		props.setLook(wTabFolder, Props.WIDGET_STYLE_TAB);
 		
 		//////////////////////////
		// START OF GENERAL TAB   ///
		//////////////////////////

		wGeneralTab=new CTabItem(wTabFolder, SWT.NONE);
		wGeneralTab.setText(Messages.getString("JobFTPPUT.Tab.General.Label"));
		
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
	    wServerSettings.setText(Messages.getString("JobFTPPUT.ServerSettings.Group.Label"));

	    FormLayout ServerSettingsgroupLayout = new FormLayout();
	    ServerSettingsgroupLayout.marginWidth = 10;
	    ServerSettingsgroupLayout.marginHeight = 10;

	    wServerSettings.setLayout(ServerSettingsgroupLayout);

        // ServerName line
        wlServerName = new Label(wServerSettings, SWT.RIGHT);
        wlServerName.setText(Messages.getString("JobFTPPUT.Server.Label"));
        props.setLook(wlServerName);
        fdlServerName = new FormData();
        fdlServerName.left = new FormAttachment(0, 0);
        fdlServerName.top = new FormAttachment(wName, margin);
        fdlServerName.right = new FormAttachment(middle, 0);
        wlServerName.setLayoutData(fdlServerName);
        wServerName = new TextVar(jobMeta, wServerSettings, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wServerName);
        wServerName.addModifyListener(lsMod);
        fdServerName = new FormData();
        fdServerName.left = new FormAttachment(middle, margin);
        fdServerName.top = new FormAttachment(wName, margin);
        fdServerName.right = new FormAttachment(100, 0);
        wServerName.setLayoutData(fdServerName);

        // ServerPort line
        wlServerPort = new Label(wServerSettings, SWT.RIGHT);
        wlServerPort.setText(Messages.getString("JobFTPPUT.Port.Label"));
        props.setLook(wlServerPort);
        fdlServerPort = new FormData();
        fdlServerPort.left = new FormAttachment(0, 0);
        fdlServerPort.top = new FormAttachment(wServerName, margin);
        fdlServerPort.right = new FormAttachment(middle, 0);
        wlServerPort.setLayoutData(fdlServerPort);
        wServerPort = new TextVar(jobMeta, wServerSettings, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wServerPort);
        wServerPort.setToolTipText(Messages.getString("JobFTPPUT.Port.Tooltip"));
        wServerPort.addModifyListener(lsMod);
        fdServerPort = new FormData();
        fdServerPort.left = new FormAttachment(middle, margin);
        fdServerPort.top = new FormAttachment(wServerName, margin);
        fdServerPort.right = new FormAttachment(100, 0);
        wServerPort.setLayoutData(fdServerPort);

        // UserName line
        wlUserName = new Label(wServerSettings, SWT.RIGHT);
        wlUserName.setText(Messages.getString("JobFTPPUT.Username.Label"));
        props.setLook(wlUserName);
        fdlUserName = new FormData();
        fdlUserName.left = new FormAttachment(0, 0);
        fdlUserName.top = new FormAttachment(wServerPort, margin);
        fdlUserName.right = new FormAttachment(middle, 0);
        wlUserName.setLayoutData(fdlUserName);
        wUserName = new TextVar(jobMeta, wServerSettings, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wUserName);
        wUserName.addModifyListener(lsMod);
        fdUserName = new FormData();
        fdUserName.left = new FormAttachment(middle, margin);
        fdUserName.top = new FormAttachment(wServerPort, margin);
        fdUserName.right = new FormAttachment(100, 0);
        wUserName.setLayoutData(fdUserName);

        // Password line
        wlPassword = new Label(wServerSettings, SWT.RIGHT);
        wlPassword.setText(Messages.getString("JobFTPPUT.Password.Label"));
        props.setLook(wlPassword);
        fdlPassword = new FormData();
        fdlPassword.left = new FormAttachment(0, 0);
        fdlPassword.top = new FormAttachment(wUserName, margin);
        fdlPassword.right = new FormAttachment(middle, 0);
        wlPassword.setLayoutData(fdlPassword);
        wPassword = new TextVar(jobMeta, wServerSettings, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wPassword);
        wPassword.setEchoChar('*');
        wPassword.addModifyListener(lsMod);
        fdPassword = new FormData();
        fdPassword.left = new FormAttachment(middle, margin);
        fdPassword.top = new FormAttachment(wUserName, margin);
        fdPassword.right = new FormAttachment(100, 0);
        wPassword.setLayoutData(fdPassword);
        
        // Proxy host line
        wProxyHost = new LabelTextVar(jobMeta,wServerSettings, Messages.getString("JobFTPPUT.ProxyHost.Label"), Messages.getString("JobFTPPUT.ProxyHost.Tooltip"));
        props.setLook(wProxyHost);
        wProxyHost.addModifyListener(lsMod);
        fdProxyHost = new FormData();
        fdProxyHost.left 	= new FormAttachment(0, 0);
        fdProxyHost.top		= new FormAttachment(wPassword, 2*margin);
        fdProxyHost.right	= new FormAttachment(100, 0);
        wProxyHost.setLayoutData(fdProxyHost);

        // Proxy port line
        wProxyPort = new LabelTextVar(jobMeta,wServerSettings, Messages.getString("JobFTPPUT.ProxyPort.Label"), Messages.getString("JobFTPPUT.ProxyPort.Tooltip"));
        props.setLook(wProxyPort);
        wProxyPort.addModifyListener(lsMod);
        fdProxyPort = new FormData();
        fdProxyPort.left 	= new FormAttachment(0, 0);
        fdProxyPort.top  	= new FormAttachment(wProxyHost, margin);
        fdProxyPort.right	= new FormAttachment(100, 0);
        wProxyPort.setLayoutData(fdProxyPort);

        // Proxy username line
        wProxyUsername = new LabelTextVar(jobMeta,wServerSettings, Messages.getString("JobFTPPUT.ProxyUsername.Label"), Messages.getString("JobFTPPUT.ProxyUsername.Tooltip"));
        props.setLook(wProxyUsername);
        wProxyUsername.addModifyListener(lsMod);
        fdProxyUsername = new FormData();
        fdProxyUsername.left = new FormAttachment(0, 0);
        fdProxyUsername.top  = new FormAttachment(wProxyPort, margin);
        fdProxyUsername.right= new FormAttachment(100, 0);
        wProxyUsername.setLayoutData(fdProxyUsername);
        
        // Proxy password line
        wProxyPassword = new LabelTextVar(jobMeta,wServerSettings, Messages.getString("JobFTPPUT.ProxyPassword.Label"), Messages.getString("JobFTPPUT.ProxyPassword.Tooltip"));
        props.setLook(wProxyPassword);
        wProxyPassword.addModifyListener(lsMod);
        fdProxyPasswd=new FormData();
        fdProxyPasswd.left = new FormAttachment(0, 0);
        fdProxyPasswd.top  = new FormAttachment(wProxyUsername, margin);
        fdProxyPasswd.right= new FormAttachment(100, 0);
        wProxyPassword.setLayoutData(fdProxyPasswd);
        
        
        // Test connection button
		wTest=new Button(wServerSettings,SWT.PUSH);
		wTest.setText(Messages.getString("JobFTPPUT.TestConnection.Label"));
 		props.setLook(wTest);
		fdTest=new FormData();
		wTest.setToolTipText(Messages.getString("JobFTPPUT.TestConnection.Tooltip"));
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
	     wAdvancedSettings.setText(Messages.getString("JobFTPPUT.AdvancedSettings.Group.Label"));
	     FormLayout AdvancedSettingsgroupLayout = new FormLayout();
	     AdvancedSettingsgroupLayout.marginWidth = 10;
	     AdvancedSettingsgroupLayout.marginHeight = 10;
	     wAdvancedSettings.setLayout(AdvancedSettingsgroupLayout);
	     
	     // Binary mode selection...
        wlBinaryMode = new Label(wAdvancedSettings, SWT.RIGHT);
        wlBinaryMode.setText(Messages.getString("JobFTPPUT.BinaryMode.Label"));
        props.setLook(wlBinaryMode);
        fdlBinaryMode = new FormData();
        fdlBinaryMode.left = new FormAttachment(0, 0);
        fdlBinaryMode.top = new FormAttachment(wServerSettings, margin);
        fdlBinaryMode.right = new FormAttachment(middle, 0);
        wlBinaryMode.setLayoutData(fdlBinaryMode);
        wBinaryMode = new Button(wAdvancedSettings, SWT.CHECK);
        props.setLook(wBinaryMode);
        wBinaryMode.setToolTipText(Messages.getString("JobFTPPUT.BinaryMode.Tooltip"));
        fdBinaryMode = new FormData();
        fdBinaryMode.left = new FormAttachment(middle, 0);
        fdBinaryMode.top = new FormAttachment(wServerSettings, margin);
        fdBinaryMode.right = new FormAttachment(100, 0);
        wBinaryMode.setLayoutData(fdBinaryMode);
        
	     // TimeOut...
        wlTimeout = new Label(wAdvancedSettings, SWT.RIGHT);
        wlTimeout.setText(Messages.getString("JobFTPPUT.Timeout.Label"));
        props.setLook(wlTimeout);
        fdlTimeout = new FormData();
        fdlTimeout.left = new FormAttachment(0, 0);
        fdlTimeout.top = new FormAttachment(wBinaryMode, margin);
        fdlTimeout.right = new FormAttachment(middle, 0);
        wlTimeout.setLayoutData(fdlTimeout);
        wTimeout = new TextVar(jobMeta, wAdvancedSettings, SWT.SINGLE | SWT.LEFT | SWT.BORDER, Messages
                .getString("JobFTPPUT.Timeout.Tooltip"));
        props.setLook(wTimeout);
        wTimeout.setToolTipText(Messages.getString("JobFTPPUT.Timeout.Tooltip"));
        fdTimeout = new FormData();
        fdTimeout.left = new FormAttachment(middle, 0);
        fdTimeout.top = new FormAttachment(wBinaryMode, margin);
        fdTimeout.right = new FormAttachment(100, 0);
        wTimeout.setLayoutData(fdTimeout);
        
        // active connection?
        wlActive = new Label(wAdvancedSettings, SWT.RIGHT);
        wlActive.setText(Messages.getString("JobFTPPUT.ActiveConns.Label"));
        props.setLook(wlActive);
        fdlActive = new FormData();
        fdlActive.left = new FormAttachment(0, 0);
        fdlActive.top = new FormAttachment(wTimeout, margin);
        fdlActive.right = new FormAttachment(middle, 0);
        wlActive.setLayoutData(fdlActive);
        wActive = new Button(wAdvancedSettings, SWT.CHECK);
        wActive.setToolTipText(Messages.getString("JobFTPPUT.ActiveConns.Tooltip"));
        props.setLook(wActive);
        fdActive = new FormData();
        fdActive.left = new FormAttachment(middle, 0);
        fdActive.top = new FormAttachment(wTimeout, margin);
        fdActive.right = new FormAttachment(100, 0);
        wActive.setLayoutData(fdActive);
        
        // Control encoding line
        //
        // The drop down is editable as it may happen an encoding may not be present
        // on one machine, but you may want to use it on your execution server
        //
        wlControlEncoding=new Label(wAdvancedSettings, SWT.RIGHT);
        wlControlEncoding.setText(Messages.getString("JobFTPPUT.ControlEncoding.Label"));
        props.setLook(wlControlEncoding);
        fdlControlEncoding=new FormData();
        fdlControlEncoding.left  = new FormAttachment(0, 0);
        fdlControlEncoding.top   = new FormAttachment(wActive, margin);
        fdlControlEncoding.right = new FormAttachment(middle, 0);
        wlControlEncoding.setLayoutData(fdlControlEncoding);
        wControlEncoding=new Combo(wAdvancedSettings, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        wControlEncoding.setToolTipText(Messages.getString("JobFTPPUT.ControlEncoding.Tooltip"));
        wControlEncoding.setItems(encodings);
        props.setLook(wControlEncoding);
        fdControlEncoding=new FormData();
        fdControlEncoding.left = new FormAttachment(middle, 0);
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
		wFilesTab.setText(Messages.getString("JobFTPPUT.Tab.Files.Label"));
		
		wFilesComp = new Composite(wTabFolder, SWT.NONE);
		props.setLook(wFilesComp);
	
		FormLayout FilesLayout = new FormLayout();
		FilesLayout.marginWidth  = 3;
		FilesLayout.marginHeight = 3;
		wFilesComp.setLayout(FilesLayout);
			
		 // ////////////////////////
	     // START OF Source SETTINGS GROUP///
	     // /
	    wSourceSettings = new Group(wFilesComp, SWT.SHADOW_NONE);
	    props.setLook(wSourceSettings);
	    wSourceSettings.setText(Messages.getString("JobFTPPUT.SourceSettings.Group.Label"));
	    FormLayout SourceSettinsgroupLayout = new FormLayout();
	    SourceSettinsgroupLayout.marginWidth = 10;
	    SourceSettinsgroupLayout.marginHeight = 10;
	    wSourceSettings.setLayout(SourceSettinsgroupLayout);
	     

        // Local (source) directory line
        wlLocalDirectory = new Label(wSourceSettings, SWT.RIGHT);
        wlLocalDirectory.setText(Messages.getString("JobFTPPUT.LocalDir.Label"));
        props.setLook(wlLocalDirectory);
        fdlLocalDirectory = new FormData();
        fdlLocalDirectory.left = new FormAttachment(0, 0);
        fdlLocalDirectory.top = new FormAttachment(0, margin);
        fdlLocalDirectory.right = new FormAttachment(middle, -margin);
        wlLocalDirectory.setLayoutData(fdlLocalDirectory);
        
        // Browse folders button ...
		wbLocalDirectory=new Button(wSourceSettings, SWT.PUSH| SWT.CENTER);
		props.setLook(wbLocalDirectory);
		wbLocalDirectory.setText(Messages.getString("JobFTPPUT.BrowseFolders.Label"));
		fdbLocalDirectory=new FormData();
		fdbLocalDirectory.right= new FormAttachment(100, 0);
		fdbLocalDirectory.top  = new FormAttachment(0, margin);
		wbLocalDirectory.setLayoutData(fdbLocalDirectory);
        
        wLocalDirectory = new TextVar(jobMeta, wSourceSettings, SWT.SINGLE | SWT.LEFT | SWT.BORDER, Messages
            .getString("JobFTPPUT.LocalDir.Tooltip"));
        props.setLook(wLocalDirectory);
        wLocalDirectory.addModifyListener(lsMod);
        fdLocalDirectory = new FormData();
        fdLocalDirectory.left = new FormAttachment(middle, 0);
        fdLocalDirectory.top = new FormAttachment(0, margin);
        fdLocalDirectory.right = new FormAttachment(wbLocalDirectory, -margin);
        wLocalDirectory.setLayoutData(fdLocalDirectory);
        
        // Wildcard line
        wlWildcard = new Label(wSourceSettings, SWT.RIGHT);
        wlWildcard.setText(Messages.getString("JobFTPPUT.Wildcard.Label"));
        props.setLook(wlWildcard);
        fdlWildcard = new FormData();
        fdlWildcard.left = new FormAttachment(0, 0);
        fdlWildcard.top = new FormAttachment(wLocalDirectory, margin);
        fdlWildcard.right = new FormAttachment(middle, -margin);
        wlWildcard.setLayoutData(fdlWildcard);
        wWildcard = new TextVar(jobMeta, wSourceSettings, SWT.SINGLE | SWT.LEFT | SWT.BORDER, Messages.getString("JobFTPPUT.Wildcard.Tooltip"));
        props.setLook(wWildcard);
        wWildcard.addModifyListener(lsMod);
        fdWildcard = new FormData();
        fdWildcard.left = new FormAttachment(middle, 0);
        fdWildcard.top = new FormAttachment(wLocalDirectory, margin);
        fdWildcard.right = new FormAttachment(100, 0);
        wWildcard.setLayoutData(fdWildcard);
        
        // Remove files after retrieval...
        wlRemove = new Label(wSourceSettings, SWT.RIGHT);
        wlRemove.setText(Messages.getString("JobFTPPUT.RemoveFiles.Label"));
        props.setLook(wlRemove);
        fdlRemove = new FormData();
        fdlRemove.left = new FormAttachment(0, 0);
        fdlRemove.top = new FormAttachment(wWildcard, 2*margin);
        fdlRemove.right = new FormAttachment(middle, -margin);
        wlRemove.setLayoutData(fdlRemove);
        wRemove = new Button(wSourceSettings, SWT.CHECK);
        props.setLook(wRemove);
        wRemove.setToolTipText(Messages.getString("JobFTPPUT.RemoveFiles.Tooltip"));
        fdRemove = new FormData();
        fdRemove.left = new FormAttachment(middle, 0);
        fdRemove.top = new FormAttachment(wWildcard, 2*margin);
        fdRemove.right = new FormAttachment(100, 0);
        wRemove.setLayoutData(fdRemove);
        
        // OnlyNew files after retrieval...
        wlOnlyNew = new Label(wSourceSettings, SWT.RIGHT);
        wlOnlyNew.setText(Messages.getString("JobFTPPUT.DontOverwrite.Label"));
        props.setLook(wlOnlyNew);
        fdlOnlyNew = new FormData();
        fdlOnlyNew.left = new FormAttachment(0, 0);
        fdlOnlyNew.top = new FormAttachment(wRemove, margin);
        fdlOnlyNew.right = new FormAttachment(middle, 0);
        wlOnlyNew.setLayoutData(fdlOnlyNew);
        wOnlyNew = new Button(wSourceSettings, SWT.CHECK);
        wOnlyNew.setToolTipText(Messages.getString("JobFTPPUT.DontOverwrite.Tooltip"));
        props.setLook(wOnlyNew);
        fdOnlyNew = new FormData();
        fdOnlyNew.left = new FormAttachment(middle, 0);
        fdOnlyNew.top = new FormAttachment(wRemove, margin);
        fdOnlyNew.right = new FormAttachment(100, 0);
        wOnlyNew.setLayoutData(fdOnlyNew);
        
        fdSourceSettings = new FormData();
        fdSourceSettings.left = new FormAttachment(0, margin);
        fdSourceSettings.top = new FormAttachment(0, 2*margin);
        fdSourceSettings.right = new FormAttachment(100, -margin);
        wSourceSettings.setLayoutData(fdSourceSettings);
       // ///////////////////////////////////////////////////////////
       // / END OF Source SETTINGSGROUP
       // ///////////////////////////////////////////////////////////
		
        
		 // ////////////////////////
	     // START OF Target SETTINGS GROUP///
	     // /
	    wTargetSettings = new Group(wFilesComp, SWT.SHADOW_NONE);
	    props.setLook(wTargetSettings);
	    wTargetSettings.setText(Messages.getString("JobFTPPUT.TargetSettings.Group.Label"));
	    FormLayout TargetSettinsgroupLayout = new FormLayout();
	    TargetSettinsgroupLayout.marginWidth = 10;
	    TargetSettinsgroupLayout.marginHeight = 10;
	    wTargetSettings.setLayout(TargetSettinsgroupLayout);

        // Remote Directory line
        wlRemoteDirectory = new Label(wTargetSettings, SWT.RIGHT);
        wlRemoteDirectory.setText(Messages.getString("JobFTPPUT.RemoteDir.Label"));
        props.setLook(wlRemoteDirectory);
        fdlRemoteDirectory = new FormData();
        fdlRemoteDirectory.left = new FormAttachment(0, 0);
        fdlRemoteDirectory.top = new FormAttachment(wSourceSettings, margin);
        fdlRemoteDirectory.right = new FormAttachment(middle, -margin);
        wlRemoteDirectory.setLayoutData(fdlRemoteDirectory);
       
	    // Test remote folder  button ...
		wbTestRemoteDirectoryExists=new Button(wTargetSettings, SWT.PUSH| SWT.CENTER);
		props.setLook(wbTestRemoteDirectoryExists);
		wbTestRemoteDirectoryExists.setText(Messages.getString("JobFTPPUT.TestFolderExists.Label"));
		fdbTestRemoteDirectoryExists=new FormData();
		fdbTestRemoteDirectoryExists.right= new FormAttachment(100, 0);
		fdbTestRemoteDirectoryExists.top  = new FormAttachment(wSourceSettings, margin);
		wbTestRemoteDirectoryExists.setLayoutData(fdbTestRemoteDirectoryExists);
        
        wRemoteDirectory = new TextVar(jobMeta, wTargetSettings, SWT.SINGLE | SWT.LEFT | SWT.BORDER, Messages
            .getString("JobFTPPUT.RemoteDir.Tooltip"));
        props.setLook(wRemoteDirectory);
        wRemoteDirectory.addModifyListener(lsMod);
        fdRemoteDirectory = new FormData();
        fdRemoteDirectory.left = new FormAttachment(middle, 0);
        fdRemoteDirectory.top = new FormAttachment(wSourceSettings, margin);
        fdRemoteDirectory.right = new FormAttachment(wbTestRemoteDirectoryExists, -margin);
        wRemoteDirectory.setLayoutData(fdRemoteDirectory);
        
        fdTargetSettings = new FormData();
        fdTargetSettings.left = new FormAttachment(0, margin);
        fdTargetSettings.top = new FormAttachment(wSourceSettings, margin);
        fdTargetSettings.right = new FormAttachment(100, -margin);
        wTargetSettings.setLayoutData(fdTargetSettings);
       // ///////////////////////////////////////////////////////////
       // / END OF Target SETTINGSGROUP
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
        lsCheckRemoteFolder     = new Listener() { public void handleEvent(Event e) { checkRemoteFolder(jobMeta.environmentSubstitute(wRemoteDirectory.getText())); } };
        
        wbLocalDirectory.addSelectionListener
		(
			new SelectionAdapter()
			{
				public void widgetSelected(SelectionEvent e)
				{
					DirectoryDialog ddialog = new DirectoryDialog(shell, SWT.OPEN);
					if (wLocalDirectory.getText()!=null)
					{
						ddialog.setFilterPath(jobMeta.environmentSubstitute(wLocalDirectory.getText()) );
					}
					
					 // Calling open() will open and run the dialog.
			        // It will return the selected directory, or
			        // null if user cancels
			        String dir = ddialog.open();
			        if (dir != null) {
			          // Set the text box to the new selection
			        	wLocalDirectory.setText(dir);
			        }
					
				}
			}
		);
		

        wCancel.addListener(SWT.Selection, lsCancel);
        wOK.addListener(SWT.Selection, lsOK);
        wbTestRemoteDirectoryExists.addListener(SWT.Selection, lsCheckRemoteFolder    );
  
        wTest.addListener    (SWT.Selection, lsTest    );
        
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
        wRemoteDirectory.addSelectionListener(lsDef);
        wLocalDirectory.addSelectionListener(lsDef);
        wWildcard.addSelectionListener(lsDef);

        // Detect X or ALT-F4 or something that kills this window...
        shell.addShellListener(new ShellAdapter()
        {
            public void shellClosed(ShellEvent e)
            {
                cancel();
            }
        });

        getData();
        wTabFolder.setSelection(0);
        BaseStepDialog.setSize(shell);
        shell.open();
        props.setDialogSize(shell, "JobSFTPDialogSize");
        while (!shell.isDisposed())
        {
            if (!display.readAndDispatch())
                display.sleep();
        }
        return jobEntry;
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
    private void test()
    {
    	if(connectToFTP(false,null))
    	{
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_INFORMATION );
			mb.setMessage(Messages.getString("JobFTPPUT.Connected.OK",wServerName.getText()) +Const.CR);
			mb.setText(Messages.getString("JobFTPPUT.Connected.Title.Ok"));
			mb.open();
		}else
		{
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
			mb.setMessage(Messages.getString("JobFTPPUT.Connected.NOK.ConnectionBad",wServerName.getText()) +Const.CR);
			mb.setText(Messages.getString("JobFTPPUT.Connected.Title.Bad"));
			mb.open(); 
	    }
	   
    }
    private void checkRemoteFolder(String remoteFoldername)
    {
    	if(!Const.isEmpty(remoteFoldername))
    	{
	    	if(connectToFTP(true,remoteFoldername))
	    	{
				MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_INFORMATION );
				mb.setMessage(Messages.getString("JobFTPPUT.FolderExists.OK",remoteFoldername) +Const.CR);
				mb.setText(Messages.getString("JobFTPPUT.FolderExists.Title.Ok"));
				mb.open();
			}else
			{
				MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
				mb.setMessage(Messages.getString("JobFTPPUT.FolderExists.NOK",remoteFoldername) +Const.CR);
				mb.setText(Messages.getString("JobFTPPUT.FolderExists.Title.Bad"));
				mb.open(); 
		    }
    	}
    }
    private boolean connectToFTP(boolean checkfolder,String remoteFoldername)
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
			
	        if(checkfolder)
	        {
	        	if(pwdFolder!=null)ftpclient.chdir(pwdFolder);
	        	// move to spool dir ...
				if (!Const.isEmpty(remoteFoldername))
				{
	                String realFtpDirectory = jobMeta.environmentSubstitute(remoteFoldername);
					ftpclient.chdir(realFtpDirectory);
				}
	        }
	        	
	        retval=true;
		}
	     catch (Exception e)
	    {
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
			mb.setMessage(Messages.getString("JobFTPPUT.ErrorConnect.NOK",e.getMessage()) +Const.CR);
			mb.setText(Messages.getString("JobFTPPUT.ErrorConnect.Title.Bad"));
			mb.open(); 
	    } 
	    return retval;
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
        wName.selectAll();

        wServerName.setText(Const.NVL(jobEntry.getServerName(), ""));
        wServerPort.setText(jobEntry.getServerPort());
        wUserName.setText(Const.NVL(jobEntry.getUserName(), ""));
        wPassword.setText(Const.NVL(jobEntry.getPassword(), ""));
        wRemoteDirectory.setText(Const.NVL(jobEntry.getRemoteDirectory(), ""));
        wLocalDirectory.setText(Const.NVL(jobEntry.getLocalDirectory(), ""));
        wWildcard.setText(Const.NVL(jobEntry.getWildcard(), ""));
        wRemove.setSelection(jobEntry.getRemove());
        wBinaryMode.setSelection(jobEntry.isBinaryMode());
        wTimeout.setText("" + jobEntry.getTimeout());
        wOnlyNew.setSelection(jobEntry.isOnlyPuttingNewFiles());
        wActive.setSelection(jobEntry.isActiveConnection());
        wControlEncoding.setText(jobEntry.getControlEncoding());
        
        wProxyHost.setText(Const.NVL(jobEntry.getProxyHost(), ""));       
        wProxyPort.setText(Const.NVL(jobEntry.getProxyPort(), ""));
        wProxyUsername.setText(Const.NVL(jobEntry.getProxyUsername(), ""));
        wProxyPassword.setText(Const.NVL(jobEntry.getProxyPassword(), ""));
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
        jobEntry.setServerName(wServerName.getText());
        jobEntry.setServerPort(wServerPort.getText());
        jobEntry.setUserName(wUserName.getText());
        jobEntry.setPassword(wPassword.getText());
        jobEntry.setRemoteDirectory(wRemoteDirectory.getText());
        jobEntry.setLocalDirectory(wLocalDirectory.getText());
        jobEntry.setWildcard(wWildcard.getText());
        jobEntry.setRemove(wRemove.getSelection());
        jobEntry.setBinaryMode(wBinaryMode.getSelection());
        jobEntry.setTimeout(Const.toInt(wTimeout.getText(), 10000));
        jobEntry.setOnlyPuttingNewFiles(wOnlyNew.getSelection());
        jobEntry.setActiveConnection(wActive.getSelection());
        jobEntry.setControlEncoding(wControlEncoding.getText());
        
        jobEntry.setProxyHost(wProxyHost.getText()); 
        jobEntry.setProxyPort(wProxyPort.getText());
        jobEntry.setProxyUsername(wProxyUsername.getText());
        jobEntry.setProxyPassword(wProxyPassword.getText());

        dispose();
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
