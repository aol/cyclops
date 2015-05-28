package com.aol.cyclops.lambda.api;

import org.pcollections.ConsPStack;
import org.pcollections.HashTreePMap;
import org.pcollections.PMap;
import org.pcollections.PStack;

public class Reducers {
	
	public static <T> Monoid<PStack<T>> toPStack() { 
		return	Monoid.<PStack<T>>of(ConsPStack.empty(), 
								(PStack<T> a) -> b -> a.plusAll(b),
								(T x) -> ConsPStack.singleton(x));
	}
	public static <K,V> Monoid<PMap<K,V>> toPMap() { 
		return	Monoid.<PMap<K,V>>of(HashTreePMap.empty(), 
								(PMap<K,V> a) -> b -> a.plusAll(b));
	}
}
