package storagemanager.buffermanager.datatypes;

/**
 * Represent data that does not change in size
 *
 * @author Nicholas Chieppa
 */
public abstract class StaticData<E> extends Datatype<E> {
    protected StaticData(ValidDataTypes type) {
        super(type);
    }

    @Override
    public int getSize() {
        return type.sizeInBytes;
    }

    @Override
    public boolean matches(Object obj) {
        try {
            type.objectClass.cast(obj);
        } catch (ClassCastException e) {
            return false;
        }
        return true;
    }
}
