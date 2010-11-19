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

package org.pentaho.di.core.database;


/**
 * Contains Database Connection information through static final members for a PALO database.
 * These connections are typically custom-made.
 * That means that reading, writing, etc, is not done through JDBC. 
 * 
 * @author Matt
 * @since  18-Sep-2007
 */
public class PALODatabaseMeta extends GenericDatabaseMeta implements DatabaseInterface
{
	/**
	 * Construct a new database connection.
	 * 
	 */
	public PALODatabaseMeta(String name, String access, String host, String db, String port, String user, String pass)
	{
		super(name, access, host, db, port, user, pass);
	}
	
	public PALODatabaseMeta()
	{
	}
	
	public String getDatabaseTypeDesc()
	{
		return "PALO";
	}

	public String getDatabaseTypeDescLong()
	{
		return "Palo MOLAP Server";
	}
	
	/**
	 * @return Returns the databaseType.
	 */
	public int getDatabaseType()
	{
		return DatabaseMeta.TYPE_DATABASE_PALO;
	}
		
	public int[] getAccessTypeList()
	{
		return new int[] { DatabaseMeta.TYPE_ACCESS_PLUGIN, };
	}
	
	public int getDefaultDatabasePort() {
		return 7777;
	}	

	public String getDatabaseFactoryName() {
		return "plugin.palo.core.PaloHelper";
		// return PaloDatabaseFactory.class.getName();
	}
}
