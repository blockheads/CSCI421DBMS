package buffermanager.Datatype;

import storagemanager.StorageManagerException;

/**
 * The valid data types for the dbms
 *
 * @author Nicholas Chieppa
 */
public enum ValidDataTypes {
    CHAR(2, char[].class),
    VARCHAR(2, char[].class),
    INTEGER(4, int.class),
    DOUBLE(8, double.class),
    BOOLEAN(1, boolean.class); // end of types

    final int sizeInBytes;
    final Class<?> objectClass;

    ValidDataTypes(int sizeInBytes, Class<?> objectClass) {
        this.sizeInBytes = sizeInBytes;
        this.objectClass = objectClass;
    }

    public static Datatype resolveType(String string) throws StorageManagerException {
        String[] attribute = string.split("([()])");
        switch (attribute[0].toUpperCase()) {
            case "INTEGER":
                return new IntegerData();
            case "DOUBLE":
                return new DoubleData();
            case "BOOLEAN":
                return new BooleanData();
            case "VARCHAR":
                return new VarcharData(Integer.parseInt(attribute[1]));
            case "CHAR":
                return new CharData(Integer.parseInt(attribute[1]));
        }
        throw new StorageManagerException(String.format(StorageManagerException.INVALID_TYPE_EXCEPTION_FORMAT, attribute[0].toLowerCase()));
    }

}
