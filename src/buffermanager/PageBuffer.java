package buffermanager;

import buffermanager.Datatype.ValidDataTypes;
import buffermanager.Page.AgeTracker;
import buffermanager.Page.Page;
import buffermanager.Page.PageTypes;
import datamanager.DataManager;

import java.util.*;

public class PageBuffer {

    private final HashMap<Integer, EnumMap<PageTypes, TreeSet<Page>>> pages = new HashMap<>();
    private Set<AgeTracker> ageTrackers;

    /**
     * loads a page into memory
     */
    private Page loadPage(int tableId, int pageId){
        Page page = DataManager.getPage(tableId, String.valueOf(pageId));
        // already loaded pages from this table
        if(pages.containsKey(tableId)){
            this.pages.get(tableId).get(PageTypes.RECORD_PAGE).add(page);
        }
        // have not loaded pages from this table
        else{
            EnumMap<PageTypes, TreeSet<Page>> properties = new EnumMap<PageTypes, TreeSet<Page>>(PageTypes.class);
            TreeSet<Page> pages = new TreeSet<Page>();
            properties.put(PageTypes.RECORD_PAGE, pages);
            // load index here
            // actually get our page
            pages.add(page);
            this.pages.put(tableId, properties);
        }

        return page;
    }

    /**
     * Retrieves a page, returns from  the tree-set if it's already in memory
     * @return
     */
    public Page getPage(int tableId, int pageId){

        if(this.pages.containsKey(tableId)){

            // check if loaded in
            for(Page page: this.pages.get(tableId).get(PageTypes.RECORD_PAGE)){
                if(pageId == page.getPageID()){
                    return page;
                }
            }

        }

        return loadPage(tableId, pageId);
    }

    // empties all the loaded pages out into respective tables
    public void purge(){
        for(Integer tableId: this.pages.keySet()){
            System.out.println("Purging data for table: " + tableId);
            for(Page page: this.pages.get(tableId).get(PageTypes.RECORD_PAGE)){
                System.out.println("Saving page: " + page.getPageID());
                DataManager.savePage(page, tableId);
            }
            // todo: save index pages
        }
    }


}
