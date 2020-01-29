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
        if (attribute.length == 3 && !attribute[2].equals("")) {
            throw new StorageManagerException(String.format(StorageManagerException.INVALID_TYPE_EXCEPTION_FORMAT, string.toLowerCase()));
        }
        switch (attribute[0].toUpperCase()) {
            case "INTEGER":
                return new IntegerData();
            case "DOUBLE":
                return new DoubleData();
            case "BOOLEAN":
                return new BooleanData();
            case "VARCHAR":
                try {
                    return new VarcharData(Integer.parseInt(attribute[1]));
                } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
                    throw new StorageManagerException(String.format(StorageManagerException.INVALID_CHAR_BOUNDS, attribute[0].toLowerCase()));
                }
            case "CHAR":
                try {
                    return new CharData(Integer.parseInt(attribute[1]));
                } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
                    throw new StorageManagerException(String.format(StorageManagerException.INVALID_CHAR_BOUNDS, attribute[0].toLowerCase()));
                }
        }
        throw new StorageManagerException(String.format(StorageManagerException.INVALID_TYPE_EXCEPTION_FORMAT, string.toLowerCase()));
    }

}
