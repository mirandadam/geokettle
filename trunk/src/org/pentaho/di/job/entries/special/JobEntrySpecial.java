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

package org.pentaho.di.job.entries.special;
import java.util.Calendar;
import java.util.List;

import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleJobException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobEntryType;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.Repository;
import org.w3c.dom.Node;





/**
 * This class can contain a few special job entries such as Start and Dummy.
 *
 * @author Matt
 * @since 05-11-2003
 *
 */

public class JobEntrySpecial extends JobEntryBase implements Cloneable, JobEntryInterface
{
	public final static int NOSCHEDULING = 0;
	public final static int INTERVAL = 1;
	public final static int DAILY = 2;
	public final static int WEEKLY = 3;
	public final static int MONTHLY = 4;

	private boolean start;
	private boolean dummy;
	private boolean repeat = false;
	private int schedulerType = NOSCHEDULING;
	private int intervalSeconds = 0;
	private int intervalMinutes = 60;
	private int dayOfMonth = 1;
	private int weekDay = 1;
	private int minutes = 0;
	private int hour = 12;

	public JobEntrySpecial()
	{
		this(null, false, false);
	}

	public JobEntrySpecial(String name, boolean start, boolean dummy)
	{
		super(name, "");
		this.start = start;
		this.dummy = dummy;
		setJobEntryType(JobEntryType.SPECIAL);
	}

	public JobEntrySpecial(JobEntryBase jeb)
	{
		super(jeb);
	}

    public Object clone()
    {
        JobEntrySpecial je = (JobEntrySpecial) super.clone();
        return je;
    }

	public String getXML()
	{
        StringBuffer retval = new StringBuffer(200);

		retval.append(super.getXML());

		retval.append("      ").append(XMLHandler.addTagValue("start",         start));
		retval.append("      ").append(XMLHandler.addTagValue("dummy",         dummy));
		retval.append("      ").append(XMLHandler.addTagValue("repeat",        repeat));
		retval.append("      ").append(XMLHandler.addTagValue("schedulerType", schedulerType));
		retval.append("      ").append(XMLHandler.addTagValue("intervalSeconds",      intervalSeconds));
		retval.append("      ").append(XMLHandler.addTagValue("intervalMinutes",      intervalMinutes));
		retval.append("      ").append(XMLHandler.addTagValue("hour",          hour));
		retval.append("      ").append(XMLHandler.addTagValue("minutes",       minutes));
		retval.append("      ").append(XMLHandler.addTagValue("weekDay",       weekDay));
		retval.append("      ").append(XMLHandler.addTagValue("DayOfMonth",    dayOfMonth));

		return retval.toString();
	}

	public void loadXML(Node entrynode, List<DatabaseMeta> databases, List<SlaveServer> slaveServers, Repository rep) throws KettleXMLException
	{
		try
		{
			super.loadXML(entrynode, databases, slaveServers);
			start = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "start"));
			dummy = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "dummy"));
			repeat = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "repeat"));
			setSchedulerType( Const.toInt(XMLHandler.getTagValue(entrynode, "schedulerType"), NOSCHEDULING) );
			setIntervalSeconds   ( Const.toInt(XMLHandler.getTagValue(entrynode, "intervalSeconds"), 0) );
			setIntervalMinutes	 ( Const.toInt(XMLHandler.getTagValue(entrynode, "intervalMinutes"), 0) );
			setHour      ( Const.toInt(XMLHandler.getTagValue(entrynode, "hour"), 0) );
			setMinutes   ( Const.toInt(XMLHandler.getTagValue(entrynode, "minutes"), 0) );
			setWeekDay   ( Const.toInt(XMLHandler.getTagValue(entrynode, "weekDay"), 0) );
			setDayOfMonth( Const.toInt(XMLHandler.getTagValue(entrynode, "dayOfMonth"), 0) );
		}
		catch(KettleException e)
		{
			throw new KettleXMLException("Unable to load job entry of type 'special' from XML node", e);
		}
	}

	public void loadRep(Repository rep, long id_jobentry, List<DatabaseMeta>  databases, List<SlaveServer> slaveServers)
		throws KettleException
	{
		try
		{
			super.loadRep(rep, id_jobentry, databases, slaveServers);

			start = rep.getJobEntryAttributeBoolean(id_jobentry, "start");
			dummy = rep.getJobEntryAttributeBoolean(id_jobentry, "dummy");
			repeat = rep.getJobEntryAttributeBoolean(id_jobentry, "repeat");
			schedulerType  = (int)rep.getJobEntryAttributeInteger(id_jobentry, "schedulerType");
			intervalSeconds  = (int)rep.getJobEntryAttributeInteger(id_jobentry, "intervalSeconds");
			intervalMinutes  = (int)rep.getJobEntryAttributeInteger(id_jobentry, "intervalMinutes");
			hour  = (int)rep.getJobEntryAttributeInteger(id_jobentry, "hour");
			minutes  = (int)rep.getJobEntryAttributeInteger(id_jobentry, "minutes");
			weekDay  = (int)rep.getJobEntryAttributeInteger(id_jobentry, "weekDay");
			dayOfMonth  = (int)rep.getJobEntryAttributeInteger(id_jobentry, "dayOfMonth");
		}
		catch(KettleDatabaseException dbe)
		{
			throw new KettleException("Unable to load job entry of type 'special' from the repository for id_jobentry="+id_jobentry, dbe);
		}
	}

	// Save the attributes of this job entry
	//
	public void saveRep(Repository rep, long id_job)
		throws KettleException
	{
		super.saveRep(rep, id_job);

		try
		{
			rep.saveJobEntryAttribute(id_job, getID(), "start", start);
			rep.saveJobEntryAttribute(id_job, getID(), "dummy", dummy);
			rep.saveJobEntryAttribute(id_job, getID(), "repeat", repeat);
			rep.saveJobEntryAttribute(id_job, getID(), "schedulerType", schedulerType);
			rep.saveJobEntryAttribute(id_job, getID(), "intervalSeconds", intervalSeconds);
			rep.saveJobEntryAttribute(id_job, getID(), "intervalMinutes", intervalMinutes);
			rep.saveJobEntryAttribute(id_job, getID(), "hour", hour);
			rep.saveJobEntryAttribute(id_job, getID(), "minutes", minutes);
			rep.saveJobEntryAttribute(id_job, getID(), "weekDay", weekDay);
			rep.saveJobEntryAttribute(id_job, getID(), "dayOfMonth", dayOfMonth);
		}
		catch(KettleDatabaseException dbe)
		{
			throw new KettleException("Unable to save job entry of type 'special' to the repository with id_job="+id_job, dbe);
		}
	}

	public boolean isStart()
	{
		return start;
	}

	public boolean isDummy()
	{
		return dummy;
	}

	public Result execute(Result previousResult, int nr, Repository rep, Job parentJob) throws KettleJobException
	{
		Result result = previousResult;

		if (isStart())
		{
			try {
				long sleepTime = getNextExecutionTime();
				if (sleepTime>0) {
					parentJob.getLog().logBasic(parentJob.getJobname(), "Sleeping: " + (sleepTime/1000/60) + " minutes (sleep time="+sleepTime+")");
					long totalSleep = 0L;
					while (totalSleep<sleepTime && !parentJob.isStopped()) {
						Thread.sleep(1000L);
						totalSleep+=1000L;
					}
				}
			} catch (InterruptedException e) {
				throw new KettleJobException(e);
			}
			result = previousResult;
			result.setResult( true );
		}
		else
		if (isDummy())
		{
			result = previousResult;
		}
		return result;
	}

	private long getNextExecutionTime() {
		switch (schedulerType) {
		case NOSCHEDULING:
			return 0;
		case INTERVAL:
			return getNextIntervalExecutionTime();
		case DAILY:
			return getNextDailyExecutionTime();
		case WEEKLY:
			return getNextWeeklyExecutionTime();
		case MONTHLY:
			return getNextMonthlyExecutionTime();
		default:
			break;
		}
		return 0;
	}

	private long getNextIntervalExecutionTime() {
	    return intervalSeconds * 1000 + intervalMinutes * 1000 * 60;
	}

	private long getNextMonthlyExecutionTime() {
		Calendar calendar = Calendar.getInstance();

		long nowMillis = calendar.getTimeInMillis();
		int amHour = hour;
		if(amHour>12) {
			amHour = amHour-12;
			calendar.set(Calendar.AM_PM,Calendar.PM);
		} else {
			calendar.set(Calendar.AM_PM,Calendar.AM);
		}
		calendar.set(Calendar.HOUR,amHour);
		calendar.set(Calendar.MINUTE,minutes);
		calendar.set(Calendar.DAY_OF_MONTH,dayOfMonth);
		if(calendar.getTimeInMillis()<=nowMillis) {
			calendar.add(Calendar.MONTH,1);
		}
		return calendar.getTimeInMillis()-nowMillis;
	}

	private long getNextWeeklyExecutionTime() {
		Calendar calendar = Calendar.getInstance();

		long nowMillis = calendar.getTimeInMillis();
		int amHour = hour;
		if(amHour>12) {
			amHour = amHour-12;
			calendar.set(Calendar.AM_PM,Calendar.PM);
		} else {
			calendar.set(Calendar.AM_PM,Calendar.AM);
		}
		calendar.set(Calendar.HOUR,amHour);
		calendar.set(Calendar.MINUTE,minutes);
		calendar.set(Calendar.DAY_OF_WEEK,weekDay+1);
		if(calendar.getTimeInMillis()<=nowMillis) {
			calendar.add(Calendar.WEEK_OF_YEAR,1);
		}
		return calendar.getTimeInMillis()-nowMillis;
	}

	private long getNextDailyExecutionTime() {
		Calendar calendar = Calendar.getInstance();

		long nowMillis = calendar.getTimeInMillis();
		int amHour = hour;
		if(amHour>12) {
			amHour = amHour-12;
			calendar.set(Calendar.AM_PM,Calendar.PM);
		} else {
			calendar.set(Calendar.AM_PM,Calendar.AM);
		}
		calendar.set(Calendar.HOUR,amHour);
		calendar.set(Calendar.MINUTE,minutes);
		if(calendar.getTimeInMillis()<=nowMillis) {
			calendar.add(Calendar.DAY_OF_MONTH,1);
		}
		return calendar.getTimeInMillis()-nowMillis;
	}

	public boolean evaluates()
	{
		return false;
	}

	public boolean isUnconditional()
	{
		return true;
	}

	public int getSchedulerType() {
		return schedulerType;
	}

	public int getHour() {
		return hour;
	}

	public int getMinutes() {
		return minutes;
	}

	public int getWeekDay() {
		return weekDay;
	}

	public int getDayOfMonth() {
		return dayOfMonth;
	}

	public void setDayOfMonth(int dayOfMonth) {
		this.dayOfMonth = dayOfMonth;
	}

	public void setHour(int hour) {
		this.hour = hour;
	}

	public void setMinutes(int minutes) {
		this.minutes = minutes;
	}

	public void setWeekDay(int weekDay) {
		this.weekDay = weekDay;
	}

	public void setSchedulerType(int schedulerType) {
		this.schedulerType = schedulerType;
	}

	public boolean isRepeat() {
		return repeat;
	}

	public void setRepeat(boolean repeat) {
		this.repeat = repeat;
	}

	public int getIntervalSeconds() {
	    return intervalSeconds;
	}

	public void setIntervalSeconds(int intervalSeconds) {
	    this.intervalSeconds = intervalSeconds;
	}

	public int getIntervalMinutes() {
		return intervalMinutes;
	}

	public void setIntervalMinutes(int intervalMinutes) {
		this.intervalMinutes = intervalMinutes;
	}

    /**
     * @param dummy the dummy to set
     */
    public void setDummy(boolean dummy)
    {
        this.dummy = dummy;
    }

    /**
     * @param start the start to set
     */
    public void setStart(boolean start)
    {
        this.start = start;
    }

    @Override
    public void check(List<CheckResultInterface> remarks, JobMeta jobMeta)
    {

    }


}