/*************************************************************************************** 
 * Copyright (C) 2007 Samatar.  All rights reserved. 
 * This software was developed by Samatar, Brahim and is provided under the terms 
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

/*
 * Created on 24-03-2008
 *
 */
package org.pentaho.di.trans.steps.propertyinput;

import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.ValueMeta;


/**
 * Describes an Property field
 * 
 * @author Samatar Hassan
 * @since 24-03-2008
 */
public class PropertyInputField implements Cloneable
{
    public final static int TYPE_TRIM_NONE  = 0;
    public final static int TYPE_TRIM_LEFT  = 1;
    public final static int TYPE_TRIM_RIGHT = 2;
    public final static int TYPE_TRIM_BOTH  = 3;
    
    public final static String trimTypeCode[] = { "none", "left", "right", "both" };
    
    public final static String trimTypeDesc[] = {
      Messages.getString("PropertyInputField.TrimType.None"),
      Messages.getString("PropertyInputField.TrimType.Left"),
      Messages.getString("PropertyInputField.TrimType.Right"),
      Messages.getString("PropertyInputField.TrimType.Both")
    };
    
    public final static int COLUMN_KEY  = 0;
    public final static int COLUMN_VALUE  = 1;
    
    public final static String ColumnCode[] = {"key","value"};
    
    
    public final static String ColumnDesc[] = { 
    	Messages.getString("PropertyInputField.Column.Key"), 
    	Messages.getString("PropertyInputField.Column.Value")};
    
	private String 	  name;
	private int		  column;
    private int 	  type;
    private int       length;
    private String    format;
    private int       trimtype;
    private int       precision;
    private String 	  currencySymbol;
	private String 	  decimalSymbol;
	private String 	  groupSymbol;
	private boolean   repeat;

    private String    samples[];

    
	public PropertyInputField(String fieldname)
	{
		this.name           = fieldname;
		this.column         = COLUMN_KEY;
		this.length         = -1;
		this.type           = ValueMetaInterface.TYPE_STRING;
		this.format         = "";
		this.trimtype       = TYPE_TRIM_NONE;
		this.groupSymbol    = "";
		this.decimalSymbol  = "";
		this.currencySymbol = "";
		this.precision      = -1;
		this.repeat         = false;
	}
    
    public PropertyInputField()
    {
        this(null);
    }

  


    public final static int getTrimTypeByCode(String tt)
    {
        if (tt==null) return 0;
        
        for (int i=0;i<trimTypeCode.length;i++)
        {
            if (trimTypeCode[i].equalsIgnoreCase(tt)) return i;
        }
        return 0;
    }
       
    public String getColumnDesc()
	{
		return getColumnDesc(column);
	}
    
    public final static String getColumnDesc(int i)
    {
        if (i<0 || i>=ColumnDesc.length) return ColumnDesc[0];
        return ColumnDesc[i]; 
    }
    public final static int getTrimTypeByDesc(String tt)
    {
        if (tt==null) return 0;
        
        for (int i=0;i<trimTypeDesc.length;i++)
        {
            if (trimTypeDesc[i].equalsIgnoreCase(tt)) return i;
        }
        return 0;
    }
    public String getColumnCode()
	{
		return getColumnCode(column);
	}
    public final static String getColumnCode(int i)
    {
        if (i<0 || i>=ColumnCode.length) return ColumnCode[0];
        return ColumnCode[i]; 
    }
    public final static int getColumnByCode(String tt)
    {
        if (tt==null) return 0;
        
        for (int i=0;i<ColumnCode.length;i++)
        {
            if (ColumnCode[i].equalsIgnoreCase(tt)) return i;
        }
        return 0;
    }
    public final static String getTrimTypeCode(int i)
    {
        if (i<0 || i>=trimTypeCode.length) return trimTypeCode[0];
        return trimTypeCode[i]; 
    }
    
    public final static String getTrimTypeDesc(int i)
    {
        if (i<0 || i>=trimTypeDesc.length) return trimTypeDesc[0];
        return trimTypeDesc[i]; 
    }
    
    public Object clone()
	{
		try
		{
			PropertyInputField retval = (PropertyInputField) super.clone();
          
            
			return retval;
		}
		catch(CloneNotSupportedException e)
		{
			return null;
		}
	}	
    

    public int getLength()
	{
		return length;
	}
	
	public void setLength(int length)
	{
		this.length = length;
	}
	
	public String getName()
	{
		return name;
	}

	public void setName(String fieldname)
	{
		this.name = fieldname;
	}

	public int getType()
	{
		return type;
	}

	public String getTypeDesc()
	{
		return ValueMeta.getTypeDesc(type);
	}
	
	public void setType(int type)
	{
		this.type = type;
	}
	
	public String getFormat()
	{
		return format;
	}
	
	public void setFormat(String format)
	{
		this.format = format;
	}
	
	public void setSamples(String samples[])
	{
		this.samples = samples;
	}
    
    public String[] getSamples()
    {
        return samples;
    }

	public int getTrimType()
	{
		return trimtype;
	}
	
    public String getTrimTypeCode()
	{
		return getTrimTypeCode(trimtype);
	}
  
	public String getTrimTypeDesc()
	{
		return getTrimTypeDesc(trimtype);
	}
	
	public void setTrimType(int trimtype)
	{
		this.trimtype= trimtype;
	}
	
	public String getGroupSymbol()
	{
		return groupSymbol;
	}
	
	public void setGroupSymbol(String group_symbol)
	{
		this.groupSymbol = group_symbol;
	}

	public String getDecimalSymbol()
	{
		return decimalSymbol;
	}
	
	public void setDecimalSymbol(String decimal_symbol)
	{
		this.decimalSymbol = decimal_symbol;
	}

	public String getCurrencySymbol()
	{
		return currencySymbol;
	}
	
	public void setCurrencySymbol(String currency_symbol)
	{
		this.currencySymbol = currency_symbol;
	}

	public int getPrecision()
	{
		return precision;
	}
	
	public void setPrecision(int precision)
	{
		this.precision = precision;
	}
	
	public boolean isRepeated()
	{
		return repeat;
	}
	
	public void setRepeated(boolean repeat)
	{
		this.repeat = repeat;
	}
	
	public void flipRepeated()
	{
		repeat = !repeat;		
	}
	
	 public final static int getColumnByDesc(String tt)
     {
        if (tt==null) return 0;
        
        for (int i=0;i<ColumnDesc.length;i++)
        {
            if (ColumnDesc[i].equalsIgnoreCase(tt)) return i;
        }
        return 0;
     }
	 public void setColumn(int column)
	{
		this.column= column;
	}
    
   
}