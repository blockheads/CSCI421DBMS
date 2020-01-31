package storagemanager.buffermanager.pageManager;

import storagemanager.buffermanager.page.Page;

import java.util.TreeSet;

public class AgingTreeSet<E> extends TreeSet<E> {

    private final AgedObjectPool<E> objectPool;

    public AgingTreeSet(AgedObjectPool<E> objectPool) {
        this.objectPool = objectPool;
    }

    @Override
    public boolean add(E o) {
        remove(objectPool.add(o));
        return super.add(o);
    }

    @Override
    public boolean remove(Object o) {
        return super.remove(o);
    }
}
