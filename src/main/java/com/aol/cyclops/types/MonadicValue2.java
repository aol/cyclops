package com.aol.cyclops.types;

import java.util.function.Function;

import org.reactivestreams.Publisher;

import com.aol.cyclops.Monoid;
import com.aol.cyclops.control.Xor;
import com.aol.cyclops.types.stream.reactive.ValueSubscriber;

/**
 * Represents a MonadicValue that stores a single element that can be one of two
 * data types. (E.g. Xor<T1,T2> or Try<T1,Exception>
 * 
 * @author johnmcclean
 *
 * @param <T1> 1st allowable data type for element
 * @param <T2> 2nd allowable data type for element
 */
public interface MonadicValue2<T1, T2> extends MonadicValue<T2> {
    /**
     * Perform a flattening transformation of this Monadicvalue2
     * 
     * @param mapper
     *            transformation function
     * @return Transformed MonadicValue2
     */
    <R2> MonadicValue2<T1, R2> flatMap(Function<? super T2, ? extends MonadicValue2<? extends T1, ? extends R2>> mapper);

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
     * @param monoid Monoid to be used to combine values
     * @param v2 MonadicValue to combine with
     * @return Combined MonadicValue
     */
    default MonadicValue2<T1, T2> combineEager(final Monoid<T2> monoid, final MonadicValue2<? extends T1, ? extends T2> v2) {
        return unit(this.<T2> flatMap(t1 -> v2.map(t2 -> monoid
                                                                   .apply(t1, t2)))
                        .orElseGet(() -> orElseGet(() -> monoid.zero())));
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.aol.cyclops.types.MonadicValue#map(java.util.function.Function)
     */
    @Override
    <R> MonadicValue2<T1, R> map(Function<? super T2, ? extends R> fn);

    /*
     * (non-Javadoc)
     * 
     * @see com.aol.cyclops.types.MonadicValue#unit(java.lang.Object)
     */
    @Override
    <T2> MonadicValue2<T1, T2> unit(T2 unit);

    /**
     * A flattening transformation operation that takes the first value from the returned Publisher.
     * <pre>
     * {@code 
     *   Xor.primary(1).map(i->i+2).flatMapPublisher(i->Arrays.asList(()->i*3,20);
     *   //Xor[9]
     * 
     * }</pre>
     * 
     *
     * @param mapper  transformation function
     * @return MonadicValue2 the first element returned after the flatMap function is applied
     */

    default <R> MonadicValue2<T1, R> flatMapIterable(final Function<? super T2, ? extends Iterable<? extends R>> mapper) {
        return this.flatMap(a -> {
            return Xor.fromIterable(mapper.apply(a));

        });
    }

    /**
    
     * A flattening transformation operation that takes the first value from the returned Publisher.
     * <pre>
     * {@code 
     *   Ior.primary(1).map(i->i+2).flatMapPublisher(i->Flux.just(()->i*3,20);
     *   //Xor[9]
     * 
     * }</pre>
     * @param mapper FlatMap  transformation function
     * @return MonadicValue2 subscribed from publisher after the flatMap function is applied
     */
    default <R> MonadicValue2<T1, R> flatMapPublisher(final Function<? super T2, ? extends Publisher<? extends R>> mapper) {
        return this.flatMap(a -> {
            final Publisher<? extends R> publisher = mapper.apply(a);
            final ValueSubscriber<R> sub = ValueSubscriber.subscriber();

            publisher.subscribe(sub);
            return unit(sub.get());
        });
    }
}
