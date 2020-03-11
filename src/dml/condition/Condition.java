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
        ATTR, BOOL, INT, STR
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
    private RHS rhsType;

    /**
     * The data on the right hand side that we are checking against
     */
    private Object rhsObject;

    /**
     * Represent a single part (test between two junctions) of the statement
     * @param segment the segment of the statement to represent
     */
    Condition (Table table, String segment) throws DMLParserException {
        this.table = table;

        String[] sides = segment.split(conditionsRegex, 2);
        equality = equalityMap.get(segment.substring(sides[0].length(),
                sides[0].length() + (segment.length() - (sides[0].length() + sides[1].length()))));

        for (int i = 0; i < sides.length; i++) sides[i] = sides[i].trim();

        attribute = table.getAttribute(sides[0]);
        if (attribute == null) throw new DMLParserException("");

        switch (attribute.getDataType().split("[(]")[0]) {
            case "integer":
                rhsType = RHS.INT;
                try {
                    rhsObject = Integer.parseInt(sides[1]);
                } catch (NumberFormatException e) {
                    attemptAsAttr(sides[1]);
                }
                break;
            case "varchar":
            case "char":
                rhsType = RHS.STR;
                if (sides[1].contains("\"")) {
                    rhsObject = sides[1].replaceAll("\"","");
                }
                else
                    attemptAsAttr(sides[1]);
                break;
            case "boolean":
                rhsType = RHS.BOOL;
                try {
                    rhsObject = Boolean.parseBoolean(sides[1]);
                } catch (NumberFormatException e) {
                    attemptAsAttr(sides[1]);
                }
                break;
            default:
                throw new DMLParserException("The attribute " + attribute.getName() + " has type "
                        + attribute.getDataType() + " which is unrecognized by this clause.");
        }
    }

    void attemptAsAttr(String rhs) throws DMLParserException {
        rhsObject = table.getAttribute(rhs);
        if (rhsObject == null) throw new DMLParserException(rhs + " is not an object of type " + rhsType + " or an attr in the table");
        if (!((Attribute) rhsObject).getDataType().equals(attribute.getDataType()))
            throw new DMLParserException("attributes: (" + attribute.getName() + ", " +
                    ((Attribute) rhsObject).getName() + " have incompatible data types.");
        rhsType = RHS.ATTR;
    }


    @Override
    public Object[][] resolveAgainst(Object[][] data) {
        return new Object[0][];
    }


}
