package buffermanager.Datatype;

import storagemanager.StorageManagerException;

/**
 * The valid data types for the dbms
 *
 * @author Nicholas Chieppa
 */
public enum ValidDataTypes {
    CHAR(2, Character[].class),
    VARCHAR(2, Character[].class),
    INTEGER(4, Integer.class),
    DOUBLE(8, Double.class),
    BOOLEAN(1, Boolean.class); // end of types

    final int sizeInBytes;

    public Class<?> getObjectClass() {
        return objectClass;
    }

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
