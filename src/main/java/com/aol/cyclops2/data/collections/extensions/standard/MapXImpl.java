package com.aol.cyclops2.data.collections.extensions.standard;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collector;

import cyclops.collections.mutable.MapX;
import org.jooq.lambda.tuple.Tuple2;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.reactivestreams.Subscription;

@AllArgsConstructor
public class MapXImpl<K, V> implements MapX<K, V> {
    private final Map<K, V> map;
    @Getter
    private final Collector<Tuple2<? extends K, ? extends V>, ?, Map<K, V>> collector;

    public MapXImpl(final Map<K, V> map) {
        this.map = map;
        this.collector = MapX.defaultCollector();
    }

    public MapXImpl() {
        this.collector = MapX.defaultCollector();
        this.map = (Map) this.collector.supplier()
                                       .get();
    }

    @Override
    public <R> R unwrap() {
        return (R)map;
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
     * @param key
     * @param value
     * @return
     * @see java.util.Map#put(java.lang.Object, java.lang.Object)
     */
    @Override
    public V put(final K key, final V value) {
        return map.put(key, value);
    }

    /**
     * @param key
     * @return
     * @see java.util.Map#remove(java.lang.Object)
     */
    @Override
    public V remove(final Object key) {
        return map.remove(key);
    }

    /**
     * @param m
     * @see java.util.Map#putAll(java.util.Map)
     */
    @Override
    public void putAll(final Map<? extends K, ? extends V> m) {
        map.putAll(m);
    }

    /**
     * 
     * @see java.util.Map#clear()
     */
    @Override
    public void clear() {
        map.clear();
    }

    /**
     * @return
     * @see java.util.Map#keySet()
     */
    @Override
    public Set<K> keySet() {
        return map.keySet();
    }

    /**
     * @return
     * @see java.util.Map#values()
     */
    @Override
    public Collection<V> values() {
        return map.values();
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
    public V merge(final K key, final V value, final BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        return map.merge(key, value, remappingFunction);
    }

    @Override
    public <X extends Throwable> Subscription forEach(long numberOfElements, Consumer<? super Tuple2<K, V>> consumer) {
        return stream().forEach(numberOfElements,consumer);
    }

    @Override
    public <X extends Throwable> Subscription forEach(long numberOfElements, Consumer<? super Tuple2<K, V>> consumer, Consumer<? super Throwable> consumerError) {
        return stream().forEach(numberOfElements,consumer,consumerError);
    }

    @Override
    public <X extends Throwable> Subscription forEach(long numberOfElements, Consumer<? super Tuple2<K, V>> consumer, Consumer<? super Throwable> consumerError, Runnable onComplete) {
        return stream().forEach(numberOfElements,consumer,consumerError,onComplete);
    }
    @Override
    public <X extends Throwable> Subscription forEachSubscribe(Consumer<? super Tuple2<K, V>> consumer) {
        return stream().forEachSubscribe(consumer);
    }

    @Override
    public <X extends Throwable> Subscription forEachSubscribe(Consumer<? super Tuple2<K, V>> consumer, Consumer<? super Throwable> consumerError) {
        return stream().forEachSubscribe(consumer,consumerError);
    }

    @Override
    public <X extends Throwable> Subscription forEachSubscribe(Consumer<? super Tuple2<K, V>> consumer, Consumer<? super Throwable> consumerError, Runnable onComplete) {
        return stream().forEachSubscribe(consumer,consumerError,onComplete);
    }

    @Override
    public <X extends Throwable> void forEach(Consumer<? super Tuple2<K, V>> consumerElement, Consumer<? super Throwable> consumerError) {
        stream().forEach(consumerElement,consumerError);
    }

    @Override
    public <X extends Throwable> void forEach(Consumer<? super Tuple2<K, V>> consumerElement, Consumer<? super Throwable> consumerError, Runnable onComplete) {
            stream().forEach(consumerElement,consumerError,onComplete);
    }

    @Override
    public String toString() {
        return "MapX[" + map.toString() + "]";
    }
}
