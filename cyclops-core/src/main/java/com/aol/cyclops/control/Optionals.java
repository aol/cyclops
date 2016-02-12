package com.aol.cyclops.control;

import java.util.Optional;
import java.util.function.Function;

import com.aol.cyclops.Reducer;
import com.aol.cyclops.Semigroup;
import com.aol.cyclops.data.collections.extensions.CollectionX;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.monad.AnyM;

public class Optionals {

	public static <T> Optional<ListX<T>> sequence(CollectionX<Optional<T>> opts){
		return AnyM.sequence(AnyM.<T>listFromOptional(opts)).unwrap();
	}
	
	public static <T,R> Optional<R> accumulatePresent(CollectionX<Optional<T>> maybes,Reducer<R> reducer){
		return sequence(maybes).map(s->s.mapReduce(reducer));
	}
	public static <T,R> Optional<R> accumulatePresent(CollectionX<Optional<T>> maybes,Function<? super T, R> mapper,Semigroup<R> reducer){
		return sequence(maybes).map(s->s.map(mapper).reduce(reducer.reducer()).get());
	}

}
