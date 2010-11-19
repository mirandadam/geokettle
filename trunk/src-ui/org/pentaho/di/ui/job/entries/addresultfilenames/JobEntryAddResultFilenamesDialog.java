 /**********************************************************************
 **                                                                   **
 **               This code belongs to the KETTLE project.            **
 **                                                                   **
 **                                                                   **
 **                                                                   **
 **********************************************************************/
package org.pentaho.di.ui.job.entries.addresultfilenames;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox; 
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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import org.pentaho.di.repository.Repository;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.core.Const;

import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.ui.job.dialog.JobDialog;
import org.pentaho.di.ui.job.entry.JobEntryDialog;
import org.pentaho.di.job.entry.JobEntryDialogInterface;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.job.entries.addresultfilenames.JobEntryAddResultFilenames;
import org.pentaho.di.job.entries.addresultfilenames.Messages;

/**
 * This dialog allows you to edit the Delete Files job entry settings.
 *
 * @author Samatar Hassan
 * @since  06-05-2007
 */

public class JobEntryAddResultFilenamesDialog extends JobEntryDialog implements JobEntryDialogInterface
{
   private static final String[] FILETYPES = new String[] {
           Messages.getString("JobEntryAddResultFilenames.Filetype.All") };
	
	private Label        wlName;
	private Text         wName;
    private FormData     fdlName, fdName;

	private Label        wlFilename;
	private Button       wbFilename,wbDirectory;
	private TextVar      wFilename;
	private FormData     fdlFilename, fdbFilename, fdFilename,fdbDirectory;
	
	private Label        wlIncludeSubfolders;
	private Button       wIncludeSubfolders;
	private FormData     fdlIncludeSubfolders, fdIncludeSubfolders;
	
	private Label        wlDeleteAllBefore;
	private Button       wDeleteAllBefore;
	private FormData     fdlDeleteAllBefore, fdDeleteAllBefore;

	private Button       wOK, wCancel;
	private Listener     lsOK, lsCancel;

	private JobEntryAddResultFilenames jobEntry;
	private Shell         	    shell;

	private SelectionAdapter lsDef;
	
	private boolean changed;

	private Label wlPrevious;
	private Button wPrevious;
	private FormData fdlPrevious, fdPrevious;

	private Label wlFields;
	private TableView wFields;
	private FormData fdlFields, fdFields;

	private Group wSettings;
	private FormData fdSettings;

	private Label    wlFilemask;
	private TextVar  wFilemask;
	private FormData fdlFilemask, fdFilemask;

	private Button   wbdFilename; // Delete
	private Button   wbeFilename; // Edit
	private Button   wbaFilename; // Add or change
	private FormData fdbeFilename, fdbaFilename, fdbdFilename;

	 public JobEntryAddResultFilenamesDialog(Shell parent, JobEntryInterface jobEntryInt, Repository rep, JobMeta jobMeta)
	 {
        super(parent, jobEntryInt, rep, jobMeta);
        jobEntry = (JobEntryAddResultFilenames) jobEntryInt;

		if (this.jobEntry.getName() == null) 
			this.jobEntry.setName(Messages.getString("JobEntryAddResultFilenames.Name.Default"));
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

		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText(Messages.getString("JobEntryAddResultFilenames.Title"));

		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Filename line
		wlName=new Label(shell, SWT.RIGHT);
		wlName.setText(Messages.getString("JobEntryAddResultFilenames.Name.Label"));
 		props.setLook(wlName);
		fdlName=new FormData();
		fdlName.left = new FormAttachment(0, 0);
		fdlName.right= new FormAttachment(middle, -margin);
		fdlName.top  = new FormAttachment(0, margin);
		wlName.setLayoutData(fdlName);
		wName=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wName);
		wName.addModifyListener(lsMod);
		fdName=new FormData();
		fdName.left = new FormAttachment(middle, 0);
		fdName.top  = new FormAttachment(0, margin);
		fdName.right= new FormAttachment(100, 0);
		wName.setLayoutData(fdName);

		// SETTINGS grouping?
		// ////////////////////////
		// START OF SETTINGS GROUP
		// 

		wSettings = new Group(shell, SWT.SHADOW_NONE);
		props.setLook(wSettings);
		wSettings.setText(Messages.getString("JobEntryAddResultFilenames.Settings.Label"));

		FormLayout groupLayout = new FormLayout();
		groupLayout.marginWidth = 10;
		groupLayout.marginHeight = 10;
		wSettings.setLayout(groupLayout);

		wlIncludeSubfolders = new Label(wSettings, SWT.RIGHT);
		wlIncludeSubfolders.setText(Messages.getString("JobEntryAddResultFilenames.IncludeSubfolders.Label"));
		props.setLook(wlIncludeSubfolders);
		fdlIncludeSubfolders = new FormData();
		fdlIncludeSubfolders.left = new FormAttachment(0, 0);
		fdlIncludeSubfolders.top = new FormAttachment(wName, margin);
		fdlIncludeSubfolders.right = new FormAttachment(middle, -margin);
		wlIncludeSubfolders.setLayoutData(fdlIncludeSubfolders);
		wIncludeSubfolders = new Button(wSettings, SWT.CHECK);
		props.setLook(wIncludeSubfolders);
		wIncludeSubfolders.setToolTipText(Messages.getString("JobEntryAddResultFilenames.IncludeSubfolders.Tooltip"));
		fdIncludeSubfolders = new FormData();
		fdIncludeSubfolders.left = new FormAttachment(middle, 0);
		fdIncludeSubfolders.top = new FormAttachment(wName, margin);
		fdIncludeSubfolders.right = new FormAttachment(100, 0);
		wIncludeSubfolders.setLayoutData(fdIncludeSubfolders);
		wIncludeSubfolders.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				jobEntry.setChanged();
			}
		});
		
	
		wlPrevious = new Label(wSettings, SWT.RIGHT);
		wlPrevious.setText(Messages.getString("JobEntryAddResultFilenames.Previous.Label"));
		props.setLook(wlPrevious);
		fdlPrevious = new FormData();
		fdlPrevious.left = new FormAttachment(0, 0);
		fdlPrevious.top = new FormAttachment(wIncludeSubfolders, margin );
		fdlPrevious.right = new FormAttachment(middle, -margin);
		wlPrevious.setLayoutData(fdlPrevious);
		wPrevious = new Button(wSettings, SWT.CHECK);
		props.setLook(wPrevious);
		wPrevious.setSelection(jobEntry.argFromPrevious);
		wPrevious.setToolTipText(Messages.getString("JobEntryAddResultFilenames.Previous.Tooltip"));
		fdPrevious = new FormData();
		fdPrevious.left = new FormAttachment(middle, 0);
		fdPrevious.top = new FormAttachment(wIncludeSubfolders, margin );
		fdPrevious.right = new FormAttachment(100, 0);
		wPrevious.setLayoutData(fdPrevious);
		wPrevious.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				
				setPrevious();				
				jobEntry.setChanged();
			}
		});
		
		wlDeleteAllBefore = new Label(wSettings, SWT.RIGHT);
		wlDeleteAllBefore.setText(Messages.getString("JobEntryAddResultFilenames.DeleteAllBefore.Label"));
		props.setLook(wlDeleteAllBefore);
		fdlDeleteAllBefore = new FormData();
		fdlDeleteAllBefore.left = new FormAttachment(0, 0);
		fdlDeleteAllBefore.top = new FormAttachment(wPrevious, margin);
		fdlDeleteAllBefore.right = new FormAttachment(middle, -margin);
		wlDeleteAllBefore.setLayoutData(fdlDeleteAllBefore);
		wDeleteAllBefore = new Button(wSettings, SWT.CHECK);
		props.setLook(wDeleteAllBefore);
		wDeleteAllBefore.setToolTipText(Messages.getString("JobEntryAddResultFilenames.DeleteAllBefore.Tooltip"));
		fdDeleteAllBefore = new FormData();
		fdDeleteAllBefore.left = new FormAttachment(middle, 0);
		fdDeleteAllBefore.top = new FormAttachment(wPrevious, margin);
		fdDeleteAllBefore.right = new FormAttachment(100, 0);
		wDeleteAllBefore.setLayoutData(fdDeleteAllBefore);
		wDeleteAllBefore.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				jobEntry.setChanged();
			}
		});
		
		
		
		fdSettings = new FormData();
		fdSettings.left = new FormAttachment(0, margin);
		fdSettings.top = new FormAttachment(wName, margin);
		fdSettings.right = new FormAttachment(100, -margin);
		wSettings.setLayoutData(fdSettings);
		
		// ///////////////////////////////////////////////////////////
		// / END OF SETTINGS GROUP
		// ///////////////////////////////////////////////////////////

		// Filename line
		wlFilename=new Label(shell, SWT.RIGHT);
		wlFilename.setText(Messages.getString("JobEntryAddResultFilenames.Filename.Label"));
		props.setLook(wlFilename);
		fdlFilename=new FormData();
		fdlFilename.left = new FormAttachment(0, 0);
		fdlFilename.top  = new FormAttachment(wSettings, 2*margin);
		fdlFilename.right= new FormAttachment(middle, -margin);
		wlFilename.setLayoutData(fdlFilename);
		
		
		
		// Browse Source folders button ...
		wbDirectory=new Button(shell, SWT.PUSH| SWT.CENTER);
		props.setLook(wbDirectory);
		wbDirectory.setText(Messages.getString("JobEntryAddResultFilenames.BrowseFolders.Label"));
		fdbDirectory=new FormData();
		fdbDirectory.right= new FormAttachment(100, -margin);
		fdbDirectory.top  = new FormAttachment(wSettings, margin);
		wbDirectory.setLayoutData(fdbDirectory);
		
		wbDirectory.addSelectionListener
		(
			new SelectionAdapter()
			{
				public void widgetSelected(SelectionEvent e)
				{
					DirectoryDialog ddialog = new DirectoryDialog(shell, SWT.OPEN);
					if (wFilename.getText()!=null)
					{
						ddialog.setFilterPath(jobMeta.environmentSubstitute(wFilename.getText()) );
					}
					
					 // Calling open() will open and run the dialog.
			        // It will return the selected directory, or
			        // null if user cancels
			        String dir = ddialog.open();
			        if (dir != null) {
			          // Set the text box to the new selection
			        	wFilename.setText(dir);
			        }
					
				}
			}
		);
				

		wbFilename=new Button(shell, SWT.PUSH| SWT.CENTER);
		props.setLook(wbFilename);
		wbFilename.setText(Messages.getString("JobEntryAddResultFilenames.BrowseFiles.Label"));
		fdbFilename=new FormData();
		fdbFilename.right= new FormAttachment(100, 0);
		fdbFilename.top  = new FormAttachment(wSettings, margin);
		fdbFilename.right= new FormAttachment(wbDirectory, -margin);
		wbFilename.setLayoutData(fdbFilename);

		wbaFilename=new Button(shell, SWT.PUSH| SWT.CENTER);
		props.setLook(wbaFilename);
		wbaFilename.setText(Messages.getString("JobEntryAddResultFilenames.FilenameAdd.Button"));
		fdbaFilename=new FormData();
		fdbaFilename.right= new FormAttachment(wbFilename, -margin);
		fdbaFilename.top  = new FormAttachment(wSettings, margin);
		wbaFilename.setLayoutData(fdbaFilename);

		wFilename=new TextVar(jobMeta,shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wFilename);
		wFilename.addModifyListener(lsMod);
		fdFilename=new FormData();
		fdFilename.left = new FormAttachment(middle, 0);
		fdFilename.top  = new FormAttachment(wSettings, 2*margin);
		fdFilename.right= new FormAttachment(wbFilename, -55);
		wFilename.setLayoutData(fdFilename);

		// Whenever something changes, set the tooltip to the expanded version:
		wFilename.addModifyListener(new ModifyListener()
		{
			public void modifyText(ModifyEvent e)
			{
				wFilename.setToolTipText(jobMeta.environmentSubstitute( wFilename.getText() ) );
			}
		}
		);

		wbFilename.addSelectionListener
		(
			new SelectionAdapter()
			{
				public void widgetSelected(SelectionEvent e)
				{
					FileDialog dialog = new FileDialog(shell, SWT.OPEN);
					dialog.setFilterExtensions(new String[] {"*"});
					if (wFilename.getText()!=null)
					{
						dialog.setFileName(jobMeta.environmentSubstitute(wFilename.getText()) );
					}
					dialog.setFilterNames(FILETYPES);
					if (dialog.open()!=null)
					{
						wFilename.setText(dialog.getFilterPath()+Const.FILE_SEPARATOR+dialog.getFileName());
					}
				}
			}
		);
		
		// Filemask
		wlFilemask = new Label(shell, SWT.RIGHT);
		wlFilemask.setText(Messages.getString("JobEntryAddResultFilenames.Wildcard.Label"));
		props.setLook(wlFilemask);
		fdlFilemask = new FormData();
		fdlFilemask.left = new FormAttachment(0, 0);
		fdlFilemask.top = new FormAttachment(wFilename, margin);
		fdlFilemask.right = new FormAttachment(middle, -margin);
		wlFilemask.setLayoutData(fdlFilemask);
		wFilemask = new TextVar(jobMeta,shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER, 
				                Messages.getString("JobEntryAddResultFilenames.Wildcard.Tooltip"));
		props.setLook(wFilemask);
		wFilemask.addModifyListener(lsMod);
		fdFilemask = new FormData();
		fdFilemask.left = new FormAttachment(middle, 0);
		fdFilemask.top = new FormAttachment(wFilename, margin);
		fdFilemask.right= new FormAttachment(wbFilename, -55);
		wFilemask.setLayoutData(fdFilemask);

		// Buttons to the right of the screen...
		wbdFilename=new Button(shell, SWT.PUSH| SWT.CENTER);
		props.setLook(wbdFilename);
		wbdFilename.setText(Messages.getString("JobEntryAddResultFilenames.FilenameDelete.Button"));
		wbdFilename.setToolTipText(Messages.getString("JobEntryAddResultFilenames.FilenameDelete.Tooltip"));
		fdbdFilename=new FormData();
		fdbdFilename.right = new FormAttachment(100, 0);
		fdbdFilename.top  = new FormAttachment (wFilemask, 40);
		wbdFilename.setLayoutData(fdbdFilename);

		wbeFilename=new Button(shell, SWT.PUSH| SWT.CENTER);
		props.setLook(wbeFilename);
		wbeFilename.setText(Messages.getString("JobEntryAddResultFilenames.FilenameEdit.Button"));
		wbeFilename.setToolTipText(Messages.getString("JobEntryAddResultFilenames.FilenameEdit.Tooltip"));
		fdbeFilename=new FormData();
		fdbeFilename.right = new FormAttachment(100, 0);
		fdbeFilename.left = new FormAttachment(wbdFilename, 0, SWT.LEFT);
		fdbeFilename.top  = new FormAttachment (wbdFilename, margin);
		wbeFilename.setLayoutData(fdbeFilename);

		wlFields = new Label(shell, SWT.NONE);
		wlFields.setText(Messages.getString("JobEntryAddResultFilenames.Fields.Label"));
		props.setLook(wlFields);
		fdlFields = new FormData();
		fdlFields.left = new FormAttachment(0, 0);
		fdlFields.right= new FormAttachment(middle, -margin);
		fdlFields.top = new FormAttachment(wFilemask,margin);
		wlFields.setLayoutData(fdlFields);

		int rows = jobEntry.arguments == null
			? 1
			: (jobEntry.arguments.length == 0
			? 0
			: jobEntry.arguments.length);
		final int FieldsRows = rows;

		ColumnInfo[] colinf=new ColumnInfo[]
			{
				new ColumnInfo(Messages.getString("JobEntryAddResultFilenames.Fields.Argument.Label"),  ColumnInfo.COLUMN_TYPE_TEXT,    false),
				new ColumnInfo(Messages.getString("JobEntryAddResultFilenames.Fields.Wildcard.Label"), ColumnInfo.COLUMN_TYPE_TEXT,    false ),
			};

		colinf[0].setUsingVariables(true);
		colinf[0].setToolTip(Messages.getString("JobEntryAddResultFilenames.Fields.Column"));
		colinf[1].setUsingVariables(true);
		colinf[1].setToolTip(Messages.getString("JobEntryAddResultFilenames.Wildcard.Column"));

		wFields = new TableView(jobMeta,shell, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, colinf,
			FieldsRows, lsMod, props);

		fdFields = new FormData();
		fdFields.left = new FormAttachment(0, 0);
		fdFields.top = new FormAttachment(wlFields, margin);
		fdFields.right = new FormAttachment(100, -75);
		fdFields.bottom = new FormAttachment(100, -50);
		wFields.setLayoutData(fdFields);

		wlFields.setEnabled(!jobEntry.argFromPrevious);
		wFields.setEnabled(!jobEntry.argFromPrevious);

		// Add the file to the list of files...
		SelectionAdapter selA = new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent arg0)
			{
				wFields.add(new String[] { wFilename.getText(), wFilemask.getText() } );
				wFilename.setText("");
				wFilemask.setText("");
				wFields.removeEmptyRows();
				wFields.setRowNums();
				wFields.optWidth(true);
			}
		};
		wbaFilename.addSelectionListener(selA);
		wFilename.addSelectionListener(selA);

		// Delete files from the list of files...
		wbdFilename.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent arg0)
			{
				int idx[] = wFields.getSelectionIndices();
				wFields.remove(idx);
				wFields.removeEmptyRows();
				wFields.setRowNums();
			}
		});

		// Edit the selected file & remove from the list...
		wbeFilename.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent arg0)
			{
				int idx = wFields.getSelectionIndex();
				if (idx>=0)
				{
					String string[] = wFields.getItem(idx);
					wFilename.setText(string[0]);
					wFilemask.setText(string[1]);
					wFields.remove(idx);
				}
				wFields.removeEmptyRows();
				wFields.setRowNums();
			}
		});

        wOK = new Button(shell, SWT.PUSH);
        wOK.setText(Messages.getString("System.Button.OK"));
        wCancel = new Button(shell, SWT.PUSH);
        wCancel.setText(Messages.getString("System.Button.Cancel"));

		BaseStepDialog.positionBottomButtons(shell, new Button[] { wOK, wCancel }, margin, wFields);

		// Add listeners
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel(); } };
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();     } };

		wCancel.addListener(SWT.Selection, lsCancel);
		wOK.addListener    (SWT.Selection, lsOK    );

		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };

		wName.addSelectionListener( lsDef );
		wFilename.addSelectionListener( lsDef );

		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );

		getData();
		setPrevious();

		BaseStepDialog.setSize(shell);

		shell.open();
		while (!shell.isDisposed())
		{
				if (!display.readAndDispatch()) display.sleep();
		}
		return jobEntry;
	}
	
	private void setPrevious()
	{
		wlFields.setEnabled(!wPrevious.getSelection());
		wFields.setEnabled(!wPrevious.getSelection());
		
		wFilename.setEnabled(!wPrevious.getSelection());
		wlFilename.setEnabled(!wPrevious.getSelection());
		wbFilename.setEnabled(!wPrevious.getSelection());
		
		wlFilemask.setEnabled(!wPrevious.getSelection());
		wFilemask.setEnabled(!wPrevious.getSelection());
		
		wbdFilename.setEnabled(!wPrevious.getSelection());
		wbeFilename.setEnabled(!wPrevious.getSelection());
		wbaFilename.setEnabled(!wPrevious.getSelection());
		
		wbDirectory.setEnabled(!wPrevious.getSelection());
	}

	public void dispose()
	{
		WindowProperty winprop = new WindowProperty(shell);
		props.setScreen(winprop);
		shell.dispose();
	}
	

	/**
	 * Copy information from the meta-data input to the dialog fields.
	 */
	public void getData()
	{
		if (jobEntry.getName()    != null) wName.setText( jobEntry.getName() );
		wName.selectAll();
		
		if (jobEntry.arguments != null)
		{
			for (int i = 0; i < jobEntry.arguments.length; i++)
			{
				TableItem ti = wFields.table.getItem(i);
				if (jobEntry.arguments[i] != null)
					ti.setText(1, jobEntry.arguments[i]);
					if (jobEntry.filemasks[i] != null)
						ti.setText(2, jobEntry.filemasks[i]);
			}
			wFields.setRowNums();
			wFields.optWidth(true);
		}
		wPrevious.setSelection(jobEntry.argFromPrevious);		
		wIncludeSubfolders.setSelection(jobEntry.includeSubfolders);
		wDeleteAllBefore.setSelection(jobEntry.deleteallbefore);	
		
		

	}

	private void cancel()
	{
		jobEntry.setChanged(changed);
		jobEntry=null;
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
		jobEntry.setIncludeSubfolders(wIncludeSubfolders.getSelection());
		jobEntry.setArgumentsPrevious (wPrevious.getSelection());
		jobEntry.setDeleteAllBefore (wDeleteAllBefore.getSelection());

		int nritems = wFields.nrNonEmpty();
		int nr = 0;
		for (int i = 0; i < nritems; i++)
		{
			String arg = wFields.getNonEmpty(i).getText(1);
			if (arg != null && arg.length() != 0)
				nr++;
		}
		jobEntry.arguments = new String[nr];
		jobEntry.filemasks = new String[nr];
		nr = 0;
		for (int i = 0; i < nritems; i++)
		{
			String arg = wFields.getNonEmpty(i).getText(1);
			String wild = wFields.getNonEmpty(i).getText(2);
			if (arg != null && arg.length() != 0)
			{
				jobEntry.arguments[nr] = arg;
				jobEntry.filemasks[nr] = wild;
				nr++;
			}
		}
		dispose();
	}

	public String toString()
	{
		return this.getClass().getName();
	}

	public boolean evaluates()
	{
		return true;
	}

	public boolean isUnconditional()
	{
		return false;
	}
}