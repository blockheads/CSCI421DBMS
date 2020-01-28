package buffermanager;

import java.io.File;
import java.io.Serializable;

public class Page implements Serializable {
    /**
     * A page class, this stores our records and id ( does not need to be volatile talked to professor )
     * Oh also it makes sense for a page to be a Java file, as it should have a literal place on the computer
     * it represents upon construction.
     */

    private Object[][] records;
    private String id;
    private Page previous;
    private Page next;

}
