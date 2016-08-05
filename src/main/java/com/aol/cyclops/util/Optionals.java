package com.aol.cyclops.util;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

import org.reactivestreams.Publisher;

import com.aol.cyclops.Reducer;
import com.aol.cyclops.Semigroup;
import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.Maybe;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.data.collections.extensions.CollectionX;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.types.Value;

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
	  public static <T1,T2,R> Optional<R> combine(Optional<? extends T1> f, Value<? extends T2> v, BiFunction<? super T1,? super T2,? extends R> fn){
	        return narrow(Maybe.fromOptional(f).combine(v, fn).toOptional());
	    }
	    public static <T1,T2,R> Optional<R> zip(Optional<? extends T1> f, Iterable<? extends T2> v, BiFunction<? super T1,? super T2,? extends R> fn){
	        return narrow(Maybe.fromOptional(f).zip(v, fn).toOptional());
	    }
	    public static <T1,T2,R> Optional<R> zip(Publisher<? extends T2> p,Optional<? extends T1> f,  BiFunction<? super T1,? super T2,? extends R> fn){
	        return narrow(Maybe.fromOptional(f).zip(fn,p).toOptional());
	    }

	    public static <T> Optional<T> narrow(Optional<? extends T> f){
	        return (Optional<T>)f;
	    }

}
