package storagemanager.buffermanager;

import storagemanager.buffermanager.diskUtils.DataManager;
import storagemanager.StorageManagerException;
import storagemanager.buffermanager.pageManager.PageBuffer;

import java.io.IOException;
import java.util.*;


public class BufferManager {
    /**
     * manages two maps,
     * manage catalog
     * and two maps one for
     * table id -> set of pages
     * table id -> catalog of table
     *
     **/
    private PageBuffer pageBuffer;
    private final Map<Integer, Table> tableMap;
    private final int pageSize;

    public BufferManager(String dbLoc, int maxPages, int pageSize){
        DataManager.setDbmsPath(dbLoc);
        this.pageSize = pageSize;
        tableMap = new HashMap<>();
        pageBuffer = new PageBuffer(this, maxPages);
    }

    /**
     * check table map, add to table map if doesn't exist
     * need to manually search all the pages in the directory as they may not be loaded into memory
     * find correct page, insert on page
     * needs to handle splitting.
     * calls get record to check if exists
     * if id doesnt then ->
     * inserts a record through insertion sort.
     */
    public void insertRecord(int tableId, Object[] record) throws StorageManagerException, IOException {
        Table table = getTable(tableId);

        // in this case just create page and insert in empty page, it's our first entry
        pageBuffer.insertRecord(table, record);
    }

    /**
     * Check table map, find the correct page\
     * need to manually search all the pages in the directory as they may not be loaded into memory
     * 1. check if element exists
     * 2. give us element if exists
     * 3. tells us the page it is in.
     * How to find the correct page:
     *    binary-search on the pages, and go through the records of each page
     */
    public void getRecord(){

    }

    /**
     * deletes the record and moves all the records below up one
     */
    public void deleteRecord(){

    }

    /**
     * Updates a table after it has been modified in the table map
     */
    public void updateTable(Table table){
        tableMap.put(table.getId(),table);
    }

    /**
     * tihs function loads a table into memory
     */
     private Table loadTable(int id) {
        Table table = null;
        try {
            table = DataManager.getTable(id);
        } catch (IOException e) {
            e.printStackTrace();
        }
        tableMap.put(id,table);
        return table;
    }

    public Table getTable(int id) {
        if(tableMap.get(id) == null) {
            return loadTable(id);
        }
        return tableMap.get(id);
    }

    public Map<Integer, Table> getTableMap() {
        return tableMap;
    }

    /**
     *
     */
    public void deletePage(){

    }

    /**
     * take the bottom half of a page, create a new page and write the rest to that
     * have to check over our values in our page map, check our current page
     * if there's pages above our current page, update each +1
     */
    public void split(){

    }

    public void clear(){

    }

    public boolean isFull(){
        return false;
    }

    public void writeOutPage(){

    }

    public int getPageSize() {
        return pageSize;
    }

    /**
     * Functions to execute when the program is shut down
     */
    public void shutDown(){
        pageBuffer.purge();
        // need to write out our tables as well
        for(Table table: tableMap.values()){
            DataManager.saveTable(table,table.getId());
        }
    }

}
