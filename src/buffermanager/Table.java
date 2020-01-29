package buffermanager;

import buffermanager.Datatype.Datatype;
import buffermanager.Datatype.ValidDataTypes;
import storagemanager.StorageManagerException;

import java.io.Serializable;
import java.util.ArrayList;

public class Table implements Serializable {
    private final int id;
    private int recordSize = 0;
    private Integer[] keyIndices;
    private ArrayList<Datatype> datatypes = new ArrayList<>();

    // This let's us know if the pageMap corresponding to this table has been loaded into memory.
    private boolean loadedPageMap;

    // this is the list of all the pages associated with the table.
    private Integer[] pages;

    // this is the max amount of records which can be stored inside of a table
    private int maxRecords;

    public Table(int id, String[] dataTypes, Integer[] keyIndices) throws StorageManagerException {
        this.id = id;

        // calculates recordSize
        for(String dataType: dataTypes){
            Datatype attribute = ValidDataTypes.resolveType(dataType);
            recordSize += attribute.getSize();
            this.datatypes.add(attribute);
        }
        // do some math here

        this.keyIndices = keyIndices;
        this.maxRecords = Math.floorDiv(4096, recordSize);
    }

    public int getRecordSize() {
        return recordSize;
    }

    public Integer[] getKeyIndices() {
        return keyIndices;
    }

    public boolean isLoadedPageMap() {
        return loadedPageMap;
    }

    public void setLoadedPageMap(boolean loadedPageMap) {
        this.loadedPageMap = loadedPageMap;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("table ").append(id).append(" with attributes: \n");
        for (Datatype datatype: this.datatypes) {
            builder.append(datatype.getType() + " size: " + datatype.getSize()).append("\n");
        }
        builder.append("Primary keys at: ");
        for (Integer index: keyIndices) {
            builder.append(index).append(", ");
        }
        builder.replace(builder.lastIndexOf(", "), builder.length(),"");
        return builder.toString();
    }
}
