package com.aol.cyclops.collections;

import java.util.Arrays;
import java.util.stream.Stream;

import org.pcollections.OrderedPSet;
import org.pcollections.POrderedSet;
import org.pcollections.PSet;

import com.aol.cyclops.sequence.Monoid;
import com.aol.cyclops.sequence.Reducers;

public class OrderedPSets {
	public static <T> POrderedSet<T> of(T...values){
		return OrderedPSet.from(Arrays.asList(values));
	}
	public static <T> POrderedSet<T> empty(){
		return OrderedPSet.empty();
	}
	public static <T> POrderedSet<T> singleton(T value){
		return OrderedPSet.singleton(value);
	}
	public static<T> POrderedSet<T> toPOrderedSet(Stream<T> stream){
		return (POrderedSet<T>)toPOrderedSet().mapReduce(stream);
	}
	public static <T> Monoid<POrderedSet<T>> toPOrderedSet() { 
		return	Reducers.toPOrderedSet();
	}
}
