package org.pentaho.di.ui.trans.steps.ogrfileinput;

import org.eclipse.swt.SWT;
// import org.eclipse.swt.custom.CCombo;
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
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
// import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.TransPreviewFactory;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.steps.ogrfileinput.OGRFileInputMeta;
import org.pentaho.di.trans.steps.ogrfileinput.Messages;
import org.pentaho.di.ui.core.dialog.EnterNumberDialog;
import org.pentaho.di.ui.core.dialog.EnterTextDialog;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.dialog.PreviewRowsDialog;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.dialog.TransPreviewProgressDialog;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

public class OGRFileInputDialog extends BaseStepDialog implements StepDialogInterface
{
	
	private Label        wlFilename;
	private Button       wbFilename;
	private TextVar      wFilename;
	private FormData     fdlFilename, fdbFilename, fdFilename;

	private Label        wlConnectionString;
	private TextVar      wConnectionString;
	private FormData     fdlConnectionString, fdConnectionString;

	private Label        wlSpatialFilter;
	private TextVar      wSpatialFilter;
	private FormData     fdlSpatialFilter, fdSpatialFilter;
	
	private Label        wlAttributeFilter;
	private TextVar      wAttributeFilter;
	private FormData     fdlAttributeFilter, fdAttributeFilter;
	
	private Label        wlSkipFailure;
	private Button       wSkipFailure;
	private FormData     fdlSkipFailure,fdSkipFailure;
	
	private Label        wlLimit;
	private Text         wLimit;
	private FormData     fdlLimit, fdLimit;
	
	private Label        wlAddRownr;
	private Button       wAddRownr;
	private FormData     fdlAddRownr, fdAddRownr;

	private Label        wlFieldRownr;
	private Text         wFieldRownr;
	private FormData     fdlFieldRownr, fdFieldRownr;
    
	private OGRFileInputMeta input;
	private boolean backupChanged, backupAddRownr, backupSkipFailure;

	public OGRFileInputDialog(Shell parent, Object in, TransMeta tr, String sname)
	{
		super(parent, (BaseStepMeta)in, tr, sname);
		input=(OGRFileInputMeta)in;
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
		backupChanged = input.hasChanged();
		backupSkipFailure = input.isSkipFailureAdded();
		backupAddRownr = input.isRowNrAdded();

		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText(Messages.getString("OGRFileInputDialog.Dialog.Title")); //$NON-NLS-1$
		
		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Stepname line
		wlStepname=new Label(shell, SWT.RIGHT);
		wlStepname.setText(Messages.getString("System.Label.StepName")); //$NON-NLS-1$
 		props.setLook(wlStepname);
		fdlStepname=new FormData();
		fdlStepname.left = new FormAttachment(0, 0);
		fdlStepname.right= new FormAttachment(middle, -margin);
		fdlStepname.top  = new FormAttachment(0, margin*2);
		wlStepname.setLayoutData(fdlStepname);
		wStepname=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wStepname.setText(stepname);
 		props.setLook(wStepname);
		wStepname.addModifyListener(lsMod);
		fdStepname=new FormData();
		fdStepname.left = new FormAttachment(middle, 0);
		fdStepname.top  = new FormAttachment(0, margin);
		fdStepname.right= new FormAttachment(100, 0);
		wStepname.setLayoutData(fdStepname);
	
		// Filename line
		wlFilename=new Label(shell, SWT.RIGHT);
		wlFilename.setText(Messages.getString("System.Label.Filename")); //$NON-NLS-1$
 		props.setLook(wlFilename);
		fdlFilename=new FormData();
		fdlFilename.left = new FormAttachment(0, 0);
		fdlFilename.top  = new FormAttachment(wStepname, margin*2);
		fdlFilename.right= new FormAttachment(middle, -margin);
		wlFilename.setLayoutData(fdlFilename);
		
		wbFilename=new Button(shell, SWT.PUSH| SWT.CENTER);
 		props.setLook(wbFilename);
		wbFilename.setText(Messages.getString("System.Button.Browse")); //$NON-NLS-1$
		fdbFilename=new FormData();
		fdbFilename.right= new FormAttachment(100, 0);
		fdbFilename.top  = new FormAttachment(wStepname, margin);
		wbFilename.setLayoutData(fdbFilename);

		wFilename=new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wFilename);
		wFilename.addModifyListener(lsMod);
		fdFilename=new FormData();
		fdFilename.left = new FormAttachment(middle, 0);
		fdFilename.right= new FormAttachment(wbFilename, -margin);
		fdFilename.top  = new FormAttachment(wStepname, margin);
		wFilename.setLayoutData(fdFilename);
		
		// Connection string line
		wlConnectionString=new Label(shell, SWT.RIGHT);
		wlConnectionString.setText(Messages.getString("OGRFileInputDialog.Dialog.ConnectionString.Label")); //$NON-NLS-1$
 		props.setLook(wlConnectionString);
		fdlConnectionString=new FormData();
		fdlConnectionString.left = new FormAttachment(0, 0);
		fdlConnectionString.right= new FormAttachment(middle, -margin);
		fdlConnectionString.top  = new FormAttachment(wbFilename, margin*2);
		wlConnectionString.setLayoutData(fdlConnectionString);
		wConnectionString=new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		//wConnectionString.setText(connectionString);
 		props.setLook(wConnectionString);
		wConnectionString.addModifyListener(lsMod);
		fdConnectionString=new FormData();
		fdConnectionString.left = new FormAttachment(middle, 0);
		fdConnectionString.top  = new FormAttachment(wbFilename, margin);
		fdConnectionString.right= new FormAttachment(100, 0);
		wConnectionString.setLayoutData(fdConnectionString);		

		// Spatial filter line
		wlSpatialFilter=new Label(shell, SWT.RIGHT);
		wlSpatialFilter.setText(Messages.getString("OGRFileInputDialog.Dialog.SpatialFilter.Label")); //$NON-NLS-1$
 		props.setLook(wlSpatialFilter);
		fdlSpatialFilter=new FormData();
		fdlSpatialFilter.left = new FormAttachment(0, 0);
		fdlSpatialFilter.right= new FormAttachment(middle, -margin);
		fdlSpatialFilter.top  = new FormAttachment(wConnectionString, margin*2);
		wlSpatialFilter.setLayoutData(fdlSpatialFilter);
		wSpatialFilter=new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		//wSpatialFilter.setText(SpatialFilter);
 		props.setLook(wSpatialFilter);
		wSpatialFilter.addModifyListener(lsMod);
		fdSpatialFilter=new FormData();
		fdSpatialFilter.left = new FormAttachment(middle, 0);
		fdSpatialFilter.top  = new FormAttachment(wConnectionString, margin);
		fdSpatialFilter.right= new FormAttachment(100, 0);
		wSpatialFilter.setLayoutData(fdSpatialFilter);		

		// Where clause line
		wlAttributeFilter=new Label(shell, SWT.RIGHT);
		wlAttributeFilter.setText(Messages.getString("OGRFileInputDialog.Dialog.WhereClause.Label")); //$NON-NLS-1$
 		props.setLook(wlAttributeFilter);
		fdlAttributeFilter=new FormData();
		fdlAttributeFilter.left = new FormAttachment(0, 0);
		fdlAttributeFilter.right= new FormAttachment(middle, -margin);
		fdlAttributeFilter.top  = new FormAttachment(wSpatialFilter, margin*2);
		wlAttributeFilter.setLayoutData(fdlAttributeFilter);
		wAttributeFilter=new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		//wAttributeFilter.setText(AttributeFilter);
 		props.setLook(wAttributeFilter);
		wAttributeFilter.addModifyListener(lsMod);
		fdAttributeFilter=new FormData();
		fdAttributeFilter.left = new FormAttachment(middle, 0);
		fdAttributeFilter.top  = new FormAttachment(wSpatialFilter, margin);
		fdAttributeFilter.right= new FormAttachment(100, 0);
		wAttributeFilter.setLayoutData(fdAttributeFilter);		
		
		//Skip failures
		wlSkipFailure=new Label(shell, SWT.RIGHT);
		wlSkipFailure.setText(Messages.getString("OGRFileInputDialog.Dialog.SkipFailure.Label")); //$NON-NLS-1$
 		props.setLook(wlSkipFailure);
		fdlSkipFailure=new FormData();
		fdlSkipFailure.left = new FormAttachment(0, 0);
		fdlSkipFailure.top  = new FormAttachment(wAttributeFilter, margin*2);
		fdlSkipFailure.right= new FormAttachment(middle, -margin);
		wlSkipFailure.setLayoutData(fdlSkipFailure);
		wSkipFailure=new Button(shell, SWT.CHECK );
 		props.setLook(wSkipFailure);
		wSkipFailure.setToolTipText(Messages.getString("OGRFileInputDialog.SkipFailure.Tooltip")); //$NON-NLS-1$
		fdSkipFailure=new FormData();
		fdSkipFailure.left = new FormAttachment(middle, 0);
		fdSkipFailure.top  = new FormAttachment(wAttributeFilter, margin);
		wSkipFailure.setLayoutData(fdSkipFailure);
		wSkipFailure.addSelectionListener(new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { input.setChanged(); setFlags(); } } );

		// Limit input ...
		wlLimit=new Label(shell, SWT.RIGHT);
		wlLimit.setText(Messages.getString("OGRFileInputDialog.LimitSize.Label")); //$NON-NLS-1$
 		props.setLook(wlLimit);
		fdlLimit=new FormData();
		fdlLimit.left = new FormAttachment(0, 0);
		fdlLimit.right= new FormAttachment(middle, -margin);
		fdlLimit.top  = new FormAttachment(wSkipFailure, margin*2);
		wlLimit.setLayoutData(fdlLimit);
		wLimit=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wLimit);
		wLimit.addModifyListener(lsMod);
		fdLimit=new FormData();
		fdLimit.left = new FormAttachment(middle, 0);
		fdLimit.top  = new FormAttachment(wSkipFailure, margin);
		fdLimit.right= new FormAttachment(100, 0);
		wLimit.setLayoutData(fdLimit);

		// Add rownr (1...)?
		wlAddRownr=new Label(shell, SWT.RIGHT);
		wlAddRownr.setText(Messages.getString("OGRFileInputDialog.AddRowNr.Label")); //$NON-NLS-1$
 		props.setLook(wlAddRownr);
		fdlAddRownr=new FormData();
		fdlAddRownr.left = new FormAttachment(0, 0);
		fdlAddRownr.top  = new FormAttachment(wLimit, margin*2);
		fdlAddRownr.right= new FormAttachment(middle, -margin);
		wlAddRownr.setLayoutData(fdlAddRownr);
		wAddRownr=new Button(shell, SWT.CHECK );
 		props.setLook(wAddRownr);
		wAddRownr.setToolTipText(Messages.getString("OGRFileInputDialog.AddRowNr.Tooltip")); //$NON-NLS-1$
		fdAddRownr=new FormData();
		fdAddRownr.left = new FormAttachment(middle, 0);
		fdAddRownr.top  = new FormAttachment(wLimit, margin);
		wAddRownr.setLayoutData(fdAddRownr);
		wAddRownr.addSelectionListener(new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { input.setChanged(); setFlags(); } } );

		// FieldRownr input ...
		wlFieldRownr=new Label(shell, SWT.LEFT);
		wlFieldRownr.setText(Messages.getString("OGRFileInputDialog.FieldnameOfRowNr.Label")); //$NON-NLS-1$
 		props.setLook(wlFieldRownr);
		fdlFieldRownr=new FormData();
		fdlFieldRownr.left = new FormAttachment(wAddRownr, margin);
		fdlFieldRownr.top  = new FormAttachment(wLimit, margin*2);
		wlFieldRownr.setLayoutData(fdlFieldRownr);
		wFieldRownr=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wFieldRownr);
		wFieldRownr.addModifyListener(lsMod);
		fdFieldRownr=new FormData();
		fdFieldRownr.left = new FormAttachment(wlFieldRownr, margin);
		fdFieldRownr.top  = new FormAttachment(wLimit, margin);
		fdFieldRownr.right= new FormAttachment(100, 0);
		wFieldRownr.setLayoutData(fdFieldRownr);
		
		// Some buttons
		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(Messages.getString("System.Button.OK")); //$NON-NLS-1$
        wPreview=new Button(shell, SWT.PUSH);
        wPreview.setText(Messages.getString("System.Button.Preview")); //$NON-NLS-1$
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(Messages.getString("System.Button.Cancel")); //$NON-NLS-1$
		
		setButtonPositions(new Button[] { wOK, wPreview, wCancel }, margin, null);

		// Add listeners
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel(); } };
        lsPreview  = new Listener() { public void handleEvent(Event e) { preview(); } };
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();     } };
		
		wCancel.addListener(SWT.Selection, lsCancel);
        wPreview.addListener (SWT.Selection, lsPreview);
		wOK.addListener    (SWT.Selection, lsOK    );
		
		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
		
		wStepname.addSelectionListener( lsDef );
		wLimit.addSelectionListener( lsDef );
		wFieldRownr.addSelectionListener( lsDef );
		
		wbFilename.addSelectionListener
		(
			new SelectionAdapter()
			{
				public void widgetSelected(SelectionEvent e) 
				{
					wConnectionString.setText("");
					FileDialog dialog = new FileDialog(shell, SWT.OPEN);

					if (wFilename.getText()!=null)
					{
						dialog.setFileName(wFilename.getText());
					}
						
					dialog.setFilterNames(new String[] {Messages.getString("OGRFileInputDialog.Filter.SHPFiles"), Messages.getString("System.FileType.AllFiles")}); //$NON-NLS-1$ //$NON-NLS-2$
					
					if (dialog.open()!=null)
					{
						String str = dialog.getFilterPath()+Const.FILE_SEPARATOR+dialog.getFileName();
						wFilename.setText(str);
					}
				}
			}
		);

		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );
		
		getData();
		input.setChanged(changed);

		// Set the shell size, based upon previous time...
		setSize();
		
		shell.open();
		while (!shell.isDisposed())
		{
				if (!display.readAndDispatch()) display.sleep();
		}
		return stepname;
	}
	
	protected void setFlags()
    {
        wlFieldRownr.setEnabled( wAddRownr.getSelection() );
        wFieldRownr.setEnabled( wAddRownr.getSelection() );

    }
	
	/**
	 * Copy information from the meta-data input to the dialog fields.
	 */ 
	public void getData()
	{
		if (input.getGisFileName() != null) 
		{
			wFilename.setText(input.getGisFileName());
			wFilename.setToolTipText(transMeta.environmentSubstitute(input.getGisFileName()));
		}
		
		if (input.getConnectionString() != null) 
		{
			wConnectionString.setText(input.getConnectionString());
		}
		
		if (input.getSpatialFilter() != null) 
		{
			wSpatialFilter.setText(input.getSpatialFilter());
		}

		if (input.getAttributeFilter() != null) 
		{
			wAttributeFilter.setText(input.getAttributeFilter());
		}
		
		wSkipFailure.setSelection(input.isSkipFailureAdded());
		wLimit.setText(Integer.toString(input.getRowLimit())); //$NON-NLS-1$
		wAddRownr.setSelection(input.isRowNrAdded());
		if (input.getRowNrField()!=null) wFieldRownr.setText(input.getRowNrField());
		
        setFlags();
		
		wStepname.selectAll();
		
		wFilename.addModifyListener(new ModifyListener()
		{
			public void modifyText(ModifyEvent arg0)
			{
				wFilename.setToolTipText(transMeta.environmentSubstitute(wFilename.getText()));
				//wConnectionString.setText("");
			}
		});
		
		wConnectionString.addModifyListener(new ModifyListener()
		{
			public void modifyText(ModifyEvent arg0)
			{
				wFilename.setText("");
			}
		});

	}
	
	private void cancel()
	{
		stepname=null;
		input.setRowNrAdded( backupSkipFailure );
		input.setRowNrAdded( backupAddRownr );
		input.setChanged(backupChanged);
		dispose();
	}
	
	public void getInfo(OGRFileInputMeta meta) throws KettleStepException
	{
		// copy info to Meta class (input)
		meta.setGisFileName( wFilename.getText() );
		meta.setConnectionString(wConnectionString.getText() );
		meta.setSpatialFilter(wSpatialFilter.getText() );
		meta.setAttributeFilter(wAttributeFilter.getText() );
		meta.setSkipFailureAdded( wSkipFailure.getSelection() );
		meta.setRowLimit( Const.toInt(wLimit.getText(), 0 ) );
        meta.setRowNrAdded( wAddRownr.getSelection() );
		meta.setRowNrField( wFieldRownr.getText() );

		if (Const.isEmpty(meta.getGisFileName()) && Const.isEmpty(meta.getConnectionString()))
		{
			throw new KettleStepException(Messages.getString("OGRFileInputDialog.Exception.SpecifyAFileToUse")); //$NON-NLS-1$
		}
	}
	
	private void ok()
	{
		try
		{
			stepname = wStepname.getText(); // return value
			getInfo(input);
			dispose();
		}
		catch(KettleStepException e)
		{
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
			mb.setMessage(e.toString());
			mb.setText(Messages.getString("System.Warning")); //$NON-NLS-1$
			mb.open();
			
			// Close anyway!
			dispose();
		}
	}
	
    // Preview the data
    private void preview()
    {
        // Create the XML input step
    	try
    	{
	        OGRFileInputMeta oneMeta = new OGRFileInputMeta();
	        getInfo(oneMeta);
            
            TransMeta previewMeta = TransPreviewFactory.generatePreviewTransformation(transMeta, oneMeta, wStepname.getText());
	        
	        EnterNumberDialog numberDialog = new EnterNumberDialog(shell, props.getDefaultPreviewSize(), Messages.getString("OGRFileInputDialog.PreviewSize.DialogTitle"), Messages.getString("OGRFileInputDialog.PreviewSize.DialogMessage")); //$NON-NLS-1$ //$NON-NLS-2$
	        int previewSize = numberDialog.open();
	        if (previewSize>0)
	        {
	            TransPreviewProgressDialog progressDialog = new TransPreviewProgressDialog(shell, previewMeta, new String[] { wStepname.getText() }, new int[] { previewSize } );
	            progressDialog.open();
	
	            Trans trans = progressDialog.getTrans();
	            String loggingText = progressDialog.getLoggingText();
	
	            if (!progressDialog.isCancelled())
	            {
	                if (trans.getResult()!=null && trans.getResult().getNrErrors()>0)
	                {
	                	EnterTextDialog etd = new EnterTextDialog(shell, Messages.getString("System.Dialog.PreviewError.Title"),   //$NON-NLS-1$
	                			Messages.getString("System.Dialog.PreviewError.Message"), loggingText, true ); //$NON-NLS-1$
	                	etd.setReadOnly();
	                	etd.open();
	                }
	            }
	            
	            PreviewRowsDialog prd =new PreviewRowsDialog(shell, transMeta, SWT.NONE, wStepname.getText(), progressDialog.getPreviewRowsMeta(wStepname.getText()), progressDialog.getPreviewRows(wStepname.getText()), loggingText);
	            prd.open();
	        }
    	}
    	catch(Exception e)
    	{
    		new ErrorDialog(shell, Messages.getString("System.Dialog.PreviewError.Title"),  //$NON-NLS-1$
    				Messages.getString("System.Dialog.PreviewError.Message"), e); //$NON-NLS-1$
    	}
    }
}
