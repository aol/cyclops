package com.aol.cyclops.util;

import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import com.aol.cyclops.Reducer;
import com.aol.cyclops.Semigroup;
import com.aol.cyclops.data.collections.extensions.CollectionX;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.Maybe;
import com.aol.cyclops.control.ReactiveSeq;

public class Optionals {

    public static <T> Optional<ListX<T>> sequence(CollectionX<Optional<T>> opts){
        return sequence(opts).map(s->s.toListX());
        
    }
	public static <T> Optional<ReactiveSeq<T>> sequence(Stream<Optional<T>> opts){
	    return AnyM.genericSequence(opts.map(f->AnyM.fromOptional(f)),
                ()->AnyM.fromOptional(Optional.of(Stream.<T>empty())))
                .map(s->ReactiveSeq.fromStream(s))
                .unwrap();
		
	}
	
	public static <T,R> Optional<R> accumulatePresent(CollectionX<Optional<T>> maybes,Reducer<R> reducer){
		return sequence(maybes).map(s->s.mapReduce(reducer));
	}
	public static <T,R> Optional<R> accumulatePresent(CollectionX<Optional<T>> maybes,Function<? super T, R> mapper,Semigroup<R> reducer){
		return sequence(maybes).map(s->s.map(mapper).reduce(reducer.reducer()).get());
	}

}
