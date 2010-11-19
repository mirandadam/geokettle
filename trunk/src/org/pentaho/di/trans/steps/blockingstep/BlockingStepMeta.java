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
 
package org.pentaho.di.trans.steps.blockingstep;

import java.io.File;
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


public class BlockingStepMeta  extends BaseStepMeta implements StepMetaInterface {
	
    /** Directory to store the temp files */
    private String  directory;

    /** Temp files prefix... */
    private String  prefix;

    /** The cache size: number of rows to keep in memory */
    private int     cacheSize;

    /**
     * Compress files: if set to true, temporary files are compressed, thus reducing I/O at the cost of slightly higher
     * CPU usage
     */
    private boolean compressFiles;

    /**
     * Pass all rows, or only the last one. Only the last row was the original behaviour.
     */
	private boolean passAllRows;

	/**
	 * Cache size: how many rows do we keep in memory
	 */
	public static final int CACHE_SIZE = 5000;	
		   
	public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev, String input[], String output[], RowMetaInterface info)
    {
        CheckResult cr;
        
        if (prev != null && prev.size() > 0)
        {
            // Check the sort directory
            String realDirectory = transMeta.environmentSubstitute(directory);

            File f = new File(realDirectory);
            if (f.exists())
            {
                if (f.isDirectory())
                {
                    cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, Messages.getString("BlockingStepMeta.CheckResult.DirectoryExists", realDirectory),
                            stepMeta);
                    remarks.add(cr);
                }
                else
                {
                    cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, Messages.getString("BlockingStepMeta.CheckResult.ExistsButNoDirectory",
                            realDirectory), stepMeta);
                    remarks.add(cr);
                }
            }
            else
            {
                cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, Messages.getString("BlockingStepMeta.CheckResult.DirectoryNotExists", realDirectory),
                        stepMeta);
                remarks.add(cr);
            }
        }
        else
        {
            cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, Messages.getString("BlockingStepMeta.CheckResult.NoFields"), stepMeta);
            remarks.add(cr);
        }

        // See if we have input streams leading to this step!
        if (input.length>0) 
        {
            cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, Messages.getString("BlockingStepMeta.CheckResult.StepExpectingRowsFromOtherSteps"), stepMeta); //$NON-NLS-1$
            remarks.add(cr);
        } 
        else 
        {
            cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, Messages.getString("BlockingStepMeta.CheckResult.NoInputReceivedError"), stepMeta); //$NON-NLS-1$
            remarks.add(cr);
        }
    }
	
	public void getFields(RowMetaInterface inputRowMeta, String name, RowMetaInterface[] info, StepMeta nextStep, VariableSpace space) throws KettleStepException
	{
		// Default: no values are added to the row in the step
	}

    public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans) {
        return new BlockingStep(stepMeta, stepDataInterface, copyNr, transMeta, trans);
    }

    public StepDataInterface getStepData() {
        return new BlockingStepData();
    }

    public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleXMLException {
        readData(stepnode);        
    }

    private void readData(Node stepnode)
    {
       	passAllRows   = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "pass_all_rows"));
        directory     = XMLHandler.getTagValue(stepnode, "directory");
        prefix        = XMLHandler.getTagValue(stepnode, "prefix");
        cacheSize     = Const.toInt(XMLHandler.getTagValue(stepnode, "cache_size"), CACHE_SIZE);
        compressFiles = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "compress"));
    }    
    
    public void setDefault() {
    	passAllRows   = false;    	
        directory     = "%%java.io.tmpdir%%";
        prefix        = "block";
        cacheSize     = CACHE_SIZE;
        compressFiles = true;    	
    }
    
    public String getXML()
    {
        StringBuilder retval = new StringBuilder(300);

        retval.append("      ").append(XMLHandler.addTagValue("pass_all_rows", passAllRows));
        retval.append("      ").append(XMLHandler.addTagValue("directory",     directory));
        retval.append("      ").append(XMLHandler.addTagValue("prefix",        prefix));
        retval.append("      ").append(XMLHandler.addTagValue("cache_size",    cacheSize));
        retval.append("      ").append(XMLHandler.addTagValue("compress",      compressFiles));

        return retval.toString();
    }    
    
    public void readRep(Repository rep, long id_step, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleException {
        try
        {
        	passAllRows   =       rep.getStepAttributeBoolean(id_step, "pass_all_rows");
            directory     =       rep.getStepAttributeString(id_step,  "directory");
            prefix        =       rep.getStepAttributeString(id_step,  "prefix");
            cacheSize     = (int) rep.getStepAttributeInteger(id_step, "cache_size");
            compressFiles =       rep.getStepAttributeBoolean(id_step, "compress");
            if (cacheSize == 0) cacheSize = CACHE_SIZE;
        }
        catch (Exception e)
        {
            throw new KettleException("Unexpected error reading step information from the repository", e);
        }    	
    }

    public void saveRep(Repository rep, long id_transformation, long id_step) throws KettleException {
        try
        {
        	rep.saveStepAttribute(id_transformation, id_step, "pass_all_rows", passAllRows);
            rep.saveStepAttribute(id_transformation, id_step, "directory",     directory);
            rep.saveStepAttribute(id_transformation, id_step, "prefix",        prefix);
            rep.saveStepAttribute(id_transformation, id_step, "cache_size",    cacheSize);
            rep.saveStepAttribute(id_transformation, id_step, "compress",      compressFiles);
        }
        catch (Exception e)
        {
            throw new KettleException("Unable to save step information to the repository for id_step=" + id_step, e);
        }    	
    }

    /**
     * @return Returns the cacheSize.
     */
    public int getCacheSize()
    {
        return cacheSize;
    }

    /**
     * @param cacheSize The cacheSize to set.
     */
    public void setCacheSize(int cacheSize)
    {
        this.cacheSize = cacheSize;
    }
    
    /**
     * @return Returns the prefix.
     */
    public String getPrefix()
    {
        return prefix;
    }

    /**
     * @param prefix The prefix to set.
     */
    public void setPrefix(String prefix)
    {
        this.prefix = prefix;
    }
    
    /**
     * @return Returns whether temporary files should be compressed
     */
    public boolean getCompress()
    {
        return compressFiles;
    }

    /**
     * @param compressFiles Whether to compress temporary files created during sorting
     */
    public void setCompress(boolean compressFiles)
    {
        this.compressFiles = compressFiles;
    }    
 
    /**
     * @return true when all rows are passed and false when only the last one is passed. 
     */
	public boolean isPassAllRows() 
	{
		return passAllRows;
	}

	/**
	 *  @param passAllRows set to true if all rows should be passed and false if only the last one should be passed
	 */
	public void setPassAllRows(boolean passAllRows) 
	{
		this.passAllRows = passAllRows;
	}

	/** 
	 * @return The directory to store the temporary files in.
	 */
	public String getDirectory() 
	{
		return directory;
	}

	/**
	 * Set the directory to store the temp files in.
	 */
	public void setDirectory(String directory) 
	{
		this.directory = directory;
	}
}