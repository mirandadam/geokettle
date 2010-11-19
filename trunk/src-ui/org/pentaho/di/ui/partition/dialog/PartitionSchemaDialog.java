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
package org.pentaho.di.ui.partition.dialog;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Image;
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
import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.database.PartitionDatabaseMeta;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.partition.PartitionSchema;
import org.pentaho.di.ui.partition.dialog.Messages;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.dialog.EnterSelectionDialog;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.core.widget.TextVar;




/**
 * 
 * Dialog that allows you to edit the settings of the cluster schema
 * 
 * @see ClusterSchema
 * @author Matt
 * @since 17-11-2006
 *
 */

public class PartitionSchemaDialog extends Dialog 
{
	private PartitionSchema partitionSchema;
	
	private Shell     shell;

    // Name
	private Text     wName;

	// Dynamic definition?
	private Button   wDynamic;
	private TextVar  wNumber;
	
    // Partitions
    private TableView wPartitions;
    
	private Button    wOK, wGet, wCancel;
	
    private ModifyListener lsMod;

	private PropsUI     props;

    private int middle;
    private int margin;

    private PartitionSchema originalSchema;
    private boolean ok;

    private List<DatabaseMeta> databases;

	private VariableSpace variableSpace;
    
	public PartitionSchemaDialog(Shell par, PartitionSchema partitionSchema, List<DatabaseMeta> databases, VariableSpace variableSpace)
	{
		super(par, SWT.NONE);
		this.partitionSchema=(PartitionSchema) partitionSchema.clone();
        this.originalSchema=partitionSchema;
        this.databases = databases;
        this.variableSpace = variableSpace;
        
		props=PropsUI.getInstance();
        ok=false;
	}
	
	public boolean open() 
	{
		Shell parent = getParent();
		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN );
 		props.setLook(shell);
		shell.setImage((Image) GUIResource.getInstance().getImageFolderConnections());
		
		lsMod = new ModifyListener() 
		{
			public void modifyText(ModifyEvent e) 
			{
				partitionSchema.setChanged();
			}
		};

		middle = props.getMiddlePct();
		margin = Const.MARGIN;

		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;
		
		shell.setText(Messages.getString("PartitionSchemaDialog.Shell.Title"));
		shell.setLayout (formLayout);
 		
		// First, add the buttons...
		
		// Buttons
		wOK     = new Button(shell, SWT.PUSH); 
		wOK.setText(" &OK ");

        wGet    = new Button(shell, SWT.PUSH); 
        wGet.setText(Messages.getString("PartitionSchema.ImportPartitions"));

		wCancel = new Button(shell, SWT.PUSH); 
		wCancel.setText(" &Cancel ");

		Button[] buttons = new Button[] { wOK, wGet, wCancel };
		BaseStepDialog.positionBottomButtons(shell, buttons, margin, null);
		
		// The rest stays above the buttons, so we added those first...
        
        // What's the schema name??
		//
        Label wlName = new Label(shell, SWT.RIGHT); 
        props.setLook(wlName);
        wlName.setText(Messages.getString("PartitionSchemaDialog.PartitionName.Label"));
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

        // is the schema defined dynamically using the number of slave servers in the used cluster.
        //
        Label wlDynamic = new Label(shell, SWT.RIGHT); 
        props.setLook(wlDynamic);
        wlDynamic.setText(Messages.getString("PartitionSchemaDialog.Dynamic.Label"));
        FormData fdlDynamic = new FormData();
        fdlDynamic.top   = new FormAttachment(wName, margin);
        fdlDynamic.left  = new FormAttachment(0, 0);  // First one in the left top corner
        fdlDynamic.right = new FormAttachment(middle, 0);
        wlDynamic.setLayoutData(fdlDynamic);

        wDynamic = new Button(shell, SWT.CHECK );
        props.setLook(wDynamic);
        wDynamic.setToolTipText(Messages.getString("PartitionSchemaDialog.Dynamic.Tooltip"));
        FormData fdDynamic = new FormData();
        fdDynamic.top  = new FormAttachment(wName, margin);
        fdDynamic.left = new FormAttachment(middle, margin); // To the right of the label
        fdDynamic.right= new FormAttachment(95, 0);
        wDynamic.setLayoutData(fdDynamic);

        // The number of partitions per cluster schema
        //
        Label wlNumber = new Label(shell, SWT.RIGHT); 
        props.setLook(wlNumber);
        wlNumber.setText(Messages.getString("PartitionSchemaDialog.Number.Label"));
        FormData fdlNumber = new FormData();
        fdlNumber.top   = new FormAttachment(wDynamic, margin);
        fdlNumber.left  = new FormAttachment(0, 0);  // First one in the left top corner
        fdlNumber.right = new FormAttachment(middle, 0);
        wlNumber.setLayoutData(fdlNumber);

        wNumber = new TextVar(variableSpace, shell, SWT.LEFT | SWT.BORDER | SWT.SINGLE, Messages.getString("PartitionSchemaDialog.Number.Tooltip") );
        props.setLook(wNumber);
        FormData fdNumber = new FormData();
        fdNumber.top  = new FormAttachment(wDynamic, margin);
        fdNumber.left = new FormAttachment(middle, margin); // To the right of the label
        fdNumber.right= new FormAttachment(95, 0);
        wNumber.setLayoutData(fdNumber);
        
        // Schema list:
        Label wlPartitions = new Label(shell, SWT.RIGHT);
        wlPartitions.setText(Messages.getString("PartitionSchemaDialog.Partitions.Label"));
        props.setLook(wlPartitions);
        FormData fdlPartitions=new FormData();
        fdlPartitions.left  = new FormAttachment(0, 0);
        fdlPartitions.right = new FormAttachment(middle, 0);
        fdlPartitions.top   = new FormAttachment(wNumber, margin);
        wlPartitions.setLayoutData(fdlPartitions);
        
        ColumnInfo[] partitionColumns=new ColumnInfo[] 
            {
                new ColumnInfo(Messages.getString("PartitionSchemaDialog.PartitionID.Label"), ColumnInfo.COLUMN_TYPE_TEXT, false, false),
            };
        wPartitions=new TableView(
        					  Variables.getADefaultVariableSpace(),  // probably better push this up. TODO
        		              shell, 
                              SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, 
                              partitionColumns, 
                              1,  
                              lsMod,
                              props
                              );
        props.setLook(wPartitions);
        FormData fdPartitions=new FormData();
        fdPartitions.left   = new FormAttachment(middle, margin);
        fdPartitions.right  = new FormAttachment(100, 0);
        fdPartitions.top    = new FormAttachment(wNumber, margin);
        fdPartitions.bottom = new FormAttachment(wOK, -margin*2);
        wPartitions.setLayoutData(fdPartitions);
        
		// Add listeners
		wOK.addListener(SWT.Selection, new Listener () { public void handleEvent (Event e) { ok(); } } );
        wGet.addListener(SWT.Selection, new Listener () { public void handleEvent (Event e) { importPartitions(); } } );
        wCancel.addListener(SWT.Selection, new Listener () { public void handleEvent (Event e) { cancel(); } } );
		
        SelectionAdapter selAdapter=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
		wName.addSelectionListener(selAdapter);

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
	

    public void dispose()
	{
		props.setScreen(new WindowProperty(shell));
		shell.dispose();
	}
    
    public void getData()
	{
		wName.setText( Const.NVL(partitionSchema.getName(), "") );

        refreshPartitions();
        
        wDynamic.setSelection( partitionSchema.isDynamicallyDefined() );
        wNumber.setText( Const.NVL(partitionSchema.getNumberOfPartitionsPerSlave(), "") );
        
		wName.setFocus();
	}
    
	private void refreshPartitions()
    {
        wPartitions.clearAll(false);
        List<String> partitionIDs = partitionSchema.getPartitionIDs();
        for (int i=0;i<partitionIDs.size();i++)
        {
            TableItem item = new TableItem(wPartitions.table, SWT.NONE);
            item.setText( 1, partitionIDs.get(i) );
        }
        wPartitions.removeEmptyRows();
        wPartitions.setRowNums();
        wPartitions.optWidth(true);
    }

    private void cancel()
	{
		originalSchema = null;
		dispose();
	}
	
	public void ok()
	{
        getInfo();
        originalSchema.setName( partitionSchema.getName() );
        originalSchema.setPartitionIDs( partitionSchema.getPartitionIDs() );
        originalSchema.setDynamicallyDefined( wDynamic.getSelection() );
        originalSchema.setNumberOfPartitionsPerSlave( wNumber.getText() );
        originalSchema.setChanged();

        ok=true;
        
        dispose();
	}
    
    // Get dialog info in partition schema meta-data
	// 
	private void getInfo()
    {
        partitionSchema.setName(wName.getText());
        
        List<String> parts = new ArrayList<String>();

        int nrNonEmptyPartitions = wPartitions.nrNonEmpty(); 
        for (int i=0;i<nrNonEmptyPartitions;i++)
        {
            parts.add( wPartitions.getNonEmpty(i).getText(1) );
        }
        partitionSchema.setPartitionIDs(parts);
    }
    
    protected void importPartitions()
    {
        List<String> partitionedDatabaseNames = new ArrayList<String>();
        
        for (int i=0;i<databases.size();i++)
        {
            DatabaseMeta databaseMeta = (DatabaseMeta) databases.get(i); 
            if (databaseMeta.isPartitioned())
            {
                partitionedDatabaseNames.add(databaseMeta.getName());
            }
        }
        String dbNames[] = (String[]) partitionedDatabaseNames.toArray(new String[partitionedDatabaseNames.size()]);
        
        if (dbNames.length>0)
        {
            EnterSelectionDialog dialog = new EnterSelectionDialog(shell, dbNames, Messages.getString("PartitionSchema.SelectDatabase"), 
						Messages.getString("PartitionSchema.SelectPartitionnedDatabase"));
            String dbName = dialog.open();
            if (dbName!=null)
            {
                DatabaseMeta databaseMeta = DatabaseMeta.findDatabase(databases, dbName);
                PartitionDatabaseMeta[] partitioningInformation = databaseMeta.getPartitioningInformation();
                if (partitioningInformation!=null)
                {
                    // Here we are...
                    wPartitions.clearAll(false);
                    
                    for (int i = 0; i < partitioningInformation.length; i++)
                    {
                        PartitionDatabaseMeta meta = partitioningInformation[i];
                        wPartitions.add(new String[] { meta.getPartitionId() } );
                    }
                    
                    wPartitions.removeEmptyRows();
                    wPartitions.setRowNums();
                    wPartitions.optWidth(true);
                }
            }
        }   
    }    
}