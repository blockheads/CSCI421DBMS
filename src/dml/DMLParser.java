package dml;

public class DMLParser implements IDMLParser {

    private static DMLParser dmlParser;

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
    public void parseDMLStatement(String statement) throws DMLParserException {}
    
    @Override
    public Object[][] parseDMLQuery(String statement) throws DMLParserException{
        return null;
    }

}
