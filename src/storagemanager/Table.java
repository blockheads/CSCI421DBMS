package storagemanager;

import java.io.Serializable;

public class Table implements Serializable {
    private int recordSize;
    private Integer[] keyIndices;

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

}
