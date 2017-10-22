package com.aol.cyclops2.data.collections.extensions.api;

import cyclops.control.Option;

import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

public interface PIndexed<T> extends PCollection<T>{//, List<T> {
    Option<T> get(int index);
    T getOrElse(int index, T alt);
    T getOrElseGet(int index, Supplier<? extends T> alt);


}
