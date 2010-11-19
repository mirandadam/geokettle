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

import java.util.List;
import java.util.Map;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.ResultFile;
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




/**
 * 
 * @author matt
 * @since 26-may-2006
 *
 */

public class FilesToResultMeta extends BaseStepMeta implements StepMetaInterface
{
	private String filenameField;
	
	private int fileType;
	
	/**
	 * @return Returns the fieldname that contains the filename.
	 */
	public String getFilenameField()
	{
		return filenameField;
	}

	/**
	 * @param filenameField set the fieldname that contains the filename.
	 */
	public void setFilenameField(String filenameField)
	{
		this.filenameField = filenameField;
	}

	/**
	 * @return Returns the fileType.
	 * @see ResultFile
	 */
	public int getFileType()
	{
		return fileType;
	}

	/**
	 * @param fileType The fileType to set.
	 * @see ResultFile
	 */
	public void setFileType(int fileType)
	{
		this.fileType = fileType;
	}

	
	
	public FilesToResultMeta()
	{
		super(); // allocate BaseStepMeta
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
	
	public String getXML()
	{
		StringBuffer xml = new StringBuffer();
		
		xml.append(XMLHandler.addTagValue("filename_field", filenameField));  // $NON-NLS-1
		xml.append(XMLHandler.addTagValue("file_type", ResultFile.getTypeCode(fileType))); //$NON-NLS-1$
		
		return xml.toString();
	}
	
	private void readData(Node stepnode)
	{
		filenameField = XMLHandler.getTagValue(stepnode, "filename_field"); //$NON-NLS-1$
		fileType = ResultFile.getType( XMLHandler.getTagValue(stepnode, "file_type") ); //$NON-NLS-1$
	}

	public void setDefault()
	{
		filenameField=null;
		fileType = ResultFile.FILE_TYPE_GENERAL;
	}

	public void readRep(Repository rep, long id_step, List<DatabaseMeta> databases, Map<String, Counter> counters)
		throws KettleException
	{
		filenameField = rep.getStepAttributeString(id_step, "filename_field"); //$NON-NLS-1$
		fileType = ResultFile.getType( rep.getStepAttributeString(id_step, "file_type") ); //$NON-NLS-1$
	}

	public void saveRep(Repository rep, long id_transformation, long id_step)
		throws KettleException
	{
		rep.saveStepAttribute(id_transformation, id_step, "filename_field", filenameField); //$NON-NLS-1$
		rep.saveStepAttribute(id_transformation, id_step, "file_type", ResultFile.getTypeCode(fileType)); //$NON-NLS-1$
	}

	public void getFields(RowMetaInterface rowMeta, String origin, RowMetaInterface[] info, StepMeta nextStep, VariableSpace space) throws KettleStepException
	{
		// Default: nothing changes to rowMeta
	}

	public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepinfo, RowMetaInterface prev, String input[], String output[], RowMetaInterface info)
	{
		// See if we have input streams leading to this step!
		if (input.length>0)
		{
			CheckResult cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, Messages.getString("FilesToResultMeta.CheckResult.StepReceivingInfoFromOtherSteps"), stepinfo); //$NON-NLS-1$
			remarks.add(cr);
		}
		else
		{
			CheckResult cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, Messages.getString("FilesToResultMeta.CheckResult.NoInputReceivedError"), stepinfo); //$NON-NLS-1$
			remarks.add(cr);
		}
	}

	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta tr, Trans trans)
	{
		return new FilesToResult(stepMeta, stepDataInterface, cnr, tr, trans);
	}

	public StepDataInterface getStepData()
	{
		return new FilesToResultData();
	}


}
