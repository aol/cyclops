package com.aol.cyclops.collections.extensions.persistent;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.jooq.lambda.Collectable;
import org.jooq.lambda.tuple.Tuple2;
import org.pcollections.PBag;
import org.pcollections.PMap;

import com.aol.cyclops.collections.extensions.CollectionX;
import com.aol.cyclops.collections.extensions.standard.SetX;
import com.aol.cyclops.lambda.types.Traversable;

import lombok.AllArgsConstructor;
import lombok.experimental.Wither;
@AllArgsConstructor
public class PMapXImpl<K,V> implements PMapX<K,V> {

	@Wither
	private final PMap<K,V> map;

	/**
	 * @param key
	 * @param value
	 * @return
	 * @see org.pcollections.PMap#plus(java.lang.Object, java.lang.Object)
	 */
	public PMapX<K, V> plus(K key, V value) {
		return withMap(map.plus(key, value));
	}

	/**
	 * @param map
	 * @return
	 * @see org.pcollections.PMap#plusAll(java.util.Map)
	 */
	public PMapX<K, V> plusAll(Map<? extends K, ? extends V> map) {
		return withMap(this.map.plusAll(map));
	}

	/**
	 * @param key
	 * @return
	 * @see org.pcollections.PMap#minus(java.lang.Object)
	 */
	public PMapX<K, V> minus(Object key) {
		return withMap(map.minus(key));
	}

	/**
	 * @param keys
	 * @return
	 * @see org.pcollections.PMap#minusAll(java.util.Collection)
	 */
	public PMapX<K, V> minusAll(Collection<?> keys) {
		return withMap(map.minusAll(keys));
	}

	/**
	 * @param k
	 * @param v
	 * @return
	 * @deprecated
	 * @see org.pcollections.PMap#put(java.lang.Object, java.lang.Object)
	 */
	public V put(K k, V v) {
		return map.put(k, v);
	}

	/**
	 * @param k
	 * @return
	 * @deprecated
	 * @see org.pcollections.PMap#remove(java.lang.Object)
	 */
	public V remove(Object k) {
		return map.remove(k);
	}

	/**
	 * @param m
	 * @deprecated
	 * @see org.pcollections.PMap#putAll(java.util.Map)
	 */
	public void putAll(Map<? extends K, ? extends V> m) {
		map.putAll(m);
	}

	/**
	 * @deprecated
	 * @see org.pcollections.PMap#clear()
	 */
	public void clear() {
		map.clear();
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
	 * @return
	 * @see java.util.Map#keySet()
	 */
	public SetX<K> keySet() {
		return SetX.fromIterable(map.keySet());
	}

	/**
	 * @return
	 * @see java.util.Map#values()
	 */
	public CollectionX<V> values() {
		return CollectionX.fromCollection(map.values());
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
	public V getOrDefault(Object key, V defaultValue) {
		return map.getOrDefault(key, defaultValue);
	}

	/**
	 * @param action
	 * @see java.util.Map#forEach(java.util.function.BiConsumer)
	 */
	public  void forEach(BiConsumer<? super K, ? super V> action) {
		map.forEach(action);
	}

	/**
	 * @param function
	 * @see java.util.Map#replaceAll(java.util.function.BiFunction)
	 */
	@Deprecated
	public  void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
		map.replaceAll(function);
	}

	/**
	 * @param key
	 * @param value
	 * @return
	 * @see java.util.Map#putIfAbsent(java.lang.Object, java.lang.Object)
	 */
	@Deprecated
	public  V putIfAbsent(K key, V value) {
		return map.putIfAbsent(key, value);
	}

	/**
	 * @param key
	 * @param value
	 * @return
	 * @see java.util.Map#remove(java.lang.Object, java.lang.Object)
	 */
	@Deprecated
	public boolean remove(Object key, Object value) {
		return map.remove(key, value);
	}

	/**
	 * @param key
	 * @param oldValue
	 * @param newValue
	 * @return
	 * @see java.util.Map#replace(java.lang.Object, java.lang.Object, java.lang.Object)
	 */
	@Deprecated
	public boolean replace(K key, V oldValue, V newValue) {
		return map.replace(key, oldValue, newValue);
	}

	/**
	 * @param key
	 * @param value
	 * @return
	 * @see java.util.Map#replace(java.lang.Object, java.lang.Object)
	 */
	@Deprecated
	public  V replace(K key, V value) {
		return map.replace(key, value);
	}

	/**
	 * @param key
	 * @param mappingFunction
	 * @return
	 * @see java.util.Map#computeIfAbsent(java.lang.Object, java.util.function.Function)
	 */
	@Deprecated
	public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
		return map.computeIfAbsent(key, mappingFunction);
	}

	/**
	 * @param key
	 * @param remappingFunction
	 * @return
	 * @see java.util.Map#computeIfPresent(java.lang.Object, java.util.function.BiFunction)
	 */
	@Deprecated
	public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
		return map.computeIfPresent(key, remappingFunction);
	}

	/**
	 * @param key
	 * @param remappingFunction
	 * @return
	 * @see java.util.Map#compute(java.lang.Object, java.util.function.BiFunction)
	 */
	@Deprecated
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
	@Deprecated
	public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
		return map.merge(key, value, remappingFunction);
	}

	

	

	
	
}
