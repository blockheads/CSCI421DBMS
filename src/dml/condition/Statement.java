package dml.condition;

import ddl.catalog.Attribute;
import ddl.catalog.Table;
import dml.DMLParserException;

import java.util.*;

/**
 * A statement is a complete clause
 *
 * @author Nicholas Chieppa
 */
public class Statement implements Resolvable {

    private enum Connection {
        CONJUNCTION, DISJUNCTION
    }

    private final static String disjunctionReg = "(or)\\b";
    private final static String conjunctionReg = "(and)\\b";
    private final static String junctionReg = "(and|or)\\b";
    public static String conditionsRegex = "(!=|>=|<=|=|>|<)";

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

    /**
     * Used for selection
     *
     * Break a where statement into parts where only one table is required for evaluation,
     * Tables are broken up by and statements, a fromWhere should be run again after the internal table is built
     * to generate the final set
     * @return a collection of statements by table
     */
    public static Map<Table, Pair<Statement, Set<Attribute>>> fromMutliTableWhere(Set<Table> tables, String condition) {
        Map<Table, ArrayList<Resolvable>> usesable = new HashMap<>();

        String[] conjunctions = condition.split(conjunctionReg);

        for (String conjunction: conjunctions) {
            String[] disjunctions = conjunction.split(disjunctionReg);
            Set<Table> usedTables = new HashSet<>();
            for (String disjunction : disjunctions) {
                usedTables.addAll(whichTables(tables, condition));
            }

        }

        Map<Table, Pair<Statement, Set<Attribute>>> statements = new HashMap<>();
        for (Table table: usesable.keySet()) {
            Statement statement =  new Statement(usesable.get(table));
            statements.put(table, new Pair<>(statement, statement.getUsedAttributes()));
        }
        return statements;
    }

    private static boolean isAttr(String rhs) {
        return false;
    }

    private static Set<Table> whichTables(Set<Table> tables, String condition) {
        return null;
    }

    @Override
    public Set<Object[]> resolveAgainst(Set<Object[]> records) {
        Set<Object[]> result = new HashSet<>();
        for (Resolvable resolvable : this.resolvable) {
            result.addAll(resolvable.resolveAgainst(records));
        }
        return result;
    }

    @Override
    public Set<Attribute> getUsedAttributes() {
        Set<Attribute> usedAttr = new HashSet<>();
        for (Resolvable resolvable : this.resolvable) {
            usedAttr.addAll(resolvable.getUsedAttributes());
        }
        return usedAttr;
    }
}
