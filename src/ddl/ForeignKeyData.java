package ddl;

import java.io.Serializable;

// class for storing parsed foreign key data
public class ForeignKeyData implements Serializable {

    public String getReferenceTable() {
        return referenceTable;
    }

    public String[] getReferences() {
        return references;
    }

    public String[] getAttributes() {
        return attributes;
    }

    // <rname>
    private String referenceTable;
    // (<r1>... <rN>):
    private String[] references;
    // (<a1>...<aN>)
    private String[] attributes;

    public ForeignKeyData(String referenceTable, String[] references, String[] attributes){
        this.referenceTable = referenceTable;
        this.references = references;
        this.attributes = attributes;
    }

}
