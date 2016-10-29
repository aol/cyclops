package com.aol.cyclops.types.anyM;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;

import org.jooq.lambda.Collectable;
import org.jooq.lambda.Seq;
import org.jooq.lambda.function.Function3;
import org.jooq.lambda.function.Function4;
import org.jooq.lambda.function.Function5;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.jooq.lambda.tuple.Tuple4;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

import com.aol.cyclops.Matchables;
import com.aol.cyclops.Monoid;
import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.Matchable.CheckValue1;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.control.Streamable;
import com.aol.cyclops.control.Trampoline;
import com.aol.cyclops.control.Xor;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.internal.monads.AnyMSeqImpl;
import com.aol.cyclops.internal.monads.AnyMonads;
import com.aol.cyclops.types.Applicative;
import com.aol.cyclops.types.ExtendedTraversable;
import com.aol.cyclops.types.FilterableFunctor;
import com.aol.cyclops.types.IterableFoldable;
import com.aol.cyclops.types.Sequential;
import com.aol.cyclops.types.Traversable;
import com.aol.cyclops.types.Value;
import com.aol.cyclops.types.applicative.zipping.ApplyingZippingApplicativeBuilder;
import com.aol.cyclops.types.applicative.zipping.ZippingApplicativable;
import com.aol.cyclops.types.stream.ConvertableSequence;
import com.aol.cyclops.types.stream.CyclopsCollectable;
import com.aol.cyclops.types.stream.reactive.ReactiveStreamsTerminalOperations;
import com.aol.cyclops.util.function.Predicates;
import com.aol.cyclops.util.function.QuadFunction;
import com.aol.cyclops.util.function.QuintFunction;
import com.aol.cyclops.util.function.TriFunction;

import lombok.val;

/**
 * Wrapper around 'Any' non-scalar 'M'onad
 * 
 * @author johnmcclean
 *
 * @param <T> Data types of elements managed by wrapped non-scalar Monad.
 */
public interface AnyMSeq<T> extends AnyM<T>, IterableFoldable<T>, ConvertableSequence<T>, ExtendedTraversable<T>, Sequential<T>,
        CyclopsCollectable<T>, FilterableFunctor<T>, ZippingApplicativable<T>, ReactiveStreamsTerminalOperations<T>, Publisher<T> {

    /**
     * Equivalence test, returns true if this Monad is equivalent to the supplied monad
     * e.g.
     * <pre>
     * {code
     *    Stream.of(1) and Arrays.asList(1) are equivalent
     * }
     * </pre>
     * 
     * 
     * @param t Monad to compare to
     * @return true if equivalent
     */
    default boolean eqv(final AnyMSeq<T> t) {
        return Predicates.eqvIterable(t)
                         .test(this);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.AnyM#collect(java.util.stream.Collector)
     */
    @Override
    default <R, A> R collect(final Collector<? super T, A, R> collector) {
        return stream().collect(collector);

    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.AnyM#combine(com.aol.cyclops.types.Value, java.util.function.BiFunction)
     */
    @Override
    default <T2, R> AnyMSeq<R> combine(Value<? extends T2> app, BiFunction<? super T, ? super T2, ? extends R> fn) {
        
        return (AnyMSeq<R>)AnyM.super.combine(app, fn);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.AnyM#combine(java.util.function.BinaryOperator, com.aol.cyclops.types.Applicative)
     */
    @Override
    default AnyMSeq<T> combine(BinaryOperator<Applicative<T>> combiner, Applicative<T> app) {
        
        return ( AnyMSeq<T>)AnyM.super.combine(combiner, app);
    }

    /**
     * Perform a flatMap operation that will only work as normal for AnyMSeq types, but for AnyMValue (which can only hold a single value) 
     * will take the first value from the Iterable returned.
     * 
     * @param fn FlatMaping function
     * @return AnyM with flattening transformation
     */
    @Override
    <R> AnyMSeq<R> flatMapFirst(Function<? super T, ? extends Iterable<? extends R>> fn);

    /**
     * Perform a flatMap operation that will only work as normal for AnyMSeq types, but for AnyMValue (which can only hold a single value) 
     * will take the first value from the Publisher returned.
     * 
     * @param fn FlatMaping function
     * @return AnyM with flattening transformation
     */
    @Override
    <R> AnyMSeq<R> flatMapFirstPublisher(Function<? super T, ? extends Publisher<? extends R>> fn);

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#traversable()
     */
    @Override
    default Traversable<T> traversable() {
        final Object o = unwrap();
        if (o instanceof Traversable) {
            return (Traversable) o;
        }
        return stream();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Foldable#foldable()
     */
    @Override
    default IterableFoldable<T> foldable() {
        final Object o = unwrap();
        if (o instanceof IterableFoldable) {
            return (IterableFoldable) o;
        }
        return stream();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#limit(long)
     */
    @Override
    default AnyMSeq<T> limit(final long num) {

        return AnyM.ofSeq(ExtendedTraversable.super.limit(num));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#limitWhile(java.util.function.Predicate)
     */
    @Override
    default AnyMSeq<T> limitWhile(final Predicate<? super T> p) {

        return AnyM.ofSeq(ExtendedTraversable.super.limitWhile(p));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#limitUntil(java.util.function.Predicate)
     */
    @Override
    default AnyMSeq<T> limitUntil(final Predicate<? super T> p) {

        return AnyM.ofSeq(ExtendedTraversable.super.limitUntil(p));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#limitLast(int)
     */
    @Override
    default AnyMSeq<T> limitLast(final int num) {

        return AnyM.ofSeq(ExtendedTraversable.super.limitLast(num));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#onEmpty(java.lang.Object)
     */
    @Override
    default AnyMSeq<T> onEmpty(final T value) {
        return AnyM.ofSeq(ExtendedTraversable.super.onEmpty(value));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#onEmptyGet(java.util.function.Supplier)
     */
    @Override
    default AnyMSeq<T> onEmptyGet(final Supplier<? extends T> supplier) {

        return AnyM.ofSeq(ExtendedTraversable.super.onEmptyGet(supplier));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#onEmptyThrow(java.util.function.Supplier)
     */
    @Override
    default <X extends Throwable> AnyMSeq<T> onEmptyThrow(final Supplier<? extends X> supplier) {

        return AnyM.ofSeq(ExtendedTraversable.super.onEmptyThrow(supplier));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.applicative.zipping.ZippingApplicativable#applicatives()
     */
    @Override
    default <R> ApplyingZippingApplicativeBuilder<T, R, ZippingApplicativable<R>> applicatives() {
        final Streamable<T> streamable = toStreamable();
        return new ApplyingZippingApplicativeBuilder<T, R, ZippingApplicativable<R>>(
                                                                                     streamable, streamable);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.applicative.zipping.ZippingApplicativable#ap1(java.util.function.Function)
     */
    @Override
    default <R> ZippingApplicativable<R> ap1(final Function<? super T, ? extends R> fn) {
        val dup = stream().duplicateSequence();
        final Streamable<T> streamable = dup.v1.toStreamable();
        return new ApplyingZippingApplicativeBuilder<T, R, ZippingApplicativable<R>>(
                                                                                     streamable, streamable).applicative(fn)
                                                                                                            .ap(dup.v2);

    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#subscribe(org.reactivestreams.Subscriber)
     */
    @Override
    default void subscribe(final Subscriber<? super T> sub) {
        if (unwrap() instanceof Publisher) {
            ((Publisher) unwrap()).subscribe(sub);
        } else {
            this.stream()
                .subscribe(sub);
        }
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.sequence.SequenceMCollectable#collectable()
     */
    @Override
    default Collectable<T> collectable() {
        return ZippingApplicativable.super.collectable();
    }

    /**
     * @return The first value of this monad
     */
    default Value<T> toFirstValue() {

        return () -> firstOrNull(toListX());
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.stream.ConvertableSequence#toXor()
     */
    @Override
    default Xor<?, ListX<T>> toXor() {
        return toValue().toXor();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.stream.ConvertableSequence#toXorSecondary()
     */
    @Override
    default Xor<ListX<T>, ?> toXorSecondary() {
        return toValue().toXor()
                        .swap();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Functor#cast(java.lang.Class)
     */
    @Override
    default <U> AnyMSeq<U> cast(final Class<? extends U> type) {

        return (AnyMSeq<U>) ZippingApplicativable.super.cast(type);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Functor#trampoline(java.util.function.Function)
     */
    @Override
    default <R> AnyMSeq<R> trampoline(final Function<? super T, ? extends Trampoline<? extends R>> mapper) {

        return (AnyMSeq<R>) ZippingApplicativable.super.trampoline(mapper);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Functor#patternMatch(java.lang.Object, java.util.function.Function)
     */
    @Override
    default <R> AnyMSeq<R> patternMatch(final Function<CheckValue1<T, R>, CheckValue1<T, R>> case1, final Supplier<? extends R> otherwise) {

        return (AnyMSeq<R>) ZippingApplicativable.super.patternMatch(case1, otherwise);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#cycle(int)
     */
    @Override
    default AnyMSeq<T> cycle(final int times) {

        return AnyM.fromIterable(ZippingApplicativable.super.cycle(times));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#cycle(com.aol.cyclops.Monoid, int)
     */
    @Override
    default AnyMSeq<T> cycle(final Monoid<T> m, final int times) {

        return AnyM.fromIterable(ZippingApplicativable.super.cycle(m, times));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#cycleWhile(java.util.function.Predicate)
     */
    @Override
    default AnyMSeq<T> cycleWhile(final Predicate<? super T> predicate) {

        return AnyM.fromIterable(ZippingApplicativable.super.cycleWhile(predicate));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#cycleUntil(java.util.function.Predicate)
     */
    @Override
    default AnyMSeq<T> cycleUntil(final Predicate<? super T> predicate) {

        return AnyM.fromIterable(ZippingApplicativable.super.cycleUntil(predicate));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#zip(java.lang.Iterable, java.util.function.BiFunction)
     */
    @Override
    default <U, R> AnyMSeq<R> zip(final Iterable<? extends U> other, final BiFunction<? super T, ? super U, ? extends R> zipper) {

        return AnyM.fromIterable(ZippingApplicativable.super.zip(other, zipper));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#zip(java.util.stream.Stream, java.util.function.BiFunction)
     */
    @Override
    default <U, R> AnyMSeq<R> zip(final Stream<? extends U> other, final BiFunction<? super T, ? super U, ? extends R> zipper) {

        return AnyM.fromIterable(ZippingApplicativable.super.zip(other, zipper));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#zip(org.jooq.lambda.Seq, java.util.function.BiFunction)
     */
    @Override
    default <U, R> AnyMSeq<R> zip(final Seq<? extends U> other, final BiFunction<? super T, ? super U, ? extends R> zipper) {

        return AnyM.fromIterable(ZippingApplicativable.super.zip(other, zipper));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#zip(java.util.stream.Stream)
     */
    @Override
    default <U> AnyMSeq<Tuple2<T, U>> zip(final Stream<? extends U> other) {

        return AnyM.fromIterable(ZippingApplicativable.super.zip(other));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#zip(java.lang.Iterable)
     */
    @Override
    default <U> AnyMSeq<Tuple2<T, U>> zip(final Iterable<? extends U> other) {

        return AnyM.fromIterable(ZippingApplicativable.super.zip(other));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#zip(org.jooq.lambda.Seq)
     */
    @Override
    default <U> AnyMSeq<Tuple2<T, U>> zip(final Seq<? extends U> other) {

        return AnyM.fromIterable(ZippingApplicativable.super.zip(other));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#zip3(java.util.stream.Stream, java.util.stream.Stream)
     */
    @Override
    default <S, U> AnyMSeq<Tuple3<T, S, U>> zip3(final Stream<? extends S> second, final Stream<? extends U> third) {

        return AnyM.fromIterable(ZippingApplicativable.super.zip3(second, third));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#zip4(java.util.stream.Stream, java.util.stream.Stream, java.util.stream.Stream)
     */
    @Override
    default <T2, T3, T4> AnyMSeq<Tuple4<T, T2, T3, T4>> zip4(final Stream<? extends T2> second, final Stream<? extends T3> third,
            final Stream<? extends T4> fourth) {

        return AnyM.fromIterable(ZippingApplicativable.super.zip4(second, third, fourth));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#zipWithIndex()
     */
    @Override
    default AnyMSeq<Tuple2<T, Long>> zipWithIndex() {

        return AnyM.fromIterable(ZippingApplicativable.super.zipWithIndex());
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#sliding(int)
     */
    @Override
    default AnyMSeq<ListX<T>> sliding(final int windowSize) {

        return AnyM.fromIterable(ZippingApplicativable.super.sliding(windowSize));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#sliding(int, int)
     */
    @Override
    default AnyMSeq<ListX<T>> sliding(final int windowSize, final int increment) {

        return AnyM.fromIterable(ZippingApplicativable.super.sliding(windowSize, increment));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#grouped(int, java.util.function.Supplier)
     */
    @Override
    default <C extends Collection<? super T>> AnyMSeq<C> grouped(final int size, final Supplier<C> supplier) {

        return AnyM.fromIterable(ZippingApplicativable.super.grouped(size, supplier));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#groupedUntil(java.util.function.Predicate)
     */
    @Override
    default AnyMSeq<ListX<T>> groupedUntil(final Predicate<? super T> predicate) {

        return AnyM.fromIterable(ZippingApplicativable.super.groupedUntil(predicate));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#groupedStatefullyUntil(java.util.function.BiPredicate)
     */
    @Override
    default AnyMSeq<ListX<T>> groupedStatefullyUntil(final BiPredicate<ListX<? super T>, ? super T> predicate) {

        return AnyM.fromIterable(ZippingApplicativable.super.groupedStatefullyUntil(predicate));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#combine(java.util.function.BiPredicate, java.util.function.BinaryOperator)
     */
    @Override
    default AnyMSeq<T> combine(final BiPredicate<? super T, ? super T> predicate, final BinaryOperator<T> op) {

        return AnyM.fromIterable(ZippingApplicativable.super.combine(predicate, op));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#groupedWhile(java.util.function.Predicate)
     */
    @Override
    default AnyMSeq<ListX<T>> groupedWhile(final Predicate<? super T> predicate) {

        return AnyM.fromIterable(ZippingApplicativable.super.groupedWhile(predicate));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#groupedWhile(java.util.function.Predicate, java.util.function.Supplier)
     */
    @Override
    default <C extends Collection<? super T>> AnyMSeq<C> groupedWhile(final Predicate<? super T> predicate, final Supplier<C> factory) {

        return AnyM.fromIterable(ZippingApplicativable.super.groupedWhile(predicate, factory));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#groupedUntil(java.util.function.Predicate, java.util.function.Supplier)
     */
    @Override
    default <C extends Collection<? super T>> AnyMSeq<C> groupedUntil(final Predicate<? super T> predicate, final Supplier<C> factory) {

        return AnyM.fromIterable(ZippingApplicativable.super.groupedUntil(predicate, factory));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#grouped(int)
     */
    @Override
    default AnyMSeq<ListX<T>> grouped(final int groupSize) {

        return AnyM.fromIterable(ZippingApplicativable.super.grouped(groupSize));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#grouped(java.util.function.Function, java.util.stream.Collector)
     */
    @Override
    default <K, A, D> AnyMSeq<Tuple2<K, D>> grouped(final Function<? super T, ? extends K> classifier, final Collector<? super T, A, D> downstream) {

        return AnyM.fromIterable(ZippingApplicativable.super.grouped(classifier, downstream));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#grouped(java.util.function.Function)
     */
    @Override
    default <K> AnyMSeq<Tuple2<K, Seq<T>>> grouped(final Function<? super T, ? extends K> classifier) {

        return AnyM.fromIterable(ZippingApplicativable.super.grouped(classifier));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#takeWhile(java.util.function.Predicate)
     */
    @Override
    default AnyMSeq<T> takeWhile(final Predicate<? super T> p) {

        return AnyM.fromIterable(ZippingApplicativable.super.takeWhile(p));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#dropWhile(java.util.function.Predicate)
     */
    @Override
    default AnyMSeq<T> dropWhile(final Predicate<? super T> p) {

        return AnyM.fromIterable(ZippingApplicativable.super.dropWhile(p));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#takeUntil(java.util.function.Predicate)
     */
    @Override
    default AnyMSeq<T> takeUntil(final Predicate<? super T> p) {

        return AnyM.fromIterable(ZippingApplicativable.super.takeUntil(p));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#dropUntil(java.util.function.Predicate)
     */
    @Override
    default AnyMSeq<T> dropUntil(final Predicate<? super T> p) {

        return AnyM.fromIterable(ZippingApplicativable.super.dropUntil(p));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#dropRight(int)
     */
    @Override
    default AnyMSeq<T> dropRight(final int num) {

        return AnyM.fromIterable(ZippingApplicativable.super.dropRight(num));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#takeRight(int)
     */
    @Override
    default AnyMSeq<T> takeRight(final int num) {

        return AnyM.fromIterable(ZippingApplicativable.super.takeRight(num));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#reverse()
     */
    @Override
    default AnyMSeq<T> reverse() {

        return AnyM.fromIterable(ZippingApplicativable.super.reverse());
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#shuffle()
     */
    @Override
    default AnyMSeq<T> shuffle() {

        return AnyM.fromIterable(ZippingApplicativable.super.shuffle());
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#shuffle(java.util.Random)
     */
    @Override
    default AnyMSeq<T> shuffle(final Random random) {

        return AnyM.fromIterable(ZippingApplicativable.super.shuffle(random));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#distinct()
     */
    @Override
    default AnyMSeq<T> distinct() {

        return AnyM.fromIterable(ZippingApplicativable.super.distinct());
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#scanLeft(com.aol.cyclops.Monoid)
     */
    @Override
    default AnyMSeq<T> scanLeft(final Monoid<T> monoid) {

        return AnyM.fromIterable(ZippingApplicativable.super.scanLeft(monoid));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#scanLeft(java.lang.Object, java.util.function.BiFunction)
     */
    @Override
    default <U> AnyMSeq<U> scanLeft(final U seed, final BiFunction<? super U, ? super T, ? extends U> function) {

        return AnyM.fromIterable(ZippingApplicativable.super.scanLeft(seed, function));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#scanRight(com.aol.cyclops.Monoid)
     */
    @Override
    default AnyMSeq<T> scanRight(final Monoid<T> monoid) {

        return AnyM.fromIterable(ZippingApplicativable.super.scanRight(monoid));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#scanRight(java.lang.Object, java.util.function.BiFunction)
     */
    @Override
    default <U> AnyMSeq<U> scanRight(final U identity, final BiFunction<? super T, ? super U, ? extends U> combiner) {

        return AnyM.fromIterable(ZippingApplicativable.super.scanRight(identity, combiner));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#sorted()
     */
    @Override
    default AnyMSeq<T> sorted() {

        return AnyM.fromIterable(ZippingApplicativable.super.sorted());
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#sorted(java.util.Comparator)
     */
    @Override
    default AnyMSeq<T> sorted(final Comparator<? super T> c) {

        return AnyM.fromIterable(ZippingApplicativable.super.sorted(c));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#skip(long)
     */
    @Override
    default AnyMSeq<T> skip(final long num) {

        return AnyM.fromIterable(ZippingApplicativable.super.skip(num));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#skipWhile(java.util.function.Predicate)
     */
    @Override
    default AnyMSeq<T> skipWhile(final Predicate<? super T> p) {

        return AnyM.fromIterable(ZippingApplicativable.super.skipWhile(p));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#skipUntil(java.util.function.Predicate)
     */
    @Override
    default AnyMSeq<T> skipUntil(final Predicate<? super T> p) {

        return AnyM.fromIterable(ZippingApplicativable.super.skipUntil(p));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#intersperse(java.lang.Object)
     */
    @Override
    default AnyMSeq<T> intersperse(final T value) {

        return AnyM.fromIterable(ZippingApplicativable.super.intersperse(value));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#skipLast(int)
     */
    @Override
    default AnyMSeq<T> skipLast(final int num) {

        return AnyM.fromIterable(ZippingApplicativable.super.skipLast(num));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#slice(long, long)
     */
    @Override
    default AnyMSeq<T> slice(final long from, final long to) {

        return AnyM.fromIterable(ZippingApplicativable.super.slice(from, to));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#sorted(java.util.function.Function)
     */
    @Override
    default <U extends Comparable<? super U>> AnyMSeq<T> sorted(final Function<? super T, ? extends U> function) {

        return AnyM.fromIterable(ZippingApplicativable.super.sorted(function));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.ExtendedTraversable#permutations()
     */
    @Override
    default AnyMSeq<ReactiveSeq<T>> permutations() {

        return AnyM.fromIterable(ExtendedTraversable.super.permutations());
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.ExtendedTraversable#combinations(int)
     */
    @Override
    default AnyMSeq<ReactiveSeq<T>> combinations(final int size) {

        return AnyM.fromIterable(ExtendedTraversable.super.combinations(size));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.ExtendedTraversable#combinations()
     */
    @Override
    default AnyMSeq<ReactiveSeq<T>> combinations() {

        return AnyM.fromIterable(ExtendedTraversable.super.combinations());
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Filterable#ofType(java.lang.Class)
     */
    @Override
    default <U> AnyMSeq<U> ofType(final Class<? extends U> type) {

        return (AnyMSeq<U>) FilterableFunctor.super.ofType(type);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Filterable#filterNot(java.util.function.Predicate)
     */
    @Override
    default AnyMSeq<T> filterNot(final Predicate<? super T> fn) {
        return (AnyMSeq<T>) FilterableFunctor.super.filterNot(fn);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.IterableFunctor#unitIterator(java.util.Iterator)
     */
    @Override
    <U> AnyMSeq<U> unitIterator(Iterator<U> U);

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.EmptyUnit#emptyUnit()
     */
    @Override
    <T> AnyMSeq<T> emptyUnit();

    /* (non-Javadoc)
    * @see com.aol.cyclops.monad.AnyM#stream()
    */
    @Override
    ReactiveSeq<T> stream();

    /* (non-Javadoc)
     * @see com.aol.cyclops.monad.AnyM#unwrap()
     */
    @Override
    <R> R unwrap();

    /* (non-Javadoc)
     * @see com.aol.cyclops.monad.AnyM#filter(java.util.function.Predicate)
     */
    @Override
    AnyMSeq<T> filter(Predicate<? super T> p);

    /* (non-Javadoc)
     * @see com.aol.cyclops.monad.AnyM#map(java.util.function.Function)
     */
    @Override
    <R> AnyMSeq<R> map(Function<? super T, ? extends R> fn);

    /* (non-Javadoc)
     * @see com.aol.cyclops.monad.AnyM#peek(java.util.function.Consumer)
     */
    @Override
    default AnyMSeq<T> peek(final Consumer<? super T> c) {

        return map(i -> {
            c.accept(i);
            return i;
        });
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.monad.AnyM#flatten()
     */
    @Override
    <T1> AnyMSeq<T1> flatten();

    /* (non-Javadoc)
     * @see com.aol.cyclops.monad.AnyM#aggregate(com.aol.cyclops.monad.AnyM)
     */
    @Override
    AnyMSeq<List<T>> aggregate(AnyM<T> next);

    /**
     * Perform a two level nested internal iteration over this Stream and the supplied monad (allowing null handling, exception handling
     * etc to be injected, for example)
     * 
     * <pre>
     * {@code 
     * AnyM.fromArray(1,2,3)
    					.forEachAnyM2(a->AnyM.fromIntStream(IntStream.range(10,13)),
    								a->b->a+b);
    								
     * 
     *  //AnyM[11,14,12,15,13,16]
     * }
     * </pre>
     * 
     * 
     * @param monad Nested Monad to iterate over
     * @param yieldingFunction Function with pointers to the current element from both Streams that generates the new elements
     * @return LazyFutureStream with elements generated via nested iteration
     */
    <R1, R> AnyMSeq<R> forEach2(Function<? super T, ? extends AnyM<R1>> monad,
            Function<? super T, Function<? super R1, ? extends R>> yieldingFunction);

    /**
     * Perform a two level nested internal iteration over this Stream and the supplied monad (allowing null handling, exception handling
     * etc to be injected, for example)
     * 
     * <pre>
     * {@code 
     * AnyM.fromArray(1,2,3)
    					.forEach2(a->AnyM.fromIntStream(IntStream.range(10,13)),
    					            a->b-> a<3 && b>10,
    								a->b->a+b);
    								
     * 
     *  //AnyM[14,15]
     * }
     * </pre>
     * @param monad Nested Monad to iterate over
     * @param filterFunction Filter to apply over elements before passing non-filtered values to the yielding function
     * @param yieldingFunction Function with pointers to the current element from both monads that generates the new elements
     * @return
     */
    <R1, R> AnyMSeq<R> forEach2(Function<? super T, ? extends AnyM<R1>> monad, Function<? super T, Function<? super R1, Boolean>> filterFunction,
            Function<? super T, Function<? super R1, ? extends R>> yieldingFunction);

    /** 
     * Perform a three level nested internal iteration over this Stream and the supplied streams
      *<pre>
     * {@code 
     * AnyM.fromArray(1,2)
    					.forEach2(a->AnyM.fromIntStream(IntStream.range(10,13)),
    					(a->b->AnyM.fromArray(""+(a+b),"hello world"),
    								a->b->c->c+":"a+":"+b);
    								
     * 
     *  //AnyM[11:1:2,hello world:1:2,14:1:4,hello world:1:4,12:1:2,hello world:1:2,15:1:5,hello world:1:5]
     * }
     * </pre> 
     * @param monad1 Nested monad to flatMap over
     * @param monad2 Nested monad to flatMap over
     * @param filterFunction Filter to apply over elements before passing non-filtered values to the yielding function
     * @param yieldingFunction Function with pointers to the current element from both monads that generates the new elements
     * @return AnyM with elements generated via nested iteration
     */
    <R1, R2, R> AnyMSeq<R> forEach3(Function<? super T, ? extends AnyM<R1>> monad1,
            Function<? super T, Function<? super R1, ? extends AnyM<R2>>> monad2,
            Function<? super T, Function<? super R1, Function<? super R2, Boolean>>> filterFunction,
            Function<? super T, Function<? super R1, Function<? super R2, ? extends R>>> yieldingFunction);

    /**
     * Perform a three level nested internal iteration over this AnyM and the supplied monads
     *<pre>
     * {@code 
     * AnyM.fromArray(1,2,3)
    				.forEach3(a->AnyM.fromStream(IntStream.range(10,13)),
    					 a->b->AnyM.fromArray(""+(a+b),"hello world"),
    				         a->b->c-> c!=3,
    							a->b->c->c+":"a+":"+b);
    							
     * 
     *  //SequenceM[11:1:2,hello world:1:2,14:1:4,hello world:1:4,12:1:2,hello world:1:2,15:1:5,hello world:1:5]
     * }
    * </pre> 
     * 
     * @param monad1 Nested Stream to iterate over
     * @param monad2 Nested Stream to iterate over
     * @param yieldingFunction Function with pointers to the current element from both Monads that generates the new elements
     * @return AnyM with elements generated via nested iteration
     */
    <R1, R2, R> AnyMSeq<R> forEach3(Function<? super T, ? extends AnyM<R1>> monad1,
            Function<? super T, Function<? super R1, ? extends AnyM<R2>>> monad2,
            Function<? super T, Function<? super R1, Function<? super R2, ? extends R>>> yieldingFunction);

    /**
     * flatMap operation
      * 
     * AnyM follows the javaslang modified 'monad' laws https://gist.github.com/danieldietrich/71be006b355d6fbc0584
     * In particular left-identity becomes
     * Left identity: unit(a).flatMap(f) ≡ select(f.apply(a))
     * Or in plain English, if your flatMap function returns multiple values (such as flatMap by Stream) but the current Monad only can only hold one value,
     * only the first value is accepted.
     * 
     * Example 1 : multi-values are supported (AnyM wraps a Stream, List, Set etc)
     * <pre>
     * {@code 
     *   AnyM<Integer> anyM = AnyM.fromStream(Stream.of(1,2,3)).flatMap(i->AnyM.fromArray(i+1,i+2));
     *   
     *   //AnyM[Stream[2,3,3,4,4,5]]
     * }
     * </pre>
     * Example 2 : multi-values are not supported (AnyM wraps a Stream, List, Set etc)
     * <pre>
     * {@code 
     *   AnyM<Integer> anyM = AnyM.fromOptional(Optional.of(1)).flatMap(i->AnyM.fromArray(i+1,i+2));
     *   
     *   //AnyM[Optional[2]]
     * }
     * </pre>
     * @param fn flatMap function
     * @return  flatMapped AnyM
     */
    <R> AnyMSeq<R> flatMap(Function<? super T, ? extends AnyM<? extends R>> fn);

    /**
     * Apply function/s inside supplied Monad to data in current Monad
     * 
     * e.g. with Streams
     * <pre>{@code 
     * 
     * AnyM<Integer> applied =AnyM.fromStream(Stream.of(1,2,3))
     * 								.applyM(AnyM.fromStreamable(Streamable.of( (Integer a)->a+1 ,(Integer a) -> a*2)));
    
     	assertThat(applied.toList(),equalTo(Arrays.asList(2, 2, 3, 4, 4, 6)));
     }</pre>
     * 
     * with Optionals 
     * <pre>
     * {@code
     * 
     *  Any<Integer> applied =AnyM.fromOptional(Optional.of(2)).applyM(AnyM.fromOptional(Optional.of( (Integer a)->a+1)) );
    	assertThat(applied.toList(),equalTo(Arrays.asList(3)));
    	
    	}
    	</pre>
     * 
     * @param fn
     * @return
     */
    <R> AnyMSeq<R> applyM(AnyM<Function<? super T, ? extends R>> fn);

    /* (non-Javadoc)
     * @see com.aol.cyclops.monad.AnyM#unit(java.lang.Object)
     */
    @Override
    <T> AnyMSeq<T> unit(T value);

    /* (non-Javadoc)
     * @see com.aol.cyclops.monad.AnyM#empty()
     */
    @Override
    <T> AnyMSeq<T> empty();

    AnyMSeq<T> replicateM(int times);

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.AnyM#bind(java.util.function.Function)
     */
    @Override
    <R> AnyMSeq<R> bind(Function<? super T, ?> fn);

    /**
     * Convert a Stream of Monads to a Monad with a List applying the supplied function in the process
     * 
    <pre>{@code 
       Stream<CompletableFuture<Integer>> futures = createFutures();
       AnyMSeq<List<String>> futureList = AnyMonads.traverse(AsAnyMList.anyMList(futures), (Integer i) -> "hello" +i);
        }
        </pre>
     * 
     * @param seq Stream of Monads
     * @param fn Function to apply 
     * @return Monad with a list
     */
    public static <T, R> AnyMSeq<ListX<R>> traverse(final Collection<? extends AnyMSeq<T>> seq, final Function<? super T, ? extends R> fn) {

        return AnyMSeqImpl.from(new AnyMonads().traverse(seq, fn));
    }

    /**
     * Convert a Collection of Monads to a Monad with a List
     * 
     * <pre>
     * {@code
        List<CompletableFuture<Integer>> futures = createFutures();
        AnyMSeq<List<Integer>> futureList = AnyMonads.sequence(AsAnyMList.anyMList(futures));
    
       //where AnyM wraps  CompletableFuture<List<Integer>>
      }</pre>
     * 
     * @param seq Collection of monads to convert
     * @return Monad with a List
     */
    public static <T1> AnyMSeq<ListX<T1>> sequence(final Collection<? extends AnyMSeq<T1>> seq) {
        return AnyMSeqImpl.from(new AnyMonads().sequence(seq));
    }

    /**
     * Convert a Stream of Monads to a Monad with a Stream applying the supplied function in the process
     *
     */
    public static <T, R> AnyMSeq<Stream<R>> traverse(final Stream<AnyMSeq<T>> source, final Supplier<AnyMSeq<Stream<T>>> unitEmpty,
            final Function<? super T, ? extends R> fn) {
        return sequence(source, unitEmpty).map(s -> s.map(fn));
    }

    /**
     * Convert a Stream of Monads to a Monad with a Stream
     *
     */
    public static <T> AnyMSeq<Stream<T>> sequence(final Stream<AnyMSeq<T>> source, final Supplier<AnyMSeq<Stream<T>>> unitEmpty) {

        return Matchables.anyM(AnyM.sequence(source, unitEmpty))
                         .visit(s -> {
                             throw new IllegalStateException(
                                                             "unreachable");
                         } , v -> v);
    }

    /**
     * Lift a function so it accepts an AnyM and returns an AnyM (any monad)
     * AnyM view simplifies type related challenges.
     * 
     * @param fn
     * @return
     */
    public static <U, R> Function<AnyMSeq<U>, AnyMSeq<R>> liftM(final Function<? super U, ? extends R> fn) {
        return u -> u.map(input -> fn.apply(input));
    }

    /**
     * Lift a function so it accepts a Monad and returns a Monad (simplex view of a wrapped Monad)
     * AnyM view simplifies type related challenges. The actual native type is not specified here.
     * 
     * e.g.
     * 
     * <pre>{@code
     * 	BiFunction<AnyMSeq<Integer>,AnyMSeq<Integer>,AnyMSeq<Integer>> add = Monads.liftM2(this::add);
     *   
     *  Optional<Integer> result = add.apply(getBase(),getIncrease());
     *  
     *   private Integer add(Integer a, Integer b){
    			return a+b;
    	}
     * }</pre>
     * The add method has no null handling, but we can lift the method to Monadic form, and use Optionals to automatically handle null / empty value cases.
     * 
     * 
     * @param fn BiFunction to lift
     * @return Lifted BiFunction
     */
    public static <U1, U2, R> BiFunction<AnyMSeq<U1>, AnyMSeq<U2>, AnyMSeq<R>> liftM2(final BiFunction<? super U1, ? super U2, ? extends R> fn) {

        return (u1, u2) -> u1.bind(input1 -> u2.map(input2 -> fn.apply(input1, input2))
                                               .unwrap());
    }

    /**
     * Lift a jOOλ Function3  into Monadic form. A good use case it to take an existing method and lift it so it can accept and return monads
     * 
     * <pre>
     * {@code
     * Function3 <AnyMSeq<Double>,AnyMSeq<Entity>,AnyMSeq<String>,AnyMSeq<Integer>> fn = liftM3(this::myMethod);
     *    
     * }
     * </pre>
     * 
     * Now we can execute the Method with Streams, Optional, Futures, Try's etc to transparently inject iteration, null handling, async execution and / or error handling
     * 
     * @param fn Function to lift
     * @return Lifted function
     */
    public static <U1, U2, U3, R> Function3<AnyMSeq<U1>, AnyMSeq<U2>, AnyMSeq<U3>, AnyMSeq<R>> liftM3(
            final Function3<? super U1, ? super U2, ? super U3, ? extends R> fn) {
        return (u1, u2, u3) -> u1.bind(input1 -> u2.bind(input2 -> u3.map(input3 -> fn.apply(input1, input2, input3)))
                                                   .unwrap());
    }

    /**
     * Lift a TriFunction into Monadic form. A good use case it to take an existing method and lift it so it can accept and return monads
     * 
     * <pre>
     * {@code
     * TriFunction<AnyMSeq<Double>,AnyMSeq<Entity>,AnyMSeq<String>,AnyMSeq<Integer>> fn = liftM3(this::myMethod);
     *    
     * }
     * </pre>
     * 
     * Now we can execute the Method with Streams, Optional, Futures, Try's etc to transparently inject iteration, null handling, async execution and / or error handling
     * 
     * @param fn Function to lift
     * @return Lifted function
     */
    public static <U1, U2, U3, R> TriFunction<AnyMSeq<U1>, AnyMSeq<U2>, AnyMSeq<U3>, AnyMSeq<R>> liftM3Cyclops(
            final TriFunction<? super U1, ? super U2, ? super U3, ? extends R> fn) {
        return (u1, u2, u3) -> u1.bind(input1 -> u2.bind(input2 -> u3.map(input3 -> fn.apply(input1, input2, input3))
                                                                     .unwrap())
                                                   .unwrap());
    }

    /**
     * Lift a  jOOλ Function4 into Monadic form.
     * 
     * @param fn Quad funciton to lift
     * @return Lifted Quad function
     */
    public static <U1, U2, U3, U4, R> Function4<AnyMSeq<U1>, AnyMSeq<U2>, AnyMSeq<U3>, AnyMSeq<U4>, AnyMSeq<R>> liftM4(
            final Function4<? super U1, ? super U2, ? super U3, ? super U4, ? extends R> fn) {

        return (u1, u2, u3, u4) -> u1.bind(input1 -> u2.bind(input2 -> u3.bind(input3 -> u4.map(input4 -> fn.apply(input1, input2, input3, input4))))
                                                       .unwrap());
    }

    /**
     * Lift a QuadFunction into Monadic form.
     * 
     * @param fn Quad funciton to lift
     * @return Lifted Quad function
     */
    public static <U1, U2, U3, U4, R> QuadFunction<AnyMSeq<U1>, AnyMSeq<U2>, AnyMSeq<U3>, AnyMSeq<U4>, AnyMSeq<R>> liftM4Cyclops(
            final QuadFunction<? super U1, ? super U2, ? super U3, ? super U4, ? extends R> fn) {

        return (u1, u2, u3, u4) -> u1.bind(input1 -> u2.bind(input2 -> u3.bind(input3 -> u4.map(input4 -> fn.apply(input1, input2, input3, input4))
                                                                                           .unwrap())
                                                                         .unwrap())
                                                       .unwrap());
    }

    /**
     * Lift a  jOOλ Function5 (5 parameters) into Monadic form
     * 
     * @param fn Function to lift
     * @return Lifted Function
     */
    public static <U1, U2, U3, U4, U5, R> Function5<AnyMSeq<U1>, AnyMSeq<U2>, AnyMSeq<U3>, AnyMSeq<U4>, AnyMSeq<U5>, AnyMSeq<R>> liftM5(
            final Function5<? super U1, ? super U2, ? super U3, ? super U4, ? super U5, ? extends R> fn) {

        return (u1, u2, u3, u4,
                u5) -> u1.bind(input1 -> u2.bind(input2 -> u3.bind(input3 -> u4.bind(input4 -> u5.map(input5 -> fn.apply(input1, input2, input3,
                                                                                                                         input4, input5)))))
                                           .unwrap());
    }

    /**
     * Lift a QuintFunction (5 parameters) into Monadic form
     * 
     * @param fn Function to lift
     * @return Lifted Function
     */
    public static <U1, U2, U3, U4, U5, R> QuintFunction<AnyMSeq<U1>, AnyMSeq<U2>, AnyMSeq<U3>, AnyMSeq<U4>, AnyMSeq<U5>, AnyMSeq<R>> liftM5Cyclops(
            final QuintFunction<? super U1, ? super U2, ? super U3, ? super U4, ? super U5, ? extends R> fn) {

        return (u1, u2, u3, u4,
                u5) -> u1.bind(input1 -> u2.bind(input2 -> u3.bind(input3 -> u4.bind(input4 -> u5.map(input5 -> fn.apply(input1, input2, input3,
                                                                                                                         input4, input5))
                                                                                                 .unwrap())
                                                                               .unwrap())
                                                             .unwrap())
                                           .unwrap());
    }

    /**
     * Lift a Curried Function {@code(2 levels a->b->fn.apply(a,b) )} into Monadic form
     * 
     * @param fn Function to lift
     * @return Lifted function 
     */
    public static <U1, U2, R> Function<AnyMSeq<U1>, Function<AnyMSeq<U2>, AnyMSeq<R>>> liftM2(final Function<U1, Function<U2, R>> fn) {
        return u1 -> u2 -> u1.bind(input1 -> u2.map(input2 -> fn.apply(input1)
                                                                .apply(input2))
                                               .unwrap());

    }

    /**
     * Lift a Curried Function {@code(3 levels a->b->c->fn.apply(a,b,c) )} into Monadic form
     * 
     * @param fn Function to lift
     * @return Lifted function 
     */
    public static <U1, U2, U3, R> Function<AnyMSeq<U1>, Function<AnyMSeq<U2>, Function<AnyMSeq<U3>, AnyMSeq<R>>>> liftM3(
            final Function<? super U1, Function<? super U2, Function<? super U3, ? extends R>>> fn) {
        return u1 -> u2 -> u3 -> u1.bind(input1 -> u2.bind(input2 -> u3.map(input3 -> fn.apply(input1)
                                                                                        .apply(input2)
                                                                                        .apply(input3)))
                                                     .unwrap());
    }

    /**
     * Lift a Curried Function {@code(4 levels a->b->c->d->fn.apply(a,b,c,d) )} into Monadic form
     * 
     * @param fn Function to lift
     * @return Lifted function 
     */
    public static <U1, U2, U3, U4, R> Function<AnyMSeq<U1>, Function<AnyMSeq<U2>, Function<AnyMSeq<U3>, Function<AnyMSeq<U4>, AnyMSeq<R>>>>> liftM4(
            final Function<? super U1, Function<? super U2, Function<? super U3, Function<? super U4, ? extends R>>>> fn) {

        return u1 -> u2 -> u3 -> u4 -> u1.bind(input1 -> u2.bind(input2 -> u3.bind(input3 -> u4.map(input4 -> fn.apply(input1)
                                                                                                                .apply(input2)
                                                                                                                .apply(input3)
                                                                                                                .apply(input4))))
                                                           .unwrap());
    }

    /**
     * Lift a Curried Function {@code (5 levels a->b->c->d->e->fn.apply(a,b,c,d,e) ) }into Monadic form
     * 
     * @param fn Function to lift
     * @return Lifted function 
     */
    public static <U1, U2, U3, U4, U5, R> Function<AnyMSeq<U1>, Function<AnyMSeq<U2>, Function<AnyMSeq<U3>, Function<AnyMSeq<U4>, Function<AnyMSeq<U5>, AnyMSeq<R>>>>>> liftM5(
            final Function<? super U1, Function<? super U2, Function<? super U3, Function<? super U4, Function<? super U5, ? extends R>>>>> fn) {

        return u1 -> u2 -> u3 -> u4 -> u5 -> u1.bind(input1 -> u2.bind(input2 -> u3.bind(input3 -> u4.bind(input4 -> u5.map(input5 -> fn.apply(input1)
                                                                                                                                        .apply(input2)
                                                                                                                                        .apply(input3)
                                                                                                                                        .apply(input4)
                                                                                                                                        .apply(input5)))))
                                                                 .unwrap());
    }

}
