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

package org.pentaho.di.trans.steps.mappinginput;

import java.util.ArrayList;
import java.util.Collections;
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
import org.pentaho.di.core.row.RowMeta;
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
import org.pentaho.di.trans.steps.mapping.MappingValueRename;
import org.w3c.dom.Node;



/*
 * Created on 02-jun-2003
 * 
 */

public class MappingInputMeta extends BaseStepMeta implements StepMetaInterface
{
    private String fieldName[];

    private int    fieldType[];

    private int    fieldLength[];

    private int    fieldPrecision[];
    
    /**
	 * Select: flag to indicate that the non-selected fields should also be
	 * taken along, ordered by fieldname
	 */
    private boolean selectingAndSortingUnspecifiedFields;

	private volatile RowMetaInterface inputRowMeta;
	private volatile List<MappingValueRename> valueRenames;

    public MappingInputMeta()
    {
        super(); // allocate BaseStepMeta
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
    public int[] getFieldType()
    {
        return fieldType;
    }

    /**
     * @param fieldType The fieldType to set.
     */
    public void setFieldType(int[] fieldType)
    {
        this.fieldType = fieldType;
    }
    
    public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleXMLException
    {
        readData(stepnode);
    }

    public Object clone()
    {
        MappingInputMeta retval = (MappingInputMeta) super.clone();

        int nrfields = fieldName.length;

        retval.allocate(nrfields);

        for (int i = 0; i < nrfields; i++)
        {
            retval.fieldName[i] = fieldName[i];
            retval.fieldType[i] = fieldType[i];
            fieldLength[i] = fieldLength[i];
            fieldPrecision[i] = fieldPrecision[i];
        }

        return retval;
    }

    public void allocate(int nrfields)
    {
        fieldName = new String[nrfields];
        fieldType = new int[nrfields];
        fieldLength = new int[nrfields];
        fieldPrecision = new int[nrfields];
    }

    private void readData(Node stepnode) throws KettleXMLException
    {
        try
        {
            Node fields = XMLHandler.getSubNode(stepnode, "fields"); //$NON-NLS-1$
            int nrfields = XMLHandler.countNodes(fields, "field"); //$NON-NLS-1$

            allocate(nrfields);

            for (int i = 0; i < nrfields; i++)
            {
                Node fnode = XMLHandler.getSubNodeByNr(fields, "field", i); //$NON-NLS-1$

                fieldName[i] = XMLHandler.getTagValue(fnode, "name"); //$NON-NLS-1$
                fieldType[i] = ValueMeta.getType(XMLHandler.getTagValue(fnode, "type")); //$NON-NLS-1$
                String slength = XMLHandler.getTagValue(fnode, "length"); //$NON-NLS-1$
                String sprecision = XMLHandler.getTagValue(fnode, "precision"); //$NON-NLS-1$

                fieldLength[i] = Const.toInt(slength, -1);
                fieldPrecision[i] = Const.toInt(sprecision, -1);
            }
            
			selectingAndSortingUnspecifiedFields = "Y".equalsIgnoreCase(XMLHandler.getTagValue(fields, "select_unspecified"));
        }
        catch (Exception e)
        {
            throw new KettleXMLException(Messages.getString("MappingInputMeta.Exception.UnableToLoadStepInfoFromXML"), e); //$NON-NLS-1$
        }
    }

    public String getXML()
    {
        StringBuffer retval = new StringBuffer(300);
 
        retval.append("    <fields>").append(Const.CR); //$NON-NLS-1$
        for (int i = 0; i < fieldName.length; i++)
        {
            if (fieldName[i] != null && fieldName[i].length() != 0)
            {
                retval.append("      <field>").append(Const.CR); //$NON-NLS-1$
                retval.append("        ").append(XMLHandler.addTagValue("name", fieldName[i])); //$NON-NLS-1$ //$NON-NLS-2$
                retval.append("        ").append(XMLHandler.addTagValue("type", ValueMeta.getTypeDesc(fieldType[i]))); //$NON-NLS-1$ //$NON-NLS-2$
                retval.append("        ").append(XMLHandler.addTagValue("length", fieldLength[i])); //$NON-NLS-1$ //$NON-NLS-2$
                retval.append("        ").append(XMLHandler.addTagValue("precision", fieldPrecision[i])); //$NON-NLS-1$ //$NON-NLS-2$
                retval.append("      </field>").append(Const.CR); //$NON-NLS-1$
            }
        }
        
        retval.append("        ").append(XMLHandler.addTagValue("select_unspecified", selectingAndSortingUnspecifiedFields)); //$NON-NLS-1$ //$NON-NLS-2$
		
        retval.append("    </fields>").append(Const.CR); //$NON-NLS-1$

        return retval.toString();
    }

    public void setDefault()
    {
        int nrfields = 0;
        
        selectingAndSortingUnspecifiedFields = false;

        allocate(nrfields);

        for (int i = 0; i < nrfields; i++)
        {
            fieldName[i] = "field" + i; //$NON-NLS-1$
            fieldType[i] = ValueMetaInterface.TYPE_STRING;
            fieldLength[i] = 30;
            fieldPrecision[i] = -1;
        }
    }
    
    public void getFields(RowMetaInterface row, String origin, RowMetaInterface[] info, StepMeta nextStep, VariableSpace space) throws KettleStepException 
    {
    	// Row should normally be empty when we get here.
    	// That is because there is no previous step to this mapping input step from the viewpoint of this single sub-transformation.
    	// From the viewpoint of the transformation that executes the mapping, it's important to know what comes out at the exit points.
    	// For that reason we need to re-order etc, based on the input specification...
    	//
    	if (inputRowMeta!=null) {
    		// this gets set only in the parent transformation...
    		// It includes all the renames that needed to be done
    		// 
    		if (selectingAndSortingUnspecifiedFields) {
    			
    			// First rename any fields...
    			if (valueRenames != null) {
                    for (MappingValueRename valueRename : valueRenames) {
                    	ValueMetaInterface valueMeta = inputRowMeta.searchValueMeta(valueRename.getSourceValueName());
                    	if (valueMeta==null) {
                    		throw new KettleStepException(Messages.getString("MappingInput.Exception.UnableToFindMappedValue", valueRename.getSourceValueName()));
                    	}
                    	valueMeta.setName(valueRename.getTargetValueName());
                    }
    			}
    			
    			// Select the specified fields from the input, re-order everything and put the other fields at the back, sorted...
    			// 
    			RowMetaInterface newRow = new RowMeta();
    			
    	    	for (int i=0;i<fieldName.length;i++) {
    	    		int index = inputRowMeta.indexOfValue(fieldName[i]);
    	    		if (index<0) {
    	    			throw new KettleStepException(Messages.getString("MappingInputMeta.Exception.UnknownField", fieldName[i]));
    	    		}
    	    		
    	    		newRow.addValueMeta(inputRowMeta.getValueMeta(index));
    	        }
    	    	
    	    	// Now get the unspecified fields.
				// Sort the fields
				// Add them after the specified fields...
				//
				List<String> extra = new ArrayList<String>();
				for (int i=0;i<inputRowMeta.size();i++) {
					String fieldName = inputRowMeta.getValueMeta(i).getName();
					if (newRow.indexOfValue(fieldName)<0) {
						extra.add(fieldName);
					}
				}
				Collections.sort(extra);
				for (String fieldName : extra) {
					ValueMetaInterface extraValue = inputRowMeta.searchValueMeta(fieldName);
					newRow.addValueMeta(extraValue);
				}
				
				// now merge the new row...
				// This is basically the input row meta data with the fields re-ordered.
				//
				row.mergeRowMeta(newRow);
    		}
    		else {
        		row.mergeRowMeta(inputRowMeta); 
        		
        		// Validate the existence of all the specified fields...
        		//
    	    	for (int i=0;i<fieldName.length;i++) {
    	    		if (row.indexOfValue(fieldName[i])<0) {
    	    			throw new KettleStepException(Messages.getString("MappingInputMeta.Exception.UnknownField", fieldName[i]));
    	    		}
    	        }
    		}
    	}
    	else {
	    	// We'll have to work with the statically provided information
	    	for (int i=0;i<fieldName.length;i++)
	        {
	            if (!Const.isEmpty(fieldName[i]))
	            {
	                ValueMetaInterface v=new ValueMeta(fieldName[i], fieldType[i]);
	                if (v.getType()==ValueMetaInterface.TYPE_NONE) v.setType(ValueMetaInterface.TYPE_STRING);
	                v.setLength(fieldLength[i]);
	                v.setPrecision(fieldPrecision[i]);
	                v.setOrigin(origin);
	                row.addValueMeta(v);
	            }
	        }
    	}
    }
    

    public void readRep(Repository rep, long id_step, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleException
    {
        try
        {
            int nrfields = rep.countNrStepAttributes(id_step, "field_name"); //$NON-NLS-1$

            allocate(nrfields);

            for (int i = 0; i < nrfields; i++)
            {
                fieldName[i] = rep.getStepAttributeString(id_step, i, "field_name"); //$NON-NLS-1$
                fieldType[i] = ValueMeta.getType( rep.getStepAttributeString(id_step, i, "field_type") ); //$NON-NLS-1$
                fieldLength[i] = (int) rep.getStepAttributeInteger(id_step, i, "field_length"); //$NON-NLS-1$
                fieldPrecision[i] = (int) rep.getStepAttributeInteger(id_step, i, "field_precision"); //$NON-NLS-1$
            }
            
			selectingAndSortingUnspecifiedFields = rep.getStepAttributeBoolean(id_step, "select_unspecified");
        }
        catch (Exception e)
        {
            throw new KettleException(Messages.getString("MappingInputMeta.Exception.UnexpectedErrorInReadingStepInfo"), e); //$NON-NLS-1$
        }
    }

    public void saveRep(Repository rep, long id_transformation, long id_step) throws KettleException
    {
        try
        {
            for (int i = 0; i < fieldName.length; i++)
            {
                if (fieldName[i] != null && fieldName[i].length() != 0)
                {
                    rep.saveStepAttribute(id_transformation, id_step, i, "field_name", fieldName[i]); //$NON-NLS-1$
                    rep.saveStepAttribute(id_transformation, id_step, i, "field_type", ValueMeta.getTypeDesc(fieldType[i])); //$NON-NLS-1$
                    rep.saveStepAttribute(id_transformation, id_step, i, "field_length", fieldLength[i]); //$NON-NLS-1$
                    rep.saveStepAttribute(id_transformation, id_step, i, "field_precision", fieldPrecision[i]); //$NON-NLS-1$
                }
            }
            
			rep.saveStepAttribute(id_transformation, id_step, "select_unspecified", selectingAndSortingUnspecifiedFields); //$NON-NLS-1$
        }
        catch (Exception e)
        {
            throw new KettleException(Messages.getString("MappingInputMeta.Exception.UnableToSaveStepInfo") + id_step, e); //$NON-NLS-1$
        }
    }

    public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepinfo, RowMetaInterface prev, String input[], String output[], RowMetaInterface info)
    {
        CheckResult cr;
        if (prev == null || prev.size() == 0)
        {
            cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, Messages.getString("MappingInputMeta.CheckResult.NotReceivingFieldsError"), stepinfo); //$NON-NLS-1$
            remarks.add(cr);
        }
        else
        {
            cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, Messages.getString("MappingInputMeta.CheckResult.StepReceivingDatasFromPreviousOne", prev.size() + ""), stepinfo); //$NON-NLS-1$ //$NON-NLS-2$
            remarks.add(cr);
        }

        // See if we have input streams leading to this step!
        if (input.length > 0)
        {
            cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, Messages.getString("MappingInputMeta.CheckResult.StepReceivingInfoFromOtherSteps"), stepinfo); //$NON-NLS-1$
            remarks.add(cr);
        }
        else
        {
            cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK , Messages.getString("MappingInputMeta.CheckResult.NoInputReceived"), stepinfo); //$NON-NLS-1$
            remarks.add(cr);
        }
    }

    public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta tr, Trans trans)
    {
        return new MappingInput(stepMeta, stepDataInterface, cnr, tr, trans);
    }

    public StepDataInterface getStepData()
    {
        return new MappingInputData();
    }

	public void setInputRowMeta(RowMetaInterface inputRowMeta) {
		this.inputRowMeta = inputRowMeta;
	}

	/**
	 * @return the inputRowMeta
	 */
	public RowMetaInterface getInputRowMeta() {
		return inputRowMeta;
	}

	/**
	 * @return the valueRenames
	 */
	public List<MappingValueRename> getValueRenames() {
		return valueRenames;
	}

	/**
	 * @param valueRenames the valueRenames to set
	 */
	public void setValueRenames(List<MappingValueRename> valueRenames) {
		this.valueRenames = valueRenames;
	}

	/**
	 * @return the selectingAndSortingUnspecifiedFields
	 */
	public boolean isSelectingAndSortingUnspecifiedFields() {
		return selectingAndSortingUnspecifiedFields;
	}

	/**
	 * @param selectingAndSortingUnspecifiedFields the selectingAndSortingUnspecifiedFields to set
	 */
	public void setSelectingAndSortingUnspecifiedFields(boolean selectingAndSortingUnspecifiedFields) {
		this.selectingAndSortingUnspecifiedFields = selectingAndSortingUnspecifiedFields;
	}

}
