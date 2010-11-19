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
 
/* Modifications to original RegexEval step made by Daniel Einspanjer */

package org.pentaho.di.trans.steps.regexeval;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;


/**
 * Use regular expression to validate a field or capture new fields out of an existing field.
 * 
 * @author deinspanjer
 * @since 27-03-2008
 * @author Matt
 * @since 15-08-2007
 */
public class RegexEval extends BaseStep implements StepInterface
{
	private RegexEvalMeta meta;
	private RegexEvalData data;
	
	public RegexEval(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
			Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}

    public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		meta=(RegexEvalMeta)smi;
		data=(RegexEvalData)sdi;
		
		Object[] row = getRow();
		
		if (row==null)  // no more input to be expected...
		{
			setOutputDone();
			return false;
		}
		
		if (first) // we just got started
		{
			first=false;
			// get the RowMeta
			data.previousRowMeta = getInputRowMeta().clone();
			data.outputRowMeta = getInputRowMeta().clone();
			meta.getFields(data.outputRowMeta, getStepname(), null, null, this);
			
			data.nrExtraFields = meta.isAllowCaptureGroupsFlagSet() ? meta.getFieldName().length : 0;
			
			// Check if a Field (matcher) is given
			if (meta.getMatcher()!=null)
			{
				 // Cache the position of the Field
				if (data.indexOfFieldToEvaluate<0)
				{
					data.indexOfFieldToEvaluate = data.previousRowMeta.indexOfValue(meta.getMatcher());
					data.indexOfMatchResultField = data.outputRowMeta.indexOfValue(meta.getResultFieldName());
					
					if (data.nrExtraFields > 0)
					{
						data.indexOfFirstCaptureGroupField = data.outputRowMeta.indexOfValue(meta.getFieldName()[0]);
					}
					
					if (data.indexOfFieldToEvaluate<0)
		            {                    
		                // The field is unreachable !
						logError(Messages.getString("RegexEval.Log.ErrorFindingField")+ "[" + meta.getMatcher()+"]"); 
						throw new KettleStepException(Messages.getString("RegexEval.Exception.CouldnotFindField",meta.getMatcher())); 
					}		                
		                
					
					// Let's check that Result Field is given
					if (environmentSubstitute(meta.getResultFieldName()) == null )
					{
						//	Result field is missing !
						logError(Messages.getString("RegexEval.Log.ErrorResultFieldMissing")); 
						throw new KettleStepException(Messages.getString("RegexEval.Exception.ErrorResultFieldMissing"));
					}
		                
				}
			}
			else
			{
				// Matcher is missing !
				log.logError("Error",Messages.getString("RegexEval.Log.ErrorMatcherMissing"));
				throw new KettleStepException(Messages.getString("RegexEval.Exception.ErrorMatcherMissing")); 
			}
			
			// Now create objects to do string to data type conversion...
			data.conversionRowMeta = data.outputRowMeta.clone();
			for (ValueMetaInterface valueMeta : data.conversionRowMeta.getValueMetaList()) {
				valueMeta.setType(ValueMetaInterface.TYPE_STRING);
			}

		}
		
		// reserve room
		Object[] outputRow = RowDataUtil.allocateRowData(data.outputRowMeta.size());
				
		for (int i = 0; i < data.previousRowMeta.size(); i++)
		{
			outputRow[i] = row[i];
		}
		boolean sendToErrorRow=false;
		String errorMessage = null;
		try{
			// Get the Field value
			String fieldValue= getInputRowMeta().getString(row,data.indexOfFieldToEvaluate);
			
			if (fieldValue == null)
			{
				outputRow[data.indexOfMatchResultField] = false;
                for (int i = 0; i < data.nrExtraFields; i++)
                {
                    ValueMetaInterface valueMeta = data.outputRowMeta.getValueMeta(data.indexOfFirstCaptureGroupField+i); 
                    ValueMetaInterface conversionValueMeta = data.conversionRowMeta.getValueMeta(data.indexOfFirstCaptureGroupField+i);
                    Object convertedValue = valueMeta.convertDataFromString
                    (
                            null,
                            conversionValueMeta,
                            meta.getFieldNullIf()[i],
                            meta.getFieldIfNull()[i],
                            meta.getFieldTrimType()[i]
                    );
    
                    outputRow[data.indexOfFirstCaptureGroupField+i] = convertedValue;
                }
			}
			else
			{
				// Search engine
				Matcher m = data.pattern.matcher(fieldValue);
	
				// Start search
				boolean isMatch = m.matches();
				outputRow[data.indexOfMatchResultField] = isMatch;
	
				String[] values = new String[data.nrExtraFields];
	
				for (int i = 0; i < data.nrExtraFields; i++)
				{
					if (isMatch)
					{
						if (data.nrExtraFields != m.groupCount())
						{
							// Runtime exception case. The number of capture groups in the regex doesn't match the number of fields.
							log.logError("Error",Messages.getString("RegexEval.Log.ErrorCaptureGroupFieldsMismatch", String.valueOf(m.groupCount()), String.valueOf(data.nrExtraFields)));
							throw new KettleStepException(Messages.getString("RegexEval.Exception.ErrorCaptureGroupFieldsMismatch", String.valueOf(m.groupCount()), String.valueOf(data.nrExtraFields))); 
						}
						values[i] = m.group(i+1);
					}
	
					ValueMetaInterface valueMeta = data.outputRowMeta.getValueMeta(data.indexOfFirstCaptureGroupField+i); 
					ValueMetaInterface conversionValueMeta = data.conversionRowMeta.getValueMeta(data.indexOfFirstCaptureGroupField+i);
					Object convertedValue = valueMeta.convertDataFromString
					(
							values[i],
							conversionValueMeta,
							meta.getFieldNullIf()[i],
							meta.getFieldIfNull()[i],
							meta.getFieldTrimType()[i]
					);
	
					outputRow[data.indexOfFirstCaptureGroupField+i] = convertedValue;
				}
			}
			if (log.isRowLevel()) logRowlevel(Messages.getString("RegexEval.Log.ReadRow") + " " +  getInputRowMeta().getString(row)); 
			
			putRow(data.outputRowMeta, outputRow);  // copy row to output rowset(s);
		}catch(KettleException e)
		{
			if (getStepMeta().isDoingErrorHandling())
	        {
                sendToErrorRow = true;
                errorMessage = e.toString();
	        }else
	        {
				throw new KettleStepException(Messages.getString("RegexEval.Log.ErrorInStep"), e); //$NON-NLS-1$
	        }
			 
			 if (sendToErrorRow)
	         {
				 // Simply add this row to the error row
	             putError(getInputRowMeta(), outputRow, 1, errorMessage, null, "REGEX001");
	         }
		}
		return true;
	}
		
	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(RegexEvalMeta)smi;
		data=(RegexEvalData)sdi;
		
		if (super.init(smi, sdi))
		{
			// Embedded options
			StringBuilder options = new StringBuilder();
			
			if (meta.isCaseInsensitiveFlagSet())
			{
				options.append("(?i)");
			}
			if (meta.isCommentFlagSet())
			{
				options.append("(?x)");
			}
			if (meta.isDotAllFlagSet())
			{
				options.append("(?s)");
			}
			if (meta.isMultilineFlagSet())
			{
				options.append("(?m)");
			}
			if (meta.isUnicodeFlagSet())
			{
				options.append("(?u)");
			}
			if (meta.isUnixLineEndingsFlagSet())
			{
				options.append("(?d)");
			}
		
			// Regular expression
			String regularexpression= meta.getScript();
			if (meta.isUseVariableInterpolationFlagSet())
			{
				regularexpression = environmentSubstitute(meta.getScript());
			}
			if (log.isDetailed()) logDetailed(Messages.getString("RegexEval.Log.Regexp") + " " + options+regularexpression); 
			
			if (meta.isCanonicalEqualityFlagSet())
			{
				data.pattern = Pattern.compile(options+regularexpression,Pattern.CANON_EQ);
			}
			else
			{
				data.pattern = Pattern.compile(options+regularexpression);	
			}
		    return true;
		}
		return false;
	}

	public void run()
	{
    	BaseStep.runStepThread(this, meta, data);
	}
}