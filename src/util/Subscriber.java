package util;

/**
 * A subscriber receives updates from 0 to many subjects
 * @param <E> the datatype that will be received on an update
 */
public abstract class Subscriber<E> {

    /**
     * A value to be pushed into the subscriber by the subject
     * @param next the value that is received
     */
    protected abstract void onUpdate(E next);
}
