package com.oath.cyclops.reactor.hkt;


import com.oath.cyclops.hkt.Higher;
import cyclops.companion.reactor.Fluxs;
import cyclops.monads.ReactorWitness.flux;
import cyclops.monads.WitnessType;
import cyclops.monads.transformers.StreamT;
import cyclops.reactive.ReactiveSeq;
import cyclops.typeclasses.Active;
import cyclops.typeclasses.InstanceDefinitions;
import cyclops.typeclasses.Nested;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.Cancellation;
import reactor.core.publisher.*;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.TimedScheduler;
import reactor.util.function.Tuple2;

import java.time.Duration;
import java.util.*;
import java.util.function.*;
import java.util.logging.Level;
import java.util.stream.Collector;
import java.util.stream.Stream;

/**
 * Simulates Higher Kinded Types for Reactor Flux's
 *
 * FluxKind is a Flux and a Higher Kinded Type (flux,T)
 *
 * @author johnmcclean
 *
 * @param <T> Data type stored within the Flux
 */


public final class FluxKind<T> implements Higher<flux, T>, Publisher<T> {

    private FluxKind(Flux<T> boxed) {
        this.boxed = boxed;
    }

    public <R> FluxKind<R> fold(Function<? super Flux<?  super T>,? extends Flux<R>> op){
        return widen(op.apply(boxed));
    }
    public Active<flux,T> allTypeclasses(){
        return Active.of(this, Fluxs.Instances.definitions());
    }

    public static <T> Higher<flux,T> widenK(final Flux<T> completableList) {

        return new FluxKind<>(
                completableList);
    }
    public <W2,R> Nested<flux,W2,R> mapM(java.util.function.Function<? super T,? extends Higher<W2,R>> fn, InstanceDefinitions<W2> defs){
        return Fluxs.mapM(boxed,fn,defs);
    }

    public <W extends WitnessType<W>> StreamT<W, T> liftM(W witness) {
        return Fluxs.liftM(boxed,witness);
    }


    /**
     * Construct a HKT encoded completed Flux
     *
     * @param value To encode inside a HKT encoded Flux
     * @return Completed HKT encoded FFlux
     */
    public static <T> FluxKind<T> just(T value){

        return widen(Flux.just(value));
    }
    public static <T> FluxKind<T> just(T... values){

            return widen(Flux.just(values));
    }
    public static <T> FluxKind<T> empty(){
        return widen(Flux.empty());
    }

    /**
     * Convert a Flux to a simulated HigherKindedType that captures Flux nature
     * and Flux element data type separately. Recover via @see FluxKind#narrow
     *
     * If the supplied Flux implements FluxKind it is returned already, otherwise it
     * is wrapped into a Flux implementation that does implement FluxKind
     *
     * @param completableFlux Flux to widen to a FluxKind
     * @return FluxKind encoding HKT info about Fluxs
     */
    public static <T> FluxKind<T> widen(final Flux<T> completableFlux) {

        return new FluxKind<T>(
                         completableFlux);
    }
    /**
     * Widen a FluxKind nested inside another HKT encoded type
     *
     * @param flux HTK encoded type containing  a Flux to widen
     * @return HKT encoded type with a widened Flux
     */
    public static <C2,T> Higher<C2, Higher<flux,T>> widen2(Higher<C2, FluxKind<T>> flux){
        //a functor could be used (if C2 is a functor / one exists for C2 type) instead of casting
        //cast seems safer as Higher<StreamType.µ,T> must be a StreamType
        return (Higher)flux;
    }
    public static <T> FluxKind<T> widen(final Publisher<T> completableFlux) {

        return new FluxKind<T>(Flux.from(
                         completableFlux));
    }


    /**
     * Convert the raw Higher Kinded Type for FluxKind types into the FluxKind type definition class
     *
     * @param future HKT encoded list into a FluxKind
     * @return FluxKind
     */
    public static <T> FluxKind<T> narrowK(final Higher<flux, T> future) {
       return (FluxKind<T>)future;
    }

    /**
     * Convert the HigherKindedType definition for a Flux into
     *
     * @param completableFlux Type Constructor to convert back into narrowed type
     * @return Flux from Higher Kinded Type
     */
    public static <T> Flux<T> narrow(final Higher<flux, T> completableFlux) {

            return ((FluxKind<T>)completableFlux).narrow();



    }



        private final Flux<T> boxed;

        /**
         * @return wrapped Flux
         */
        public Flux<T> narrow() {
            return boxed;
        }


        public ReactiveSeq<T> toReactiveSeq(){
            return ReactiveSeq.fromPublisher(boxed);
        }
        /**
         * @param s
         * @see org.reactivestreams.Publisher#subscribe(org.reactivestreams.Subscriber)
         */
        public void subscribe(Subscriber<? super T> s) {
            boxed.subscribe(s);
        }
        /**
         * @return
         * @see java.lang.Object#hashCode()
         */
        public int hashCode() {
            return boxed.hashCode();
        }
        /**
         * @param obj
         * @return
         * @see java.lang.Object#equals(java.lang.Object)
         */
        public boolean equals(Object obj) {
            return boxed.equals(obj);
        }

        /**
         * @return
         * @see reactor.core.publisher.Flux#toString()
         */
        public String toString() {
            return "[FluxKind "+boxed.toString() + "]";
        }
        /**
         * @param predicate
         * @return
         * @see reactor.core.publisher.Flux#all(java.util.function.Predicate)
         */
        public final Mono<Boolean> all(Predicate<? super T> predicate) {
            return boxed.all(predicate);
        }
        /**
         * @param predicate
         * @return
         * @see reactor.core.publisher.Flux#any(java.util.function.Predicate)
         */
        public final Mono<Boolean> any(Predicate<? super T> predicate) {
            return boxed.any(predicate);
        }
        /**
         * @param transformer
         * @return
         * @see reactor.core.publisher.Flux#as(java.util.function.Function)
         */
        public final <P> P as(Function<? super Flux<T>, P> transformer) {
            return boxed.as(transformer);
        }
        /**
         * @return
         * @see reactor.core.publisher.Flux#awaitOnSubscribe()
         */
        public final Flux<T> awaitOnSubscribe() {
            return boxed.awaitOnSubscribe();
        }
        /**
         * @return
         * @see reactor.core.publisher.Flux#blockFirst()
         */
        public final T blockFirst() {
            return boxed.blockFirst();
        }
        /**
         * @param d
         * @return
         * @see reactor.core.publisher.Flux#blockFirst(java.time.Duration)
         */
        public final T blockFirst(Duration d) {
            return boxed.blockFirst(d);
        }
        /**
         * @param timeout
         * @return
         * @see reactor.core.publisher.Flux#blockFirstMillis(long)
         */
        public final T blockFirstMillis(long timeout) {
            return boxed.blockFirstMillis(timeout);
        }
        /**
         * @return
         * @see reactor.core.publisher.Flux#blockLast()
         */
        public final T blockLast() {
            return boxed.blockLast();
        }
        /**
         * @param d
         * @return
         * @see reactor.core.publisher.Flux#blockLast(java.time.Duration)
         */
        public final T blockLast(Duration d) {
            return boxed.blockLast(d);
        }
        /**
         * @param timeout
         * @return
         * @see reactor.core.publisher.Flux#blockLastMillis(long)
         */
        public final T blockLastMillis(long timeout) {
            return boxed.blockLastMillis(timeout);
        }
        /**
         * @return
         * @see reactor.core.publisher.Flux#buffer()
         */
        public final Flux<List<T>> buffer() {
            return boxed.buffer();
        }
        /**
         * @param maxSize
         * @return
         * @see reactor.core.publisher.Flux#buffer(int)
         */
        public final Flux<List<T>> buffer(int maxSize) {
            return boxed.buffer(maxSize);
        }
        /**
         * @param maxSize
         * @param bufferSupplier
         * @return
         * @see reactor.core.publisher.Flux#buffer(int, java.util.function.Supplier)
         */
        public final <C extends Collection<? super T>> Flux<C> buffer(int maxSize, Supplier<C> bufferSupplier) {
            return boxed.buffer(maxSize, bufferSupplier);
        }
        /**
         * @param maxSize
         * @param skip
         * @return
         * @see reactor.core.publisher.Flux#buffer(int, int)
         */
        public final Flux<List<T>> buffer(int maxSize, int skip) {
            return boxed.buffer(maxSize, skip);
        }
        /**
         * @param maxSize
         * @param skip
         * @param bufferSupplier
         * @return
         * @see reactor.core.publisher.Flux#buffer(int, int, java.util.function.Supplier)
         */
        public final <C extends Collection<? super T>> Flux<C> buffer(int maxSize, int skip,
                Supplier<C> bufferSupplier) {
            return boxed.buffer(maxSize, skip, bufferSupplier);
        }
        /**
         * @param other
         * @return
         * @see reactor.core.publisher.Flux#buffer(org.reactivestreams.Publisher)
         */
        public final Flux<List<T>> buffer(Publisher<?> other) {
            return boxed.buffer(other);
        }
        /**
         * @param other
         * @param bufferSupplier
         * @return
         * @see reactor.core.publisher.Flux#buffer(org.reactivestreams.Publisher, java.util.function.Supplier)
         */
        public final <C extends Collection<? super T>> Flux<C> buffer(Publisher<?> other, Supplier<C> bufferSupplier) {
            return boxed.buffer(other, bufferSupplier);
        }
        /**
         * @param bucketOpening
         * @param closeSelector
         * @return
         * @see reactor.core.publisher.Flux#buffer(org.reactivestreams.Publisher, java.util.function.Function)
         */
        public final <U, V> Flux<List<T>> buffer(Publisher<U> bucketOpening,
                Function<? super U, ? extends Publisher<V>> closeSelector) {
            return boxed.buffer(bucketOpening, closeSelector);
        }
        /**
         * @param bucketOpening
         * @param closeSelector
         * @param bufferSupplier
         * @return
         * @see reactor.core.publisher.Flux#buffer(org.reactivestreams.Publisher, java.util.function.Function, java.util.function.Supplier)
         */
        public final <U, V, C extends Collection<? super T>> Flux<C> buffer(Publisher<U> bucketOpening,
                Function<? super U, ? extends Publisher<V>> closeSelector, Supplier<C> bufferSupplier) {
            return boxed.buffer(bucketOpening, closeSelector, bufferSupplier);
        }
        /**
         * @param timespan
         * @return
         * @see reactor.core.publisher.Flux#buffer(java.time.Duration)
         */
        public final Flux<List<T>> buffer(Duration timespan) {
            return boxed.buffer(timespan);
        }
        /**
         * @param timespan
         * @param timeshift
         * @return
         * @see reactor.core.publisher.Flux#buffer(java.time.Duration, java.time.Duration)
         */
        public final Flux<List<T>> buffer(Duration timespan, Duration timeshift) {
            return boxed.buffer(timespan, timeshift);
        }
        /**
         * @param maxSize
         * @param timespan
         * @return
         * @see reactor.core.publisher.Flux#buffer(int, java.time.Duration)
         */
        public final Flux<List<T>> buffer(int maxSize, Duration timespan) {
            return boxed.buffer(maxSize, timespan);
        }
        /**
         * @param maxSize
         * @param timespan
         * @param bufferSupplier
         * @return
         * @see reactor.core.publisher.Flux#buffer(int, java.time.Duration, java.util.function.Supplier)
         */
        public final <C extends Collection<? super T>> Flux<C> buffer(int maxSize, Duration timespan,
                Supplier<C> bufferSupplier) {
            return boxed.buffer(maxSize, timespan, bufferSupplier);
        }
        /**
         * @param timespan
         * @return
         * @see reactor.core.publisher.Flux#bufferMillis(long)
         */
        public final Flux<List<T>> bufferMillis(long timespan) {
            return boxed.bufferMillis(timespan);
        }
        /**
         * @param timespan
         * @param timer
         * @return
         * @see reactor.core.publisher.Flux#bufferMillis(long, reactor.core.scheduler.TimedScheduler)
         */
        public final Flux<List<T>> bufferMillis(long timespan, TimedScheduler timer) {
            return boxed.bufferMillis(timespan, timer);
        }
        /**
         * @param timespan
         * @param timeshift
         * @return
         * @see reactor.core.publisher.Flux#bufferMillis(long, long)
         */
        public final Flux<List<T>> bufferMillis(long timespan, long timeshift) {
            return boxed.bufferMillis(timespan, timeshift);
        }
        /**
         * @param timespan
         * @param timeshift
         * @param timer
         * @return
         * @see reactor.core.publisher.Flux#bufferMillis(long, long, reactor.core.scheduler.TimedScheduler)
         */
        public final Flux<List<T>> bufferMillis(long timespan, long timeshift, TimedScheduler timer) {
            return boxed.bufferMillis(timespan, timeshift, timer);
        }
        /**
         * @param maxSize
         * @param timespan
         * @return
         * @see reactor.core.publisher.Flux#bufferMillis(int, long)
         */
        public final Flux<List<T>> bufferMillis(int maxSize, long timespan) {
            return boxed.bufferMillis(maxSize, timespan);
        }
        /**
         * @param maxSize
         * @param timespan
         * @param timer
         * @return
         * @see reactor.core.publisher.Flux#bufferMillis(int, long, reactor.core.scheduler.TimedScheduler)
         */
        public final Flux<List<T>> bufferMillis(int maxSize, long timespan, TimedScheduler timer) {
            return boxed.bufferMillis(maxSize, timespan, timer);
        }
        /**
         * @param maxSize
         * @param timespan
         * @param timer
         * @param bufferSupplier
         * @return
         * @see reactor.core.publisher.Flux#bufferMillis(int, long, reactor.core.scheduler.TimedScheduler, java.util.function.Supplier)
         */
        public final <C extends Collection<? super T>> Flux<C> bufferMillis(int maxSize, long timespan,
                TimedScheduler timer, Supplier<C> bufferSupplier) {
            return boxed.bufferMillis(maxSize, timespan, timer, bufferSupplier);
        }
        /**
         * @return
         * @see reactor.core.publisher.Flux#cache()
         */
        public final Flux<T> cache() {
            return boxed.cache();
        }
        /**
         * @param history
         * @return
         * @see reactor.core.publisher.Flux#cache(int)
         */
        public final Flux<T> cache(int history) {
            return boxed.cache(history);
        }
        /**
         * @param ttl
         * @return
         * @see reactor.core.publisher.Flux#cache(java.time.Duration)
         */
        public final Flux<T> cache(Duration ttl) {
            return boxed.cache(ttl);
        }
        /**
         * @param history
         * @param ttl
         * @return
         * @see reactor.core.publisher.Flux#cache(int, java.time.Duration)
         */
        public final Flux<T> cache(int history, Duration ttl) {
            return boxed.cache(history, ttl);
        }
        /**
         * @param clazz
         * @return
         * @see reactor.core.publisher.Flux#cast(java.lang.Class)
         */
        public final <E> Flux<E> cast(Class<E> clazz) {
            return boxed.cast(clazz);
        }
        /**
         * @param scheduler
         * @return
         * @see reactor.core.publisher.Flux#cancelOn(reactor.core.scheduler.Scheduler)
         */
        public final Flux<T> cancelOn(Scheduler scheduler) {
            return boxed.cancelOn(scheduler);
        }
        /**
         * @param containerSupplier
         * @param collector
         * @return
         * @see reactor.core.publisher.Flux#collect(java.util.function.Supplier, java.util.function.BiConsumer)
         */
        public final <E> Mono<E> collect(Supplier<E> containerSupplier, BiConsumer<E, ? super T> collector) {
            return boxed.collect(containerSupplier, collector);
        }
        /**
         * @param collector
         * @return
         * @see reactor.core.publisher.Flux#collect(java.util.stream.Collector)
         */
        public final <R, A> Mono<R> collect(Collector<T, A, R> collector) {
            return boxed.collect(collector);
        }
        /**
         * @return
         * @see reactor.core.publisher.Flux#collectList()
         */
        public final Mono<List<T>> collectList() {
            return boxed.collectList();
        }
        /**
         * @param keyExtractor
         * @return
         * @see reactor.core.publisher.Flux#collectMap(java.util.function.Function)
         */
        public final <K> Mono<Map<K, T>> collectMap(Function<? super T, ? extends K> keyExtractor) {
            return boxed.collectMap(keyExtractor);
        }
        /**
         * @param keyExtractor
         * @param valueExtractor
         * @return
         * @see reactor.core.publisher.Flux#collectMap(java.util.function.Function, java.util.function.Function)
         */
        public final <K, V> Mono<Map<K, V>> collectMap(Function<? super T, ? extends K> keyExtractor,
                Function<? super T, ? extends V> valueExtractor) {
            return boxed.collectMap(keyExtractor, valueExtractor);
        }
        /**
         * @param keyExtractor
         * @param valueExtractor
         * @param mapSupplier
         * @return
         * @see reactor.core.publisher.Flux#collectMap(java.util.function.Function, java.util.function.Function, java.util.function.Supplier)
         */
        public final <K, V> Mono<Map<K, V>> collectMap(Function<? super T, ? extends K> keyExtractor,
                Function<? super T, ? extends V> valueExtractor, Supplier<Map<K, V>> mapSupplier) {
            return boxed.collectMap(keyExtractor, valueExtractor, mapSupplier);
        }
        /**
         * @param keyExtractor
         * @return
         * @see reactor.core.publisher.Flux#collectMultimap(java.util.function.Function)
         */
        public final <K> Mono<Map<K, Collection<T>>> collectMultimap(Function<? super T, ? extends K> keyExtractor) {
            return boxed.collectMultimap(keyExtractor);
        }
        /**
         * @param keyExtractor
         * @param valueExtractor
         * @return
         * @see reactor.core.publisher.Flux#collectMultimap(java.util.function.Function, java.util.function.Function)
         */
        public final <K, V> Mono<Map<K, Collection<V>>> collectMultimap(Function<? super T, ? extends K> keyExtractor,
                Function<? super T, ? extends V> valueExtractor) {
            return boxed.collectMultimap(keyExtractor, valueExtractor);
        }
        /**
         * @param keyExtractor
         * @param valueExtractor
         * @param mapSupplier
         * @return
         * @see reactor.core.publisher.Flux#collectMultimap(java.util.function.Function, java.util.function.Function, java.util.function.Supplier)
         */
        public final <K, V> Mono<Map<K, Collection<V>>> collectMultimap(Function<? super T, ? extends K> keyExtractor,
                Function<? super T, ? extends V> valueExtractor, Supplier<Map<K, Collection<V>>> mapSupplier) {
            return boxed.collectMultimap(keyExtractor, valueExtractor, mapSupplier);
        }
        /**
         * @return
         * @see reactor.core.publisher.Flux#collectSortedList()
         */
        public final Mono<List<T>> collectSortedList() {
            return boxed.collectSortedList();
        }
        /**
         * @param comparator
         * @return
         * @see reactor.core.publisher.Flux#collectSortedList(java.util.Comparator)
         */
        public final Mono<List<T>> collectSortedList(Comparator<? super T> comparator) {
            return boxed.collectSortedList(comparator);
        }
        /**
         * @param transformer
         * @return
         * @see reactor.core.publisher.Flux#compose(java.util.function.Function)
         */
        public final <V> Flux<V> compose(Function<? super Flux<T>, ? extends Publisher<V>> transformer) {
            return boxed.compose(transformer);
        }
        /**
         * @param mapper
         * @return
         * @see reactor.core.publisher.Flux#concatMap(java.util.function.Function)
         */
        public final <V> Flux<V> concatMap(Function<? super T, ? extends Publisher<? extends V>> mapper) {
            return boxed.concatMap(mapper);
        }
        /**
         * @param mapper
         * @param prefetch
         * @return
         * @see reactor.core.publisher.Flux#concatMap(java.util.function.Function, int)
         */
        public final <V> Flux<V> concatMap(Function<? super T, ? extends Publisher<? extends V>> mapper, int prefetch) {
            return boxed.concatMap(mapper, prefetch);
        }
        /**
         * @param mapper
         * @return
         * @see reactor.core.publisher.Flux#concatMapDelayError(java.util.function.Function)
         */
        public final <V> Flux<V> concatMapDelayError(Function<? super T, Publisher<? extends V>> mapper) {
            return boxed.concatMapDelayError(mapper);
        }
        /**
         * @param mapper
         * @param prefetch
         * @return
         * @see reactor.core.publisher.Flux#concatMapDelayError(java.util.function.Function, int)
         */
        public final <V> Flux<V> concatMapDelayError(Function<? super T, ? extends Publisher<? extends V>> mapper,
                int prefetch) {
            return boxed.concatMapDelayError(mapper, prefetch);
        }
        /**
         * @param mapper
         * @param delayUntilEnd
         * @param prefetch
         * @return
         * @see reactor.core.publisher.Flux#concatMapDelayError(java.util.function.Function, boolean, int)
         */
        public final <V> Flux<V> concatMapDelayError(Function<? super T, ? extends Publisher<? extends V>> mapper,
                boolean delayUntilEnd, int prefetch) {
            return boxed.concatMapDelayError(mapper, delayUntilEnd, prefetch);
        }
        /**
         * @param mapper
         * @return
         * @see reactor.core.publisher.Flux#concatMapIterable(java.util.function.Function)
         */
        public final <R> Flux<R> concatMapIterable(Function<? super T, ? extends Iterable<? extends R>> mapper) {
            return boxed.concatMapIterable(mapper);
        }
        /**
         * @param mapper
         * @param prefetch
         * @return
         * @see reactor.core.publisher.Flux#concatMapIterable(java.util.function.Function, int)
         */
        public final <R> Flux<R> concatMapIterable(Function<? super T, ? extends Iterable<? extends R>> mapper,
                int prefetch) {
            return boxed.concatMapIterable(mapper, prefetch);
        }
        /**
         * @param other
         * @return
         * @see reactor.core.publisher.Flux#concatWith(org.reactivestreams.Publisher)
         */
        public final Flux<T> concatWith(Publisher<? extends T> other) {
            return boxed.concatWith(other);
        }
        /**
         * @return
         * @see reactor.core.publisher.Flux#count()
         */
        public final Mono<Long> count() {
            return boxed.count();
        }
        /**
         * @param defaultV
         * @return
         * @see reactor.core.publisher.Flux#defaultIfEmpty(java.lang.Object)
         */
        public final Flux<T> defaultIfEmpty(T defaultV) {
            return boxed.defaultIfEmpty(defaultV);
        }
        /**
         * @param delay
         * @return
         * @see reactor.core.publisher.Flux#delay(java.time.Duration)
         */
        public final Flux<T> delay(Duration delay) {
            return boxed.delay(delay);
        }
        /**
         * @param delay
         * @return
         * @see reactor.core.publisher.Flux#delayMillis(long)
         */
        public final Flux<T> delayMillis(long delay) {
            return boxed.delayMillis(delay);
        }
        /**
         * @param delay
         * @param timer
         * @return
         * @see reactor.core.publisher.Flux#delayMillis(long, reactor.core.scheduler.TimedScheduler)
         */
        public final Flux<T> delayMillis(long delay, TimedScheduler timer) {
            return boxed.delayMillis(delay, timer);
        }
        /**
         * @param delay
         * @return
         * @see reactor.core.publisher.Flux#delaySubscription(java.time.Duration)
         */
        public final Flux<T> delaySubscription(Duration delay) {
            return boxed.delaySubscription(delay);
        }
        /**
         * @param subscriptionDelay
         * @return
         * @see reactor.core.publisher.Flux#delaySubscription(org.reactivestreams.Publisher)
         */
        public final <U> Flux<T> delaySubscription(Publisher<U> subscriptionDelay) {
            return boxed.delaySubscription(subscriptionDelay);
        }
        /**
         * @param delay
         * @return
         * @see reactor.core.publisher.Flux#delaySubscriptionMillis(long)
         */
        public final Flux<T> delaySubscriptionMillis(long delay) {
            return boxed.delaySubscriptionMillis(delay);
        }
        /**
         * @param delay
         * @param timer
         * @return
         * @see reactor.core.publisher.Flux#delaySubscriptionMillis(long, reactor.core.scheduler.TimedScheduler)
         */
        public final Flux<T> delaySubscriptionMillis(long delay, TimedScheduler timer) {
            return boxed.delaySubscriptionMillis(delay, timer);
        }
        /**
         * @return
         * @see reactor.core.publisher.Flux#dematerialize()
         */
        public final <X> Flux<X> dematerialize() {
            return boxed.dematerialize();
        }
        /**
         * @return
         * @see reactor.core.publisher.Flux#distinct()
         */
        public final Flux<T> distinct() {
            return boxed.distinct();
        }
        /**
         * @param keySelector
         * @return
         * @see reactor.core.publisher.Flux#distinct(java.util.function.Function)
         */
        public final <V> Flux<T> distinct(Function<? super T, ? extends V> keySelector) {
            return boxed.distinct(keySelector);
        }
        /**
         * @return
         * @see reactor.core.publisher.Flux#distinctUntilChanged()
         */
        public final Flux<T> distinctUntilChanged() {
            return boxed.distinctUntilChanged();
        }
        /**
         * @param keySelector
         * @return
         * @see reactor.core.publisher.Flux#distinctUntilChanged(java.util.function.Function)
         */
        public final <V> Flux<T> distinctUntilChanged(Function<? super T, ? extends V> keySelector) {
            return boxed.distinctUntilChanged(keySelector);
        }
        /**
         * @param afterTerminate
         * @return
         * @see reactor.core.publisher.Flux#doAfterTerminate(java.lang.Runnable)
         */
        public final Flux<T> doAfterTerminate(Runnable afterTerminate) {
            return boxed.doAfterTerminate(afterTerminate);
        }
        /**
         * @param onCancel
         * @return
         * @see reactor.core.publisher.Flux#doOnCancel(java.lang.Runnable)
         */
        public final Flux<T> doOnCancel(Runnable onCancel) {
            return boxed.doOnCancel(onCancel);
        }
        /**
         * @param onComplete
         * @return
         * @see reactor.core.publisher.Flux#doOnComplete(java.lang.Runnable)
         */
        public final Flux<T> doOnComplete(Runnable onComplete) {
            return boxed.doOnComplete(onComplete);
        }
        /**
         * @param onError
         * @return
         * @see reactor.core.publisher.Flux#doOnError(java.util.function.Consumer)
         */
        public final Flux<T> doOnError(Consumer<? super Throwable> onError) {
            return boxed.doOnError(onError);
        }
        /**
         * @param exceptionType
         * @param onError
         * @return
         * @see reactor.core.publisher.Flux#doOnError(java.lang.Class, java.util.function.Consumer)
         */
        public final <E extends Throwable> Flux<T> doOnError(Class<E> exceptionType, Consumer<? super E> onError) {
            return boxed.doOnError(exceptionType, onError);
        }
        /**
         * @param predicate
         * @param onError
         * @return
         * @see reactor.core.publisher.Flux#doOnError(java.util.function.Predicate, java.util.function.Consumer)
         */
        public final Flux<T> doOnError(Predicate<? super Throwable> predicate, Consumer<? super Throwable> onError) {
            return boxed.doOnError(predicate, onError);
        }
        /**
         * @param onNext
         * @return
         * @see reactor.core.publisher.Flux#doOnNext(java.util.function.Consumer)
         */
        public final Flux<T> doOnNext(Consumer<? super T> onNext) {
            return boxed.doOnNext(onNext);
        }
        /**
         * @param consumer
         * @return
         * @see reactor.core.publisher.Flux#doOnRequest(java.util.function.LongConsumer)
         */
        public final Flux<T> doOnRequest(LongConsumer consumer) {
            return boxed.doOnRequest(consumer);
        }
        /**
         * @param onSubscribe
         * @return
         * @see reactor.core.publisher.Flux#doOnSubscribe(java.util.function.Consumer)
         */
        public final Flux<T> doOnSubscribe(Consumer<? super Subscription> onSubscribe) {
            return boxed.doOnSubscribe(onSubscribe);
        }
        /**
         * @param onTerminate
         * @return
         * @see reactor.core.publisher.Flux#doOnTerminate(java.lang.Runnable)
         */
        public final Flux<T> doOnTerminate(Runnable onTerminate) {
            return boxed.doOnTerminate(onTerminate);
        }
        /**
         * @return
         * @see reactor.core.publisher.Flux#elapsed()
         */
        public final Flux<Tuple2<Long, T>> elapsed() {
            return boxed.elapsed();
        }
        /**
         * @param scheduler
         * @return
         * @see reactor.core.publisher.Flux#elapsed(reactor.core.scheduler.TimedScheduler)
         */
        public final Flux<Tuple2<Long, T>> elapsed(TimedScheduler scheduler) {
            return boxed.elapsed(scheduler);
        }
        /**
         * @param index
         * @return
         * @see reactor.core.publisher.Flux#elementAt(int)
         */
        public final Mono<T> elementAt(int index) {
            return boxed.elementAt(index);
        }
        /**
         * @param index
         * @param defaultValue
         * @return
         * @see reactor.core.publisher.Flux#elementAt(int, java.lang.Object)
         */
        public final Mono<T> elementAt(int index, T defaultValue) {
            return boxed.elementAt(index, defaultValue);
        }
        /**
         * @param p
         * @return
         * @see reactor.core.publisher.Flux#filter(java.util.function.Predicate)
         */
        public final Flux<T> filter(Predicate<? super T> p) {
            return boxed.filter(p);
        }
        /**
         * @param other
         * @return
         * @see reactor.core.publisher.Flux#firstEmittingWith(org.reactivestreams.Publisher)
         */
        public final Flux<T> firstEmittingWith(Publisher<? extends T> other) {
            return boxed.firstEmittingWith(other);
        }
        /**
         * @param mapper
         * @return
         * @see reactor.core.publisher.Flux#flatMap(java.util.function.Function)
         */
        public final <R> Flux<R> flatMap(Function<? super T, ? extends Publisher<? extends R>> mapper) {
            return boxed.flatMap(mapper);
        }
        /**
         * @param mapper
         * @param concurrency
         * @return
         * @see reactor.core.publisher.Flux#flatMap(java.util.function.Function, int)
         */
        public final <V> Flux<V> flatMap(Function<? super T, ? extends Publisher<? extends V>> mapper,
                int concurrency) {
            return boxed.flatMap(mapper, concurrency);
        }
        /**
         * @param mapper
         * @param concurrency
         * @param prefetch
         * @return
         * @see reactor.core.publisher.Flux#flatMap(java.util.function.Function, int, int)
         */
        public final <V> Flux<V> flatMap(Function<? super T, ? extends Publisher<? extends V>> mapper, int concurrency,
                int prefetch) {
            return boxed.flatMap(mapper, concurrency, prefetch);
        }
        /**
         * @param mapper
         * @param delayError
         * @param concurrency
         * @param prefetch
         * @return
         * @see reactor.core.publisher.Flux#flatMap(java.util.function.Function, boolean, int, int)
         */
        public final <V> Flux<V> flatMap(Function<? super T, ? extends Publisher<? extends V>> mapper,
                boolean delayError, int concurrency, int prefetch) {
            return boxed.flatMap(mapper, delayError, concurrency, prefetch);
        }
        /**
         * @param mapperOnNext
         * @param mapperOnError
         * @param mapperOnComplete
         * @return
         * @see reactor.core.publisher.Flux#flatMap(java.util.function.Function, java.util.function.Function, java.util.function.Supplier)
         */
        public final <R> Flux<R> flatMap(Function<? super T, ? extends Publisher<? extends R>> mapperOnNext,
                Function<Throwable, ? extends Publisher<? extends R>> mapperOnError,
                Supplier<? extends Publisher<? extends R>> mapperOnComplete) {
            return boxed.flatMap(mapperOnNext, mapperOnError, mapperOnComplete);
        }
        /**
         * @param mapper
         * @return
         * @see reactor.core.publisher.Flux#flatMapIterable(java.util.function.Function)
         */
        public final <R> Flux<R> flatMapIterable(Function<? super T, ? extends Iterable<? extends R>> mapper) {
            return boxed.flatMapIterable(mapper);
        }
        /**
         * @param mapper
         * @param prefetch
         * @return
         * @see reactor.core.publisher.Flux#flatMapIterable(java.util.function.Function, int)
         */
        public final <R> Flux<R> flatMapIterable(Function<? super T, ? extends Iterable<? extends R>> mapper,
                int prefetch) {
            return boxed.flatMapIterable(mapper, prefetch);
        }
        /**
         * @return
         * @see reactor.core.publisher.Flux#getPrefetch()
         */
        public long getPrefetch() {
            return boxed.getPrefetch();
        }
        /**
         * @param keyMapper
         * @return
         * @see reactor.core.publisher.Flux#groupBy(java.util.function.Function)
         */
        public final <K> Flux<GroupedFlux<K, T>> groupBy(Function<? super T, ? extends K> keyMapper) {
            return boxed.groupBy(keyMapper);
        }
        /**
         * @param keyMapper
         * @param valueMapper
         * @return
         * @see reactor.core.publisher.Flux#groupBy(java.util.function.Function, java.util.function.Function)
         */
        public final <K, V> Flux<GroupedFlux<K, V>> groupBy(Function<? super T, ? extends K> keyMapper,
                Function<? super T, ? extends V> valueMapper) {
            return boxed.groupBy(keyMapper, valueMapper);
        }
        /**
         * @param other
         * @param leftEnd
         * @param rightEnd
         * @param resultSelector
         * @return
         * @see reactor.core.publisher.Flux#groupJoin(org.reactivestreams.Publisher, java.util.function.Function, java.util.function.Function, java.util.function.BiFunction)
         */
        public final <TRight, TLeftEnd, TRightEnd, R> Flux<R> groupJoin(Publisher<? extends TRight> other,
                Function<? super T, ? extends Publisher<TLeftEnd>> leftEnd,
                Function<? super TRight, ? extends Publisher<TRightEnd>> rightEnd,
                BiFunction<? super T, ? super Flux<TRight>, ? extends R> resultSelector) {
            return boxed.groupJoin(other, leftEnd, rightEnd, resultSelector);
        }
        /**
         * @param handler
         * @return
         * @see reactor.core.publisher.Flux#handle(java.util.function.BiConsumer)
         */
        public final <R> Flux<R> handle(BiConsumer<? super T, SynchronousSink<R>> handler) {
            return boxed.handle(handler);
        }
        /**
         * @param value
         * @return
         * @see reactor.core.publisher.Flux#hasElement(java.lang.Object)
         */
        public final Mono<Boolean> hasElement(T value) {
            return boxed.hasElement(value);
        }
        /**
         * @return
         * @see reactor.core.publisher.Flux#hasElements()
         */
        public final Mono<Boolean> hasElements() {
            return boxed.hasElements();
        }
        /**
         * @return
         * @see reactor.core.publisher.Flux#hide()
         */
        public final Flux<T> hide() {
            return boxed.hide();
        }
        /**
         * @return
         * @see reactor.core.publisher.Flux#ignoreElements()
         */
        public final Mono<T> ignoreElements() {
            return boxed.ignoreElements();
        }
        /**
         * @param other
         * @param leftEnd
         * @param rightEnd
         * @param resultSelector
         * @return
         * @see reactor.core.publisher.Flux#join(org.reactivestreams.Publisher, java.util.function.Function, java.util.function.Function, java.util.function.BiFunction)
         */
        public final <TRight, TLeftEnd, TRightEnd, R> Flux<R> join(Publisher<? extends TRight> other,
                Function<? super T, ? extends Publisher<TLeftEnd>> leftEnd,
                Function<? super TRight, ? extends Publisher<TRightEnd>> rightEnd,
                BiFunction<? super T, ? super TRight, ? extends R> resultSelector) {
            return boxed.join(other, leftEnd, rightEnd, resultSelector);
        }
        /**
         * @return
         * @see reactor.core.publisher.Flux#last()
         */
        public final Mono<T> last() {
            return boxed.last();
        }
        /**
         * @param defaultValue
         * @return
         * @see reactor.core.publisher.Flux#last(java.lang.Object)
         */
        public final Mono<T> last(T defaultValue) {
            return boxed.last(defaultValue);
        }
        /**
         * @return
         * @see reactor.core.publisher.Flux#log()
         */
        public final Flux<T> log() {
            return boxed.log();
        }
        /**
         * @param category
         * @return
         * @see reactor.core.publisher.Flux#log(java.lang.String)
         */
        public final Flux<T> log(String category) {
            return boxed.log(category);
        }
        /**
         * @param category
         * @param level
         * @param options
         * @return
         * @see reactor.core.publisher.Flux#log(java.lang.String, java.util.logging.Level, reactor.core.publisher.SignalType[])
         */
        public final Flux<T> log(String category, Level level, SignalType... options) {
            return boxed.log(category, level, options);
        }
        /**
         * @param category
         * @param level
         * @param showOperatorLine
         * @param options
         * @return
         * @see reactor.core.publisher.Flux#log(java.lang.String, java.util.logging.Level, boolean, reactor.core.publisher.SignalType[])
         */
        public final Flux<T> log(String category, Level level, boolean showOperatorLine, SignalType... options) {
            return boxed.log(category, level, showOperatorLine, options);
        }
        /**
         * @param mapper
         * @return
         * @see reactor.core.publisher.Flux#map(java.util.function.Function)
         */
        public final <V> Flux<V> map(Function<? super T, ? extends V> mapper) {
            return boxed.map(mapper);
        }
        /**
         * @param mapper
         * @return
         * @see reactor.core.publisher.Flux#mapError(java.util.function.Function)
         */
        public final Flux<T> mapError(Function<? super Throwable, ? extends Throwable> mapper) {
            return boxed.mapError(mapper);
        }
        /**
         * @param type
         * @param mapper
         * @return
         * @see reactor.core.publisher.Flux#mapError(java.lang.Class, java.util.function.Function)
         */
        public final <E extends Throwable> Flux<T> mapError(Class<E> type,
                Function<? super E, ? extends Throwable> mapper) {
            return boxed.mapError(type, mapper);
        }
        /**
         * @param predicate
         * @param mapper
         * @return
         * @see reactor.core.publisher.Flux#mapError(java.util.function.Predicate, java.util.function.Function)
         */
        public final Flux<T> mapError(Predicate<? super Throwable> predicate,
                Function<? super Throwable, ? extends Throwable> mapper) {
            return boxed.mapError(predicate, mapper);
        }
        /**
         * @return
         * @see reactor.core.publisher.Flux#materialize()
         */
        public final Flux<Signal<T>> materialize() {
            return boxed.materialize();
        }
        /**
         * @param other
         * @return
         * @see reactor.core.publisher.Flux#mergeWith(org.reactivestreams.Publisher)
         */
        public final Flux<T> mergeWith(Publisher<? extends T> other) {
            return boxed.mergeWith(other);
        }
        /**
         * @return
         * @see reactor.core.publisher.Flux#next()
         */
        public final Mono<T> next() {
            return boxed.next();
        }
        /**
         * @param clazz
         * @return
         * @see reactor.core.publisher.Flux#ofType(java.lang.Class)
         */
        public final <U> Flux<U> ofType(Class<U> clazz) {
            return boxed.ofType(clazz);
        }
        /**
         * @return
         * @see reactor.core.publisher.Flux#onBackpressureBuffer()
         */
        public final Flux<T> onBackpressureBuffer() {
            return boxed.onBackpressureBuffer();
        }
        /**
         * @param maxSize
         * @return
         * @see reactor.core.publisher.Flux#onBackpressureBuffer(int)
         */
        public final Flux<T> onBackpressureBuffer(int maxSize) {
            return boxed.onBackpressureBuffer(maxSize);
        }
        /**
         * @param maxSize
         * @param onOverflow
         * @return
         * @see reactor.core.publisher.Flux#onBackpressureBuffer(int, java.util.function.Consumer)
         */
        public final Flux<T> onBackpressureBuffer(int maxSize, Consumer<? super T> onOverflow) {
            return boxed.onBackpressureBuffer(maxSize, onOverflow);
        }
        /**
         * @return
         * @see reactor.core.publisher.Flux#onBackpressureDrop()
         */
        public final Flux<T> onBackpressureDrop() {
            return boxed.onBackpressureDrop();
        }
        /**
         * @param onDropped
         * @return
         * @see reactor.core.publisher.Flux#onBackpressureDrop(java.util.function.Consumer)
         */
        public final Flux<T> onBackpressureDrop(Consumer<? super T> onDropped) {
            return boxed.onBackpressureDrop(onDropped);
        }
        /**
         * @return
         * @see reactor.core.publisher.Flux#onBackpressureError()
         */
        public final Flux<T> onBackpressureError() {
            return boxed.onBackpressureError();
        }
        /**
         * @return
         * @see reactor.core.publisher.Flux#onBackpressureLatest()
         */
        public final Flux<T> onBackpressureLatest() {
            return boxed.onBackpressureLatest();
        }
        /**
         * @param fallback
         * @return
         * @see reactor.core.publisher.Flux#onErrorResumeWith(java.util.function.Function)
         */
        public final Flux<T> onErrorResumeWith(Function<? super Throwable, ? extends Publisher<? extends T>> fallback) {
            return boxed.onErrorResumeWith(fallback);
        }
        /**
         * @param type
         * @param fallback
         * @return
         * @see reactor.core.publisher.Flux#onErrorResumeWith(java.lang.Class, java.util.function.Function)
         */
        public final <E extends Throwable> Flux<T> onErrorResumeWith(Class<E> type,
                Function<? super E, ? extends Publisher<? extends T>> fallback) {
            return boxed.onErrorResumeWith(type, fallback);
        }
        /**
         * @param predicate
         * @param fallback
         * @return
         * @see reactor.core.publisher.Flux#onErrorResumeWith(java.util.function.Predicate, java.util.function.Function)
         */
        public final Flux<T> onErrorResumeWith(Predicate<? super Throwable> predicate,
                Function<? super Throwable, ? extends Publisher<? extends T>> fallback) {
            return boxed.onErrorResumeWith(predicate, fallback);
        }
        /**
         * @param fallbackValue
         * @return
         * @see reactor.core.publisher.Flux#onErrorReturn(java.lang.Object)
         */
        public final Flux<T> onErrorReturn(T fallbackValue) {
            return boxed.onErrorReturn(fallbackValue);
        }
        /**
         * @param type
         * @param fallbackValue
         * @return
         * @see reactor.core.publisher.Flux#onErrorReturn(java.lang.Class, java.lang.Object)
         */
        public final <E extends Throwable> Flux<T> onErrorReturn(Class<E> type, T fallbackValue) {
            return boxed.onErrorReturn(type, fallbackValue);
        }
        /**
         * @param predicate
         * @param fallbackValue
         * @return
         * @see reactor.core.publisher.Flux#onErrorReturn(java.util.function.Predicate, java.lang.Object)
         */
        public final <E extends Throwable> Flux<T> onErrorReturn(Predicate<? super Throwable> predicate,
                T fallbackValue) {
            return boxed.onErrorReturn(predicate, fallbackValue);
        }
        /**
         * @return
         * @see reactor.core.publisher.Flux#onTerminateDetach()
         */
        public final Flux<T> onTerminateDetach() {
            return boxed.onTerminateDetach();
        }
        /**
         * @return
         * @see reactor.core.publisher.Flux#parallel()
         */
        public final ParallelFlux<T> parallel() {
            return boxed.parallel();
        }
        /**
         * @param parallelism
         * @return
         * @see reactor.core.publisher.Flux#parallel(int)
         */
        public final ParallelFlux<T> parallel(int parallelism) {
            return boxed.parallel(parallelism);
        }
        /**
         * @param parallelism
         * @param prefetch
         * @return
         * @see reactor.core.publisher.Flux#parallel(int, int)
         */
        public final ParallelFlux<T> parallel(int parallelism, int prefetch) {
            return boxed.parallel(parallelism, prefetch);
        }
        /**
         * @return
         * @see reactor.core.publisher.Flux#publish()
         */
        public final ConnectableFlux<T> publish() {
            return boxed.publish();
        }
        /**
         * @param prefetch
         * @return
         * @see reactor.core.publisher.Flux#publish(int)
         */
        public final ConnectableFlux<T> publish(int prefetch) {
            return boxed.publish(prefetch);
        }
        /**
         * @param transform
         * @return
         * @see reactor.core.publisher.Flux#publish(java.util.function.Function)
         */
        public final <R> Flux<R> publish(Function<? super Flux<T>, ? extends Publisher<? extends R>> transform) {
            return boxed.publish(transform);
        }
        /**
         * @param transform
         * @param prefetch
         * @return
         * @see reactor.core.publisher.Flux#publish(java.util.function.Function, int)
         */
        public final <R> Flux<R> publish(Function<? super Flux<T>, ? extends Publisher<? extends R>> transform,
                int prefetch) {
            return boxed.publish(transform, prefetch);
        }
        /**
         * @return
         * @see reactor.core.publisher.Flux#publishNext()
         */
        public final Mono<T> publishNext() {
            return boxed.publishNext();
        }
        /**
         * @param scheduler
         * @return
         * @see reactor.core.publisher.Flux#publishOn(reactor.core.scheduler.Scheduler)
         */
        public final Flux<T> publishOn(Scheduler scheduler) {
            return boxed.publishOn(scheduler);
        }
        /**
         * @param scheduler
         * @param prefetch
         * @return
         * @see reactor.core.publisher.Flux#publishOn(reactor.core.scheduler.Scheduler, int)
         */
        public final Flux<T> publishOn(Scheduler scheduler, int prefetch) {
            return boxed.publishOn(scheduler, prefetch);
        }
        /**
         * @param aggregator
         * @return
         * @see reactor.core.publisher.Flux#reduce(java.util.function.BiFunction)
         */
        public final Mono<T> reduce(BiFunction<T, T, T> aggregator) {
            return boxed.reduce(aggregator);
        }
        /**
         * @param initial
         * @param accumulator
         * @return
         * @see reactor.core.publisher.Flux#reduce(java.lang.Object, java.util.function.BiFunction)
         */
        public final <A> Mono<A> reduce(A initial, BiFunction<A, ? super T, A> accumulator) {
            return boxed.reduce(initial, accumulator);
        }
        /**
         * @param initial
         * @param accumulator
         * @return
         * @see reactor.core.publisher.Flux#reduceWith(java.util.function.Supplier, java.util.function.BiFunction)
         */
        public final <A> Mono<A> reduceWith(Supplier<A> initial, BiFunction<A, ? super T, A> accumulator) {
            return boxed.reduceWith(initial, accumulator);
        }
        /**
         * @return
         * @see reactor.core.publisher.Flux#repeat()
         */
        public final Flux<T> repeat() {
            return boxed.repeat();
        }
        /**
         * @param predicate
         * @return
         * @see reactor.core.publisher.Flux#repeat(java.util.function.BooleanSupplier)
         */
        public final Flux<T> repeat(BooleanSupplier predicate) {
            return boxed.repeat(predicate);
        }
        /**
         * @param numRepeat
         * @return
         * @see reactor.core.publisher.Flux#repeat(long)
         */
        public final Flux<T> repeat(long numRepeat) {
            return boxed.repeat(numRepeat);
        }
        /**
         * @param numRepeat
         * @param predicate
         * @return
         * @see reactor.core.publisher.Flux#repeat(long, java.util.function.BooleanSupplier)
         */
        public final Flux<T> repeat(long numRepeat, BooleanSupplier predicate) {
            return boxed.repeat(numRepeat, predicate);
        }
        /**
         * @param whenFactory
         * @return
         * @see reactor.core.publisher.Flux#repeatWhen(java.util.function.Function)
         */
        public final Flux<T> repeatWhen(Function<Flux<Long>, ? extends Publisher<?>> whenFactory) {
            return boxed.repeatWhen(whenFactory);
        }
        /**
         * @return
         * @see reactor.core.publisher.Flux#replay()
         */
        public final ConnectableFlux<T> replay() {
            return boxed.replay();
        }
        /**
         * @param history
         * @return
         * @see reactor.core.publisher.Flux#replay(int)
         */
        public final ConnectableFlux<T> replay(int history) {
            return boxed.replay(history);
        }
        /**
         * @param ttl
         * @return
         * @see reactor.core.publisher.Flux#replay(java.time.Duration)
         */
        public final ConnectableFlux<T> replay(Duration ttl) {
            return boxed.replay(ttl);
        }
        /**
         * @param history
         * @param ttl
         * @return
         * @see reactor.core.publisher.Flux#replay(int, java.time.Duration)
         */
        public final ConnectableFlux<T> replay(int history, Duration ttl) {
            return boxed.replay(history, ttl);
        }
        /**
         * @param ttl
         * @param timer
         * @return
         * @see reactor.core.publisher.Flux#replayMillis(long, reactor.core.scheduler.TimedScheduler)
         */
        public final ConnectableFlux<T> replayMillis(long ttl, TimedScheduler timer) {
            return boxed.replayMillis(ttl, timer);
        }
        /**
         * @param history
         * @param ttl
         * @param timer
         * @return
         * @see reactor.core.publisher.Flux#replayMillis(int, long, reactor.core.scheduler.TimedScheduler)
         */
        public final ConnectableFlux<T> replayMillis(int history, long ttl, TimedScheduler timer) {
            return boxed.replayMillis(history, ttl, timer);
        }
        /**
         * @return
         * @see reactor.core.publisher.Flux#retry()
         */
        public final Flux<T> retry() {
            return boxed.retry();
        }
        /**
         * @param numRetries
         * @return
         * @see reactor.core.publisher.Flux#retry(long)
         */
        public final Flux<T> retry(long numRetries) {
            return boxed.retry(numRetries);
        }
        /**
         * @param retryMatcher
         * @return
         * @see reactor.core.publisher.Flux#retry(java.util.function.Predicate)
         */
        public final Flux<T> retry(Predicate<Throwable> retryMatcher) {
            return boxed.retry(retryMatcher);
        }
        /**
         * @param numRetries
         * @param retryMatcher
         * @return
         * @see reactor.core.publisher.Flux#retry(long, java.util.function.Predicate)
         */
        public final Flux<T> retry(long numRetries, Predicate<Throwable> retryMatcher) {
            return boxed.retry(numRetries, retryMatcher);
        }
        /**
         * @param whenFactory
         * @return
         * @see reactor.core.publisher.Flux#retryWhen(java.util.function.Function)
         */
        public final Flux<T> retryWhen(Function<Flux<Throwable>, ? extends Publisher<?>> whenFactory) {
            return boxed.retryWhen(whenFactory);
        }
        /**
         * @param timespan
         * @return
         * @see reactor.core.publisher.Flux#sample(java.time.Duration)
         */
        public final Flux<T> sample(Duration timespan) {
            return boxed.sample(timespan);
        }
        /**
         * @param sampler
         * @return
         * @see reactor.core.publisher.Flux#sample(org.reactivestreams.Publisher)
         */
        public final <U> Flux<T> sample(Publisher<U> sampler) {
            return boxed.sample(sampler);
        }
        /**
         * @param timespan
         * @return
         * @see reactor.core.publisher.Flux#sampleFirst(java.time.Duration)
         */
        public final Flux<T> sampleFirst(Duration timespan) {
            return boxed.sampleFirst(timespan);
        }
        /**
         * @param samplerFactory
         * @return
         * @see reactor.core.publisher.Flux#sampleFirst(java.util.function.Function)
         */
        public final <U> Flux<T> sampleFirst(Function<? super T, ? extends Publisher<U>> samplerFactory) {
            return boxed.sampleFirst(samplerFactory);
        }
        /**
         * @param timespan
         * @return
         * @see reactor.core.publisher.Flux#sampleFirstMillis(long)
         */
        public final Flux<T> sampleFirstMillis(long timespan) {
            return boxed.sampleFirstMillis(timespan);
        }
        /**
         * @param timespan
         * @return
         * @see reactor.core.publisher.Flux#sampleMillis(long)
         */
        public final Flux<T> sampleMillis(long timespan) {
            return boxed.sampleMillis(timespan);
        }
        /**
         * @param throttlerFactory
         * @return
         * @see reactor.core.publisher.Flux#sampleTimeout(java.util.function.Function)
         */
        public final <U> Flux<T> sampleTimeout(Function<? super T, ? extends Publisher<U>> throttlerFactory) {
            return boxed.sampleTimeout(throttlerFactory);
        }
        /**
         * @param throttlerFactory
         * @param maxConcurrency
         * @return
         * @see reactor.core.publisher.Flux#sampleTimeout(java.util.function.Function, int)
         */
        public final <U> Flux<T> sampleTimeout(Function<? super T, ? extends Publisher<U>> throttlerFactory,
                int maxConcurrency) {
            return boxed.sampleTimeout(throttlerFactory, maxConcurrency);
        }
        /**
         * @param accumulator
         * @return
         * @see reactor.core.publisher.Flux#scan(java.util.function.BiFunction)
         */
        public final Flux<T> scan(BiFunction<T, T, T> accumulator) {
            return boxed.scan(accumulator);
        }
        /**
         * @param initial
         * @param accumulator
         * @return
         * @see reactor.core.publisher.Flux#scan(java.lang.Object, java.util.function.BiFunction)
         */
        public final <A> Flux<A> scan(A initial, BiFunction<A, ? super T, A> accumulator) {
            return boxed.scan(initial, accumulator);
        }
        /**
         * @param initial
         * @param accumulator
         * @return
         * @see reactor.core.publisher.Flux#scanWith(java.util.function.Supplier, java.util.function.BiFunction)
         */
        public final <A> Flux<A> scanWith(Supplier<A> initial, BiFunction<A, ? super T, A> accumulator) {
            return boxed.scanWith(initial, accumulator);
        }
        /**
         * @return
         * @see reactor.core.publisher.Flux#share()
         */
        public final Flux<T> share() {
            return boxed.share();
        }
        /**
         * @return
         * @see reactor.core.publisher.Flux#single()
         */
        public final Mono<T> single() {
            return boxed.single();
        }
        /**
         * @param defaultValue
         * @return
         * @see reactor.core.publisher.Flux#single(java.lang.Object)
         */
        public final Mono<T> single(T defaultValue) {
            return boxed.single(defaultValue);
        }
        /**
         * @return
         * @see reactor.core.publisher.Flux#singleOrEmpty()
         */
        public final Mono<T> singleOrEmpty() {
            return boxed.singleOrEmpty();
        }
        /**
         * @param skipped
         * @return
         * @see reactor.core.publisher.Flux#skip(long)
         */
        public final Flux<T> skip(long skipped) {
            return boxed.skip(skipped);
        }
        /**
         * @param timespan
         * @return
         * @see reactor.core.publisher.Flux#skip(java.time.Duration)
         */
        public final Flux<T> skip(Duration timespan) {
            return boxed.skip(timespan);
        }
        /**
         * @param n
         * @return
         * @see reactor.core.publisher.Flux#skipLast(int)
         */
        public final Flux<T> skipLast(int n) {
            return boxed.skipLast(n);
        }
        /**
         * @param timespan
         * @return
         * @see reactor.core.publisher.Flux#skipMillis(long)
         */
        public final Flux<T> skipMillis(long timespan) {
            return boxed.skipMillis(timespan);
        }
        /**
         * @param timespan
         * @param timer
         * @return
         * @see reactor.core.publisher.Flux#skipMillis(long, reactor.core.scheduler.TimedScheduler)
         */
        public final Flux<T> skipMillis(long timespan, TimedScheduler timer) {
            return boxed.skipMillis(timespan, timer);
        }
        /**
         * @param untilPredicate
         * @return
         * @see reactor.core.publisher.Flux#skipUntil(java.util.function.Predicate)
         */
        public final Flux<T> skipUntil(Predicate<? super T> untilPredicate) {
            return boxed.skipUntil(untilPredicate);
        }
        /**
         * @param other
         * @return
         * @see reactor.core.publisher.Flux#skipUntilOther(org.reactivestreams.Publisher)
         */
        public final Flux<T> skipUntilOther(Publisher<?> other) {
            return boxed.skipUntilOther(other);
        }
        /**
         * @param skipPredicate
         * @return
         * @see reactor.core.publisher.Flux#skipWhile(java.util.function.Predicate)
         */
        public final Flux<T> skipWhile(Predicate<? super T> skipPredicate) {
            return boxed.skipWhile(skipPredicate);
        }
        /**
         * @return
         * @see reactor.core.publisher.Flux#sort()
         */
        public final Flux<T> sort() {
            return boxed.sort();
        }
        /**
         * @param sortFunction
         * @return
         * @see reactor.core.publisher.Flux#sort(java.util.Comparator)
         */
        public final Flux<T> sort(Comparator<? super T> sortFunction) {
            return boxed.sort(sortFunction);
        }
        /**
         * @param iterable
         * @return
         * @see reactor.core.publisher.Flux#startWith(java.lang.Iterable)
         */
        public final Flux<T> startWith(Iterable<? extends T> iterable) {
            return boxed.startWith(iterable);
        }
        /**
         * @param values
         * @return
         * @see reactor.core.publisher.Flux#startWith(java.lang.Object[])
         */
        public final Flux<T> startWith(T... values) {
            return boxed.startWith(values);
        }
        /**
         * @param publisher
         * @return
         * @see reactor.core.publisher.Flux#startWith(org.reactivestreams.Publisher)
         */
        public final Flux<T> startWith(Publisher<? extends T> publisher) {
            return boxed.startWith(publisher);
        }
        /**
         * @return
         * @see reactor.core.publisher.Flux#subscribe()
         */
        public final Cancellation subscribe() {
            return boxed.subscribe();
        }
        /**
         * @param prefetch
         * @return
         * @see reactor.core.publisher.Flux#subscribe(int)
         */
        public final Cancellation subscribe(int prefetch) {
            return boxed.subscribe(prefetch);
        }
        /**
         * @param consumer
         * @return
         * @see reactor.core.publisher.Flux#subscribe(java.util.function.Consumer)
         */
        public final Cancellation subscribe(Consumer<? super T> consumer) {
            return boxed.subscribe(consumer);
        }
        /**
         * @param consumer
         * @param prefetch
         * @return
         * @see reactor.core.publisher.Flux#subscribe(java.util.function.Consumer, int)
         */
        public final Cancellation subscribe(Consumer<? super T> consumer, int prefetch) {
            return boxed.subscribe(consumer, prefetch);
        }
        /**
         * @param consumer
         * @param errorConsumer
         * @return
         * @see reactor.core.publisher.Flux#subscribe(java.util.function.Consumer, java.util.function.Consumer)
         */
        public final Cancellation subscribe(Consumer<? super T> consumer, Consumer<? super Throwable> errorConsumer) {
            return boxed.subscribe(consumer, errorConsumer);
        }
        /**
         * @param consumer
         * @param errorConsumer
         * @param completeConsumer
         * @return
         * @see reactor.core.publisher.Flux#subscribe(java.util.function.Consumer, java.util.function.Consumer, java.lang.Runnable)
         */
        public final Cancellation subscribe(Consumer<? super T> consumer, Consumer<? super Throwable> errorConsumer,
                Runnable completeConsumer) {
            return boxed.subscribe(consumer, errorConsumer, completeConsumer);
        }
        /**
         * @param consumer
         * @param errorConsumer
         * @param completeConsumer
         * @param prefetch
         * @return
         * @see reactor.core.publisher.Flux#subscribe(java.util.function.Consumer, java.util.function.Consumer, java.lang.Runnable, int)
         */
        public final Cancellation subscribe(Consumer<? super T> consumer, Consumer<? super Throwable> errorConsumer,
                Runnable completeConsumer, int prefetch) {
            return boxed.subscribe(consumer, errorConsumer, completeConsumer, prefetch);
        }
        /**
         * @param scheduler
         * @return
         * @see reactor.core.publisher.Flux#subscribeOn(reactor.core.scheduler.Scheduler)
         */
        public final Flux<T> subscribeOn(Scheduler scheduler) {
            return boxed.subscribeOn(scheduler);
        }
        /**
         * @param subscriber
         * @return
         * @see reactor.core.publisher.Flux#subscribeWith(org.reactivestreams.Subscriber)
         */
        public final <E extends Subscriber<? super T>> E subscribeWith(E subscriber) {
            return boxed.subscribeWith(subscriber);
        }
        /**
         * @param alternate
         * @return
         * @see reactor.core.publisher.Flux#switchIfEmpty(org.reactivestreams.Publisher)
         */
        public final Flux<T> switchIfEmpty(Publisher<? extends T> alternate) {
            return boxed.switchIfEmpty(alternate);
        }
        /**
         * @param fn
         * @return
         * @see reactor.core.publisher.Flux#switchMap(java.util.function.Function)
         */
        public final <V> Flux<V> switchMap(Function<? super T, Publisher<? extends V>> fn) {
            return boxed.switchMap(fn);
        }
        /**
         * @param fn
         * @param prefetch
         * @return
         * @see reactor.core.publisher.Flux#switchMap(java.util.function.Function, int)
         */
        public final <V> Flux<V> switchMap(Function<? super T, Publisher<? extends V>> fn, int prefetch) {
            return boxed.switchMap(fn, prefetch);
        }
        /**
         * @param type
         * @param fallback
         * @return
         * @see reactor.core.publisher.Flux#switchOnError(java.lang.Class, org.reactivestreams.Publisher)
         */
        public final <E extends Throwable> Flux<T> switchOnError(Class<E> type, Publisher<? extends T> fallback) {
            return boxed.switchOnError(type, fallback);
        }
        /**
         * @param predicate
         * @param fallback
         * @return
         * @see reactor.core.publisher.Flux#switchOnError(java.util.function.Predicate, org.reactivestreams.Publisher)
         */
        public final Flux<T> switchOnError(Predicate<? super Throwable> predicate, Publisher<? extends T> fallback) {
            return boxed.switchOnError(predicate, fallback);
        }
        /**
         * @param fallback
         * @return
         * @see reactor.core.publisher.Flux#switchOnError(org.reactivestreams.Publisher)
         */
        public final Flux<T> switchOnError(Publisher<? extends T> fallback) {
            return boxed.switchOnError(fallback);
        }
        /**
         * @param n
         * @return
         * @see reactor.core.publisher.Flux#take(long)
         */
        public final Flux<T> take(long n) {
            return boxed.take(n);
        }
        /**
         * @param timespan
         * @return
         * @see reactor.core.publisher.Flux#take(java.time.Duration)
         */
        public final Flux<T> take(Duration timespan) {
            return boxed.take(timespan);
        }
        /**
         * @param n
         * @return
         * @see reactor.core.publisher.Flux#takeLast(int)
         */
        public final Flux<T> takeLast(int n) {
            return boxed.takeLast(n);
        }
        /**
         * @param timespan
         * @return
         * @see reactor.core.publisher.Flux#takeMillis(long)
         */
        public final Flux<T> takeMillis(long timespan) {
            return boxed.takeMillis(timespan);
        }
        /**
         * @param timespan
         * @param timer
         * @return
         * @see reactor.core.publisher.Flux#takeMillis(long, reactor.core.scheduler.TimedScheduler)
         */
        public final Flux<T> takeMillis(long timespan, TimedScheduler timer) {
            return boxed.takeMillis(timespan, timer);
        }
        /**
         * @param predicate
         * @return
         * @see reactor.core.publisher.Flux#takeUntil(java.util.function.Predicate)
         */
        public final Flux<T> takeUntil(Predicate<? super T> predicate) {
            return boxed.takeUntil(predicate);
        }
        /**
         * @param other
         * @return
         * @see reactor.core.publisher.Flux#takeUntilOther(org.reactivestreams.Publisher)
         */
        public final Flux<T> takeUntilOther(Publisher<?> other) {
            return boxed.takeUntilOther(other);
        }
        /**
         * @param continuePredicate
         * @return
         * @see reactor.core.publisher.Flux#takeWhile(java.util.function.Predicate)
         */
        public final Flux<T> takeWhile(Predicate<? super T> continuePredicate) {
            return boxed.takeWhile(continuePredicate);
        }
        /**
         * @return
         * @see reactor.core.publisher.Flux#then()
         */
        public final Mono<Void> then() {
            return boxed.then();
        }
        /**
         * @param other
         * @return
         * @see reactor.core.publisher.Flux#then(org.reactivestreams.Publisher)
         */
        public final Mono<Void> then(Publisher<Void> other) {
            return boxed.then(other);
        }
        /**
         * @param afterSupplier
         * @return
         * @see reactor.core.publisher.Flux#then(java.util.function.Supplier)
         */
        public final Mono<Void> then(Supplier<? extends Publisher<Void>> afterSupplier) {
            return boxed.then(afterSupplier);
        }
        /**
         * @param other
         * @return
         * @see reactor.core.publisher.Flux#thenMany(org.reactivestreams.Publisher)
         */
        public final <V> Flux<V> thenMany(Publisher<V> other) {
            return boxed.thenMany(other);
        }
        /**
         * @param afterSupplier
         * @return
         * @see reactor.core.publisher.Flux#thenMany(java.util.function.Supplier)
         */
        public final <V> Flux<V> thenMany(Supplier<? extends Publisher<V>> afterSupplier) {
            return boxed.thenMany(afterSupplier);
        }
        /**
         * @param timeout
         * @return
         * @see reactor.core.publisher.Flux#timeout(java.time.Duration)
         */
        public final Flux<T> timeout(Duration timeout) {
            return boxed.timeout(timeout);
        }
        /**
         * @param timeout
         * @param fallback
         * @return
         * @see reactor.core.publisher.Flux#timeout(java.time.Duration, org.reactivestreams.Publisher)
         */
        public final Flux<T> timeout(Duration timeout, Publisher<? extends T> fallback) {
            return boxed.timeout(timeout, fallback);
        }
        /**
         * @param firstTimeout
         * @return
         * @see reactor.core.publisher.Flux#timeout(org.reactivestreams.Publisher)
         */
        public final <U> Flux<T> timeout(Publisher<U> firstTimeout) {
            return boxed.timeout(firstTimeout);
        }
        /**
         * @param firstTimeout
         * @param nextTimeoutFactory
         * @return
         * @see reactor.core.publisher.Flux#timeout(org.reactivestreams.Publisher, java.util.function.Function)
         */
        public final <U, V> Flux<T> timeout(Publisher<U> firstTimeout,
                Function<? super T, ? extends Publisher<V>> nextTimeoutFactory) {
            return boxed.timeout(firstTimeout, nextTimeoutFactory);
        }
        /**
         * @param firstTimeout
         * @param nextTimeoutFactory
         * @param fallback
         * @return
         * @see reactor.core.publisher.Flux#timeout(org.reactivestreams.Publisher, java.util.function.Function, org.reactivestreams.Publisher)
         */
        public final <U, V> Flux<T> timeout(Publisher<U> firstTimeout,
                Function<? super T, ? extends Publisher<V>> nextTimeoutFactory, Publisher<? extends T> fallback) {
            return boxed.timeout(firstTimeout, nextTimeoutFactory, fallback);
        }
        /**
         * @param timeout
         * @return
         * @see reactor.core.publisher.Flux#timeoutMillis(long)
         */
        public final Flux<T> timeoutMillis(long timeout) {
            return boxed.timeoutMillis(timeout);
        }
        /**
         * @param timeout
         * @param timer
         * @return
         * @see reactor.core.publisher.Flux#timeoutMillis(long, reactor.core.scheduler.TimedScheduler)
         */
        public final Flux<T> timeoutMillis(long timeout, TimedScheduler timer) {
            return boxed.timeoutMillis(timeout, timer);
        }
        /**
         * @param timeout
         * @param fallback
         * @return
         * @see reactor.core.publisher.Flux#timeoutMillis(long, org.reactivestreams.Publisher)
         */
        public final Flux<T> timeoutMillis(long timeout, Publisher<? extends T> fallback) {
            return boxed.timeoutMillis(timeout, fallback);
        }
        /**
         * @param timeout
         * @param fallback
         * @param timer
         * @return
         * @see reactor.core.publisher.Flux#timeoutMillis(long, org.reactivestreams.Publisher, reactor.core.scheduler.TimedScheduler)
         */
        public final Flux<T> timeoutMillis(long timeout, Publisher<? extends T> fallback, TimedScheduler timer) {
            return boxed.timeoutMillis(timeout, fallback, timer);
        }
        /**
         * @return
         * @see reactor.core.publisher.Flux#timestamp()
         */
        public final Flux<Tuple2<Long, T>> timestamp() {
            return boxed.timestamp();
        }
        /**
         * @param scheduler
         * @return
         * @see reactor.core.publisher.Flux#timestamp(reactor.core.scheduler.TimedScheduler)
         */
        public final Flux<Tuple2<Long, T>> timestamp(TimedScheduler scheduler) {
            return boxed.timestamp(scheduler);
        }
        /**
         * @return
         * @see reactor.core.publisher.Flux#toIterable()
         */
        public final Iterable<T> toIterable() {
            return boxed.toIterable();
        }
        /**
         * @param batchSize
         * @return
         * @see reactor.core.publisher.Flux#toIterable(long)
         */
        public final Iterable<T> toIterable(long batchSize) {
            return boxed.toIterable(batchSize);
        }
        /**
         * @param batchSize
         * @param queueProvider
         * @return
         * @see reactor.core.publisher.Flux#toIterable(long, java.util.function.Supplier)
         */
        public final Iterable<T> toIterable(long batchSize, Supplier<Queue<T>> queueProvider) {
            return boxed.toIterable(batchSize, queueProvider);
        }
        /**
         * @return
         * @see reactor.core.publisher.Flux#toStream()
         */
        public Stream<T> toStream() {
            return boxed.toStream();
        }
        /**
         * @param batchSize
         * @return
         * @see reactor.core.publisher.Flux#toStream(int)
         */
        public Stream<T> toStream(int batchSize) {
            return boxed.toStream(batchSize);
        }
        /**
         * @param transformer
         * @return
         * @see reactor.core.publisher.Flux#transform(java.util.function.Function)
         */
        public final <V> Flux<V> transformMono(Function<? super Flux<T>, ? extends Publisher<V>> transformer) {
            return boxed.transform(transformer);
        }
        /**
         * @return
         * @see reactor.core.publisher.Flux#window()
         */
        public final Flux<Flux<T>> window() {
            return boxed.window();
        }
        /**
         * @param maxSize
         * @return
         * @see reactor.core.publisher.Flux#window(int)
         */
        public final Flux<Flux<T>> window(int maxSize) {
            return boxed.window(maxSize);
        }
        /**
         * @param maxSize
         * @param skip
         * @return
         * @see reactor.core.publisher.Flux#window(int, int)
         */
        public final Flux<Flux<T>> window(int maxSize, int skip) {
            return boxed.window(maxSize, skip);
        }
        /**
         * @param boundary
         * @return
         * @see reactor.core.publisher.Flux#window(org.reactivestreams.Publisher)
         */
        public final Flux<Flux<T>> window(Publisher<?> boundary) {
            return boxed.window(boundary);
        }
        /**
         * @param bucketOpening
         * @param closeSelector
         * @return
         * @see reactor.core.publisher.Flux#window(org.reactivestreams.Publisher, java.util.function.Function)
         */
        public final <U, V> Flux<Flux<T>> window(Publisher<U> bucketOpening,
                Function<? super U, ? extends Publisher<V>> closeSelector) {
            return boxed.window(bucketOpening, closeSelector);
        }
        /**
         * @param timespan
         * @return
         * @see reactor.core.publisher.Flux#window(java.time.Duration)
         */
        public final Flux<Flux<T>> window(Duration timespan) {
            return boxed.window(timespan);
        }
        /**
         * @param timespan
         * @param timeshift
         * @return
         * @see reactor.core.publisher.Flux#window(java.time.Duration, java.time.Duration)
         */
        public final Flux<Flux<T>> window(Duration timespan, Duration timeshift) {
            return boxed.window(timespan, timeshift);
        }
        /**
         * @param maxSize
         * @param timespan
         * @return
         * @see reactor.core.publisher.Flux#window(int, java.time.Duration)
         */
        public final Flux<Flux<T>> window(int maxSize, Duration timespan) {
            return boxed.window(maxSize, timespan);
        }
        /**
         * @param timespan
         * @return
         * @see reactor.core.publisher.Flux#windowMillis(long)
         */
        public final Flux<Flux<T>> windowMillis(long timespan) {
            return boxed.windowMillis(timespan);
        }
        /**
         * @param timespan
         * @param timer
         * @return
         * @see reactor.core.publisher.Flux#windowMillis(long, reactor.core.scheduler.TimedScheduler)
         */
        public final Flux<Flux<T>> windowMillis(long timespan, TimedScheduler timer) {
            return boxed.windowMillis(timespan, timer);
        }
        /**
         * @param timespan
         * @param timeshift
         * @param timer
         * @return
         * @see reactor.core.publisher.Flux#windowMillis(long, long, reactor.core.scheduler.TimedScheduler)
         */
        public final Flux<Flux<T>> windowMillis(long timespan, long timeshift, TimedScheduler timer) {
            return boxed.windowMillis(timespan, timeshift, timer);
        }
        /**
         * @param maxSize
         * @param timespan
         * @return
         * @see reactor.core.publisher.Flux#windowMillis(int, long)
         */
        public final Flux<Flux<T>> windowMillis(int maxSize, long timespan) {
            return boxed.windowMillis(maxSize, timespan);
        }
        /**
         * @param maxSize
         * @param timespan
         * @param timer
         * @return
         * @see reactor.core.publisher.Flux#windowMillis(int, long, reactor.core.scheduler.TimedScheduler)
         */
        public final Flux<Flux<T>> windowMillis(int maxSize, long timespan, TimedScheduler timer) {
            return boxed.windowMillis(maxSize, timespan, timer);
        }
        /**
         * @param other
         * @param resultSelector
         * @return
         * @see reactor.core.publisher.Flux#withLatestFrom(org.reactivestreams.Publisher, java.util.function.BiFunction)
         */
        public final <U, R> Flux<R> withLatestFrom(Publisher<? extends U> other,
                BiFunction<? super T, ? super U, ? extends R> resultSelector) {
            return boxed.withLatestFrom(other, resultSelector);
        }
        /**
         * @param source2
         * @param combinator
         * @return
         * @see reactor.core.publisher.Flux#zipWith(org.reactivestreams.Publisher, java.util.function.BiFunction)
         */
        public final <T2, V> Flux<V> zipWith(Publisher<? extends T2> source2,
                BiFunction<? super T, ? super T2, ? extends V> combinator) {
            return boxed.zipWith(source2, combinator);
        }
        /**
         * @param source2
         * @param prefetch
         * @param combinator
         * @return
         * @see reactor.core.publisher.Flux#zipWith(org.reactivestreams.Publisher, int, java.util.function.BiFunction)
         */
        public final <T2, V> Flux<V> zipWith(Publisher<? extends T2> source2, int prefetch,
                BiFunction<? super T, ? super T2, ? extends V> combinator) {
            return boxed.zipWith(source2, prefetch, combinator);
        }
        /**
         * @param source2
         * @return
         * @see reactor.core.publisher.Flux#zipWith(org.reactivestreams.Publisher)
         */
        public final <T2> Flux<Tuple2<T, T2>> zipWith(Publisher<? extends T2> source2) {
            return boxed.zipWith(source2);
        }
        /**
         * @param source2
         * @param prefetch
         * @return
         * @see reactor.core.publisher.Flux#zipWith(org.reactivestreams.Publisher, int)
         */
        public final <T2> Flux<Tuple2<T, T2>> zipWith(Publisher<? extends T2> source2, int prefetch) {
            return boxed.zipWith(source2, prefetch);
        }
        /**
         * @param iterable
         * @return
         * @see reactor.core.publisher.Flux#zipWithIterable(java.lang.Iterable)
         */
        public final <T2> Flux<Tuple2<T, T2>> zipWithIterable(Iterable<? extends T2> iterable) {
            return boxed.zipWithIterable(iterable);
        }
        /**
         * @param iterable
         * @param zipper
         * @return
         * @see reactor.core.publisher.Flux#zipWithIterable(java.lang.Iterable, java.util.function.BiFunction)
         */
        public final <T2, V> Flux<V> zipWithIterable(Iterable<? extends T2> iterable,
                BiFunction<? super T, ? super T2, ? extends V> zipper) {
            return boxed.zipWithIterable(iterable, zipper);
        }




}
