package com.aol.cyclops.matcher;

import java.util.function.Predicate;

public class Predicates {

	public static <T> Predicate<T> p(Predicate<T> p){
		return p;
	}

	private static final Predicate ANY_INSTANCE = test ->true;
	public static final <Y> Predicate<Y> ANY(){  return ANY_INSTANCE; };
	public static final <Y> Predicate<Y> ANY(Class c){  return a -> a.getClass().isAssignableFrom(c); };
}
