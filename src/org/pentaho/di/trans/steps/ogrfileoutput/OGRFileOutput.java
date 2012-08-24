package org.pentaho.di.trans.steps.ogrfileoutput;

import java.io.IOException;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.geospatial.OGRWriter;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.ogrfileoutput.Messages;

/**
 * Write data to an OGR data destination.
 * 
 * @author tbadard
 * @since 11-jun-2010
 */
public class OGRFileOutput extends BaseStep implements StepInterface {
	private OGRFileOutputMeta meta;
	private OGRFileOutputData data;

	public OGRFileOutput(StepMeta stepMeta,StepDataInterface stepDataInterface, int copyNr,TransMeta transMeta, Trans trans) 
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}

	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi)throws KettleException 
	{
		meta = (OGRFileOutputMeta) smi;
		data = (OGRFileOutputData) sdi;

		Object[] r = getRow(); // this also waits for a previous step to be
		// finished.
		if (r == null) // no more input to be expected...
		{
			try 
			{
				//data.ogrWriter.write();
				setOutputDone();
				return false;
			} 
			catch (Exception e) 
			{
				logError("Because of an error, this step can't continue: ", e);
				setErrors(1);
				stopAll();
				setOutputDone(); // signal end to receiver(s)
				return false;
			}
			finally {
				data.ogrWriter.close();
			}
		}

		if (first) 
		{
			first = false;
			data.outputRowMeta = getInputRowMeta().clone();

			try 
			{
				data.ogrWriter.createLayer(data.outputRowMeta);
			} 
			catch (Exception e) 
			{
				logError("Because of an error, this step can't continue: ", e);
				data.ogrWriter.close();
				setErrors(1);
				stopAll();
				setOutputDone(); // signal end to receiver(s)
				return false;
			}
		}

		try 
		{
			data.ogrWriter.putRow(r,data.outputRowMeta);
			incrementLinesOutput();
		} 
		catch (Exception e) 
		{
			logError("Because of an error, this step can't continue: ", e);
			data.ogrWriter.close();
			setErrors(1);
			stopAll();
			setOutputDone(); // signal end to receiver(s)
			return false;
		}
		return true;
	}

	public boolean init(StepMetaInterface smi, StepDataInterface sdi) {
		meta = (OGRFileOutputMeta) smi;
		data = (OGRFileOutputData) sdi;

		if (super.init(smi, sdi)) {

			try 
			{
				if (meta.getGisFileName()!=null)
					data.file_gis = KettleVFS.getFileObject(this.environmentSubstitute(meta.getGisFileName()));
				else
					data.file_gis = null;

				data.file_format = meta.getOgrOutputFormat();
				data.file_options = meta.getOgrOptions();
				data.file_geomtype = meta.getOgrGeomType();
				data.connectionString = meta.getConnectionString();
				data.layerName = meta.getLayerName();
				data.write_mode = meta.getOgrWriteMode();
				data.fid_field = meta.getOgrFIDField();
				data.preserve_fid_field = meta.isPreserveFIDField();

			} 			
			catch (IOException e) 
			{
				logError("IOException occured in OGRFileOutput.init():", e);
				return false;
			}

			return true;
		}
		return false;
	}

	private void openNextFile() throws KettleException {

		try 
		{

			if (data.file_gis!=null) {

				String ogr_path = data.file_gis.getURL().getPath();
				if (Const.isWindows()) {
					data.ogrWriter = new OGRWriter(ogr_path.substring(3).replace('/', '\\'),true,data.file_format,data.file_options,data.file_geomtype,data.layerName,data.write_mode,data.fid_field,data.preserve_fid_field);
				} else {
					data.ogrWriter = new OGRWriter(ogr_path,true,data.file_format,data.file_options,data.file_geomtype,data.layerName,data.write_mode,data.fid_field,data.preserve_fid_field);
				}
			}

			if (data.connectionString!=null && !(data.connectionString.trim().equals(""))) {
				data.ogrWriter = new OGRWriter(data.connectionString,false,data.file_format,data.file_options,data.file_geomtype,data.layerName,data.write_mode,data.fid_field,data.preserve_fid_field);
			}

			data.ogrWriter.open();

			logBasic(Messages.getString("OGRFileOutput.Log.OpenedGISFile") + " : [" + data.ogrWriter + "]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$	     		        	

		} 
		catch (Exception e) 
		{
			logError(Messages.getString("OGRFileOutput.Log.Error.CouldNotOpenGISFile1") + data.file_gis + Messages.getString("XBaseOutput.Log.Error.CouldNotOpenXBaseFile2") + e.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
			throw new KettleException(e);
		}
	}

	public void dispose(StepMetaInterface smi, StepDataInterface sdi)
	{
		closeLastFile();
		super.dispose(smi, sdi);
	}

	private void closeLastFile() 
	{
		logBasic(Messages.getString("OGRFileOutput.Log.FinishedReadingRecords")); //$NON-NLS-1$
		data.ogrWriter.close();
	}

	// Run is were the action happens!
	public void run() 
	{
		try 
		{
			logBasic(Messages.getString("OGRFileOutput.Log.StartingToRun")); //$NON-NLS-1$
			openNextFile();
			while (!isStopped() && processRow(meta, data));
		} 
		catch (Exception e) 
		{
			logError(Messages.getString("OGRFileOutput.Log.Error.UnexpectedError") + " : " + e.toString()); //$NON-NLS-1$ //$NON-NLS-2$
			logError(Const.getStackTracker(e));
			setErrors(1);
			stopAll();
		} 
		finally 
		{
			dispose(meta, data);
			markStop();
			logSummary();
		}
	}
}
