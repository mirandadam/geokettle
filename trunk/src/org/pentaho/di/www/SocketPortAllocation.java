package org.pentaho.di.www;

import java.util.Date;

public class SocketPortAllocation {
	private boolean allocated;
	private int		port;
	private Date	lastRequested;
	
	private String  transformationName;
	private String  sourceSlaveName;
	private String  sourceStepName;
	private String  sourceStepCopy;
	private String  targetSlaveName;
	private String  targetStepName;
	private String  targetStepCopy;
	
	/**
	 * @param port
	 * @param lastRequested
	 * @param slaveName
	 * @param transformationName
	 * @param sourceStepName
	 * @param sourceStepCopy
	 */
	public SocketPortAllocation(int port, Date lastRequested, String transformationName, String sourceSlaveName, String sourceStepName, String sourceStepCopy, String targetSlaveName, String targetStepName, String targetStepCopy) {
		this.port = port;
		this.lastRequested = lastRequested;
		this.transformationName = transformationName;

		this.sourceSlaveName = sourceSlaveName;
		this.sourceStepName = sourceStepName;
		this.sourceStepCopy = sourceStepCopy;
		
		this.targetSlaveName = targetSlaveName;
		this.targetStepName = targetStepName;
		this.targetStepCopy = targetStepCopy;
		this.allocated=true;
	}

	/**
	 * @return the port
	 */
	public int getPort() {
		return port;
	}

	/**
	 * @param port
	 *            the port to set
	 */
	public void setPort(int port) {
		this.port = port;
	}
	
	public boolean equals(Object obj) {
		if (obj==this) return true;
		if (!(obj instanceof SocketPortAllocation)) return false;
		
		SocketPortAllocation allocation = (SocketPortAllocation) obj;
		
		return allocation.getPort()==port;
	}

	public int hashCode() {
		return Integer.valueOf(port).hashCode();
	}

	/**
	 * @return the lastRequested
	 */
	public Date getLastRequested() {
		return lastRequested;
	}

	/**
	 * @param lastRequested
	 *            the lastRequested to set
	 */
	public void setLastRequested(Date lastRequested) {
		this.lastRequested = lastRequested;
	}

	/**
	 * @return the transformationName
	 */
	public String getTransformationName() {
		return transformationName;
	}

	/**
	 * @param transformationName the transformationName to set
	 */
	public void setTransformationName(String transformationName) {
		this.transformationName = transformationName;
	}

	/**
	 * @return the allocated
	 */
	public boolean isAllocated() {
		return allocated;
	}

	/**
	 * @param allocated the allocated to set
	 */
	public void setAllocated(boolean allocated) {
		this.allocated = allocated;
	}

	/**
	 * @return the sourceStepName
	 */
	public String getSourceStepName() {
		return sourceStepName;
	}

	/**
	 * @param sourceStepName the sourceStepName to set
	 */
	public void setSourceStepName(String sourceStepName) {
		this.sourceStepName = sourceStepName;
	}

	/**
	 * @return the sourceStepCopy
	 */
	public String getSourceStepCopy() {
		return sourceStepCopy;
	}

	/**
	 * @param sourceStepCopy the sourceStepCopy to set
	 */
	public void setSourceStepCopy(String sourceStepCopy) {
		this.sourceStepCopy = sourceStepCopy;
	}

	/**
	 * @return the targetStepName
	 */
	public String getTargetStepName() {
		return targetStepName;
	}

	/**
	 * @param targetStepName the targetStepName to set
	 */
	public void setTargetStepName(String targetStepName) {
		this.targetStepName = targetStepName;
	}

	/**
	 * @return the targetStepCopy
	 */
	public String getTargetStepCopy() {
		return targetStepCopy;
	}

	/**
	 * @param targetStepCopy the targetStepCopy to set
	 */
	public void setTargetStepCopy(String targetStepCopy) {
		this.targetStepCopy = targetStepCopy;
	}

	/**
	 * @return the sourceSlaveName
	 */
	public String getSourceSlaveName() {
		return sourceSlaveName;
	}

	/**
	 * @param sourceSlaveName the sourceSlaveName to set
	 */
	public void setSourceSlaveName(String sourceSlaveName) {
		this.sourceSlaveName = sourceSlaveName;
	}

	/**
	 * @return the targetSlaveName
	 */
	public String getTargetSlaveName() {
		return targetSlaveName;
	}

	/**
	 * @param targetSlaveName the targetSlaveName to set
	 */
	public void setTargetSlaveName(String targetSlaveName) {
		this.targetSlaveName = targetSlaveName;
	}

}
