package testing;

import storagemanager.buffermanager.BufferManager;
import storagemanager.StorageManager;
import storagemanager.StorageManagerException;

public class BufferManagerTests {
    /**
     * I had some time so I thought i'd create a buffer manager testing file.
     * This is just to get some ground work on constructing pages for the database ect. Basically just to see how it
     * work...
     */

    public static void main(String... args) throws StorageManagerException {
        BufferManager bufferManager = new BufferManager();
        // also testing some storage manager stuff
        try{
            // the paramaters don't matter right now...
            StorageManager storageManager = new StorageManager("/",100,4096,false);
            testCreateTable(storageManager);
        }
        catch (Exception e){
            e.printStackTrace();
        }

        testWritePage(bufferManager);

        testShutDown(bufferManager);

    }

    /**
     * Basic method to create a table in a folder.
     */
    public static void testCreateTable(StorageManager storageManager) throws StorageManagerException {
        storageManager.addTable(0, new String[]{"Integer", "varChar(3)", "boolean", "char(3)"}, new Integer[]{0, 2, 3});
    }

    /**
     * Testing writing a record to a page.
     */
    public static void testWritePage(BufferManager bufferManager) throws StorageManagerException {
        bufferManager.insertRecord(0,new Object[]{0, new char[]{'t', 'e', 's', 't'}, true, "123".toCharArray()});
        bufferManager.insertRecord(0,new Object[]{0, "test".toCharArray(), true, "124".toCharArray()});
        bufferManager.insertRecord(0,new Object[]{2, "test".toCharArray(), false, "123".toCharArray()});
        bufferManager.insertRecord(0,new Object[]{3, "test".toCharArray(), false, "123".toCharArray()});
        bufferManager.insertRecord(0,new Object[]{0, "a".toCharArray(), false, "123".toCharArray()});
    }

    /**
     * Simple test to see how the program handles shutting down
     * @param bufferManager
     */
    public static void testShutDown(BufferManager bufferManager){
        bufferManager.shutDown();
    }
}
