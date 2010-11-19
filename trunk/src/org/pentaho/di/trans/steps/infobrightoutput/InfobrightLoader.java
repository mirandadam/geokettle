package org.pentaho.di.trans.steps.infobrightoutput;

import java.io.IOException;
import java.sql.SQLException;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

/**
 * Uses named pipe capability to load Kettle-sourced data into
 * an Infobright table.
 * 
 * @author geoffrey.falk@infobright.com
 */
public class InfobrightLoader extends BaseStep implements StepInterface {
  
  private final KettleRecordPopulator populator;
  
  private InfobrightLoaderMeta meta;
  private InfobrightLoaderData data;
  
  private boolean triedToClosePipe = false;

  /**
   * Standard constructor.  Does nothing special.
   * 
   * @param stepMeta
   * @param stepDataInterface
   * @param copyNr
   * @param transMeta
   * @param trans
   */
  public InfobrightLoader(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans) {
    super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
    WindowsJNILibraryUtil.fixJavaLibraryPath(); // TODO move to Windows-specific class
    populator = new KettleRecordPopulator();
  }

  /** {@inheritDoc}
   * @see org.pentaho.di.trans.step.StepInterface#processRow(org.pentaho.di.trans.step.StepMetaInterface, org.pentaho.di.trans.step.StepDataInterface)
   */
  public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException {
    meta = (InfobrightLoaderMeta) smi;
    data = (InfobrightLoaderData) sdi;

    Object[] row = getRow();
    
    // no more input to be expected...
    if (row == null) {
      setOutputDone();
      return false;
    }
    if (first) {
      first = false;
      data.outputRowMeta = getInputRowMeta().clone();
      meta.getFields(data.outputRowMeta, getStepname(), null, null, this);
      data.insertRowMeta = getInputRowMeta().clone();
    }

    try {
      Object[] outputRowData = writeToLoader(row, data.insertRowMeta);
      if (outputRowData != null) {
        putRow(data.outputRowMeta, row); // in case we want it go further...
        incrementLinesOutput();
      }

      if (checkFeedback(getLinesRead())) {
        if(log.isBasic()) {
          logBasic("linenr " + getLinesRead());
        }
      }
    } catch(Exception e) {
      logError("Because of an error, this step can't continue: " + e.getMessage());
      logError(Const.getStackTracker(e));
      setErrors(1);
      stopAll();
      setOutputDone();  // signal end to receiver(s)
      return false;
    }
    return true;
  }

  /** {@inheritDoc}
   * @see org.pentaho.di.trans.step.BaseStep#init(org.pentaho.di.trans.step.StepMetaInterface, org.pentaho.di.trans.step.StepDataInterface)
   */
  public boolean init(StepMetaInterface smi, StepDataInterface sdi) {
    boolean res = false;
    meta = (InfobrightLoaderMeta) smi;
    data = (InfobrightLoaderData) sdi;
    
    if (super.init(smi, sdi)) {
      try {
        data.databaseSetup(meta, this);
        res = true;

      } catch (Exception ex) {
        logError("An error occurred intialising this step", ex);
        logError(Const.getStackTracker(ex));
        stopAll();
        setErrors(1);
        return false;
      }
    }
    return res;
  }

  /**
   * Called by Kettle to start component once initialized.  All data processing is done inside
   * this loop. 
   */
  public void run() {
    try {
      logBasic(Messages.getString("InfobrightLoader.Log.StartingToRun"));
      while (processRow(meta, data) && !isStopped())
        {}
      closePipe();
    } catch (Exception e) {
      logError(Messages.getString("InfobrightLoader.Log.UnexpectedError") + " : " + e.toString());
      logError(Const.getStackTracker(e));
      setErrors(1);
      stopAll();
    } finally {
      safeClosePipe();
      dispose(meta, data);
      logSummary();
      markStop();
    }
  }

  /** {@inheritDoc}
   * @see org.pentaho.di.trans.step.BaseStep#stopRunning(org.pentaho.di.trans.step.StepMetaInterface, org.pentaho.di.trans.step.StepDataInterface)
   */
  @Override
  public void stopRunning(StepMetaInterface smi, StepDataInterface sdi) {
    if (data.loader != null) {
      logDebug("Trying to kill the loader statement...");
      try {
        data.loader.killQuery();
        logDebug("Loader statement killed.");
      } catch (SQLException sqle) {
        logError(Messages.getString("InfobrightLoader.Log.FailedToKillQuery") + " : " + sqle.toString());
        logError(Const.getStackTracker(sqle));
      }
    }
  }
  
  private synchronized void closePipe() throws KettleException {
    try {
      if (data != null) {
        data.dispose(); // gtf: OutputStream gets closed here
      }
    } catch (Exception e) {    
      throw new KettleException(e); // FIX FOR IB TICKET #390822
    } finally {
      triedToClosePipe = true;     
    }
  }
  
  private synchronized void safeClosePipe() {
    if (!triedToClosePipe) {
      try {
        closePipe();
      } catch (Exception e) {
        logError(Messages.getString("InfobrightLoader.Log.UnexpectedError") + " : " + e.toString());
        logError(Const.getStackTracker(e));
      } finally {
        triedToClosePipe = true;
      }
    }
  }
  
  private Object[] writeToLoader(Object[] row, RowMetaInterface rowMeta) throws KettleException {

    Object[] outputRowData = row; // TODO set to null if there's an error
    try {
      populator.populate(data.record, row, rowMeta);
      data.record.writeTo(data.loader.getOutputStream());
      logRowlevel("loading: ..."); // does it make sense to have this for binary format?
    } catch (IOException ex) {
      throw new KettleException(ex);
    }
    return outputRowData;
  }
}
