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
 
package org.pentaho.di.trans.steps.databasejoin;

import java.sql.ResultSet;

import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

/**
 * Use values from input streams to joins with values in a database.
 * Freehand SQL can be used to do this.
 * 
 * @author Matt
 * @since 26-apr-2003
 */
public class DatabaseJoin extends BaseStep implements StepInterface
{
	private DatabaseJoinMeta meta;
	private DatabaseJoinData data;
	
	public DatabaseJoin(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
	
	private synchronized void lookupValues(RowMetaInterface rowMeta, Object[] rowData) throws KettleException
	{
		if (first)
		{
			first=false;

			data.outputRowMeta = rowMeta.clone();
			meta.getFields(data.outputRowMeta, getStepname(), new RowMetaInterface[] { meta.getTableFields(), }, null, this);
			
			data.lookupRowMeta = new RowMeta();
			
			if (log.isDetailed()) logDetailed(Messages.getString("DatabaseJoin.Log.CheckingRow")+rowMeta.getString(rowData)); //$NON-NLS-1$
			
			data.keynrs = new int[meta.getParameterField().length];
			
			for (int i=0;i<meta.getParameterField().length;i++)
			{
				data.keynrs[i]=rowMeta.indexOfValue(meta.getParameterField()[i]);
				if (data.keynrs[i]<0)
				{
					throw new KettleStepException(Messages.getString("DatabaseJoin.Exception.FieldNotFound",meta.getParameterField()[i])); //$NON-NLS-1$ //$NON-NLS-2$
				}
				
				data.lookupRowMeta.addValueMeta( rowMeta.getValueMeta(data.keynrs[i]).clone() );
			}
		}
		
		// Construct the parameters row...
		Object[] lookupRowData = new Object[data.lookupRowMeta.size()];
		for (int i=0;i<data.keynrs.length;i++)
		{
			lookupRowData[i] = rowData[ data.keynrs[i] ];
		}
		
		// Set the values on the prepared statement (for faster exec.)
		ResultSet rs = data.db.openQuery(data.pstmt, data.lookupRowMeta, lookupRowData);
		
		// Get a row from the database...
		//
		Object[] add = data.db.getRow(rs);
		RowMetaInterface addMeta = data.db.getReturnRowMeta();
		
		incrementLinesInput();
		
		int counter = 0;
		while (add!=null && (meta.getRowLimit()==0 || counter<meta.getRowLimit()))
		{
			counter++;

			Object[] newRow = RowDataUtil.resizeArray(rowData, data.outputRowMeta.size());
			int newIndex = rowMeta.size();
			for (int i=0;i<addMeta.size();i++) {
				newRow[newIndex++] = add[i];
			}
			// we have to clone, otherwise we only get the last new value
			putRow(data.outputRowMeta, data.outputRowMeta.cloneRow(newRow));
			
			if (log.isRowLevel()) logRowlevel(Messages.getString("DatabaseJoin.Log.PutoutRow")+data.outputRowMeta.getString(newRow)); //$NON-NLS-1$
			
			// Get a new row
			if (meta.getRowLimit()==0 || counter<meta.getRowLimit()) 
			{
				add = data.db.getRow(rs);
				incrementLinesInput();
			}
		}
		
		// Nothing found? Perhaps we have to put something out after all?
		if (counter==0 && meta.isOuterJoin())
		{
			if (data.notfound==null)
			{
				// Just return null values for all values...
				//
				data.notfound = new Object[data.db.getReturnRowMeta().size()];
			}
			Object[] newRow = RowDataUtil.resizeArray(rowData, data.outputRowMeta.size());
			int newIndex = rowMeta.size();
			for (int i=0;i<data.notfound.length;i++) {
				newRow[newIndex++] = data.notfound[i];
			}
			putRow(data.outputRowMeta, newRow);
		}
		
		data.db.closeQuery(rs);
	}
	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		meta=(DatabaseJoinMeta)smi;
		data=(DatabaseJoinData)sdi;
		
		 boolean sendToErrorRow=false;
		 String errorMessage = null;

		Object[] r=getRow();       // Get row from input rowset & set row busy!
		if (r==null)  // no more input to be expected...
		{
			setOutputDone();
			return false;
		}
		  
		try
		{
			lookupValues(getInputRowMeta(), r); // add new values to the row in rowset[0].
			
            if (checkFeedback(getLinesRead())) 
            {
            	if(log.isBasic()) logBasic(Messages.getString("DatabaseJoin.Log.LineNumber")+getLinesRead()); //$NON-NLS-1$
            }
		}
		catch(KettleException e)
		{

			if (getStepMeta().isDoingErrorHandling())
	        {
                sendToErrorRow = true;
                errorMessage = e.toString();
	        }
			else
			{

				logError(Messages.getString("DatabaseJoin.Log.ErrorInStepRunning")+e.getMessage()); //$NON-NLS-1$
				setErrors(1);
				stopAll();
				setOutputDone();  // signal end to receiver(s)
				return false;
			}
			if (sendToErrorRow)
			{
			   // Simply add this row to the error row
			   putError(getInputRowMeta(), r, 1, errorMessage, null, "DBJOIN001");
			}
		}		
			
		return true;
	}
    
    /** Stop the running query */
    public synchronized void stopRunning(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
    {
        meta=(DatabaseJoinMeta)smi;
        data=(DatabaseJoinData)sdi;

        if (data.db!=null && !data.isCanceled)
        {
            data.db.cancelStatement(data.pstmt);
            setStopped(true);
            data.isCanceled=true;
        }
    }

	
	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(DatabaseJoinMeta)smi;
		data=(DatabaseJoinData)sdi;

		if (super.init(smi, sdi))
		{
			data.db=new Database(meta.getDatabaseMeta());
			data.db.shareVariablesWith(this);
			
			try
			{
                if (getTransMeta().isUsingUniqueConnections())
                {
                    synchronized (getTrans()) { data.db.connect(getTrans().getThreadName(), getPartitionID()); }
                }
                else
                {
                    data.db.connect(getPartitionID());
                }
				
                if (log.isDetailed()) logDetailed(Messages.getString("DatabaseJoin.Log.ConnectedToDB")); //$NON-NLS-1$
	
                String sql=meta.getSql();
                if(meta.isVariableReplace()) sql=environmentSubstitute(sql);
				// Prepare the SQL statement
				data.pstmt = data.db.prepareSQL(sql);
				if(log.isDebug()) log.logDebug(toString(), Messages.getString("DatabaseJoin.Log.SQLStatement",sql));
				data.db.setQueryLimit(meta.getRowLimit());
				
				return true;
			}
			catch(KettleException e)
			{
				logError(Messages.getString("DatabaseJoin.Log.DatabaseError")+e.getMessage()); //$NON-NLS-1$
				if (data.db!=null) {
                	data.db.disconnect();
				}
			}
		}
		
		return false;
	}
	
	public void dispose(StepMetaInterface smi, StepDataInterface sdi)
	{
	    meta = (DatabaseJoinMeta)smi;
	    data = (DatabaseJoinData)sdi;
	    
	    if (data.db!=null) {
        	data.db.disconnect();
	    }
	    
	    super.dispose(smi, sdi);
	}
	
	public String toString()
	{
		return this.getClass().getName();
	}

    //
    // Run is were the action happens!
    public void run()
    {
    	BaseStep.runStepThread(this, meta, data);
    }	
}