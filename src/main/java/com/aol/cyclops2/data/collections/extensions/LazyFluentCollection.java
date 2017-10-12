package com.aol.cyclops2.data.collections.extensions;

import java.util.Collection;

import cyclops.reactive.ReactiveSeq;

public interface LazyFluentCollection<T, C extends Collection<T>> {

    C get();

    ReactiveSeq<T> stream();






}
