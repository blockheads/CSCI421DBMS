package storagemanager.buffermanager;

import storagemanager.buffermanager.datatypes.Datatype;
import storagemanager.buffermanager.datatypes.ValidDataTypes;
import storagemanager.StorageManagerException;
import storagemanager.buffermanager.diskUtils.DataManager;
import storagemanager.buffermanager.page.Page;
import storagemanager.buffermanager.page.RecordPage;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;

public class Table implements Serializable {
    // specific UID to make several runs compatiable when loading in/saving objects
    private static final long serialVersionUID = 1L;

    private final int id;

    private int recordSize = 0;
    private Integer[] keyIndices;

    private ArrayList<Datatype> datatypes = new ArrayList<>();

    // int that tells us the current highest page
    private transient TreeSet<Integer> highestPage;

    // This let's us know if the pageMap corresponding to this table has been loaded into memory.
    private boolean loadedPageMap;

    // this is the max amount of records which can be stored inside of a table
    private int maxRecords;

    public Table(int id, String[] dataTypes, Integer[] keyIndices) throws StorageManagerException {
        this.id = id;

        // calculates recordSize
        for(String dataType: dataTypes){
            Datatype attribute = ValidDataTypes.resolveType(dataType);
            recordSize += attribute.getSize();
            this.datatypes.add(attribute);
            if (this.datatypes.size() == 0) attribute.setIndex(0);
            attribute.setIndex(this.datatypes.get(this.datatypes.size() - 1).nextIndex());
        }
        // do some math here

        this.keyIndices = keyIndices;
        this.maxRecords = Math.floorDiv(4096, recordSize);
    }

    public int getMaxRecords() {
        return maxRecords;
    }

    public int getNewHighestPage() {
        if (this.highestPage == null)
            this.highestPage = DataManager.getPages(id);
        this.highestPage.add(this.highestPage.last() + 1);
        return this.highestPage.last();
    }

    public int getHighestPage() {
        return highestPage.last();
    }

    public void removePage(RecordPage page) {
        this.highestPage.remove(page.getPageID());
    }

    /**
     * Simply returns all the pages associated with this table, in this case it's a range from 0 to the
     * highest page
     * @return
     */
    public TreeSet<Integer> getPages(){
        return new TreeSet<>(highestPage);
    }

    public int getId() {
        return id;
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

    public boolean validRecord(Object[] record) {
        for (int i = 0; i < record.length; i++) {
            Object attribute = record[i];
            if (!datatypes.get(i).matches(attribute))
                return false;
        }
        return true;
    }

    public int compareDataTypes(int index, Object obj1, Object obj2) {
        return datatypes.get(index).compareObjects(obj1, obj2);
    }

    public int compareArrayRecords(int key, byte[] first, byte[] second) {
        Datatype datatype = this.datatypes.get(key);
        int fromIndex = datatype.getIndex();
        int toIndex = datatype.nextIndex();
        return Arrays.compare(first, fromIndex, toIndex, second, fromIndex, toIndex);
    }

    public byte[] resolveRecordAsBytes(Object[] record) {
        ByteBuffer buffer = ByteBuffer.allocate(recordSize);
        for (int i = 0; i < record.length; i++) {
            Object obj = record[i];
            Datatype datatype = this.datatypes.get(i);

            buffer.put(datatype.toByteArray(obj));
        }
        return buffer.array();
    }

    public Object resolveBytesAsObject(byte[] record) {
        Object[] Orecord = new Object[datatypes.size()];
        for (int i = 0; i < datatypes.size(); i++) {
            Datatype datatype = datatypes.get(i);
            Orecord[i] = datatype.toObject(record, datatype.getIndex());
        }
        return Orecord;
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
