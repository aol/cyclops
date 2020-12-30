package com.oath.cyclops.types.persistent;

import com.oath.cyclops.types.persistent.views.MapView;
import cyclops.control.Option;
import cyclops.data.tuple.Tuple2;
import cyclops.reactive.ReactiveSeq;

import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;


public interface PersistentMap<K,V> extends Iterable<Tuple2<K,V>> {

    PersistentMap<K,V> put(K key, V value);
    default PersistentMap<K,V> put(Tuple2<K,V> keyAndValue){
      return put(keyAndValue._1(),keyAndValue._2());
    }
    PersistentMap<K,V> putAll(PersistentMap<? extends K, ? extends V> map);
    PersistentMap<K,V> remove(K key);
    PersistentMap<K,V> removeAllKeys(Iterable<? extends K> keys);


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

    default  boolean allMatch(Predicate<? super Tuple2<K,V>> c){
        return !stream().filterNot(c)
            .findFirst()
            .isPresent();
    }
    default boolean equalTo(PersistentMap<K,V> map){
      if(size()!=map.size())
        return false;
        return allMatch(map::contains);

    }


    default boolean contains(Tuple2<K, V> t) {
        return get(t._1()).filter(v-> Objects.equals(v,t._2())).isPresent();
    }
    default MapView<K,V> mapView(){
      return new MapView.Impl<>(this);
    }


}
