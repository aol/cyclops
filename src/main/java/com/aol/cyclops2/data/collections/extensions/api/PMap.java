package com.aol.cyclops2.data.collections.extensions.api;

import cyclops.control.Option;
import cyclops.data.tuple.Tuple2;
import cyclops.reactive.ReactiveSeq;

import java.util.Collection;
import java.util.Map;
import java.util.function.Supplier;

public interface PMap<K,V> extends Iterable<Tuple2<K,V>> {
    PMap<K,V> plus(K key, V value);
    PMap<K,V> plusAll(PMap<? extends K, ? extends V> map);
    PMap<K,V> minus(K key);
    PMap<K,V> minusAll(Iterable<? extends K> keys);


    Option<V> getValue(K key);
    V getValueOrElse(K key, V alt);
    V getValueOrElseGet(K key, Supplier<? extends V> alt);

    int size();

    boolean containsKey(K key);

    default boolean isEmpty(){
        return size()==0;
    }

    default ReactiveSeq<Tuple2<K,V>> stream(){

        return ReactiveSeq.fromIterable(this);
    }


}
