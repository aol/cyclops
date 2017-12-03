package com.oath.cyclops.data.collections.extensions.persistent;

import java.util.*;
import java.util.function.*;

import com.oath.cyclops.types.persistent.PersistentMap;
import cyclops.collections.immutable.PersistentMapX;
import cyclops.control.Option;
import cyclops.data.tuple.Tuple2;

import lombok.AllArgsConstructor;
import lombok.experimental.Wither;
import org.reactivestreams.Subscription;

@AllArgsConstructor
public class PMapXImpl<K, V> implements PersistentMapX<K, V> {

    @Wither
    private final PersistentMap<K, V> map;

    @Override
    public <R> R unwrap() {
        return (R)map;
    }

    /**
     * @param key
     * @param value
     * @return
     * @see PersistentMap#put(java.lang.Object, java.lang.Object)
     */
    @Override
    public PersistentMapX<K, V> put(final K key, final V value) {
        return withMap(map.put(key, value));
    }

    @Override
    public PersistentMap<K, V> put(Tuple2<K, V> keyAndValue) {
        return map.put(keyAndValue);
    }

    /**
     * @param map
     * @return
     * @see PersistentMap#putAll(PersistentMap)
     */
    @Override
    public PersistentMapX<K, V> putAll(final PersistentMap<? extends K, ? extends V> map) {
        return withMap(this.map.putAll(map));
    }

    /**
     * @param key
     * @return
     * @see PersistentMap#remove(K)
     */
    @Override
    public PersistentMapX<K, V> remove(final K key) {
        return withMap(map.remove(key));
    }


    @Override
    public PersistentMapX<K, V> removeAllKeys(final Iterable<? extends K> keys) {
        return withMap(map.removeAllKeys(keys));
    }



    /**
     * @return
     * @see java.util.Map#size()
     */
    @Override
    public int size() {
        return map.size();
    }

    /**
     * @return
     * @see java.util.Map#isEmpty()
     */
    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public Iterator<Tuple2<K, V>> iterator() {
        return this.map.iterator();
    }

    /**
     * @param key
     * @return
     * @see java.util.Map#containsKey(java.lang.Object)
     */
    @Override
    public boolean containsKey(final Object key) {
        return map.containsKey((K)key);
    }


    /**
     * @param o
     * @return
     * @see java.util.Map#equals(java.lang.Object)
     */
    @Override
    public boolean equals(final Object o) {
        if(o instanceof Map){
            Map m = (Map)o;
            return m.equals(this.toMap(t2-> t2._1(),t2->t2._2()));
        }
        return map.equals(o);
    }

    /**
     * @return
     * @see java.util.Map#hashCode()
     */
    @Override
    public int hashCode() {
        return map.hashCode();
    }



    @Override
    public <X extends Throwable> Subscription forEach(long numberOfElements, Consumer<? super Tuple2<K, V>> consumer) {
        return this.stream().forEach(numberOfElements, consumer);
    }

    @Override
    public <X extends Throwable> Subscription forEach(long numberOfElements, Consumer<? super Tuple2<K, V>> consumer, Consumer<? super Throwable> consumerError) {
        return this.stream().forEach(numberOfElements,consumer,consumerError);
    }

    @Override
    public <X extends Throwable> Subscription forEach(long numberOfElements, Consumer<? super Tuple2<K, V>> consumer, Consumer<? super Throwable> consumerError, Runnable onComplete) {
        return this.stream().forEach(numberOfElements,consumer,consumerError,onComplete);
    }

    @Override
    public <X extends Throwable> Subscription forEachSubscribe(Consumer<? super Tuple2<K, V>> consumer) {
        return this.stream().forEachSubscribe(consumer);
    }

    @Override
    public <X extends Throwable> Subscription forEachSubscribe(Consumer<? super Tuple2<K, V>> consumer, Consumer<? super Throwable> consumerError) {
        return this.stream().forEachSubscribe(consumer,consumerError);
    }

    @Override
    public <X extends Throwable> Subscription forEachSubscribe(Consumer<? super Tuple2<K, V>> consumer, Consumer<? super Throwable> consumerError, Runnable onComplete) {
        return this.stream().forEachSubscribe(consumer,consumerError,onComplete);
    }

    @Override
    public <X extends Throwable> void forEach(Consumer<? super Tuple2<K, V>> consumerElement, Consumer<? super Throwable> consumerError) {
        stream().forEach(t->{
            consumerElement.accept(t);
        },e->consumerError.accept(e));

    }

    @Override
    public <X extends Throwable> void forEach(Consumer<? super Tuple2<K, V>> consumerElement, Consumer<? super Throwable> consumerError, Runnable onComplete) {
        stream().forEach(t->{
            consumerElement.accept(t);
        },consumerError,onComplete);

    }

    @Override
    public Option<V> get(K key) {
        return map.get(key);
    }

    @Override
    public V getOrElse(K key, V alt) {
        return map.getOrElse(key,alt);
    }

    @Override
    public V getOrElseGet(K key, Supplier<? extends V> alt) {
        return map.getOrElseGet(key,alt);
    }

    @Override
    public String toString() {
        return "PersistentMapX" + map.toString() + "";
    }
}
