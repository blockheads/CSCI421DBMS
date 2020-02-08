package storagemanager.util;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * An observable data type
 * @param <E> the underling datatype
 */
public class Subject<E> {

    private boolean closed = false;
    private E currentSubject = null;

    private Set<Subscriber<E>> subscribers = Collections.synchronizedSet(new HashSet<Subscriber<E>>());

    /**
     * Create a subject with a value of null
     */
    public Subject() {}

    /**
     * Create a subject and initialize it with a value
     * @param init the value to start at
     */
    public Subject(E init) {
        this.currentSubject = init;
    }

    /**
     * Add a subscriber to receive updates
     * @param subscriber a subscriber to receive push updates
     * @return true if added
     */
    public boolean addSubscriber(Subscriber<E> subscriber) {
        synchronized (this) {
            if (closed) return false;
            return subscribers.add(subscriber);
        }
    }

    /**
     * Push an update to all subscribers
     * @param update the newest element of the subject
     */
    public synchronized void push(E update) {
        if (closed) return;
        synchronized (this) {
            if (closed) return;
            currentSubject = update;
            for (Subscriber<E> subscriber : subscribers) {
                subscriber.onUpdate(update);
            }
        }
    }

    /**
     * Close the subject permanently
     */
    public void closeSubject() {
        closed = true;
        subscribers = null; // let garbage collector remove references for me
    }

    public boolean isClosed() {
        return closed;
    }

    public synchronized E getCurrentSubject() {
        return currentSubject;
    }
}
