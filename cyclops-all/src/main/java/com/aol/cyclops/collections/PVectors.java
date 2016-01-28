package com.aol.cyclops.collections;

import java.util.Arrays;
import java.util.stream.Stream;

import org.pcollections.PVector;
import org.pcollections.TreePVector;

import com.aol.cyclops.sequence.Monoid;
import com.aol.cyclops.sequence.Reducers;

public class PVectors {

	public<T> PVector<T> of(T...values){
		return TreePVector.from(Arrays.asList(values));
	}
	public<T> PVector<T> empty(){
		return TreePVector .empty();
	}
	public<T> PVector<T> singleton(T value){
		return TreePVector.singleton(value);
	}
	public static<T> PVector<T> toPVector(Stream<T> stream){
		return (PVector<T>)toPVector().mapReduce(stream);
	}
	public static <T> Monoid<PVector<T>> toPVector() { 
		return	Reducers.toPVector();
	}
}
