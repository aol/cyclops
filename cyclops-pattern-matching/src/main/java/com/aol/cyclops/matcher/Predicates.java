package com.aol.cyclops.matcher;

import java.util.function.Predicate;

public class Predicates {

	public static <T> Predicate<T> p(Predicate<T> p){
		return p;
	}
}
