package ddl;

public class DDLParserException extends Exception {

    public DDLParserException(String message) {
        super(message);
    }

    public final static String INVALID_STATEMENT = "You have entered in a invalid statement not supported by the database.";
}
