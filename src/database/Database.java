package database;

import ddl.DDLParser;
import ddl.DDLParserException;
import ddl.IDDLParser;
import ddl.catalog.Catalog;
import storagemanager.AStorageManager;
import storagemanager.StorageManager;
import storagemanager.StorageManagerException;

/**
 * Class to create and access a database.
 */

public class Database implements IDatabase{
    public static Database database = null;
    public static AStorageManager storageManager;
    public static Catalog catalog;
    public static IDDLParser parser;

    private Database(String dbLoc, int pageBufferSize, int pageSize) {
        try {
            storageManager = new StorageManager(dbLoc, pageBufferSize, pageSize, false);
        } catch (StorageManagerException e) {
            System.err.println(e.getLocalizedMessage());
        }
        catalog = Catalog.createOrLoadCatalog();
        parser = DDLParser.createParser();
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
        try {
            parser.parseDDLstatement(statement);
        } catch (DDLParserException e) {
            System.err.println(e.getLocalizedMessage());
        }
    }

    @Override
    public Object[][] executeQuery(String query) {
        return new Object[0][];
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
