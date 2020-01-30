package buffermanager.Datatype;

import java.io.Serializable;

public abstract class Datatype<E> implements Serializable {

    protected final ValidDataTypes type;

    protected Datatype(ValidDataTypes type) {
        this.type = type;
    }

    public abstract int getSize();

    public ValidDataTypes getType() {
        return type;
    }

    public abstract E resolveData();

    public int compareObjects(Object obj1, Object obj2) {
        return type.comparator.compare(obj1, obj2);
    }

    @Override
    public String toString() {
        return type.toString();
    }
}
