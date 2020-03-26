package dml;

import ddl.catalog.Attribute;
import ddl.catalog.Table;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UpdateFilter {

    private enum MATHOP {
        ADD, SUB, MUL, DIV
    }

    private final static String mathopRegex = "([+\\-*/])";
    private final static Map<String, MATHOP> mathopMap = new HashMap<>() {{
       put("+", MATHOP.ADD);
       put("-", MATHOP.SUB);
       put("*", MATHOP.MUL);
       put("/", MATHOP.DIV);
    }};

    private enum VALTYPES {
        ATTR, DOUBLE, INTEGER
    }

    private final Table table;

    private final List<MATHOP> operations = new ArrayList<>();
    private final List<Attribute> assigess = new ArrayList<>();

    private final List<List<Object>> inputs = new ArrayList<>();
    private final List<List<VALTYPES>> inputTypes = new ArrayList<>();

    UpdateFilter(Table table, String allColumns) throws DMLParserException {
        this.table = table;
        String[] columns = allColumns.split(",[ ]*");

        for (String column : columns) {
            String[] parts = column.split("[ ]*=[ ]*");
            assigess.add(table.getAttribute(parts[0].trim()));

            String[] operands = parts[1].split(mathopRegex);

            inputs.add(new ArrayList<>());
            inputTypes.add(new ArrayList<>());

            parseOperand(operands[0].trim());
            parseOperand(operands[1].trim());

             operations.add(mathopMap.get(parts[1].substring(operands[0].length(),
                    operands[0].length() + (parts[1].length() - (operands[0].length() + operands[1].length())))));
        }
    }

    private void parseOperand(String operand) throws DMLParserException {

        try {
            if (operand.contains(".")) {
                Double d = Double.parseDouble(operand);

                inputs.get(inputs.size() - 1).add(d);
                inputTypes.get(inputs.size() - 1).add(VALTYPES.DOUBLE);
            } else {
                Integer d = Integer.parseInt(operand);

                inputs.get(inputs.size() - 1).add(d);
                inputTypes.get(inputs.size() - 1).add(VALTYPES.INTEGER);
            }
        } catch (NumberFormatException e) {
            Attribute attribute = table.getAttribute(operand);
            if (attribute == null) throw new DMLParserException("");

            inputs.get(inputs.size() - 1).add(attribute);
            inputTypes.get(inputs.size() - 1).add(VALTYPES.ATTR);
        }
    }

    public Object[] performUpdate(Object[] oldTuple) {
        Object[] newTuple = oldTuple.clone();
        for (int i = 0; i < assigess.size(); i++) {
            Attribute assign = assigess.get(i);



//            switch (operations.get(i)) {
//                case ADD:
//                    newTuple[table.getIndex(assign)] = ;
//                    break;
//                case SUB:
//                    newTuple[table.getIndex(assign)];
//                    break;
//                case MUL:
//                    newTuple[table.getIndex(assign)];
//                    break;
//                case DIV:
//                    newTuple[table.getIndex(assign)];
//                    break;
//            }
        }
        return newTuple;
    }

    public Double doubleMath () {
        return 0.0;
    }

    public Integer intMath() {
        return 0;
    }

}
