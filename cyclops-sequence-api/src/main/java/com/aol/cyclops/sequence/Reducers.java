package com.aol.cyclops.sequence;

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
	
	public static Monoid<String> toString(String joiner){
		return Monoid.of("", (a,b) -> a + joiner +b);
	}
	public static Monoid<Integer> toTotalInt(){
		return Monoid.of(0, (a,b) -> a+b);
	}
	public static Monoid<Integer> toCountInt(){
		
		return Monoid.of(0, a ->b -> a+1,(x) -> Integer.valueOf(""+x));
	}
	
	public static Monoid<Double> toTotalDouble(){
		return Monoid.of(0.0, (a,b) -> a+b);
	}
	public static Monoid<Double> toCountDouble(){
		return Monoid.of(0.0, a->b -> a+1,(x) -> Double.valueOf(""+x));
	}
	
}
