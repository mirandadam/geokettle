/*
 * Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Samatar HASSAN.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
*/
package org.pentaho.di.trans.steps.stringcut;

import java.util.List;
import java.util.Map;

import org.w3c.dom.Node;

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
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;


/**
 * @author Samatar Hassan
 * @since 30 September 2008
 */
public class StringCutMeta extends BaseStepMeta implements StepMetaInterface {
	
	private String fieldInStream[];
	
	private String fieldOutStream[];
	
	private String cutFrom[];
	
	private String cutTo[];
	

	public StringCutMeta() {
		super(); // allocate BaseStepMeta
	}

	/**
	 * @return Returns the fieldInStream.
	 */
	public String[] getFieldInStream() {
		return fieldInStream;
	}

	/**
	 * @param fieldInStream
	 *            The fieldInStream to set.
	 */
	public void setFieldInStream(String[] keyStream) {
		this.fieldInStream = keyStream;
	}
	
	/**
	 * @return Returns the fieldOutStream.
	 */
	public String[] getFieldOutStream() {
		return fieldOutStream;
	}
	
	/**
	 * @param keyStream  The fieldOutStream to set.
	 */
	public void setFieldOutStream(String[] keyStream) {
		this.fieldOutStream = keyStream;
	}
	public String[] getCutFrom() {
		return cutFrom;
	}
	
	public void setCutFrom(String[] cutFrom) {
		this.cutFrom = cutFrom;
	}
	public String[] getCutTo() {
		return cutTo;
	}
	
	public void setCutTo(String[] cutTo) {
		this.cutTo = cutTo;
	}
	
	 public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters)
	  throws KettleXMLException
	{
		readData(stepnode);
	}
	public void allocate(int nrkeys) {
		fieldInStream = new String[nrkeys];
		fieldOutStream = new String[nrkeys];
		cutTo = new String[nrkeys];
		cutFrom= new String[nrkeys];
	}

	public Object clone() {
		StringCutMeta retval = (StringCutMeta) super.clone();
		int nrkeys = fieldInStream.length;

		retval.allocate(nrkeys);

		for (int i = 0; i < nrkeys; i++) {
			retval.fieldInStream[i] = fieldInStream[i];
			retval.fieldOutStream[i] = fieldOutStream[i];
			retval.cutTo[i] = cutTo[i];
			retval.cutFrom[i] = cutFrom[i];
		}

		return retval;
	}

	private void readData(Node stepnode) throws KettleXMLException
	{
		try
		{
			int nrkeys;

			Node lookup = XMLHandler.getSubNode(stepnode, "fields"); //$NON-NLS-1$
			nrkeys = XMLHandler.countNodes(lookup, "field"); //$NON-NLS-1$

			allocate(nrkeys);

			for (int i = 0; i < nrkeys; i++) {
				Node fnode = XMLHandler.getSubNodeByNr(lookup, "field", i); //$NON-NLS-1$
				fieldInStream[i] = Const.NVL(XMLHandler.getTagValue(fnode,"in_stream_name"), ""); //$NON-NLS-1$
				fieldOutStream[i] = Const.NVL(XMLHandler.getTagValue(fnode,"out_stream_name"), ""); //$NON-NLS-1$
				cutFrom[i] = Const.NVL(XMLHandler.getTagValue(fnode, "cut_from"), ""); //$NON-NLS-1$
				cutTo[i] = Const.NVL(XMLHandler.getTagValue(fnode, "cut_to"), ""); //$NON-NLS-1$
			}
		} catch (Exception e) {
			throw new KettleXMLException(
					Messages.getString("StringCutMeta.Exception.UnableToReadStepInfoFromXML"), e); //$NON-NLS-1$
		}
	}

	public void setDefault() {
		fieldInStream = null;
		fieldOutStream = null;

		int nrkeys = 0;

		allocate(nrkeys);
	}

	public String getXML() {
		StringBuffer retval = new StringBuffer(500);

		retval.append("    <fields>").append(Const.CR); //$NON-NLS-1$

		for (int i = 0; i < fieldInStream.length; i++) {
			retval.append("      <field>").append(Const.CR); //$NON-NLS-1$
			retval.append("        ").append(XMLHandler.addTagValue("in_stream_name", fieldInStream[i])); //$NON-NLS-1$ //$NON-NLS-2$
			retval.append("        ").append(XMLHandler.addTagValue("out_stream_name", fieldOutStream[i])); 
			retval.append("        ").append(XMLHandler.addTagValue("cut_from", cutFrom[i])); //$NON-NLS-1$ //$NON-NLS-2$
			retval.append("        ").append(XMLHandler.addTagValue("cut_to", cutTo[i])); //$NON-NLS-1$ //$NON-NLS-2$
			retval.append("      </field>").append(Const.CR); //$NON-NLS-1$
		}

		retval.append("    </fields>").append(Const.CR); //$NON-NLS-1$

		return retval.toString();
	}

	 public void readRep(Repository rep, long id_step, List<DatabaseMeta> databases, Map<String, Counter> counters)
     throws KettleException
     {
		try {

			int nrkeys = rep.countNrStepAttributes(id_step, "in_stream_name"); //$NON-NLS-1$

			allocate(nrkeys);
			for (int i = 0; i < nrkeys; i++) {
				fieldInStream[i] = Const.NVL(rep.getStepAttributeString(id_step, i,	"in_stream_name"), ""); //$NON-NLS-1$
				fieldOutStream[i] = Const.NVL(rep.getStepAttributeString(id_step, i,	"out_stream_name"), "");
				cutFrom[i] = Const.NVL(rep.getStepAttributeString(id_step, i, "cut_from"), ""); //$NON-NLS-1$
				cutTo[i] = Const.NVL(rep.getStepAttributeString(id_step, i, "cut_to"), ""); //$NON-NLS-1$
			}
		} catch (Exception e) {
			throw new KettleException(
					Messages.getString("StringCutMeta.Exception.UnexpectedErrorInReadingStepInfo"), e); //$NON-NLS-1$
		}
	}

	public void saveRep(Repository rep, long id_transformation, long id_step)
			throws KettleException {
		try {

			for (int i = 0; i < fieldInStream.length; i++) {
				rep.saveStepAttribute(id_transformation, id_step, i,"in_stream_name", fieldInStream[i]); //$NON-NLS-1$
				rep.saveStepAttribute(id_transformation, id_step, i,"out_stream_name", fieldOutStream[i]);
				rep.saveStepAttribute(id_transformation, id_step, i,"cut_from", cutFrom[i]); //$NON-NLS-1$
				rep.saveStepAttribute(id_transformation, id_step, i,"cut_to", cutTo[i]); //$NON-NLS-1$
			}
		} catch (Exception e) {
			throw new KettleException(
					Messages.getString("StringCutMeta.Exception.UnableToSaveStepInfo") + id_step, e); //$NON-NLS-1$
		}
	}
	 public void getFields(RowMetaInterface inputRowMeta, String name, RowMetaInterface info[], StepMeta nextStep,
	            VariableSpace space) throws KettleStepException
	  {			
		for(int i=0;i<fieldOutStream.length;i++) {
			if (!Const.isEmpty(fieldOutStream[i])){
				ValueMetaInterface v = new ValueMeta(space.environmentSubstitute(fieldOutStream[i]), ValueMeta.TYPE_STRING);
				v.setLength(100, -1);
				v.setOrigin(name);
				inputRowMeta.addValueMeta(v);
			}
		}	
	}
	 public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepinfo,
	            RowMetaInterface prev, String input[], String output[], RowMetaInterface info)
	  {

		CheckResult cr;
		String error_message = ""; //$NON-NLS-1$
		boolean first = true;
		boolean error_found = false;

		if (prev == null) {
			error_message += Messages.getString("StringCutMeta.CheckResult.NoInputReceived") + Const.CR; //$NON-NLS-1$
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR,	error_message, stepinfo);
			remarks.add(cr);
		} else {

			for (int i = 0; i < fieldInStream.length; i++) {
				String field = fieldInStream[i];

				ValueMetaInterface v = prev.searchValueMeta(field);
				if (v == null) {
					if (first) {
						first = false;
						error_message += Messages.getString("StringCutMeta.CheckResult.MissingInStreamFields") + Const.CR; //$NON-NLS-1$
					}
					error_found = true;
					error_message += "\t\t" + field + Const.CR; //$NON-NLS-1$
				}
			}
			if (error_found) {
				cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR,error_message, stepinfo);
			} else {
				cr = new CheckResult(
						CheckResult.TYPE_RESULT_OK,
						Messages.getString("StringCutMeta.CheckResult.FoundInStreamFields"), stepinfo); //$NON-NLS-1$
			}
			remarks.add(cr);

			// Check whether all are strings
			first = true;
			error_found = false;
			for (int i = 0; i < fieldInStream.length; i++) {
				String field = fieldInStream[i];

				ValueMetaInterface v = prev.searchValueMeta(field);
				if (v != null) {
					if (v.getType() != ValueMeta.TYPE_STRING) {
						if (first) {
							first = false;
							error_message += Messages.getString("StringCutMeta.CheckResult.OperationOnNonStringFields") + Const.CR; //$NON-NLS-1$
						}
						error_found = true;
						error_message += "\t\t" + field + Const.CR; //$NON-NLS-1$
					}
				}
			}
			if (error_found) {
				cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR,
						error_message, stepinfo);
			} else {
				cr = new CheckResult(
						CheckResult.TYPE_RESULT_OK,
						Messages.getString("StringCutMeta.CheckResult.AllOperationsOnStringFields"), stepinfo); //$NON-NLS-1$
			}
			remarks.add(cr);

			if (fieldInStream.length>0) {
				for (int idx = 0; idx < fieldInStream.length; idx++) {
					if (Const.isEmpty(fieldInStream[idx])) {
						cr = new CheckResult(
								CheckResult.TYPE_RESULT_ERROR,
								Messages.getString("StringCutMeta.CheckResult.InStreamFieldMissing", new Integer(idx + 1).toString()), stepinfo); //$NON-NLS-1$
						remarks.add(cr);
					
					}
				}
			}

			// Check if all input fields are distinct.
			for (int idx = 0; idx < fieldInStream.length; idx++) {
				for (int jdx = 0; jdx < fieldInStream.length; jdx++) {
					if (fieldInStream[idx].equals(fieldInStream[jdx])
							&& idx != jdx && idx < jdx) {
						error_message = Messages.getString("StringCutMeta.CheckResult.FieldInputError", fieldInStream[idx]); //$NON-NLS-1$
						cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR,error_message, stepinfo);
						remarks.add(cr);
					}
				}
			}

		}
	}

	
	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, 
			int cnr, TransMeta transMeta, Trans trans)
	{
		return new StringCut(stepMeta, stepDataInterface, cnr, transMeta, trans);
	}

	public StepDataInterface getStepData() {
		return new StringCutData();
	}

	public boolean supportsErrorHandling() {
		return true;
	}

}