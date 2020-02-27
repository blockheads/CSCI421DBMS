package testing;

import ddl.DDLParser;
import ddl.IDDLParser;

import javax.swing.text.html.parser.Parser;

public class DDLParserTests {

    public static void main(String[] args){

        IDDLParser parser = DDLParser.createParser();
        assert parser != null;

        // create table test
        try{
            parser.parseDDLstatement("create table bazzle( baz double PRIMARYKEY);");
        }
        catch (Exception e){
            e.printStackTrace();
        }

        // a little more advanced
        try{
            parser.parseDDLstatement("create table foo(" +
                    "baz integer," +
                    "bar Double notnull," +
                    "primarykey(bar baz)," +
                    "foreignkey( bar ) references bazzle( baz ));");
        }
        catch (Exception e){
            e.printStackTrace();
        }

        try{

            parser.parseDDLstatement("alter table test add blergh integer default 1;");
        }
        catch (Exception e){
            e.printStackTrace();
        }

        try{

            parser.parseDDLstatement("drop table test;");
        }
        catch (Exception e){
            e.printStackTrace();
        }

        try{

            parser.parseDDLstatement("alter table test drop blergh;");
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }
}
