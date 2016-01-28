package com.aol.cyclops.collections;

import java.util.Arrays;
import java.util.stream.Stream;

import org.pcollections.HashTreePSet;
import org.pcollections.PSet;

import com.aol.cyclops.collections.extensions.MapPSetX;
import com.aol.cyclops.collections.extensions.SetX;
import com.aol.cyclops.sequence.Monoid;
import com.aol.cyclops.sequence.Reducers;

/**
 * Convenient companion class for working with Persistent Sets
 * 
 * @author johnmcclean
 *
 */
public class PSets {
	public static <T> SetX<T> of(T...values){
		
		return new MapPSetX<>(HashTreePSet.from(Arrays.asList(values)));
	}
	public static <T> SetX<T> empty(){
		return new MapPSetX<>(HashTreePSet .empty());
	}
	public static <T> SetX<T> singleton(T value){
		return new MapPSetX<>(HashTreePSet.singleton(value));
	}
	public static<T> SetX<T> toPSet(Stream<T> stream){
		return new MapPSetX<>((PSet<T>)toPSet().mapReduce(stream));
	}
	public static <T> Monoid<PSet<T>> toPSet() { 
		return	Reducers.toPSet();
	}
}
