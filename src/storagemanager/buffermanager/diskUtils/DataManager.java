package storagemanager.buffermanager.diskUtils;

import ddl.catalog.Catalog;
import storagemanager.StorageManager;
import storagemanager.StorageManagerException;
import storagemanager.buffermanager.page.Page;
import storagemanager.buffermanager.Table;
import storagemanager.buffermanager.page.PageTypes;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.TreeSet;

public abstract class DataManager {

    /**
     * The goal of this class is to keep track of things like file names
     * for both pages and tables.
     * I guess for now we can keep it simple with just some static helper methods
     */

    private static final String extraPath = "db" + File.separator;
    private static String dbmsPath = "";
    private static int pageSize = 4096;
    public static final String tableObjName = "tabledata";
    public static final String catalogObjName = "catalog";

    public static void setDbmsPath(String dbmsPath) {
        resolveDBPath(dbmsPath);
        new File(DataManager.dbmsPath).mkdirs();
    }

    public static void deleteDb (String dbmsPath) throws StorageManagerException {
        resolveDBPath(dbmsPath);
        try {
            File path = new File(dbmsPath);
            if (path.exists())
                delete(new File(dbmsPath));
        } catch (SecurityException | IOException e) {
            throw new StorageManagerException(StorageManager.CANNOT_MAKE_NEW_DB);
        }
    }

    private static void delete(File f) throws IOException { // https://stackoverflow.com/questions/779519/delete-directories-recursively-in-java
        if (f.isDirectory()) {
            for (File c : f.listFiles())
                delete(c);
        }
        if (!f.delete())
            throw new FileNotFoundException("Failed to delete file: " + f);
    }

    private static void resolveDBPath(String dbmsPath) {
        dbmsPath = dbmsPath.replace("/", File.separator).replace("\\", File.separator);
        if (dbmsPath.lastIndexOf(File.separator) != dbmsPath.length() - 1)
            dbmsPath += File.separator;
        DataManager.dbmsPath = dbmsPath + DataManager.extraPath;
    }

    public static void setPageSize(int pageSize) {
        DataManager.pageSize = pageSize;
    }

    public static int getPageSize() {
        return pageSize;
    }

    public static Catalog getCatalog() throws IOException {
        return (Catalog) ObjectSaver.load(dbmsPath + catalogObjName);
    }

    public static void saveCatalog(Catalog catalog) throws IOException {
        ObjectSaver.save(catalog, dbmsPath + catalogObjName, false);
    }

    public static Table getTable(int table) throws IOException {
        return (Table)ObjectSaver.load(dbmsPath + table + File.separator + tableObjName);
    }

    public static boolean dropTable(int tableID) throws StorageManagerException {
        try {
            File path = new File(dbmsPath + tableID + File.separator);
            if (path.exists())
                delete(path);
            else return false;
        } catch (SecurityException | IOException e) {
            throw new StorageManagerException(String.format(StorageManager.TABLE_DNE_FORMAT, tableID));
        }
        return true;
    }

    public static Page getPage(int table, PageTypes pageTypes, int page) throws IOException {
        return (Page)ObjectSaver.load(dbmsPath + table + File.separator + pageTypes.relLoc + File.separator + page);
    }

    public static void saveTable(Table table, int tableId) throws StorageManagerException {
        try {
            ObjectSaver.save(table, dbmsPath + tableId + File.separator + tableObjName, true);
        } catch (IOException e) {
            throw new StorageManagerException(StorageManager.CANNOT_SAVE_DATA);
        }
    }

    public static boolean createTableDirectory(int tableID) {
        return new File(dbmsPath + tableID).mkdir();
    }

    public static void savePage(Page page, int table) throws StorageManagerException {
        String superPath = dbmsPath + table + File.separator;
        new File(superPath + page.getPageType().relLoc).mkdir();
        try {
            ObjectSaver.save(page,superPath + page.getPageType().relLoc + File.separator + page.getPageID(), true);
        } catch (IOException e) {
            throw new StorageManagerException(StorageManager.CANNOT_SAVE_DATA);
        }
    }

    public static void incrementPage(int table, PageTypes pageType, int page) {
        File file = new File(dbmsPath + table + File.separator + pageType.relLoc + File.separator + page);
        file.renameTo(new File(dbmsPath + table + File.separator + pageType.relLoc + File.separator + (page + 1)));
    }

    public static boolean deletePage(Page page) {
        return deletePage(page.getTableID(), page.getPageID(), page.getPageType());
    }

    public static boolean deletePage(int tableID, int pageID, PageTypes pageType) {
        return new File(dbmsPath + tableID + File.separator + pageType.relLoc + File.separator +  pageID).delete();
    }

    /**
     * Function gets all the files given a specified tableId
     * @param id: THe table id
     * @return a list of strings containing all the pages associated with a table
     */
    public static TreeSet<Integer> getPages(int id){
        File file = new File(dbmsPath + String.valueOf(id) + File.separator + PageTypes.RECORD_PAGE.relLoc);
        File[] pageList = file.listFiles();

        TreeSet<Integer> pageNames = new TreeSet<>();
        if(pageList != null){
            for(File pageFile: pageList){
                // TODO: we can get rid of this check if we just add a prefix or sufix
                // TODO: or some sort of identifier to a page.
                    try{
                    pageNames.add(Integer.parseInt( pageFile.getName() ));
                }
                catch (Exception e){
                    // todo: handle exception extra file which is not a integer based file name inside
                    // todo: the folder.
                }

            }
        }
        return pageNames;
    }
}
