package com.aol.cyclops.lambda.monads;

import java.util.Iterator;
import java.util.function.Function;

public interface IterableFunctor<T> extends Iterable<T>,Functor<T>{

	<U> IterableFunctor<U> unitIterator(Iterator<U> U);
	<R> IterableFunctor<R>  map(Function<? super T,? extends R> fn);
}
