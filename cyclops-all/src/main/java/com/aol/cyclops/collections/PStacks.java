package com.aol.cyclops.collections;

import java.util.Arrays;
import java.util.stream.Stream;

import org.pcollections.ConsPStack;
import org.pcollections.PStack;

import com.aol.cyclops.sequence.Monoid;
import com.aol.cyclops.sequence.Reducers;

public class PStacks {

	public static <T> PStack<T> of(T...values){
		return ConsPStack.from(Arrays.asList(values));
	}
	public static <T> PStack<T> empty(){
		return ConsPStack.empty();
	}
	public static <T> PStack<T> singleton(T value){
		return ConsPStack.singleton(value);
	}
	public static<T> PStack<T> toPStack(Stream<T> stream){
		return (PStack<T>)toPStack().mapReduce(stream);
	}
	public static <T> Monoid<PStack<T>> toPStack() { 
		return	Reducers.toPStack();
	}
}
