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

package org.pentaho.di.trans.steps.constant;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.w3c.dom.Node;

/*
 * Created on 4-apr-2003
 *
 */
public class ConstantMeta extends BaseStepMeta implements StepMetaInterface
{	
	private  String currency[];
	private  String decimal[];
	private  String group[];
	private  String value[];
	
	private  String fieldName[];
	private  String fieldType[];
	private  String fieldFormat[];

	private  int fieldLength[];
	private  int fieldPrecision[];

	public ConstantMeta()
	{
		super(); // allocate BaseStepMeta
	}
	
    /**
     * @return Returns the currency.
     */
    public String[] getCurrency()
    {
        return currency;
    }
    
    /**
     * @param currency The currency to set.
     */
    public void setCurrency(String[] currency)
    {
        this.currency = currency;
    }
    
    /**
     * @return Returns the decimal.
     */
    public String[] getDecimal()
    {
        return decimal;
    }
    
    /**
     * @param decimal The decimal to set.
     */
    public void setDecimal(String[] decimal)
    {
        this.decimal = decimal;
    }
    
    /**
     * @return Returns the fieldFormat.
     */
    public String[] getFieldFormat()
    {
        return fieldFormat;
    }
    
    /**
     * @param fieldFormat The fieldFormat to set.
     */
    public void setFieldFormat(String[] fieldFormat)
    {
        this.fieldFormat = fieldFormat;
    }
    
    /**
     * @return Returns the fieldLength.
     */
    public int[] getFieldLength()
    {
        return fieldLength;
    }
    
    /**
     * @param fieldLength The fieldLength to set.
     */
    public void setFieldLength(int[] fieldLength)
    {
        this.fieldLength = fieldLength;
    }
    
    /**
     * @return Returns the fieldName.
     */
    public String[] getFieldName()
    {
        return fieldName;
    }
    
    /**
     * @param fieldName The fieldName to set.
     */
    public void setFieldName(String[] fieldName)
    {
        this.fieldName = fieldName;
    }
    
    /**
     * @return Returns the fieldPrecision.
     */
    public int[] getFieldPrecision()
    {
        return fieldPrecision;
    }
    
    /**
     * @param fieldPrecision The fieldPrecision to set.
     */
    public void setFieldPrecision(int[] fieldPrecision)
    {
        this.fieldPrecision = fieldPrecision;
    }
    
    /**
     * @return Returns the fieldType.
     */
    public String[] getFieldType()
    {
        return fieldType;
    }
    
    /**
     * @param fieldType The fieldType to set.
     */
    public void setFieldType(String[] fieldType)
    {
        this.fieldType = fieldType;
    }
    
    /**
     * @return Returns the group.
     */
    public String[] getGroup()
    {
        return group;
    }
    
    /**
     * @param group The group to set.
     */
    public void setGroup(String[] group)
    {
        this.group = group;
    }
    
    /**
     * @return Returns the value.
     */
    public String[] getValue()
    {
        return value;
    }
    
    /**
     * @param value The value to set.
     */
    public void setValue(String[] value)
    {
        this.value = value;
    }
    
    
    
	public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleXMLException {
		readData(stepnode);
	}

	public void allocate(int nrfields)
	{
		fieldName      = new String[nrfields];
		fieldType      = new String[nrfields];
		fieldFormat    = new String[nrfields];
		fieldLength    = new int[nrfields];
		fieldPrecision = new int[nrfields];
		currency       = new String[nrfields];
		decimal        = new String[nrfields];
		group          = new String[nrfields];
		value          = new String[nrfields];
	}
	
	public Object clone()
	{
		ConstantMeta retval = (ConstantMeta)super.clone();

		int nrfields=fieldName.length;

		retval.allocate(nrfields);
		
		for (int i=0;i<nrfields;i++)
		{
			retval.fieldName[i]   = fieldName[i];
			retval.fieldType[i]   = fieldType[i];
			retval.fieldFormat[i] = fieldFormat[i];
			retval.currency[i]    = currency[i];
			retval.decimal[i]     = decimal[i];
			retval.group[i]       = group[i];
			retval.value[i]       = value[i];
			fieldLength[i]        = fieldLength[i]; 
			fieldPrecision[i]     = fieldPrecision[i]; 
		}
		
		return retval;
	}

	private void readData(Node stepnode)
		throws KettleXMLException
	{
		try
		{
			Node fields = XMLHandler.getSubNode(stepnode, "fields");
			int  nrfields=XMLHandler.countNodes(fields, "field");
	
			allocate(nrfields);
			
			String slength, sprecision;
			
			for (int i=0;i<nrfields;i++)
			{
				Node fnode = XMLHandler.getSubNodeByNr(fields, "field", i);
				
				fieldName[i]   = XMLHandler.getTagValue(fnode, "name");
				fieldType[i]   = XMLHandler.getTagValue(fnode, "type");
				fieldFormat[i] = XMLHandler.getTagValue(fnode, "format");
				currency[i]    = XMLHandler.getTagValue(fnode, "currency");
				decimal[i]     = XMLHandler.getTagValue(fnode, "decimal");
				group[i]       = XMLHandler.getTagValue(fnode, "group");
				value[i]       = XMLHandler.getTagValue(fnode, "nullif");
				slength        = XMLHandler.getTagValue(fnode, "length");
				sprecision     = XMLHandler.getTagValue(fnode, "precision");
				
				fieldLength[i]    = Const.toInt(slength, -1);
				fieldPrecision[i] = Const.toInt(sprecision, -1);
			}
        }
		catch(Exception e)
		{
			throw new KettleXMLException("Unable to load step info from XML", e);
		}
	}
	
	public void setDefault()
	{
		int i, nrfields=0;
	
		allocate(nrfields);

        DecimalFormat decimalFormat = new DecimalFormat();

		for (i=0;i<nrfields;i++)
		{
			fieldName[i]      = "field"+i;				
			fieldType[i]      = "Number";
			fieldFormat[i]    = "\u00A40,000,000.00;\u00A4-0,000,000.00";
			fieldLength[i]    = 9;
			fieldPrecision[i] = 2;
			currency[i]       = decimalFormat.getDecimalFormatSymbols().getCurrencySymbol();
			decimal[i]        = new String(new char[] { decimalFormat.getDecimalFormatSymbols().getDecimalSeparator() } );
			group[i]          = new String(new char[] { decimalFormat.getDecimalFormatSymbols().getGroupingSeparator() } );
			value[i]          = "-";
		}

	}
	
	public void getFields(RowMetaInterface rowMeta, String name, RowMetaInterface[] info, StepMeta nextStep, VariableSpace space) throws KettleStepException
	{
		for (int i=0;i<fieldName.length;i++)
		{
			if (fieldName[i]!=null && fieldName[i].length()!=0)
			{
				int type=ValueMeta.getType(fieldType[i]);
				if (type==ValueMetaInterface.TYPE_NONE) type=ValueMetaInterface.TYPE_STRING;
				ValueMetaInterface v=new ValueMeta(fieldName[i], type);
				v.setLength(fieldLength[i]);
                v.setPrecision(fieldPrecision[i]);
				v.setOrigin(name);
				rowMeta.addValueMeta(v);
			}
		}
	}
		
	public String getXML()
	{
        StringBuffer retval = new StringBuffer(300);
		
		retval.append("    <fields>").append(Const.CR);
		for (int i=0;i<fieldName.length;i++)
		{
			if (fieldName[i]!=null && fieldName[i].length()!=0)
			{
				retval.append("      <field>").append(Const.CR);
				retval.append("        ").append(XMLHandler.addTagValue("name",      fieldName[i]));
				retval.append("        ").append(XMLHandler.addTagValue("type",      fieldType[i]));
				retval.append("        ").append(XMLHandler.addTagValue("format",    fieldFormat[i]));
				retval.append("        ").append(XMLHandler.addTagValue("currency",  currency[i]));
				retval.append("        ").append(XMLHandler.addTagValue("decimal",   decimal[i]));
				retval.append("        ").append(XMLHandler.addTagValue("group",     group[i]));
				retval.append("        ").append(XMLHandler.addTagValue("nullif",    value[i]));
				retval.append("        ").append(XMLHandler.addTagValue("length",    fieldLength[i]));
				retval.append("        ").append(XMLHandler.addTagValue("precision", fieldPrecision[i]));
				retval.append("      </field>").append(Const.CR);
			}
		}
		retval.append("    </fields>").append(Const.CR);

		return retval.toString();
	}

	public void readRep(Repository rep, long id_step, List<DatabaseMeta> databases, Map<String, Counter> counters)
		throws KettleException
	{
		try
		{
			int nrfields = rep.countNrStepAttributes(id_step, "field_name");
			
			allocate(nrfields);
	
			for (int i=0;i<nrfields;i++)
			{
				fieldName[i]      =       rep.getStepAttributeString (id_step, i, "field_name");
				fieldType[i]      =       rep.getStepAttributeString (id_step, i, "field_type");
	
				fieldFormat[i]    =       rep.getStepAttributeString (id_step, i, "field_format");
				currency[i]       =       rep.getStepAttributeString (id_step, i, "field_currency");
				decimal[i]        =       rep.getStepAttributeString (id_step, i, "field_decimal");
				group[i]          =       rep.getStepAttributeString (id_step, i, "field_group");
				value[i]          =       rep.getStepAttributeString (id_step, i, "field_nullif");
				fieldLength[i]    =  (int)rep.getStepAttributeInteger(id_step, i, "field_length");
				fieldPrecision[i] =  (int)rep.getStepAttributeInteger(id_step, i, "field_precision");
			}
		}
		catch(Exception e)
		{
			throw new KettleException("Unexpected error reading step information from the repository", e);
		}
	}

	public void saveRep(Repository rep, long id_transformation, long id_step)
		throws KettleException
	{
		try
		{
			for (int i=0;i<fieldName.length;i++)
			{
				if (fieldName[i]!=null && fieldName[i].length()!=0)
				{
					rep.saveStepAttribute(id_transformation, id_step, i, "field_name",      fieldName[i]);
					rep.saveStepAttribute(id_transformation, id_step, i, "field_type",      fieldType[i]);
					rep.saveStepAttribute(id_transformation, id_step, i, "field_format",    fieldFormat[i]);
					rep.saveStepAttribute(id_transformation, id_step, i, "field_currency",  currency[i]);
					rep.saveStepAttribute(id_transformation, id_step, i, "field_decimal",   decimal[i]);
					rep.saveStepAttribute(id_transformation, id_step, i, "field_group",     group[i]);
					rep.saveStepAttribute(id_transformation, id_step, i, "field_nullif",    value[i]);
					rep.saveStepAttribute(id_transformation, id_step, i, "field_length",    fieldLength[i]);
					rep.saveStepAttribute(id_transformation, id_step, i, "field_precision", fieldPrecision[i]);
				}
			}
		}
		catch(Exception e)
		{
			throw new KettleException("Unable to save step information to the repository for id_step="+id_step, e);
		}
	}

	public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev, String input[], String output[], RowMetaInterface info)
	{
		CheckResult cr;
		if (prev!=null && prev.size()>0)
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, Messages.getString("ConstantMeta.CheckResult.FieldsReceived", ""+prev.size()), stepMeta);
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, Messages.getString("ConstantMeta.CheckResult.NoFields"), stepMeta);
			remarks.add(cr);
		}
        
        // Check the constants...
        ConstantData data = new ConstantData();
        ConstantMeta meta = (ConstantMeta) stepMeta.getStepMetaInterface();
        Constant.buildRow(meta, data, remarks);
	}
	
	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta, Trans trans)
	{
		return new Constant(stepMeta, stepDataInterface, cnr, transMeta, trans);
	}

	public StepDataInterface getStepData()
	{
		return new ConstantData();
	}
}