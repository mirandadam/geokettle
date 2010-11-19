package org.pentaho.di.repository;

import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.ProgressMonitorListener;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.job.JobEntryLoader;
import org.pentaho.di.job.JobPlugin;
import org.pentaho.di.trans.StepLoader;
import org.pentaho.di.trans.StepPlugin;

public class RepositoryCreationHelper {

	private Repository repository;
	private LogWriter log;
	private DatabaseMeta databaseMeta;
	private Database database;
	
	private StepLoader stepLoader;
	
    public RepositoryCreationHelper(Repository repository) {
		this.repository = repository;
		this.databaseMeta = this.repository.getDatabaseMeta();
		this.database = this.repository.getDatabase();

		this.log = LogWriter.getInstance();
		this.stepLoader = StepLoader.getInstance();
	}

	/**
     * Create or upgrade repository tables & fields, populate lookup tables, ...
     * 
     * @param monitor The progress monitor to use, or null if no monitor is present.
     * @param upgrade True if you want to upgrade the repository, false if you want to create it.
     * @param statements the list of statements to populate
     * @param dryrun true if we don't actually execute the statements
     * 
     * @throws KettleException in case something goes wrong!
     */
	public synchronized void createRepositorySchema(ProgressMonitorListener monitor, boolean upgrade, List<String> statements, boolean dryrun) throws KettleException
	{
		RowMetaInterface table;
		String sql;
		String tablename;
		String schemaTable;
		String indexname;
		String keyfield[];
		String user[], pass[], code[], desc[], prof[];

		int KEY = 9; // integer, no need for bigint!

		log.logBasic(toString(), "Starting to create or modify the repository tables...");
        String message = (upgrade?"Upgrading ":"Creating")+" the Kettle repository...";
		if (monitor!=null) monitor.beginTask(message, 31);
        
        repository.setAutoCommit(true);
        
        //////////////////////////////////////////////////////////////////////////////////
        // R_LOG
        //
        // Log the operations we do in the repository.
        //
        table = new RowMeta();
        tablename = Repository.TABLE_R_REPOSITORY_LOG;
        schemaTable = databaseMeta.getQuotedSchemaTableCombination(null, tablename);
        if (monitor!=null) monitor.subTask("Checking table "+schemaTable);
        table.addValueMeta(new ValueMeta(Repository.FIELD_REPOSITORY_LOG_ID_REPOSITORY_LOG, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
        table.addValueMeta(new ValueMeta(Repository.FIELD_REPOSITORY_LOG_REP_VERSION,    ValueMetaInterface.TYPE_STRING, Repository.REP_STRING_CODE_LENGTH, 0));
        table.addValueMeta(new ValueMeta(Repository.FIELD_REPOSITORY_LOG_LOG_DATE,       ValueMetaInterface.TYPE_DATE));
        table.addValueMeta(new ValueMeta(Repository.FIELD_REPOSITORY_LOG_LOG_USER,       ValueMetaInterface.TYPE_STRING, Repository.REP_STRING_CODE_LENGTH, 0));
        table.addValueMeta(new ValueMeta(Repository.FIELD_REPOSITORY_LOG_OPERATION_DESC, ValueMetaInterface.TYPE_STRING, Repository.REP_STRING_LENGTH, 0));
        sql = database.getDDL(schemaTable, table, null, false, Repository.FIELD_REPOSITORY_LOG_ID_REPOSITORY_LOG, false);
        
        if (!Const.isEmpty(sql))
        {
        	statements.add(sql);
        	if (!dryrun) {
	            try
	            {
	                if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
	                database.execStatements(sql);
	                if (log.isDetailed()) log.logDetailed(toString(), "Created/altered table " + schemaTable);
	            }
	            catch (KettleException dbe)
	            {
	                throw new KettleException("Unable to create or modify table " + schemaTable, dbe);
	            }
        	}
        }
        else
        {
            if (log.isDetailed()) log.logDetailed(toString(), "Table " + schemaTable + " is OK.");
        }

        if (!dryrun) {
        	repository.insertLogEntry((upgrade?"Upgrade":"Creation")+" of the Kettle repository");
        }

        //////////////////////////////////////////////////////////////////////////////////
        // R_VERSION
        //
        // Let's start with the version table
        //
        table = new RowMeta();
        tablename = Repository.TABLE_R_VERSION;
        schemaTable = databaseMeta.getQuotedSchemaTableCombination(null, tablename);
        if (monitor!=null) monitor.subTask("Checking table "+schemaTable);
        table.addValueMeta(new ValueMeta(Repository.FIELD_VERSION_ID_VERSION,       ValueMetaInterface.TYPE_INTEGER, KEY, 0));
        table.addValueMeta(new ValueMeta(Repository.FIELD_VERSION_MAJOR_VERSION,    ValueMetaInterface.TYPE_INTEGER, 3, 0));
        table.addValueMeta(new ValueMeta(Repository.FIELD_VERSION_MINOR_VERSION,    ValueMetaInterface.TYPE_INTEGER, 3, 0));
        table.addValueMeta(new ValueMeta(Repository.FIELD_VERSION_UPGRADE_DATE,     ValueMetaInterface.TYPE_DATE, 0, 0));
        table.addValueMeta(new ValueMeta(Repository.FIELD_VERSION_IS_UPGRADE,       ValueMetaInterface.TYPE_BOOLEAN, 1, 0));
        sql = database.getDDL(schemaTable, table, null, false, Repository.FIELD_VERSION_ID_VERSION, false);
        boolean create = false;
        if (!Const.isEmpty(sql))
        {
        	create = sql.toUpperCase().indexOf("CREATE TABLE")>=0;
        	statements.add(sql);
        	if (!dryrun) {
	            try
	            {
	                if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
	                database.execStatements(sql);
	                if (log.isDetailed()) log.logDetailed(toString(), "Created/altered table " + schemaTable);
	            }
	            catch (KettleException dbe)
	            {
	                throw new KettleException("Unable to create or modify table " + schemaTable, dbe);
	            }
        	}
        }
        else
        {
            if (log.isDetailed()) log.logDetailed(toString(), "Table " + schemaTable + " is OK.");
        }

        // Insert an extra record in R_VERSION every time we pass here...
        //
        try
        {
        	// if the table doesn't exist, don't try to grab an ID from it...
        	long nextId;
        	if (sql.toUpperCase().indexOf("CREATE TABLE")<0) { 
        		nextId = repository.getNextID(schemaTable, Repository.FIELD_VERSION_ID_VERSION);
        	} else {
        		nextId = 1;
        	}
            Object[] data = new Object[] {
                    Long.valueOf(nextId),
                    Long.valueOf(Repository.REQUIRED_MAJOR_VERSION),
                    Long.valueOf(Repository.REQUIRED_MINOR_VERSION),
                    new Date(),
                    Boolean.valueOf(upgrade),
                };
            if (dryrun) {
            	sql = database.getSQLOutput(null, Repository.TABLE_R_VERSION, table, data, null);
            	statements.add(sql);
            } else {
            	database.execStatement("INSERT INTO "+databaseMeta.getQuotedSchemaTableCombination(null, Repository.TABLE_R_VERSION)+" VALUES(?, ?, ?, ?, ?)", table, data);
            }
        }
        catch(KettleException e)
        {
            throw new KettleException("Unable to insert new version log record into "+schemaTable, e);
        }
        
		//////////////////////////////////////////////////////////////////////////////////
		// R_DATABASE_TYPE
		//
		// Create table...
		//
		boolean ok_database_type = true;
		table = new RowMeta();
		tablename = Repository.TABLE_R_DATABASE_TYPE;
		schemaTable = databaseMeta.getQuotedSchemaTableCombination(null, tablename);
		if (monitor!=null) monitor.subTask("Checking table "+schemaTable);
		table.addValueMeta(new ValueMeta(Repository.FIELD_DATABASE_TYPE_ID_DATABASE_TYPE, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_DATABASE_TYPE_CODE,             ValueMetaInterface.TYPE_STRING, Repository.REP_STRING_CODE_LENGTH, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_DATABASE_TYPE_DESCRIPTION,      ValueMetaInterface.TYPE_STRING, Repository.REP_STRING_CODE_LENGTH, 0));
		sql = database.getDDL(schemaTable, table, null, false, Repository.FIELD_DATABASE_TYPE_ID_DATABASE_TYPE, false);
		create = false;
		if (!Const.isEmpty(sql))
		{
			create = sql.toUpperCase().indexOf("CREATE TABLE")>=0;
        	statements.add(sql);
        	if (!dryrun) {
				try
				{
	                if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
					database.execStatements(sql);
	                if (log.isDetailed()) log.logDetailed(toString(), "Created/altered table " + schemaTable);
				}
				catch (KettleException dbe)
				{
					throw new KettleException("Unable to create or modify table " + schemaTable, dbe);
				}
        	}
		}
		else
		{
            if (log.isDetailed()) log.logDetailed(toString(), "Table " + schemaTable + " is OK.");
		}

		if (ok_database_type)
		{
			//
			// Populate...
			//
			code = DatabaseMeta.getDBTypeDescList();
			desc = DatabaseMeta.getDBTypeDescLongList();

			if (!dryrun) {
				database.prepareInsert(table, null, tablename);
			}

			for (int i = 1; i < code.length; i++)
			{
				RowMetaAndData lookup = null;
                if (upgrade) lookup = database.getOneRow("SELECT "+repository.quote(Repository.FIELD_DATABASE_TYPE_ID_DATABASE_TYPE)+" FROM " + schemaTable + " WHERE " + repository.quote(Repository.FIELD_DATABASE_TYPE_CODE) +" = '" + code[i] + "'");
				if (lookup == null)
				{
					long nextid = i;
					if (!create) {
						nextid = repository.getNextDatabaseTypeID();
					}

					Object[] tableData = new Object[] { new Long(nextid), code[i], desc[i], };
					
					if (dryrun) {
		            	sql = database.getSQLOutput(null, tablename, table, tableData, null);
		            	statements.add(sql);
					} else {
						database.setValuesInsert(table, tableData);
						database.insertRow();
					}
				}
			}

			try
			{
				if (!dryrun) {
					database.closeInsert();
				}
                if (log.isDetailed()) log.logDetailed(toString(), "Populated table " + schemaTable);
			}
			catch (KettleException dbe)
			{
                throw new KettleException("Unable to close insert after populating table " + schemaTable, dbe);
			}
		}
		if (monitor!=null) monitor.worked(1);

		//////////////////////////////////////////////////////////////////////////////////
		//
		// R_DATABASE_CONTYPE
		//
		// Create table...
		// 
		boolean ok_database_contype = true;
		table = new RowMeta();
		tablename = Repository.TABLE_R_DATABASE_CONTYPE;
		schemaTable = databaseMeta.getQuotedSchemaTableCombination(null, tablename);
		if (monitor!=null) monitor.subTask("Checking table "+schemaTable);
		table.addValueMeta(new ValueMeta(Repository.FIELD_DATABASE_CONTYPE_ID_DATABASE_CONTYPE, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_DATABASE_CONTYPE_CODE, ValueMetaInterface.TYPE_STRING, Repository.REP_STRING_CODE_LENGTH, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_DATABASE_CONTYPE_DESCRIPTION, ValueMetaInterface.TYPE_STRING, Repository.REP_STRING_CODE_LENGTH, 0));
		sql = database.getDDL(schemaTable, table, null, false, Repository.FIELD_DATABASE_CONTYPE_ID_DATABASE_CONTYPE, false);

		if (!Const.isEmpty(sql))
		{
        	statements.add(sql);
        	if (!dryrun) {
	            if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
				database.execStatements(sql);
	            if (log.isDetailed()) log.logDetailed(toString(), "Created or altered table " + schemaTable);
        	}
		}
		else
		{
            if (log.isDetailed()) log.logDetailed(toString(), "Table " + schemaTable + " is OK.");
		}

		if (ok_database_contype)
		{
			//
			// Populate with data...
			//
			code = DatabaseMeta.dbAccessTypeCode;
			desc = DatabaseMeta.dbAccessTypeDesc;

			if (!dryrun) {
				database.prepareInsert(table, null, tablename);
			}

			for (int i = 0; i < code.length; i++)
			{
                RowMetaAndData lookup = null;
                if (upgrade) lookup = database.getOneRow("SELECT "+repository.quote(Repository.FIELD_DATABASE_CONTYPE_ID_DATABASE_CONTYPE)+" FROM " + schemaTable + " WHERE " 
                		+ repository.quote(Repository.FIELD_DATABASE_CONTYPE_CODE) + " = '" + code[i] + "'");
				if (lookup == null)
				{
					long nextid = i+1;
					if (!create) {
						nextid = repository.getNextDatabaseConnectionTypeID();
					}

                    Object[] tableData = new Object[] { 
                            new Long(nextid),
                            code[i],
                            desc[i],
                    };
					if (dryrun) {
		            	sql = database.getSQLOutput(null, tablename, table, tableData, null);
		            	statements.add(sql);
					} else {
						database.setValuesInsert(table, tableData);
						database.insertRow();
					}
				}
			}

            try
            {
                if (!dryrun) {
                	database.closeInsert();
                }
                if (log.isDetailed()) log.logDetailed(toString(), "Populated table " + schemaTable);
            }
            catch(KettleException dbe)
            {
                throw new KettleException("Unable to close insert after populating table " + schemaTable, dbe);
            }
		}
		if (monitor!=null) monitor.worked(1);

		//////////////////////////////////////////////////////////////////////////////////
		//
		// R_NOTE
		//
		// Create table...
		table = new RowMeta();
		tablename = Repository.TABLE_R_NOTE;
		schemaTable = databaseMeta.getQuotedSchemaTableCombination(null, tablename);
		if (monitor!=null) monitor.subTask("Checking table "+schemaTable);
		table.addValueMeta(new ValueMeta(Repository.FIELD_NOTE_ID_NOTE, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_NOTE_VALUE_STR, ValueMetaInterface.TYPE_STRING, Repository.REP_STRING_LENGTH, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_NOTE_GUI_LOCATION_X, ValueMetaInterface.TYPE_INTEGER, 6, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_NOTE_GUI_LOCATION_Y, ValueMetaInterface.TYPE_INTEGER, 6, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_NOTE_GUI_LOCATION_WIDTH, ValueMetaInterface.TYPE_INTEGER, 6, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_NOTE_GUI_LOCATION_HEIGHT, ValueMetaInterface.TYPE_INTEGER, 6, 0));
		sql = database.getDDL(schemaTable, table, null, false, Repository.FIELD_NOTE_ID_NOTE, false);
        
		if (!Const.isEmpty(sql))
		{
        	statements.add(sql);
        	if (!dryrun) {
	            if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
				database.execStatements(sql);
	            if (log.isDetailed()) log.logDetailed(toString(), "Created or altered table " + schemaTable);
        	}
		}
		else
		{
            if (log.isDetailed()) log.logDetailed(toString(), "Table " + schemaTable + " is OK.");
		}
		if (monitor!=null) monitor.worked(1);

		//////////////////////////////////////////////////////////////////////////////////
		//
		// R_DATABASE
		//
		// Create table...
		table = new RowMeta();
		tablename = Repository.TABLE_R_DATABASE;
		schemaTable = databaseMeta.getQuotedSchemaTableCombination(null, tablename);
		if (monitor!=null) monitor.subTask("Checking table "+schemaTable);
		table.addValueMeta(new ValueMeta(Repository.FIELD_DATABASE_ID_DATABASE, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_DATABASE_NAME, ValueMetaInterface.TYPE_STRING, Repository.REP_STRING_CODE_LENGTH, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_DATABASE_ID_DATABASE_TYPE, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_DATABASE_ID_DATABASE_CONTYPE, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_DATABASE_HOST_NAME, ValueMetaInterface.TYPE_STRING, Repository.REP_STRING_CODE_LENGTH, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_DATABASE_DATABASE_NAME, ValueMetaInterface.TYPE_STRING, Repository.REP_STRING_CODE_LENGTH, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_DATABASE_PORT, ValueMetaInterface.TYPE_INTEGER, 7, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_DATABASE_USERNAME, ValueMetaInterface.TYPE_STRING, Repository.REP_STRING_CODE_LENGTH, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_DATABASE_PASSWORD, ValueMetaInterface.TYPE_STRING, Repository.REP_STRING_CODE_LENGTH, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_DATABASE_SERVERNAME, ValueMetaInterface.TYPE_STRING, Repository.REP_STRING_CODE_LENGTH, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_DATABASE_DATA_TBS, ValueMetaInterface.TYPE_STRING, Repository.REP_STRING_CODE_LENGTH, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_DATABASE_INDEX_TBS, ValueMetaInterface.TYPE_STRING, Repository.REP_STRING_CODE_LENGTH, 0));
		sql = database.getDDL(schemaTable, table, null, false, Repository.FIELD_DATABASE_ID_DATABASE, false);
        
		if (!Const.isEmpty(sql))
		{
        	statements.add(sql);
        	if (!dryrun) {
        		if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
        		database.execStatements(sql);
        		if (log.isDetailed()) log.logDetailed(toString(), "Created or altered table " + schemaTable);
        	}
		}
		else
		{
            if (log.isDetailed()) log.logDetailed(toString(), "Table " + schemaTable + " is OK.");
		}
		if (monitor!=null) monitor.worked(1);

        //////////////////////////////////////////////////////////////////////////////////
        //
        // R_DATABASE_ATTRIBUTE
        //
        // Create table...
        table = new RowMeta();
        tablename = Repository.TABLE_R_DATABASE_ATTRIBUTE;
        schemaTable = databaseMeta.getQuotedSchemaTableCombination(null, tablename);
        if (monitor!=null) monitor.subTask("Checking table "+schemaTable);
        table.addValueMeta(new ValueMeta(Repository.FIELD_DATABASE_ATTRIBUTE_ID_DATABASE_ATTRIBUTE, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
        table.addValueMeta(new ValueMeta(Repository.FIELD_DATABASE_ATTRIBUTE_ID_DATABASE, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
        table.addValueMeta(new ValueMeta(Repository.FIELD_DATABASE_ATTRIBUTE_CODE, ValueMetaInterface.TYPE_STRING, Repository.REP_STRING_CODE_LENGTH, 0));
        table.addValueMeta(new ValueMeta(Repository.FIELD_DATABASE_ATTRIBUTE_VALUE_STR, ValueMetaInterface.TYPE_STRING, Repository.REP_STRING_LENGTH, 0));
        sql = database.getDDL(schemaTable, table, null, false, Repository.FIELD_DATABASE_ATTRIBUTE_ID_DATABASE_ATTRIBUTE, false);
        
        if (!Const.isEmpty(sql))
        {
        	statements.add(sql);
        	if (!dryrun) {
	            if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
	            database.execStatements(sql);
	            if (log.isDetailed()) log.logDetailed(toString(), "Created or altered table " + schemaTable);
        	}            
            try
            {
                indexname = "IDX_" + schemaTable.substring(2) + "_AK";
                keyfield = new String[] { Repository.FIELD_DATABASE_ATTRIBUTE_ID_DATABASE, Repository.FIELD_DATABASE_ATTRIBUTE_CODE, };
                if (!database.checkIndexExists(schemaTable, keyfield))
                {
                    sql = database.getCreateIndexStatement(schemaTable, indexname, keyfield, false, true, false, false);
                	statements.add(sql);
                	if (!dryrun) {
	                    if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
	                    database.execStatements(sql);
	                    if (log.isDetailed()) log.logDetailed(toString(), "Created lookup index " + indexname + " on " + schemaTable);
                	}
                }
            }
            catch(KettleException kdbe)
            {
                // Ignore this one: index is not properly detected, it already exists...
            }

        }
        else
        {
            if (log.isDetailed()) log.logDetailed(toString(), "Table " + schemaTable + " is OK.");
        }
        if (monitor!=null) monitor.worked(1);

		//////////////////////////////////////////////////////////////////////////////////
		//
		// R_DIRECTORY
		//
		// Create table...
		table = new RowMeta();
		tablename = Repository.TABLE_R_DIRECTORY;
		schemaTable = databaseMeta.getQuotedSchemaTableCombination(null, tablename);
		if (monitor!=null) monitor.subTask("Checking table "+schemaTable);
		table.addValueMeta(new ValueMeta(Repository.FIELD_DIRECTORY_ID_DIRECTORY,        ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_DIRECTORY_ID_DIRECTORY_PARENT, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_DIRECTORY_DIRECTORY_NAME,      ValueMetaInterface.TYPE_STRING, Repository.REP_STRING_CODE_LENGTH, 0));
		sql = database.getDDL(schemaTable, table, null, false, Repository.FIELD_DIRECTORY_ID_DIRECTORY, false);

		if (!Const.isEmpty(sql))
		{
        	statements.add(sql);
        	if (!dryrun) {
	            if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
				database.execStatements(sql);
	            if (log.isDetailed()) log.logDetailed(toString(), "Created or altered table " + schemaTable);
        	}
        	
			try
			{
				indexname = "IDX_" + schemaTable.substring(2) + "_AK";
				keyfield = new String[] { Repository.FIELD_DIRECTORY_ID_DIRECTORY_PARENT, Repository.FIELD_DIRECTORY_DIRECTORY_NAME };
				if (!database.checkIndexExists(schemaTable, keyfield))
				{
					sql = database.getCreateIndexStatement(schemaTable, indexname, keyfield, false, true, false, false);
		        	statements.add(sql);
		        	if (!dryrun) {
	                    if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
						database.execStatements(sql);
	                    if (log.isDetailed()) log.logDetailed(toString(), "Created lookup index " + indexname + " on " + schemaTable);
		        	}
				}
			}
			catch(KettleException kdbe)
			{
				// Ignore this one: index is not properly detected, it already exists...
			}

		}
		else
		{
            if (log.isDetailed()) log.logDetailed(toString(), "Table " + schemaTable + " is OK.");
		}
		if (monitor!=null) monitor.worked(1);

		//////////////////////////////////////////////////////////////////////////////////
		//
		// R_TRANSFORMATION
		//
		// Create table...
		table = new RowMeta();
		tablename = Repository.TABLE_R_TRANSFORMATION;
		schemaTable = databaseMeta.getQuotedSchemaTableCombination(null, tablename);
		if (monitor!=null) monitor.subTask("Checking table "+schemaTable);
		table.addValueMeta(new ValueMeta(Repository.FIELD_TRANSFORMATION_ID_TRANSFORMATION, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_TRANSFORMATION_ID_DIRECTORY, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_TRANSFORMATION_NAME, ValueMetaInterface.TYPE_STRING, Repository.REP_STRING_CODE_LENGTH, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_TRANSFORMATION_DESCRIPTION, ValueMetaInterface.TYPE_STRING, Repository.REP_STRING_LENGTH, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_TRANSFORMATION_EXTENDED_DESCRIPTION, ValueMetaInterface.TYPE_STRING, Repository.REP_STRING_LENGTH, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_TRANSFORMATION_TRANS_VERSION, ValueMetaInterface.TYPE_STRING, Repository.REP_STRING_CODE_LENGTH, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_TRANSFORMATION_TRANS_STATUS, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_TRANSFORMATION_ID_STEP_READ, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_TRANSFORMATION_ID_STEP_WRITE, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_TRANSFORMATION_ID_STEP_INPUT, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_TRANSFORMATION_ID_STEP_OUTPUT, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_TRANSFORMATION_ID_STEP_UPDATE, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_TRANSFORMATION_ID_DATABASE_LOG, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_TRANSFORMATION_TABLE_NAME_LOG, ValueMetaInterface.TYPE_STRING, Repository.REP_STRING_CODE_LENGTH, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_TRANSFORMATION_USE_BATCHID, ValueMetaInterface.TYPE_BOOLEAN, 1, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_TRANSFORMATION_USE_LOGFIELD, ValueMetaInterface.TYPE_BOOLEAN, 1, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_TRANSFORMATION_ID_DATABASE_MAXDATE, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_TRANSFORMATION_TABLE_NAME_MAXDATE, ValueMetaInterface.TYPE_STRING, Repository.REP_STRING_CODE_LENGTH, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_TRANSFORMATION_FIELD_NAME_MAXDATE, ValueMetaInterface.TYPE_STRING, Repository.REP_STRING_CODE_LENGTH, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_TRANSFORMATION_OFFSET_MAXDATE, ValueMetaInterface.TYPE_NUMBER, 12, 2));
		table.addValueMeta(new ValueMeta(Repository.FIELD_TRANSFORMATION_DIFF_MAXDATE, ValueMetaInterface.TYPE_NUMBER, 12, 2));
		table.addValueMeta(new ValueMeta(Repository.FIELD_TRANSFORMATION_CREATED_USER, ValueMetaInterface.TYPE_STRING, Repository.REP_STRING_CODE_LENGTH, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_TRANSFORMATION_CREATED_DATE, ValueMetaInterface.TYPE_DATE, 20, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_TRANSFORMATION_MODIFIED_USER, ValueMetaInterface.TYPE_STRING, Repository.REP_STRING_CODE_LENGTH, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_TRANSFORMATION_MODIFIED_DATE, ValueMetaInterface.TYPE_DATE, 20, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_TRANSFORMATION_SIZE_ROWSET, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		sql = database.getDDL(schemaTable, table, null, false, Repository.FIELD_TRANSFORMATION_ID_TRANSFORMATION, false);

        if (!Const.isEmpty(sql))
		{
        	statements.add(sql);
        	if (!dryrun) {
	            if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
				database.execStatements(sql);
	            if (log.isDetailed()) log.logDetailed(toString(), "Created or altered table " + schemaTable);
        	}
		}
		else
		{
            if (log.isDetailed()) log.logDetailed(toString(), "Table " + schemaTable + " is OK.");
		}

		// In case of an update, the added column R_TRANSFORMATION.ID_DIRECTORY == NULL!!!
        //
        if (database.checkTableExists(schemaTable)) {
	        sql = "SELECT * FROM "+schemaTable+" WHERE "+repository.quote(Repository.FIELD_TRANSFORMATION_ID_DIRECTORY)+" IS NULL";
	        List<Object[]> rows = database.getRows(sql, 1);
	        if (rows!=null && rows.size()>0) {
		        sql = "UPDATE " + schemaTable + " SET "+repository.quote(Repository.FIELD_TRANSFORMATION_ID_DIRECTORY)+"=0 WHERE "+repository.quote(Repository.FIELD_TRANSFORMATION_ID_DIRECTORY)+" IS NULL";
		        statements.add(sql);
				if (!dryrun) {
					database.execStatement(sql);
				}
	        }
        }

		if (monitor!=null) monitor.worked(1);

		//////////////////////////////////////////////////////////////////////////////////
		//
		// R_TRANS_ATTRIBUTE
		//
		// Create table...
		table = new RowMeta();
		tablename = Repository.TABLE_R_TRANS_ATTRIBUTE;
		schemaTable = databaseMeta.getQuotedSchemaTableCombination(null, tablename);
		if (monitor!=null) monitor.subTask("Checking table "+schemaTable);
		table.addValueMeta(new ValueMeta(Repository.FIELD_TRANS_ATTRIBUTE_ID_TRANS_ATTRIBUTE, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_TRANS_ATTRIBUTE_ID_TRANSFORMATION, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_TRANS_ATTRIBUTE_NR, ValueMetaInterface.TYPE_INTEGER, 6, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_TRANS_ATTRIBUTE_CODE, ValueMetaInterface.TYPE_STRING, Repository.REP_STRING_CODE_LENGTH, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_TRANS_ATTRIBUTE_VALUE_NUM, ValueMetaInterface.TYPE_INTEGER, 18, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_TRANS_ATTRIBUTE_VALUE_STR, ValueMetaInterface.TYPE_STRING, Repository.REP_STRING_LENGTH, 0));
		sql = database.getDDL(schemaTable, table, null, false, Repository.FIELD_TRANS_ATTRIBUTE_ID_TRANS_ATTRIBUTE, false);

		if (!Const.isEmpty(sql))
		{
	        statements.add(sql);
			if (!dryrun) {
	            if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
				database.execStatements(sql);
	            if (log.isDetailed()) log.logDetailed(toString(), "Created or altered table " + schemaTable);
			}
			try
			{
				indexname = "IDX_TRANS_ATTRIBUTE_LOOKUP";
				keyfield = new String[] { Repository.FIELD_TRANS_ATTRIBUTE_ID_TRANSFORMATION, Repository.FIELD_TRANS_ATTRIBUTE_CODE, Repository.FIELD_TRANS_ATTRIBUTE_NR };

				if (!database.checkIndexExists(schemaTable, keyfield))
				{
					sql = database.getCreateIndexStatement(schemaTable, indexname, keyfield, false, true, false, false);
		        	statements.add(sql);
		        	if (!dryrun) {
	                    if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
						database.execStatements(sql);
	                    if (log.isDetailed()) log.logDetailed(toString(), "Created lookup index " + indexname + " on " + schemaTable);
		        	}
				}
			}
			catch(KettleException kdbe)
			{
				// Ignore this one: index is not properly detected, it already exists...
			}
		}
		else
		{
            if (log.isDetailed()) log.logDetailed(toString(), "Table " + schemaTable + " is OK.");
		}
		if (monitor!=null) monitor.worked(1);

		
		//////////////////////////////////////////////////////////////////////////////////
		//
		// R_JOB_ATTRIBUTE
		//
		// Create table...
		table = new RowMeta();
		tablename = Repository.TABLE_R_JOB_ATTRIBUTE;
		schemaTable = databaseMeta.getQuotedSchemaTableCombination(null, tablename);
		if (monitor!=null) monitor.subTask("Checking table "+schemaTable);
		table.addValueMeta(new ValueMeta(Repository.FIELD_JOB_ATTRIBUTE_ID_JOB_ATTRIBUTE, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_JOB_ATTRIBUTE_ID_JOB, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_JOB_ATTRIBUTE_NR, ValueMetaInterface.TYPE_INTEGER, 6, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_JOB_ATTRIBUTE_CODE, ValueMetaInterface.TYPE_STRING, Repository.REP_STRING_CODE_LENGTH, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_JOB_ATTRIBUTE_VALUE_NUM, ValueMetaInterface.TYPE_INTEGER, 18, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_JOB_ATTRIBUTE_VALUE_STR, ValueMetaInterface.TYPE_STRING, Repository.REP_STRING_LENGTH, 0));
		sql = database.getDDL(schemaTable, table, null, false, Repository.FIELD_JOB_ATTRIBUTE_ID_JOB_ATTRIBUTE, false);

		if (!Const.isEmpty(sql))
		{
	        statements.add(sql);
			if (!dryrun) {
	            if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
				database.execStatements(sql);
	            if (log.isDetailed()) log.logDetailed(toString(), "Created or altered table " + schemaTable);
			}
			try
			{
				indexname = "IDX_JOB_ATTRIBUTE_LOOKUP";
				keyfield = new String[] { Repository.FIELD_JOB_ATTRIBUTE_ID_JOB, Repository.FIELD_JOB_ATTRIBUTE_CODE, Repository.FIELD_JOB_ATTRIBUTE_NR };

				if (!database.checkIndexExists(schemaTable, keyfield))
				{
					sql = database.getCreateIndexStatement(schemaTable, indexname, keyfield, false, true, false, false);
		        	statements.add(sql);
		        	if (!dryrun) {
	                    if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
						database.execStatements(sql);
	                    if (log.isDetailed()) log.logDetailed(toString(), "Created lookup index " + indexname + " on " + schemaTable);
		        	}
				}
			}
			catch(KettleException kdbe)
			{
				// Ignore this one: index is not properly detected, it already exists...
			}
		}
		else
		{
            if (log.isDetailed()) log.logDetailed(toString(), "Table " + schemaTable + " is OK.");
		}
		if (monitor!=null) monitor.worked(1);		
		
		//////////////////////////////////////////////////////////////////////////////////
		//
		// R_DEPENDENCY
		//
		// Create table...
		table = new RowMeta();
		tablename = Repository.TABLE_R_DEPENDENCY;
		schemaTable = databaseMeta.getQuotedSchemaTableCombination(null, tablename);
		if (monitor!=null) monitor.subTask("Checking table "+schemaTable);
		table.addValueMeta(new ValueMeta(Repository.FIELD_DEPENDENCY_ID_DEPENDENCY, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_DEPENDENCY_ID_TRANSFORMATION, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_DEPENDENCY_ID_DATABASE, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_DEPENDENCY_TABLE_NAME, ValueMetaInterface.TYPE_STRING, Repository.REP_STRING_CODE_LENGTH, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_DEPENDENCY_FIELD_NAME, ValueMetaInterface.TYPE_STRING, Repository.REP_STRING_CODE_LENGTH, 0));
		sql = database.getDDL(schemaTable, table, null, false, Repository.FIELD_DEPENDENCY_ID_DEPENDENCY, false);

		if (!Const.isEmpty(sql))
		{
        	statements.add(sql);
        	if (!dryrun) {
	            if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
				database.execStatements(sql);
	            if (log.isDetailed()) log.logDetailed(toString(), "Created or altered table " + schemaTable);
        	}
		}
		else
		{
            if (log.isDetailed()) log.logDetailed(toString(), "Table " + schemaTable + " is OK.");
		}
		if (monitor!=null) monitor.worked(1);

        //////////////////////////////////////////////////////////////////////////////////
        //
        // R_PARTITION_SCHEMA
        //
        // Create table...
        table = new RowMeta();
        tablename = Repository.TABLE_R_PARTITION_SCHEMA;
        schemaTable = databaseMeta.getQuotedSchemaTableCombination(null, tablename);
        if (monitor!=null) monitor.subTask("Checking table "+schemaTable);
        table.addValueMeta(new ValueMeta(Repository.FIELD_PARTITION_SCHEMA_ID_PARTITION_SCHEMA, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
        table.addValueMeta(new ValueMeta(Repository.FIELD_PARTITION_SCHEMA_NAME, ValueMetaInterface.TYPE_STRING, Repository.REP_STRING_CODE_LENGTH, 0));
        table.addValueMeta(new ValueMeta(Repository.FIELD_PARTITION_SCHEMA_DYNAMIC_DEFINITION, ValueMetaInterface.TYPE_BOOLEAN, 1, 0));
        table.addValueMeta(new ValueMeta(Repository.FIELD_PARTITION_SCHEMA_PARTITIONS_PER_SLAVE, ValueMetaInterface.TYPE_STRING, Repository.REP_STRING_CODE_LENGTH, 0));
        sql = database.getDDL(schemaTable, table, null, false, Repository.FIELD_PARTITION_SCHEMA_ID_PARTITION_SCHEMA, false);

        if (!Const.isEmpty(sql))
        {
        	statements.add(sql);
        	if (!dryrun) {
	            if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
	            database.execStatements(sql);
	            if (log.isDetailed()) log.logDetailed(toString(), "Created or altered table " + schemaTable);
        	}
        }
        else
        {
            if (log.isDetailed()) log.logDetailed(toString(), "Table " + schemaTable + " is OK.");
        }
        if (monitor!=null) monitor.worked(1);
        
        //////////////////////////////////////////////////////////////////////////////////
        //
        // R_PARTITION
        //
        // Create table...
        table = new RowMeta();
        tablename = Repository.TABLE_R_PARTITION;
        schemaTable = databaseMeta.getQuotedSchemaTableCombination(null, tablename);
        if (monitor!=null) monitor.subTask("Checking table "+schemaTable);
        table.addValueMeta(new ValueMeta(Repository.FIELD_PARTITION_ID_PARTITION, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
        table.addValueMeta(new ValueMeta(Repository.FIELD_PARTITION_ID_PARTITION_SCHEMA, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
        table.addValueMeta(new ValueMeta(Repository.FIELD_PARTITION_PARTITION_ID, ValueMetaInterface.TYPE_STRING, Repository.REP_STRING_CODE_LENGTH, 0));
        sql = database.getDDL(schemaTable, table, null, false, Repository.FIELD_PARTITION_ID_PARTITION, false);

        if (!Const.isEmpty(sql))
        {
        	statements.add(sql);
        	if (!dryrun) {
	            if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
	            database.execStatements(sql);
	            if (log.isDetailed()) log.logDetailed(toString(), "Created or altered table " + schemaTable);
        	}
        }
        else
        {
            if (log.isDetailed()) log.logDetailed(toString(), "Table " + schemaTable + " is OK.");
        }
        if (monitor!=null) monitor.worked(1);

        //////////////////////////////////////////////////////////////////////////////////
        //
        // R_TRANS_PARTITION_SCHEMA
        //
        // Create table...
        table = new RowMeta();
        tablename = Repository.TABLE_R_TRANS_PARTITION_SCHEMA;
        schemaTable = databaseMeta.getQuotedSchemaTableCombination(null, tablename);
        if (monitor!=null) monitor.subTask("Checking table "+schemaTable);
        table.addValueMeta(new ValueMeta(Repository.FIELD_TRANS_PARTITION_SCHEMA_ID_TRANS_PARTITION_SCHEMA, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
        table.addValueMeta(new ValueMeta(Repository.FIELD_TRANS_PARTITION_SCHEMA_ID_TRANSFORMATION, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
        table.addValueMeta(new ValueMeta(Repository.FIELD_TRANS_PARTITION_SCHEMA_ID_PARTITION_SCHEMA, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
        sql = database.getDDL(schemaTable, table, null, false, Repository.FIELD_TRANS_PARTITION_SCHEMA_ID_TRANS_PARTITION_SCHEMA, false);

        if (!Const.isEmpty(sql))
        {
        	statements.add(sql);
        	if (!dryrun) {
	            if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
	            database.execStatements(sql);
	            if (log.isDetailed()) log.logDetailed(toString(), "Created or altered table " + schemaTable);
        	}
        }
        else
        {
            if (log.isDetailed()) log.logDetailed(toString(), "Table " + schemaTable + " is OK.");
        }
        if (monitor!=null) monitor.worked(1);


        //////////////////////////////////////////////////////////////////////////////////
        //
        // R_CLUSTER
        //
        // Create table...
        table = new RowMeta();
        tablename = Repository.TABLE_R_CLUSTER;
        schemaTable = databaseMeta.getQuotedSchemaTableCombination(null, tablename);
        if (monitor!=null) monitor.subTask("Checking table "+schemaTable);
        table.addValueMeta(new ValueMeta(Repository.FIELD_CLUSTER_ID_CLUSTER, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
        table.addValueMeta(new ValueMeta(Repository.FIELD_CLUSTER_NAME, ValueMetaInterface.TYPE_STRING, Repository.REP_STRING_CODE_LENGTH, 0));
        table.addValueMeta(new ValueMeta(Repository.FIELD_CLUSTER_BASE_PORT, ValueMetaInterface.TYPE_STRING, Repository.REP_STRING_CODE_LENGTH, 0));
        table.addValueMeta(new ValueMeta(Repository.FIELD_CLUSTER_SOCKETS_BUFFER_SIZE, ValueMetaInterface.TYPE_STRING, Repository.REP_STRING_CODE_LENGTH, 0));
        table.addValueMeta(new ValueMeta(Repository.FIELD_CLUSTER_SOCKETS_FLUSH_INTERVAL, ValueMetaInterface.TYPE_STRING, Repository.REP_STRING_CODE_LENGTH, 0));
        table.addValueMeta(new ValueMeta(Repository.FIELD_CLUSTER_SOCKETS_COMPRESSED, ValueMetaInterface.TYPE_BOOLEAN, 0, 0));
        table.addValueMeta(new ValueMeta(Repository.FIELD_CLUSTER_DYNAMIC, ValueMetaInterface.TYPE_BOOLEAN, 0, 0));
        sql = database.getDDL(schemaTable, table, null, false, Repository.FIELD_CLUSTER_ID_CLUSTER, false);

        if (!Const.isEmpty(sql))
        {
        	statements.add(sql);
        	if (!dryrun) {
	            if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
	            database.execStatements(sql);
	            if (log.isDetailed()) log.logDetailed(toString(), "Created or altered table " + schemaTable);
        	}
        }
        else
        {
            if (log.isDetailed()) log.logDetailed(toString(), "Table " + schemaTable + " is OK.");
        }
        if (monitor!=null) monitor.worked(1);
        
        //////////////////////////////////////////////////////////////////////////////////
        //
        // R_SLAVE
        //
        // Create table...
        table = new RowMeta();
        tablename = Repository.TABLE_R_SLAVE;
        schemaTable = databaseMeta.getQuotedSchemaTableCombination(null, tablename);
        if (monitor!=null) monitor.subTask("Checking table "+schemaTable);
        table.addValueMeta(new ValueMeta(Repository.FIELD_SLAVE_ID_SLAVE, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
        table.addValueMeta(new ValueMeta(Repository.FIELD_SLAVE_NAME, ValueMetaInterface.TYPE_STRING, Repository.REP_STRING_CODE_LENGTH, 0));
        table.addValueMeta(new ValueMeta(Repository.FIELD_SLAVE_HOST_NAME, ValueMetaInterface.TYPE_STRING, Repository.REP_STRING_CODE_LENGTH, 0));
        table.addValueMeta(new ValueMeta(Repository.FIELD_SLAVE_PORT, ValueMetaInterface.TYPE_STRING, Repository.REP_STRING_CODE_LENGTH, 0));
        table.addValueMeta(new ValueMeta(Repository.FIELD_SLAVE_USERNAME, ValueMetaInterface.TYPE_STRING, Repository.REP_STRING_CODE_LENGTH, 0));
        table.addValueMeta(new ValueMeta(Repository.FIELD_SLAVE_PASSWORD, ValueMetaInterface.TYPE_STRING, Repository.REP_STRING_CODE_LENGTH, 0));
        table.addValueMeta(new ValueMeta(Repository.FIELD_SLAVE_PROXY_HOST_NAME, ValueMetaInterface.TYPE_STRING, Repository.REP_STRING_CODE_LENGTH, 0));
        table.addValueMeta(new ValueMeta(Repository.FIELD_SLAVE_PROXY_PORT, ValueMetaInterface.TYPE_STRING, Repository.REP_STRING_CODE_LENGTH, 0));
        table.addValueMeta(new ValueMeta(Repository.FIELD_SLAVE_NON_PROXY_HOSTS, ValueMetaInterface.TYPE_STRING, Repository.REP_STRING_CODE_LENGTH, 0));
        table.addValueMeta(new ValueMeta(Repository.FIELD_SLAVE_MASTER, ValueMetaInterface.TYPE_BOOLEAN));
        sql = database.getDDL(schemaTable, table, null, false, Repository.FIELD_SLAVE_ID_SLAVE, false);

        if (!Const.isEmpty(sql))
        {
        	statements.add(sql);
        	if (!dryrun) {
	            if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
	            database.execStatements(sql);
	            if (log.isDetailed()) log.logDetailed(toString(), "Created or altered table " + schemaTable);
        	}
        }
        else
        {
            if (log.isDetailed()) log.logDetailed(toString(), "Table " + schemaTable + " is OK.");
        }
        if (monitor!=null) monitor.worked(1);

        //////////////////////////////////////////////////////////////////////////////////
        //
        // R_CLUSTER_SLAVE
        //
        // Create table...
        table = new RowMeta();
        tablename = Repository.TABLE_R_CLUSTER_SLAVE;
        schemaTable = databaseMeta.getQuotedSchemaTableCombination(null, tablename);
        if (monitor!=null) monitor.subTask("Checking table "+schemaTable);
        table.addValueMeta(new ValueMeta(Repository.FIELD_CLUSTER_SLAVE_ID_CLUSTER_SLAVE, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
        table.addValueMeta(new ValueMeta(Repository.FIELD_CLUSTER_SLAVE_ID_CLUSTER, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
        table.addValueMeta(new ValueMeta(Repository.FIELD_CLUSTER_SLAVE_ID_SLAVE, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
        sql = database.getDDL(schemaTable, table, null, false, Repository.FIELD_CLUSTER_SLAVE_ID_CLUSTER_SLAVE, false);

        if (!Const.isEmpty(sql))
        {
        	statements.add(sql);
        	if (!dryrun) {
	            if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
	            database.execStatements(sql);
	            if (log.isDetailed()) log.logDetailed(toString(), "Created or altered table " + schemaTable);
        	}
        }
        else
        {
            if (log.isDetailed()) log.logDetailed(toString(), "Table " + schemaTable + " is OK.");
        }
        if (monitor!=null) monitor.worked(1);

        //////////////////////////////////////////////////////////////////////////////////
        //
        // R_TRANS_SLAVE
        //
        // Create table...
        table = new RowMeta();
        tablename = Repository.TABLE_R_TRANS_SLAVE;
        schemaTable = databaseMeta.getQuotedSchemaTableCombination(null, tablename);
        if (monitor!=null) monitor.subTask("Checking table "+schemaTable);
        table.addValueMeta(new ValueMeta(Repository.FIELD_TRANS_SLAVE_ID_TRANS_SLAVE, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
        table.addValueMeta(new ValueMeta(Repository.FIELD_TRANS_SLAVE_ID_TRANSFORMATION, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
        table.addValueMeta(new ValueMeta(Repository.FIELD_TRANS_SLAVE_ID_SLAVE, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
        sql = database.getDDL(schemaTable, table, null, false, Repository.FIELD_TRANS_SLAVE_ID_TRANS_SLAVE, false);

        if (!Const.isEmpty(sql))
        {
        	statements.add(sql);
        	if (!dryrun) {
	            if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
	            database.execStatements(sql);
	            if (log.isDetailed()) log.logDetailed(toString(), "Created or altered table " + schemaTable);
        	}
        }
        else
        {
            if (log.isDetailed()) log.logDetailed(toString(), "Table " + schemaTable + " is OK.");
        }
        if (monitor!=null) monitor.worked(1);


        //////////////////////////////////////////////////////////////////////////////////
        //
        // R_TRANS_CLUSTER
        //
        // Create table...
        table = new RowMeta();
        tablename = Repository.TABLE_R_TRANS_CLUSTER;
        schemaTable = databaseMeta.getQuotedSchemaTableCombination(null, tablename);
        if (monitor!=null) monitor.subTask("Checking table "+schemaTable);
        table.addValueMeta(new ValueMeta(Repository.FIELD_TRANS_CLUSTER_ID_TRANS_CLUSTER, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
        table.addValueMeta(new ValueMeta(Repository.FIELD_TRANS_CLUSTER_ID_TRANSFORMATION, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
        table.addValueMeta(new ValueMeta(Repository.FIELD_TRANS_CLUSTER_ID_CLUSTER, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
        sql = database.getDDL(schemaTable, table, null, false, Repository.FIELD_TRANS_CLUSTER_ID_TRANS_CLUSTER, false);

        if (!Const.isEmpty(sql))
        {
        	statements.add(sql);
        	if (!dryrun) {
	            if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
	            database.execStatements(sql);
	            if (log.isDetailed()) log.logDetailed(toString(), "Created or altered table " + schemaTable);
        	}
        }
        else
        {
            if (log.isDetailed()) log.logDetailed(toString(), "Table " + schemaTable + " is OK.");
        }
        if (monitor!=null) monitor.worked(1);

		//
		// R_TRANS_HOP
		//
		// Create table...
		table = new RowMeta();
		tablename = Repository.TABLE_R_TRANS_HOP;
		schemaTable = databaseMeta.getQuotedSchemaTableCombination(null, tablename);
		if (monitor!=null) monitor.subTask("Checking table "+schemaTable);
		table.addValueMeta(new ValueMeta(Repository.FIELD_TRANS_HOP_ID_TRANS_HOP, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_TRANS_HOP_ID_TRANSFORMATION, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_TRANS_HOP_ID_STEP_FROM, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_TRANS_HOP_ID_STEP_TO, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_TRANS_HOP_ENABLED, ValueMetaInterface.TYPE_BOOLEAN, 1, 0));
		sql = database.getDDL(schemaTable, table, null, false, Repository.FIELD_TRANS_HOP_ID_TRANS_HOP, false);

		if (!Const.isEmpty(sql))
		{
        	statements.add(sql);
        	if (!dryrun) {
	            if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
				database.execStatements(sql);
	            if (log.isDetailed()) log.logDetailed(toString(), "Created or altered table " + schemaTable);
        	}
		}
		else
		{
            if (log.isDetailed()) log.logDetailed(toString(), "Table " + schemaTable + " is OK.");
		}
		if (monitor!=null) monitor.worked(1);

		///////////////////////////////////////////////////////////////////////////////
		// R_TRANS_STEP_CONDITION
		//
		table = new RowMeta();
		tablename = Repository.TABLE_R_TRANS_STEP_CONDITION;
		schemaTable = databaseMeta.getQuotedSchemaTableCombination(null, tablename);
		if (monitor!=null) monitor.subTask("Checking table "+schemaTable);
		table.addValueMeta(new ValueMeta(Repository.FIELD_TRANS_STEP_CONDITION_ID_TRANSFORMATION, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_TRANS_STEP_CONDITION_ID_STEP, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_TRANS_STEP_CONDITION_ID_CONDITION, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		sql = database.getDDL(schemaTable, table, null, false, null, false);

		if (!Const.isEmpty(sql)) // Doesn't exists: create the table...
		{
        	statements.add(sql);
        	if (!dryrun) {
	            if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
				database.execStatements(sql);
	            if (log.isDetailed()) log.logDetailed(toString(), "Created or altered table " + schemaTable);
        	}
		}
		else
		{
            if (log.isDetailed()) log.logDetailed(toString(), "Table " + schemaTable + " is OK.");
		}
		if (monitor!=null) monitor.worked(1);

		///////////////////////////////////////////////////////////////////////////////
		// R_CONDITION
		//
		table = new RowMeta();
		tablename = Repository.TABLE_R_CONDITION;
		schemaTable = databaseMeta.getQuotedSchemaTableCombination(null, tablename);
		if (monitor!=null) monitor.subTask("Checking table "+schemaTable);
		table.addValueMeta(new ValueMeta(Repository.FIELD_CONDITION_ID_CONDITION, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_CONDITION_ID_CONDITION_PARENT, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_CONDITION_NEGATED, ValueMetaInterface.TYPE_BOOLEAN, 1, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_CONDITION_OPERATOR, ValueMetaInterface.TYPE_STRING, Repository.REP_STRING_CODE_LENGTH, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_CONDITION_LEFT_NAME, ValueMetaInterface.TYPE_STRING, Repository.REP_STRING_CODE_LENGTH, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_CONDITION_CONDITION_FUNCTION, ValueMetaInterface.TYPE_STRING, Repository.REP_STRING_CODE_LENGTH, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_CONDITION_RIGHT_NAME, ValueMetaInterface.TYPE_STRING, Repository.REP_STRING_CODE_LENGTH, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_CONDITION_ID_VALUE_RIGHT, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		sql = database.getDDL(schemaTable, table, null, false, Repository.FIELD_CONDITION_ID_CONDITION, false);

		if (!Const.isEmpty(sql)) // Doesn't exist: create the table...
		{
        	statements.add(sql);
        	if (!dryrun) {
	            if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
				database.execStatements(sql);
	            if (log.isDetailed()) log.logDetailed(toString(), "Created or altered table " + schemaTable);
        	}
		}
		else
		{
            if (log.isDetailed()) log.logDetailed(toString(), "Table " + schemaTable + " is OK.");
		}
		if (monitor!=null) monitor.worked(1);

		///////////////////////////////////////////////////////////////////////////////
		// R_VALUE
		//
		tablename = Repository.TABLE_R_VALUE;
		schemaTable = databaseMeta.getQuotedSchemaTableCombination(null, tablename);
		if (monitor!=null) monitor.subTask("Checking table "+schemaTable);
		table = new RowMeta();
		table.addValueMeta(new ValueMeta(Repository.FIELD_VALUE_ID_VALUE, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_VALUE_NAME, ValueMetaInterface.TYPE_STRING, Repository.REP_STRING_CODE_LENGTH, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_VALUE_VALUE_TYPE, ValueMetaInterface.TYPE_STRING, Repository.REP_STRING_CODE_LENGTH, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_VALUE_VALUE_STR, ValueMetaInterface.TYPE_STRING, Repository.REP_STRING_CODE_LENGTH, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_VALUE_IS_NULL, ValueMetaInterface.TYPE_BOOLEAN, 1, 0));
		sql = database.getDDL(schemaTable, table, null, false, Repository.FIELD_VALUE_ID_VALUE, false);

		if (!Const.isEmpty(sql)) // Doesn't exists: create the table...
		{
        	statements.add(sql);
        	if (!dryrun) {
	            if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
				database.execStatements(sql);
	            if (log.isDetailed()) log.logDetailed(toString(), "Created or altered table " + schemaTable);
        	}		
        }
		else
		{
            if (log.isDetailed()) log.logDetailed(toString(), "Table " + schemaTable + " is OK.");
		}
		if (monitor!=null) monitor.worked(1);

		//////////////////////////////////////////////////////////////////////////////////
		//
		// R_STEP_TYPE
		//
		// Create table...
		boolean ok_step_type = true;
		table = new RowMeta();
		tablename = Repository.TABLE_R_STEP_TYPE;
		schemaTable = databaseMeta.getQuotedSchemaTableCombination(null, tablename);
		if (monitor!=null) monitor.subTask("Checking table "+schemaTable);
		table.addValueMeta(new ValueMeta(Repository.FIELD_STEP_TYPE_ID_STEP_TYPE, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_STEP_TYPE_CODE, ValueMetaInterface.TYPE_STRING, Repository.REP_STRING_CODE_LENGTH, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_STEP_TYPE_DESCRIPTION, ValueMetaInterface.TYPE_STRING, Repository.REP_STRING_CODE_LENGTH, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_STEP_TYPE_HELPTEXT, ValueMetaInterface.TYPE_STRING, Repository.REP_STRING_CODE_LENGTH, 0));
		sql = database.getDDL(schemaTable, table, null, false, "ID_STEP_TYPE", false);
		create = false;
		if (!Const.isEmpty(sql)) // Doesn't exists: create the table...
		{
			create = sql.toUpperCase().indexOf("CREATE TABLE")>=0;
        	statements.add(sql);
        	if (!dryrun) {
	            if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
				database.execStatements(sql);
	            if (log.isDetailed()) log.logDetailed(toString(), "Created or altered table " + schemaTable);
        	}
		}
		else
		{
            if (log.isDetailed()) log.logDetailed(toString(), "Table " + schemaTable + " is OK.");
		}

		if (ok_step_type && !dryrun)
		{
			updateStepTypes(statements, dryrun, create);
            if (log.isDetailed()) log.logDetailed(toString(), "Populated table " + schemaTable);
		}
		if (monitor!=null) monitor.worked(1);

		//////////////////////////////////////////////////////////////////////////////////
		//
		// R_STEP
		//
		// Create table
		table = new RowMeta();
		tablename = Repository.TABLE_R_STEP;
		schemaTable = databaseMeta.getQuotedSchemaTableCombination(null, tablename);
		if (monitor!=null) monitor.subTask("Checking table "+schemaTable);
		table.addValueMeta(new ValueMeta(Repository.FIELD_STEP_ID_STEP, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_STEP_ID_TRANSFORMATION, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_STEP_NAME, ValueMetaInterface.TYPE_STRING, Repository.REP_STRING_CODE_LENGTH, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_STEP_DESCRIPTION, ValueMetaInterface.TYPE_STRING, Repository.REP_STRING_LENGTH, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_STEP_ID_STEP_TYPE, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_STEP_DISTRIBUTE, ValueMetaInterface.TYPE_BOOLEAN, 1, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_STEP_COPIES, ValueMetaInterface.TYPE_INTEGER, 3, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_STEP_GUI_LOCATION_X, ValueMetaInterface.TYPE_INTEGER, 6, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_STEP_GUI_LOCATION_Y, ValueMetaInterface.TYPE_INTEGER, 6, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_STEP_GUI_DRAW, ValueMetaInterface.TYPE_BOOLEAN, 1, 0));
		sql = database.getDDL(schemaTable, table, null, false, Repository.FIELD_STEP_ID_STEP, false);

		if (!Const.isEmpty(sql)) // Doesn't exists: create the table...
		{
        	statements.add(sql);
        	if (!dryrun) {
	            if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
				database.execStatements(sql);
	            if (log.isDetailed()) log.logDetailed(toString(), "Created or altered table " + schemaTable);
        	}
		}
		else
		{
            if (log.isDetailed()) log.logDetailed(toString(), "Table " + schemaTable + " is OK.");
		}
		if (monitor!=null) monitor.worked(1);

		//////////////////////////////////////////////////////////////////////////////////
		//
		// R_STEP_ATTRIBUTE
		//
		// Create table...
		tablename = Repository.TABLE_R_STEP_ATTRIBUTE;
		schemaTable = databaseMeta.getQuotedSchemaTableCombination(null, tablename);
		if (monitor!=null) monitor.subTask("Checking table "+schemaTable);
		table = new RowMeta();
		table.addValueMeta(new ValueMeta(Repository.FIELD_STEP_ATTRIBUTE_ID_STEP_ATTRIBUTE, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_STEP_ATTRIBUTE_ID_TRANSFORMATION, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_STEP_ATTRIBUTE_ID_STEP, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_STEP_ATTRIBUTE_NR, ValueMetaInterface.TYPE_INTEGER, 6, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_STEP_ATTRIBUTE_CODE, ValueMetaInterface.TYPE_STRING, Repository.REP_STRING_CODE_LENGTH, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_STEP_ATTRIBUTE_VALUE_NUM, ValueMetaInterface.TYPE_INTEGER, 18, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_STEP_ATTRIBUTE_VALUE_STR, ValueMetaInterface.TYPE_STRING, Repository.REP_STRING_LENGTH, 0));
        sql = database.getDDL(schemaTable, table, null, false, Repository.FIELD_STEP_ATTRIBUTE_ID_STEP_ATTRIBUTE, false);
        
		if (!Const.isEmpty(sql)) // Doesn't exist: create the table...
		{
        	statements.add(sql);
        	if (!dryrun) {
	            if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
				database.execStatements(sql);
	            if (log.isDetailed()) log.logDetailed(toString(), "Created or altered table " + schemaTable);
        	}

			try
			{
				indexname = "IDX_" + schemaTable.substring(2) + "_LOOKUP";
				keyfield = new String[] { Repository.FIELD_STEP_ATTRIBUTE_ID_STEP, Repository.FIELD_STEP_ATTRIBUTE_CODE, Repository.FIELD_STEP_ATTRIBUTE_NR, };
				if (!database.checkIndexExists(schemaTable, keyfield))
				{
					sql = database.getCreateIndexStatement(schemaTable, indexname, keyfield, false, true, false, false);
					statements.add(sql);
		        	if (!dryrun) {
	                    if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
						database.execStatements(sql);
	                    if (log.isDetailed()) log.logDetailed(toString(), "Created lookup index " + indexname + " on " + schemaTable);
		        	}
				}
			}
			catch(KettleException kdbe)
			{
				// Ignore this one: index is not properly detected, it already exists...
			}
		}
		else
		{
            if (log.isDetailed()) log.logDetailed(toString(), "Table " + schemaTable + " is OK.");
		}
		if (monitor!=null) monitor.worked(1);

		//////////////////////////////////////////////////////////////////////////////////
		//
		// R_STEP_DATABASE
		//
		// Keeps the links between transformation steps and databases.
		// That way investigating dependencies becomes easier to program.
		//
		// Create table...
		tablename = Repository.TABLE_R_STEP_DATABASE;
		schemaTable = databaseMeta.getQuotedSchemaTableCombination(null, tablename);
		if (monitor!=null) monitor.subTask("Checking table "+schemaTable);
		table = new RowMeta();
		table.addValueMeta(new ValueMeta(Repository.FIELD_STEP_DATABASE_ID_TRANSFORMATION, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_STEP_DATABASE_ID_STEP, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_STEP_DATABASE_ID_DATABASE, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		sql = database.getDDL(schemaTable, table, null, false, null, false);
        
		if (!Const.isEmpty(sql)) // Doesn't exist: create the table...
		{
        	statements.add(sql);
        	if (!dryrun) {
	            if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
				database.execStatements(sql);
	            if (log.isDetailed()) log.logDetailed(toString(), "Created or altered table " + schemaTable);
        	}

			try
			{
				indexname = "IDX_" + schemaTable.substring(2) + "_LU1";
				keyfield = new String[] { Repository.FIELD_STEP_DATABASE_ID_TRANSFORMATION, };
				if (!database.checkIndexExists(schemaTable, keyfield))
				{
					sql = database.getCreateIndexStatement(schemaTable, indexname, keyfield, false, false, false, false);
		        	statements.add(sql);
		        	if (!dryrun) {
	                    if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
						database.execStatements(sql);
	                    if (log.isDetailed()) log.logDetailed(toString(), "Created lookup index " + indexname + " on " + schemaTable);
		        	}
				}
			}
			catch(KettleException kdbe)
			{
				// Ignore this one: index is not properly detected, it already exists...
			}

			try
			{
				indexname = "IDX_" + schemaTable.substring(2) + "_LU2";
				keyfield = new String[] { Repository.FIELD_STEP_DATABASE_ID_DATABASE, };
				if (!database.checkIndexExists(schemaTable, keyfield))
				{
					sql = database.getCreateIndexStatement(schemaTable, indexname, keyfield, false, false, false, false);
		        	statements.add(sql);
		        	if (!dryrun) {
	                    if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
						database.execStatements(sql);
	                    if (log.isDetailed()) log.logDetailed(toString(), "Created lookup index " + indexname + " on " + schemaTable);
		        	}
				}
			}
			catch(KettleException kdbe)
			{
				// Ignore this one: index is not properly detected, it already exists...
			}
		}
		else
		{
            if (log.isDetailed()) log.logDetailed(toString(), "Table " + schemaTable + " is OK.");
		}
		if (monitor!=null) monitor.worked(1);

		//////////////////////////////////////////////////////////////////////////////////
		//
		// R_TRANS_NOTE
		//
		// Create table...
		table = new RowMeta();
		tablename = Repository.TABLE_R_TRANS_NOTE;
		schemaTable = databaseMeta.getQuotedSchemaTableCombination(null, tablename);
		if (monitor!=null) monitor.subTask("Checking table "+schemaTable);
		table.addValueMeta(new ValueMeta(Repository.FIELD_TRANS_NOTE_ID_TRANSFORMATION, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_TRANS_NOTE_ID_NOTE, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		sql = database.getDDL(schemaTable, table, null, false, null, false);

		if (!Const.isEmpty(sql)) // Doesn't exist: create the table...
		{
        	statements.add(sql);
        	if (!dryrun) {
	            if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
				database.execStatements(sql);
	            if (log.isDetailed()) log.logDetailed(toString(), "Created or altered table " + schemaTable);
        	}
		}
		else
		{
            if (log.isDetailed()) log.logDetailed(toString(), "Table " + schemaTable + " is OK.");
		}
		if (monitor!=null) monitor.worked(1);

		//////////////////////////////////////////////////////////////////////////////////
		//
		// R_LOGLEVEL
		//
		// Create table...
		boolean ok_loglevel = true;
		tablename = Repository.TABLE_R_LOGLEVEL;
		schemaTable = databaseMeta.getQuotedSchemaTableCombination(null, tablename);
		if (monitor!=null) monitor.subTask("Checking table "+schemaTable);
		table = new RowMeta();
		table.addValueMeta(new ValueMeta(Repository.FIELD_LOGLEVEL_ID_LOGLEVEL, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_LOGLEVEL_CODE, ValueMetaInterface.TYPE_STRING, Repository.REP_STRING_CODE_LENGTH, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_LOGLEVEL_DESCRIPTION, ValueMetaInterface.TYPE_STRING, Repository.REP_STRING_CODE_LENGTH, 0));
		sql = database.getDDL(schemaTable, table, null, false, Repository.FIELD_LOGLEVEL_ID_LOGLEVEL, false);

		create=false;
		if (!Const.isEmpty(sql)) // Doesn't exist: create the table...
		{
			create = sql.toUpperCase().indexOf("CREATE TABLE")>=0;
        	statements.add(sql);
        	if (!dryrun) {
	            if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
				database.execStatements(sql);
	            if (log.isDetailed()) log.logDetailed(toString(), "Created or altered table " + schemaTable);
        	}
		}
		else
		{
            if (log.isDetailed()) log.logDetailed(toString(), "Table " + schemaTable + " is OK.");
		}

		if (ok_loglevel)
		{
			//
			// Populate with data...
			//
			code = LogWriter.logLevelDescription;
			desc = LogWriter.log_level_desc_long;

			if (!dryrun) {
				database.prepareInsert(table, null, tablename);
			}

			for (int i = 1; i < code.length; i++)
			{
                RowMetaAndData lookup = null;
                if (upgrade) lookup = database.getOneRow("SELECT "+repository.quote(Repository.FIELD_LOGLEVEL_ID_LOGLEVEL)+" FROM " + schemaTable + " WHERE " 
                		+ database.getDatabaseMeta().quoteField("CODE") + " = '" + code[i] + "'");
				if (lookup == null)
				{
					long nextid = i;
					if (!create) nextid = repository.getNextLoglevelID();

					RowMetaAndData tableData = new RowMetaAndData();
                    tableData.addValue(new ValueMeta(Repository.FIELD_LOGLEVEL_ID_LOGLEVEL, ValueMetaInterface.TYPE_INTEGER), new Long(nextid));
                    tableData.addValue(new ValueMeta(Repository.FIELD_LOGLEVEL_CODE, ValueMetaInterface.TYPE_STRING), code[i]);
                    tableData.addValue(new ValueMeta(Repository.FIELD_LOGLEVEL_DESCRIPTION, ValueMetaInterface.TYPE_STRING), desc[i]);

                    if (dryrun) {
		            	sql = database.getSQLOutput(null, tablename, tableData.getRowMeta(), tableData.getData(), null);
		            	statements.add(sql);
					} else {
						database.setValuesInsert(tableData.getRowMeta(), tableData.getData());
						database.insertRow();
					}
				}
			}
            
            try
            {
                if (!dryrun) {
                	database.closeInsert();
                }
                if (log.isDetailed()) log.logDetailed(toString(), "Populated table " + schemaTable);
            }
            catch(KettleException dbe)
            {
                throw new KettleException("Unable to close insert after populating table " + schemaTable, dbe);
            }
		}
		if (monitor!=null) monitor.worked(1);

		//////////////////////////////////////////////////////////////////////////////////
		//
		// R_LOG
		//
		// Create table...
		table = new RowMeta();
		tablename = Repository.TABLE_R_LOG;
		schemaTable = databaseMeta.getQuotedSchemaTableCombination(null, tablename);
		if (monitor!=null) monitor.subTask("Checking table "+schemaTable);
		table.addValueMeta(new ValueMeta(Repository.FIELD_LOG_ID_LOG, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_LOG_NAME, ValueMetaInterface.TYPE_STRING, Repository.REP_STRING_CODE_LENGTH, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_LOG_ID_LOGLEVEL, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_LOG_LOGTYPE, ValueMetaInterface.TYPE_STRING, Repository.REP_STRING_CODE_LENGTH, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_LOG_FILENAME, ValueMetaInterface.TYPE_STRING, Repository.REP_STRING_CODE_LENGTH, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_LOG_FILEEXTENTION, ValueMetaInterface.TYPE_STRING, Repository.REP_STRING_CODE_LENGTH, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_LOG_ADD_DATE, ValueMetaInterface.TYPE_BOOLEAN, 1, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_LOG_ADD_TIME, ValueMetaInterface.TYPE_BOOLEAN, 1, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_LOG_ID_DATABASE_LOG, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_LOG_TABLE_NAME_LOG, ValueMetaInterface.TYPE_STRING, Repository.REP_STRING_CODE_LENGTH, 0));
		sql = database.getDDL(schemaTable, table, null, false, Repository.FIELD_LOG_ID_LOG, false);

		if (!Const.isEmpty(sql)) // Doesn't exist: create the table...
		{
        	statements.add(sql);
        	if (!dryrun) {
	            if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
				database.execStatements(sql);
	            if (log.isDetailed()) log.logDetailed(toString(), "Created or altered table " + schemaTable);
        	}
		}
		else
		{
            if (log.isDetailed()) log.logDetailed(toString(), "Table " + schemaTable + " is OK.");
		}
		if (monitor!=null) monitor.worked(1);

		//////////////////////////////////////////////////////////////////////////////////
		//
		// R_JOB
		//
		// Create table...
		table = new RowMeta();
		tablename = Repository.TABLE_R_JOB;
		schemaTable = databaseMeta.getQuotedSchemaTableCombination(null, tablename);
		if (monitor!=null) monitor.subTask("Checking table "+schemaTable);
		table.addValueMeta(new ValueMeta(Repository.FIELD_JOB_ID_JOB, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_JOB_ID_DIRECTORY, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_JOB_NAME, ValueMetaInterface.TYPE_STRING, Repository.REP_STRING_CODE_LENGTH, 0));
        table.addValueMeta(new ValueMeta(Repository.FIELD_JOB_DESCRIPTION, ValueMetaInterface.TYPE_STRING, Repository.REP_STRING_LENGTH, 0));
        table.addValueMeta(new ValueMeta(Repository.FIELD_JOB_EXTENDED_DESCRIPTION, ValueMetaInterface.TYPE_STRING, Repository.REP_STRING_LENGTH, 0));
        table.addValueMeta(new ValueMeta(Repository.FIELD_JOB_JOB_VERSION, ValueMetaInterface.TYPE_STRING, Repository.REP_STRING_CODE_LENGTH, 0));
        table.addValueMeta(new ValueMeta(Repository.FIELD_JOB_JOB_STATUS, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_JOB_ID_DATABASE_LOG, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_JOB_TABLE_NAME_LOG, ValueMetaInterface.TYPE_STRING, Repository.REP_STRING_CODE_LENGTH, 0));
        table.addValueMeta(new ValueMeta(Repository.FIELD_JOB_CREATED_USER, ValueMetaInterface.TYPE_STRING, Repository.REP_STRING_CODE_LENGTH, 0));
        table.addValueMeta(new ValueMeta(Repository.FIELD_JOB_CREATED_DATE, ValueMetaInterface.TYPE_DATE, 20, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_JOB_MODIFIED_USER, ValueMetaInterface.TYPE_STRING, Repository.REP_STRING_CODE_LENGTH, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_JOB_MODIFIED_DATE, ValueMetaInterface.TYPE_DATE, 20, 0));
        table.addValueMeta(new ValueMeta(Repository.FIELD_JOB_USE_BATCH_ID, ValueMetaInterface.TYPE_BOOLEAN, 0, 0));
        table.addValueMeta(new ValueMeta(Repository.FIELD_JOB_PASS_BATCH_ID, ValueMetaInterface.TYPE_BOOLEAN, 0, 0));
        table.addValueMeta(new ValueMeta(Repository.FIELD_JOB_USE_LOGFIELD, ValueMetaInterface.TYPE_BOOLEAN, 0, 0));
        table.addValueMeta(new ValueMeta(Repository.FIELD_JOB_SHARED_FILE, ValueMetaInterface.TYPE_STRING, Repository.REP_STRING_CODE_LENGTH, 0)); // 255 max length for now.

        sql = database.getDDL(schemaTable, table, null, false, Repository.FIELD_JOB_ID_JOB, false);
		if (!Const.isEmpty(sql)) // Doesn't exist: create the table...
		{
        	statements.add(sql);
        	if (!dryrun) {
	            if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
				database.execStatements(sql);
	            if (log.isDetailed()) log.logDetailed(toString(), "Created or altered table " + schemaTable);
        	}
		}
		else
		{
            if (log.isDetailed()) log.logDetailed(toString(), "Table " + schemaTable + " is OK.");
		}
		if (monitor!=null) monitor.worked(1);

		//////////////////////////////////////////////////////////////////////////////////
		//
		// R_JOBENTRY_TYPE
		//
		// Create table...
		boolean ok_jobentry_type = true;
		table = new RowMeta();
		tablename = Repository.TABLE_R_JOBENTRY_TYPE;
		schemaTable = databaseMeta.getQuotedSchemaTableCombination(null, tablename);
		if (monitor!=null) monitor.subTask("Checking table "+schemaTable);
		table.addValueMeta(new ValueMeta(Repository.FIELD_JOBENTRY_TYPE_ID_JOBENTRY_TYPE, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_JOBENTRY_TYPE_CODE, ValueMetaInterface.TYPE_STRING, Repository.REP_STRING_CODE_LENGTH, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_JOBENTRY_TYPE_DESCRIPTION, ValueMetaInterface.TYPE_STRING, Repository.REP_STRING_CODE_LENGTH, 0));
		sql = database.getDDL(schemaTable, table, null, false, Repository.FIELD_JOBENTRY_TYPE_ID_JOBENTRY_TYPE, false);

		create = false;
		if (!Const.isEmpty(sql)) // Doesn't exist: create the table...
		{
			create = sql.toUpperCase().indexOf("CREATE TABLE")>=0;
        	statements.add(sql);
        	if (!dryrun) {
	            if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
				database.execStatements(sql);
	            if (log.isDetailed()) log.logDetailed(toString(), "Created or altered table " + schemaTable);
        	}
		}
		else
		{
            if (log.isDetailed()) log.logDetailed(toString(), "Table " + schemaTable + " is OK.");
		}

		if (ok_jobentry_type)
		{
			//
			// Populate with data...
			//
			updateJobEntryTypes(statements, dryrun, create);
            if (log.isDetailed()) log.logDetailed(toString(), "Populated table " + schemaTable);
		}
		if (monitor!=null) monitor.worked(1);

		//////////////////////////////////////////////////////////////////////////////////
		//
		// R_JOBENTRY
		//
		// Create table...
		table = new RowMeta();
		tablename = Repository.TABLE_R_JOBENTRY;
		schemaTable = databaseMeta.getQuotedSchemaTableCombination(null, tablename);
		if (monitor!=null) monitor.subTask("Checking table "+schemaTable);
		table.addValueMeta(new ValueMeta(Repository.FIELD_JOBENTRY_ID_JOBENTRY, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_JOBENTRY_ID_JOB, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_JOBENTRY_ID_JOBENTRY_TYPE, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_JOBENTRY_NAME, ValueMetaInterface.TYPE_STRING, Repository.REP_STRING_CODE_LENGTH, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_JOBENTRY_DESCRIPTION, ValueMetaInterface.TYPE_STRING, Repository.REP_STRING_LENGTH, 0));
		sql = database.getDDL(schemaTable, table, null, false, Repository.FIELD_JOBENTRY_ID_JOBENTRY, false);

		if (!Const.isEmpty(sql)) // Doesn't exist: create the table...
		{
        	statements.add(sql);
        	if (!dryrun) {
	            if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
				database.execStatements(sql);
	            if (log.isDetailed()) log.logDetailed(toString(), "Created or altered table " + schemaTable);
        	}
		}
		else
		{
            if (log.isDetailed()) log.logDetailed(toString(), "Table " + schemaTable + " is OK.");
		}
		if (monitor!=null) monitor.worked(1);

		//////////////////////////////////////////////////////////////////////////////////
		//
		// R_JOBENTRY_COPY
		//
		// Create table...
		table = new RowMeta();
		tablename = Repository.TABLE_R_JOBENTRY_COPY;
		schemaTable = databaseMeta.getQuotedSchemaTableCombination(null, tablename);
		if (monitor!=null) monitor.subTask("Checking table "+schemaTable);
		table.addValueMeta(new ValueMeta(Repository.FIELD_JOBENTRY_COPY_ID_JOBENTRY_COPY, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_JOBENTRY_COPY_ID_JOBENTRY, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_JOBENTRY_COPY_ID_JOB, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_JOBENTRY_COPY_ID_JOBENTRY_TYPE, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_JOBENTRY_COPY_NR, ValueMetaInterface.TYPE_INTEGER, 4, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_JOBENTRY_COPY_GUI_LOCATION_X, ValueMetaInterface.TYPE_INTEGER, 6, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_JOBENTRY_COPY_GUI_LOCATION_Y, ValueMetaInterface.TYPE_INTEGER, 6, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_JOBENTRY_COPY_GUI_DRAW, ValueMetaInterface.TYPE_BOOLEAN, 1, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_JOBENTRY_COPY_PARALLEL, ValueMetaInterface.TYPE_BOOLEAN, 1, 0));
		sql = database.getDDL(schemaTable, table, null, false, Repository.FIELD_JOBENTRY_COPY_ID_JOBENTRY_COPY, false);

		if (!Const.isEmpty(sql)) // Doesn't exist: create the table...
		{
        	statements.add(sql);
        	if (!dryrun) {
	            if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
				database.execStatements(sql);
	            if (log.isDetailed()) log.logDetailed(toString(), "Created or altered table " + schemaTable);
        	}	            
		}
		else
		{
            if (log.isDetailed()) log.logDetailed(toString(), "Table " + schemaTable + " is OK.");
		}
		if (monitor!=null) monitor.worked(1);

		//////////////////////////////////////////////////////////////////////////////////
		//
		// R_JOBENTRY_ATTRIBUTE
		//
		// Create table...
		table = new RowMeta();
		tablename = Repository.TABLE_R_JOBENTRY_ATTRIBUTE;
		schemaTable = databaseMeta.getQuotedSchemaTableCombination(null, tablename);
		if (monitor!=null) monitor.subTask("Checking table "+schemaTable);
		table.addValueMeta(new ValueMeta(Repository.FIELD_JOBENTRY_ATTRIBUTE_ID_JOBENTRY_ATTRIBUTE, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_JOBENTRY_ATTRIBUTE_ID_JOB, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_JOBENTRY_ATTRIBUTE_ID_JOBENTRY, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_JOBENTRY_ATTRIBUTE_NR, ValueMetaInterface.TYPE_INTEGER, 6, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_JOBENTRY_ATTRIBUTE_CODE, ValueMetaInterface.TYPE_STRING, Repository.REP_STRING_CODE_LENGTH, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_JOBENTRY_ATTRIBUTE_VALUE_NUM, ValueMetaInterface.TYPE_NUMBER, 13, 2));
		table.addValueMeta(new ValueMeta(Repository.FIELD_JOBENTRY_ATTRIBUTE_VALUE_STR, ValueMetaInterface.TYPE_STRING, Repository.REP_STRING_LENGTH, 0));
		sql = database.getDDL(schemaTable, table, null, false, Repository.FIELD_JOBENTRY_ATTRIBUTE_ID_JOBENTRY_ATTRIBUTE, false);

		if (!Const.isEmpty(sql)) // Doesn't exist: create the table...
		{
        	statements.add(sql);
        	if (!dryrun) {
	            if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
				database.execStatements(sql);
	            if (log.isDetailed()) log.logDetailed(toString(), "Created or altered table " + schemaTable);
        	}

			try
			{
				indexname = "IDX_" + schemaTable.substring(2) + "_LOOKUP";
				keyfield = new String[] { Repository.FIELD_JOBENTRY_ATTRIBUTE_ID_JOBENTRY_ATTRIBUTE, Repository.FIELD_JOBENTRY_ATTRIBUTE_CODE, Repository.FIELD_JOBENTRY_ATTRIBUTE_NR, };
	
				if (!database.checkIndexExists(schemaTable, keyfield))
				{
					sql = database.getCreateIndexStatement(schemaTable, indexname, keyfield, false, true, false, false);
		        	statements.add(sql);
		        	if (!dryrun) {
	                    if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
						database.execStatements(sql);
	                    if (log.isDetailed()) log.logDetailed(toString(), "Created lookup index " + indexname + " on " + schemaTable);
		        	}
				}
			}
			catch(KettleException kdbe)
			{
				// Ignore this one: index is not properly detected, it already exists...
			}
		}
		else
		{
            if (log.isDetailed()) log.logDetailed(toString(), "Table " + schemaTable + " is OK.");
		}
		if (monitor!=null) monitor.worked(1);

		//////////////////////////////////////////////////////////////////////////////////
		//
		// R_JOB_HOP
		//
		// Create table...
		table = new RowMeta();
		tablename = Repository.TABLE_R_JOB_HOP;
		schemaTable = databaseMeta.getQuotedSchemaTableCombination(null, tablename);
		if (monitor!=null) monitor.subTask("Checking table "+schemaTable);
		table.addValueMeta(new ValueMeta(Repository.FIELD_JOB_HOP_ID_JOB_HOP, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_JOB_HOP_ID_JOB, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_JOB_HOP_ID_JOBENTRY_COPY_FROM, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_JOB_HOP_ID_JOBENTRY_COPY_TO, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_JOB_HOP_ENABLED, ValueMetaInterface.TYPE_BOOLEAN, 1, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_JOB_HOP_EVALUATION, ValueMetaInterface.TYPE_BOOLEAN, 1, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_JOB_HOP_UNCONDITIONAL, ValueMetaInterface.TYPE_BOOLEAN, 1, 0));
		sql = database.getDDL(schemaTable, table, null, false, Repository.FIELD_JOB_HOP_ID_JOB_HOP, false);

		if (!Const.isEmpty(sql)) // Doesn't exist: create the table...
		{
        	statements.add(sql);
        	if (!dryrun) {
	            if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
				database.execStatements(sql);
	            if (log.isDetailed()) log.logDetailed(toString(), "Created or altered table " + schemaTable);
        	}
		}
		else
		{
            if (log.isDetailed()) log.logDetailed(toString(), "Table " + schemaTable + " is OK.");
		}
		if (monitor!=null) monitor.worked(1);

		//////////////////////////////////////////////////////////////////////////////////
		//
		// R_JOB_NOTE
		//
		// Create table...
		table = new RowMeta();
		tablename = Repository.TABLE_R_JOB_NOTE;
		schemaTable = databaseMeta.getQuotedSchemaTableCombination(null, tablename);
		if (monitor!=null) monitor.subTask("Checking table "+schemaTable);
		table.addValueMeta(new ValueMeta(Repository.FIELD_JOB_NOTE_ID_JOB, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_JOB_NOTE_ID_NOTE, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		sql = database.getDDL(schemaTable, table, null, false, null, false);

		if (!Const.isEmpty(sql)) // Doesn't exist: create the table...
		{
        	statements.add(sql);
        	if (!dryrun) {
	            if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
				database.execStatements(sql);
	            if (log.isDetailed()) log.logDetailed(toString(), "Created or altered table " + schemaTable);
        	}
		}
		else
		{
            if (log.isDetailed()) log.logDetailed(toString(), "Table " + schemaTable + " is OK.");
		}
		if (monitor!=null) monitor.worked(1);

		///////////////////////////////////////////////////////////////////////////////////
		//
		//  User tables...
		//
		///////////////////////////////////////////////////////////////////////////////////

		//////////////////////////////////////////////////////////////////////////////////
		//
		// R_PROFILE
		//
		// Create table...
        Map<String, Long> profiles = new Hashtable<String, Long>();
        
		boolean ok_profile = true;
		tablename = Repository.TABLE_R_PROFILE;
		schemaTable = databaseMeta.getQuotedSchemaTableCombination(null, tablename);
		if (monitor!=null) monitor.subTask("Checking table "+schemaTable);
		table = new RowMeta();
		table.addValueMeta(new ValueMeta(Repository.FIELD_PROFILE_ID_PROFILE, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_PROFILE_NAME, ValueMetaInterface.TYPE_STRING, Repository.REP_STRING_CODE_LENGTH, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_PROFILE_DESCRIPTION, ValueMetaInterface.TYPE_STRING, Repository.REP_STRING_CODE_LENGTH, 0));
		sql = database.getDDL(schemaTable, table, null, false, Repository.FIELD_PROFILE_ID_PROFILE, false);

		create = false;
		if (!Const.isEmpty(sql)) // Doesn't exist: create the table...
		{
			create = sql.toUpperCase().indexOf("CREATE TABLE")>=0;
        	statements.add(sql);
        	if (!dryrun) {
	            if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
				database.execStatements(sql);
	            if (log.isDetailed()) log.logDetailed(toString(), "Created or altered table " + schemaTable);
        	}
		}
		else
		{
            if (log.isDetailed()) log.logDetailed(toString(), "Table " + schemaTable + " is OK.");
		}

		if (ok_profile)
		{
			//
			// Populate with data...
			//
			code = new String[] { "Administrator", "User", "Read-only" };
			desc = new String[] { "Administrator profile, manage users", "Normal user, all tools", "Read-only users" };

			if (!dryrun) {
				database.prepareInsert(table, null, tablename);
			}

			for (int i = 0; i < code.length; i++)
			{
                RowMetaAndData lookup = null;
                if (upgrade) lookup = database.getOneRow("SELECT "+repository.quote(Repository.FIELD_PROFILE_ID_PROFILE)+" FROM " + schemaTable + " WHERE "
                		+ repository.quote(Repository.FIELD_PROFILE_NAME) + " = '" + code[i] + "'");
				if (lookup == null)
				{
					long nextid = i+1;
					if (!create) nextid = repository.getNextProfileID();

					RowMetaAndData tableData = new RowMetaAndData();
                    tableData.addValue(new ValueMeta(Repository.FIELD_PROFILE_ID_PROFILE, ValueMetaInterface.TYPE_INTEGER), new Long(nextid));
                    tableData.addValue(new ValueMeta(Repository.FIELD_PROFILE_NAME, ValueMetaInterface.TYPE_STRING), code[i]);
                    tableData.addValue(new ValueMeta(Repository.FIELD_PROFILE_DESCRIPTION, ValueMetaInterface.TYPE_STRING), desc[i]);

                    if (dryrun) {
		            	sql = database.getSQLOutput(null, tablename, tableData.getRowMeta(), tableData.getData(), null);
		            	statements.add(sql);	
                    } else {
						database.setValuesInsert(tableData);
						database.insertRow();
	                    if (log.isDetailed()) log.logDetailed(toString(), "Inserted new row into table "+schemaTable+" : "+table);
                    }
                    profiles.put(code[i], new Long(nextid));
				}
			}

            try
            {
                if (!dryrun) {
                	database.closeInsert();
                }
                if (log.isDetailed()) log.logDetailed(toString(), "Populated table " + schemaTable);
            }
            catch(KettleException dbe)
            {
                throw new KettleException("Unable to close insert after populating table " + schemaTable, dbe);
            }
		}
		if (monitor!=null) monitor.worked(1);

		//////////////////////////////////////////////////////////////////////////////////
		//
		// R_USER
		//
		// Create table...
        Map<String, Long> users = new Hashtable<String, Long>();
		boolean ok_user = true;
		table = new RowMeta();
		tablename = Repository.TABLE_R_USER;
		schemaTable = databaseMeta.getQuotedSchemaTableCombination(null, tablename);
		if (monitor!=null) monitor.subTask("Checking table "+schemaTable);
		table.addValueMeta(new ValueMeta(Repository.FIELD_USER_ID_USER, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_USER_ID_PROFILE, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_USER_LOGIN, ValueMetaInterface.TYPE_STRING, Repository.REP_STRING_CODE_LENGTH, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_USER_PASSWORD, ValueMetaInterface.TYPE_STRING, Repository.REP_STRING_CODE_LENGTH, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_USER_NAME, ValueMetaInterface.TYPE_STRING, Repository.REP_STRING_CODE_LENGTH, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_USER_DESCRIPTION, ValueMetaInterface.TYPE_STRING, Repository.REP_STRING_CODE_LENGTH, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_USER_ENABLED, ValueMetaInterface.TYPE_BOOLEAN, 1, 0));
		sql = database.getDDL(schemaTable, table, null, false, Repository.FIELD_USER_ID_USER, false);

		create = false;
		if (!Const.isEmpty(sql)) // Doesn't exist: create the table...
		{
			create = sql.toUpperCase().indexOf("CREATE TABLE")>=0;
        	statements.add(sql);
        	if (!dryrun) {
	            if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
				database.execStatements(sql);
	            if (log.isDetailed()) log.logDetailed(toString(), "Created or altered table " + schemaTable);
        	}
		}
		else
		{
            if (log.isDetailed()) log.logDetailed(toString(), "Table " + schemaTable + " is OK.");
		}

		if (ok_user)
		{
			//
			// Populate with data...
			//
			user = new String[] { "admin", "guest" };
			pass = new String[] { "admin", "guest" };
			code = new String[] { "Administrator", "Guest account" };
			desc = new String[] { "User manager", "Read-only guest account" };
			prof = new String[] { "Administrator", "Read-only" };

			if (!dryrun) {
				database.prepareInsert(table, null, tablename);
			}

			for (int i = 0; i < user.length; i++)
			{
                RowMetaAndData lookup = null;
                if (upgrade) lookup = database.getOneRow("SELECT "+repository.quote(Repository.FIELD_USER_ID_USER)+" FROM " + schemaTable + " WHERE "
                		+ repository.quote(Repository.FIELD_USER_LOGIN) + " = '" + user[i] + "'");
				if (lookup == null)
				{
					long nextid = i+1;
					if (!create) nextid = repository.getNextUserID();
					String password = Encr.encryptPassword(pass[i]);
                    
                    Long profileID = (Long)profiles.get( prof[i] );
                    long id_profile = -1L;
                    if (profileID!=null) id_profile = profileID.longValue();
                    
					RowMetaAndData tableData = new RowMetaAndData();
                    tableData.addValue(new ValueMeta(Repository.FIELD_USER_ID_USER, ValueMetaInterface.TYPE_INTEGER), Long.valueOf(nextid));
                    tableData.addValue(new ValueMeta(Repository.FIELD_USER_ID_PROFILE, ValueMetaInterface.TYPE_INTEGER), Long.valueOf(id_profile));
                    tableData.addValue(new ValueMeta(Repository.FIELD_USER_LOGIN, ValueMetaInterface.TYPE_STRING), user[i]);
                    tableData.addValue(new ValueMeta(Repository.FIELD_USER_PASSWORD, ValueMetaInterface.TYPE_STRING), password);
                    tableData.addValue(new ValueMeta(Repository.FIELD_USER_NAME, ValueMetaInterface.TYPE_STRING), code[i]);
                    tableData.addValue(new ValueMeta(Repository.FIELD_USER_DESCRIPTION, ValueMetaInterface.TYPE_STRING), desc[i]);
                    tableData.addValue(new ValueMeta(Repository.FIELD_USER_ENABLED, ValueMetaInterface.TYPE_BOOLEAN), Boolean.TRUE);

                    if (dryrun) {
		            	sql = database.getSQLOutput(null, tablename, tableData.getRowMeta(), tableData.getData(), null);
		            	statements.add(sql);	
                    } else {
						database.setValuesInsert(tableData);
						database.insertRow();
                    }
                    users.put(user[i], new Long(nextid));
				}
			}
            
            try
            {
                if (!dryrun) {
                	database.closeInsert();
                }
                if (log.isDetailed()) log.logDetailed(toString(), "Populated table " + schemaTable);
            }
            catch(KettleException dbe)
            {
                throw new KettleException("Unable to close insert after populating table " + schemaTable, dbe);
            }
		}
		if (monitor!=null) monitor.worked(1);

		//////////////////////////////////////////////////////////////////////////////////
		//
		// R_PERMISSION
		//
		// Create table...
        Map<String, Long> permissions = new Hashtable<String, Long>();
		boolean ok_permission = true;
		table = new RowMeta();
		tablename = Repository.TABLE_R_PERMISSION;
		schemaTable = databaseMeta.getQuotedSchemaTableCombination(null, tablename);
		if (monitor!=null) monitor.subTask("Checking table "+schemaTable);
		table.addValueMeta(new ValueMeta(Repository.FIELD_PERMISSION_ID_PERMISSION, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_PERMISSION_CODE, ValueMetaInterface.TYPE_STRING, Repository.REP_STRING_CODE_LENGTH, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_PERMISSION_DESCRIPTION, ValueMetaInterface.TYPE_STRING, Repository.REP_STRING_CODE_LENGTH, 0));
		sql = database.getDDL(schemaTable, table, null, false, Repository.FIELD_PERMISSION_ID_PERMISSION, false);

		create = false;
		if (!Const.isEmpty(sql)) // Doesn't exist: create the table...
		{
			create = sql.toUpperCase().indexOf("CREATE TABLE")>=0;
        	statements.add(sql);
        	if (!dryrun) {
	            if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
				database.execStatements(sql);
	            if (log.isDetailed()) log.logDetailed(toString(), "Created or altered table " + schemaTable);
        	}
		}
		else
		{
            if (log.isDetailed()) log.logDetailed(toString(), "Table " + schemaTable + " is OK.");
		}

		if (ok_permission)
		{
			//
			// Populate with data...
			//
			code = PermissionMeta.permissionTypeCode;
			desc = PermissionMeta.permissionTypeDesc;

			if (!dryrun) {
				database.prepareInsert(table, null, tablename);
			}

			for (int i = 1; i < code.length; i++)
			{
                RowMetaAndData lookup = null;
                if (upgrade) lookup = database.getOneRow("SELECT "+repository.quote(Repository.FIELD_PERMISSION_ID_PERMISSION)+" FROM " + schemaTable + " WHERE " 
                		+ repository.quote(Repository.FIELD_PERMISSION_CODE) + " = '" + code[i] + "'");
				if (lookup == null)
				{
					long nextid = i;
					if (!create) nextid = repository.getNextPermissionID();

                    RowMetaAndData tableData = new RowMetaAndData();
                    tableData.addValue(new ValueMeta(Repository.FIELD_PERMISSION_ID_PERMISSION, ValueMetaInterface.TYPE_INTEGER), new Long(nextid));
                    tableData.addValue(new ValueMeta(Repository.FIELD_PERMISSION_CODE, ValueMetaInterface.TYPE_STRING), code[i]);
                    tableData.addValue(new ValueMeta(Repository.FIELD_PERMISSION_DESCRIPTION, ValueMetaInterface.TYPE_STRING), desc[i]);

                    if (dryrun) {
		            	sql = database.getSQLOutput(null, tablename, tableData.getRowMeta(), tableData.getData(), null);
		            	statements.add(sql);	
                    } else {
						database.setValuesInsert(tableData);
						database.insertRow();
	                    if (log.isDetailed()) log.logDetailed(toString(), "Inserted new row into table "+schemaTable+" : "+table);
                    }
                    permissions.put(code[i], new Long(nextid));
				}
			}

            try
            {
                if (!dryrun) {
                	database.closeInsert();
                }
                if (log.isDetailed()) log.logDetailed(toString(), "Populated table " + schemaTable);
            }
            catch(KettleException dbe)
            {
                throw new KettleException("Unable to close insert after populating table " + schemaTable, dbe);
            }
		}
		if (monitor!=null) monitor.worked(1);

		//////////////////////////////////////////////////////////////////////////////////
		//
		// R_PROFILE_PERMISSION
		//
		// Create table...
		boolean ok_profile_permission = true;
		table = new RowMeta();
		tablename = Repository.TABLE_R_PROFILE_PERMISSION;
		schemaTable = databaseMeta.getQuotedSchemaTableCombination(null, tablename);
		if (monitor!=null) monitor.subTask("Checking table "+schemaTable);
		table.addValueMeta(new ValueMeta(Repository.FIELD_PROFILE_PERMISSION_ID_PROFILE, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		table.addValueMeta(new ValueMeta(Repository.FIELD_PROFILE_PERMISSION_ID_PERMISSION, ValueMetaInterface.TYPE_INTEGER, KEY, 0));
		sql = database.getDDL(schemaTable, table, null, false, null, false);

		create = false;
		if (!Const.isEmpty(sql)) // Doesn't exist: create the table...
		{
			create = sql.toUpperCase().indexOf("CREATE TABLE")>=0;
        	statements.add(sql);
        	if (!dryrun) {
	            if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
				database.execStatements(sql);
	            if (log.isDetailed()) log.logDetailed(toString(), "Created or altered table " + schemaTable);
        	}
			try
			{
				indexname = "IDX_" + schemaTable.substring(2) + "_PK";
				keyfield = new String[] { Repository.FIELD_PROFILE_PERMISSION_ID_PROFILE, Repository.FIELD_PROFILE_PERMISSION_ID_PERMISSION, };
				if (!database.checkIndexExists(schemaTable, keyfield))
				{
					sql = database.getCreateIndexStatement(schemaTable, indexname, keyfield, false, true, false, false);
		        	statements.add(sql);
		        	if (!dryrun) {	
	                    if (log.isDetailed()) log.logDetailed(toString(), "executing SQL statements: "+Const.CR+sql);
						database.execStatements(sql);
	                    if (log.isDetailed()) log.logDetailed(toString(), "Created lookup index " + indexname + " on " + schemaTable);
		        	}
				}
			}
			catch(KettleException kdbe)
			{
				// Ignore this one: index is not properly detected, it already exists...
			}
		}
		else
		{
            if (log.isDetailed()) log.logDetailed(toString(), "Table " + schemaTable + " is OK.");
		}

		if (ok_profile_permission)
		{
			if (!dryrun) {
				database.prepareInsert(table, null, tablename);
			}

			// Administrator default:
            Long profileID = (Long)profiles.get( "Administrator");
            long id_profile = -1L;
            if (profileID!=null) id_profile = profileID.longValue();
			
            if (log.isDetailed()) log.logDetailed(toString(), "Administrator profile id = "+id_profile);
            String perms[] = new String[]
				{ 
                    PermissionMeta.permissionTypeCode[PermissionMeta.TYPE_PERMISSION_ADMIN],
                    PermissionMeta.permissionTypeCode[PermissionMeta.TYPE_PERMISSION_TRANSFORMATION],
                    PermissionMeta.permissionTypeCode[PermissionMeta.TYPE_PERMISSION_JOB],
                    PermissionMeta.permissionTypeCode[PermissionMeta.TYPE_PERMISSION_SCHEMA] 
				};
			
			for (int i=0;i < perms.length ; i++)
			{
                Long permissionID = (Long) permissions.get(perms[i]);
                long id_permission = -1L;
                if (permissionID!=null) id_permission = permissionID.longValue();
                
                if (log.isDetailed()) log.logDetailed(toString(), "Permission id for '"+perms[i]+"' = "+id_permission);

				RowMetaAndData lookup = null;
                if (upgrade) 
                {
                    String lookupSQL = "SELECT "+repository.quote(Repository.FIELD_PROFILE_PERMISSION_ID_PROFILE)+
                                   " FROM " + schemaTable + 
                                   " WHERE "+repository.quote(Repository.FIELD_PROFILE_PERMISSION_ID_PROFILE)+"=" + id_profile + " AND +"+repository.quote(Repository.FIELD_PROFILE_PERMISSION_ID_PERMISSION)+"=" + id_permission;
                    if (log.isDetailed()) log.logDetailed(toString(), "Executing SQL: "+lookupSQL);
                    lookup = database.getOneRow(lookupSQL);
                }
				if (lookup == null) // if the combination is not yet there, insert...
				{
                    String insertSQL="INSERT INTO "+schemaTable+"("+repository.quote(Repository.FIELD_PROFILE_PERMISSION_ID_PROFILE)+", "+repository.quote(Repository.FIELD_PROFILE_PERMISSION_ID_PERMISSION)+")"
                    			+" VALUES("+id_profile+","+id_permission+")";
                    statements.add(insertSQL);
                    if (!dryrun) {
                    	database.execStatement(insertSQL);
                    }
                    if (log.isDetailed()) log.logDetailed(toString(), "insertSQL = ["+insertSQL+"]");
				}
				else
				{
                    if (log.isDetailed()) log.logDetailed(toString(), "Found id_profile="+id_profile+", id_permission="+id_permission);
				}
			}

			// User profile
            profileID = (Long)profiles.get( "User" );
            id_profile = -1L;
            if (profileID!=null) id_profile = profileID.longValue();
            
            if (log.isDetailed()) log.logDetailed(toString(), "User profile id = "+id_profile);
            perms = new String[]
                { 
                      PermissionMeta.permissionTypeCode[PermissionMeta.TYPE_PERMISSION_TRANSFORMATION],
                      PermissionMeta.permissionTypeCode[PermissionMeta.TYPE_PERMISSION_JOB],
                      PermissionMeta.permissionTypeCode[PermissionMeta.TYPE_PERMISSION_SCHEMA] 
                };

            for (int i = 0; i < perms.length; i++)
			{
                Long permissionID = (Long) permissions.get(perms[i]);
                long id_permission = -1L;
                if (permissionID!=null) id_permission = permissionID.longValue();

                RowMetaAndData lookup = null;
                if (upgrade) lookup = database.getOneRow("SELECT "+repository.quote(Repository.FIELD_PROFILE_PERMISSION_ID_PROFILE)+
                			" FROM " + schemaTable + 
                			" WHERE "+repository.quote(Repository.FIELD_PROFILE_PERMISSION_ID_PROFILE)+"=" + id_profile + " AND "+repository.quote(Repository.FIELD_PROFILE_PERMISSION_ID_PERMISSION)+"=" + id_permission);
				if (lookup == null) // if the combination is not yet there, insert...
				{
					RowMetaAndData tableData = new RowMetaAndData();
                    tableData.addValue(new ValueMeta(Repository.FIELD_PROFILE_PERMISSION_ID_PROFILE, ValueMetaInterface.TYPE_INTEGER), new Long(id_profile));
                    tableData.addValue(new ValueMeta(Repository.FIELD_PROFILE_PERMISSION_ID_PERMISSION, ValueMetaInterface.TYPE_INTEGER), new Long(id_permission));

                    if (dryrun) {
		            	sql = database.getSQLOutput(null, tablename, tableData.getRowMeta(), tableData.getData(), null);
		            	statements.add(sql);	
                    } else {
						database.setValuesInsert(tableData);
						database.insertRow();
                    }
				}
			}

            try
            {
                if (!dryrun) {
                	database.closeInsert();
                }
                if (log.isDetailed()) log.logDetailed(toString(), "Populated table " + schemaTable);
            }
            catch(KettleException dbe)
            {
                throw new KettleException("Unable to close insert after populating table " + schemaTable, dbe);
            }
		}
        
		if (monitor!=null) monitor.worked(1);
		if (monitor!=null) monitor.done();
        
        log.logBasic(toString(), (upgrade?"Upgraded":"Created")+ " "+Repository.repositoryTableNames.length+" repository tables.");

	}

	/**
	 * Update the list in R_STEP_TYPE using the StepLoader StepPlugin entries
	 * 
	 * @throws KettleException if the update didn't go as planned.
	 */
	public List<String> updateStepTypes(List<String> statements, boolean dryrun, boolean create) throws KettleException
	{
		synchronized (repository) {
			
			// We should only do an update if something has changed...
			for (int i = 0; i < stepLoader.nrStepsWithType(StepPlugin.TYPE_ALL); i++)
			{
				StepPlugin sp = stepLoader.getStepWithType(StepPlugin.TYPE_ALL, i);
				long id = -1;
				if (!create) id = repository.getStepTypeID(sp.getID()[0]);
				if (id < 0) // Not found, we need to add this one...
				{
					// We need to add this one ...
					id = i+1;
					if (!create) id = repository.getNextStepTypeID();
	
					RowMetaAndData table = new RowMetaAndData();
					table.addValue(new ValueMeta(Repository.FIELD_STEP_TYPE_ID_STEP_TYPE, ValueMetaInterface.TYPE_INTEGER), new Long(id));
					table.addValue(new ValueMeta(Repository.FIELD_STEP_TYPE_CODE, ValueMetaInterface.TYPE_STRING), sp.getID()[0]);
					table.addValue(new ValueMeta(Repository.FIELD_STEP_TYPE_DESCRIPTION, ValueMetaInterface.TYPE_STRING), sp.getDescription());
					table.addValue(new ValueMeta(Repository.FIELD_STEP_TYPE_HELPTEXT, ValueMetaInterface.TYPE_STRING), sp.getTooltip());
	
					if (dryrun) {
		            	String sql = database.getSQLOutput(null, Repository.TABLE_R_STEP_TYPE, table.getRowMeta(), table.getData(), null);
		            	statements.add(sql);
					} else {
						database.prepareInsert(table.getRowMeta(), null, Repository.TABLE_R_STEP_TYPE);
						database.setValuesInsert(table);
						database.insertRow();
						database.closeInsert();
					}
				}
			}
		}
		return statements;
	}
	
	/**
	 * Update the list in R_JOBENTRY_TYPE 
	 * @param create 
	 * 
	 * @exception KettleException if something went wrong during the update.
	 */
	public void updateJobEntryTypes(List<String> statements, boolean dryrun, boolean create) throws KettleException
	{
		synchronized (repository) {
				
	        // We should only do an update if something has changed...
	        JobEntryLoader jobEntryLoader = JobEntryLoader.getInstance();
	        JobPlugin[] jobPlugins = jobEntryLoader.getJobEntriesWithType(JobPlugin.TYPE_ALL);
	        
	        for (int i = 0; i < jobPlugins.length; i++)
	        {
	            String type_desc = jobPlugins[i].getID();
	            String type_desc_long = jobPlugins[i].getDescription();
	            long id = -1;
	            if (!create) id = repository.getJobEntryTypeID(type_desc);
	            if (id < 0) // Not found, we need to add this one...
	            {
	                // We need to add this one ...
	                id = i+1;
	                if (!create) id = repository.getNextJobEntryTypeID();
	
	                RowMetaAndData table = new RowMetaAndData();
	                table.addValue(new ValueMeta(Repository.FIELD_JOBENTRY_TYPE_ID_JOBENTRY_TYPE, ValueMetaInterface.TYPE_INTEGER), new Long(id));
	                table.addValue(new ValueMeta(Repository.FIELD_JOBENTRY_TYPE_CODE, ValueMetaInterface.TYPE_STRING), type_desc);
	                table.addValue(new ValueMeta(Repository.FIELD_JOBENTRY_TYPE_DESCRIPTION, ValueMetaInterface.TYPE_STRING), type_desc_long);
	
					if (dryrun) {
		            	String sql = database.getSQLOutput(null, Repository.TABLE_R_JOBENTRY_TYPE, table.getRowMeta(), table.getData(), null);
		            	statements.add(sql);
					} else {
		                database.prepareInsert(table.getRowMeta(), null, Repository.TABLE_R_JOBENTRY_TYPE);
		                database.setValuesInsert(table);
		                database.insertRow();
		                database.closeInsert();
					}
	            }
	        }
		}
	}

}
