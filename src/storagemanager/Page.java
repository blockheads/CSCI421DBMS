package storagemanager;

import java.io.Serializable;

public class Page implements Serializable {

    private Object[][] records;
    private volatile Integer id;

}
