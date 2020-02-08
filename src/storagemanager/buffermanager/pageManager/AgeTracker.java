package storagemanager.buffermanager.pageManager;

import storagemanager.util.Subject;
import storagemanager.util.Subscriber;

import java.util.Comparator;

/**
 * Tracks an objects age. Higher aged objects were used more recently.
 * @param <E> object to track
 *
 * @author Nicholas Chieppa
 */
public class AgeTracker<E> implements Comparable<AgeTracker<E>>, Comparator<AgeTracker<E>> {

    private final E object;
    private int age;
    private final AgedObjectPool<E> pool;

    private final Subject<AgeTracker<E>> ageIncrement = new Subject<>();

    public AgeTracker(E object, AgedObjectPool<E> pool) {
        this.object = object;
        this.pool = pool;
        this.age = pool.getHighestAge();
    }

    public void resetAge(int basis) {
        this.age %= basis;
    }

    public boolean ageIncrement() {
        age++;
        ageIncrement.push(this);
        return age == Integer.MAX_VALUE;
    }

    public E getObject() {
        return object;
    }

    @Override
    public int compareTo(AgeTracker<E> o) {
        return age - o.age;
    }

    public int getAge() {
        return age;
    }

    public boolean objectEquals(E obj) {
        return object.equals(obj);
    }

    @Override
    public int compare(AgeTracker<E> o1, AgeTracker<E> o2) {
        return o1.age - o2.age;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AgeTracker)
            return ((AgeTracker) obj).object.equals(object);
        return false;
    }

    @Override
    public int hashCode() {
        return object.hashCode();
    }

    public void ageSubscriber(Subscriber<AgeTracker<E>> ageTrackerSubscriber) {
        ageIncrement.addSubscriber(ageTrackerSubscriber);
    }
}
