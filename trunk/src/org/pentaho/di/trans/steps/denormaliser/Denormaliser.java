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
 package org.pentaho.di.trans.steps.denormaliser;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueDataUtil;
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
 * Denormalises data based on key-value pairs
 * 
 * @author Matt
 * @since 17-jan-2006
 */
public class Denormaliser extends BaseStep implements StepInterface
{
	private DenormaliserMeta meta;
	private DenormaliserData data;
	
	public Denormaliser(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
		
		meta=(DenormaliserMeta)getStepMeta().getStepMetaInterface();
		data=(DenormaliserData)stepDataInterface;
	}
	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		Object[] r=getRow();    // get row!
		if (r==null)  // no more input to be expected...
		{
			// Don't forget the last set of rows...
			if (data.previous!=null) 
			{
                // deNormalise(data.previous); --> That would overdo it.
				//
                Object[] outputRowData = buildResult(data.inputRowMeta, data.previous);
				putRow(data.outputRowMeta, outputRowData);
			}

			setOutputDone();
			return false;
		}
				
		if (first)
		{
			data.inputRowMeta = getInputRowMeta();
			data.outputRowMeta = data.inputRowMeta.clone();
			meta.getFields(data.outputRowMeta, getStepname(), null, null, this);
			
            data.keyFieldNr = data.inputRowMeta.indexOfValue( meta.getKeyField() );
            if (data.keyFieldNr<0)
            {
                logError(Messages.getString("Denormaliser.Log.KeyFieldNotFound",meta.getKeyField())); //$NON-NLS-1$ //$NON-NLS-2$
                setErrors(1);
                stopAll();
                return false;
            }
            
            Map<Integer, Integer> subjects = new Hashtable<Integer, Integer>();
            data.fieldNameIndex = new int[meta.getDenormaliserTargetField().length];
            for (int i=0;i<meta.getDenormaliserTargetField().length;i++)
			{
                DenormaliserTargetField field = meta.getDenormaliserTargetField()[i];
				int idx = data.inputRowMeta.indexOfValue( field.getFieldName() );
				if (idx<0)
				{
					logError(Messages.getString("Denormaliser.Log.UnpivotFieldNotFound",field.getFieldName())); //$NON-NLS-1$ //$NON-NLS-2$
					setErrors(1);
					stopAll();
					return false;
				}
                data.fieldNameIndex[i] = idx;
                subjects.put(Integer.valueOf(idx), Integer.valueOf(idx));
                
                // See if by accident, the value fieldname isn't the same as the key fieldname.
                // This is not supported of-course and given the complexity of the step, you can miss:
                if (data.fieldNameIndex[i]==data.keyFieldNr)
                {
                    logError(Messages.getString("Denormaliser.Log.ValueFieldSameAsKeyField", field.getFieldName())); //$NON-NLS-1$ //$NON-NLS-2$
                    setErrors(1);
                    stopAll();
                    return false;
                }

                // Fill a hashtable with the key strings and the position(s) of the field(s) in the row to take.
                // Store the indexes in a List so that we can accommodate multiple key/value pairs...
                // 
                String keyValue = environmentSubstitute(field.getKeyValue());
                List<Integer> indexes = data.keyValue.get(keyValue);
                if (indexes==null)
                {
                    indexes = new ArrayList<Integer>(2);
                }
                indexes.add(Integer.valueOf(i)); // Add the index to the list...
				data.keyValue.put(keyValue, indexes); // store the list
			}
            
            Set<Integer> subjectSet = subjects.keySet();
            data.fieldNrs = subjectSet.toArray(new Integer[subjectSet.size()]);
            
            data.groupnrs = new int[meta.getGroupField().length];
			for (int i=0;i<meta.getGroupField().length;i++)
			{
				data.groupnrs[i] = data.inputRowMeta.indexOfValue( meta.getGroupField()[i] );
				if (data.groupnrs[i]<0)
				{
					logError(Messages.getString("Denormaliser.Log.GroupingFieldNotFound",meta.getGroupField()[i])); //$NON-NLS-1$ //$NON-NLS-2$
					setErrors(1);
					stopAll();
					return false;
				}
			}
            
            List<Integer> removeList = new ArrayList<Integer>();
            removeList.add(Integer.valueOf(data.keyFieldNr));
            for (int i=0;i<data.fieldNrs.length;i++)
            {
                removeList.add(data.fieldNrs[i]);
            }
            Collections.sort(removeList);
            
            data.removeNrs = new int[removeList.size()];
            for (int i=0;i<removeList.size();i++) data.removeNrs[i] = removeList.get(i);
            
			data.previous=r;     // copy the row to previous
			newGroup();          // Create a new result row (init)
			
			first=false;
		}

				
		// System.out.println("Check for same group...");
        
		if (!sameGroup(data.inputRowMeta, data.previous, r))
		{
            // System.out.println("Different group!");
            
    		Object[] outputRowData = buildResult(data.inputRowMeta, data.previous);
    		putRow(data.outputRowMeta, outputRowData);        // copy row to possible alternate rowset(s).
            //System.out.println("Wrote row: "+data.previous);
            newGroup();       // Create a new group aggregate (init)
            deNormalise(data.inputRowMeta, r);
		}
        else
        {
            deNormalise(data.inputRowMeta, r);
        }

		data.previous=r;
        
        if (checkFeedback(getLinesRead())) 
        {
        	if(log.isBasic()) logBasic(Messages.getString("Denormaliser.Log.LineNumber")+getLinesRead()); //$NON-NLS-1$
        }
			
		return true;
	}
	
	private Object[] buildResult(RowMetaInterface rowMeta, Object[] rowData) throws KettleValueException
    {
		// Deleting objects: we need to create a new object array
		// It's useless to call RowDataUtil.resizeArray
		//
		Object[] outputRowData = RowDataUtil.allocateRowData( data.outputRowMeta.size() );
		int outputIndex = 0;
		
		// Copy the data from the incoming row, but remove the unwanted fields in the same loop...
		//
		int removeIndex=0;
		for (int i=0;i<rowMeta.size();i++) {
			if (removeIndex<data.removeNrs.length && i==data.removeNrs[removeIndex]) {
				removeIndex++;
			} else
			{
				outputRowData[outputIndex++]=rowData[i];
			}
		}

        // Add the unpivoted fields...
		//
        for (int i=0;i<data.targetResult.length;i++)
        {
            Object resultValue = data.targetResult[i];
            DenormaliserTargetField field = meta.getDenormaliserTargetField()[i];
            switch(field.getTargetAggregationType())
            {
            case DenormaliserTargetField.TYPE_AGGR_AVERAGE :
                long count = data.counters[i];
                Object sum  = data.sum[i];
                if (count>0)
                {
                	if (sum instanceof Long) resultValue = (long)((Long)sum / count);
                	else if (sum instanceof Double) resultValue = (double)((Double)sum / count);
                	else if (sum instanceof BigDecimal) resultValue = ((BigDecimal)sum).divide(new BigDecimal(count));
                	else resultValue = null; // TODO: perhaps throw an exception here?<
                }
                break;
            case DenormaliserTargetField.TYPE_AGGR_COUNT_ALL :
                if (resultValue == null) resultValue = Long.valueOf(0);
                if (field.getTargetType() != ValueMetaInterface.TYPE_INTEGER)
                {
                    resultValue = data.outputRowMeta.getValueMeta(outputIndex).convertData(new ValueMeta("num_values_aggregation", ValueMetaInterface.TYPE_INTEGER), resultValue);
                }
                break;
            default: break;
            }
            outputRowData[outputIndex++] = resultValue;
        }
        
        return outputRowData;
    }

    // Is the row r of the same group as previous?
	private boolean sameGroup(RowMetaInterface rowMeta, Object[] previous, Object[] rowData) throws KettleValueException
	{
		return rowMeta.compare(previous, rowData, data.groupnrs)==0;
	}

	/** Initialize a new group... */
	private void newGroup()
	{
		// There is no need anymore to take care of the meta-data.
		// That is done once in DenormaliserMeta.getFields()
		//
        data.targetResult = new Object[meta.getDenormaliserTargetFields().length];
        
        for (int i=0;i<meta.getDenormaliserTargetFields().length;i++)
        {
            data.targetResult[i] = null;

            data.counters[i]=0L; // set to 0
            data.sum[i]=null;
        }
	}
	
    /**
     * This method de-normalizes a single key-value pair.
     * It looks up the key and determines the value name to store it in.
     * It converts it to the right type and stores it in the result row.
     * 
     * @param r
     * @throws KettleValueException
     */
	private void deNormalise(RowMetaInterface rowMeta, Object[] rowData) throws KettleValueException
	{
		ValueMetaInterface valueMeta = rowMeta.getValueMeta(data.keyFieldNr);
		Object valueData = rowData[data.keyFieldNr];
		String key = valueMeta.getCompatibleString(valueData);
        if ( !Const.isEmpty(key) )
        {
            // Get all the indexes for the given key value...
        	// 
            List<Integer> indexes = data.keyValue.get(key);
            if (indexes!=null) // otherwise we're not interested.
            {
                for (int i=0;i<indexes.size();i++)
                {
                    Integer keyNr = indexes.get(i);
                    if (keyNr!=null)
                    {
                        // keyNr is the field in DenormaliserTargetField[]
                        //
                        int idx = keyNr.intValue();
                        DenormaliserTargetField field = meta.getDenormaliserTargetField()[idx];
                        
                        // This is the value we need to de-normalise, convert, aggregate.
                        //
                        ValueMetaInterface sourceMeta = rowMeta.getValueMeta(data.fieldNameIndex[idx]);
                        Object sourceData = rowData[data.fieldNameIndex[idx]];
                        Object targetData;
                        // What is the target value metadata??
                        // 
                        ValueMetaInterface targetMeta = data.outputRowMeta.getValueMeta(data.inputRowMeta.size()-data.removeNrs.length+idx);
                        // What was the previous target in the result row?
                        //
                        Object prevTargetData = data.targetResult[idx];
                        
                        switch(field.getTargetAggregationType())
                        {
                        case DenormaliserTargetField.TYPE_AGGR_SUM:
                            targetData = targetMeta.convertData(sourceMeta, sourceData);
                        	if (prevTargetData!=null)
                        	{
                        		prevTargetData = ValueDataUtil.plus(targetMeta, prevTargetData, targetMeta, targetData);
                        	}
                        	else
                        	{
                        		prevTargetData = targetData;
                        	}
                            break;
                        case DenormaliserTargetField.TYPE_AGGR_MIN:
                            if (sourceMeta.compare(sourceData, targetMeta, prevTargetData)<0) {
                            	prevTargetData = targetMeta.convertData(sourceMeta, sourceData);
                            }
                            break;
                        case DenormaliserTargetField.TYPE_AGGR_MAX:
                            if (sourceMeta.compare(sourceData, targetMeta, prevTargetData)>0) {
                            	prevTargetData = targetMeta.convertData(sourceMeta, sourceData);
                            }
                            break;
                        case DenormaliserTargetField.TYPE_AGGR_COUNT_ALL:
                            prevTargetData = ++data.counters[idx];
                            break;
                        case DenormaliserTargetField.TYPE_AGGR_AVERAGE:
                            targetData = targetMeta.convertData(sourceMeta, sourceData);
                            if (!sourceMeta.isNull(sourceData)) 
                            {
                                prevTargetData = data.counters[idx]++;
                                if (data.sum[idx]==null)
                                {
                                	data.sum[idx] = targetData;
                                }
                                else
                                {
                                	data.sum[idx] = ValueDataUtil.plus(targetMeta, data.sum[idx], targetMeta, targetData);
                                }
                                // data.sum[idx] = (Integer)data.sum[idx] + (Integer)sourceData;
                            }
                            break;
                        case DenormaliserTargetField.TYPE_AGGR_NONE:
                        default:
                            targetData = targetMeta.convertData(sourceMeta, sourceData);
                            prevTargetData = targetMeta.convertData(sourceMeta, sourceData); // Overwrite the previous
                            break;
                        }
                        
                        // Update the result row too
                        //
                        data.targetResult[idx] = prevTargetData;
                    }
                }
            }
        }
	}
    
	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(DenormaliserMeta)smi;
		data=(DenormaliserData)sdi;
		
		if (super.init(smi, sdi))
		{
            data.counters = new long[meta.getDenormaliserTargetField().length];
            data.sum      = new Object[meta.getDenormaliserTargetField().length];

            return true;
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