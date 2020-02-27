package ddl.catalog;

import database.Database;
import ddl.DDLParserException;
import storagemanager.StorageManagerException;

import java.io.Serializable;
import java.util.*;

public class Table implements Serializable {
    private int tableID;
    private final String tableName;
    private final Set<Attribute> attributes;
    private final Map<Attribute, Integer> attributeIndices = new HashMap<>();
    private final Map<String, Attribute> attributeMap = new HashMap<>();

    /**
     *  the primary key of the table if set it cannot be added to or reset
     */
    private ArrayList<Attribute> primaryKey;
    private ArrayList<Set<Attribute>> uniques = new ArrayList<>();
    private Set<ForeignKey> foreignKeys = new HashSet<>();

    public Table(String tableName, ArrayList<Attribute> attributes) throws DDLParserException {
        this.tableName = tableName;
        this.attributes = new HashSet<>(attributes);
        if (this.attributes.size() != attributes.size())
            throw new DDLParserException("Duplicated attribute");

        for (int i = 0; i < attributes.size(); i++) {
            Attribute attribute = attributes.get(i);
            // use this loop to find 1 attribute of the table with a primary key constraint.
            // If there is more than one then error
            if (attribute.hasConstraint(Constraint.PRIMARYKEY)) {
                if (primaryKey == null) {
                    primaryKey = new ArrayList<>() {{
                        add(attribute);
                    }};
                } else {
                    throw new DDLParserException(""); // multiple primary keys
                }
            }

            if (attribute.hasConstraint(Constraint.UNIQUE)) { // add singleton uniques
                uniques.add(new HashSet<>() {{
                    add(attribute);
                }});
            }

            attributeMap.put(attribute.getName(), attribute);
            attributeIndices.put(attribute, i);
        }
    }

    /**
     * Set the id of the table
     * @param tableID a new table id
     */
    void setTableID(int tableID) {
        this.tableID = tableID;
    }

    /**
     * get the id of the table
     * @return the tables underlying id, used in the storage manager
     */
    int getTableID() {
        return tableID;
    }

    public String getTableName() {
        return tableName;
    }

    /**
     * Get the records in the table
     * @return the records in a table
     * @throws StorageManagerException if the table does not exist
     */
    public Object[][] getRecords() throws StorageManagerException {
        return Database.storageManager.getRecords(tableID);
    }

    /**
     * Delete underlying table from db
     * @throws StorageManagerException the table does not exist
     */
    void dropTable() throws StorageManagerException {
        Database.storageManager.dropTable(tableID);
    }

    /**
     * Create a new underlying table
     * @throws StorageManagerException the table already exists
     */
    void createTable() throws StorageManagerException {
        Database.storageManager.addTable(tableID, generateDatatype(), generateKeyIndices());
    }

    private String[] generateDatatype() {
        String[] dataTypes = new String[attributes.size()];
        int i = 0;
        for (Iterator<Attribute> iterator = attributes.iterator(); iterator.hasNext(); i++) {
            Attribute attribute = iterator.next();
            dataTypes[i] = attribute.getDataType().toString();
        }
        return dataTypes;
    }

    private Integer[] generateKeyIndices() {
        Integer[] primaryKey = new Integer[this.primaryKey.size()];
        ArrayList<Attribute> key = this.primaryKey;
        for (int i = 0; i < key.size(); i++) {
            Attribute attribute = key.get(i);
            primaryKey[i] = attributeIndices.get(attribute);
        }
        return primaryKey;
    }

    /**
     * Checks if a primary key has been defined
     * @return true if a primary key constraint exists
     */
    public boolean hasPrimaryKey() {
        return primaryKey != null;
    }

    public boolean partOfPrimaryKey(String name) throws DDLParserException {
        if (attributeMap.containsKey(name)) {
            return primaryKey.contains(attributeMap.get(name));
        } else {
            throw new DDLParserException(""); // attribute does not exist
        }
    }

    /**
     * Get an attribute in the table if it exists
     * @param name the name of the attribute
     * @return the attribute or null
     */
    public Attribute getAttribute(String name) {
        return attributeMap.get(name);
    }

    /**
     * Create a primary key from a constraint
     * @param names the names of the attributes in the key
     * @throws DDLParserException primary key already defined, or attribute in primary key constraint dne
     */
    public void setPrimaryKey(String[] names) throws DDLParserException {
        if (primaryKey != null)
            throw new DDLParserException(""); // attempt to define multiple primary keys
        primaryKey = new ArrayList<>();
        for (String name: names) {
            if (attributeMap.containsKey(name)) {
                primaryKey.add(attributeMap.get(name));
            } else {
                primaryKey = null;
                throw new DDLParserException(""); // attribute with given name does not exist
            }
        }
    }

    public void addUnique(String[] names) throws DDLParserException {
        final Set<Attribute> attributes = new HashSet<>();
        for (String name: names) {
            if (attributeMap.containsKey(name)) {
                attributes.add(attributeMap.get(name));
            } else
                throw new DDLParserException("");
        }
        uniques.add(attributes);
    }

    void typeMatch(List<String> attributes, Table table, List<String> references) throws DDLParserException {
        for (int i = 0; i < attributes.size(); i++) {
            String attribute = attributes.get(i);
            String reference = references.get(i);

            if (!table.attributeMap.containsKey(reference)) throw new DDLParserException("");
            if (!attributeMap.containsKey(attribute)) throw new DDLParserException("");

            if (!attributeMap.get(attribute).sameType(table.attributeMap.get(reference).getDataType())) {
                throw new DDLParserException(""); // type mismatch
            }
        }
    }

    void addForeignKey(ForeignKey foreignKey) {
        foreignKeys.add(foreignKey);
    }

    public void dropForeignKeysTo(String tableName) {
        for (Iterator<ForeignKey> iterator = foreignKeys.iterator(); iterator.hasNext(); ) {
            ForeignKey data = iterator.next();
            if (data.isReferencingTable(tableName)) {
                foreignKeys.remove(data);
            }
        }
    }

    void dropForeignsReferencing(String attribute) {
        for (Iterator<ForeignKey> iterator = foreignKeys.iterator(); iterator.hasNext(); ) {
            ForeignKey keyData = iterator.next();
            if (keyData.containsReference(attribute)) foreignKeys.remove(keyData);
        }
    }

    private void dropForeignsWithAttribute(String attribute) {
        for (Iterator<ForeignKey> iterator = foreignKeys.iterator(); iterator.hasNext(); ) {
            ForeignKey keyData = iterator.next();
            if (keyData.containsAttribute(attribute)) foreignKeys.remove(keyData);
        }
    }

    void dropAttribute(String name) throws DDLParserException {
        if (attributeMap.containsKey(name)) {
            if (primaryKey.contains(attributeMap.get(name))) {
                throw new DDLParserException(""); //cant drop primary keys
            }
            Attribute attribute = attributeMap.remove(name);
            removeUniques(attribute);
            dropForeignsWithAttribute(name);
        } else {
            throw new DDLParserException("");
        }
    }

    private void removeUniques(Attribute attribute) {
        for (Iterator<Set<Attribute>> iterator = uniques.iterator(); iterator.hasNext(); ) {
            Set<Attribute> unique = iterator.next();
            if (unique.contains(attribute)) uniques.remove(unique);
        }
    }

    public void addAttribute(Attribute attribute) throws DDLParserException {
        if (!attributeMap.containsKey(attribute.getName())) {
            attributeMap.put(attribute.getName(), attribute);
        } else {
            throw new DDLParserException("");
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Table)
            return tableID == ((Table) obj).tableID;
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(tableID);
    }
}
