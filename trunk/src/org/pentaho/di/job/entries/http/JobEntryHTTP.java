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

package org.pentaho.di.job.entries.http;

import static org.pentaho.di.job.entry.validator.AndValidator.putValidators;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.andValidator;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.integerValidator;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.notBlankValidator;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Authenticator;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.ResultFile;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobEntryType;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.resource.ResourceEntry;
import org.pentaho.di.resource.ResourceReference;
import org.pentaho.di.resource.ResourceEntry.ResourceType;
import org.w3c.dom.Node;

/**
 * This defines an HTTP job entry.
 *
 * @author Matt
 * @since 05-11-2003
 *
 */
public class JobEntryHTTP extends JobEntryBase implements Cloneable, JobEntryInterface
{
  private static final String URL_FIELDNAME = "URL";

  // Base info
  private String url;

  private String targetFilename;

  private boolean fileAppended;

  private boolean dateTimeAdded;

  private String targetFilenameExtention;

  // Send file content to server?
  private String uploadFilename;

  // The fieldname that contains the URL
  // Get it from a previous transformation with Result.
  private String urlFieldname;

  private boolean runForEveryRow;

  // Proxy settings
  private String proxyHostname;

  private String proxyPort;

  private String nonProxyHosts;

  private String username;

  private String password;

  public JobEntryHTTP(String n)
  {
    super(n, "");
    url = null;
    setID(-1L);
    setJobEntryType(JobEntryType.HTTP);
  }

  public JobEntryHTTP()
  {
    this("");
  }

  public JobEntryHTTP(JobEntryBase jeb)
  {
    super(jeb);
  }

  public Object clone()
  {
    JobEntryHTTP je = (JobEntryHTTP) super.clone();
    return je;
  }

  public String getXML()
  {
    StringBuffer retval = new StringBuffer(300);

    retval.append(super.getXML());

    retval.append("      ").append(XMLHandler.addTagValue("url", url));
    retval.append("      ").append(XMLHandler.addTagValue("targetfilename", targetFilename));
    retval.append("      ").append(XMLHandler.addTagValue("file_appended", fileAppended));
    retval.append("      ").append(XMLHandler.addTagValue("date_time_added", dateTimeAdded));
    retval.append("      ").append(XMLHandler.addTagValue("targetfilename_extention", targetFilenameExtention));
    retval.append("      ").append(XMLHandler.addTagValue("uploadfilename", uploadFilename));

    retval.append("      ").append(XMLHandler.addTagValue("url_fieldname", urlFieldname));
    retval.append("      ").append(XMLHandler.addTagValue("run_every_row", runForEveryRow));

    retval.append("      ").append(XMLHandler.addTagValue("username", username));
    retval.append("      ").append(XMLHandler.addTagValue("password", Encr.encryptPasswordIfNotUsingVariables(password)));

    retval.append("      ").append(XMLHandler.addTagValue("proxy_host", proxyHostname));
    retval.append("      ").append(XMLHandler.addTagValue("proxy_port", proxyPort));
    retval.append("      ").append(XMLHandler.addTagValue("non_proxy_hosts", nonProxyHosts));

    return retval.toString();
  }

  public void loadXML(Node entrynode, List<DatabaseMeta> databases, List<SlaveServer> slaveServers, Repository rep) throws KettleXMLException
  {
    try
    {
      super.loadXML(entrynode, databases, slaveServers);
      url = XMLHandler.getTagValue(entrynode, "url");
      targetFilename = XMLHandler.getTagValue(entrynode, "targetfilename");
      fileAppended = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "file_appended"));
      dateTimeAdded = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "date_time_added"));
      targetFilenameExtention = XMLHandler.getTagValue(entrynode, "targetfilename_extention");

      uploadFilename = XMLHandler.getTagValue(entrynode, "uploadfilename");

      urlFieldname = XMLHandler.getTagValue(entrynode, "url_fieldname");
      runForEveryRow = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "run_every_row"));

      username = XMLHandler.getTagValue(entrynode, "username");
      password = Encr.decryptPasswordOptionallyEncrypted( XMLHandler.getTagValue(entrynode, "password"));
      
      proxyHostname = XMLHandler.getTagValue(entrynode, "proxy_host");
      proxyPort = XMLHandler.getTagValue(entrynode, "proxy_port");
      nonProxyHosts = XMLHandler.getTagValue(entrynode, "non_proxy_hosts");
    } catch (KettleXMLException xe)
    {
      throw new KettleXMLException("Unable to load job entry of type 'HTTP' from XML node", xe);
    }
  }

  public void loadRep(Repository rep, long id_jobentry, List<DatabaseMeta> databases, List<SlaveServer> slaveServers) throws KettleException
  {
    try
    {
      super.loadRep(rep, id_jobentry, databases, slaveServers);
      url = rep.getJobEntryAttributeString(id_jobentry, "url");
      targetFilename = rep.getJobEntryAttributeString(id_jobentry, "targetfilename");
      fileAppended = rep.getJobEntryAttributeBoolean(id_jobentry, "file_appended");
      dateTimeAdded = "Y".equalsIgnoreCase(rep.getJobEntryAttributeString(id_jobentry, "date_time_added"));
      targetFilenameExtention = rep.getJobEntryAttributeString(id_jobentry, "targetfilename_extention");

      uploadFilename = rep.getJobEntryAttributeString(id_jobentry, "uploadfilename");

      urlFieldname = rep.getJobEntryAttributeString(id_jobentry, "url_fieldname");
      runForEveryRow = rep.getJobEntryAttributeBoolean(id_jobentry, "run_every_row");

      username = rep.getJobEntryAttributeString(id_jobentry, "username");
      password = Encr.decryptPasswordOptionallyEncrypted( rep.getJobEntryAttributeString(id_jobentry, "password") );

      proxyHostname = rep.getJobEntryAttributeString(id_jobentry, "proxy_host");
      int intPort = (int) rep.getJobEntryAttributeInteger(id_jobentry, "proxy_port");
      proxyPort = rep.getJobEntryAttributeString(id_jobentry, "proxy_port"); // backward compatible.
      if (intPort > 0 && Const.isEmpty(proxyPort))
        proxyPort = Integer.toString(intPort);

      nonProxyHosts = rep.getJobEntryAttributeString(id_jobentry, "non_proxy_hosts");
    } catch (KettleException dbe)
    {
      throw new KettleException("Unable to load job entry of type 'HTTP' from the repository for id_jobentry="
          + id_jobentry, dbe);
    }
  }

  public void saveRep(Repository rep, long id_job) throws KettleException
  {
    try
    {
      super.saveRep(rep, id_job);

      rep.saveJobEntryAttribute(id_job, getID(), "url", url);
      rep.saveJobEntryAttribute(id_job, getID(), "targetfilename", targetFilename);
      rep.saveJobEntryAttribute(id_job, getID(), "file_appended", fileAppended);
      rep.saveJobEntryAttribute(id_job, getID(), "date_time_added", dateTimeAdded);
      rep.saveJobEntryAttribute(id_job, getID(), "targetfilename_extention", targetFilenameExtention);

      rep.saveJobEntryAttribute(id_job, getID(), "uploadfilename", uploadFilename);

      rep.saveJobEntryAttribute(id_job, getID(), "url_fieldname", urlFieldname);
      rep.saveJobEntryAttribute(id_job, getID(), "run_every_row", runForEveryRow);

      rep.saveJobEntryAttribute(id_job, getID(), "username", username);
      rep.saveJobEntryAttribute(id_job, getID(), "password", Encr.encryptPasswordIfNotUsingVariables(password));

      rep.saveJobEntryAttribute(id_job, getID(), "proxy_host", proxyHostname);
      rep.saveJobEntryAttribute(id_job, getID(), "proxy_port", proxyPort);
      rep.saveJobEntryAttribute(id_job, getID(), "non_proxy_hosts", nonProxyHosts);
    } catch (KettleDatabaseException dbe)
    {
      throw new KettleException("Unable to load job entry of type 'HTTP' to the repository for id_job=" + id_job, dbe);
    }
  }

  /**
   * @return Returns the URL.
   */
  public String getUrl()
  {
    return url;
  }

  /**
   * @param url The URL to set.
   */
  public void setUrl(String url)
  {
    this.url = url;
  }

  /**
   * @return Returns the target filename.
   */
  public String getTargetFilename()
  {
    return targetFilename;
  }

  /**
   * @param targetFilename The target filename to set.
   */
  public void setTargetFilename(String targetFilename)
  {
    this.targetFilename = targetFilename;
  }

  public String getNonProxyHosts()
  {
    return nonProxyHosts;
  }

  public void setNonProxyHosts(String nonProxyHosts)
  {
    this.nonProxyHosts = nonProxyHosts;
  }

  public String getPassword()
  {
    return password;
  }

  public void setPassword(String password)
  {
    this.password = password;
  }

  public String getProxyHostname()
  {
    return proxyHostname;
  }

  public void setProxyHostname(String proxyHostname)
  {
    this.proxyHostname = proxyHostname;
  }

  public String getProxyPort()
  {
    return proxyPort;
  }

  public void setProxyPort(String proxyPort)
  {
    this.proxyPort = proxyPort;
  }

  public String getUsername()
  {
    return username;
  }

  public void setUsername(String username)
  {
    this.username = username;
  }

  /**
   *  We made this one synchronized in the JVM because otherwise, this is not thread safe.
   *  In that case if (on an application server for example) several HTTP's are running at the same time,
   *  you get into problems because the System.setProperty() calls are system wide!
   */
  public synchronized Result execute(Result previousResult, int nr, Repository rep, Job parentJob)
  {
    LogWriter log = LogWriter.getInstance();

    Result result = previousResult;
    result.setResult(false);

    log.logBasic(toString(), "Start of HTTP job entry.");

    // Get previous result rows...
    List<RowMetaAndData> resultRows;
    String urlFieldnameToUse;

    if (Const.isEmpty(urlFieldname))
      urlFieldnameToUse = URL_FIELDNAME;
    else
      urlFieldnameToUse = urlFieldname;

    if (runForEveryRow)
    {
      resultRows = previousResult.getRows();
      if (resultRows == null)
      {
        result.setNrErrors(1);
        log.logError(toString(), "Unable to get result from previous job entry : can't continue.");
        return result;
      }
    } else
    {
      resultRows = new ArrayList<RowMetaAndData>();
      RowMetaAndData row = new RowMetaAndData();
      row.addValue(new ValueMeta(urlFieldnameToUse, ValueMetaInterface.TYPE_STRING), environmentSubstitute(url));
      resultRows.add(row);
      System.out.println("Added one row to rows: " + row);
    }

    URL server = null;

    String beforeProxyHost = System.getProperty("http.proxyHost");
    String beforeProxyPort = System.getProperty("http.proxyPort");
    String beforeNonProxyHosts = System.getProperty("http.nonProxyHosts");

    for (int i = 0; i < resultRows.size() && result.getNrErrors() == 0; i++)
    {
      RowMetaAndData row = (RowMetaAndData) resultRows.get(i);

      OutputStream outputFile = null;
      OutputStream uploadStream = null;
      BufferedInputStream fileStream = null;
      InputStream input = null;

      try
      {
        String urlToUse = environmentSubstitute(row.getString(urlFieldnameToUse, ""));

        log.logBasic(toString(), "Connecting to URL: " + urlToUse);

        if (!Const.isEmpty(proxyHostname))
        {
          System.setProperty("http.proxyHost", environmentSubstitute(proxyHostname));
          System.setProperty("http.proxyPort", environmentSubstitute(proxyPort));
          if (nonProxyHosts != null)
            System.setProperty("http.nonProxyHosts", environmentSubstitute(nonProxyHosts));
        }

        if (!Const.isEmpty(username))
        {
          Authenticator.setDefault(new Authenticator()
          {
            protected PasswordAuthentication getPasswordAuthentication()
            {
              String realPassword = environmentSubstitute(password);
              return new PasswordAuthentication(environmentSubstitute(username), realPassword != null ? realPassword
                  .toCharArray() : new char[]
              {});
            }
          });
        }

        String realTargetFile = environmentSubstitute(targetFilename);
        if (dateTimeAdded)
        {
          SimpleDateFormat daf = new SimpleDateFormat();
          Date now = new Date();

          daf.applyPattern("yyyMMdd");
          realTargetFile += "_" + daf.format(now);
          daf.applyPattern("HHmmss");
          realTargetFile += "_" + daf.format(now);

          if (!Const.isEmpty(targetFilenameExtention))
          {
            realTargetFile += "." + environmentSubstitute(targetFilenameExtention);
          }
        }

        // Create the output File...
        outputFile = KettleVFS.getOutputStream(realTargetFile, fileAppended);

        // Get a stream for the specified URL
        server = new URL(urlToUse);
        URLConnection connection = server.openConnection();

        // See if we need to send a file over?
        String realUploadFilename = environmentSubstitute(uploadFilename);
        if (!Const.isEmpty(realUploadFilename))
        {
          if(log.isDetailed()) log.logDetailed(toString(), "Start sending content of file [" + realUploadFilename + "] to server.");

          connection.setDoOutput(true);

          // Grab an output stream to upload data to web server
          uploadStream = connection.getOutputStream();

          fileStream = new BufferedInputStream(new FileInputStream(new File(realUploadFilename)));

          int c;
          while ((c = fileStream.read()) >= 0)
          {
            uploadStream.write(c);
          }

          // Close upload and file
          uploadStream.close();
          uploadStream = null;
          fileStream.close();
          fileStream = null;

          if(log.isDetailed()) log.logDetailed(toString(), "Finished sending content to server.");
        }

        if(log.isDetailed()) log.logDetailed(toString(), "Start reading reply from webserver.");

        // Read the result from the server...
        input = server.openStream();
        Date date = new Date(connection.getLastModified());
        log.logBasic(toString(), "Resource type: \"" + connection.getContentType() + "\", last modified on: \"" + date
            + "\".");

        int oneChar;
        long bytesRead = 0L;
        while ((oneChar = input.read()) != -1)
        {
          outputFile.write(oneChar);
          bytesRead++;
        }

        log.logBasic(toString(), "Finished writing " + bytesRead + " bytes to result file [" + realTargetFile + "]");

        // Add to the result files...
        ResultFile resultFile = new ResultFile(ResultFile.FILE_TYPE_GENERAL, KettleVFS.getFileObject(realTargetFile),
            parentJob.getJobname(), toString());
        result.getResultFiles().put(resultFile.getFile().toString(), resultFile);

        result.setResult(true);
      } catch (MalformedURLException e)
      {
        result.setNrErrors(1);
        log.logError(toString(), "The specified URL is not valid [" + url + "] : " + e.getMessage());
        log.logError(toString(), Const.getStackTracker(e));
      } catch (IOException e)
      {
        result.setNrErrors(1);
        log.logError(toString(), "I was unable to save the HTTP result to file because of a I/O error: "
            + e.getMessage());
        log.logError(toString(), Const.getStackTracker(e));
      } catch (Exception e)
      {
        result.setNrErrors(1);
        log.logError(toString(), "Error getting file from HTTP : " + e.getMessage());
        log.logError(toString(), Const.getStackTracker(e));
      } finally
      {
        // Close it all
        try
        {
          if (uploadStream != null)
            uploadStream.close(); // just to make sure
          if (fileStream != null)
            fileStream.close(); // just to make sure

          if (input != null)
            input.close();
          if (outputFile != null)
            outputFile.close();
        } catch (Exception e)
        {
          log.logError(toString(), "Unable to close streams : " + e.getMessage());
          result.setNrErrors(1);
        }

        // Set the proxy settings back as they were on the system!
        System.setProperty("http.proxyHost", Const.NVL(beforeProxyHost, ""));
        System.setProperty("http.proxyPort", Const.NVL(beforeProxyPort, ""));
        System.setProperty("http.nonProxyHosts", Const.NVL(beforeNonProxyHosts, ""));
      }

    }

    return result;
  }

  public boolean evaluates()
  {
    return true;
  }

  public String getUploadFilename()
  {
    return uploadFilename;
  }

  public void setUploadFilename(String uploadFilename)
  {
    this.uploadFilename = uploadFilename;
  }

  /**
   * @return Returns the getFieldname.
   */
  public String getUrlFieldname()
  {
    return urlFieldname;
  }

  /**
   * @param getFieldname The getFieldname to set.
   */
  public void setUrlFieldname(String getFieldname)
  {
    this.urlFieldname = getFieldname;
  }

  /**
   * @return Returns the runForEveryRow.
   */
  public boolean isRunForEveryRow()
  {
    return runForEveryRow;
  }

  /**
   * @param runForEveryRow The runForEveryRow to set.
   */
  public void setRunForEveryRow(boolean runForEveryRow)
  {
    this.runForEveryRow = runForEveryRow;
  }

  /**
   * @return Returns the fileAppended.
   */
  public boolean isFileAppended()
  {
    return fileAppended;
  }

  /**
   * @param fileAppended The fileAppended to set.
   */
  public void setFileAppended(boolean fileAppended)
  {
    this.fileAppended = fileAppended;
  }

  /**
   * @return Returns the dateTimeAdded.
   */
  public boolean isDateTimeAdded()
  {
    return dateTimeAdded;
  }

  /**
   * @param dateTimeAdded The dateTimeAdded to set.
   */
  public void setDateTimeAdded(boolean dateTimeAdded)
  {
    this.dateTimeAdded = dateTimeAdded;
  }

  /**
   * @return Returns the uploadFilenameExtention.
   */
  public String getTargetFilenameExtention()
  {
    return targetFilenameExtention;
  }

  /**
   * @param uploadFilenameExtention The uploadFilenameExtention to set.
   */
  public void setTargetFilenameExtention(String uploadFilenameExtention)
  {
    this.targetFilenameExtention = uploadFilenameExtention;
  }

  public List<ResourceReference> getResourceDependencies(JobMeta jobMeta)
  {
    List<ResourceReference> references = super.getResourceDependencies(jobMeta);
    String realUrl = jobMeta.environmentSubstitute(url);
    ResourceReference reference = new ResourceReference(this);
    reference.getEntries().add(new ResourceEntry(realUrl, ResourceType.URL));
    references.add(reference);
    return references;
  }

  @Override
  public void check(List<CheckResultInterface> remarks, JobMeta jobMeta)
  {
    andValidator().validate(this, "targetFilename", remarks, putValidators(notBlankValidator())); //$NON-NLS-1$
    andValidator().validate(this, "targetFilenameExtention", remarks, putValidators(notBlankValidator())); //$NON-NLS-1$
    andValidator().validate(this, "uploadFilename", remarks, putValidators(notBlankValidator())); //$NON-NLS-1$
    andValidator().validate(this, "proxyPort", remarks, putValidators(integerValidator())); //$NON-NLS-1$
  }

}