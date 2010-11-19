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
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.SQLStatement;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.ui.core.dialog.EnterTextDialog;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.dialog.Messages;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;


/**
 * Dialog to display the results of an SQL generation operation.
 * 
 * @author Matt
 * @since 19-06-2003
 *
 */
public class SQLStatementsDialog extends Dialog
{
	private List<SQLStatement>    stats;
		
	private TableView    wFields;
	private FormData     fdFields;

	private Button wClose, wView, wEdit, wExec;
	private FormData fdClose, fdView, fdEdit, fdExec;
	private Listener lsClose, lsView, lsEdit, lsExec;

	private Shell    shell;
	private PropsUI    props;
	
	private Color    red;
	
	private String stepname;
	
	private VariableSpace variables;
    
    /**
     * @deprecated Use CT without <i>props</i> parameter
     */
    public SQLStatementsDialog(Shell parent, VariableSpace space, int style, LogWriter log, PropsUI props, List<SQLStatement> stats)
    {
        this(parent, space, style, stats);
        this.props = props;
    }
    
    public SQLStatementsDialog(Shell parent, VariableSpace space, int style, List<SQLStatement> stats)
    {
            super(parent, style);
            this.stats=stats;
            this.props=PropsUI.getInstance();
            this.variables = space;
            
            this.stepname = null;
    }

	public String open()
	{
		Shell parent = getParent();
		Display display = parent.getDisplay();
		
		red    = display.getSystemColor(SWT.COLOR_RED);

		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX);
 		props.setLook(shell);
		shell.setImage(GUIResource.getInstance().getImageConnection());

		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText(Messages.getString("SQLStatementDialog.Title"));
		
		int margin = Const.MARGIN;
		
		int FieldsCols=4;
		int FieldsRows=stats.size();
		
		ColumnInfo[] colinf=new ColumnInfo[FieldsCols];
		colinf[0]=new ColumnInfo(Messages.getString("SQLStatementDialog.TableCol.Stepname"),     ColumnInfo.COLUMN_TYPE_TEXT,   false, true);
		colinf[1]=new ColumnInfo(Messages.getString("SQLStatementDialog.TableCol.Connection"),   ColumnInfo.COLUMN_TYPE_TEXT,   false, true);
		colinf[2]=new ColumnInfo(Messages.getString("SQLStatementDialog.TableCol.SQL"),          ColumnInfo.COLUMN_TYPE_TEXT,   false, true);
		colinf[3]=new ColumnInfo(Messages.getString("SQLStatementDialog.TableCol.Error"),        ColumnInfo.COLUMN_TYPE_TEXT,   false, true);
		
		wFields=new TableView(variables, shell, 
						      SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, 
						      colinf, 
						      FieldsRows,  
							  true, // read-only
						      null,
							  props
						      );
		
		fdFields=new FormData();
		fdFields.left   = new FormAttachment(0, 0);
		fdFields.top    = new FormAttachment(0, 0);
		fdFields.right  = new FormAttachment(100, 0);
		fdFields.bottom = new FormAttachment(100, -50);
		wFields.setLayoutData(fdFields);

		wClose=new Button(shell, SWT.PUSH);
		wClose.setText(Messages.getString("System.Button.Close"));
		fdClose=new FormData();
		fdClose.left   =new FormAttachment(25, 0);
		fdClose.bottom =new FormAttachment(100, 0);
		wClose.setLayoutData(fdClose);

		wView=new Button(shell, SWT.PUSH);
		wView.setText(Messages.getString("SQLStatementDialog.Button.ViewSQL"));
		fdView=new FormData();
		fdView.left=new FormAttachment(wClose, margin);
		fdView.bottom =new FormAttachment(100, 0);
		wView.setLayoutData(fdView);

		wExec=new Button(shell, SWT.PUSH);
		wExec.setText(Messages.getString("SQLStatementDialog.Button.ExecSQL"));
		fdExec=new FormData();
		fdExec.left=new FormAttachment(wView, margin);
		fdExec.bottom =new FormAttachment(100, 0);
		wExec.setLayoutData(fdExec);

		wEdit=new Button(shell, SWT.PUSH);
		wEdit.setText(Messages.getString("SQLStatementDialog.Button.EditStep"));
		fdEdit=new FormData();
		fdEdit.left=new FormAttachment(wExec, margin);
		fdEdit.bottom =new FormAttachment(100, 0);
		wEdit.setLayoutData(fdEdit);

		// Add listeners
		lsClose = new Listener() { public void handleEvent(Event e) { close(); } };
		lsView  = new Listener() { public void handleEvent(Event e) { view(); } };
		lsExec  = new Listener() { public void handleEvent(Event e) { exec(); } };
		lsEdit  = new Listener() { public void handleEvent(Event e) { edit(); } };

		wClose.addListener(SWT.Selection, lsClose    );
		wView .addListener(SWT.Selection, lsView     );
		wExec .addListener(SWT.Selection, lsExec     );
		wEdit .addListener(SWT.Selection, lsEdit     );
		
		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { close(); } } );

		getData();
		
		BaseStepDialog.setSize(shell);

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
		for (int i=0;i<stats.size();i++)
		{
			SQLStatement stat = (SQLStatement)stats.get(i);
			TableItem ti = wFields.table.getItem(i); 

			String name         = stat.getStepname();
			DatabaseMeta dbinfo = stat.getDatabase();
			String sql          = stat.getSQL();
			String error        = stat.getError();
			
			if (name!=null)   ti.setText(1, name);
			if (dbinfo!=null) ti.setText(2, dbinfo.getName() );
			if (sql!=null)    ti.setText(3, sql);
			if (error!=null)  ti.setText(4, error);

			Color col = ti.getBackground();
			if (stat.hasError()) col=red;
			ti.setBackground(col);
		}
		wFields.setRowNums();
		wFields.optWidth(true);
	}
	
	private String getSQL()
	{
		StringBuffer sql = new StringBuffer();
		
		int idx[] = wFields.table.getSelectionIndices();
		
		// None selected: don't waste users time: select them all!
		if (idx.length==0) 
		{
			idx=new int[stats.size()];
			for (int i=0;i<stats.size();i++) idx[i]=i;
		}
		
		for (int i=0;i<idx.length;i++)
		{
			SQLStatement stat = (SQLStatement)stats.get(idx[i]);
			DatabaseMeta di = stat.getDatabase();
			if (i > 0)
			    sql.append("-------------------------------------------------------------------------------------------").append(Const.CR);
			sql.append(Messages.getString("SQLStatementDialog.Log.Step", stat.getStepname()));
			sql.append(Messages.getString("SQLStatementDialog.Log.Connection", (di != null ? di.getName() : Messages.getString("SQLStatementDialog.Log.Undefined"))));
			if (stat.hasSQL())
			{
				sql.append("-- SQL                  : ");
				sql.append(stat.getSQL()).append(Const.CR);
			}
			if (stat.hasError())
			{
				sql.append(Messages.getString("SQLStatementDialog.Log.Error", stat.getError()));
			}
		}

		return sql.toString();
	}
	
	// View SQL statement:
	private void view()
	{
		String sql = getSQL();
		EnterTextDialog etd = new EnterTextDialog(shell, Messages.getString("SQLStatementDialog.ViewSQL.Title"),
		    Messages.getString("SQLStatementDialog.ViewSQL.Message"), sql, true);
		etd.setReadOnly();
		etd.open();
	}
	
	private void exec()
	{
		int idx[] = wFields.table.getSelectionIndices();
		
		// None selected: don't waste users time: select them all!
		if (idx.length==0) 
		{
			idx=new int[stats.size()];
			for (int i=0;i<stats.size();i++) idx[i]=i;
		}
		
		int errors = 0;
		for (int i=0;i<idx.length;i++)
		{
			SQLStatement stat = (SQLStatement)stats.get(idx[i]);
			if (stat.hasError()) errors++;
		}
		
		if (errors==0)
		{
			for (int i=0;i<idx.length;i++)
			{
				SQLStatement stat = (SQLStatement)stats.get(idx[i]);
				DatabaseMeta di = stat.getDatabase();
				if (di!=null && !stat.hasError())
				{
					Database db = new Database(di);
					try
					{
						db.connect();
						try
						{
							db.execStatements(stat.getSQL());
						}
						catch(KettleDatabaseException dbe)
						{
							errors++;
							new ErrorDialog(shell, Messages.getString("SQLStatementDialog.Error.Title"),
							    Messages.getString("SQLStatementDialog.Error.CouldNotExec", stat.getSQL()), dbe);
						}
					}
					catch(KettleDatabaseException dbe)
					{
						new ErrorDialog(shell, Messages.getString("SQLStatementDialog.Error.Title"),
						    Messages.getString("SQLStatementDialog.Error.CouldNotConnect", (di == null ? "" : di.getName())), dbe);
					}
					finally
					{
						db.disconnect();
					}
				}
			}
			if (errors==0)
			{
				MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_INFORMATION );
				mb.setMessage(Messages.getString("SQLStatementDialog.Success.Message", Integer.toString(idx.length)));
				mb.setText(Messages.getString("SQLStatementDialog.Success.Title"));
				mb.open();
			}
		}
		else
		{
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
			mb.setMessage(Messages.getString("SQLStatementDialog.Error.Message", Integer.toString(errors)));
			mb.setText(Messages.getString("SQLStatementDialog.Error.Title"));
			mb.open();
		}
	}
	
	private void edit()
	{
		int idx=wFields.table.getSelectionIndex();
		if (idx>=0)
		{
			stepname = wFields.table.getItem(idx).getText(1);
			dispose();
		}	
		else
		{
			stepname = null;
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
            mb.setText(Messages.getString("StepFieldsDialog.OriginStep.Title")); //$NON-NLS-1$
            mb.setMessage(Messages.getString("StepFieldsDialog.OriginStep.Message")); //$NON-NLS-1$
            mb.open();
		}
	}
	
	private void close()
	{
		dispose();
	}
}
