package com.aol.cyclops.data.collections;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Stream;

import org.pcollections.OrderedPSet;
import org.pcollections.POrderedSet;

import com.aol.cyclops.Reducer;
import com.aol.cyclops.Reducers;

public class POrderedSets {
	public static <T> POrderedSet<T> of(T...values){
		return OrderedPSet.from(Arrays.asList(values));
	}
	public static <T> POrderedSet<T> empty(){
		return OrderedPSet.empty();
	}
	public static <T> POrderedSet<T> singleton(T value){
		return OrderedPSet.singleton(value);
	}
	public static<T> POrderedSet<T> fromCollection(Collection<T> stream){
		if(stream instanceof POrderedSet)
			return (POrderedSet)(stream);
		return OrderedPSet.from(stream);
	}
	public static<T> POrderedSet<T> toPOrderedSet(Stream<T> stream){
		return (POrderedSet<T>)toPOrderedSet().mapReduce(stream);
	}
	public static <T> Reducer<POrderedSet<T>> toPOrderedSet() { 
		return	Reducers.toPOrderedSet();
	}
}
