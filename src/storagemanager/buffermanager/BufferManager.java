package storagemanager.buffermanager;

import storagemanager.buffermanager.page.Page;
import storagemanager.buffermanager.page.RecordPage;
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
    private Map<Integer, Table> tableMap;

    public BufferManager(){
        tableMap = new HashMap<>();
        pageBuffer = new PageBuffer(this);
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
    public void insertRecord(int tableId, Object[] record) throws StorageManagerException {
        if(!tableMap.containsKey(tableId)){
            loadTable(tableId);
        }
        // in this case just create page and insert in empty page, it's our first entry
        pageBuffer.insertRecord(tableId, record);
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
     * This creates a 4k page which will be stored in our pageMap
     * and loaded pageMap for now TODO: should this be stored in loaded page map
     *
     */
    public Page createPage(int tableId) throws IOException {

        // we have to load in our table if it isn't loaded here.
        // todo: check if this is needed
        if(!tableMap.containsKey(tableId)){
            loadTable(tableId);
        }

        Table table = tableMap.get(tableId);

        // right now were just going to add pages like this
        TreeSet<Integer> pages = DataManager.getPages(tableId);
        int newPageName = 0;

        if(!pages.isEmpty()) {
            newPageName = pages.last() + 1;
        }

        Page page = new RecordPage(newPageName, table, this);

        DataManager.savePage(page,tableId);
        return page;
    }


    /**
     * tihs function loads a table into memory
     */
    public Table loadTable(int id) {
        Table table = DataManager.getTable(id);
        tableMap.put(id,table);
        return table;
    }

    public Table getTable(int id) {
        return tableMap.getOrDefault(id, loadTable(id));
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

    /**
     * Functions to execute when the program is shut down
     */
    public void shutDown(){
        pageBuffer.purge();
    }

}
