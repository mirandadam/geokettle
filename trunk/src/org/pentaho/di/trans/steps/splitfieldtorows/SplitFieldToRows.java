package org.pentaho.di.trans.steps.splitfieldtorows;


import org.pentaho.di.core.Const;
import org.pentaho.di.core.SimpleTokenizer;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

 public class SplitFieldToRows extends BaseStep implements StepInterface
{
	private SplitFieldToRowsMeta meta;
	private SplitFieldToRowsData data;
	
	public SplitFieldToRows(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
	
	private boolean splitField(RowMetaInterface rowMeta, Object[] rowData) throws KettleException
	{
		if (first)
		{
			first = false;
			
			data.outputRowMeta = getInputRowMeta().clone();
			meta.getFields(data.outputRowMeta, getStepname(), null, null, this);
			
			String realSplitFieldName=environmentSubstitute(meta.getSplitField());
			data.fieldnr = rowMeta.indexOfValue(realSplitFieldName);
			
			int numErrors = 0;
			if (Const.isEmpty(meta.getNewFieldname()))
			{
				logError(Messages.getString("SplitFieldToRows.Log.NewFieldNameIsNull")); //$NON-NLS-1$
				numErrors++;
			}

			if (data.fieldnr < 0)
			{
				logError(Messages.getString("SplitFieldToRows.Log.CouldNotFindFieldToSplit", realSplitFieldName)); //$NON-NLS-1$
				numErrors++;
			}
			
			if (!rowMeta.getValueMeta(data.fieldnr).isString())
			{
				logError(Messages.getString("SplitFieldToRows.Log.SplitFieldNotValid",realSplitFieldName)); //$NON-NLS-1$
				numErrors++;
			}

			if(meta.includeRowNumber())	
			{
				String realRowNumberField=environmentSubstitute(meta.getRowNumberField());
				if(Const.isEmpty(realRowNumberField))
				{
					log.logError(toString(), Messages.getString("SplitFieldToRows.Exception.RownrFieldMissing"));
					numErrors++;
				}
			}
			
			if (numErrors > 0)
			{
				setErrors(numErrors);
				stopAll();
				return false;
			}
			
			data.splitMeta = rowMeta.getValueMeta(data.fieldnr);
			data.realDelimiter=environmentSubstitute(meta.getDelimiter());
		}
		
		String originalString = data.splitMeta.getString(rowData[data.fieldnr]);
		if (originalString == null) {
			originalString = "";
		}
		
		if(meta.includeRowNumber() && meta.resetRowNumber()) data.rownr=1L;
		
		SimpleTokenizer tokenizer = new SimpleTokenizer(originalString, data.realDelimiter);
		while (tokenizer.hasMoreTokens()) {
			Object[] outputRow = RowDataUtil.createResizedCopy(rowData, data.outputRowMeta.size());
			outputRow[rowMeta.size()] = tokenizer.nextToken();
			// Include row number in output?
			if(meta.includeRowNumber())
			{
				outputRow[rowMeta.size()+1]=data.rownr;
			}
			putRow(data.outputRowMeta, outputRow);
			data.rownr ++;
		}
		
		return true;
	}
	
	public synchronized boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		meta=(SplitFieldToRowsMeta)smi;
		data=(SplitFieldToRowsData)sdi;

		Object[] r = getRow();   // get row from rowset, wait for our turn, indicate busy!
		if (r == null)  // no more input to be expected...
		{
			setOutputDone();
			return false;
		}
		
		boolean ok = splitField(getInputRowMeta(), r);
		if (!ok)
		{
			setOutputDone();
			return false;
		}
					
        if (checkFeedback(getLinesRead())) {
			if(log.isDetailed()) 
			{
				if(log.isDetailed()) logBasic(Messages.getString("SplitFieldToRows.Log.LineNumber")+getLinesRead()); //$NON-NLS-1$
			}
		}
			
		return true;
	}
	
	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta = (SplitFieldToRowsMeta)smi;
		data = (SplitFieldToRowsData)sdi;
		
		if (super.init(smi, sdi))
		{
			data.rownr = 1L;
		    return true;
		}
		return false;
	}

			
	//
	// Run is were the action happens!
	//
	public void run()
	{
		BaseStep.runStepThread(this, meta, data);
	}
}
