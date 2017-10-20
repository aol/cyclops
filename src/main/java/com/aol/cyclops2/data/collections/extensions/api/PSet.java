package com.aol.cyclops2.data.collections.extensions.api;

import com.aol.cyclops2.types.traversable.IterableX;
import cyclops.reactive.ReactiveSeq;

import java.util.Collection;
import java.util.Set;

public interface PSet<T>  extends PCollection<T>, Iterable<T>{//}, Set<T> {

    PSet<T> plus(T e);
    PSet<T> plusAll(Iterable<? extends T> list);

    PSet<T> removeValue(T e);

    PSet<T> removeAll(Iterable<? extends T> list);

    default ReactiveSeq<T> stream(){
        return ReactiveSeq.fromIterable(this);
    }

}
