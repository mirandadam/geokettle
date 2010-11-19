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
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.ui.core.dialog.Messages;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.ui.core.PropsUI;



/**
 * A dialog that sets the printer settings for a Kettle printout.
 * 
 * @author Matt
 * @since 19-06-2003
 * 
 */
public class EnterPrintDialog extends Dialog
{
	private int retval;
	private Image image;
		
	private Label        wlCanvas;
	private Canvas       wCanvas;
    private FormData     fdlCanvas, fdCanvas;
    
	private Label        wlCols;
	private Slider       wCols;
	private FormData     fdlCols, fdCols;

	private Label        wlRows;
	private Slider       wRows;
	private FormData     fdlRows, fdRows;

	private Label        wlScale;
	private Slider       wScale;
	private FormData     fdlScale, fdScale;

	private Label        wlLeft;
	private Text         wLeft;
	private FormData     fdlLeft, fdLeft;

	private Label        wlRight;
	private Text         wRight;
	private FormData     fdlRight, fdRight;

	private Label        wlTop;
	private Text         wTop;
	private FormData     fdlTop, fdTop;

	private Label        wlBottom;
	private Text         wBottom;
	private FormData     fdlBottom, fdBottom;
		
	private Button wOK, wCancel;
	private FormData fdOK, fdCancel;
	private Listener lsOK, lsCancel;

	private Shell  shell;
	private PropsUI props;
	
	public  int nrcols, nrrows, scale;
	public  Point page;
	public  double factorx, factory;
	public  double leftMargin, rightMargin, topMargin, bottomMargin; 
		
	public EnterPrintDialog(Shell parent, int nrcols, int nrrows, int scale, double factorX, double factorY, Rectangle m, double marginLeft, double marginRigth, double marginTop, double marginBottom, Image image)
	{
		super(parent, SWT.NONE);
		props = PropsUI.getInstance();
		this.nrcols = nrcols;
        this.nrrows = nrrows;
        this.scale  = scale;
        this.image  = image;
        this.factorx = factorX;
        this.factory = factorY;
        this.leftMargin   = marginLeft;
        this.rightMargin  = marginRigth;
        this.topMargin    = marginTop;
        this.bottomMargin = marginBottom;
		
		page = new Point(m.width, m.height);
	}

	public int open()
	{
		Shell parent = getParent();
		Display display = parent.getDisplay();
		
		retval = SWT.OK;

		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.RESIZE | SWT.MAX | SWT.MIN);
 		props.setLook(shell);

		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText(Messages.getString("EnterPrintDialog.Title"));
		
		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Canvas
		wlCanvas=new Label(shell, SWT.NONE);
		wlCanvas.setText(Messages.getString("EnterPrintDialog.PrintArea.Label"));
 		props.setLook(wlCanvas);
		fdlCanvas=new FormData();
		fdlCanvas.left = new FormAttachment(0, 0);
		fdlCanvas.top  = new FormAttachment(0, margin);
		wlCanvas.setLayoutData(fdlCanvas);
		wCanvas=new Canvas(shell, SWT.BORDER);
 		props.setLook(wCanvas);
		wCanvas.addPaintListener(new PaintListener() 
			{
				public void paintControl(PaintEvent pe) 
				{
					repaint(pe.gc, pe.width, pe.height);   
				}
			}
		)
		;
		fdCanvas=new FormData();
		fdCanvas.left  = new FormAttachment(0, 0);
		fdCanvas.top   = new FormAttachment(wlCanvas, margin);
		fdCanvas.right = new FormAttachment(100, 0);
		fdCanvas.bottom= new FormAttachment(100, -220);
		wCanvas.setLayoutData(fdCanvas);

		// Rows
		wlRows=new Label(shell, SWT.NONE);
		wlRows.setText(Messages.getString("EnterPrintDialog.Rows.Label"));
 		props.setLook(wlRows);
		fdlRows=new FormData();
		fdlRows.left = new FormAttachment(0, 0);
		fdlRows.right= new FormAttachment(middle, -margin);
		fdlRows.top  = new FormAttachment(wCanvas, margin);
		wlRows.setLayoutData(fdlRows);
		wRows=new Slider(shell, SWT.HORIZONTAL);
		wRows.setIncrement(1);
		wRows.setMinimum(1);
		wRows.setMaximum(11);
		wRows.setThumb(1);
		wRows.addSelectionListener(new SelectionAdapter() 
			{
				public void widgetSelected(SelectionEvent se) 
				{
					Slider sl = (Slider)se.widget;
					nrrows = sl.getSelection();
					wCanvas.redraw();
				}
			}
		);
 		props.setLook(wRows);
		fdRows=new FormData();
		fdRows.left  = new FormAttachment(middle, 0);
		fdRows.top   = new FormAttachment(wCanvas, margin);
		fdRows.right = new FormAttachment(100, 0);
		wRows.setLayoutData(fdRows);

		// Cols
		wlCols=new Label(shell, SWT.NONE);
		wlCols.setText(Messages.getString("EnterPrintDialog.Cols.Label"));
 		props.setLook(wlCols);
		fdlCols=new FormData();
		fdlCols.left = new FormAttachment(0, 0);
		fdlCols.right= new FormAttachment(middle, -margin);
		fdlCols.top  = new FormAttachment(wRows, margin);
		wlCols.setLayoutData(fdlCols);
		wCols=new Slider(shell, SWT.HORIZONTAL);
		wCols.setIncrement(1);
		wCols.setMinimum(1);
		wCols.setMaximum(11);
		wCols.setThumb(1);
		wCols.addSelectionListener(new SelectionAdapter() 
			{
				public void widgetSelected(SelectionEvent se) 
				{
					Slider sl = (Slider)se.widget;
					nrcols = sl.getSelection();
					wCanvas.redraw();
				}
			}
		);
 		props.setLook(wCols);
		fdCols=new FormData();
		fdCols.left  = new FormAttachment(middle, 0);
		fdCols.top   = new FormAttachment(wRows, margin);
		fdCols.right = new FormAttachment(100, 0);
		wCols.setLayoutData(fdCols);

		// Scale
		wlScale=new Label(shell, SWT.NONE);
		wlScale.setText(Messages.getString("EnterPrintDialog.Scaling.Label"));
 		props.setLook(wlScale);
		fdlScale=new FormData();
		fdlScale.left = new FormAttachment(0, 0);
		fdlScale.right= new FormAttachment(middle, -margin);
		fdlScale.top  = new FormAttachment(wCols, margin);
		wlScale.setLayoutData(fdlScale);
		wScale=new Slider(shell, SWT.HORIZONTAL);
		wScale.setIncrement(10);
		wScale.setMinimum(10);
		wScale.setMaximum(500);
		wScale.setThumb(10);
		wScale.setPageIncrement(25);
		wScale.addSelectionListener(new SelectionAdapter() 
			{
				public void widgetSelected(SelectionEvent se) 
				{
					Slider sl = (Slider)se.widget;
					scale = sl.getSelection();
					wCanvas.redraw();
				}
			}
		);
 		props.setLook(wScale);
		fdScale=new FormData();
		fdScale.left  = new FormAttachment(middle, 0);
		fdScale.top   = new FormAttachment(wCols, margin);
		fdScale.right = new FormAttachment(100, 0);
		wScale.setLayoutData(fdScale);

		// Left
		wlLeft=new Label(shell, SWT.NONE);
		wlLeft.setText(Messages.getString("EnterPrintDialog.LeftMargin.Label"));
 		props.setLook(wlLeft);
		fdlLeft=new FormData();
		fdlLeft.left = new FormAttachment(0, 0);
		fdlLeft.right= new FormAttachment(middle, -margin);
		fdlLeft.top  = new FormAttachment(wScale, margin);
		wlLeft.setLayoutData(fdlLeft);
		wLeft=new Text(shell, SWT.BORDER);
		wLeft.addModifyListener(new ModifyListener() 
			{
				public void modifyText(ModifyEvent e) 
				{
					Text w = (Text)e.widget;
					leftMargin = Const.toDouble(w.getText(), 0.00);
				}
			}
		);
 		props.setLook(wLeft);
		fdLeft=new FormData();
		fdLeft.left  = new FormAttachment(middle, 0);
		fdLeft.top   = new FormAttachment(wScale, margin);
		fdLeft.right = new FormAttachment(100, 0);
		wLeft.setLayoutData(fdLeft);


		// Right
		wlRight=new Label(shell, SWT.NONE);
		wlRight.setText(Messages.getString("EnterPrintDialog.RightMargin.Label"));
 		props.setLook(wlRight);
		fdlRight=new FormData();
		fdlRight.left = new FormAttachment(0, 0);
		fdlRight.right= new FormAttachment(middle, -margin);
		fdlRight.top  = new FormAttachment(wLeft, margin);
		wlRight.setLayoutData(fdlRight);
		wRight=new Text(shell, SWT.BORDER);
		wRight.addModifyListener(new ModifyListener() 
			{
				public void modifyText(ModifyEvent e) 
				{
					Text w = (Text)e.widget;
					rightMargin = Const.toDouble(w.getText(), 0.00);
				}
			}
		);
 		props.setLook(wRight);
		fdRight=new FormData();
		fdRight.left  = new FormAttachment(middle, 0);
		fdRight.top   = new FormAttachment(wLeft, margin);
		fdRight.right = new FormAttachment(100, 0);
		wRight.setLayoutData(fdRight);

		// Top
		wlTop=new Label(shell, SWT.NONE);
		wlTop.setText(Messages.getString("EnterPrintDialog.TopMargin.Label"));
 		props.setLook(wlTop);
		fdlTop=new FormData();
		fdlTop.left = new FormAttachment(0, 0);
		fdlTop.right= new FormAttachment(middle, -margin);
		fdlTop.top  = new FormAttachment(wRight, margin);
		wlTop.setLayoutData(fdlTop);
		wTop=new Text(shell, SWT.BORDER);
		wTop.addModifyListener(new ModifyListener() 
			{
				public void modifyText(ModifyEvent e) 
				{
					Text w = (Text)e.widget;
					topMargin = Const.toDouble(w.getText(), 0.00);
				}
			}
		);
 		props.setLook(wTop);
		fdTop=new FormData();
		fdTop.left  = new FormAttachment(middle, 0);
		fdTop.top   = new FormAttachment(wRight, margin);
		fdTop.right = new FormAttachment(100, 0);
		wTop.setLayoutData(fdTop);

		// Bottom
		wlBottom=new Label(shell, SWT.NONE);
		wlBottom.setText(Messages.getString("EnterPrintDialog.BottomMargin.Label"));
 		props.setLook(wlBottom);
		fdlBottom=new FormData();
		fdlBottom.left = new FormAttachment(0, 0);
		fdlBottom.right= new FormAttachment(middle, -margin);
		fdlBottom.top  = new FormAttachment(wTop, margin);
		wlBottom.setLayoutData(fdlBottom);
		wBottom=new Text(shell, SWT.BORDER);
		wBottom.addModifyListener(new ModifyListener() 
			{
				public void modifyText(ModifyEvent e) 
				{
					Text w = (Text)e.widget;
					bottomMargin = Const.toDouble(w.getText(), 0.00);
				}
			}
		);
 		props.setLook(wBottom);
		fdBottom=new FormData();
		fdBottom.left  = new FormAttachment(middle, 0);
		fdBottom.top   = new FormAttachment(wTop, margin);
		fdBottom.right = new FormAttachment(100, 0);
		wBottom.setLayoutData(fdBottom);


		// Some buttons
		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(Messages.getString("System.Button.OK"));
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(Messages.getString("System.Button.Cancel"));
		fdOK=new FormData();
		fdOK.left       = new FormAttachment(33, 0);
		fdOK.bottom     = new FormAttachment(100, 0);
		wOK.setLayoutData(fdOK);
		fdCancel=new FormData();
		fdCancel.left   = new FormAttachment(66, 0);
		fdCancel.bottom = new FormAttachment(100, 0);
		wCancel.setLayoutData(fdCancel);

		// Add listeners
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel(); } };
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();     } };
		
		wOK.addListener    (SWT.Selection, lsOK     );
		wCancel.addListener(SWT.Selection, lsCancel );
		
		// Detect [X] or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );

		getData();

		BaseStepDialog.setSize(shell);

		shell.open();
		while (!shell.isDisposed())
		{
				if (!display.readAndDispatch()) display.sleep();
		}
		return retval;
	}

	public void dispose()
	{
		props.setScreen(new WindowProperty(shell));
		shell.dispose();
	}
	
	public void getData()
	{
		wCols.setSelection(nrcols);
		wRows.setSelection(nrrows);
		wScale.setSelection(scale);
		wLeft  .setText(Double.toString(leftMargin  ));
		wRight .setText(Double.toString(rightMargin ));
		wTop   .setText(Double.toString(topMargin   ));
		wBottom.setText(Double.toString(bottomMargin));
	}
	
	private void cancel()
	{
		retval=SWT.CANCEL;
		dispose();
	}
	
	private void ok()
	{
		nrcols = wCols.getSelection();
		nrrows = wRows.getSelection();
		scale  = wScale.getSelection();
		dispose();
	}
	
	private void repaint(GC gc, int width, int height)
	{
		ImageData imd = image.getImageData();
		
		double sizeOnPaperX = imd.width * factorx;
		double sizeOnPaperY = imd.height * factory;
		double actualSizeX   = sizeOnPaperX * scale / 100;
		double actualSizeY   = sizeOnPaperY * scale / 100;
		
		// What % of the screen is filled?
		// The canvas is nrcols * nrrows nr of pages large.
		double percentScreenX = actualSizeX / (page.x * nrcols);
		double percentScreenY = actualSizeY / (page.y * nrrows);
				
		gc.drawImage(image, 0, 0, imd.width, imd.height,
		                  0, 0, (int)(width*percentScreenX), (int)(height*percentScreenY)
		            );

		StringBuffer text = new StringBuffer();
		text.append(nrcols).append("x").append(nrrows).append(" @ ").append(scale).append("%");
		gc.drawText(text.toString(), 0, 0);
		for (int c=1;c<nrcols;c++)
		{
			gc.drawLine(c*(width/nrcols), 0, c*(width/nrcols), height);		
		}

		for (int r=1;r<nrrows;r++)
		{
			gc.drawLine(0, r*(height/nrrows), width, r*(height/nrrows));		
		}
	}

}
