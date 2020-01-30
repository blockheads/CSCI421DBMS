package buffermanager.Datatype;

import java.util.Arrays;

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
    public char[] resolveData(Object obj) {
        final char[] chars = new char[maxChars];
        final char[] resolved = super.resolveData(obj);
        for (int i = 0; i < maxChars; i++) {
            if (i < resolved.length) {
                chars[i] = resolved[i];
            } else {
                chars[i] = ' ';
            }
        }
        return chars;
    }

    @Override
    public String resolveToString(Object obj) {
        return String.valueOf(resolveData(obj));
    }

    public int getMaxChars() {
        return maxChars;
    }

    @Override
    public String toString() {
        return type + " " + maxChars;
    }
}
