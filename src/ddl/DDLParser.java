package ddl;

import database.Database;
import ddl.catalog.*;
import storagemanager.StorageManagerException;
import storagemanager.buffermanager.datatypes.DataTypeException;
import storagemanager.buffermanager.datatypes.Datatype;
import storagemanager.buffermanager.datatypes.ValidDataTypes;
import storagemanager.buffermanager.datatypes.VarcharData;

import java.util.ArrayList;

/**
 * This a a parser for DDL statements
 */

public class DDLParser implements IDDLParser {

    public static final String CANNOT_LOAD_CATALOG = "Could not load catalog.";
    public static final String CANNOT_SAVE_CATALOG = "Could not load catalog.";

    private final String createTableStatement = "create table";
    private final String alterTableStatement = "alter table";
    private final String dropTableStatement = "drop table";
    // parsing constants
    private final String CREATE_TABLE_STATMENT = "create table";
    private final String ALTER_TABLE_STATEMENT = "alter table";
    private final String DROP_TABLE_STATEMENT = "drop table";
    private final String PRIMARY_KEY_STR = "primarykey";
    private final String UNIQUE_STR = "unique";
    private final String FOREIGN_KEY_STR = "foreignkey";
    private final String REFERENCES_STR = "references";
    // more keywords
    private final String ADD_STR = "add";
    private final String DROP_STR = "drop";
    private final String DEFAULT_STR = "default";

    // parsing errors
    private final static String INVALID_STATEMENT = "A invalid statement has been entered not supported by the database. " +
            "\n%s";
    private final static String STATEMENT_MISSING_SEMICOLON = "A statement is missing a semicolon. \n%s";

    private final static String INVALID_DATATYPE = "A statement is attempting to specify a Datatype not supported by the " +
            "database %s";

    private final static String CREATE_TABLE_MISSING_PAREN = "A create table statement is missing a parenthesis. \n%s";
    private final static String CREATE_TABLE_MISSING_REFERENCES = "A create table statement is missing references keyword" +
            "on foreign key definition.\n%s";
    private final static String CREATE_TABLE_MULT_PKS = "A create table statement is attempting to create multiple " +
            "primary keys \n%s";
    private final static String CREATE_TABLE_INVALID_ATTRIBUTE_LEN = "A create table statement is attempting to define " +
            "a attribute without a type, or a name.\n%s";
    private final static String CREATE_TABLE_INVALID_ATTRIBUTE_CON = "A create table statement is attempting to define " +
            "a invalid attribute constraint. %s";
    private final static String CREATE_TABLE_CONSTRAINT_DEF = "A create table statement is attempting to define " +
            "a constraint which has already been defined. %s";
    private final static String CREATE_TABLE_ALREADY_EXISTS = "A table with the name %s already exists.";
    private final static String DROP_TABLE_EMPTY_NAME = "A drop table statement does not specify a table name. \n%s";
    private final static String DROP_TABLE_DNE = "The table %s does not exist and cannot be dropped.";
    private final static String ALTER_TABLE_NO_ADD_DROP = "A alter table statement does not specify either to " +
            "add or drop from a table.\n%s";

    private final static String ALTER_TABLE_DROP_NO_ATR = "A alter table statement does not specify a attribute to drop." +
            "\n%s";
    private final static String ALTER_TABLE_INVALID_ATTRIBUTE_LEN = "A alter table statement is attempting to alter " +
            "a attribute without a invalid amount of arguments. \n%s";
    private final static String ALTER_TABLE_INVALID_ATTRIBUTE_DEFAULT = "A alter table statement is attempting to alter " +
            "a attribute with a invalid default value. %s";
    private final static String ALTER_TABLE_DROP_INVALID_LEN = "A alter table statement is attempting to drop a attribute" +
            "with a invalid name.\n%s";


    public static DDLParser ddlParser = null;

    private DDLParser() {}

    /**
     * This will create an instance of this parser and return it.
     * @return an instance of a IDDLParser
     */
    public static IDDLParser createParser() {
        if (ddlParser != null) {
            System.err.println("You cannot create more than one parser.");
            return null;
        }

        ddlParser = new DDLParser();
        return ddlParser;
    }

    @Override
    public void parseDDLstatement(String statement) throws DDLParserException {

        // convert to lowercase
        statement = statement.toLowerCase();
        // strip leading white space
        statement = statement.stripLeading();


        //todo: each statement needs to check it ends with a semicolon
        // now search for the end of our statement
        int iend = statement.indexOf(";");

        if(iend == -1){
            throw new DDLParserException(String.format(STATEMENT_MISSING_SEMICOLON, statement));
        }

        try {
            // we check if we begin with a valid statement
            if (statement.startsWith(CREATE_TABLE_STATMENT)) {

                String args = statement.substring(CREATE_TABLE_STATMENT.length(), iend).trim();
                parseCreateTableStatement(statement, args);
            } else if (statement.startsWith(ALTER_TABLE_STATEMENT)) {
                String args = statement.substring(ALTER_TABLE_STATEMENT.length(), iend).trim();
                parseAlterTableStatement(statement, args);
            } else if (statement.startsWith(DROP_TABLE_STATEMENT)) {
                String args = statement.substring(DROP_TABLE_STATEMENT.length(), iend).trim();
                parseDropTableStatement(statement, args, iend);
            } else {
                throw new DDLParserException(String.format(INVALID_STATEMENT, statement));
            }
        } catch (StorageManagerException e) {
            System.err.println(e.getLocalizedMessage());
        }

    }

    /**
     * Parses a create table statement, constructs the following data
     *
     * String[] primaryKeyData = null;
     *  null by default, this contains a list of the attributeNames that define the primary key
     *
     * ArrayList<ForeignKeyData> foreignKeysData = new ArrayList<>();
     *  This arraylist contains all the ForeignKeyData objects ( see class ) storing their information
     *
     * ArrayList<String[]> uniqueKeysData = new ArrayList<>();
     *  This stores each definition of unique on an attribute, with the attributeNames stored inside a String[]
     *
     * ArrayList<Attribute> attributes = new ArrayList<>();
     *  This stores all the attributes (see class), holding their type, name, and constraints
     *
     * @param args
     * @throws DDLParserException
     * @throws StorageManagerException
     */
    private void parseCreateTableStatement(String statement, String args) throws DDLParserException, StorageManagerException {
        // strip any leading whitespace before the name
        args = args.stripLeading();

        /**
         * Parses for a table name given the arguments
         * searches for occurence of ( returns all the string data before
         * if can't find throws a DDL Parser Exception
         * @param args
         */
        int ibeg = args.indexOf("(");

        if (ibeg == -1)
            throw new DDLParserException(String.format(CREATE_TABLE_MISSING_PAREN, statement));

        String tableName =  args.substring(0 , ibeg); //this will give the name

        if (Database.catalog.getTable(tableName) != null)
            throw new DDLParserException(String.format(CREATE_TABLE_ALREADY_EXISTS, tableName));

        int iend = args.lastIndexOf(")");

        if (iend == -1)
            throw new DDLParserException(String.format(CREATE_TABLE_MISSING_PAREN, statement));

        String innerStatements = args.substring(ibeg + 1,iend);

        // we can only have one primary key
        String[] primaryKeyData = new String[0];
        // we can have multiple foreign keys or unique keys
        ArrayList<ForeignKeyData> foreignKeysData = new ArrayList<>();
        ArrayList<String[]> uniqueKeysData = new ArrayList<>();
        ArrayList<Attribute> attributes = new ArrayList<>();

        // counter for primary keys, obviously we can only have one
        int primaryKeyCount = 0;

        // then we split our inner statements by ,
        for(String innerStatement: innerStatements.split(",")){

            // check we have a valid amount of parenthesis
            int leftParenCount = innerStatement.length() - innerStatement.replaceAll("[(]","").length();
            int rightParenCount = innerStatement.length() - innerStatement.replaceAll("[)]","").length();

            if(leftParenCount != rightParenCount){
                throw new DDLParserException(String.format( CREATE_TABLE_MISSING_PAREN, innerStatement));
            }

            innerStatement = innerStatement.stripLeading();

            if(innerStatement.startsWith(PRIMARY_KEY_STR)){

                // tsk, tsk tsk...
                if(primaryKeyCount > 0){
                    throw new DDLParserException(String.format(CREATE_TABLE_MULT_PKS, innerStatement));
                }

                String primaryKeys = parseParentheses(statement, innerStatement);
                primaryKeyData = primaryKeys.split("\\s+");

                primaryKeyCount++;

            }
            else if(innerStatement.startsWith(FOREIGN_KEY_STR)){

                int refIndx = innerStatement.indexOf(REFERENCES_STR);

                if (refIndx == -1)
                    throw new DDLParserException( String.format(CREATE_TABLE_MISSING_REFERENCES, innerStatement));

                // similar to .split()
                String foreignKeyStr = innerStatement.substring(0,refIndx);
                String refStr = innerStatement.substring(refIndx+REFERENCES_STR.length());

                // retrieve our foreign keys
                String foreignKeys = parseParentheses(statement, foreignKeyStr);
                String[] foreignKeyArr = foreignKeys.split("\\s+");

                // then get our reference
                String references = parseParentheses(statement, refStr);
                String[] foreignKeyRefArr = references.split("\\s+");

                // and retrieve the table we are referencing
                int pIndx = refStr.indexOf("(");
                String referenceName = refStr.substring(0,pIndx).trim();

                foreignKeysData.add(new ForeignKeyData(referenceName, foreignKeyArr, foreignKeyRefArr));

            }
            else if(innerStatement.startsWith(UNIQUE_STR)){
                String unique = parseParentheses(statement, innerStatement);
                uniqueKeysData.add(unique.split("\\s+"));

            }
            else{
                //split dis boy up after delimiting spaces
                innerStatement = innerStatement.trim();
                String[] attributeData = innerStatement.split("\\s+");

                if(attributeData.length < 2){
                    throw new DDLParserException(String.format(CREATE_TABLE_INVALID_ATTRIBUTE_LEN, innerStatement));
                }

                // otherwise we assign it's name as the first index
                String attributeName = attributeData[0];


                // and then it's DataType must be resolved.
                try{

                    // construct our new attribute
                    ValidDataTypes.resolveType(attributeData[1]);
                    Attribute attribute = new Attribute(attributeName,attributeData[1]);

                    // iterating over constraints
                    for(int i=2; i < attributeData.length; i++){

                        String constraintName = attributeData[i];

                        try{
                            Constraint constraint;

                            // error handling 'not null'
                            if(constraintName.equals("not")){
                                // check next is null
                                i++;
                                constraintName = attributeData[i];
                                if(constraintName.equals("null"))
                                    constraint = Constraint.NOTNULL;
                                else
                                    throw new DDLParserException(String.format( CREATE_TABLE_CONSTRAINT_DEF, constraintName));
                            }
                            else{
                                constraint = Constraint.valueOf(constraintName.toUpperCase());
                            }


                            // check if the constraint is already defined
                            if(attribute.hasConstraint(constraint)){
                                throw new DDLParserException(String.format( CREATE_TABLE_CONSTRAINT_DEF, constraintName));
                            }

                            if(constraint.equals(Constraint.PRIMARYKEY)){

                                if(primaryKeyCount > 0){
                                    throw new DDLParserException(String.format(CREATE_TABLE_MULT_PKS, innerStatement));
                                }

                                // in this case our primary key data is this attribute
                                primaryKeyData = new String[]{attributeName};

                                primaryKeyCount++;
                            }

                            attribute.addConstraint(constraint);

                        }catch (IllegalArgumentException e){
                            throw new DDLParserException(String.format(CREATE_TABLE_INVALID_ATTRIBUTE_CON, constraintName));
                        }

                    }

                    // add our attribute to the list
                    attributes.add(attribute);

                }
                catch (IllegalArgumentException e){
                    throw new DDLParserException(String.format(INVALID_DATATYPE, attributeData[1].toUpperCase()));
                }

            }

        }


        Table table = new Table(tableName, attributes);

        // settitng our primary key data
        table.setPrimaryKey(primaryKeyData);

        // set our foreign keys
        for(ForeignKeyData foreignKeyData: foreignKeysData){
            new ForeignKey(table, foreignKeyData.getAttributes(), foreignKeyData.getReferenceTable(),
                    foreignKeyData.getReferences());
        }

        // add our uniques
        for(String[] unique: uniqueKeysData){
            table.addUnique(unique);
        }

        // adding our table to the catalog
        Database.catalog.addTable(table);

    }

    private void parseAlterTableStatement(String statement, String args) throws DDLParserException, StorageManagerException {

        int addIdx = args.indexOf(ADD_STR);
        int dropIdx = args.indexOf(DROP_STR);

        // adding
        if (addIdx != -1){
            String tableName = args.substring(0, addIdx).trim();

            // get our table
            Table table = Database.catalog.getTable(tableName);

            // add statement is everything after 'add'
            String addStatement = args.substring(addIdx + ADD_STR.length()).trim();

            Attribute attribute;

            String[] attributeData = addStatement.split("\\s+");

            if(attributeData.length < 2){
                throw new DDLParserException(String.format(ALTER_TABLE_INVALID_ATTRIBUTE_LEN, statement));
            }

            // otherwise we assign it's name as the first index
            String attributeName = attributeData[0];

            try{

                int defaultIdx = args.indexOf(DEFAULT_STR);

                // not sure how we want to handle default value yet...

                // attribute definition with no default
                if(defaultIdx == -1)
                {
                    if(attributeData.length > 2)
                        throw new DDLParserException(String.format(ALTER_TABLE_INVALID_ATTRIBUTE_LEN, statement));

                    ValidDataTypes.resolveType(attributeData[1]);
                    attribute = new Attribute(attributeName, attributeData[1]);

                    //get the table from catalog
                    //Call addAttribute, if no error continue
                    //Call getRecords
                    //Call Catalog replace table with the same table that you got previously


                    table.addAttribute(attribute);
                    Object[][] tableRec = table.getRecords();
                    Database.catalog.replaceTable(table);
                    for(Object[] record: tableRec){
                        //Call tables insert record with each record array in get records extended with null value
                        Object[] extendedRec = new Object[record.length + 1];
                        System.arraycopy(record, 0, extendedRec, 0, record.length);
                        extendedRec[record.length] = null;
                        table.addRecord(extendedRec);
                    }

                }
                // attribute defintion with a default
                else {
                    // we should have attrName, typeName, default, defaultValue
                    if (attributeData.length < 3) {
                        throw new DDLParserException(String.format(ALTER_TABLE_INVALID_ATTRIBUTE_LEN, statement));
                    }
                    if (attributeData.length > 4)
                        throw new DDLParserException(String.format(ALTER_TABLE_INVALID_ATTRIBUTE_LEN, statement));

                    String defaultVal = attributeData[3];

                    // and then it's DataType must be resolved.
                    Datatype defaultData = ValidDataTypes.resolveType(attributeData[1]);

                    try {
                        Object defaultValue = defaultData.parseData(defaultVal);

                        attribute = new Attribute(attributeName, attributeData[1]);

                        // function call goes here
                        table.addAttribute(attribute);
                        Object[][] tableRec = table.getRecords();
                        Database.catalog.replaceTable(table);
                        for(Object[] record: tableRec){
                            //Call tables insert record with each record array in get records extended with new value
                            Object[] extendedRec = new Object[record.length + 1];
                            System.arraycopy(record, 0, extendedRec, 0, record.length);
                            extendedRec[record.length] = defaultValue;
                            table.addRecord(extendedRec);
                        }


                    } catch (DataTypeException e) {
                        throw new DDLParserException(String.format(ALTER_TABLE_INVALID_ATTRIBUTE_DEFAULT, attributeData[3]));
                    }

                    // need to figure out way to store default value

                }

            }
            catch (IllegalArgumentException | StorageManagerException e){
                throw new DDLParserException(String.format(INVALID_DATATYPE, attributeData[1].toUpperCase()));
            }


        }
        // dropping
        else if (dropIdx != -1){
            String tableName = args.substring(0, DROP_STR.length()).trim();

            // figure out element we are dropping
            String droppedAtr = args.substring(dropIdx+DROP_STR.length()).trim();

            if( droppedAtr.split("\\s+").length > 1)
                throw new DDLParserException(String.format(ALTER_TABLE_INVALID_ATTRIBUTE_LEN,statement));

            if(droppedAtr.isEmpty())
                throw new DDLParserException(String.format(ALTER_TABLE_DROP_NO_ATR, statement));

            //Get the table from the catalog
            //Call  Catalog.removeAttributeFromTable, This gives you an index of the attribute dropped, The function fails if attribute dne or is a primary key
            //Call getRecords on table
            //Call Catalog with replace table with the same table that that you got previously
            //Call tables insert record with each record array from the get records but with the index gotten previously removed

            Table table = Database.catalog.getTable(tableName);

            // call alterTableDrop(tableName, droppedAtr);
            int idx = Database.catalog.removeAttributeFromTable(tableName, droppedAtr);

            Object[][] tableRec = table.getRecords();
            Database.catalog.replaceTable(table);
            for(Object[] record: tableRec){
                //Call tables insert record with each record array in get records extended with new value
                Object[] reducedRec = new Object[record.length- 1];
                int j = 0;
                for(int i=0; i<record.length; i++){
                    if(i != idx){
                        reducedRec[j] = record[i];
                        j++;
                    }
                }
                table.addRecord(reducedRec);
            }

        }
        // erroring
        else{
            throw new DDLParserException(String.format(ALTER_TABLE_NO_ADD_DROP, statement));
        }

    }

    private void parseDropTableStatement(String statement, String args, int end) throws DDLParserException {

        if(args.length() == 0){
            throw new DDLParserException(String.format(DROP_TABLE_EMPTY_NAME, statement));
        }

        // trim whitespace, and theres our table name WOW!
        String tableName = args.trim();

        try {
            Database.catalog.dropTable(tableName);
        } catch (StorageManagerException e) {
            throw new DDLParserException(e.getLocalizedMessage());
        } catch (NullPointerException e) {
            throw new DDLParserException(String.format(DROP_TABLE_DNE, tableName));
        }

        // call drop(tableName);

    }


    /**
     * Parses the first occurenece of parentheses, throwing a DDLParserException if missing any
     * @param args
     * @return
     * @throws DDLParserException
     */
    private String parseParentheses(String statement, String args) throws DDLParserException{
        int ibeg = args.indexOf("(");

        if (ibeg == -1)
            throw new DDLParserException(String.format(CREATE_TABLE_MISSING_PAREN, statement));

        int iend = args.indexOf(")");

        if (iend == -1)
            throw new DDLParserException(String.format(CREATE_TABLE_MISSING_PAREN, statement));

        // rreturn the substring, trimming starting and ending whitespace
        return args.substring(ibeg+1,iend).trim();
    }

}
