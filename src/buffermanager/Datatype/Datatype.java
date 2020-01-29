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

    @Override
    public String toString() {
        return type.toString();
    }
}
