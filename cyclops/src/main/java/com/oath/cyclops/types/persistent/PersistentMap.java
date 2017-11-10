package com.oath.cyclops.types.persistent;

import com.oath.cyclops.types.persistent.views.MapView;
import cyclops.control.Option;
import cyclops.data.tuple.Tuple2;
import cyclops.reactive.ReactiveSeq;

import java.util.Iterator;
import java.util.Objects;
import java.util.function.Supplier;

public interface PersistentMap<K,V> extends Iterable<Tuple2<K,V>> {

    PersistentMap<K,V> put(K key, V value);
    default PersistentMap<K,V> put(Tuple2<K,V> keyAndValue){
      return put(keyAndValue._1(),keyAndValue._2());
    }
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

    default boolean equalTo(PersistentMap<K,V> map){
      if(size()!=map.size())
        return false;
      Iterator<Tuple2<K,V>> iterator = iterator();
      while(iterator.hasNext()){
        Tuple2<K, V> t2 = iterator.next();
        if(!Objects.equals(map.getOrElse(t2._1(),null),t2._2())){
          return false;
        }

      }
      return true;
    }

    default MapView<K,V> mapView(){
      return new MapView.Impl<>(this);
    }


}
