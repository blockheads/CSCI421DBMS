package dml;

import database.Database;
import ddl.DDLParserException;
import ddl.catalog.Attribute;
import ddl.catalog.Catalog;
import ddl.catalog.Table;
import dml.condition.Resolvable;
import dml.condition.Statement;
import storagemanager.StorageManagerException;
import storagemanager.buffermanager.datatypes.DataTypeException;
import storagemanager.util.StringParser;

import java.util.*;

public class DMLParser implements IDMLParser {

    private static DMLParser dmlParser;

    private static final String NOT_NULL = "Some of the values in the record is null where a non-null value is expected";
    private static final String NOT_UNIQUE = "Some of the values in the record is not unique where a unique value is expected";
    private static final String NOT_FK = "Some of the values in the record do not have corresponding foreign keys where expected";
    private static final String TABLE_DNE = "The table you are trying to query does not exist.";

    private static final String selectRegex = "[ ]*(from|where|order by)[ ]*";

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
        DMLCommands.valueOf(statement.substring(0, statement.indexOf(' ')).toUpperCase()).handle.parseDMLStatement(StringParser.toLowerCaseNonString(statement));

    }


    
    @Override
    public Object[][] parseDMLQuery(String statement) throws DMLParserException {
        String[] parts = statement.split(selectRegex, 4);
        Set<Table> tables = new HashSet<>();
        Map<String, String> attrDotTable = new HashMap<>();
        Set<String> attrNeeded = new HashSet<>();
        ArrayList<Attribute> attrOrder = new ArrayList<>();
        ArrayList<String> orderBy = null;
        Resolvable whereClause = Statement.whereTrue();

        { // get tables needed
            String[] tStrings = parts[1].split("[ ]*,[ ]*");
            for (String tname : tStrings) {
                Table table = Database.catalog.getTable(tname);
                if (table == null) throw new DMLParserException("Table not real");
                tables.add(table);
            }
        }

        if (parts[0].contains("*")) { // select all attributes
            for (Table table : tables) {
                for (Attribute attribute : table.getAttributes()) {
                    Attribute faxAttr = new Attribute(table.getTableName() + "." + attribute.getName(), attribute.getDataType());
                    attrDotTable.put(attribute.getName(), faxAttr.getName());
                    attrDotTable.put(faxAttr.getName(), faxAttr.getName());
                    attrNeeded.add(faxAttr.getName());
                    attrNeeded.add(attribute.getName());
                    attrOrder.add(faxAttr);
                }
            }
        } else { // specific columns selected
            parts[0] = parts[0].substring(parts[0].indexOf('t') + 1).trim();
            String[] aStrings = parts[0].split("[ ]*,[ ]*");
            attrOrder = new ArrayList<>(aStrings.length);
            for (String aname : aStrings) {
                Attribute attr = null;
                for (Table table : tables) {
                    attr = table.getAttribute(aname);
                    if (attr != null) {
                        Attribute faxAttr = new Attribute(table.getTableName() + "." + attr.getName(), attr.getDataType());
                        attrDotTable.put(attr.getName(), faxAttr.getName());
                        attrDotTable.put(faxAttr.getName(), faxAttr.getName());
                        attrNeeded.add(aname);
                        attrOrder.add(faxAttr);
                        break;
                    }
                }
                if (attr == null) throw new DMLParserException("Attr not real");
            }
        }

        try {
            final Table lastTable = new Table(Table.generateInternalIdentifier(), attrOrder);
            for (Table table : tables)
                lastTable.addSubname(table.getTableName());

            if (parts.length >= 3) {
                int start = 2;
                if (statement.contains("where")) {
                    whereClause = Statement.fromWhere(lastTable, parts[start]);
                    start ++;
                }
                if (statement.contains("order by")) {
                    Set<String> used = new HashSet<>();
                    orderBy = new ArrayList<>();
                    String[] sOrder = parts[start].split(" [ ]*");
                    for (String aname: sOrder) {
                        if (attrNeeded.contains(attrDotTable.get(aname))) {
                            if (!used.contains(attrDotTable.get(aname))) {
                                orderBy.add(attrDotTable.get(aname));
                                used.add(attrDotTable.get(aname));
                            }
                        } else {
                            throw new DMLParserException("Order by needs avail attr");
                        }
                    }
                    for (Attribute attribute : attrOrder) {
                        if (!used.contains(attribute.getName())) {
                            orderBy.add(attrDotTable.get(attribute.getName()));
                        }
                    }
                    String[] attr = new String[attrOrder.size()];
                    attr = orderBy.toArray(attr);
                    lastTable.setPrimaryKey(attr);
                }
            }

            if (!statement.contains("order by"))  {
                String[] attr = new String[attrOrder.size()];
                for (int i = 0; i < attrOrder.size(); i++) {
                    Attribute attribute = attrOrder.get(i);
                    attr[i] = attribute.getName();
                }
                lastTable.setPrimaryKey(attr);
            }

            Iterator<Table> tableIterator = tables.iterator();
            Set<Object[]> records;
            Table it = tableIterator.next();
            if (tables.size() >= 2) {
                for (int i = 1; i < tables.size() - 1; i++) {
                    Table generated = it.join(tableIterator.next());
                    if (it.getSubtableCount() > 0) it.dropTable();
                    it = generated;
                }
                Table generated = it.join(tableIterator.next());
                if (it.getSubtableCount() > 0) it.dropTable();

                records = whereClause.resolveAgainst(new HashSet<>(Arrays.asList(generated.getRecords())));

                generated.project(lastTable, records);
                generated.dropTable();

            } else {
                records = whereClause.resolveAgainst(new HashSet<>(Arrays.asList(it.getRecords())));
                it.project(lastTable, records);
            }

            Object[][] finalRecords = lastTable.getRecords();
            lastTable.dropTable();
            return finalRecords;

        } catch (DDLParserException | StorageManagerException e) {
            throw new DMLParserException("Error making internal table");
        }
    }

}
