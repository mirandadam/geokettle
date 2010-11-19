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
 * Contains Vertica Analytic Database information through static final members 
 * 
 * @author DEinspanjer
 * @since  2009-03-16
 * @author Matt
 * @since  May-2008
 */
public class VerticaDatabaseMeta extends BaseDatabaseMeta implements DatabaseInterface
{
	/**
	 * Construct a new database connection.
	 * 
	 */
	public VerticaDatabaseMeta(String name, String access, String host, String db, String port, String user, String pass)
	{
		super(name, access, host, db, port, user, pass);
	}
	
	public VerticaDatabaseMeta()
	{
	}
	
	public String getDatabaseTypeDesc()
	{
		return "VERTICA";
	}

	public String getDatabaseTypeDescLong()
	{
		return "Vertica";
	}
	
	/**
	 * @return Returns the databaseType.
	 */
	public int getDatabaseType()
	{
		return DatabaseMeta.TYPE_DATABASE_VERTICA;
	}
		
	public int[] getAccessTypeList()
	{
		return new int[] { DatabaseMeta.TYPE_ACCESS_NATIVE, DatabaseMeta.TYPE_ACCESS_ODBC, DatabaseMeta.TYPE_ACCESS_JNDI };
	}
	
	public String getDriverClass()
	{
        if (getAccessType()==DatabaseMeta.TYPE_ACCESS_NATIVE)
        {
            return "com.vertica.Driver";            
        }
        else
        {
            return "sun.jdbc.odbc.JdbcOdbcDriver"; // always ODBC!
        }

	}
	
    public String getURL(String hostname, String port, String databaseName)
    {
        if (getAccessType()==DatabaseMeta.TYPE_ACCESS_NATIVE)
        {
        	return "jdbc:vertica://"+hostname+":"+port+"/"+databaseName;
        }
        else
        {
            return "jdbc:odbc:"+databaseName;
        }
	}

	/**
	 * Checks whether or not the command setFetchSize() is supported by the JDBC driver...
	 * @return true is setFetchSize() is supported!
	 */
	public boolean isFetchSizeSupported()
	{
		return false;
	}
	
	/**
	 * @return true if the database supports bitmap indexes
	 */
	public boolean supportsBitmapIndex()
	{
		return false;
	}
	
	/**
	 * @return true if Kettle can create a repository on this type of database.
	 */
	public boolean supportsRepository()
	{
		return false;
	}
	
	/**
	 * @param tableName The table to be truncated.
	 * @return The SQL statement to truncate a table: remove all rows from it without a transaction
	 */
	public String getTruncateTableStatement(String tableName)
	{
	    throw new UnsupportedOperationException("Truncate not supported.");
	}


	/**
	 * Generates the SQL statement to add a column to the specified table
	 * For this generic type, i set it to the most common possibility.
	 * 
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
		return "--NOTE: Table cannot be altered unless all projections are dropped.\nALTER TABLE "+tablename+" ADD "+getFieldDefinition(v, tk, pk, use_autoinc, true, false);
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
		return "--NOTE: Table cannot be altered unless all projections are dropped.\nALTER TABLE "+tablename+" MODIFY "+getFieldDefinition(v, tk, pk, use_autoinc, true, false);
	}

	public String getFieldDefinition(ValueMetaInterface v, String tk, String pk, boolean use_autoinc, boolean add_fieldname, boolean add_cr)
	{
		String retval="";
		
		String fieldname = v.getName();
		int    length    = v.getLength();
		// Unused in vertica
		//int    precision = v.getPrecision();
		
		if (add_fieldname) retval+=fieldname+" ";
		
		int type         = v.getType();
		switch(type)
		{
		case ValueMetaInterface.TYPE_DATE   : retval+="TIMESTAMP"; break;
		case ValueMetaInterface.TYPE_BOOLEAN: retval+="BOOLEAN"; break;
		case ValueMetaInterface.TYPE_NUMBER : 
        case ValueMetaInterface.TYPE_BIGNUMBER:
        	retval+="FLOAT";
        	break;
		case ValueMetaInterface.TYPE_INTEGER:
			retval+="INTEGER";
			break;
		case ValueMetaInterface.TYPE_STRING:
			retval+="VARCHAR("+length+")"; 
			break;
		default:
			retval+=" UNKNOWN";
			break;
		}
		
		if (add_cr) retval+=Const.CR;
		
		return retval;
	}

    public String[] getUsedLibraries()
    {
        return new String[] { "vertica_2.5_jdk_5.jar" };
    }

    @Override
    public int getDefaultDatabasePort()
    {
        return 5433;
    }

    @Override
    public String getLimitClause(int nrRows)
    {
        return " LIMIT "+nrRows;
    }

    @Override
    public int getMaxTextFieldLength()
    {
        return 0;
    }

    @Override
    public int getMaxVARCHARLength()
    {
        return 4000;
    }

    @Override
    public String[] getReservedWords()
    {
        return new String[]
                          {
                              // From "SQL Reference Manual.pdf" found on support.vertica.com
                              "ABORT", "ABSOLUTE", "ACCESS", "ACTION", "ADD", "AFTER", "AGGREGATE", 
                              "ALL", "ALSO", "ALTER", "ANALYSE", "ANALYZE", "AND", "ANY", 
                              "ARRAY", "AS", "ASC", "ASSERTION", "ASSIGNMENT", "AT", "AUTHORIZATION", 
                              "BACKWARD", "BEFORE", "BEGIN", "BETWEEN", "BIGINT", "BINARY", "BIT", 
                              "BLOCK_DICT", "BLOCKDICT_COMP", "BOOLEAN", "BOTH", "BY", 
                              "CACHE", "CALLED", "CASCADE", "CASE", "CAST", "CATALOG_PATH", "CHAIN", "CHAR", "CHARACTER", 
                              "CHARACTERISTICS", "CHECK", "CHECKPOINT", "CLASS", "CLOSE", "CLUSTER", 
                              "COALESCE", "COLLATE", "COLUMN", "COMMENT", "COMMIT", "COMMITTED", "COMMONDELTA_COMP", 
                              "CONSTRAINT", "CONSTRAINTS", "CONVERSION", "CONVERT", "COPY", "CORRELATION", 
                              "CREATE", "CREATEDB", "CREATEUSER", "CROSS", "CSV", "CURRENT_DATABASE", 
                              "CURRENT_DATE", "CURRENT_TIME", "CURRENT_TIMESTAMP", "CURRENT_USER", "CURSOR", "CYCLE", 
                              "DATA", "DATABASE", "DATAPATH", "DAY", "DEALLOCATE", "DEC", "DECIMAL", "DECLARE", "DEFAULT", "DEFAULTS",
                              "DEFERRABLE", "DEFERRED", "DEFINER", "DELETE", "DELIMITER", "DELIMITERS", 
                              "DELTARANGE_COMP", "DELTARANGE_COMP_SP", "DELTAVAL", 
                              "DESC", "DETERMINES", "DIRECT", "DISTINCT", "DISTVALINDEX", "DO", "DOMAIN", "DOUBLE", "DROP", 
                              "EACH", "ELSE", "ENCODING", "ENCRYPTED", "END", "EPOCH", "ERROR", "ESCAPE", 
                              "EXCEPT", "EXCEPTIONS", "EXCLUDING", "EXCLUSIVE", "EXECUTE", "EXISTS", "EXPLAIN", "EXTERNAL", "EXTRACT", 
                              "FALSE", "FETCH", "FIRST", "FLOAT", "FOR", "FORCE", "FOREIGN", "FORWARD", "FREEZE", "FROM", "FULL", "FUNCTION",  
                              "GLOBAL", "GRANT", "GROUP", 
                              "HANDLER", "HAVING", "HOLD", "HOUR", 
                              "ILIKE", "IMMEDIATE", "IMMUTABLE", "IMPLICIT", "IN", "IN_P", "INCLUDING", "INCREMENT", "INDEX", "INHERITS",
                              "INITIALLY", "INNER", "INOUT", "INPUT", "INSENSITIVE", "INSERT", "INSTEAD", 
                              "INT", "INTEGER", "INTERSECT", "INTERVAL", "INTO", "INVOKER", "IS", "ISNULL", "ISOLATION", 
                              "JOIN", 
                              "KEY",  
                              "LANCOMPILER", "LANGUAGE", "LARGE", "LAST", "LATEST", "LEADING", "LEFT", "LESS", "LEVEL", 
                              "LIKE", "LIMIT", "LISTEN", "LOAD", "LOCAL", "LOCALTIME", "LOCALTIMESTAMP", "LOCATION", "LOCK", 
                              "MATCH", "MAXVALUE", "MERGEOUT", "MINUTE", "MINVALUE", "MOBUF", "MODE", 
                              "MONTH", "MOVE", "MOVEOUT", "MULTIALGORITHM_COMP", "MULTIALGORITHM_COMP_SP", 
                              "NAMES", "NATIONAL", "NATURAL", "NCHAR", "NEW", "NEXT", "NO", "NOCREATEDB", 
                              "NOCREATEUSER", "NODE", "NODES", "NONE",  
                              "NOT", "NOTHING", "NOTIFY", "NOTNULL", "NOWAIT", "NULL", "NULLIF", "NUMERIC", 
                              "OBJECT", "OF", "OFF", "OFFSET", "OIDS", "OLD", "ON", "ONLY", "OPERATOR", "OPTION", "OR", 
                              "ORDER", "OUT", "OUTER", "OVERLAPS", "OVERLAY", "OWNER", 
                              "PARTIAL", "PARTITION", "PASSWORD", "PLACING", "POSITION", "PRECISION", "PREPARE", "PRESERVE",
                              "PRIMARY", "PRIOR", "PRIVILEGES", "PROCEDURAL", "PROCEDURE", "PROJECTION", 
                              "QUOTE", 
                              "READ", "REAL", "RECHECK", "RECORD", "RECOVER", "REFERENCES", "REFRESH", "REINDEX", "REJECTED", 
                              "RELATIVE", "RELEASE", "RENAME", "REPEATABLE", "REPLACE", "RESET", "RESTART", "RESTRICT", "RETURNS", 
                              "REVOKE", "RIGHT", "RLE", "ROLLBACK", "ROW", "ROWS", "RULE", 
                              "SAVEPOINT", "SCHEMA", "SCROLL", "SECOND", "SECURITY", "SEGMENTED", "SELECT", "SEQUENCE", 
                              "SERIALIZABLE", "SESSION", "SESSION_USER", "SET", "SETOF", "SHARE", "SHOW", 
                              "SIMILAR", "SIMPLE", "SMALLINT", "SOME", "SPLIT", "STABLE", "START", "STATEMENT", "STATISTICS",
                              "STDIN", "STDOUT", "STORAGE", "STRICT", "SUBSTRING", "SYSID",  
                              "TABLE", "TABLESPACE", "TEMP", "TEMPLATE", "TEMPORARY", "TERMINATOR", 
                              "THAN", "THEN", "TIME", "TIMESTAMP", "TIMESTAMPTZ", "TIMETZ", "TO", "TOAST", 
                              "TRAILING", "TRANSACTION", "TREAT", "TRIGGER", "TRIM", "TRUE", "TRUE_P", "TRUNCATE", "TRUSTED", "TYPE", 
                              "UNCOMMITTED", "UNENCRYPTED", "UNION", "UNIQUE", "UNKNOWN", "UNLISTEN", "UNSEGMENTED", 
                              "UNTIL", "UPDATE", "USAGE", "USER", "USING", 
                              "VACUUM", "VALID", "VALIDATOR", "VALINDEX", "VALUES", "VARCHAR", "VARYING", "VERBOSE", "VIEW", "VOLATILE", 
                              "WHEN", "WHERE", "WITH", "WITHOUT", "WORK", "WRITE", 
                              "YEAR", 
                              "ZONE"
                          };    }

    @Override
    public String getSQLColumnExists(String columnname, String tablename)
    {
        return super.getSQLColumnExists(columnname, tablename) + getLimitClause(1);
    }

    @Override
    public String getSQLQueryFields(String tableName)
    {
        return super.getSQLQueryFields(tableName) + getLimitClause(1);
    }

    @Override
    public String getSQLTableExists(String tablename)
    {
        return super.getSQLTableExists(tablename) + getLimitClause(1);
    }

    @Override
    public String[] getViewTypes()
    {
        return new String[] {};
    }

    @Override
    public boolean supportsAutoInc()
    {
        return false;
    }

    @Override
    public boolean supportsBooleanDataType()
    {
        return true;
    }
    
    @Override
    public boolean supportsViews()
    {
    	//TODO: This will change in Vertica 3.0
        return false;
    }
}
