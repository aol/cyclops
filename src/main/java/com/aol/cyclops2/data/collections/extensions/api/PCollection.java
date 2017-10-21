package com.aol.cyclops2.data.collections.extensions.api;

import cyclops.reactive.ReactiveSeq;

import java.util.Collection;
import java.util.Objects;

public interface PCollection<T> extends Iterable<T>{

     PCollection<T> plus(T e);

     PCollection<T> plusAll(Iterable<? extends T> list);


     PCollection<T> removeValue(T e);

     int size();

     PCollection<T> removeAll(Iterable<? extends T> list);

     default boolean isEmpty(){
         return size()==0;
     }

     default boolean containsValue(T item){
         return stream().filter(t-> Objects.equals(t,item)).findFirst().isPresent();
     }

     default ReactiveSeq<T> stream(){
         return ReactiveSeq.fromIterable(this);
     }


}
