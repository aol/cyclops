package com.aol.cyclops.data.collections.extensions;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Stream;

import cyclops.stream.ReactiveSeq;

public interface LazyFluentCollection<T, C extends Collection<T>> {//extends Collection<T> {

    C get();

    ReactiveSeq<T> stream();






}
