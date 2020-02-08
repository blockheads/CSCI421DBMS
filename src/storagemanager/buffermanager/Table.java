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
    private Integer[] byteKeyIndices;

    private ArrayList<Datatype> datatypes = new ArrayList<>();

    // int that tells us the current highest page
    private transient TreeSet<Integer> highestPage;

    // This let's us know if the pageMap corresponding to this table has been loaded into memory.
    private boolean loadedPageMap;

    // this is the max amount of records which can be stored inside of a table
    private int maxRecords;

    public Table(int id, String[] dataTypes, Integer[] keyIndices, int pageSize) throws StorageManagerException {
        this.id = id;

        // calculates recordSize
        for(String dataType: dataTypes){
            Datatype attribute = ValidDataTypes.resolveType(dataType);
            recordSize += attribute.getSize();
            if (this.datatypes.size() == 0) attribute.setIndex(0);
            else attribute.setIndex(this.datatypes.get(this.datatypes.size() - 1).nextIndex());
            this.datatypes.add(attribute);
        }
        // do some math here

        this.keyIndices = keyIndices;
        this.byteKeyIndices = new Integer[keyIndices.length];
        Integer[] indices = this.keyIndices;
        for (int i = 0; i < indices.length; i++) {
            Integer keyIndex = indices[i];
            byteKeyIndices[i] = this.datatypes.get(keyIndex).getIndex();
        }
        this.maxRecords = Math.floorDiv(pageSize, recordSize);
    }

    public int getMaxRecords() {
        return maxRecords;
    }

    public int getNewHighestPage() {
        if (this.highestPage == null)
            this.highestPage = DataManager.getPages(id);
        this.highestPage.add(this.getHighestPage() + 1);
        return this.highestPage.last();
    }

    public int getHighestPage() {
        if (highestPage == null)
            highestPage = DataManager.getPages(id);
        if (highestPage.isEmpty())
            return -1;
        return highestPage.last();
    }

    public void resetPages() {
        highestPage = null;
    }

    public void removePage(RecordPage page) {
        this.highestPage.remove(page.getPageID());
    }
    public void setPages(TreeSet<Integer> pages) {
        this.highestPage = pages;
    }

    /**
     * Simply returns all the pages associated with this table, in this case it's a range from 0 to the
     * highest page
     * @return
     */
    public TreeSet<Integer> getPages(){
        getHighestPage();
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

    public Object[] recordToKey(Object[] record) {
        Object[] recordKey = new Object[keyIndices.length];
        int pos = 0;
        for (Integer keyIndex: this.keyIndices) {
            recordKey[pos++] = record[keyIndex];
        }
        return  recordKey;
    }

    public Object[] keyToRecord(Object[] key) {
        Object[] keyRecord = new Object[datatypes.size()];
        int pos = 0;
        for (Integer integer: keyIndices) {
            keyRecord[integer] = key[pos++];
        }
        return  keyRecord;
    }


    /**
     * Takes in a record and returns only the part's of the object[] which contain
     * key indices
     * @param record
     * @return
     */
    public Object[] getKeys(Object[] record){
        Integer[] keyIndeces = getKeyIndices();
        Object[] keys = new Object[keyIndeces.length];
        int j=0;
        for( int i: keyIndeces){
            keys[j] = record[i];
            j++;
        }
        return keys;
    }

    public Integer[] getByteKeyIndices() {
        return byteKeyIndices;
    }

    public ArrayList<Datatype> getDatatypes() {
        return datatypes;
    }

    public boolean validRecord(Object[] record) {
        for (int i = 0; i < record.length; i++) {
            Object attribute = record[i];
            if (attribute != null && !datatypes.get(i).matches(attribute))
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

    public Object[] resolveBytesAsObject(byte[] record) {
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
