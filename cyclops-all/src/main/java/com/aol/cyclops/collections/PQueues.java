package com.aol.cyclops.collections;

import java.util.Arrays;
import java.util.stream.Stream;

import org.pcollections.AmortizedPQueue;
import org.pcollections.OrderedPSet;
import org.pcollections.POrderedSet;
import org.pcollections.PQueue;

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
	public static<T> PQueue<T> toPQueue(Stream<T> stream){
		return (PQueue<T>)toPQueue().mapReduce(stream);
	}
	public static <T> Monoid<PQueue<T>> toPQueue() { 
		return	Reducers.toPQueue();
	}
}
