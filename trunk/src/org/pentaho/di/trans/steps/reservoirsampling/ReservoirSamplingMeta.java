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

/*
 *    ReservoirSamplingMeta.java
 *    Copyright 2007 Pentaho Corporation.  All rights reserved. 
 *
 */

package org.pentaho.di.trans.steps.reservoirsampling;

import java.util.List;
import java.util.Map;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
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

/**
 * Contains the meta data for the ReservoirSampling step.
 *
 * @author Mark Hall (mhall{[at]}pentaho.org)
 * @version 1.0
 */
public class ReservoirSamplingMeta
  extends BaseStepMeta
  implements StepMetaInterface {

  public static final String XML_TAG = "reservoir_sampling";

  // Size of the sample to output
  protected String m_sampleSize = "100";

  // Seed for the random number generator
  protected String m_randomSeed = "1";

  /**
   * Creates a new <code>ReservoirMeta</code> instance.
   */
  public ReservoirSamplingMeta() {
    super(); // allocate BaseStepMeta
  }

  /**
   * Get the sample size to generate.
   *
   * @return the sample size
   */
  public String getSampleSize() {
    return m_sampleSize;
  }

  /**
   * Set the size of the sample to generate
   *
   * @param sampleS the size of the sample
   */
  public void setSampleSize(String sampleS) {
    m_sampleSize = sampleS;
  }

  /**
   * Get the random seed
   *
   * @return the random seed
   */
  public String getSeed() {
    return m_randomSeed;
  }

  /**
   * Set the seed value for the random number
   * generator
   *
   * @param seed the seed value
   */
  public void setSeed(String seed) {
    m_randomSeed = seed;
  }

  /**
   * Return the XML describing this (configured) step
   *
   * @return a <code>String</code> containing the XML
   */
  public String getXML() {
    StringBuffer retval = new StringBuffer(100);

    retval.append("<" + XML_TAG + ">");
    
    retval.append(XMLHandler.addTagValue("sample_size",
                                         m_sampleSize));
    retval.append(XMLHandler.addTagValue("seed",
                                         m_randomSeed));
    retval.append("</" + XML_TAG + ">");

    return retval.toString();
  }

  /**
   * Check for equality
   *
   * @param obj an <code>Object</code> to compare with
   * @return true if equal to the supplied object
   */
  public boolean equals(Object obj) {       
    if (obj != null && (obj.getClass().equals(this.getClass()))) {
      ReservoirSamplingMeta m = (ReservoirSamplingMeta)obj;
      return (getXML() == m.getXML());
    }
    
    return false;
  }

  /**
   * Set the defaults for this step.
   */
  public void setDefault() {
    m_sampleSize = "100";
    m_randomSeed = "1";
  }

  /**
   * Clone this step's meta data
   *
   * @return the cloned meta data
   */
  public Object clone() {
    ReservoirSamplingMeta retval = (ReservoirSamplingMeta) super.clone();
    return retval;
  }

  /**
   * Loads the meta data for this (configured) step
   * from XML.
   *
   * @param stepnode the step to load
   * @exception KettleXMLException if an error occurs
   */
  public void loadXML(Node stepnode, 
                      List<DatabaseMeta> databases, 
                      Map<String, Counter> counters)
    throws KettleXMLException {

    int nrSteps = 
      XMLHandler.countNodes(stepnode, XML_TAG);

    if (nrSteps > 0) {
      Node reservoirnode = 
        XMLHandler.getSubNodeByNr(stepnode, XML_TAG, 0); 

      m_sampleSize = XMLHandler.getTagValue(reservoirnode, "sample_size");
      m_randomSeed = XMLHandler.getTagValue(reservoirnode, "seed");
      
    }
  }

  /**
   * Read this step's configuration from a repository
   *
   * @param rep the repository to access
   * @param id_step the id for this step
   * @exception KettleException if an error occurs
   */
  public void readRep(Repository rep, 
                      long id_step, 
                      List<DatabaseMeta> databases, 
                      Map<String, Counter> counters) 
    throws KettleException {
    
    m_sampleSize = rep.getStepAttributeString(id_step, 0, "sample_size");
    m_randomSeed =  rep.getStepAttributeString(id_step, 0, "seed");
  }

  /**
   * Save this step's meta data to a repository
   *
   * @param rep the repository to save to
   * @param id_transformation transformation id
   * @param id_step step id
   * @exception KettleException if an error occurs
   */
  public void saveRep(Repository rep, 
                      long id_transformation, 
                      long id_step)
    throws KettleException {
    
    rep.saveStepAttribute(id_transformation, 
                          id_step, 0, 
                          "sample_size",
                          m_sampleSize);
    rep.saveStepAttribute(id_transformation,
                          id_step, 0,
                          "seed",
                          m_randomSeed);
  }

  /**
   * Generates row meta data to represent
   * the fields output by this step
   *
   * @param row the meta data for the output produced
   * @param origin the name of the step to be used as the origin
   * @param info The input rows metadata that enters the step through 
   * the specified channels in the same order as in method getInfoSteps(). 
   * The step metadata can then choose what to do with it: ignore it or not.
   * @param nextStep if this is a non-null value, it's the next step in 
   * the transformation. The one who's asking, the step where the data is 
   * targetted towards.
   * @param space not sure what this is :-)
   * @exception KettleStepException if an error occurs
   */
  public void getFields(RowMetaInterface row, 
                        String origin, 
                        RowMetaInterface[] info, 
                        StepMeta nextStep, 
                        VariableSpace space) 
    throws KettleStepException {
    
    // nothing to do, as no fields are added/deleted
  }

  /**
   * Check the settings of this step and put findings
   * in a remarks list.
   *
   * @param remarks the list to put the remarks in. 
   * see <code>org.pentaho.di.core.CheckResult</code>
   * @param transmeta the transform meta data
   * @param stepMeta the step meta data
   * @param prev the fields coming from a previous step
   * @param input the input step names
   * @param output the output step names
   * @param info the fields that are used as information by the step
   */
  public void check(List<CheckResultInterface> remarks, 
                    TransMeta transmeta,
                    StepMeta stepMeta, 
                    RowMetaInterface prev, 
                    String[] input, 
                    String[] output,
                    RowMetaInterface info) {

    CheckResult cr;

    if ((prev == null) || (prev.size() == 0)) {
      cr = new CheckResult(CheckResult.TYPE_RESULT_WARNING,
          "Not receiving any fields from previous steps!", stepMeta);
      remarks.add(cr);
    } else {
      cr = new CheckResult(CheckResult.TYPE_RESULT_OK,
          "Step is connected to previous one, receiving " + prev.size() +
          " fields", stepMeta);
      remarks.add(cr);
    }

    // See if we have input streams leading to this step!
    if (input.length > 0) {
      cr = new CheckResult(CheckResult.TYPE_RESULT_OK,
          "Step is receiving info from other steps.", stepMeta);
      remarks.add(cr);
    } else {
      cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR,
          "No input received from other steps!", stepMeta);
      remarks.add(cr);
    }
  }

   /**
   * Get the executing step, needed by Trans to launch a step.
   *
   * @param stepMeta the step info
   * @param stepDataInterface the step data interface linked 
   * to this step. Here the step can store temporary data, 
   * database connections, etc.
   * @param cnr the copy number to get.
   * @param tr the transformation info.
   * @param trans the launching transformation
   * @return a <code>StepInterface</code> value
   */
  public StepInterface getStep(StepMeta stepMeta, 
                               StepDataInterface stepDataInterface, 
                               int cnr, 
                               TransMeta tr, 
                               Trans trans) {
    return new ReservoirSampling(stepMeta, stepDataInterface, cnr, tr, trans);
  }

  /**
   * Get a new instance of the appropriate data class. This 
   * data class implements the StepDataInterface. It basically 
   * contains the persisting data that needs to live on, even 
   * if a worker thread is terminated.
   *
   * @return a <code>StepDataInterface</code> value
   */
  public StepDataInterface getStepData() {
    return new ReservoirSamplingData();
  }
}