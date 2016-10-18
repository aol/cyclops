package com.aol.cyclops.types;

import java.util.function.Function;

import org.reactivestreams.Publisher;

import com.aol.cyclops.Monoid;
import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.Maybe;
import com.aol.cyclops.types.anyM.AnyMValue;
import com.aol.cyclops.types.stream.reactive.ValueSubscriber;

/**
 * Represents a MonadicValue that can have a single data type 
 *   (for example a Maybe, Eval as opposed to an Xor or Either type which may have a value that
 *   is of one of two data types).
 * 
 * @author johnmcclean
 *
 * @param <T> Data type of element stored inside this MonadicValue1
 */
public interface MonadicValue1<T> extends MonadicValue<T> {

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.MonadicValue#unit(java.lang.Object)
     */
    @Override
    public <T> MonadicValue1<T> unit(T unit);

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.MonadicValue#map(java.util.function.Function)
     */
    @Override
    <R> MonadicValue<R> map(Function<? super T, ? extends R> fn);

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.MonadicValue#anyM()
     */
    @Override
    default AnyMValue<T> anyM() {
        return AnyM.ofValue(this);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.MonadicValue#coflatMap(java.util.function.Function)
     */
    @Override
    default <R> MonadicValue<R> coflatMap(final Function<? super MonadicValue<T>, R> mapper) {
        return mapper.andThen(r -> unit(r))
                     .apply(this);
    }

    /**
     * Eagerly combine two MonadicValues using the supplied monoid (@see ApplicativeFunctor for type appropraite i.e. lazy / async alternatives)
     * 
     * <pre>
     * {@code 
     * 
     *  Monoid<Integer> add = Monoid.of(1,Semigroups.intSum);
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
     *  Monoid<Integer> firstNonNull = Monoid.of(null , Semigroups.firstNonNull());
     *  Maybe.of(10).combineEager(firstNonNull,Maybe.of(10));
     *  //Maybe[10]
     * }</pre>
     * 
     * @param monoid
     * @param v2
     * @return
     */
    default MonadicValue1<T> combineEager(final Monoid<T> monoid, final MonadicValue<? extends T> v2) {
        return unit(this.<T> flatMap(t1 -> v2.map(t2 -> monoid.combiner()
                                                              .apply(t1, t2)))
                        .orElseGet(() -> orElseGet(() -> monoid.zero())));
    }

    /**
     * A flattening transformation operation (@see {@link java.util.Optional#flatMap(Function)}
     * 
     * <pre>
     * {@code 
     *   Eval.now(1).map(i->i+2).flatMap(i->Eval.later(()->i*3);
     *   //Eval[9]
     * 
     * }</pre>
     * 
     * 
     * @param mapper transformation function
     * @return MonadicValue
     */
    <R> MonadicValue<R> flatMap(Function<? super T, ? extends MonadicValue<? extends R>> mapper);

    /**
     * A flattening transformation operation that takes the first value from the returned Iterable.
     * 
     * <pre>
     * {@code 
     *   Maybe.just(1).map(i->i+2).flatMapIterable(i->Arrays.asList(()->i*3,20);
     *   //Maybe[9]
     * 
     * }</pre>
     * 
     * 
     * @param mapper  transformation function
     * @return  MonadicValue
     */
    default <R> MonadicValue<R> flatMapIterable(final Function<? super T, ? extends Iterable<? extends R>> mapper) {
        return this.flatMap(a -> {
            return Maybe.fromIterable(mapper.apply(a));
        });
    }

    /**
     * A flattening transformation operation that takes the first value from the returned Publisher.
     * <pre>
     * {@code 
     *   FutureW.ofResult(1).map(i->i+2).flatMapPublisher(i->Flux.just(()->i*3,20);
     *   //FutureW[9]
     * 
     * }</pre>
     * 
     * @param mapper transformation function
     * @return  MonadicValue
     */
    default <R> MonadicValue<R> flatMapPublisher(final Function<? super T, ? extends Publisher<? extends R>> mapper) {

        return this.flatMap(a -> {
            final Publisher<? extends R> publisher = mapper.apply(a);
            final ValueSubscriber<R> sub = ValueSubscriber.subscriber();
            publisher.subscribe(sub);

            final Maybe<R> maybe = sub.toMaybe();
            return unit(maybe.get());

        });
    }
}
