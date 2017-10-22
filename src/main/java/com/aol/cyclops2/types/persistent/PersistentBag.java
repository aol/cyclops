package com.aol.cyclops2.types.persistent;

public interface PersistentBag<T> extends PersistentCollection<T> {
    PersistentBag<T> plus(T e);
    PersistentBag<T> plusAll(Iterable<? extends T> list);
    PersistentBag<T> removeValue(T e);
    PersistentBag<T> removeAll(Iterable<? extends T> list);
}
