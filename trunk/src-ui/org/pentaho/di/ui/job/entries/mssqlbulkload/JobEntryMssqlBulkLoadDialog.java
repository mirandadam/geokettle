/*************************************************************************************** 
 * Copyright (C) 2007 Samatar  All rights reserved. 
 * This software was developed by Samatar and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. A copy of the license, 
 * is included with the binaries and source code. The Original Code is Samatar.  
 * The Initial Developer is Samatar.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an 
 * "AS IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. 
 * Please refer to the license for the specific language governing your rights 
 * and limitations.
 ***************************************************************************************/


package org.pentaho.di.ui.job.entries.mssqlbulkload;

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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entries.mssqlbulkload.JobEntryMssqlBulkLoad;
import org.pentaho.di.job.entries.mssqlbulkload.Messages;
import org.pentaho.di.job.entry.JobEntryDialogInterface;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.ui.core.database.dialog.DatabaseExplorerDialog;
import org.pentaho.di.ui.core.dialog.EnterSelectionDialog;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.job.dialog.JobDialog;
import org.pentaho.di.ui.job.entry.JobEntryDialog;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

/**
 * Dialog class for the MSSqlBulkLoader.
 * 
 * @author Samatar Hassan
 * @since  Jan-2007
 */
public class JobEntryMssqlBulkLoadDialog extends JobEntryDialog implements JobEntryDialogInterface
{

	private static final String[] FILETYPES = new String[] { Messages.getString("JobMssqlBulkLoad.Filetype.Text"),Messages.getString("JobMssqlBulkLoad.Filetype.Csv"), Messages.getString("JobMssqlBulkLoad.Filetype.All") };

	private Label wlName;
	private Text wName;
	private FormData fdlName, fdName;

	private CCombo wConnection;

	// Schema name
	private Label wlSchemaname;
	private TextVar wSchemaname;
	private FormData fdlSchemaname, fdSchemaname;

	private Label wlTablename;
	private TextVar wTablename;
	private FormData fdlTablename, fdTablename;

	private Button wOK, wCancel;
	private Listener lsOK, lsCancel;
	private JobEntryMssqlBulkLoad jobEntry;
	private Shell shell;
	private SelectionAdapter lsDef;
	private boolean changed;

	// File
	private Label wlFilename;
	private Button wbFilename;
	private TextVar wFilename;
	private FormData fdlFilename, fdbFilename, fdFilename;


	// Field Terminator
	private Label        wlFieldTerminator;
	private TextVar      wFieldTerminator;
	private FormData     fdlFieldTerminator,  fdFieldTerminator;

	//Line terminated
	private Label wlLineterminated;
	private TextVar wLineterminated;
	private FormData fdlLineterminated, fdLineterminated;


	//List Columns
	private Label wlOrderBy;
	private TextVar wOrderBy;
	private FormData fdlOrderBy, fdOrderBy;

	//start file at line
	private Label wlStartFile;
	private TextVar wStartFile;
	private FormData fdlStartFile, fdStartFile;
	
	//End file line
	private Label wlEndFile;
	private TextVar wEndFile;
	private FormData fdlEndFile, fdEndFile;
	
	//Specific Codepage
	private Label wlSpecificCodePage;
	private TextVar wSpecificCodePage;
	private FormData fdlSpecificCodePage, fdSpecificCodePage;
	
	
	//Maximum Errors allowed
	private Label wlMaxErrors;
	private TextVar wMaxErrors;
	private FormData fdlMaxErrors, fdMaxErrors;


	private Button wbTable;
	private Button wbOrderBy;
	

	//  Add File to result
	private Group wFileResult,wConnectionGroup,wDataFileGroup;
    private FormData fdFileResult,fdConnectionGroup,fdDataFileGroup;
    
    
	private Label        wlAddFileToResult;
	private Button       wAddFileToResult;
	private FormData     fdlAddFileToResult, fdAddFileToResult;
	
	//  Truncate table
	private Label        wlTruncate;
	private Button       wTruncate;
	private FormData     fdlTruncate, fdTruncate;
	
	// Fire Triggers
	private Label        wlFireTriggers;
	private Button       wFireTriggers;
	private FormData     fdlFireTriggers, fdFireTriggers;
	
	// Check Constaints
	private Label        wlCheckConstraints;
	private Button       wCheckConstraints;
	private FormData     fdlCheckConstraints, fdCheckConstraints;
	
	// Add Datetime
	private Label        wlAddDateTime;
	private Button       wAddDateTime;
	private FormData     fdlAddDateTime, fdAddDateTime;
	
	
	// Keep nulls
	private Label        wlKeepNulls;
	private Button       wKeepNulls;
	private FormData     fdlKeepNulls, fdKeepNulls;
	
	// Keep Identity?
	private Label        wlKeepIdentity;
	private Button       wKeepIdentity;
	private FormData     fdlKeepIdentity, fdKeepIdentity;
	
	// Tablock
	private Label        wlTablock;
	private Button       wTablock;
	private FormData     fdlTablock, fdTablock;
	
	
	// Data file type
	private Label wlDataFiletype;
	private  CCombo wDataFiletype;
	private FormData fdlDataFiletype, fdDataFiletype;
	
	// Format file

	private Label wlFormatFilename;
	private Button wbFormatFilename;
	private TextVar wFormatFilename;
	private FormData fdlFormatFilename, fdbFormatFilename, fdFormatFilename;
	
	// Order Direction
	private Label wlOrderDirection;
	private  CCombo wOrderDirection;
	private FormData fdlOrderDirection, fdOrderDirection;
	
	// CodePage
	private Label wlCodePage;
	private  CCombo wCodePage;
	private FormData fdlCodePage, fdCodePage;
	

	private Label        wlErrorFilename;
	private Button       wbErrorFilename;
	private TextVar      wErrorFilename;
	private FormData     fdlErrorFilename, fdbErrorFilename, fdErrorFilename;
	
	
	//Batch Size
	private Label wlBatchSize;
	private TextVar wBatchSize;
	private FormData fdlBatchSize, fdBatchSize;
	
	
	// Kilobytes per Batch
	private Label wlRowsPerBatch;
	private TextVar wRowsPerBatch;
	private FormData fdlRowsPerBatch, fdRowsPerBatch;
	
	private CTabFolder   wTabFolder;
	private Composite    wGeneralComp,wAdvancedComp;	
	private CTabItem     wGeneralTab,wAdvancedTab;
	private FormData	 fdGeneralComp,fdAdvancedComp;
	private FormData     fdTabFolder; 



    public JobEntryMssqlBulkLoadDialog(Shell parent, JobEntryInterface jobEntryInt, Repository rep, JobMeta jobMeta)
    {
        super(parent, jobEntryInt, rep, jobMeta);
        jobEntry = (JobEntryMssqlBulkLoad) jobEntryInt;
        if (this.jobEntry.getName() == null)
			this.jobEntry.setName(Messages.getString("JobMssqlBulkLoad.Name.Default"));
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
		shell.setText(Messages.getString("JobMssqlBulkLoad.Title"));

		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;
		
		wTabFolder = new CTabFolder(shell, SWT.BORDER);
 		props.setLook(wTabFolder, Props.WIDGET_STYLE_TAB);
 		
 		//////////////////////////
		// START OF GENERAL TAB   ///
		//////////////////////////
		wGeneralTab=new CTabItem(wTabFolder, SWT.NONE);
		wGeneralTab.setText(Messages.getString("JobMssqlBulkLoad.Tab.General.Label"));
		
		wGeneralComp = new Composite(wTabFolder, SWT.NONE);
 		props.setLook(wGeneralComp);

		FormLayout generalLayout = new FormLayout();
		generalLayout.marginWidth  = 3;
		generalLayout.marginHeight = 3;
		wGeneralComp.setLayout(generalLayout);
				

		// Filename line
		wlName = new Label(wGeneralComp, SWT.RIGHT);
		wlName.setText(Messages.getString("JobMssqlBulkLoad.Name.Label"));
		props.setLook(wlName);
		fdlName = new FormData();
		fdlName.left = new FormAttachment(0, 0);
		fdlName.right = new FormAttachment(middle, 0);
		fdlName.top = new FormAttachment(0, margin);
		wlName.setLayoutData(fdlName);
		wName = new Text(wGeneralComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wName);
		wName.addModifyListener(lsMod);
		fdName = new FormData();
		fdName.left = new FormAttachment(middle, 0);
		fdName.top = new FormAttachment(0, margin);
		fdName.right = new FormAttachment(100, 0);
		wName.setLayoutData(fdName);
		
	    // ///////////////////////////////
	    // START OF ConnectionGroup GROUP///
	    // /////////////////////////////////
	    wConnectionGroup = new Group(wGeneralComp, SWT.SHADOW_NONE);
	    props.setLook(wConnectionGroup);
	    wConnectionGroup.setText(Messages.getString("JobMssqlBulkLoad.ConnectionGroup.Group.Label"));

	    FormLayout ConnectionGroupLayout = new FormLayout();
	    ConnectionGroupLayout .marginWidth = 10;
	    ConnectionGroupLayout .marginHeight = 10;

	    wConnectionGroup.setLayout(ConnectionGroupLayout );
			
		// Connection line
		wConnection = addConnectionLine(wConnectionGroup, wName, middle, margin);
		if (jobEntry.getDatabase()==null && jobMeta.nrDatabases()==1) wConnection.select(0);
		wConnection.addModifyListener(lsMod);
		
		// Schema name line
		wlSchemaname = new Label(wConnectionGroup, SWT.RIGHT);
		wlSchemaname.setText(Messages.getString("JobMssqlBulkLoad.Schemaname.Label"));
		props.setLook(wlSchemaname);
		fdlSchemaname = new FormData();
		fdlSchemaname.left = new FormAttachment(0, 0);
		fdlSchemaname.right = new FormAttachment(middle, 0);
		fdlSchemaname.top = new FormAttachment(wConnection, margin);
		wlSchemaname.setLayoutData(fdlSchemaname);

		wSchemaname = new TextVar(jobMeta, wConnectionGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wSchemaname);
		wSchemaname.setToolTipText(Messages.getString("JobMssqlBulkLoad.Schemaname.Tooltip"));
		wSchemaname.addModifyListener(lsMod);
		fdSchemaname = new FormData();
		fdSchemaname.left = new FormAttachment(middle, 0);
		fdSchemaname.top = new FormAttachment(wConnection, margin);
		fdSchemaname.right = new FormAttachment(100, 0);
		wSchemaname.setLayoutData(fdSchemaname);

		// Table name line
		wlTablename = new Label(wConnectionGroup, SWT.RIGHT);
		wlTablename.setText(Messages.getString("JobMssqlBulkLoad.Tablename.Label"));
		props.setLook(wlTablename);
		fdlTablename = new FormData();
		fdlTablename.left = new FormAttachment(0, 0);
		fdlTablename.right = new FormAttachment(middle, 0);
		fdlTablename.top = new FormAttachment(wSchemaname, margin);
		wlTablename.setLayoutData(fdlTablename);

		wbTable=new Button(wConnectionGroup, SWT.PUSH| SWT.CENTER);
		props.setLook(wbTable);
		wbTable.setText(Messages.getString("System.Button.Browse"));
		FormData fdbTable = new FormData();
		fdbTable.right= new FormAttachment(100, 0);
		fdbTable.top  = new FormAttachment(wSchemaname, margin/2);
		wbTable.setLayoutData(fdbTable);
		wbTable.addSelectionListener( new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { getTableName(); } } );

		wTablename = new TextVar(jobMeta, wConnectionGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wTablename);
		wTablename.setToolTipText(Messages.getString("JobMssqlBulkLoad.Tablename.Tooltip"));
		wTablename.addModifyListener(lsMod);
		fdTablename = new FormData();
		fdTablename.left = new FormAttachment(middle, 0);
		fdTablename.top = new FormAttachment(wSchemaname, margin);
		fdTablename.right = new FormAttachment(wbTable, -margin);
		wTablename.setLayoutData(fdTablename);
		
		//Truncate table
		wlTruncate = new Label(wConnectionGroup, SWT.RIGHT);
		wlTruncate.setText(Messages.getString("JobMssqlBulkLoad.Truncate.Label"));
		props.setLook(wlTruncate);
		fdlTruncate = new FormData();
		fdlTruncate.left = new FormAttachment(0, 0);
		fdlTruncate.top = new FormAttachment(wTablename, margin);
		fdlTruncate.right = new FormAttachment(middle, -margin);
		wlTruncate.setLayoutData(fdlTruncate);
		wTruncate = new Button(wConnectionGroup, SWT.CHECK);
		props.setLook(wTruncate);
		wTruncate.setToolTipText(Messages.getString("JobMssqlBulkLoad.Truncate.Tooltip"));
		fdTruncate = new FormData();
		fdTruncate.left = new FormAttachment(middle, 0);
		fdTruncate.top = new FormAttachment(wTablename, margin);
		fdTruncate.right = new FormAttachment(100, 0);
		wTruncate.setLayoutData(fdTruncate);
		wTruncate.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				jobEntry.setChanged();
			}
		});
		
		
		
	     fdConnectionGroup = new FormData();
	     fdConnectionGroup .left = new FormAttachment(0, margin);
	     fdConnectionGroup .top = new FormAttachment(wName, margin);
	     fdConnectionGroup .right = new FormAttachment(100, -margin);
	     wConnectionGroup.setLayoutData(fdConnectionGroup );
	     // ///////////////////////////////////////////////////////////
	     // / END OF ConnectionGroup GROUP
	     // ///////////////////////////////////////////////////////////

	    // ////////////////////////
	    // START OF DataFileGroup GROUP///
	    // ///////////////////////////////
	    wDataFileGroup = new Group(wGeneralComp, SWT.SHADOW_NONE);
	    props.setLook(wDataFileGroup);
	    wDataFileGroup.setText(Messages.getString("JobMssqlBulkLoad.DataFileGroup.Group.Label"));

	    FormLayout DataFileGroupLayout = new FormLayout();
	    DataFileGroupLayout .marginWidth = 10;
	    DataFileGroupLayout .marginHeight = 10;
	    wDataFileGroup.setLayout(DataFileGroupLayout );

		// Filename line
		wlFilename = new Label(wDataFileGroup, SWT.RIGHT);
		wlFilename.setText(Messages.getString("JobMssqlBulkLoad.Filename.Label"));
		props.setLook(wlFilename);
		fdlFilename = new FormData();
		fdlFilename.left = new FormAttachment(0, 0);
		fdlFilename.top = new FormAttachment(wConnectionGroup, margin);
		fdlFilename.right = new FormAttachment(middle, -margin);
		wlFilename.setLayoutData(fdlFilename);

		wbFilename = new Button(wDataFileGroup, SWT.PUSH | SWT.CENTER);
		props.setLook(wbFilename);
		wbFilename.setText(Messages.getString("System.Button.Browse"));
		fdbFilename = new FormData();
		fdbFilename.right = new FormAttachment(100, 0);
		fdbFilename.top = new FormAttachment(wConnectionGroup, 0);
		wbFilename.setLayoutData(fdbFilename);

		wFilename = new TextVar(jobMeta, wDataFileGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wFilename);
		wFilename.setToolTipText(Messages.getString("JobMssqlBulkLoad.Filename.Tooltip"));
		wFilename.addModifyListener(lsMod);
		fdFilename = new FormData();
		fdFilename.left = new FormAttachment(middle, 0);
		fdFilename.top = new FormAttachment(wConnectionGroup, margin);
		fdFilename.right = new FormAttachment(wbFilename, -margin);
		wFilename.setLayoutData(fdFilename);

		// Whenever something changes, set the tooltip to the expanded version:
		wFilename.addModifyListener(new ModifyListener()
		{
			public void modifyText(ModifyEvent e)
			{
				wFilename.setToolTipText(jobMeta.environmentSubstitute(wFilename.getText()));
			}
		});

		wbFilename.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				FileDialog dialog = new FileDialog(shell, SWT.OPEN);
				dialog.setFilterExtensions(new String[] { "*.txt", "*.csv", "*" });
				if (wFilename.getText() != null)
				{
					dialog.setFileName(jobMeta.environmentSubstitute(wFilename.getText()));
				}
				dialog.setFilterNames(FILETYPES);
				if (dialog.open() != null)
				{
					wFilename.setText(dialog.getFilterPath() + Const.FILE_SEPARATOR
						+ dialog.getFileName());
				}
			}
		});


		// Data file type
		wlDataFiletype = new Label(wDataFileGroup, SWT.RIGHT);
		wlDataFiletype.setText(Messages.getString("JobMysqlBulkLoad.DataFiletype.Label"));
		props.setLook(wlDataFiletype);
		fdlDataFiletype = new FormData();
		fdlDataFiletype.left = new FormAttachment(0, 0);
		fdlDataFiletype.right = new FormAttachment(middle, 0);
		fdlDataFiletype.top = new FormAttachment(wFilename, margin);
		wlDataFiletype.setLayoutData(fdlDataFiletype);
		wDataFiletype = new CCombo(wDataFileGroup, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER);
					wDataFiletype.add("char");
					wDataFiletype.add("native");
					wDataFiletype.add("widechar");
					wDataFiletype.add("widenative");					
					wDataFiletype.select(0); // +1: starts at -1

		props.setLook(wDataFiletype);
		fdDataFiletype= new FormData();
		fdDataFiletype.left = new FormAttachment(middle, 0);
		fdDataFiletype.top = new FormAttachment(wFilename, margin);
		fdDataFiletype.right = new FormAttachment(100, 0);
		wDataFiletype.setLayoutData(fdDataFiletype);
		wDataFiletype.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				setDataType();
				
			}
		});

		
	     fdDataFileGroup = new FormData();
	     fdDataFileGroup .left = new FormAttachment(0, margin);
	     fdDataFileGroup .top = new FormAttachment(wConnectionGroup, margin);
	     fdDataFileGroup .right = new FormAttachment(100, -margin);
	     wDataFileGroup.setLayoutData(fdDataFileGroup );
	     // ///////////////////////////////////////////////////////////
	     // / END OF DataFileGroup GROUP
	     // ///////////////////////////////////////////////////////////


		// FieldTerminator
		wlFieldTerminator = new Label(wGeneralComp, SWT.RIGHT);
		wlFieldTerminator.setText(Messages.getString("JobMssqlBulkLoad.FieldTerminator.Label"));
		props.setLook(wlFieldTerminator);
		fdlFieldTerminator = new FormData();
		fdlFieldTerminator.left = new FormAttachment(0, 0);
		fdlFieldTerminator.right = new FormAttachment(middle, 0);
		fdlFieldTerminator.top = new FormAttachment(wDataFileGroup, 3*margin);
		wlFieldTerminator.setLayoutData(fdlFieldTerminator);

		wFieldTerminator = new TextVar(jobMeta, wGeneralComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wFieldTerminator);
		wFieldTerminator.setToolTipText(Messages.getString("JobMssqlBulkLoad.FieldTerminator.Tooltip"));
		wFieldTerminator.addModifyListener(lsMod);
		fdFieldTerminator = new FormData();
		fdFieldTerminator.left = new FormAttachment(middle, 0);
		fdFieldTerminator.top = new FormAttachment(wDataFileGroup, 3*margin);
		fdFieldTerminator.right = new FormAttachment(100, 0);
		wFieldTerminator.setLayoutData(fdFieldTerminator);

		// Line terminated
		wlLineterminated = new Label(wGeneralComp, SWT.RIGHT);
		wlLineterminated.setText(Messages.getString("JobMssqlBulkLoad.Lineterminated.Label"));
		props.setLook(wlLineterminated);
		fdlLineterminated = new FormData();
		fdlLineterminated.left = new FormAttachment(0, 0);
		fdlLineterminated.right = new FormAttachment(middle, 0);
		fdlLineterminated.top = new FormAttachment(wFieldTerminator, margin);
		wlLineterminated.setLayoutData(fdlLineterminated);

		wLineterminated = new TextVar(jobMeta, wGeneralComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wLineterminated);
		wLineterminated.setToolTipText(Messages.getString("JobMssqlBulkLoad.Lineterminated.Tooltip"));
		wLineterminated.addModifyListener(lsMod);
		fdLineterminated = new FormData();
		fdLineterminated.left = new FormAttachment(middle, 0);
		fdLineterminated.top = new FormAttachment(wFieldTerminator, margin);
		fdLineterminated.right = new FormAttachment(100, 0);
		wLineterminated.setLayoutData(fdLineterminated);
		


		fdGeneralComp=new FormData();
		fdGeneralComp.left  = new FormAttachment(0, 0);
		fdGeneralComp.top   = new FormAttachment(0, 0);
		fdGeneralComp.right = new FormAttachment(100, 0);
		fdGeneralComp.bottom= new FormAttachment(500, -margin);
		wGeneralComp.setLayoutData(fdGeneralComp);
		
		wGeneralComp.layout();
		wGeneralTab.setControl(wGeneralComp);
 		props.setLook(wGeneralComp);
 		
 		//////////////////////////
		// END OF GENERAL TAB   ///
		//////////////////////////
 		
 		//////////////////////////////////////
		// START OF Advanced TAB   ///
		/////////////////////////////////////

		wAdvancedTab=new CTabItem(wTabFolder, SWT.NONE);
		wAdvancedTab.setText(Messages.getString("JobMssqlBulkLoad.Tab.Advanced.Label"));

		FormLayout AdvancedLayout = new FormLayout ();
		AdvancedLayout.marginWidth  = 3;
		AdvancedLayout.marginHeight = 3;
		
		wAdvancedComp = new Composite(wTabFolder, SWT.NONE);
 		props.setLook(wAdvancedComp);
 		wAdvancedComp.setLayout(AdvancedLayout);
		
		// CodePage
		wlCodePage = new Label(wAdvancedComp, SWT.RIGHT);
		wlCodePage.setText(Messages.getString("JobMysqlBulkLoad.CodePage.Label"));
		props.setLook(wlCodePage);
		fdlCodePage = new FormData();
		fdlCodePage.left = new FormAttachment(0, 0);
		fdlCodePage.right = new FormAttachment(middle, 0);
		fdlCodePage.top = new FormAttachment(0, margin);
		wlCodePage.setLayoutData(fdlCodePage);
		wCodePage = new CCombo(wAdvancedComp, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER);
					wCodePage.add("ACP");
					wCodePage.add("OEM");
					wCodePage.add("RAW");
					wCodePage.add(Messages.getString("JobMssqlBulkLoad.CodePage.Specific"));
					wCodePage.select(0); // +1: starts at -1

		props.setLook(wCodePage);
		fdCodePage= new FormData();
		fdCodePage.left = new FormAttachment(middle, 0);
		fdCodePage.top = new FormAttachment(0, margin);
		fdCodePage.right = new FormAttachment(100, 0);
		wCodePage.setLayoutData(fdCodePage);
		wCodePage.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				setCodeType();
				
			}
		});

		// Specific CodePage
		wlSpecificCodePage = new Label(wAdvancedComp, SWT.RIGHT);
		wlSpecificCodePage.setText(Messages.getString("JobMssqlBulkLoad.SpecificCodePage.Label"));
		props.setLook(wlSpecificCodePage);
		fdlSpecificCodePage = new FormData();
		fdlSpecificCodePage.left = new FormAttachment(0, 0);
		fdlSpecificCodePage.right = new FormAttachment(middle, 0);
		fdlSpecificCodePage.top = new FormAttachment(wCodePage, margin);
		wlSpecificCodePage.setLayoutData(fdlSpecificCodePage);

		wSpecificCodePage = new TextVar(jobMeta, wAdvancedComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wSpecificCodePage);
		wSpecificCodePage.addModifyListener(lsMod);
		fdSpecificCodePage = new FormData();
		fdSpecificCodePage.left = new FormAttachment(middle, 0);
		fdSpecificCodePage.top = new FormAttachment(wCodePage, margin);
		fdSpecificCodePage.right = new FormAttachment(100, 0);
		wSpecificCodePage.setLayoutData(fdSpecificCodePage);
		
		
		

		// FormatFilename line
		wlFormatFilename = new Label(wAdvancedComp, SWT.RIGHT);
		wlFormatFilename.setText(Messages.getString("JobMssqlBulkLoad.FormatFilename.Label"));
		props.setLook(wlFormatFilename);
		fdlFormatFilename = new FormData();
		fdlFormatFilename.left = new FormAttachment(0, 0);
		fdlFormatFilename.top = new FormAttachment(wSpecificCodePage, margin);
		fdlFormatFilename.right = new FormAttachment(middle, -margin);
		wlFormatFilename.setLayoutData(fdlFormatFilename);

		wbFormatFilename = new Button(wAdvancedComp, SWT.PUSH | SWT.CENTER);
		props.setLook(wbFormatFilename);
		wbFormatFilename.setText(Messages.getString("System.Button.Browse"));
		fdbFormatFilename = new FormData();
		fdbFormatFilename.right = new FormAttachment(100, 0);
		fdbFormatFilename.top = new FormAttachment(wSpecificCodePage, 0);
		wbFormatFilename.setLayoutData(fdbFormatFilename);

		wFormatFilename = new TextVar(jobMeta, wAdvancedComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wFormatFilename);
		wFormatFilename.setToolTipText(Messages.getString("JobMssqlBulkLoad.FormatFilename.Tooltip"));
		wFormatFilename.addModifyListener(lsMod);
		fdFormatFilename = new FormData();
		fdFormatFilename.left = new FormAttachment(middle, 0);
		fdFormatFilename.top = new FormAttachment(wSpecificCodePage, margin);
		fdFormatFilename.right = new FormAttachment(wbFormatFilename, -margin);
		wFormatFilename.setLayoutData(fdFormatFilename);

		// Whenever something changes, set the tooltip to the expanded version:
		wFormatFilename.addModifyListener(new ModifyListener()
		{
			public void modifyText(ModifyEvent e)
			{
				wFormatFilename.setToolTipText(jobMeta.environmentSubstitute(wFormatFilename.getText()));
			}
		});

		wbFormatFilename.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				FileDialog dialog = new FileDialog(shell, SWT.OPEN);
				dialog.setFilterExtensions(new String[] { "*.txt", "*.csv", "*" });
				if (wFormatFilename.getText() != null)
				{
					dialog.setFileName(jobMeta.environmentSubstitute(wFormatFilename.getText()));
				}
				dialog.setFilterNames(FILETYPES);
				if (dialog.open() != null)
				{
					wFormatFilename.setText(dialog.getFilterPath() + Const.FILE_SEPARATOR
						+ dialog.getFileName());
				}
			}
		});

		
		
		
		//Fire Triggers?
		wlFireTriggers = new Label(wAdvancedComp, SWT.RIGHT);
		wlFireTriggers.setText(Messages.getString("JobMssqlBulkLoad.FireTriggers.Label"));
		props.setLook(wlFireTriggers);
		fdlFireTriggers = new FormData();
		fdlFireTriggers.left = new FormAttachment(0, 0);
		fdlFireTriggers.top = new FormAttachment(wFormatFilename, margin);
		fdlFireTriggers.right = new FormAttachment(middle, -margin);
		wlFireTriggers.setLayoutData(fdlFireTriggers);
		wFireTriggers = new Button(wAdvancedComp, SWT.CHECK);
		props.setLook(wFireTriggers);
		wFireTriggers.setToolTipText(Messages.getString("JobMssqlBulkLoad.FireTriggers.Tooltip"));
		fdFireTriggers = new FormData();
		fdFireTriggers.left = new FormAttachment(middle, 0);
		fdFireTriggers.top = new FormAttachment(wFormatFilename, margin);
		fdFireTriggers.right = new FormAttachment(100, 0);
		wFireTriggers.setLayoutData(fdFireTriggers);
		wFireTriggers.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				jobEntry.setChanged();
			}
		});
		

		// CHECK CONSTRAINTS
		wlCheckConstraints = new Label(wAdvancedComp, SWT.RIGHT);
		wlCheckConstraints.setText(Messages.getString("JobMssqlBulkLoad.CheckConstraints.Label"));
		props.setLook(wlCheckConstraints);
		fdlCheckConstraints = new FormData();
		fdlCheckConstraints.left = new FormAttachment(0, 0);
		fdlCheckConstraints.top = new FormAttachment(wFireTriggers, margin);
		fdlCheckConstraints.right = new FormAttachment(middle, -margin);
		wlCheckConstraints.setLayoutData(fdlCheckConstraints);
		wCheckConstraints = new Button(wAdvancedComp, SWT.CHECK);
		props.setLook(wCheckConstraints);
		wCheckConstraints.setToolTipText(Messages.getString("JobMssqlBulkLoad.CheckConstraints.Tooltip"));
		fdCheckConstraints = new FormData();
		fdCheckConstraints.left = new FormAttachment(middle, 0);
		fdCheckConstraints.top = new FormAttachment(wFireTriggers, margin);
		fdCheckConstraints.right = new FormAttachment(100, 0);
		wCheckConstraints.setLayoutData(fdCheckConstraints);
		wCheckConstraints.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				jobEntry.setChanged();
			}
		});
		
		// Keep Nulls
		wlKeepNulls = new Label(wAdvancedComp, SWT.RIGHT);
		wlKeepNulls.setText(Messages.getString("JobMssqlBulkLoad.KeepNulls.Label"));
		props.setLook(wlKeepNulls);
		fdlKeepNulls = new FormData();
		fdlKeepNulls.left = new FormAttachment(0, 0);
		fdlKeepNulls.top = new FormAttachment(wCheckConstraints, margin);
		fdlKeepNulls.right = new FormAttachment(middle, -margin);
		wlKeepNulls.setLayoutData(fdlKeepNulls);
		wKeepNulls = new Button(wAdvancedComp, SWT.CHECK);
		props.setLook(wKeepNulls);
		wKeepNulls.setToolTipText(Messages.getString("JobMssqlBulkLoad.KeepNulls.Tooltip"));
		fdKeepNulls = new FormData();
		fdKeepNulls.left = new FormAttachment(middle, 0);
		fdKeepNulls.top = new FormAttachment(wCheckConstraints, margin);
		fdKeepNulls.right = new FormAttachment(100, 0);
		wKeepNulls.setLayoutData(fdKeepNulls);
		wKeepNulls.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				jobEntry.setChanged();
			}
		});
		
		// Keep Identity
		wlKeepIdentity = new Label(wAdvancedComp, SWT.RIGHT);
		wlKeepIdentity.setText(Messages.getString("JobMssqlBulkLoad.KeepIdentity.Label"));
		props.setLook(wlKeepIdentity);
		fdlKeepIdentity = new FormData();
		fdlKeepIdentity.left = new FormAttachment(0, 0);
		fdlKeepIdentity.top = new FormAttachment(wKeepNulls, margin);
		fdlKeepIdentity.right = new FormAttachment(middle, -margin);
		wlKeepIdentity.setLayoutData(fdlKeepIdentity);
		wKeepIdentity = new Button(wAdvancedComp, SWT.CHECK);
		props.setLook(wKeepIdentity);
		wKeepIdentity.setToolTipText(Messages.getString("JobMssqlBulkLoad.KeepIdentity.Tooltip"));
		fdKeepIdentity = new FormData();
		fdKeepIdentity.left = new FormAttachment(middle, 0);
		fdKeepIdentity.top = new FormAttachment(wKeepNulls, margin);
		fdKeepIdentity.right = new FormAttachment(100, 0);
		wKeepIdentity.setLayoutData(fdKeepIdentity);
		wKeepIdentity.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				jobEntry.setChanged();
			}
		});
		
		// TABBLOCK
		wlTablock = new Label(wAdvancedComp, SWT.RIGHT);
		wlTablock.setText(Messages.getString("JobMssqlBulkLoad.Tablock.Label"));
		props.setLook(wlTablock);
		fdlTablock = new FormData();
		fdlTablock.left = new FormAttachment(0, 0);
		fdlTablock.top = new FormAttachment(wKeepIdentity, margin);
		fdlTablock.right = new FormAttachment(middle, -margin);
		wlTablock.setLayoutData(fdlTablock);
		wTablock = new Button(wAdvancedComp, SWT.CHECK);
		props.setLook(wTablock);
		wTablock.setToolTipText(Messages.getString("JobMssqlBulkLoad.Tablock.Tooltip"));
		fdTablock = new FormData();
		fdTablock.left = new FormAttachment(middle, 0);
		fdTablock.top = new FormAttachment(wKeepIdentity, margin);
		fdTablock.right = new FormAttachment(100, 0);
		wTablock.setLayoutData(fdTablock);
		wTablock.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				jobEntry.setChanged();
			}
		});
		
		
		// Start file
		wlStartFile = new Label(wAdvancedComp, SWT.RIGHT);
		wlStartFile.setText(Messages.getString("JobMssqlBulkLoad.StartFile.Label"));
		props.setLook(wlStartFile);
		fdlStartFile = new FormData();
		fdlStartFile.left = new FormAttachment(0, 0);
		fdlStartFile.right = new FormAttachment(middle, 0);
		fdlStartFile.top = new FormAttachment(wTablock, margin);
		wlStartFile.setLayoutData(fdlStartFile);

		wStartFile = new TextVar(jobMeta, wAdvancedComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wStartFile);
		wStartFile.setToolTipText(Messages.getString("JobMssqlBulkLoad.StartFile.Tooltip"));
		wStartFile.addModifyListener(lsMod);
		fdStartFile = new FormData();
		fdStartFile.left = new FormAttachment(middle, 0);
		fdStartFile.top = new FormAttachment(wTablock, margin);
		fdStartFile.right = new FormAttachment(100, 0);
		wStartFile.setLayoutData(fdStartFile);
		
		
		// End file
		wlEndFile = new Label(wAdvancedComp, SWT.RIGHT);
		wlEndFile.setText(Messages.getString("JobMssqlBulkLoad.EndFile.Label"));
		props.setLook(wlEndFile);
		fdlEndFile = new FormData();
		fdlEndFile.left = new FormAttachment(0, 0);
		fdlEndFile.right = new FormAttachment(middle, 0);
		fdlEndFile.top = new FormAttachment(wStartFile, margin);
		wlEndFile.setLayoutData(fdlEndFile);

		wEndFile = new TextVar(jobMeta, wAdvancedComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wEndFile);
		wEndFile.setToolTipText(Messages.getString("JobMssqlBulkLoad.EndFile.Tooltip"));
		wEndFile.addModifyListener(lsMod);
		fdEndFile = new FormData();
		fdEndFile.left = new FormAttachment(middle, 0);
		fdEndFile.top = new FormAttachment(wStartFile, margin);
		fdEndFile.right = new FormAttachment(100, 0);
		wEndFile.setLayoutData(fdEndFile);
		
		// Specifies how the data in the data file is sorted
		wlOrderBy = new Label(wAdvancedComp, SWT.RIGHT);
		wlOrderBy.setText(Messages.getString("JobMssqlBulkLoad.OrderBy.Label"));
		props.setLook(wlOrderBy);
		fdlOrderBy = new FormData();
		fdlOrderBy.left = new FormAttachment(0, 0);
		fdlOrderBy.right = new FormAttachment(middle, 0);
		fdlOrderBy.top = new FormAttachment(wEndFile, margin);
		wlOrderBy.setLayoutData(fdlOrderBy);
		
		wbOrderBy=new Button(wAdvancedComp, SWT.PUSH| SWT.CENTER);
		props.setLook(wbOrderBy);
		wbOrderBy.setText(Messages.getString("System.Button.Edit"));
		FormData fdbListattribut = new FormData();
		fdbListattribut.right= new FormAttachment(100, 0);
		fdbListattribut.top  = new FormAttachment(wEndFile, margin);
		wbOrderBy.setLayoutData(fdbListattribut);
		wbOrderBy.addSelectionListener( new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { getListColumns(); } } );

		wOrderBy = new TextVar(jobMeta, wAdvancedComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wOrderBy);
		wOrderBy.setToolTipText(Messages.getString("JobMssqlBulkLoad.OrderBy.Tooltip"));
		wOrderBy.addModifyListener(lsMod);
		fdOrderBy = new FormData();
		fdOrderBy.left = new FormAttachment(middle, 0);
		fdOrderBy.top = new FormAttachment(wEndFile, margin);
		fdOrderBy.right = new FormAttachment(wbOrderBy, -margin);
		wOrderBy.setLayoutData(fdOrderBy);
		
		
		// Order Direction
		wlOrderDirection = new Label(wAdvancedComp, SWT.RIGHT);
		wlOrderDirection.setText(Messages.getString("JobMysqlBulkLoad.OrderDirection.Label"));
		props.setLook(wlOrderDirection);
		fdlOrderDirection = new FormData();
		fdlOrderDirection.left = new FormAttachment(0, 0);
		fdlOrderDirection.right = new FormAttachment(middle, 0);
		fdlOrderDirection.top = new FormAttachment(wOrderBy, margin);
		wlOrderDirection.setLayoutData(fdlOrderDirection);
		wOrderDirection = new CCombo(wAdvancedComp, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER);
					wOrderDirection.add(Messages.getString("JobMysqlBulkLoad.OrderDirectionAsc.Label"));
					wOrderDirection.add(Messages.getString("JobMysqlBulkLoad.OrderDirectionDesc.Label"));
					wOrderDirection.select(0); // +1: starts at -1

		props.setLook(wOrderDirection);
		fdOrderDirection= new FormData();
		fdOrderDirection.left = new FormAttachment(middle, 0);
		fdOrderDirection.top = new FormAttachment(wOrderBy, margin);
		fdOrderDirection.right = new FormAttachment(100, 0);
		wOrderDirection.setLayoutData(fdOrderDirection);
		
		
		// ErrorFilename line
		wlErrorFilename=new Label(wAdvancedComp, SWT.RIGHT);
		wlErrorFilename.setText(Messages.getString("JobMysqlBulkLoad.ErrorFilename.Label"));
 		props.setLook(wlErrorFilename);
		fdlErrorFilename=new FormData();
		fdlErrorFilename.left = new FormAttachment(0, 0);
		fdlErrorFilename.top  = new FormAttachment(wOrderDirection, margin);
		fdlErrorFilename.right= new FormAttachment(middle, -margin);
		wlErrorFilename.setLayoutData(fdlErrorFilename);

		wbErrorFilename=new Button(wAdvancedComp, SWT.PUSH| SWT.CENTER);
 		props.setLook(wbErrorFilename);
		wbErrorFilename.setText(Messages.getString("System.Button.Browse"));
		fdbErrorFilename=new FormData();
		fdbErrorFilename.right= new FormAttachment(100, 0);
		fdbErrorFilename.top  = new FormAttachment(wOrderDirection, 0);
		wbErrorFilename.setLayoutData(fdbErrorFilename);

		wErrorFilename=new TextVar(jobMeta, wAdvancedComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wErrorFilename);
		wErrorFilename.addModifyListener(lsMod);
		wErrorFilename.setToolTipText(Messages.getString("JobMysqlBulkLoad.ErrorFilename.Tooltip"));
		fdErrorFilename=new FormData();
		fdErrorFilename.left = new FormAttachment(middle, 0);
		fdErrorFilename.top  = new FormAttachment(wOrderDirection, margin);
		fdErrorFilename.right= new FormAttachment(wbErrorFilename, -margin);
		wErrorFilename.setLayoutData(fdErrorFilename);

		// Whenever something changes, set the tooltip to the expanded version:
		wErrorFilename.addModifyListener(new ModifyListener()
			{
				public void modifyText(ModifyEvent e)
				{
					wErrorFilename.setToolTipText(jobMeta.environmentSubstitute( wErrorFilename.getText() ) );
				}
			}
		);

		wbErrorFilename.addSelectionListener
		(
			new SelectionAdapter()
			{
				public void widgetSelected(SelectionEvent e)
				{
					DirectoryDialog dialog = new DirectoryDialog(shell, SWT.OPEN);
					if (wErrorFilename.getText()!=null)
					{
						dialog.setFilterPath(jobMeta.environmentSubstitute(wErrorFilename.getText()) );
					}
				
					String dir=dialog.open();
					if(dir!=null)
					{
						wErrorFilename.setText(dir);
					}
				}
			}
		);

		// Add Date time
		wlAddDateTime = new Label(wAdvancedComp, SWT.RIGHT);
		wlAddDateTime.setText(Messages.getString("JobMssqlBulkLoad.AddDateTime.Label"));
		props.setLook(wlAddDateTime);
		fdlAddDateTime = new FormData();
		fdlAddDateTime.left = new FormAttachment(0, 0);
		fdlAddDateTime.top = new FormAttachment(wErrorFilename, margin);
		fdlAddDateTime.right = new FormAttachment(middle, -margin);
		wlAddDateTime.setLayoutData(fdlAddDateTime);
		wAddDateTime = new Button(wAdvancedComp, SWT.CHECK);
		props.setLook(wAddDateTime);
		wAddDateTime.setToolTipText(Messages.getString("JobMssqlBulkLoad.AddDateTime.Tooltip"));
		fdAddDateTime = new FormData();
		fdAddDateTime.left = new FormAttachment(middle, 0);
		fdAddDateTime.top = new FormAttachment(wErrorFilename, margin);
		fdAddDateTime.right = new FormAttachment(100, 0);
		wAddDateTime.setLayoutData(fdAddDateTime);
		wAddDateTime.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				jobEntry.setChanged();
			}
		});
		
		


		// Maximum errors allowed
		wlMaxErrors = new Label(wAdvancedComp, SWT.RIGHT);
		wlMaxErrors.setText(Messages.getString("JobMssqlBulkLoad.MaxErrors.Label"));
		props.setLook(wlMaxErrors);
		fdlMaxErrors = new FormData();
		fdlMaxErrors.left = new FormAttachment(0, 0);
		fdlMaxErrors.right = new FormAttachment(middle, 0);
		fdlMaxErrors.top = new FormAttachment(wAddDateTime, margin);
		wlMaxErrors.setLayoutData(fdlMaxErrors);

		wMaxErrors = new TextVar(jobMeta, wAdvancedComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wMaxErrors);
		wlMaxErrors.setToolTipText(Messages.getString("JobMssqlBulkLoad.MaxErrors.Tooltip"));
		wMaxErrors.addModifyListener(lsMod);
		fdMaxErrors = new FormData();
		fdMaxErrors.left = new FormAttachment(middle, 0);
		fdMaxErrors.top = new FormAttachment(wAddDateTime, margin);
		fdMaxErrors.right = new FormAttachment(100, 0);
		wMaxErrors.setLayoutData(fdMaxErrors);

		// Batch Size
		wlBatchSize = new Label(wAdvancedComp, SWT.RIGHT);
		wlBatchSize.setText(Messages.getString("JobMssqlBulkLoad.BatchSize.Label"));
		props.setLook(wlBatchSize);
		fdlBatchSize = new FormData();
		fdlBatchSize.left = new FormAttachment(0, 0);
		fdlBatchSize.right = new FormAttachment(middle, 0);
		fdlBatchSize.top = new FormAttachment(wMaxErrors, margin);
		wlBatchSize.setLayoutData(fdlBatchSize);

		wBatchSize = new TextVar(jobMeta, wAdvancedComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wBatchSize);
		wBatchSize.setToolTipText(Messages.getString("JobMssqlBulkLoad.BatchSize.Tooltip"));
		wBatchSize.addModifyListener(lsMod);
		fdBatchSize = new FormData();
		fdBatchSize.left = new FormAttachment(middle, 0);
		fdBatchSize.top = new FormAttachment(wMaxErrors, margin);
		fdBatchSize.right = new FormAttachment(100, 0);
		wBatchSize.setLayoutData(fdBatchSize);



		// Rows per Batch
		wlRowsPerBatch = new Label(wAdvancedComp, SWT.RIGHT);
		wlRowsPerBatch.setText(Messages.getString("JobMssqlBulkLoad.RowsPerBatch.Label"));
		props.setLook(wlRowsPerBatch);
		fdlRowsPerBatch = new FormData();
		fdlRowsPerBatch.left = new FormAttachment(0, 0);
		fdlRowsPerBatch.right = new FormAttachment(middle, 0);
		fdlRowsPerBatch.top = new FormAttachment(wBatchSize, margin);
		wlRowsPerBatch.setLayoutData(fdlRowsPerBatch);

		wRowsPerBatch = new TextVar(jobMeta, wAdvancedComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wRowsPerBatch);
		wRowsPerBatch.setToolTipText(Messages.getString("JobMssqlBulkLoad.RowsPerBatch.Label"));
		wRowsPerBatch.addModifyListener(lsMod);
		fdRowsPerBatch = new FormData();
		fdRowsPerBatch.left = new FormAttachment(middle, 0);
		fdRowsPerBatch.top = new FormAttachment(wBatchSize, margin);
		fdRowsPerBatch.right = new FormAttachment(100, 0);
		wRowsPerBatch.setLayoutData(fdRowsPerBatch);

		
		
		 // fileresult grouping?
	     // ////////////////////////
	     // START OF FILE RESULT GROUP///
	     // /
	    wFileResult = new Group(wAdvancedComp, SWT.SHADOW_NONE);
	    props.setLook(wFileResult);
	    wFileResult.setText(Messages.getString("JobMssqlBulkLoad.FileResult.Group.Label"));

	    FormLayout groupLayout = new FormLayout();
	    groupLayout.marginWidth = 10;
	    groupLayout.marginHeight = 10;

	    wFileResult.setLayout(groupLayout);
	      
	      
	  	//Add file to result
		wlAddFileToResult = new Label(wFileResult, SWT.RIGHT);
		wlAddFileToResult.setText(Messages.getString("JobMssqlBulkLoad.AddFileToResult.Label"));
		props.setLook(wlAddFileToResult);
		fdlAddFileToResult = new FormData();
		fdlAddFileToResult.left = new FormAttachment(0, 0);
		fdlAddFileToResult.top = new FormAttachment(wRowsPerBatch, margin);
		fdlAddFileToResult.right = new FormAttachment(middle, -margin);
		wlAddFileToResult.setLayoutData(fdlAddFileToResult);
		wAddFileToResult = new Button(wFileResult, SWT.CHECK);
		props.setLook(wAddFileToResult);
		wAddFileToResult.setToolTipText(Messages.getString("JobMssqlBulkLoad.AddFileToResult.Tooltip"));
		fdAddFileToResult = new FormData();
		fdAddFileToResult.left = new FormAttachment(middle, 0);
		fdAddFileToResult.top = new FormAttachment(wRowsPerBatch, margin);
		fdAddFileToResult.right = new FormAttachment(100, 0);
		wAddFileToResult.setLayoutData(fdAddFileToResult);
		wAddFileToResult.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				jobEntry.setChanged();
			}
		});
		
	      
	      
	     fdFileResult = new FormData();
	     fdFileResult.left = new FormAttachment(0, margin);
	     fdFileResult.top = new FormAttachment(wRowsPerBatch, margin);
	     fdFileResult.right = new FormAttachment(100, -margin);
	     wFileResult.setLayoutData(fdFileResult);
	     // ///////////////////////////////////////////////////////////
	     // / END OF FilesResult GROUP
	     // ///////////////////////////////////////////////////////////

	
		fdAdvancedComp=new FormData();
		fdAdvancedComp.left  = new FormAttachment(0, 0);
		fdAdvancedComp.top   = new FormAttachment(0, 0);
		fdAdvancedComp.right = new FormAttachment(100, 0);
		fdAdvancedComp.bottom= new FormAttachment(500, -margin);
		wAdvancedComp.setLayoutData(fdAdvancedComp);
		
		wAdvancedComp.layout();
		wAdvancedTab.setControl(wAdvancedComp);
 		props.setLook(wAdvancedComp);
 		
 		//////////////////////////
		// END OF Advanced TAB   ///
		//////////////////////////
 		
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
		
		wCancel.addListener(SWT.Selection, lsCancel);
		wOK.addListener    (SWT.Selection, lsOK    );
		
		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };

		wName.addSelectionListener(lsDef);
		wTablename.addSelectionListener(lsDef);
	
		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );
		
		
		getData();
		setDataType();
		setCodeType();
		BaseStepDialog.setSize(shell);

		shell.open();
		props.setDialogSize(shell, "JobMssqlBulkLoadDialogSize");
		wTabFolder.setSelection(0);
		while (!shell.isDisposed())
		{
			if (!display.readAndDispatch())
				display.sleep();
		}
		return jobEntry;
	}

	public void dispose()
	{
		WindowProperty winprop = new WindowProperty(shell);
		props.setScreen(winprop);
		shell.dispose();
	}

	private void setDataType()
	{
		if(wDataFiletype.getSelectionIndex()==0 || wDataFiletype.getSelectionIndex()==2)
		{
			wFieldTerminator.setEnabled(true);
			wlFieldTerminator.setEnabled(true);
		}
		else
		{
			wFieldTerminator.setEnabled(false);
			wlFieldTerminator.setEnabled(false);
		}
	}
	
	private void setCodeType()
	{
		if(wCodePage.getSelectionIndex()==3)
		{
			wSpecificCodePage.setEnabled(true);
			wlSpecificCodePage.setEnabled(true);
		}
		else
		{
			wSpecificCodePage.setEnabled(false);
			wlSpecificCodePage.setEnabled(false);
		}
	}
	
	
	/**
	 * Copy information from the meta-data input to the dialog fields.
	 */
	public void getData()
	{
		if (jobEntry.getName() != null)
			wName.setText(jobEntry.getName());
		if (jobEntry.getDatabase() != null)
			wConnection.setText(jobEntry.getDatabase().getName());
		if (jobEntry.getSchemaname() != null)
			wSchemaname.setText(jobEntry.getSchemaname());
		if (jobEntry.getTablename() != null)
			wTablename.setText(jobEntry.getTablename());
		if (jobEntry.getFilename() != null)
			wFilename.setText(jobEntry.getFilename());
		if (jobEntry.getDataFileType() != null)
			wDataFiletype.setText(jobEntry.getDataFileType());
		if (jobEntry.getFieldTerminator() != null)
			wFieldTerminator.setText(jobEntry.getFieldTerminator());
		if (jobEntry.getLineterminated() != null)
			wLineterminated.setText(jobEntry.getLineterminated());	
		if (jobEntry.getCodePage() != null)
		{
			wCodePage.setText(jobEntry.getCodePage());
		}else
			wCodePage.setText("RAW");
		if (jobEntry.getSpecificCodePage() != null)
			wSpecificCodePage.setText(jobEntry.getSpecificCodePage());
		if (jobEntry.getFormatFilename() != null)
			wFormatFilename.setText(jobEntry.getFormatFilename());

		wFireTriggers.setSelection(jobEntry.isFireTriggers());
		wCheckConstraints.setSelection(jobEntry.isCheckConstraints());
		wKeepNulls.setSelection(jobEntry.isKeepNulls());
		wKeepIdentity.setSelection(jobEntry.isKeepIdentity());
		
		wTablock.setSelection(jobEntry.isTablock());
	
		wStartFile.setText(""+jobEntry.getStartFile());
		wEndFile.setText(""+jobEntry.getEndFile());
		
		
		
		if (jobEntry.getOrderBy() != null)
			wOrderBy.setText(jobEntry.getOrderBy());
		if (jobEntry.getOrderDirection() != null)
		{
			if(jobEntry.getOrderDirection().equals("Asc"))
				wOrderDirection.select(0);
			else
				wOrderDirection.select(1);
		}else
			wOrderDirection.select(0);
		
		if (jobEntry.getErrorFilename() != null)
			wErrorFilename.setText(jobEntry.getErrorFilename());  
		
		wMaxErrors.setText(""+jobEntry.getMaxErrors());
		wBatchSize.setText(""+jobEntry.getBatchSize());
		wRowsPerBatch.setText(""+jobEntry.getRowsPerBatch());
		
		wAddDateTime.setSelection(jobEntry.isAddDatetime());
		
		wAddFileToResult.setSelection(jobEntry.isAddFileToResult());
		wTruncate.setSelection(jobEntry.isTruncate());
		
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
		jobEntry.setDatabase(jobMeta.findDatabase(wConnection.getText()));
		jobEntry.setSchemaname(wSchemaname.getText());
		jobEntry.setTablename(wTablename.getText());
		jobEntry.setFilename(wFilename.getText());
		jobEntry.setDataFileType(wDataFiletype.getText());
		jobEntry.setFieldTerminator(wFieldTerminator.getText());
		jobEntry.setLineterminated(wLineterminated.getText());
		jobEntry.setCodePage(wCodePage.getText());
		jobEntry.setSpecificCodePage(wSpecificCodePage.getText());
		jobEntry.setFormatFilename(wFormatFilename.getText());
		jobEntry.setFireTriggers(wFireTriggers.getSelection());
		jobEntry.setCheckConstraints(wCheckConstraints.getSelection());
		jobEntry.setKeepNulls(wKeepNulls.getSelection());
		jobEntry.setKeepIdentity(wKeepIdentity.getSelection());
		
		jobEntry.setTablock(wTablock.getSelection());
		
		jobEntry.setStartFile(Const.toInt(wStartFile.getText(),0));
		jobEntry.setEndFile(Const.toInt(wEndFile.getText(),0));
		jobEntry.setOrderBy(wOrderBy.getText());
		if(wOrderDirection.getSelectionIndex()==0)
			jobEntry.setOrderDirection("Asc");
		else
			jobEntry.setOrderDirection("Desc");
		
		jobEntry.setErrorFilename(wErrorFilename.getText());
		jobEntry.setMaxErrors(Const.toInt(wMaxErrors.getText(), 0));
		jobEntry.setBatchSize(Const.toInt(wBatchSize.getText(), 0));
		jobEntry.setRowsPerBatch(Const.toInt(wRowsPerBatch.getText(), 0));
		
		jobEntry.setAddDatetime(wAddDateTime.getSelection());
		
		jobEntry.setAddFileToResult(wAddFileToResult.getSelection());
		jobEntry.setTruncate(wTruncate.getSelection());
		
		dispose();
	}

	public String toString()
	{
		return this.getClass().getName();
	}
	
	private void getTableName()
	{
		// New class: SelectTableDialog
		int connr = wConnection.getSelectionIndex();
		if (connr>=0)
		{
			DatabaseMeta inf = jobMeta.getDatabase(connr);
                        
			DatabaseExplorerDialog std = new DatabaseExplorerDialog(shell, SWT.NONE, inf, jobMeta.getDatabases());
			std.setSelectedSchema(wSchemaname.getText());
			std.setSelectedTable(wTablename.getText());
			std.setSplitSchemaAndTable(true);
			if (std.open() != null)
			{
				wTablename.setText(Const.NVL(std.getTableName(), ""));
			}
		}
		else
		{
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
			mb.setMessage(Messages.getString("JobMssqlBulkLoad.ConnectionError2.DialogMessage"));
			mb.setText(Messages.getString("System.Dialog.Error.Title"));
			mb.open(); 
		}                    
	}

	/**
	 * Get a list of columns, comma separated, allow the user to select from it.
	 */
	private void getListColumns()
	{
		if (!Const.isEmpty(wTablename.getText()))
		{
			DatabaseMeta databaseMeta = jobMeta.findDatabase(wConnection.getText());
			if (databaseMeta!=null)
			{
				Database database = new Database(databaseMeta);
				database.shareVariablesWith(jobMeta);
				try
				{
					database.connect();
					String schemaTable = databaseMeta.getQuotedSchemaTableCombination(wSchemaname.getText(), wTablename.getText());
					RowMetaInterface row = database.getTableFields(schemaTable);
					String available[] = row.getFieldNames();
                    
					String source[] = wOrderBy.getText().split(",");
					for (int i=0;i<source.length;i++) source[i] = Const.trim(source[i]);
					int idxSource[] = Const.indexsOfStrings(source, available);
					EnterSelectionDialog dialog = new EnterSelectionDialog(shell, available, Messages.getString("JobMssqlBulkLoad.SelectColumns.Title"), Messages.getString("JobMssqlBulkLoad.SelectColumns.Message"));
					dialog.setMulti(true);
					dialog.setSelectedNrs(idxSource);
					if (dialog.open()!=null)
					{
						String columns="";
						int idx[] = dialog.getSelectionIndeces();
						for (int i=0;i<idx.length;i++)
						{
							if (i>0) columns+=", ";
							columns+=available[idx[i]];
						}
						wOrderBy.setText(columns);
					}
				}
				catch(KettleDatabaseException e)
				{
					new ErrorDialog(shell, Messages.getString("System.Dialog.Error.Title"), Messages.getString("JobMssqlBulkLoad.ConnectionError2.DialogMessage"), e);
				}
				finally
				{
					database.disconnect();
				}
			}
		}
	}	
}