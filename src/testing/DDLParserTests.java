package testing;

import ddl.DDLParser;
import ddl.IDDLParser;

import javax.swing.text.html.parser.Parser;

public class DDLParserTests {

    public static void main(String[] args){

        IDDLParser parser = DDLParser.createParser();

        try{
            assert parser != null;
            parser.parseDDLstatement("alter table test add poop integer default 1;");
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }
}
