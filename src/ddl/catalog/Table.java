package ddl.catalog;

import database.Database;
import ddl.DDLParserException;
import storagemanager.StorageManagerException;
import storagemanager.buffermanager.datatypes.DataTypeException;

import java.io.Serializable;
import java.util.*;

public class Table implements Serializable {

    private static final String DUPLICATED_ATTR_FORMAT = "Attribute %s previously defined on table %s.";
    private static final String DUPLICATED_NAME_ATTR_FORMAT = "Attribute %s shares a name with another attribute on table %s.";
    private static final String ATTR_DNE_FORMAT = "Attribute %s is not defined on table %s.";
    private static final String ATTR_PRIM_KEY_FORMAT = "Attribute %s is a primary key on table %s and cannot be dropped.";
    private static final String PRIM_KEY_EXISTS_FORMAT = "Table %s already has a defined primary key %s.";
    private static final String ATTR_DUPE_PRIM_KEY_FORMAT = "You can not have duplicate attributes in a primary key (%s).";
    private static final String TYPE_MISMATCH_FORMAT = "Table %s attribute %s(%s) referencing table %s attribute %s(%s) do not have equal types.";


    private int tableID;
    private final String tableName;
    private final Set<Attribute> attributes;
    private final Map<Attribute, Integer> attributeIndices = new HashMap<>();
    private final Map<String, Attribute> attributeMap = new HashMap<>();

    /**
     *  the primary key of the table if set it cannot be added to or reset
     */
    private ArrayList<Attribute> primaryKey;
    private Set<Attribute> primaryKeyParts;
    private ArrayList<Set<Attribute>> uniques = new ArrayList<>();
    private Set<ForeignKey> foreignKeys = new HashSet<>();

    public Table(String tableName, ArrayList<Attribute> attributes) throws DDLParserException {
        this.tableName = tableName;
        this.attributes = new HashSet<>(attributes);

        for (int i = 0; i < attributes.size(); i++) {
            Attribute attribute = attributes.get(i);
            // use this loop to find 1 attribute of the table with a primary key constraint.
            // If there is more than one then error
//            if (attribute.hasConstraint(Constraint.PRIMARYKEY)) {
//                if (primaryKey == null) {
//                    primaryKey = new ArrayList<>() {{
//                        add(attribute);
//                    }};
//                } else {
//                    throw new DDLParserException(String.format(PRIM_KEY_EXISTS_FORMAT, tableName, primaryKeys())); // multiple primary keys
//                }
//            }

            if (attribute.hasConstraint(Constraint.UNIQUE)) { // add singleton uniques
                uniques.add(new HashSet<>() {{
                    add(attribute);
                }});
            }

            if (attributeMap.containsKey(attribute.getName()))
                throw new DDLParserException(String.format(DUPLICATED_NAME_ATTR_FORMAT, attribute.getName(), tableName));
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

    /**
     * Add a record to the underlying table
     * @param record the record to add
     * @throws StorageManagerException inserting the record failed
     */
    public void addRecord(Object[] record) throws StorageManagerException {
        Database.storageManager.insertRecord(tableID, record);
    }

    public Object[] getRecordFromString(String recordString) throws StorageManagerException, DataTypeException {
        Object[] record = new Object[attributes.size()];

        String[] recordData = recordString.split(" [ ]*");

        for (Attribute attribute: attributes) {
            int index = attributeIndices.get(attribute);
            record[index] = Database.storageManager.underlyingDatatypes(tableID).get(index).parseData(recordData[index]);
        }
        return record;
    }

    public void addRecord(String recordString) throws StorageManagerException, DataTypeException {
        addRecord(getRecordFromString(recordString));
    }

    private String[] generateDatatype() {
        String[] dataTypes = new String[attributes.size()];
        int i = 0;
        for (Iterator<Attribute> iterator = attributes.iterator(); iterator.hasNext(); i++) {
            Attribute attribute = iterator.next();
            dataTypes[i] = attribute.getDataType();
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

    /**
     * Get an attribute in the table if it exists
     * @param name the name of the attribute
     * @return the attribute or null
     */
    public Attribute getAttribute(String name) {
        return attributeMap.get(name);
    }

    /**
     * Get the index of the attribute that you are getting
     * @param attribute the attribute
     * @return the index of the attribute
     */
    public Integer getIndex(Attribute attribute) {return attributeIndices.get(attribute); }

    /**
     * Create a primary key from a constraint
     * @param names the names of the attributes in the key
     * @throws DDLParserException primary key already defined, or attribute in primary key constraint dne
     */
    public void setPrimaryKey(String[] names) throws DDLParserException {
        if(names == null)
            return;
        if (primaryKey != null)
            throw new DDLParserException(PRIM_KEY_EXISTS_FORMAT); // attempt to define multiple primary keys
        ArrayList<Attribute> primaryKey = new ArrayList<Attribute>();
        Set<Attribute> primaryKeyParts = new HashSet<>();
        for (String name: names) {
            containsAttribute(name);
            if (primaryKeyParts.contains(attributeMap.get(name))) {
                throw new DDLParserException(String.format(ATTR_DUPE_PRIM_KEY_FORMAT, name));
            } else {
                primaryKey.add(attributeMap.get(name));
                primaryKeyParts.add(attributeMap.get(name));
            }
        }
        this.primaryKey = new ArrayList<>(primaryKey);
        this.primaryKeyParts = primaryKeyParts;
    }

    public void addUnique(String[] names) throws DDLParserException {
        final Set<Attribute> attributes = new HashSet<>();
        for (String name: names) {
            containsAttribute(name);
            attributes.add(attributeMap.get(name));
        }
        uniques.add(attributes);
    }

    /**
     * Get the attribute values that make up a tuples primary key
     * @param tuple the tuple to grab the primary key from
     * @return the primary key
     */
    private Object[] getPrimaryKeyAttrValues(Object[] tuple) {
        Object[] primaryKeyValues = new Object[primaryKey.size()];
        for (int i = 0; i < primaryKey.size(); i++) {
            primaryKeyValues[i] = tuple[attributeIndices.get(primaryKey.get(i))];
        }
        return primaryKeyValues;
    }

    private int compareAttrValues(Object[] obj1, Object[] obj2) {
        for (int i = 0; i < obj1.length; i++) {
            int c = compareAttrValues(obj1[i], obj2[i]);
            if (c != 0) return c;
        }
        return 0;
    }

    public static int compareAttrValues(Object obj1, Object obj2) {
        if (obj1 == null || obj2 == null) {
            return (obj1 == obj2)?0:((obj1 == null)?-1:1);
        }

        if (obj1 instanceof Comparable)
            return ((Comparable) obj1).compareTo(obj2);
        return 0;
    }

    /**
     * Check a tuple for nulls, helps to enforce the not null condition
     * @param tuple the tuple being checked for nulls
     * @return true if there are no nulls in notnull positions else false
     */
    public boolean checkNotNullConditions(Object[] tuple) {
        if (tuple.length < attributes.size()) return false;
        for (Attribute attribute: attributes) {
            if (attribute.hasConstraint(Constraint.NOTNULL) || primaryKeyParts.contains(attribute)) {
                if (tuple[attributeIndices.get(attribute)] == null) return false;
            }
        }
        return true;
    }

    public boolean checkUniqueConditions(Object[][] table, Object[] tuple) {

        final class UniquePair {
            final Object[] tuple;
            final int index;
            public UniquePair (Object[] tuple, int index) {
                this.tuple = tuple;
                this.index = index;
            }
        }

        for (Set<Attribute> unique : uniques) {
            HashMap<Integer, ArrayList<UniquePair>> uniqueLists = new HashMap<>();
            int index = 0;
            for (Object[] tableEntry : table) { // get all the uniques from the table
                Object[] uniqueEntry = new Object[unique.size()];
                int i = 0;
                int hash = 0;
                for (Attribute attribute : unique) {
                    uniqueEntry[i] = tableEntry[attributeIndices.get(attribute)];
                    hash = Objects.hash(hash, uniqueEntry[i++]);
                }

                ArrayList<UniquePair> hashList = uniqueLists.get(hash);
                if (hashList == null)
                    uniqueLists.put(hash, new ArrayList<>(){{add(new UniquePair(uniqueEntry, index));}});
                else
                    uniqueLists.get(hash).add(new UniquePair(uniqueEntry, index));
            }

            Object[] uniqueEntry = new Object[unique.size()];
            int i = 0;
            int hash = 0;
            for (Attribute attribute : unique) { // generate an object list for the tuple
                uniqueEntry[i] = tuple[attributeIndices.get(attribute)];
                hash = Objects.hash(hash, uniqueEntry[i]);
            }

            for (UniquePair uniquePair : uniqueLists.getOrDefault(hash, new ArrayList<>())) {
                if (compareAttrValues(uniquePair.tuple, uniqueEntry) == 0) // found an identical unique
                    if (compareAttrValues(getPrimaryKeyAttrValues(table[uniquePair.index]), getPrimaryKeyAttrValues(tuple)) != 0) // attributes with the same primary key dont count
                        return false;
            }
        }
        return true;
    }

    public boolean checkForeignKeyConditions(Set<Object[]> tuples) throws StorageManagerException {
        for (ForeignKey foreignKey : foreignKeys) {
            ReferenceTable referenceTable = foreignKey.getReferenceTable(this);
            for (Object[] tuple : tuples)
                if (!referenceTable.match(tuple)) return false;
        }
        return true;
    }

    void typeMatch(List<String> attributes, Table table, List<String> references) throws DDLParserException {
        for (int i = 0; i < attributes.size(); i++) {
            String attribute = attributes.get(i);
            String reference = references.get(i);

            table.containsAttribute(reference);
            containsAttribute(attribute);

            if (!attributeMap.get(attribute).sameType(table.attributeMap.get(reference).getDataType())) {
                throw new DDLParserException(
                        String.format(TYPE_MISMATCH_FORMAT, tableName, attribute, attributeMap.get(attribute).getDataType(),
                        table.tableName, reference, table.attributeMap.get(attribute).getDataType())); // type mismatch
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

    void dropForeignsReferencing(String tableName, String attribute) {
        for (Iterator<ForeignKey> iterator = foreignKeys.iterator(); iterator.hasNext(); ) {
            ForeignKey keyData = iterator.next();
            if (keyData.isReferencingTable(tableName) && keyData.containsReference(attribute)) foreignKeys.remove(keyData);
        }
    }

    private void dropForeignsWithAttribute(String attribute) {
        for (Iterator<ForeignKey> iterator = foreignKeys.iterator(); iterator.hasNext(); ) {
            ForeignKey keyData = iterator.next();
            if (keyData.containsAttribute(attribute)) foreignKeys.remove(keyData);
        }
    }

    int dropAttribute(String name) throws DDLParserException {
        if (containsAttribute(name)) {
            if (primaryKey.contains(attributeMap.get(name))) {
                throw new DDLParserException(String.format(ATTR_PRIM_KEY_FORMAT, name, tableName)); //cant drop primary keys
            }
            Attribute attribute = attributeMap.remove(name);
            attributes.remove(attribute);
            int index = attributeIndices.remove(attribute);
            attributeIndices.forEach((attr, integer) -> {
                if (integer > index) {
                    attributeIndices.put(attr, --integer);
                }
            });
            removeUniques(attribute);
            dropForeignsWithAttribute(name);
            return index;
        }
        return -1;
    }

    private void removeUniques(Attribute attribute) {
        uniques.removeIf(attr -> attr.contains(attribute));
    }

    public void addAttribute(Attribute attribute) throws DDLParserException {
        if (!attributeMap.containsKey(attribute.getName())) {
            attributeMap.put(attribute.getName(), attribute);
            attributes.add(attribute);
            attributeIndices.put(attribute, attributeIndices.size());
        } else {
            throw new DDLParserException(String.format(DUPLICATED_ATTR_FORMAT, attribute.getName(), tableName));
        }
    }

    private boolean containsAttribute(String name) throws DDLParserException {
        if (attributeMap.containsKey(name)) return true;
        throw new DDLParserException(String.format(ATTR_DNE_FORMAT, name, tableName));
    }

    private String arrayToString(String[] arry) {
        StringBuilder builder = new StringBuilder();
        builder.append('[');
        for (int i = 0; i < arry.length && builder.append(", ") != null; i++) {
            builder.append(arry[i]);
        }
        builder.append(']');
        return builder.toString();
    }

    private String primaryKeys() {
        StringBuilder builder = new StringBuilder();
        builder.append('[');
        for (Iterator<Attribute> iterator = primaryKey.iterator(); iterator.hasNext() && builder.append(", ") != null; ) {
            Attribute key = iterator.next();
            builder.append(key.getName());
        }
        builder.append(']');
        return builder.toString();
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
