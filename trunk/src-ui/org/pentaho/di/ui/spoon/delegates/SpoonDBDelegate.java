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
package org.pentaho.di.ui.spoon.delegates;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.DBCache;
import org.pentaho.di.core.NotePadMeta;
import org.pentaho.di.core.SQLStatement;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.gui.UndoInterface;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryUtil;
import org.pentaho.di.trans.HasDatabasesInterface;
import org.pentaho.di.trans.StepLoader;
import org.pentaho.di.trans.TransHopMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.selectvalues.SelectValuesMeta;
import org.pentaho.di.trans.steps.tableinput.TableInputMeta;
import org.pentaho.di.trans.steps.tableoutput.TableOutputMeta;
import org.pentaho.di.ui.core.database.dialog.DatabaseDialog;
import org.pentaho.di.ui.core.database.dialog.DatabaseExplorerDialog;
import org.pentaho.di.ui.core.database.dialog.SQLEditor;
import org.pentaho.di.ui.core.database.dialog.XulDatabaseDialog;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.dialog.SQLStatementsDialog;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.spoon.Messages;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.spoon.dialog.GetJobSQLProgressDialog;
import org.pentaho.di.ui.spoon.dialog.GetSQLProgressDialog;

public class SpoonDBDelegate extends SpoonDelegate
{
	public SpoonDBDelegate(Spoon spoon)
	{
		super(spoon);
	}

	public void sqlConnection(DatabaseMeta databaseMeta)
	{
		SQLEditor sql = new SQLEditor(spoon.getShell(), SWT.NONE, databaseMeta, DBCache.getInstance(), "");
		sql.open();
	}

	public void editConnection(DatabaseMeta databaseMeta)
	{
		HasDatabasesInterface hasDatabasesInterface = spoon.getActiveHasDatabasesInterface();
		if (hasDatabasesInterface == null)
			return; // program error, exit just to make sure.

		DatabaseMeta before = (DatabaseMeta) databaseMeta.clone();

		// DatabaseDialog con = new DatabaseDialog(spoon.getShell(), databaseMeta);
    XulDatabaseDialog con = new XulDatabaseDialog(spoon.getShell(), databaseMeta);
		con.setDatabases(hasDatabasesInterface.getDatabases());
		String newname = con.open();
		if (!Const.isEmpty(newname)) // null: CANCEL
		{
		  databaseMeta = con.getDatabaseMeta();
			// newname =
			// db.verifyAndModifyDatabaseName(transMeta.getDatabases(), name);

			// Store undo/redo information
			DatabaseMeta after = (DatabaseMeta) databaseMeta.clone();
			spoon.addUndoChange((UndoInterface) hasDatabasesInterface, new DatabaseMeta[] { before },
					new DatabaseMeta[] { after }, new int[] { hasDatabasesInterface
							.indexOfDatabase(databaseMeta) });

			saveConnection(databaseMeta);

			spoon.refreshTree();
		}
		spoon.setShellText();
	}

	public void dupeConnection(HasDatabasesInterface hasDatabasesInterface, DatabaseMeta databaseMeta)
	{
		String name = databaseMeta.getName();
		int pos = hasDatabasesInterface.indexOfDatabase(databaseMeta);
		if (databaseMeta != null)
		{
			DatabaseMeta databaseMetaCopy = (DatabaseMeta) databaseMeta.clone();
			String dupename = Messages.getString("Spoon.Various.DupeName") + name; // "(copy
			// of)
			// "
			databaseMetaCopy.setName(dupename);

			DatabaseDialog con = new DatabaseDialog(spoon.getShell(), databaseMetaCopy);
			String newname = con.open();
			if (newname != null) // null: CANCEL
			{
				databaseMetaCopy.verifyAndModifyDatabaseName(hasDatabasesInterface.getDatabases(), name);
				hasDatabasesInterface.addDatabase(pos + 1, databaseMetaCopy);
				spoon
						.addUndoNew((UndoInterface) hasDatabasesInterface,
								new DatabaseMeta[] { (DatabaseMeta) databaseMetaCopy.clone() },
								new int[] { pos + 1 });
				saveConnection(databaseMetaCopy);
				spoon.refreshTree();
			}
		}
	}

	public void clipConnection(DatabaseMeta databaseMeta)
	{
		String xml = XMLHandler.getXMLHeader() + databaseMeta.getXML();
		GUIResource.getInstance().toClipboard(xml);
	}

	/**
	 * Delete a database connection
	 * 
	 * @param name
	 *            The name of the database connection.
	 */
	public void delConnection(HasDatabasesInterface hasDatabasesInterface, DatabaseMeta db)
	{
		int pos = hasDatabasesInterface.indexOfDatabase(db);
		boolean worked = false;

		// delete from repository?
		Repository rep = spoon.getRepository();
		if (rep != null)
		{
			if (!rep.getUserInfo().isReadonly())
			{
				try
				{
					long id_database = rep.getDatabaseID(db.getName());
					rep.delDatabase(id_database);

					worked = true;
				} catch (KettleException dbe)
				{

					new ErrorDialog(spoon.getShell(), Messages.getString("Spoon.Dialog.ErrorDeletingConnection.Title"), Messages.getString("Spoon.Dialog.ErrorDeletingConnection.Message", db.getName()), dbe);// "Error
					// deleting
					// connection
					// ["+db+"]
					// from
					// repository!"
				}
			} else
			{
				new ErrorDialog(spoon.getShell(), Messages.getString("Spoon.Dialog.ErrorDeletingConnection.Title"), 
						Messages.getString("Spoon.Dialog.ErrorDeletingConnection.Message", db.getName()), 
						new KettleException(Messages.getString("Spoon.Dialog.Exception.ReadOnlyUser")));// "Error
				// deleting
				// connection
				// ["+db+"]
				// from
				// repository!"
				// //This
				// user
				// is
				// read-only!
			}
		}

		if (spoon.getRepository() == null || worked)
		{
			spoon.addUndoDelete((UndoInterface) hasDatabasesInterface, new DatabaseMeta[] { (DatabaseMeta) db
					.clone() }, new int[] { pos });
			hasDatabasesInterface.removeDatabase(pos);
			DBCache.getInstance().clear(db.getName());  // remove this from the cache as well.
		}

		spoon.refreshTree();
		spoon.setShellText();
	}

	public void exploreDB(DatabaseMeta databaseMeta)
	{
		List<DatabaseMeta> databases = null;
		HasDatabasesInterface activeHasDatabasesInterface = spoon.getActiveHasDatabasesInterface();
		if (activeHasDatabasesInterface != null)
			databases = activeHasDatabasesInterface.getDatabases();

		DatabaseExplorerDialog std = new DatabaseExplorerDialog(spoon.getShell(), SWT.NONE, databaseMeta,
				databases, true);
		std.open();
	}

	public void clearDBCache(DatabaseMeta databaseMeta)
	{
		if (databaseMeta != null)
		{
			DBCache.getInstance().clear(databaseMeta.getName());
		} else
		{
			DBCache.getInstance().clear(null);
		}
	}

	public void getSQL()
	{
		TransMeta transMeta = spoon.getActiveTransformation();
		if (transMeta != null)
			getTransSQL(transMeta);
		JobMeta jobMeta = spoon.getActiveJob();
		if (jobMeta != null)
			getJobSQL(jobMeta);
	}

	/**
	 * Get & show the SQL required to run the loaded transformation...
	 * 
	 */
	public void getTransSQL(TransMeta transMeta)
	{
		GetSQLProgressDialog pspd = new GetSQLProgressDialog(spoon.getShell(), transMeta);
		List<SQLStatement> stats = pspd.open();
		if (stats != null) // null means error, but we already displayed the
		// error
		{
			if (stats.size() > 0)
			{
				SQLStatementsDialog ssd = new SQLStatementsDialog(spoon.getShell(), Variables
						.getADefaultVariableSpace(), SWT.NONE, stats);
				String sn = ssd.open();

	            if (sn != null)
	            {
	                StepMeta esi = transMeta.findStep(sn);
	                if (esi != null)
	                {
	                    spoon.delegates.steps.editStep(transMeta,esi);
	                }
	            }
			} else
			{
				MessageBox mb = new MessageBox(spoon.getShell(), SWT.OK | SWT.ICON_INFORMATION);
				mb.setMessage(Messages.getString("Spoon.Dialog.NoSQLNeedEexecuted.Message"));
				mb.setText(Messages.getString("Spoon.Dialog.NoSQLNeedEexecuted.Title"));// "SQL"
				mb.open();
			}
		}
	}

	/**
	 * Get & show the SQL required to run the loaded job entry...
	 * 
	 */
	public void getJobSQL(JobMeta jobMeta)
	{
		GetJobSQLProgressDialog pspd = new GetJobSQLProgressDialog(spoon.getShell(), jobMeta, spoon
				.getRepository());
		List<SQLStatement> stats = pspd.open();
		if (stats != null) // null means error, but we already displayed the
		// error
		{
			if (stats.size() > 0)
			{
				SQLStatementsDialog ssd = new SQLStatementsDialog(spoon.getShell(), (VariableSpace) jobMeta,
						SWT.NONE, stats);
				ssd.open();
			} else
			{
				MessageBox mb = new MessageBox(spoon.getShell(), SWT.OK | SWT.ICON_INFORMATION);
				mb.setMessage(Messages.getString("Spoon.Dialog.JobNoSQLNeedEexecuted.Message")); //$NON-NLS-1$
				mb.setText(Messages.getString("Spoon.Dialog.JobNoSQLNeedEexecuted.Title")); //$NON-NLS-1$
				mb.open();
			}
		}
	}

	public boolean copyTable(DatabaseMeta sourceDBInfo, DatabaseMeta targetDBInfo, String tablename)
	{
		try
		{
			//
			// Create a new transformation...
			//
			TransMeta meta = new TransMeta();
			meta.addDatabase(sourceDBInfo);
			meta.addDatabase(targetDBInfo);

			//
			// Add a note
			//
			String note = Messages.getString("Spoon.Message.Note.ReadInformationFromTableOnDB", tablename,
					sourceDBInfo.getDatabaseName())
					+ Const.CR;// "Reads information from table ["+tablename+"]
			// on database ["+sourceDBInfo+"]"
			note += Messages.getString("Spoon.Message.Note.WriteInformationToTableOnDB", tablename,
					targetDBInfo.getDatabaseName());// "After that, it writes
			// the information to table
			// ["+tablename+"] on
			// database
			// ["+targetDBInfo+"]"
			NotePadMeta ni = new NotePadMeta(note, 150, 10, -1, -1);
			meta.addNote(ni);

			// 
			// create the source step...
			//
			String fromstepname = Messages.getString("Spoon.Message.Note.ReadFromTable", tablename); // "read
			// from
			// ["+tablename+"]";
			TableInputMeta tii = new TableInputMeta();
			tii.setDatabaseMeta(sourceDBInfo);
			tii.setSQL("SELECT * FROM " + tablename);

			StepLoader steploader = StepLoader.getInstance();

			String fromstepid = steploader.getStepPluginID(tii);
			StepMeta fromstep = new StepMeta(fromstepid, fromstepname, tii);
			fromstep.setLocation(150, 100);
			fromstep.setDraw(true);
			fromstep.setDescription(Messages.getString("Spoon.Message.Note.ReadInformationFromTableOnDB",
					tablename, sourceDBInfo.getDatabaseName()));
			meta.addStep(fromstep);

			//
			// add logic to rename fields in case any of the field names contain
			// reserved words...
			// Use metadata logic in SelectValues, use SelectValueInfo...
			//
			Database sourceDB = new Database(sourceDBInfo);
			sourceDB.shareVariablesWith(meta);
			sourceDB.connect();

			// Get the fields for the input table...
			RowMetaInterface fields = sourceDB.getTableFields(tablename);

			// See if we need to deal with reserved words...
			int nrReserved = targetDBInfo.getNrReservedWords(fields);
			if (nrReserved > 0)
			{
				SelectValuesMeta svi = new SelectValuesMeta();
				svi.allocate(0, 0, nrReserved);
				int nr = 0;
				for (int i = 0; i < fields.size(); i++)
				{
					ValueMetaInterface v = fields.getValueMeta(i);
					if (targetDBInfo.isReservedWord(v.getName()))
					{
						svi.getMeta()[nr].setName(v.getName());
						svi.getMeta()[nr].setRename(targetDBInfo.quoteField(v.getName()));
						nr++;
					}
				}

				String selstepname = Messages.getString("Spoon.Message.Note.HandleReservedWords"); // "Handle
				// reserved
				// words";
				String selstepid = steploader.getStepPluginID(svi);
				StepMeta selstep = new StepMeta(selstepid, selstepname, svi);
				selstep.setLocation(350, 100);
				selstep.setDraw(true);
				selstep.setDescription(Messages.getString("Spoon.Message.Note.RenamesReservedWords",
						targetDBInfo.getDatabaseTypeDesc()));// "Renames
				// reserved
				// words for
				// "+targetDBInfo.getDatabaseTypeDesc()
				meta.addStep(selstep);

				TransHopMeta shi = new TransHopMeta(fromstep, selstep);
				meta.addTransHop(shi);
				fromstep = selstep;
			}

			// 
			// Create the target step...
			//
			//
			// Add the TableOutputMeta step...
			//
			String tostepname = Messages.getString("Spoon.Message.Note.WriteToTable", tablename);
			TableOutputMeta toi = new TableOutputMeta();
			toi.setDatabaseMeta(targetDBInfo);
			toi.setTablename(tablename);
			toi.setCommitSize(200);
			toi.setTruncateTable(true);

			String tostepid = steploader.getStepPluginID(toi);
			StepMeta tostep = new StepMeta(tostepid, tostepname, toi);
			tostep.setLocation(550, 100);
			tostep.setDraw(true);
			tostep.setDescription(Messages.getString("Spoon.Message.Note.WriteInformationToTableOnDB2",
					tablename, targetDBInfo.getDatabaseName()));
			meta.addStep(tostep);

			//
			// Add a hop between the two steps...
			//
			TransHopMeta hi = new TransHopMeta(fromstep, tostep);
			meta.addTransHop(hi);

			// OK, if we're still here: overwrite the current transformation...
			// Set a name on this generated transformation
			// 
			String name = "Copy table from [" + sourceDBInfo.getName() + "] to [" + targetDBInfo.getName()
					+ "]";
			String transName = name;
			int nr = 1;
			if (spoon.delegates.trans.getTransformation(transName) != null)
			{
				nr++;
				transName = name + " " + nr;
			}
			meta.setName(transName);
			spoon.delegates.trans.addTransGraph(meta);

			spoon.refreshGraph();
			spoon.refreshTree();
		} catch (Exception e)
		{
			new ErrorDialog(spoon.getShell(), Messages.getString("Spoon.Dialog.UnexpectedError.Title"),
					Messages.getString("Spoon.Dialog.UnexpectedError.Message"), new KettleException(e
							.getMessage(), e));
			return false;
		}
		return true;
	}

	public void saveConnection(DatabaseMeta db)
	{
		// Also add to repository?
		Repository rep = spoon.getRepository();

		if (rep != null)
		{
			if (!rep.userinfo.isReadonly())
			{
				try
				{
					rep.lockRepository();
					rep.insertLogEntry("Saving database '" + db.getName() + "'");

					RepositoryUtil.saveDatabaseMeta(db, rep);
					spoon.getLog().logDetailed(toString(),
							Messages.getString("Spoon.Log.SavedDatabaseConnection", db.getDatabaseName()));

					// Put a commit behind it!
					rep.commit();

					db.setChanged(false);
				} catch (KettleException ke)
				{
					rep.rollback(); // In case of failure: undo changes!
					new ErrorDialog(spoon.getShell(), Messages.getString("Spoon.Dialog.ErrorSavingConnection.Title"), Messages.getString("Spoon.Dialog.ErrorSavingConnection.Message", db.getDatabaseName()), ke);
				} 
				finally
				{
					try
					{
						rep.unlockRepository();
					} catch (KettleException e)
					{
						new ErrorDialog(spoon.getShell(), "Error",
								"Unexpected error unlocking the repository database", e);
					}

				}
			} else
			{
				new ErrorDialog(
						spoon.getShell(),
						Messages.getString("Spoon.Dialog.UnableSave.Title"),
						Messages.getString("Spoon.Dialog.ErrorSavingConnection.Message", db.getDatabaseName()),
						new KettleException(Messages.getString("Spoon.Dialog.Exception.ReadOnlyRepositoryUser")));// This
				// repository
				// user
				// is
				// read-only!
			}
		}
	}

	public void newConnection()
	{
		HasDatabasesInterface hasDatabasesInterface = spoon.getActiveHasDatabasesInterface();
		if (hasDatabasesInterface == null && spoon.rep==null)
		{
			return;
		}

		DatabaseMeta databaseMeta = new DatabaseMeta();
		if (hasDatabasesInterface instanceof VariableSpace) {
			databaseMeta.shareVariablesWith((VariableSpace)hasDatabasesInterface);
		}
		else {
			databaseMeta.initializeVariablesFrom(null);
		}
		
    // DatabaseDialog con = new DatabaseDialog(spoon.getShell(), databaseMeta);
    XulDatabaseDialog con = new XulDatabaseDialog(spoon.getShell(), databaseMeta);
    String con_name = con.open();
    if (!Const.isEmpty(con_name))
    {
      databaseMeta = con.getDatabaseMeta();
			if (hasDatabasesInterface!=null)
			{
				databaseMeta.verifyAndModifyDatabaseName(hasDatabasesInterface.getDatabases(), null);
				hasDatabasesInterface.addDatabase(databaseMeta);
				spoon.addUndoNew((UndoInterface) hasDatabasesInterface,
						new DatabaseMeta[] { (DatabaseMeta) databaseMeta.clone() },
						new int[] { hasDatabasesInterface.indexOfDatabase(databaseMeta) });
				saveConnection(databaseMeta);
				spoon.refreshTree();
			}
			else
			{
				// Save it in the repository...
				try 
				{
					if (!spoon.rep.userinfo.isReadonly())
					{
						RepositoryUtil.saveDatabaseMeta(databaseMeta,spoon.rep);
					}
					else
					{
						throw new KettleException(Messages.getString("Spoon.Dialog.Exception.ReadOnlyRepositoryUser"));
					}
				} 
				catch (KettleException e) 
				{
					new ErrorDialog(spoon.getShell(), Messages.getString("Spoon.Dialog.ErrorSavingConnection.Title"), Messages.getString("Spoon.Dialog.ErrorSavingConnection.Message", databaseMeta.getName()), e);
				}
			}
		}
	}

}
