package storagemanager;

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
    private Map<Integer, TreeSet<Page>> loadedPageMap;
    private Map<Integer, Table> tableMap;

    public BufferManager(){
        loadedPageMap = new HashMap<>();
        tableMap = new HashMap<>();
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
    public void insertRecord(){

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
     *
     */
    public void deletePage(){

    }

    /**
     * take the bottom half of a page, create a new page and write the rest to that
     * have to check over our values in our page map, check our current page
     * if theres pages above our current page, update each +1
     */
    public void split(){

    }

    public void purge(){

    }

    public void clear(){

    }

    public boolean isFull(){
        if(true){

        }
    }

    public void writeOutPage(){

    }
}
