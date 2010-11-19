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

package org.pentaho.di.ui.job.entries.mail;

import java.nio.charset.Charset;
import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox; 
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.ResultFile;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entries.mail.JobEntryMail;
import org.pentaho.di.job.entries.mail.Messages;
import org.pentaho.di.job.entry.JobEntryDialogInterface;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.core.widget.ControlSpaceKeyAdapter;
import org.pentaho.di.ui.core.widget.LabelText;
import org.pentaho.di.ui.core.widget.LabelTextVar;
import org.pentaho.di.ui.job.dialog.JobDialog;
import org.pentaho.di.ui.job.entry.JobEntryDialog;
import org.pentaho.di.ui.trans.step.BaseStepDialog;


/**
 * Dialog that allows you to edit a JobEntryMail object.
 * 
 * @author Matt
 * @since 19-06-2003
 */
public class JobEntryMailDialog extends JobEntryDialog implements JobEntryDialogInterface
{
    private LabelText wName;

    private FormData fdName;

    private LabelTextVar wDestination;

	private LabelTextVar wDestinationCc;

	private LabelTextVar wDestinationBCc;

    private FormData fdDestination;

	private FormData fdDestinationCc;

	private FormData fdDestinationBCc;

    private LabelTextVar wServer;

    private FormData fdServer;

    private LabelTextVar wPort;

    private FormData fdPort;

    private Label wlUseAuth;

    private Button wUseAuth;

    private FormData fdlUseAuth, fdUseAuth;

    private Label wlUseSecAuth;

    private Button wUseSecAuth;

    private FormData fdlUseSecAuth, fdUseSecAuth;

    private LabelTextVar wAuthUser;

    private FormData fdAuthUser;

    private LabelTextVar wAuthPass;

    private FormData fdAuthPass;

    private LabelTextVar wReply,wReplyName;

    private FormData fdReply,fdReplyName;

    private LabelTextVar wSubject;

    private FormData fdSubject;

    private Label wlAddDate;

    private Button wAddDate;

    private FormData fdlAddDate, fdAddDate;

    private Label wlIncludeFiles;

    private Button wIncludeFiles;

    private FormData fdlIncludeFiles, fdIncludeFiles;

    private Label wlTypes;

    private List wTypes;

    private FormData fdlTypes, fdTypes;

    private Label wlZipFiles;

    private Button wZipFiles;

    private FormData fdlZipFiles, fdZipFiles;

    private LabelTextVar wZipFilename;

    private FormData fdZipFilename;

    private LabelTextVar wPerson;

    private FormData fdPerson;

    private LabelTextVar wPhone;

    private FormData fdPhone;

    private Label wlComment;

    private Text wComment;

    private FormData fdlComment, fdComment;

    private Label wlOnlyComment, wlUseHTML, wlUsePriority;

    private Button wOnlyComment, wUseHTML,wUsePriority;

    private FormData fdlOnlyComment, fdOnlyComment, fdlUseHTML, fdUseHTML, fdUsePriority;
    
    private Label        wlEncoding;
    private CCombo       wEncoding;
    private FormData     fdlEncoding, fdEncoding;
    
    private Label        wlSecureConnectionType;
    private CCombo       wSecureConnectionType;
    private FormData     fdlSecureConnectionType, fdSecureConnectionType;
    
    private Label        wlPriority;
    private CCombo       wPriority;
    private FormData     fdlPriority, fdPriority;
    
    
    private Label        wlImportance;
    private CCombo       wImportance;
    private FormData     fdlImportance, fdImportance;


    private Button wOK, wCancel;

    private Listener lsOK, lsCancel;

    private Shell shell;

    private SelectionAdapter lsDef;

    private JobEntryMail jobEntry;

    private boolean backupDate, backupChanged;

    private Display display;
        
    private boolean  gotEncodings = false;
    
    private LabelTextVar wReplyToAddress;

    private FormData fdReplyToAddress;
    
	private CTabFolder   wTabFolder;
	private Composite    wGeneralComp,wContentComp,wAttachedComp,wMessageComp;	
	private CTabItem     wGeneralTab,wContentTab,wAttachedTab,wMessageTab;
	private FormData	 fdGeneralComp,fdContentComp,fdAttachedComp,fdMessageComp;
	private FormData     fdTabFolder;
	
	private Group wDestinationGroup,wReplyGroup,wServerGroup,
		wAuthentificationGroup,wMessageSettingsGroup,wMessageGroup,wResultFilesGroup;
	private FormData fdDestinationGroup,fdReplyGroup,fdServerGroup,
		fdAuthentificationGroup,fdMessageSettingsGroup,fdMessageGroup,fdResultFilesGroup;
    
    public JobEntryMailDialog(Shell parent, JobEntryInterface jobEntryInt, Repository rep, JobMeta jobMeta)
    {
        super(parent, jobEntryInt, rep, jobMeta);
        jobEntry = (JobEntryMail) jobEntryInt;
    }

    public JobEntryInterface open()
    {
        Shell parent = getParent();
        display = parent.getDisplay();

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
        backupChanged = jobEntry.hasChanged();
        backupDate = jobEntry.getIncludeDate();

        FormLayout formLayout = new FormLayout();
        formLayout.marginWidth = Const.FORM_MARGIN;
        formLayout.marginHeight = Const.FORM_MARGIN;

        shell.setLayout(formLayout);
        shell.setText(Messages.getString("JobMail.Header"));

        int middle = props.getMiddlePct();
        int margin = Const.MARGIN;

        // Name line
        wName = new LabelText(shell, Messages.getString("JobMail.NameOfEntry.Label"), Messages.getString("JobMail.NameOfEntry.Tooltip"));
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
		wGeneralTab.setText(Messages.getString("JobMail.Tab.General.Label"));
		
		wGeneralComp = new Composite(wTabFolder, SWT.NONE);
 		props.setLook(wGeneralComp);

		FormLayout generalLayout = new FormLayout();
		generalLayout.marginWidth  = 3;
		generalLayout.marginHeight = 3;
		wGeneralComp.setLayout(generalLayout);
		
		
		// ////////////////////////
		// START OF Destination Settings GROUP
		// ////////////////////////

		wDestinationGroup = new Group(wGeneralComp, SWT.SHADOW_NONE);
		props.setLook(wDestinationGroup);
		wDestinationGroup.setText(Messages.getString("JobMail.Group.DestinationAddress.Label"));
		
		FormLayout destinationgroupLayout = new FormLayout();
		destinationgroupLayout.marginWidth = 10;
		destinationgroupLayout.marginHeight = 10;
		wDestinationGroup.setLayout(destinationgroupLayout);
        

        // Destination line
        wDestination = new LabelTextVar(jobMeta, wDestinationGroup, Messages.getString("JobMail.DestinationAddress.Label"), Messages.getString("JobMail.DestinationAddress.Tooltip"));
        wDestination.addModifyListener(lsMod);
        fdDestination = new FormData();
        fdDestination.left = new FormAttachment(0, 0);
        fdDestination.top = new FormAttachment(wName, margin);
        fdDestination.right = new FormAttachment(100, 0);
        wDestination.setLayoutData(fdDestination);


		// Destination Cc
		wDestinationCc = new LabelTextVar(jobMeta, wDestinationGroup, Messages.getString("JobMail.DestinationAddressCc.Label"), Messages.getString("JobMail.DestinationAddressCc.Tooltip"));
		wDestinationCc.addModifyListener(lsMod);
		fdDestinationCc = new FormData();
		fdDestinationCc.left = new FormAttachment(0, 0);
		fdDestinationCc.top = new FormAttachment(wDestination, margin);
		fdDestinationCc.right = new FormAttachment(100, 0);
		wDestinationCc.setLayoutData(fdDestinationCc);

		// Destination BCc
		wDestinationBCc = new LabelTextVar(jobMeta, wDestinationGroup, Messages.getString("JobMail.DestinationAddressBCc.Label"), Messages.getString("JobMail.DestinationAddressBCc.Tooltip"));
		wDestinationBCc.addModifyListener(lsMod);
		fdDestinationBCc = new FormData();
		fdDestinationBCc.left = new FormAttachment(0, 0);
		fdDestinationBCc.top = new FormAttachment(wDestinationCc, margin);
		fdDestinationBCc.right = new FormAttachment(100, 0);
		wDestinationBCc.setLayoutData(fdDestinationBCc);


    	fdDestinationGroup = new FormData();
    	fdDestinationGroup.left = new FormAttachment(0, margin);
    	fdDestinationGroup.top = new FormAttachment(wName, margin);
    	fdDestinationGroup.right = new FormAttachment(100, -margin);
		wDestinationGroup.setLayoutData(fdDestinationGroup);
		
		// ///////////////////////////////////////////////////////////
		// / END OF DESTINATION ADDRESS  GROUP
		// ///////////////////////////////////////////////////////////

		
		// ////////////////////////
		// START OF Reply Settings GROUP
		// ////////////////////////

		wReplyGroup = new Group(wGeneralComp, SWT.SHADOW_NONE);
		props.setLook(wReplyGroup);
		wReplyGroup.setText(Messages.getString("JobMail.Group.Reply.Label"));
		
		FormLayout replygroupLayout = new FormLayout();
		replygroupLayout.marginWidth = 10;
		replygroupLayout.marginHeight = 10;
		wReplyGroup.setLayout(replygroupLayout);

	       // Reply name
        wReplyName = new LabelTextVar(jobMeta, wReplyGroup, Messages.getString("JobMail.ReplyName.Label"), 
        		Messages.getString("JobMail.ReplyName.Tooltip"));
        wReplyName.addModifyListener(lsMod);
        fdReplyName = new FormData();
        fdReplyName.left = new FormAttachment(0, 0);
        fdReplyName.top = new FormAttachment(wDestinationGroup, 2*margin);
        fdReplyName.right = new FormAttachment(100, 0);
        wReplyName.setLayoutData(fdReplyName);
        
        // Reply line
        wReply = new LabelTextVar(jobMeta, wReplyGroup, Messages.getString("JobMail.ReplyAddress.Label"), 
        		Messages.getString("JobMail.ReplyAddress.Tooltip"));
        wReply.addModifyListener(lsMod);
        fdReply = new FormData();
        fdReply.left = new FormAttachment(0, 0);
        fdReply.top = new FormAttachment(wReplyName, margin);
        fdReply.right = new FormAttachment(100, 0);
        wReply.setLayoutData(fdReply);


    	fdReplyGroup = new FormData();
    	fdReplyGroup.left = new FormAttachment(0, margin);
    	fdReplyGroup.top = new FormAttachment(wDestinationGroup, margin);
    	fdReplyGroup.right = new FormAttachment(100, -margin);
		wReplyGroup.setLayoutData(fdReplyGroup);
		
		// ///////////////////////////////////////////////////////////
		// / END OF Replay  GROUP
		// ///////////////////////////////////////////////////////////

		
        // Reply to
        wReplyToAddress = new LabelTextVar(jobMeta, wGeneralComp, Messages.getString("JobMail.ReplyToAddress.Label"),
            Messages.getString("JobMail.ReplyToAddress.Tooltip"));
        wReplyToAddress.addModifyListener(lsMod);
        fdReplyToAddress = new FormData();
        fdReplyToAddress.left = new FormAttachment(0, 0);
        fdReplyToAddress.top = new FormAttachment(wReplyGroup, 2*margin);
        fdReplyToAddress.right = new FormAttachment(100, 0);
        wReplyToAddress.setLayoutData(fdReplyToAddress);
        
        // Contact line
        wPerson = new LabelTextVar(jobMeta, wGeneralComp, Messages.getString("JobMail.ContactPerson.Label"), Messages.getString("JobMail.ContactPerson.Tooltip"));
        wPerson.addModifyListener(lsMod);
        fdPerson = new FormData();
        fdPerson.left = new FormAttachment(0, 0);
        fdPerson.top = new FormAttachment(wReplyToAddress, 2*margin);
        fdPerson.right = new FormAttachment(100, 0);
        wPerson.setLayoutData(fdPerson);

        // Phone line
        wPhone = new LabelTextVar(jobMeta, wGeneralComp, Messages.getString("JobMail.ContactPhone.Label"), Messages.getString("JobMail.ContactPhone.Tooltip"));
        wPhone.addModifyListener(lsMod);
        fdPhone = new FormData();
        fdPhone.left = new FormAttachment(0, 0);
        fdPhone.top = new FormAttachment(wPerson, margin);
        fdPhone.right = new FormAttachment(100, 0);
        wPhone.setLayoutData(fdPhone);    
     
		fdGeneralComp=new FormData();
		fdGeneralComp.left  = new FormAttachment(0, 0);
		fdGeneralComp.top   = new FormAttachment(0, 0);
		fdGeneralComp.right = new FormAttachment(100, 0);
		fdGeneralComp.bottom= new FormAttachment(500, -margin);
		wGeneralComp.setLayoutData(fdGeneralComp);
		
		wGeneralComp.layout();
		wGeneralTab.setControl(wGeneralComp);
 		props.setLook(wGeneralComp);
 		
 		
 		
		/////////////////////////////////////////////////////////////
		/// END OF GENERAL TAB
		/////////////////////////////////////////////////////////////
        
 		//////////////////////////////////////
		// START OF SERVER TAB   ///
		/////////////////////////////////////
		
		
		
		wContentTab=new CTabItem(wTabFolder, SWT.NONE);
		wContentTab.setText(Messages.getString("JobMailDialog.Server.Label"));

		FormLayout contentLayout = new FormLayout ();
		contentLayout.marginWidth  = 3;
		contentLayout.marginHeight = 3;
		
		wContentComp = new Composite(wTabFolder, SWT.NONE);
 		props.setLook(wContentComp);
 		wContentComp.setLayout(contentLayout);
 		
		// ////////////////////////
		// START OF SERVER  GROUP
		/////////////////////////// 

		wServerGroup = new Group(wContentComp, SWT.SHADOW_NONE);
		props.setLook(wServerGroup);
		wServerGroup.setText(Messages.getString("JobMail.Group.SMTPServer.Label"));
		
		FormLayout servergroupLayout = new FormLayout();
		servergroupLayout.marginWidth = 10;
		servergroupLayout.marginHeight = 10;
		wServerGroup.setLayout(servergroupLayout);
        
 		
 		  // Server line
        wServer = new LabelTextVar(jobMeta, wServerGroup, Messages.getString("JobMail.SMTPServer.Label"), Messages.getString("JobMail.SMTPServer.Tooltip"));
        wServer.addModifyListener(lsMod);
        fdServer = new FormData();
        fdServer.left = new FormAttachment(0, 0);
        fdServer.top = new FormAttachment(0, margin);
        fdServer.right = new FormAttachment(100, 0);
        wServer.setLayoutData(fdServer);

        // Port line
        wPort = new LabelTextVar(jobMeta, wServerGroup, Messages.getString("JobMail.Port.Label"), Messages.getString("JobMail.Port.Tooltip"));
        wPort.addModifyListener(lsMod);
        fdPort = new FormData();
        fdPort.left = new FormAttachment(0, 0);
        fdPort.top = new FormAttachment(wServer, margin);
        fdPort.right = new FormAttachment(100, 0);
        wPort.setLayoutData(fdPort);
        
    	fdServerGroup = new FormData();
    	fdServerGroup.left = new FormAttachment(0, margin);
    	fdServerGroup.top = new FormAttachment(wName, margin);
    	fdServerGroup.right = new FormAttachment(100, -margin);
		wServerGroup.setLayoutData(fdServerGroup);
		
		// //////////////////////////////////////
		// / END OF SERVER ADDRESS  GROUP
		// ///////////////////////////////////////

		// ////////////////////////////////////
		// START OF AUTHENTIFICATION  GROUP
		////////////////////////////////////// 

		wAuthentificationGroup = new Group(wContentComp, SWT.SHADOW_NONE);
		props.setLook(wAuthentificationGroup);
		wAuthentificationGroup.setText(Messages.getString("JobMail.Group.Authentification.Label"));
		
		FormLayout authentificationgroupLayout = new FormLayout();
		authentificationgroupLayout.marginWidth = 10;
		authentificationgroupLayout.marginHeight = 10;
		wAuthentificationGroup.setLayout(authentificationgroupLayout);
		
		
        // Authentication?
        wlUseAuth = new Label(wAuthentificationGroup, SWT.RIGHT);
        wlUseAuth.setText(Messages.getString("JobMail.UseAuthentication.Label"));
        props.setLook(wlUseAuth);
        fdlUseAuth = new FormData();
        fdlUseAuth.left = new FormAttachment(0, 0);
        fdlUseAuth.top = new FormAttachment(wServerGroup, 2*margin);
        fdlUseAuth.right = new FormAttachment(middle, -margin);
        wlUseAuth.setLayoutData(fdlUseAuth);
        wUseAuth = new Button(wAuthentificationGroup, SWT.CHECK);
        props.setLook(wUseAuth);
        fdUseAuth = new FormData();
        fdUseAuth.left = new FormAttachment(middle, margin);
        fdUseAuth.top = new FormAttachment(wServerGroup, 2*margin);
        fdUseAuth.right = new FormAttachment(100, 0);
        wUseAuth.setLayoutData(fdUseAuth);
        wUseAuth.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent e)
            {
            	setUseAuth();
                jobEntry.setChanged();
            }
        });

        // AuthUser line
        wAuthUser = new LabelTextVar(jobMeta, wAuthentificationGroup, Messages.getString("JobMail.AuthenticationUser.Label"),
            Messages.getString("JobMail.AuthenticationUser.Tooltip"));
        wAuthUser.addModifyListener(lsMod);
        fdAuthUser = new FormData();
        fdAuthUser.left = new FormAttachment(0, 0);
        fdAuthUser.top = new FormAttachment(wUseAuth, margin);
        fdAuthUser.right = new FormAttachment(100, 0);
        wAuthUser.setLayoutData(fdAuthUser);

        // AuthPass line
        wAuthPass = new LabelTextVar(jobMeta, wAuthentificationGroup, Messages.getString("JobMail.AuthenticationPassword.Label"), Messages.getString("JobMail.AuthenticationPassword.Tooltip"));
        wAuthPass.setEchoChar('*');
        wAuthPass.addModifyListener(lsMod);
        fdAuthPass = new FormData();
        fdAuthPass.left = new FormAttachment(0, 0);
        fdAuthPass.top = new FormAttachment(wAuthUser, margin);
        fdAuthPass.right = new FormAttachment(100, 0);
        wAuthPass.setLayoutData(fdAuthPass);

        // Use secure authentication?
        wlUseSecAuth = new Label(wAuthentificationGroup, SWT.RIGHT);
        wlUseSecAuth.setText(Messages.getString("JobMail.UseSecAuthentication.Label"));
        props.setLook(wlUseSecAuth);
        fdlUseSecAuth = new FormData();
        fdlUseSecAuth.left = new FormAttachment(0, 0);
        fdlUseSecAuth.top = new FormAttachment(wAuthPass, 2*margin);
        fdlUseSecAuth.right = new FormAttachment(middle, -margin);
        wlUseSecAuth.setLayoutData(fdlUseSecAuth);
        wUseSecAuth = new Button(wAuthentificationGroup, SWT.CHECK);
        props.setLook(wUseSecAuth);
        fdUseSecAuth = new FormData();
        fdUseSecAuth.left = new FormAttachment(middle, margin);
        fdUseSecAuth.top = new FormAttachment(wAuthPass, 2*margin);
        fdUseSecAuth.right = new FormAttachment(100, 0);
        wUseSecAuth.setLayoutData(fdUseSecAuth);
        wUseSecAuth.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent e)
            {
            	setSecureConnectiontype();
                jobEntry.setChanged();
                
            }
        });
        
        // SecureConnectionType
        wlSecureConnectionType=new Label(wAuthentificationGroup, SWT.RIGHT);
        wlSecureConnectionType.setText(Messages.getString("JobMail.SecureConnectionType.Label"));
        props.setLook(wlSecureConnectionType);
        fdlSecureConnectionType=new FormData();
        fdlSecureConnectionType.left = new FormAttachment(0, 0);
        fdlSecureConnectionType.top  = new FormAttachment(wUseSecAuth, margin);
        fdlSecureConnectionType.right= new FormAttachment(middle, -margin);
        wlSecureConnectionType.setLayoutData(fdlSecureConnectionType);
        wSecureConnectionType=new CCombo(wAuthentificationGroup, SWT.BORDER | SWT.READ_ONLY);
        wSecureConnectionType.setEditable(true);
        props.setLook(wSecureConnectionType);
        wSecureConnectionType.addModifyListener(lsMod);
        fdSecureConnectionType=new FormData();
        fdSecureConnectionType.left = new FormAttachment(middle, margin);
        fdSecureConnectionType.top  = new FormAttachment(wUseSecAuth,margin);
        fdSecureConnectionType.right= new FormAttachment(100, 0);
        wSecureConnectionType.setLayoutData(fdSecureConnectionType);
        wSecureConnectionType.add("SSL");
        wSecureConnectionType.add("TLS");
        wSecureConnectionType.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent e)
            {
            	setSecureConnectiontype();
                jobEntry.setChanged();
                
            }
        });

        
 		
      	fdAuthentificationGroup = new FormData();
      	fdAuthentificationGroup.left = new FormAttachment(0, margin);
      	fdAuthentificationGroup.top = new FormAttachment(wServerGroup, margin);
      	fdAuthentificationGroup.right = new FormAttachment(100, -margin);
      	fdAuthentificationGroup.bottom = new FormAttachment(100, -margin);
      	wAuthentificationGroup.setLayoutData(fdAuthentificationGroup);
		
		// //////////////////////////////////////
		// / END OF AUTHENTIFICATION GROUP
		// ///////////////////////////////////////
 		
		fdContentComp = new FormData();
 		fdContentComp.left  = new FormAttachment(0, 0);
 		fdContentComp.top   = new FormAttachment(0, 0);
 		fdContentComp.right = new FormAttachment(100, 0);
 		fdContentComp.bottom= new FormAttachment(100, 0);
 		wContentComp.setLayoutData(wContentComp);

		wContentComp.layout();
		wContentTab.setControl(wContentComp);


		/////////////////////////////////////////////////////////////
		/// END OF SERVER TAB
		/////////////////////////////////////////////////////////////
 		
		//////////////////////////////////////
		// START OF MESSAGE          TAB   ///
		/////////////////////////////////////
		
		
		
		wMessageTab=new CTabItem(wTabFolder, SWT.NONE);
		wMessageTab.setText(Messages.getString("JobMail.Tab.Message.Label"));

		FormLayout messageLayout = new FormLayout ();
		messageLayout.marginWidth  = 3;
		messageLayout.marginHeight = 3;
		
		wMessageComp = new Composite(wTabFolder, SWT.NONE);
 		props.setLook(wMessageComp);
 		wMessageComp.setLayout(contentLayout);
		
		// ////////////////////////////////////
		// START OF MESSAGE SETTINGS  GROUP
		////////////////////////////////////// 

		wMessageSettingsGroup = new Group(wMessageComp, SWT.SHADOW_NONE);
		props.setLook(wMessageSettingsGroup);
		wMessageSettingsGroup.setText(Messages.getString("JobMail.Group.MessageSettings.Label"));
		
		FormLayout messagesettingsgroupLayout = new FormLayout();
		messagesettingsgroupLayout.marginWidth = 10;
		messagesettingsgroupLayout.marginHeight = 10;
		wMessageSettingsGroup.setLayout(messagesettingsgroupLayout);
        
        // Add date to logfile name?
        wlAddDate = new Label(wMessageSettingsGroup, SWT.RIGHT);
        wlAddDate.setText(Messages.getString("JobMail.IncludeDate.Label"));
        props.setLook(wlAddDate);
        fdlAddDate = new FormData();
        fdlAddDate.left = new FormAttachment(0, 0);
        fdlAddDate.top = new FormAttachment(0,margin);
        fdlAddDate.right = new FormAttachment(middle, -margin);
        wlAddDate.setLayoutData(fdlAddDate);
        wAddDate = new Button(wMessageSettingsGroup, SWT.CHECK);
        props.setLook(wAddDate);
        fdAddDate = new FormData();
        fdAddDate.left = new FormAttachment(middle, margin);
        fdAddDate.top = new FormAttachment(0, margin);
        fdAddDate.right = new FormAttachment(100, 0);
        wAddDate.setLayoutData(fdAddDate);
        wAddDate.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent e)
            {
                jobEntry.setChanged();
            }
        });

        // Only send the comment in the mail body
        wlOnlyComment = new Label(wMessageSettingsGroup, SWT.RIGHT);
        wlOnlyComment.setText(Messages.getString("JobMail.OnlyCommentInBody.Label"));
        props.setLook(wlOnlyComment);
        fdlOnlyComment = new FormData();
        fdlOnlyComment.left = new FormAttachment(0, 0);
        fdlOnlyComment.top = new FormAttachment(wAddDate, margin);
        fdlOnlyComment.right = new FormAttachment(middle, -margin);
        wlOnlyComment.setLayoutData(fdlOnlyComment);
        wOnlyComment = new Button(wMessageSettingsGroup, SWT.CHECK);
        props.setLook(wOnlyComment);
        fdOnlyComment = new FormData();
        fdOnlyComment.left = new FormAttachment(middle, margin);
        fdOnlyComment.top = new FormAttachment(wAddDate, margin );
        fdOnlyComment.right = new FormAttachment(100, 0);
        wOnlyComment.setLayoutData(fdOnlyComment);
        wOnlyComment.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent e)
            {
                jobEntry.setChanged();
            }
        });
        
        
        // HTML format ?
        wlUseHTML = new Label(wMessageSettingsGroup, SWT.RIGHT);
        wlUseHTML.setText(Messages.getString("JobMail.UseHTMLInBody.Label"));
        props.setLook(wlUseHTML);
        fdlUseHTML = new FormData();
        fdlUseHTML.left = new FormAttachment(0, 0);
        fdlUseHTML.top = new FormAttachment(wOnlyComment, margin );
        fdlUseHTML.right = new FormAttachment(middle, -margin);
        wlUseHTML.setLayoutData(fdlUseHTML);
        wUseHTML = new Button(wMessageSettingsGroup, SWT.CHECK);
        props.setLook(wUseHTML);
        fdUseHTML = new FormData();
        fdUseHTML.left = new FormAttachment(middle, margin);
        fdUseHTML.top = new FormAttachment(wOnlyComment, margin );
        fdUseHTML.right = new FormAttachment(100, 0);
        wUseHTML.setLayoutData(fdUseHTML);
        wUseHTML.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent e)
            {
            	SetEnabledEncoding();
            	jobEntry.setChanged();
            }
        });
           
        
        
        // Encoding
        wlEncoding=new Label(wMessageSettingsGroup, SWT.RIGHT);
        wlEncoding.setText(Messages.getString("JobMail.Encoding.Label"));
        props.setLook(wlEncoding);
        fdlEncoding=new FormData();
        fdlEncoding.left = new FormAttachment(0, 0);
        fdlEncoding.top  = new FormAttachment(wUseHTML, margin);
        fdlEncoding.right= new FormAttachment(middle, -margin);
        wlEncoding.setLayoutData(fdlEncoding);
        wEncoding=new CCombo(wMessageSettingsGroup, SWT.BORDER | SWT.READ_ONLY);
        wEncoding.setEditable(true);
        props.setLook(wEncoding);
        wEncoding.addModifyListener(lsMod);
        fdEncoding=new FormData();
        fdEncoding.left = new FormAttachment(middle, margin);
        fdEncoding.top  = new FormAttachment(wUseHTML,margin);
        fdEncoding.right= new FormAttachment(100, 0);
        wEncoding.setLayoutData(fdEncoding);
        wEncoding.addFocusListener(new FocusListener()
            {
                public void focusLost(org.eclipse.swt.events.FocusEvent e)
                {
                }
            
                public void focusGained(org.eclipse.swt.events.FocusEvent e)
                {
                    Cursor busy = new Cursor(shell.getDisplay(), SWT.CURSOR_WAIT);
                    shell.setCursor(busy);
                    setEncodings();
                    shell.setCursor(null);
                    busy.dispose();
                }
            }
        );

        // Use Priority ?
        wlUsePriority = new Label(wMessageSettingsGroup, SWT.RIGHT);
        wlUsePriority.setText(Messages.getString("JobMail.UsePriority.Label"));
        props.setLook(wlUsePriority);
        fdlPriority = new FormData();
        fdlPriority.left = new FormAttachment(0, 0);
        fdlPriority.top = new FormAttachment(wEncoding, margin );
        fdlPriority.right = new FormAttachment(middle, -margin);
        wlUsePriority.setLayoutData(fdlPriority);
        wUsePriority = new Button(wMessageSettingsGroup, SWT.CHECK);
        wUsePriority.setToolTipText(Messages.getString("JobMail.UsePriority.Tooltip"));
        props.setLook(wUsePriority);
        fdUsePriority = new FormData();
        fdUsePriority.left = new FormAttachment(middle, margin);
        fdUsePriority.top = new FormAttachment(wEncoding, margin );
        fdUsePriority.right = new FormAttachment(100, 0);
        wUsePriority.setLayoutData(fdUsePriority);
        wUsePriority.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent e)
            {
            	activeUsePriority();
            	jobEntry.setChanged();
            }
        });
        
        // Priority
        wlPriority = new Label(wMessageSettingsGroup, SWT.RIGHT);
        wlPriority.setText(Messages.getString("JobMail.Priority.Label"));
        props.setLook(wlPriority);
        fdlPriority = new FormData();
        fdlPriority.left = new FormAttachment(0, 0);
        fdlPriority.right = new FormAttachment(middle, -margin);
        fdlPriority.top = new FormAttachment(wUsePriority, margin);
        wlPriority.setLayoutData(fdlPriority);
        wPriority = new CCombo(wMessageSettingsGroup, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER);
        wPriority.add(Messages.getString("JobMail.Priority.Low.Label"));
        wPriority.add(Messages.getString("JobMail.Priority.Normal.Label"));
        wPriority.add(Messages.getString("JobMail.Priority.High.Label"));
        wPriority.select(1); // +1: starts at -1
        props.setLook(wPriority);
        fdPriority = new FormData();
        fdPriority.left = new FormAttachment(middle, 0);
        fdPriority.top = new FormAttachment(wUsePriority, margin);
        fdPriority.right = new FormAttachment(100, 0);
        wPriority.setLayoutData(fdPriority);

        // Importance
        wlImportance = new Label(wMessageSettingsGroup, SWT.RIGHT);
        wlImportance.setText(Messages.getString("JobMail.Importance.Label"));
        props.setLook(wlImportance);
        fdlImportance = new FormData();
        fdlImportance.left = new FormAttachment(0, 0);
        fdlImportance.right = new FormAttachment(middle, -margin);
        fdlImportance.top = new FormAttachment(wPriority, margin);
        wlImportance.setLayoutData(fdlImportance);
        wImportance = new CCombo(wMessageSettingsGroup, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER);
        wImportance.add(Messages.getString("JobMail.Priority.Low.Label"));
        wImportance.add(Messages.getString("JobMail.Priority.Normal.Label"));
        wImportance.add(Messages.getString("JobMail.Priority.High.Label"));
   
        wImportance.select(1); // +1: starts at -1
        
        props.setLook(wImportance);
        fdImportance = new FormData();
        fdImportance.left = new FormAttachment(middle, 0);
        fdImportance.top = new FormAttachment(wPriority, margin);
        fdImportance.right = new FormAttachment(100, 0);
        wImportance.setLayoutData(fdImportance);
        
        
        fdMessageSettingsGroup = new FormData();
    	fdMessageSettingsGroup.left = new FormAttachment(0, margin);
      	fdMessageSettingsGroup.top = new FormAttachment(wName, margin);
      	fdMessageSettingsGroup.right = new FormAttachment(100, -margin);
      	wMessageSettingsGroup.setLayoutData(fdMessageSettingsGroup);
		
		// //////////////////////////////////////
		// / END OF MESSAGE SETTINGS  GROUP
		// ///////////////////////////////////////
      	
      	
      	
    	// ////////////////////////////////////
		// START OF MESSAGE   GROUP
		////////////////////////////////////// 

		wMessageGroup = new Group(wMessageComp, SWT.SHADOW_NONE);
		props.setLook(wMessageGroup);
		wMessageGroup.setText(Messages.getString("JobMail.Group.Message.Label"));
		
		FormLayout messagegroupLayout = new FormLayout();
		messagegroupLayout.marginWidth = 10;
		messagegroupLayout.marginHeight = 10;
		wMessageGroup.setLayout(messagegroupLayout);
        
        // Subject line
        wSubject = new LabelTextVar(jobMeta, wMessageGroup, Messages.getString("JobMail.Subject.Label"), Messages.getString("JobMail.Subject.Tooltip"));
        wSubject.addModifyListener(lsMod);
        fdSubject = new FormData();
        fdSubject.left = new FormAttachment(0, 0);
        fdSubject.top = new FormAttachment(wMessageSettingsGroup, margin);
        fdSubject.right = new FormAttachment(100, 0);
        wSubject.setLayoutData(fdSubject);
 		
        // Comment line
        wlComment = new Label(wMessageGroup, SWT.RIGHT);
        wlComment.setText(Messages.getString("JobMail.Comment.Label"));
        props.setLook(wlComment);
        fdlComment = new FormData();
        fdlComment.left = new FormAttachment(0, 0);
        fdlComment.top = new FormAttachment(wSubject, 2*margin);
        fdlComment.right = new FormAttachment(middle, margin);
        wlComment.setLayoutData(fdlComment);

        wComment = new Text(wMessageGroup, SWT.MULTI | SWT.LEFT | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
        props.setLook(wComment);
        wComment.addModifyListener(lsMod);
        fdComment = new FormData();
        fdComment.left = new FormAttachment(middle, margin);
        fdComment.top = new FormAttachment(wSubject, 2*margin);
        fdComment.right = new FormAttachment(100, 0);
        fdComment.bottom = new FormAttachment(100, -margin);
        wComment.setLayoutData(fdComment);
        //SelectionAdapter lsVar = VariableButtonListenerFactory.getSelectionAdapter(shell, wComment, jobMeta);
        wComment.addKeyListener(new ControlSpaceKeyAdapter(jobMeta, wComment));
        
        fdMessageGroup = new FormData();
        fdMessageGroup.left = new FormAttachment(0, margin);
    	fdMessageGroup.top = new FormAttachment(wMessageSettingsGroup, margin);
    	fdMessageGroup.bottom = new FormAttachment(100, -margin);
      	fdMessageGroup.right = new FormAttachment(100, -margin);
      	wMessageGroup.setLayoutData(fdMessageGroup);
		
		// //////////////////////////////////////
		// / END OF MESSAGE   GROUP
		// ///////////////////////////////////////
 		
		fdMessageComp = new FormData();
		fdMessageComp.left  = new FormAttachment(0, 0);
		fdMessageComp.top   = new FormAttachment(0, 0);
		fdMessageComp.right = new FormAttachment(100, 0);
		fdMessageComp.bottom= new FormAttachment(100, 0);
		wMessageComp.setLayoutData(wMessageComp);

		wMessageComp.layout();
		wMessageTab.setControl(wMessageComp);


		/////////////////////////////////////////////////////////////
		/// END OF MESSAGE TAB
		/////////////////////////////////////////////////////////////
 		
 		
		
		//////////////////////////////////////
		// START OF ATTACHED FILES   TAB   ///
		/////////////////////////////////////
		
		
		
		wAttachedTab=new CTabItem(wTabFolder, SWT.NONE);
		wAttachedTab.setText(Messages.getString("JobMail.Tab.AttachedFiles.Label"));

		FormLayout attachedLayout = new FormLayout ();
		attachedLayout.marginWidth  = 3;
		attachedLayout.marginHeight = 3;
		
		wAttachedComp = new Composite(wTabFolder, SWT.NONE);
 		props.setLook(wAttachedComp);
 		wAttachedComp.setLayout(attachedLayout);
		
 		// ////////////////////////////////////
		// START OF Result File   GROUP
		////////////////////////////////////// 

		wResultFilesGroup = new Group(wAttachedComp, SWT.SHADOW_NONE);
		props.setLook(wResultFilesGroup);
		wResultFilesGroup.setText(Messages.getString("JobMail.Group.AddPreviousFiles.Label"));
		
		FormLayout resultfilesgroupLayout = new FormLayout();
		resultfilesgroupLayout.marginWidth = 10;
		resultfilesgroupLayout.marginHeight = 10;
		wResultFilesGroup.setLayout(resultfilesgroupLayout);
		
		
 		 // Include Files?
        wlIncludeFiles = new Label(wResultFilesGroup, SWT.RIGHT);
        wlIncludeFiles.setText(Messages.getString("JobMail.AttachFiles.Label"));
        props.setLook(wlIncludeFiles);
        fdlIncludeFiles = new FormData();
        fdlIncludeFiles.left = new FormAttachment(0, 0);
        fdlIncludeFiles.top = new FormAttachment(0, margin);
        fdlIncludeFiles.right = new FormAttachment(middle, -margin);
        wlIncludeFiles.setLayoutData(fdlIncludeFiles);
        wIncludeFiles = new Button(wResultFilesGroup, SWT.CHECK);
        props.setLook(wIncludeFiles);
        fdIncludeFiles = new FormData();
        fdIncludeFiles.left = new FormAttachment(middle, margin);
        fdIncludeFiles.top = new FormAttachment(0, margin);
        fdIncludeFiles.right = new FormAttachment(100, 0);
        wIncludeFiles.setLayoutData(fdIncludeFiles);
        wIncludeFiles.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent e)
            {
                jobEntry.setChanged();
                setFlags();
            }
        });

        // Include Files?
        wlTypes = new Label(wResultFilesGroup, SWT.RIGHT);
        wlTypes.setText(Messages.getString("JobMail.SelectFileTypes.Label"));
        props.setLook(wlTypes);
        fdlTypes = new FormData();
        fdlTypes.left = new FormAttachment(0, 0);
        fdlTypes.top = new FormAttachment(wIncludeFiles, margin);
        fdlTypes.right = new FormAttachment(middle, -margin);
        wlTypes.setLayoutData(fdlTypes);
        wTypes = new List(wResultFilesGroup, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
        props.setLook(wTypes);
        fdTypes = new FormData();
        fdTypes.left = new FormAttachment(middle, margin);
        fdTypes.top = new FormAttachment(wIncludeFiles, margin);
        fdTypes.bottom = new FormAttachment(wIncludeFiles, margin + 150);
        fdTypes.right = new FormAttachment(100, 0);
        wTypes.setLayoutData(fdTypes);
        for (int i = 0; i < ResultFile.getAllTypeDesc().length; i++)
        {
            wTypes.add(ResultFile.getAllTypeDesc()[i]);
        }

        // Zip Files?
        wlZipFiles = new Label(wResultFilesGroup, SWT.RIGHT);
        wlZipFiles.setText(Messages.getString("JobMail.ZipFiles.Label"));
        props.setLook(wlZipFiles);
        fdlZipFiles = new FormData();
        fdlZipFiles.left = new FormAttachment(0, 0);
        fdlZipFiles.top = new FormAttachment(wTypes, margin);
        fdlZipFiles.right = new FormAttachment(middle, -margin);
        wlZipFiles.setLayoutData(fdlZipFiles);
        wZipFiles = new Button(wResultFilesGroup, SWT.CHECK);
        props.setLook(wZipFiles);
        fdZipFiles = new FormData();
        fdZipFiles.left = new FormAttachment(middle, margin);
        fdZipFiles.top = new FormAttachment(wTypes, margin);
        fdZipFiles.right = new FormAttachment(100, 0);
        wZipFiles.setLayoutData(fdZipFiles);
        wZipFiles.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent e)
            {
                jobEntry.setChanged();
                setFlags();
            }
        });

        // ZipFilename line
        wZipFilename = new LabelTextVar(jobMeta, wResultFilesGroup, Messages.getString("JobMail.ZipFilename.Label"),
            Messages.getString("JobMail.ZipFilename.Tooltip"));
        wZipFilename.addModifyListener(lsMod);
        fdZipFilename = new FormData();
        fdZipFilename.left = new FormAttachment(0, 0);
        fdZipFilename.top = new FormAttachment(wZipFiles, margin);
        fdZipFilename.right = new FormAttachment(100, 0);
        wZipFilename.setLayoutData(fdZipFilename);
		
        fdResultFilesGroup = new FormData();
        fdResultFilesGroup.left = new FormAttachment(0, margin);
        fdResultFilesGroup.top = new FormAttachment(0, margin);
    	fdResultFilesGroup.bottom = new FormAttachment(100, -margin);
    	fdResultFilesGroup.right = new FormAttachment(100, -margin);
      	wResultFilesGroup.setLayoutData(fdResultFilesGroup);
		
		// //////////////////////////////////////
		// / END OF RESULT FILES   GROUP
		// ///////////////////////////////////////
		
		fdAttachedComp = new FormData();
		fdAttachedComp.left  = new FormAttachment(0, 0);
		fdAttachedComp.top   = new FormAttachment(0, 0);
		fdAttachedComp.right = new FormAttachment(100, 0);
		fdAttachedComp.bottom= new FormAttachment(100, 0);
		wAttachedComp.setLayoutData(wAttachedComp);

		wAttachedComp.layout();
		wAttachedTab.setControl(wAttachedComp);


		/////////////////////////////////////////////////////////////
		/// END OF FILES TAB
		/////////////////////////////////////////////////////////////
 		
		
		
		
		
		
		fdTabFolder = new FormData();
		fdTabFolder.left  = new FormAttachment(0, 0);
		fdTabFolder.top   = new FormAttachment(wName, margin);
		fdTabFolder.right = new FormAttachment(100, 0);
		fdTabFolder.bottom= new FormAttachment(100, -50);
		wTabFolder.setLayoutData(fdTabFolder);

 		
        // Some buttons
        wOK = new Button(shell, SWT.PUSH);
        wOK.setText(Messages.getString("System.Button.OK"));
        wCancel = new Button(shell, SWT.PUSH);
        wCancel.setText(Messages.getString("System.Button.Cancel"));

        BaseStepDialog.positionBottomButtons(shell, new Button[] { wOK, wCancel }, margin, wTabFolder);
        //setButtonPositions(new Button[] { wOK, wCancel }, margin, wTabFolder);

        
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

        wOK.addListener(SWT.Selection, lsOK);
        wCancel.addListener(SWT.Selection, lsCancel);

        lsDef = new SelectionAdapter()
        {
            public void widgetDefaultSelected(SelectionEvent e)
            {
                ok();
            }
        };
        wName.addSelectionListener(lsDef);
        wServer.addSelectionListener(lsDef);
        wSubject.addSelectionListener(lsDef);
        wDestination.addSelectionListener(lsDef);
		wDestinationCc.addSelectionListener(lsDef);
		wDestinationBCc.addSelectionListener(lsDef);
        wReply.addSelectionListener(lsDef);
        wPerson.addSelectionListener(lsDef);
        wPhone.addSelectionListener(lsDef);
        wZipFilename.addSelectionListener(lsDef);

        // Detect [X] or ALT-F4 or something that kills this window...
        shell.addShellListener(new ShellAdapter()
        {
            public void shellClosed(ShellEvent e)
            {
                cancel();
            }
        });

        // BaseStepDialog.setTraverseOrder(new Control[] {wName, wDestination, wServer, wUseAuth,
        // wAuthUser, wAuthPass, wReply,
        // wSubject, wAddDate, wIncludeFiles, wTypes, wZipFiles, wZipFilename, wPerson, wPhone,
        // wComment, wOK, wCancel });

        getData();

        SetEnabledEncoding();
        activeUsePriority();
        setFlags();
        setUseAuth();
        BaseStepDialog.setSize(shell);

        shell.open();
        props.setDialogSize(shell, "JobMailDialogSize");
        wTabFolder.setSelection(0);
        while (!shell.isDisposed())
        {
            if (!display.readAndDispatch())
                display.sleep();
        }
        return jobEntry;
    }
	private void activeUsePriority()
	{
		wlPriority.setEnabled(wUsePriority.getSelection());
		wPriority.setEnabled(wUsePriority.getSelection());
		wlImportance.setEnabled(wUsePriority.getSelection());
		wImportance.setEnabled(wUsePriority.getSelection());
	}
    private void SetEnabledEncoding ()
    {
        wEncoding.setEnabled(wUseHTML.getSelection());
        wlEncoding.setEnabled(wUseHTML.getSelection());
        	
    }
    protected void setSecureConnectiontype()
    {
    	wSecureConnectionType.setEnabled(wUseSecAuth.getSelection());
    	wlSecureConnectionType.setEnabled(wUseSecAuth.getSelection());
    }
    protected void setFlags()
    {
        wlTypes.setEnabled(wIncludeFiles.getSelection());
        wTypes.setEnabled(wIncludeFiles.getSelection());
        wlZipFiles.setEnabled(wIncludeFiles.getSelection());
        wZipFiles.setEnabled(wIncludeFiles.getSelection());
        wZipFilename.setEnabled(wIncludeFiles.getSelection() && wZipFiles.getSelection());


    }
	protected void setUseAuth()
	{
        wAuthUser.setEnabled(wUseAuth.getSelection());
        wAuthPass.setEnabled(wUseAuth.getSelection());
        wUseSecAuth.setEnabled(wUseAuth.getSelection());
        wlUseSecAuth.setEnabled(wUseAuth.getSelection());
        if (!wUseAuth.getSelection())
        {
        	wSecureConnectionType.setEnabled(false);
        	wlSecureConnectionType.setEnabled(false);
        }
        else
        {
        	setSecureConnectiontype();
        }
        
	}
    public void dispose()
    {
        WindowProperty winprop = new WindowProperty(shell);
        props.setScreen(winprop);
        shell.dispose();
    }

    public void getData()
    {
        if (jobEntry.getName() != null)
            wName.setText(jobEntry.getName());
        if (jobEntry.getDestination() != null)
            wDestination.setText(jobEntry.getDestination());
		if (jobEntry.getDestinationCc() != null)
			wDestinationCc.setText(jobEntry.getDestinationCc());
		if (jobEntry.getDestinationBCc() != null)
			wDestinationBCc.setText(jobEntry.getDestinationBCc());
        if (jobEntry.getServer() != null)
            wServer.setText(jobEntry.getServer());
        if (jobEntry.getPort() != null)
            wPort.setText(jobEntry.getPort());
        if (jobEntry.getReplyAddress() != null)
            wReply.setText(jobEntry.getReplyAddress());
        if (jobEntry.getReplyName() != null)
            wReplyName.setText(jobEntry.getReplyName());
        if (jobEntry.getSubject() != null)
            wSubject.setText(jobEntry.getSubject());
        if (jobEntry.getContactPerson() != null)
            wPerson.setText(jobEntry.getContactPerson());
        if (jobEntry.getContactPhone() != null)
            wPhone.setText(jobEntry.getContactPhone());
        if (jobEntry.getComment() != null)
            wComment.setText(jobEntry.getComment());

        wAddDate.setSelection(jobEntry.getIncludeDate());
        wIncludeFiles.setSelection(jobEntry.isIncludingFiles());

        if (jobEntry.getFileType() != null)
        {
            int types[] = jobEntry.getFileType();
            wTypes.setSelection(types);
        }

        wZipFiles.setSelection(jobEntry.isZipFiles());
        if (jobEntry.getZipFilename() != null)
            wZipFilename.setText(jobEntry.getZipFilename());

        wUseAuth.setSelection(jobEntry.isUsingAuthentication());
        wUseSecAuth.setSelection(jobEntry.isUsingSecureAuthentication());
        if (jobEntry.getAuthenticationUser() != null)
            wAuthUser.setText(jobEntry.getAuthenticationUser());
        if (jobEntry.getAuthenticationPassword() != null)
            wAuthPass.setText(jobEntry.getAuthenticationPassword());

        wOnlyComment.setSelection(jobEntry.isOnlySendComment());
        
        wUseHTML.setSelection(jobEntry.isUseHTML());
        
        if (jobEntry.getEncoding()!=null) 
        {
        	wEncoding.setText(""+jobEntry.getEncoding());
        }else {
        	
        	wEncoding.setText("UTF-8");}
        	
        // Secure connection type	
        if (jobEntry.getSecureConnectionType() !=null)
        {
        	wSecureConnectionType.setText(jobEntry.getSecureConnectionType());
        }
        else
        {
        	wSecureConnectionType.setText("SSL");
        }
        wUsePriority.setSelection(jobEntry.isUsePriority());
        
        // Priority
        
        if (jobEntry.getPriority()!=null)
        {
	        if (jobEntry.getPriority().equals("low")) 
	        {
	        	wPriority.select(0); // Low
	        }
	        else if (jobEntry.getPriority().equals("normal")) 
	        {	
	        	wPriority.select(1); // Normal
	        
	        }
		    else 
		    {	
		    	wPriority.select(2);  // Default High
		    
		    }
        }
        else
        {
        	wPriority.select(3);  // Default High
        }
        
        // Importance
        if (jobEntry.getImportance()!=null)
        {
	        if (jobEntry.getImportance().equals("low")) 
	        {
	        	wImportance.select(0); // Low
	        }
	        else if (jobEntry.getImportance().equals("normal")) 
	        {	
	        	wImportance.select(1); // Normal
	        
	        }
		    else 
		    {	
		    	wImportance.select(2);  // Default High
		    
		    }
        }
        else
        {
        	wImportance.select(3);  // Default High
        }
        
        if (jobEntry.getReplyToAddresses() != null)
            wReplyToAddress.setText(jobEntry.getReplyToAddresses());
    }

    private void cancel()
    {
        jobEntry.setChanged(backupChanged);
        jobEntry.setIncludeDate(backupDate);

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
        jobEntry.setDestination(wDestination.getText());
		jobEntry.setDestinationCc(wDestinationCc.getText());
		jobEntry.setDestinationBCc(wDestinationBCc.getText());
        jobEntry.setServer(wServer.getText());
        jobEntry.setPort(wPort.getText());
        jobEntry.setReplyAddress(wReply.getText());
        jobEntry.setReplyName(wReplyName.getText());
        jobEntry.setSubject(wSubject.getText());
        jobEntry.setContactPerson(wPerson.getText());
        jobEntry.setContactPhone(wPhone.getText());
        jobEntry.setComment(wComment.getText());

        jobEntry.setIncludeDate(wAddDate.getSelection());
        jobEntry.setIncludingFiles(wIncludeFiles.getSelection());
        jobEntry.setFileType(wTypes.getSelectionIndices());
        jobEntry.setZipFilename(wZipFilename.getText());
        jobEntry.setZipFiles(wZipFiles.getSelection());
        jobEntry.setAuthenticationUser(wAuthUser.getText());
        jobEntry.setAuthenticationPassword(wAuthPass.getText());
        jobEntry.setUsingAuthentication(wUseAuth.getSelection());
        jobEntry.setUsingSecureAuthentication(wUseSecAuth.getSelection());
        jobEntry.setOnlySendComment(wOnlyComment.getSelection());
        jobEntry.setUseHTML(wUseHTML.getSelection());
        jobEntry.setUsePriority(wUsePriority.getSelection());
        
        jobEntry.setEncoding(wEncoding.getText());
        jobEntry.setPriority(wPriority.getText());
        
        // Priority
        if (wPriority.getSelectionIndex()==0)
        {
        	jobEntry.setPriority("low");
        }
        else if (wPriority.getSelectionIndex()==1)
        {
        	jobEntry.setPriority("normal");
        }
        else
        {
        	jobEntry.setPriority("high");
        }
            
        // Importance
        if (wImportance.getSelectionIndex()==0)
        {
        	jobEntry.setImportance("low");
        }
        else if (wImportance.getSelectionIndex()==1)
        {
        	jobEntry.setImportance("normal");
        }
        else
        {
        	jobEntry.setImportance("high");
        }
                  
        // Secure Connection type
        jobEntry.setSecureConnectionType(wSecureConnectionType.getText());
        
        jobEntry.setReplyToAddresses(wReplyToAddress.getText());
        
        dispose();
    }
    

	private void setEncodings()
    {
        // Encoding of the text file:
        if (!gotEncodings)
        {
            gotEncodings = true;
            
            wEncoding.removeAll();
            java.util.List<Charset> values = new ArrayList<Charset>(Charset.availableCharsets().values());
            for (Charset charSet:values)
            {
                wEncoding.add( charSet.displayName() );
            }
            
            // Now select the default!
            String defEncoding = Const.getEnvironmentVariable("file.encoding", "UTF-8");
            int idx = Const.indexOfString(defEncoding, wEncoding.getItems() );
            if (idx>=0) wEncoding.select( idx );
        }
    }
}
