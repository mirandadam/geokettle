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
package org.pentaho.di.ui.job.dialog;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.parameters.UnknownParamException;
import org.pentaho.di.job.JobExecutionConfiguration;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.trans.step.BaseStepDialog;



public class JobExecutionConfigurationDialog extends Dialog
{
    private Display display;
    private Shell parent;
    private Shell shell;
    private PropsUI props;
    private boolean retval;
    
    private Button wOK, wCancel;
    
    private Group gLocal;
    
    private JobExecutionConfiguration configuration;
    private JobMeta jobMeta;

    private Button wExecLocal;
    private Button wExecRemote;
    private CCombo wRemoteHost;
    private Label wlRemoteHost;
    private Button wPassExport;

    private TableView wArguments;
    private Label wlArguments;
    private TableView wParams;
    private Label wlParams;
    private Label wlVariables;
    private TableView wVariables;
    
    private Group gDetails;

    
    private Label wlLogLevel;
    private CCombo wLogLevel;
    private Button wSafeMode;
    private Button wClearLog;

    private Label wlReplayDate;
    private Text wReplayDate;

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    public JobExecutionConfigurationDialog(Shell parent, JobExecutionConfiguration configuration, JobMeta jobMeta)
    {
        super(parent);
        this.parent = parent;
        this.configuration = configuration;
        this.jobMeta  = jobMeta;
        
        // Fill the parameters, maybe do this in another place?
        Map<String, String> params = configuration.getParams();
        params.clear();
        String[] paramNames = jobMeta.listParameters();
        for ( String name : paramNames ) {
        	params.put(name, "");
        }        
                
        props = PropsUI.getInstance();
    }
    
    public boolean open()
    {
        display = parent.getDisplay();
        shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN);
        props.setLook(shell);
		shell.setImage(GUIResource.getInstance().getImageJobGraph());
        
        FormLayout formLayout = new FormLayout ();
        formLayout.marginWidth  = Const.FORM_MARGIN;
        formLayout.marginHeight = Const.FORM_MARGIN;

        shell.setLayout(formLayout);
        shell.setText(Messages.getString("JobExecutionConfigurationDialog.Shell.Title")); //$NON-NLS-1$

        int margin = Const.MARGIN;
        int tabsize = 5*margin;
        
        wOK = new Button(shell, SWT.PUSH);
        wOK.setText(Messages.getString("JobExecutionConfigurationDialog.Button.Launch"));
        wOK.addSelectionListener(new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { ok(); }});
        wCancel = new Button(shell, SWT.PUSH);
        wCancel.setText(Messages.getString("System.Button.Cancel"));
        wCancel.addSelectionListener(new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { cancel(); }});
        
        BaseStepDialog.positionBottomButtons(shell, new Button[] { wOK, wCancel }, margin, null);
        
        
        gLocal = new Group(shell, SWT.SHADOW_ETCHED_IN);
        gLocal.setText(Messages.getString("JobExecutionConfigurationDialog.LocalGroup.Label")); //$NON-NLS-1$;
        // The layout
        FormLayout localLayout = new FormLayout();
        localLayout.marginWidth  = Const.FORM_MARGIN;
        localLayout.marginHeight = Const.FORM_MARGIN;
        gLocal.setLayout(localLayout);
        // 
        FormData fdLocal=new FormData();
        fdLocal.top    = new FormAttachment(0, 0);
        fdLocal.left   = new FormAttachment(0, 0);
        fdLocal.right  = new FormAttachment(100, 0);
        gLocal.setBackground(shell.getBackground()); // the default looks ugly
        gLocal.setLayoutData(fdLocal);

        /////////////////////////////////////////////////////////////////////////////////////////////////
        // Local execution
        //
        wExecLocal=new Button(gLocal, SWT.RADIO);
        wExecLocal.setText(Messages.getString("JobExecutionConfigurationDialog.ExecLocal.Label")); //$NON-NLS-1$
        wExecLocal.setToolTipText(Messages.getString("JobExecutionConfigurationDialog.ExecLocal.Tooltip")); //$NON-NLS-1$ //$NON-NLS-2$
        props.setLook(wExecLocal);
        FormData fdExecLocal = new FormData();
        fdExecLocal.left  = new FormAttachment(0, 0);
        fdExecLocal.right = new FormAttachment(33, 0);
        wExecLocal.setLayoutData(fdExecLocal);
        wExecLocal.addSelectionListener(new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { enableFields(); }});
        
        /////////////////////////////////////////////////////////////////////////////////////////////////
        // remote execution
        //
        wExecRemote=new Button(gLocal, SWT.RADIO);
        wExecRemote.setText(Messages.getString("JobExecutionConfigurationDialog.ExecRemote.Label")); //$NON-NLS-1$
        wExecRemote.setToolTipText(Messages.getString("JobExecutionConfigurationDialog.ExecRemote.Tooltip")); //$NON-NLS-1$ //$NON-NLS-2$
        props.setLook(wExecRemote);
        FormData fdExecRemote = new FormData();
        fdExecRemote.left  = new FormAttachment(33, margin);
        fdExecRemote.right = new FormAttachment(66, 0);
        wExecRemote.setLayoutData(fdExecRemote);
        wExecRemote.addSelectionListener(new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { enableFields(); }});

        wlRemoteHost = new Label(gLocal, SWT.LEFT);
        props.setLook(wlRemoteHost);
        wlRemoteHost.setText(Messages.getString("JobExecutionConfigurationDialog.RemoteHost.Label")); //$NON-NLS-1$
        wlRemoteHost.setToolTipText(Messages.getString("JobExecutionConfigurationDialog.RemoteHost.Tooltip")); //$NON-NLS-1$ //$NON-NLS-2$
        FormData fdlRemoteHost = new FormData();
        fdlRemoteHost.left  = new FormAttachment(33, tabsize);
        fdlRemoteHost.top   = new FormAttachment(wExecRemote, margin*2);
        wlRemoteHost.setLayoutData(fdlRemoteHost);

        wRemoteHost = new CCombo(gLocal, SWT.READ_ONLY | SWT.BORDER);
        wRemoteHost.setToolTipText(Messages.getString("JobExecutionConfigurationDialog.RemoteHost.Tooltip")); //$NON-NLS-1$ //$NON-NLS-2$
        props.setLook(wRemoteHost);
        FormData fdRemoteHost = new FormData();
        fdRemoteHost.left  = new FormAttachment(wlRemoteHost, margin);
        fdRemoteHost.right = new FormAttachment(66, 0);
        fdRemoteHost.top   = new FormAttachment(wExecRemote, margin*2);
        wRemoteHost.setLayoutData(fdRemoteHost);
        for (int i=0;i<jobMeta.getSlaveServers().size();i++)
        {
            SlaveServer slaveServer = (SlaveServer)jobMeta.getSlaveServers().get(i);
            wRemoteHost.add(slaveServer.toString());
        }
        
        wPassExport = new Button(gLocal, SWT.CHECK);
        wPassExport.setText(Messages.getString("JobExecutionConfigurationDialog.PassExport.Label")); //$NON-NLS-1$
        wPassExport.setToolTipText(Messages.getString("JobExecutionConfigurationDialog.PassExport.Tooltip")); //$NON-NLS-1$ //$NON-NLS-2$
        props.setLook(wPassExport);
        FormData fdPassExport = new FormData();
        fdPassExport.left  = new FormAttachment(33, margin);
        fdPassExport.top   = new FormAttachment(wRemoteHost, margin);
        wPassExport.setLayoutData(fdPassExport);
        
        /////////////////////////////////////////////////////////////////////////////////////////////////
        // Replay date, arguments & variables
        //

        gDetails = new Group(shell, SWT.SHADOW_ETCHED_IN);
        gDetails.setText(Messages.getString("JobExecutionConfigurationDialog.DetailsGroup.Label")); //$NON-NLS-1$;
        // The layout
        FormLayout detailsLayout = new FormLayout();
        detailsLayout.marginWidth  = Const.FORM_MARGIN;
        detailsLayout.marginHeight = Const.FORM_MARGIN;
        gDetails.setLayout(detailsLayout);
        // 
        FormData fdDetails=new FormData();
        fdDetails.left   = new FormAttachment(0, 0);
        fdDetails.top    = new FormAttachment(gLocal, margin*2);
        fdDetails.right  = new FormAttachment(100, 0);
        gDetails.setBackground(shell.getBackground()); // the default looks ugly
        gDetails.setLayoutData(fdDetails);

        wSafeMode = new Button(gDetails, SWT.CHECK);
        wSafeMode.setText(Messages.getString("JobExecutionConfigurationDialog.SafeMode.Label")); //$NON-NLS-1$
        wSafeMode.setToolTipText(Messages.getString("JobExecutionConfigurationDialog.SafeMode.Tooltip")); //$NON-NLS-1$ //$NON-NLS-2$
        props.setLook(wSafeMode);
        FormData fdSafeMode = new FormData();
        fdSafeMode.left  = new FormAttachment(50, margin);
        fdSafeMode.right = new FormAttachment(100, 0);
        fdSafeMode.top   = new FormAttachment(0, 0);
        wSafeMode.setLayoutData(fdSafeMode);
        wSafeMode.addSelectionListener(new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { enableFields(); }});

        wClearLog = new Button(gDetails, SWT.CHECK);
        wClearLog.setText(Messages.getString("JobExecutionConfigurationDialog.ClearLog.Label")); //$NON-NLS-1$
        wClearLog.setToolTipText(Messages.getString("JobExecutionConfigurationDialog.ClearLog.Tooltip")); //$NON-NLS-1$ //$NON-NLS-2$
        props.setLook(wClearLog);
        FormData fdClearLog = new FormData();
        fdClearLog.left  = new FormAttachment( 50, margin);
        fdClearLog.right = new FormAttachment(100, 0);
        fdClearLog.top   = new FormAttachment(wSafeMode, margin);
        wClearLog.setLayoutData(fdClearLog);
        wClearLog.addSelectionListener(new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { enableFields(); }});

        wlLogLevel = new Label(gDetails, SWT.LEFT);
        props.setLook(wlLogLevel);
        wlLogLevel.setText(Messages.getString("JobExecutionConfigurationDialog.LogLevel.Label")); //$NON-NLS-1$
        wlLogLevel.setToolTipText(Messages.getString("JobExecutionConfigurationDialog.LogLevel.Tooltip")); //$NON-NLS-1$ //$NON-NLS-2$
        FormData fdlLogLevel = new FormData();
        fdlLogLevel.left  = new FormAttachment(0, 0);
        fdlLogLevel.right = new FormAttachment(50, 0);
        fdlLogLevel.top   = new FormAttachment(wClearLog, margin);
        wlLogLevel.setLayoutData(fdlLogLevel);

        wLogLevel = new CCombo(gDetails, SWT.READ_ONLY | SWT.BORDER);
        wLogLevel.setToolTipText(Messages.getString("JobExecutionConfigurationDialog.LogLevel.Tooltip")); //$NON-NLS-1$ //$NON-NLS-2$
        props.setLook(wLogLevel);
        FormData fdLogLevel = new FormData();
        fdLogLevel.left  = new FormAttachment(50, margin);
        fdLogLevel.right = new FormAttachment(100, 0);
        fdLogLevel.top   = new FormAttachment(wClearLog, margin);
        wLogLevel.setLayoutData(fdLogLevel);
        wLogLevel.setItems( LogWriter.log_level_desc_long );

        // ReplayDate
        wlReplayDate = new Label(gDetails, SWT.LEFT);
        props.setLook(wlReplayDate);
        wlReplayDate.setText(Messages.getString("JobExecutionConfigurationDialog.ReplayDate.Label")); //$NON-NLS-1$
        wlReplayDate.setToolTipText(Messages.getString("JobExecutionConfigurationDialog.ReplayDate.Tooltip")); //$NON-NLS-1$ //$NON-NLS-2$
        FormData fdlReplayDate = new FormData();
        fdlReplayDate.left   = new FormAttachment(0, 0);
        fdlReplayDate.right  = new FormAttachment(50, 0);
        fdlReplayDate.top    = new FormAttachment(wLogLevel, margin);
        wlReplayDate.setLayoutData(fdlReplayDate);

        wReplayDate = new Text(gDetails, SWT.LEFT | SWT.BORDER | SWT.SINGLE);
        props.setLook(wReplayDate);
        wReplayDate.setToolTipText(Messages.getString("JobExecutionConfigurationDialog.ReplayDate.Tooltip")); //$NON-NLS-1$ //$NON-NLS-2$
        FormData fdReplayDate = new FormData();
        fdReplayDate.left   = new FormAttachment(50, margin);
        fdReplayDate.right  = new FormAttachment(100, 0);
        fdReplayDate.top    = new FormAttachment(wLogLevel, margin);
        wReplayDate.setLayoutData(fdReplayDate);

        // Variables
        wlVariables = new Label(shell, SWT.LEFT);
        props.setLook(wlVariables);
        wlVariables.setText(Messages.getString("JobExecutionConfigurationDialog.Variables.Label")); //$NON-NLS-1$
        wlVariables.setToolTipText(Messages.getString("JobExecutionConfigurationDialog.Variables.Tooltip")); //$NON-NLS-1$ //$NON-NLS-2$
        FormData fdlVariables = new FormData();
        fdlVariables.left   = new FormAttachment(50, margin);
        fdlVariables.right  = new FormAttachment(100, 0);
        fdlVariables.top    = new FormAttachment(gDetails, margin*2);
        wlVariables.setLayoutData(fdlVariables);

        ColumnInfo[] cVariables = {
            new ColumnInfo( Messages.getString("JobExecutionConfigurationDialog.VariablesColumn.Argument"), ColumnInfo.COLUMN_TYPE_TEXT, false, false), //Stepname
            new ColumnInfo( Messages.getString("JobExecutionConfigurationDialog.VariablesColumn.Value"), ColumnInfo.COLUMN_TYPE_TEXT, false, false), //Preview size
          };
                      
        int nrVariables = configuration.getVariables() !=null ? configuration.getVariables().size() : 0; 
        wVariables = new TableView(jobMeta, shell, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, cVariables, nrVariables, false, null, props);
        FormData fdVariables = new FormData();
        fdVariables.left   = new FormAttachment(50, margin);
        fdVariables.right  = new FormAttachment(100, 0);
        fdVariables.top    = new FormAttachment(wlVariables, margin);
        fdVariables.bottom = new FormAttachment(wOK, -margin*2);
        wVariables.setLayoutData(fdVariables);        

        // Arguments
        wlArguments = new Label(shell, SWT.LEFT);
        props.setLook(wlArguments);
        wlArguments.setText(Messages.getString("JobExecutionConfigurationDialog.Arguments.Label")); //$NON-NLS-1$
        wlArguments.setToolTipText(Messages.getString("JobExecutionConfigurationDialog.Arguments.Tooltip")); //$NON-NLS-1$ //$NON-NLS-2$
        FormData fdlArguments = new FormData();
        fdlArguments.left   = new FormAttachment(0, 0);
        fdlArguments.right  = new FormAttachment(50, -margin);
        fdlArguments.top    = new FormAttachment(wVariables, 0, SWT.CENTER);
        wlArguments.setLayoutData(fdlArguments);

        ColumnInfo[] cArguments = {
            new ColumnInfo( Messages.getString("JobExecutionConfigurationDialog.ArgumentsColumn.Argument"), ColumnInfo.COLUMN_TYPE_TEXT, false, true ), //Stepname
            new ColumnInfo( Messages.getString("JobExecutionConfigurationDialog.ArgumentsColumn.Value"), ColumnInfo.COLUMN_TYPE_TEXT, false, false), //Preview size
          };
              
        int nrArguments = configuration.getArguments() !=null ? configuration.getArguments().size() : 10; 
        wArguments = new TableView(jobMeta, shell, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, cArguments, nrArguments, false, null, props);
        FormData fdArguments = new FormData();
        fdArguments.left   = new FormAttachment(0, 0);
        fdArguments.right  = new FormAttachment(50, -margin);
        fdArguments.top    = new FormAttachment(wlArguments, margin);
        fdArguments.bottom = new FormAttachment(wOK, -margin*2);
        wArguments.setLayoutData(fdArguments);
        
        
        // Named parameters
        wlParams = new Label(shell, SWT.LEFT);
        props.setLook(wlParams);
        wlParams.setText(Messages.getString("JobExecutionConfigurationDialog.Params.Label")); //$NON-NLS-1$
        wlParams.setToolTipText(Messages.getString("JobExecutionConfigurationDialog.Params.Tooltip")); //$NON-NLS-1$ //$NON-NLS-2$
        FormData fdlParams = new FormData();
        fdlParams.left   = new FormAttachment(0, 0);
        fdlParams.right  = new FormAttachment(50, -margin);
        fdlParams.top    = new FormAttachment(gDetails, margin*2);
        wlParams.setLayoutData(fdlParams);

        ColumnInfo[] cParams = {
            new ColumnInfo( Messages.getString("JobExecutionConfigurationDialog.ParamsColumn.Argument"), ColumnInfo.COLUMN_TYPE_TEXT, false, true ), // Argument
            new ColumnInfo( Messages.getString("JobExecutionConfigurationDialog.ParamsColumn.Value"), ColumnInfo.COLUMN_TYPE_TEXT, false, false), // Actual value
            new ColumnInfo( Messages.getString("JobExecutionConfigurationDialog.ParamsColumn.Default"), ColumnInfo.COLUMN_TYPE_TEXT, false, true), // Default value
          };
              
        String[] namedParams = jobMeta.listParameters();
        int nrParams = namedParams.length; 
        wParams = new TableView(jobMeta, shell, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, cParams, nrParams, true, null, props);
        FormData fdParams = new FormData();
        fdParams.left   = new FormAttachment(0, 0);
        fdParams.right  = new FormAttachment(50, -margin);
        fdParams.top    = new FormAttachment(wlParams, margin);        
        fdParams.bottom = new FormAttachment(wlArguments, -margin * 2);
        wParams.setLayoutData(fdParams);                        

        getData();
        
        BaseStepDialog.setSize(shell);
        
        // Set the focus on the OK button
        wOK.setFocus();
        
        shell.open();
        while (!shell.isDisposed())
        {
            if (!display.readAndDispatch()) display.sleep();
        }
        
        return retval;
    }

    private void getParamsData()
    {
        wParams.clearAll(false);
        List<String> paramNames = new ArrayList<String>( configuration.getParams().keySet() );
        Collections.sort(paramNames);
        
        for (int i=0;i<paramNames.size();i++)
        {
        	String paramName = paramNames.get(i);
        	String paramValue = configuration.getParams().get(paramName);
        	
        	String defaultValue;
			try {
				defaultValue = jobMeta.getParameterDefault(paramName);
			} catch (UnknownParamException e) {
				defaultValue = "";
			}
        	
            TableItem tableItem = new TableItem(wParams.table, SWT.NONE);
            tableItem.setText(1, paramName);
            tableItem.setText(2, Const.NVL(paramValue, ""));
            tableItem.setText(3, Const.NVL(defaultValue, ""));
        }
        wParams.removeEmptyRows();
        wParams.setRowNums();
        wParams.optWidth(true);
    }    
    
    
    private void getVariablesData()
    {
        wVariables.clearAll(false);
        List<String> variableNames = new ArrayList<String>( configuration.getVariables().keySet() );
        Collections.sort(variableNames);
        
        List<String> paramNames = new ArrayList<String>( configuration.getParams().keySet() );
        
        for (int i=0;i<variableNames.size();i++)
        {
        	String variableName = variableNames.get(i);
        	String variableValue = configuration.getVariables().get(variableName);
        	
        	if ( ! paramNames.contains(variableName) )  {
        		//
        		// Do not put the parameters among the variables.
        		//
                TableItem tableItem = new TableItem(wVariables.table, SWT.NONE);
                tableItem.setText(1, variableName);
                tableItem.setText(2, Const.NVL(variableValue, ""));
        	}
        }
        wVariables.removeEmptyRows();
        wVariables.setRowNums();
        wVariables.optWidth(true);
    }

    private void getArgumentsData()
    {
        wArguments.clearAll(false);
        
        List<String> argumentNames = new ArrayList<String>( configuration.getArguments().keySet() );
        Collections.sort(argumentNames);
        
        for (int i=0;i<10;i++)
        {
        	String argumentName = new DecimalFormat("00").format(i+1); 
        	String argumentValue = configuration.getArguments().get(argumentName);
        	
            TableItem tableItem = new TableItem(wArguments.table, SWT.NONE);
            tableItem.setText(1, Const.NVL(argumentName, ""));
            tableItem.setText(2, Const.NVL(argumentValue, ""));
        }
                
        wArguments.removeEmptyRows();
        wArguments.setRowNums();
        wArguments.optWidth(true);
    }

    private void cancel()
    {
        dispose();
    }
    
    private void dispose()
    {
        props.setScreen(new WindowProperty(shell));
        shell.dispose();
    }

    private void ok()
    {
    	if (Const.isOSX())
    	{
    		// OSX bug workaround.
    		//
    		wParams.applyOSXChanges();
    		wVariables.applyOSXChanges();
    		wArguments.applyOSXChanges();
    	}
        getInfo();
        retval=true;
        dispose();
    }
    
    public void getData()
    {
        wExecLocal.setSelection(configuration.isExecutingLocally());
        wExecRemote.setSelection(configuration.isExecutingRemotely());
        wSafeMode.setSelection(configuration.isSafeModeEnabled());
        wClearLog.setSelection(configuration.isClearingLog());
        wRemoteHost.setText( configuration.getRemoteServer()==null ? "" : configuration.getRemoteServer().toString() );
        wPassExport.setSelection(configuration.isPassingExport());
        int logIndex = wLogLevel.indexOf(LogWriter.getInstance().getLogLevelLongDesc());
        if (logIndex>=0) wLogLevel.select( logIndex );
        else wLogLevel.setText(LogWriter.getInstance().getLogLevelLongDesc());
        if (configuration.getReplayDate()!=null) wReplayDate.setText(simpleDateFormat.format(configuration.getReplayDate()));

        getParamsData();
        getArgumentsData();
        getVariablesData();
        
        enableFields();
    }
    
    public void getInfo()
    {
        try
        {
            configuration.setExecutingLocally(wExecLocal.getSelection());
            configuration.setExecutingRemotely(wExecRemote.getSelection());
            
            // Remote data
            //
            if (wExecRemote.getSelection())
            {
                String serverName = wRemoteHost.getText();
                configuration.setRemoteServer(jobMeta.findSlaveServer(serverName));
            }
            configuration.setPassingExport(wPassExport.getSelection());
            
            // various settings
            //
            if (!Const.isEmpty(wReplayDate.getText()))
            {
                configuration.setReplayDate(simpleDateFormat.parse(wReplayDate.getText()));
            }
            else
            {
                configuration.setReplayDate(null);
            }
            configuration.setSafeModeEnabled(wSafeMode.getSelection() );
            configuration.setClearingLog(wClearLog.getSelection());
            configuration.setLogLevel( LogWriter.getLogLevel(wLogLevel.getText()) );
            
            // The lower part of the dialog...
            getInfoParameters();
            getInfoVariables();
            getInfoArguments();
        }
        catch(Exception e)
        {
            new ErrorDialog(shell, "Error in settings", "There is an error in the dialog settings", e);
        }
    }
    
    /**
     * Get the parameters from the dialog.
     */
    private void getInfoParameters()
    {
        Map<String,String> map = new HashMap<String, String>();
    	int nrNonEmptyVariables = wParams.nrNonEmpty(); 
        for (int i=0;i<nrNonEmptyVariables;i++)
        {
            TableItem tableItem = wParams.getNonEmpty(i);
            String paramName = tableItem.getText(1);
            String paramValue = tableItem.getText(2);
            String defaultValue = tableItem.getText(3);
            
            if ( Const.isEmpty(paramValue) )  {
            	paramValue = Const.NVL(defaultValue, "");
            }
            
            map.put(paramName, paramValue);            
        }
        configuration.setParams(map);
    }    
    
    private void getInfoVariables()
    {
        Map<String,String> map = new HashMap<String, String>();
    	int nrNonEmptyVariables = wVariables.nrNonEmpty(); 
        for (int i=0;i<nrNonEmptyVariables;i++)
        {
            TableItem tableItem = wVariables.getNonEmpty(i);
            String varName = tableItem.getText(1);
            String varValue = tableItem.getText(2);
            
            if (!Const.isEmpty(varName))
            {
                map.put(varName, varValue);
            }
        }
        configuration.setVariables(map);
    }
    
    private void getInfoArguments()
    {
    	Map<String,String> map = new HashMap<String, String>();
    	int nrNonEmptyArguments = wArguments.nrNonEmpty(); 
    	for (int i=0;i<nrNonEmptyArguments;i++)
        {
            TableItem tableItem = wArguments.getNonEmpty(i);
            String varName = tableItem.getText(1);
            String varValue = tableItem.getText(2);
            
            if (!Const.isEmpty(varName))
            {
                map.put(varName, varValue);
            }
        }
        configuration.setArguments(map);
    }
    
    private void enableFields()
    {
        boolean enableRemote = wExecRemote.getSelection();
                
        wRemoteHost.setEnabled(enableRemote);
        wlRemoteHost.setEnabled(enableRemote);
        wPassExport.setEnabled(enableRemote);
    }
    
    /**
     * @return the configuration
     */
    public JobExecutionConfiguration getConfiguration()
    {
        return configuration;
    }

    /**
     * @param configuration the configuration to set
     */
    public void setConfiguration(JobExecutionConfiguration configuration)
    {
        this.configuration = configuration;
    }
}