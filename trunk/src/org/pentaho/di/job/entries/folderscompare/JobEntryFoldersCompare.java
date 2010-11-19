 /**********************************************************************
 **                                                                   **
 **               This code belongs to the KETTLE project.            **
 **                                                                   **
 ** Kettle, from version 2.2 on, is released into the public domain   **
 ** under the Lesser GNU Public License (LGPL).                       **
 **                                                                   **
 ** For more details, please read the document LICENSE.txt, included  **
 ** in this project                                                   **
 **                                                                   **
 ** http://www.kettle.be                                              **
 ** info@kettle.be                                                    **
 **                                                                   **
 **********************************************************************/

package org.pentaho.di.job.entries.folderscompare;

import static org.pentaho.di.job.entry.validator.AbstractFileValidator.putVariableSpace;
import static org.pentaho.di.job.entry.validator.AndValidator.putValidators;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.andValidator;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.fileExistsValidator;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.notNullValidator;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Iterator;

import java.util.HashMap;
import java.util.Set;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSelectInfo;
import org.apache.commons.vfs.FileSelector;
import org.apache.commons.vfs.FileType;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobEntryType;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.job.entry.validator.ValidatorContext;
import org.pentaho.di.repository.Repository;
import org.w3c.dom.Node;


/**
 * This defines a 'folder compare' job entry. It will compare 2 folders,
 * and will either follow the true flow upon the files being the same or the false
 * flow otherwise.
 *
 * @author Samatar Hassan
 * @since 25-11-2007
 *
 */
public class JobEntryFoldersCompare extends JobEntryBase implements Cloneable, JobEntryInterface
{
	private String filename1;
	private String filename2;
	private String wildcard;
	private String compareonly;
	private boolean includesubfolders;
	private boolean comparefilecontent;
	private boolean comparefilesize;

	public JobEntryFoldersCompare(String n)
	{
		
		super(n, ""); //$NON-NLS-1$
		includesubfolders=false;
		comparefilesize=false;
		comparefilecontent=false;
		compareonly="all";
		wildcard=null;
     	filename1=null;
     	filename2=null;
		setID(-1L);
		setJobEntryType(JobEntryType.FOLDERS_COMPARE);

	}
	public void setCompareOnly(String comparevalue)
	{
		this.compareonly=comparevalue;
	}
	public String getCompareOnly()
	{
		return compareonly;
	}
	public JobEntryFoldersCompare()
	{
		this("");
	}

	public JobEntryFoldersCompare(JobEntryBase jeb)
	{
		super(jeb);
	}

    public Object clone()
    {
        JobEntryFoldersCompare je = (JobEntryFoldersCompare)super.clone();
        return je;
    }

	public String getXML()
	{
        StringBuffer retval = new StringBuffer(50);

		retval.append(super.getXML());
		retval.append("      ").append(XMLHandler.addTagValue("include_subfolders", includesubfolders));
		retval.append("      ").append(XMLHandler.addTagValue("compare_filecontent", comparefilecontent));
		retval.append("      ").append(XMLHandler.addTagValue("compare_filesize", comparefilesize));
		
		
		retval.append("      ").append(XMLHandler.addTagValue("compareonly", compareonly));
		retval.append("      ").append(XMLHandler.addTagValue("wildcard", wildcard));
		retval.append("      ").append(XMLHandler.addTagValue("filename1", filename1));
		retval.append("      ").append(XMLHandler.addTagValue("filename2", filename2));

		return retval.toString();
	}

	public void loadXML(Node entrynode, List<DatabaseMeta> databases, List<SlaveServer> slaveServers, Repository rep)
	throws KettleXMLException
{
	try
	{
		super.loadXML(entrynode, databases, slaveServers);
			includesubfolders = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "include_subfolders")); 
			comparefilecontent = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "compare_filecontent")); 
			comparefilesize = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "compare_filesize"));
			
			
			compareonly = XMLHandler.getTagValue(entrynode, "compareonly");
			wildcard = XMLHandler.getTagValue(entrynode, "wildcard");
			filename1 = XMLHandler.getTagValue(entrynode, "filename1");
			filename2 = XMLHandler.getTagValue(entrynode, "filename2");
		}
		catch(KettleXMLException xe)
		{
			throw new KettleXMLException(Messages.getString("JobFoldersCompare.Meta.UnableLoadXML",xe.getMessage()));
		}
	}

	public void loadRep(Repository rep, long id_jobentry, List<DatabaseMeta> databases, List<SlaveServer> slaveServers) throws KettleException
	{
		try
		{
			super.loadRep(rep, id_jobentry, databases, slaveServers);
			includesubfolders = rep.getJobEntryAttributeBoolean(id_jobentry, "include_subfolders");
			comparefilecontent = rep.getJobEntryAttributeBoolean(id_jobentry, "compare_filecontent");
			comparefilesize = rep.getJobEntryAttributeBoolean(id_jobentry, "compare_filesize");
			
			compareonly = rep.getJobEntryAttributeString(id_jobentry, "compareonly");
			wildcard = rep.getJobEntryAttributeString(id_jobentry, "wildcard");
			filename1 = rep.getJobEntryAttributeString(id_jobentry, "filename1");
			filename2 = rep.getJobEntryAttributeString(id_jobentry, "filename2");
		}
		catch(KettleException dbe)
		{
			throw new KettleException(Messages.getString("JobFoldersCompare.Meta.UnableLoadRep",""+id_jobentry, dbe.getMessage()));
		}
	}

	public void saveRep(Repository rep, long id_job)
		throws KettleException
	{
		try
		{
			super.saveRep(rep, id_job);
			rep.saveJobEntryAttribute(id_job, getID(), "include_subfolders", includesubfolders);
			rep.saveJobEntryAttribute(id_job, getID(), "compare_filecontent", comparefilecontent);
			rep.saveJobEntryAttribute(id_job, getID(), "compare_filesize", comparefilesize);
			
			
			rep.saveJobEntryAttribute(id_job, getID(), "compareonly", compareonly);
			rep.saveJobEntryAttribute(id_job, getID(), "wildcard", wildcard);
			rep.saveJobEntryAttribute(id_job, getID(), "filename1", filename1);
			rep.saveJobEntryAttribute(id_job, getID(), "filename2", filename2);
		}
		catch(KettleDatabaseException dbe)
		{
			throw new KettleException(Messages.getString("JobFoldersCompare.Meta.UnableSaveRep",""+id_job, dbe.getMessage()));
		}
	}
	public void setIncludeSubfolders(boolean includeSubfolders) 
	{
		this.includesubfolders = includeSubfolders;
	}
	 public boolean isIncludeSubfolders()
	{
	  return includesubfolders;
	}
	public void setCompareFileContent(boolean comparefilecontent) 
	{
		this.comparefilecontent = comparefilecontent;
	}
	 public boolean isCompareFileContent()
	{
	   return comparefilecontent;
	}
	 
	public void setCompareFileSize(boolean comparefilesize) 
	{
		this.comparefilesize = comparefilesize;
	}
	 public boolean isCompareFileSize()
	{
	   return comparefilesize;
	}
	 
	 
	 
	 
    public String getRealWildcard()
    {
        return environmentSubstitute(getWildcard());
    }
    public String getRealFilename1()
    {
        return environmentSubstitute(getFilename1());
    }

    public String getRealFilename2()
    {
        return environmentSubstitute(getFilename2());
    }

    /**
     * Check whether 2 files have the same contents.
     *
     * @param file1 first file to compare
     * @param file2 second file to compare
     * @return true if files are equal, false if they are not
     *
     * @throws IOException upon IO problems
     */
    protected boolean equalFileContents(FileObject file1, FileObject file2)
        throws IOException
    {
   	    // Really read the contents and do comparisons
    		
        DataInputStream in1 = new DataInputStream(new BufferedInputStream(
            		                                       KettleVFS.getInputStream(KettleVFS.getFilename(file1))));
        DataInputStream in2 = new DataInputStream(new BufferedInputStream(
            		                                       KettleVFS.getInputStream(KettleVFS.getFilename(file2))));


        
        char ch1, ch2;
        while ( in1.available() != 0 && in2.available() != 0 )
        {
          	ch1 = (char)in1.readByte();
       		ch2 = (char)in2.readByte();
       		if ( ch1 != ch2 )
       			return false;
        }
        if ( in1.available() != in2.available() )
        {
          	return false;
        }
        else
        {
          	return true;
        }
   	}

	public Result execute(Result previousResult, int nr, Repository rep, Job parentJob)
	{
		LogWriter log = LogWriter.getInstance();
		Result result = previousResult;
		result.setResult( false );
		boolean ok=true;

		String realFilename1 = getRealFilename1();
		String realFilename2 = getRealFilename2();

		FileObject folder1 = null;
		FileObject folder2 = null;
		FileObject filefolder1 = null;
		FileObject filefolder2 = null;
		
		try 
		{       
			if (filename1!=null && filename2!=null)
			{
				// Get Folders/Files to compare
				folder1 = KettleVFS.getFileObject(realFilename1);
				folder2 = KettleVFS.getFileObject(realFilename2);

				if (folder1.exists() && folder2.exists() )
				{	
					if(!folder1.getType().equals(folder2.getType()))
					{
						// pb...we try to compare file with folder !!!
						log.logError(toString(),Messages.getString("JobFoldersCompare.Log.CanNotCompareFilesFolders"));
						
						if(folder1.getType()==FileType.FILE)
							log.logError(toString(),Messages.getString("JobFoldersCompare.Log.IsAFile",realFilename1));
						else if(folder1.getType()==FileType.FOLDER)
							log.logError(toString(),Messages.getString("JobFoldersCompare.Log.IsAFolder",realFilename1));
						else 
							log.logError(toString(),Messages.getString("JobFoldersCompare.Log.IsUnknownFileType",realFilename1));
						
						if(folder2.getType()==FileType.FILE)
							log.logError(toString(),Messages.getString("JobFoldersCompare.Log.IsAFile",realFilename2));
						else if(folder2.getType()==FileType.FOLDER)
							log.logError(toString(),Messages.getString("JobFoldersCompare.Log.IsAFolder",realFilename2));
						else 
							log.logError(toString(),Messages.getString("JobFoldersCompare.Log.IsUnknownFileType",realFilename2));
						
					}
					else
					{
						if(folder1.getType()==FileType.FILE)
						{
							// simply compare 2 files ..
							if ( equalFileContents(folder1, folder2) )
								result.setResult( true );
							else
								result.setResult( false );
						}
						else if(folder1.getType()==FileType.FOLDER)
						{
							// We compare 2 folders ...

							FileObject list1[] = folder1.findFiles(new TextFileSelector (folder1.toString()));
							FileObject list2[] = folder2.findFiles(new TextFileSelector (folder2.toString()));
							
							int lenList1=list1.length;
							int lenList2=list2.length;
							
							if(log.isDetailed()) 
							{
								log.logDetailed(toString(),Messages.getString("JobFoldersCompare.Log.FolderContains",realFilename1, ""+lenList1 ));
								log.logDetailed(toString(),Messages.getString("JobFoldersCompare.Log.FolderContains",realFilename2, ""+lenList2 ));
							}
							if(lenList1==lenList2)
							{
						
								HashMap<String, String> collection1 = new HashMap<String, String>();
								HashMap<String, String> collection2 = new HashMap<String, String>();
								
								
								for ( int i=0; i < list1.length; i++ ) 
								{
									// Put files list1 in TreeMap collection1
									collection1.put(list1[i].getName().getBaseName(),list1[i].toString());
						        }
						
								
								for ( int i=0; i < list2.length; i++ ) 
								{
									// Put files list2 in TreeMap collection2
									collection2.put(list2[i].getName().getBaseName(),list2[i].toString());
						        }
								
								// Let's now fetch Folder1
								// and for each entry, we will search it in Folder2
								// if the entry exists..we will compare file entry (file or folder?)
								// if the 2 entry are file (not folder), we will compare content
								Set<Map.Entry<String,String>> entrees = collection1.entrySet();
								Iterator<Map.Entry<String,String>> iterateur = entrees.iterator();
						
								while(iterateur.hasNext())
								{
								   Map.Entry<String,String> entree = iterateur.next();
								   if(!collection2.containsKey(entree.getKey() ))
								   {
									   ok=false;
									   if(log.isDetailed())
										   log.logDetailed(toString(), Messages.getString("JobFoldersCompare.Log.FileCanNotBeFoundIn",entree.getKey().toString(),realFilename2));
								   }
								   else
								   {
									   if(log.isDebug()) log.logDebug(toString(),Messages.getString("JobFoldersCompare.Log.FileIsFoundIn",entree.getKey().toString(),realFilename2));
									   
									   filefolder1= KettleVFS.getFileObject(entree.getValue().toString());
									   filefolder2= KettleVFS.getFileObject(collection2.get(entree.getKey()).toString());
									   
									   if(!filefolder2.getType().equals(filefolder1.getType()))
									   {
										   // The file1 exist in the folder2..but they don't have the same type
										   ok=false;
										   if(log.isDetailed())
											   log.logDetailed(toString(),Messages.getString("JobFoldersCompare.Log.FilesNotSameType", filefolder1.toString(),filefolder2.toString()));
									
										   if(filefolder1.getType()==FileType.FILE)
												log.logError(toString(),Messages.getString("JobFoldersCompare.Log.IsAFile",filefolder1.toString()));
											else if(filefolder1.getType()==FileType.FOLDER)
												log.logError(toString(),Messages.getString("JobFoldersCompare.Log.IsAFolder",filefolder1.toString()));
											else 
												log.logError(toString(),Messages.getString("JobFoldersCompare.Log.IsUnknownFileType",filefolder1.toString()));
										   
										   if(filefolder2.getType()==FileType.FILE)
												log.logError(toString(),Messages.getString("JobFoldersCompare.Log.IsAFile",filefolder2.toString()));
											else if(filefolder2.getType()==FileType.FOLDER)
												log.logError(toString(),Messages.getString("JobFoldersCompare.Log.IsAFolder",filefolder2.toString()));
											else 
												log.logError(toString(),Messages.getString("JobFoldersCompare.Log.IsUnknownFileType",filefolder2.toString()));
											
										   
									   }
									   else
									   {
										   // Files are the same type ...
										   if(filefolder2.getType()== FileType.FILE)
										   {
											   // Let's compare file size
											   if(comparefilesize)
											   {
												   long filefolder1_size=filefolder1.getContent().getSize();
												   long filefolder2_size=filefolder2.getContent().getSize();
												   if(filefolder1_size!=filefolder2_size)
												   {
													   ok=false;
													   if(log.isDetailed())
													   {
														   log.logDetailed(toString(),Messages.getString("JobFoldersCompare.Log.FilesNotSameSize",filefolder1.toString(),filefolder2.toString()));
														   log.logDetailed(toString(),Messages.getString("JobFoldersCompare.Log.SizeFileIs",filefolder1.toString(),""+filefolder1_size));
														   log.logDetailed(toString(),Messages.getString("JobFoldersCompare.Log.SizeFileIs",filefolder2.toString(),""+filefolder2_size));
													   }
													 }
											   }
											   
											   if(ok)
											   {
												   // Let's compare files content..
												   if(comparefilecontent)
												   {
													   if (!equalFileContents(filefolder1, filefolder2) )
														{
														   ok=false;
														   if(log.isDetailed())
															   log.logDetailed(toString(),Messages.getString("JobFoldersCompare.Log.FilesNotSameContent",filefolder1.toString(),filefolder2.toString()));
														}
												   }
											   }
										   }
									   }
									 
								   }
								   //log.logBasic(toString(),entree.getKey() + " - " + entree.getValue());
								}
								
							
								result.setResult(ok);
							}
							else
							{
								// The 2 folders don't have the same files number
								if(log.isDetailed())
									log.logDetailed(toString(), Messages.getString("JobFoldersCompare.Log.FoldersDifferentFiles",realFilename1.toString(),realFilename2.toString()));
							}
							
						}
						else
						{
							// File type unknown !!
						}
					}
					
					
				
				}
				else
				{
					if ( ! folder1.exists() )
						log.logError(toString(), Messages.getString("JobFileCompare.Log.FileNotExist",realFilename1));
					if ( ! folder2.exists() )
						log.logError(toString(), Messages.getString("JobFileCompare.Log.FileNotExist",realFilename2));
					result.setResult( false );
					result.setNrErrors(1);
				}
			}
			else
			{
				log.logError(toString(), Messages.getString("JobFoldersCompare.Log.Need2Files"));
			}
		}
		catch ( Exception e )
		{
			result.setResult( false );
			result.setNrErrors(1);
			log.logError(toString(), Messages.getString("JobFoldersCompare.Log.ErrorComparing",realFilename2, realFilename2,e.getMessage()));
		}	
		finally
		{
			try 
			{
			    if ( folder1 != null )  	folder1.close();
			    if ( folder2 != null )   	folder2.close();		
			    if ( filefolder1 != null )  filefolder1.close();
			    if ( filefolder2 != null )  filefolder2.close();	
		    }
			catch ( IOException e ) { }			
		}
		

		return result;
	}
	private class TextFileSelector implements FileSelector 
	{
		LogWriter log = LogWriter.getInstance();
		String source_folder=null;
		public TextFileSelector(String sourcefolderin) 
		{
			 if (!Const.isEmpty(sourcefolderin)) source_folder=sourcefolderin;
		 
		 }
		 
		public boolean includeFile(FileSelectInfo info) 
		{
			boolean returncode=false;
			try
			{
				if (!info.getFile().toString().equals(source_folder))
				{
					// Pass over the Base folder itself
					String short_filename= info.getFile().getName().getBaseName();
					
					if (info.getFile().getParent().equals(info.getBaseFolder()))
					 {
						// In the Base Folder... 
						if((info.getFile().getType() == FileType.FILE && compareonly.equals("only_files")) ||
								(info.getFile().getType() == FileType.FOLDER && compareonly.equals("only_folders"))  ||	
								(GetFileWildcard(short_filename) && compareonly.equals("specify") ) ||
								(compareonly.equals("all")) )	
						
							returncode=true;
					 }
					else
					{
						// Not in the Base Folder...Only if include sub folders  
						if(includesubfolders)
						{
							if((info.getFile().getType() == FileType.FILE && compareonly.equals("only_files")) ||
									(info.getFile().getType() == FileType.FOLDER && compareonly.equals("only_folders"))  ||	
									(GetFileWildcard(short_filename) && compareonly.equals("specify") ) ||
									(compareonly.equals("all")) )	
							
								returncode=true;
						}
					}
					
					
				}
			}
			catch (Exception e) 
			{
				

				log.logError(toString(), "Error while finding files ... in [" + info.getFile().toString() + "]. Exception :"+e.getMessage());
				 returncode= false;
			}
			return returncode;
		}

		public boolean traverseDescendents(FileSelectInfo info) 
		{
			return true;
		}
	}
	/**********************************************************
	 * 
	 * @param selectedfile
	 * @param wildcard
	 * @return True if the selectedfile matches the wildcard
	 **********************************************************/
	private boolean GetFileWildcard(String selectedfile)
	{
		Pattern pattern = null;
		boolean getIt=true;
	
        if (!Const.isEmpty(wildcard))
        {
        	 pattern = Pattern.compile(wildcard);
			// First see if the file matches the regular expression!
			if (pattern!=null)
			{
				Matcher matcher = pattern.matcher(selectedfile);
				getIt = matcher.matches();
			}
        }
		
		return getIt;
	}
	
	

	public boolean evaluates()
	{
		return true;
	}


    public void setWildcard(String wildcard)
	{
		this.wildcard = wildcard;
	}
    public String getWildcard()
	{
		return wildcard;
	}
	public void setFilename1(String filename)
	{
		this.filename1 = filename;
	}

	public String getFilename1()
	{
		return filename1;
	}

	public void setFilename2(String filename)
	{
		this.filename2 = filename;
	}

	public String getFilename2()
	{
		return filename2;
	}


		  public void check(List<CheckResultInterface> remarks, JobMeta jobMeta) {
		    ValidatorContext ctx = new ValidatorContext();
		    putVariableSpace(ctx, getVariables());
		    putValidators(ctx, notNullValidator(), fileExistsValidator());
		    andValidator().validate(this, "filename1", remarks, ctx); //$NON-NLS-1$
		    andValidator().validate(this, "filename2", remarks, ctx); //$NON-NLS-1$
		  }
}