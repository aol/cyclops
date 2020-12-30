package com.oath.cyclops.internal.react;

import java.util.Comparator;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.oath.cyclops.internal.react.stream.LazyStreamWrapper;
import com.oath.cyclops.react.async.subscription.Continueable;
import com.oath.cyclops.react.async.subscription.Subscription;
import com.oath.cyclops.react.collectors.lazy.BatchingCollector;
import com.oath.cyclops.react.collectors.lazy.LazyResultConsumer;
import com.oath.cyclops.react.collectors.lazy.MaxActive;
import com.oath.cyclops.react.threads.ReactPool;
import com.oath.cyclops.types.reactive.FutureStreamSynchronousPublisher;
import com.oath.cyclops.types.stream.Connectable;
import com.oath.cyclops.types.stream.PausableConnectable;
import cyclops.companion.Streams;
import cyclops.data.Seq;
import cyclops.futurestream.FutureStream;

import cyclops.function.Monoid;
import cyclops.function.Reducer;
import cyclops.futurestream.LazyReact;
import cyclops.reactive.ReactiveSeq;
import com.oath.cyclops.async.QueueFactories;
import com.oath.cyclops.async.adapters.QueueFactory;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.With;
import org.reactivestreams.Subscriber;


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
            FutureStream.super.subscribe(s);
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


    @Override
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

    @Override
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
     * Cancel the CompletableFutures in this stage of the stream
     */
    @Override
    public void cancel() {
        this.subscription.closeAll();
        //also need to mark cancelled =true and check during toX
    }

    @Override
    public Connectable<U> schedule(final String cron, final ScheduledExecutorService ex) {
        return ReactiveSeq.<U> fromStream(this.stream())
                          .schedule(cron, ex);
    }

    @Override
    public Connectable<U> scheduleFixedDelay(final long delay, final ScheduledExecutorService ex) {
        return ReactiveSeq.<U> fromStream(this.stream())
                          .scheduleFixedDelay(delay, ex);
    }

    @Override
    public Connectable<U> scheduleFixedRate(final long rate, final ScheduledExecutorService ex) {
        return ReactiveSeq.<U> fromStream(this.stream())
                          .scheduleFixedRate(rate, ex);
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
    public <T> FutureStream<T> unit(final T unit) {
        return fromStream(stream().unit(unit));
    }

    @Override
    public Connectable<U> hotStream(final Executor e) {
        return Streams.hotStream(this, e);
    }

    @Override
    public Connectable<U> primedHotStream(final Executor e) {
        return Streams.primedHotStream(this, e);
    }

    @Override
    public PausableConnectable<U> pausableHotStream(final Executor e) {
        return Streams.pausableHotStream(this, e);
    }

    @Override
    public PausableConnectable<U> primedPausableHotStream(final Executor e) {
        return Streams.primedPausableHotStream(this, e);
    }



    @Override
    public <R> R fold(Function<? super ReactiveSeq<U>, ? extends R> sync, Function<? super ReactiveSeq<U>, ? extends R> reactiveStreams, Function<? super ReactiveSeq<U>, ? extends R> asyncNoBackPressure) {
        return sync.apply(this);
    }



    @Override
    public U foldRight(final Monoid<U> reducer) {
        return reducer.foldRight(this);
    }

    @Override
    public <T> T foldMapRight(final Reducer<T,U> reducer) {
        return reducer.foldMap(reverse());

    }

    @Override
    public <R> R foldMap(final Reducer<R,U> reducer) {
        return reducer.foldMap(this);
    }

    @Override
    public <R> R foldMap(final Function<? super U, ? extends R> mapper, final Monoid<R> reducer) {

        return Reducer.fromMonoid(reducer,(U a)-> (R)mapper.apply(a))
                      .foldMap(this);
    }

    @Override
    public U reduce(final Monoid<U> reducer) {
        return reduce(reducer.zero(),reducer);
    }


    @Override
    public Seq<U> reduce(final Iterable<? extends Monoid<U>> reducers) {
        return Streams.reduce(this, reducers);
    }

    @Override
    public U foldRight(final U identity, final BinaryOperator<U> accumulator) {
        return reverse().foldLeft(identity, accumulator);
    }

    @Override
    public Optional<U> min(final Comparator<? super U> comparator) {
        return Streams.min(this, comparator);
    }

    @Override
    public Optional<U> max(final Comparator<? super U> comparator) {
        return Streams.max(this, comparator);
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

        return Streams.xMatch(this, num, c);
    }

    @Override
    public boolean noneMatch(final Predicate<? super U> c) {
        return !anyMatch(c);
    }

    @Override
    public final String join() {
        return Streams.join(this);
    }

    @Override
    public final String join(final String sep) {
        return Streams.join(this, sep);
    }

    @Override
    public String join(final String sep, final String start, final String end) {
        return Streams.join(this, sep, start, end);
    }


}
