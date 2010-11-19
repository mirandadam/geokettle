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
package org.pentaho.di.ui.core.dialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.ui.core.dialog.Messages;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.ui.core.PropsUI;

/**
 * This dialog allows you to enter a number.
 * 
 * @author Matt
 * @since 19-06-2003
 */
public class EnterNumberDialog extends Dialog
{
    private Label wlNumber;
    private Text wNumber;
    private FormData fdlNumber, fdNumber;

    private Button wOK, wCancel;
    private Listener lsOK, lsCancel;
    private boolean hideCancelButton;

    private Shell shell;
    private SelectionAdapter lsDef;

    private int samples;
    private String shellText;
    private String lineText;
    private PropsUI props;

    /**
     * @deprecated Use the CT without the <i>Props</i> parameter (at 2nd position)
     */
    public EnterNumberDialog(Shell parent, PropsUI props, int samples, String shellText, String lineText)
    {
        super(parent, SWT.NONE);
        this.props = props;
        this.samples = samples;
        this.shellText = shellText;
        this.lineText = lineText;
    }
    
    public EnterNumberDialog(Shell parent, int samples, String shellText, String lineText)
    {
        super(parent, SWT.NONE);
        this.props = PropsUI.getInstance();
        this.samples = samples;
        this.shellText = shellText;
        this.lineText = lineText;
    }

    public int open()
    {
        Shell parent = getParent();
        Display display = parent.getDisplay();

        shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.RESIZE);
        props.setLook(shell);
        shell.setImage(GUIResource.getInstance().getImageSpoon());

        FormLayout formLayout = new FormLayout ();
        formLayout.marginWidth = Const.FORM_MARGIN;
        formLayout.marginHeight = Const.FORM_MARGIN;

        shell.setLayout(formLayout);
        shell.setText(shellText);

        int length = Const.LENGTH;
        int margin = Const.MARGIN;

        // From step line
        wlNumber = new Label(shell, SWT.NONE);
        wlNumber.setText(lineText);
        props.setLook(wlNumber);
        fdlNumber = new FormData();
        fdlNumber.left = new FormAttachment(0, 0);
        fdlNumber.top = new FormAttachment(0, margin);
        wlNumber.setLayoutData(fdlNumber);
        wNumber = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        wNumber.setText("100");
        props.setLook(wNumber);
        fdNumber = new FormData();
        fdNumber.left = new FormAttachment(0, 0);
        fdNumber.top = new FormAttachment(wlNumber, margin);
        fdNumber.right = new FormAttachment(0, length);
        wNumber.setLayoutData(fdNumber);

        // Some buttons
        Button[] buttons=null;
        
        wOK = new Button(shell, SWT.PUSH);
        wOK.setText(Messages.getString("System.Button.OK"));
        if (!hideCancelButton)
        {
            wCancel = new Button(shell, SWT.PUSH);
            wCancel.setText(Messages.getString("System.Button.Cancel"));
            buttons = new Button[] { wOK, wCancel };
        }
        else
        {
            buttons = new Button[] { wOK };
        }

        BaseStepDialog.positionBottomButtons(shell, buttons, margin, wNumber);

        // Add listeners
        lsOK       = new Listener() { public void handleEvent(Event e) { ok();     } };
        if (!hideCancelButton)
        {
            lsCancel = new Listener() { public void handleEvent(Event e) { cancel(); } };
        }
        wOK.addListener    (SWT.Selection, lsOK     );
        wCancel.addListener(SWT.Selection, lsCancel );
        
        lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
        wNumber.addSelectionListener(lsDef);

        // Detect [X] or ALT-F4 or something that kills this window...
        shell.addShellListener( new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );

        
        getData();

        BaseStepDialog.setSize(shell);

        
        shell.open();
        while (!shell.isDisposed())
        {
            if (!display.readAndDispatch()) display.sleep();
        }
        return samples;
    }

    public void dispose()
    {
        props.setScreen(new WindowProperty(shell));
        shell.dispose();
    }

    public void getData()
    {
        wNumber.setText(Integer.toString(samples));
        wNumber.selectAll();
    }

    private void cancel()
    {
        samples = -1;
        dispose();
    }

    private void ok()
    {
        try
        {
            samples = Integer.parseInt(wNumber.getText());
            dispose();
        }
        catch (Exception e)
        {
            MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
            mb.setMessage(Messages.getString("Dialog.Error.EnterInteger"));
            mb.setText(Messages.getString("Dialog.Error.Header"));
            mb.open();
            wNumber.selectAll();
        }
    }
    
    public void setHideCancel(boolean hideCancel)
    {
        hideCancelButton = hideCancel;
    }
}
