
/*
 * Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Samatar HASSAN.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
 */
package org.pentaho.di.trans.steps.replacestring;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

/**
 * Search and replace in string.
 * 
 * @author Samatar Hassan
 * @since 28 September 2008
 */
public class ReplaceString extends BaseStep implements StepInterface {
    private ReplaceStringMeta meta;

    private ReplaceStringData data;

    public ReplaceString(StepMeta stepMeta, StepDataInterface stepDataInterface,
            int copyNr, TransMeta transMeta, Trans trans) {
        super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
    }
    
    public static String replaceString(String originalString, Pattern pattern, String replaceByString) 
    {
        if (originalString == null) {
            return null;
        }

        final Matcher matcher = pattern.matcher(originalString);

        if (replaceByString == null) {
            if (matcher.matches()) {
                return null;
            }
            else {
                return originalString;
            }
        }
        else {
            return matcher.replaceAll(replaceByString);
        }
    }


    private Pattern buildPattern(boolean literalParsing, boolean caseSensitive, boolean wholeWord, String patternString) {
        int flags = 0;
        if (literalParsing) flags |= Pattern.LITERAL;
        if (!caseSensitive) flags |= Pattern.CASE_INSENSITIVE;

        /* XXX: I don't like this parameter.
         * I think it would almost always be better for the user
         * to define either word boundaries or ^/$ anchors explicitly in their pattern.
         */   
        if (wholeWord) {
            patternString = "\\b"+patternString+"\\b";
        }

        return Pattern.compile(patternString, flags);
    }

    private synchronized Object[] getOneRow(RowMetaInterface rowMeta,Object[] row) throws KettleException {

    	Object[] rowData = RowDataUtil.resizeArray(row, data.outputRowMeta.size());
        
        int length = meta.getFieldInStream().length;

        for (int i = 0; i < length; i++) {
            String value = replaceString(
                    getInputRowMeta().getString(row, data.inStreamNrs[i]),	
                    data.patterns[i],
                    data.replaceByString[i]);

            if(Const.isEmpty(data.outStreamNrs[i])) 
                rowData[data.inStreamNrs[i]] = value;
            else
                rowData[data.inputFieldsNr+i] = value;	
        }
        return rowData;
    }

    public boolean processRow(StepMetaInterface smi, StepDataInterface sdi)
    throws KettleException {
        meta = (ReplaceStringMeta) smi;
        data = (ReplaceStringData) sdi;

        Object[] r = getRow(); // Get row from input rowset & set row busy!
        if (r == null) // no more input to be expected...
        {
            setOutputDone();
            return false;
        }

        if (first) {
            first = false;

            // What's the format of the output row?
            data.outputRowMeta = getInputRowMeta().clone();
            data.inputFieldsNr=data.outputRowMeta.size();
            meta.getFields(data.outputRowMeta, getStepname(), null, null, this);

            final int numFields = meta.getFieldInStream().length;
            data.inStreamNrs = new int[numFields];
            data.outStreamNrs = new String[numFields];
            data.patterns = new Pattern[numFields];
            data.replaceByString = new String[numFields];
            
            for (int i = 0; i < numFields; i++) {
                data.inStreamNrs[i] = getInputRowMeta().indexOfValue(meta.getFieldInStream()[i]);
                if (data.inStreamNrs[i] < 0) // couldn't find field!
                    throw new KettleStepException(Messages.getString("ReplaceString.Exception.FieldRequired", meta.getFieldInStream()[i])); //$NON-NLS-1$ //$NON-NLS-2$

                // check field type
                if(getInputRowMeta().getValueMeta(data.inStreamNrs[i]).getType()!=ValueMeta.TYPE_STRING)
                    throw new KettleStepException(Messages.getString("ReplaceString.Exception.FieldTypeNotString", meta.getFieldInStream()[i]));

                data.outStreamNrs[i] = environmentSubstitute(meta.getFieldOutStream()[i]);

                data.patterns[i] = buildPattern(
                        meta.getUseRegEx()[i] != ReplaceStringMeta.USE_REGEX_YES,
                        meta.getCaseSensitive()[i] == ReplaceStringMeta.CASE_SENSITIVE_YES,
                        meta.getWholeWord()[i] == ReplaceStringMeta.WHOLE_WORD_YES,
                        meta.getReplaceString()[i]);

                data.replaceByString[i] = meta.getReplaceByString()[i];
            }
        } // end if first


        try 
        {
            Object[] output = getOneRow(getInputRowMeta(),r);
            putRow(data.outputRowMeta, output);

            if (checkFeedback(getLinesRead()))
            {
                if(log.isDetailed())
                    logDetailed(Messages.getString("ReplaceString.Log.LineNumber") + getLinesRead()); //$NON-NLS-1$
            }
        } catch (KettleException e) 
        {
            boolean sendToErrorRow=false;
            String errorMessage = null;

            if (getStepMeta().isDoingErrorHandling())
            {
                sendToErrorRow = true;
                errorMessage = e.toString();
            }
            else
            {
                logError(Messages.getString("ReplaceString.Log.ErrorInStep",e.getMessage())); //$NON-NLS-1$
                setErrors(1);
                stopAll();
                setOutputDone();  // signal end to receiver(s)
                return false;
            }
            if (sendToErrorRow){
                // Simply add this row to the error row
                putError(getInputRowMeta(), r, 1, errorMessage, null, "ReplaceString001");
            }
        }
        return true;
    }

    public boolean init(StepMetaInterface smi, StepDataInterface sdi) {

        meta = (ReplaceStringMeta) smi;
        data = (ReplaceStringData) sdi;

        if (super.init(smi, sdi)) {

            return true;
        }
        return false;
    }

    public void dispose(StepMetaInterface smi, StepDataInterface sdi) {
        meta = (ReplaceStringMeta) smi;
        data = (ReplaceStringData) sdi;

        super.dispose(smi, sdi);
    }

    //
    // Run is were the action happens!
    public void run()
    {
        BaseStep.runStepThread(this, meta, data);
    }
}