package dml;

import database.Database;
import ddl.catalog.Attribute;
import ddl.catalog.Catalog;
import ddl.catalog.Table;
import dml.condition.Statement;
import storagemanager.StorageManagerException;
import storagemanager.buffermanager.datatypes.DataTypeException;

import java.util.*;

public class DMLParser implements IDMLParser {

    private static DMLParser dmlParser;

    private static final String NOT_NULL = "Some of the values in the record is null where a non-null value is expected";
    private static final String NOT_UNIQUE = "Some of the values in the record is not unique where a unique value is expected";
    private static final String NOT_FK = "Some of the values in the record do not have corresponding foreign keys where expected";
    private static final String TABLE_DNE = "The table you are trying to query does not exist.";

    private enum DMLCommands {
        INSERT(statement -> {

            String[] dml = statement.split(" [ ]*", 5);
            String[] values = dml[4].split(",[ ]*");

            Table table =  Database.catalog.getTable(dml[2]);
            if (table == null) throw new DMLParserException(TABLE_DNE);

            boolean generateRefTable = true;
            for (String value: values) {
                try {
                    Object[][] tableValues = table.getRecords();
                    Object[] record = table.getRecordFromString(value.substring(1, value.length() - 1));
                    if (!table.checkNotNullConditions(record)) throw new DMLParserException(NOT_NULL);
                    if (!table.checkUniqueConditions(tableValues, Collections.singleton(record))) throw new DMLParserException(NOT_UNIQUE);
                    if (!table.checkForeignKeyConditions(record, generateRefTable)) throw new DMLParserException(NOT_FK);
                    generateRefTable = false;
                    table.addRecord(record);
                } catch (StorageManagerException | DataTypeException e) {
                    throw new DMLParserException(e.getLocalizedMessage());
                }
            }
        }),
        DELETE(statement -> {
            String[] dml = statement.split(" [ ]*", 5);


            Table table =  Database.catalog.getTable(dml[2]);
            if (table == null) throw new DMLParserException("Table DNE");

            try {
                Object[][] tableData = table.getRecords();
                Statement whereExp = Statement.fromWhere(table, dml[4]);
                for (Object[] row : whereExp.resolveAgainst(new HashSet<>(Arrays.asList(tableData)))) {
                    table.deleteRecord(row);
                }

            } catch (StorageManagerException e) {
                throw new DMLParserException(e.getLocalizedMessage());
            }


        }),
        UPDATE(statement -> {

            String[] dml = statement.split(" [ ]*", 4);
            Table table = Database.catalog.getTable(dml[1]);
            if (table == null) throw new DMLParserException("Table DNE");

            String[] values = dml[3].split("[ ]*where[ ]*", 2);
            UpdateFilter updateFilter = new UpdateFilter(table, values[0].trim());
            Statement whereExp = Statement.fromWhere(table, values[1].trim());
            try {
                Object[][] tableData = table.getRecords();
                Set<Object[]> rows = whereExp.resolveAgainst(new HashSet<>(Arrays.asList(tableData)));
                Set<Object[]> updatedData = new HashSet<>();
                for (Object[] tuple : rows) {
                    updatedData.add(updateFilter.performUpdate(tuple));
                }

                // dont need to check nulls because you cant update a value to a null

                if (!table.checkUniqueConditions(tableData, updatedData)) throw new DMLParserException(NOT_UNIQUE);
                if (!table.checkForeignKeyConditions(updatedData)) throw new DMLParserException(NOT_FK);
                table.updateRecord(updatedData);
            } catch (StorageManagerException e) {
                throw new DMLParserException(e.getLocalizedMessage());
            }
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
        DMLCommands.valueOf(statement.substring(0, statement.indexOf(' ')).toUpperCase()).handle.parseDMLStatement(statement.toLowerCase());
    }
    
    @Override
    public Object[][] parseDMLQuery(String statement) throws DMLParserException{
        return null;
    }

}
