package storagemanager.buffermanager.datatypes;

import java.io.Serializable;

public abstract class Datatype<E> implements Serializable {

    protected final ValidDataTypes type;
    private int index;

    protected Datatype(ValidDataTypes type) {
        this.type = type;
    }

    public abstract int getSize();

    public ValidDataTypes getType() {
        return type;
    }

    public E resolveData(Object obj) {
        try {
            return (E) type.objectClass.cast(obj);
        } catch (ClassCastException e) {
            e.printStackTrace();
            System.exit(1);
        }
        return null;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    public int nextIndex() {
        return index + type.sizeInBytes;
    }

    public abstract byte[] toByteArray(E attribute);

    public abstract E toObject(byte[] attributes, int start);

    public abstract boolean matches(Object obj);

    public String resolveToString(Object obj) {
        return resolveData(obj).toString();
    }

    public int compareObjects(Object obj1, Object obj2) {
        return type.comparator.compare(obj1, obj2);
    }

    @Override
    public String toString() {
        return type.toString();
    }
}
