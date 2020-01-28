package buffermanager;

import java.io.Serializable;

public class Table implements Serializable {
    private int recordSize = 0;
    private Integer[] keyIndices;
    // This let's us know if the pageMap corresponding to this table has been loaded into memory.
    private boolean loadedPageMap;

    // this is the list of all the pages associated with the table.
    private Integer[] pages;

    // this is the max amount of records which can be stored inside of a table
    private int maxRecords;

    public Table(int id, String[] dataTypes, Integer[] keyIndices){

        // calculates recordSize
        for(String dataType: dataTypes){
            dataType = dataType.toLowerCase();
            if(dataType.equals("integer"))
                this.recordSize += 4;
            else if(dataType.equals("double"))
                this.recordSize += 8;
            else if(dataType.equals("boolean"))
                this.recordSize += 1;
            // TODO: handle these cases....
            else if(dataType.startsWith("char")){
                break;
            }
            else if(dataType.startsWith("varchar")){
                break;
            }
            else{
                // ERROR OUT!
                System.out.println("Invalid type entered: " + dataType);
            }

        }
        // do some math here

        this.keyIndices = keyIndices;
        this.maxRecords = Math.floorDiv(4096, recordSize);
    }

    public int getRecordSize() {
        return recordSize;
    }

    public Integer[] getKeyIndices() {
        return keyIndices;
    }

    public boolean isLoadedPageMap() {
        return loadedPageMap;
    }

    public void setLoadedPageMap(boolean loadedPageMap) {
        this.loadedPageMap = loadedPageMap;
    }

}
