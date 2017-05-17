package com.aol.cyclops2.data.collections.extensions.persistent;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import cyclops.collections.immutable.PMapX;
import org.jooq.lambda.tuple.Tuple2;
import org.pcollections.PMap;

import com.aol.cyclops2.data.collections.extensions.CollectionX;
import cyclops.collections.SetX;

import lombok.AllArgsConstructor;
import lombok.experimental.Wither;
import org.reactivestreams.Subscription;

@AllArgsConstructor
public class PMapXImpl<K, V> implements PMapX<K, V> {

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
    public PMapX<K, V> plus(final K key, final V value) {
        return withMap(map.plus(key, value));
    }

    /**
     * @param map
     * @return
     * @see org.pcollections.PMap#plusAll(java.util.Map)
     */
    @Override
    public PMapX<K, V> plusAll(final Map<? extends K, ? extends V> map) {
        return withMap(this.map.plusAll(map));
    }

    /**
     * @param key
     * @return
     * @see org.pcollections.PMap#minus(java.lang.Object)
     */
    @Override
    public PMapX<K, V> minus(final Object key) {
        return withMap(map.minus(key));
    }

    /**
     * @param keys
     * @return
     * @see org.pcollections.PMap#minusAll(java.util.Collection)
     */
    @Override
    public PMapX<K, V> minusAll(final Collection<?> keys) {
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
        return map.put(k, v);
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
        return map.remove(k);
    }

    /**
     * @param m
     * @deprecated
     * @see org.pcollections.PMap#putAll(java.util.Map)
     */
    @Deprecated
    @Override
    public void putAll(final Map<? extends K, ? extends V> m) {
        map.putAll(m);
    }

    /**
     * @deprecated
     * @see org.pcollections.PMap#clear()
     */
    @Deprecated
    @Override
    public void clear() {
        map.clear();
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
        return map.containsKey(key);
    }

    /**
     * @param value
     * @return
     * @see java.util.Map#containsValue(java.lang.Object)
     */
    @Override
    public boolean containsValue(final Object value) {
        return map.containsValue(value);
    }

    /**
     * @param key
     * @return
     * @see java.util.Map#get(java.lang.Object)
     */
    @Override
    public V get(final Object key) {
        return map.get(key);
    }

    /**
     * @return
     * @see java.util.Map#keySet()
     */
    @Override
    public SetX<K> keySet() {
        return SetX.fromIterable(map.keySet());
    }

    /**
     * @return
     * @see java.util.Map#values()
     */
    @Override
    public CollectionX<V> values() {
        return CollectionX.fromCollection(map.values());
    }

    /**
     * @return
     * @see java.util.Map#entrySet()
     */
    @Override
    public Set<java.util.Map.Entry<K, V>> entrySet() {
        return map.entrySet();
    }

    /**
     * @param o
     * @return
     * @see java.util.Map#equals(java.lang.Object)
     */
    @Override
    public boolean equals(final Object o) {
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
        return map.getOrDefault(key, defaultValue);
    }

    /**
     * @param action
     * @see java.util.Map#forEach(java.util.function.BiConsumer)
     */
    @Override
    public void forEach(final BiConsumer<? super K, ? super V> action) {
        map.forEach(action);
    }

    /**
     * @param function
     * @see java.util.Map#replaceAll(java.util.function.BiFunction)
     */
    @Override
    @Deprecated
    public void replaceAll(final BiFunction<? super K, ? super V, ? extends V> function) {
        map.replaceAll(function);
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
        return map.putIfAbsent(key, value);
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
        return map.remove(key, value);
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
        return map.replace(key, oldValue, newValue);
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
        return map.replace(key, value);
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
        return map.computeIfAbsent(key, mappingFunction);
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
        return map.computeIfPresent(key, remappingFunction);
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
        return map.compute(key, remappingFunction);
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
        return map.merge(key, value, remappingFunction);
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
    public <X extends Throwable> Subscription subscribe(Consumer<? super Tuple2<K, V>> consumer) {
        return this.stream().subscribe(consumer);
    }

    @Override
    public <X extends Throwable> Subscription subscribe(Consumer<? super Tuple2<K, V>> consumer, Consumer<? super Throwable> consumerError) {
        return this.stream().subscribe(consumer,consumerError);
    }

    @Override
    public <X extends Throwable> Subscription subscribe(Consumer<? super Tuple2<K, V>> consumer, Consumer<? super Throwable> consumerError, Runnable onComplete) {
        return this.stream().subscribe(consumer,consumerError,onComplete);
    }

    @Override
    public <X extends Throwable> void forEach(Consumer<? super Tuple2<K, V>> consumerElement, Consumer<? super Throwable> consumerError) {

    }

    @Override
    public <X extends Throwable> void forEach(Consumer<? super Tuple2<K, V>> consumerElement, Consumer<? super Throwable> consumerError, Runnable onComplete) {

    }
}
