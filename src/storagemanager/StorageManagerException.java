package storagemanager;

public class StorageManagerException extends Exception {

    public static final String INVALID_TYPE_EXCEPTION_FORMAT = "%s is not a valid attribute type.";
    public static final String INVALID_CHAR_BOUNDS = "the attribute %s requires a numerical boundary.";

    public StorageManagerException(String errorMsg){
        super(errorMsg);
    }
}
