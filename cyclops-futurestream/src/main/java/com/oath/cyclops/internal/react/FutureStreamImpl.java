package com.oath.cyclops.internal.react;

import com.oath.cyclops.async.QueueFactories;
import com.oath.cyclops.async.adapters.QueueFactory;
import com.oath.cyclops.internal.react.stream.LazyStreamWrapper;
import com.oath.cyclops.react.async.subscription.Continueable;
import com.oath.cyclops.react.async.subscription.Subscription;
import com.oath.cyclops.react.collectors.lazy.BatchingCollector;
import com.oath.cyclops.react.collectors.lazy.LazyResultConsumer;
import com.oath.cyclops.react.collectors.lazy.MaxActive;
import com.oath.cyclops.react.threads.ReactPool;
import com.oath.cyclops.types.reactive.FutureStreamSynchronousPublisher;
import cyclops.companion.Streams;
import cyclops.function.Monoid;
import cyclops.function.Reducer;
import cyclops.futurestream.FutureStream;
import cyclops.futurestream.LazyReact;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.With;
import org.reactivestreams.Subscriber;

import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class FutureStreamImpl<U> implements FutureStream<U> {

    @With
    private final Optional<Consumer<Throwable>> errorHandler;
    private final LazyStreamWrapper<U> lastActive;

    @With
    private final Supplier<LazyResultConsumer<U>> lazyCollector;
    @With
    private final QueueFactory<U> queueFactory;
    @With
    private final LazyReact simpleReact;
    @With
    private final Continueable subscription;
    private final static ReactPool<LazyReact> pool = ReactPool.elasticPool(() -> new LazyReact(
                                                                                               Executors.newSingleThreadExecutor()));
    @With
    private final ConsumerHolder error;
    @With
    private final MaxActive maxActive;

    @AllArgsConstructor
    static class ConsumerHolder {
        volatile Consumer<Throwable> forward;
    }

    public FutureStreamImpl(final LazyReact lazyReact, final Stream<U> stream) {

        this.simpleReact = lazyReact;

        this.lastActive = new LazyStreamWrapper<>(()->
                                                  stream, lazyReact);
        this.error = new ConsumerHolder(
                                        a -> {
                                        });
        this.errorHandler = Optional.of((e) -> {
            error.forward.accept(e);

        });
        this.lazyCollector = () -> new BatchingCollector<U>(
                                                            getMaxActive(), this);
        this.queueFactory = QueueFactories.unboundedNonBlockingQueue();
        this.subscription = new Subscription();

        this.maxActive = lazyReact.getMaxActive();

    }

    private final AtomicBoolean subscribed = new AtomicBoolean(false);
    @Override
    public void subscribe(Subscriber<? super U> s) {
        if(subscribed.compareAndSet(false,true))
            new FutureStreamSynchronousPublisher(this).subscribe(s);
    }

    public FutureStreamImpl(final LazyReact lazyReact, final Supplier<Stream<U>> stream) {

        this.simpleReact = lazyReact;

        this.lastActive = new LazyStreamWrapper<>((Supplier)stream, lazyReact);
        this.error = new ConsumerHolder(
                a -> {
                });
        this.errorHandler = Optional.of((e) -> {
            error.forward.accept(e);

        });
        this.lazyCollector = () -> new BatchingCollector<U>(
                getMaxActive(), this);
        this.queueFactory = QueueFactories.unboundedNonBlockingQueue();
        this.subscription = new Subscription();

        this.maxActive = lazyReact.getMaxActive();

    }



    public void forwardErrors(final Consumer<Throwable> c) {
        error.forward = c;
    }

    @Override
    public LazyReact getPopulator() {
        return pool.nextReactor();
    }

    @Override
    public void returnPopulator(final LazyReact service) {
        pool.populate(service);
    }

    @Override
    public <R, A> R collect(final Collector<? super U, A, R> collector) {
        return block(collector);
    }


    public void close() {

    }

    @Override
    public FutureStream<U> withAsync(final boolean b) {

        return this.withSimpleReact(this.simpleReact.withAsync(b));
    }

    @Override
    public Executor getTaskExecutor() {
        return this.simpleReact.getExecutor();
    }



    @Override
    public boolean isAsync() {
        return this.simpleReact.isAsync();
    }

    @Override
    public FutureStream<U> withTaskExecutor(final Executor e) {
        return this.withSimpleReact(simpleReact.withExecutor(e));
    }




    @Override
    public <U> FutureStream<U> withLastActive(final LazyStreamWrapper<U> w) {
        return new FutureStreamImpl(
                errorHandler, w, lazyCollector, queueFactory, simpleReact, subscription, error, maxActive);

    }


    @Override
    public FutureStream<U> maxActive(final int max) {
        return this.withMaxActive(new MaxActive(
                                                max, max));
    }

    /**
     * Cancel the futures in this stage of the stream
     */
    public void cancel() {
        this.subscription.closeAll();
        //also need to mark cancelled =true and check during toX
    }



    @Override
    public <T> FutureStream<T> unitIterable(final Iterable<T> it) {
        return simpleReact.fromIterable(it);
    }

    @Override
    public FutureStream<U> append(final U value) {
        return fromStream(stream().append(value));
    }

    @Override
    public FutureStream<U> prepend(final U value) {

        return fromStream(stream().prepend(value));
    }











    @Override
    public U foldRight(final Monoid<U> reducer) {
        return reducer.foldRight(this);
    }

    @Override
    public <T> T foldMapRight(final Reducer<T,U> reducer) {
        return reducer.foldMap(reverse().stream());

    }

    @Override
    public <R> R foldMap(final Reducer<R,U> reducer) {
        return reducer.foldMap(stream());
    }

    @Override
    public <R> R foldMap(final Function<? super U, ? extends R> mapper, final Monoid<R> reducer) {

        return Reducer.fromMonoid(reducer,(U a)-> (R)mapper.apply(a))
                      .foldMap(stream());
    }


    @Override
    public U foldRight(final U identity, final BinaryOperator<U> accumulator) {
        return reverse().foldLeft(identity, accumulator);
    }



    @Override
    public long count() {
        return this.collect(Collectors.toList())
                   .size();
    }

    @Override
    public boolean allMatch(final Predicate<? super U> c) {

        return filterNot(c).count() == 0l;
    }

    @Override
    public boolean anyMatch(final Predicate<? super U> c) {

        return filter(c).findAny()
                        .isPresent();
    }

    @Override
    public boolean xMatch(final int num, final Predicate<? super U> c) {

        return Streams.xMatch(stream(), num, c);
    }

    @Override
    public boolean noneMatch(final Predicate<? super U> c) {
        return !anyMatch(c);
    }

    @Override
    public final String join() {
        return Streams.join(stream());
    }

    @Override
    public final String join(final String sep) {
        return Streams.join(stream(), sep);
    }

    @Override
    public String join(final String sep, final String start, final String end) {
        return Streams.join(stream(), sep, start, end);
    }


}
