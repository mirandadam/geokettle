/*************************************************************************************** 
 * Copyright (C) 2007 Samatar.  All rights reserved. 
 * This software was developed by Samatar and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. A copy of the license, 
 * is included with the binaries and source code. The Original Code is Samatar.  
 * The Initial Developer is Samatar.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an 
 * "AS IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. 
 * Please refer to the license for the specific language governing your rights 
 * and limitations.
 ***************************************************************************************/


package org.pentaho.di.ui.trans.steps.rssinput;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.graphics.Cursor;


import org.pentaho.di.core.Props;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.Const;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.TransPreviewFactory;
import org.pentaho.di.ui.trans.dialog.TransPreviewProgressDialog;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.trans.steps.rssinput.RssInputMeta;
import org.pentaho.di.trans.steps.rssinput.RssInputField;
import org.pentaho.di.trans.steps.rssinput.Messages;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.core.dialog.EnterNumberDialog;
import org.pentaho.di.ui.core.dialog.EnterTextDialog;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.dialog.PreviewRowsDialog;

import org.eclipse.swt.widgets.Group;


public class RssInputDialog extends BaseStepDialog implements StepDialogInterface
{
	private CTabFolder   wTabFolder;
	private FormData     fdTabFolder;
	
	private CTabItem     wGeneralTab, wContentTab, wFieldsTab;

	private Composite    wGeneralComp, wContentComp, wFieldsComp;
	private FormData     fdGeneralComp, fdContentComp, fdFieldsComp;

	
	private Label        wlUrlList;
	private TableView    wUrlList;
	private FormData     fdlUrlList, fdUrlList;


	private Label        wlInclRownum;
	private Button       wInclRownum;
	private FormData     fdlInclRownum, fdRownum;
	
	private Label        wlInclRownumField;
	private TextVar      wInclRownumField;
	private FormData     fdlInclRownumField, fdInclRownumField,fdAdditional;
	
	
	private Label        wlInclUrl;
	private Button       wInclUrl;
	private FormData     fdlInclUrl,fdUrl;
	
	
	private Label        wlInclUrlField;
	private TextVar      wInclUrlField;
	private FormData     fdInclUrlField,fdlInclUrlField;
	
	
	private Label        wlLimit;
	private Text         wLimit;
	private FormData     fdlLimit, fdLimit;
   
	private Label        wlReadFrom;
	private Text         wReadFrom;
	private FormData     fdlReadFrom, fdReadFrom;
	
	private Group wAdditional;
	
	private TableView    wFields;
	private FormData     fdFields;

	private RssInputMeta input;
	
	private Group GroupUrlField ;
	
	private FormData fdGroupUrlField ,fdUrlField,fdUrlInField,fdlUrlInField,fdlUrlField;
	private Label wlUrlField, wlUrlInField ;
	private CCombo wUrlField;
	private Button wUrlInField;

	
	public static final int dateLengths[] = new int[]
	{
		23, 19, 14, 10, 10, 10, 10, 8, 8, 8, 8, 6, 6
	};

	public RssInputDialog(Shell parent, Object in, TransMeta transMeta, String sname)
	{
		super(parent, (BaseStepMeta)in, transMeta, sname);
		input=(RssInputMeta)in;
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
		changed = input.hasChanged();
		
		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText(Messages.getString("RssInputDialog.DialogTitle"));
		
		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Stepname line
		wlStepname=new Label(shell, SWT.RIGHT);
		wlStepname.setText(Messages.getString("System.Label.StepName"));
 		props.setLook(wlStepname);
		fdlStepname=new FormData();
		fdlStepname.left = new FormAttachment(0, 0);
		fdlStepname.top  = new FormAttachment(0, margin);
		fdlStepname.right= new FormAttachment(middle, -margin);
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

		wTabFolder = new CTabFolder(shell, SWT.BORDER);
 		props.setLook(wTabFolder, Props.WIDGET_STYLE_TAB);

		//////////////////////////
		// START OF GENERAL TAB   ///
		//////////////////////////
		wGeneralTab=new CTabItem(wTabFolder, SWT.NONE);
		wGeneralTab.setText(Messages.getString("RssInputDialog.General.Tab"));
		
		wGeneralComp = new Composite(wTabFolder, SWT.NONE);
 		props.setLook(wGeneralComp);

		FormLayout fileLayout = new FormLayout();
		fileLayout.marginWidth  = 3;
		fileLayout.marginHeight = 3;
		wGeneralComp.setLayout(fileLayout);
		
		
		
		
		// ///////////////////////////////
		// START OF URL FIELD GROUP  //
		///////////////////////////////// 

		GroupUrlField = new Group(wGeneralComp, SWT.SHADOW_NONE);
		props.setLook(GroupUrlField);
		GroupUrlField.setText(Messages.getString("RssInputDialog.GroupUrlField.Label"));
		
		FormLayout UrlFieldgroupLayout = new FormLayout();
		UrlFieldgroupLayout.marginWidth = 10;
		UrlFieldgroupLayout.marginHeight = 10;
		GroupUrlField.setLayout(UrlFieldgroupLayout);
		
		//Is URL defined in a Field		
		wlUrlInField = new Label(GroupUrlField, SWT.RIGHT);
		wlUrlInField.setText(Messages.getString("RssInputDialog.UrlInField.Label"));
		props.setLook(wlUrlInField);
		fdlUrlInField = new FormData();
		fdlUrlInField.left = new FormAttachment(0, 0);
		fdlUrlInField.top = new FormAttachment(0, margin);
		fdlUrlInField.right = new FormAttachment(middle, -margin);
		wlUrlInField.setLayoutData(fdlUrlInField);
		
		
		wUrlInField = new Button(GroupUrlField, SWT.CHECK);
		props.setLook(wUrlInField);
		wUrlInField.setToolTipText(Messages.getString("RssInputDialog.UrlInField.Tooltip"));
		fdUrlInField = new FormData();
		fdUrlInField.left = new FormAttachment(middle, margin);
		fdUrlInField.top = new FormAttachment(0, margin);
		wUrlInField.setLayoutData(fdUrlInField);		
		SelectionAdapter lsurl = new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent arg0)
            {
            	ActiveUrlInField();
            	input.setChanged();
            }
        };
        wUrlInField.addSelectionListener(lsurl);
        
        
             
		// If URL defined in a Field
		wlUrlField=new Label(GroupUrlField, SWT.RIGHT);
        wlUrlField.setText(Messages.getString("RssInputDialog.UrlField.Label"));
        props.setLook(wlUrlField);
        fdlUrlField=new FormData();
        fdlUrlField.left = new FormAttachment(0, 0);
        fdlUrlField.top  = new FormAttachment(wUrlInField, margin);
        fdlUrlField.right= new FormAttachment(middle, -margin);
        wlUrlField.setLayoutData(fdlUrlField);
        
        
        wUrlField=new CCombo(GroupUrlField, SWT.BORDER | SWT.READ_ONLY);
        wUrlField.setEditable(true);
        props.setLook(wUrlField);
        wUrlField.addModifyListener(lsMod);
        fdUrlField=new FormData();
        fdUrlField.left = new FormAttachment(middle, margin);
        fdUrlField.top  = new FormAttachment(wUrlInField, margin);
        fdUrlField.right= new FormAttachment(100, -margin);
        wUrlField.setLayoutData(fdUrlField);
        wUrlField.addFocusListener(new FocusListener()
            {
                public void focusLost(org.eclipse.swt.events.FocusEvent e)
                {
                }
            
                public void focusGained(org.eclipse.swt.events.FocusEvent e)
                {
                    Cursor busy = new Cursor(shell.getDisplay(), SWT.CURSOR_WAIT);
                    shell.setCursor(busy);
                    setURLPreviousField();
                    shell.setCursor(null);
                    busy.dispose();
                }
            }
        );           	
        
		fdGroupUrlField = new FormData();
		fdGroupUrlField.left = new FormAttachment(0, margin);
		fdGroupUrlField.top = new FormAttachment(wStepname, margin);
		fdGroupUrlField.right = new FormAttachment(100, -margin);
		GroupUrlField.setLayoutData(fdGroupUrlField);
		
		// ///////////////////////////////////////////////////////////
		// / END OF Output Field GROUP
		// ///////////////////////////////////////////////////////////		

 		
 		// URL List line
		wlUrlList=new Label(wGeneralComp, SWT.RIGHT);
		wlUrlList.setText(Messages.getString("RssInputDialog.UrlList.Label"));
 		props.setLook(wlUrlList);
		fdlUrlList=new FormData();
		fdlUrlList.left = new FormAttachment(0, 0);
		//fdlUrlList.right = new FormAttachment(middle, -margin);
		fdlUrlList.top  = new FormAttachment(GroupUrlField, margin);
		wlUrlList.setLayoutData(fdlUrlList);
 			
 		ColumnInfo[] colinfo=new ColumnInfo[1];
		colinfo[0]=new ColumnInfo( Messages.getString("RssInputDialog.Url"),ColumnInfo.COLUMN_TYPE_TEXT,false);
		colinfo[0].setUsingVariables(true);
		colinfo[0].setToolTip(Messages.getString("RssInputDialog.Url.Tooltip"));
		
		wUrlList = new TableView(transMeta,wGeneralComp, SWT.FULL_SELECTION | SWT.SINGLE | SWT.BORDER, colinfo, 2,  lsMod, props);
 		props.setLook(wUrlList);
		fdUrlList=new FormData();
		fdUrlList.left   = new FormAttachment(0, 0);
		fdUrlList.right  = new FormAttachment(100, -margin);
		fdUrlList.top    = new FormAttachment(wlUrlList, 10);
		fdUrlList.bottom = new FormAttachment(100, -margin);
		wUrlList.setLayoutData(fdUrlList);
		
       
		fdGeneralComp=new FormData();
		fdGeneralComp.left  = new FormAttachment(0, 0);
		fdGeneralComp.top   = new FormAttachment(0, 0);
		fdGeneralComp.right = new FormAttachment(100, 0);
		fdGeneralComp.bottom= new FormAttachment(100, 0);
		wGeneralComp.setLayoutData(fdGeneralComp);
	
		wGeneralComp.layout();
		wGeneralTab.setControl(wGeneralComp);
		
		/////////////////////////////////////////////////////////////
		/// END OF FILE TAB
		/////////////////////////////////////////////////////////////
		
		//////////////////////////
		// START OF CONTENT TAB///
		///
		wContentTab=new CTabItem(wTabFolder, SWT.NONE);
		wContentTab.setText(Messages.getString("RssInputDialog.Content.Tab"));

		FormLayout contentLayout = new FormLayout ();
		contentLayout.marginWidth  = 3;
		contentLayout.marginHeight = 3;
		
		wContentComp = new Composite(wTabFolder, SWT.NONE);
 		props.setLook(wContentComp);
		wContentComp.setLayout(contentLayout);
		
		wlReadFrom=new Label(wContentComp, SWT.RIGHT);
		wlReadFrom.setText(Messages.getString("RssInputDialog.ReadFrom.Label"));
 		props.setLook(wlReadFrom);
		fdlReadFrom=new FormData();
		fdlReadFrom.left = new FormAttachment(0, 0);
		fdlReadFrom.top  = new FormAttachment(wUrlList, margin);
		fdlReadFrom.right= new FormAttachment(middle, -margin);
		wlReadFrom.setLayoutData(fdlReadFrom);
		wReadFrom=new Text(wContentComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wReadFrom.setToolTipText(Messages.getString("RssInputDialog.ReadFrom.Tooltip"));
 		props.setLook(wReadFrom);
		wReadFrom.addModifyListener(lsMod);
		fdReadFrom=new FormData();
		fdReadFrom.left = new FormAttachment(middle, 0);
		fdReadFrom.top  = new FormAttachment(wUrlList, margin);
		fdReadFrom.right= new FormAttachment(100, 0);
		wReadFrom.setLayoutData(fdReadFrom);

		
 		wlLimit=new Label(wContentComp, SWT.RIGHT);
		wlLimit.setText(Messages.getString("RssInputDialog.Limit.Label"));
 		props.setLook(wlLimit);
		fdlLimit=new FormData();
		fdlLimit.left = new FormAttachment(0, 0);
		fdlLimit.top  = new FormAttachment(wReadFrom, margin);
		fdlLimit.right= new FormAttachment(middle, -margin);
		wlLimit.setLayoutData(fdlLimit);
		wLimit=new Text(wContentComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wLimit);
		wLimit.addModifyListener(lsMod);
		fdLimit=new FormData();
		fdLimit.left = new FormAttachment(middle, 0);
		fdLimit.top  = new FormAttachment(wReadFrom, margin);
		fdLimit.right= new FormAttachment(100, 0);
		wLimit.setLayoutData(fdLimit);

		
		// ///////////////////////////////////
		// START OF Additional Field GROUP  //
		////////////////////////////////////// 

		wAdditional = new Group(wContentComp, SWT.SHADOW_NONE);
		props.setLook(wAdditional);
		wAdditional.setText(Messages.getString("RssInputDialog.Group.AdditionalGroup.Label"));
		
		FormLayout AdditionalgroupLayout = new FormLayout();
		AdditionalgroupLayout.marginWidth = 10;
		AdditionalgroupLayout.marginHeight = 10;
		wAdditional.setLayout(AdditionalgroupLayout);
		
		// Include Url in output ?
		wlInclUrl=new Label(wAdditional, SWT.RIGHT);
		wlInclUrl.setText(Messages.getString("RssInputDialog.InclUrl.Label"));
 		props.setLook(wlInclUrl);
		fdlInclUrl=new FormData();
		fdlInclUrl.left = new FormAttachment(0, 0);
		fdlInclUrl.top  = new FormAttachment(wLimit, margin);
		fdlInclUrl.right= new FormAttachment(middle, -margin);
		wlInclUrl.setLayoutData(fdlInclUrl);
		wInclUrl=new Button(wAdditional, SWT.CHECK );
 		props.setLook(wInclUrl);
		wInclUrl.setToolTipText(Messages.getString("RssInputDialog.InclUrl.Tooltip"));
		fdUrl=new FormData();
		fdUrl.left = new FormAttachment(middle, 0);
		fdUrl.top  = new FormAttachment(wLimit, margin);
		wInclUrl.setLayoutData(fdUrl);
		
		// Url fieldname
		wlInclUrlField=new Label(wAdditional, SWT.RIGHT);
		wlInclUrlField.setText(Messages.getString("RssInputDialog.InclUrlField.Label"));
 		props.setLook(wlInclUrlField);
		fdlInclUrlField=new FormData();
		fdlInclUrlField.left = new FormAttachment(wInclUrl, margin);
		fdlInclUrlField.top  = new FormAttachment(wLimit, margin);
		wlInclUrlField.setLayoutData(fdlInclUrlField);
		wInclUrlField=new TextVar(transMeta,wAdditional, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wInclUrlField);
		wInclUrlField.addModifyListener(lsMod);
		fdInclUrlField=new FormData();
		fdInclUrlField.left = new FormAttachment(wlInclUrlField, margin);
		fdInclUrlField.top  = new FormAttachment(wLimit, margin);
		fdInclUrlField.right= new FormAttachment(100, 0);
		wInclUrlField.setLayoutData(fdInclUrlField);
		
		// Include rownumber in ouput?
		wlInclRownum=new Label(wAdditional, SWT.RIGHT);
		wlInclRownum.setText(Messages.getString("RssInputDialog.InclRownum.Label"));
 		props.setLook(wlInclRownum);
		fdlInclRownum=new FormData();
		fdlInclRownum.left = new FormAttachment(0, 0);
		fdlInclRownum.top  = new FormAttachment(wInclUrl, margin);
		fdlInclRownum.right= new FormAttachment(middle, -margin);
		wlInclRownum.setLayoutData(fdlInclRownum);
		wInclRownum=new Button(wAdditional, SWT.CHECK );
 		props.setLook(wInclRownum);
		wInclRownum.setToolTipText(Messages.getString("RssInputDialog.InclRownum.Tooltip"));
		fdRownum=new FormData();
		fdRownum.left = new FormAttachment(middle, 0);
		fdRownum.top  = new FormAttachment(wInclUrl, margin);
		wInclRownum.setLayoutData(fdRownum);

		// Rownum fieldname
		wlInclRownumField=new Label(wAdditional, SWT.RIGHT);
		wlInclRownumField.setText(Messages.getString("RssInputDialog.InclRownumField.Label"));
 		props.setLook(wlInclRownumField);
		fdlInclRownumField=new FormData();
		fdlInclRownumField.left = new FormAttachment(wInclRownum, margin);
		fdlInclRownumField.top  = new FormAttachment(wInclUrl, margin);
		wlInclRownumField.setLayoutData(fdlInclRownumField);
		wInclRownumField=new TextVar(transMeta,wAdditional, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wInclRownumField);
		wInclRownumField.addModifyListener(lsMod);
		fdInclRownumField=new FormData();
		fdInclRownumField.left = new FormAttachment(wlInclRownumField, margin);
		fdInclRownumField.top  = new FormAttachment(wInclUrl, margin);
		fdInclRownumField.right= new FormAttachment(100, 0);
		wInclRownumField.setLayoutData(fdInclRownumField);
		
		fdAdditional = new FormData();
		fdAdditional.left = new FormAttachment(0, margin);
		fdAdditional.top = new FormAttachment(wLimit, margin);
		fdAdditional.right = new FormAttachment(100, -margin);
		wAdditional.setLayoutData(fdAdditional);
		
		// ///////////////////////////////////////////////////////////
		// / END OF AdditionalField GROUP
		// ///////////////////////////////////////////////////////////		

		fdContentComp = new FormData();
		fdContentComp.left  = new FormAttachment(0, 0);
		fdContentComp.top   = new FormAttachment(0, 0);
		fdContentComp.right = new FormAttachment(100, 0);
		fdContentComp.bottom= new FormAttachment(100, 0);
		wContentComp.setLayoutData(fdContentComp);

		wContentComp.layout();
		wContentTab.setControl(wContentComp);


		// ///////////////////////////////////////////////////////////
		// / END OF CONTENT TAB
		// ///////////////////////////////////////////////////////////


		// Fields tab...
		//
		wFieldsTab = new CTabItem(wTabFolder, SWT.NONE);
		wFieldsTab.setText(Messages.getString("RssInputDialog.Fields.Tab"));
		
		FormLayout fieldsLayout = new FormLayout ();
		fieldsLayout.marginWidth  = Const.FORM_MARGIN;
		fieldsLayout.marginHeight = Const.FORM_MARGIN;
		
		wFieldsComp = new Composite(wTabFolder, SWT.NONE);
		wFieldsComp.setLayout(fieldsLayout);
 		props.setLook(wFieldsComp);
		
 		wGet=new Button(wFieldsComp, SWT.PUSH);
		wGet.setText(Messages.getString("RssInputDialog.GetFields.Button"));
		fdGet=new FormData();
		fdGet.left=new FormAttachment(50, 0);
		fdGet.bottom =new FormAttachment(100, 0);
		wGet.setLayoutData(fdGet);
		
		final int FieldsRows=input.getInputFields().length;
		
		// Prepare a list of possible formats...
		String dats[] = Const.getDateFormats();
		String nums[] = Const.getNumberFormats();
		int totsize = dats.length + nums.length;
		String formats[] = new String[totsize];
		for (int x=0;x<dats.length;x++) formats[x] = dats[x];
		for (int x=0;x<nums.length;x++) formats[dats.length+x] = nums[x];
		
		
		ColumnInfo[] colinf=new ColumnInfo[]
            {
			 new ColumnInfo(
         Messages.getString("RssInputDialog.Field.Name"),
         ColumnInfo.COLUMN_TYPE_TEXT,
         false),
         new ColumnInfo(
                 Messages.getString("RssInputDialog.Field.Column"),
                 ColumnInfo.COLUMN_TYPE_CCOMBO,RssInputField.ColumnDesc,
                 false),
			 new ColumnInfo(
         Messages.getString("RssInputDialog.Field.Type"),
         ColumnInfo.COLUMN_TYPE_CCOMBO,
         ValueMeta.getTypes(),
         true ),
			 new ColumnInfo(
         Messages.getString("RssInputDialog.Field.Format"),
         ColumnInfo.COLUMN_TYPE_CCOMBO,
         formats),
			 new ColumnInfo(
         Messages.getString("RssInputDialog.Field.Length"),
         ColumnInfo.COLUMN_TYPE_TEXT,
         false),
			 new ColumnInfo(
         Messages.getString("RssInputDialog.Field.Precision"),
         ColumnInfo.COLUMN_TYPE_TEXT,
         false),
			 new ColumnInfo(
         Messages.getString("RssInputDialog.Field.Currency"),
         ColumnInfo.COLUMN_TYPE_TEXT,
         false),
			 new ColumnInfo(
         Messages.getString("RssInputDialog.Field.Decimal"),
         ColumnInfo.COLUMN_TYPE_TEXT,
         false),
			 new ColumnInfo(
         Messages.getString("RssInputDialog.Field.Group"),
         ColumnInfo.COLUMN_TYPE_TEXT,
         false),
			 new ColumnInfo(
         Messages.getString("RssInputDialog.Field.TrimType"),
         ColumnInfo.COLUMN_TYPE_CCOMBO,
         RssInputField.trimTypeDesc,
         true ),
			 new ColumnInfo(
         Messages.getString("RssInputDialog.Field.Repeat"),
         ColumnInfo.COLUMN_TYPE_CCOMBO,
         new String[] { Messages.getString("System.Combo.Yes"), Messages.getString("System.Combo.No") },
         true ),
     
    };
		
		colinf[0].setUsingVariables(true);
		colinf[0].setToolTip(Messages.getString("RssInputDialog.Field.Name.Tooltip"));
		colinf[1].setUsingVariables(true);
		colinf[1].setToolTip(Messages.getString("RssInputDialog.Field.Column.Tooltip"));
		
		wFields=new TableView(transMeta,wFieldsComp, 
						      SWT.FULL_SELECTION | SWT.MULTI, 
						      colinf, 
						      FieldsRows,  
						      lsMod,
							  props
						      );

		fdFields=new FormData();
		fdFields.left  = new FormAttachment(0, 0);
		fdFields.top   = new FormAttachment(0, 0);
		fdFields.right = new FormAttachment(100, 0);
		fdFields.bottom= new FormAttachment(wGet, -margin);
		wFields.setLayoutData(fdFields);

		fdFieldsComp=new FormData();
		fdFieldsComp.left  = new FormAttachment(0, 0);
		fdFieldsComp.top   = new FormAttachment(0, 0);
		fdFieldsComp.right = new FormAttachment(100, 0);
		fdFieldsComp.bottom= new FormAttachment(100, 0);
		wFieldsComp.setLayoutData(fdFieldsComp);
		
		wFieldsComp.layout();
		wFieldsTab.setControl(wFieldsComp);
		
		fdTabFolder = new FormData();
		fdTabFolder.left  = new FormAttachment(0, 0);
		fdTabFolder.top   = new FormAttachment(wStepname, margin);
		fdTabFolder.right = new FormAttachment(100, 0);
		fdTabFolder.bottom= new FormAttachment(100, -50);
		wTabFolder.setLayoutData(fdTabFolder);
		
		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(Messages.getString("System.Button.OK"));

		wPreview=new Button(shell, SWT.PUSH);
		wPreview.setText(Messages.getString("RssInputDialog.Button.PreviewRows"));
		
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(Messages.getString("System.Button.Cancel"));
		
		setButtonPositions(new Button[] { wOK, wPreview, wCancel }, margin, wTabFolder);

		// Add listeners
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();     } };
		lsGet      = new Listener() { public void handleEvent(Event e) { get();      } };
		lsPreview  = new Listener() { public void handleEvent(Event e) { preview();   } };
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel();     } };
		
		wOK.addListener     (SWT.Selection, lsOK     );
		wGet.addListener    (SWT.Selection, lsGet    );
		wPreview.addListener(SWT.Selection, lsPreview);
		wCancel.addListener (SWT.Selection, lsCancel );
		
		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
		
		wStepname.addSelectionListener( lsDef );
		wLimit.addSelectionListener( lsDef );
		wInclRownumField.addSelectionListener( lsDef );
		
	
		//wFilename.addSelectionListener(selA);
	

		
		// Enable/disable the right fields to allow a row number to be added to each row...
		wInclRownum.addSelectionListener(new SelectionAdapter() 
			{
				public void widgetSelected(SelectionEvent e) 
				{
					setIncludeRownum();
				}
			}
		);
		
		// Enable/disable the right fields to allow a row number to be added to each row...
		wInclUrl.addSelectionListener(new SelectionAdapter() 
			{
				public void widgetSelected(SelectionEvent e) 
				{
					setIncludeUrl();
				}
			}
		);
		
		
		
		
		
		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );

		wTabFolder.setSelection(0);

		// Set the shell size, based upon previous time...
		setSize();
		getData(input);
		ActiveUrlInField();
		input.setChanged(changed);
		wFields.optWidth(true);
		
		shell.open();
		while (!shell.isDisposed())
		{
				if (!display.readAndDispatch()) display.sleep();
		}
		return stepname;
	}

	private void ActiveUrlInField()
	{
		wUrlList.setEnabled(!wUrlInField.getSelection());
		wlUrlList.setEnabled(!wUrlInField.getSelection());
		wUrlField.setEnabled(wUrlInField.getSelection());
		wlUrlField.setEnabled(wUrlInField.getSelection());
		wPreview.setEnabled(!wUrlInField.getSelection());
		
		if(wUrlInField.getSelection())
		{
			wInclUrlField.setText("");
			wInclUrl.setSelection(false);
		}
		wInclUrlField.setEnabled(!wUrlInField.getSelection());
		wInclUrl.setEnabled(!wUrlInField.getSelection());
		
	}
	 private void setURLPreviousField()
	 {
		 try{
	           
			 wUrlField.removeAll();
				
			 RowMetaInterface r = transMeta.getPrevStepFields(stepname);
				if (r!=null)
				{
		             r.getFieldNames();
		             
		             for (int i=0;i<r.getFieldNames().length;i++)
						{	
		            	 wUrlField.add(r.getFieldNames()[i]);					
							
						}
				}
			 
			
		 }catch(KettleException ke){
				new ErrorDialog(shell, Messages.getString("RssInputDialog.FailedToGetFields.DialogTitle"), Messages.getString("RssInputDialog.FailedToGetFields.DialogMessage"), ke); //$NON-NLS-1$ //$NON-NLS-2$
			}
	 }
	 
	
	private void get()
	{
		
		try{
        	RssInputMeta meta = new RssInputMeta();
        	getInfo(meta);
        	
        	// Check for URL
        	if(!checkInputURL(meta)) return;
   
        	// Clear Fields Grid
            wFields.removeAll();
           
            wFields.add(new String[] { RssInputField.getColumnDesc(0), RssInputField.getColumnDesc(0), "String", "", "","","","","none", "N" } );
            wFields.add(new String[] { RssInputField.getColumnDesc(1), RssInputField.getColumnDesc(1), "String", "", "","","","","none", "N" } );
            wFields.add(new String[] { RssInputField.getColumnDesc(2), RssInputField.getColumnDesc(2), "String", "", "","","","","none", "N" } );
            wFields.add(new String[] { RssInputField.getColumnDesc(3), RssInputField.getColumnDesc(3), "String", "", "","","","","none", "N" } );
            wFields.add(new String[] { RssInputField.getColumnDesc(4), RssInputField.getColumnDesc(4), "String", "", "","","","","none", "N" } );
            wFields.add(new String[] { RssInputField.getColumnDesc(5), RssInputField.getColumnDesc(5), "String", "", "","","","","none", "N" } );
            wFields.add(new String[] { RssInputField.getColumnDesc(6), RssInputField.getColumnDesc(6), "String", "", "","","","","none", "N" } );
          
            wFields.removeEmptyRows();
			wFields.setRowNums();
			wFields.optWidth(true);
		}
		 catch(Exception e)
	     {
	            new ErrorDialog(shell, Messages.getString("RssInputDialog.ErrorGettingFields.DialogTitle"), Messages.getString("getXMLDataDialog.ErrorGettingFields.DialogMessage"), e);
	     }
	}

	


	public void setIncludeRownum()
	{
		wlInclRownumField.setEnabled(wInclRownum.getSelection());
		wInclRownumField.setEnabled(wInclRownum.getSelection());
	}

	public void setIncludeUrl()
	{
		wlInclUrlField.setEnabled(wInclUrl.getSelection());
		wInclUrlField.setEnabled(wInclUrl.getSelection());
	}

	/**
	 * Read the data from the TextFileInputMeta object and show it in this dialog.
	 * 
	 * @param in The TextFileInputMeta object to obtain the data from.
	 */
	public void getData(RssInputMeta in)
	{
		if (in.getReadFrom()!=null) wReadFrom.setText(in.getReadFrom());
		
		if (in.getUrl() !=null) 
		{
			wUrlList.removeAll();
			for (int i=0;i<in.getUrl().length;i++) 
			{
				wUrlList.add(new String[] {in.getUrl()[i]});
			}
			wUrlList.removeEmptyRows();
			wUrlList.setRowNums();
			wUrlList.optWidth(true);
		}
		
		
		wUrlInField.setSelection(in.urlInField());
		if (in.getUrlFieldname()!=null) wUrlField.setText(in.getUrlFieldname());
		
		
		wInclRownum.setSelection(in.includeRowNumber());
		if (in.getRowNumberField()!=null) wInclRownumField.setText(in.getRowNumberField());
		
		wInclUrl.setSelection(in.includeUrl());
		if (in.geturlField()!=null) wInclUrlField.setText(in.geturlField());
		
		wLimit.setText(""+in.getRowLimit());

		log.logDebug(toString(), Messages.getString("RssInputDialog.Log.GettingFieldsInfo"));
		for (int i=0;i<in.getInputFields().length;i++)
		{
		    RssInputField field = in.getInputFields()[i];
		    
            if (field!=null)
            {
    			TableItem item  = wFields.table.getItem(i);
    			String name     = field.getName();
    			String column	= field.getColumnDesc();
    			String type     = field.getTypeDesc();
    			String format   = field.getFormat();
    			String length   = ""+field.getLength();
    			String prec     = ""+field.getPrecision();
    			String curr     = field.getCurrencySymbol();
    			String group    = field.getGroupSymbol();
    			String decim    = field.getDecimalSymbol();
    			String trim     = field.getTrimTypeDesc();
    			String rep      = field.isRepeated()?Messages.getString("System.Combo.Yes"):Messages.getString("System.Combo.No");
    			
                if (name    !=null) item.setText( 1, name);
                if (column  !=null) item.setText( 2, column);
    			if (type    !=null) item.setText( 3, type);
    			if (format  !=null) item.setText( 4, format);
    			if (length  !=null && !"-1".equals(length)) item.setText( 5, length);
    			if (prec    !=null && !"-1".equals(prec)) item.setText( 6, prec);
    			if (curr    !=null) item.setText( 7, curr);
    			if (decim   !=null) item.setText( 8, decim);
    			if (group   !=null) item.setText( 9, group);
    			if (trim    !=null) item.setText(10, trim);
    			if (rep     !=null) item.setText(11, rep);                
            }
		}
        
        wFields.removeEmptyRows();
        wFields.setRowNums();
        wFields.optWidth(true);

		setIncludeRownum();
		setIncludeUrl();

		wStepname.selectAll();
	}

	private void cancel()
	{
		stepname=null;
		input.setChanged(changed);
		dispose();
	}
	
	private void ok()
	{
		if (Const.isEmpty(wStepname.getText())) return;

        try
        {
            getInfo(input);
        }
        catch(KettleException e)
        {
            new ErrorDialog(shell, Messages.getString("RssInputDialog.ErrorParsingData.DialogTitle"), Messages.getString("RssInputDialog.ErrorParsingData.DialogMessage"), e);
        }
		dispose();
	}
	
	private void getInfo(RssInputMeta in) throws KettleException
	{
		stepname = wStepname.getText(); // return value

		in.setReadFrom( wReadFrom.getText() );
		
		in.seturlInField( wUrlInField.getSelection() );
		
		in.setUrlFieldname(wUrlField.getText() );
		
		in.setRowLimit( Const.toLong(wLimit.getText(), 0L) );
		in.setIncludeRowNumber( wInclRownum.getSelection() );
		in.setRowNumberField( wInclRownumField.getText() );
		
		in.setIncludeUrl( wInclUrl.getSelection() );
		in.seturlField( wInclUrlField.getText() );

		int nrFields    = wFields.nrNonEmpty();
		int nrUrls      = wUrlList.nrNonEmpty();
         
		in.allocate(nrUrls,nrFields);
		

		in.setUrl( wUrlList.getItems(0) );
	
		
		
		for (int i=0;i<nrFields;i++)
		{
		    RssInputField field = new RssInputField();
		    
			TableItem item  = wFields.getNonEmpty(i);
            
			field.setName( item.getText(1) );
			field.setColumn( RssInputField.getColumnByDesc(item.getText(2)) );
			field.setType( ValueMeta.getType(item.getText(3)) );
			field.setFormat( item.getText(4) );
			field.setLength( Const.toInt(item.getText(5), -1) );
			field.setPrecision( Const.toInt(item.getText(6), -1) );
			field.setCurrencySymbol( item.getText(7) );
			field.setDecimalSymbol( item.getText(8) );
			field.setGroupSymbol( item.getText(9) );
			field.setTrimType( RssInputField.getTrimTypeByDesc(item.getText(10)) );
			field.setRepeated( Messages.getString("System.Combo.Yes").equalsIgnoreCase(item.getText(11)) );		
            
			in.getInputFields()[i] = field;
		}		 
	}
	
	// check at least one Url is given
	private boolean checkInputURL(RssInputMeta meta)
	{
        if (wUrlList.nrNonEmpty()<1)
        {
            MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
            mb.setMessage(Messages.getString("RssInput.Log.UrlMissing"));
            mb.setText(Messages.getString("System.Dialog.Error.Title"));
            mb.open(); 

            return false;
        }
        else
        {
        	return true;
        }
	
	}
	
	// Preview the data
	private void preview()
	{
        try
        {
            // Create the XML input step
        	RssInputMeta oneMeta = new RssInputMeta();
            getInfo(oneMeta);
            
            // check if the path is given
    		//if (!checkLoopXPath(oneMeta)) return;
    		 TransMeta previewMeta = TransPreviewFactory.generatePreviewTransformation(transMeta, oneMeta, wStepname.getText());
            
            EnterNumberDialog numberDialog = new EnterNumberDialog(shell, props.getDefaultPreviewSize(), Messages.getString("getXMLDataDialog.NumberRows.DialogTitle"), Messages.getString("getXMLDataDialog.NumberRows.DialogMessage"));
            
            int previewSize = numberDialog.open();
            if (previewSize>0)
            {
                TransPreviewProgressDialog progressDialog = new TransPreviewProgressDialog(shell, previewMeta, new String[] { wStepname.getText() }, new int[] { previewSize } );
                progressDialog.open();
                
                if (!progressDialog.isCancelled())
                {
                    Trans trans = progressDialog.getTrans();
                    String loggingText = progressDialog.getLoggingText();
                    
                    if (trans.getResult()!=null && trans.getResult().getNrErrors()>0)
                    {
                    	EnterTextDialog etd = new EnterTextDialog(shell, Messages.getString("System.Dialog.PreviewError.Title"),  
                    			Messages.getString("System.Dialog.PreviewError.Message"), loggingText, true );
                    	etd.setReadOnly();
                    	etd.open();
                    }
                             
                    PreviewRowsDialog prd = new PreviewRowsDialog(shell, transMeta, SWT.NONE, wStepname.getText(),
							progressDialog.getPreviewRowsMeta(wStepname.getText()), progressDialog
									.getPreviewRows(wStepname.getText()), loggingText);
					prd.open();
                    
                }
            }
        }
        catch(KettleException e)
        {
            new ErrorDialog(shell, Messages.getString("RssInputDialog.ErrorPreviewingData.DialogTitle"), Messages.getString("RssInputDialog.ErrorPreviewingData.DialogMessage"), e);
        }
	}

	public String toString()
	{
		return this.getClass().getName();
	}
	
	
}