package dml.condition;

import ddl.catalog.Attribute;
import ddl.catalog.Table;
import dml.DMLParserException;

import java.util.HashMap;
import java.util.Map;

/**
 * A condition is the deepest level in a statement,
 * a condition takes in a table segment of data and limits the data returned based on a user defined equality
 *
 * @author Nicholas Chieppa
 */
public class Condition implements Resolvable {

    /**
     * Equality test on both sides of the equation
     */
    private enum Equality {
        EQUAL, NOTEQUAL, GREATER, LESS, GREATER_EQUAL, LESS_EQUAL
    }

    /**
     * The data on the right hand side of the equation
     */
    private enum RHS {
        ATTR, BOOL, STR
    }


    public String conditionsRegex = "(!=|>=|<=|=|>|<)";
    private final Map<String, Equality> equalityMap = new HashMap<>() {{
        put("=", Equality.EQUAL);
        put("!=", Equality.NOTEQUAL);
        put(">", Equality.GREATER);
        put("<", Equality.LESS);
        put(">=", Equality.GREATER_EQUAL);
        put("<=", Equality.LESS_EQUAL);
    }};

    public String booleanRegex = "(true|false)";

    private final Table table;

    /**
     * The attribute on the left hand side of the equation
     */
    private final Attribute attribute;

    /**
     * The equality test
     */
    private final Equality equality;

    /**
     * The type of data on the right hand side that we are checking against
     */
    private final RHS rhsType;

    /**
     * The data on the right hand side that we are checking against
     */
    private final Object rhsObject;

    /**
     * Represent a single part (test between two junctions) of the statement
     * @param segment the segment of the statement to represent
     */
    Condition (Table table, String segment) throws DMLParserException {
        this.table = table;

        String[] sides = segment.split(conditionsRegex, 2);
        equality = equalityMap.get(segment.substring(sides[0].length(),
                sides[0].length() + (segment.length() - (sides[0].length() + sides[1].length()))));

        attribute = table.getAttribute(sides[0]);
        if (attribute == null) throw new DMLParserException("");

        if (sides[1].matches(booleanRegex)) {
            rhsObject = Boolean.valueOf(sides[1]);
            rhsType = RHS.BOOL;
        } else if (sides[1].contains("\"")) {
            rhsObject = sides[1].replaceAll("\"", "");
            rhsType = RHS.STR;
        } else {
            Attribute attribute = table.getAttribute(sides[1]);
            if (attribute == null) throw new DMLParserException("");
            rhsObject = attribute;
            rhsType = RHS.ATTR;
        }
    }

    @Override
    public Object[][] resolveAgainst(Object[][] data) {
        return new Object[0][];
    }


}
