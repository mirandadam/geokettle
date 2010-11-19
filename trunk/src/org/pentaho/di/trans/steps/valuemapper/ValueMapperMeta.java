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

package org.pentaho.di.trans.steps.valuemapper;

import java.util.List;
import java.util.Map;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.annotations.Step;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
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
import org.pentaho.di.trans.step.StepCategory;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.w3c.dom.Node;


/**
 * Maps String values of a certain field to new values
 * 
 * Created on 03-apr-2006
 */
@Step(name="ValueMapper",image="ui/images/VMP.png",tooltip="BaseStep.TypeTooltipDesc.MapValues",description="BaseStep.TypeLongDesc.ValueMapper",
		category=StepCategory.CATEGORY_TRANSFORM)
public class ValueMapperMeta extends BaseStepMeta implements StepMetaInterface
{
    private String fieldToUse;
    private String targetField;
    private String nonMatchDefault;
    
	private String sourceValue[];
	private String targetValue[];
	
	public ValueMapperMeta()
	{
		super(); // allocate BaseStepMeta
	}
	
	/**
     * @return Returns the fieldName.
     */
    public String[] getSourceValue()
    {
        return sourceValue;
    }
    
    /**
     * @param fieldName The fieldName to set.
     */
    public void setSourceValue(String[] fieldName)
    {
        this.sourceValue = fieldName;
    }
 
    /**
     * @return Returns the fieldValue.
     */
    public String[] getTargetValue()
    {
        return targetValue;
    }
    
    /**
     * @param fieldValue The fieldValue to set.
     */
    public void setTargetValue(String[] fieldValue)
    {
        this.targetValue = fieldValue;
    }
 	
	public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String,Counter> counters)
		throws KettleXMLException
	{
		readData(stepnode);
	}

	public void allocate(int count)
	{
		sourceValue  = new String[count];
		targetValue = new String[count];
	}

	public Object clone()
	{
		ValueMapperMeta retval = (ValueMapperMeta)super.clone();

		int count=sourceValue.length;
		
		retval.allocate(count);
				
		for (int i=0;i<count;i++)
		{
			retval.sourceValue[i]  = sourceValue[i];
			retval.targetValue[i] = targetValue[i];
		}
		
		return retval;
	}
	
	private void readData(Node stepnode)
		throws KettleXMLException
	{
		try
		{
            fieldToUse      = XMLHandler.getTagValue(stepnode, "field_to_use"); //$NON-NLS-1$
            targetField     = XMLHandler.getTagValue(stepnode, "target_field"); //$NON-NLS-1$
            nonMatchDefault = XMLHandler.getTagValue(stepnode, "non_match_default"); //$NON-NLS-1$
            
			Node fields = XMLHandler.getSubNode(stepnode, "fields"); //$NON-NLS-1$
			int count   = XMLHandler.countNodes(fields, "field"); //$NON-NLS-1$
			
			allocate(count);
					
			for (int i=0;i<count;i++)
			{
				Node fnode = XMLHandler.getSubNodeByNr(fields, "field", i); //$NON-NLS-1$
				
				sourceValue[i] = XMLHandler.getTagValue(fnode, "source_value"); //$NON-NLS-1$
				targetValue[i] = XMLHandler.getTagValue(fnode, "target_value"); //$NON-NLS-1$
			}
		}
		catch(Exception e)
		{
			throw new KettleXMLException(Messages.getString("ValueMapperMeta.RuntimeError.UnableToReadXML.VALUEMAPPER0004"), e); //$NON-NLS-1$
		}
	}
	
	public void setDefault()
	{
		int count=0;
		
		allocate(count);

		for (int i=0;i<count;i++)
		{
			sourceValue[i] = "field"+i; //$NON-NLS-1$
			targetValue[i] = ""; //$NON-NLS-1$
		}
	}

	public void getFields(RowMetaInterface r, String name, RowMetaInterface[] info, StepMeta nextStep, VariableSpace space)
	{
		ValueMetaInterface extra = null;
		if (!Const.isEmpty(getTargetField()))
        {
		    extra = new ValueMeta(getTargetField(), ValueMetaInterface.TYPE_STRING);
		    
            // Lengths etc?
            // Take the max length of all the strings...
		    //
            int maxlen = -1;
            for (int i=0;i<targetValue.length;i++)
            {
                if (targetValue[i]!=null && targetValue[i].length()>maxlen) maxlen=targetValue[i].length();
            }
            
            // include default value in max length calculation
            //
            if (nonMatchDefault != null && nonMatchDefault.length()>maxlen) 
            {
                maxlen = nonMatchDefault.length();
            }
            extra.setLength(maxlen);
            extra.setOrigin(name);
            r.addValueMeta(extra);
        }
		else
		{
			if (!Const.isEmpty(getFieldToUse())) {
				extra = r.searchValueMeta(getFieldToUse());
			}
		}
		
		if (extra!=null) {
			// The output of a changed field or new field is always a normal storage type...
			//
			extra.setStorageType(ValueMetaInterface.STORAGE_TYPE_NORMAL);
		}
	}

	public String getXML()
	{
        StringBuffer retval = new StringBuffer();

        retval.append("    ").append(XMLHandler.addTagValue("field_to_use", fieldToUse)); //$NON-NLS-1$ //$NON-NLS-2$
        retval.append("    ").append(XMLHandler.addTagValue("target_field", targetField)); //$NON-NLS-1$ //$NON-NLS-2$
        retval.append("    ").append(XMLHandler.addTagValue("non_match_default", nonMatchDefault)); //$NON-NLS-1$ //$NON-NLS-2$
        
		retval.append("    <fields>").append(Const.CR); //$NON-NLS-1$
		
		for (int i=0;i<sourceValue.length;i++)
		{
			retval.append("      <field>").append(Const.CR); //$NON-NLS-1$
			retval.append("        ").append(XMLHandler.addTagValue("source_value", sourceValue[i])); //$NON-NLS-1$ //$NON-NLS-2$
			retval.append("        ").append(XMLHandler.addTagValue("target_value", targetValue[i])); //$NON-NLS-1$ //$NON-NLS-2$
			retval.append("      </field>").append(Const.CR); //$NON-NLS-1$
		}
		retval.append("    </fields>").append(Const.CR); //$NON-NLS-1$

		return retval.toString();
	}
	
	public void readRep(Repository rep, long id_step, List<DatabaseMeta> databases, Map<String,Counter> counters)
		throws KettleException
	{
		try
		{
            fieldToUse      = rep.getStepAttributeString(id_step, "field_to_use"); //$NON-NLS-1$
            targetField     = rep.getStepAttributeString(id_step, "target_field"); //$NON-NLS-1$
            nonMatchDefault = rep.getStepAttributeString(id_step, "non_match_default"); //$NON-NLS-1$
            
			int nrfields = rep.countNrStepAttributes(id_step, "source_value"); //$NON-NLS-1$
			
			allocate(nrfields);
	
			for (int i=0;i<nrfields;i++)
			{
				sourceValue[i] = rep.getStepAttributeString(id_step, i, "source_value"); //$NON-NLS-1$
				targetValue[i] = rep.getStepAttributeString(id_step, i, "target_value"); //$NON-NLS-1$
			}
		}
		catch(Exception e)
		{
			throw new KettleException(Messages.getString("ValueMapperMeta.RuntimeError.UnableToReadRepository.VALUEMAPPER0005"), e); //$NON-NLS-1$
		}
	}

	public void saveRep(Repository rep, long id_transformation, long id_step)
		throws KettleException
	{
		try
		{
            rep.saveStepAttribute(id_transformation, id_step, "field_to_use",       fieldToUse); //$NON-NLS-1$
            rep.saveStepAttribute(id_transformation, id_step, "target_field",       targetField); //$NON-NLS-1$
            rep.saveStepAttribute(id_transformation, id_step, "non_match_default",  nonMatchDefault); //$NON-NLS-1$
            
			for (int i=0;i<sourceValue.length;i++)
			{
				rep.saveStepAttribute(id_transformation, id_step, i, "source_value",      sourceValue[i]); //$NON-NLS-1$
				rep.saveStepAttribute(id_transformation, id_step, i, "target_value",     targetValue[i]); //$NON-NLS-1$
			}
		}
		catch(Exception e)
		{
			throw new KettleException(Messages.getString("ValueMapperMeta.RuntimeError.UnableToSaveRepository.VALUEMAPPER0006", ""+id_step), e); //$NON-NLS-1$
		}

	}
	public void check(List<CheckResultInterface> remarks, TransMeta transmeta, StepMeta stepinfo, RowMetaInterface prev, String input[], String output[], RowMetaInterface info)
	{
		CheckResult cr;
		if (prev==null || prev.size()==0)
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_WARNING, Messages.getString("ValueMapperMeta.CheckResult.NotReceivingFieldsFromPreviousSteps"), stepinfo); //$NON-NLS-1$
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("ValueMapperMeta.CheckResult.ReceivingFieldsFromPreviousSteps", ""+prev.size()), stepinfo); //$NON-NLS-1$ //$NON-NLS-2$
			remarks.add(cr);
		}
		
		// See if we have input streams leading to this step!
		if (input.length>0)
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("ValueMapperMeta.CheckResult.ReceivingInfoFromOtherSteps"), stepinfo); //$NON-NLS-1$
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("ValueMapperMeta.CheckResult.NotReceivingInfoFromOtherSteps"), stepinfo); //$NON-NLS-1$
			remarks.add(cr);
		}
	}

	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta, Trans trans)
	{
		return new ValueMapper(stepMeta, stepDataInterface, cnr, transMeta, trans);
	}

	public StepDataInterface getStepData()
	{
		return new ValueMapperData();
	}

    /**
     * @return Returns the fieldToUse.
     */
    public String getFieldToUse()
    {
        return fieldToUse;
    }

    /**
     * @param fieldToUse The fieldToUse to set.
     */
    public void setFieldToUse(String fieldToUse)
    {
        this.fieldToUse = fieldToUse;
    }

    /**
     * @return Returns the targetField.
     */
    public String getTargetField()
    {
        return targetField;
    }

    /**
     * @param targetField The targetField to set.
     */
    public void setTargetField(String targetField)
    {
        this.targetField = targetField;
    }

    /**
     * @return the non match default. This is the string that will be used to fill in the data when no match is found.
     */
	public String getNonMatchDefault() 
	{
		return nonMatchDefault;
	}

	/**
	 * @param nonMatchDefault the non match default. This is the string that will be used to fill in the data when no match is found.
	 */
	public void setNonMatchDefault(String nonMatchDefault) 
	{
		this.nonMatchDefault = nonMatchDefault;
	}
}