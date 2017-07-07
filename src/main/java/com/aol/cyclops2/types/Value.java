package com.aol.cyclops2.types;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import com.aol.cyclops2.types.foldable.Convertable;
import com.aol.cyclops2.types.foldable.Folds;
import cyclops.control.lazy.Either;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import cyclops.control.Eval;
import cyclops.control.Ior;
import cyclops.control.Maybe;
import cyclops.stream.ReactiveSeq;
import cyclops.control.Try;
import cyclops.control.Xor;
import com.aol.cyclops2.types.reactive.ValueSubscriber;
import cyclops.function.Predicates;

import lombok.AllArgsConstructor;

/**
 * A data type that stores at most 1 Values
 *
 * @author johnmcclean
 *
 * @param <T> Data type of element in this value
 */
@FunctionalInterface
public interface Value<T> extends   Folds<T>,
                                    Convertable<T>,
                                    Publisher<T>,
                                    Predicate<T> {
    @Override
    default T apply() {
        return this.orElse(null);
    }

    /* An Iterator over the list returned from toList()
         *
         *  (non-Javadoc)
         * @see java.lang.Iterable#iterator()
         */
    @Override
    default Iterator<T> iterator() {
        return Convertable.super.iterator();
    }

    /* (non-Javadoc)
     * @see java.util.function.Predicate#test(java.lang.Object)
     */
    @Override
    default boolean test(final T t) {
        if (!(t instanceof Value))
            return Predicates.eqv(Maybe.ofNullable(t))
                             .test(this);
        else
            return Predicates.eqv((Value) t)
                             .test(this);

    }

    /**
     * @return A factory class generating Values from reactive-streams Subscribers
     */
    default ValueSubscriber<T> newSubscriber() {
        return ValueSubscriber.subscriber();
    }

    /* (non-Javadoc)
     * @see org.reactivestreams.Publisher#forEachAsync(org.reactivestreams.Subscriber)
     */
    @Override
    default void subscribe(final Subscriber<? super T> sub) {
        sub.onSubscribe(new Subscription() {

            AtomicBoolean running = new AtomicBoolean(
                                                      true);
            AtomicBoolean cancelled = new AtomicBoolean(false);

            @Override
            public void request(final long n) {

                if (n < 1) {
                    sub.onError(new IllegalArgumentException(
                                                             "3.9 While the Subscription is not cancelled, Subscription.request(long n) MUST throw a java.lang.IllegalArgumentException if the argument is <= 0."));
                }

                if (!running.compareAndSet(true, false)) {

                    return;
                }
                try {
                    T value = get();
                    if(!cancelled.get())
                        sub.onNext(get());

                } catch (final Throwable t) {
                    sub.onError(t);

                }
                try {
                    sub.onComplete();

                } finally {

                }

            }

            @Override
            public void cancel() {

                cancelled.set(true);

            }

        });

    }

    /**
     * Construct a generic Value from the provided Supplier
     *
     * @param supplier Value supplier
     * @return Value wrapping a value that can be generated from the provided Supplier
     */
    public static <T> Value<T> of(final Supplier<T> supplier) {
        return new ValueImpl<T>(
                                supplier);
    }

    @AllArgsConstructor
    public static class ValueImpl<T> implements Value<T> {
        private final Supplier<T> delegate;

        @Override
        public T get() {
            return delegate.get();
        }

        @Override
        public Iterator<T> iterator() {
            return stream().iterator();
        }
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.foldable.Folds#reactiveStream()
     */
 //   @Override
    default ReactiveSeq<T> stream() {
        return ReactiveSeq.generate(()->Try.withCatch(() -> get(), NoSuchElementException.class))
                          .take(1)
                          .filter(Try::isSuccess)
                          .map(Try::get);
    }



    /**
     * Use the value stored in this Value to seed a Stream generated from the provided function
     *
     * @param fn Function to generate a Stream
     * @return Stream generated from a seed value (the Value stored in this Value) and the provided function
     */
    default ReactiveSeq<T> iterate(final UnaryOperator<T> fn) {
        return ReactiveSeq.iterate(get(), fn);
    }

    /**
     * @return A Stream that repeats the value stored in this Value over and over
     */
    default ReactiveSeq<T> generate() {
        return ReactiveSeq.generate(this);
    }


    /**
     * @return Primary Xor that has the same value as this Value
     */
    default  Xor<?, T> toXor() {
        if (this instanceof Xor)
            return (Xor) this;
        final Optional<T> o = toOptional();
        return o.isPresent() ? Xor.primary(o.get()) : Xor.secondary(null);

    }

    /**
     * Convert to an Xor where the secondary value will be used if no primary value is present
     *
    * @param secondary Value to use in case no primary value is present
    * @return Primary Xor with same value as this Value, or a Secondary Xor with the provided Value if this Value is zero
    */
    default <ST> Xor<ST, T> toXor(final ST secondary) {
        final Optional<T> o = toOptional();
        return o.isPresent() ? Xor.primary(o.get()) : Xor.secondary(secondary);
    }
    default  Either<Throwable, T> toEither() {
       return Either.fromPublisher(this);
    }

    /**
     * Lazily convert this Value to an Either.right instance
     */
    default <LT> Either<LT,T> toRight(){
        return Either.fromIterable(this);
    }
    /**
     * Lazily convert this Value to an Either.left instance
     */
    default <RT> Either<T,RT> toLeft(){
        return Either.<RT,T>fromIterable(this)
                     .swap();
    }
    /**
     * @param throwable Exception to use if this Value is zero
     * @return Try that has the same value as this Value or the provided Exception
     */
    default <X extends Throwable> Try<T, X> toTry(final X throwable) {
        return Try.fromXor(toTry().asXor().secondaryMap(t->throwable));

    }

    /**
     * @return This Value converted to a Try. If this Value is zero the Try will contain a NoSuchElementException
     */
    default Try<T, Throwable> toTry() {
        return Try.fromPublisher(this);
    }

    /**
     * Convert this Value to a Try that will catch the provided exception types on subsequent operations
     *
     * @param classes Exception classes to catch on subsequent operations
     * @return This Value to converted to a Try.
     */
    default <X extends Throwable> Try<T, X> toTry(final Class<X>... classes) {
        return Try.fromPublisher(this,classes);
    }


    /**
     * Return an Ior that can be this object or a Ior.primary or Ior.secondary
     * @return new Ior
     */
    default  Ior<?, T> toIor() {
        if (this instanceof Ior)
            return (Ior) this;
        final Optional<T> o = toOptional();
        return o.isPresent() ? Ior.primary(o.get()) : Ior.secondary(null);
    }


    default Eval<T> toEval() {
        return Eval.fromPublisher(this);
    }
    /**
     * Return the value, evaluated right now.
     * @return value evaluated from this object.
     */
    default Eval<T> toEvalNow() {
        return Eval.now(get());
    }

    /**
     * Return the value, evaluated later.
     * @return value evaluated from this object.
     */
    default Eval<T> toEvalLater() {
        return Eval.later(this);
    }

    /**
     * Return the value of this object, evaluated always.
     * @return value evaluated from this object.
     */
    default Eval<T> toEvalAlways() {
        return Eval.always(this);
    }

    default Maybe<T> toMaybe() {
        return Maybe.fromPublisher(this);
    }

    /**
     * Returns a function result or a supplier result. The takeOne one if the function isn't null and the second one if it is.
     * @return new Maybe with the result of a function or supplier.
     */
    default Maybe<T> toMaybeEager() {
        return visit(p -> Maybe.ofNullable(p), () -> Maybe.none());
    }


    /**
     * Returns the class name and the name of the subclass, if there is any value, the value is showed between square brackets.
     * @return String
     */
    default String mkString() {

        if (isPresent())
            return getClass().getSimpleName() + "[" + get() + "]";
        return getClass().getSimpleName() + "[]";
    }



}
