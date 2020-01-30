package buffermanager;

import buffermanager.Datatype.Datatype;
import buffermanager.Datatype.StaticData;
import buffermanager.Datatype.ValidDataTypes;
import datamanager.DataManager;
import storagemanager.StorageManagerException;

import java.io.Serializable;
import java.util.ArrayList;

public class Table implements Serializable {
    public int getId() {
        return id;
    }

    private final int id;
    // specific UID to make several runs compatiable when loading in/saving objects
    private static final long serialVersionUID = 1L;

    private int recordSize = 0;
    private Integer[] keyIndices;

    private ArrayList<Datatype> datatypes = new ArrayList<>();

    // This let's us know if the pageMap corresponding to this table has been loaded into memory.
    private boolean loadedPageMap;

    // this is the list of all the pages associated with the table.
    private Integer[] pages;

    public int getMaxRecords() {
        return maxRecords;
    }

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

    public int dataTypeCount() {return datatypes.size();}

    public Integer[] getKeyIndices() {
        return keyIndices;
    }

    public ArrayList<Datatype> getDatatypes() {
        return datatypes;
    }

    public int compareDataTypes(int index, Object obj1, Object obj2) {
        return datatypes.get(index).compareObjects(obj1, obj2);
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
