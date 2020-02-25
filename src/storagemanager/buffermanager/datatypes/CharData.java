package storagemanager.buffermanager.datatypes;

import java.nio.ByteBuffer;

/**
 *
 */
public class CharData extends Datatype<String>{

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
    public String resolveData(Object obj) {
        final char[] chars = new char[maxChars];
        final char[] resolved = super.resolveData(obj).toCharArray();
        for (int i = 0; i < maxChars; i++) {
            if (i < resolved.length) {
                chars[i] = resolved[i];
            } else {
                chars[i] = ' ';
            }
        }
        return String.valueOf(chars);
    }

    @Override
    public boolean isType(Object obj) {
        if (obj instanceof String)
            return ((String) obj).length() <= maxChars;
        return false;
    }

    @Override
    public byte[] toByteArray(String attribute) {
        ByteBuffer b = ByteBuffer.allocate(getSize());
        char[] chars = attribute.toCharArray();
        for (int i = 0; i < chars.length; i++)
            b.putChar(chars[i]);
        for (int i = chars.length; i < maxChars; i++)
            b.putChar(padding);
        return b.array();
    }

    @Override
    public String toObject(byte[] attributes, int start) {
        ByteBuffer b = ByteBuffer.wrap(attributes, start, getSize());
        char[] chars = new char[maxChars];
        for (int i = 0; i < maxChars; i++) {
            chars[i] = b.getChar();
        }
        return String.valueOf(chars);
    }

    @Override
    public boolean matches(Object obj) {
        String s;
        try {
            s = (String) type.objectClass.cast(obj);
        } catch (ClassCastException e) {
            return false;
        }
        return s.length() <= maxChars;
    }

    @Override
    public int nextIndex() {
        return getIndex() + maxChars * type.sizeInBytes;
    }

    @Override
    public String resolveToString(Object obj) {
        return "'" + String.valueOf(resolveData(obj)) + "'";
    }

    public int getMaxChars() {
        return maxChars;
    }

    @Override
    public String toString() {
        return type + " " + maxChars;
    }

    @Override
    public boolean validData(String data) {
        return data.length() <= maxChars;
    }
}
