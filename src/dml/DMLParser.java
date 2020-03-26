package dml;

import database.Database;
import ddl.catalog.Attribute;
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
            if (table == null) throw new DMLParserException("Table DNE");
            for (String value: values) {
                try {
                    Object[][] tableValues = table.getRecords();
                    Object[] record = table.getRecordFromString(value.substring(1, value.length() - 1));
                    System.out.println(table.checkUniqueConditions(tableValues, record));
                    table.addRecord(record);
                } catch (StorageManagerException e) {
                    e.printStackTrace();
                } catch (DataTypeException e) {
                    e.printStackTrace();
                }
            }
        }),
        DELETE(statement -> {
            String[] dml = statement.split(" [ ]*", 5);


            Table table =  Database.catalog.getTable(dml[2]);
            if (table == null) throw new DMLParserException("Table DNE");


            // then we break up and statements inside of each or statement

            // grab all the records
            try {

                // iterate over the records and check the conditional is satisfied, if so delete it.
                Object[][] records =  table.getRecords();

                for(Object[] record: records){

                    boolean statementTruth = true;

                    // evaluate our conditionals
                    for(String andStatement:  dml[4].split("and")){

                        boolean orStatementTruth = true;

                        for(String expression: andStatement.split("or")){

                            if(!parseConditional(expression, record, table)){
                                orStatementTruth = false;
                                break;
                            }

                        }

                        if(!orStatementTruth)
                            statementTruth = false;
                            break;

                    }

                    // delete record
                    if(statementTruth)
                        System.out.println("dropping record " + record);

                }

                // for each record check conditional to see if it should be deleted

            } catch (StorageManagerException e) {
                e.printStackTrace();
            }


        }),
        UPDATE(statement -> {

        });

        final DMLHandle handle;
        private DMLCommands(DMLHandle handle) {
            this.handle = handle;
        }
    }

    /**
     * Evaluates a conditional statement to see if the record should be deleted from the table
     * @return true if deleted, false otherwise
     */
    private static boolean parseConditional(String conditional, Object[] record, Table table){

        String[] dml = conditional.split(" [ ]*", 5);

        String attributeName = dml[0];
        String attributeValue = dml[2];


        switch (dml[1]){
            case "=":

                break;
            case ">":
                break;
            case "<":
                break;
            case ">=":
                break;
            case "<=":
                break;
                // error out
            default:
                break;
        }

        return true;

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
        DMLCommands.valueOf(statement.substring(0, statement.indexOf(' ')).toUpperCase()).handle.parseDMLStatement(statement.toLowerCase());
    }
    
    @Override
    public Object[][] parseDMLQuery(String statement) throws DMLParserException{
        return null;
    }

}
