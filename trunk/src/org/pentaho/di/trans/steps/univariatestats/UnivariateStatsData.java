/*
 *    This program is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU Lesser General Public License (LGPL) as 
 *    published by the Free Software Foundation; either version 2 of the 
 *    License, or (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public 
 *    License and along with this program; if not, write to the Free 
 *    Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

/*
 *    UnivariateStatsData.java
 *    Copyright 2007 Pentaho Corporation.  All rights reserved. 
 *
 */

package org.pentaho.di.trans.steps.univariatestats;

import java.util.ArrayList;
import java.util.Arrays;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;


/**
 * Holds temporary data and has routines for computing
 * derived statistics.
 *
 * @author Mark Hall (mhall{[at]}pentaho.org)
 * @version 1.0
 */
public class UnivariateStatsData extends BaseStepData 
  implements StepDataInterface {

  // this class contains intermediate results,
  // info about the input format, derived output
  // format etc.

  // the input data format
  protected RowMetaInterface m_inputRowMeta;
  
  // the output data format
  protected RowMetaInterface m_outputRowMeta;

  /**
   * Inner class used to hold operating field index,
   * intermediate data and final results for a stats calculation.
   *
   * Has functions to compute the mean, standard deviation
   * and arbitrary percentiles. Percentiles can be computed
   * using interpolation or a simple method. See 
   * <a href="http://www.itl.nist.gov/div898/handbook/prc/section2/prc252.htm">
   * The Engineering Statistics Handbook</a> for details.
   */
  public static class FieldIndex {
    public int m_columnIndex;
    public double m_count;
    public double m_mean;
    public double m_stdDev;
    public double m_max;
    public double m_min;
    public double m_median;
    public double m_arbitraryPercentile;
    public double m_sum;
    public double m_sumSq;

    protected void calculateDerived() {
      m_mean = Double.NaN;
      m_stdDev = Double.NaN;
      if (m_count > 0) {
        m_mean = m_sum / m_count;
        m_stdDev = Double.POSITIVE_INFINITY;
        if (m_count > 1) {
          m_stdDev = m_sumSq - 
            (m_sum * m_sum) / m_count;
          m_stdDev /= (m_count - 1);
          if (m_stdDev < 0) {
            // round to zero
            m_stdDev = 0;
          }
          m_stdDev = Math.sqrt(m_stdDev);
        }
      }
    }

    /**
     * Compute a percentile. Can compute percentiles
     * using interpolation or a simple method
     * (see <a href="http://www.itl.nist.gov/div898/handbook/prc/section2/prc252.htm"
     * The Engineering Statistics Handbook</a> for details).
     * 
     *
     * @param p the percentile to compute (0 <= p <= 1)
     * @param vals a sorted array of values to compute the percentile
     * from
     * @param interpolate true if interpolation is to be used
     * @return the percentile value
     */
    private double percentile(double p, double [] vals,
                              boolean interpolate) {
      double n = m_count;

      // interpolation
      if (interpolate) {
        double i = p * (n + 1);
        // special cases
        if (i <= 1) {
          return m_min;
        }
        if (i >= n) {
        return m_max;
        }
        double low_obs = Math.floor(i);
        double high_obs = low_obs + 1;
        
        double r1 = high_obs - i;
        double r2 = 1.0 - r1;
        
        double x1 = vals[(int)low_obs - 1];
        double x2 = vals[(int)high_obs - 1];
        
        return (r1 * x1) + (r2 * x2);
      }

      // simple method
      double i = p * n;
      double res = 0;
      if (i == 0) {
        return m_min;
      }
      if (i == n) {
        return m_max;
      }
      if (i - Math.floor(i) > 0) {
        i = Math.floor(i);
        res = vals[(int)i];
      } else {
        res = (vals[(int)(i - 1)] + vals[(int)i]) / 2.0;
      }
      return res;
    }

    /**
     * Constructs an array of Objects containing the requested
     * statistics for one univariate stats meta function using
     * this <code>FieldIndex</code>.
     *
     * @param usmf the<code>UnivariateStatsMetaFunction</code> to
     * compute stats for. This contains the input field selected
     * by the user along with which stats to compute for it.
     * @return an array of computed statistics
     */
    public Object [] generateOutputValues(UnivariateStatsMetaFunction usmf,
                                          ArrayList<Number> cache) {
      calculateDerived();

      // process cache?
      if (cache != null) {
        double [] result = new double[(int)m_count];
        for (int i = 0; i < cache.size(); i++) {
          result[i] = cache.get(i).doubleValue();
        }
        Arrays.sort(result);

        if (usmf.getCalcMedian()) {
          m_median = percentile(0.5, result, 
                                usmf.getInterpolatePercentile());
        }

        if (usmf.getCalcPercentile() >= 0) {
          m_arbitraryPercentile = 
            percentile(usmf.getCalcPercentile(), result, 
                       usmf.getInterpolatePercentile());
        }
      }

      Object [] result = new Object[usmf.numberOfMetricsRequested()];      

      int index = 0;
      if (usmf.getCalcN()) {
        result[index++] = new Double(m_count);
      }
      if (usmf.getCalcMean()) {
        result[index++] = new Double(m_mean);
      }
      if (usmf.getCalcStdDev()) {
        result[index++] = new Double(m_stdDev);
      }
      if (usmf.getCalcMin()) {
        result[index++] = new Double(m_min);
      }
      if (usmf.getCalcMax()) {
        result[index++] = new Double(m_max);
      }
      if (usmf.getCalcMedian()) {
        result[index++] = new Double(m_median);
      }
      if (usmf.getCalcPercentile() >= 0) {
        result[index++] = new Double(m_arbitraryPercentile);
      }
      return result;
    }
  }

  /** 
   * contains the FieldIndexs - one for each 
   * UnivariateStatsMetaFunction
   */
  protected FieldIndex [] m_indexes;
      
  /**
   * Creates a new <code>UnivariateStatsData</code> instance.
   */
  public UnivariateStatsData() {
    super();
  }

  /**
   * Set the FieldIndexes
   *
   * @param fis a <code>FieldIndex[]</code> value
   */
  public void setFieldIndexes(FieldIndex [] fis) {
    m_indexes = fis;
  }

  /**
   * Get the fieldIndexes
   *
   * @return a <code>FieldIndex[]</code> value
   */
  public FieldIndex [] getFieldIndexes() {
    return m_indexes;
  }

  /**
   * Get the meta data for the input format
   *
   * @return a <code>RowMetaInterface</code> value
   */
  public RowMetaInterface getInputRowMeta() {
    return m_inputRowMeta;
  }

  /**
   * Save the meta data for the input format.
   * (I'm not sure that this is really needed)
   *
   * @param rmi a <code>RowMetaInterface</code> value
   */
  public void setInputRowMeta(RowMetaInterface rmi) {
    m_inputRowMeta = rmi;
  }

  /**
   * Get the meta data for the output format
   *
   * @return a <code>RowMetaInterface</code> value
   */
  public RowMetaInterface getOutputRowMeta() {
    return m_outputRowMeta;
  }

  /**
   * Set the meta data for the output format
   *
   * @param rmi a <code>RowMetaInterface</code> value
   */
  public void setOutputRowMeta(RowMetaInterface rmi) {
    m_outputRowMeta = rmi;
  }
}
