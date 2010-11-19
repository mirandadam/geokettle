/*
 * Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
*/
package org.pentaho.di.trans.step;

import java.util.List;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.changed.ChangedFlag;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.core.xml.XMLInterface;
import org.pentaho.di.repository.Repository;
import org.w3c.dom.Node;

/**
 * This class contains the metadata to handle proper error handling on a step level.
 * 
 * @author Matt
 * 
 */
public class StepErrorMeta extends ChangedFlag implements XMLInterface, Cloneable
{
    public static final String XML_TAG = "error";
    
    /** The source step that can send the error rows */
    private StepMeta sourceStep;
    
    /** The target step to send the error rows to */
    private StepMeta targetStep;
    
    /** Is the error handling enabled? */
    private boolean  enabled;

    /** the name of the field value to contain the number of errors (null or empty means it's not needed) */  
    private String   nrErrorsValuename;

    /** the name of the field value to contain the error description(s) (null or empty means it's not needed) */  
    private String   errorDescriptionsValuename;

    /** the name of the field value to contain the fields for which the error(s) occured (null or empty means it's not needed) */  
    private String   errorFieldsValuename;

    /** the name of the field value to contain the error code(s) (null or empty means it's not needed) */  
    private String   errorCodesValuename;
    
    /** The maximum number of errors allowed before we stop processing with a hard error */
    private long     maxErrors;
    
    /** The maximum percent of errors allowed before we stop processing with a hard error */
    private int      maxPercentErrors;
    
    /** The minimum number of rows to read before the percentage evaluation takes place */
    private long     minPercentRows;
    
    private VariableSpace variables;

    
    /**
     * Create a new step error handling metadata object
     * @param sourceStep The source step that can send the error rows
     */
    public StepErrorMeta(VariableSpace space, StepMeta sourceStep)
    {
        this.sourceStep = sourceStep;
        this.enabled = false;
        this.variables = space;
    }

    /**
     * Create a new step error handling metadata object
     * @param sourceStep The source step that can send the error rows
     * @param targetStep The target step to send the error rows to
     */
    public StepErrorMeta(VariableSpace space, StepMeta sourceStep, StepMeta targetStep)
    {
        this.sourceStep = sourceStep;
        this.targetStep = targetStep;
        this.enabled = false;
        this.variables = space;
    }
    
    /**
     * Create a new step error handling metadata object
     * @param sourceStep The source step that can send the error rows
     * @param targetStep The target step to send the error rows to
     * @param nrErrorsValuename the name of the field value to contain the number of errors (null or empty means it's not needed) 
     * @param errorDescriptionsValuename the name of the field value to contain the error description(s) (null or empty means it's not needed) 
     * @param errorFieldsValuename the name of the field value to contain the fields for which the error(s) occured (null or empty means it's not needed)
     * @param errorCodesValuename the name of the field value to contain the error code(s) (null or empty means it's not needed)
     */
    public StepErrorMeta(VariableSpace space, StepMeta sourceStep, StepMeta targetStep, String nrErrorsValuename, String errorDescriptionsValuename, String errorFieldsValuename, String errorCodesValuename)
    {
        this.sourceStep = sourceStep;
        this.targetStep = targetStep;
        this.enabled = false;
        this.nrErrorsValuename = nrErrorsValuename;
        this.errorDescriptionsValuename = errorDescriptionsValuename;
        this.errorFieldsValuename = errorFieldsValuename;
        this.errorCodesValuename = errorCodesValuename;
        this.variables = space;
    }
    
    public StepErrorMeta clone()
    {
        try
        {
            return (StepErrorMeta) super.clone();
        }
        catch(CloneNotSupportedException e)
        {
            return null;
        }
    }

    public String getXML()
    {
        StringBuffer xml = new StringBuffer(300);

        xml.append("      ").append(XMLHandler.openTag(XML_TAG)).append(Const.CR);
        xml.append("        ").append(XMLHandler.addTagValue("source_step", sourceStep!=null ? sourceStep.getName() : ""));
        xml.append("        ").append(XMLHandler.addTagValue("target_step", targetStep!=null ? targetStep.getName() : ""));
        xml.append("        ").append(XMLHandler.addTagValue("is_enabled", enabled));
        xml.append("        ").append(XMLHandler.addTagValue("nr_valuename", nrErrorsValuename));
        xml.append("        ").append(XMLHandler.addTagValue("descriptions_valuename", errorDescriptionsValuename));
        xml.append("        ").append(XMLHandler.addTagValue("fields_valuename", errorFieldsValuename));
        xml.append("        ").append(XMLHandler.addTagValue("codes_valuename", errorCodesValuename));
        xml.append("        ").append(XMLHandler.addTagValue("max_errors", maxErrors));
        xml.append("        ").append(XMLHandler.addTagValue("max_pct_errors", maxPercentErrors));
        xml.append("        ").append(XMLHandler.addTagValue("min_pct_rows", minPercentRows));
        xml.append("      ").append(XMLHandler.closeTag(XML_TAG)).append(Const.CR);
        
        return xml.toString();
    }
    
    public StepErrorMeta(VariableSpace variables, Node node, List<StepMeta> steps)
    {
    	this.variables = variables;
    	
        sourceStep = StepMeta.findStep(steps, XMLHandler.getTagValue(node, "source_step"));
        targetStep = StepMeta.findStep(steps, XMLHandler.getTagValue(node, "target_step"));
        enabled = "Y".equals( XMLHandler.getTagValue(node, "is_enabled") );
        nrErrorsValuename = XMLHandler.getTagValue(node, "nr_valuename");
        errorDescriptionsValuename = XMLHandler.getTagValue(node, "descriptions_valuename");
        errorFieldsValuename = XMLHandler.getTagValue(node, "fields_valuename");
        errorCodesValuename = XMLHandler.getTagValue(node, "codes_valuename");
        maxErrors = Const.toLong(XMLHandler.getTagValue(node, "max_errors"), -1L);
        maxPercentErrors = Const.toInt(XMLHandler.getTagValue(node, "max_pct_errors"), -1);
        minPercentRows = Const.toLong(XMLHandler.getTagValue(node, "min_pct_rows"), -1L);
    }

    public void saveRep(Repository rep, long id_transformation, long id_step) throws KettleException
    {
        rep.saveStepAttribute(id_transformation, id_step, "step_error_handling_source_step", sourceStep!=null ? sourceStep.getName() : "");
        rep.saveStepAttribute(id_transformation, id_step, "step_error_handling_target_step", targetStep!=null ? targetStep.getName() : "");
        rep.saveStepAttribute(id_transformation, id_step, "step_error_handling_is_enabled",  enabled);
        rep.saveStepAttribute(id_transformation, id_step, "step_error_handling_nr_valuename",  nrErrorsValuename);
        rep.saveStepAttribute(id_transformation, id_step, "step_error_handling_descriptions_valuename",  errorDescriptionsValuename);
        rep.saveStepAttribute(id_transformation, id_step, "step_error_handling_fields_valuename",  errorFieldsValuename);
        rep.saveStepAttribute(id_transformation, id_step, "step_error_handling_codes_valuename",  errorCodesValuename);
        rep.saveStepAttribute(id_transformation, id_step, "step_error_handling_max_errors",  maxErrors);
        rep.saveStepAttribute(id_transformation, id_step, "step_error_handling_max_pct_errors",  maxPercentErrors);
        rep.saveStepAttribute(id_transformation, id_step, "step_error_handling_min_pct_rows",  minPercentRows);
    }
    
    public StepErrorMeta(VariableSpace variables, Repository rep, StepMeta stepMeta, List<StepMeta> steps) throws KettleException
    {
    	this.variables = variables;
    	
        sourceStep = stepMeta;
        targetStep = StepMeta.findStep( steps, rep.getStepAttributeString(stepMeta.getID(), "step_error_handling_target_step") );
        enabled = rep.getStepAttributeBoolean(stepMeta.getID(), "step_error_handling_is_enabled");
        nrErrorsValuename = rep.getStepAttributeString(stepMeta.getID(), "step_error_handling_nr_valuename");
        errorDescriptionsValuename = rep.getStepAttributeString(stepMeta.getID(), "step_error_handling_descriptions_valuename");
        errorFieldsValuename = rep.getStepAttributeString(stepMeta.getID(), "step_error_handling_fields_valuename");
        errorCodesValuename = rep.getStepAttributeString(stepMeta.getID(), "step_error_handling_codes_valuename");
        maxErrors = rep.getStepAttributeInteger(stepMeta.getID(), "step_error_handling_max_errors");
        maxPercentErrors = (int) rep.getStepAttributeInteger(stepMeta.getID(), "step_error_handling_max_pct_errors");
        minPercentRows = rep.getStepAttributeInteger(stepMeta.getID(), "step_error_handling_min_pct_rows");
    }
    
    /**
     * @return the error codes valuename
     */
    public String getErrorCodesValuename()
    {
        return errorCodesValuename;
    }

    /**
     * @param errorCodesValuename the error codes valuename to set
     */
    public void setErrorCodesValuename(String errorCodesValuename)
    {
        this.errorCodesValuename = errorCodesValuename;
    }

    /**
     * @return the error descriptions valuename
     */
    public String getErrorDescriptionsValuename()
    {
        return errorDescriptionsValuename;
    }

    /**
     * @param errorDescriptionsValuename the error descriptions valuename to set
     */
    public void setErrorDescriptionsValuename(String errorDescriptionsValuename)
    {
        this.errorDescriptionsValuename = errorDescriptionsValuename;
    }

    /**
     * @return the error fields valuename
     */
    public String getErrorFieldsValuename()
    {
        return errorFieldsValuename;
    }

    /**
     * @param errorFieldsValuename the error fields valuename to set
     */
    public void setErrorFieldsValuename(String errorFieldsValuename)
    {
        this.errorFieldsValuename = errorFieldsValuename;
    }

    /**
     * @return the nr errors valuename
     */
    public String getNrErrorsValuename()
    {
        return nrErrorsValuename;
    }

    /**
     * @param nrErrorsValuename the nr errors valuename to set
     */
    public void setNrErrorsValuename(String nrErrorsValuename)
    {
        this.nrErrorsValuename = nrErrorsValuename;
    }

    /**
     * @return the target step
     */
    public StepMeta getTargetStep()
    {
        return targetStep;
    }

    /**
     * @param targetStep the target step to set
     */
    public void setTargetStep(StepMeta targetStep)
    {
        this.targetStep = targetStep;
    }

    /**
     * @return The source step can send the error rows
     */
    public StepMeta getSourceStep()
    {
        return sourceStep;
    }

    /**
     * @param sourceStep The source step can send the error rows
     */
    public void setSourceStep(StepMeta sourceStep)
    {
        this.sourceStep = sourceStep;
    }

    /**
     * @return the enabled flag: Is the error handling enabled?
     */
    public boolean isEnabled()
    {
        return enabled;
    }

    /**
     * @param enabled the enabled flag to set: Is the error handling enabled?
     */
    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    public RowMetaInterface getErrorFields()
    {
        return getErrorRowMeta(0L, null, null, null);
    }
    
    public RowMetaInterface getErrorRowMeta(long nrErrors, String errorDescriptions, String fieldNames, String errorCodes)
    {
        RowMetaInterface row = new RowMeta();
        
        String nrErr = variables.environmentSubstitute(getNrErrorsValuename());
        if (!Const.isEmpty(nrErr))
        {
            ValueMetaInterface v = new ValueMeta(nrErr, ValueMetaInterface.TYPE_INTEGER);
            v.setLength(3);
            row.addValueMeta(v);
        }
        String errDesc = variables.environmentSubstitute(getErrorDescriptionsValuename());
        if (!Const.isEmpty(errDesc))
        {
            ValueMetaInterface v = new ValueMeta(errDesc, ValueMetaInterface.TYPE_STRING);
            row.addValueMeta(v);
        }
        String errFields = variables.environmentSubstitute(getErrorFieldsValuename());
        if (!Const.isEmpty(errFields))
        {
            ValueMetaInterface v = new ValueMeta(errFields, ValueMetaInterface.TYPE_STRING);
            row.addValueMeta(v);
        }
        String errCodes = variables.environmentSubstitute(getErrorCodesValuename());
        if (!Const.isEmpty(errCodes))
        {
            ValueMetaInterface v = new ValueMeta(errCodes, ValueMetaInterface.TYPE_STRING);
            row.addValueMeta(v);
        }
        
        return row;
    }
    
    public void addErrorRowData(Object[] row, int startIndex, long nrErrors, String errorDescriptions, String fieldNames, String errorCodes)
    {
        int index = startIndex;
        
        String nrErr = variables.environmentSubstitute(getNrErrorsValuename());
        if (!Const.isEmpty(nrErr))
        {
            row[index] = new Long(nrErrors);
            index++;
        }
        String errDesc = variables.environmentSubstitute(getErrorDescriptionsValuename());
        if (!Const.isEmpty(errDesc))
        {
            row[index] = errorDescriptions;
            index++;
        }
        String errFields = variables.environmentSubstitute(getErrorFieldsValuename());
        if (!Const.isEmpty(errFields))
        {
            row[index] = fieldNames;
            index++;
        }
        String errCodes = variables.environmentSubstitute(getErrorCodesValuename());
        if (!Const.isEmpty(errCodes))
        {
            row[index] = errorCodes;
            index++;
        }
    }


    /**
     * @return the maxErrors
     */
    public long getMaxErrors()
    {
        return maxErrors;
    }

    /**
     * @param maxErrors the maxErrors to set
     */
    public void setMaxErrors(long maxErrors)
    {
        this.maxErrors = maxErrors;
    }

    /**
     * @return the maxPercentErrors
     */
    public int getMaxPercentErrors()
    {
        return maxPercentErrors;
    }

    /**
     * @param maxPercentErrors the maxPercentErrors to set
     */
    public void setMaxPercentErrors(int maxPercentErrors)
    {
        this.maxPercentErrors = maxPercentErrors;
    }

    /**
     * @return the minRowsForPercent
     */
    public long getMinPercentRows()
    {
        return minPercentRows;
    }

    /**
     * @param minRowsForPercent the minRowsForPercent to set
     */
    public void setMinPercentRows(long minRowsForPercent)
    {
        this.minPercentRows = minRowsForPercent;
    }
}
