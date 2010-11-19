package org.pentaho.di.ui.trans.steps.webservices;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.xml.namespace.QName;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.steps.webservices.Messages;
import org.pentaho.di.trans.steps.webservices.WebServiceField;
import org.pentaho.di.trans.steps.webservices.WebServiceMeta;
import org.pentaho.di.trans.steps.webservices.wsdl.ComplexType;
import org.pentaho.di.trans.steps.webservices.wsdl.Wsdl;
import org.pentaho.di.trans.steps.webservices.wsdl.WsdlOpParameter;
import org.pentaho.di.trans.steps.webservices.wsdl.WsdlOpParameterContainer;
import org.pentaho.di.trans.steps.webservices.wsdl.WsdlOperation;
import org.pentaho.di.trans.steps.webservices.wsdl.WsdlOperationContainer;
import org.pentaho.di.trans.steps.webservices.wsdl.WsdlParamContainer;
import org.pentaho.di.trans.steps.webservices.wsdl.XsdType;
import org.pentaho.di.trans.steps.webservices.wsdl.WsdlOpParameter.ParameterMode;
import org.pentaho.di.ui.core.database.dialog.DatabaseDialog;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.step.BaseStepDialog;


public class WebServiceDialog extends BaseStepDialog implements StepDialogInterface
{
    private WebServiceMeta meta;

    private CTabFolder wTabFolder;

    private Label wlURL;
    private Button wbURL;
    private Button wbFile;
    private TextVar wURL;

    private Label wlOperation;
    private CCombo wOperation;

    private Label wlStep;
    private Text wStep;

    private Label wlPassInputData;
    private Button wPassInputData;

    private Label wlCompatible;
    private Button wCompatible;

    private Label wlRepeatingElement;
    private TextVar wRepeatingElement;

    private Label wlReplyAsString;
    private Button wReplyAsString;

    private Label wlHttpLogin;
    private TextVar wHttpLogin;

    private Label wlHttpPassword;
    private TextVar wHttpPassword;

    private Label wlProxyHost;
    private TextVar wProxyHost;

    private Label wlProxyPort;
    private TextVar wProxyPort;

    /** The input fields */
    private TableView fieldInTableView;

    /** The output fields */
    private TableView fieldOutTableView;

    /** Web service tab item */
    private CTabItem tabItemWebService;

    /** input fields tab item */
    private CTabItem tabItemFieldIn;

    /** output fields tab item*/
    private CTabItem tabItemFieldOut;

    /** WSDL*/
    private Wsdl wsdl;

    private WsdlOperation wsdlOperation;
    private WsdlParamContainer inWsdlParamContainer;
    private WsdlParamContainer outWsdlParamContainer;
    
    private Button wAddInput;
    private Button wAddOutput;
    
    
    private ModifyListener lsMod = new ModifyListener()
    {
        public void modifyText(ModifyEvent e)
        {
            meta.setChanged();
        }
    };

    private void selectWSDLOperation(String anOperationName) throws KettleStepException
    {
        // Tab management
    	// 
        loadOperation(anOperationName);

        // We close all tabs and reconstruct it all to make sure we always show the correct data
        // 
        if (inWsdlParamContainer != null)
        {
            wStep.setVisible(true);
            wlStep.setVisible(true);
            if (!inWsdlParamContainer.isArray())
            {
                wStep.setText("1");
                wStep.setEditable(false);
            }
            else
            {
                //wStep.setText(Integer.toString(WebServiceMeta.DEFAULT_STEP));
                wStep.setEditable(true);
            }
            addTabFieldIn();
            setComboValues();
        }
        else
        {
            wStep.setText("1");
            wStep.setEditable(false);
            wStep.setVisible(false);
            wlStep.setVisible(false);

            removeTabField(tabItemFieldIn);
            tabItemFieldIn = null;
        }
        if (outWsdlParamContainer != null)
        {
            addTabFieldOut();
        }
        else
        {
            removeTabField(tabItemFieldOut);
            tabItemFieldOut = null;
        }
    }

    private void loadWebService(String anURI) throws KettleStepException
    {
        anURI = transMeta.environmentSubstitute(anURI);

        // 
        //
        try
        {
            if (wProxyHost.getText() != null && !"".equals(wProxyHost.getText()))
            {
                Properties systemProperties = System.getProperties();
                systemProperties.setProperty("http.proxyHost", transMeta.environmentSubstitute(wProxyHost.getText()));
                systemProperties.setProperty("http.proxyPort", transMeta.environmentSubstitute(wProxyPort.getText()));
            }
            wsdl = new Wsdl(new URI(anURI), null, null);
        }
        catch (Exception e)
        {
            wsdl = null;
            new ErrorDialog(shell,
                            Messages.getString("WebServiceDialog.ERROR0009.UnreachableURI"),//$NON-NLS-1$
                            Messages.getString("WebServiceDialog.ErrorDialog.Title") + anURI, //$NON-NLS-1$ 
                            e); 

            log.logError(Messages.getString("WebServiceDialog.ErrorDialog.Title") + anURI, e.getMessage()); //$NON-NLS-1$
            return;
        }
        String text = wOperation.getText();
        wOperation.removeAll();
        if (wsdl != null)
        {
            List<WsdlOperation> listeOperations = wsdl.getOperations();
            Collections.sort(listeOperations, new Comparator<WsdlOperation>()
            {
                public int compare(WsdlOperation op1, WsdlOperation op2)
                {
                    return op1.getOperationQName().getLocalPart().compareTo(op2.getOperationQName().getLocalPart());
                }
            });
            for (Iterator<WsdlOperation> itr = listeOperations.iterator(); itr.hasNext();)
            {
                WsdlOperation op = itr.next();
                wOperation.add(op.getOperationQName().getLocalPart());
                if (op.getOperationQName().getLocalPart().equals(text))
                {
                    wOperation.setText(text);
                }
            }
        }

    }
    
    private void loadOperation(String anOperationName) throws KettleStepException
    {
        wsdlOperation = null;
        inWsdlParamContainer = null;
        outWsdlParamContainer = null;
        if (wsdl != null)
        {
            for (Iterator<WsdlOperation> vItOperation = wsdl.getOperations().iterator(); vItOperation.hasNext() && wsdlOperation == null;)
            {
                WsdlOperation vCurrentOperation = vItOperation.next();
                if (vCurrentOperation.getOperationQName().getLocalPart().equals(anOperationName))
                {
                    wsdlOperation = vCurrentOperation;
                }
            }
        }

        if (wsdlOperation != null)
        {
            for (int cpt = 0; cpt < wsdlOperation.getParameters().size(); cpt++)
            {
                WsdlOpParameter param = (WsdlOpParameter) wsdlOperation.getParameters().get(cpt);
                if (param.isArray())
                {
                    //setInFieldArgumentName(param.getName().getLocalPart());
                    if (param.getItemXmlType() != null)
                    {
                        ComplexType type = param.getItemComplexType();
                        if (type != null)
                        {
                            for (Iterator<String> itrType = type.getElementNames().iterator(); itrType.hasNext();)
                            {
                                String attributeName = (String) itrType.next();
                                QName attributeType = type.getElementType(attributeName);
                                if (!WebServiceMeta.XSD_NS_URI.equals(attributeType.getNamespaceURI()))
                                {
                                    throw new KettleStepException(Messages.getString("WebServiceDialog.ERROR0007.UnsupporteOperation.ComplexType")); //$NON-NLS-1$
                                }
                            }
                        }
                        if (ParameterMode.IN.equals(param.getMode()) || ParameterMode.INOUT.equals(param.getMode()) || ParameterMode.UNDEFINED.equals(param.getMode()))
                        {
                            if (inWsdlParamContainer != null)
                            {
                                throw new KettleStepException(Messages.getString("WebServiceDialog.ERROR0006.UnsupportedOperation.MultipleArrays")); //$NON-NLS-1$
                            }
                            else
                            {
                                inWsdlParamContainer = new WsdlOpParameterContainer(param);
                            }
                        }
                        else if (ParameterMode.OUT.equals(param.getMode()) || ParameterMode.INOUT.equals(param.getMode()) || ParameterMode.UNDEFINED.equals(param.getMode()))
                        {
                            if (outWsdlParamContainer != null)
                            {
                                throw new KettleStepException(Messages.getString("WebServiceDialog.ERROR0006.UnsupportedOperation.MultipleArrays")); //$NON-NLS-1$
                            }
                            else
                            {
                                outWsdlParamContainer = new WsdlOpParameterContainer(param);
                            }
                        }
                    }
                }
                else
                {
                    if (ParameterMode.IN.equals(param.getMode()) || ParameterMode.INOUT.equals(param.getMode()) || ParameterMode.UNDEFINED.equals(param.getMode()))
                    {
                        if (inWsdlParamContainer != null && !(inWsdlParamContainer instanceof WsdlOperationContainer))
                        {
                            throw new KettleStepException(Messages.getString("WebServiceDialog.ERROR0008.UnsupportedOperation.IncorrectParams")); //$NON-NLS-1$
                        }
                        else
                        {
                            inWsdlParamContainer = new WsdlOperationContainer(wsdlOperation, param.getMode());
                        }
                    }
                    else if (ParameterMode.OUT.equals(param.getMode()) || ParameterMode.INOUT.equals(param.getMode()) || ParameterMode.UNDEFINED.equals(param.getMode()))
                    {
                        if (outWsdlParamContainer != null && !(outWsdlParamContainer instanceof WsdlOperationContainer))
                        {
                            throw new KettleStepException(Messages.getString("WebServiceDialog.ERROR0008.UnsupportedOperation.IncorrectParams")); //$NON-NLS-1$
                        }
                        else
                        {
                            outWsdlParamContainer = new WsdlOperationContainer(wsdlOperation, param.getMode());
                        }
                    }
                    else {
                    	System.out.println("Parameter : "+param.getName().getLocalPart()+", mode="+param.getMode().toString()+", is not considered");
                    }
                }
            }
            if (wsdlOperation.getReturnType() != null)
            {
                outWsdlParamContainer = new WsdlOpParameterContainer((WsdlOpParameter) wsdlOperation.getReturnType());
                if (wsdlOperation.getReturnType().isArray())
                {
                    if (wsdlOperation.getReturnType().getItemXmlType() != null)
                    {
                        ComplexType type = wsdlOperation.getReturnType().getItemComplexType();
                        if (type != null)
                        {
                            for (Iterator<String> itrType = type.getElementNames().iterator(); itrType.hasNext();)
                            {
                                String attributeName = (String) itrType.next();
                                QName attributeType = type.getElementType(attributeName);
                                if (!WebServiceMeta.XSD_NS_URI.equals(attributeType.getNamespaceURI()))
                                {
                                    throw new KettleStepException(Messages.getString("WebServiceDialog.ERROR0007.UnsupportedOperation.ComplexType")); //$NON-NLS-1$
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    /**
     * Initialization of the tree:
     * - construction using the URL of the WS
     * - add selection listeners to the tree
     * @throws KettleStepException 
     *
     */
    private void initTreeTabWebService(String anURI) throws KettleStepException
    {
        String text = wOperation.getText();

        loadWebService(anURI);

        selectWSDLOperation(text);

        if (wsdlOperation != null)
        {
            wOperation.setText(text);
        }
    }

    private void addTabFieldIn()
    {
        TableView oldTableView = fieldInTableView;
        int margin = Const.MARGIN;
        
        Composite vCompositeTabField = new Composite(wTabFolder, SWT.NONE);
        FormLayout formLayout = new FormLayout();
        formLayout.marginWidth = Const.FORM_MARGIN;
        formLayout.marginHeight = Const.FORM_MARGIN;
        
        vCompositeTabField.setLayout(formLayout);
        props.setLook(vCompositeTabField);

        if (tabItemFieldIn == null)
        {
            tabItemFieldIn = new CTabItem(wTabFolder, SWT.NONE);
        }
        final ColumnInfo colNames = new ColumnInfo(Messages.getString("WebServiceDialog.NameColumn.Column"), //$NON-NLS-1$
                                                   ColumnInfo.COLUMN_TYPE_CCOMBO,
                                                   new String[] {},
                                                   false);
        fieldColumns.add(colNames);
        ColumnInfo[] colinf = new ColumnInfo[] {
            new ColumnInfo(Messages.getString("WebServiceDialog.NameColumn.Column"), //$NON-NLS-1$
		                   ColumnInfo.COLUMN_TYPE_TEXT,
		                   false),
            new ColumnInfo(Messages.getString("WebServiceDialog.WsNameColumn.Column"), //$NON-NLS-1$
                           ColumnInfo.COLUMN_TYPE_TEXT,
                           false),
            new ColumnInfo(Messages.getString("WebServiceDialog.TypeColumn.Column"), //$NON-NLS-1$
                           ColumnInfo.COLUMN_TYPE_TEXT,
                           false),
           };
        fieldInTableView = new TableView(transMeta, vCompositeTabField, SWT.FULL_SELECTION | SWT.MULTI, colinf, 1,  lsMod, props);
        fieldInTableView.setReadonly(false);
        fieldInTableView.clearAll();
        String containerName = inWsdlParamContainer == null ? meta.getInFieldContainerName() : inWsdlParamContainer.getContainerName();
        tabItemFieldIn.setText(containerName == null ? "in" : containerName);
        

        Button vButton = new Button(vCompositeTabField, SWT.NONE);
        vButton.setText(Messages.getString("System.Button.GetFields")); //$NON-NLS-1$
        vButton.addSelectionListener(new SelectionAdapter()
        {
        	@Override
        	public void widgetSelected(SelectionEvent event) {
                if (inWsdlParamContainer == null)
                {
                    try
                    {
                        loadWebService(wURL.getText());
                        loadOperation(wOperation.getText());
                    }
                    catch (KettleStepException e)
                    {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                RowMetaInterface r = getInWebServiceFields();
                if (r != null)
                {
                    BaseStepDialog.getFieldsFromPrevious(r, fieldInTableView, 2, new int[] { 2 }, new int[] {}, -1, -1, null);
                }
                // Define type for new entries
                if (inWsdlParamContainer != null)
                {
                    TableItem[] items = fieldInTableView.table.getItems();
                    for (int i = 0; i < items.length; i++)
                    {
                        String type = inWsdlParamContainer.getParamType(items[i].getText(2));
                        if (type != null)
                        {
                            items[i].setText(3, type);
                        }
                        else
                        {
                            items[i].dispose();
                        }
                    }
                }
            }
        });
        
        
        Button[] buttons = new Button[] {vButton};
        BaseStepDialog.positionBottomButtons(vCompositeTabField, buttons, Const.MARGIN, null);

        FormData fdTable = new FormData();
        fdTable.left = new FormAttachment(0, 0);
        fdTable.top = new FormAttachment(0, margin);
        fdTable.right = new FormAttachment(100, 0);
        fdTable.bottom = new FormAttachment(vButton, 0);
        fieldInTableView.setLayoutData(fdTable);

        FormData fdInComp=new FormData();
        fdInComp.left  = new FormAttachment(0, 0);
        fdInComp.top   = new FormAttachment(0, 0);
        fdInComp.right = new FormAttachment(100, 0);
        fdInComp.bottom= new FormAttachment(100, 0);
        vCompositeTabField.setLayoutData(fdInComp);
    
        vCompositeTabField.layout();

        tabItemFieldIn.setControl(vCompositeTabField);

        if (inWsdlParamContainer != null)
        {
            RowMetaInterface r = getInWebServiceFields();
            for (int i = 0; i < r.size(); ++i)
            {
                String wsName = r.getValueMeta(i).getName();
                TableItem vTableItem = new TableItem(fieldInTableView.table, SWT.NONE);
                vTableItem.setText(2, Const.NVL(wsName, ""));
            	vTableItem.setText(3, Const.NVL(inWsdlParamContainer.getParamType(wsName), ""));

                if (oldTableView != null)
                {
                    TableItem[] oldItems = oldTableView.table.getItems();
                    String previousField = getField(oldItems, wsName);
                    if (previousField != null)
                    {
                        vTableItem.setText(1, previousField);
                    }
                }
            }
        }
        if (oldTableView != null)
        {
            oldTableView.dispose();
        }
        fieldInTableView.removeEmptyRows();
        fieldInTableView.setRowNums();
        fieldInTableView.optWidth(true);
    }
    
    private String getField(TableItem[] items, String wsName)
    {
    	if (wsName==null) return null;
    	
        String ret = null;
        for (int i = 0; i < items.length && ret == null; i++)
        {
            if (items[i].getText(2).equals(wsName))
            {
                ret = items[i].getText(1);
            }
        }
        return ret;
    }

    private void addTabFieldOut()
    {
        TableView oldTableView = fieldOutTableView;
        int margin = Const.MARGIN;
        
        // Initialization of the output tab
        //
        Composite vCompositeTabFieldOut = new Composite(wTabFolder, SWT.NONE);
        FormLayout formLayout = new FormLayout();
        formLayout.marginWidth = Const.FORM_MARGIN;
        formLayout.marginHeight = Const.FORM_MARGIN;
        
        vCompositeTabFieldOut.setLayout(formLayout);
        props.setLook(vCompositeTabFieldOut);

        if (tabItemFieldOut == null)
        {
            tabItemFieldOut = new CTabItem(wTabFolder, SWT.NONE);
        }
        ColumnInfo[] colinf = new ColumnInfo[] {
        		new ColumnInfo(Messages.getString("WebServiceDialog.NameColumn.Column"), //$NON-NLS-1$
                               ColumnInfo.COLUMN_TYPE_TEXT,
                               false),
                new ColumnInfo(Messages.getString("WebServiceDialog.WsNameColumn.Column"), //$NON-NLS-1$
                               ColumnInfo.COLUMN_TYPE_TEXT,
                               false),
                new ColumnInfo(Messages.getString("WebServiceDialog.TypeColumn.Column"), //$NON-NLS-1$
                               ColumnInfo.COLUMN_TYPE_TEXT,
                               false)
        	};
        fieldOutTableView = new TableView(transMeta, vCompositeTabFieldOut, SWT.FULL_SELECTION | SWT.MULTI, colinf, 1,  lsMod, props);
        String outContainerName = outWsdlParamContainer == null ? meta.getOutFieldContainerName() : outWsdlParamContainer.getContainerName();
        tabItemFieldOut.setText(outContainerName == null ? "out" : outContainerName);
        fieldOutTableView.setReadonly(false);
        
        Button vButton = new Button(vCompositeTabFieldOut, SWT.NONE);
        vButton.setText(Messages.getString("System.Button.GetFields")); //$NON-NLS-1$
        vButton.addSelectionListener(new SelectionAdapter()
	        {
	        	public void widgetSelected(SelectionEvent event) {
	                if (outWsdlParamContainer == null)
	                {
	                    try
	                    {
	                        loadWebService(wURL.getText());
	                        loadOperation(wOperation.getText());
	                    }
	                    catch (KettleStepException e)
	                    {
	                        // TODO Auto-generated catch block
	                        e.printStackTrace();
	                    }
	                }
	                RowMetaInterface r = getOutWebServiceFields();
	                if (r != null)
	                {
	                    BaseStepDialog.getFieldsFromPrevious(r, fieldOutTableView, 2, new int[] { 1, 2 }, new int[] {}, -1, -1, null);
	                }
	                // Define type for new entries
	                if (outWsdlParamContainer != null)
	                {
	                    TableItem[] items = fieldOutTableView.table.getItems();
	                    for (int i = 0; i < items.length; i++)
	                    {
	                        items[i].setText(3, outWsdlParamContainer.getParamType(items[i].getText(2)));
	                    }
	                }
	            }
	        });
        Button[] buttons = new Button[] {vButton};
        BaseStepDialog.positionBottomButtons(vCompositeTabFieldOut, buttons, Const.MARGIN, null);

        FormData fdTable = new FormData();
        fdTable.left = new FormAttachment(0, 0);
        fdTable.top = new FormAttachment(0, margin);
        fdTable.right = new FormAttachment(100, 0);
        fdTable.bottom = new FormAttachment(vButton, 0);
        fieldOutTableView.setLayoutData(fdTable);

        FormData fdInComp=new FormData();
        fdInComp.left  = new FormAttachment(0, 0);
        fdInComp.top   = new FormAttachment(0, 0);
        fdInComp.right = new FormAttachment(100, 0);
        fdInComp.bottom= new FormAttachment(100, 0);
        vCompositeTabFieldOut.setLayoutData(fdInComp);
    
        vCompositeTabFieldOut.layout();

        tabItemFieldOut.setControl(vCompositeTabFieldOut);

        if (fieldOutTableView.table.getItemCount() == 0 && outWsdlParamContainer != null)
        {
        	RowMetaInterface r = getOutWebServiceFields();
            for (int i = 0; i < r.size(); ++i)
            {
                String wsName = r.getValueMeta(i).getName();
                String wsType = r.getValueMeta(i).getTypeDesc();
                
                TableItem vTableItem = new TableItem(fieldOutTableView.table, SWT.NONE);
                vTableItem.setText(2, wsName);
                vTableItem.setText(3, wsType);
                if (oldTableView != null)
                {
                    String previousField = getField(oldTableView.table.getItems(), wsName);
                    if (previousField != null && !"".equals(previousField))
                    {
                        vTableItem.setText(1, previousField);
                    }
                    else
                    {
                        vTableItem.setText(1, wsName);
                    }
                }
                else
                {
                    vTableItem.setText(1, wsName);
                }
            }
        }
        fieldOutTableView.removeEmptyRows();
        fieldOutTableView.setRowNums();
        fieldOutTableView.optWidth(true);
    }

    private RowMetaInterface getInWebServiceFields()
    {
        RowMetaInterface r = null;
        if (inWsdlParamContainer != null)
        {
            r = new RowMeta();
            String[] params = inWsdlParamContainer.getParamNames();
            // If we have already saved fields mapping, we only show these mappings
            for (int cpt = 0; cpt < params.length; cpt++)
            {
                ValueMetaInterface value = new ValueMeta(params[cpt], XsdType.xsdTypeToKettleType(inWsdlParamContainer.getParamType(params[cpt])));
                r.addValueMeta(value);
            }
        }
        return r;
    }

    private RowMetaInterface getOutWebServiceFields()
    {
        RowMetaInterface r = null;
        if (outWsdlParamContainer != null)
        {
            r = new RowMeta();
            String[] outParams = outWsdlParamContainer.getParamNames();
            // If we have already saved fields mapping, we only show these mappings
            for (int cpt = 0; cpt < outParams.length; cpt++)
            {
                ValueMetaInterface value = new ValueMeta(outParams[cpt], XsdType.xsdTypeToKettleType(outWsdlParamContainer.getParamType(outParams[cpt])));
                r.addValueMeta(value);
            }
        }
        return r;
    }
    
    private void removeTabField(CTabItem tab)
    {
        if (tab != null)
        {
            tab.dispose();
            tab = null;
        }
    }

    /**
     * Here we populate the dialog using the incoming web services meta data
     */
    private void getData()
    {
        wStepname.setText(stepname);

        wURL.setText(meta.getUrl() == null ? "" : meta.getUrl()); //$NON-NLS-1$
        wProxyHost.setText(meta.getProxyHost() == null ? "" : meta.getProxyHost()); //$NON-NLS-1$
        wProxyPort.setText(meta.getProxyPort() == null ? "" : meta.getProxyPort()); //$NON-NLS-1$
        wHttpLogin.setText(meta.getHttpLogin() == null ? "" : meta.getHttpLogin()); //$NON-NLS-1$
        wHttpPassword.setText(meta.getHttpPassword() == null ? "" : meta.getHttpPassword()); //$NON-NLS-1$
        DatabaseDialog.checkPasswordVisible(wHttpPassword.getTextWidget());
        wStep.setText(Integer.toString(meta.getCallStep()));
        wPassInputData.setSelection(meta.isPassingInputData());
        wCompatible.setSelection(meta.isCompatible());
        wRepeatingElement.setText(Const.NVL(meta.getRepeatingElementName(), ""));
        wReplyAsString.setSelection(meta.isReturningReplyAsString());
        
        if (wURL.getText() != null && !"".equals(wURL.getText())) //$NON-NLS-1$
        {
            wOperation.setText(meta.getOperationName() == null ? "" : meta.getOperationName());
        }
        if (meta.getInFieldContainerName() != null || meta.getInFieldArgumentName() != null || !meta.getFieldsIn().isEmpty())
        {
            addTabFieldIn();
            wStep.setEditable(false);

            for (Iterator<WebServiceField> itr = meta.getFieldsIn().iterator(); itr.hasNext();)
            {
                WebServiceField field = (WebServiceField) itr.next();
                TableItem vTableItem = new TableItem(fieldInTableView.table, SWT.NONE);
                if (field.getName() != null)
                {
                    vTableItem.setText(1, field.getName());
                }
                vTableItem.setText(2, field.getWsName());
                vTableItem.setText(3, field.getXsdType());
            }
            
            fieldInTableView.removeEmptyRows();
            fieldInTableView.setRowNums();
            fieldInTableView.optWidth(true);
        }
        else
        {
            wStep.setEditable(false);
            wStep.setVisible(false);
            wlStep.setVisible(false);
        }
        if (!meta.getFieldsOut().isEmpty())
        {
            addTabFieldOut();

            for (Iterator<WebServiceField> itr = meta.getFieldsOut().iterator(); itr.hasNext();)
            {
                WebServiceField field = (WebServiceField) itr.next();
                TableItem vTableItem = new TableItem(fieldOutTableView.table, SWT.NONE);
                if (field.getName() != null)
                {
                    vTableItem.setText(1, field.getName());
                }
                vTableItem.setText(2, field.getWsName());
                vTableItem.setText(3, field.getXsdType());
            }
            fieldOutTableView.removeEmptyRows();
            fieldOutTableView.setRowNums();
            fieldOutTableView.optWidth(true);
        }
    }

    /**
     * Save the data and close the dialog
     *
     */
    private void getInfo(WebServiceMeta webServiceMeta)
    {
    	webServiceMeta.setUrl(wURL.getText());
    	webServiceMeta.setProxyHost(wProxyHost.getText());
    	webServiceMeta.setProxyPort(wProxyPort.getText());
    	webServiceMeta.setHttpLogin(wHttpLogin.getText());
    	webServiceMeta.setHttpPassword(wHttpPassword.getText());
    	webServiceMeta.setCallStep(Const.toInt(wStep.getText(), WebServiceMeta.DEFAULT_STEP));
    	webServiceMeta.setPassingInputData(wPassInputData.getSelection());
    	webServiceMeta.setCompatible(wCompatible.getSelection());
    	webServiceMeta.setRepeatingElementName(wRepeatingElement.getText());
    	webServiceMeta.setReturningReplyAsString(wReplyAsString.getSelection());

        if (wsdlOperation != null)
        {
        	webServiceMeta.setOperationName(wsdlOperation.getOperationQName().getLocalPart());
        	webServiceMeta.setOperationNamespace(wsdlOperation.getOperationQName().getNamespaceURI());
        }
        else if (wsdl != null)
        {
        	webServiceMeta.setOperationName(null);
        	webServiceMeta.setOperationNamespace(null);
        }
        if (inWsdlParamContainer != null)
        {
        	webServiceMeta.setInFieldContainerName(inWsdlParamContainer.getContainerName());
        	webServiceMeta.setInFieldArgumentName(inWsdlParamContainer.getItemName());
        }
        else if (wsdl != null)
        {
        	webServiceMeta.setInFieldContainerName(null);
        	webServiceMeta.setInFieldArgumentName(null);
        }
        if (outWsdlParamContainer != null)
        {
        	webServiceMeta.setOutFieldContainerName(outWsdlParamContainer.getContainerName());
        	webServiceMeta.setOutFieldArgumentName(outWsdlParamContainer.getItemName());
        }
        else if (wsdl != null)
        {
        	webServiceMeta.setOutFieldContainerName(null);
        	webServiceMeta.setOutFieldArgumentName(null);
        }

        // Input fields...
        //
        webServiceMeta.getFieldsIn().clear();
        if (tabItemFieldIn != null)
        {
            int nbRow = fieldInTableView.nrNonEmpty();
            
            for (int i = 0; i < nbRow; ++i)
            {
                TableItem vTableItem = fieldInTableView.getNonEmpty(i);
                WebServiceField field = new WebServiceField();
                field.setName(vTableItem.getText(1));
                field.setWsName(vTableItem.getText(2));
                field.setXsdType(Const.NVL(vTableItem.getText(3), "String"));
                webServiceMeta.addFieldIn(field);
            }
        }

        // output fields...
        //
        webServiceMeta.getFieldsOut().clear();
        if (tabItemFieldOut != null)
        {
            int nbRow = fieldOutTableView.nrNonEmpty();
            
            for (int i = 0; i < nbRow; ++i)
            {
                TableItem vTableItem = fieldOutTableView.getNonEmpty(i);
                // If output name is null we do not add the field
                if (!"".equals(vTableItem.getText(1)))
                {
                    WebServiceField field = new WebServiceField();
                    field.setName(vTableItem.getText(1));
                    field.setWsName(vTableItem.getText(2));
                    field.setXsdType(vTableItem.getText(3));
                    webServiceMeta.addFieldOut(field);
                }
            }
        }
    }

    public WebServiceDialog(Shell aShell, Object in, TransMeta transMeta, String sname)
    {
        super(aShell, (WebServiceMeta)in, transMeta, sname);
        meta = (WebServiceMeta) in;
    }

    public String open()
    {
        Shell parent = getParent();
        Display display = parent.getDisplay();

        shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN);
        props.setLook(shell);
        setShellImage(shell, meta);

        changed = meta.hasChanged();

        FormLayout formLayout = new FormLayout();
        formLayout.marginWidth = Const.FORM_MARGIN;
        formLayout.marginHeight = Const.FORM_MARGIN;

        shell.setLayout(formLayout);
        shell.setText(Messages.getString("WebServiceDialog.DialogTitle")); //$NON-NLS-1$

        int middle = props.getMiddlePct();
        int margin = Const.MARGIN;

        // Stepname line
        wlStepname = new Label(shell, SWT.RIGHT);
        wlStepname.setText(Messages.getString("System.Label.StepName")); //$NON-NLS-1$
        props.setLook(wlStepname);
        fdlStepname = new FormData();
        fdlStepname.left = new FormAttachment(0, 0);
        fdlStepname.top = new FormAttachment(0, margin);
        fdlStepname.right = new FormAttachment(middle, -margin);
        wlStepname.setLayoutData(fdlStepname);
        wStepname = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        wStepname.setText(stepname);
        props.setLook(wStepname);
        wStepname.addModifyListener(lsMod);
        fdStepname = new FormData();
        fdStepname.left = new FormAttachment(middle, 0);
        fdStepname.top = new FormAttachment(0, margin);
        fdStepname.right = new FormAttachment(100, 0);
        wStepname.setLayoutData(fdStepname);

        wTabFolder = new CTabFolder(shell, SWT.BORDER);
        props.setLook(wTabFolder, Props.WIDGET_STYLE_TAB);

        // Add a tab which contains information on the web service(s)
        //
        tabItemWebService = new CTabItem(wTabFolder, SWT.NONE);
        tabItemWebService.setText(Messages.getString("WebServiceDialog.MainTab.TabTitle")); //$NON-NLS-1$
        Composite compositeTabWebService = new Composite(wTabFolder, SWT.NONE);
        props.setLook(compositeTabWebService);

        FormLayout fileLayout = new FormLayout();
        fileLayout.marginWidth = 3;
        fileLayout.marginHeight = 3;
        compositeTabWebService.setLayout(fileLayout);

        // URL
        wlURL = new Label(compositeTabWebService, SWT.RIGHT);
        wlURL.setText(Messages.getString("WebServiceDialog.URL.Label")); //$NON-NLS-1$
        props.setLook(wlURL);
        FormData fdlURL = new FormData();
        fdlURL.left = new FormAttachment(0, 0);
        fdlURL.top = new FormAttachment(0, margin);
        fdlURL.right = new FormAttachment(middle, -margin);
        wlURL.setLayoutData(fdlURL);

        wbURL = new Button(compositeTabWebService, SWT.PUSH | SWT.CENTER);
        props.setLook(wbURL);
        wbURL.setText(Messages.getString("WebServiceDialog.URL.Load")); //$NON-NLS-1$
        FormData fdbURL = new FormData();
        fdbURL.right = new FormAttachment(100, 0);
        fdbURL.top = new FormAttachment(0, 0);
        wbURL.setLayoutData(fdbURL);

        wbURL.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent e)
            {
                // If the URL is specified, we always try to load
            	//
                if (!Const.isEmpty(wURL.getText()))
                {
                    try
                    {
                        initTreeTabWebService(wURL.getText());
                    }
                    catch (Throwable throwable)
                    {
                		new ErrorDialog(shell, 
                				Messages.getString("WebServiceDialog.Exception.UnableToLoadWebService.Title"), // $NON-NLS-1$ 
                				Messages.getString("WebServiceDialog.Exception.UnableToLoadWebService.Message"), // $NON-NLS-1$ 
                				throwable);
                    }
                }
            }
        });

        wbFile = new Button(compositeTabWebService, SWT.PUSH | SWT.CENTER);
        props.setLook(wbFile);
        wbFile.setText(Messages.getString("WebServiceDialog.File.Load")); //$NON-NLS-1$
        FormData fdbFile = new FormData();
        fdbFile.right = new FormAttachment(wbURL, 0);
        fdbFile.top = new FormAttachment(0, 0);
        wbFile.setLayoutData(fdbFile);

        wbFile.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent e)
            {
            	// We will load the WSDL from a file so we can at least try to debug the metadata extraction phase from the support side.
            	//
				FileDialog dialog = new FileDialog(shell, SWT.OPEN);
				dialog.setFilterExtensions(new String[] {"*.wsdl;*.WSDL", "*.*"});
				dialog.setFilterNames(new String[] {Messages.getString("WebServiceDialog.FileType.WsdlFiles"), Messages.getString("System.FileType.CSVFiles"), Messages.getString("System.FileType.TextFiles"), Messages.getString("System.FileType.AllFiles")});
				
				if (dialog.open()!=null)
				{
					String filename = dialog.getFilterPath()+System.getProperty("file.separator")+dialog.getFileName();
                    try
                    {
                        initTreeTabWebService(new File(filename).toURI().toASCIIString());
                    }
                    catch (Throwable throwable)
                    {
                		new ErrorDialog(shell, 
                				Messages.getString("WebServiceDialog.Exception.UnableToLoadWebService.Title"), // $NON-NLS-1$ 
                				Messages.getString("WebServiceDialog.Exception.UnableToLoadWebService.Message"), // $NON-NLS-1$ 
                				throwable);
                    }
				}
            }
        });

        wURL = new TextVar(transMeta, compositeTabWebService, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        wURL.addModifyListener(lsMod);
        props.setLook(wURL);
        FormData fdURL = new FormData();
        fdURL.left = new FormAttachment(middle, 0);
        fdURL.top = new FormAttachment(0, margin);
        fdURL.right = new FormAttachment(wbFile, -margin);
        wURL.setLayoutData(fdURL);

        // Operation
        wlOperation = new Label(compositeTabWebService, SWT.RIGHT);
        wlOperation.setText(Messages.getString("WebServiceDialog.Operation.Label")); //$NON-NLS-1$
        props.setLook(wlOperation);
        FormData fdlOperation = new FormData();
        fdlOperation.left = new FormAttachment(0, 0);
        fdlOperation.top = new FormAttachment(wURL, margin);
        fdlOperation.right = new FormAttachment(middle, -margin);
        wlOperation.setLayoutData(fdlOperation);
        wOperation = new CCombo(compositeTabWebService, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        wOperation.addModifyListener(lsMod);
        wOperation.setToolTipText(Messages.getString("WebServiceDialog.Operation.Tooltip")); //$NON-NLS-1$
        props.setLook(wOperation);
        FormData fdOperation = new FormData();
        fdOperation.top = new FormAttachment(wURL, margin);
        fdOperation.left = new FormAttachment(middle, 0);
        fdOperation.right = new FormAttachment(100, 0);
        wOperation.setLayoutData(fdOperation);
        wOperation.addSelectionListener(new SelectionListener()
        {

            public void widgetSelected(SelectionEvent arg0)
            {
                try
                {
                    selectWSDLOperation(wOperation.getText());
                }
                catch (KettleStepException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

            public void widgetDefaultSelected(SelectionEvent arg0)
            {
                // TODO Auto-generated method stub

            }

        });

        // Pas d'appel
        wlStep = new Label(compositeTabWebService, SWT.RIGHT);
        wlStep.setText(Messages.getString("WebServiceDialog.Step.Label")); //$NON-NLS-1$
        props.setLook(wlStep);
        FormData fdlStep = new FormData();
        fdlStep.left = new FormAttachment(0, 0);
        fdlStep.top = new FormAttachment(wOperation, margin);
        fdlStep.right = new FormAttachment(middle, -margin);
        wlStep.setLayoutData(fdlStep);
        wStep = new Text(compositeTabWebService, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        wStep.addModifyListener(lsMod);
        wStep.setToolTipText(Messages.getString("WebServiceDialog.Step.Tooltip")); //$NON-NLS-1$
        props.setLook(wStep);
        FormData fdStep = new FormData();
        fdStep.top = new FormAttachment(wOperation, margin);
        fdStep.left = new FormAttachment(middle, 0);
        fdStep.right = new FormAttachment(100, 0);
        wStep.setLayoutData(fdStep);

        // Option to pass all input data to output
        //
        wlPassInputData = new Label(compositeTabWebService, SWT.RIGHT);
        wlPassInputData.setText(Messages.getString("WebServiceDialog.PassInputData.Label")); //$NON-NLS-1$
        props.setLook(wlPassInputData);
        FormData fdlPassInputData = new FormData();
        fdlPassInputData.left = new FormAttachment(0, 0);
        fdlPassInputData.top = new FormAttachment(wStep, margin);
        fdlPassInputData.right = new FormAttachment(middle, -margin);
        wlPassInputData.setLayoutData(fdlPassInputData);
        wPassInputData = new Button(compositeTabWebService, SWT.CHECK);
        wPassInputData.setToolTipText(Messages.getString("WebServiceDialog.PassInputData.Tooltip")); //$NON-NLS-1$
        props.setLook(wPassInputData);
        FormData fdPassInputData = new FormData();
        fdPassInputData.top = new FormAttachment(wStep, margin);
        fdPassInputData.left = new FormAttachment(middle, 0);
        fdPassInputData.right = new FormAttachment(100, 0);
        wPassInputData.setLayoutData(fdPassInputData);

        // Option to use 2.5/3.0 compatible parsing logic
        //
        wlCompatible = new Label(compositeTabWebService, SWT.RIGHT);
        wlCompatible.setText(Messages.getString("WebServiceDialog.Compatible.Label")); //$NON-NLS-1$
        props.setLook(wlCompatible);
        FormData fdlCompatible = new FormData();
        fdlCompatible.left = new FormAttachment(0, 0);
        fdlCompatible.top = new FormAttachment(wPassInputData, margin);
        fdlCompatible.right = new FormAttachment(middle, -margin);
        wlCompatible.setLayoutData(fdlCompatible);
        wCompatible = new Button(compositeTabWebService, SWT.CHECK);
        wCompatible.setToolTipText(Messages.getString("WebServiceDialog.Compatible.Tooltip")); //$NON-NLS-1$
        props.setLook(wCompatible);
        FormData fdCompatible = new FormData();
        fdCompatible.top = new FormAttachment(wPassInputData, margin);
        fdCompatible.left = new FormAttachment(middle, 0);
        fdCompatible.right = new FormAttachment(100, 0);
        wCompatible.setLayoutData(fdCompatible);
        
        // HTTP Login
        wlRepeatingElement = new Label(compositeTabWebService, SWT.RIGHT);
        wlRepeatingElement.setText(Messages.getString("WebServiceDialog.RepeatingElement.Label")); //$NON-NLS-1$
        props.setLook(wlRepeatingElement);
        FormData fdlRepeatingElement = new FormData();
        fdlRepeatingElement.top = new FormAttachment(wCompatible, margin);
        fdlRepeatingElement.left = new FormAttachment(0, 0);
        fdlRepeatingElement.right = new FormAttachment(middle, -margin);
        wlRepeatingElement.setLayoutData(fdlRepeatingElement);
        wRepeatingElement = new TextVar(transMeta, compositeTabWebService, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        wRepeatingElement.addModifyListener(lsMod);
        wRepeatingElement.setToolTipText(Messages.getString("WebServiceDialog.RepeatingElement.Tooltip")); //$NON-NLS-1$
        props.setLook(wRepeatingElement);
        FormData fdRepeatingElement = new FormData();
        fdRepeatingElement.top = new FormAttachment(wCompatible, margin);
        fdRepeatingElement.left = new FormAttachment(middle, 0);
        fdRepeatingElement.right = new FormAttachment(100, 0);
        wRepeatingElement.setLayoutData(fdRepeatingElement);

        // Return the SOAP body as a String or not?
        //
        wlReplyAsString = new Label(compositeTabWebService, SWT.RIGHT);
        wlReplyAsString.setText(Messages.getString("WebServiceDialog.ReplyAsString.Label")); //$NON-NLS-1$
        props.setLook(wlReplyAsString);
        FormData fdlBodyAsString = new FormData();
        fdlBodyAsString.left = new FormAttachment(0, 0);
        fdlBodyAsString.top = new FormAttachment(wRepeatingElement, margin);
        fdlBodyAsString.right = new FormAttachment(middle, -margin);
        wlReplyAsString.setLayoutData(fdlBodyAsString);
        wReplyAsString = new Button(compositeTabWebService, SWT.CHECK);
        wReplyAsString.setToolTipText(Messages.getString("WebServiceDialog.ReplyAsString.Tooltip")); //$NON-NLS-1$
        props.setLook(wReplyAsString);
        FormData fdBodyAsString = new FormData();
        fdBodyAsString.top = new FormAttachment(wRepeatingElement, margin);
        fdBodyAsString.left = new FormAttachment(middle, 0);
        fdBodyAsString.right = new FormAttachment(100, 0);
        wReplyAsString.setLayoutData(fdBodyAsString);


        //////////////////////////
        // START HTTP AUTH GROUP

        Group gHttpAuth = new Group(compositeTabWebService, SWT.SHADOW_ETCHED_IN);
        gHttpAuth.setText(Messages.getString("WebServicesDialog.HttpAuthGroup.Label")); //$NON-NLS-1$;
        FormLayout httpAuthLayout = new FormLayout();
        httpAuthLayout.marginWidth = 3;
        httpAuthLayout.marginHeight = 3;
        gHttpAuth.setLayout(httpAuthLayout);
        props.setLook(gHttpAuth);

        // HTTP Login
        wlHttpLogin = new Label(gHttpAuth, SWT.RIGHT);
        wlHttpLogin.setText(Messages.getString("WebServiceDialog.HttpLogin.Label")); //$NON-NLS-1$
        props.setLook(wlHttpLogin);
        FormData fdlHttpLogin = new FormData();
        fdlHttpLogin.top = new FormAttachment(0, margin);
        fdlHttpLogin.left = new FormAttachment(0, 0);
        fdlHttpLogin.right = new FormAttachment(middle, -margin);
        wlHttpLogin.setLayoutData(fdlHttpLogin);
        wHttpLogin = new TextVar(transMeta, gHttpAuth, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        wHttpLogin.addModifyListener(lsMod);
        wHttpLogin.setToolTipText(Messages.getString("WebServiceDialog.HttpLogin.Tooltip")); //$NON-NLS-1$
        props.setLook(wHttpLogin);
        FormData fdHttpLogin = new FormData();
        fdHttpLogin.top = new FormAttachment(0, margin);
        fdHttpLogin.left = new FormAttachment(middle, 0);
        fdHttpLogin.right = new FormAttachment(100, 0);
        wHttpLogin.setLayoutData(fdHttpLogin);

        // HTTP Password
        wlHttpPassword = new Label(gHttpAuth, SWT.RIGHT);
        wlHttpPassword.setText(Messages.getString("WebServiceDialog.HttpPassword.Label")); //$NON-NLS-1$
        props.setLook(wlHttpPassword);
        FormData fdlHttpPassword = new FormData();
        fdlHttpPassword.top = new FormAttachment(wHttpLogin, margin);
        fdlHttpPassword.left = new FormAttachment(0, 0);
        fdlHttpPassword.right = new FormAttachment(middle, -margin);
        wlHttpPassword.setLayoutData(fdlHttpPassword);
        wHttpPassword = new TextVar(transMeta, gHttpAuth, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        wHttpPassword.addModifyListener(lsMod);
        wHttpPassword.setToolTipText(Messages.getString("WebServiceDialog.HttpPassword.Tooltip")); //$NON-NLS-1$
        wHttpPassword.setEchoChar('*');
        props.setLook(wHttpPassword);
        FormData fdHttpPassword = new FormData();
        fdHttpPassword.top = new FormAttachment(wHttpLogin, margin);
        fdHttpPassword.left = new FormAttachment(middle, 0);
        fdHttpPassword.right = new FormAttachment(100, 0);
        wHttpPassword.setLayoutData(fdHttpPassword);
        
        FormData fdHttpAuth = new FormData();
        fdHttpAuth.left = new FormAttachment(0, 0);
        fdHttpAuth.right = new FormAttachment(100, 0);
        fdHttpAuth.top = new FormAttachment(wReplyAsString, margin);
        gHttpAuth.setLayoutData(fdHttpAuth);

        // END HTTP AUTH GROUP
        //////////////////////////

        //////////////////////////
        // START PROXY GROUP

        Group gProxy = new Group(compositeTabWebService, SWT.SHADOW_ETCHED_IN);
        gProxy.setText(Messages.getString("WebServicesDialog.ProxyGroup.Label")); //$NON-NLS-1$;
        FormLayout proxyLayout = new FormLayout();
        proxyLayout.marginWidth = 3;
        proxyLayout.marginHeight = 3;
        gProxy.setLayout(proxyLayout);
        props.setLook(gProxy);

        // HTTP Login
        wlProxyHost = new Label(gProxy, SWT.RIGHT);
        wlProxyHost.setText(Messages.getString("WebServiceDialog.ProxyHost.Label")); //$NON-NLS-1$
        props.setLook(wlProxyHost);
        FormData fdlProxyHost = new FormData();
        fdlProxyHost.top = new FormAttachment(0, margin);
        fdlProxyHost.left = new FormAttachment(0, 0);
        fdlProxyHost.right = new FormAttachment(middle, -margin);
        wlProxyHost.setLayoutData(fdlProxyHost);
        wProxyHost = new TextVar(transMeta, gProxy, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        wProxyHost.addModifyListener(lsMod);
        wProxyHost.setToolTipText(Messages.getString("WebServiceDialog.ProxyHost.Tooltip")); //$NON-NLS-1$
        props.setLook(wProxyHost);
        FormData fdProxyHost = new FormData();
        fdProxyHost.top = new FormAttachment(0, margin);
        fdProxyHost.left = new FormAttachment(middle, 0);
        fdProxyHost.right = new FormAttachment(100, 0);
        wProxyHost.setLayoutData(fdProxyHost);

        // HTTP Password
        wlProxyPort = new Label(gProxy, SWT.RIGHT);
        wlProxyPort.setText(Messages.getString("WebServiceDialog.ProxyPort.Label")); //$NON-NLS-1$
        props.setLook(wlProxyPort);
        FormData fdlProxyPort = new FormData();
        fdlProxyPort.top = new FormAttachment(wProxyHost, margin);
        fdlProxyPort.left = new FormAttachment(0, 0);
        fdlProxyPort.right = new FormAttachment(middle, -margin);
        wlProxyPort.setLayoutData(fdlProxyPort);
        wProxyPort = new TextVar(transMeta, gProxy, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        wProxyPort.addModifyListener(lsMod);
        wProxyPort.setToolTipText(Messages.getString("WebServiceDialog.ProxyPort.Tooltip")); //$NON-NLS-1$
        props.setLook(wProxyPort);
        FormData fdProxyPort = new FormData();
        fdProxyPort.top = new FormAttachment(wProxyHost, margin);
        fdProxyPort.left = new FormAttachment(middle, 0);
        fdProxyPort.right = new FormAttachment(100, 0);
        wProxyPort.setLayoutData(fdProxyPort);

        FormData fdProxy = new FormData();
        fdProxy.left = new FormAttachment(0, 0);
        fdProxy.right = new FormAttachment(100, 0);
        fdProxy.top = new FormAttachment(gHttpAuth, margin);
        gProxy.setLayoutData(fdProxy);

        // END HTTP AUTH GROUP
        //////////////////////////

        // Layout du tab
        FormData fdFileComp = new FormData();
        fdFileComp.left = new FormAttachment(0, 0);
        fdFileComp.top = new FormAttachment(0, 0);
        fdFileComp.right = new FormAttachment(100, 0);
        fdFileComp.bottom = new FormAttachment(100, 0);
        compositeTabWebService.setLayoutData(fdFileComp);

        compositeTabWebService.layout();
        tabItemWebService.setControl(compositeTabWebService);
        
        //          OK, if the password contains a variable, we don't want to have the password hidden...
        wHttpPassword.addModifyListener(new ModifyListener()
        {
            public void modifyText(ModifyEvent e)
            {
                DatabaseDialog.checkPasswordVisible(wHttpPassword.getTextWidget());
            }
        });
        wURL.addListener(SWT.Selection, new Listener()
        {
            public void handleEvent(Event e)
            {
                getData();
            }
        });

        SelectionAdapter selAdapter = new SelectionAdapter()
        {
            public void widgetDefaultSelected(SelectionEvent e)
            {
                ok();
            }
        };
        wHttpPassword.addSelectionListener(selAdapter);
        wHttpLogin.addSelectionListener(selAdapter);
        wStep.addSelectionListener(selAdapter);
        wProxyHost.addSelectionListener(selAdapter);
        wProxyPort.addSelectionListener(selAdapter);
        wStepname.addSelectionListener(selAdapter);

        wTabFolder.setSelection(tabItemWebService);
        FormData fdTabFolder = new FormData();
        fdTabFolder.left = new FormAttachment(0, 0);
        fdTabFolder.top = new FormAttachment(wStepname, margin);
        fdTabFolder.right = new FormAttachment(100, 0);
        fdTabFolder.bottom = new FormAttachment(100, -50);
        wTabFolder.setLayoutData(fdTabFolder);

        // Boutons OK / Cancel

        wOK = new Button(shell, SWT.PUSH);
        wOK.setText(Messages.getString("System.Button.OK")); //$NON-NLS-1$

        wAddInput = new Button(shell, SWT.PUSH);
        wAddInput.setText(Messages.getString("WebServiceDialog.Label.AddInputButton")); //$NON-NLS-1$

        wAddOutput = new Button(shell, SWT.PUSH);
        wAddOutput.setText(Messages.getString("WebServiceDialog.Label.AddOutputButton")); //$NON-NLS-1$

        wCancel = new Button(shell, SWT.PUSH);
        wCancel.setText(Messages.getString("System.Button.Cancel")); //$NON-NLS-1$

        setButtonPositions(new Button[] {wOK, wAddInput, wAddOutput, wCancel}, margin, wTabFolder);

        // Detect X or ALT-F4 or something that kills this window...
        shell.addShellListener(new ShellAdapter()
        {
            public void shellClosed(ShellEvent e)
            {
                cancel();
            }
        });

        wOK.addSelectionListener(new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { ok(); } });
        wAddInput.addSelectionListener(new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { addTabFieldIn(); wTabFolder.setSelection(tabItemFieldIn); } });
        wAddOutput.addSelectionListener(new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { addTabFieldOut(); wTabFolder.setSelection(tabItemFieldOut);} });
        wCancel.addSelectionListener(new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { cancel(); } });

        lsDef = new SelectionAdapter()
        {
            public void widgetDefaultSelected(SelectionEvent e)
            {
                ok();
            }
        };

        getData();

        setComboValues();
        // Set the shell size, based upon previous time...
        setSize();

        shell.open();

        while (!shell.isDisposed())
        {
            if (!display.readAndDispatch()) display.sleep();
        }

        
        return stepname;
    }

    private void ok()
    {
		if (Const.isEmpty(wStepname.getText())) return;

        stepname = wStepname.getText(); // return value

        getInfo(meta);
        
        dispose();
    }

    private void cancel()
    {
        stepname = null;

        meta.setChanged(changed);

        dispose();
    }

    private List<ColumnInfo> fieldColumns = new ArrayList<ColumnInfo>();
    
    /**
     * Fields from previous step
     */
    private RowMetaInterface prevFields;

    /*
     * Previous fields are read asynchonous because this might take some time
     * and the user is able to do other things, where he will not need the previous fields
     *
    private boolean bPreviousFieldsLoaded = false;
     */

    private void setComboValues() {
        Runnable fieldLoader = new Runnable() {
            public void run() {
                try {
                    prevFields = transMeta.getPrevStepFields(stepname);
                } catch (KettleException e) {
                    prevFields = new RowMeta();
                    String msg = Messages.getString("SelectValuesDialog.DoMapping.UnableToFindInput");
                    log.logError(toString(), msg);
                }
                String[] prevStepFieldNames = prevFields.getFieldNames();
                Arrays.sort(prevStepFieldNames);
                // bPreviousFieldsLoaded = true;
                for (int i = 0; i < fieldColumns.size(); i++) {
                    ColumnInfo colInfo = (ColumnInfo) fieldColumns.get(i);
                    colInfo.setComboValues(prevStepFieldNames);
                }
            }
        };
        shell.getDisplay().asyncExec(fieldLoader);
    }
}
