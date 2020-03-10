package dml.condition;

/**
 * A statement is a complete clause
 *
 * @author Nicholas Chieppa
 */
public class Statement implements Resolvable {

    private final Resolvable resolvable;

    private Statement() {
        resolvable = null;
    }

    public static Statement formStatement(String string) {
        return new Statement();
    }

    @Override
    public Object[][] resolveAgainst(Object[][] data) {
        return resolvable.resolveAgainst(data);
    }
}
