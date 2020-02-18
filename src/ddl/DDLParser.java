package ddl;

/**
 * This a a parser for DDL statements
 */

public class DDLParser implements IDDLParser {

    private final String createTableStatement = "create table";
    private final String alterTableStatement = "alter table";
    private final String dropTableStatement = "drop table";

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

        // we check if we begin with a valid statement
        if(statement.startsWith(createTableStatement)){
            String args = statement.substring(createTableStatement.length());
            parseCreateTableStatement(args);
        }
        else if(statement.startsWith(alterTableStatement)){
            String args = statement.substring(alterTableStatement.length());
            parseAlterTableStatement(args);
        }
        else if(statement.startsWith(dropTableStatement)){
            String args = statement.substring(dropTableStatement.length());
            parseDropTableStatement(args);
        }
        else {
            throw new DDLParserException(DDLParserException.INVALID_STATEMENT);
        }

    }

    private void parseCreateTableStatement(String args){

    }

    private void parseAlterTableStatement(String args){

    }

    private void parseDropTableStatement(String args){

    }
}
