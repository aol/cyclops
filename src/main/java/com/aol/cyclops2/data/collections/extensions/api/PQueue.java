package com.aol.cyclops2.data.collections.extensions.api;

import java.util.Collection;

public interface PQueue<T> extends PIndexed<T>{

    PQueue<T> minus();
    PQueue<T> plus(T e);
    PQueue<T> plusAll(Iterable<? extends T> list);


    public PQueue<T> removeValue(T e);
    public PQueue<T> removeAll(Iterable<? extends T> list);




}
