package ddl.catalog;

import ddl.DDLParser;
import ddl.DDLParserException;
import storagemanager.StorageManagerException;
import storagemanager.buffermanager.diskUtils.DataManager;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Catalog implements Serializable {

    private static String TABLE_DOES_NOT_EXIST = "The table (%s) does not exist.";

    private static Catalog catalog;

    private final Map<String, Table> tables;
    private final TableIDGenerator idGenerator;

    private Catalog() {
        tables = new HashMap<>();
        idGenerator = new TableIDGenerator();
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
     * @return the catalog for this database
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
     * Try to load a catalog from disk, If this cannot be done then create a new one.
     * // Todo: I am unsure how a database is supposed to be deleted or created from scratch.
     * // Todo: in storage manager there is a restart parameter but that is missing from creating a database.
     * @return a catalog for the database to use
     */
    public static Catalog createOrLoadCatalog() {
        if (catalog != null) return catalog;
        try {
            catalog = loadCatalog();
        } catch (DDLParserException e) {
            catalog = newCatalog();
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
     * Add a table to the catalog.
     * @return true if the table did not exist
     * @throws StorageManagerException an underlying table with the same id already exists
     */
    public boolean addTable(Table table) throws StorageManagerException {
        if (tables.containsKey(table.getTableName())) return false;
        addTable(table, idGenerator.getNewID());
        return true;
    }

    /**
     * Replace a table in the catalog for a table with the same id
     * @param table the table to replace. The tables should have the same name
     * @return if the table was replaced
     * @throws StorageManagerException no table to replace
     */
    public boolean replaceTable(Table table) throws StorageManagerException {
        if (!tables.containsKey(table.getTableName())) return false;
        table.dropTable();
        addTable(table, tables.get(table.getTableName()).getTableID());
        return true;
    }

    /**
     * Add or replace a table in the catalog.
     * @param table the table to place
     * @param tableID the id the table should have
     * @post a table is added to the catalog even if it overwrites another
     */
    private void addTable(Table table, int tableID) throws StorageManagerException {
        table.setTableID(tableID);
        table.createTable();
        tables.put(table.getTableName(), table);
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
     * Remove an attribute from the table
     * @param table the table name
     * @param attribute the name of the attribute
     * @return the index that the attribute was removed from
     * @throws DDLParserException the attribute or table dne
     */
    public int removeAttributeFromTable(String table, String attribute) throws DDLParserException {
        if (tables.containsKey(table)) {
            int location = tables.get(table).dropAttribute(attribute);
            for (Table rtable: tables.values()) {
                rtable.dropForeignsReferencing(table, attribute);
            }
            return location;
        } else {
            throw new DDLParserException(String.format(TABLE_DOES_NOT_EXIST, table));
        }
    }

    /**
     * Drop a table from the catalog
     * @param tableName the name of the table
     * @throws StorageManagerException the underlying table cannot be dropped
     * @throws NullPointerException the table dne
     */
    public void dropTable(String tableName) throws StorageManagerException, NullPointerException{
        tables.remove(tableName).dropTable();
        for (String name: tables.keySet()) {
            tables.get(name).dropForeignKeysTo(tableName);
        }
    }
}
