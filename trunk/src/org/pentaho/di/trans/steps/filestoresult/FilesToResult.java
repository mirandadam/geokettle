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

package org.pentaho.di.trans.steps.filestoresult;

import java.io.IOException;

import org.pentaho.di.core.ResultFile;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

/**
 * Writes filenames to a next job entry in a Job
 * 
 * @author matt
 * @since 26-may-2006
 */
public class FilesToResult extends BaseStep implements StepInterface
{
	private FilesToResultMeta meta;

	private FilesToResultData data;

	public FilesToResult(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr,
			TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}

	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		meta = (FilesToResultMeta) smi;
		data = (FilesToResultData) sdi;

		Object[] r = getRow(); // get row, set busy!
		if (r == null) // no more input to be expected...
		{
			for (ResultFile resultFile : data.filenames)
			{
				addResultFile( resultFile );
			}
			logBasic(Messages.getString("FilesToResult.Log.AddedNrOfFiles", String.valueOf(data.filenames
					.size())));
			setOutputDone();
			return false;
		}

		if (first)
		{
			first = false;

			data.filenameIndex = getInputRowMeta().indexOfValue(meta.getFilenameField());

			if (data.filenameIndex < 0)
			{
				logError(Messages.getString("FilesToResult.Log.CouldNotFindField", meta.getFilenameField())); //$NON-NLS-1$ //$NON-NLS-2$
				setErrors(1);
				stopAll();
				return false;
			}
		}

		// OK, get the filename field from the row
		String filename = (String)r[data.filenameIndex];

		try
		{
			ResultFile resultFile = new ResultFile(meta.getFileType(), KettleVFS.getFileObject(filename),
					getTrans().getName(), getStepname());

			// Add all rows to rows buffer...
			data.filenames.add(resultFile);
		} 
		catch (IOException e)
		{
			throw new KettleException(e);
		}

		// Copy to any possible next steps...
		data.outputRowMeta = getInputRowMeta().clone();
		meta.getFields(data.outputRowMeta, getStepname(), null, null, this);
		putRow(data.outputRowMeta, r); // copy row to possible alternate
		// rowset(s).

		if (checkFeedback(getLinesRead())) {
			logBasic(Messages.getString("FilesToResult.Log.LineNumber") + getLinesRead()); //$NON-NLS-1$
		}

		return true;
	}

	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta = (FilesToResultMeta) smi;
		data = (FilesToResultData) sdi;

		if (super.init(smi, sdi))
		{
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