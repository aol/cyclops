package com.aol.cyclops2.data.collections.extensions;

import java.util.Collection;

import com.aol.cyclops2.data.collections.extensions.api.PCollection;
import cyclops.reactive.ReactiveSeq;

public interface LazyFluentCollection<T, C extends PCollection<T>> {

    C get();

    ReactiveSeq<T> stream();






}
