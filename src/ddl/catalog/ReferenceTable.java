package ddl.catalog;

import storagemanager.StorageManagerException;

import java.util.*;

/**
 * A reference table is a structure that saves some work by storing the already considered arrays,
 * its good for many updates or inserts, but slower when only doing a few updates
 * @author Nicholas Chieppa
 */
public class ReferenceTable {

    /**
     * The set of reference tuple values on the references side of the query
     */
    private Map<Integer, List<Object[]>> references = new HashMap<>();

    /**
     * The order that the key is considered in
     */
    private ArrayList<Integer> accessOrder = new ArrayList<>();

    /**
     * Create a reference table to store information about tables to be compared
     * The list of attributes are in order of the indexes that are supposed to be accessed
     * @param referenceTable the table being references
     * @param referenceAttr the attributes being references
     * @param source the table that has the foreign key written on it
     * @param sourceAttributes the attributes that match to the reference attr
     */
    public ReferenceTable(Table referenceTable, List<Attribute> referenceAttr, Table source, List<Attribute> sourceAttributes) throws StorageManagerException {
        Object[][] referenceTableData = referenceTable.getRecords();
        for (Object[] tuple : referenceTableData) {
            Object[] referenceTuple = new Object[referenceAttr.size()];
            int hash = 0;
            int i = 0;
            for (Attribute attribute: referenceAttr) {
                referenceTuple[i] = tuple[referenceTable.getIndex(attribute)];
                hash = Objects.hash(hash, tuple[referenceTable.getIndex(attribute)]);
            }
            if (!references.containsKey(hash))
                references.put(hash, new ArrayList<>(){{add(referenceTuple);}});
            else
                references.get(hash).add(referenceTuple);
        }

        for (Attribute attribute : sourceAttributes)
            accessOrder.add(source.getIndex(attribute));
    }

    private int compare(Object[] refTuple, Object[] tuple) {
        for (int i = 0; i < accessOrder.size(); i++) {
            int c = Table.compareAttrValues(refTuple[i], tuple[accessOrder.get(i)]);
            if(c != 0) return c;
        }
        return 0;
    }

    public boolean match(Object[] tuple) {
        int hash = 0;
        for (Integer index: accessOrder) {
            hash = Objects.hash(hash, tuple[index]);
        }
        if (references.containsKey(hash)) {
            for (Object[] refTuple : references.get(hash)) {
                if (compare(refTuple, tuple) == 0) return true;
            }
        }
        return false;
    }
}
