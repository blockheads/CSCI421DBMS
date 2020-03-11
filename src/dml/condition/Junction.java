package dml.condition;

public class Junction implements Resolvable {

    enum Connection {
        CONJUNCTION, DISJUNCTION
    }

    /**
     * The type of junction this represents
     */
    private final Connection type;

    /**
     * The clause on the LHS
     */
    private final Resolvable LHS;

    /**
     * The clause on the RHS
     */
    private final Resolvable RHS;


    Junction(Connection type, Resolvable lhs, Resolvable rhs) {
        this.type = type;
        LHS = lhs;
        RHS = rhs;
    }

    @Override
    public Object[][] resolveAgainst(Object[][] data) {
        return new Object[0][];
    }

}
