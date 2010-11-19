package org.pentaho.di.trans.steps.stepmeta;

import java.util.List;
import java.util.Map;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
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

public class StepMetastructureMeta extends BaseStepMeta implements StepMetaInterface {

	private String fieldName;
	private String comments;
	private String typeName;
	private String positionName;
	private String lengthName;
	private String precisionName;
	private String originName;
	
	private boolean outputRowcount;
	private String rowcountField;
	
	
	public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters)
	throws KettleXMLException
	{
		readData(stepnode);
	}
	
	public Object clone()
	{
		Object retval = super.clone();
		return retval;
	}
	
	
	public String getXML()
    {
        StringBuffer retval=new StringBuffer(500);
        
        retval.append("      ").append(XMLHandler.addTagValue("outputRowcount", outputRowcount));
        retval.append("    ").append(XMLHandler.addTagValue("rowcountField",  rowcountField));

        return retval.toString();
    }
	
	private void readData(Node stepnode) throws KettleXMLException
    {
        try
        {	
        	outputRowcount    = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "outputRowcount"));
        	rowcountField = XMLHandler.getTagValue(stepnode, "rowcountField");
        }
        catch(Exception e)
        {
            throw new KettleXMLException("Unable to load step info from XML", e);
        }
    }
	
	public void readRep(Repository rep, long id_step, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleException {
        try
        {
        	outputRowcount          =      rep.getStepAttributeBoolean(id_step, "outputRowcount");
        	rowcountField        =      rep.getStepAttributeString (id_step, "rowcountField");
    
        }
        catch(Exception e)
        {
            throw new KettleException("Unexpected error reading step information from the repository", e);
        }
    }
	
	public void saveRep(Repository rep, long id_transformation, long id_step) throws KettleException
    {
        try
        {
            rep.saveStepAttribute(id_transformation, id_step, "outputRowcount",      outputRowcount);
            rep.saveStepAttribute(id_transformation, id_step, "rowcountField",       rowcountField);
            
        }
        catch(Exception e)
        {
            throw new KettleException("Unable to save step information to the repository for id_step="+id_step, e);
        }
    }
	
	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta tr, Trans trans)
	{
		return new StepMetastructure(stepMeta, stepDataInterface, cnr, tr, trans);
	}
	
	public StepDataInterface getStepData()
	{
		return new StepMetastructureData();
	}
	
	public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepinfo, RowMetaInterface prev, String input[], String output[], RowMetaInterface info)
	{
		CheckResult cr;
		
		
		cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, "Not implemented", stepinfo);
		remarks.add(cr);
		
	}
	
	public void getFields(RowMetaInterface r, String name, RowMetaInterface[] info, StepMeta nextStep, VariableSpace space) throws KettleStepException
    {
        //we create a new output row structure - clear r
		r.clear();
        
		this.setDefault();
		// create the new fields
		//Position
        ValueMetaInterface positionFieldValue = new ValueMeta(positionName, ValueMetaInterface.TYPE_INTEGER);
        positionFieldValue.setOrigin(name);
        r.addValueMeta(positionFieldValue);
        //field name
        ValueMetaInterface nameFieldValue = new ValueMeta(fieldName, ValueMetaInterface.TYPE_STRING);
        nameFieldValue.setOrigin(name);
        r.addValueMeta(nameFieldValue);
        //comments
        ValueMetaInterface commentsFieldValue = new ValueMeta(comments, ValueMetaInterface.TYPE_STRING);
        nameFieldValue.setOrigin(name);
        r.addValueMeta(commentsFieldValue);
        //Type
        ValueMetaInterface typeFieldValue = new ValueMeta(typeName, ValueMetaInterface.TYPE_STRING);
        typeFieldValue.setOrigin(name);
        r.addValueMeta(typeFieldValue);
        //Length
        ValueMetaInterface lengthFieldValue = new ValueMeta(lengthName, ValueMetaInterface.TYPE_INTEGER);
        lengthFieldValue.setOrigin(name);
        r.addValueMeta(lengthFieldValue);
        //Precision
        ValueMetaInterface precisionFieldValue = new ValueMeta(precisionName, ValueMetaInterface.TYPE_INTEGER);
        precisionFieldValue.setOrigin(name);
        r.addValueMeta(precisionFieldValue);
        //Origin
        ValueMetaInterface originFieldValue = new ValueMeta(originName, ValueMetaInterface.TYPE_STRING);
        originFieldValue.setOrigin(name);
        r.addValueMeta(originFieldValue);
        
        if (isOutputRowcount()) {
	        //RowCount
	        ValueMetaInterface v=new ValueMeta(this.getRowcountField(), ValueMetaInterface.TYPE_INTEGER);
	        v.setOrigin(name);
	        r.addValueMeta( v );
        }

    }
	
	public void setDefault()
	{
		positionName = Messages.getString("StepMetastructureMeta.PositionName");
    	fieldName = Messages.getString("StepMetastructureMeta.FieldName");
    	comments = Messages.getString("StepMetastructureMeta.Comments");
        typeName = Messages.getString("StepMetastructureMeta.TypeName");
    	lengthName = Messages.getString("StepMetastructureMeta.LengthName");
    	precisionName = Messages.getString("StepMetastructureMeta.PrecisionName");
    	originName = Messages.getString("StepMetastructureMeta.OriginName");
    	
	}
	
	public boolean isOutputRowcount() {
		return outputRowcount;
	}

	public void setOutputRowcount(boolean outputRowcount) {
		this.outputRowcount = outputRowcount;
	}

	public String getRowcountField() {
		return rowcountField;
	}

	public void setRowcountField(String rowcountField) {
		this.rowcountField = rowcountField;
	}

}
