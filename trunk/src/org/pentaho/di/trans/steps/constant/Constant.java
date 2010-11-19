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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

/**
 * Generates a number of (empty or the same) rows
 * 
 * @author Matt
 * @since 4-apr-2003
 */
public class Constant extends BaseStep implements StepInterface
{
	private ConstantMeta meta;
	private ConstantData data;
	
	public Constant(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
		
		meta=(ConstantMeta)getStepMeta().getStepMetaInterface();
		data=(ConstantData)stepDataInterface;
	}
	
    public static final RowMetaAndData buildRow(ConstantMeta meta, ConstantData data, List<CheckResultInterface> remarks)
    {
        RowMetaInterface rowMeta=new RowMeta();
        Object[] rowData = new Object[meta.getFieldName().length];

        for (int i=0;i<meta.getFieldName().length;i++)
        {
            int valtype = ValueMeta.getType(meta.getFieldType()[i]); 
            if (meta.getFieldName()[i]!=null)
            {
                ValueMetaInterface value=new ValueMeta(meta.getFieldName()[i], valtype); // build a value!
                value.setLength(meta.getFieldLength()[i]);
                value.setPrecision(meta.getFieldPrecision()[i]);
                String stringValue = meta.getValue()[i];
                
                // If the value is empty: consider it to be NULL.
                if (stringValue==null || stringValue.length()==0)
                {
                    rowData[i]=null;
                    
                    if ( value.getType() == ValueMetaInterface.TYPE_NONE )
                    {
                        String message = Messages.getString("Constant.CheckResult.SpecifyTypeError", value.getName(), stringValue);
                        remarks.add(new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, message, null));                    
                    }
                }
                else
                {
                    switch(value.getType())
                    {
                    case ValueMetaInterface.TYPE_NUMBER:
                        try
                        {
                            if (meta.getFieldFormat()[i]!=null || meta.getDecimal()[i] !=null ||
                            meta.getGroup()[i]       !=null || meta.getCurrency()[i]!=null    
                            )
                            {
                                if (meta.getFieldFormat()[i]!=null && meta.getFieldFormat()[i].length()>=1) data.df.applyPattern(meta.getFieldFormat()[i]);
                                if (meta.getDecimal()[i] !=null && meta.getDecimal()[i].length()>=1) data.dfs.setDecimalSeparator( meta.getDecimal()[i].charAt(0) );
                                if (meta.getGroup()[i]   !=null && meta.getGroup()[i].length()>=1) data.dfs.setGroupingSeparator( meta.getGroup()[i].charAt(0) );
                                if (meta.getCurrency()[i]!=null && meta.getCurrency()[i].length()>=1) data.dfs.setCurrencySymbol( meta.getCurrency()[i] );
                                
                                data.df.setDecimalFormatSymbols(data.dfs);
                            }
                            
                            rowData[i] = new Double( data.nf.parse(stringValue).doubleValue() );
                        }
                        catch(Exception e)
                        {
                            String message = Messages.getString("Constant.BuildRow.Error.Parsing.Number", value.getName(), stringValue, e.toString() );
                            remarks.add(new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, message, null));
                        }
                        break;
                        
                    case ValueMetaInterface.TYPE_STRING:
                        rowData[i] = stringValue;
                        break;
                        
                    case ValueMetaInterface.TYPE_DATE:
                        try
                        {
                            if (meta.getFieldFormat()[i]!=null)
                            {
                                data.daf.applyPattern(meta.getFieldFormat()[i]);
                                data.daf.setDateFormatSymbols(data.dafs);
                            }
                            
                            rowData[i] = data.daf.parse(stringValue);
                        }
                        catch(Exception e)
                        {
                            String message = Messages.getString("Constant.BuildRow.Error.Parsing.Date", value.getName(), stringValue, e.toString() );
                            remarks.add(new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, message, null));
                        }
                        break;
                        
                    case ValueMetaInterface.TYPE_INTEGER:
                        try
                        {
                            rowData[i] = new Long( Long.parseLong(stringValue) );
                        }
                        catch(Exception e)
                        {
                            String message = Messages.getString("Constant.BuildRow.Error.Parsing.Integer", value.getName(), stringValue, e.toString() );
                            remarks.add(new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, message, null));
                        }
                        break;
    
                    case ValueMetaInterface.TYPE_BIGNUMBER:
                        try
                        {
                            rowData[i] = new BigDecimal(stringValue);
                        }
                        catch(Exception e)
                        {
                            String message = Messages.getString("Constant.BuildRow.Error.Parsing.BigNumber", value.getName(), stringValue, e.toString() );
                            remarks.add(new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, message, null));
                        }
                        break;
                        
                    case ValueMetaInterface.TYPE_BOOLEAN:
                        rowData[i] = Boolean.valueOf( "Y".equalsIgnoreCase(stringValue) || "TRUE".equalsIgnoreCase(stringValue) );
                        break;
                        
                    case ValueMetaInterface.TYPE_BINARY:                    
                        rowData[i] = stringValue.getBytes();                        
                        break;                        
                    
                    // -- Begin GeoKettle modification --
                    case ValueMetaInterface.TYPE_GEOMETRY:
                    	ValueMetaInterface stringMeta = new ValueMeta(meta.getFieldName()[i], ValueMetaInterface.TYPE_STRING);
                    	try {
                    		rowData[i] = value.convertData(stringMeta, stringValue);
                    	} catch (KettleValueException e) {
                    		// TODO: add the string to Messages
                    		String message = Messages.getString("Constant.BuildRow.Error.Parsing.Geometry", value.getName(), stringValue, e.toString() );
                    		remarks.add(new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, message, null));
                    	}
                    	break;
                    // -- End GeoKettle modification --
                        
                    default:
                        String message = Messages.getString("Constant.CheckResult.SpecifyTypeError", value.getName(), stringValue);
                        remarks.add(new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, message, null));
                    }
                }
                // Now add value to the row!
                // This is in fact a copy from the fields row, but now with data.
                rowMeta.addValueMeta(value); 
            }
        }
        
        return new RowMetaAndData(rowMeta, rowData);
    }	
	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		Object[] r=null;
		r = getRow();
        
        if (r==null) // no more rows to be expected from the previous step(s)
        {
            setOutputDone();
            return false;
        }
        
        if ( data.firstRow )
        {
        	// The output meta is the original input meta + the 
        	// additional constant fields.
        	
        	data.firstRow = false;
            data.outputMeta = getInputRowMeta().clone();
        	
        	RowMetaInterface constants = data.constants.getRowMeta();        	
        	data.outputMeta.mergeRowMeta(constants);
        }
          
        // Add the constant data to the end of the row.
        r = RowDataUtil.addRowData(r, getInputRowMeta().size(), data.constants.getData());
                
        putRow(data.outputMeta, r);

        if (log.isRowLevel())
        {
            log.logRowlevel(toString(), Messages.getString("Constant.Log.Wrote.Row", Long.toString(getLinesWritten()), getInputRowMeta().getString(r)) );
        }
        
        if (checkFeedback(getLinesWritten())) 
        {
        	if(log.isBasic()) logBasic( Messages.getString("Constant.Log.LineNr", Long.toString(getLinesWritten()) ));
        }
		
		return true;
	}
		
	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(ConstantMeta)smi;
		data=(ConstantData)sdi;
		
		data.firstRow = true;
		
		if (super.init(smi, sdi))
		{
            // Create a row (constants) with all the values in it...
            List<CheckResultInterface> remarks = new ArrayList<CheckResultInterface>(); // stores the errors...
            data.constants = buildRow(meta, data, remarks);           
		    if (remarks.isEmpty()) 
            { 
		        return true;
            }
            else
            {
                for (int i=0;i<remarks.size();i++)
                {
                    CheckResultInterface cr = remarks.get(i);
                    log.logError(getStepname(), cr.getText());
                }
            }
		}
		return false;
	}

	//
	// Run is were the action happens!
	public void run()
	{
    	BaseStep.runStepThread(this, meta, data);
	}
}