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

import org.pentaho.di.core.Const;
import org.pentaho.di.core.row.ValueMetaInterface;

/**
 * Contains Oracle RDB specific information through static final members 
 * 
 * @author Matt
 * @since  27-jul-2006
 */
public class OracleRDBDatabaseMeta extends BaseDatabaseMeta implements DatabaseInterface
{
	/**
	 * Construct a new database connections.  Note that not all these parameters are not allways mandatory.
	 * 
	 * @param name The database name
	 * @param access The type of database access
	 * @param host The hostname or IP address
	 * @param db The database name
	 * @param port The port on which the database listens.
	 * @param user The username
	 * @param pass The password
	 */
	public OracleRDBDatabaseMeta(String name, String access, String host, String db, String port, String user, String pass)
	{
		super(name, access, host, db, port, user, pass);
	}
	
	public OracleRDBDatabaseMeta()
	{
	}

	public String getDatabaseTypeDesc()
	{
		return "ORACLERDB";
	}

	public String getDatabaseTypeDescLong()
	{
		return "Oracle RDB";
	}
	
	/**
	 * @return Returns the databaseType.
	 */
	public int getDatabaseType()
	{
		return DatabaseMeta.TYPE_DATABASE_ORACLE_RDB;
	}
		
	public int[] getAccessTypeList()
	{
		return new int[] { DatabaseMeta.TYPE_ACCESS_NATIVE, DatabaseMeta.TYPE_ACCESS_ODBC, DatabaseMeta.TYPE_ACCESS_JNDI };
	}
		
	/**
	 * @return Whether or not the database can use auto increment type of fields (pk)
	 */
	public boolean supportsAutoInc()
	{
		return false;
	}
	
	/**
	 * @see org.pentaho.di.core.database.DatabaseInterface#getLimitClause(int)
	 */
	public String getLimitClause(int nrRows)
	{
		return " WHERE ROWNUM <= "+nrRows;
	}
	
	/**
	 * Returns the minimal SQL to launch in order to determine the layout of the resultset for a given database table
	 * @param tableName The name of the table to determine the layout for
	 * @return The SQL to launch.
	 */
	public String getSQLQueryFields(String tableName)
	{
	    return "SELECT /*+FIRST_ROWS*/ * FROM "+tableName+" WHERE ROWNUM < 1";
	}

	
	public String getDriverClass()
	{
		if (getAccessType()==DatabaseMeta.TYPE_ACCESS_ODBC)
		{
			return "sun.jdbc.odbc.JdbcOdbcDriver";
		}
		else
		{
			return "oracle.rdb.jdbc.rdbThin.Driver";
		}
	}


    public String getURL(String hostname, String port, String databaseName)
    {
		if (getAccessType()==DatabaseMeta.TYPE_ACCESS_ODBC)
		{
			return "jdbc:odbc:"+databaseName;
		}
        else
		{
            return "jdbc:rdbThin://"+hostname+":"+port+"/"+databaseName;
		}

	}
    
    /**
     * Oracle doesn't support options in the URL, we need to put these in a Properties object at connection time...
     */
    public boolean supportsOptionsInURL()
    {
        return false;
    }

	/**
	 * @return true if the database supports sequences
	 */
	public boolean supportsSequences()
	{
		return true;
	}
    
    public boolean supportsRepository()
    {
        return false;
    }

    /**
     * Check if a sequence exists.
     * @param sequenceName The sequence to check
     * @return The SQL to get the name of the sequence back from the databases data dictionary
     */
    public String getSQLSequenceExists(String sequenceName)
    {
        return "SELECT * FROM USER_SEQUENCES WHERE SEQUENCE_NAME = '"+sequenceName.toUpperCase()+"'";
    }
    
    /**
     * Get the current value of a database sequence
     * @param sequenceName The sequence to check
     * @return The current value of a database sequence
     */
    public String getSQLCurrentSequenceValue(String sequenceName)
    {
        return "SELECT "+sequenceName+".currval FROM DUAL";
    }

    /**
     * Get the SQL to get the next value of a sequence. (Oracle only) 
     * @param sequenceName The sequence name
     * @return the SQL to get the next value of a sequence. (Oracle only)
     */
    public String getSQLNextSequenceValue(String sequenceName)
    {
        return "SELECT "+sequenceName+".nextval FROM dual";
    }


	/**
	 * @return true if we need to supply the schema-name to getTables in order to get a correct list of items.
	 */
	public boolean useSchemaNameForTableList()
	{
		return true;
	}

	/**
	 * @return true if the database supports synonyms
	 */
	public boolean supportsSynonyms()
	{
		return true;
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
		return "ALTER TABLE "+tablename+" ADD ( "+getFieldDefinition(v, tk, pk, use_autoinc, true, false)+" ) ";
	}

	/**
	 * Generates the SQL statement to drop a column from the specified table
	 * @param tablename The table to add
	 * @param v The column defined as a value
	 * @param tk the name of the technical key field
	 * @param use_autoinc whether or not this field uses auto increment
	 * @param pk the name of the primary key field
	 * @param semicolon whether or not to add a semi-colon behind the statement.
	 * @return the SQL statement to drop a column from the specified table
	 */
	public String getDropColumnStatement(String tablename, ValueMetaInterface v, String tk, boolean use_autoinc, String pk, boolean semicolon)
	{
		return "ALTER TABLE "+tablename+" DROP ( "+v.getName()+" ) "+Const.CR;
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
		return "ALTER TABLE "+tablename+" MODIFY ("+getFieldDefinition(v, tk, pk, use_autoinc, true, false)+" )";
	}

	public String getFieldDefinition(ValueMetaInterface v, String tk, String pk, boolean use_autoinc, boolean add_fieldname, boolean add_cr)
	{
		StringBuffer retval=new StringBuffer(128);
		
		String fieldname = v.getName();
		int    length    = v.getLength();
		int    precision = v.getPrecision();
		
		if (add_fieldname) retval.append(fieldname).append(' ');
		
		int type         = v.getType();
		switch(type)
		{
		case ValueMetaInterface.TYPE_DATE   : retval.append("DATE"); break;
		case ValueMetaInterface.TYPE_BOOLEAN: retval.append("CHAR(1)"); break;
		case ValueMetaInterface.TYPE_NUMBER : 
		case ValueMetaInterface.TYPE_INTEGER: 
        case ValueMetaInterface.TYPE_BIGNUMBER: 
			retval.append("NUMBER"); 
			if (length>0)
			{
				retval.append('(').append(length);
				if (precision>0)
				{
					retval.append(", ").append(precision);
				}
				retval.append(')');
			}
			break;
		case ValueMetaInterface.TYPE_STRING:
			if (length>=DatabaseMeta.CLOB_LENGTH)
			{
				retval.append("CLOB");
			}
			else
			{
				if (length>0 && length<=2000)
				{
					retval.append("VARCHAR2(").append(length).append(')');
				}
				else
				{
                    if (length<=0)
                    {
                        retval.append("VARCHAR2(2000)"); // We don't know, so we just use the maximum...
                    }
                    else
                    {
                        retval.append("CLOB"); 
                    }
				}
			}
			break;
		default:
			retval.append(" UNKNOWN");
			break;
		}
		
		if (add_cr) retval.append(Const.CR);
		
		return retval.toString();
	}
	
	/* (non-Javadoc)
	 * @see com.ibridge.kettle.core.database.DatabaseInterface#getReservedWords()
	 */
	public String[] getReservedWords()
	{
		return new String[] 
	     {
			"ACCESS", "ADD", "ALL", "ALTER", "AND", "ANY", "ARRAYLEN", "AS", "ASC", "AUDIT", "BETWEEN",
			"BY", "CHAR", "CHECK", "CLUSTER", "COLUMN", "COMMENT", "COMPRESS", "CONNECT", "CREATE", "CURRENT", "DATE",
			"DECIMAL", "DEFAULT", "DELETE", "DESC", "DISTINCT", "DROP", "ELSE", "EXCLUSIVE", "EXISTS", "FILE", "FLOAT",
			"FOR", "FROM", "GRANT", "GROUP", "HAVING", "IDENTIFIED", "IMMEDIATE", "IN", "INCREMENT", "INDEX", "INITIAL",
			"INSERT", "INTEGER", "INTERSECT", "INTO", "IS", "LEVEL", "LIKE", "LOCK", "LONG", "MAXEXTENTS", "MINUS",
			"MODE", "MODIFY", "NOAUDIT", "NOCOMPRESS", "NOT", "NOTFOUND", "NOWAIT", "NULL", "NUMBER", "OF", "OFFLINE",
			"ON", "ONLINE", "OPTION", "OR", "ORDER", "PCTFREE", "PRIOR", "PRIVILEGES", "PUBLIC", "RAW", "RENAME",
			"RESOURCE", "REVOKE", "ROW", "ROWID", "ROWLABEL", "ROWNUM", "ROWS", "SELECT", "SESSION", "SET", "SHARE",
			"SIZE", "SMALLINT", "SQLBUF", "START", "SUCCESSFUL", "SYNONYM", "SYSDATE", "TABLE", "THEN", "TO", "TRIGGER",
			"UID", "UNION", "UNIQUE", "UPDATE", "USER", "VALIDATE", "VALUES", "VARCHAR", "VARCHAR2", "VIEW", "WHENEVER",
			"WHERE", "WITH"
		 };
	}
	

    public String getSQLLockTables(String tableNames[])
    {
        StringBuffer sql=new StringBuffer(128);
        for (int i=0;i<tableNames.length;i++)
        {
            sql.append("LOCK TABLE ").append(tableNames[i]).append(" IN EXCLUSIVE MODE;").append(Const.CR);
        }
        return sql.toString();
    }
    
    public String getSQLUnlockTables(String tableNames[])
    {
        return null; // commit handles the unlocking!
    }

    public String[] getUsedLibraries()
    {
        return new String[] { "rdbthin.jar" };
    }    
}
