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
package org.pentaho.di.trans;

import org.pentaho.di.core.RowSet;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.StepInterface;

/**
 * Allows you to "Inject" rows into a step.
 * 
 * @author Matt
 *
 */
public class RowProducer
{
    private RowSet rowSet;
    private StepInterface stepInterface;
    
    public RowProducer(StepInterface stepInterface, RowSet rowSet)
    {
        this.stepInterface = stepInterface;
        this.rowSet = rowSet;
    }
    
    public void putRow(RowMetaInterface rowMeta, Object[] row)
    {
        rowSet.putRow(rowMeta, row);
    }
    
    public void finished()
    {
        rowSet.setDone();
    }

    /**
     * @return Returns the rowSet.
     */
    public RowSet getRowSet()
    {
        return rowSet;
    }

    /**
     * @param rowSet The rowSet to set.
     */
    public void setRowSet(RowSet rowSet)
    {
        this.rowSet = rowSet;
    }

    /**
     * @return Returns the stepInterface.
     */
    public StepInterface getStepInterface()
    {
        return stepInterface;
    }

    /**
     * @param stepInterface The stepInterface to set.
     */
    public void setStepInterface(StepInterface stepInterface)
    {
        this.stepInterface = stepInterface;
    }
    
    
}
