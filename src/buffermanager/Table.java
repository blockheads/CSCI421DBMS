package buffermanager;

import java.io.Serializable;

public class Table implements Serializable {
    private int recordSize;
    private Integer[] keyIndices;
    // This let's us know if the pageMap corresponding to this table has been loaded into memory.
    private boolean loadedPageMap;

    public int getRecordSize() {
        return recordSize;
    }

    public void setRecordSize(int recordSize) {
        this.recordSize = recordSize;
    }

    public Integer[] getKeyIndices() {
        return keyIndices;
    }

    public void setKeyIndices(Integer[] keyIndices) {
        this.keyIndices = keyIndices;
    }

    public boolean isLoadedPageMap() {
        return loadedPageMap;
    }

    public void setLoadedPageMap(boolean loadedPageMap) {
        this.loadedPageMap = loadedPageMap;
    }

}
