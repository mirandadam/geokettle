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

import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.util.StringUtil;
import org.pentaho.di.ui.core.dialog.EnterTextDialog;


/**
 * 
 * Dialog that allows you to edit the settings of a database connection.
 * 
 * @see <code>DatabaseInfo</code>
 * @author Matt
 * @since 18-05-2003
 * 
 */
public class DatabaseDialog extends XulDatabaseDialog
{
    public DatabaseDialog(Shell parent, DatabaseMeta databaseMeta)
    {
        super(parent, databaseMeta);
    }

    public String open()
    {
        return super.open();
    }
    
    public static final void checkPasswordVisible(Text wPassword)
    {
        String password = wPassword.getText();
        java.util.List<String> list = new ArrayList<String>();
        StringUtil.getUsedVariables(password, list, true);
        // ONLY show the variable in clear text if there is ONE variable used
        // Also, it has to be the only string in the field.
        //

        if (list.size() != 1)
        {
            wPassword.setEchoChar('*');
        }
        else
        {
        	String variableName = null;
            if ((password.startsWith(StringUtil.UNIX_OPEN) && password.endsWith(StringUtil.UNIX_CLOSE)))
            {
            	//  ${VAR}
            	//  012345
            	// 
            	variableName = password.substring(StringUtil.UNIX_OPEN.length(), password.length()-StringUtil.UNIX_CLOSE.length());
            }
            if ((password.startsWith(StringUtil.WINDOWS_OPEN) && password.endsWith(StringUtil.WINDOWS_CLOSE)))
            {
            	//  %VAR%
            	//  01234
            	// 
            	variableName = password.substring(StringUtil.WINDOWS_OPEN.length(), password.length()-StringUtil.WINDOWS_CLOSE.length());
            }
            
            // If there is a variable name in there AND if it's defined in the system properties...
            // Otherwise, we'll leave it alone.
            //
            if (variableName!=null && System.getProperty(variableName)!=null)
            {
                wPassword.setEchoChar('\0'); // Show it all...
            }
            else
            {
                wPassword.setEchoChar('*');
            }
        }
    }
    
    /**
     * Test the database connection
     */
    public static final void test(Shell shell, DatabaseMeta dbinfo)
    {
        String[] remarks = dbinfo.checkParameters();
        if (remarks.length == 0)
        {
        	// Get a "test" report from this database
        	//
        	String reportMessage = dbinfo.testConnection();

        	EnterTextDialog dialog = new EnterTextDialog(shell, Messages.getString("DatabaseDialog.ConnectionReport.title"), Messages.getString("DatabaseDialog.ConnectionReport.description"), reportMessage.toString()); //$NON-NLS-1$ //$NON-NLS-2$
            dialog.setReadOnly();
            dialog.setFixed(true);
            dialog.setModal();
            dialog.open();
        }
        else
        {
            String message = ""; //$NON-NLS-1$
            for (int i = 0; i < remarks.length; i++)
                message += "    * " + remarks[i] + Const.CR; //$NON-NLS-1$

            MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
            mb.setText(Messages.getString("DatabaseDialog.ErrorParameters2.title")); //$NON-NLS-1$
            mb.setMessage(Messages.getString("DatabaseDialog.ErrorParameters2.description", message)); //$NON-NLS-1$
            mb.open();
        }
    }
}