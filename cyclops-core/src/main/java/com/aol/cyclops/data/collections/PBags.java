package com.aol.cyclops.data.collections;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Stream;

import org.pcollections.HashTreePBag;
import org.pcollections.PBag;

import com.aol.cyclops.Reducer;
import com.aol.cyclops.Reducers;

public class PBags {
	
	public static<T> PBag<T> of(T...values){
		
		
		return HashTreePBag.from(Arrays.asList(values));
	}
	
	public static<T> PBag<T> empty(){
		return HashTreePBag .empty();
	}
	public static<T> PBag<T> singleton(T value){
		return HashTreePBag.singleton(value);
	}
	public static<T> PBag<T> fromCollection(Collection<T> stream){
		if(stream instanceof PBag)
			return (PBag)(stream);
		return HashTreePBag.from(stream);
	}
	public static<T> PBag<T> fromStream(Stream<T> stream){
		return (PBag<T>)toPBag().mapReduce(stream);
	}
	public static <T> Reducer<PBag<T>> toPBag() { 
		return	Reducers.toPBag();
	}
}
