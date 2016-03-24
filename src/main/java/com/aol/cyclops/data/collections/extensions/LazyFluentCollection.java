package com.aol.cyclops.data.collections.extensions;

import java.util.Collection;

import com.aol.cyclops.control.ReactiveSeq;

public interface LazyFluentCollection<T,C extends Collection<T>> {
    
    C get();
    
    ReactiveSeq<T> stream();
}
