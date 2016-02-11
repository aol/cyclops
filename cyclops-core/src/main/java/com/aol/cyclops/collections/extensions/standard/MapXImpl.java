package com.aol.cyclops.collections.extensions.standard;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collector;

import org.jooq.lambda.tuple.Tuple2;

import com.aol.cyclops.lambda.types.IterableFunctor;
import com.aol.cyclops.sequence.SequenceM;

import lombok.AllArgsConstructor;
import lombok.Getter;
@AllArgsConstructor
public class MapXImpl<K,V> implements MapX<K,V>{
	private final Map<K,V> map;
	@Getter
	private final Collector<Tuple2<? extends K,? extends V>,?,Map<K,V>> collector;
	public MapXImpl(Map< K,V> map){
		this.map = map;
		this.collector = MapX.defaultCollector();
	}
	public MapXImpl(){
		this.collector = MapX.defaultCollector();
		this.map = (Map)this.collector.supplier().get();
	}
	/**
	 * @return
	 * @see java.util.Map#size()
	 */
	public int size() {
		return map.size();
	}
	/**
	 * @return
	 * @see java.util.Map#isEmpty()
	 */
	public boolean isEmpty() {
		return map.isEmpty();
	}
	/**
	 * @param key
	 * @return
	 * @see java.util.Map#containsKey(java.lang.Object)
	 */
	public boolean containsKey(Object key) {
		return map.containsKey(key);
	}
	/**
	 * @param value
	 * @return
	 * @see java.util.Map#containsValue(java.lang.Object)
	 */
	public boolean containsValue(Object value) {
		return map.containsValue(value);
	}
	/**
	 * @param key
	 * @return
	 * @see java.util.Map#get(java.lang.Object)
	 */
	public V get(Object key) {
		return map.get(key);
	}
	/**
	 * @param key
	 * @param value
	 * @return
	 * @see java.util.Map#put(java.lang.Object, java.lang.Object)
	 */
	public V put(K key, V value) {
		return map.put(key, value);
	}
	/**
	 * @param key
	 * @return
	 * @see java.util.Map#remove(java.lang.Object)
	 */
	public V remove(Object key) {
		return map.remove(key);
	}
	/**
	 * @param m
	 * @see java.util.Map#putAll(java.util.Map)
	 */
	public void putAll(Map<? extends K, ? extends V> m) {
		map.putAll(m);
	}
	/**
	 * 
	 * @see java.util.Map#clear()
	 */
	public void clear() {
		map.clear();
	}
	/**
	 * @return
	 * @see java.util.Map#keySet()
	 */
	public Set<K> keySet() {
		return map.keySet();
	}
	/**
	 * @return
	 * @see java.util.Map#values()
	 */
	public Collection<V> values() {
		return map.values();
	}
	/**
	 * @return
	 * @see java.util.Map#entrySet()
	 */
	public Set<java.util.Map.Entry<K, V>> entrySet() {
		return map.entrySet();
	}
	/**
	 * @param o
	 * @return
	 * @see java.util.Map#equals(java.lang.Object)
	 */
	public boolean equals(Object o) {
		return map.equals(o);
	}
	/**
	 * @return
	 * @see java.util.Map#hashCode()
	 */
	public int hashCode() {
		return map.hashCode();
	}
	/**
	 * @param key
	 * @param defaultValue
	 * @return
	 * @see java.util.Map#getOrDefault(java.lang.Object, java.lang.Object)
	 */
	public  V getOrDefault(Object key, V defaultValue) {
		return map.getOrDefault(key, defaultValue);
	}
	/**
	 * @param action
	 * @see java.util.Map#forEach(java.util.function.BiConsumer)
	 */
	public void forEach(BiConsumer<? super K, ? super V> action) {
		map.forEach(action);
	}
	/**
	 * @param function
	 * @see java.util.Map#replaceAll(java.util.function.BiFunction)
	 */
	public  void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
		map.replaceAll(function);
	}
	/**
	 * @param key
	 * @param value
	 * @return
	 * @see java.util.Map#putIfAbsent(java.lang.Object, java.lang.Object)
	 */
	public V putIfAbsent(K key, V value) {
		return map.putIfAbsent(key, value);
	}
	/**
	 * @param key
	 * @param value
	 * @return
	 * @see java.util.Map#remove(java.lang.Object, java.lang.Object)
	 */
	public  boolean remove(Object key, Object value) {
		return map.remove(key, value);
	}
	/**
	 * @param key
	 * @param oldValue
	 * @param newValue
	 * @return
	 * @see java.util.Map#replace(java.lang.Object, java.lang.Object, java.lang.Object)
	 */
	public  boolean replace(K key, V oldValue, V newValue) {
		return map.replace(key, oldValue, newValue);
	}
	/**
	 * @param key
	 * @param value
	 * @return
	 * @see java.util.Map#replace(java.lang.Object, java.lang.Object)
	 */
	public  V replace(K key, V value) {
		return map.replace(key, value);
	}
	/**
	 * @param key
	 * @param mappingFunction
	 * @return
	 * @see java.util.Map#computeIfAbsent(java.lang.Object, java.util.function.Function)
	 */
	public  V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
		return map.computeIfAbsent(key, mappingFunction);
	}
	/**
	 * @param key
	 * @param remappingFunction
	 * @return
	 * @see java.util.Map#computeIfPresent(java.lang.Object, java.util.function.BiFunction)
	 */
	public  V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
		return map.computeIfPresent(key, remappingFunction);
	}
	/**
	 * @param key
	 * @param remappingFunction
	 * @return
	 * @see java.util.Map#compute(java.lang.Object, java.util.function.BiFunction)
	 */
	public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
		return map.compute(key, remappingFunction);
	}
	/**
	 * @param key
	 * @param value
	 * @param remappingFunction
	 * @return
	 * @see java.util.Map#merge(java.lang.Object, java.lang.Object, java.util.function.BiFunction)
	 */
	public  V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
		return map.merge(key, value, remappingFunction);
	}
	
	
}
