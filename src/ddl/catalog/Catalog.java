package ddl.catalog;

import ddl.DDLParser;
import ddl.DDLParserException;
import storagemanager.buffermanager.diskUtils.DataManager;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Catalog implements Serializable {
    private static Catalog catalog;

    private final Map<String, Table> tables;

    private Catalog() {
        tables = new HashMap<>();
    }

    public static Catalog newCatalog() {
        if (catalog != null) return catalog;
        catalog = new Catalog();
        return catalog;
    }

    /**
     * Load a catalog from disk. The storage manager should already be initialized.
     * @pre a database has previously been created. The storagemanager has already been loaded
     * @throws DDLParserException a catalog has never been created for this database
     */
    public static Catalog loadCatalog() throws DDLParserException {
        if (catalog != null) return catalog;
        try {
            catalog = DataManager.getCatalog();
        } catch (IOException e) {
            throw new DDLParserException(DDLParser.CANNOT_LOAD_CATALOG);
        }
        return catalog;
    }

    /**
     * Save the catalog to disk
     * @throws DDLParserException the catalog could not be saved
     */
    public void saveCatalog() throws DDLParserException {
        try {
            DataManager.saveCatalog(this);
        } catch (IOException e) {
            throw new DDLParserException(DDLParser.CANNOT_SAVE_CATALOG);
        }
    }

    /**
     * Add a table to the catalog
     * @return true if the table did not exist
     */
    public boolean addTable(String tableName, Table table) {
        if (tables.get(tableName) != null) {
            tables.put(tableName, table);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Get a table by its name
     * @param tableName the tables name
     * @return the table if it exists. Otherwise null
     */
    public Table getTable(String tableName) {
        return tables.getOrDefault(tableName, null);
    }

    /**
     * Drop a table from the catalog
     * @param tableName the name of the table
     * @return true if the table was deleted
     */
    public boolean dropTable(String tableName) {
        if (tables.get(tableName) != null) {
            tables.remove(tableName);
            return true;
        } else {
            return false;
        }
    }
}
