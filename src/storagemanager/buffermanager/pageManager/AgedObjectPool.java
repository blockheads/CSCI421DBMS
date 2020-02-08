package storagemanager.buffermanager.pageManager;

import storagemanager.util.Subject;
import storagemanager.util.Subscriber;

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
        for (AgeTracker<E> obj: objects) {
            if (obj.getObject().equals(o.getObject())) {
                obj.ageIncrement();
                return null;
            }
        }
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

    /**
     * Remove a page without telling anyone about it
     * @param o the page
     * @return page removed
     */
    public boolean remove(AgeTracker<E> o) {
        return objects.remove(o);
    }

    public ArrayList<E> getObjects() {
        return new ArrayList<>() {{
            for(AgeTracker<E> ageTracker: objects)
                add(ageTracker.getObject());
        }};
    }

    public int getHighestAge() {
        // if there are no ages being tracked just return 0
        if(objects.size() == 0)
            return 0;
        return objects.get(objects.size() - 1).getAge();
    }

    public void reset() {
        objects = new ArrayList<>();
    }

    public void subscribe(Subscriber<E> removalSubscriber) {
        objectRemoval.addSubscriber(removalSubscriber);
    }

}
