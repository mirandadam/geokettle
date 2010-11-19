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

import org.pentaho.di.core.row.ValueMetaInterface;

/**
 * Contains SAP R/3 system specific information through static final members 
 * 
 * @author Matt
 * @since  03-07-2005
 */
public class SAPR3DatabaseMeta extends BaseDatabaseMeta implements DatabaseInterface
{
    public static final String ATTRIBUTE_SAP_LANGUAGE = "SAPLanguage";
    public static final String ATTRIBUTE_SAP_SYSTEM_NUMBER = "SAPSystemNumber";
    public static final String ATTRIBUTE_SAP_CLIENT = "SAPClient";
    
	/**
	 * Construct a new database connection.
	 * 
	 */
	public SAPR3DatabaseMeta(String name, String access, String host, String db, String port, String user, String pass)
	{
		super(name, access, host, db, port, user, pass);
	}
	
	public SAPR3DatabaseMeta()
	{
	}
	
	public String getDatabaseTypeDesc()
	{
		return "SAPR3";
	}

	public String getDatabaseTypeDescLong()
	{
		return "SAP R/3 System";
	}
	
	/**
	 * @return Returns the databaseType.
	 */
	public int getDatabaseType()
	{
		return DatabaseMeta.TYPE_DATABASE_SAPR3;
	}
		
	public int[] getAccessTypeList()
	{
		return new int[] { DatabaseMeta.TYPE_ACCESS_PLUGIN };
	}
	
	public int getDefaultDatabasePort()
	{
		return -1;
	}

	/**
	 * @return Whether or not the database can use auto increment type of fields (pk)
	 */
	public boolean supportsAutoInc()
	{
		return false;
	}
	
	public String getDriverClass()
	{
			return null;
	}

    public String getURL(String hostname, String port, String databaseName)
    {
		return null;
	}

	/**
	 * @return true if the database supports bitmap indexes
	 */
	public boolean supportsBitmapIndex()
	{
		return false;
	}

	/**
	 * @return true if the database supports synonyms
	 */
	public boolean supportsSynonyms()
	{
		return false;
	}

	/**
	 * Generates the SQL statement to add a column to the specified table
	 * @param tablename The table to add
	 * @param v The column defined as a value
	 * @param tk the name of the technical key field
	 * @param use_autoinc whether or not this field uses auto increment
	 * @param pk the name of the primary key field
	 * @param semicolon whether or not to add a semi-colon behind the statement.
	 * @return the SQL statement to add a column to the specified table
	 */
	public String getAddColumnStatement(String tablename, ValueMetaInterface v, String tk, boolean use_autoinc, String pk, boolean semicolon)
	{
		return null;
	}

	/**
	 * Generates the SQL statement to modify a column in the specified table
	 * @param tablename The table to add
	 * @param v The column defined as a value
	 * @param tk the name of the technical key field
	 * @param use_autoinc whether or not this field uses auto increment
	 * @param pk the name of the primary key field
	 * @param semicolon whether or not to add a semi-colon behind the statement.
	 * @return the SQL statement to modify a column in the specified table
	 */
	public String getModifyColumnStatement(String tablename, ValueMetaInterface v, String tk, boolean use_autoinc, String pk, boolean semicolon)
	{
		return null;
	}

	public String getFieldDefinition(ValueMetaInterface v, String tk, String pk, boolean use_autoinc, boolean add_fieldname, boolean add_cr)
	{
	    return null;
	}
	
	public String [] getReservedWords()
	{
		return null;
	}
    
    public String[] getUsedLibraries()
    {
        return new String[] { };
    }
}
