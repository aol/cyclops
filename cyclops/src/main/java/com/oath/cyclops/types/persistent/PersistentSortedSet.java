package com.oath.cyclops.types.persistent;

import cyclops.control.Option;

public interface PersistentSortedSet<T> extends PersistentSet<T> {
    public PersistentSortedSet<T> plus(T e);

    public PersistentSortedSet<T> plusAll(Iterable<? extends T> list);

    public PersistentSortedSet<T> removeValue(T e);

    public PersistentSortedSet<T> removeAll(Iterable<? extends T> list);

    Option<T> get(int index);

}
