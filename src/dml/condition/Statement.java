package dml.condition;

import ddl.catalog.Table;
import dml.DMLParserException;

/**
 * A statement is a complete clause
 *
 * @author Nicholas Chieppa
 */
public class Statement implements Resolvable {

    private final static String junctionReg = "(and|or)\\b";

    private final Resolvable resolvable;

    private Statement(Resolvable resolvable) {
        this.resolvable = resolvable;
    }

    public static Statement fromWhere(Table table, String string) throws DMLParserException {

        final String[] clauses = string.split(junctionReg);
        final Junction.Connection[] junctions = new Junction.Connection[clauses.length - 1];

        int lengths = 0;
        for (int i = 0; i < clauses.length; i ++) {
            lengths += clauses[i].length();
            clauses[i] = clauses[i].trim();
            if (i < junctions.length)
                switch (string.substring(lengths).charAt(0)) {
                    case 'a':
                        junctions[i] = Junction.Connection.CONJUNCTION;
                        lengths += 3;
                        break;
                    case 'o':
                        junctions[i] = Junction.Connection.DISJUNCTION;
                        lengths += 2;
                        break;
                    default:
                        throw new DMLParserException("Invalid conjunction");
                }
        }

        if (clauses.length == 0) throw new DMLParserException("");

        Resolvable resolvable = new Condition(table, clauses[0]);

        for (int i = 0; i < junctions.length; i++) {
            resolvable = new Junction(junctions[i], resolvable, new Condition(table, clauses[i + 1]));
        }

        return new Statement(resolvable);
    }

    @Override
    public Object[][] resolveAgainst(Object[][] data) {
        return resolvable.resolveAgainst(data);
    }
}
