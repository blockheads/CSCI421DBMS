package buffermanager.Datatype;

/**
 *
 */
public class CharData extends Datatype<char[]>{

    int maxChars;

    /**
     * @param maxChars The amount of chars this attr can hold
     */
    protected CharData(int maxChars) {
        super(ValidDataTypes.CHAR);
        this.maxChars = maxChars;
    }

    protected CharData(ValidDataTypes dataType, int maxChars) {
        super(dataType);
        this.maxChars = maxChars;
    }

    @Override
    public int getSize() {
        return maxChars * type.sizeInBytes;
    }

    @Override
    public char[] resolveData() {
        return null;
    }

    public int getMaxChars() {
        return maxChars;
    }

    @Override
    public String toString() {
        return type + " " + maxChars;
    }
}
