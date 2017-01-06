package com.aol.cyclops2.internal.react;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import cyclops.Streams;
import cyclops.stream.FutureStream;
import org.jooq.lambda.Collectable;
import org.jooq.lambda.Seq;

import cyclops.function.Monoid;
import cyclops.function.Reducer;
import cyclops.async.LazyReact;
import cyclops.stream.ReactiveSeq;
import cyclops.async.QueueFactories;
import cyclops.async.QueueFactory;
import cyclops.collections.ListX;
import com.aol.cyclops2.internal.react.stream.LazyStreamWrapper;
import com.aol.cyclops2.react.async.subscription.Continueable;
import com.aol.cyclops2.react.async.subscription.Subscription;
import com.aol.cyclops2.react.collectors.lazy.BatchingCollector;
import com.aol.cyclops2.react.collectors.lazy.LazyResultConsumer;
import com.aol.cyclops2.react.collectors.lazy.MaxActive;
import com.aol.cyclops2.react.threads.ReactPool;
import com.aol.cyclops2.types.stream.HeadAndTail;
import com.aol.cyclops2.types.stream.HotStream;
import com.aol.cyclops2.types.stream.PausableHotStream;
import com.nurkiewicz.asyncretry.RetryExecutor;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Wither;
import lombok.extern.slf4j.Slf4j;


@Getter
@Slf4j
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class FutureStreamImpl<U> implements FutureStream<U> {

    @Wither
    private final Optional<Consumer<Throwable>> errorHandler;
    private final LazyStreamWrapper<U> lastActive;

    @Wither
    private final Supplier<LazyResultConsumer<U>> lazyCollector;
    @Wither
    private final QueueFactory<U> queueFactory;
    @Wither
    private final LazyReact simpleReact;
    @Wither
    private final Continueable subscription;
    private final static ReactPool<LazyReact> pool = ReactPool.elasticPool(() -> new LazyReact(
                                                                                               Executors.newSingleThreadExecutor()));
    @Wither
    private final ConsumerHolder error;
    @Wither
    private final MaxActive maxActive;

    @AllArgsConstructor
    static class ConsumerHolder {
        volatile Consumer<Throwable> forward;
    }

    public FutureStreamImpl(final LazyReact lazyReact, final Stream<U> stream) {

        this.simpleReact = lazyReact;

        this.lastActive = new LazyStreamWrapper<>(
                                                  stream, lazyReact);
        this.error = new ConsumerHolder(
                                        a -> {
                                        });
        this.errorHandler = Optional.of((e) -> {
            error.forward.accept(e);
            log.error(e.getMessage(), e);
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
    public RetryExecutor getRetrier() {
        return this.simpleReact.getRetrier();
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
    public FutureStream<U> withRetrier(final RetryExecutor retry) {
        return this.withSimpleReact(simpleReact.withRetrier(retry));
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
        //also need to mark cancelled =true and check during collection
    }

    @Override
    public HotStream<U> schedule(final String cron, final ScheduledExecutorService ex) {
        return ReactiveSeq.<U> fromStream(this.toStream())
                          .schedule(cron, ex);
    }

    @Override
    public HotStream<U> scheduleFixedDelay(final long delay, final ScheduledExecutorService ex) {
        return ReactiveSeq.<U> fromStream(this.toStream())
                          .scheduleFixedDelay(delay, ex);
    }

    @Override
    public HotStream<U> scheduleFixedRate(final long rate, final ScheduledExecutorService ex) {
        return ReactiveSeq.<U> fromStream(this.toStream())
                          .scheduleFixedRate(rate, ex);
    }

    @Override
    public <T> FutureStream<T> unitIterator(final Iterator<T> it) {
        return simpleReact.from(it);
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
    public HotStream<U> hotStream(final Executor e) {
        return Streams.hotStream(this, e);
    }

    @Override
    public HotStream<U> primedHotStream(final Executor e) {
        return Streams.primedHotStream(this, e);
    }

    @Override
    public PausableHotStream<U> pausableHotStream(final Executor e) {
        return Streams.pausableHotStream(this, e);
    }

    @Override
    public PausableHotStream<U> primedPausableHotStream(final Executor e) {
        return Streams.primedPausableHotStream(this, e);
    }

   // @Override
    public String format() {
        return Seq.seq((Stream<U>)this)
                  .format();
    }

    @Override
    public Collectable<U> collectable() {
        //in order for tasks to be executed concurrently we need to make sure that collect is
        //ultimately called via LazyStream#collect. Passing 'this' directly into Seq results in 'this' being returned
        //Seq implements the collection extensions on SeqImpl, so we need to construct a SeqImpl with this as the Stream.
        return Seq.seq(new DelegateStream<U>(
                                             this));
    }

    @Override
    public U foldRight(final Monoid<U> reducer) {
        return reducer.reduce(this);
    }

    @Override
    public <T> T foldRightMapToType(final Reducer<T> reducer) {
        return reducer.mapReduce(reverse());

    }

    @Override
    public <R> R mapReduce(final Reducer<R> reducer) {
        return reducer.mapReduce(this);
    }

    @Override
    public <R> R mapReduce(final Function<? super U, ? extends R> mapper, final Monoid<R> reducer) {
        return Reducer.fromMonoid(reducer, mapper)
                      .mapReduce(this);
    }

    @Override
    public U reduce(final Monoid<U> reducer) {
        return reducer.reduce(this);
    }

    @Override
    public ListX<U> reduce(final Stream<? extends Monoid<U>> reducers) {
        return Streams.reduce(this, reducers);
    }

    @Override
    public ListX<U> reduce(final Iterable<? extends Monoid<U>> reducers) {
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

    /* (non-Javadoc)
     * @see cyclops2.stream.ReactiveSeq#headAndTail()
     */
    @Override
    public HeadAndTail<U> headAndTail() {
        return Streams.headAndTail(this);
    }

}
