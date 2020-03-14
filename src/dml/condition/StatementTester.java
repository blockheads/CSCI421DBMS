package dml.condition;

import ddl.DDLParserException;
import ddl.catalog.Attribute;
import ddl.catalog.Constraint;
import ddl.catalog.Table;
import dml.DMLParserException;
import dml.condition.Statement;

import java.util.ArrayList;

class StatementTester {
    public static void main(String[] args) throws DDLParserException, DMLParserException {
        ArrayList<Attribute> attributes = new ArrayList<>() {{
            add(new Attribute("a1", "integer", Constraint.PRIMARYKEY));
            add(new Attribute("a2", "integer", Constraint.NOTNULL));
            add(new Attribute("a3", "integer"));
            add(new Attribute("a", "integer"));
            add(new Attribute("b", "integer"));
            add(new Attribute("c", "integer"));
            add(new Attribute("d", "integer"));
        }};
        Table t = new Table("hello", attributes);
        Statement statement = Statement.fromWhere(t, "a1 <= 2");
        Statement statement2 = Statement.fromWhere(t, "a1 != a2");
        Statement statement3 = Statement.fromWhere(t, "a1 <= 2 and a2 != a3");
        Statement statement4 = Statement.fromWhere(t, "a1 <=  2   and   a2 != a3 or a3 = a1 ");
        Statement statement5 = Statement.fromWhere(t, "a = 1 or b = 2 and c = 3 and d = 4");

        return;
    }
}