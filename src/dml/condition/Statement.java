package dml.condition;

import ddl.DDLParserException;
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
    private final static String conditionsRegex = "[ ]*(!=|>=|<=|=|>|<)[ ]*";

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

    public static Resolvable whereTrue () {
        return new Resolvable() {
            @Override
            public Set<Object[]> resolveAgainst(Set<Object[]> records) {
                return records;
            }

            @Override
            public Set<Attribute> getUsedAttributes() {
                return new HashSet<>();
            }
        };
    }

    /**
     * Used for selection
     *
     * Break a where statement into parts where only one table is required for evaluation,
     * Tables are broken up by and statements, a fromWhere should be run again after the internal table is built
     * to generate the final set
     * @return a collection of statements by table
     */
    public static Map<Table, Resolvable> fromMutliTableWhere(Set<Table> tables, String condition) throws DDLParserException, DMLParserException {
        Map<Set<Table>, Table> createdTables = new HashMap<>();
        Map<Table, Resolvable> preStatements = new HashMap<>();
        int internal_id = 0;

        String[] conjunctions = condition.split(conjunctionReg);

        int c_id = 0;
        for (String conjunction: conjunctions) {
            String[] disjunctions = conjunction.trim().split(disjunctionReg);
            Set<Table> usedTables = new HashSet<>();

            for (String disjunction : disjunctions) { // get all the tables needed to preform this operation
                usedTables.addAll(whichTables(tables, disjunction.trim()));
            }

            Table nTable = null; // find a table that matches all the tables needed in the operation
            if (usedTables.size() == 1) {
                nTable = usedTables.iterator().next(); // sets are terrible
            } else {
                if (createdTables.containsKey(usedTables)) {
                    nTable = createdTables.get(usedTables);
                } else {
                    nTable = new Table(internal_id++, usedTables);
                    createdTables.put(usedTables, nTable);
                }
            }

            Statement s = Statement.fromWhere(nTable, conjunction);
            if (preStatements.containsKey(nTable)) {
                preStatements.put(nTable, new Conjunction(preStatements.get(nTable), s));
            } else {
                preStatements.put(nTable, s);
            }

        }

        return preStatements;
    }

    private static boolean isAttr(String rhs) {
        if (rhs.contains("\"")) { // detect strings
            return false;
        } else if (rhs.equals("null")) return false;
        else return rhs.matches("(.)*[a-zA-Z]+(.)*"); // must be an attr if the side has a letter, otherwise its a number
    }

    private static Set<Table> whichTables(Set<Table> tables, String condition) {
        Set<Table> usedTables = new HashSet<>();
        String[] sides = condition.split(conditionsRegex, 2);
        boolean checkRHS = isAttr(sides[1]);
        for (Table table : tables) {
            if (table.containsAttribute(sides[0])) usedTables.add(table);
            if (checkRHS && table.containsAttribute(sides[1])) usedTables.add(table);
        }
        return usedTables;
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
