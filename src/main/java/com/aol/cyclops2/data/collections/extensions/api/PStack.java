package com.aol.cyclops2.data.collections.extensions.api;

import java.util.Collection;

public interface PStack<T> extends PIndexed<T> {
    PStack<T> plus(T e);


    PStack<T> plusAll(Iterable<? extends T> list);


    PStack<T> updateAt(int i, T e);


    PStack<T> insertAt(int i, T e);


    PStack<T> insertAt(int i, Iterable<? extends T> list);


    PStack<T> removeValue(T e);


    PStack<T> removeAll(Iterable<? extends T> list);


    PStack<T> removeAt(int i);


//    PStack<T> subList(int start, int end);
//    PStack<T> subList(int start);


}
