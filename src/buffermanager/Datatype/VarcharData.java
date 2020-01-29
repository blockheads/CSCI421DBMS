package buffermanager.Datatype;

public class VarcharData extends CharData {
    /**
     * @param maxChars The amount of chars this attr can hold
     */
    public VarcharData(int maxChars) {
        super(ValidDataTypes.VARCHAR, maxChars);
    }
}
