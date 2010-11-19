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

import org.eclipse.swt.SWT;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.HasSlaveServersInterface;
import org.pentaho.di.ui.cluster.dialog.SlaveServerDialog;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.spoon.SpoonSlave;
import org.pentaho.di.ui.spoon.TabMapEntry;
import org.pentaho.xul.swt.tab.TabItem;
import org.pentaho.xul.swt.tab.TabSet;

public class SpoonSlaveDelegate extends SpoonDelegate
{
	public SpoonSlaveDelegate(Spoon spoon)
	{
		super(spoon);
	}


	public void addSpoonSlave(SlaveServer slaveServer)
	{
		TabSet tabfolder = spoon.tabfolder;

		// See if there is a SpoonSlave for this slaveServer...
		String tabName = spoon.delegates.tabs.makeSlaveTabName(slaveServer);
		TabItem tabItem = spoon.delegates.tabs.findTabItem(tabName, TabMapEntry.OBJECT_TYPE_SLAVE_SERVER);
		if (tabItem == null)
		{
			SpoonSlave spoonSlave = new SpoonSlave(tabfolder.getSwtTabset(), SWT.NONE, spoon, slaveServer);
			tabItem = new TabItem(tabfolder, tabName, tabName);
			tabItem.setToolTipText("Status of slave server : " + slaveServer.getName() + " : "
					+ slaveServer.getServerAndPort());
			tabItem.setControl(spoonSlave);

			spoon.delegates.tabs.addTab(new TabMapEntry(tabItem, tabName, spoonSlave, TabMapEntry.OBJECT_TYPE_SLAVE_SERVER));
		}
		int idx = tabfolder.indexOf(tabItem);
		tabfolder.setSelected(idx);
	}

	public void delSlaveServer(HasSlaveServersInterface hasSlaveServersInterface, SlaveServer slaveServer) throws KettleException
	{

		Repository rep = spoon.getRepository();

		if (rep != null && slaveServer.getId() > 0)
		{
			// remove the slave server from the repository too...
			rep.delSlave(slaveServer.getId());
		}

		int idx = hasSlaveServersInterface.getSlaveServers().indexOf(slaveServer);
		hasSlaveServersInterface.getSlaveServers().remove(idx);
		spoon.refreshTree();

	}

	public void newSlaveServer(HasSlaveServersInterface hasSlaveServersInterface)
	{
		SlaveServer slaveServer = new SlaveServer();

		SlaveServerDialog dialog = new SlaveServerDialog(spoon.getShell(), slaveServer);
		if (dialog.open())
		{
			hasSlaveServersInterface.getSlaveServers().add(slaveServer);
			spoon.refreshTree();
		}
	}
}
