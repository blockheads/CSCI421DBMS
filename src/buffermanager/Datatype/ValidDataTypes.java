package buffermanager.Datatype;

import storagemanager.StorageManagerException;

import java.util.Arrays;
import java.util.Comparator;

/**
 * The valid data types for the dbms
 *
 * @author Nicholas Chieppa
 */
public enum ValidDataTypes {
    CHAR(2, char[].class, (o1, o2) -> {
        return Arrays.toString(o1).toUpperCase().compareTo(Arrays.toString(o2).toUpperCase());
    }),
    VARCHAR(2, char[].class, (o1, o2) -> {
        return Arrays.toString(o1).toUpperCase().compareTo(Arrays.toString(o2).toUpperCase());
    }),
    INTEGER(4, Integer.class, Integer::compareTo),
    DOUBLE(8, Double.class, Double::compareTo),
    BOOLEAN(1, Boolean.class, Boolean::compareTo); // end of types

    final int sizeInBytes;

    public Class<?> getObjectClass() {
        return objectClass;
    }

    final Class<?> objectClass;
    final Comparator comparator;

    <E> ValidDataTypes(int sizeInBytes, Class<E> objectClass, Comparator<E> comparator) {
        this.sizeInBytes = sizeInBytes;
        this.objectClass = objectClass;
        this.comparator = comparator;
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
