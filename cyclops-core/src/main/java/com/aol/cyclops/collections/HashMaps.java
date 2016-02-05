package com.aol.cyclops.collections;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.aol.cyclops.collections.extensions.standard.MapX;



public class HashMaps {

	public static <K, V> MapX<K, V> of() {
		return MapX.fromMap(MapX.immutableCollector(), new HashMap<>());
	}
	public static <K, V> MapX<K, V> of(K key, V value) {
		return MapX.fromMap(MapX.immutableCollector(), new Builder<K, V>(key, value).build());
	}
	public static <K, V> MapX<K, V> of(K key, V value,K key1, V value1) {
		return MapX.fromMap(MapX.immutableCollector(), new Builder<K, V>(key, value).put(key1, value1).build());
	}
	public static <K, V> MapX<K, V> of(K key, V value,K key1, V value1,K key2, V value2) {
		return MapX.fromMap(MapX.immutableCollector(), new Builder<K, V>(key, value).put(key, value, key1, value1, key2, value2).build());
	}
	public static <K, V> MapX<K, V> of(K key, V value,K key1, V value1,K key2, V value2,K key3, V value3) {
		return MapX.fromMap(MapX.immutableCollector(), new Builder<K, V>(key, value).put(key, value, key1, value1, key2, value2,key3, value3).build());
	}

	
	public static <K, V> Builder<K, V> from(Map<K,V> map) {
		return new Builder<K, V>(map);
	}
	
	public static <K, V> Builder<K, V> map(K key, V value) {
		return new Builder<K, V>(key, value);
	}
	public static <K, V> Builder<K, V> map(K key, V value,K key1, V value1) {
		return new Builder<K, V>(key, value).put(key1, value1);
	}
	public static <K, V> Builder<K, V> map(K key, V value,K key1, V value1,K key2, V value2) {
		return new Builder<K, V>(key, value).put(key, value, key1, value1, key2, value2);
	}
	public static <K, V> Builder<K, V> map(K key, V value,K key1, V value1,K key2, V value2,K key3, V value3) {
		return new Builder<K, V>(key, value).put(key, value, key1, value1, key2, value2,key3, value3);
	}

	public static final class Builder<K, V> {
		private final Map<K, V> build;

		public Builder(K key, V value) {
			build = new HashMap<K, V>();
			build.put(key, value);
		}
		public Builder(Map<K,V> map) {
			build = new HashMap<K, V>(map);
			
		}
		
		public Builder<K, V> putAll(Map<K,V> map){
			build.putAll(map);
			return this;
		}

		public Builder<K, V> put(K key, V value) {
			build.put(key, value);
			return this;
		}
		public Builder<K, V> put(K key, V value,K key1, V value1) {
			build.put(key, value);
			build.put(key1, value1);
			return this;
		}
		public Builder<K, V> put(K key, V value,K key1, V value1,K key2, V value2) {
			build.put(key, value);
			build.put(key1, value1);
			build.put(key2, value2);
			return this;
		}
		public Builder<K, V> put(K key, V value,K key1, V value1,K key2, V value2,K key3, V value3) {
			build.put(key, value);
			build.put(key1, value1);
			build.put(key2, value2);
			build.put(key3, value3);
			return this;
		}

		public MapX<K, V> build() {
			return MapX.fromMap(MapX.immutableCollector(),build);
		}
		
	}

}
