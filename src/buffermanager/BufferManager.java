package buffermanager;

import buffermanager.Page.Page;
import buffermanager.Page.RecordPage;
import datamanager.DataManager;
import storagemanager.StorageManagerException;

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

        ArrayList<Integer> pages = DataManager.getPages(tableId);

        // in this case just create page and insert in empty page, it's our first entry
        if(pages.isEmpty()){
            return;
        }
        pages.sort(Integer::compareTo);
        //todo: call get record here
        //getRecord()... doesn't exist continue

        // right now we are just going through the pages iteratively can be changed to binary later
        for(Integer pageId: pages){
            Page<Object[]> page = pageBuffer.getRecordPage(tableId, pageId);
            // we know our table by now
            page.insertRecord(record);
        }


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
    public void createPage(int tableId) throws IOException {

        // we have to load in our table if it isn't loaded here.
        // todo: check if this is needed
        if(!tableMap.containsKey(tableId)){
            loadTable(tableId);
        }

        Table table = tableMap.get(tableId);

        // right now were just going to add pages like this
        ArrayList<Integer> pages = DataManager.getPages(tableId);
        int newPageName = 0;

        if(!pages.isEmpty()) {
            pages.sort(Integer::compareTo);
            newPageName = pages.get(pages.size() -1 ) + 1;
        }

        Page page = new RecordPage(newPageName, table, this);

        DataManager.savePage(page,tableId);
        // then load the page into memory
//        loadPage(table);

    }


    /**
     * tihs function loads a table into memory
     */
    public Table loadTable(int id){
        Table table = DataManager.getTable(id);
        tableMap.put(id,table);
        return table;
    }

    public Table getTable(int id) {
        return tableMap.getOrDefault(id, loadTable(id));
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
