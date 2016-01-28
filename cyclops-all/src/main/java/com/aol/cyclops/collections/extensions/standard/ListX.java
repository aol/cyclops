package com.aol.cyclops.collections.extensions.standard;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.aol.cyclops.collections.extensions.CollectionX;

public interface ListX<T> extends List<T>, MutableCollectionX<T> {
	
	public Collector<T,?,List<T>> getCollector();
	
	default <T1> CollectionX<T1> from(Collection<T1> c){
		return new ListXImpl<T1>(c.stream().collect(Collectors.toList()));
	}
	
	default <X> CollectionX<X> fromStream(Stream<X> stream){
		return new ListXImpl<>(stream.collect(Collectors.toList()));
	}
}
