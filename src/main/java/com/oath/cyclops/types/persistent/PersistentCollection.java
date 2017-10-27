package com.oath.cyclops.types.persistent;

import cyclops.reactive.ReactiveSeq;

import java.util.Objects;

public interface PersistentCollection<T> extends Iterable<T>{

     PersistentCollection<T> plus(T e);

     PersistentCollection<T> plusAll(Iterable<? extends T> list);


     PersistentCollection<T> removeValue(T e);

     int size();

     PersistentCollection<T> removeAll(Iterable<? extends T> list);

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
