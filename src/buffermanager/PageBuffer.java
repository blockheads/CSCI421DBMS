package buffermanager;

import datamanager.DataManager;

import java.util.Arrays;
import java.util.HashMap;
import java.util.TreeSet;

public class PageBuffer extends HashMap<Integer, TreeSet<Page>> {

    public PageBuffer(){
        super();
    }

    /**
     * loads a page into memory
     */
    private Page loadPage(int tableId, String pageId){
        Page page = DataManager.getPage(tableId,pageId);
        // already loaded pages from this table
        if(this.containsKey(tableId)){
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
    public void insetRecord(Table table, Integer[] pageIds, Object[] record){
        // right now it just inserts at first available spot
        Page page = searchPages(table,pageIds,record);
        System.out.println("Selected page: " + page.getId());
        boolean res = page.insertRecord(table, record);
        if(res)
            System.out.println("inserted record succesfully");
        else
            System.out.println("failed to insert record, already exists.");
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

    /**
     * This manages searching over pages to find the correct page using a binary search
     */
    public Page searchPages(Table table,Integer[] pageIds,Object[] record){

        if(pageIds.length == 0){
            // in this case there is no page to even find.
            return null;
        }

        // first I guess we perform a bst over the pages already loaded into memory

        // iterative binary search
        int l = 0, r = pageIds.length - 1;

        Page lastEmptyPage = null;

        while (l <= r) {
            int m = l + (r - l) / 2;

            // retrieve page at m
            Page page = getPage(table.getId(),Integer.toString(m));

            // page is empty
            if(page.getEntries() == 0){
                // in this case, we say we want to move to the left from the empty page. Mark this page for
                // deletion potentially?
                r = m - 1;
                // we want to mark the last empty page we visited, this is so we can use it to return our result
                // as opposed to not finding any possible page for our result
                lastEmptyPage = page;
            }
            else{
                // Check if x is present at mid
                int[] bounds = page.bounds(table,record);
                // if we are contained within the bounds of the page, or the first/last entries of the page are our
                // entry, then this is most certainly our page
                if((bounds[0] == 1 && bounds[1] == -1) || bounds[0] == 0 || bounds[1] == 0)
                    return page;

                // otherwise we check if we are greater than the page
                if (bounds[1] == 1)
                    l = m + 1;

                    // Then we mus be smaller
                else
                    r = m - 1;
            }
        }

        if(lastEmptyPage != null)
            return lastEmptyPage;

        // we return a null page if there is no page here, todo: add exception
        return null;

    }

}
