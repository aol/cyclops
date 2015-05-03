package com.aol.cyclops.comprehensions;

import java.util.function.Function;
import java.util.function.Predicate;

public interface Comprehender<T> {

	public Object filter(T t, Predicate p);
	public Object map(T t, Function fn);
	public T flatMap(T t, Function fn);
	
}
