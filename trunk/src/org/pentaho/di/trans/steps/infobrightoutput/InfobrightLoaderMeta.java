package org.pentaho.di.trans.steps.infobrightoutput;

import java.util.List;
import java.util.Map;

import org.pentaho.di.core.Counter;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.tableoutput.TableOutputMeta;
import org.w3c.dom.Node;

import com.infobright.etl.model.DataFormat;


/**
 * Metadata for the Infobright loader.
 *
 * @author geoffrey.falk@infobright.com
 */
public class InfobrightLoaderMeta extends TableOutputMeta implements StepMetaInterface {

  private DataFormat dataFormat;
  private boolean rejectErrors = false;
  
  /**
   * Default constructor.
   */
  public InfobrightLoaderMeta()
  {
    super();
    setIgnoreErrors(false);
    setTruncateTable(false);
  }

  /** {@inheritDoc}
   * @see org.pentaho.di.trans.step.StepMetaInterface#getStep(org.pentaho.di.trans.step.StepMeta, org.pentaho.di.trans.step.StepDataInterface, int, org.pentaho.di.trans.TransMeta, org.pentaho.di.trans.Trans)
   */
  public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta tr, Trans trans)
  {
    InfobrightLoader loader = new InfobrightLoader(stepMeta, stepDataInterface, cnr, tr, trans);
    return loader;
  }
  
  /** {@inheritDoc}
   * @see org.pentaho.di.trans.step.StepMetaInterface#getStepData()
   */
  public StepDataInterface getStepData()
  {
    return new InfobrightLoaderData();
  }

  /** {@inheritDoc}
   * @see org.pentaho.di.trans.step.BaseStepMeta#clone()
   */
  public Object clone()
  {
    InfobrightLoaderMeta retval = (InfobrightLoaderMeta) super.clone();
    return retval;
  }

  public String getInfobrightProductType() {
    return dataFormat.getDisplayText();
  }

  public void setDataFormat(DataFormat dataFormat) {
    this.dataFormat = dataFormat;
  }
  
  public void setDefault() {
    this.dataFormat = DataFormat.TXT_VARIABLE; // default for ICE
    // this.dataFormat = DataFormat.BINARY; // default for IEE
  }

  @Override
  public String getXML() {
    String ret = super.getXML();
    return ret + new String("    "+XMLHandler.addTagValue("data_format", dataFormat.toString()));
  }

  //@SuppressWarnings("unchecked")
  @Override
  public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters)
      throws KettleXMLException {
    super.loadXML(stepnode, databases, counters);
    dataFormat = Enum.valueOf(DataFormat.class, XMLHandler.getTagValue(stepnode, "data_format"));
  }
  
  /** @return the rejectErrors */
  public boolean isRejectErrors() {
    return rejectErrors;
  }

  /** @param rejectErrors the rejectErrors to set. */
  public void setRejectErrors(boolean rejectErrors) {
    this.rejectErrors = rejectErrors;
  }
}
