package buffermanager;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
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
    // this is a unloaded page map, hence it stores all the data for all the pages unloaded and loaded into memory.
    private Map<Integer, Integer[]> pageMap;

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
     * This creates a 4k page which will be stored in our pageMap
     * and loaded pageMap for now TODO: should this be stored in loaded page map
     *
     */
    public void createPage(final String filename, int table) throws IOException {

        Page page = new Page(filename);
        boolean newFile = page.createNewFile();

        if (newFile) {
            System.out.println("New file created successfully");

            RandomAccessFile raf = new RandomAccessFile(page, "rw");
            // limited to 4k file size
            raf.setLength(4096);
            raf.close();

            System.out.println("File bytes set.");

            // then load the page into memory
            loadPage(table);
        }
        else{
            System.out.println("New file created un-successfully");
            //...?
        }

    }

    /**
     * Helper function to load a page into memory
     */
    public void loadPage(int table){

        // first check if our table is loaded into memory

    }

    /**
     * Helper function to load all the pages a table has into memory.
     */
    public void loadTablePages(int table){

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

    public void purge(){

    }

    public void clear(){

    }

    public boolean isFull(){
        return false;
    }

    public void writeOutPage(){

    }

}
