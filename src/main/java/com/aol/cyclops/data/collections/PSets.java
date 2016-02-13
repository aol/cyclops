package com.aol.cyclops.data.collections;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Stream;

import org.pcollections.HashTreePSet;
import org.pcollections.PSet;

import com.aol.cyclops.Reducer;
import com.aol.cyclops.Reducers;
import com.aol.cyclops.data.collections.extensions.persistent.PSetXImpl;

/**
 * Convenient companion class for working with Persistent Sets
 * 
 * @author johnmcclean
 *
 */
public class PSets {
	public static <T> PSet<T> of(T...values){
		
		return HashTreePSet.from(Arrays.asList(values));
	}
	public static <T> PSet<T> empty(){
		return new PSetXImpl<>(HashTreePSet .empty());
	}
	public static <T> PSet<T> singleton(T value){
		return new PSetXImpl<>(HashTreePSet.singleton(value));
	}
	public static<T> PSet<T> fromCollection(Collection<T> stream){
		if(stream instanceof PSet)
			return (PSet)(stream);
		return HashTreePSet.from(stream);
	}
	public static<T> PSet<T> fromStream(Stream<T> stream){
		return (PSet<T>)toPSet().mapReduce(stream);
	}
	public static <T> Reducer<PSet<T>> toPSet() { 
		return	Reducers.toPSet();
	}
}
