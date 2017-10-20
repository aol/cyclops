package com.aol.cyclops2.data.collections.extensions.api;

import java.util.Collection;

public interface PBag<T> extends PCollection<T> {
    PBag<T> plus(T e);
    PBag<T> plusAll(Iterable<? extends T> list);
    PBag<T> removeValue(T e);
    PBag<T> removeAll(Iterable<? extends T> list);
}
