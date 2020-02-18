package ddl;

import storagemanager.StorageManagerException;
import storagemanager.buffermanager.datatypes.Datatype;

public class Attribute {

    // An attribute has a datatype
    private final Datatype dataType;
    // and a name
    private final String name;

    // also it has constraints
    private boolean notNull = false;
    private boolean primaryKey = false;
    private boolean unique = false;

    public Attribute(String name, Datatype dataType) throws StorageManagerException {
        this.name = name;
        this.dataType = dataType;

    }

    public Datatype getDataType() {
        return dataType;
    }

    public String getName() {
        return name;
    }

    public void setNotNull(boolean notNull) {
        this.notNull = notNull;
    }

    public void setPrimaryKey(boolean primaryKey) {
        this.primaryKey = primaryKey;
    }

    public void setUnique(boolean unique) {
        this.unique = unique;
    }


    public boolean isNotNull() {
        return notNull;
    }

    public boolean isPrimaryKey() {
        return primaryKey;
    }

    public boolean isUnique() {
        return unique;
    }
}
