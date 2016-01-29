package com.aol.cyclops.collections;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Stream;

import org.pcollections.AmortizedPQueue;
import org.pcollections.HashTreePSet;
import org.pcollections.OrderedPSet;
import org.pcollections.POrderedSet;
import org.pcollections.PQueue;
import org.pcollections.PSet;

import com.aol.cyclops.collections.extensions.persistent.PSetX;
import com.aol.cyclops.collections.extensions.persistent.PSetXImpl;
import com.aol.cyclops.sequence.Monoid;
import com.aol.cyclops.sequence.Reducers;

public class PQueues {
	public static <T> PQueue<T> of(T...values){
		
		PQueue<T> result = empty();
		for(T value : values){
			result = result.plus(value);
		}
		return result;
		
	}
	public static <T> PQueue<T> empty(){
		return AmortizedPQueue.empty();
	}
	public static <T> PQueue<T> singleton(T value){
		PQueue<T> result = empty();
		result = result.plus(value);
		return result;
	}
	public static<T> PQueue<T> fromCollection(Collection<T> stream){
		if(stream instanceof PQueue)
			return (PQueue)(stream);
		return  PQueues.<T>empty().plusAll(stream);
		
	}
	public static<T> PQueue<T> fromStream(Stream<T> stream){
		return (PQueue<T>)toPQueue().mapReduce(stream);
	}
	public static <T> Monoid<PQueue<T>> toPQueue() { 
		return	Reducers.toPQueue();
	}
}
