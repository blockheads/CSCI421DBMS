package testing;

import database.Database;
import database.IDatabase;
import org.w3c.dom.CDATASection;
import storagemanager.AStorageManager;

public class Custom3Tester {
    public static void main(String[] args) {
        String dbLoc = "db/";
        int pageBufferSize = 20;
        int pageSize = 4096;
        AStorageManager sm;

        IDatabase database = Database.getConnection(dbLoc, pageBufferSize, pageSize);

        // assumes table 1 in storage manager... change if needed
        int table1Id = 1;
        String createTable1 = "create table foo(" +
                "id integer unique," +
                "name varchar(40) not null," +
                "amount double unique," +
                "married boolean," +
                "primarykey( id amount )" +
                ");";
        database.executeNonQuery(createTable1);
        database.executeNonQuery("insert into foo values (1 \"foo\" 2.1 false);");
        database.terminateDatabase();

        database = Database.getConnection(dbLoc, pageBufferSize, pageSize);
        database.executeNonQuery("insert into foo values (1 \"foo\" 2.1 false);");
    }
}
