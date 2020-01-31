package storagemanager.buffermanager.pageManager;

import storagemanager.buffermanager.BufferManager;
import storagemanager.buffermanager.Table;
import storagemanager.buffermanager.page.Page;
import storagemanager.buffermanager.page.PageTypes;
import storagemanager.buffermanager.page.RecordPage;
import storagemanager.buffermanager.diskUtils.DataManager;
import storagemanager.StorageManagerException;
import util.Subscriber;

import java.io.IOException;
import java.util.*;
import java.util.HashMap;
import java.util.TreeSet;

public class PageBuffer {

    private final HashMap<Integer, EnumMap<PageTypes, TreeSet<Page>>> pages = new HashMap<>();
    private final BufferManager bufferManager;

    private final AgedObjectPool<Page> pagePool;
    private final Subscriber<Page> removalSubscriber = new Subscriber<Page>() {
        @Override
        protected void onUpdate(Page next) {
            pages.get(next.getTableID()).get(next.getPageType()).remove(next);
            if (next.isEmpty()) destroyPage(next);
            else writeOutPage(next);
        }
    };

    public PageBuffer(BufferManager bufferManager, int maxPages) {
        this.bufferManager = bufferManager;
        pagePool = new AgedObjectPool<>(maxPages);
        pagePool.subscribe(removalSubscriber);
    }

    public void addPage(Page page) {
        bufferManager.getTable(page.getTableID());
        if (pages.containsKey(page.getTableID())) { // table already in page buffer
            pages.get(page.getTableID()).get(page.getPageType()).add(page);
            // add age tracker
            return;
        }
        // load a table into the buffer
        this.pages.put(page.getTableID(), new EnumMap<>(PageTypes.class));
        this.pages.get(page.getTableID()).put(page.getPageType(), new TreeSet<>(){{add(page);}});
        page.setPageAgeTracker(pagePool.createTrackerForPool(page));
        // add age tracker
    }

    /**
     * loads a page into memory
     */
    private Page loadPage(int tableId, int pageId){
        Page page = DataManager.getPage(tableId, String.valueOf(pageId));
        page.setPageID(pageId);
        page.setBufferManager(bufferManager);
        page.setTable(bufferManager.getTable(tableId));
        page.setPageBuffer(this);
        page.setPageAgeTracker(pagePool.createTrackerForPool(page));

        // already loaded pages from this table
        if(pages.containsKey(tableId)){
            this.pages.get(tableId).get(PageTypes.RECORD_PAGE).add(page);
        }
        else{ // have not loaded pages from this table
            EnumMap<PageTypes, TreeSet<Page>> properties = new EnumMap<>(PageTypes.class);
            TreeSet<Page> recordPages = new TreeSet<>();
            properties.put(page.getPageType(), recordPages);
            // load index here
            // actually get our page
            recordPages.add(page);
            this.pages.put(tableId, properties);
        }

        return page;
    }

    /**
     * Retrieves a page, returns from  the tree-set if it's already in memory
     * @return
     */
    public RecordPage getRecordPage(int tableId, int pageId){

        if(this.pages.containsKey(tableId)){

            // check if loaded in
            for(Page page: this.pages.get(tableId).get(PageTypes.RECORD_PAGE)){
                if(pageId == page.getPageID()){
                    return (RecordPage) page;
                }
            }

        }

        return (RecordPage) loadPage(tableId, pageId);
    }

    public void insertRecord(int table, Object[] record) throws  StorageManagerException{
        TreeSet<Integer> pagesOnDisk = DataManager.getPages(table);
        if (pagesOnDisk.isEmpty()) {
            try {
                RecordPage newRecordPage = (RecordPage) bufferManager.createPage(table);
                addPage(newRecordPage);
                pagesOnDisk.add(newRecordPage.getPageID());
            } catch (IOException e) {
                throw new StorageManagerException("");
            }
        }
        insertRecord(bufferManager.getTable(table), pagesOnDisk, record);
    }

    /**
     * Inserts a record into a page at appropriate spot
     */
    private void insertRecord(Table table, TreeSet<Integer> pageIds, Object[] record) throws StorageManagerException {
        // right now it just inserts at first available spot
        RecordPage page = (RecordPage) searchPages(table, pageIds, record);
        System.out.println("Selected page: " + page.getPageID());
        page.insertRecord(record);
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

    /**
     * This manages searching over pages to find the correct page using a binary search
     */
    public RecordPage searchPages(Table table, TreeSet<Integer> pageIds, Object[] record){

        if(pageIds.isEmpty()){
            // in this case there is no page to even find.
            return null;
        }

        // first I guess we perform a bst over the pages already loaded into memory

        // iterative binary search
        int l = 0, r = pageIds.size() - 1;

        RecordPage lastEmptyPage = null;

        while (l <= r) {
            int m = l + (r - l) / 2;

            // retrieve page at m
            RecordPage page = getRecordPage(table.getId(), m);

            // page is empty
            if(page.getEntriesCount() == 0){
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

                if ((bounds[0] == 1 && bounds[1] == -1) // bigger than first element and smaller than last element
                        || (bounds[0] == 0 || bounds[1] == 0) // equal to the first or last element
                        || (bounds[1] == 1 && pageIds.last() == m) // there is no page bigger than me, but im larger than the first element: this is my page
                        || (bounds[0] == -1 && pageIds.first() == m)) // there is no page smaller than me, but im smaller than the first element: this is my page
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

    public void destroyPage(Page page) { // delete a page from the system
        removePage(page);
    }

    public void writeOutPage(Page page) { // write out a page to disk and remove it from the buffer
        removePage(page);
    }

    private void removePage(Page page) { // remove a page without saving it to disk
        pages.get(page.getTableID()).get(page.getPageType()).remove(page);
    }

}
