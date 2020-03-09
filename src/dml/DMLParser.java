package dml;

import database.Database;
import ddl.catalog.Catalog;
import ddl.catalog.Table;
import storagemanager.StorageManagerException;
import storagemanager.buffermanager.datatypes.DataTypeException;

import java.util.Set;

public class DMLParser implements IDMLParser {

    private static DMLParser dmlParser;

    private enum DMLCommands {
        INSERT(statement -> {

            String[] dml = statement.split(" [ ]*", 5);
            String[] values = dml[4].split(",[ ]*");

            Table table =  Database.catalog.getTable(dml[2]);
            for (String value: values) {
                try {
                    table.addRecord(value.substring(1, value.length() - 1));
                } catch (StorageManagerException e) {
                    e.printStackTrace();
                } catch (DataTypeException e) {
                    e.printStackTrace();
                }
            }
        }),
        DELETE(statement -> {

        }),
        UPDATE(statement -> {

        });

        final DMLHandle handle;
        private DMLCommands(DMLHandle handle) {
            this.handle = handle;
        }
    }


    /**
     * This will create an instance of this parser and return it.
     * @return an instance of a IDMLParser
     */
    public static IDMLParser createParser(){
        if (dmlParser != null) {
            System.err.println("You cannot create more than one parser.");
            return dmlParser;
        }

        dmlParser = new DMLParser();
        return dmlParser;
    }

    @Override
    public void parseDMLStatement(String statement) throws DMLParserException {
        DMLCommands.valueOf(statement.substring(0, statement.indexOf(' ')).toLowerCase()).handle.parseDMLStatement(statement.toLowerCase());
    }
    
    @Override
    public Object[][] parseDMLQuery(String statement) throws DMLParserException{
        return null;
    }

}
