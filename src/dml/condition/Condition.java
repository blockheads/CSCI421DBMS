package dml.condition;

import java.util.HashMap;
import java.util.Map;

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

    private final Map<String, Equality> conditions = new HashMap<>() {{
        put("=", Equality.EQUAL);
        put("!=", Equality.NOTEQUAL);
        put(">", Equality.GREATER);
        put("<", Equality.LESS);
        put(">=", Equality.GREATER_EQUAL);
        put("<=", Equality.LESS_EQUAL);
    }};

    /**
     * The attribute on the left hand side of the equation
     */
    private final String attribute;

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
    Condition (String segment) {

    }

    @Override
    public Object[][] resolveAgainst(Object[][] data) {
        return new Object[0][];
    }


}
