package storagemanager.buffermanager.page;

import storagemanager.StorageManager;
import storagemanager.buffermanager.datatypes.Datatype;
import storagemanager.buffermanager.Table;
import storagemanager.StorageManagerException;
import storagemanager.buffermanager.diskUtils.DataManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class RecordPage extends Page<Object[]> {

    /**
     * A page class, this stores our records and id ( does not need to be volatile talked to professor )
     * Oh also it makes sense for a page to be a Java file, as it should have a literal place on the computer
     * it represents upon construction.
     */

    private static final long serialVersionUID = 2L;

    private Object[][] records;

    RecordPage(Table table, int pageID){
        super(table, pageID, PageTypes.RECORD_PAGE, 2);
        this.records = new Object[table.getMaxRecords()][];
    }

    public void updateRecord(Object[] record) throws StorageManagerException {

        int index = findRecord(record);

        if(index < 0)
            throw new StorageManagerException(StorageManager.UPDATE_RECORD_NOT_FOUND);

        // otherwise we just update it
        records[index] = record;

    }

    @Override
    public boolean insertRecord(Object[] record) throws StorageManagerException {
        // we split if we are full.
        if(!hasSpace()){
            splitPage();
            bufferManager.insertRecord(table.getId(), record);
            return true;
        }


        // iterative binary
        int l = 0,r=entries, m=0;
        while (l <= r) {
            m = l + (r - l) / 2;

            // retrieve record at m
            // Check if record is present at mid
            if(records[m] == null) {
                break;
            }

            int res = compareRecord(record, m);

            // in this case the record already exists in the page
            if(res == 0)
                throw new StorageManagerException(String.format(StorageManager.INSERT_RECORD_EXISTS_FORMAT, recordToString(record)));

            // If record greater, ignore left half
            if (res == 1)
                l = m + 1;
                // If record is smaller, ignore right half
            else
                r = m - 1;
        }
        // we need to compare our value to the value we are inserting on
        //m = l;
        // if we are greater than the record we are inserting over, and it is not null we are inbetewen two values
        // and we are greater than the record we are inserting over, so we want to insert +1 more than where we are inserting
        if(records[m] != null) {
            int res = compareRecord(record, m);
            if(res == 1)
                m += 1;
        }

        // if we reach here, then element was
        // not present

        // this gives us all the entries above m
        int aboveIndex = entries-(m+1);
        for(int i=aboveIndex; i>=0; i--){
            int currentIndex = m+i;
            int newIndex = m+i+1;
            records[newIndex] = records[currentIndex];
        }

        records[m] = record;
        entries++;
        return true;
    }

    @Override
    public boolean deleteRecord(Object[] key) throws StorageManagerException {
        int index = findRecord(key);

        if(index < 0)
            throw new StorageManagerException(StorageManager.REMOVE_RECORD_NOT_FOUND);

        // otherwise we remove it
        records[index] = null;
        for (int i = index; i < entries; i++) {
            if (i + 1 == table.getMaxRecords()) {
                records[i] = null;
                break;
            }
            records[i] = records[i+1];
        }
        entries--;

        if (entries < minRecords && !(table.getPages().last() == pageID) && !(table.getPages().first() == pageID)) {
            mergePage();
        }

        return true;
    }

    @Override
    public void mergePage() throws StorageManagerException {
        delete();
        for (Object[] record: getRecords()) {
            bufferManager.insertRecord(table.getId(), record);
        }
    }

    @Override
    public boolean recordExists(Object[] record) {
        return false;
    }

    @Override
    /**
     *
     */
    public Page<Object[]> splitPage() throws StorageManagerException {
        RecordPage other = (RecordPage) Page.createPage(table, pageID + 1, PageTypes.RECORD_PAGE, bufferManager, pageBuffer);

        // split at n/2
        int splitPoint = Math.floorDiv(entries, 2);
        int j=0;

        int startOffset = splitPoint;

        // if we don't have a equal split we increment the start offset up one
        if(entries-splitPoint != splitPoint){
            startOffset+=1;
        }

        for(int i=startOffset; i<entries; i++){
            other.setRecord(this.records[i].clone(), j);
            this.records[i] = null;
            j++;
        }

        other.entries = splitPoint;
        this.entries = j + ((table.getMaxRecords() % 2 == 0)?0:1);

        // Creating a new page may have pushed this one out. This page needs to be resaved
        if (pageBuffer.isPageLoaded(table.getId(), PageTypes.RECORD_PAGE, pageID) == null)
            this.save();
        return other;
    }

    @Override
    public boolean hasSpace() {
        return table.getMaxRecords() > entries;
    }

    @Override
    public void save() throws StorageManagerException {
        DataManager.savePage(this,table.getId());
    }

    /**
     * find's a record within a page
     */
    public int findRecord(Object[] recordOrKey) {
        Object[] record = table.getRecordFromKey(recordOrKey);

        // iterative binary search
        int l = 0, r = entries - 1;
        while (l <= r) {
            int m = l + (r - l) / 2;

            // retrieve record at m
            // Check if record is present at mid
            int res = compareRecord(record, m);
            if(res == 0)
                return m;

            // If record greater, ignore left half
            if (res == 1)
                l = m + 1;
                // If record is smaller, ignore right half
            else
                r = m - 1;
        }

        // if we reach here, then element was
        // not present
        return -1;
    }
    /***
     * getRecord uses the findRecord method to find the index then returns the object[]
     */
    public Object[] getRecord(Object[] record)  {
        int index = findRecord(record);
        if (index < 0) return null;
        return records[index];
    }

    @Override
    public Object[][] getRecords() {
        return Arrays.copyOf(this.records, entries);
    }

    /**
     * Get's the bounds of a page
     */
    public int[] bounds(Object[] recordOrKey) {
        Object[] record = table.getRecordFromKey(recordOrKey);
        return new int[]{compareRecord(record, 0), compareRecord(record, entries-1)};
    }

    /**
     * This method compares a record to another record returns
     * 1 : the record is greater than the other record
     * -1: the record is less than the other record
     * 0: the record is equal to the other record
     */
    private int compareRecord(Object[] recordOrKey, int index) {

        Object[] record = table.getRecordFromKey(recordOrKey);

        for(int i=0; i < table.getKeyIndices().length; i++){

            int keyIndex = table.getKeyIndices()[i];

            int ret = 0;

            ret = table.compareDataTypes(keyIndex, record[keyIndex], records[index][keyIndex]);

           if(ret != 0) {
               // restricting this function to return -1 or 1
               if(ret >= 1)
                   return 1;
               else
                   return -1;
           }
        }

        return 0;

    }



    /**
     * Sets a pages records to a passed in value at given index
     * @param records
     */
    public void setRecord(Object[] records, int index) {
        this.records[index] = records;
    }

    /**
     * Convert a records values into a string
     * @param record the record being converted
     * @return a comma separated string representing the record
     */
    private String recordToString(Object[] record) {
        StringBuilder builder = new StringBuilder();
        ArrayList<Datatype> datatypes = table.getDatatypes();
        builder.append("{");
        for (int i = 0; i < datatypes.size(); i++) {
            Datatype datatype = datatypes.get(i);
            builder.append(datatype.resolveToString(record[i]));
            if (i + 1 < datatypes.size()) builder.append(", ");
        }
        builder.append("}");
        return builder.toString();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(getPageType()).append(":").append(pageID).append("\n");
        builder.append("\t").append("Entries\\Max").append(": ").append(entries).append(" ").append(table.getMaxRecords());
        return builder.toString();
    }
}
