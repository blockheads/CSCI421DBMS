package buffermanager.Datatype;

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

    public abstract E resolveData();
}
