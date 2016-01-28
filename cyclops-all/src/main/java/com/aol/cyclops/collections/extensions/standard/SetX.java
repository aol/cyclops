package com.aol.cyclops.collections.extensions.standard;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.aol.cyclops.collections.extensions.CollectionX;

public interface SetX<T> extends Set<T>, MutableCollectionX<T> {
	
	public Collector<T,?,Set<T>> getCollector();
	
	default <T1> CollectionX<T1> from(Collection<T1> c){
		return new SetXImpl<T1>(c.stream().collect(Collectors.toSet()));
	}
	
	default <X> CollectionX<X> fromStream(Stream<X> stream){
		return new SetXImpl<>(stream.collect(Collectors.toSet()));
	}
}
