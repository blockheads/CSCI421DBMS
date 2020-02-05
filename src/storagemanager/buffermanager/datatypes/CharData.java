package storagemanager.buffermanager.datatypes;

import java.nio.ByteBuffer;

/**
 *
 */
public class CharData extends Datatype<char[]>{

    int maxChars;
    char padding = ' ';

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
    public byte[] toByteArray(char[] attribute) {
        ByteBuffer b = ByteBuffer.allocate(getSize());
        for (int i = 0; i < attribute.length; i++)
            b.putChar(attribute[i]);
        for (int i = attribute.length; i < maxChars; i++)
            b.putChar(padding);
        return b.array();
    }

    @Override
    public char[] toObject(byte[] attributes, int start) {
        ByteBuffer b = ByteBuffer.wrap(attributes, start, getSize());
        return b.toString().toCharArray();
    }

    @Override
    public boolean matches(Object obj) {
        char[] chars;
        try {
            chars = (char[]) type.objectClass.cast(obj);
        } catch (ClassCastException e) {
            return false;
        }
        return chars.length <= maxChars;
    }

    @Override
    public int nextIndex() {
        return getIndex() + maxChars * type.sizeInBytes;
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
