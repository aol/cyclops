package com.aol.cyclops.types;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.aol.cyclops.util.stream.StreamUtils;

public interface IterableFilterable<T> extends Filterable<T> {
	default Filterable<T> removeAll(Stream<T> stream){
		Set<T> set= stream.collect(Collectors.toSet());
		return filterNot(i-> set.contains(i));
	}
	default  Filterable<T> removeAll(Iterable<T> it){
		return removeAll(StreamUtils.stream(it));
	}
	
	default  Filterable<T> removeAll(T... values){
		return removeAll(Stream.of(values));
		
	}
	default  Filterable<T> retainAll(Iterable<T> it){
		return retainAll(StreamUtils.stream(it));
	}

	default  Filterable<T> retainAll(Stream<T> stream){
		Set<T> set= stream.collect(Collectors.toSet());
		return filter(i-> set.contains(i));
	}
	default  Filterable<T> retainAll(T... values){
		return retainAll(Stream.of(values));
	}
	
}
