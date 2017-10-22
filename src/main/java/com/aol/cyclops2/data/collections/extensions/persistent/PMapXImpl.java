package com.aol.cyclops2.data.collections.extensions.persistent;

import java.util.*;
import java.util.function.*;

import cyclops.collectionx.immutable.PersistentMapX;
import cyclops.collectionx.mutable.ListX;
import cyclops.control.Option;
import cyclops.data.tuple.Tuple2;
import com.aol.cyclops2.data.collections.extensions.api.PMap;

import com.aol.cyclops2.data.collections.extensions.CollectionX;
import cyclops.collectionx.mutable.SetX;

import lombok.AllArgsConstructor;
import lombok.experimental.Wither;
import org.reactivestreams.Subscription;

@AllArgsConstructor
public class PMapXImpl<K, V> implements PersistentMapX<K, V> {

    @Wither
    private final PMap<K, V> map;

    @Override
    public <R> R unwrap() {
        return (R)map;
    }

    /**
     * @param key
     * @param value
     * @return
     * @see org.pcollections.PMap#plus(java.lang.Object, java.lang.Object)
     */
    @Override
    public PersistentMapX<K, V> plus(final K key, final V value) {
        return withMap(map.plus(key, value));
    }

    /**
     * @param map
     * @return
     * @see org.pcollections.PMap#plusAll(java.util.Map)
     */
    @Override
    public PersistentMapX<K, V> plusAll(final PMap<? extends K, ? extends V> map) {
        return withMap(this.map.plusAll(map));
    }

    /**
     * @param key
     * @return
     * @see org.pcollections.PMap#minus(java.lang.Object)
     */
    @Override
    public PersistentMapX<K, V> minus(final K key) {
        return withMap(map.minus(key));
    }


    @Override
    public PersistentMapX<K, V> minusAll(final Iterable<? extends K> keys) {
        return withMap(map.minusAll(keys));
    }

    /**
     * @param k
     * @param v
     * @return
     * @deprecated
     * @see org.pcollections.PMap#put(java.lang.Object, java.lang.Object)
     */
    @Deprecated
    @Override
    public V put(final K k, final V v) {
        return v;
    }

    /**
     * @param k
     * @return
     * @deprecated
     * @see org.pcollections.PMap#remove(java.lang.Object)
     */
    @Deprecated
    @Override
    public V remove(final Object k) {
        return null;
    }

    /**
     * @param m
     * @deprecated
     * @see org.pcollections.PMap#putAll(java.util.Map)
     */
    @Deprecated
    @Override
    public void putAll(final Map<? extends K, ? extends V> m) {

    }

    /**
     * @deprecated
     * @see org.pcollections.PMap#clear()
     */
    @Deprecated
    @Override
    public void clear(){
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
     * @param value
     * @return
     * @see java.util.Map#containsValue(java.lang.Object)
     */
    @Override
    public boolean containsValue(final Object value) {
        return map.stream().filter(t2-> Objects.equals(t2._2(),value)).findFirst().isPresent();
    }

    /**
     * @param key
     * @return
     * @see java.util.Map#get(java.lang.Object)
     */
    @Override
    public V get(final Object key) {

        return map.getValueOrElse((K)key,null);
    }

    /**
     * @return
     * @see java.util.Map#keySet()
     */
    @Override
    public SetX<K> keySet() {
        return SetX.fromIterable(map.stream().map(t2-> t2._1()));
    }

    /**
     * @return
     * @see java.util.Map#values()
     */
    @Override
    public CollectionX<V> values() {
        return ListX.fromIterable(map.stream().map(t2->t2._2()));
    }

    /**
     * @return
     * @see java.util.Map#entrySet()
     */
    @Override
    public Set<java.util.Map.Entry<K, V>> entrySet() {
        Set<java.util.Map.Entry<K, V>> x = map.stream()
                .map(t2 -> (java.util.Map.Entry<K, V>)new AbstractMap.SimpleEntry<>(t2._1(), t2._2())).toSet();
        return x;
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

    /**
     * @param key
     * @param defaultValue
     * @return
     * @see java.util.Map#getOrDefault(java.lang.Object, java.lang.Object)
     */
    @Override
    public V getOrDefault(final Object key, final V defaultValue) {
        return map.getValueOrElse((K)key, defaultValue);
    }

    /**
     * @param action
     * @see java.util.Map#forEach(java.util.function.BiConsumer)
     */
    @Override
    public void forEach(final BiConsumer<? super K, ? super V> action) {
        map.stream().forEach(t2->action.accept(t2._1(),t2._2()));
    }

    /**
     * @param function
     * @see java.util.Map#replaceAll(java.util.function.BiFunction)
     */
    @Override
    @Deprecated
    public void replaceAll(final BiFunction<? super K, ? super V, ? extends V> function) {

    }

    /**
     * @param key
     * @param value
     * @return
     * @see java.util.Map#putIfAbsent(java.lang.Object, java.lang.Object)
     */
    @Override
    @Deprecated
    public V putIfAbsent(final K key, final V value) {
        return value;
    }

    /**
     * @param key
     * @param value
     * @return
     * @see java.util.Map#remove(java.lang.Object, java.lang.Object)
     */
    @Override
    @Deprecated
    public boolean remove(final Object key, final Object value) {
        return false;
    }

    /**
     * @param key
     * @param oldValue
     * @param newValue
     * @return
     * @see java.util.Map#replace(java.lang.Object, java.lang.Object, java.lang.Object)
     */
    @Override
    @Deprecated
    public boolean replace(final K key, final V oldValue, final V newValue) {
        return false;
    }

    /**
     * @param key
     * @param value
     * @return
     * @see java.util.Map#replace(java.lang.Object, java.lang.Object)
     */
    @Override
    @Deprecated
    public V replace(final K key, final V value) {
        return value;
    }

    /**
     * @param key
     * @param mappingFunction
     * @return
     * @see java.util.Map#computeIfAbsent(java.lang.Object, java.util.function.Function)
     */
    @Override
    @Deprecated
    public V computeIfAbsent(final K key, final Function<? super K, ? extends V> mappingFunction) {
        return mappingFunction.apply(key);
    }

    /**
     * @param key
     * @param remappingFunction
     * @return
     * @see java.util.Map#computeIfPresent(java.lang.Object, java.util.function.BiFunction)
     */
    @Override
    @Deprecated
    public V computeIfPresent(final K key, final BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return remappingFunction.apply(key, getValueOrElse(key,null));
    }

    /**
     * @param key
     * @param remappingFunction
     * @return
     * @see java.util.Map#compute(java.lang.Object, java.util.function.BiFunction)
     */
    @Override
    @Deprecated
    public V compute(final K key, final BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return remappingFunction.apply(key, getValueOrElse(key,null));
    }

    /**
     * @param key
     * @param value
     * @param remappingFunction
     * @return
     * @see java.util.Map#merge(java.lang.Object, java.lang.Object, java.util.function.BiFunction)
     */
    @Override
    @Deprecated
    public V merge(final K key, final V value, final BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        return value;
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
    public Option<V> getValue(K key) {
        return map.getValue(key);
    }

    @Override
    public V getValueOrElse(K key, V alt) {
        return map.getValueOrElse(key,alt);
    }

    @Override
    public V getValueOrElseGet(K key, Supplier<? extends V> alt) {
        return map.getValueOrElseGet(key,alt);
    }

    @Override
    public String toString() {
        return "PersistentMapX" + map.toString() + "";
    }
}
