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

package org.pentaho.di.ui.core.database.dialog;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.i18n.GlobalMessages;
import org.pentaho.di.i18n.LanguageChoice;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.ui.database.DatabaseConnectionDialog;
import org.pentaho.ui.xul.XulComponent;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.containers.XulDialog;

public class XulDatabaseDialog {

  private DatabaseMeta databaseMeta;

  private Shell shell;

  private Shell parentShell;

  private String databaseName;

  private java.util.List<DatabaseMeta> databases;

  private boolean modalDialog;

  DataOverrideHandler dataHandler = null;
  
  private LogWriter log;
  
  private static final String EVENT_ID = "dataHandler"; //$NON-NLS-1$
  
  private static final String MESSAGES = "org.pentaho.di.ui.core.database.dialog.messages.messages"; //$NON-NLS-1$
  
  private static final String DIALOG_FRAGMENT_FILE = "/feature_override.xul"; //$NON-NLS-1$
  
  private static final String FRAGMENT_ID = "test-button-box"; //$NON-NLS-1$
  
  private static final String EXTENDED_WIDGET_CLASSNAME = "org.pentaho.di.ui.core.database.dialog.tags.ExtTextbox"; //$NON-NLS-1$
  
  private static final String EXTENDED_WIDGET_ID = "VARIABLETEXTBOX"; //$NON-NLS-1$
  
  public XulDatabaseDialog(Shell parent, DatabaseMeta dbMeta) {

    parentShell = parent;
    databaseMeta = dbMeta;
    if (dbMeta != null) {
      databaseName = databaseMeta.getName();
    }
    databases = null;
    
    log = LogWriter.getInstance();
  }

  /**
   * Opens the XUL database dialog
  * @return databaseName (or NULL on error or cancel)
  * TODO: Fix deprecation warning in v3.2 by using the new dialog 
  */
  @SuppressWarnings("deprecation")
  public String open() {

    XulDomContainer container = null;
    try {
      DatabaseConnectionDialog dcDialog = new DatabaseConnectionDialog();
      dcDialog.registerClass(EXTENDED_WIDGET_ID, EXTENDED_WIDGET_CLASSNAME);
      container = dcDialog.getSwtInstance(shell);  //Attention: onload: loadConnectionData() is called here the first time, see below for second time

      container.addEventHandler(EVENT_ID, DataOverrideHandler.class.getName());

      dataHandler = (DataOverrideHandler)container.getEventHandler(EVENT_ID);
      if (databaseMeta != null) {
        dataHandler.setData(databaseMeta);
      }
      dataHandler.setDatabases(databases);
      dataHandler.getControls();

    } catch (XulException e) {
      new ErrorDialog(parentShell, Messages.getString("XulDatabaseDialog.Error.Titel"), Messages //$NON-NLS-1$
          .getString("XulDatabaseDialog.Error.HandleXul"), e); //$NON-NLS-1$
      return null;
    }

    try {
      // Inject the button panel that contains the "Feature List" and "Explore" buttons

      XulComponent boxElement = container.getDocumentRoot().getElementById(FRAGMENT_ID);
      XulComponent parentElement = boxElement.getParent();

      ResourceBundle res = null;
      Locale primaryLocale = GlobalMessages.getLocale();
      Locale failOverLocale = LanguageChoice.getInstance().getFailoverLocale();
      try{
        res = GlobalMessages.getBundle(primaryLocale, MESSAGES);
      }catch(MissingResourceException e){
        try{
          res = GlobalMessages.getBundle(failOverLocale, MESSAGES);
        }catch(MissingResourceException e2){
          res = null;
          log.logError(Messages.getString("XulDatabaseDialog.Error.ResourcesNotFound.Title"),  //$NON-NLS-1$
              Messages.getString("XulDatabaseDialog.Error.ResourcesNotFound",   //$NON-NLS-1$
                  primaryLocale == null ? "" : primaryLocale.toString(),  //$NON-NLS-1$
                  failOverLocale == null ? "" : failOverLocale.toString()),   //$NON-NLS-1$
              e2);
        }
      }

      XulDomContainer fragmentContainer = null;
      String pkg = getClass().getPackage().getName().replace('.', '/');
      
      // Kludge: paths of execution do not account for a null resourcebundle gracefully, need 
      // to check for it here. 
      if (res != null){
        fragmentContainer = container.loadFragment(pkg.concat(DIALOG_FRAGMENT_FILE), res);
      } else{
        fragmentContainer = container.loadFragment(pkg.concat(DIALOG_FRAGMENT_FILE));
      }
      
      XulComponent newBox = fragmentContainer.getDocumentRoot().getFirstChild();
      parentElement.replaceChild(boxElement, newBox);

    } catch (Exception e) {
      new ErrorDialog(parentShell, Messages.getString("XulDatabaseDialog.Error.Titel"), Messages //$NON-NLS-1$
          .getString("XulDatabaseDialog.Error.HandleXul"), e); //$NON-NLS-1$
      return null;
    }

    try {
      final XulDialog dialog = (XulDialog) container.getDocumentRoot().getRootElement();
      ((Shell)dialog.getRootObject()).setImage(GUIResource.getInstance().getImageConnection());
      
      parentShell.addDisposeListener(new DisposeListener(){

        public void widgetDisposed(DisposeEvent arg0) {
          dialog.hide();
        }
        
      });
      
      dialog.show();  	//Attention: onload: loadConnectionData() is called here the second time, see above for first time
      					// caught with a HACK in DataHandler.loadConnectionData()

      databaseMeta = (DatabaseMeta) dataHandler.getData();
      databaseName = Const.isEmpty(databaseMeta.getName()) ? null : databaseMeta.getName();
      
    } catch (Exception e) {
      new ErrorDialog(parentShell, Messages.getString("XulDatabaseDialog.Error.Titel"), Messages  //$NON-NLS-1$
          .getString("XulDatabaseDialog.Error.Dialog"), e); //$NON-NLS-1$
      return null;
    }
    return databaseName;
  }

  public DatabaseMeta getDatabaseMeta() {
    return databaseMeta;
  }

  public void setDatabases(java.util.List<DatabaseMeta> databases) {
    this.databases = databases;
  }

  /**
   * @return the modalDialog
   */
  public boolean isModalDialog() {
    return modalDialog;
  }

  /**
   * @param modalDialog the modalDialog to set
   */
  public void setModalDialog(boolean modalDialog) {
    this.modalDialog = modalDialog;
  }

}