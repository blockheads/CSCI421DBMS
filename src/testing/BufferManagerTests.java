package testing;

import buffermanager.BufferManager;
import storagemanager.StorageManager;
import storagemanager.StorageManagerException;

import java.io.IOException;

public class BufferManagerTests {
    /**
     * I had some time so I thought i'd create a buffer manager testing file.
     * This is just to get some ground work on constructing pages for the database ect. Basically just to see how it
     * work...
     */

    public static void main(String... args){
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

        try {
            testCreatePage(bufferManager);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Basic method to create a table in a folder.
     */
    public static void testCreateTable(StorageManager storageManager) throws StorageManagerException {
        storageManager.addTable(0, new String[]{"Integer", "varChar(3)"}, new Integer[]{0, 3});
    }

    /**
     * For now just using static methods to call into for testing... better than nothing, and I don't really care
     * to use assert, as the data is very visual right now...
     */

    public static void testCreatePage(BufferManager bufferManager) throws IOException {
        bufferManager.createPage(0);
    }
}
