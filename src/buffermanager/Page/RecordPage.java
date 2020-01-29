package buffermanager.Page;

import buffermanager.BufferManager;
import buffermanager.Table;
import storagemanager.StorageManagerException;

public class RecordPage extends Page<Object[]> {

    /**
     * A page class, this stores our records and id ( does not need to be volatile talked to professor )
     * Oh also it makes sense for a page to be a Java file, as it should have a literal place on the computer
     * it represents upon construction.
     */

    private static final long serialVersionUID = 2L;

    private Object[][] records;

    public int getEntriesCount() {
        return entries;
    }

    // the amount of current records stored inside the page
    private int entries;

    public RecordPage(int id, Table table, BufferManager bufferManager){
        super(id, table, bufferManager);
        // initially just a empty array with no entries?
        System.out.println("created new page with maxRecords: " + table.getMaxRecords() + " and record size: " + table.getRecordSize());
        this.records = new Object[table.getMaxRecords()][table.dataTypeCount()];
    }

    @Override
    public void insertRecord(Object[] record) throws StorageManagerException {

    }

    @Override
    public void deleteRecord(Object[] record) throws StorageManagerException {

    }

    @Override
    public boolean recordExists(Object[] record) {
        return false;
    }


    /**
     * Checks if a page has space to write to
     * @return
     */
    public boolean hasSpace(Table table){
        return entries < table.getMaxRecords();
    }



    public void insertRecord(int index, Object[] record){
        records[index] = record;
        entries++;
        // just for nice testing output
        int remaining = records.length-entries;
        System.out.println("Inserted " + record[0] + " into page " + pageID + " there are " + remaining + " records left");
    }

    @Override
    public Page splitPage() {
        return null;
    }

    @Override
    public boolean hasSpace() {
        return table.getMaxRecords() > entries;
    }
}
