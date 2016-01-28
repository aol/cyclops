package com.aol.cyclops.collections;

import java.util.Arrays;
import java.util.stream.Stream;

import org.pcollections.HashTreePSet;
import org.pcollections.PSet;

import com.aol.cyclops.sequence.Monoid;
import com.aol.cyclops.sequence.Reducers;

public class PSets {
	public static <T> PSet<T> of(T...values){
		return HashTreePSet.from(Arrays.asList(values));
	}
	public static <T> PSet<T> empty(){
		return HashTreePSet .empty();
	}
	public static <T> PSet<T> singleton(T value){
		return HashTreePSet.singleton(value);
	}
	public static<T> PSet<T> toPSet(Stream<T> stream){
		return (PSet<T>)toPSet().mapReduce(stream);
	}
	public static <T> Monoid<PSet<T>> toPSet() { 
		return	Reducers.toPSet();
	}
}
