package com.aol.cyclops2.types.persistent;

import cyclops.control.Option;

import java.util.function.Supplier;

public interface PersistentIndexed<T> extends PersistentCollection<T> {
    Option<T> get(int index);
    T getOrElse(int index, T alt);
    T getOrElseGet(int index, Supplier<? extends T> alt);
}
