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
package org.pentaho.di.trans.steps.fileexists;

import java.util.List;
import java.util.Map;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.shared.SharedObjectInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.w3c.dom.Node;

/*
 * Created on 03-Juin-2008
 * 
 */

public class FileExistsMeta extends BaseStepMeta implements StepMetaInterface
{
	private boolean addresultfilenames;
	
    /** dynamic filename */
    private String       filenamefield;
    
    private String filetypefieldname;

    private boolean  includefiletype;
    
    /** function result: new value name */
    private String       resultfieldname;
    
    public FileExistsMeta()
    {
        super(); // allocate BaseStepMeta
    }

    /**
     * @return Returns the filenamefield.
     */
    public String getDynamicFilenameField()
    {
        return filenamefield;
    }

    /**
     * @param filenamefield The filenamefield to set.
     */
    public void setDynamicFilenameField(String filenamefield)
    {
        this.filenamefield = filenamefield;
    }

    /**
     * @return Returns the resultName.
     */
    public String getResultFieldName()
    {
        return resultfieldname;
    }

    /**
     * @param resultfieldname The resultfieldname to set.
     */
    public void setResultFieldName(String resultfieldname)
    {
        this.resultfieldname = resultfieldname;
    }
    
    /**
     * @param filetypefieldname The filetypefieldname to set.
     */
    public void setFileTypeFieldName(String filetypefieldname)
    {
        this.filetypefieldname = filetypefieldname;
    }

    /**
     * @return Returns the filetypefieldname.
     */
    public String getFileTypeFieldName()
    {
        return filetypefieldname;
    }

    
    public boolean includeFileType()
    {
    	return includefiletype;
    }
    
    public boolean addResultFilenames()
    {
    	return addresultfilenames;
    }
    
    public void setaddResultFilenames(boolean addresultfilenames)
    {
    	this.addresultfilenames=addresultfilenames;
    }
    
    public void setincludeFileType(boolean includefiletype)
    {
    	this.includefiletype=includefiletype;
    }
    
	public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters)
	throws KettleXMLException
	{
		readData(stepnode, databases);
	}
 

    public Object clone()
    {
        FileExistsMeta retval = (FileExistsMeta) super.clone();
       
        return retval;
    }

    public void setDefault()
    {
        resultfieldname = "result"; //$NON-NLS-1$
        filetypefieldname=null;
        includefiletype=false;
        addresultfilenames=false;
    }
	
	public void getFields(RowMetaInterface inputRowMeta, String name, RowMetaInterface info[], StepMeta nextStep, VariableSpace space) throws KettleStepException
	{    	
        // Output fields (String)
		 if (!Const.isEmpty(resultfieldname))
	     {
			 ValueMetaInterface v = new ValueMeta(space.environmentSubstitute(resultfieldname), ValueMeta.TYPE_BOOLEAN);
			 v.setOrigin(name);
			 inputRowMeta.addValueMeta(v);
	     }
		 
		 if (includefiletype &&  !Const.isEmpty(filetypefieldname))
	     {
			 ValueMetaInterface v = new ValueMeta(space.environmentSubstitute(filetypefieldname), ValueMeta.TYPE_STRING);
			 v.setOrigin(name);
			 inputRowMeta.addValueMeta(v);
	     }
    }

    public String getXML()
    {
        StringBuffer retval = new StringBuffer();

        retval.append("    " + XMLHandler.addTagValue("filenamefield", filenamefield)); //$NON-NLS-1$ //$NON-NLS-2$
        retval.append("    " + XMLHandler.addTagValue("resultfieldname", resultfieldname)); //$NON-NLS-1$ //$NON-NLS-2$
        retval.append("    ").append(XMLHandler.addTagValue("includefiletype",       includefiletype));
        retval.append("    " + XMLHandler.addTagValue("filetypefieldname", filetypefieldname));
        retval.append("    ").append(XMLHandler.addTagValue("addresultfilenames",       addresultfilenames));
        return retval.toString();
    }

    private void readData(Node stepnode, List<? extends SharedObjectInterface> databases)
	throws KettleXMLException
	{
	try
	{
            filenamefield = XMLHandler.getTagValue(stepnode, "filenamefield"); //$NON-NLS-1$
            resultfieldname = XMLHandler.getTagValue(stepnode, "resultfieldname");
            includefiletype  = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "includefiletype"));
            filetypefieldname = XMLHandler.getTagValue(stepnode, "filetypefieldname");
            addresultfilenames  = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "addresultfilenames"));   
        }
        catch (Exception e)
        {
            throw new KettleXMLException(Messages.getString("FileExistsMeta.Exception.UnableToReadStepInfo"), e); //$NON-NLS-1$
        }
    }

    public void readRep(Repository rep, long id_step, List<DatabaseMeta> databases, Map<String, Counter> counters)
	throws KettleException
	{
    	try
		{
            filenamefield = rep.getStepAttributeString(id_step, "filenamefield"); //$NON-NLS-1$
            resultfieldname = rep.getStepAttributeString(id_step, "resultfieldname"); //$NON-NLS-1$
            includefiletype  = rep.getStepAttributeBoolean(id_step, "includefiletype");
            filetypefieldname = rep.getStepAttributeString(id_step, "filetypefieldname"); //$NON-NLS-1$
            addresultfilenames  = rep.getStepAttributeBoolean(id_step, "addresultfilenames");   
        }
        catch (Exception e)
        {
            throw new KettleException(Messages.getString("FileExistsMeta.Exception.UnexpectedErrorReadingStepInfo"), e); //$NON-NLS-1$
        }
    }

    public void saveRep(Repository rep, long id_transformation, long id_step) throws KettleException
    {
        try
        {
            rep.saveStepAttribute(id_transformation, id_step, "filenamefield", filenamefield); //$NON-NLS-1$
            rep.saveStepAttribute(id_transformation, id_step, "resultfieldname", resultfieldname); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "includefiletype",          includefiletype);
			rep.saveStepAttribute(id_transformation, id_step, "filetypefieldname", filetypefieldname);
			rep.saveStepAttribute(id_transformation, id_step, "addresultfilenames",          addresultfilenames);
        }
        catch (Exception e)
        {
            throw new KettleException(Messages.getString("FileExistsMeta.Exception.UnableToSaveStepInfo") + id_step, e); //$NON-NLS-1$
        }
    }

	public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev, String input[], String output[], RowMetaInterface info)
	{
        CheckResult cr;
        String error_message = ""; //$NON-NLS-1$

      
        if (Const.isEmpty(resultfieldname))
        {
            error_message = Messages.getString("FileExistsMeta.CheckResult.ResultFieldMissing"); //$NON-NLS-1$
            cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepMeta);
            remarks.add(cr);
        }
        else
        {
            error_message = Messages.getString("FileExistsMeta.CheckResult.ResultFieldOK"); //$NON-NLS-1$
            cr = new CheckResult(CheckResult.TYPE_RESULT_OK, error_message, stepMeta);
            remarks.add(cr);
        }
        if (Const.isEmpty(filenamefield))
        {
            error_message = Messages.getString("FileExistsMeta.CheckResult.FileFieldMissing"); //$NON-NLS-1$
            cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepMeta);
            remarks.add(cr);
        }
        else
        {
            error_message = Messages.getString("FileExistsMeta.CheckResult.FileFieldOK"); //$NON-NLS-1$
            cr = new CheckResult(CheckResult.TYPE_RESULT_OK, error_message, stepMeta);
            remarks.add(cr);
        }
        // See if we have input streams leading to this step!
        if (input.length > 0)
        {
            cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("FileExistsMeta.CheckResult.ReceivingInfoFromOtherSteps"), stepMeta); //$NON-NLS-1$
            remarks.add(cr);
        }
        else
        {
            cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("FileExistsMeta.CheckResult.NoInpuReceived"), stepMeta); //$NON-NLS-1$
            remarks.add(cr);
        }

    }

    public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta, Trans trans)
    {
        return new FileExists(stepMeta, stepDataInterface, cnr, transMeta, trans);
    }

    public StepDataInterface getStepData()
    {
        return new FileExistsData();
    }

    public boolean supportsErrorHandling()
    {
        return true;
    }

}
