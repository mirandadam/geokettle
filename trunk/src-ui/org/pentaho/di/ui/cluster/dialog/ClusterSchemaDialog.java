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
package org.pentaho.di.ui.cluster.dialog;

import java.util.List;

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
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.cluster.ClusterSchema;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.Const;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.dialog.EnterSelectionDialog;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

/**
 * 
 * Dialog that allows you to edit the settings of the cluster schema
 * 
 * @see ClusterSchema
 * @author Matt
 * @since 17-11-2006
 *
 */

public class ClusterSchemaDialog extends Dialog 
{
	// private static LogWriter log = LogWriter.getInstance();
	
	private ClusterSchema clusterSchema;
	
	private Shell     shell;

    // Name
	private Text     wName;

    // Servers
    private TableView     wServers;

	private Button    wOK, wCancel;
	
    private ModifyListener lsMod;

	private PropsUI     props;

    private int middle;
    private int margin;

    private ClusterSchema originalSchema;
    private boolean ok;

    private Button wSelect;

    private TextVar wPort;

    private TextVar wBufferSize;

    private TextVar wFlushInterval;

    private Button wCompressed;

    private Button wDynamic;

    private List<SlaveServer> slaveServers;
    
	public ClusterSchemaDialog(Shell par, ClusterSchema clusterSchema, List<SlaveServer> slaveServers)
	{
		super(par, SWT.NONE);
		this.clusterSchema=(ClusterSchema) clusterSchema.clone();
        this.originalSchema=clusterSchema;
        this.slaveServers = slaveServers;
                
		props=PropsUI.getInstance();
        ok=false;
	}
	
	public boolean open() 
	{
		Shell parent = getParent();
		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN );
 		props.setLook(shell);
		shell.setImage( GUIResource.getInstance().getImageCluster());

		lsMod = new ModifyListener() 
		{
			public void modifyText(ModifyEvent e) 
			{
				clusterSchema.setChanged();
			}
		};

		middle = props.getMiddlePct();
		margin = Const.MARGIN;

		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;
		
		shell.setText(Messages.getString("ClusterSchemaDialog.Shell.Title")); //$NON-NLS-1$
		shell.setLayout (formLayout);
 		
		// First, add the buttons...
		
		// Buttons
		wOK     = new Button(shell, SWT.PUSH); 
		wOK.setText(Messages.getString("System.Button.OK")); //$NON-NLS-1$

		wCancel = new Button(shell, SWT.PUSH); 
		wCancel.setText(Messages.getString("System.Button.Cancel")); //$NON-NLS-1$

		Button[] buttons = new Button[] { wOK, wCancel };
		BaseStepDialog.positionBottomButtons(shell, buttons, margin, null);
		
		// The rest stays above the buttons, so we added those first...
        
        // What's the schema name??
        Label wlName = new Label(shell, SWT.RIGHT); 
        props.setLook(wlName);
        wlName.setText(Messages.getString("ClusterSchemaDialog.Schema.Label")); //$NON-NLS-1$
        FormData fdlName = new FormData();
        fdlName.top   = new FormAttachment(0, 0);
        fdlName.left  = new FormAttachment(0, 0);  // First one in the left top corner
        fdlName.right = new FormAttachment(middle, 0);
        wlName.setLayoutData(fdlName);

        wName = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
        props.setLook(wName);
        wName.addModifyListener(lsMod);
        FormData fdName = new FormData();
        fdName.top  = new FormAttachment(0, 0);
        fdName.left = new FormAttachment(middle, margin); // To the right of the label
        fdName.right= new FormAttachment(95, 0);
        wName.setLayoutData(fdName);
        
        // What's the base port??
        Label wlPort = new Label(shell, SWT.RIGHT); 
        props.setLook(wlPort);
        wlPort.setText(Messages.getString("ClusterSchemaDialog.Port.Label")); //$NON-NLS-1$
        FormData fdlPort = new FormData();
        fdlPort.top   = new FormAttachment(wName, margin);
        fdlPort.left  = new FormAttachment(0, 0);  // First one in the left top corner
        fdlPort.right = new FormAttachment(middle, 0);
        wlPort.setLayoutData(fdlPort);

        wPort = new TextVar(clusterSchema, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
        props.setLook(wPort);
        wPort.addModifyListener(lsMod);
        FormData fdPort = new FormData();
        fdPort.top  = new FormAttachment(wName, margin);
        fdPort.left = new FormAttachment(middle, margin); // To the right of the label
        fdPort.right= new FormAttachment(95, 0);
        wPort.setLayoutData(fdPort);

        
        // What are the sockets buffer sizes??
        Label wlBufferSize = new Label(shell, SWT.RIGHT); 
        props.setLook(wlBufferSize);
        wlBufferSize.setText(Messages.getString("ClusterSchemaDialog.SocketBufferSize.Label")); //$NON-NLS-1$
        FormData fdlBufferSize = new FormData();
        fdlBufferSize.top   = new FormAttachment(wPort, margin);
        fdlBufferSize.left  = new FormAttachment(0, 0);  // First one in the left top corner
        fdlBufferSize.right = new FormAttachment(middle, 0);
        wlBufferSize.setLayoutData(fdlBufferSize);

        wBufferSize = new TextVar(clusterSchema,shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
        props.setLook(wBufferSize);
        wBufferSize.addModifyListener(lsMod);
        FormData fdBufferSize = new FormData();
        fdBufferSize.top  = new FormAttachment(wPort, margin);
        fdBufferSize.left = new FormAttachment(middle, margin); // To the right of the label
        fdBufferSize.right= new FormAttachment(95, 0);
        wBufferSize.setLayoutData(fdBufferSize);

        // What are the sockets buffer sizes??
        Label wlFlushInterval = new Label(shell, SWT.RIGHT); 
        props.setLook(wlFlushInterval);
        wlFlushInterval.setText(Messages.getString("ClusterSchemaDialog.SocketFlushRows.Label"));   //$NON-NLS-1$
        FormData fdlFlushInterval = new FormData();
        fdlFlushInterval.top   = new FormAttachment(wBufferSize, margin);
        fdlFlushInterval.left  = new FormAttachment(0, 0);  // First one in the left top corner
        fdlFlushInterval.right = new FormAttachment(middle, 0);
        wlFlushInterval.setLayoutData(fdlFlushInterval);

        wFlushInterval = new TextVar(clusterSchema, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
        props.setLook(wFlushInterval);
        wFlushInterval.addModifyListener(lsMod);
        FormData fdFlushInterval = new FormData();
        fdFlushInterval.top  = new FormAttachment(wBufferSize, margin);
        fdFlushInterval.left = new FormAttachment(middle, margin); // To the right of the label
        fdFlushInterval.right= new FormAttachment(95, 0);
        wFlushInterval.setLayoutData(fdFlushInterval);

        // What are the sockets buffer sizes??
        Label wlCompressed = new Label(shell, SWT.RIGHT); 
        props.setLook(wlCompressed);
        wlCompressed.setText(Messages.getString("ClusterSchemaDialog.SocketDataCompressed.Label")); //$NON-NLS-1$
        FormData fdlCompressed = new FormData();
        fdlCompressed.top   = new FormAttachment(wFlushInterval, margin);
        fdlCompressed.left  = new FormAttachment(0, 0);  // First one in the left top corner
        fdlCompressed.right = new FormAttachment(middle, 0);
        wlCompressed.setLayoutData(fdlCompressed);

        wCompressed = new Button(shell, SWT.CHECK );
        props.setLook(wCompressed);
        FormData fdCompressed = new FormData();
        fdCompressed.top  = new FormAttachment(wFlushInterval, margin);
        fdCompressed.left = new FormAttachment(middle, margin); // To the right of the label
        fdCompressed.right= new FormAttachment(95, 0);
        wCompressed.setLayoutData(fdCompressed);

        // What are the sockets buffer sizes??
        Label wlDynamic = new Label(shell, SWT.RIGHT); 
        wlDynamic.setToolTipText(Messages.getString("ClusterSchemaDialog.DynamicCluster.Tooltip")); //$NON-NLS-1$
        props.setLook(wlDynamic);
        wlDynamic.setText(Messages.getString("ClusterSchemaDialog.DynamicCluster.Label")); //$NON-NLS-1$
        FormData fdlDynamic = new FormData();
        fdlDynamic.top   = new FormAttachment(wCompressed, margin);
        fdlDynamic.left  = new FormAttachment(0, 0);  // First one in the left top corner
        fdlDynamic.right = new FormAttachment(middle, 0);
        wlDynamic.setLayoutData(fdlDynamic);

        wDynamic = new Button(shell, SWT.CHECK );
        wDynamic.setToolTipText(Messages.getString("ClusterSchemaDialog.DynamicCluster.Tooltip")); //$NON-NLS-1$
        props.setLook(wDynamic);
        FormData fdDynamic = new FormData();
        fdDynamic.top  = new FormAttachment(wCompressed, margin);
        fdDynamic.left = new FormAttachment(middle, margin); // To the right of the label
        fdDynamic.right= new FormAttachment(95, 0);
        wDynamic.setLayoutData(fdDynamic);
        
        // Schema servers:
        Label wlServers = new Label(shell, SWT.RIGHT);
        wlServers.setText(Messages.getString("ClusterSchemaDialog.SlaveServers.Label")); //$NON-NLS-1$
        props.setLook(wlServers);
        FormData fdlServers=new FormData();
        fdlServers.left = new FormAttachment(0, 0);
        fdlServers.right = new FormAttachment(middle, 0);
        fdlServers.top  = new FormAttachment(wDynamic, margin);
        wlServers.setLayoutData(fdlServers);
        
        // Some buttons to manage...
        wSelect = new Button(shell, SWT.PUSH);
        wSelect.setText(Messages.getString("ClusterSchemaDialog.SelectSlaveServers.Label")); //$NON-NLS-1$
        props.setLook(wSelect);
        FormData fdSelect=new FormData();
        fdSelect.right= new FormAttachment(100, 0);
        fdSelect.top  = new FormAttachment(wlServers, 5*margin);
        wSelect.setLayoutData(fdSelect);
        wSelect.addSelectionListener(new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { selectSlaveServers(); }});

        ColumnInfo[] partitionColumns = new ColumnInfo[] { 
                new ColumnInfo( Messages.getString("ClusterSchemaDialog.ColumnInfoName.Label"), ColumnInfo.COLUMN_TYPE_TEXT, true, false), //$NON-NLS-1$
                new ColumnInfo( Messages.getString("ClusterSchemaDialog.ColumnInfoServiceURL.Label"), ColumnInfo.COLUMN_TYPE_TEXT, true, true), //$NON-NLS-1$
                new ColumnInfo( Messages.getString("ClusterSchemaDialog.ColumnInfoMaster.Label"), ColumnInfo.COLUMN_TYPE_TEXT, true, true), //$NON-NLS-1$
        };
        wServers = new TableView(clusterSchema, shell, SWT.BORDER | SWT.FULL_SELECTION | SWT.SINGLE, partitionColumns, 1, lsMod, props);
        wServers.setReadonly(false);
        props.setLook(wServers);
        FormData fdServers = new FormData();
        fdServers.left = new FormAttachment(middle, margin );
        fdServers.right = new FormAttachment(wSelect, -2*margin);
        fdServers.top = new FormAttachment(wDynamic, margin);
        fdServers.bottom = new FormAttachment(wOK, -margin * 2);
        wServers.setLayoutData(fdServers);
        wServers.table.addSelectionListener(new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { editSlaveServer(); }});
		
		// Add listeners
		wOK.addListener(SWT.Selection, new Listener () { public void handleEvent (Event e) { ok(); } } );
        wCancel.addListener(SWT.Selection, new Listener () { public void handleEvent (Event e) { cancel(); } } );
		
        SelectionAdapter selAdapter=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
		wName.addSelectionListener(selAdapter);
        wPort.addSelectionListener(selAdapter);
        wBufferSize.addSelectionListener(selAdapter);
        wFlushInterval.addSelectionListener(selAdapter);

		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );
	
		getData();

		BaseStepDialog.setSize(shell);
		
		shell.open();
		Display display = parent.getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) display.sleep();
		}
		return ok;
	}
	
    private void editSlaveServer()
    {
        int idx = wServers.getSelectionIndex();
        if (idx>=0)
        {
            SlaveServer slaveServer = clusterSchema.findSlaveServer(wServers.getItems(0)[idx]);
            if (slaveServer!=null)
            {
                SlaveServerDialog dialog = new SlaveServerDialog(shell, slaveServer);
                if (dialog.open())
                {
                    refreshSlaveServers();
                }
            }
        }
    }

    private void selectSlaveServers()
    {
        String[] names = SlaveServer.getSlaveServerNames(slaveServers);
        int idx[] = Const.indexsOfFoundStrings(wServers.getItems(0), names);
        
        EnterSelectionDialog dialog = new EnterSelectionDialog(shell, names, Messages.getString("ClusterSchemaDialog.SelectServers.Label"),  //$NON-NLS-1$
						Messages.getString("ClusterSchemaDialog.SelectServersCluster.Label")); //$NON-NLS-1$
        dialog.setSelectedNrs(idx);
        dialog.setMulti(true);
        if (dialog.open()!=null)
        {
            clusterSchema.getSlaveServers().clear();
            int[] indeces = dialog.getSelectionIndeces();
            for (int i=0;i<indeces.length;i++)
            {
                SlaveServer slaveServer = SlaveServer.findSlaveServer(slaveServers, names[indeces[i]]);
                clusterSchema.getSlaveServers().add(slaveServer);
            }
            
            refreshSlaveServers();
        }
    }

    public void dispose()
	{
		props.setScreen(new WindowProperty(shell));
		shell.dispose();
	}
    
    public void getData()
	{
		wName.setText( Const.NVL(clusterSchema.getName(), "") ); //$NON-NLS-1$
		wPort.setText( Const.NVL(clusterSchema.getBasePort(), "")); //$NON-NLS-1$
        wBufferSize.setText( Const.NVL(clusterSchema.getSocketsBufferSize(), "")); //$NON-NLS-1$
        wFlushInterval.setText( Const.NVL(clusterSchema.getSocketsFlushInterval(), "")); //$NON-NLS-1$
        wCompressed.setSelection( clusterSchema.isSocketsCompressed());
        wDynamic.setSelection( clusterSchema.isDynamic());
        
        refreshSlaveServers();
        
		wName.setFocus();
	}
    
	private void refreshSlaveServers()
    {
        wServers.clearAll(false);
        List<SlaveServer> slServers = clusterSchema.getSlaveServers();
        for (int i=0;i<slServers.size();i++)
        {
            TableItem item = new TableItem(wServers.table, SWT.NONE);
            SlaveServer slaveServer = slServers.get(i);
            item.setText(1, Const.NVL(slaveServer.getName(), "")); //$NON-NLS-1$
            item.setText(2, Const.NVL(slaveServer.toString(), "")); //$NON-NLS-1$
            item.setText(3, slaveServer.isMaster()?"Y":"N"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        wServers.removeEmptyRows();
        wServers.setRowNums();
        wServers.optWidth(true);
    }

    private void cancel()
	{
		originalSchema = null;
		dispose();
	}
	
	public void ok()
	{
        getInfo();
        originalSchema.setName(clusterSchema.getName());
        originalSchema.setBasePort(clusterSchema.getBasePort());
        originalSchema.setSocketsBufferSize(clusterSchema.getSocketsBufferSize());
        originalSchema.setSocketsFlushInterval(clusterSchema.getSocketsFlushInterval());
        originalSchema.setSocketsCompressed(clusterSchema.isSocketsCompressed());
        originalSchema.setDynamic(clusterSchema.isDynamic());
        originalSchema.setSlaveServers(clusterSchema.getSlaveServers());
        originalSchema.setChanged();

        ok=true;
        
        // Debug: dynamic lis names/urls of slaves on the console
        //
        /*
        if (originalSchema.isDynamic()) {
			// Find a master that is available
    		//
    		List<SlaveServer> dynamicSlaves = null;
    		for (SlaveServer slave : originalSchema.getSlaveServers()) {
    			if (slave.isMaster() && dynamicSlaves==null) {
    				try {
						List<SlaveServerDetection> detections = slave.getSlaveServerDetections();
						dynamicSlaves = new ArrayList<SlaveServer>();
						for (SlaveServerDetection detection : detections) {
							if (detection.isActive()) {
								dynamicSlaves.add(detection.getSlaveServer());
								log.logBasic(toString(), "Found dynamic slave : "+detection.getSlaveServer().getName()+" --> "+detection.getSlaveServer().getServerAndPort());
							}
						}
					} catch (Exception e) {
						log.logError(toString(), "Unable to contact master : "+slave.getName()+" --> "+slave.getServerAndPort(), e);
					}
    			}
    		}
        }
        */
        
        dispose();
	}
    
	private void getInfo()
    {
        clusterSchema.setName(wName.getText());
        clusterSchema.setBasePort(wPort.getText());
        clusterSchema.setSocketsBufferSize(wBufferSize.getText());
        clusterSchema.setSocketsFlushInterval(wFlushInterval.getText());
        clusterSchema.setSocketsCompressed(wCompressed.getSelection());
        clusterSchema.setDynamic(wDynamic.getSelection());

        String[] names = SlaveServer.getSlaveServerNames(slaveServers);
        int idx[] = Const.indexsOfFoundStrings(wServers.getItems(0), names);
        
        clusterSchema.getSlaveServers().clear();
        for (int i=0;i<idx.length;i++)
        {
            SlaveServer slaveServer = SlaveServer.findSlaveServer(slaveServers, names[idx[i]]);
            clusterSchema.getSlaveServers().add(slaveServer);
        }
            
    }
	
	@Override
	public String toString() {
		return getClass().getSimpleName();
	}
}