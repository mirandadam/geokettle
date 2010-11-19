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

package org.pentaho.di.ui.spoon.dialog;
import org.eclipse.swt.SWT;
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
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.Const;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.ui.spoon.dialog.Messages;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.dialog.PreviewRowsDialog;


/**
 * Shows a dialog that allows you to select the steps you want to preview by entering a number of rows.
 *  
 * @author Matt
 *
 */
public class EnterPreviewRowsDialog extends Dialog
{
	private String       stepname;
		
	private Label        wlStepList;
	private List wStepList;
    private FormData     fdlStepList, fdStepList;
	
	private Button wShow, wClose;
	private Listener lsShow, lsClose;

	private Shell         shell;
	private java.util.List<String> stepNames;
	private java.util.List<RowMetaInterface> rowMetas;
	private java.util.List<java.util.List<Object[]>> rowDatas;
	private PropsUI 		  props;

	public EnterPreviewRowsDialog(Shell parent, int style, java.util.List<String> stepNames, java.util.List<RowMetaInterface> rowMetas, java.util.List<java.util.List<Object[]>> rowBuffers)
	{
		super(parent, style);
		this.stepNames=stepNames;
		this.rowDatas=rowBuffers;
        this.rowMetas = rowMetas;
		props=PropsUI.getInstance();
	}

	public Object open()
	{
		Shell parent = getParent();
		Display display = parent.getDisplay();

		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX);
 		props.setLook(shell);

		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText(Messages.getString("EnterPreviewRowsDialog.Dialog.PreviewStep.Title")); //Select the preview step:
		shell.setImage(GUIResource.getInstance().getImageLogoSmall());

		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Filename line
		wlStepList=new Label(shell, SWT.NONE);
		wlStepList.setText(Messages.getString("EnterPreviewRowsDialog.Dialog.PreviewStep.Message")); //Step name : 
 		props.setLook(wlStepList);
		fdlStepList=new FormData();
		fdlStepList.left = new FormAttachment(0, 0);
		fdlStepList.top  = new FormAttachment(0, margin);
		wlStepList.setLayoutData(fdlStepList);
		wStepList=new List(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER | SWT.READ_ONLY | SWT.V_SCROLL | SWT.H_SCROLL);
		for (int i=0;i<stepNames.size();i++)
		{
			wStepList.add((String)stepNames.get(i)); 
		}
		wStepList.select(0);
 		props.setLook(wStepList);
		fdStepList=new FormData();
		fdStepList.left   = new FormAttachment(middle, 0);
		fdStepList.top    = new FormAttachment(0, margin);
		fdStepList.bottom = new FormAttachment(100, -60);
		fdStepList.right  = new FormAttachment(100, 0);
		wStepList.setLayoutData(fdStepList);
		wStepList.addSelectionListener(new SelectionAdapter()
		{
			public void widgetDefaultSelected(SelectionEvent arg0)
			{
				show();
			}
		});

		wShow=new Button(shell, SWT.PUSH);
		wShow.setText(Messages.getString("System.Button.Show"));

		wClose=new Button(shell, SWT.PUSH);
		wClose.setText(Messages.getString("System.Button.Close"));

		BaseStepDialog.positionBottomButtons(shell, new Button[] { wShow, wClose }, margin, null);
		// Add listeners
		lsShow       = new Listener() { public void handleEvent(Event e) { show();     } };
		lsClose   = new Listener() { public void handleEvent(Event e) { close(); } };

		wShow.addListener (SWT.Selection, lsShow    );
		wClose.addListener(SWT.Selection, lsClose    );
		
		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { close(); } } );

		getData();

		BaseStepDialog.setSize(shell);

		// Immediately show the only preview entry
		if (stepNames.size()==1)
		{
			wStepList.select(0);
			show();
		}
		
		shell.open();
		while (!shell.isDisposed())
		{
				if (!display.readAndDispatch()) display.sleep();
		}
		return stepname;
	}

	public void dispose()
	{
		props.setScreen(new WindowProperty(shell));
		shell.dispose();
	}
	
	/**
	 * Copy information from the meta-data input to the dialog fields.
	 */ 
	public void getData()
	{	
	}

	private void close()
	{
		dispose();
	}
	
	private void show()
	{
		if (rowDatas.size()==0) return;
		
		int nr = wStepList.getSelectionIndex();

		java.util.List<Object[]> buffer = (java.util.List<Object[]>)rowDatas.get(nr);
        RowMetaInterface rowMeta = (RowMetaInterface)rowMetas.get(nr);
		String    name   = (String)stepNames.get(nr);
		
        if (rowMeta!=null && buffer!=null && buffer.size()>0)
        {
    		PreviewRowsDialog prd = new PreviewRowsDialog(shell,
    										Variables.getADefaultVariableSpace(),
    				                        SWT.NONE, name, rowMeta, buffer);
    		prd.open();
        }
        else
        {
        	MessageBox mb = new MessageBox(shell, SWT.ICON_INFORMATION | SWT.OK);
        	mb.setText(Messages.getString("EnterPreviewRowsDialog.Dialog.NoPreviewRowsFound.Title"));
        	mb.setMessage(Messages.getString("EnterPreviewRowsDialog.Dialog.NoPreviewRowsFound.Message"));
        	mb.open();
        }
	}
}
