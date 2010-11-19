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

 
 
package org.pentaho.di.ui.core.widget;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;

/**
 * Used to define the behaviour and the content of a Table column in a TableView object.
 * 
 * @author Matt
 * @since 27-05-2003
 *
 */
public class ColumnInfo 
{
	public static final int COLUMN_TYPE_NONE    =  0;
	public static final int COLUMN_TYPE_TEXT    =  1;
	public static final int COLUMN_TYPE_CCOMBO  =  2;
	public static final int COLUMN_TYPE_BUTTON  =  3;
	public static final int COLUMN_TYPE_ICON    =  4;
	
	private int      type;
	private String   name;

	private String[] combovals;
	private boolean  numeric; 
	private String   tooltip;
	private int      allignement;
	private boolean  readonly;
	private String   button_text;
	private boolean  hide_negative;
    
    private ValueMetaInterface valueMeta;
	
	private SelectionAdapter selButton;
    
    private boolean usingVariables;
    private boolean passwordField;
    
    private ComboValuesSelectionListener comboValuesSelectionListener;
    
    /**
     * Creates a column info class for use with the TableView class.
     * 
     * @param colname The column name
     * @param coltype The column type (see: COLUMN_TYPE_...)
     */
    public ColumnInfo(String colname, int coltype)
    {
        name=colname;
        type=coltype;
        combovals=null;
        numeric=false;
        tooltip=null;
        allignement=SWT.LEFT;
        readonly=false;
        hide_negative=false;
        valueMeta=new ValueMeta(colname, ValueMetaInterface.TYPE_STRING);
    }
    
    /**
     * Creates a column info class for use with the TableView class.
     * The type of column info to be created is : COLUMN_TYPE_CCOMBO
     * 
     * @param colname The column name
     * @param coltype The column type (see: COLUMN_TYPE_...)
     * @param combo   The choices in the combo box
     */
	public ColumnInfo(String colname, int coltype, String[] combo)
	{
		this(colname, coltype);
		combovals=combo;
		numeric=false;
		tooltip=null;
		allignement=SWT.LEFT;
		readonly=false;
		hide_negative=false;
        valueMeta=new ValueMeta(colname, ValueMetaInterface.TYPE_STRING);
	}

    /**
     * Creates a column info class for use with the TableView class.
     * 
     * @param colname The column name
     * @param coltype The column type (see: COLUMN_TYPE_...)
     * @param numeric true if the column type is numeric.   Use setValueType() to specify the type of numeric: ValueMetaInterface.TYPE_INTEGER is the default.
     */
	public ColumnInfo(String colname, int coltype, boolean numeric)
	{
		this(colname, coltype);
		this.combovals=null;
		this.numeric=numeric;
		this.tooltip=null;
		this.allignement=SWT.LEFT;
		this.readonly=false;
		this.hide_negative=false;
        if (numeric)
        {
            valueMeta=new ValueMeta(colname, ValueMetaInterface.TYPE_INTEGER);
        }
        else
        {
            valueMeta=new ValueMeta(colname, ValueMetaInterface.TYPE_STRING);
        }
	}

    
    /**
     * Creates a column info class for use with the TableView class.
     * The type of column info to be created is : COLUMN_TYPE_CCOMBO
     * 
     * @param colname The column name
     * @param coltype The column type (see: COLUMN_TYPE_...)
     * @param combo   The choices in the combo box
     * @param ro      true if the column is read-only (you can't type in the combo box, you CAN make a choice)
     */
	public ColumnInfo(String colname, int coltype, String[] combo, boolean ro)
	{
		this(colname, coltype, combo);
		readonly=ro;
	}
    
    /**
     * Creates a column info class for use with the TableView class.
     * 
     * @param colname The column name
     * @param coltype The column type (see: COLUMN_TYPE_...)
     * @param num     true if the column type is numeric. Use setValueType() to specify the type of numeric: ValueMetaInterface.TYPE_INTEGER is the default.
     * @param ro      true if the column is read-only.  
     */
    public ColumnInfo(String colname, int coltype, boolean num, boolean ro)
    {
        this(colname, coltype, num);
        readonly=ro;
    }
    
    public String toString() 
    {
    	return name;
    }
	
	public void setToolTip(String tip)
    {
        tooltip = tip;
    }

    public void setReadOnly(boolean ro)
    {
        readonly = ro;
    }

    public void setAllignement(int allign)
    {
        allignement = allign;
    }

    public void setComboValues(String cv[])
    {
        combovals = cv;
    }

    public void setButtonText(String bt)
    {
        button_text = bt;
    }

    public String getName()
    {
        return name;
    }

    public int getType()
    {
        return type;
    }

    public String[] getComboValues()
    {
        String retval[] = combovals; // Copy structure!
        return retval;
    }

    /**
     * @return the numeric
     */
    public boolean isNumeric()
    {
        return numeric;
    }

    /**
     * @param numeric the numeric to set
     */
    public void setNumeric(boolean numeric)
    {
        this.numeric = numeric;
    }

    public String getToolTip()
    {
        return tooltip;
    }

    public int getAllignement()
    {
        return allignement;
    }

    public boolean isReadOnly()
    {
        return readonly;
    }

    public String getButtonText()
    {
        return button_text;
    }       
	
	public void setSelectionAdapter(SelectionAdapter sb)
	{
		selButton = sb;
	}
	
	public SelectionAdapter getSelectionAdapter()
	{
		return selButton;
	}
	
	public void hideNegative()
	{
		hide_negative = true;
	}
	
	public void showNegative()
	{
		hide_negative = false;
	}
	
	public boolean isNegativeHidden()
	{
		return hide_negative;
	}

    /**
     * @return the valueMeta
     */
    public ValueMetaInterface getValueMeta()
    {
        return valueMeta;
    }

    /**
     * @param valueMeta the valueMeta to set
     */
    public void setValueMeta(ValueMetaInterface valueMeta)
    {
        this.valueMeta = valueMeta;
    }

    /**
     * @return the usingVariables
     */
    public boolean isUsingVariables()
    {
        return usingVariables;
    }

    /**
     * @param usingVariables the usingVariables to set
     */
    public void setUsingVariables(boolean usingVariables)
    {
        this.usingVariables = usingVariables;
    }

    /**
     * @return the password
     */
    public boolean isPasswordField()
    {
        return passwordField;
    }

    /**
     * @param password the password to set
     */
    public void setPasswordField(boolean password)
    {
        this.passwordField = password;
    }

	/**
	 * @return the comboValuesSelectionListener
	 */
	public ComboValuesSelectionListener getComboValuesSelectionListener() {
		return comboValuesSelectionListener;
	}

	/**
	 * @param comboValuesSelectionListener the comboValuesSelectionListener to set
	 */
	public void setComboValuesSelectionListener(ComboValuesSelectionListener comboValuesSelectionListener) {
		this.comboValuesSelectionListener = comboValuesSelectionListener;
	}
}
