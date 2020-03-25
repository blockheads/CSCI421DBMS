package ddl.catalog;

import database.Database;
import ddl.DDLParserException;
import storagemanager.StorageManagerException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

// class for storing foreign key data
public class ForeignKey implements Serializable {

    private static String INSIGNIFICANT_ATTRIBUTES = "Foreign Keys need equal amount of attributes (size=%s) to references (size=%s)";
    private static String TABLE_DNE = "The table you are referencing (%s) does not exist.";
    private static String CIRCULAR_FOREIGN_KEY = "A Foreign key cannot reference itself.";

    // <rname>
    private final String referenceTable;
    // (<r1>... <rN>):
    private List<String> references;
    // (<a1>...<aN>)
    private List<String> attributes;

    /**
     * Create a foreign key relationship
     * @param table the source table
     * @param attributes the attributes from the source table
     * @param referenceTable the reference table
     * @param references the attributes in the reference table
     * @throws DDLParserException Reference table dne, or types dont match
     */
    public ForeignKey(Table table, String[] attributes, String referenceTable, String[] references) throws DDLParserException {
        if (table.getTableName().equals(referenceTable)) throw new DDLParserException(CIRCULAR_FOREIGN_KEY);
        if (references.length != attributes.length) throw new DDLParserException(String.format(INSIGNIFICANT_ATTRIBUTES, attributes.length, references.length));
        if (Database.catalog.getTable(referenceTable) == null) throw new DDLParserException(String.format(TABLE_DNE, referenceTable));

        this.referenceTable = referenceTable;
        this.references = new ArrayList<String>(Arrays.asList(references));
        this.attributes = new ArrayList<String>(Arrays.asList(attributes));

        table.typeMatch(this.attributes, Database.catalog.getTable(referenceTable), this.references);

        table.addForeignKey(this);
    }

    public boolean isReferencingTable(String name) {
        return referenceTable.equals(name);
    }

    public boolean containsAttribute(String name) {
        return attributes.contains(name);
    }

    public boolean containsReference(String name) {
        return attributes.contains(name);
    }

    public boolean hasMatch(Object[] tuple) throws StorageManagerException {
        Table referenceTable = Database.catalog.getTable(this.referenceTable);
        Object[][] referenceRecords = referenceTable.getRecords();
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(references, attributes, referenceTable);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ForeignKey)
            return ((ForeignKey) obj).referenceTable.equals(referenceTable) &&  references.equals(((ForeignKey) obj).references) && attributes.equals(((ForeignKey) obj).attributes);
        return false;
    }
}
