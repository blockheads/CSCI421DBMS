package ddl.catalog;

import java.io.Serializable;

public class TableIDGenerator implements Serializable {
    int tableID = 0;
    TableIDGenerator() {}

    public int getNewID() {
        return ++tableID;
    }
}
