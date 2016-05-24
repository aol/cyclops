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
        return sequence(opts.stream()).map(s->s.toListX());
        
    }
    public static <T> Optional<ListX<T>> sequencePresent(CollectionX<Optional<T>> opts){
        Optional<ListX<T>> unwrapped = AnyM.sequence(opts.map(o->AnyM.fromOptional(o))).unwrap();
        return unwrapped;
    }
	public static <T> Optional<ReactiveSeq<T>> sequence(Stream<Optional<T>> opts){
	    return AnyM.sequence(opts.map(f->AnyM.fromOptional(f)),
                ()->AnyM.fromOptional(Optional.of(Stream.<T>empty())))
                .map(s->ReactiveSeq.fromStream(s))
                .unwrap();
		
	}
	
	public static <T,R> Optional<R> accumulatePresent(CollectionX<Optional<T>> maybes,Reducer<R> reducer){
		return sequencePresent(maybes).map(s->s.mapReduce(reducer));
	}
	public static <T,R> Optional<R> accumulatePresent(CollectionX<Optional<T>> maybes,Function<? super T, R> mapper,Semigroup<R> reducer){
		return sequencePresent(maybes).map(s->s.map(mapper).reduce(reducer.reducer()).get());
	}

}
