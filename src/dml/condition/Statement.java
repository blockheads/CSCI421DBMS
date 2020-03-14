package dml.condition;

import ddl.catalog.Table;
import dml.DMLParserException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * A statement is a complete clause
 *
 * @author Nicholas Chieppa
 */
public class Statement implements Resolvable {

    private enum Connection {
        CONJUNCTION, DISJUNCTION
    }

    private final static String junctionReg = "(and|or)\\b";

    /**
     * The list of resolvable clauses
     * Each index contains a list of 'and' statements that are adjacent
     * Each index separation represents an 'or' statement
     */
    private final ArrayList<Resolvable> resolvable;

    private Statement(ArrayList<Resolvable> resolvable) {
        this.resolvable = resolvable;
    }

    /**
     * Create a statement, aka a where clause on table attributes
     * @param table the table to grab attributes from
     * @param string the string from the user
     * @return a resolvable statement
     * @throws DMLParserException the user string is malformed; the table is missing an attribute
     */
    public static Statement fromWhere(Table table, String string) throws DMLParserException {

        final String[] clauses = string.split(junctionReg);
        final Connection[] junctions = new Connection[clauses.length - 1];

        int lengths = 0;
        for (int i = 0; i < clauses.length; i ++) {
            lengths += clauses[i].length();
            clauses[i] = clauses[i].trim();
            if (i < junctions.length)
                switch (string.substring(lengths).charAt(0)) {
                    case 'a':
                        junctions[i] = Connection.CONJUNCTION;
                        lengths += 3;
                        break;
                    case 'o':
                        junctions[i] = Connection.DISJUNCTION;
                        lengths += 2;
                        break;
                    default:
                        throw new DMLParserException("Invalid conjunction");
                }
        }

        int ors = 0;
        ArrayList<Resolvable> resolvables = new ArrayList<>();
        resolvables.add(new Condition(table, clauses[0]));

        for (int i = 0; i < junctions.length; i++) {

            switch (junctions[i]) {
                case CONJUNCTION:
                    resolvables.set(ors, new Conjunction(resolvables.get(ors), new Condition(table, clauses[i + 1])));
                    break;
                case DISJUNCTION:
                    resolvables.add(new Condition(table, clauses[i + 1]));
                    ors ++;
                    break;
            }
        }

        return new Statement(resolvables);
    }

    @Override
    public Set<Object[]> resolveAgainst(Set<Object[]> data) {
        Set<Object[]> result = new HashSet<>();
        for (Resolvable resolvable : this.resolvable) {
            result.addAll(resolvable.resolveAgainst(data));
        }
        return result;
    }
}
