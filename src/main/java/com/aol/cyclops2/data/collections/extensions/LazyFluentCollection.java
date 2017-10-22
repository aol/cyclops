package com.aol.cyclops2.data.collections.extensions;

import com.aol.cyclops2.types.persistent.PersistentCollection;
import cyclops.reactive.ReactiveSeq;

public interface LazyFluentCollection<T, C extends PersistentCollection<T>> {

    C get();

    ReactiveSeq<T> stream();






}
