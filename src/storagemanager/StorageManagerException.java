package storagemanager;

public class StorageManagerException extends Exception {

    public static final String INVALID_TYPE_EXCEPTION_FORMAT = "%s is not a valid attribute type.";

    public StorageManagerException(String errorMsg){
        super(errorMsg);
    }
}
