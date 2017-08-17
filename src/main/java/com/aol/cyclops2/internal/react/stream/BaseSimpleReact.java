package com.aol.cyclops2.internal.react.stream;

import java.util.Collection;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.aol.cyclops2.types.futurestream.BaseSimpleReactStream;


import lombok.Getter;

public abstract class BaseSimpleReact implements ReactBuilder {

    @Getter
    private final Executor queueService;

    protected abstract Executor getExecutor();


    protected abstract boolean isAsync();

    public abstract <U> BaseSimpleReactStream<U> construct(Stream s);

    protected BaseSimpleReact() {
        queueService = null;
    }

    protected BaseSimpleReact(final Executor queueService) {
        this.queueService = queueService;
    }

    public BaseSimpleReactStream<Integer> range(final int startInclusive, final int endExclusive) {
        return from(IntStream.range(startInclusive, endExclusive));
    }

    /**
     * Start a reactiveBuffer flow from a JDK Iterator
     * 
     * @param iterator SimpleReact will iterate over this iterator concurrently to skip the reactiveBuffer dataflow
     * @return Next stage in the reactiveBuffer flow
     */
    @SuppressWarnings("unchecked")
    public <U> BaseSimpleReactStream<U> from(final Iterator<U> iterator) {
        return from(StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED), false));

    }

    /**
     * Start a reactiveBuffer flow from a Collection using an Iterator
     * 
     * @param collection - Collection SimpleReact will iterate over at the skip of the flow
     *
     * @return Next stage in the reactiveBuffer flow
     */
    @SuppressWarnings("unchecked")
    public <R> BaseSimpleReactStream<R> from(final Collection<R> collection) {
        return from(collection.stream());
    }

    /**
     * Start a reactiveBuffer flow from a JDK Iterator
     * 
     * @param iter SimpleReact will iterate over this iterator concurrently to skip the reactiveBuffer dataflow
     * @return Next stage in the reactiveBuffer flow
     */
    @SuppressWarnings("unchecked")
    public <U> BaseSimpleReactStream<U> fromIterable(final Iterable<U> iter) {
        return this.from(StreamSupport.stream(Spliterators.spliteratorUnknownSize(iter.iterator(), Spliterator.ORDERED), false));

    }

    /**
     * Start a reactiveBuffer dataflow from a reactiveStream of CompletableFutures.
     * 
     * @param stream of CompletableFutures that will be used to drive the reactiveBuffer dataflow
     * @return Next stage in the reactiveBuffer flow
     */
    public <U> BaseSimpleReactStream<U> fromStream(final Stream<CompletableFuture<U>> stream) {

        final Stream s = stream;
        return construct(s);
    }

    /**
     * Start a reactiveBuffer dataflow from a reactiveStream.
     * 
     * @param stream that will be used to drive the reactiveBuffer dataflow
     * @return Next stage in the reactiveBuffer flow
     */
    public <U> BaseSimpleReactStream<U> from(final Stream<U> stream) {

        final Stream s = stream.map(it -> CompletableFuture.completedFuture(it));
        return construct(s);
    }

    /**
     * Start a reactiveBuffer dataflow from a reactiveStream.
     * 
     * @param stream that will be used to drive the reactiveBuffer dataflow
     * @return Next stage in the reactiveBuffer flow
     */
    public <U> BaseSimpleReactStream<U> from(final IntStream stream) {

        return (BaseSimpleReactStream<U>) from(stream.boxed());

    }

    /**
     * Start a reactiveBuffer dataflow from a reactiveStream.
     * 
     * @param stream that will be used to drive the reactiveBuffer dataflow
     * @return Next stage in the reactiveBuffer flow
     */
    public <U> BaseSimpleReactStream<U> from(final DoubleStream stream) {

        return (BaseSimpleReactStream<U>) from(stream.boxed());

    }

    /**
     * Start a reactiveBuffer dataflow from a reactiveStream.
     * 
     * @param stream that will be used to drive the reactiveBuffer dataflow
     * @return Next stage in the reactiveBuffer flow
     */
    public <U> BaseSimpleReactStream<U> from(final LongStream stream) {

        return (BaseSimpleReactStream<U>) from(stream.boxed());

    }

    public <U> BaseSimpleReactStream<U> of(final U... array) {
        return from(Stream.of(array));
    }

    public <U> BaseSimpleReactStream<U> from(final CompletableFuture<U> cf) {
        return this.construct(Stream.of(cf));
    }

    public <U> BaseSimpleReactStream<U> from(final CompletableFuture<U>... cf) {
        return this.construct(Stream.of(cf));
    }

    /**
     * 
     * Start a reactiveBuffer dataflow with a list of one-off-suppliers
     * 
     * @param actions
     *            List of Suppliers to provide data (and thus events) that
     *            downstream jobs will react too
     * @return Next stage in the reactiveBuffer flow
     */
    @SuppressWarnings("unchecked")
    public <U> BaseSimpleReactStream<U> react(final Collection<Supplier<U>> actions) {

        return react(actions.toArray(new Supplier[] {}));
    }

    /**
     * 
     * Start a reactiveBuffer dataflow with a list of one-off-suppliers
     * 
     * @param actions
     *           LazyList of Suppliers to provide data (and thus events) that
     *            downstream jobs will react too
     * @return Next stage in the reactiveBuffer flow
     */
    @SuppressWarnings("unchecked")
    public <U> BaseSimpleReactStream<U> react(final Stream<Supplier<U>> actions) {

        return construct(actions.map(next -> CompletableFuture.supplyAsync(next, getExecutor())));

    }

    /**
     * 
     * Start a reactiveBuffer dataflow with a list of one-off-suppliers
     * 
     * @param actions
     *           Iterator over Suppliers to provide data (and thus events) that
     *            downstream jobs will react too
     * @return Next stage in the reactiveBuffer flow
     */
    @SuppressWarnings("unchecked")
    public <U> BaseSimpleReactStream<U> react(final Iterator<Supplier<U>> actions) {

        return construct(StreamSupport.stream(Spliterators.spliteratorUnknownSize(actions, Spliterator.ORDERED), false)
                                      .map(next -> CompletableFuture.supplyAsync(next, getExecutor())));

    }

    /**
     * 
     * Start a reactiveBuffer dataflow with a list of one-off-suppliers
     * 
     * @param actions
     *           LazyList of Suppliers to provide data (and thus events) that
     *            downstream jobs will react too
     * @return Next stage in the reactiveBuffer flow
     */
    @SuppressWarnings("unchecked")
    public <U> BaseSimpleReactStream<U> reactIterable(final Iterable<Supplier<U>> actions) {

        return construct(StreamSupport.stream(Spliterators.spliteratorUnknownSize(actions.iterator(), Spliterator.ORDERED), false)
                                      .map(next -> CompletableFuture.supplyAsync(next, getExecutor())));

    }

    /**
     * 
     * Start a reactiveBuffer dataflow with an array of one-off-suppliers
     * 
     * @param actions Array of Suppliers to provide data (and thus events) that
     *            downstream jobs will react too
     * @return Next stage in the reactiveBuffer flow
     */
    public <U> BaseSimpleReactStream<U> react(final Supplier<U>... actions) {

        return this.<U> reactI(actions);

    }

    /**
     * This internal method has been left protected, so it can be mocked / stubbed as some of the entry points are final
     * 
     */
    @SuppressWarnings("unchecked")
    protected <U> BaseSimpleReactStream<U> reactI(final Supplier<U>... actions) {
        return construct(Stream.of(actions)
                               .map(next -> CompletableFuture.supplyAsync(next, getExecutor())));
    }

}