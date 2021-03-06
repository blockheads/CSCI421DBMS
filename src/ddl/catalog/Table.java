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

    public static final String INTERNAL_TABLE_SIG = "_internal_";
    private static final TableIDGenerator idGenerator = new TableIDGenerator();

    private int tableID;
    private final String tableName;
    private final Set<String> tableSubnames;
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
        this.tableSubnames = new HashSet<>();
        this.attributes = new HashSet<>(attributes);

        for (int i = 0; i < attributes.size(); i++) {
            Attribute attribute = attributes.get(i);
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

    public Table(List<Table> joinedTables) throws DDLParserException {
        this(generateInternalIdentifier(), new ArrayList<>() {{
            for (Table table: joinedTables) {
                if (table.tableName.contains(INTERNAL_TABLE_SIG))
                {
                    for (Attribute attribute : table.getAttributes()) {
                        if (attribute.getName().contains(INTERNAL_TABLE_SIG))
                            continue;
                        this.add(new Attribute(attribute.getName(), attribute.getDataType()));
                    }
                } else {
                    for (Attribute attribute : table.getAttributes()) {
                        this.add(new Attribute(table.getTableName() + "." + attribute.getName(), attribute.getDataType()));
                    }
                }
            }
        }});
        for (Table table: joinedTables) {
            tableSubnames.add(table.getTableName());
            tableSubnames.addAll(table.getTableSubnames());
        }
        String[] attrNames = new String[this.attributes.size()];
        for (Attribute attribute : attributeIndices.keySet()) {
            attrNames[attributeIndices.get(attribute)] = attribute.getName();
        }
        setPrimaryKey(attrNames);
    }

    /**
     * Join two tables together by creating an internal table,
     * Remember to drop this table when done.
     * @param other the table to join this
     * @return a new table, the joined table has a primary key on every attribute
     */
    public Table join(Table other) throws DDLParserException, StorageManagerException {
        List<Table> tables = new ArrayList<>(2);
        tables.add(0,this); tables.add(1,other);
        return join(new Table(tables), other);
    }

    /**
     * Join two tables together by instantiating an internal table,
     * Remember to drop this table when done.
     * @param descriptor a table that describes the resultant, this table should have a primary key defined.
     *                   This table should not be in the catalog or have an underlying table defined
     * @param other the table to join this
     * @return descriptor object with an id defined and records from both tables in the storage manager
     */
    public Table join(Table descriptor, Table other) throws StorageManagerException {
        descriptor.setTableID(Database.catalog.generateTableID());
        descriptor.createTable();

        Object[][] thisData = this.getRecords();
        Object[][] otherData = other.getRecords();

        for (Object[] thisRecord : thisData) {
            for (Object[] otherRecord : otherData) {
                Object[] newRecord = new Object[descriptor.attributes.size()];
                for (Attribute attribute : this.attributes) {
                    if (descriptor.containsAttribute(tableName, attribute.getName())) {
                        newRecord[descriptor.getIndex(descriptor.getAttribute(tableName, attribute.getName()))] = thisRecord[attributeIndices.get(attribute)];
                    }
                }
                for (Attribute attribute : other.attributes) {
                    if (descriptor.containsAttribute(other.tableName, attribute.getName())) {
                        newRecord[descriptor.getIndex(descriptor.getAttribute(other.tableName, attribute.getName()))] = otherRecord[other.attributeIndices.get(attribute)];
                    }
                }
                try {
                    descriptor.addRecord(newRecord);
                } catch (StorageManagerException e) {}
            }
        }
        return descriptor;
    }

    public Table project(Table descriptor, Set<Object[]> records) throws StorageManagerException {
        descriptor.setTableID(Database.catalog.generateTableID());
        descriptor.createTable();

        for (Object[] record : records) {
            Object[] newRecord = new Object[descriptor.attributes.size()];
            for (Attribute attribute : descriptor.attributes) {
                newRecord[descriptor.getIndex(descriptor.getAttribute(attribute.getName()))] = record[getAttributeIndex(attribute.getName())];
            }
            descriptor.addRecord(newRecord);
        }
        return descriptor;
    }

    public static String generateInternalIdentifier() {
        return Table.INTERNAL_TABLE_SIG + idGenerator.getNewID();
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

    public void addSubname(String subname) {
        tableSubnames.add(subname);
    }

    public boolean isPartTable(String name) {
        return tableName.equals(name) || hasSubName(name);
    }

    public boolean hasSubName(String name) {
        return tableSubnames.contains(name);
    }

    public Set<String> getTableSubnames() {
        return new HashSet<>(tableSubnames);
    }

    public int getSubtableCount() {
        return tableSubnames.size();
    }

    public Integer getAttributeIndex(String name) {
        return attributeIndices.get(getAttribute(name));
    }

    public List<Attribute> getAttributes() {
        List<Attribute> attributes = new ArrayList<>(attributeIndices.keySet().size());
        for (Attribute attribute : attributeIndices.keySet()) {
            attributes.add(attributeIndices.get(attribute), attribute);
        }
        return attributes;
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
    public void dropTable() throws StorageManagerException {
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
    public void addRecord(String recordString) throws StorageManagerException, DataTypeException {
        addRecord(getRecordFromString(recordString));
    }

    /**
     * Update a record in the underlying table
     * @param record the record to update
     * @throws StorageManagerException the record could not be updated
     */
    public void updateRecord(Object[] record) throws StorageManagerException {
        Database.storageManager.updateRecord(tableID, record);
    }
    public void updateRecord(Set<Object[]> records) throws StorageManagerException {
        for (Object[] tuple : records)
            updateRecord(tuple);
    }

    /**
     * Delete a record from the table
     * @param record a record to delete
     * @throws StorageManagerException the record could not be deleted
     */
    public void deleteRecord(Object[] record) throws StorageManagerException {
        Database.storageManager.removeRecord(tableID, getPrimaryKeyAttrValues(record));
    }

    public Object[] getRecordFromString(String recordString) throws StorageManagerException, DataTypeException {
        Object[] record = new Object[attributes.size()];

        String[] recordData = recordString.split(" [ ]*");

        for (Attribute attribute: attributes) {
            int index = attributeIndices.get(attribute);
            if(recordData[index].equals("null"))
                record[index] = null;
            else
                record[index] = Database.storageManager.underlyingDatatypes(tableID).get(index).parseData(recordData[index]);
        }
        return record;
    }

    private String[] generateDatatype() {
        String[] dataTypes = new String[attributes.size()];
        for (Attribute attribute : attributes) {
            dataTypes[attributeIndices.get(attribute)] =  attribute.getDataType();
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
        if (name.contains(".")) {
            String[] nameSplit = name.split("\\.", 2);
            if (nameSplit[0].equals(tableName))
                return attributeMap.get(nameSplit[1]);
            else if (tableSubnames.contains(nameSplit[0]))
                return attributeMap.get(name);
            return null;
        }
        for (String subName : tableSubnames){
            Attribute got = attributeMap.get(subName + "." + name);
            if (got != null) return got;
        }
        return attributeMap.get(name);
    }

    public Attribute getAttribute(String tableSubname, String attributeName) {
        return getAttribute(tableSubname + "." + attributeName);
    }

    public boolean containsAttribute(String name) {
        return (getAttribute(name) != null);
    }

    public boolean containsAttribute(String tableSubname, String attributeName) {
        return (getAttribute(tableSubname + "." + attributeName) != null);
    }

    public int getAttributeCount() {
        return attributes.size();
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
            containsAttributeError(name);
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
            containsAttributeError(name);
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
            if (tuple[attributeIndices.get(attribute)] == null && (attribute.hasConstraint(Constraint.NOTNULL) || primaryKeyParts.contains(attribute))) {
                return false;
            }
        }
        return true;
    }

    public boolean checkUniqueConditions(Object[][] table, Set<Object[]> tuples) {

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
                    uniqueLists.put(hash, new ArrayList<>());
                uniqueLists.get(hash).add(new UniquePair(uniqueEntry, index));
                index++;
            }

            HashMap<Integer, ArrayList<Object[]>> generatedLists = new HashMap<>();
            for (Object[] tuple : tuples) {
                Object[] uniqueEntry = new Object[unique.size()];
                int i = 0;
                int hash = 0;
                for (Attribute attribute : unique) { // generate an object list for the tuple
                    uniqueEntry[i] = tuple[attributeIndices.get(attribute)];
                    hash = Objects.hash(hash, uniqueEntry[i]);
                }

                if (generatedLists.containsKey(hash)) { // check the new values against themselves
                    for (Object[] generatedTuple : generatedLists.get(hash)) {
                        if (compareAttrValues(generatedTuple, uniqueEntry) == 0) return false;
                    }
                    generatedLists.get(hash).add(uniqueEntry);
                } else {
                    generatedLists.put(hash, new ArrayList<>() {{
                        add(uniqueEntry);
                    }});
                }

                for (UniquePair uniquePair : uniqueLists.getOrDefault(hash, new ArrayList<>())) {
                    if (compareAttrValues(uniquePair.tuple, uniqueEntry) == 0) // found an identical unique
                        if (compareAttrValues(getPrimaryKeyAttrValues(table[uniquePair.index]), getPrimaryKeyAttrValues(tuple)) != 0) // attributes with the same primary key dont count
                            return false;
                }
            }
        }
        return true;
    }

    private transient Map<ForeignKey, ReferenceTable> referenceTableMap = new HashMap<>();
    public boolean checkForeignKeyConditions(Object[] tuple, boolean reset) throws StorageManagerException {
        for (ForeignKey foreignKey : foreignKeys) {
            ReferenceTable referenceTable = (reset)?foreignKey.getReferenceTable(this):this.referenceTableMap.get(foreignKey);
            referenceTableMap.put(foreignKey, referenceTable);
            if (!referenceTable.match(tuple)) return false;
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

            table.containsAttributeError(reference);
            containsAttributeError(attribute);

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
        if (containsAttributeError(name)) {
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

    private boolean containsAttributeError(String name) throws DDLParserException {
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
        return tableName.hashCode();
    }
}
