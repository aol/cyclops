package com.aol.cyclops.types;

import java.util.Iterator;
import java.util.function.Function;

import org.reactivestreams.Publisher;

import com.aol.cyclops.Monoid;
import com.aol.cyclops.types.stream.reactive.ValueSubscriber;

public interface MonadicValue2<T1, T2> extends MonadicValue<T2> {
    /**
     * Perform a flattening transformation of this Monadicvalue2
     * 
     * @param mapper
     *            transformation function
     * @return Transformed MonadicValue2
     */
    <R1, R2> MonadicValue2<R1, R2> flatMap(Function<? super T2, ? extends MonadicValue2<? extends R1, ? extends R2>> mapper);

    /**
     * Eagerly combine two MonadicValues using the supplied monoid
     * 
     * <pre>
     * {@code 
     * 
     *  Monoid<Integer> add = Mondoid.of(1,Semigroups.intSum);
     *  Maybe.of(10).combineEager(add,Maybe.none());
     *  //Maybe[10]
     *  
     *  Maybe.none().combineEager(add,Maybe.of(10));
     *  //Maybe[10]
     *  
     *  Maybe.none().combineEager(add,Maybe.none());
     *  //Maybe.none()
     *  
     *  Maybe.of(10).combineEager(add,Maybe.of(10));
     *  //Maybe[20]
     *  
     *  Monoid
    <Integer> firstNonNull = Monoid.of(null , Semigroups.firstNonNull());
     *  Maybe.of(10).combineEager(firstNonNull,Maybe.of(10));
     *  //Maybe[10]
     * }
     * 
     * @param monoid
     * @param v2
     * @return
     */
    default MonadicValue2<T1, T2> combineEager(Monoid<T2> monoid, MonadicValue2<? extends T1, ? extends T2> v2) {
        return unit(this.<T1, T2> flatMap(t1 -> v2.map(t2 -> monoid.combiner()
                                                                   .apply(t1, t2)))
                        .orElseGet(() -> this.orElseGet(() -> monoid.zero())));
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.aol.cyclops.types.MonadicValue#map(java.util.function.Function)
     */
    <R> MonadicValue2<T1, R> map(Function<? super T2, ? extends R> fn);

    /*
     * (non-Javadoc)
     * 
     * @see com.aol.cyclops.types.MonadicValue#unit(java.lang.Object)
     */
    <T2> MonadicValue2<T1, T2> unit(T2 unit);
    
	default <R> MonadicValue2<T1, R> flatMapIterable(Function<? super T2, ? extends Iterable<? extends R>> mapper) {
		return this.flatMap(a -> {
			Iterator<? extends R> it = mapper.apply(a).iterator();
			if (it.hasNext()) {
				R r = it.next();
				return unit(r);
			} else {
				return null;
			}
		});
	}

	default <R> MonadicValue2<T1, R> flatMapPublisher(Function<? super T2, ? extends Publisher<? extends R>> mapper) {
		return this.flatMap(a -> {
			Publisher<? extends R> publisher = mapper.apply(a);
			ValueSubscriber<R> sub = ValueSubscriber.subscriber();
			publisher.subscribe(sub);
			return unit(sub.get());
		});
	}
}
