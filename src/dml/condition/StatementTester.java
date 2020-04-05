package dml.condition;

import ddl.DDLParserException;
import ddl.catalog.Attribute;
import ddl.catalog.Constraint;
import ddl.catalog.Table;
import dml.DMLParserException;
import dml.condition.Statement;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;

class StatementTester {
    public static void main(String[] args) throws DDLParserException, DMLParserException {
        int test = 1;

        switch (test) {
            case 0: {
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
            }
            break;
            case 1: {
                ArrayList<Attribute> attributes1 = new ArrayList<>() {{
                        add(new Attribute("a1", "integer", Constraint.PRIMARYKEY));
                        add(new Attribute("a2", "integer", Constraint.NOTNULL));
                        add(new Attribute("s3", "integer"));
                    }};
                ArrayList<Attribute> attributes2 = new ArrayList<>() {{
                        add(new Attribute("b1", "integer", Constraint.PRIMARYKEY));
                        add(new Attribute("b2", "integer", Constraint.NOTNULL));
                        add(new Attribute("s3", "integer"));
                    }};
                Table t1 = new Table("A", attributes1);
                Table t2 = new Table("B", attributes2);
                HashSet<Table> tables = new HashSet<>();
                tables.add(t1); tables.add(t2);
//                Map<Table, Resolvable> resolve1 = Statement.fromMutliTableWhere(tables, "a1 = 1 and b1 = 2");
                Map<Table, Resolvable> resolve2 = Statement.fromMutliTableWhere(tables, "a1 = 1 and b1 = a1");
                Map<Table, Resolvable> resolve3 = Statement.fromMutliTableWhere(tables, "a1 = A.s3 and b1 = 2");
                Map<Table, Resolvable> resolve4 = Statement.fromMutliTableWhere(tables, "a1 = A.s3 and b1 = 2 or b2 = 3");
                Map<Table, Resolvable> resolve5 = Statement.fromMutliTableWhere(tables, "a1 = A.s3 and b1 = A.s3 or b2 = 3 and b1 = A.a1 or B.s3 = 7");
                int i = 2;
            }
            break;
        }
    }
}