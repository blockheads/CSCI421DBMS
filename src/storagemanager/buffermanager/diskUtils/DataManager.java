package storagemanager.buffermanager.diskUtils;

import storagemanager.buffermanager.page.Page;
import storagemanager.buffermanager.Table;
import storagemanager.buffermanager.page.PageTypes;

import java.io.File;
import java.io.IOException;
import java.util.TreeSet;

public abstract class DataManager {

    /**
     * The goal of this class is to keep track of things like file names
     * for both pages and tables.
     * I guess for now we can keep it simple with just some static helper methods
     */

    private static String dbmsPath = "db" + File.separator;
    private static int pageSize = 4096;
    public static final String tableObjName = "tabledata";

    public static void setDbmsPath(String dbmsPath) {
        dbmsPath = dbmsPath.replace("/", File.separator).replace("\\", File.separator);
        if (dbmsPath.lastIndexOf(File.separator) != dbmsPath.length() - 1)
            dbmsPath += File.separator;
        DataManager.dbmsPath = dbmsPath + DataManager.dbmsPath;
        new File(DataManager.dbmsPath).mkdirs();
    }

    public static void setPageSize(int pageSize) {
        DataManager.pageSize = pageSize;
    }

    public static int getPageSize() {
        return pageSize;
    }

    public static Table getTable(int table) throws IOException {
        return (Table)ObjectSaver.load(dbmsPath + table + File.separator + tableObjName);
    }

    public static Page getPage(int table, PageTypes pageTypes, int page) throws IOException {
        return (Page)ObjectSaver.load(dbmsPath + table + File.separator + pageTypes.relLoc + File.separator + page);
    }

    public static void saveTable(Table table, int tableId){
        ObjectSaver.save(table, dbmsPath + tableId + File.separator + tableObjName, true);
    }

    public static boolean createTableDirectory(int tableID) {
        return new File(dbmsPath + tableID).mkdir();
    }

    public static void savePage(Page page, int table){
        String superPath = dbmsPath + table + File.separator;
        new File(superPath + page.getPageType().relLoc).mkdir();
        ObjectSaver.save(page,superPath + page.getPageType().relLoc + File.separator + page.getPageID(), true);
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
