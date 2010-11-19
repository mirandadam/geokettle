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
import org.pentaho.di.core.geospatial.SRS;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.row.ValueMetaInterface;

//-- Begin GeoKettle modification --
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ByteOrderValues;
import com.vividsolutions.jts.io.InputStreamInStream;
import com.vividsolutions.jts.io.OutputStreamOutStream;
import com.vividsolutions.jts.io.WKBReader;
import com.vividsolutions.jts.io.WKBWriter;
//-- End GeoKettle modification --

/**
 * Contains MySQL specific information through static final members 
 * 
 * @author Matt
 * @since  11-mrt-2005
 */
//-- Begin GeoKettle modification --
public class MySQLDatabaseMeta extends BaseDatabaseMeta implements DatabaseInterface, GeodatabaseInterface {
	private static final LogWriter LOGGER = LogWriter.getInstance();
// -- End GeoKettle modification --
	public MySQLDatabaseMeta(String name, String access, String host, String db, String port, String user, String pass)
	{
		super(name, access, host, db, port, user, pass);
	}
	
	public MySQLDatabaseMeta()
	{
        super();
	}
	
	public String getDatabaseTypeDesc()
	{
		return "MYSQL";
	}

	public String getDatabaseTypeDescLong()
	{
		return "MySQL";
	}
	
	/**
	 * @return Returns the databaseType.
	 */
	public int getDatabaseType()
	{
		return DatabaseMeta.TYPE_DATABASE_MYSQL;
	}
		
	public int[] getAccessTypeList()
	{
		return new int[] { DatabaseMeta.TYPE_ACCESS_NATIVE, DatabaseMeta.TYPE_ACCESS_ODBC, DatabaseMeta.TYPE_ACCESS_JNDI };
	}
	
	public int getDefaultDatabasePort()
	{
		if (getAccessType()==DatabaseMeta.TYPE_ACCESS_NATIVE) return 3306;
		return -1;
	}
	
	public String getLimitClause(int nrRows)
	{
		return " LIMIT "+nrRows;	
	}
	
    /**
	 * Returns the minimal SQL to launch in order to determine the layout of the resultset for a given database table
	 * @param tableName The name of the table to determine the layout for
	 * @return The SQL to launch.
	 */
	public String getSQLQueryFields(String tableName)
	{
	    return "SELECT * FROM "+tableName+" LIMIT 0"; //$NON-NLS-1$ //$NON-NLS-2$
	}

    public String getSQLTableExists(String tablename)
    {
        return getSQLQueryFields(tablename);
    }
    
    public String getSQLColumnExists(String columnname, String tablename)
    {
        return  getSQLQueryColumnFields(columnname, tablename);
    }
    public String getSQLQueryColumnFields(String columnname, String tableName)
    {
        return "SELECT " + columnname + " FROM "+tableName +" LIMIT 0"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
    
	/**
	 * @see org.pentaho.di.core.database.DatabaseInterface#getNotFoundTK(boolean)
	 */
	public int getNotFoundTK(boolean use_autoinc)
	{
		if ( supportsAutoInc() && use_autoinc)
		{
			return 1;
		}
		return super.getNotFoundTK(use_autoinc);
	}

	public String getDriverClass()
	{
		if (getAccessType()==DatabaseMeta.TYPE_ACCESS_ODBC)
		{
			return "sun.jdbc.odbc.JdbcOdbcDriver";
		}
		else
		{
			return "org.gjt.mm.mysql.Driver";
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
            if (Const.isEmpty(port))
            {
                return "jdbc:mysql://"+hostname+"/"+databaseName;
            }
            else
            {
                return "jdbc:mysql://"+hostname+":"+port+"/"+databaseName;
            }
		}
	}
    
    /**
     * @return The extra option separator in database URL for this platform (usually this is semicolon ; ) 
     */
    public String getExtraOptionSeparator()
    {
        return "&";
    }
    
    /**
     * @return This indicator separates the normal URL from the options
     */
    public String getExtraOptionIndicator()
    {
        return "?";
    }

	/**
	 * @return true if the database supports transactions.
	 */
	public boolean supportsTransactions()
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
	 * @return true if the database supports views
	 */
	public boolean supportsViews()
	{
		return true;
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
		return "ALTER TABLE "+tablename+" ADD "+getFieldDefinition(v, tk, pk, use_autoinc, true, false);
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
		return "ALTER TABLE "+tablename+" MODIFY "+getFieldDefinition(v, tk, pk, use_autoinc, true, false);
	}

	public String getFieldDefinition(ValueMetaInterface v, String tk, String pk, boolean use_autoinc, boolean add_fieldname, boolean add_cr)
	{
		String retval="";
		
		String fieldname = v.getName();
		int    length    = v.getLength();
		int    precision = v.getPrecision();
		
		if (add_fieldname) retval+=fieldname+" ";
		
		int type         = v.getType();
		switch(type)
		{
		case ValueMetaInterface.TYPE_DATE      : retval+="DATETIME"; break;
		case ValueMetaInterface.TYPE_BOOLEAN   : 
			if (supportsBooleanDataType()) {
				retval+="BOOLEAN"; 
			} else {
				retval+="CHAR(1)";
			}
			break;

		case ValueMetaInterface.TYPE_NUMBER    :
		case ValueMetaInterface.TYPE_INTEGER   : 
        case ValueMetaInterface.TYPE_BIGNUMBER : 
			if (fieldname.equalsIgnoreCase(tk) || // Technical key
			    fieldname.equalsIgnoreCase(pk)    // Primary key
			    ) 
			{
				if (use_autoinc)
				{
					retval+="BIGINT AUTO_INCREMENT NOT NULL PRIMARY KEY";
				}
				else
				{
					retval+="BIGINT NOT NULL PRIMARY KEY";
				}
			} 
			else
			{
				// Integer values...
				if (precision==0)
				{
					if (length>9)
					{
						if (length<19) {
							// can hold signed values between -9223372036854775808 and 9223372036854775807
							// 18 significant digits
							retval+="BIGINT";
						}
						else {
							retval+="DECIMAL("+length+")";
						}
					}
					else
					{
						retval+="INT";
					}
				}
				// Floating point values...
				else  
				{
					if (length>15)
					{
						retval+="DECIMAL("+length;
						if (precision>0) retval+=", "+precision;
						retval+=")";
					}
					else
					{
						// A double-precision floating-point number is accurate to approximately 15 decimal places.
						// http://mysql.mirrors-r-us.net/doc/refman/5.1/en/numeric-type-overview.html 
						retval+="DOUBLE";
					}
				}
			}
			break;
		case ValueMetaInterface.TYPE_STRING:
			if (length>0)
			{
				if (length==1) retval+="CHAR(1)";
				else if (length<     256) retval+="VARCHAR("+length+")";
				else if (length<   65536) retval+="TEXT";
				else if (length<16777215) retval+="MEDIUMTEXT";
				else retval+="LONGTEXT";
			}
			else
			{
				retval+="TINYTEXT";
			}
			break;
        case ValueMetaInterface.TYPE_BINARY:
            retval+="LONGBLOB";
            break;
        // -- Begin GeoKettle modification --
		case ValueMetaInterface.TYPE_GEOMETRY:
			// TODO: see PostGIS implementation for more todos
			retval+=("GEOMETRY");
			break;
        // -- End GeoKettle modification --
		default:
			retval+=" UNKNOWN";
			break;
		}
		
		if (add_cr) retval+=Const.CR;
		
		return retval;
	}
	
	/* (non-Javadoc)
	 * @see org.pentaho.di.core.database.DatabaseInterface#getReservedWords()
	 */
	public String[] getReservedWords()
	{
		return new String[]
		{
			"ADD", "ALL", "ALTER", "ANALYZE", "AND", "AS", "ASC", "ASENSITIVE", "BEFORE", "BETWEEN", "BIGINT",
			"BINARY", "BLOB", "BOTH", "BY", "CALL", "CASCADE", "CASE", "CHANGE", "CHAR", "CHARACTER",
			"CHECK", "COLLATE", "COLUMN", "CONDITION", "CONNECTION", "CONSTRAINT", "CONTINUE", "CONVERT", "CREATE", "CROSS", "CURRENT_DATE",
			"CURRENT_TIME", "CURRENT_TIMESTAMP", "CURRENT_USER", "CURSOR", "DATABASE", "DATABASES", "DAY_HOUR", "DAY_MICROSECOND", "DAY_MINUTE", "DAY_SECOND", "DEC",
			"DECIMAL", "DECLARE", "DEFAULT", "DELAYED", "DELETE", "DESC", "DESCRIBE", "DETERMINISTIC", "DISTINCT", "DISTINCTROW", "DIV",
			"DOUBLE", "DROP", "DUAL", "EACH", "ELSE", "ELSEIF", "ENCLOSED", "ESCAPED", "EXISTS", "EXIT", "EXPLAIN",
			"FALSE", "FETCH", "FLOAT", "FOR", "FORCE", "FOREIGN", "FROM", "FULLTEXT", "GOTO", "GRANT", "GROUP",
			"HAVING", "HIGH_PRIORITY", "HOUR_MICROSECOND", "HOUR_MINUTE", "HOUR_SECOND", "IF", "IGNORE", "IN", "INDEX", "INFILE", "INNER",
			"INOUT", "INSENSITIVE", "INSERT", "INT", "INTEGER", "INTERVAL", "INTO", "IS", "ITERATE", "JOIN", "KEY",
			"KEYS", "KILL", "LEADING", "LEAVE", "LEFT", "LIKE", "LIMIT", "LINES", "LOAD", "LOCALTIME", "LOCALTIMESTAMP", "LOCATE", 
			"LOCK", "LONG", "LONGBLOB", "LONGTEXT", "LOOP", "LOW_PRIORITY", "MATCH", "MEDIUMBLOB", "MEDIUMINT", "MEDIUMTEXT", "MIDDLEINT",
			"MINUTE_MICROSECOND", "MINUTE_SECOND", "MOD", "MODIFIES", "NATURAL", "NOT", "NO_WRITE_TO_BINLOG", "NULL", "NUMERIC", "ON", "OPTIMIZE",
			"OPTION", "OPTIONALLY", "OR", "ORDER", "OUT", "OUTER", "OUTFILE", "POSITION", "PRECISION", "PRIMARY", "PROCEDURE", "PURGE",
			"READ", "READS", "REAL", "REFERENCES", "REGEXP", "RENAME", "REPEAT", "REPLACE", "REQUIRE", "RESTRICT", "RETURN",
			"REVOKE", "RIGHT", "RLIKE", "SCHEMA", "SCHEMAS", "SECOND_MICROSECOND", "SELECT", "SENSITIVE", "SEPARATOR", "SET", "SHOW",
			"SMALLINT", "SONAME", "SPATIAL", "SPECIFIC", "SQL", "SQLEXCEPTION", "SQLSTATE", "SQLWARNING", "SQL_BIG_RESULT", "SQL_CALC_FOUND_ROWS", "SQL_SMALL_RESULT",
			"SSL", "STARTING", "STRAIGHT_JOIN", "TABLE", "TERMINATED", "THEN", "TINYBLOB", "TINYINT", "TINYTEXT", "TO", "TRAILING",
			"TRIGGER", "TRUE", "UNDO", "UNION", "UNIQUE", "UNLOCK", "UNSIGNED", "UPDATE", "USAGE", "USE", "USING",
			"UTC_DATE", "UTC_TIME", "UTC_TIMESTAMP", "VALUES", "VARBINARY", "VARCHAR", "VARCHARACTER", "VARYING", "WHEN", "WHERE", "WHILE",
			"WITH", "WRITE", "XOR", "YEAR_MONTH", "ZEROFILL",
			// -- Begin GeoKettle modification --
			"GEOMETRY"
	        // -- End GeoKettle modification --
        };
	}
	
	/* (non-Javadoc)
	 * @see org.pentaho.di.core.database.DatabaseInterface#getStartQuote()
	 */
	public String getStartQuote()
	{
		return "`";
	}
	
	/**
	 * Simply add an underscore in the case of MySQL!
	 * @see org.pentaho.di.core.database.DatabaseInterface#getEndQuote()
	 */
	public String getEndQuote()
	{
		return "`";
	}
    
    /**
     * @param tableNames The names of the tables to lock
     * @return The SQL command to lock database tables for write purposes.
     */
    public String getSQLLockTables(String tableNames[])
    {
        String sql="LOCK TABLES ";
        for (int i=0;i<tableNames.length;i++)
        {
            if (i>0) sql+=", ";
            sql+=tableNames[i]+" WRITE";
        }
        sql+=";"+Const.CR;

        return sql;
    }

    /**
     * @param tableName The name of the table to unlock
     * @return The SQL command to unlock a database table.
     */
    public String getSQLUnlockTables(String tableName[])
    {
        return "UNLOCK TABLES"; // This unlocks all tables
    }

    /**
     * @return true if the database supports a boolean, bit, logical, ... datatype
     * The default is false: map to a string.
     */
    public boolean supportsBooleanDataType()
    {
        return false;
    }
    
    public boolean needsToLockAllTables() {
    	return true;
    }
    
    /**
     * @return extra help text on the supported options on the selected database platform.
     */
    public String getExtraOptionsHelpText()
    {
        return "http://dev.mysql.com/doc/refman/5.0/en/connector-j-reference-configuration-properties.html";
    }

    public String[] getUsedLibraries()
    {
        return new String[] { "mysql-connector-java-3.1.14-bin.jar" };
    }

    // -- Begin GeoKettle modification --
    
    /*
     * (non-Javadoc)
     * @see org.pentaho.di.core.database.GeodatabaseInterface#convertToJTSGeometry(java.lang.Object)
     */
	public Geometry convertToJTSGeometry(ValueMetaInterface vmi, Object o, Database db) {
		if (o instanceof byte[]) {
			try {
				// treat the values read from MySQL as byte stream
				InputStream inStream = new ByteArrayInputStream( (byte[]) o );
				if (inStream == null) {
					throw new IOException("Could not read from byte array!");
				}
				
				// read first 4 bytes that represent the SRID
				byte[] sridFromByteArray = new byte[4];
				inStream.read(sridFromByteArray);
				
				// Map MySQL's SRID with the EPSG-SRID or take a custom SRS from WKT
				int mysql_srid = ByteOrderValues.getInt(sridFromByteArray, ByteOrderValues.LITTLE_ENDIAN);
				SRS epsg_srid = convertToEPSG_SRID(mysql_srid, db.getConnection());
				vmi.setGeometrySRS(epsg_srid);
				
				// parse the rest of the byte array as WKB and convert to Geometry
				Geometry geom = new WKBReader().read( new InputStreamInStream(inStream) );
				geom.setSRID(epsg_srid.getSRID());
				return geom;
			} catch (Exception e) {
				LOGGER.logError("GeoKettle", "Conversion from MySQL-geometry failed.");
				return null;
			}
		} else {
			LOGGER.logDetailed("GeoKettle", "No MySQL-geometry found to convert");
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.pentaho.di.core.database.GeodatabaseInterface#convertToObject(com.vividsolutions.jts.geom.Geometry, java.sql.Connection)
	 */
	public Object convertToObject(ValueMetaInterface vmi, Geometry geom, Database db) {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		
		try {
			// Map the EPSG- (or custom-) SRID with MySQL's SRID.
			int mysql_srid = convertToDBMS_SRID(vmi.getGeometrySRS(), db.getConnection());
			
			// preserve SRID for MySQL in first 4 bytes, use little endian
			byte[] sridInByteArray = new byte[4];
			ByteOrderValues.putInt(mysql_srid, sridInByteArray, ByteOrderValues.LITTLE_ENDIAN);
			outStream.write(sridInByteArray);
			WKBWriter wkbWriter = new WKBWriter(2, ByteOrderValues.LITTLE_ENDIAN);	// base 2, little endian
			wkbWriter.write(geom, new OutputStreamOutStream(outStream));			// fill byte array with values from Geometry
			return outStream.toByteArray();
		} catch (Exception e) {
			LOGGER.logError("GeoKettle", "Conversion to MySQL-geometry failed.");
			return null;
		}
	}

	public int convertToDBMS_SRID(SRS epsg_srid, Connection conn) {
		// TODO: GeoKettle: Check if the SRID exists. If not, add it to spatial_ref_sys
		// table and return the DBMS-SRID from the newly created SRS.
		return epsg_srid.getSRID();
	}

	public SRS convertToEPSG_SRID(int dbms_srid, Connection conn) {
		// TODO: GeoKettle: If the dmbs_srid cannot be converted to an EPSG SRID, connect
		// to the database and get the WKT of the spatial reference system.
		return SRS.createFromEPSG(Integer.toString(dbms_srid));
	}
	
	// -- End GeoKettle modification --
}


