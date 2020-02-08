package storagemanager;

import storagemanager.buffermanager.BufferManager;
import storagemanager.buffermanager.Table;
import storagemanager.buffermanager.diskUtils.DataManager;
import storagemanager.buffermanager.pageManager.PageBuffer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class StorageManager extends AStorageManager {

    public static final String TABLE_EXISTS_EXCEPTION_FORMAT = "table %s cannot be created because it already exists";
    public static final String TABLE_DNE_FORMAT = "table %s cannot be loaded because it does not exist or cannot be found";
    public static final String INVALID_TYPE_EXCEPTION_FORMAT = "%s is not a valid attribute type.";
    public static final String INVALID_CHAR_BOUNDS = "the attribute %s requires a numerical boundary.";
    public static final String INSERT_RECORD_EXISTS_FORMAT = "the record %s already exists and cannot be inserted.";
    public static final String INSERT_RECORD_INVALID_DATA = "a record contains invalid data and cannot be inserted.";
    public static final String UPDATE_RECORD_INVALID_DATA = "a record contains invalid data and cannot be updated.";
    public static final String UPDATE_RECORD_NOT_FOUND = "a record cannot be found and cannot be updated.";
    public static final String RECORD_NOT_FOUND = "the record cannot be found.";
    public static final String REMOVE_RECORD_NOT_FOUND = "a record cannot be found and cannot be removed.";
    public static final String REMOVE_RECORD_INVALID_DATA = "a record contains invalid data and cannot be  removed.";
    public static final String CANNOT_SAVE_DATA = "Data failed to be saved to disk.";
    public static final String CANNOT_MAKE_NEW_DB = "A new database cannot be created";
    public static final String CANNOT_LOAD_DATA = "Data failed to be loaded from disk.";
    public static final String CANNOT_LOAD_FILE = "File %s failed to be loaded from disk.";


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
        DataManager.setPageSize(pageSize);
        DataManager.setDbmsPath(dbLoc);
        bufferManager = new BufferManager(pageBufferSize, pageSize);
    }

    @Override
    public Object[][] getRecords(int table) throws StorageManagerException {
        return bufferManager.getAllRecords(table);
    }

    @Override
    public Object[] getRecord(int table, Object[] keyValue) throws StorageManagerException { return bufferManager.getRecord(table, keyValue);
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
        bufferManager.updateRecord(table, record);
    }

    @Override
    public void removeRecord(int table, Object[] keyValue) throws StorageManagerException {
        bufferManager.removeRecord(table,keyValue);
    }

    @Override
    public void dropTable(int table) throws StorageManagerException {

        //check to see if directory exists
        //delete directory
        try {
            String current_dir = new File(".").getCanonicalPath();
            String dir_to_delete = current_dir + String.valueOf(table);
            if(!deleteDir(new File(dir_to_delete))) throw new StorageManagerException(String.format(TABLE_DNE_FORMAT, table));
        }
        catch (IOException e){
            System.out.println("Current dir not found");
        }

    }
    //helper method to get rid of directory holding pages for DropTable
    private static boolean deleteDir(File file) {
        if (file.isDirectory()) {
            String[] children = file.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(file, children[i]));

                if (!success) {
                    return false;
                }
            }
        }
        return file.delete();
    }

    @Override
    public void clearTable(int table) throws StorageManagerException {
        bufferManager.clearTable(table);
    }

    public void addTable(int id, String[] dataTypes, Integer[] keyIndices) throws StorageManagerException{
        if(!DataManager.createTableDirectory(id))
            throw new StorageManagerException(String.format(TABLE_EXISTS_EXCEPTION_FORMAT, id));

        Table table = new Table(id, dataTypes, keyIndices, bufferManager.getPageSize());
        System.out.println(table);
        DataManager.saveTable(table,id);
    }

    @Override
    public void purgeBuffer() throws StorageManagerException {
        bufferManager.shutDown();
    }

    @Override
    public void terminateDatabase() throws StorageManagerException {
        purgeBuffer();
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
        DataManager.deleteDb(dbLoc);
    }
}
