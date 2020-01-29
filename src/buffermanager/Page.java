package buffermanager;

import util.ObjectSaver;

import java.io.File;
import java.io.Serializable;

public class Page implements Serializable,Comparable<Page> {

    /**
     * A page class, this stores our records and id ( does not need to be volatile talked to professor )
     * Oh also it makes sense for a page to be a Java file, as it should have a literal place on the computer
     * it represents upon construction.
     */

    private static final long serialVersionUID = 2L;

    private Object[][] records;
    private String id;

    public int getEntries() {
        return entries;
    }

    // the amount of current records stored inside the page
    private int entries;

    public Page(String id, int maxRecords, int recordSize){
        this.id = id;
        // initially just a empty array with no entries?
        System.out.println("created new page with maxRecords: " + maxRecords + " and record size: " + recordSize);
        this.records = new Object[maxRecords][recordSize];
    }

    public String getId() {
        return id;
    }

    /**
     * Checks if a page has space to write to
     * @return
     */
    public boolean hasSpace(Table table){
        return entries < table.getMaxRecords();
    }

    @Override
    public int compareTo(Page page) {
        return id.compareTo(page.id);
    }

    public void insertRecord(int index, Object[] record){
        records[index] = record;
        entries++;
        // just for nice testing output
        int remaining = records.length-entries;
        System.out.println("Inserted " + record[0] + " into page " + id + " there are " + remaining + " records left");
    }
}
