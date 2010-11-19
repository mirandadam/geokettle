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
package org.pentaho.di.trans.steps.scriptvalues_mod;

import java.util.List;
import java.util.Map;

import org.pentaho.di.core.RowSet;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.trans.step.RowListener;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepListener;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
	/**
	 * Dummy class used for test().
	 */
	public class ScriptValuesModDummy implements StepInterface, ScriptValuesModInterface
	{
		private RowMetaInterface inputRowMeta;
		private RowMetaInterface outputRowMeta;

		public ScriptValuesModDummy(RowMetaInterface inputRowMeta, RowMetaInterface outputRowMeta) {
			this.inputRowMeta = inputRowMeta;
			this.outputRowMeta = outputRowMeta;
		}
		public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException {
			return false;
		}

		public void addRowListener(RowListener rowListener) {
		}

		public void dispose(StepMetaInterface sii, StepDataInterface sdi) {
		}

		public long getErrors() {
			return 0;
		}

		public List<RowSet> getInputRowSets() {
			return null;
		}

		public long getLinesInput() {
			return 0;
		}

		public long getLinesOutput() {
			return 0;
		}

		public long getLinesRead() {
			return 0;
		}

		public long getLinesUpdated() {
			return 0;
		}

		public long getLinesWritten() {
			return 0;
		}

		public long getLinesRejected() {
			return 0;
		}		
		
		public List<RowSet> getOutputRowSets() {
			return null;
		}

		public String getPartitionID() {
			return null;
		}

		public Object[] getRow() throws KettleException {
			return null;
		}

		public List<RowListener> getRowListeners() {
			return null;
		}

		public String getStepID() {
			return null;
		}

		public String getStepname() {
			return null;
		}

		public boolean init(StepMetaInterface stepMetaInterface, StepDataInterface stepDataInterface) {
			return false;
		}

		public boolean isAlive() {
			return false;
		}

		public boolean isPartitioned() {
			return false;
		}

		public boolean isStopped() {
			return false;
		}

		public void markStart() {
		}

		public void markStop() {
		}

		public void putRow(RowMetaInterface rowMeta, Object[] row) throws KettleException {
		}

		public void removeRowListener(RowListener rowListener) {
		}

		public void run() {
		}

		public void setErrors(long errors) {
		}

		public void setOutputDone() {
		}

		public void setPartitionID(String partitionID) {
		}

		public void start() {
		}

		public void stopAll() {
		}

		public void stopRunning(StepMetaInterface stepMetaInterface, StepDataInterface stepDataInterface) throws KettleException {
		}

		public void cleanup() {
		}

		public void pauseRunning() {
		}

		public void resumeRunning() {
		}

		public void copyVariablesFrom(VariableSpace space) {
		}

		public String environmentSubstitute(String aString) {
			return null;
		}

		public String[] environmentSubstitute(String[] string) {
			return null;
		}

		public boolean getBooleanValueOfVariable(String variableName, boolean defaultValue) {
			return false;
		}

		public VariableSpace getParentVariableSpace() {
			return null;
		}
		
		public void setParentVariableSpace(VariableSpace parent) 
		{
		}

		public String getVariable(String variableName, String defaultValue) {
			return defaultValue;
		}

		public String getVariable(String variableName) {
			return null;
		}

		public void initializeVariablesFrom(VariableSpace parent) {
		}

		public void injectVariables(Map<String, String> prop) {
		}

		public String[] listVariables() {
			return null;
		}

		public void setVariable(String variableName, String variableValue) {
		}

		public void shareVariablesWith(VariableSpace space) {
		}	
		
		public RowMetaInterface getInputRowMeta() {
			return inputRowMeta;
		}

		public RowMetaInterface getOutputRowMeta() {
			return outputRowMeta;
		}

		public void initBeforeStart() throws KettleStepException {
		}

		public void setLinesRejected(long linesRejected) {
		}

		public int getCopy() {
			return 0;
		}

		public void addStepListener(StepListener stepListener) {
		}
		
		public boolean isMapping() {
			return false;
		}
		
		public StepMeta getStepMeta() {
			return null;
		}
	}
