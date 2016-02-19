package com.aol.cyclops.data.collections;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.jooq.lambda.tuple.Tuple2;
import org.pcollections.HashTreePMap;
import org.pcollections.PBag;
import org.pcollections.PMap;

import com.aol.cyclops.Monoid;
import com.aol.cyclops.Reducer;
import com.aol.cyclops.Reducers;



public class PMaps {

	public static <K, V> PMap<K, V> of() {
		return HashTreePMap.from(new HashMap<>());
	}
	public static <K, V> PMap<K, V> of(K key, V value) {
		return HashTreePMap.from(new Builder<K, V>(key, value).build());
	}
	public static <K, V> PMap<K, V> of(K key, V value,K key1, V value1) {
		return HashTreePMap.from(new Builder<K, V>(key, value).put(key1, value1).build());
	}
	public static <K, V> PMap<K, V> of(K key, V value,K key1, V value1,K key2, V value2) {
		return HashTreePMap.from(new Builder<K, V>(key, value).put(key, value, key1, value1, key2, value2).build());
	}
	public static <K, V> PMap<K, V> of(K key, V value,K key1, V value1,K key2, V value2,K key3, V value3) {
		return HashTreePMap.from(new Builder<K, V>(key, value).put(key, value, key1, value1, key2, value2,key3, value3).build());
	}

	public static<K,V> PMap<K,V> toPMap(Stream<Tuple2<K,V>> stream){
		return (PMap<K,V>)toPMap().mapReduce(stream);
	}
	public static <K,V> Reducer<PMap<K,V>> toPMap() { 
		return	Reducers.toPMap();
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

		public PMap<K, V> build() {
			return HashTreePMap.from(build);
		}
	}

}
