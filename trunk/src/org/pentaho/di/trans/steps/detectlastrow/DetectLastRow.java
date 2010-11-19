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
package org.pentaho.di.trans.steps.detectlastrow;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

/**
 * Detect last row in a stream
 *  
 * @author Samatar
 * @since 03June2008
 */
public class DetectLastRow extends BaseStep implements StepInterface
{
    private DetectLastRowMeta meta;
    private DetectLastRowData data;
    private Object[] previousRow;
    
    public DetectLastRow(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
    {
        super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
    }
       
    public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
    {
        meta=(DetectLastRowMeta)smi;
        data=(DetectLastRowData)sdi;
        
        Object[] r = getRow();      // Get row from input rowset & set row busy!        
        
        if(first)
        {
        	if (getInputRowMeta() == null)  {
        		return false;
        	}
        	
			// get the RowMeta
			data.previousRowMeta = getInputRowMeta().clone();
			data.NrPrevFields = data.previousRowMeta.size();
			data.outputRowMeta = data.previousRowMeta;
			meta.getFields(data.outputRowMeta, getStepname(), null, null, this);
        }
		Object[] outputRow=null;
		
		if (r==null)  // no more input to be expected...
		{
			if(previousRow != null) 
			{
				//
				// Output the last row with last row indicator set to true.
				//
		        outputRow = RowDataUtil.addRowData(previousRow, getInputRowMeta().size(), data.getTrueArray());
				putRow(data.outputRowMeta, outputRow);  // copy row to output rowset(s);

				if (log.isRowLevel()) {
					logRowlevel(Messages.getString("DetectLastRow.Log.WroteRowToNextStep")+data.outputRowMeta.getString(outputRow)); //$NON-NLS-1$
				}

		        if (checkFeedback(getLinesRead())) {
		        	logBasic(Messages.getString("DetectLastRow.Log.LineNumber")+getLinesRead()); //$NON-NLS-1$
		        }				
			}
			setOutputDone();
			return false;
		}

		if(!first)
		{
	        outputRow = RowDataUtil.addRowData(previousRow, getInputRowMeta().size(), data.getFalseArray());
			putRow(data.outputRowMeta, outputRow);  // copy row to output rowset(s);

			if (log.isRowLevel()) {
				logRowlevel(Messages.getString("DetectLastRow.Log.WroteRowToNextStep")+data.outputRowMeta.getString(outputRow)); //$NON-NLS-1$
			}

	        if (checkFeedback(getLinesRead())) {
	        	logBasic(Messages.getString("DetectLastRow.Log.LineNumber")+getLinesRead()); //$NON-NLS-1$
	        }		
		}
		// keep track of the current row
		previousRow = r;
		if (first) first = false;
            
        return true;
    }
    
    public boolean init(StepMetaInterface smi, StepDataInterface sdi)
    {
        meta=(DetectLastRowMeta)smi;
        data=(DetectLastRowData)sdi;

        if (super.init(smi, sdi))
        {
        	if(Const.isEmpty(meta.getResultFieldName()))
        	{
        		log.logError(toString(), Messages.getString("DetectLastRow.Error.ResultFieldMissing"));
        		return false;
        	}

            return true;
        }
        return false;
    }
        
    public void dispose(StepMetaInterface smi, StepDataInterface sdi)
    {
        meta = (DetectLastRowMeta)smi;
        data = (DetectLastRowData)sdi;
     
        super.dispose(smi, sdi);
    }
    
    //
    // Run is were the action happens!
    public void run()
    {
    	BaseStep.runStepThread(this, meta, data);
    }	    
}