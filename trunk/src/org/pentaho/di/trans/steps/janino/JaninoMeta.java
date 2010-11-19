 /**********************************************************************
 **                                                                   **
 **               This code belongs to the KETTLE project.            **
 **                                                                   **
 ** Kettle, from version 2.2 on, is released into the public domain   **
 ** under the Lesser GNU Public License (LGPL).                       **
 **                                                                   **
 ** For more details, please read the document LICENSE.txt, included  **
 ** in this project                                                   **
 **                                                                   **
 ** http://www.kettle.be                                              **
 ** info@kettle.be                                                    **
 **                                                                   **
 **********************************************************************/
 
package org.pentaho.di.trans.steps.janino;

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




/**
 * Contains the meta-data for the Formula step: calculates ad-hoc formula's
 * Powered by Pentaho's "libformula"
 * 
 * Created on 22-feb-2007
 */

public class JaninoMeta extends BaseStepMeta implements StepMetaInterface
{
    /** The formula calculations to be performed */
    private JaninoMetaFunction[] formula;
    
    public JaninoMeta()
	{
		super(); // allocate BaseStepMeta
	}

    public JaninoMetaFunction[] getFormula()
    {
        return formula;
    }
    
    public void setFormula(JaninoMetaFunction[] calcTypes)
    {
        this.formula = calcTypes;
    }
    
    public void allocate(int nrCalcs)
    {
        formula = new JaninoMetaFunction[nrCalcs];
    }
    
	public void loadXML(Node stepnode, List<DatabaseMeta> databases,  Map<String, Counter> counters) throws KettleXMLException
	{
        int nrCalcs   = XMLHandler.countNodes(stepnode,   JaninoMetaFunction.XML_TAG);
        allocate(nrCalcs);
        for (int i=0;i<nrCalcs;i++)
        {
            Node calcnode = XMLHandler.getSubNodeByNr(stepnode, JaninoMetaFunction.XML_TAG, i);
            formula[i] = new JaninoMetaFunction(calcnode);
        }
	}
    
    public String getXML()
    {
        StringBuffer retval = new StringBuffer();
       
        if (formula!=null)
        for (int i=0;i<formula.length;i++)
        {
            retval.append("       "+formula[i].getXML()+Const.CR);
        }
        
        return retval.toString();
    }

    public boolean equals(Object obj)
    {       
        if (obj != null && (obj.getClass().equals(this.getClass())))
        {
        	JaninoMeta m = (JaninoMeta)obj;
            return (getXML() == m.getXML());
        }

        return false;
    }        
    
	public Object clone()
	{
		JaninoMeta retval = (JaninoMeta) super.clone();
        if (formula!=null)
        {
            retval.allocate(formula.length);
            for (int i=0;i<formula.length;i++) retval.getFormula()[i] = (JaninoMetaFunction) formula[i].clone();
        }
        else
        {
            retval.allocate(0);
        }
		return retval;
	}

	public void setDefault()
	{
        formula = new JaninoMetaFunction[0]; 
	}

	public void readRep(Repository rep, long id_step, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleException 
	{
        int nrCalcs     = rep.countNrStepAttributes(id_step, "field_name");
        allocate(nrCalcs);
        for (int i=0;i<nrCalcs;i++)
        {
            formula[i] = new JaninoMetaFunction(rep, id_step, i);
        }
	}
	
	public void saveRep(Repository rep, long id_transformation, long id_step)
		throws KettleException
	{
        for (int i=0;i<formula.length;i++)
        {
            formula[i].saveRep(rep, id_transformation, id_step, i);
        }
	}
	
	@Override
	public void getFields(RowMetaInterface row, String name, RowMetaInterface[] info, StepMeta nextStep, VariableSpace space) throws KettleStepException {
        for (int i=0;i<formula.length;i++)
        {
            JaninoMetaFunction fn = formula[i];
            if (Const.isEmpty(fn.getReplaceField())) { // Not replacing a field.
		        if (!Const.isEmpty(fn.getFieldName())) // It's a new field!
		        {
		            ValueMetaInterface v = new ValueMeta(fn.getFieldName(), fn.getValueType());
		            v.setLength(fn.getValueLength(), fn.getValuePrecision());
		            v.setOrigin(name);
		            row.addValueMeta(v);
		        }
            } else { // Replacing a field
            	int index = row.indexOfValue(fn.getReplaceField());
            	if (index<0) {
            		throw new KettleStepException("Unknown field specified to replace with a formula result: ["+fn.getReplaceField()+"]");
            	}
            	// Change the data type etc.
            	//
            	ValueMetaInterface v = row.getValueMeta(index).clone();
	            v.setLength(fn.getValueLength(), fn.getValuePrecision());
	            v.setOrigin(name);
	            row.setValueMeta(index, v); // replace it
            }
        }
    }
	    
	/**
	 * Checks the settings of this step and puts the findings in a remarks List.
	 * @param remarks The list to put the remarks in @see org.pentaho.di.core.CheckResult
	 * @param stepMeta The stepMeta to help checking
	 * @param prev The fields coming from the previous step
	 * @param input The input step names
	 * @param output The output step names
	 * @param info The fields that are used as information by the step
	 */
	public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepinfo, RowMetaInterface prev, String input[], String output[], RowMetaInterface info)
	{
		CheckResult cr;
		if (prev==null || prev.size()==0)
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_WARNING, Messages.getString("JaninoMeta.CheckResult.ExpectedInputError"), stepinfo);
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("JaninoMeta.CheckResult.FieldsReceived", ""+prev.size()), stepinfo);
			remarks.add(cr);
		}
		
		// See if we have input streams leading to this step!
		if (input.length>0)
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("JaninoMeta.CheckResult.ExpectedInputOk"), stepinfo);
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("JaninoMeta.CheckResult.ExpectedInputError"), stepinfo);
			remarks.add(cr);
		}
	}

	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta tr, Trans trans)
	{
		return new Janino(stepMeta, stepDataInterface, cnr, tr, trans);
	}
	
	public StepDataInterface getStepData()
	{
		return new JaninoData();
	}


}
