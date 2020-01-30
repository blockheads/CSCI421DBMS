package datamanager;

import buffermanager.Page;
import buffermanager.Table;
import util.ObjectSaver;

import java.io.File;
import java.util.ArrayList;

public class DataManager {
    /**
     * The goal of this class is to keep track of things like file names
     * for both pages and tables.
     * I guess for now we can keep it simple with just some static helper methods
     */



    public static Table getTable(int table){
        return (Table)ObjectSaver.load(table + "\\tableData");
    }

    public static Page getPage(int table, String page){
        return (Page)ObjectSaver.load(table + "\\" + page);
    }

    public static void saveTable(Table table, int tableId){
        ObjectSaver.save(table,tableId + "\\tabledata");
    }

    public static void savePage(Page page, int table){
        ObjectSaver.save(page,table + "\\" + page.getId());
    }

    /**
     * Function gets all the files given a specified tableId
     * @param id: THe table id
     * @return a list of strings containing all the pages associated with a table
     */
    public static ArrayList<Integer> getPages(int id){
        File file = new File(String.valueOf(id));
        File[] pageList = file.listFiles();

        ArrayList<Integer> pageNames = new ArrayList<>();
        if(pageList != null){
            for(File pageFile: pageList){
                // TODO: we can get rid of this check if we just add a prefix or sufix
                // TODO: or some sort of identifier to a page.
                if(!pageFile.getName().equals("tabledata")){
                    try{
                        pageNames.add(Integer.parseInt( pageFile.getName() ));
                    }
                    catch (Exception e){
                        // todo: handle exception extra file which is not a integer based file name inside
                        // todo: the folder.
                    }

                }
            }
        }
        return pageNames;
    }

}
