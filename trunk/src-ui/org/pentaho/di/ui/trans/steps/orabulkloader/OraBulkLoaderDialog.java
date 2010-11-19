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

package org.pentaho.di.ui.trans.steps.orabulkloader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.SQLStatement;
import org.pentaho.di.core.SourceToTargetMapping;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.ui.trans.step.TableItemInsertListener;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.orabulkloader.Messages;
import org.pentaho.di.trans.steps.orabulkloader.OraBulkLoaderMeta;
import org.pentaho.di.ui.core.database.dialog.DatabaseExplorerDialog;
import org.pentaho.di.ui.core.database.dialog.SQLEditor;
import org.pentaho.di.ui.core.dialog.EnterMappingDialog;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.core.widget.TextVar;


/**
 * Dialog class for the Oracle bulk loader step. 
 * Created on 21feb2007.
 * 
 * @author Sven Boden
 */
public class OraBulkLoaderDialog extends BaseStepDialog implements StepDialogInterface
{
	private CCombo				wConnection;

    private Label               wlSchema;
    private TextVar             wSchema;
    private FormData            fdlSchema, fdSchema;

	private Label				wlTable;
	private Button				wbTable;
	private TextVar				wTable;
	private FormData			fdlTable, fdbTable, fdTable;

	private Label				wlSqlldr;
	private Button				wbSqlldr;
	private TextVar				wSqlldr;
	private FormData			fdlSqlldr, fdbSqlldr, fdSqlldr;
	
	private Label               wlLoadMethod;
	private CCombo              wLoadMethod;
	private FormData            fdlLoadMethod, fdLoadMethod;	

	private Label               wlLoadAction;
	private CCombo              wLoadAction;
	private FormData            fdlLoadAction, fdLoadAction;		

	private Label				wlMaxErrors;
	private TextVar				wMaxErrors;
	private FormData			fdlMaxErrors, fdMaxErrors;		
	
	private Label				wlCommit;
	private TextVar				wCommit;
	private FormData			fdlCommit, fdCommit;		

	private Label				wlBindSize;
	private TextVar				wBindSize;
	private FormData			fdlBindSize, fdBindSize;		

	private Label				wlReadSize;
	private TextVar				wReadSize;
	private FormData			fdlReadSize, fdReadSize;			
	
	private Label				wlReturn;
	private TableView			wReturn;
	private FormData			fdlReturn, fdReturn;

	private Label				wlControlFile;
	private Button				wbControlFile;
	private TextVar				wControlFile;
	private FormData			fdlControlFile, fdbControlFile, fdControlFile;

	private Label				wlDataFile;
	private Button				wbDataFile;
	private TextVar				wDataFile;
	private FormData			fdlDataFile, fdbDataFile, fdDataFile;	

	private Label				wlLogFile;
	private Button				wbLogFile;
	private TextVar				wLogFile;
	private FormData			fdlLogFile, fdbLogFile, fdLogFile;	

	private Label				wlBadFile;
	private Button				wbBadFile;
	private TextVar				wBadFile;
	private FormData			fdlBadFile, fdbBadFile, fdBadFile;		

	private Label				wlDiscardFile;
	private Button				wbDiscardFile;
	private TextVar				wDiscardFile;
	private FormData			fdlDiscardFile, fdbDiscardFile, fdDiscardFile;		

	private Label				wlDbNameOverride;
	private TextVar				wDbNameOverride;
	private FormData			fdlDbNameOverride, fdDbNameOverride;		
		
    private Label               wlEncoding;
    private Combo               wEncoding;
    private FormData            fdlEncoding, fdEncoding;
    
    private Label               wlCharacterSetName;
    private Combo               wCharacterSetName;
    private FormData            fdlCharacterSetName, fdCharacterSetName;
	
	private Label				wlDirectPath;
	private Button				wDirectPath;
	private FormData			fdlDirectPath, fdDirectPath;	

	private Label				wlEraseFiles;
	private Button				wEraseFiles;
	private FormData			fdlEraseFiles, fdEraseFiles;		
	
	private Label				wlFailOnWarning;
	private Button				wFailOnWarning;
	private FormData			fdlFailOnWarning, fdFailOnWarning;
	
	private Label				wlFailOnError;
	private Button				wFailOnError;
	private FormData			fdlFailOnError, fdFailOnError;
	
	private Label				wlAltRecordTerm;
	private TextVar				wAltRecordTerm;
	private FormData			fdlAltRecordTerm, fdAltRecordTerm;
	
	private Button				wGetLU;
	private FormData			fdGetLU;
	private Listener			lsGetLU;
	
	private Button     wDoMapping;
	private FormData   fdDoMapping;

	private OraBulkLoaderMeta	input;
	
    private Map<String, Integer> inputFields;
    
	private ColumnInfo[] ciReturn ;
	
	/**
	 * List of ColumnInfo that should have the field names of the selected database table
	 */
	private List<ColumnInfo> tableFieldColumns = new ArrayList<ColumnInfo>();
	
    // These should not be translated, they are required to exist on all
    // platforms according to the documentation of "Charset".
    private static String[] encodings = { "",                //$NON-NLS-1$
    	                                  "US-ASCII",        //$NON-NLS-1$
    	                                  "ISO-8859-1",      //$NON-NLS-1$
    	                                  "UTF-8",           //$NON-NLS-1$
    	                                  "UTF-16BE",        //$NON-NLS-1$
    	                                  "UTF-16LE",        //$NON-NLS-1$
    	                                  "UTF-16" };        //$NON-NLS-1$
    
    private static String[] characterSetNames = { "",              //$NON-NLS-1$
        					                      "US7ASCII",      //$NON-NLS-1$
        					                      "WE8ISO8859P1",  //$NON-NLS-1$
        					                      "UTF8", };       //$NON-NLS-1$

    private static final String[] ALL_FILETYPES = new String[] {
        	Messages.getString("OraBulkLoaderDialog.Filetype.All") };


	public OraBulkLoaderDialog(Shell parent, Object in, TransMeta transMeta, String sname)
	{
		super(parent, (BaseStepMeta)in, transMeta, sname);
		input = (OraBulkLoaderMeta) in;
        inputFields =new HashMap<String, Integer>();
	}

	public String open()
	{
		Shell parent = getParent();
		Display display = parent.getDisplay();

		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN);
 		props.setLook(shell);
        setShellImage(shell, input);

		ModifyListener lsMod = new ModifyListener()
		{
			public void modifyText(ModifyEvent e)
			{
				input.setChanged();
			}
		};
		FocusListener lsFocusLost = new FocusAdapter() {
			public void focusLost(FocusEvent arg0) {
				setTableFieldCombo();
			}
		};
		changed = input.hasChanged();

		FormLayout formLayout = new FormLayout();
		formLayout.marginWidth = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText(Messages.getString("OraBulkLoaderDialog.Shell.Title")); //$NON-NLS-1$

		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Stepname line
		wlStepname = new Label(shell, SWT.RIGHT);
		wlStepname.setText(Messages.getString("OraBulkLoaderDialog.Stepname.Label")); //$NON-NLS-1$
 		props.setLook(wlStepname);
		fdlStepname = new FormData();
		fdlStepname.left = new FormAttachment(0, 0);
		fdlStepname.right = new FormAttachment(middle, -margin);
		fdlStepname.top = new FormAttachment(0, margin);
		wlStepname.setLayoutData(fdlStepname);
		wStepname = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wStepname.setText(stepname);
 		props.setLook(wStepname);
		wStepname.addModifyListener(lsMod);
		fdStepname = new FormData();
		fdStepname.left = new FormAttachment(middle, 0);
		fdStepname.top = new FormAttachment(0, margin);
		fdStepname.right = new FormAttachment(100, 0);
		wStepname.setLayoutData(fdStepname);

		// Connection line
		wConnection = addConnectionLine(shell, wStepname, middle, margin);
		if (input.getDatabaseMeta()==null && transMeta.nrDatabases()==1) wConnection.select(0);
		wConnection.addModifyListener(lsMod);

        // Schema line...
        wlSchema=new Label(shell, SWT.RIGHT);
        wlSchema.setText(Messages.getString("OraBulkLoaderDialog.TargetSchema.Label")); //$NON-NLS-1$
        props.setLook(wlSchema);
        fdlSchema=new FormData();
        fdlSchema.left = new FormAttachment(0, 0);
        fdlSchema.right= new FormAttachment(middle, -margin);
        fdlSchema.top  = new FormAttachment(wConnection, margin*2);
        wlSchema.setLayoutData(fdlSchema);

        wSchema=new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wSchema);
        wSchema.addModifyListener(lsMod);
    	wSchema.addFocusListener(lsFocusLost);
        fdSchema=new FormData();
        fdSchema.left = new FormAttachment(middle, 0);
        fdSchema.top  = new FormAttachment(wConnection, margin*2);
        fdSchema.right= new FormAttachment(100, 0);
        wSchema.setLayoutData(fdSchema);

		// Table line...
		wlTable = new Label(shell, SWT.RIGHT);
		wlTable.setText(Messages.getString("OraBulkLoaderDialog.TargetTable.Label")); //$NON-NLS-1$
 		props.setLook(wlTable);
		fdlTable = new FormData();
		fdlTable.left = new FormAttachment(0, 0);
		fdlTable.right = new FormAttachment(middle, -margin);
		fdlTable.top = new FormAttachment(wSchema, margin);
		wlTable.setLayoutData(fdlTable);
		
		wbTable = new Button(shell, SWT.PUSH | SWT.CENTER);
 		props.setLook(wbTable);
		wbTable.setText(Messages.getString("OraBulkLoaderDialog.Browse.Button")); //$NON-NLS-1$
		fdbTable = new FormData();
		fdbTable.right = new FormAttachment(100, 0);
		fdbTable.top = new FormAttachment(wSchema, margin);
		wbTable.setLayoutData(fdbTable);
		wTable = new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wTable);
		wTable.addModifyListener(lsMod);
		wTable.addFocusListener(lsFocusLost);
		fdTable = new FormData();
		fdTable.left = new FormAttachment(middle, 0);
		fdTable.top = new FormAttachment(wSchema, margin);
		fdTable.right = new FormAttachment(wbTable, -margin);
		wTable.setLayoutData(fdTable);

		// Sqlldr line...
		wlSqlldr = new Label(shell, SWT.RIGHT);
		wlSqlldr.setText(Messages.getString("OraBulkLoaderDialog.Sqlldr.Label")); //$NON-NLS-1$
 		props.setLook(wlSqlldr);
		fdlSqlldr = new FormData();
		fdlSqlldr.left = new FormAttachment(0, 0);
		fdlSqlldr.right = new FormAttachment(middle, -margin);
		fdlSqlldr.top = new FormAttachment(wTable, margin);
		wlSqlldr.setLayoutData(fdlSqlldr);
		
		wbSqlldr = new Button(shell, SWT.PUSH | SWT.CENTER);
 		props.setLook(wbSqlldr);
		wbSqlldr.setText(Messages.getString("OraBulkLoaderDialog.Browse.Button")); //$NON-NLS-1$
		fdbSqlldr = new FormData();
		fdbSqlldr.right = new FormAttachment(100, 0);
		fdbSqlldr.top = new FormAttachment(wTable, margin);
		wbSqlldr.setLayoutData(fdbSqlldr);
		wSqlldr = new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wSqlldr);
		wSqlldr.addModifyListener(lsMod);
		fdSqlldr = new FormData();
		fdSqlldr.left = new FormAttachment(middle, 0);
		fdSqlldr.top = new FormAttachment(wTable, margin);
		fdSqlldr.right = new FormAttachment(wbSqlldr, -margin);
		wSqlldr.setLayoutData(fdSqlldr);
				
		// Load Method line
		wlLoadMethod = new Label(shell, SWT.RIGHT);
		wlLoadMethod.setText(Messages.getString("OraBulkLoaderDialog.LoadMethod.Label"));
		props.setLook(wlLoadMethod);
		fdlLoadMethod = new FormData();
		fdlLoadMethod.left = new FormAttachment(0, 0);
		fdlLoadMethod.right = new FormAttachment(middle, -margin);
		fdlLoadMethod.top = new FormAttachment(wSqlldr, margin);
		wlLoadMethod.setLayoutData(fdlLoadMethod);
		wLoadMethod = new CCombo(shell, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER);
		wLoadMethod.add(Messages.getString("OraBulkLoaderDialog.AutoEndLoadMethod.Label"));
		wLoadMethod.add(Messages.getString("OraBulkLoaderDialog.ManualLoadMethod.Label"));
		wLoadMethod.add(Messages.getString("OraBulkLoaderDialog.AutoConcLoadMethod.Label"));
		wLoadMethod.select(0); // +1: starts at -1
		wLoadMethod.addModifyListener(lsMod);
		
		props.setLook(wLoadMethod);
		fdLoadMethod= new FormData();
		fdLoadMethod.left = new FormAttachment(middle, 0);
		fdLoadMethod.top = new FormAttachment(wSqlldr, margin);
		fdLoadMethod.right = new FormAttachment(100, 0);
		wLoadMethod.setLayoutData(fdLoadMethod);
		
		fdLoadMethod = new FormData();
		fdLoadMethod.left = new FormAttachment(middle, 0);
		fdLoadMethod.top = new FormAttachment(wSqlldr, margin);
		fdLoadMethod.right = new FormAttachment(100, 0);
		wLoadMethod.setLayoutData(fdLoadMethod);					
		
		// Load Action line
		wlLoadAction = new Label(shell, SWT.RIGHT);
		wlLoadAction.setText(Messages.getString("OraBulkLoaderDialog.LoadAction.Label"));
		props.setLook(wlLoadAction);
		fdlLoadAction = new FormData();
		fdlLoadAction.left = new FormAttachment(0, 0);
		fdlLoadAction.right = new FormAttachment(middle, -margin);
		fdlLoadAction.top = new FormAttachment(wLoadMethod, margin);
		wlLoadAction.setLayoutData(fdlLoadAction);
		wLoadAction = new CCombo(shell, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER);
		wLoadAction.add(Messages.getString("OraBulkLoaderDialog.AppendLoadAction.Label"));
		wLoadAction.add(Messages.getString("OraBulkLoaderDialog.InsertLoadAction.Label"));
		wLoadAction.add(Messages.getString("OraBulkLoaderDialog.ReplaceLoadAction.Label"));
		wLoadAction.add(Messages.getString("OraBulkLoaderDialog.TruncateLoadAction.Label"));
		
		wLoadAction.select(0); // +1: starts at -1
		wLoadAction.addModifyListener(lsMod);
		
		props.setLook(wLoadAction);
		fdLoadAction= new FormData();
		fdLoadAction.left = new FormAttachment(middle, 0);
		fdLoadAction.top = new FormAttachment(wLoadMethod, margin);
		fdLoadAction.right = new FormAttachment(100, 0);
		wLoadAction.setLayoutData(fdLoadAction);
		
		fdLoadAction = new FormData();
		fdLoadAction.left = new FormAttachment(middle, 0);
		fdLoadAction.top = new FormAttachment(wLoadMethod, margin);
		fdLoadAction.right = new FormAttachment(100, 0);
		wLoadAction.setLayoutData(fdLoadAction);				

		// MaxErrors file line
		wlMaxErrors = new Label(shell, SWT.RIGHT);
		wlMaxErrors.setText(Messages.getString("OraBulkLoaderDialog.MaxErrors.Label")); //$NON-NLS-1$
 		props.setLook(wlMaxErrors);
		fdlMaxErrors = new FormData();
		fdlMaxErrors.left = new FormAttachment(0, 0);
		fdlMaxErrors.top = new FormAttachment(wLoadAction, margin);
		fdlMaxErrors.right = new FormAttachment(middle, -margin);
		wlMaxErrors.setLayoutData(fdlMaxErrors);
		wMaxErrors = new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wMaxErrors);
		wMaxErrors.addModifyListener(lsMod);
		fdMaxErrors = new FormData();
		fdMaxErrors.left = new FormAttachment(middle, 0);
		fdMaxErrors.top = new FormAttachment(wLoadAction, margin);
		fdMaxErrors.right = new FormAttachment(100, 0);
		wMaxErrors.setLayoutData(fdMaxErrors);						
		
		// Commmit/batch file line
		wlCommit = new Label(shell, SWT.RIGHT);
		wlCommit.setText(Messages.getString("OraBulkLoaderDialog.Commit.Label")); //$NON-NLS-1$
 		props.setLook(wlCommit);
		fdlCommit = new FormData();
		fdlCommit.left = new FormAttachment(0, 0);
		fdlCommit.top = new FormAttachment(wMaxErrors, margin);
		fdlCommit.right = new FormAttachment(middle, -margin);
		wlCommit.setLayoutData(fdlCommit);
		wCommit = new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wCommit);
		wCommit.addModifyListener(lsMod);
		fdCommit = new FormData();
		fdCommit.left = new FormAttachment(middle, 0);
		fdCommit.top = new FormAttachment(wMaxErrors, margin);
		fdCommit.right = new FormAttachment(100, 0);
		wCommit.setLayoutData(fdCommit);				
		
		// Bindsize line
		wlBindSize = new Label(shell, SWT.RIGHT);
		wlBindSize.setText(Messages.getString("OraBulkLoaderDialog.BindSize.Label")); //$NON-NLS-1$
 		props.setLook(wlBindSize);
		fdlBindSize = new FormData();
		fdlBindSize.left = new FormAttachment(0, 0);
		fdlBindSize.top = new FormAttachment(wCommit, margin);
		fdlBindSize.right = new FormAttachment(middle, -margin);
		wlBindSize.setLayoutData(fdlBindSize);
		wBindSize = new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wBindSize);
		wBindSize.addModifyListener(lsMod);
		fdBindSize = new FormData();
		fdBindSize.left = new FormAttachment(middle, 0);
		fdBindSize.top = new FormAttachment(wCommit, margin);
		fdBindSize.right = new FormAttachment(100, 0);
		wBindSize.setLayoutData(fdBindSize);		

		// Readsize line
		wlReadSize = new Label(shell, SWT.RIGHT);
		wlReadSize.setText(Messages.getString("OraBulkLoaderDialog.ReadSize.Label")); //$NON-NLS-1$
 		props.setLook(wlReadSize);
		fdlReadSize = new FormData();
		fdlReadSize.left = new FormAttachment(0, 0);
		fdlReadSize.top = new FormAttachment(wBindSize, margin);
		fdlReadSize.right = new FormAttachment(middle, -margin);
		wlReadSize.setLayoutData(fdlReadSize);
		wReadSize = new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wReadSize);
		wReadSize.addModifyListener(lsMod);
		fdReadSize = new FormData();
		fdReadSize.left = new FormAttachment(middle, 0);
		fdReadSize.top = new FormAttachment(wBindSize, margin);
		fdReadSize.right = new FormAttachment(100, 0);
		wReadSize.setLayoutData(fdReadSize);		

		// Db Name Override line
		wlDbNameOverride = new Label(shell, SWT.RIGHT);
		wlDbNameOverride.setText(Messages.getString("OraBulkLoaderDialog.DbNameOverride.Label")); //$NON-NLS-1$
 		props.setLook(wlDbNameOverride);
		fdlDbNameOverride = new FormData();
		fdlDbNameOverride.left = new FormAttachment(0, 0);
		fdlDbNameOverride.top = new FormAttachment(wReadSize, margin);
		fdlDbNameOverride.right = new FormAttachment(middle, -margin);
		wlDbNameOverride.setLayoutData(fdlDbNameOverride);
		wDbNameOverride = new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wDbNameOverride);
		wDbNameOverride.addModifyListener(lsMod);
		fdDbNameOverride = new FormData();
		fdDbNameOverride.left = new FormAttachment(middle, 0);
		fdDbNameOverride.top = new FormAttachment(wReadSize, margin);
		fdDbNameOverride.right = new FormAttachment(100, 0);
		wDbNameOverride.setLayoutData(fdDbNameOverride);				
		
		// Control file line
		wlControlFile = new Label(shell, SWT.RIGHT);
		wlControlFile.setText(Messages.getString("OraBulkLoaderDialog.ControlFile.Label")); //$NON-NLS-1$
 		props.setLook(wlControlFile);
		fdlControlFile = new FormData();
		fdlControlFile.left = new FormAttachment(0, 0);
		fdlControlFile.top = new FormAttachment(wDbNameOverride, margin);
		fdlControlFile.right = new FormAttachment(middle, -margin);
		wlControlFile.setLayoutData(fdlControlFile);		
		wbControlFile = new Button(shell, SWT.PUSH | SWT.CENTER);
 		props.setLook(wbControlFile);
		wbControlFile.setText(Messages.getString("OraBulkLoaderDialog.Browse.Button")); //$NON-NLS-1$
		fdbControlFile = new FormData();
		fdbControlFile.right = new FormAttachment(100, 0);
		fdbControlFile.top = new FormAttachment(wDbNameOverride, margin);
		wbControlFile.setLayoutData(fdbControlFile);
		wControlFile = new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);				
 		props.setLook(wControlFile);
		wControlFile.addModifyListener(lsMod);
		fdControlFile = new FormData();
		fdControlFile.left = new FormAttachment(middle, 0);
		fdControlFile.top = new FormAttachment(wDbNameOverride, margin);
		fdControlFile.right = new FormAttachment(wbControlFile, -margin);
		wControlFile.setLayoutData(fdControlFile);		

		// Data file line
		wlDataFile = new Label(shell, SWT.RIGHT);
		wlDataFile.setText(Messages.getString("OraBulkLoaderDialog.DataFile.Label")); //$NON-NLS-1$
 		props.setLook(wlDataFile);
		fdlDataFile = new FormData();
		fdlDataFile.left = new FormAttachment(0, 0);
		fdlDataFile.top = new FormAttachment(wControlFile, margin);
		fdlDataFile.right = new FormAttachment(middle, -margin);
		wlDataFile.setLayoutData(fdlDataFile);
		wbDataFile = new Button(shell, SWT.PUSH | SWT.CENTER);
 		props.setLook(wbDataFile);
		wbDataFile.setText(Messages.getString("OraBulkLoaderDialog.Browse.Button")); //$NON-NLS-1$
		fdbDataFile = new FormData();
		fdbDataFile.right = new FormAttachment(100, 0);
		fdbDataFile.top = new FormAttachment(wControlFile, margin);
		wbDataFile.setLayoutData(fdbDataFile);	
		wDataFile = new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wDataFile);
		wDataFile.addModifyListener(lsMod);
		fdDataFile = new FormData();
		fdDataFile.left = new FormAttachment(middle, 0);
		fdDataFile.top = new FormAttachment(wControlFile, margin);
		fdDataFile.right = new FormAttachment(wbDataFile, -margin);
		wDataFile.setLayoutData(fdDataFile);
		
		// Log file line
		wlLogFile = new Label(shell, SWT.RIGHT);
		wlLogFile.setText(Messages.getString("OraBulkLoaderDialog.LogFile.Label")); //$NON-NLS-1$
 		props.setLook(wlLogFile);
		fdlLogFile = new FormData();
		fdlLogFile.left = new FormAttachment(0, 0);
		fdlLogFile.top = new FormAttachment(wDataFile, margin);
		fdlLogFile.right = new FormAttachment(middle, -margin);
		wlLogFile.setLayoutData(fdlLogFile);
		wbLogFile = new Button(shell, SWT.PUSH | SWT.CENTER);
 		props.setLook(wbLogFile);
		wbLogFile.setText(Messages.getString("OraBulkLoaderDialog.Browse.Button")); //$NON-NLS-1$
		fdbLogFile = new FormData();
		fdbLogFile.right = new FormAttachment(100, 0);
		fdbLogFile.top = new FormAttachment(wDataFile, margin);
		wbLogFile.setLayoutData(fdbLogFile);
		wLogFile = new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wLogFile);
		wLogFile.addModifyListener(lsMod);
		fdLogFile = new FormData();
		fdLogFile.left = new FormAttachment(middle, 0);
		fdLogFile.top = new FormAttachment(wDataFile, margin);
		fdLogFile.right = new FormAttachment(wbLogFile, -margin);
		wLogFile.setLayoutData(fdLogFile);		

		// Bad file line
		wlBadFile = new Label(shell, SWT.RIGHT);
		wlBadFile.setText(Messages.getString("OraBulkLoaderDialog.BadFile.Label")); //$NON-NLS-1$
 		props.setLook(wlBadFile);
		fdlBadFile = new FormData();
		fdlBadFile.left = new FormAttachment(0, 0);
		fdlBadFile.top = new FormAttachment(wLogFile, margin);
		fdlBadFile.right = new FormAttachment(middle, -margin);
		wlBadFile.setLayoutData(fdlBadFile);
		wbBadFile = new Button(shell, SWT.PUSH | SWT.CENTER);
 		props.setLook(wbBadFile);
		wbBadFile.setText(Messages.getString("OraBulkLoaderDialog.Browse.Button")); //$NON-NLS-1$
		fdbBadFile = new FormData();
		fdbBadFile.right = new FormAttachment(100, 0);
		fdbBadFile.top = new FormAttachment(wLogFile, margin);
		wbBadFile.setLayoutData(fdbBadFile);		
		wBadFile = new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wBadFile);
		wBadFile.addModifyListener(lsMod);
		fdBadFile = new FormData();
		fdBadFile.left = new FormAttachment(middle, 0);
		fdBadFile.top = new FormAttachment(wLogFile, margin);
		fdBadFile.right = new FormAttachment(wbBadFile, -margin);
		wBadFile.setLayoutData(fdBadFile);		
		
		// Discard file line
		wlDiscardFile = new Label(shell, SWT.RIGHT);
		wlDiscardFile.setText(Messages.getString("OraBulkLoaderDialog.DiscardFile.Label")); //$NON-NLS-1$
 		props.setLook(wlDiscardFile);
		fdlDiscardFile = new FormData();
		fdlDiscardFile.left = new FormAttachment(0, 0);
		fdlDiscardFile.top = new FormAttachment(wBadFile, margin);
		fdlDiscardFile.right = new FormAttachment(middle, -margin);
		wlDiscardFile.setLayoutData(fdlDiscardFile);
		wbDiscardFile = new Button(shell, SWT.PUSH | SWT.CENTER);
 		props.setLook(wbDiscardFile);
		wbDiscardFile.setText(Messages.getString("OraBulkLoaderDialog.Browse.Button")); //$NON-NLS-1$
		fdbDiscardFile = new FormData();
		fdbDiscardFile.right = new FormAttachment(100, 0);
		fdbDiscardFile.top = new FormAttachment(wBadFile, margin);
		wbDiscardFile.setLayoutData(fdbDiscardFile);
		wDiscardFile = new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wDiscardFile);
		wDiscardFile.addModifyListener(lsMod);
		fdDiscardFile = new FormData();
		fdDiscardFile.left = new FormAttachment(middle, 0);
		fdDiscardFile.top = new FormAttachment(wBadFile, margin);
		fdDiscardFile.right = new FormAttachment(wbDiscardFile, -margin);
		wDiscardFile.setLayoutData(fdDiscardFile);			
		
		//
        // Control encoding line
        //
        // The drop down is editable as it may happen an encoding may not be present
        // on one machine, but you may want to use it on your execution server
        //
        wlEncoding=new Label(shell, SWT.RIGHT);
        wlEncoding.setText(Messages.getString("OraBulkLoaderDialog.Encoding.Label"));
        props.setLook(wlEncoding);
        fdlEncoding=new FormData();
        fdlEncoding.left  = new FormAttachment(0, 0);
        fdlEncoding.top   = new FormAttachment(wDiscardFile, margin);
        fdlEncoding.right = new FormAttachment(middle, -margin);
        wlEncoding.setLayoutData(fdlEncoding);
        wEncoding=new Combo(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        wEncoding.setToolTipText(Messages.getString("OraBulkLoaderDialog.Encoding.Tooltip"));
        wEncoding.setItems(encodings);
        props.setLook(wEncoding);
        fdEncoding=new FormData();
        fdEncoding.left = new FormAttachment(middle, 0);
        fdEncoding.top  = new FormAttachment(wDiscardFile, margin);
        fdEncoding.right= new FormAttachment(100, 0);        
        wEncoding.setLayoutData(fdEncoding);
        wEncoding.addModifyListener(lsMod);
               
        // Oracle character set name line
        wlCharacterSetName=new Label(shell, SWT.RIGHT);
        wlCharacterSetName.setText(Messages.getString("OraBulkLoaderDialog.CharacterSetName.Label"));
        props.setLook(wlCharacterSetName);
        fdlCharacterSetName=new FormData();
        fdlCharacterSetName.left  = new FormAttachment(0, 0);
        fdlCharacterSetName.top   = new FormAttachment(wEncoding, margin);
        fdlCharacterSetName.right = new FormAttachment(middle, -margin);
        wlCharacterSetName.setLayoutData(fdlCharacterSetName);
        wCharacterSetName=new Combo(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        wCharacterSetName.setToolTipText(Messages.getString("OraBulkLoaderDialog.CharacterSetName.Tooltip"));
        wCharacterSetName.setItems(characterSetNames);
        props.setLook(wCharacterSetName);
        fdCharacterSetName=new FormData();
        fdCharacterSetName.left = new FormAttachment(middle, 0);
        fdCharacterSetName.top  = new FormAttachment(wEncoding, margin);
        fdCharacterSetName.right= new FormAttachment(100, 0);        
        wCharacterSetName.setLayoutData(fdCharacterSetName);
        wCharacterSetName.addModifyListener(lsMod);
        
        // Alternate Record Terminator
		wlAltRecordTerm = new Label(shell, SWT.RIGHT);
		wlAltRecordTerm.setText(Messages.getString("OraBulkLoaderDialog.AltRecordTerm.Label")); //$NON-NLS-1$
 		props.setLook(wlAltRecordTerm);
		fdlAltRecordTerm = new FormData();
		fdlAltRecordTerm.left = new FormAttachment(0, 0);
		fdlAltRecordTerm.top = new FormAttachment(wCharacterSetName, margin);
		fdlAltRecordTerm.right = new FormAttachment(middle, -margin);
		wlAltRecordTerm.setLayoutData(fdlAltRecordTerm);
		wAltRecordTerm = new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wAltRecordTerm);
		fdAltRecordTerm = new FormData();
		fdAltRecordTerm.left = new FormAttachment(middle, 0);
		fdAltRecordTerm.top = new FormAttachment(wCharacterSetName, margin);
		fdAltRecordTerm.right = new FormAttachment(100, 0);
		wAltRecordTerm.setLayoutData(fdAltRecordTerm);
		wAltRecordTerm.addModifyListener(lsMod);		
				
		// DirectPath line
		wlDirectPath = new Label(shell, SWT.RIGHT);
		wlDirectPath.setText(Messages.getString("OraBulkLoaderDialog.DirectPath.Label")); //$NON-NLS-1$
 		props.setLook(wlDirectPath);
		fdlDirectPath = new FormData();
		fdlDirectPath.left = new FormAttachment(0, 0);
		fdlDirectPath.top = new FormAttachment(wAltRecordTerm, margin);
		fdlDirectPath.right = new FormAttachment(middle, -margin);
		wlDirectPath.setLayoutData(fdlDirectPath);
		wDirectPath = new Button(shell, SWT.CHECK);
 		props.setLook(wDirectPath);
		fdDirectPath = new FormData();
		fdDirectPath.left = new FormAttachment(middle, 0);
		fdDirectPath.top = new FormAttachment(wAltRecordTerm, margin);
		fdDirectPath.right = new FormAttachment(100, 0);
		wDirectPath.setLayoutData(fdDirectPath);	
		wDirectPath.addSelectionListener(new SelectionAdapter() 
		    {
			    public void widgetSelected(SelectionEvent e) 
			    {
  				    input.setChanged();
  		    	}
		    }
	    );

		// Erase files line
		wlEraseFiles = new Label(shell, SWT.RIGHT);
		wlEraseFiles.setText(Messages.getString("OraBulkLoaderDialog.EraseFiles.Label")); //$NON-NLS-1$
 		props.setLook(wlEraseFiles);
		fdlEraseFiles = new FormData();
		fdlEraseFiles.left = new FormAttachment(0, 0);
		fdlEraseFiles.top = new FormAttachment(wDirectPath, margin);
		fdlEraseFiles.right = new FormAttachment(middle, -margin);
		wlEraseFiles.setLayoutData(fdlEraseFiles);
		wEraseFiles = new Button(shell, SWT.CHECK);
 		props.setLook(wEraseFiles);
		fdEraseFiles = new FormData();
		fdEraseFiles.left = new FormAttachment(middle, 0);
		fdEraseFiles.top = new FormAttachment(wDirectPath, margin);
		fdEraseFiles.right = new FormAttachment(100, 0);
		wEraseFiles.setLayoutData(fdEraseFiles);				
		wEraseFiles.addSelectionListener(new SelectionAdapter() 
    	    {
		        public void widgetSelected(SelectionEvent e) 
		        {
				    input.setChanged();
		    	}
	        }
        );
		
		// Fail on warning line
		wlFailOnWarning = new Label(shell, SWT.RIGHT);
		wlFailOnWarning.setText(Messages.getString("OraBulkLoaderDialog.FailOnWarning.Label")); //$NON-NLS-1$
 		props.setLook(wlFailOnWarning);
		fdlFailOnWarning = new FormData();
		fdlFailOnWarning.left = new FormAttachment(0, 0);
		fdlFailOnWarning.top = new FormAttachment(wEraseFiles, margin);
		fdlFailOnWarning.right = new FormAttachment(middle, -margin);
		wlFailOnWarning.setLayoutData(fdlFailOnWarning);
		wFailOnWarning = new Button(shell, SWT.CHECK);
 		props.setLook(wFailOnWarning);
		fdFailOnWarning = new FormData();
		fdFailOnWarning.left = new FormAttachment(middle, 0);
		fdFailOnWarning.top = new FormAttachment(wEraseFiles, margin);
		fdFailOnWarning.right = new FormAttachment(100, 0);
		wFailOnWarning.setLayoutData(fdFailOnWarning);				
		wFailOnWarning.addSelectionListener(new SelectionAdapter() 
    	    {
		        public void widgetSelected(SelectionEvent e) 
		        {
				    input.setChanged();
		    	}
	        }
        );
		
		// Fail on error line
		wlFailOnError = new Label(shell, SWT.RIGHT);
		wlFailOnError.setText(Messages.getString("OraBulkLoaderDialog.FailOnError.Label")); //$NON-NLS-1$
 		props.setLook(wlFailOnError);
		fdlFailOnError = new FormData();
		fdlFailOnError.left = new FormAttachment(0, 0);
		fdlFailOnError.top = new FormAttachment(wFailOnWarning, margin);
		fdlFailOnError.right = new FormAttachment(middle, -margin);
		wlFailOnError.setLayoutData(fdlFailOnError);
		wFailOnError = new Button(shell, SWT.CHECK);
 		props.setLook(wFailOnError);
		fdFailOnError = new FormData();
		fdFailOnError.left = new FormAttachment(middle, 0);
		fdFailOnError.top = new FormAttachment(wFailOnWarning, margin);
		fdFailOnError.right = new FormAttachment(100, 0);
		wFailOnError.setLayoutData(fdFailOnError);				
		wFailOnError.addSelectionListener(new SelectionAdapter() 
    	    {
		        public void widgetSelected(SelectionEvent e) 
		        {
				    input.setChanged();
		    	}
	        }
        );
		
		// THE BUTTONS
		wOK = new Button(shell, SWT.PUSH);
		wOK.setText(Messages.getString("System.Button.OK")); //$NON-NLS-1$
		wSQL = new Button(shell, SWT.PUSH);
		wSQL.setText(Messages.getString("OraBulkLoaderDialog.SQL.Button")); //$NON-NLS-1$
		wCancel = new Button(shell, SWT.PUSH);
		wCancel.setText(Messages.getString("System.Button.Cancel")); //$NON-NLS-1$

		setButtonPositions(new Button[] { wOK, wCancel , wSQL }, margin, null);

		// The field Table
		wlReturn = new Label(shell, SWT.NONE);
		wlReturn.setText(Messages.getString("OraBulkLoaderDialog.Fields.Label")); //$NON-NLS-1$
 		props.setLook(wlReturn);
		fdlReturn = new FormData();
		fdlReturn.left = new FormAttachment(0, 0);
		fdlReturn.top = new FormAttachment(wFailOnError, margin);
		wlReturn.setLayoutData(fdlReturn);

		int UpInsCols = 3;
		int UpInsRows = (input.getFieldTable() != null ? input.getFieldTable().length : 1);

		ciReturn = new ColumnInfo[UpInsCols];
		ciReturn[0] = new ColumnInfo(Messages.getString("OraBulkLoaderDialog.ColumnInfo.TableField"), ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] { "" }, false); //$NON-NLS-1$
		ciReturn[1] = new ColumnInfo(Messages.getString("OraBulkLoaderDialog.ColumnInfo.StreamField"), ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] { "" }, false); //$NON-NLS-1$
		ciReturn[2] = new ColumnInfo(Messages.getString("OraBulkLoaderDialog.ColumnInfo.DateMask"), ColumnInfo.COLUMN_TYPE_CCOMBO, 
				                     new String[] {"",                //$NON-NLS-1$
			                                       Messages.getString("OraBulkLoaderDialog.DateMask.Label"),
	                                        	   Messages.getString("OraBulkLoaderDialog.DateTimeMask.Label")}, true); 
		tableFieldColumns.add(ciReturn[0]);
		wReturn = new TableView(transMeta, shell, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL,
				ciReturn, UpInsRows, lsMod, props);

		wGetLU = new Button(shell, SWT.PUSH);
		wGetLU.setText(Messages.getString("OraBulkLoaderDialog.GetFields.Label")); //$NON-NLS-1$
		fdGetLU = new FormData();
		fdGetLU.top   = new FormAttachment(wlReturn, margin);
		fdGetLU.right = new FormAttachment(100, 0);
		wGetLU.setLayoutData(fdGetLU);

		wDoMapping = new Button(shell, SWT.PUSH);
		wDoMapping.setText(Messages.getString("OraBulkLoaderDialog.EditMapping.Label")); //$NON-NLS-1$
		fdDoMapping = new FormData();
		fdDoMapping.top   = new FormAttachment(wGetLU, margin);
		fdDoMapping.right = new FormAttachment(100, 0);
		wDoMapping.setLayoutData(fdDoMapping);

		wDoMapping.addListener(SWT.Selection, new Listener() { 	public void handleEvent(Event arg0) { generateMappings();}});

		
		fdReturn = new FormData();
		fdReturn.left = new FormAttachment(0, 0);
		fdReturn.top = new FormAttachment(wlReturn, margin);
		fdReturn.right = new FormAttachment(wGetLU, -margin);
		fdReturn.bottom = new FormAttachment(wOK, -2*margin);
		wReturn.setLayoutData(fdReturn);
		
	    // 
        // Search the fields in the background
        //
        
        final Runnable runnable = new Runnable()
        {
            public void run()
            {
                StepMeta stepMeta = transMeta.findStep(stepname);
                if (stepMeta!=null)
                {
                    try
                    {
                    	RowMetaInterface row = transMeta.getPrevStepFields(stepMeta);
                        
                        // Remember these fields...
                        for (int i=0;i<row.size();i++)
                        {
                        	inputFields.put(row.getValueMeta(i).getName(), Integer.valueOf(i));
                        }
                        
                        setComboBoxes(); 
                    }
                    catch(KettleException e)
                    {
                        log.logError(toString(),Messages.getString("System.Dialog.GetFieldsFailed.Message"));
                    }
                }
            }
        };
        new Thread(runnable).start();

		wbSqlldr.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent e)
            {
                FileDialog dialog = new FileDialog(shell, SWT.OPEN);
                dialog.setFilterExtensions(new String[] { "*" });
                if (wSqlldr.getText() != null)
                {
                    dialog.setFileName(wSqlldr.getText());
                }
                dialog.setFilterNames(ALL_FILETYPES);
                if (dialog.open() != null)
                {
                	wSqlldr.setText(dialog.getFilterPath() + Const.FILE_SEPARATOR
                                      + dialog.getFileName());
                }
            }
        });
			
		wbControlFile.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent e)
            {
                FileDialog dialog = new FileDialog(shell, SWT.OPEN);
                dialog.setFilterExtensions(new String[] { "*" });
                if (wControlFile.getText() != null)
                {
                    dialog.setFileName(wControlFile.getText());
                }
                dialog.setFilterNames(ALL_FILETYPES);               
                if (dialog.open() != null)
                {
                	wControlFile.setText(dialog.getFilterPath() + Const.FILE_SEPARATOR
                                      + dialog.getFileName());
                }
            }
        });		
		
		wbDataFile.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent e)
            {
                FileDialog dialog = new FileDialog(shell, SWT.OPEN);
                dialog.setFilterExtensions(new String[] { "*" });
                if (wDataFile.getText() != null)
                {
                    dialog.setFileName(wDataFile.getText());
                }                
                dialog.setFilterNames(ALL_FILETYPES);
                if (dialog.open() != null)
                {
                	wDataFile.setText(dialog.getFilterPath() + Const.FILE_SEPARATOR
                                      + dialog.getFileName());
                }
            }
        });	

		wbLogFile.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent e)
            {
                FileDialog dialog = new FileDialog(shell, SWT.OPEN);
                dialog.setFilterExtensions(new String[] { "*" });
                if (wLogFile.getText() != null)
                {
                    dialog.setFileName(wLogFile.getText());
                }                
                dialog.setFilterNames(ALL_FILETYPES);
                if (dialog.open() != null)
                {
                	wLogFile.setText(dialog.getFilterPath() + Const.FILE_SEPARATOR
                                      + dialog.getFileName());
                }
            }
        });
		
		wbBadFile.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent e)
            {
                FileDialog dialog = new FileDialog(shell, SWT.OPEN);
                dialog.setFilterExtensions(new String[] { "*" });
                if (wBadFile.getText() != null)
                {
                    dialog.setFileName(wBadFile.getText());
                }                
                dialog.setFilterNames(ALL_FILETYPES);
                if (dialog.open() != null)
                {
                	wBadFile.setText(dialog.getFilterPath() + Const.FILE_SEPARATOR
                                      + dialog.getFileName());
                }
            }
        });	
		
		wbDiscardFile.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent e)
            {
                FileDialog dialog = new FileDialog(shell, SWT.OPEN);
                dialog.setFilterExtensions(new String[] { "*" });
                if (wDiscardFile.getText() != null)
                {
                    dialog.setFileName(wDiscardFile.getText());
                }                
                dialog.setFilterNames(ALL_FILETYPES);
                if (dialog.open() != null)
                {
                	wDiscardFile.setText(dialog.getFilterPath() + Const.FILE_SEPARATOR
                                      + dialog.getFileName());
                }
            }
        });			
		
		// Add listeners
		lsOK = new Listener()
		{
			public void handleEvent(Event e)
			{
				ok();
			}
		};
		lsGetLU = new Listener()
		{
			public void handleEvent(Event e)
			{
				getUpdate();
			}
		};
		lsSQL = new Listener()
		{
			public void handleEvent(Event e)
			{
				create();
			}
		};
		lsCancel = new Listener()
		{
			public void handleEvent(Event e)
			{
				cancel();
			}
		};

		wOK.addListener(SWT.Selection, lsOK);
		wGetLU.addListener(SWT.Selection, lsGetLU);
		wSQL.addListener(SWT.Selection, lsSQL);
		wCancel.addListener(SWT.Selection, lsCancel);

		lsDef = new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };

		wStepname.addSelectionListener(lsDef);
        wSchema.addSelectionListener(lsDef);
        wTable.addSelectionListener(lsDef);
        wMaxErrors.addSelectionListener(lsDef);
        wCommit.addSelectionListener(lsDef);
        wBindSize.addSelectionListener(lsDef);
        wReadSize.addSelectionListener(lsDef);
        wDbNameOverride.addSelectionListener(lsDef);
        wControlFile.addSelectionListener(lsDef);
        wDataFile.addSelectionListener(lsDef);
        wLogFile.addSelectionListener(lsDef);
        wBadFile.addSelectionListener(lsDef);
        wDiscardFile.addSelectionListener(lsDef);
        wAltRecordTerm.addSelectionListener(lsDef);

		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(new ShellAdapter()
		{
			public void shellClosed(ShellEvent e)
			{
				cancel();
			}
		});


		wbTable.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				getTableName();
			}
		});

		// Set the shell size, based upon previous time...
		setSize();

		getData();
		setTableFieldCombo();
		input.setChanged(changed);

		shell.open();
		while (!shell.isDisposed())
		{
			if (!display.readAndDispatch())
				display.sleep();
		}
		return stepname;
	}
	protected void setComboBoxes()
    {
        // Something was changed in the row.
        //
		final Map<String, Integer> fields = new HashMap<String, Integer>();
        
        // Add the currentMeta fields...
        fields.putAll(inputFields);
        
        Set<String> keySet = fields.keySet();
        List<String> entries = new ArrayList<String>(keySet);
        
        String[] fieldNames= (String[]) entries.toArray(new String[entries.size()]);
        Const.sortStrings(fieldNames);
        // return fields
        ciReturn[1].setComboValues(fieldNames);
        
    }
	private void setTableFieldCombo(){
		Runnable fieldLoader = new Runnable() {
			public void run() {
				//clear
				for (int i = 0; i < tableFieldColumns.size(); i++) {
					ColumnInfo colInfo = (ColumnInfo) tableFieldColumns.get(i);
					colInfo.setComboValues(new String[] {});
				}
				if (!Const.isEmpty(wTable.getText())) {
					DatabaseMeta ci = transMeta.findDatabase(wConnection.getText());
					if (ci != null) {
						Database db = new Database(ci);
						try {
							db.connect();

							String schemaTable = ci	.getQuotedSchemaTableCombination(transMeta.environmentSubstitute(wSchema
											.getText()), transMeta.environmentSubstitute(wTable.getText()));
							RowMetaInterface r = db.getTableFields(schemaTable);
							if (null != r) {
								String[] fieldNames = r.getFieldNames();
								if (null != fieldNames) {
									for (int i = 0; i < tableFieldColumns.size(); i++) {
										ColumnInfo colInfo = (ColumnInfo) tableFieldColumns.get(i);
										colInfo.setComboValues(fieldNames);
									}
								}
							}
						} catch (Exception e) {
							for (int i = 0; i < tableFieldColumns.size(); i++) {
								ColumnInfo colInfo = (ColumnInfo) tableFieldColumns	.get(i);
								colInfo.setComboValues(new String[] {});
							}
							// ignore any errors here. drop downs will not be
							// filled, but no problem for the user
						}
					}
				}
			}
		};
		shell.getDisplay().asyncExec(fieldLoader);
	}
	/**
	 * Copy information from the meta-data input to the dialog fields.
	 */
	public void getData()
	{
		int i;
		if(log.isDebug()) log.logDebug(toString(), Messages.getString("OraBulkLoaderDialog.Log.GettingKeyInfo")); //$NON-NLS-1$

		wMaxErrors.setText("" + input.getMaxErrors());   //$NON-NLS-1$
		wCommit.setText("" + input.getCommitSize());     //$NON-NLS-1$
		wBindSize.setText("" + input.getBindSize());     //$NON-NLS-1$
		wReadSize.setText("" + input.getReadSize());     //$NON-NLS-1$		

		if (input.getFieldTable() != null)
			for (i = 0; i < input.getFieldTable().length; i++)
			{
				TableItem item = wReturn.table.getItem(i);
				if (input.getFieldTable()[i] != null)
					item.setText(1, input.getFieldTable()[i]);
				if (input.getFieldStream()[i] != null)
					item.setText(2, input.getFieldStream()[i]);
				String dateMask = input.getDateMask()[i];
				if (dateMask!=null) {
					if ( OraBulkLoaderMeta.DATE_MASK_DATE.equals(dateMask) )
					{
					    item.setText(3,Messages.getString("OraBulkLoaderDialog.DateMask.Label"));
					}
					else if ( OraBulkLoaderMeta.DATE_MASK_DATETIME.equals(dateMask)) 
					{
						item.setText(3,Messages.getString("OraBulkLoaderDialog.DateTimeMask.Label"));
					}
					else 
					{
						item.setText(3,"");
					}					
				} 
				else {
					item.setText(3,"");
				}
			}

		if (input.getDatabaseMeta() != null)
			wConnection.setText(input.getDatabaseMeta().getName());
		else
		{
			if (transMeta.nrDatabases() == 1)
			{
				wConnection.setText(transMeta.getDatabase(0).getName());
			}
		}
        if (input.getSchemaName() != null) wSchema.setText(input.getSchemaName());
		if (input.getTableName() != null) wTable.setText(input.getTableName());
		if (input.getSqlldr() != null) wSqlldr.setText(input.getSqlldr());
		if (input.getControlFile() != null) wControlFile.setText(input.getControlFile());
		if (input.getDataFile() != null) wDataFile.setText(input.getDataFile());
		if (input.getLogFile() != null) wLogFile.setText(input.getLogFile());
		if (input.getBadFile() != null) wBadFile.setText(input.getBadFile());
		if (input.getDiscardFile() != null) wDiscardFile.setText(input.getDiscardFile());	
		if (input.getEncoding() != null) wEncoding.setText(input.getEncoding());
		if (input.getCharacterSetName() != null) wCharacterSetName.setText(input.getCharacterSetName());
		if (input.getDbNameOverride() != null ) wDbNameOverride.setText(input.getDbNameOverride());
		if (input.getAltRecordTerm() != null ) wAltRecordTerm.setText(input.getAltRecordTerm());
		wDirectPath.setSelection(input.isDirectPath());
		wEraseFiles.setSelection(input.isEraseFiles());
		wFailOnError.setSelection(input.isFailOnError());
		wFailOnWarning.setSelection(input.isFailOnWarning());
		
		String method = input.getLoadMethod();		
		if ( OraBulkLoaderMeta.METHOD_AUTO_END.equals(method) ) 
		{
			wLoadMethod.select(0);
		}
		else if ( OraBulkLoaderMeta.METHOD_MANUAL.equals(method) ) 
		{
			wLoadMethod.select(1);
		}
		else if ( OraBulkLoaderMeta.METHOD_AUTO_CONCURRENT.equals(method) ) 
		{
			wLoadMethod.select(2);
		}
		else  
		{
			if(log.isDebug()) log.logDebug(toString(), "Internal error: load_method set to default 'auto at end'"); //$NON-NLS-1$
			wLoadMethod.select(0);
		}		
		
		String action = input.getLoadAction();
		if ( OraBulkLoaderMeta.ACTION_APPEND.equals(action))
		{
			wLoadAction.select(0);
		}
		else if ( OraBulkLoaderMeta.ACTION_INSERT.equals(action))
		{
			wLoadAction.select(1);
		}
		else if ( OraBulkLoaderMeta.ACTION_REPLACE.equals(action))
		{
			wLoadAction.select(2);
		}
		else if ( OraBulkLoaderMeta.ACTION_TRUNCATE.equals(action))
		{
			wLoadAction.select(3);
		}
		else
		{
			if(log.isDebug()) log.logDebug(toString(), "Internal error: load_action set to default 'append'"); //$NON-NLS-1$
    		wLoadAction.select(0);
		}
		
		
		wStepname.selectAll();
		wReturn.setRowNums();
		wReturn.optWidth(true);
	}
	
	private void cancel()
	{
		stepname = null;
		input.setChanged(changed);
		dispose();
	}

	private void getInfo(OraBulkLoaderMeta inf)
	{
		int nrfields = wReturn.nrNonEmpty();

		inf.allocate(nrfields);

		inf.setMaxErrors( Const.toInt(wMaxErrors.getText(), 0) );
		inf.setCommitSize( Const.toInt(wCommit.getText(), 0) );
		inf.setBindSize( Const.toInt(wBindSize.getText(), 0) );
		inf.setReadSize( Const.toInt(wReadSize.getText(), 0) );
		inf.setDbNameOverride(wDbNameOverride.getText());

		if(log.isDebug()) log.logDebug(toString(), Messages.getString("OraBulkLoaderDialog.Log.FoundFields", "" + nrfields)); //$NON-NLS-1$ //$NON-NLS-2$
		for (int i = 0; i < nrfields; i++)
		{
			TableItem item = wReturn.getNonEmpty(i);
			inf.getFieldTable()[i] = item.getText(1);
			inf.getFieldStream()[i] = item.getText(2);
			if ( Messages.getString("OraBulkLoaderDialog.DateMask.Label").equals(item.getText(3)) )
 			    inf.getDateMask()[i] = OraBulkLoaderMeta.DATE_MASK_DATE;
			else if ( Messages.getString("OraBulkLoaderDialog.DateTimeMask.Label").equals(item.getText(3)) )
				inf.getDateMask()[i] = OraBulkLoaderMeta.DATE_MASK_DATETIME;
			else inf.getDateMask()[i] = "";
		}

        inf.setSchemaName( wSchema.getText() );
		inf.setTableName( wTable.getText() );
		inf.setDatabaseMeta(  transMeta.findDatabase(wConnection.getText()) );
		inf.setSqlldr( wSqlldr.getText() );
		inf.setControlFile( wControlFile.getText() );
		inf.setDataFile( wDataFile.getText() );
		inf.setLogFile( wLogFile.getText() );
		inf.setBadFile( wBadFile.getText() );
		inf.setDiscardFile( wDiscardFile.getText() );
		inf.setEncoding( wEncoding.getText() );
		inf.setCharacterSetName( wCharacterSetName.getText() );
		inf.setAltRecordTerm( wAltRecordTerm.getText() );
		inf.setDirectPath( wDirectPath.getSelection() );
		inf.setEraseFiles( wEraseFiles.getSelection() );
		inf.setFailOnError( wFailOnError.getSelection() );
		inf.setFailOnWarning( wFailOnWarning.getSelection() );

		/*
		 * Set the loadmethod
		 */
		String method = wLoadMethod.getText();
		if ( Messages.getString("OraBulkLoaderDialog.AutoConcLoadMethod.Label").equals(method) ) 
		{
			inf.setLoadMethod(OraBulkLoaderMeta.METHOD_AUTO_CONCURRENT);
		}
		else if ( Messages.getString("OraBulkLoaderDialog.AutoEndLoadMethod.Label").equals(method) ) 
		{
			inf.setLoadMethod(OraBulkLoaderMeta.METHOD_AUTO_END);
		}
		else if ( Messages.getString("OraBulkLoaderDialog.ManualLoadMethod.Label").equals(method) ) 
		{
			inf.setLoadMethod(OraBulkLoaderMeta.METHOD_MANUAL);
		}
		else  
		{
			if(log.isDebug()) log.logDebug(toString(), "Internal error: load_method set to default 'auto concurrent', value found '" + method + "'."); //$NON-NLS-1$
			inf.setLoadMethod(OraBulkLoaderMeta.METHOD_AUTO_END);
		}	
		
		/*
		 * Set the loadaction 
		 */
		String action = wLoadAction.getText();
		if ( Messages.getString("OraBulkLoaderDialog.AppendLoadAction.Label").equals(action) ) 
		{
			inf.setLoadAction(OraBulkLoaderMeta.ACTION_APPEND);
		}
		else if ( Messages.getString("OraBulkLoaderDialog.InsertLoadAction.Label").equals(action) )
		{
			inf.setLoadAction(OraBulkLoaderMeta.ACTION_INSERT);
		}
		else if ( Messages.getString("OraBulkLoaderDialog.ReplaceLoadAction.Label").equals(action) )
		{
			inf.setLoadAction(OraBulkLoaderMeta.ACTION_REPLACE);
		}
		else if ( Messages.getString("OraBulkLoaderDialog.TruncateLoadAction.Label").equals(action) )
		{
			inf.setLoadAction(OraBulkLoaderMeta.ACTION_TRUNCATE);
		}
		else
		{
			if(log.isDebug()) log.logDebug(toString(), "Internal error: load_action set to default 'append', value found '" + action + "'."); //$NON-NLS-1$
			inf.setLoadAction(OraBulkLoaderMeta.ACTION_APPEND);	
		}

		stepname = wStepname.getText(); // return value
	}

	private void ok()
	{
		if (Const.isEmpty(wStepname.getText())) return;

		// Get the information for the dialog into the input structure.
		getInfo(input);

		if (input.getDatabaseMeta() == null)
		{
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
			mb.setMessage(Messages.getString("OraBulkLoaderDialog.InvalidConnection.DialogMessage")); //$NON-NLS-1$
			mb.setText(Messages.getString("OraBulkLoaderDialog.InvalidConnection.DialogTitle")); //$NON-NLS-1$
			mb.open();
		}

		dispose();
	}

	private void getTableName()
	{
		DatabaseMeta inf = null;
		// New class: SelectTableDialog
		int connr = wConnection.getSelectionIndex();
		if (connr >= 0)
			inf = transMeta.getDatabase(connr);

		if (inf != null)
		{
			if(log.isDebug()) log.logDebug(toString(), Messages.getString("OraBulkLoaderDialog.Log.LookingAtConnection") + inf.toString()); //$NON-NLS-1$

			DatabaseExplorerDialog std = new DatabaseExplorerDialog(shell, SWT.NONE, inf, transMeta.getDatabases());
            std.setSelectedSchema(wSchema.getText());
            std.setSelectedTable(wTable.getText());
            std.setSplitSchemaAndTable(true);
			if (std.open() != null)
			{
                wSchema.setText(Const.NVL(std.getSchemaName(), ""));
                wTable.setText(Const.NVL(std.getTableName(), ""));
			}
		}
		else
		{
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
			mb.setMessage(Messages.getString("OraBulkLoaderDialog.InvalidConnection.DialogMessage")); //$NON-NLS-1$
			mb.setText(Messages.getString("OraBulkLoaderDialog.InvalidConnection.DialogTitle")); //$NON-NLS-1$
			mb.open();
		}
	}

	private void getUpdate()
	{
		try
		{
			RowMetaInterface r = transMeta.getPrevStepFields(stepname);
			if (r != null)
			{
                TableItemInsertListener listener = new TableItemInsertListener()
                {
                    public boolean tableItemInserted(TableItem tableItem, ValueMetaInterface v)
                    {
                    	if ( v.getType() == ValueMetaInterface.TYPE_DATE )
                    	{
                    		// The default is date mask.
                    		tableItem.setText(3, Messages.getString("OraBulkLoaderDialog.DateMask.Label"));	
                    	}
                    	else
                    	{
                            tableItem.setText(3, "");
                    	}
                        return true;
                    }
                };
                BaseStepDialog.getFieldsFromPrevious(r, wReturn, 1, new int[] { 1, 2}, new int[] {}, -1, -1, listener);
			}
		}
		catch (KettleException ke)
		{
			new ErrorDialog(shell, Messages.getString("OraBulkLoaderDialog.FailedToGetFields.DialogTitle"), //$NON-NLS-1$
					Messages.getString("OraBulkLoaderDialog.FailedToGetFields.DialogMessage"), ke); //$NON-NLS-1$
		}
	}

	// Generate code for create table...
	// Conversions done by Database
	private void create()
	{
		try
		{
			OraBulkLoaderMeta info = new OraBulkLoaderMeta();
			getInfo(info);

			String name = stepname; // new name might not yet be linked to other steps!
			StepMeta stepMeta = new StepMeta(Messages.getString("OraBulkLoaderDialog.StepMeta.Title"), name, info); //$NON-NLS-1$
			RowMetaInterface prev = transMeta.getPrevStepFields(stepname);

			SQLStatement sql = info.getSQLStatements(transMeta, stepMeta, prev);
			if (!sql.hasError())
			{
				if (sql.hasSQL())
				{
					SQLEditor sqledit = new SQLEditor(shell, SWT.NONE, info.getDatabaseMeta(), transMeta.getDbCache(),
							sql.getSQL());
					sqledit.open();
				}
				else
				{
					MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_INFORMATION);
					mb.setMessage(Messages.getString("OraBulkLoaderDialog.NoSQLNeeds.DialogMessage")); //$NON-NLS-1$
					mb.setText(Messages.getString("OraBulkLoaderDialog.NoSQLNeeds.DialogTitle")); //$NON-NLS-1$
					mb.open();
				}
			}
			else
			{
				MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
				mb.setMessage(sql.getError());
				mb.setText(Messages.getString("OraBulkLoaderDialog.SQLError.DialogTitle")); //$NON-NLS-1$
				mb.open();
			}
		}
		catch (KettleException ke)
		{
			new ErrorDialog(shell, Messages.getString("OraBulkLoaderDialog.CouldNotBuildSQL.DialogTitle"), //$NON-NLS-1$
					Messages.getString("OraBulkLoaderDialog.CouldNotBuildSQL.DialogMessage"), ke); //$NON-NLS-1$
		}

	}
	/**
	 * Reads in the fields from the previous steps and from the ONE next step and opens an 
	 * EnterMappingDialog with this information. After the user did the mapping, those information 
	 * is put into the Select/Rename table.
	 */
	private void generateMappings() {

		// Determine the source and target fields...
		//
		RowMetaInterface sourceFields;
		RowMetaInterface targetFields;

		try {
			sourceFields = transMeta.getPrevStepFields(stepMeta);
		} catch(KettleException e) {
			new ErrorDialog(shell, Messages.getString("OraBulkLoaderDialog.DoMapping.UnableToFindSourceFields.Title"), Messages.getString("OraBulkLoaderDialog.DoMapping.UnableToFindSourceFields.Message"), e);
			return;
		}
		// refresh data
		input.setDatabaseMeta(transMeta.findDatabase(wConnection.getText()) );
		input.setTableName(transMeta.environmentSubstitute(wTable.getText()));
		StepMetaInterface stepMetaInterface = stepMeta.getStepMetaInterface();
		try {
			targetFields = stepMetaInterface.getRequiredFields(transMeta);
		} catch (KettleException e) {
			new ErrorDialog(shell, Messages.getString("OraBulkLoaderDialog.DoMapping.UnableToFindTargetFields.Title"), Messages.getString("OraBulkLoaderDialog.DoMapping.UnableToFindTargetFields.Message"), e);
			return;
		}

		String[] inputNames = new String[sourceFields.size()];
		for (int i = 0; i < sourceFields.size(); i++) {
			ValueMetaInterface value = sourceFields.getValueMeta(i);
			inputNames[i] = value.getName()+
			     EnterMappingDialog.STRING_ORIGIN_SEPARATOR+value.getOrigin()+")";
		}

		// Create the existing mapping list...
		//
		List<SourceToTargetMapping> mappings = new ArrayList<SourceToTargetMapping>();
		StringBuffer missingSourceFields = new StringBuffer();
		StringBuffer missingTargetFields = new StringBuffer();

		int nrFields = wReturn.nrNonEmpty();
		for (int i = 0; i < nrFields ; i++) {
			TableItem item = wReturn.getNonEmpty(i);
			String source = item.getText(2);
			String target = item.getText(1);
			
			int sourceIndex = sourceFields.indexOfValue(source); 
			if (sourceIndex<0) {
				missingSourceFields.append(Const.CR + "   " + source+" --> " + target);
			}
			int targetIndex = targetFields.indexOfValue(target);
			if (targetIndex<0) {
				missingTargetFields.append(Const.CR + "   " + source+" --> " + target);
			}
			if (sourceIndex<0 || targetIndex<0) {
				continue;
			}

			SourceToTargetMapping mapping = new SourceToTargetMapping(sourceIndex, targetIndex);
			mappings.add(mapping);
		}

		// show a confirm dialog if some missing field was found
		//
		if (missingSourceFields.length()>0 || missingTargetFields.length()>0){
			
			String message="";
			if (missingSourceFields.length()>0) {
				message+=Messages.getString("OraBulkLoaderDialog.DoMapping.SomeSourceFieldsNotFound", missingSourceFields.toString())+Const.CR;
			}
			if (missingTargetFields.length()>0) {
				message+=Messages.getString("OraBulkLoaderDialog.DoMapping.SomeTargetFieldsNotFound", missingSourceFields.toString())+Const.CR;
			}
			message+=Const.CR;
			message+=Messages.getString("OraBulkLoaderDialog.DoMapping.SomeFieldsNotFoundContinue")+Const.CR;
			MessageDialog.setDefaultImage(GUIResource.getInstance().getImageSpoon());
			boolean goOn = MessageDialog.openConfirm(shell, Messages.getString("OraBulkLoaderDialog.DoMapping.SomeFieldsNotFoundTitle"), message);
			if (!goOn) {
				return;
			}
		}
		EnterMappingDialog d = new EnterMappingDialog(OraBulkLoaderDialog.this.shell, sourceFields.getFieldNames(), targetFields.getFieldNames(), mappings);
		mappings = d.open();

		// mappings == null if the user pressed cancel
		//
		if (mappings!=null) {
			// Clear and re-populate!
			//
			wReturn.table.removeAll();
			wReturn.table.setItemCount(mappings.size());
			for (int i = 0; i < mappings.size(); i++) {
				SourceToTargetMapping mapping = (SourceToTargetMapping) mappings.get(i);
				TableItem item = wReturn.table.getItem(i);
				item.setText(2, sourceFields.getValueMeta(mapping.getSourcePosition()).getName());
				item.setText(1, targetFields.getValueMeta(mapping.getTargetPosition()).getName());
			}
			wReturn.setRowNums();
			wReturn.optWidth(true);
		}
	}
	public String toString()
	{
		return this.getClass().getName();
	}
}