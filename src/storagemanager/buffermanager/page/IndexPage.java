package storagemanager.buffermanager.page;

import storagemanager.buffermanager.BufferManager;
import storagemanager.buffermanager.Table;
import storagemanager.StorageManagerException;

public class IndexPage extends Page<Object[]> {
    public IndexPage(int pageID, Table table, BufferManager bufferManager) {
        super(pageID, table, bufferManager, PageTypes.INDEX_PAGE);
    }

    @Override
    public boolean insertRecord(Object[] record) throws StorageManagerException {
        return false;
    }

    @Override
    public boolean deleteRecord(Object[] record) throws StorageManagerException {
        return false;
    }

    @Override
    public boolean recordExists(Object[] record) {
        return false;
    }


    @Override
    public Page<Object[]> splitPage() {
        return null;
    }

    @Override
    public boolean hasSpace() {
        return false;
    }

    @Override
    public void save() {

    }
}
