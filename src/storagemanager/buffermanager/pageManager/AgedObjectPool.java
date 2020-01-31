package storagemanager.buffermanager.pageManager;

import util.Subject;
import util.Subscriber;

import java.util.ArrayList;
import java.util.Collections;

public class AgedObjectPool<E> {

    private ArrayList<AgeTracker<E>> objects = new ArrayList<>();
    private Subject<E> objectRemoval = new Subject<>();
    private final int maxSize;

    private Subscriber<AgeTracker<E>> ageTrackerSubscriber = new Subscriber<AgeTracker<E>>() {
        @Override
        protected void onUpdate(AgeTracker<E> next) {
            Collections.sort(objects);
            int basis = objects.get(0).getAge();
            if (next.getAge() == Integer.MAX_VALUE) {
                objects.forEach(eAgeTracker -> eAgeTracker.resetAge(basis));
            }
        }
    };

    /**
     * Create a pool Pages, pages get pushed out based on usage
     * @param maxSize the max amount of objects in the pool before objects are pushed
     */
    public AgedObjectPool(int maxSize) {
        this.maxSize = maxSize;
    }

    private E add(AgeTracker<E> o) {
        objects.add(o);
        if (objects.size() > maxSize) {
            E removed = objects.remove(0).getObject();
            objectRemoval.push(removed);
            return removed;
        }
        return null;
    }

    /**
     * Create a tracker for this pool, the tracker is automatically added to the pool
     * @param o object to add
     * @return a new age tracker
     */
    public AgeTracker<E> createTrackerForPool(E o) {
        AgeTracker<E> newTrackedObject =  new AgeTracker<E>(o, this);
        newTrackedObject.ageSubscriber(ageTrackerSubscriber);
        add(newTrackedObject);
        return newTrackedObject;
    }

    public boolean remove(AgeTracker<E> o) {
        boolean removal = objects.remove(o);
        if (removal) objectRemoval.push(o.getObject());
        return removal;
    }

    public ArrayList<E> getObjects() {
        return new ArrayList<>() {{
            for(AgeTracker<E> ageTracker: objects)
                add(ageTracker.getObject());
        }};
    }

    public int getHighestAge() {
        return objects.get(objects.size() - 1).getAge();
    }

    public void reset() {
        objects = new ArrayList<>();
    }

    public void subscribe(Subscriber<E> removalSubscriber) {
        objectRemoval.addSubscriber(removalSubscriber);
    }

}
