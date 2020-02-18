package ddl.catalog;

import ddl.DDLParserException;

import java.io.Serializable;
import java.util.*;

public class Table implements Serializable {
    private final int tableID;
    private Set<Attribute> attributes;
    private Map<String, Attribute> attributeMap = new HashMap<>();

    /**
     *  the primary key of the table if set it cannot be added to or reset
     */
    private Set<Attribute> primaryKey;
    private ArrayList<Set<Attribute>> uniques = new ArrayList<>();
    private ArrayList<ArrayList<Attribute>> foreignKeys = new ArrayList<>();

    public Table(int tableID, Set<Attribute> attributes) throws DDLParserException {
        this.tableID = tableID;
        this.attributes = attributes;

        for (Attribute attribute: attributes) {
            // use this loop to find 1 attribute of the table with a primary key constraint.
            // If there is more than one then error
            if (attribute.hasConstraint(Constraint.PRIMARY_KEY)) {
                if (primaryKey == null) {
                    primaryKey = new HashSet<>() {{add(attribute);}};
                } else {
                    throw new DDLParserException(""); // multiple primary keys
                }
            }

            if (attribute.hasConstraint(Constraint.UNIQUE)) { // add singleton uniques
                uniques.add(new HashSet<>(){{add(attribute);}});
            }

            // add all the attributes to a named map
            if (attributeMap.get(attribute.getName()) == null) {
                attributeMap.put(attribute.getName(), attribute);
            } else {
                throw new DDLParserException(""); // duplicate name
            }

            // add uniques to unique map
        }
    }

    public storagemanager.buffermanager.Table getUnderlyingTable() {
        return null;
    }

    public void setPrimaryKey(Set<Attribute> attributes) throws DDLParserException {
        if (primaryKey == null) {
            primaryKey = attributes; // todo add check to make sure added attributes exist
        } else {
            throw new DDLParserException(""); // primary key already set, multiple primary keys defined
        }
    }

    public void setPrimaryKey(String... names) throws DDLParserException {
        if (primaryKey != null)
            throw new DDLParserException(""); // attempt to define multiple primary keys
        for (String name: names) {
            if (attributeMap.containsKey(name)) {

            } else {
                throw new DDLParserException(""); // attribute with given name does not exist
            }
        }
    }

}
