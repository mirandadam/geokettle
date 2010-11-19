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
 
package org.pentaho.di.trans.steps.sortedmerge;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.pentaho.di.core.RowSet;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;


/**
 * Do nothing.  Pass all input data to the next steps.
 * 
 * @author Matt
 * @since 2-jun-2003
 */
public class SortedMerge extends BaseStep implements StepInterface
{
	private SortedMergeMeta meta;
	private SortedMergeData data;
	
	public SortedMerge(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
    
    
    /**
     * We read from all streams in the partition merge mode
     * For that we need at least one row on all input rowsets...
     * If we don't have a row, we wait for one.
     * 
     * TODO: keep the inputRowSets() list sorted and go from there. That should dramatically improve speed as you only need half as many comparisons.
     * 
     * @return the next row
     */
    private synchronized Object[] getRowSorted() throws KettleException {
        if (first) {
        	first=false;
        	
        	// Verify that socket connections to all the remote input steps are opened 
        	// before we start to read/write ...
        	//
        	openRemoteInputStepSocketsOnce();
        	
        	// Read one row from all rowsets...
        	// 
        	data.sortedBuffer = new ArrayList<RowSetRow>();
        	data.rowMeta = null;
        	
        	// PDI-1212:
        	// If one of the inputRowSets holds a null row (the input yields 
        	// 0 rows), then the null rowSet is removed from the InputRowSet buffer.. (BaseStep.getRowFrom())
        	// which throws this loop off by one (the next set never gets processed). 
        	// Instead of modifying BaseStep, I figure reversing the loop here would
        	// effect change in less areas. If the reverse loop causes a problem, please
        	// re-open http://jira.pentaho.com/browse/PDI-1212.
        	
        	for (int i=inputRowSets.size()-1; i >= 0 && !isStopped(); i--) {
        		
        		RowSet rowSet = inputRowSets.get(i);
                Object[] row = getRowFrom(rowSet);
                if (row!=null) {
                	// Add this row to the sortedBuffer...
                	// Which is not yet sorted, we'll get to that later.
                	//
                	data.sortedBuffer.add( new RowSetRow(rowSet, rowSet.getRowMeta(), row) );
                	if (data.rowMeta==null) data.rowMeta = rowSet.getRowMeta().clone();
                	
                    // What fields do we compare on and in what order?
                    
                    // Better cache the location of the partitioning column
                    // First time operation only
                    //
                    if (data.fieldIndices==null)
                    {
                        // Get the indexes of the specified sort fields...
                        data.fieldIndices = new int[meta.getFieldName().length];
                        for (int f=0;f<data.fieldIndices.length;f++)
                        {
                            data.fieldIndices[f] = data.rowMeta.indexOfValue(meta.getFieldName()[f]);
                            if (data.fieldIndices[f]<0)
                            {
                                throw new KettleStepException("Unable to find fieldname ["+meta.getFieldName()[f]+"] in row : "+data.rowMeta);
                            }
                            
                            data.rowMeta.getValueMeta( data.fieldIndices[f] ).setSortedDescending( !meta.getAscending()[f] );
                        }
                    }
                }
        		
        		data.comparator = new Comparator<RowSetRow>() {
    				
					public int compare(RowSetRow o1, RowSetRow o2) {
						try {
							return o1.getRowMeta().compare(o1.getRowData(), o2.getRowData(), data.fieldIndices);
						} catch (KettleValueException e) {
							return 0; // TODO see if we should fire off alarms over here... Perhaps throw a RuntimeException.
						}
					}
			    };
        		
        		// Now sort the sortedBuffer for the first time.
        		//
        		Collections.sort(data.sortedBuffer, data.comparator);
        	}
        }

        // If our sorted buffer is empty, it means we're done...
        //
        if (data.sortedBuffer.isEmpty()) {
        	return null;
        }
        
        // now that we have all rows sorted, all we need to do is find out what the smallest row is.
        // The smallest row is the first in our case...
        //
        RowSetRow smallestRow = data.sortedBuffer.get(0);
        data.sortedBuffer.remove(0);
        Object[] outputRowData = smallestRow.getRowData();

        // We read another row from the row set where the smallest row came from.
        // That we we exhaust all row sets.
        //
        Object[] extraRow = getRowFrom(smallestRow.getRowSet());
        
        // Add it to the sorted buffer in the right position...
        //
        if (extraRow!=null) {
        	// Add this one to the sortedBuffer
        	//
        	RowSetRow add = new RowSetRow(smallestRow.getRowSet(), smallestRow.getRowSet().getRowMeta(), extraRow);
        	int index = Collections.binarySearch(data.sortedBuffer, add, data.comparator);
        	if (index<0) {
        		data.sortedBuffer.add(-index-1, add);
        	} else
        	{
        		data.sortedBuffer.add(index, add);
        	}
        }
        
        // This concludes the regular program...
        //
        
        // optionally perform safe mode checking to prevent problems.
        // 
        if (isSafeModeEnabled())
        {
        	// for checking we need to get data and meta
        	//
        	safeModeChecking(smallestRow.getRowMeta());            
        }
        
        return outputRowData;
    }
	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		meta=(SortedMergeMeta)smi;
		data=(SortedMergeData)sdi;

		Object[] row=getRowSorted();    // get row, sorted
		if (row==null)  // no more input to be expected...
		{
			setOutputDone();
			return false;
		}
        
		putRow(data.rowMeta,row);     // copy row to possible alternate rowset(s).

        if (checkFeedback(getLinesRead())) logBasic(Messages.getString("SortedMerge.Log.LineNumber")+getLinesRead()); //$NON-NLS-1$
			
		return true;
	}


	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(SortedMergeMeta)smi;
		data=(SortedMergeData)sdi;
		
		if (super.init(smi, sdi))
		{
            //data.rowComparator = new RowComparator();
            
		    // Add init code here.
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