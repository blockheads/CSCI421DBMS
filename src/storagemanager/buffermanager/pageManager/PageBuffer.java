package storagemanager.buffermanager.pageManager;

import storagemanager.StorageManager;
import storagemanager.buffermanager.BufferManager;
import storagemanager.buffermanager.Table;
import storagemanager.buffermanager.page.Page;
import storagemanager.buffermanager.page.PageTypes;
import storagemanager.buffermanager.page.RecordPage;
import storagemanager.StorageManagerException;
import util.Subscriber;

import java.io.FileNotFoundException;
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

    public AgeTracker<Page> addPageToPool(Page page) {
        if (pages.containsKey(page.getTableID())) { // table already in page buffer
            pages.get(page.getTableID()).get(page.getPageType()).add(page);
            return pagePool.createTrackerForPool(page);
        }
        // load a table into the buffer
        this.pages.put(page.getTableID(), new EnumMap<>(PageTypes.class));
        this.pages.get(page.getTableID()).put(page.getPageType(), new TreeSet<>(){{add(page);}});
        return pagePool.createTrackerForPool(page);
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

        try {
            return (RecordPage) Page.loadPageFromDisk(bufferManager.getTable(tableId), PageTypes.RECORD_PAGE, pageId, bufferManager, this);
        } catch (FileNotFoundException e) {
            return (RecordPage) Page.createPage(bufferManager.getTable(tableId), PageTypes.RECORD_PAGE, bufferManager, this);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        return null;
    }

    public void insertRecord(Table table, Object[] record) throws  StorageManagerException, IOException{

        // in this case no pages have been created or loaded into memory.
        if(table.getHighestPage() == -1){
            Page.createPage(table, PageTypes.RECORD_PAGE, bufferManager, this);
        }

        insertRecord(table, table.getPages(), record);
    }

    /**
     * Inserts a record into a page at appropriate spot
     */
    private void insertRecord(Table table, TreeSet<Integer> pageIds, Object[] record) throws StorageManagerException, IOException {
        if(!table.validRecord(record)) {
            throw new StorageManagerException(StorageManager.INSERT_RECORD_INVALID_DATA);
        }
        RecordPage page = searchPages(table, pageIds, record);
        System.out.println("Selected page: " + page.getPageID());
        page.insertRecord(record);
    }

    // empties all the loaded pages out into respective tables
    public void purge(){
        for (Page page: pagePool.getObjects())
            page.save();
        pagePool.reset();
        pages.clear();
    }

    /**
     * This manages searching over pages to find the correct page using a binary search
     */
    public RecordPage searchPages(Table table, TreeSet<Integer> pageIds, Object[] record){

        if(pageIds.isEmpty()){
            // in this case there is no page to even find.
            return null;
        }

        // this is the iterative approach to searching for a page
        RecordPage smallestAvaliblePage = null;

        // this code no longer is usefull as our pages are not guaranteed in order
        for(int pageId: pageIds){
            // grab our record page.
            RecordPage page = getRecordPage(table.getId(), pageId);

            if (page.getEntriesCount() == 0) {
                return page;
            } else {
                // Check if x is present at mid
                int[] bounds = page.bounds(table, record);
                System.out.println("Bounds for page " + pageId + ": (" + bounds[0] + "," + bounds[1] + ")");
                // if we are contained within the bounds of the page, or the first/last entries of the page are our
                // entry, then this is most certainly our page

                if ((bounds[0] == 1 && bounds[1] == -1) // bigger than first element and smaller than last element
                        || (bounds[0] == 0 || bounds[1] == 0) // equal to the first or last element
                        || (bounds[1] == 1 && pageIds.last() == pageId) // there is no page bigger than me, but im larger than the first element: this is my page
                        || (bounds[0] == -1 && pageIds.first() == pageId)) // there is no page smaller than me, but im smaller than the first element: this is my page
                    return page;
                else if ((bounds[0] == bounds[1]) && bounds[0] == 1) {
                    smallestAvaliblePage = page;
                }


                // otherwise we just continue to iterate...
            }

        }

        if(smallestAvaliblePage != null)
            return smallestAvaliblePage;
        return null;

    }


    public void destroyPage(Page page) { // delete a page from the system
        removePage(page);
    }

    public void writeOutPage(Page page) { // write out a page to disk and remove it from the buffer
        // writing out a page to disk
        page.save();
        removePage(page);
    }

    private void removePage(Page page) { // remove a page without saving it to disk
        pages.get(page.getTableID()).get(page.getPageType()).remove(page);
    }

}
