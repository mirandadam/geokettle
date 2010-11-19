 /* Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Samatar Hassan.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.*/
 
package org.pentaho.di.trans.steps.delay;

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
 * Created on 27-06-2008
 *
 */

public class DelayMeta extends BaseStepMeta implements StepMetaInterface
{
	private String timeout;
	private String scaletime;
	public static String DEFAULT_SCALE_TIME="seconds";
	
	public String[] ScaleTimeCode= {"milliseconds","seconds","minutes","hours"}; // before 3.1.1 it was "millisecond","second","minute","hour"--> keep compatibilty see PDI-1850, PDI-1532
	
	public DelayMeta()
	{
		super(); // allocate BaseStepMeta
	}
   public String getXML()
    {
        StringBuffer retval = new StringBuffer();
        retval.append("    " + XMLHandler.addTagValue("timeout", timeout));
        retval.append("    " + XMLHandler.addTagValue("scaletime", scaletime));
        
        return retval.toString();
    }
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
	public String getScaleTime()
	{
		return scaletime;
	}
	 public void setScaleTimeCode(int ScaleTimeIndex)
	 {
		 switch (ScaleTimeIndex) {
		 case 0 :
			 scaletime=ScaleTimeCode[0]; // milliseconds
		 	break;
		 case 1 :
			 scaletime=ScaleTimeCode[1]; // second
		 	break;
		 case 2 :
			 scaletime=ScaleTimeCode[2]; // minutes
		 	break;
		 case 3 :
			 scaletime=ScaleTimeCode[3]; // hours
		 	break;		 	
		 default: 
			 scaletime=ScaleTimeCode[1]; // seconds
		 	break;
		 }
	 }
	 public int getScaleTimeCode()
	 {
		 int retval=1; // DEFAULT: seconds
		 if(scaletime==null) return retval;
		 if(scaletime.equals(ScaleTimeCode[0]))
			 retval=0;
		 else if(scaletime.equals(ScaleTimeCode[1]))
			 retval=1;
		 else if(scaletime.equals(ScaleTimeCode[2]))
			 retval=2;
		 else if(scaletime.equals(ScaleTimeCode[3]))
			 retval=3;
		 
		 return retval;
	 }
		
	private void readData(Node stepnode) throws KettleXMLException
	{
		try{
			timeout = XMLHandler.getTagValue(stepnode, "timeout");
			scaletime = XMLHandler.getTagValue(stepnode, "scaletime");
			// set all unknown values to seconds
			setScaleTimeCode(getScaleTimeCode()); // compatibility reasons for transformations before 3.1.1, see PDI-1850, PDI-1532
			
		}
	    catch (Exception e)
        {
            throw new KettleXMLException(Messages.getString("DelayMeta.Exception.UnableToReadStepInfo"), e);
        }
	}

	public void setDefault()
	{
		timeout="1"; //default one second
		scaletime=DEFAULT_SCALE_TIME; // defaults to "seconds"
	}
	public String getTimeOut()
	{
		return timeout;
	}
	public void setTimeOut(String timeout)
	{
		this.timeout=timeout;
	}
	public void readRep(Repository rep, long id_step, List<DatabaseMeta> databases, Map<String, Counter> counters)
		throws KettleException
	{
		try{
			timeout = rep.getStepAttributeString(id_step, "timeout");
			scaletime = rep.getStepAttributeString(id_step, "scaletime");
			// set all unknown values to seconds
			setScaleTimeCode(getScaleTimeCode()); // compatibility reasons for transformations before 3.1.1, see PDI-1850, PDI-1532
		}
		 catch (Exception e)
	     {
	        throw new KettleException(Messages.getString("DelayMeta.Exception.UnexpectedErrorReadingStepInfo"), e); //$NON-NLS-1$
	     }
	}
	
	
	public void saveRep(Repository rep, long id_transformation, long id_step)
		throws KettleException
	{
		try{
			rep.saveStepAttribute(id_transformation, id_step, "timeout", timeout);
			rep.saveStepAttribute(id_transformation, id_step, "scaletime", scaletime);
			
		}
		catch (Exception e)
        {
            throw new KettleException(Messages.getString("DelayMeta.Exception.UnexpectedErrorSavingStepInfo"), e); //$NON-NLS-1$
        }
	}
	
	public void getFields(RowMetaInterface rowMeta, String origin, RowMetaInterface[] info, StepMeta nextStep, VariableSpace space) throws KettleStepException
	{
	}
	
	public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepinfo, RowMetaInterface prev, String input[], String output[], RowMetaInterface info)
	{
		CheckResult cr;
		String error_message="";
		
		if (Const.isEmpty(timeout))
        {
            error_message = Messages.getString("DelayMeta.CheckResult.TimeOutMissing"); //$NON-NLS-1$
            cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepinfo);
        }
        else
        {
            error_message = Messages.getString("DelayMeta.CheckResult.TimeOutOk"); //$NON-NLS-1$
            cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, error_message, stepinfo);
        }
		remarks.add(cr);
		
		
	
		if (prev==null || prev.size()==0)
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_WARNING, Messages.getString("DelayMeta.CheckResult.NotReceivingFields"), stepinfo); //$NON-NLS-1$
		}
		else
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, Messages.getString("DelayMeta.CheckResult.StepRecevingData",prev.size()+""), stepinfo); //$NON-NLS-1$ //$NON-NLS-2$
		}
		remarks.add(cr);
		
		// See if we have input streams leading to this step!
		if (input.length>0)
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, Messages.getString("DelayMeta.CheckResult.StepRecevingData2"), stepinfo); //$NON-NLS-1$
		}
		else
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, Messages.getString("DelayMeta.CheckResult.NoInputReceivedFromOtherSteps"), stepinfo); //$NON-NLS-1$
		}
		remarks.add(cr);
	}
	
	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta tr, Trans trans)
	{
		return new Delay(stepMeta, stepDataInterface, cnr, tr, trans);
	}
	
	public StepDataInterface getStepData()
	{
		return new DelayData();
	}
}
