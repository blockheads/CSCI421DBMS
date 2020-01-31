package storagemanager.buffermanager.page;

import storagemanager.buffermanager.BufferManager;
import storagemanager.buffermanager.pageManager.AgeTracker;
import storagemanager.buffermanager.pageManager.PageBuffer;
import storagemanager.buffermanager.Table;
import storagemanager.StorageManagerException;

import java.io.Serializable;
import java.util.Objects;

public abstract class Page<E> implements Serializable, Comparable<Page> {

    transient int pageID;
    transient Table table;
    transient BufferManager bufferManager;
    transient PageBuffer pageBuffer;
    private transient AgeTracker<Page> pageAgeTracker;

    private final PageTypes pageType;

    int entries = 0;

    public Page(int pageID, Table table, BufferManager bufferManager, PageTypes pageType) {
        this.pageID = pageID;
        this.table = table;
        this.bufferManager = bufferManager;
        this.pageType = pageType;
    }

    public void setBufferManager(BufferManager bufferManager) {
        this.bufferManager = bufferManager;
    }

    public void setTable(Table table) {
        this.table = table;
    }

    public Table getTable() {
        return table;
    }

    public void setPageID(int pageID) {
        this.pageID = pageID;
    }

    public void setPageBuffer(PageBuffer pageBuffer) {
        this.pageBuffer = pageBuffer;
    }

    public void setPageAgeTracker(AgeTracker<Page> pageAgeTracker) {
        this.pageAgeTracker = pageAgeTracker;
    }
    public void increaseAge() {
        Objects.requireNonNull(pageAgeTracker,"An age tracker is needed to increase object age");
        pageAgeTracker.ageIncrement();
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

    public abstract boolean insertRecord(E record) throws StorageManagerException;
    public abstract boolean deleteRecord(E record) throws StorageManagerException;
    public abstract boolean recordExists(E record);
    public abstract Page<E> splitPage();
    public abstract boolean hasSpace();

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
