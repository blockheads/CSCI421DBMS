package dml;

public interface DMLHandle {
    /**
     * This function will parse DML statments that do not expect a return
     * @param statement the DML statement to parse
     * @throws DMLParserException any error in parsing
     */
    void parseDMLStatement(String statement) throws DMLParserException;
}
