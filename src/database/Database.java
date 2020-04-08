package database;

import ddl.DDLParser;
import ddl.DDLParserException;
import ddl.IDDLParser;
import ddl.catalog.Catalog;
import dml.DMLParser;
import dml.DMLParserException;
import dml.IDMLParser;
import storagemanager.AStorageManager;
import storagemanager.StorageManager;
import storagemanager.StorageManagerException;
import storagemanager.util.StringParser;

import java.util.HashSet;
import java.util.Set;

/**
 * Class to create and access a database.
 */

public class Database implements IDatabase{
    public static Database database = null;
    public static AStorageManager storageManager;
    public static Catalog catalog;
    public static IDDLParser ddlParser;
    public static IDMLParser dmlParser;

    private final Set<String> ddlCommands = new HashSet<>() {{
        add("create");
        add("drop");
        add("alter");
    }};

    private final Set<String> dmlCommands = new HashSet<>() {{
        add("insert");
        add("update");
        add("delete");
    }};

    private Database(String dbLoc, int pageBufferSize, int pageSize) {
        try {
            storageManager = new StorageManager(dbLoc, pageBufferSize, pageSize, false);
        } catch (StorageManagerException e) {
            System.err.println(e.getLocalizedMessage());
        }
        catalog = Catalog.createOrLoadCatalog();
        ddlParser = DDLParser.createParser();
        dmlParser = DMLParser.createParser();
    }

    /**
     * Static function that will create/restart and return a database
     * @param dbLoc the location to start/restart the database in
     * @param pageBufferSize the size of the page buffer; max number of pages allowed in the buffer at any given time
     * @param pageSize the size of a page in bytes
     * @return an instance of an database.IDatabase.
     */
    public static IDatabase getConnection(String dbLoc, int pageBufferSize, int pageSize ){
        if (database != null) return database;
        database = new Database(dbLoc, pageBufferSize, pageSize);
        return database;
    }

    @Override
    public void executeNonQuery(String statement) {
        statement = StringParser.toLowerCaseNonString(statement.trim());
        String[] statements = statement.split(";");
        for (String s : statements) {
            if (s.trim().length() == 0) continue;
            if (ddlCommands.contains(s.substring(0, s.indexOf(' '))))
                executeNonQueryDDL(s);
            else if (dmlCommands.contains(s.substring(0, s.indexOf(' '))))
                executeNonQueryDML(s);
            else
                System.err.println("Statement not a command: " + s);
        }
    }



    private void executeNonQueryDDL(String statement) {
        try {
            ddlParser.parseDDLstatement(statement);
        } catch (DDLParserException e) {
            System.err.println(e.getLocalizedMessage());
        }
    }

    private void executeNonQueryDML(String statement) {
        try {
            dmlParser.parseDMLStatement(statement);
        } catch (DMLParserException e) {
            System.err.println(e.getLocalizedMessage());
        }
    }

    @Override
    public Object[][] executeQuery(String query) {
        String qString = StringParser.toLowerCaseNonString(query.split(";")[0]).replaceAll("\n", " ").trim();
        try {
            return dmlParser.parseDMLQuery(qString);
        } catch (DMLParserException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void terminateDatabase() {
        try {
            catalog.saveCatalog();
        } catch (DDLParserException e) {
            e.printStackTrace();
        }
        try {
            storageManager.terminateDatabase();
        } catch (StorageManagerException e) {
            e.printStackTrace();
        }
    }
}
