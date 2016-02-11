package com.aol.cyclops.lambda.monads;

import java.util.function.Function;
import java.util.function.Predicate;

public interface FilterableFunctor<T> extends Filterable<T>, Functor<T>{
	
	FilterableFunctor<T>  filter(Predicate<? super T> fn);
	<R> FilterableFunctor<R>  map(Function<? super T,? extends R> fn);
	
	
	
}
