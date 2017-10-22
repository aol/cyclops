package com.aol.cyclops2.types.persistent;

public interface PersistentList<T> extends PersistentIndexed<T> {
    PersistentList<T> plus(T e);


    PersistentList<T> plusAll(Iterable<? extends T> list);


    PersistentList<T> updateAt(int i, T e);


    PersistentList<T> insertAt(int i, T e);


    PersistentList<T> insertAt(int i, Iterable<? extends T> list);


    PersistentList<T> removeValue(T e);


    PersistentList<T> removeAll(Iterable<? extends T> list);


    PersistentList<T> removeAt(int i);


}
