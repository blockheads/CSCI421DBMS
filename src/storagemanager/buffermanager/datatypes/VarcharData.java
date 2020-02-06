package storagemanager.buffermanager.datatypes;

public class VarcharData extends CharData {
    /**
     * @param maxChars The amount of chars this attr can hold
     */
    public VarcharData(int maxChars) {
        super(ValidDataTypes.VARCHAR, maxChars);
        padding = '\0';
    }

    @Override
    public char[] resolveData(Object obj) {
        return String.valueOf(super.resolveData(obj)).trim().toCharArray();
    }
}
