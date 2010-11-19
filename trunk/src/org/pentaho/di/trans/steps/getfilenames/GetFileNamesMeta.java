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

/* 
 * 
 * Created on 4-apr-2003
 * 
 */

package org.pentaho.di.trans.steps.getfilenames;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.vfs.FileObject;
import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.fileinput.FileInputList;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.resource.ResourceDefinition;
import org.pentaho.di.resource.ResourceEntry;
import org.pentaho.di.resource.ResourceNamingInterface;
import org.pentaho.di.resource.ResourceReference;
import org.pentaho.di.resource.ResourceEntry.ResourceType;
import org.pentaho.di.resource.ResourceNamingInterface.FileNamingType;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.w3c.dom.Node;



public class GetFileNamesMeta extends BaseStepMeta implements StepMetaInterface
{
	private static final String NO = "N";

	private static final String YES = "Y";

	/** Array of filenames */
	private String             fileName[];

	/** Wildcard or filemask (regular expression) */
	private String             fileMask[];
    
	/** Array of boolean values as string, indicating if a file is required. */
	private String             fileRequired[];
	
	/** Filter indicating file filter */
	private FileInputList.FileTypeFilter fileTypeFilter;
	
	/** The name of the field in the output containing the filename */
	private  String  filenameField;
	
	/** Flag indicating that a row number field should be included in the output */
	private  boolean includeRowNumber;
	
	/** The name of the field in the output containing the row number*/
	private  String  rowNumberField;
	
	private String dynamicFilenameField;
	
	private String dynamicWildcardField;
	
	/** file name from previous fields **/
	private boolean filefield;
	
	private boolean isaddresult;
	
	/** The maximum number or lines to read */
	private  long  rowLimit;
	
	public GetFileNamesMeta()
	{
		super(); // allocate BaseStepMeta
	}
	/**
     * @return Returns the filenameField.
     */
    public String getFilenameField()
    {
        return filenameField;
    } 
    /**
     * @return Returns the rowNumberField.
     */
    public String getRowNumberField()
    {
        return rowNumberField;
    }
    /**
     * @param dynamicFilenameField The dynamic filename field to set.
     */
    public void setDynamicFilenameField(String dynamicFilenameField)
    {
        this.dynamicFilenameField = dynamicFilenameField;
    }
    
    /**
     * @param dynamicWildcardField The dynamic wildcard field to set.
     */
    public void setDynamicWildcardField(String dynamicWildcardField)
    {
        this.dynamicWildcardField = dynamicWildcardField;
    }
    
    /**
     * @param rowNumberField The rowNumberField to set.
     */
    public void setRowNumberField(String rowNumberField)
    {
        this.rowNumberField = rowNumberField;
    }
    
    /**
     * @return Returns the dynamic filename field (from previous steps)
     */
    public String getDynamicFilenameField()
    {
        return dynamicFilenameField;
    }   
    
    /**
     * @return Returns the dynamic wildcard field (from previous steps)
     */
    public String getDynamicWildcardField()
    {
        return dynamicWildcardField;
    }  
    
    /**
     * @return Returns the includeRowNumber.
     */
    public boolean includeRowNumber()
    {
        return includeRowNumber;
    }
    
    /**
     * @return Returns the File field.
     */
    public boolean isFileField()
    {
        return filefield;
    }
    /**
     * @param filefield The filefield to set.
     */
    public void setFileField(boolean filefield)
    {
        this.filefield = filefield;
    }
    


    
    
    /**
     * @param includeRowNumber The includeRowNumber to set.
     */
    public void setIncludeRowNumber(boolean includeRowNumber)
    {
        this.includeRowNumber = includeRowNumber;
    }
    
    /**
     * @param isaddresult The isaddresult to set.
     */
    public void setAddResultFile(boolean isaddresult)
    {
        this.isaddresult = isaddresult;
    }
    
    /**
     *  @return Returns isaddresult.
     */
    public boolean isAddResultFile()
    {
        return isaddresult;
    }
	/**
	 * @return Returns the fileMask.
	 */
	public String[] getFileMask()
	{
		return fileMask;
	}
    
	/**
	 * @return Returns the fileRequired.
	 */
	public String[] getFileRequired() 
	{
		return fileRequired;
	}

	/**
	 * @param fileMask The fileMask to set.
	 */
	public void setFileMask(String[] fileMask)
	{
		this.fileMask = fileMask;
	}
    
	/**
	 * @param fileRequired The fileRequired to set.
	 */
	public void setFileRequired(String[] fileRequired)
	{
		this.fileRequired = fileRequired;
	}

	/**
	 * @return Returns the fileName.
	 */
	public String[] getFileName()
	{
		return fileName;
	}

	/**
	 * @param fileName The fileName to set.
	 */
	public void setFileName(String[] fileName)
	{
		this.fileName = fileName;
	}
    
    /**
     * @return Returns the rowLimit.
     */
    public long getRowLimit()
    {
        return rowLimit;
    }
    
    /**
     * @param rowLimit The rowLimit to set.
     */
    public void setRowLimit(long rowLimit)
    {
        this.rowLimit = rowLimit;
    }
	public void setFilterFileType(int filtertypevalue)
	{
	    this.fileTypeFilter = FileInputList.FileTypeFilter.getByOrdinal(filtertypevalue);
	}
	
	public FileInputList.FileTypeFilter getFileTypeFilter()
	{	
		return fileTypeFilter;
	}
	
	public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleXMLException
	{
		readData(stepnode);
	}

	public Object clone()
	{
		GetFileNamesMeta retval = (GetFileNamesMeta) super.clone();

		int nrfiles = fileName.length;

		retval.allocate(nrfiles);
		
        for (int i = 0; i < nrfiles; i++)
        {
            retval.fileName[i] = fileName[i];
            retval.fileMask[i] = fileMask[i];
            retval.fileRequired[i] = fileRequired[i];
        }

		return retval;
	}

	public void allocate(int nrfiles)
	{
		fileName = new String[nrfiles];
		fileMask = new String[nrfiles];
		fileRequired = new String[nrfiles];
	}

	public void setDefault()
	{
		int nrfiles = 0;
		fileTypeFilter=FileInputList.FileTypeFilter.FILES_AND_FOLDERS;
		isaddresult=true;
		filefield=false;
		includeRowNumber = false;
		rowNumberField   = "";
		dynamicFilenameField ="";
		dynamicWildcardField="";
		
		allocate(nrfiles);

		for (int i = 0; i < nrfiles; i++)
		{
			fileName[i] = "filename" + (i + 1);
			fileMask[i] = "";
			fileRequired[i] = NO;
		}
	}

	public void getFields(RowMetaInterface row, String name, RowMetaInterface[] info, StepMeta nextStep, VariableSpace space) throws KettleStepException
	{
        
		// the filename
		ValueMetaInterface filename = new ValueMeta("filename",ValueMeta.TYPE_STRING);
		filename.setLength(500);
		filename.setPrecision(-1);
		filename.setOrigin(name);
		row.addValueMeta(filename);

		// the short filename
		ValueMetaInterface short_filename = new ValueMeta("short_filename",ValueMeta.TYPE_STRING);
		short_filename.setLength(500);
		short_filename.setPrecision(-1);
		short_filename.setOrigin(name);
		row.addValueMeta(short_filename);

		// the path
		ValueMetaInterface path = new ValueMeta("path",ValueMeta.TYPE_STRING);
		path.setLength(500);
		path.setPrecision(-1);
		path.setOrigin(name);
		row.addValueMeta(path);
        
		// the type     
		ValueMetaInterface type = new ValueMeta("type",ValueMeta.TYPE_STRING);
		type.setLength(500);
		type.setPrecision(-1);
		type.setOrigin(name);
		row.addValueMeta(type);
        
		// the exists     
		ValueMetaInterface exists = new ValueMeta("exists",ValueMeta.TYPE_BOOLEAN);
		exists.setOrigin(name);
		row.addValueMeta(exists);
        
		// the ishidden     
		ValueMetaInterface ishidden = new ValueMeta("ishidden",ValueMeta.TYPE_BOOLEAN);
		ishidden.setOrigin(name);
		row.addValueMeta(ishidden);
              
		// the isreadable     
		ValueMetaInterface isreadable = new ValueMeta("isreadable",ValueMeta.TYPE_BOOLEAN);
		isreadable.setOrigin(name);
		row.addValueMeta(isreadable);
        
		// the iswriteable     
		ValueMetaInterface iswriteable = new ValueMeta("iswriteable",ValueMeta.TYPE_BOOLEAN);
		iswriteable.setOrigin(name);
		row.addValueMeta(iswriteable);  
                
		// the lastmodifiedtime     
		ValueMetaInterface lastmodifiedtime = new ValueMeta("lastmodifiedtime",ValueMeta.TYPE_DATE);
		lastmodifiedtime.setOrigin(name);
		row.addValueMeta(lastmodifiedtime);  
        
		// the size     
		ValueMetaInterface size = new ValueMeta("size", ValueMeta.TYPE_INTEGER);
		size.setOrigin(name);
		row.addValueMeta(size);
        
		// the extension     
		ValueMetaInterface extension = new ValueMeta("extension", ValueMeta.TYPE_STRING);
		extension.setOrigin(name);
		row.addValueMeta(extension); 
               
		// the uri     
		ValueMetaInterface uri = new ValueMeta("uri", ValueMeta.TYPE_STRING);
		uri.setOrigin(name);
		row.addValueMeta(uri); 
        
        
		// the rooturi     
		ValueMetaInterface rooturi = new ValueMeta("rooturi", ValueMeta.TYPE_STRING);
		rooturi.setOrigin(name);
		row.addValueMeta(rooturi); 
		
		if (includeRowNumber)
		{
			ValueMetaInterface v = new ValueMeta(space.environmentSubstitute(rowNumberField), ValueMeta.TYPE_INTEGER);
			v.setLength(ValueMetaInterface.DEFAULT_INTEGER_LENGTH, 0);
			v.setOrigin(name);
			row.addValueMeta(v);
		}
 
	}

	public String getXML()
	{
		StringBuffer retval = new StringBuffer(300);
			
		retval.append("    <filter>").append(Const.CR);
		retval.append("      ").append(XMLHandler.addTagValue("filterfiletype",  fileTypeFilter.toString()));
		retval.append("    </filter>").append(Const.CR);
		
		retval.append("    ").append(XMLHandler.addTagValue("rownum",          includeRowNumber));
	    retval.append("    ").append(XMLHandler.addTagValue("isaddresult",     isaddresult));
	    retval.append("    ").append(XMLHandler.addTagValue("filefield",       filefield));
	    retval.append("    ").append(XMLHandler.addTagValue("rownum_field",    rowNumberField));
        retval.append("    ").append(XMLHandler.addTagValue("filename_Field",  dynamicFilenameField));
        retval.append("    ").append(XMLHandler.addTagValue("wildcard_Field",  dynamicWildcardField));
        
        retval.append("    ").append(XMLHandler.addTagValue("limit", rowLimit));
        
		retval.append("    <file>").append(Const.CR);
		
		for (int i = 0; i < fileName.length; i++)
		{
			retval.append("      ").append(XMLHandler.addTagValue("name", fileName[i]));
			retval.append("      ").append(XMLHandler.addTagValue("filemask", fileMask[i]));
			retval.append("      ").append(XMLHandler.addTagValue("file_required", fileRequired[i]));
		}
		retval.append("    </file>").append(Const.CR);

		return retval.toString();
	}

	private void readData(Node stepnode) throws KettleXMLException
	{
		try
		{
			Node filternode         = XMLHandler.getSubNode(stepnode, "filter");
			Node filterfiletypenode = XMLHandler.getSubNode(filternode, "filterfiletype");
			fileTypeFilter          = FileInputList.FileTypeFilter.getByName(XMLHandler.getNodeValue(filterfiletypenode));
			
			includeRowNumber  = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "rownum"));
			isaddresult  = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "isaddresult"));
			filefield  = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "filefield"));
			rowNumberField    = XMLHandler.getTagValue(stepnode, "rownum_field");
			dynamicFilenameField    = XMLHandler.getTagValue(stepnode, "filename_Field");
			dynamicWildcardField    = XMLHandler.getTagValue(stepnode, "wildcard_Field");
			
			// Is there a limit on the number of rows we process?
			rowLimit = Const.toLong(XMLHandler.getTagValue(stepnode, "limit"), 0L);
			
			Node filenode = XMLHandler.getSubNode(stepnode, "file");
			int nrfiles   = XMLHandler.countNodes(filenode, "name");
				
			allocate(nrfiles);

			for (int i = 0; i < nrfiles; i++)
			{
				Node filenamenode     = XMLHandler.getSubNodeByNr(filenode, "name", i);
				Node filemasknode     = XMLHandler.getSubNodeByNr(filenode, "filemask", i);
				Node fileRequirednode = XMLHandler.getSubNodeByNr(filenode, "file_required", i);
				fileName[i]           = XMLHandler.getNodeValue(filenamenode);
				fileMask[i]           = XMLHandler.getNodeValue(filemasknode);
				fileRequired[i]       = XMLHandler.getNodeValue(fileRequirednode);
			}
		}
		catch (Exception e)
		{
			throw new KettleXMLException("Unable to load step info from XML", e);
		}
	}

	public void readRep(Repository rep, long id_step, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleException
	{
		try
		{
			int nrfiles = rep.countNrStepAttributes(id_step, "file_name");
			fileTypeFilter=FileInputList.FileTypeFilter.getByName(rep.getStepAttributeString(id_step, "filterfiletype"));
			
			dynamicFilenameField  = rep.getStepAttributeString(id_step, "filename_Field");
			dynamicWildcardField  = rep.getStepAttributeString(id_step, "wildcard_Field");
			
			includeRowNumber  = rep.getStepAttributeBoolean(id_step, "rownum");
			isaddresult  = rep.getStepAttributeBoolean(id_step, rep.getStepAttributeString(id_step, "isaddresult"));
			filefield  = rep.getStepAttributeBoolean(id_step, "filefield");
			rowNumberField    = rep.getStepAttributeString (id_step, "rownum_field");
			rowLimit          = rep.getStepAttributeInteger(id_step, "limit");
						
			allocate(nrfiles);

			for (int i = 0; i < nrfiles; i++)
			{
				fileName[i] = rep.getStepAttributeString(id_step, i, "file_name");
				fileMask[i] = rep.getStepAttributeString(id_step, i, "file_mask");
				fileRequired[i] = rep.getStepAttributeString(id_step, i, "file_required");
				if(!YES.equalsIgnoreCase(fileRequired[i])) fileRequired[i] = NO;
			}
		}
		catch (Exception e)
		{
			throw new KettleException("Unexpected error reading step information from the repository", e);
		}
	}

	public void saveRep(Repository rep, long id_transformation, long id_step) throws KettleException
	{
		try
		{			
			rep.saveStepAttribute(id_transformation, id_step, "filterfiletype", fileTypeFilter.toString());
			
			rep.saveStepAttribute(id_transformation, id_step, "rownum",          includeRowNumber);
			rep.saveStepAttribute(id_transformation, id_step, "isaddresult",     isaddresult);
			rep.saveStepAttribute(id_transformation, id_step, "filefield",          filefield);
			rep.saveStepAttribute(id_transformation, id_step, "filename_Field",    dynamicFilenameField);
			rep.saveStepAttribute(id_transformation, id_step, "wildcard_Field",    dynamicWildcardField);
			
			rep.saveStepAttribute(id_transformation, id_step, "rownum_field",    rowNumberField);
			rep.saveStepAttribute(id_transformation, id_step, "limit",           rowLimit);
			
			for (int i = 0; i < fileName.length; i++)
			{
				rep.saveStepAttribute(id_transformation, id_step, i, "file_name", fileName[i]);
				rep.saveStepAttribute(id_transformation, id_step, i, "file_mask", fileMask[i]);
				rep.saveStepAttribute(id_transformation, id_step, i, "file_required", fileRequired[i]);
			}
		}
		catch (Exception e)
		{
			throw new KettleException("Unable to save step information to the repository for id_step=" + id_step, e);
		}
	}

    private boolean[] buildSubdirsFlagsArray()
    {
        boolean[] flags = new boolean[fileName.length];
        for (int i = 0; i < fileName.length; i++) {
            flags[i] = false;
        }
        return flags;
    }
    private FileInputList.FileTypeFilter[] buildFileTypeFiltersArray()
    {
        FileInputList.FileTypeFilter[] filters = new FileInputList.FileTypeFilter[fileName.length];
        for (int i = 0; i < fileName.length; i++) {
            filters[i] = getFileTypeFilter();
        }
        return filters;
    }

	public String[] getFilePaths(VariableSpace space)
	{
		return FileInputList.createFilePathList(space, fileName, fileMask, fileRequired, buildSubdirsFlagsArray(), buildFileTypeFiltersArray());
	}
    
	public FileInputList getFileList(VariableSpace space)
	{
		return FileInputList.createFileList(space, fileName, fileMask, fileRequired, buildSubdirsFlagsArray(), buildFileTypeFiltersArray());
	}
	public FileInputList getDynamicFileList(VariableSpace space, String[] filename, String[] filemask, String[] filerequired)
	{
		return FileInputList.createFileList(space, filename, filemask, filerequired, buildSubdirsFlagsArray(), buildFileTypeFiltersArray());
	}


	public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepinfo, RowMetaInterface prev, String input[], String output[], RowMetaInterface info)
	{
		CheckResult cr;

		// See if we get input...
		if(filefield)
		{
			if (input.length > 0)
				cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, Messages.getString("GetFileNamesMeta.CheckResult.InputOk"), stepinfo);
			else
				cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, Messages.getString("GetFileNamesMeta.CheckResult.InputErrorKo"), stepinfo);
			remarks.add(cr);
			
			if(Const.isEmpty(dynamicFilenameField))
				cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, Messages.getString("GetFileNamesMeta.CheckResult.FolderFieldnameMissing"), stepinfo);
			else
				cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, Messages.getString("GetFileNamesMeta.CheckResult.FolderFieldnameOk"), stepinfo);
			remarks.add(cr);
			
		}else{

			if (input.length > 0)
				cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, Messages.getString("GetFileNamesMeta.CheckResult.NoInputError"), stepinfo);
			else
				cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, Messages.getString("GetFileNamesMeta.CheckResult.NoInputOk"), stepinfo);
					
			remarks.add(cr);
			
			// check specified file names
			FileInputList fileList = getFileList(transMeta);
			if (fileList.nrOfFiles() == 0)
				cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, Messages.getString("GetFileNamesMeta.CheckResult.ExpectedFilesError"), stepinfo);
			else
				cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, Messages.getString("GetFileNamesMeta.CheckResult.ExpectedFilesOk", ""+fileList.nrOfFiles()), stepinfo);	
			remarks.add(cr);
		}
	}

  @Override
  public List<ResourceReference> getResourceDependencies(TransMeta transMeta, StepMeta stepInfo) {
     List<ResourceReference> references = new ArrayList<ResourceReference>(5);
     ResourceReference reference = new ResourceReference(stepInfo);
     references.add(reference);

     String[] files = getFilePaths(transMeta);
     if ( files!=null ) {
       for (int i=0; i<files.length; i++) {
         reference.getEntries().add( new ResourceEntry(files[i], ResourceType.FILE));
       }
     }
     return references;
  }
  
	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta, Trans trans)
	{
		return new GetFileNames(stepMeta, stepDataInterface, cnr, transMeta, trans);
	}

	public StepDataInterface getStepData()
	{
		return new GetFileNamesData();
	}	
	
	
	/**
	 * Since the exported transformation that runs this will reside in a ZIP file, we can't reference files relatively.
	 * So what this does is turn the name of files into absolute paths OR it simply includes the resource in the ZIP file.
	 * For now, we'll simply turn it into an absolute path and pray that the file is on a shared drive or something like that.

	 * TODO: create options to configure this behavior 
	 */
	public String exportResources(VariableSpace space, Map<String, ResourceDefinition> definitions, ResourceNamingInterface resourceNamingInterface, Repository repository) throws KettleException {
		try {
			// The object that we're modifying here is a copy of the original!
			// So let's change the filename from relative to absolute by grabbing the file object...
			// In case the name of the file comes from previous steps, forget about this!
			// 
			if (!filefield) {
				List<FileObject> newFilenames = new ArrayList<FileObject>();
				
				FileInputList fileList = FileInputList.createFileList(space, fileName, fileMask, fileRequired);
				if (fileList.getFiles().size()>0) {
					for (FileObject fileObject : fileList.getFiles()) {
						// From : ${Internal.Transformation.Filename.Directory}/../foo/bar.csv
						// To   : /home/matt/test/files/foo/bar.csv
						//
						// If the file doesn't exist, forget about this effort too!
						//
						if (fileObject.exists()) {
							// Convert to an absolute path and add it to the list.
							// 
							newFilenames.add(fileObject);
						}
					}
					
					// Still here: set a new list of absolute filenames!
					//
					fileName=new String[newFilenames.size()];
					fileMask=new String[newFilenames.size()];
					fileRequired=new String[newFilenames.size()];
					for (int i=0;i<newFilenames.size();i++) {
						FileObject fileObject = newFilenames.get(i);
						fileName[i] = resourceNamingInterface.nameResource(fileObject.getName().getBaseName(), fileObject.getParent().getName().getPath(), space.toString(), FileNamingType.DATA_FILE);
						fileMask[i] = null;
						fileRequired[i] = "Y";
					}
				}
			}
			
			return null;
		} catch (Exception e) {
			throw new KettleException(e); //$NON-NLS-1$
		}
	}

}