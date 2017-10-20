package com.aol.cyclops2.data.collections.extensions.api;

import cyclops.control.Option;

import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

public interface PIndexed<T> extends PCollection<T>{//, List<T> {
    Option<T> get(int index);
    T getOrElse(int index, T alt);
    T getOrElseGet(int index, Supplier<? extends T> alt);

    /**
    @Deprecated
    default T set(int index, T value){
        return value;
    }

    @Deprecated
    default boolean addAll(int index, Collection<? extends T> c){
        return false;
    }

    @Deprecated
    default void add(int index, T element){

    }
    @Deprecated
    default T removeValue(int index){
        return getValue(index);
    }
    **/
}
