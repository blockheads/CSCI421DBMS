package ddl;

import ddl.catalog.Attribute;
import ddl.catalog.Constraint;
import storagemanager.StorageManagerException;
import storagemanager.buffermanager.datatypes.Datatype;
import storagemanager.buffermanager.datatypes.ValidDataTypes;

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
    private final static String INVALID_STATEMENT = "A invalid statement has been entered not supported by the database.";
    private final static String STATEMENT_MISSING_SEMICOLON = "A statement is missing a semicolon.";

    private final static String CREATE_TABLE_MISSING_PAREN = "A create table statement is missing a parenthesis.";
    private final static String CREATE_TABLE_MISSING_REFERENCES = "A create table statement is missing references keyword" +
            "on foreign key definition.";
    private final static String CREATE_TABLE_MULT_PKS = "A create table statement is attempting to create multiple " +
            "primary keys.";
    private final static String CREATE_TABLE_INVALID_ATTRIBUTE_LEN = "A create table statement is attempting to define " +
            "a attribute without a type, or a name.";
    private final static String CREATE_TABLE_INVALID_ATTRIBUTE_CON = "A create table statement is attempting to define " +
            "a invalid attribute constraint.";
    private final static String CREATE_TABLE_CONSTRAINT_DEF = "A create table statement is attempting to define " +
            "a constraint which has already been defined.";

    private final static String DROP_TABLE_EMPTY_NAME = "A drop table statement does not specify a table name.";

    private final static String ALTER_TABLE_NO_ADD_DROP = "A alter table statement does not specify either to " +
            "add or drop from a table.";

    private final static String ALTER_TABLE_DROP_NO_ATR = "A alter table statement does not specify a attribute to drop.";
    private final static String ALTER_TABLE_INVALID_ATTRIBUTE_LEN = "A alter table statement is attempting to alter " +
            "a attribute without a invalid amount of arguments.";

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
    public void parseDDLstatement(String statement) throws DDLParserException, StorageManagerException {

        // convert to lowercase
        statement = statement.toLowerCase();
        // strip leading white space
        statement = statement.stripLeading();


        //todo: each statement needs to check it ends with a semicolon
        // now search for the end of our statement
        int iend = statement.indexOf(";");

        if(iend == -1){
            throw new DDLParserException(STATEMENT_MISSING_SEMICOLON);
        }

        // we check if we begin with a valid statement
        if(statement.startsWith(CREATE_TABLE_STATMENT)){

            String args = statement.substring(CREATE_TABLE_STATMENT.length());
            parseCreateTableStatement(args);
        }
        else if(statement.startsWith(ALTER_TABLE_STATEMENT)){
            String args = statement.substring(ALTER_TABLE_STATEMENT.length());
            parseAlterTableStatement(args);
        }
        else if(statement.startsWith(DROP_TABLE_STATEMENT)){
            String args = statement.substring(DROP_TABLE_STATEMENT.length());
            parseDropTableStatement(args, iend);
        }
        else {
            throw new DDLParserException(INVALID_STATEMENT);
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
    private void parseCreateTableStatement(String args) throws DDLParserException, StorageManagerException {
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
            throw new DDLParserException(CREATE_TABLE_MISSING_PAREN);

        String tableName =  args.substring(0 , ibeg); //this will give the name

        int iend = args.lastIndexOf(")");

        if (iend == -1)
            throw new DDLParserException(CREATE_TABLE_MISSING_PAREN);

        String innerStatements = args.substring(ibeg,iend);

        // we can only have one primary key
        String[] primaryKeyData = null;
        // we can have multiple foreign keys or unique keys
        ArrayList<ForeignKeyData> foreignKeysData = new ArrayList<>();
        ArrayList<String[]> uniqueKeysData = new ArrayList<>();
        ArrayList<Attribute> attributes = new ArrayList<>();

        // counter for primary keys, obviously we can only have one
        int primaryKeyCount = 0;

        // then we split our inner statements by ,
        for(String innerStatement: innerStatements.split(",")){

            innerStatement = innerStatement.stripLeading();

            if(innerStatement.startsWith(PRIMARY_KEY_STR)){

                // tsk, tsk tsk...
                if(primaryKeyCount > 0){
                    throw new DDLParserException(CREATE_TABLE_MULT_PKS);
                }

                String primaryKeys = parseParentheses(innerStatement);
                primaryKeyData = primaryKeys.split("\\s+");

                primaryKeyCount++;

            }
            else if(innerStatement.startsWith(FOREIGN_KEY_STR)){

                int refIndx = args.indexOf(REFERENCES_STR);

                if (refIndx == -1)
                    throw new DDLParserException(CREATE_TABLE_MISSING_REFERENCES);

                // similar to .split()
                String foreignKeyStr = innerStatement.substring(0,refIndx);
                String refStr = innerStatement.substring(refIndx+REFERENCES_STR.length());

                // retrieve our foreign keys
                String foreignKeys = parseParentheses(foreignKeyStr);
                String[] foreignKeyArr = foreignKeys.split("\\s+");

                // then get our reference
                String references = parseParentheses(refStr);
                String[] foreignKeyRefArr = references.split("\\s+");

                // and retrieve the table we are referencing
                int pIndx = references.indexOf("(");
                String referenceName = references.substring(0,pIndx).trim();

                foreignKeysData.add(new ForeignKeyData(referenceName, foreignKeyArr, foreignKeyRefArr));

            }
            else if(innerStatement.startsWith(UNIQUE_STR)){
                String unique = parseParentheses(innerStatement);
                uniqueKeysData.add(unique.split("\\s+"));

            }
            else{
                //split dis boy up after delimiting spaces
                innerStatement = innerStatement.trim();
                String[] attributeData = innerStatement.split("\\s+");

                if(attributeData.length < 2){
                    throw new DDLParserException(CREATE_TABLE_INVALID_ATTRIBUTE_LEN);
                }

                // otherwise we assign it's name as the first index
                String attributeName = attributeData[0];

                // and then it's DataType must be resolved.
                ValidDataTypes type = ValidDataTypes.valueOf(attributeData[1]);

                // construct our new attribute
                Attribute attribute = new Attribute(attributeName,type);

                // iterating over constraints
                for(int i=2; i < attributeData.length; i++){

                    String constraintName = attributeData[i];

                    try{
                        Constraint constraint = Constraint.valueOf(constraintName);

                        // check if the constraint is already defined
                        if(attribute.hasConstraint(constraint)){
                            throw new DDLParserException(CREATE_TABLE_CONSTRAINT_DEF);
                        }

                        if(constraint.equals(Constraint.PRIMARY_KEY)){

                            if(primaryKeyCount > 0){
                                throw new DDLParserException(CREATE_TABLE_MULT_PKS);
                            }

                            // in this case our primary key data is this attribute
                            primaryKeyData = new String[]{attributeName};

                            primaryKeyCount++;
                        }

                    }catch (IllegalArgumentException e){
                        throw new DDLParserException(CREATE_TABLE_INVALID_ATTRIBUTE_CON);
                    }

                }

                // add our attribute to the list
                attributes.add(attribute);

            }

        }

        // call CreateTable(primaryKeyData, foreignKeyData, uniqueKeysData, attributes);

    }

    private void parseAlterTableStatement(String args) throws DDLParserException {

        int addIdx = args.indexOf(ADD_STR);
        int dropIdx = args.indexOf(DROP_STR);

        // adding
        if (addIdx != -1){
            String tableName = args.substring(0, ADD_STR.length()).trim();

            String addStatement = args.substring(ADD_STR.length()).trim();

            Attribute attribute;

            String[] attributeData = addStatement.split("\\s+");

            if(attributeData.length < 2){
                throw new DDLParserException(ALTER_TABLE_INVALID_ATTRIBUTE_LEN);
            }
            else if(attributeData.length > 2)
                throw new DDLParserException(ALTER_TABLE_INVALID_ATTRIBUTE_LEN);

            // otherwise we assign it's name as the first index
            String attributeName = attributeData[0];

            // and then it's DataType must be resolved.
            ValidDataTypes type = ValidDataTypes.valueOf(attributeData[1]);

            int defaultIdx = args.indexOf(DEFAULT_STR);

            // not sure how we want to handle default value yet...

            // attribute definition with no default
            if(defaultIdx == -1)
            {

            }
            // attribute defintion with a default
            else
            {

            }

        }
        // dropping
        else if (dropIdx != -1){
            String tableName = args.substring(0, DROP_STR.length()).trim();

            // figure out element we are dropping
            String droppedAtr = args.substring(DROP_STR.length()).trim();

            if(droppedAtr.isEmpty())
                throw new DDLParserException(ALTER_TABLE_DROP_NO_ATR);

            // call alterTableDrop(tableName, droppedAtr);

        }
        // erroring
        else{
            throw new DDLParserException(ALTER_TABLE_NO_ADD_DROP);
        }

    }

    private void parseDropTableStatement(String args, int end) throws DDLParserException {

        // remove semicolon
        args = args.substring(0,end);

        if(args.length() == 0){
            throw new DDLParserException(DROP_TABLE_EMPTY_NAME);
        }

        // trim whitespace, and theres our table name WOW!
        String tableName = args.trim();

        // call drop(tableName);

    }


    /**
     * Parses the first occurenece of parentheses, throwing a DDLParserException if missing any
     * @param args
     * @return
     * @throws DDLParserException
     */
    private String parseParentheses(String args) throws DDLParserException{
        int ibeg = args.indexOf("(");

        if (ibeg == -1)
            throw new DDLParserException(CREATE_TABLE_MISSING_PAREN);

        int iend = args.indexOf(")");

        if (iend == -1)
            throw new DDLParserException(CREATE_TABLE_MISSING_PAREN);

        // rreturn the substring, trimming starting and ending whitespace
        return args.substring(ibeg,iend).trim();
    }

}
