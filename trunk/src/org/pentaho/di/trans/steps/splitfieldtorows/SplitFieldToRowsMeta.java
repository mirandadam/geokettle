package org.pentaho.di.trans.steps.splitfieldtorows;


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


public class SplitFieldToRowsMeta extends BaseStepMeta implements StepMetaInterface
{
    /** Field to split */
	private String  splitField;
	
	/** Split field based upon this delimiter.*/
	private String  delimiter;

    /** New name of the split field */
	private String newFieldname;
	
	/** Flag indicating that a row number field should be included in the output */
	private  boolean includeRowNumber;
	
	/** The name of the field in the output containing the row number*/
	private  String  rowNumberField;
	
	/** Flag indicating that we should reset RowNum for each file */
	private boolean resetRowNumber;
	
	public SplitFieldToRowsMeta()
	{
		super(); // allocate BaseStepMeta
	}
	
    /**
     * @return Returns the delimiter.
     */
    public String getDelimiter()
    {
        return delimiter;
    }
    
    /**
     * @param delimiter The delimiter to set.
     */
    public void setDelimiter(String delimiter)
    {
        this.delimiter = delimiter;
    }
    
    /**
     * @return Returns the splitField.
     */
    public String getSplitField()
    {
        return splitField;
    }
    
    /**
     * @param splitField The splitField to set.
     */
    public void setSplitField(String splitField)
    {
        this.splitField = splitField;
    }
    
	public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleXMLException {
		readData(stepnode);
	}

	private void readData(Node stepnode)
		throws KettleXMLException
	{
		try
		{
			splitField = XMLHandler.getTagValue(stepnode, "splitfield"); //$NON-NLS-1$
			delimiter  = XMLHandler.getTagValue(stepnode, "delimiter"); //$NON-NLS-1$
			newFieldname = XMLHandler.getTagValue(stepnode, "newfield"); //$NON-NLS-1$
			includeRowNumber  = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "rownum"));
			resetRowNumber  = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "resetrownumber"));
			rowNumberField    = XMLHandler.getTagValue(stepnode, "rownum_field");
		}
		catch(Exception e)
		{
			throw new KettleXMLException(Messages.getString("SplitFieldToRowsMeta.Exception.UnableToLoadStepInfoFromXML"), e); //$NON-NLS-1$
		}
	}

	public void setDefault()
	{
		splitField = ""; //$NON-NLS-1$
		delimiter  = ";"; //$NON-NLS-1$
		newFieldname = "";
		includeRowNumber = false;
		rowNumberField   = "";
		resetRowNumber=true;
	}
	
	public void getFields(RowMetaInterface row, String name, RowMetaInterface[] info, StepMeta nextStep, VariableSpace space) throws KettleStepException {
		
		ValueMetaInterface v = new ValueMeta(newFieldname, ValueMetaInterface.TYPE_STRING);
		v.setOrigin(name);
		row.addValueMeta( v );
		
		// include row number
		if (includeRowNumber)
		{
			v = new ValueMeta(space.environmentSubstitute(rowNumberField), ValueMeta.TYPE_INTEGER);
			v.setLength(ValueMetaInterface.DEFAULT_INTEGER_LENGTH, 0);
			v.setOrigin(name);
			row.addValueMeta(v);
		}
	}

	public String getXML()
	{
        StringBuffer retval = new StringBuffer();

		retval.append("   "+XMLHandler.addTagValue("splitfield", splitField)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("   "+XMLHandler.addTagValue("delimiter", delimiter)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("   "+XMLHandler.addTagValue("newfield", newFieldname)); //$NON-NLS-1$ //$NON-NLS-2$
        retval.append("   "+XMLHandler.addTagValue("rownum",          includeRowNumber));
        retval.append("   "+XMLHandler.addTagValue("rownum_field",    rowNumberField));
        retval.append("   "+XMLHandler.addTagValue("resetrownumber",  resetRowNumber));
		
		return retval.toString();
	}
	
	public void readRep(Repository rep, long id_step, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleException {
		try
		{
			splitField  = rep.getStepAttributeString(id_step, "splitfield"); //$NON-NLS-1$
			delimiter   = rep.getStepAttributeString(id_step, "delimiter"); //$NON-NLS-1$
			newFieldname  = rep.getStepAttributeString(id_step, "newfield"); //$NON-NLS-1$
			includeRowNumber  = rep.getStepAttributeBoolean(id_step, "rownum");
			rowNumberField    = rep.getStepAttributeString (id_step, "rownum_field");
			resetRowNumber     = rep.getStepAttributeBoolean (id_step, "reset_rownumber");
		}
		catch(Exception e)
		{
			throw new KettleException(Messages.getString("SplitFieldToRowsMeta.Exception.UnexpectedErrorInReadingStepInfo"), e); //$NON-NLS-1$
		}
	}

	public void saveRep(Repository rep, long id_transformation, long id_step) throws KettleException
	{
		try
		{
			rep.saveStepAttribute(id_transformation, id_step, "splitfield", splitField); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "delimiter",  delimiter); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "newfield",  newFieldname); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "rownum",          includeRowNumber);
			rep.saveStepAttribute(id_transformation, id_step, "reset_rownumber",  resetRowNumber);
			rep.saveStepAttribute(id_transformation, id_step, "rownum_field",    rowNumberField);
		}
		catch(Exception e)
		{
			throw new KettleException(Messages.getString("SplitFieldToRowsMeta.Exception.UnableToSaveStepInfoToRepository")+id_step, e); //$NON-NLS-1$
		}
	}
	
	public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev, String[] input, String[] output, RowMetaInterface info) {
		String error_message=""; //$NON-NLS-1$
		CheckResult cr;
		
		// Look up fields in the input stream <prev>
		if (prev!=null && prev.size()>0)
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("SplitFieldToRowsMeta.CheckResult.StepReceivingFields",prev.size()+""), stepMeta); //$NON-NLS-1$ //$NON-NLS-2$
			remarks.add(cr);
			
			error_message = ""; //$NON-NLS-1$
			
			ValueMetaInterface v = prev.searchValueMeta(splitField);
			if (v==null)
			{
				error_message=Messages.getString("SplitFieldToRowsMeta.CheckResult.FieldToSplitNotPresentInInputStream" , splitField); //$NON-NLS-1$ 
				cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepMeta);
				remarks.add(cr);
			}
			else
			{
				cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("SplitFieldToRowsMeta.CheckResult.FieldToSplitFoundInInputStream" , splitField), stepMeta); //$NON-NLS-1$ 
				remarks.add(cr);
			}
		}
		else
		{
			error_message=Messages.getString("SplitFieldToRowsMeta.CheckResult.CouldNotReadFieldsFromPreviousStep")+Const.CR; //$NON-NLS-1$
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepMeta);
			remarks.add(cr);
		}

		// See if we have input streams leading to this step!
		if (input.length>0)
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("SplitFieldToRowsMeta.CheckResult.StepReceivingInfoFromOtherStep"), stepMeta); //$NON-NLS-1$
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("SplitFieldToRowsMeta.CheckResult.NoInputReceivedFromOtherStep"), stepMeta); //$NON-NLS-1$
			remarks.add(cr);
		}
		
		if (Const.isEmpty(newFieldname))
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("SplitFieldToRowsMeta.CheckResult.NewFieldNameIsNull"), stepMeta);
			remarks.add(cr);
		}
		if(includeRowNumber)
		{
			if(Const.isEmpty(transMeta.environmentSubstitute(rowNumberField)))
				cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("SplitFieldToRowsMeta.CheckResult.RowNumberFieldMissing"), stepMeta);
			else
				cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("SplitFieldToRowsMeta.CheckResult.RowNumberFieldOk"), stepMeta);
			remarks.add(cr);
		}
	}

	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta, Trans trans)
	{
		return new SplitFieldToRows(stepMeta, stepDataInterface, cnr, transMeta, trans);
	}

	public StepDataInterface getStepData()
	{
		return new SplitFieldToRowsData();
	}

	/**
	 * @return the newFieldname
	 */
	public String getNewFieldname() {
		return newFieldname;
	}

	/**
	 * @param newFieldname the newFieldname to set
	 */
	public void setNewFieldname(String newFieldname) {
		this.newFieldname = newFieldname;
	}
    /**
     * @return Returns the rowNumberField.
     */
    public String getRowNumberField()
    {
        return rowNumberField;
    }
    
    
    /**
     * @param rowNumberField The rowNumberField to set.
     */
    public void setRowNumberField(String rowNumberField)
    {
        this.rowNumberField = rowNumberField;
    }
    /**
     * @return Returns the resetRowNumber.
     */
    public boolean resetRowNumber()
    {
        return resetRowNumber;
    }
    /**
     * @param resetRowNumber The resetRowNumber to set.
     */
    public void setResetRowNumber(boolean resetRowNumber)
    {
        this.resetRowNumber = resetRowNumber;
    }
    
    /**
     * @param includeRowNumber The includeRowNumber to set.
     */
    public void setIncludeRowNumber(boolean includeRowNumber)
    {
        this.includeRowNumber = includeRowNumber;
    }
    
    /**
     * @return Returns the includeRowNumber.
     */
    public boolean includeRowNumber()
    {
        return includeRowNumber;
    }
}
