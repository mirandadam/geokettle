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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.widgets.Composite;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.EngineMetaInterface;
import org.pentaho.di.core.gui.SpoonInterface;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.spoon.Messages;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.spoon.SpoonBrowser;
import org.pentaho.di.ui.spoon.TabItemInterface;
import org.pentaho.di.ui.spoon.TabMapEntry;
import org.pentaho.di.ui.spoon.job.JobGraph;
import org.pentaho.di.ui.spoon.trans.TransGraph;
import org.pentaho.ui.util.Launch;
import org.pentaho.ui.util.Launch.Status;
import org.pentaho.xul.swt.tab.TabItem;
import org.pentaho.xul.swt.tab.TabSet;

public class SpoonTabsDelegate extends SpoonDelegate
{
	/**
	 * This contains a list of the tab map entries
	 */
	private List<TabMapEntry> tabMap;

	public SpoonTabsDelegate(Spoon spoon)
	{
		super(spoon);
		tabMap = new ArrayList<TabMapEntry>();
	}

	public boolean tabClose(TabItem item)
	{
		// Try to find the tab-item that's being closed.
		List<TabMapEntry> collection = new ArrayList<TabMapEntry>();
		collection.addAll(tabMap);
		int idx = 0;
		
		boolean close = true;
		for (TabMapEntry entry : collection)
		{
			if (item.equals(entry.getTabItem()))
			{
				TabItemInterface itemInterface = entry.getObject();

				// Can we close this tab?
				if (!itemInterface.canBeClosed())
				{
					int reply = itemInterface.showChangedWarning();
					if (reply == SWT.YES)
					{
						close = itemInterface.applyChanges();
					} else
					{
						if (reply == SWT.CANCEL)
						{
							close = false;
						} else
						{
							close = true;
						}
					}
				}

				// Also clean up the log/history associated with this
				// transformation/job
				//                            
				if (close)
				{
					if (entry.getObject() instanceof TransGraph)
					{
						TransMeta transMeta = (TransMeta) entry.getObject().getManagedObject();
						spoon.delegates.trans.closeTransformation(transMeta);
						spoon.refreshTree();
						//spoon.refreshCoreObjects();
					} else if (entry.getObject() instanceof JobGraph)
					{
						JobMeta jobMeta = (JobMeta) entry.getObject().getManagedObject();
						spoon.delegates.jobs.closeJob(jobMeta);
						spoon.refreshTree();
						//spoon.refreshCoreObjects();
					} else if (entry.getObject() instanceof SpoonBrowser)
					{
						spoon.closeSpoonBrowser();
						spoon.refreshTree();
					}

					else if (entry.getObject() instanceof Composite)
					{
						Composite comp = (Composite) entry.getObject();
						if (comp != null && !comp.isDisposed())
							comp.dispose();
					}
				}
				
				break;
			}
			
			idx+=1;
		}
		
		return close;
	}

	public void removeTab(TabItem tabItem )
	{
		for (TabMapEntry tabMapEntry : getTabs()) {
			if (tabMapEntry.getTabItem().equals(tabItem)) {
				tabMap.remove(tabMapEntry);
				break;
			}
		}
		if (!tabItem.isDisposed()){
		  tabItem.dispose();
		}
	}
	
	public void removeTab(String tabText, int tabMapEntryType)
	{
	  TabItem tabItem = null;
		for (TabMapEntry tabMapEntry : getTabs()) {
			if (tabMapEntry.getObjectName().equals(tabText) && tabMapEntry.getObjectType()==tabMapEntryType) {
        tabItem = tabMapEntry.getTabItem();
				tabMap.remove(tabMapEntry);
				break;
			}
		}

		if(tabItem != null){
	    if (!tabItem.isDisposed()){
	      tabItem.dispose();
	    }
		}

	}

	public List<TabMapEntry> getTabs()
	{
		List<TabMapEntry> list = new ArrayList<TabMapEntry>();
		list.addAll(tabMap);
		return list;
	}

	public TabMapEntry getTab(TabItem tabItem)
	{
		for (TabMapEntry tabMapEntry : tabMap) {
			if (tabMapEntry.getTabItem().equals(tabItem)) {
				return tabMapEntry;
			}
		}
		return null;
	}

	public EngineMetaInterface getActiveMeta()
	{
		TabSet tabfolder = spoon.tabfolder;
		if (tabfolder == null)
			return null;
		TabItem tabItem = tabfolder.getSelected();
		if (tabItem == null)
			return null;

		// What transformation is in the active tab?
		// TransLog, TransGraph & TransHist contain the same transformation
		//
		TabMapEntry mapEntry = getTab(tabfolder.getSelected());
		EngineMetaInterface meta = null;
		if (mapEntry != null)
		{
			if (mapEntry.getObject() instanceof TransGraph)
				meta = (mapEntry.getObject()).getMeta();
			if (mapEntry.getObject() instanceof JobGraph)
				meta = (mapEntry.getObject()).getMeta();
		}

		return meta;
	}

	public String makeLogTabName(TransMeta transMeta)
	{
		return Messages.getString("Spoon.Title.LogTransView", makeTransGraphTabName(transMeta));
	}

	public String makeHistoryTabName(TransMeta transMeta)
	{
		return Messages.getString("Spoon.Title.LogTransHistoryView", makeTransGraphTabName(transMeta));
	}

	public String makeSlaveTabName(SlaveServer slaveServer)
	{
		return "Slave server: " + slaveServer.getName();
	}
	
	public boolean addSpoonBrowser(String name, String urlString)
	{
		return addSpoonBrowser(name,urlString,true);
	}
	
	public boolean addSpoonBrowser(String name, String urlString,boolean isURL)
	{
		TabSet tabfolder = spoon.tabfolder;

		try
		{
			// OK, now we have the HTML, create a new browset tab.

			// See if there already is a tab for this browser
			// If no, add it
			// If yes, select that tab
			//
			TabItem tabItem = findTabItem(name, TabMapEntry.OBJECT_TYPE_BROWSER);
			if (tabItem == null)
			{
				CTabFolder cTabFolder = tabfolder.getSwtTabset();
				SpoonBrowser browser = new SpoonBrowser(cTabFolder, spoon, urlString,isURL);
				tabItem = new TabItem(tabfolder, name, name);
				tabItem.setImage(GUIResource.getInstance().getImageLogoSmall());
				tabItem.setControl(browser.getComposite());

				tabMap.add(new TabMapEntry(tabItem, name, browser, TabMapEntry.OBJECT_TYPE_BROWSER));
			}
			int idx = tabfolder.indexOf(tabItem);

			// keep the focus on the graph
			tabfolder.setSelected(idx);
			return true;
		} 
		catch (Throwable e)
		{
			boolean ok = false;
			if (isURL) {
				// Retry to show the welcome page in an external browser.
				//
				Status status = Launch.openURL(urlString);
				ok = status.equals(Status.Success);
			}
			if (!ok) {
				// Log an error
				//
				LogWriter.getInstance().logError(spoon.toString(), "Unable to open browser tab", e);
				return false;
			} else {
				return true;
			}
		}
	}

	public TabItem findTabItem(String tabItemText, int objectType)
	{
		for (TabMapEntry entry : tabMap)
		{
			if (entry.getTabItem().isDisposed())
				continue;
			if (objectType == entry.getObjectType()
					&& entry.getTabItem().getText().equalsIgnoreCase(tabItemText))
			{
				return entry.getTabItem();
			}
		}
		return null;
	}

	/**
	 * Rename the tabs
	 */
	public void renameTabs()
	{
		List<TabMapEntry> list = new ArrayList<TabMapEntry>(tabMap);
		for (TabMapEntry entry : list)
		{
			if (entry.getTabItem().isDisposed())
			{
				// this should not be in the map, get rid of it.
				tabMap.remove(entry.getObjectName());
				continue;
			}

			TabItem before = entry.getTabItem();
			// PDI-1683: need to get the String here, otherwise using only the "before" instance below, the reference gets changed and result is always the same
			String beforeText=before.getText();
			Object managedObject = entry.getObject().getManagedObject();
			if (managedObject != null)
			{

				if (entry.getObject() instanceof TransGraph)
				{
					TransMeta transMeta = (TransMeta) managedObject;
					entry.getTabItem().setText(makeTransGraphTabName(transMeta));
					String toolTipText = Messages.getString("Spoon.TabTrans.Tooltip", makeTransGraphTabName(transMeta));
					if (Const.isWindows() && !Const.isEmpty(transMeta.getFilename())) toolTipText+=Const.CR+Const.CR+transMeta.getFilename();
					entry.getTabItem().setToolTipText(toolTipText);
				} 
				else if (entry.getObject() instanceof JobGraph)
				{
					JobMeta jobMeta = (JobMeta) managedObject;
					entry.getTabItem().setText(makeJobGraphTabName(jobMeta));
					String toolTipText = Messages.getString("Spoon.TabJob.Tooltip", makeJobGraphTabName(jobMeta));
					if (Const.isWindows() && !Const.isEmpty(jobMeta.getFilename())) toolTipText+=Const.CR+Const.CR+jobMeta.getFilename();
					entry.getTabItem().setToolTipText(toolTipText);
				}
			}

			String after = entry.getTabItem().getText();

			if (!beforeText.equals(after)) // PDI-1683, could be improved to rename all the time
			{
				entry.setObjectName(after);

				// Also change the transformation map
				if (entry.getObject() instanceof TransGraph)
				{
					spoon.delegates.trans.removeTransformation(beforeText);
					spoon.delegates.trans.addTransformation(after, (TransMeta) entry.getObject().getManagedObject());
				}
				// Also change the job map
				if (entry.getObject() instanceof JobGraph)
				{
					spoon.delegates.jobs.removeJob(beforeText);
					spoon.delegates.jobs.addJob(after, (JobMeta) entry.getObject().getManagedObject());
				}
			}
		}
		spoon.setShellText();
	}

	public void addTab(TabMapEntry entry)
	{
		tabMap.add(entry);
	}

	public String makeTransGraphTabName(TransMeta transMeta)
	{
		if (Const.isEmpty(transMeta.getName()) && Const.isEmpty(transMeta.getFilename()))
			return Spoon.STRING_TRANS_NO_NAME;

		if (Const.isEmpty(transMeta.getName())
				|| spoon.delegates.trans.isDefaultTransformationName(transMeta.getName()))
		{
			transMeta.nameFromFilename();
		}

		return transMeta.getName();
	}

	public String makeJobLogTabName(JobMeta jobMeta)
	{
		return Messages.getString("Spoon.Title.LogJobView", makeJobGraphTabName(jobMeta));
	}

	public String makeJobHistoryTabName(JobMeta jobMeta)
	{
		return Messages.getString("Spoon.Title.LogJobHistoryView", makeJobGraphTabName(jobMeta));
	}

	public String makeJobGraphTabName(JobMeta jobMeta)
	{
		if (Const.isEmpty(jobMeta.getName()) && Const.isEmpty(jobMeta.getFilename()))
			return Spoon.STRING_JOB_NO_NAME;

		if (Const.isEmpty(jobMeta.getName()) || spoon.delegates.jobs.isDefaultJobName(jobMeta.getName()))
		{
			jobMeta.nameFromFilename();
		}

		return jobMeta.getName();
	}

	public void tabSelected(TabItem item)
	{
		ArrayList<TabMapEntry> collection = new ArrayList<TabMapEntry>(tabMap);

		// See which core objects to show
		//
		for (TabMapEntry entry : collection)
		{
			boolean isTrans = (entry.getObject() instanceof TransGraph);
			
			if (item.equals(entry.getTabItem()))
			{
				if (isTrans || entry.getObject() instanceof JobGraph)
				{
					EngineMetaInterface meta = entry.getObject().getMeta();
					if (meta != null)
					{
						meta.setInternalKettleVariables();
					}
					if (spoon.getCoreObjectsState() != SpoonInterface.STATE_CORE_OBJECTS_SPOON)
					{
						spoon.refreshCoreObjects();
					}
				}
				
				if (entry.getObject() instanceof JobGraph) {
					((JobGraph)entry.getObject()).setFocus();
				} else if (entry.getObject() instanceof TransGraph) {
					((TransGraph)entry.getObject()).setFocus();
				}
				
				break;
			}
		}

		// Also refresh the tree
		spoon.refreshTree();
		spoon.enableMenus();
		
		
	}
	
	/*
	private void setEnabled(String id,boolean enable)
	{
		spoon.getToolbar().getButtonById(id).setEnable(enable);
	}
	*/
	
}
