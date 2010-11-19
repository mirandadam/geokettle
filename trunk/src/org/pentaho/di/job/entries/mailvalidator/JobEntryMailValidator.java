/*
 * Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Samatar Hassan.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
*/

package org.pentaho.di.job.entries.mailvalidator;

import org.w3c.dom.Node;

import static org.pentaho.di.job.entry.validator.AndValidator.putValidators;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.andValidator;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.emailValidator;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.notBlankValidator;


import java.util.List;

import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobEntryType;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.Repository;

import org.pentaho.di.trans.steps.mailvalidator.MailValidation;
import org.pentaho.di.trans.steps.mailvalidator.MailValidationResult;



/**
 * Job entry mail validator.
 *
 * @author Samatar
 * @since 23-06-2008
 */
public class JobEntryMailValidator extends JobEntryBase implements Cloneable, JobEntryInterface
{

	private boolean smtpCheck;
	private String timeout;
	private String defaultSMTP;
	private String emailSender;
	private String emailAddress;

	public JobEntryMailValidator(String n, String scr)
	{
		super(n, "");
		emailAddress=null;
		smtpCheck=false;
		timeout="0";
		defaultSMTP=null;
		emailSender="noreply@domain.com";
	    setJobEntryType(JobEntryType.MAIL_VALIDATOR);
	}

	public JobEntryMailValidator()
	{
		this("", "");
	}
	
    public void setSMTPCheck(boolean smtpcheck)
    {
    	this.smtpCheck=smtpcheck;
    }
    public boolean isSMTPCheck()
    {
    	return smtpCheck;
    } 
    
    public String getEmailAddress()
    {
    	return this.emailAddress;
    }
    public void setEmailAddress(String emailAddress)
    {
    	this.emailAddress= emailAddress;
    }
    
    /**
     * @return Returns the timeout.
     */
    public String getTimeOut()
    {
        return timeout;
    }

    /**
     * @param timeout The timeout to set.
     */
    public void setTimeOut(String timeout)
    {
        this.timeout = timeout;
    } 
    
    /**
     * @return Returns the defaultSMTP.
     */
    public String getDefaultSMTP()
    {
        return defaultSMTP;
    }
    /**
     * @param defaultSMTP The defaultSMTP to set.
     */
    public void setDefaultSMTP(String defaultSMTP)
    {
        this.defaultSMTP = defaultSMTP;
    } 
    /**
     * @return Returns the emailSender.
     */
    public String geteMailSender()
    {
        return emailSender;
    }
    /**
     * @param emailSender The emailSender to set.
     */
    public void seteMailSender(String emailSender)
    {
        this.emailSender = emailSender;
    } 
	public JobEntryMailValidator(JobEntryBase jeb)
	{
		super(jeb);
	}

    public Object clone()
    {
        JobEntryMailValidator je = (JobEntryMailValidator) super.clone();
        return je;
    }

	public String getXML()
	{
        StringBuffer retval = new StringBuffer();
		retval.append("      ").append(XMLHandler.addTagValue("smtpCheck", smtpCheck));
		retval.append("      ").append(XMLHandler.addTagValue("timeout", timeout));
		retval.append("      ").append(XMLHandler.addTagValue("defaultSMTP", defaultSMTP));
		retval.append("      ").append(XMLHandler.addTagValue("emailSender", emailSender));
		retval.append("      ").append(XMLHandler.addTagValue("emailAddress", emailAddress));
		
		retval.append(super.getXML());

		return retval.toString();
	}

	  public void loadXML(Node entrynode, List<DatabaseMeta> databases, List<SlaveServer> slaveServers, Repository rep) throws KettleXMLException
	  {
	    try
	    {
	      super.loadXML(entrynode, databases, slaveServers);
			smtpCheck = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "smtpCheck"));
			timeout=XMLHandler.getTagValue(entrynode, "timeout");
			defaultSMTP=XMLHandler.getTagValue(entrynode, "defaultSMTP");
			emailSender=XMLHandler.getTagValue(entrynode, "emailSender");	
			emailAddress=XMLHandler.getTagValue(entrynode, "emailAddress");	
			
		}
		catch(Exception e)
		{
			throw new KettleXMLException(Messages.getString("JobEntryMailValidator.Meta.UnableToLoadFromXML"), e);
		}
	}

	  public void loadRep(Repository rep, long id_jobentry, List<DatabaseMeta> databases, List<SlaveServer> slaveServers) throws KettleException
	  {
	    try
	    {
	    	super.loadRep(rep, id_jobentry, databases, slaveServers);
			smtpCheck = rep.getJobEntryAttributeBoolean(id_jobentry, "smtpCheck"); 
			timeout = rep.getJobEntryAttributeString(id_jobentry, "timeout"); 
			defaultSMTP = rep.getJobEntryAttributeString(id_jobentry, "defaultSMTP"); 
			emailSender = rep.getJobEntryAttributeString(id_jobentry, "emailSender"); 
			emailAddress = rep.getJobEntryAttributeString(id_jobentry, "emailAddress"); 	
		}
		catch(KettleDatabaseException dbe)
		{
			throw new KettleException(Messages.getString("JobEntryMailValidator.Meta.UnableToLoadFromRep")+id_jobentry, dbe);
		}
	}

	// Save the attributes of this job entry
	//
	public void saveRep(Repository rep, long id_job)
		throws KettleException
	{
		try
		{
			super.saveRep(rep, id_job);
			rep.saveJobEntryAttribute(id_job, getID(), "smtpCheck", smtpCheck);
			rep.saveJobEntryAttribute(id_job, getID(), "timeout", timeout);
			rep.saveJobEntryAttribute(id_job, getID(), "defaultSMTP", defaultSMTP);
			rep.saveJobEntryAttribute(id_job, getID(), "emailSender", emailSender);
			rep.saveJobEntryAttribute(id_job, getID(), "emailAddress", emailAddress);
			
			
		}
		catch(KettleDatabaseException dbe)
		{
			throw new KettleException(Messages.getString("JobEntryMailValidator.Meta.UnableToSaveToRep")+id_job, dbe);
		}
	}


	/**
	 * Execute this job entry and return the result.
	 * In this case it means, just set the result boolean in the Result class.
	 * @param previousResult The result of the previous execution
	 * @return The Result of the execution.
	 */
	public Result execute(Result previousResult, int nr, Repository rep, Job parentJob)
	{
		Result result = previousResult;
		result.setNrErrors(1);
		result.setResult(false);
		LogWriter log = LogWriter.getInstance();
		
		String realEmailAddress=environmentSubstitute(emailAddress);
		if(Const.isEmpty(realEmailAddress))
		{
			log.logError(toString(),Messages.getString("JobEntryMailValidator.Error.EmailEmpty"));
			return result;
		}
		String realSender=environmentSubstitute(emailSender);
		if(smtpCheck)
		{
			// check sender
			if(Const.isEmpty(realSender))
			{
				log.logError(toString(),Messages.getString("JobEntryMailValidator.Error.EmailSenderEmpty"));
				return result;
			}
		}
		
		String realDefaultSMTP=environmentSubstitute(defaultSMTP);
		int timeOut=Const.toInt(environmentSubstitute(timeout), 0);
		
        // Split the mail-address:  separated by space
        String mailsCheck[] = realEmailAddress.split(" ");
        boolean exitloop=false;
        boolean mailIsValid=false;
        String MailError=null;
        for (int i = 0; i < mailsCheck.length && !exitloop; i++)
        {
        	String email = mailsCheck[i];
        	if(log.isDetailed())
        		log.logDetailed(toString(), Messages.getString("JobEntryMailValidator.CheckingMail",email));
    		
        	// Check if address is valid
    		MailValidationResult resultValidator=MailValidation.isAddressValid(email,
    				realSender,	realDefaultSMTP,timeOut,smtpCheck);
    		
    		mailIsValid=resultValidator.isValide();
    		MailError=resultValidator.getErrorMessage();
    		
        	if(log.isDetailed())
        	{
        		if(mailIsValid)
        			log.logDetailed(toString(), Messages.getString("JobEntryMailValidator.MailValid",email));
        		else
        		{
        			log.logDetailed(toString(), Messages.getString("JobEntryMailValidator.MailNotValid",email));
        			log.logDetailed(toString(), MailError);
        		}
    		
        	}
    		// invalid mail? exit loop
    		if(!resultValidator.isValide()) exitloop=true;
        }
		
		result.setResult(mailIsValid);
		if(mailIsValid) result.setNrErrors(0);

		// return result
		
		return result;
	}

	public boolean evaluates()
	{
		return true;
	}
	  @Override
	  public void check(List<CheckResultInterface> remarks, JobMeta jobMeta)
	  {

	    andValidator().validate(this, "emailAddress", remarks, putValidators(notBlankValidator())); //$NON-NLS-1$
	    andValidator().validate(this, "emailSender", remarks, putValidators(notBlankValidator(), emailValidator())); //$NON-NLS-1$

	    if (isSMTPCheck())
	    {
	      andValidator().validate(this, "defaultSMTP", remarks, putValidators(notBlankValidator())); //$NON-NLS-1$
	    }
	  }
}