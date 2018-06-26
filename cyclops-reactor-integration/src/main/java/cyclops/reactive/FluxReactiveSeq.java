package cyclops.reactive;

import com.oath.cyclops.reactor.adapter.FluxReactiveSeqImpl;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.SynchronousSink;
import reactor.core.scheduler.Scheduler;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

/*
 * Factory methods for creating ReactiveSeq instances that are backed by Flux
 */
public interface FluxReactiveSeq {

    public static <T> ReactiveSeq<T> reactiveSeq(Flux<T> flux){
        return new FluxReactiveSeqImpl<>(flux);
    }
    public static <T> ReactiveSeq<T> reactiveSeq(Publisher<T> flux){
        return new FluxReactiveSeqImpl<>(Flux.from(flux));
    }

    public static ReactiveSeq<Integer> range(int start, int end){
        return reactiveSeq(Flux.range(start,end));
    }
    public static <T> ReactiveSeq<T> of(T... data) {
        return reactiveSeq(Flux.just(data));
    }
    public static  <T> ReactiveSeq<T> of(T value){
        return reactiveSeq(Flux.just(value));
    }

    public static <T> ReactiveSeq<T> ofNullable(T nullable){
        if(nullable==null){
            return empty();
        }
        return of(nullable);
    }
    public static <T> ReactiveSeq<T> create(Consumer<? super FluxSink<T>> emitter) {
        return reactiveSeq(Flux.create(emitter));
    }


    public static <T> ReactiveSeq<T> create(Consumer<? super FluxSink<T>> emitter, FluxSink.OverflowStrategy backpressure) {
        return reactiveSeq(Flux.create(emitter,backpressure));
    }


    public static <T> ReactiveSeq<T> defer(Supplier<? extends Publisher<T>> supplier) {
        return reactiveSeq(Flux.defer(supplier));
    }

    public static <T> ReactiveSeq<T> empty() {
        return reactiveSeq(Flux.empty());
    }


    public static <T> ReactiveSeq<T> error(Throwable error) {
        return reactiveSeq(Flux.error(error));
    }


    public static <O> ReactiveSeq<O> error(Throwable throwable, boolean whenRequested) {
        return reactiveSeq(Flux.error(throwable,whenRequested));
    }


    @SafeVarargs
    public static <I> ReactiveSeq<I> first(Publisher<? extends I>... sources) {
        return reactiveSeq(Flux.first(sources));
    }


    public static <I> ReactiveSeq<I> first(Iterable<? extends Publisher<? extends I>> sources) {
        return reactiveSeq(Flux.first(sources));
    }


    public static <T> ReactiveSeq<T> from(Publisher<? extends T> source) {
        return reactiveSeq(Flux.from(source));
    }


    public static <T> ReactiveSeq<T> fromIterable(Iterable<? extends T> it) {
        return reactiveSeq(Flux.fromIterable(it));
    }


    public static <T> ReactiveSeq<T> fromStream(Stream<? extends T> s) {
        return reactiveSeq(Flux.fromStream(s));
    }


    public static <T> ReactiveSeq<T> generate(Consumer<SynchronousSink<T>> generator) {
        return reactiveSeq(Flux.generate(generator));
    }


    public static <T, S> ReactiveSeq<T> generate(Callable<S> stateSupplier, BiFunction<S, SynchronousSink<T>, S> generator) {
        return reactiveSeq(Flux.generate(stateSupplier,generator));
    }


    public static <T, S> ReactiveSeq<T> generate(Callable<S> stateSupplier, BiFunction<S, SynchronousSink<T>, S> generator, Consumer<? super S> stateConsumer) {
        return reactiveSeq(Flux.generate(stateSupplier,generator,stateConsumer));
    }


    public static ReactiveSeq<Long> interval(Duration period) {
        return reactiveSeq(Flux.interval(period));
    }


    public static ReactiveSeq<Long> interval(Duration delay, Duration period) {
        return reactiveSeq(Flux.interval(delay,period));
    }
    public static ReactiveSeq<Long> interval(Duration period, Scheduler timer) {
        return reactiveSeq(Flux.interval(period,timer));
    }

    public static ReactiveSeq<Long> interval(Duration delay, Duration period, Scheduler timer) {
        return reactiveSeq(Flux.interval(delay,period,timer));
    }

    @SafeVarargs
    public static <T> ReactiveSeq<T> just(T... data) {
        return reactiveSeq(Flux.just(data));
    }


    public static <T> ReactiveSeq<T> just(T data) {
        return reactiveSeq(Flux.just(data));
    }


}
