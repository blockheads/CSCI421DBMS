import java.util.Objects;

/**
 * Class to create and access a database.
 */

public class Database implements IDatabase{

    public static Database database = null;


    private Database() {}

    /**
     * Static function that will create/restart and return a database
     * @param dbLoc the location to start/restart the database in
     * @param pageBufferSize the size of the page buffer; max number of pages allowed in the buffer at any given time
     * @param pageSize the size of a page in bytes
     * @return an instance of an IDatabase.
     */
    public static IDatabase getConnection(String dbLoc, int pageBufferSize, int pageSize ){
        if (database != null) {
            System.err.println("You cannot create more than one connection");
            return null;
        }

        database = new Database();
        return database;
    }

    @Override
    public void executeNonQuery(String statement) {

    }

    @Override
    public Object[][] executeQuery(String query) {
        return new Object[0][];
    }

    @Override
    public void terminateDatabase() {

    }
}
