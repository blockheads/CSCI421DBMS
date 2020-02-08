package storagemanager.buffermanager.page;

import storagemanager.buffermanager.BufferManager;
import storagemanager.buffermanager.diskUtils.DataManager;
import storagemanager.buffermanager.pageManager.AgeTracker;
import storagemanager.buffermanager.pageManager.PageBuffer;
import storagemanager.buffermanager.Table;
import storagemanager.StorageManagerException;

import java.io.IOException;
import java.io.Serializable;
import java.util.Objects;

public abstract class Page<E> implements Serializable, Comparable<Page> {

    final int pageID;
    transient Table table;
    transient BufferManager bufferManager;
    transient PageBuffer pageBuffer;
    private transient AgeTracker<Page> pageAgeTracker;

    private final PageTypes pageType;

    int entries = 0;

    Page(Table table, PageTypes pageType) {
        this.pageID = table.getNewHighestPage();
        this.table = table;
        this.pageType = pageType;
    }

    public static Page loadPageFromDisk(Table table, PageTypes pageType, int pageID,
                                        BufferManager bufferManager, PageBuffer pageBuffer) throws IOException {
        Page loadedPage = DataManager.getPage(table.getId(), pageType, pageID);
        loadedPage.setBufferManager(bufferManager);
        loadedPage.setTable(table);
        loadedPage.setPageBuffer(pageBuffer);
        loadedPage.setPageAgeTracker(pageBuffer.addPageToPool(loadedPage));

        return loadedPage;
    }

    public static Page createPage(Table table, PageTypes pageType,
                                  BufferManager bufferManager, PageBuffer pageBuffer) {
        Page newPage;
        if (pageType.pageClass == RecordPage.class) {
            newPage = createRecordPage(table);
        } else {
            newPage = createIndexPage(table);
        }

        newPage.setPageBuffer(pageBuffer);
        newPage.setBufferManager(bufferManager);
        newPage.bufferManager.updateTable(table);
        newPage.setPageAgeTracker(pageBuffer.addPageToPool(newPage));

        return newPage;
    }

    private static RecordPage createRecordPage(Table table) {
        return new RecordPage(table);
    }

    private static IndexPage createIndexPage(Table table) {
        return new IndexPage(table);
    }

    private void setBufferManager(BufferManager bufferManager) {
        this.bufferManager = bufferManager;
    }
    private void setTable(Table table) {
        this.table = table;
    }
    private void setPageBuffer(PageBuffer pageBuffer) {
        this.pageBuffer = pageBuffer;
    }
    private void setPageAgeTracker(AgeTracker<Page> pageAgeTracker) {
        this.pageAgeTracker = pageAgeTracker;
    }

    /**
     * Increases the pages 'age' value,
     * a pages age determines if it should be removed from the page pool
     * in accordance to LRU-DB principle the page with the lowest age is the least used
     * and should be removed when the pool is full
     */
    public void increaseAge() {
        Objects.requireNonNull(pageAgeTracker,"An age tracker is needed to increase object age");
        pageAgeTracker.ageIncrement();
    }

    public Table getTable() {
        return table;
    }
    public int getPageID() {
        return pageID;
    }
    public int getTableID() {return table.getId();}

    public int getEntriesCount() {
        return this.entries;
    }

    public boolean isEmpty() {return this.entries == 0;}

    public PageTypes getPageType() {
        return pageType;
    }

    public abstract boolean insertRecord(E record) throws StorageManagerException, IOException;
    public abstract boolean deleteRecord(E record) throws StorageManagerException;
    public abstract boolean recordExists(E record);
    public abstract Page<E> splitPage();
    public abstract boolean hasSpace();

    /**
     * Writes out a page from memory into a hard coded location on disk
     */
    public abstract void save();

    public void delete() {
        if (this instanceof RecordPage)
            table.removePage((RecordPage) this);
        DataManager.deletePage(this);
        pageBuffer.removeFromPool(this, pageAgeTracker);
    }

    @Override
    public int compareTo(Page page) {
        return pageID - page.pageID;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Page)
            return ((Page) obj).pageID == pageID && ((Page) obj).getTableID() == getTableID();
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pageID, this.getTableID());
    }
}
