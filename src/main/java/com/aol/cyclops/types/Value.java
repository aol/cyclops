package com.aol.cyclops.types;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BinaryOperator;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collector;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import com.aol.cyclops.Monoid;
import com.aol.cyclops.Reducer;
import com.aol.cyclops.control.Eval;
import com.aol.cyclops.control.FeatureToggle;
import com.aol.cyclops.control.Ior;
import com.aol.cyclops.control.LazyReact;
import com.aol.cyclops.control.Maybe;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.control.SimpleReact;
import com.aol.cyclops.control.Try;
import com.aol.cyclops.control.Xor;
import com.aol.cyclops.data.LazyImmutable;
import com.aol.cyclops.data.Mutable;
import com.aol.cyclops.data.collections.extensions.persistent.PBagX;
import com.aol.cyclops.data.collections.extensions.persistent.POrderedSetX;
import com.aol.cyclops.data.collections.extensions.persistent.PQueueX;
import com.aol.cyclops.data.collections.extensions.persistent.PSetX;
import com.aol.cyclops.data.collections.extensions.persistent.PStackX;
import com.aol.cyclops.data.collections.extensions.persistent.PVectorX;
import com.aol.cyclops.data.collections.extensions.standard.DequeX;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.data.collections.extensions.standard.QueueX;
import com.aol.cyclops.data.collections.extensions.standard.SetX;
import com.aol.cyclops.data.collections.extensions.standard.SortedSetX;
import com.aol.cyclops.types.futurestream.LazyFutureStream;
import com.aol.cyclops.types.futurestream.SimpleReactStream;
import com.aol.cyclops.types.stream.reactive.ValueSubscriber;
import com.aol.cyclops.util.function.Predicates;

import lombok.AllArgsConstructor;

/**
 * A data type that stores at most 1 Values
 * 
 * @author johnmcclean
 *
 * @param <T> Data type of element in this value
 */
@FunctionalInterface
public interface Value<T> extends Supplier<T>, Foldable<T>, Convertable<T>, Publisher<T>, Predicate<T>, Zippable<T> {

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
     * @see org.reactivestreams.Publisher#subscribe(org.reactivestreams.Subscriber)
     */
    @Override
    default void subscribe(final Subscriber<? super T> sub) {
        sub.onSubscribe(new Subscription() {

            AtomicBoolean running = new AtomicBoolean(
                                                      true);

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

                running.set(false);

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
     * @see com.aol.cyclops.types.Foldable#stream()
     */
    @Override
    default ReactiveSeq<T> stream() {
        return ReactiveSeq.of(Try.withCatch(() -> get(), NoSuchElementException.class))
                          .filter(Try::isSuccess)
                          .map(Try::get);
    }

    /**
     * @return This value converted to a List (for pattern matching purposes)
     */
    default ListX<?> unapply() {
        return toListX();
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

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Foldable#mapReduce(com.aol.cyclops.Reducer)
     */
    @Override
    default <E> E mapReduce(final Reducer<E> monoid) {
        return monoid.mapReduce(toStream());
    }

    /**
     * Use the supplied Monoid to reduce this Value to a single result (unwraps the value stored in this Value 
     * if the provided Monoid instance obeys the Monoid laws)
     * 
     * @param monoid Monoid to apply to this value
     * @return The value stored inside this Value
     */
    default T fold(final Monoid<T> monoid) {
        return monoid.reduce(toStream());
    }

    /**
     * Use the supplied identity value and function to reduce this Value to a single result (unwraps the value stored in this Value 
     * if the provided monoid combination instance obeys the Monoid laws)
     * 
     * @param identity Identity value
     * @param accumulator Accumulation function
     * @return Result of applying Value stored in this value and the identity value to the provided accumulator
     */
    default T fold(final T identity, final BinaryOperator<T> accumulator) {
        final Optional<T> opt = toOptional();
        if (opt.isPresent())
            return accumulator.apply(identity, get());
        return identity;
    }

    /**
     * @return LazyImmutable that has the same value as this Value
     */
    default LazyImmutable<T> toLazyImmutable() {
        return LazyImmutable.of(get());
    }

    /**
     * @return Mutable that has the same value as this Value
     */
    default Mutable<T> toMutable() {
        return Mutable.of(get());
    }

    /**
     * @return Primary Xor that has the same value as this Value
     */
    default Xor<?, T> toXor() {
        if (this instanceof Xor)
            return (Xor) this;
        final Optional<T> o = toOptional();
        return o.isPresent() ? Xor.primary(o.get()) : Xor.secondary(new NoSuchElementException());

    }

    /**
     * Convert to an Xor where the secondary value will be used if no primary value is present
     * 
    * @param secondary Value to use in case no primary value is present
    * @return Primary Xor with same value as this Value, or a Secondary Xor with the provided Value if this Value is empty
    */
    default <ST> Xor<ST, T> toXor(final ST secondary) {
        final Optional<T> o = toOptional();
        return o.isPresent() ? Xor.primary(o.get()) : Xor.secondary(secondary);
    }

    /**
     * @param throwable Exception to use if this Value is empty
     * @return Try that has the same value as this Value or the provided Exception
     */
    default <X extends Throwable> Try<T, X> toTry(final X throwable) {
        return toXor().visit(secondary -> Try.failure(throwable), primary -> Try.success(primary));

    }

    /**
     * @return This Value converted to a Try. If this Value is empty the Try will contain a NoSuchElementException
     */
    default Try<T, Throwable> toTry() {
        return toXor().visit(secondary -> Try.failure(new NoSuchElementException()), primary -> Try.success(primary));

    }

    /**
     * Convert this Value to a Try that will catch the provided exception types on subsequent operations
     * 
     * @param classes Exception classes to catch on subsequent operations
     * @return This Value to converted to a Try.
     */
    default <X extends Throwable> Try<T, X> toTry(final Class<X>... classes) {
        return Try.withCatch(() -> get(), classes);
    }

    default Ior<?, T> toIor() {
        if (this instanceof Ior)
            return (Ior) this;
        final Optional<T> o = toOptional();
        return o.isPresent() ? Ior.primary(o.get()) : Ior.secondary(new NoSuchElementException());
    }

    default FeatureToggle<T> toFeatureToggle() {
        final Optional<T> opt = toOptional();
        return opt.isPresent() ? FeatureToggle.enable(opt.get()) : FeatureToggle.disable(null);
    }

    default Eval<T> toEvalNow() {
        return Eval.now(get());
    }

    default Eval<T> toEvalLater() {
        return Eval.later(this);
    }

    default Eval<T> toEvalAlways() {
        return Eval.always(this);
    }

    default Maybe<T> toMaybe() {
        return visit(p -> Maybe.ofNullable(p), () -> Maybe.none());
    }

    default ListX<T> toListX() {
        return ListX.fromIterable(toList());
    }

    default SetX<T> toSetX() {
        return SetX.fromIterable(toList());
    }

    default SortedSetX<T> toSortedSetX() {
        return SortedSetX.fromIterable(toList());
    }

    default QueueX<T> toQueueX() {
        return QueueX.fromIterable(toList());
    }

    default DequeX<T> toDequeX() {
        return DequeX.fromIterable(toList());
    }

    default PStackX<T> toPStackX() {
        return PStackX.fromCollection(toList());
    }

    default PVectorX<T> toPVectorX() {
        return PVectorX.fromCollection(toList());
    }

    default PQueueX<T> toPQueueX() {
        return PQueueX.fromCollection(toList());
    }

    default PSetX<T> toPSetX() {
        return PSetX.fromCollection(toList());
    }

    default POrderedSetX<T> toPOrderedSetX() {
        return POrderedSetX.fromCollection(toList());
    }

    default PBagX<T> toPBagX() {
        return PBagX.fromCollection(toList());
    }

    default String mkString() {

        if (isPresent())
            return getClass().getSimpleName() + "[" + get() + "]";
        return getClass().getSimpleName() + "[]";
    }

    default LazyFutureStream<T> toFutureStream(final LazyReact reactor) {
        return reactor.ofAsync(this);
    }

    default LazyFutureStream<T> toFutureStream() {
        return new LazyReact().ofAsync(this);
    }

    default SimpleReactStream<T> toSimpleReact(final SimpleReact reactor) {
        return reactor.ofAsync(this);
    }

    default SimpleReactStream<T> toSimpleReact() {
        return new SimpleReact().ofAsync(this);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Convertable#collect(java.util.stream.Collector)
     */
    @Override
    default <R, A> R collect(final Collector<? super T, A, R> collector) {
        final A state = collector.supplier()
                                 .get();
        collector.accumulator()
                 .accept(state, get());
        return collector.finisher()
                        .apply(state);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Convertable#toList()
     */
    @Override
    default List<T> toList() {
        return Convertable.super.toList();
    }

}
