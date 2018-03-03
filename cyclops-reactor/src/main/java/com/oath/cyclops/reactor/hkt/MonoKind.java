package com.oath.cyclops.reactor.hkt;


import java.time.Duration;
import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.LongConsumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.logging.Level;

import com.oath.cyclops.hkt.Higher;
import Future;
import cyclops.companion.reactor.Monos;
import cyclops.monads.ReactorWitness;
import cyclops.monads.ReactorWitness.mono;
import cyclops.monads.WitnessType;
import cyclops.monads.transformers.reactor.MonoT;
import cyclops.typeclasses.Active;
import cyclops.typeclasses.InstanceDefinitions;
import cyclops.typeclasses.Nested;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;


import reactor.core.Cancellation;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoProcessor;
import reactor.core.publisher.Signal;
import reactor.core.publisher.SignalType;
import reactor.core.publisher.SynchronousSink;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.TimedScheduler;
import reactor.util.function.Tuple2;

/**
 * Simulates Higher Kinded Types for Reactor Mono's
 *
 * MonoKind is a Mono and a Higher Kinded Type (mono,T)
 *
 * @author johnmcclean
 *
 * @param <T> Data type stored within the Mono
 */


public final class MonoKind<T> implements Higher<mono, T>, Publisher<T> {
    private MonoKind(Mono<T> boxed) {
        this.boxed = boxed;
    }


    public <R> MonoKind<R> fold(Function<? super Mono<?  super T>,? extends Mono<R>> op){
        return widen(op.apply(boxed));
    }
    public Active<mono,T> allTypeclasses(){
        return Active.of(this, Monos.Instances.definitions());
    }

    public static <T> Higher<mono,T> widenK(final Mono<T> completableList) {

        return new MonoKind<>(
                completableList);
    }
    public <W2,R> Nested<ReactorWitness.mono,W2,R> mapM(Function<? super T,? extends Higher<W2,R>> fn, InstanceDefinitions<W2> defs){
        return Monos.mapM(boxed,fn,defs);
    }

    public <W extends WitnessType<W>> MonoT<W, T> liftM(W witness) {
        return Monos.liftM(boxed,witness);
    }

    /**
     * Construct a HKT encoded completed Mono
     *
     * @param value To encode inside a HKT encoded Mono
     * @return Completed HKT encoded FMono
     */
    public static <T> MonoKind<T> just(T value){

        return widen(Mono.just(value));
    }
    public static <T> MonoKind<T> empty(){
        return widen(Mono.empty());
    }

    /**
     * Convert a Mono to a simulated HigherKindedType that captures Mono nature
     * and Mono element data type separately. Recover via @see MonoKind#narrow
     *
     * If the supplied Mono implements MonoKind it is returned already, otherwise it
     * is wrapped into a Mono implementation that does implement MonoKind
     *
     * @param completableMono Mono to widen to a MonoKind
     * @return MonoKind encoding HKT info about Monos
     */
    public static <T> MonoKind<T> widen(final Mono<T> completableMono) {

        return new MonoKind<T>(
                         completableMono);
    }

    public static <T> MonoKind<T> widen(final Publisher<T> completableMono) {

        return new MonoKind<T>(Mono.from(
                         completableMono));
    }


    /**
     * Convert the raw Higher Kinded Type for MonoKind types into the MonoKind type definition class
     *
     * @param future HKT encoded list into a MonoKind
     * @return MonoKind
     */
    public static <T> MonoKind<T> narrowK(final Higher<mono, T> future) {
       return (MonoKind<T>)future;
    }

    /**
     * Convert the HigherKindedType definition for a Mono into
     *
     * @param completableMono Type Constructor to convert back into narrowed type
     * @return Mono from Higher Kinded Type
     */
    public static <T> Mono<T> narrow(final Higher<mono, T> completableMono) {

            return ((MonoKind<T>)completableMono).narrow();



    }



        private final Mono<T> boxed;

        /**
         * @return wrapped Mono
         */
        public Mono<T> narrow() {
            return boxed;
        }


        public Future<T> toFuture(){
            return Future.of(boxed.toFuture());
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
         * @param transformer
         * @return
         * @see reactor.core.publisher.Mono#as(java.util.function.Function)
         */
        public final <P> P as(Function<? super Mono<T>, P> transformer) {
            return boxed.as(transformer);
        }
        /**
         * @param other
         * @return
         * @see reactor.core.publisher.Mono#and(reactor.core.publisher.Mono)
         */
        public final <T2> Mono<Tuple2<T, T2>> and(Mono<? extends T2> other) {
            return boxed.and(other);
        }
        /**
         * @return
         * @see reactor.core.publisher.Mono#awaitOnSubscribe()
         */
        public final Mono<T> awaitOnSubscribe() {
            return boxed.awaitOnSubscribe();
        }
        /**
         * @return
         * @see reactor.core.publisher.Mono#block()
         */
        public T block() {
            return boxed.block();
        }
        /**
         * @param timeout
         * @return
         * @see reactor.core.publisher.Mono#block(java.time.Duration)
         */
        public final T block(Duration timeout) {
            return boxed.block(timeout);
        }
        /**
         * @param timeout
         * @return
         * @see reactor.core.publisher.Mono#blockMillis(long)
         */
        public T blockMillis(long timeout) {
            return boxed.blockMillis(timeout);
        }
        /**
         * @param clazz
         * @return
         * @see reactor.core.publisher.Mono#cast(java.lang.Class)
         */
        public final <E> Mono<E> cast(Class<E> clazz) {
            return boxed.cast(clazz);
        }
        /**
         * @return
         * @see reactor.core.publisher.Mono#cache()
         */
        public final Mono<T> cache() {
            return boxed.cache();
        }
        /**
         * @param scheduler
         * @return
         * @see reactor.core.publisher.Mono#cancelOn(reactor.core.scheduler.Scheduler)
         */
        public final Mono<T> cancelOn(Scheduler scheduler) {
            return boxed.cancelOn(scheduler);
        }
        /**
         * @param transformer
         * @return
         * @see reactor.core.publisher.Mono#compose(java.util.function.Function)
         */
        public final <V> Mono<V> compose(Function<? super Mono<T>, ? extends Publisher<V>> transformer) {
            return boxed.compose(transformer);
        }
        /**
         * @param other
         * @return
         * @see reactor.core.publisher.Mono#concatWith(org.reactivestreams.Publisher)
         */
        public final Flux<T> concatWith(Publisher<? extends T> other) {
            return boxed.concatWith(other);
        }
        /**
         * @param defaultV
         * @return
         * @see reactor.core.publisher.Mono#defaultIfEmpty(java.lang.Object)
         */
        public final Mono<T> defaultIfEmpty(T defaultV) {
            return boxed.defaultIfEmpty(defaultV);
        }
        /**
         * @param delay
         * @return
         * @see reactor.core.publisher.Mono#delaySubscription(java.time.Duration)
         */
        public final Mono<T> delaySubscription(Duration delay) {
            return boxed.delaySubscription(delay);
        }
        /**
         * @param subscriptionDelay
         * @return
         * @see reactor.core.publisher.Mono#delaySubscription(org.reactivestreams.Publisher)
         */
        public final <U> Mono<T> delaySubscription(Publisher<U> subscriptionDelay) {
            return boxed.delaySubscription(subscriptionDelay);
        }
        /**
         * @param delay
         * @return
         * @see reactor.core.publisher.Mono#delaySubscriptionMillis(long)
         */
        public final Mono<T> delaySubscriptionMillis(long delay) {
            return boxed.delaySubscriptionMillis(delay);
        }
        /**
         * @param delay
         * @param timer
         * @return
         * @see reactor.core.publisher.Mono#delaySubscriptionMillis(long, reactor.core.scheduler.TimedScheduler)
         */
        public final Mono<T> delaySubscriptionMillis(long delay, TimedScheduler timer) {
            return boxed.delaySubscriptionMillis(delay, timer);
        }
        /**
         * @return
         * @see reactor.core.publisher.Mono#dematerialize()
         */
        public final <X> Mono<X> dematerialize() {
            return boxed.dematerialize();
        }
        /**
         * @param afterTerminate
         * @return
         * @see reactor.core.publisher.Mono#doAfterTerminate(java.util.function.BiConsumer)
         */
        public final Mono<T> doAfterTerminate(BiConsumer<? super T, Throwable> afterTerminate) {
            return boxed.doAfterTerminate(afterTerminate);
        }
        /**
         * @param onCancel
         * @return
         * @see reactor.core.publisher.Mono#doOnCancel(java.lang.Runnable)
         */
        public final Mono<T> doOnCancel(Runnable onCancel) {
            return boxed.doOnCancel(onCancel);
        }
        /**
         * @param onNext
         * @return
         * @see reactor.core.publisher.Mono#doOnNext(java.util.function.Consumer)
         */
        public final Mono<T> doOnNext(Consumer<? super T> onNext) {
            return boxed.doOnNext(onNext);
        }
        /**
         * @param onSuccess
         * @return
         * @see reactor.core.publisher.Mono#doOnSuccess(java.util.function.Consumer)
         */
        public final Mono<T> doOnSuccess(Consumer<? super T> onSuccess) {
            return boxed.doOnSuccess(onSuccess);
        }
        /**
         * @param onError
         * @return
         * @see reactor.core.publisher.Mono#doOnError(java.util.function.Consumer)
         */
        public final Mono<T> doOnError(Consumer<? super Throwable> onError) {
            return boxed.doOnError(onError);
        }
        /**
         * @param exceptionType
         * @param onError
         * @return
         * @see reactor.core.publisher.Mono#doOnError(java.lang.Class, java.util.function.Consumer)
         */
        public final <E extends Throwable> Mono<T> doOnError(Class<E> exceptionType, Consumer<? super E> onError) {
            return boxed.doOnError(exceptionType, onError);
        }
        /**
         * @param predicate
         * @param onError
         * @return
         * @see reactor.core.publisher.Mono#doOnError(java.util.function.Predicate, java.util.function.Consumer)
         */
        public final Mono<T> doOnError(Predicate<? super Throwable> predicate, Consumer<? super Throwable> onError) {
            return boxed.doOnError(predicate, onError);
        }
        /**
         * @param consumer
         * @return
         * @see reactor.core.publisher.Mono#doOnRequest(java.util.function.LongConsumer)
         */
        public final Mono<T> doOnRequest(LongConsumer consumer) {
            return boxed.doOnRequest(consumer);
        }
        /**
         * @param onSubscribe
         * @return
         * @see reactor.core.publisher.Mono#doOnSubscribe(java.util.function.Consumer)
         */
        public final Mono<T> doOnSubscribe(Consumer<? super Subscription> onSubscribe) {
            return boxed.doOnSubscribe(onSubscribe);
        }
        /**
         * @param onTerminate
         * @return
         * @see reactor.core.publisher.Mono#doOnTerminate(java.util.function.BiConsumer)
         */
        public final Mono<T> doOnTerminate(BiConsumer<? super T, Throwable> onTerminate) {
            return boxed.doOnTerminate(onTerminate);
        }
        /**
         * @return
         * @see reactor.core.publisher.Mono#elapsed()
         */
        public final Mono<Tuple2<Long, T>> elapsed() {
            return boxed.elapsed();
        }
        /**
         * @param scheduler
         * @return
         * @see reactor.core.publisher.Mono#elapsed(reactor.core.scheduler.TimedScheduler)
         */
        public final Mono<Tuple2<Long, T>> elapsed(TimedScheduler scheduler) {
            return boxed.elapsed(scheduler);
        }
        /**
         * @param tester
         * @return
         * @see reactor.core.publisher.Mono#filter(java.util.function.Predicate)
         */
        public final Mono<T> filter(Predicate<? super T> tester) {
            return boxed.filter(tester);
        }
        /**
         * @param mapper
         * @return
         * @see reactor.core.publisher.Mono#flatMap(java.util.function.Function)
         */
        public final <R> Flux<R> flatMap(Function<? super T, ? extends Publisher<? extends R>> mapper) {
            return boxed.flatMap(mapper);
        }
        /**
         * @param mapperOnNext
         * @param mapperOnError
         * @param mapperOnComplete
         * @return
         * @see reactor.core.publisher.Mono#flatMap(java.util.function.Function, java.util.function.Function, java.util.function.Supplier)
         */
        public final <R> Flux<R> flatMap(Function<? super T, ? extends Publisher<? extends R>> mapperOnNext,
                Function<Throwable, ? extends Publisher<? extends R>> mapperOnError,
                Supplier<? extends Publisher<? extends R>> mapperOnComplete) {
            return boxed.flatMap(mapperOnNext, mapperOnError, mapperOnComplete);
        }
        /**
         * @param mapper
         * @return
         * @see reactor.core.publisher.Mono#flatMapIterable(java.util.function.Function)
         */
        public final <R> Flux<R> flatMapIterable(Function<? super T, ? extends Iterable<? extends R>> mapper) {
            return boxed.flatMapIterable(mapper);
        }
        /**
         * @return
         * @see reactor.core.publisher.Mono#flux()
         */
        public final Flux<T> flux() {
            return boxed.flux();
        }
        /**
         * @return
         * @see reactor.core.publisher.Mono#hasElement()
         */
        public final Mono<Boolean> hasElement() {
            return boxed.hasElement();
        }
        /**
         * @param handler
         * @return
         * @see reactor.core.publisher.Mono#handle(java.util.function.BiConsumer)
         */
        public final <R> Mono<R> handle(BiConsumer<? super T, SynchronousSink<R>> handler) {
            return boxed.handle(handler);
        }
        /**
         * @return
         * @see reactor.core.publisher.Mono#hide()
         */
        public final Mono<T> hide() {
            return boxed.hide();
        }
        /**
         * @return
         * @see reactor.core.publisher.Mono#ignoreElement()
         */
        public final Mono<T> ignoreElement() {
            return boxed.ignoreElement();
        }
        /**
         * @return
         * @see reactor.core.publisher.Mono#log()
         */
        public final Mono<T> log() {
            return boxed.log();
        }
        /**
         * @param category
         * @return
         * @see reactor.core.publisher.Mono#log(java.lang.String)
         */
        public final Mono<T> log(String category) {
            return boxed.log(category);
        }
        /**
         * @param category
         * @param level
         * @param options
         * @return
         * @see reactor.core.publisher.Mono#log(java.lang.String, java.util.logging.Level, reactor.core.publisher.SignalType[])
         */
        public final Mono<T> log(String category, Level level, SignalType... options) {
            return boxed.log(category, level, options);
        }
        /**
         * @param category
         * @param level
         * @param showOperatorLine
         * @param options
         * @return
         * @see reactor.core.publisher.Mono#log(java.lang.String, java.util.logging.Level, boolean, reactor.core.publisher.SignalType[])
         */
        public final Mono<T> log(String category, Level level, boolean showOperatorLine, SignalType... options) {
            return boxed.log(category, level, showOperatorLine, options);
        }
        /**
         * @param mapper
         * @return
         * @see reactor.core.publisher.Mono#map(java.util.function.Function)
         */
        public final <R> Mono<R> map(Function<? super T, ? extends R> mapper) {
            return boxed.map(mapper);
        }
        /**
         * @param mapper
         * @return
         * @see reactor.core.publisher.Mono#mapError(java.util.function.Function)
         */
        public final Mono<T> mapError(Function<Throwable, ? extends Throwable> mapper) {
            return boxed.mapError(mapper);
        }
        /**
         * @param type
         * @param mapper
         * @return
         * @see reactor.core.publisher.Mono#mapError(java.lang.Class, java.util.function.Function)
         */
        public final <E extends Throwable> Mono<T> mapError(Class<E> type,
                Function<? super E, ? extends Throwable> mapper) {
            return boxed.mapError(type, mapper);
        }
        /**
         * @param predicate
         * @param mapper
         * @return
         * @see reactor.core.publisher.Mono#mapError(java.util.function.Predicate, java.util.function.Function)
         */
        public final Mono<T> mapError(Predicate<? super Throwable> predicate,
                Function<? super Throwable, ? extends Throwable> mapper) {
            return boxed.mapError(predicate, mapper);
        }
        /**
         * @return
         * @see reactor.core.publisher.Mono#materialize()
         */
        public final Mono<Signal<T>> materialize() {
            return boxed.materialize();
        }
        /**
         * @param other
         * @return
         * @see reactor.core.publisher.Mono#mergeWith(org.reactivestreams.Publisher)
         */
        public final Flux<T> mergeWith(Publisher<? extends T> other) {
            return boxed.mergeWith(other);
        }
        /**
         * @param other
         * @return
         * @see reactor.core.publisher.Mono#or(reactor.core.publisher.Mono)
         */
        public final Mono<T> or(Mono<? extends T> other) {
            return boxed.or(other);
        }
        /**
         * @param clazz
         * @return
         * @see reactor.core.publisher.Mono#ofType(java.lang.Class)
         */
        public final <U> Mono<U> ofType(Class<U> clazz) {
            return boxed.ofType(clazz);
        }
        /**
         * @param fallback
         * @return
         * @see reactor.core.publisher.Mono#otherwise(java.util.function.Function)
         */
        public final Mono<T> otherwise(Function<? super Throwable, ? extends Mono<? extends T>> fallback) {
            return boxed.otherwise(fallback);
        }
        /**
         * @param type
         * @param fallback
         * @return
         * @see reactor.core.publisher.Mono#otherwise(java.lang.Class, java.util.function.Function)
         */
        public final <E extends Throwable> Mono<T> otherwise(Class<E> type,
                Function<? super E, ? extends Mono<? extends T>> fallback) {
            return boxed.otherwise(type, fallback);
        }
        /**
         * @param predicate
         * @param fallback
         * @return
         * @see reactor.core.publisher.Mono#otherwise(java.util.function.Predicate, java.util.function.Function)
         */
        public final Mono<T> otherwise(Predicate<? super Throwable> predicate,
                Function<? super Throwable, ? extends Mono<? extends T>> fallback) {
            return boxed.otherwise(predicate, fallback);
        }
        /**
         * @param alternate
         * @return
         * @see reactor.core.publisher.Mono#otherwiseIfEmpty(reactor.core.publisher.Mono)
         */
        public final Mono<T> otherwiseIfEmpty(Mono<? extends T> alternate) {
            return boxed.otherwiseIfEmpty(alternate);
        }
        /**
         * @param fallback
         * @return
         * @see reactor.core.publisher.Mono#otherwiseReturn(java.lang.Object)
         */
        public final Mono<T> otherwiseReturn(T fallback) {
            return boxed.otherwiseReturn(fallback);
        }
        /**
         * @param type
         * @param fallbackValue
         * @return
         * @see reactor.core.publisher.Mono#otherwiseReturn(java.lang.Class, java.lang.Object)
         */
        public final <E extends Throwable> Mono<T> otherwiseReturn(Class<E> type, T fallbackValue) {
            return boxed.otherwiseReturn(type, fallbackValue);
        }
        /**
         * @param predicate
         * @param fallbackValue
         * @return
         * @see reactor.core.publisher.Mono#otherwiseReturn(java.util.function.Predicate, java.lang.Object)
         */
        public final <E extends Throwable> Mono<T> otherwiseReturn(Predicate<? super Throwable> predicate,
                T fallbackValue) {
            return boxed.otherwiseReturn(predicate, fallbackValue);
        }
        /**
         * @return
         * @see reactor.core.publisher.Mono#onTerminateDetach()
         */
        public final Mono<T> onTerminateDetach() {
            return boxed.onTerminateDetach();
        }
        /**
         * @param transform
         * @return
         * @see reactor.core.publisher.Mono#publish(java.util.function.Function)
         */
        public final <R> Mono<R> publish(Function<? super Mono<T>, ? extends Mono<? extends R>> transform) {
            return boxed.publish(transform);
        }
        /**
         * @param scheduler
         * @return
         * @see reactor.core.publisher.Mono#publishOn(reactor.core.scheduler.Scheduler)
         */
        public final Mono<T> publishOn(Scheduler scheduler) {
            return boxed.publishOn(scheduler);
        }
        /**
         * @return
         * @see reactor.core.publisher.Mono#repeat()
         */
        public final Flux<T> repeat() {
            return boxed.repeat();
        }
        /**
         * @param predicate
         * @return
         * @see reactor.core.publisher.Mono#repeat(java.util.function.BooleanSupplier)
         */
        public final Flux<T> repeat(BooleanSupplier predicate) {
            return boxed.repeat(predicate);
        }
        /**
         * @param numRepeat
         * @return
         * @see reactor.core.publisher.Mono#repeat(long)
         */
        public final Flux<T> repeat(long numRepeat) {
            return boxed.repeat(numRepeat);
        }
        /**
         * @param numRepeat
         * @param predicate
         * @return
         * @see reactor.core.publisher.Mono#repeat(long, java.util.function.BooleanSupplier)
         */
        public final Flux<T> repeat(long numRepeat, BooleanSupplier predicate) {
            return boxed.repeat(numRepeat, predicate);
        }
        /**
         * @param whenFactory
         * @return
         * @see reactor.core.publisher.Mono#repeatWhen(java.util.function.Function)
         */
        public final Flux<T> repeatWhen(Function<Flux<Long>, ? extends Publisher<?>> whenFactory) {
            return boxed.repeatWhen(whenFactory);
        }
        /**
         * @param repeatFactory
         * @return
         * @see reactor.core.publisher.Mono#repeatWhenEmpty(java.util.function.Function)
         */
        public final Mono<T> repeatWhenEmpty(Function<Flux<Long>, ? extends Publisher<?>> repeatFactory) {
            return boxed.repeatWhenEmpty(repeatFactory);
        }
        /**
         * @param maxRepeat
         * @param repeatFactory
         * @return
         * @see reactor.core.publisher.Mono#repeatWhenEmpty(int, java.util.function.Function)
         */
        public final Mono<T> repeatWhenEmpty(int maxRepeat,
                Function<Flux<Long>, ? extends Publisher<?>> repeatFactory) {
            return boxed.repeatWhenEmpty(maxRepeat, repeatFactory);
        }
        /**
         * @return
         * @see reactor.core.publisher.Mono#retry()
         */
        public final Mono<T> retry() {
            return boxed.retry();
        }
        /**
         * @param numRetries
         * @return
         * @see reactor.core.publisher.Mono#retry(long)
         */
        public final Mono<T> retry(long numRetries) {
            return boxed.retry(numRetries);
        }
        /**
         * @param retryMatcher
         * @return
         * @see reactor.core.publisher.Mono#retry(java.util.function.Predicate)
         */
        public final Mono<T> retry(Predicate<Throwable> retryMatcher) {
            return boxed.retry(retryMatcher);
        }
        /**
         * @param numRetries
         * @param retryMatcher
         * @return
         * @see reactor.core.publisher.Mono#retry(long, java.util.function.Predicate)
         */
        public final Mono<T> retry(long numRetries, Predicate<Throwable> retryMatcher) {
            return boxed.retry(numRetries, retryMatcher);
        }
        /**
         * @param whenFactory
         * @return
         * @see reactor.core.publisher.Mono#retryWhen(java.util.function.Function)
         */
        public final Mono<T> retryWhen(Function<Flux<Throwable>, ? extends Publisher<?>> whenFactory) {
            return boxed.retryWhen(whenFactory);
        }
        /**
         * @return
         * @see reactor.core.publisher.Mono#subscribe()
         */
        public final MonoProcessor<T> subscribe() {
            return boxed.subscribe();
        }
        /**
         * @param consumer
         * @return
         * @see reactor.core.publisher.Mono#subscribe(java.util.function.Consumer)
         */
        public final Cancellation subscribe(Consumer<? super T> consumer) {
            return boxed.subscribe(consumer);
        }
        /**
         * @param consumer
         * @param errorConsumer
         * @return
         * @see reactor.core.publisher.Mono#subscribe(java.util.function.Consumer, java.util.function.Consumer)
         */
        public final Cancellation subscribe(Consumer<? super T> consumer, Consumer<? super Throwable> errorConsumer) {
            return boxed.subscribe(consumer, errorConsumer);
        }
        /**
         * @param consumer
         * @param errorConsumer
         * @param completeConsumer
         * @return
         * @see reactor.core.publisher.Mono#subscribe(java.util.function.Consumer, java.util.function.Consumer, java.lang.Runnable)
         */
        public final Cancellation subscribe(Consumer<? super T> consumer, Consumer<? super Throwable> errorConsumer,
                Runnable completeConsumer) {
            return boxed.subscribe(consumer, errorConsumer, completeConsumer);
        }
        /**
         * @param scheduler
         * @return
         * @see reactor.core.publisher.Mono#subscribeOn(reactor.core.scheduler.Scheduler)
         */
        public final Mono<T> subscribeOn(Scheduler scheduler) {
            return boxed.subscribeOn(scheduler);
        }
        /**
         * @param subscriber
         * @return
         * @see reactor.core.publisher.Mono#subscribeWith(org.reactivestreams.Subscriber)
         */
        public final <E extends Subscriber<? super T>> E subscribeWith(E subscriber) {
            return boxed.subscribeWith(subscriber);
        }
        /**
         * @return
         * @see reactor.core.publisher.Mono#then()
         */
        public final Mono<Void> then() {
            return boxed.then();
        }

        /**
         * @param other
         * @return
         * @see reactor.core.publisher.Mono#then(reactor.core.publisher.Mono)
         */
        public final <V> Mono<V> then(Mono<V> other) {
            return boxed.then(other);
        }
        /**
         * @param sourceSupplier
         * @return
         * @see reactor.core.publisher.Mono#then(java.util.function.Supplier)
         */
        public final <V> Mono<V> then(Supplier<? extends Mono<V>> sourceSupplier) {
            return boxed.then(sourceSupplier);
        }
        /**
         * @param other
         * @return
         * @see reactor.core.publisher.Mono#thenMany(org.reactivestreams.Publisher)
         */
        public final <V> Flux<V> thenMany(Publisher<V> other) {
            return boxed.thenMany(other);
        }
        /**
         * @param sourceSupplier
         * @return
         * @see reactor.core.publisher.Mono#thenMany(java.util.function.Supplier)
         */
        public final <V> Flux<V> thenMany(Supplier<? extends Mono<V>> sourceSupplier) {
            return boxed.thenMany(sourceSupplier);
        }
        /**
         * @param timeout
         * @return
         * @see reactor.core.publisher.Mono#timeout(java.time.Duration)
         */
        public final Mono<T> timeout(Duration timeout) {
            return boxed.timeout(timeout);
        }
        /**
         * @param timeout
         * @param fallback
         * @return
         * @see reactor.core.publisher.Mono#timeout(java.time.Duration, reactor.core.publisher.Mono)
         */
        public final Mono<T> timeout(Duration timeout, Mono<? extends T> fallback) {
            return boxed.timeout(timeout, fallback);
        }
        /**
         * @param firstTimeout
         * @return
         * @see reactor.core.publisher.Mono#timeout(org.reactivestreams.Publisher)
         */
        public final <U> Mono<T> timeout(Publisher<U> firstTimeout) {
            return boxed.timeout(firstTimeout);
        }
        /**
         * @param firstTimeout
         * @param fallback
         * @return
         * @see reactor.core.publisher.Mono#timeout(org.reactivestreams.Publisher, reactor.core.publisher.Mono)
         */
        public final <U> Mono<T> timeout(Publisher<U> firstTimeout, Mono<? extends T> fallback) {
            return boxed.timeout(firstTimeout, fallback);
        }
        /**
         * @param timeout
         * @return
         * @see reactor.core.publisher.Mono#timeoutMillis(long)
         */
        public final Mono<T> timeoutMillis(long timeout) {
            return boxed.timeoutMillis(timeout);
        }
        /**
         * @param timeout
         * @param timer
         * @return
         * @see reactor.core.publisher.Mono#timeoutMillis(long, reactor.core.scheduler.TimedScheduler)
         */
        public final Mono<T> timeoutMillis(long timeout, TimedScheduler timer) {
            return boxed.timeoutMillis(timeout, timer);
        }
        /**
         * @param timeout
         * @param fallback
         * @return
         * @see reactor.core.publisher.Mono#timeoutMillis(long, reactor.core.publisher.Mono)
         */
        public final Mono<T> timeoutMillis(long timeout, Mono<? extends T> fallback) {
            return boxed.timeoutMillis(timeout, fallback);
        }
        /**
         * @param timeout
         * @param fallback
         * @param timer
         * @return
         * @see reactor.core.publisher.Mono#timeoutMillis(long, reactor.core.publisher.Mono, reactor.core.scheduler.TimedScheduler)
         */
        public final Mono<T> timeoutMillis(long timeout, Mono<? extends T> fallback, TimedScheduler timer) {
            return boxed.timeoutMillis(timeout, fallback, timer);
        }
        /**
         * @return
         * @see reactor.core.publisher.Mono#timestamp()
         */
        public final Mono<Tuple2<Long, T>> timestamp() {
            return boxed.timestamp();
        }
        /**
         * @param scheduler
         * @return
         * @see reactor.core.publisher.Mono#timestamp(reactor.core.scheduler.TimedScheduler)
         */
        public final Mono<Tuple2<Long, T>> timestamp(TimedScheduler scheduler) {
            return boxed.timestamp(scheduler);
        }

        /**
         * @param transformer
         * @return
         * @see reactor.core.publisher.Mono#transform(java.util.function.Function)
         */
        public final <V> Mono<V> transformMono(Function<? super Mono<T>, ? extends Publisher<V>> transformer) {
            return boxed.transform(transformer);
        }
        /**
         * @return
         * @see reactor.core.publisher.Mono#toString()
         */
        public String toString() {
            return boxed.toString();
        }




}
