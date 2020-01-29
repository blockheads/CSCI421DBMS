package buffermanager;

import buffermanager.Datatype.ValidDataTypes;
import buffermanager.Page.AgeTracker;
import buffermanager.Page.Page;
import buffermanager.Page.PageTypes;
import datamanager.DataManager;

import java.util.*;

public class PageBuffer {

    private HashMap<Integer, Dictionary<PageTypes, Page>> pages;
    private HashMap<Integer, Table> loadedTables;
    private Set<AgeTracker> ageTrackers;

    /**
     * loads a page into memory
     */
    private Page loadPage(int tableId, String pageId){
        Page page = DataManager.getPage(tableId,pageId);
        // already loaded pages from this table
        if(pages.containsKey(tableId)){
            this.get(tableId).add(page);
        }
        // have not loaded pages from this table
        else{
            TreeSet<Page> pages = new TreeSet<Page>();
            // actually get our page
            pages.add(page);
            this.put(tableId,pages);
        }

        return page;
    }

    /**
     * Retrieves a page, returns from  the tree-set if it's already in memory
     * @return
     */
    public Page getPage(int tableId, String pageId){

        if(this.containsKey(tableId)){

            // check if loaded in
            for(Page page: this.get(tableId)){
                if(pageId.equals(page.getId())){
                    return page;
                }
            }

        }

        return loadPage(tableId,pageId);
    }

    /**
     * Inserts a record into a page at appropriate spot
     */
    public void insetRecord(Page page, Object[] record){
        // right now it just inserts at first available spot
        page.insertRecord(page.getEntries(),record);
    }

    // empties all the loaded pages out into respective tables
    public void purge(){
        for(Integer tableId: this.keySet()){
            System.out.println("Purging data for table: " + tableId);
            for(Page page: this.get(tableId)){
                System.out.println("Saving page: " + page.getId());
                DataManager.savePage(page,tableId);
            }
        }
    }


}
