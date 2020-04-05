package dml.condition;

import ddl.catalog.Attribute;
import ddl.catalog.Table;
import dml.DMLParserException;

import java.util.*;

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
        ATTR, BOOL, INT, DOUB, STR
    }


    public static String conditionsRegex = "(!=|>=|<=|=|>|<)";
    private final static Map<String, Equality> equalityMap = new HashMap<>() {{
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
        if (equality == null) throw new DMLParserException("There must be a comparision in each clause: " + segment);

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
            case "double":
                rhsType = RHS.DOUB;
                try {
                    rhsObject = Double.parseDouble(sides[1]);
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

    private void attemptAsAttr(String rhs) throws DMLParserException {
        rhsObject = table.getAttribute(rhs);
        if (rhsObject == null) throw new DMLParserException(rhs + " is not an object of type " + rhsType + " or an attr in the table");
        if (!((Attribute) rhsObject).getDataType().equals(attribute.getDataType()))
            throw new DMLParserException("attributes: (" + attribute.getName() + ", " +
                    ((Attribute) rhsObject).getName() + " have incompatible data types.");
        rhsType = RHS.ATTR;
    }

    @Override
    public Set<Object[]> resolveAgainst(Set<Object[]> records) {
        final Set<Object[]> accepted = new HashSet<>();
        for (Object[] record : records) {
            if (resolves(record)) accepted.add(record);
        }
        return accepted;
    }

    private boolean resolves(Object[] record) {
        switch (rhsType) {
            case ATTR:
                return cmp(record[table.getIndex(attribute)], record[table.getIndex((Attribute) rhsObject)]);
            case BOOL:
                return cmp((Boolean) record[table.getIndex(attribute)], (Boolean) rhsObject);
            case INT:
                return cmp((Integer) record[table.getIndex(attribute)], (Integer) rhsObject);
            case DOUB:
                return cmp((Double) record[table.getIndex(attribute)], (Double) rhsObject);
            case STR:
                return cmp((String) record[table.getIndex(attribute)], (String) rhsObject);
        }
        return false;
    }

    private boolean cmp (Object obj1, Object obj2) {

        if (obj1 == null || obj2 == null) {
            return obj1 == obj2;
        }

        switch (attribute.getDataType().split("[(]")[0]) {
            case "integer":
                return cmp((Integer) obj1, (Integer) obj2);
            case "double":
                return cmp((Double) obj1, (Double) obj2);
            case "varchar":
            case "char":
                return cmp((String) obj1, (String) obj2);
            case "boolean":
                return cmp((Boolean) obj1, (Boolean) obj2);
            default:  // should never occur, the check for this happened when the clause was created
                throw new RuntimeException("The attribute " + attribute.getName() + " has type "
                        + attribute.getDataType() + " which is unrecognized by this clause.");
        }
    }

    private <E extends Comparable<E>> boolean cmp (E s1, E s2) {

        if (s1 == null || s2 == null) {
            return s1 == s2;
        }

        switch (equality) {
            case EQUAL:
                return s1.compareTo(s2) == 0;
            case NOTEQUAL:
                return s1.compareTo(s2) != 0;
            case GREATER:
                return s1.compareTo(s2) > 0;
            case LESS:
                return s1.compareTo(s2) < 0;
            case GREATER_EQUAL:
                return s1.compareTo(s2) >= 0;
            case LESS_EQUAL:
                return s1.compareTo(s2) <= 0;
        }
        return false;
    }

    @Override
    public Set<Attribute> getUsedAttributes() {
        Set<Attribute> usedAttr = new HashSet<>();
        usedAttr.add(attribute);
        if (rhsType == RHS.ATTR) {
            usedAttr.add((Attribute) rhsObject);
        }
        return usedAttr;
    }
}
