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


    public boolean insertRecord(Table table, Object[] record){

        // iterative binary search
        int l = 0, r = entries - 1,m=0;
        while (l <= r) {
            m = l + (r - l) / 2;

            // retrieve record at m
            // Check if record is present at mid
            int res = compareRecord(table,record,m);

            // in this case the record already exists in the page
            if(res == 0)
                return false;

            // If record greater, ignore left half
            if (res == 1)
                l = m + 1;
                // If record is smaller, ignore right half
            else
                r = m - 1;
        }
        System.out.println("We should insert at " + m);
        // if we reach here, then element was
        // not present


        records[entries] = record;
        entries++;
        // just for nice testing output
        int remaining = records.length-entries;
        System.out.println("Inserted " + record[0] + " into page " + pageID + " there are " + remaining + " records left");
        return true;
    }

    @Override
    public Page splitPage() {
        return null;
    }

    @Override
    public boolean hasSpace() {
        return table.getMaxRecords() > entries;
    }

    /**
     * find's a record within a page
     */
    public int findRecord(Table table, Object[] record){
        // iterative binary search
        int l = 0, r = entries - 1;
        while (l <= r) {
            int m = l + (r - l) / 2;

            // retrieve record at m
            // Check if record is present at mid
            int res = compareRecord(table,record,m);
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
    /**
     * Get's the bounds of a page
     */
    public int[] bounds(Table table, Object[] record){
        return new int[]{compareRecord(table, record, 0), compareRecord(table, record, entries-1)};
    }

    /**
     * This method compares a record to another record returns
     * 1 : the record is greater than the other record
     * -1: the record is less than the other record
     * 0: the record is equal to the other record
     */
    private int compareRecord(Table table, Object[] record, int index){

        for(int i=0; i < table.getKeyIndices().length; i++){

            int keyIndex = table.getKeyIndices()[i];
            System.out.println("Key index: " + keyIndex);
            Object obj = record[keyIndex];

            int ret = 0;

            ret = table.compareDataTypes(keyIndex, obj, records[index][keyIndex]);


//            // this is a big if statement which basically just compares values depending on their underlying
//            // data type as a object, calling Java's built in compare
//            if(table.getDatatypes().get(i).getType().equals(ValidDataTypes.BOOLEAN)){
//                Boolean firstVal = (Boolean)obj;
//                Boolean secondVal = (Boolean)records[index][keyIndex];
//                ret = firstVal.compareTo(secondVal);
//            }
//            else if(table.getDatatypes().get(i).getType().equals(ValidDataTypes.CHAR)){
//                Character firstVal = (Character)obj;
//                Character secondVal = (Character)records[index][keyIndex];
//                ret = firstVal.compareTo(secondVal);
//            }
//            else if(table.getDatatypes().get(i).getType().equals(ValidDataTypes.DOUBLE)){
//                Double firstVal = (Double)obj;
//                Double secondVal = (Double)records[index][keyIndex];
//                ret = firstVal.compareTo(secondVal);
//            }
//            else if(table.getDatatypes().get(i).getType().equals(ValidDataTypes.INTEGER)){
//                Integer firstVal = (Integer)obj;
//                Integer secondVal = (Integer)records[index][keyIndex];
//                ret = firstVal.compareTo(secondVal);
//            }
//            else if(table.getDatatypes().get(i).getType().equals(ValidDataTypes.VARCHAR)){
//                String firstVal = String.valueOf(obj);
//                String secondVal = String.valueOf(records[index][keyIndex]);
//                ret = firstVal.compareTo(secondVal);
//            }

           if(ret != 0)
              return ret;
        }

        return 0;

    }
}
