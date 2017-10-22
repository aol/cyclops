package com.aol.cyclops2.types.persistent;

import cyclops.control.Option;
import cyclops.data.tuple.Tuple2;
import cyclops.reactive.ReactiveSeq;

import java.util.function.Supplier;

public interface PersistentMap<K,V> extends Iterable<Tuple2<K,V>> {

    PersistentMap<K,V> put(K key, V value);
    PersistentMap<K,V> put(Tuple2<K,V> keyAndValue);
    PersistentMap<K,V> putAll(PersistentMap<? extends K, ? extends V> map);
    PersistentMap<K,V> remove(K key);
    PersistentMap<K,V> removeAll(Iterable<? extends K> keys);


    Option<V> get(K key);
    V getOrElse(K key, V alt);
    V getOrElseGet(K key, Supplier<? extends V> alt);

    int size();

    boolean containsKey(K key);

    default boolean isEmpty(){
        return size()==0;
    }

    default ReactiveSeq<Tuple2<K,V>> stream(){

        return ReactiveSeq.fromIterable(this);
    }


}
