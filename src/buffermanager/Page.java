package buffermanager;

import buffermanager.Datatype.ValidDataTypes;
import util.ObjectSaver;

import java.io.File;
import java.io.Serializable;
import java.util.Arrays;

public class Page implements Serializable,Comparable<Page> {

    /**
     * A page class, this stores our records and id ( does not need to be volatile talked to professor )
     * Oh also it makes sense for a page to be a Java file, as it should have a literal place on the computer
     * it represents upon construction.
     */

    private static final long serialVersionUID = 2L;

    private Object[][] records;
    private Integer id;
    // the amount of current records stored inside the page
    private int entries;

    public int getEntries() {
        return entries;
    }



    public Page(int id, int maxRecords, int recordSize){
        this.id = id;
        // initially just a empty array with no entries?
        System.out.println("created new page with maxRecords: " + maxRecords + " and record size: " + recordSize);
        this.records = new Object[maxRecords][recordSize];
    }

    public int getId() {
        return id;
    }

    /**
     * Checks if a page has space to write to
     * @return
     */
    public boolean hasSpace(Table table){
        return entries < table.getMaxRecords();
    }

    // this is used for sorting the pages into the tree-set please don't modify!
    // compare record is use for record comparison
    @Override
    public int compareTo(Page page) {
        return id.compareTo(page.id);
    }

    public boolean insertRecord(Table table, Object[] record){

        // iterative binary search
        int l = 0, r = entries - 1;
        while (l <= r) {
            int m = l + (r - l) / 2;

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
        System.out.println("We should insert after " + l + " and before " + r + ".");
        // if we reach here, then element was
        // not present


        records[entries] = record;
        entries++;
        // just for nice testing output
        int remaining = records.length-entries;
        System.out.println("Inserted " + record[0] + " into page " + id + " there are " + remaining + " records left");
        return true;
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
    private int compareRecord(Table table,Object[] record, int index){

        for(int i=0; i < table.getKeyIndices().length; i++){

            int keyIndex = table.getKeyIndices()[i];
            System.out.println("Key index: " + keyIndex);
            Object obj = record[keyIndex];

            int ret = 0;

            // this is a big if statement which basically just compares values depending on their underlying
            // data type as a object, calling Java's built in compare
            if(table.getDatatypes().get(i).getType().equals(ValidDataTypes.BOOLEAN)){
                Boolean firstVal = (Boolean)obj;
                Boolean secondVal = (Boolean)records[index][keyIndex];
                ret = firstVal.compareTo(secondVal);
            }
            else if(table.getDatatypes().get(i).getType().equals(ValidDataTypes.CHAR)){
                Character firstVal = (Character)obj;
                Character secondVal = (Character)records[index][keyIndex];
                ret = firstVal.compareTo(secondVal);
            }
            else if(table.getDatatypes().get(i).getType().equals(ValidDataTypes.DOUBLE)){
                Double firstVal = (Double)obj;
                Double secondVal = (Double)records[index][keyIndex];
                ret = firstVal.compareTo(secondVal);
            }
            else if(table.getDatatypes().get(i).getType().equals(ValidDataTypes.INTEGER)){
                Integer firstVal = (Integer)obj;
                Integer secondVal = (Integer)records[index][keyIndex];
                ret = firstVal.compareTo(secondVal);
            }
            else if(table.getDatatypes().get(i).getType().equals(ValidDataTypes.VARCHAR)){
                String firstVal = String.valueOf(obj);
                String secondVal = String.valueOf(records[index][keyIndex]);
                ret = firstVal.compareTo(secondVal);
            }

           if(ret != 0)
              return ret;
        }

        return 0;

    }
}
