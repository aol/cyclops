package com.aol.cyclops.collections;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Stream;

import org.pcollections.HashTreePBag;
import org.pcollections.PBag;

import com.aol.cyclops.collections.extensions.persistent.PBagX;
import com.aol.cyclops.collections.extensions.persistent.PBagXImpl;
import com.aol.cyclops.sequence.Monoid;
import com.aol.cyclops.sequence.Reducers;

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
	public static<T> PBagX<T> fromCollection(Collection<T> stream){
		if(stream instanceof PBag)
			return new PBagXImpl<>((PBag)(stream));
		return new PBagXImpl<>(HashTreePBag.from(stream));
	}
	public static<T> PBagX<T> fromStream(Stream<T> stream){
		return new PBagXImpl<>((PBag<T>)toPBag().mapReduce(stream));
	}
	public static <T> Monoid<PBag<T>> toPBag() { 
		return	Reducers.toPBag();
	}
}
