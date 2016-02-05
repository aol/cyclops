package com.aol.cyclops.lambda.monads;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import com.aol.cyclops.matcher.Case;
import com.aol.cyclops.matcher.Cases;

public interface FilterableFunctor<T> extends Filterable<T>, Functor<T>{
	
	FilterableFunctor<T>  filter(Predicate<? super T> fn);
	<R> FilterableFunctor<R>  map(Function<? super T,? extends R> fn);
	
	default <R> Functor<R> whenPresent(Case<T,R,Function<T,R>>... cases){
		return map(t->Cases.of(cases).<R>match(t))
				.filter(Optional::isPresent)
				.map(Optional::get);
	}
	
}
