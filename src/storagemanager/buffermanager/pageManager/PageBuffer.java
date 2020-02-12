package storagemanager.buffermanager.pageManager;

import storagemanager.StorageManager;
import storagemanager.buffermanager.BufferManager;
import storagemanager.buffermanager.Table;
import storagemanager.buffermanager.page.Page;
import storagemanager.buffermanager.page.PageTypes;
import storagemanager.buffermanager.page.RecordPage;
import storagemanager.StorageManagerException;
import storagemanager.util.Subscriber;

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
    public RecordPage getRecordPage(int tableId, int pageId) throws  StorageManagerException{
        Page page = isPageLoaded(tableId, PageTypes.RECORD_PAGE, pageId);
        if (page != null) {
            page.increaseAge();
            return (RecordPage) page;
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

    public Page isPageLoaded(int tableId, PageTypes pageType, int pageId) {
        if(this.pages.containsKey(tableId)){
            // check if loaded in
            for(Page page: this.pages.get(tableId).get(pageType)){
                if(pageId == page.getPageID()){
                    return (RecordPage) page;
                }
            }
        }
        return null;
    }

    public void updateRecord(Table table, Object[] record) throws StorageManagerException{

        if(!table.validRecord(record)) {
            throw new StorageManagerException(StorageManager.UPDATE_RECORD_INVALID_DATA);
        }
        RecordPage page = searchPages(table, record);
        page.updateRecord(record);
    }

    public void removeRecord(Table table, Object[] keyValue) throws StorageManagerException{
        RecordPage page = searchPages(table, keyValue);
        page.deleteRecord(keyValue);
    }

    public void insertRecord(Table table, Object[] record) throws  StorageManagerException, IOException {

        // in this case no pages have been created or loaded into memory.
        if (table.getHighestPage() == -1) {
            Page.createPage(table, PageTypes.RECORD_PAGE, bufferManager, this);
        }

        RecordPage page = searchPages(table, record);
        page.insertRecord(record);
    }

    // empties all the loaded pages out into respective tables
    public void purge() throws StorageManagerException {
        for (Page page: pagePool.getObjects())
            page.save();
        pagePool.reset();
        pages.clear();
    }

    public RecordPage searchPages(Table table, Object[] record) throws StorageManagerException{
        TreeSet<Integer> pageIds = table.getPages();
        if(pageIds.isEmpty()){
            // in this case there is no page to even find.
            return null;
        }

        // this code no longer is usefull as our pages are not guaranteed in order
        for(int pageId: pageIds){
            // grab our record page.
            RecordPage page = getRecordPage(table.getId(), pageId);

            if (page.getEntriesCount() == 0) {
                return page;
            } else {
                // Check if x is present at mid
                int[] bounds = page.bounds(record);
                // if we are contained within the bounds of the page, or the first/last entries of the page are our
                // entry, then this is most certainly our page

                if ((bounds[0] == 1 && bounds[1] == -1) // bigger than first element and smaller than last element
                        || (bounds[0] == 0 || bounds[1] == 0) // equal to the first or last element
                        || (bounds[1] == 1 && pageIds.last() == pageId) // there is no page bigger than me, but im larger than the first element: this is my page
                        || (bounds[0] == -1 && bounds[1] == -1)) // there is no page smaller than me, but im smaller than the first element: this is my page
                    return page;

                // otherwise we just continue to iterate...
            }

        }
        return null;
    }

    public void emptyTablePool(Table table) {
        for (Page page: pages.get(table.getId()).get(PageTypes.RECORD_PAGE)) {
            destroyPage(page);
        }
    }

    public void destroyPage(Page page) { // delete a page from the system
        removePage(page);
        page.delete();
    }

    public void writeOutPage(Page page) { // write out a page to disk and remove it from the buffer
        // writing out a page to disk
        try {
            page.save();
        } catch (StorageManagerException e) {
            e.printStackTrace();
        }
        removePage(page);
    }

    public void removeFromPool(Page page, AgeTracker<Page> pageAgeTracker) {
        pagePool.remove(pageAgeTracker);
        removePage(page);
    }

    private void removePage(Page page) { // remove a page without saving it to disk
        pages.get(page.getTableID()).get(page.getPageType()).remove(page);
    }

}
