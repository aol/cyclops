package com.aol.cyclops2.data.collections.extensions.api;

import cyclops.control.Option;

import java.util.Collection;
import java.util.Set;

public interface POrderedSet<T> extends PSet<T>{
    public POrderedSet<T> plus(T e);

    public POrderedSet<T> plusAll(Iterable<? extends T> list);

    public POrderedSet<T> removeValue(T e);

    public POrderedSet<T> removeAll(Iterable<? extends T> list);

    Option<T> get(int index);

}
