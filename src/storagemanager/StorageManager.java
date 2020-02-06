package storagemanager;

import storagemanager.buffermanager.BufferManager;
import storagemanager.buffermanager.Table;
import storagemanager.buffermanager.diskUtils.DataManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class StorageManager extends AStorageManager {

    private BufferManager bufferManager;

    /**
     * Creates an instance of the database. Tries to restart, if requested, the database at the provided location.
     * 
     * You can add code to this but cannot change the types and number of parameters. Testers will be called using
     * this constructor.
     * 
     * @param dbLoc the location to start/restart the database in
     * @param pageBufferSize the size of the page buffer; max number of pages allowed in the buffer at any given time
     * @param pageSize the size of a page in bytes
     * @param restart restart the database in the location if true; start a new database otherwise
     * @throws StorageManagerException database fails to restart or start
     */
    public StorageManager(String dbLoc, int pageBufferSize, int pageSize, boolean restart) throws StorageManagerException {
        super(dbLoc, pageBufferSize, pageSize, restart);
        bufferManager = new BufferManager(dbLoc, pageBufferSize, pageSize);
    }

    @Override
    public Object[][] getRecords(int table) throws StorageManagerException {
        return new Object[0][];
    }

    @Override
    public Object[] getRecord(int table, Object[] keyValue) throws StorageManagerException {
        return new Object[0];
    }

    @Override
    public void insertRecord(int table, Object[] record) throws StorageManagerException {
        try {
            bufferManager.insertRecord(table,record);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateRecord(int table, Object[] record) throws StorageManagerException {

    }

    @Override
    public void removeRecord(int table, Object[] keyValue) throws StorageManagerException {

    }

    @Override
    public void dropTable(int table) throws StorageManagerException {

    }

    @Override
    public void clearTable(int table) throws StorageManagerException {

    }

    /**
     * clear out all files in database folder
     * use File.delete() to delete directory
     */
    public void NewDatabase(){

    }

    public void addTable(int id, String[] dataTypes, Integer[] keyIndices) throws StorageManagerException{
        if(new File(String.valueOf(id)).mkdir()){
            System.out.println("Created directory");
        }
        else{
            System.out.println("Failed to create table");
            return;
        }
        Table table = new Table(id, dataTypes, keyIndices, bufferManager.getPageSize());

        System.out.println(table);
        DataManager.saveTable(table,id);
    }

    @Override
    public void purgeBuffer() throws StorageManagerException {

    }

    @Override
    public void terminateDatabase() throws StorageManagerException {

    }

    @Override
    protected void restartDatabase(String dbLoc) throws StorageManagerException {
        // try and path to the folder, if it doesn't exist error out.
        if (!Files.exists(Paths.get(dbLoc))) {
            throw new StorageManagerException("Invalid database path, attempting to restart in a directory that does" +
                    "not exist!");
        }
        // we don't need to do anything when our database restarts
    }

    @Override
    protected void newDatabase(String dbLoc, int pageBufferSize, int pageSize) throws StorageManagerException {

    }
}
