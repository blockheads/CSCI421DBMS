package storagemanager;

public class StorageManagerException extends Exception {

    public static final String INVALID_TYPE_EXCEPTION_FORMAT = "%s is not a valid attribute type.";
    public static final String INVALID_CHAR_BOUNDS = "the attribute %s requires a numerical boundary.";
    public static final String INSERT_RECORD_EXISTS_FORMAT = "the record %s already exists and cannot be inserted.";

    public StorageManagerException(String errorMsg){
        super(errorMsg);
    }
}
