package com.aol.cyclops.types;

import java.util.function.Function;
import java.util.function.Predicate;

import com.aol.cyclops.control.Matchable;
import com.aol.cyclops.control.Matchable.CheckValues;
import com.aol.cyclops.control.Maybe;

public interface FilterableFunctor<T> extends Filterable<T>, Functor<T>{
	
	FilterableFunctor<T>  filter(Predicate<? super T> fn);
	<R> FilterableFunctor<R>  map(Function<? super T,? extends R> fn);
	
	
	default <R> FilterableFunctor<R> filterMap(Function<CheckValues<T,R>,CheckValues<T,R>> case1){
		return  map(u->Matchable.of(u).mayMatch(case1))
        		.filter(Maybe::isPresent)
        		.map(Maybe::get);
    }
}
