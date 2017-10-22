package com.aol.cyclops2.types.persistent;

public interface PersistentQueue<T> extends PersistentIndexed<T> {

    PersistentQueue<T> minus();
    PersistentQueue<T> plus(T e);
    PersistentQueue<T> plusAll(Iterable<? extends T> list);


    public PersistentQueue<T> removeValue(T e);
    public PersistentQueue<T> removeAll(Iterable<? extends T> list);




}
