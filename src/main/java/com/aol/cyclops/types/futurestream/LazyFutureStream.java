package com.aol.cyclops.types.futurestream;

import static java.util.Spliterator.ORDERED;
import static java.util.Spliterators.spliteratorUnknownSize;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
import java.util.function.UnaryOperator;
import java.util.stream.BaseStream;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.jooq.lambda.Seq;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.jooq.lambda.tuple.Tuple4;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import com.aol.cyclops.Matchables;
import com.aol.cyclops.Monoid;
import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.LazyReact;
import com.aol.cyclops.control.Matchable.CheckValue1;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.control.SimpleReact;
import com.aol.cyclops.control.StreamUtils;
import com.aol.cyclops.control.Streamable;
import com.aol.cyclops.control.Trampoline;
import com.aol.cyclops.data.async.Queue;
import com.aol.cyclops.data.async.Queue.ClosedQueueException;
import com.aol.cyclops.data.async.Queue.QueueTimeoutException;
import com.aol.cyclops.data.async.QueueFactories;
import com.aol.cyclops.data.async.QueueFactory;
import com.aol.cyclops.data.collections.extensions.CollectionX;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.internal.react.LazyFutureStreamImpl;
import com.aol.cyclops.internal.react.async.future.FastFuture;
import com.aol.cyclops.internal.react.stream.CloseableIterator;
import com.aol.cyclops.internal.react.stream.LazyStreamWrapper;
import com.aol.cyclops.internal.react.stream.traits.future.operators.LazyFutureStreamUtils;
import com.aol.cyclops.internal.react.stream.traits.future.operators.OperationsOnFuturesImpl;
import com.aol.cyclops.internal.stream.LazyFutureStreamFutureOpterationsImpl;
import com.aol.cyclops.react.RetryBuilder;
import com.aol.cyclops.react.SimpleReactFailedStageException;
import com.aol.cyclops.react.ThreadPools;
import com.aol.cyclops.react.async.subscription.Continueable;
import com.aol.cyclops.react.collectors.lazy.LazyResultConsumer;
import com.aol.cyclops.react.collectors.lazy.MaxActive;
import com.aol.cyclops.types.Filterable;
import com.aol.cyclops.types.Functor;
import com.aol.cyclops.types.IterableFoldable;
import com.aol.cyclops.types.anyM.AnyMSeq;
import com.aol.cyclops.types.applicative.zipping.ApplyingZippingApplicativeBuilder;
import com.aol.cyclops.types.applicative.zipping.ZippingApplicativable;
import com.aol.cyclops.types.stream.HotStream;
import com.aol.cyclops.types.stream.future.FutureOperations;
import com.aol.cyclops.types.stream.reactive.FutureStreamSynchronousPublisher;
import com.nurkiewicz.asyncretry.AsyncRetryExecutor;
import com.nurkiewicz.asyncretry.RetryExecutor;

import lombok.val;

/**
 * Lazy Stream Factory methods
 *
 * @author johnmcclean
 *
 */

public interface LazyFutureStream<U> extends Functor<U>, Filterable<U>, LazySimpleReactStream<U>, LazyStream<U>, ReactiveSeq<U>, LazyToQueue<U>,
        ConfigurableStream<U, FastFuture<U>>, FutureStreamSynchronousPublisher<U> {

    @Override
    default LazyFutureStream<U> filterNot(Predicate<? super U> fn) {

        return (LazyFutureStream<U>) ReactiveSeq.super.filterNot(fn);
    }

    @Override
    default LazyFutureStream<U> notNull() {

        return (LazyFutureStream<U>) ReactiveSeq.super.notNull();
    }

    @Override
    default <R> LazyFutureStream<R> trampoline(Function<? super U, ? extends Trampoline<? extends R>> mapper) {

        return (LazyFutureStream<R>) ReactiveSeq.super.trampoline(mapper);
    }

    default <R> R foldRight(R identity, BiFunction<? super U, ? super R, ? extends R> accumulator) {
        return ReactiveSeq.super.foldRight(identity, accumulator);
    }

    @Override
    default <R> ApplyingZippingApplicativeBuilder<U, R, ZippingApplicativable<R>> applicatives() {
        Streamable<U> streamable = toStreamable();
        return new ApplyingZippingApplicativeBuilder<U, R, ZippingApplicativable<R>>(
                                                                                     streamable, streamable);
    }

    @Override
    default <R> ZippingApplicativable<R> ap1(Function<? super U, ? extends R> fn) {
        val dup = this.duplicateSequence();
        Streamable<U> streamable = dup.v1.toStreamable();
        return new ApplyingZippingApplicativeBuilder<U, R, ZippingApplicativable<R>>(
                                                                                     streamable, streamable).applicative(fn)
                                                                                                            .ap(dup.v2);

    }

    /**
     * <pre>
     * {@code
     *  LazyFutureStream.of(1,2,3,4,5)
     * 				 		.elapsed()
     * 				 	.forEach(System.out::println);
     * }
     * </pre>
     *
     * @return LazyFutureStream that adds the time between elements in millis to
     *         each element
     */
    default LazyFutureStream<Tuple2<U, Long>> elapsed() {
        return fromStream(ReactiveSeq.fromStream(stream())
                                     .elapsed());
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.ReactiveSeq#combine(java.util.function.BiPredicate, java.util.function.BinaryOperator)
     */
    default LazyFutureStream<U> combine(BiPredicate<? super U, ? super U> predicate, BinaryOperator<U> op) {
        return fromStream(StreamUtils.combine(this, predicate, op));
    }

    /**
     * If this SequenceM is empty replace it with a another Stream
     *
     * <pre>
     * {@code
     * assertThat(LazyFutureStream.of(4,5,6)
     * 							.onEmptySwitch(()->ReactiveSeq.of(1,2,3))
     * 							.toList(),
     * 							equalTo(Arrays.asList(4,5,6)));
     * }
     * </pre>
     *
     * @param switchTo
     *            Supplier that will generate the alternative Stream
     * @return SequenceM that will switch to an alternative Stream if empty
     */
    default LazyFutureStream<U> onEmptySwitch(Supplier<? extends Stream<U>> switchTo) {
        return fromStream(ReactiveSeq.fromStream(stream())
                                     .onEmptySwitch(switchTo));
    }

    /**
     * Perform a three level nested internal iteration over this Stream and the
     * supplied streams
     *
     * <pre>
     * {@code
     * LazyFutureStream.of(1,2)
     * 						.forEach2(a->IntStream.range(10,13),
     * 						.forEach2(a->b->Stream.of(""+(a+b),"hello world"),
     * 									a->b->c->c+":"a+":"+b);
     *
     *
     *  //LFS[11:1:2,hello world:1:2,14:1:4,hello world:1:4,12:1:2,hello world:1:2,15:1:5,hello world:1:5]
     * }
     * </pre>
     *
     * @param stream1
     *            Nested Stream to iterate over
     * @param stream2
     *            Nested Stream to iterate over
     * @param yieldingFunction
     *            Function with pointers to the current element from both
     *            Streams that generates the new elements
     * @return LazyFutureStream with elements generated via nested iteration
     */
    default <R1, R2, R> LazyFutureStream<R> forEach3(Function<? super U, ? extends BaseStream<R1, ?>> stream1,
            Function<? super U, Function<? super R1, ? extends BaseStream<R2, ?>>> stream2,
            Function<? super U, Function<? super R1, Function<? super R2, ? extends R>>> yieldingFunction) {
        return fromStream(ReactiveSeq.fromStream(stream())
                                     .forEach3(stream1, stream2, yieldingFunction));

    }

    /**
     * Perform a three level nested internal iteration over this Stream and the
     * supplied streams
     *<pre>
     * {@code
     * LazyFutureStream.of(1,2,3)
                        .forEach3(a->IntStream.range(10,13),
                              a->b->Stream.of(""+(a+b),"hello world"),
                                 a->b->c-> c!=3,
                                    a->b->c->c+":"a+":"+b);
    
     *
     *  //LazyFutureStream[11:1:2,hello world:1:2,14:1:4,hello world:1:4,12:1:2,hello world:1:2,15:1:5,hello world:1:5]
     * }
     * </pre>
     *
     * @param stream1
     *            Nested Stream to iterate over
     * @param stream2
     *            Nested Stream to iterate over
     * @param filterFunction
     *            Filter to apply over elements before passing non-filtered
     *            values to the yielding function
     * @param yieldingFunction
     *            Function with pointers to the current element from both
     *            Streams that generates the new elements
     * @return LazyFutureStream with elements generated via nested iteration
     */
    default <R1, R2, R> LazyFutureStream<R> forEach3(Function<? super U, ? extends BaseStream<R1, ?>> stream1,
            Function<? super U, Function<? super R1, ? extends BaseStream<R2, ?>>> stream2,
            Function<? super U, Function<? super R1, Function<? super R2, Boolean>>> filterFunction,
            Function<? super U, Function<? super R1, Function<? super R2, ? extends R>>> yieldingFunction) {

        return fromStream(ReactiveSeq.fromStream(stream())
                                     .forEach3(stream1, stream2, filterFunction, yieldingFunction));

    }

    /**
     * Perform a two level nested internal iteration over this Stream and the
     * supplied stream
     *
     * <pre>
     * {@code
     * LazyFutureStream.of(1,2,3)
     * 						.forEach2(a->IntStream.range(10,13),
     * 									a->b->a+b);
     *
     *
     *  //LFS[11,14,12,15,13,16]
     * }
     * </pre>
     *
     *
     * @param stream1
     *            Nested Stream to iterate over
     * @param yieldingFunction
     *            Function with pointers to the current element from both
     *            Streams that generates the new elements
     * @return LazyFutureStream with elements generated via nested iteration
     */
    default <R1, R> LazyFutureStream<R> forEach2(Function<? super U, ? extends BaseStream<R1, ?>> stream1,
            Function<? super U, Function<? super R1, ? extends R>> yieldingFunction) {
        return fromStream(ReactiveSeq.fromStream(stream())
                                     .forEach2(stream1, yieldingFunction));

    }

    /**
     * Perform a two level nested internal iteration over this Stream and the
     * supplied stream
     *
     * <pre>
     * {@code
     * LazyFutureStream.of(1,2,3)
     * 						.forEach2(a->IntStream.range(10,13),
     * 						            a->b-> a<3 && b>10,
     * 									a->b->a+b);
     *
     *
     *  //LFS[14,15]
     * }
     * </pre>
     *
     * @param stream1
     *            Nested Stream to iterate over
     * @param filterFunction
     *            Filter to apply over elements before passing non-filtered
     *            values to the yielding function
     * @param yieldingFunction
     *            Function with pointers to the current element from both
     *            Streams that generates the new elements
     * @return
     */
    default <R1, R> LazyFutureStream<R> forEach2(Function<? super U, ? extends BaseStream<R1, ?>> stream1,
            Function<? super U, Function<? super R1, Boolean>> filterFunction,
            Function<? super U, Function<? super R1, ? extends R>> yieldingFunction) {
        return fromStream(ReactiveSeq.fromStream(stream())
                                     .forEach2(stream1, filterFunction, yieldingFunction));

    }

    /**
     * Transform the elements of this Stream with a Pattern Matching case and default value
     *
     * <pre>
     * {@code
     * List<String> result = LazyFutureStream.of(1,2,3,4)
                                              .patternMatch(
                                                        c->c.hasValuesWhere( (Integer i)->i%2==0 ).then(i->"even")
                                                      )
    
     * }
     * // LazyFutureStream["odd","even","odd","even"]
     * </pre>
     *
     *
     * @param defaultValue Value if supplied case doesn't match
     * @param case1 Function to generate a case (or chain of cases as a single case)
     * @return LazyFutureStream where elements are transformed by pattern matching
     */
    @Override
    default <R> LazyFutureStream<R> patternMatch(Function<CheckValue1<U, R>, CheckValue1<U, R>> case1, Supplier<? extends R> otherwise) {

        return map(u -> Matchables.supplier(() -> u)
                                  .matches(case1, otherwise)
                                  .get());
    }

    /**
     * Remove all occurances of the specified element from the SequenceM
     * <pre>
     * {@code
     * 	LazyFutureStream.of(1,2,3,4,5,1,2,3).remove(1)
     *
     *  //LazyFutureStream[2,3,4,5,2,3]
     * }
     * </pre>
     *
     * @param t element to remove
     * @return Filtered Stream
     */
    default LazyFutureStream<U> remove(U t) {

        return (LazyFutureStream<U>) (ReactiveSeq.super.remove(t));
    }

    /**
     * Return a Stream with elements before the provided start index removed, and elements after the provided
     * end index removed
     *
     * <pre>
     * {@code
     *   LazyFutureStream.of(1,2,3,4,5,6).subStream(1,3);
     *
     *
     *   //LazyFutureStream[2,3]
     * }
     * </pre>
     *
     * @param start index inclusive
     * @param end index exclusive
     * @return LqzyFutureStream between supplied indexes of original Sequence
     */
    default LazyFutureStream<U> subStream(int start, int end) {
        return this.fromStream(ReactiveSeq.fromStream(toQueue().stream(getSubscription()))
                                          .subStream(start, end));
    }

    /**
     * Generate the permutations based on values in the LazyFutureStream
     * Makes use of Streamable to store intermediate stages in a collection
     *
     *
     * @return Permutations from this LazyFutureStream
     */
    default LazyFutureStream<ReactiveSeq<U>> permutations() {
        return this.fromStream(ReactiveSeq.fromStream(toQueue().stream(getSubscription()))
                                          .permutations());

    }

    /**
     * <pre>
     * {@code
     *   LazyFutureStream.of(1,2,3).combinations()
     *
     *   //LazyFutureStream[SequenceM[],SequenceM[1],SequenceM[2],SequenceM[3].SequenceM[1,2],SequenceM[1,3],SequenceM[2,3]
     *   			,SequenceM[1,2,3]]
     * }
     * </pre>
     *
     *
     * @return All combinations of the elements in this stream
     */
    default LazyFutureStream<ReactiveSeq<U>> combinations() {
        return this.fromStream(ReactiveSeq.fromStream(toQueue().stream(getSubscription()))
                                          .combinations());

    }

    /**
     * LazyFutureStream operators act on the results of the previous stage by default. That means limiting,
     * skipping, zipping all occur once results being to stream in from active Future tasks. This
     * operator allows access to a set of operators that behave differently. Limiting, skipping and zipping all occur on
     * the underlying Stream of Futures.
     *
     * Operating on results
     * <pre>
     * {@code
     *     new LazyReact().react(()->slow(),()->fast())
     *                    .zipWithIndex();
     *
     *     //[fast,0l],[slow,1l]
     * }</pre>
     * The first result will be fast and will have index 0 (the result index)
     *
     * Operating on futures
     * <pre>
     * {@code
     *     new LazyReact().react(()->slow(),()->fast())
     *                    .actOnFutures()
     *                    .zipWithIndex();
     *
     *     //[fast,1l],[slow,0l]
     * }</pre>
     * The first result will still be fast, but the index will now be the start index 1
     *
     * @return Access a set of operators that act on the underlying futures in this
     * Stream.
     *
     */
    default OperationsOnFutures<U> actOnFutures() {
        return new OperationsOnFuturesImpl<>(
                                             this);
    }

    /*
     * @return an iterator over this Stream
     * @see com.aol.cyclops.react.stream.traits.SimpleReactStream#iterator()
     */
    default CloseableIterator<U> iterator() {
        return (CloseableIterator) LazySimpleReactStream.super.iterator();
    }

    /*
     * @return Queue membership subscription for this Stream
     * @see com.aol.cyclops.react.stream.traits.LazySimpleReactStream#getSubscription()
     */
    Continueable getSubscription();

    /*
     * @see com.aol.cyclops.react.stream.traits.LazySimpleReactStream#withLastActive(com.aol.simple.react.stream.LazyStreamWrapper)
     */
    <R> LazyFutureStream<R> withLastActive(LazyStreamWrapper<R> streamWrapper);

    /*
     *	@return Stream Builder for this Stream
     * @see com.aol.cyclops.react.stream.traits.LazySimpleReactStream#getSimpleReact()
     */
    LazyReact getSimpleReact();

    /*
     * Subscribe to this Stream
     * If this Stream is executing in async mode it will operate as an Async Publisher, otherwise it will operate as a Synchronous publisher.
     * async() or sync() can be used just prior to subscribe.
     *
     * <pre>
     * {@code
     *  FutureStreamSubscriber<Integer> sub = new FutureStreamSubscriber();
        LazyFutureStream.of(1,2,3).subscribe(sub);
        sub.getStream().forEach(System.out::println);
     * }
     * </pre>
     *	@param s Subscriber
     * @see org.reactivestreams.Publisher#subscribe(org.reactivestreams.Subscriber)
     */
    default void subscribe(Subscriber<? super U> s) {
        /**  if(isAsync())
            FutureStreamAsyncPublisher.super.subscribe(s);
        else**/
        FutureStreamSynchronousPublisher.super.subscribe(s);
    }

    /**
     * @return an Iterator that chunks all completed elements from this stream since last it.next() call into a collection
     */
    default Iterator<Collection<U>> chunkLastReadIterator() {

        Queue.QueueReader reader = new Queue.QueueReader(
                                                         this.withQueueFactory(QueueFactories.unboundedQueue())
                                                             .toQueue(q -> q.withTimeout(100)
                                                                            .withTimeUnit(TimeUnit.MICROSECONDS)),
                                                         null);
        class Chunker implements Iterator<Collection<U>> {
            volatile boolean open = true;

            @Override
            public boolean hasNext() {

                return open == true && reader.isOpen();
            }

            @Override
            public Collection<U> next() {

                while (hasNext()) {
                    try {
                        return reader.drainToOrBlock();
                    } catch (ClosedQueueException e) {
                        open = false;
                        return new ArrayList<>();
                    } catch (QueueTimeoutException e) {
                        LockSupport.parkNanos(0l);
                    }
                }
                return new ArrayList<>();

            }
        }
        return new Chunker();
    }

    /**
     * @return a Stream that batches all completed elements from this stream since last read attempt into a collection
     */
    default LazyFutureStream<Collection<U>> chunkSinceLastRead() {
        Queue queue = this.withQueueFactory(QueueFactories.unboundedQueue())
                          .toQueue();
        Queue.QueueReader reader = new Queue.QueueReader(
                                                         queue, null);
        class Chunker implements Iterator<Collection<U>> {

            @Override
            public boolean hasNext() {

                return reader.isOpen();
            }

            @Override
            public Collection<U> next() {
                return reader.drainToOrBlock();

            }
        }
        Chunker chunker = new Chunker();
        Function<Supplier<U>, Supplier<Collection<U>>> fn = s -> {
            return () -> {

                try {
                    return chunker.next();
                } catch (ClosedQueueException e) {

                    throw new ClosedQueueException();
                }

            };
        };
        return fromStream(queue.streamBatchNoTimeout(getSubscription(), fn));

    }

    //terminal operations
    /*
     * (non-Javadoc)
     *
     * @see java.util.stream.Stream#count()
     */
    @Override
    default long count() {
        //performance optimisation count the underlying futures
        return getLastActive().stream()
                              .count();
    }

    /*
     * Change task executor for the next stage of the Stream
     *
     * <pre>
     * {@code
     *  LazyFutureStream.of(1,2,3,4)
     *  					.map(this::loadFromDb)
     *  					.withTaskExecutor(parallelBuilder().getExecutor())
     *  					.map(this::processOnDifferentExecutor)
     *  					.toList();
     * }
     * </pre>
     *
     *	@param e New executor to use
     *	@return Stream ready for next stage definition
     * @see com.aol.cyclops.react.stream.traits.ConfigurableStream#withTaskExecutor(java.util.concurrent.Executor)
     */
    LazyFutureStream<U> withTaskExecutor(Executor e);

    /*
     * Change the Retry Executor used in this stream for subsequent stages
     * <pre>
     * {@code
     * List<String> result = new LazyReact().react(() -> 1)
                .withRetrier(executor)
                .(e -> error = e)
                .retry(serviceMock).block();
     *
     * }
     * </pre>
     *
     *
     *	@param retry Retry executor to use
     *	@return Stream
     * @see com.aol.cyclops.react.stream.traits.ConfigurableStream#withRetrier(com.nurkiewicz.asyncretry.RetryExecutor)
     */
    LazyFutureStream<U> withRetrier(RetryExecutor retry);

    LazyFutureStream<U> withLazyCollector(Supplier<LazyResultConsumer<U>> lazy);

    /*
     * Change the QueueFactory type for the next phase of the Stream.
     * Default for EagerFutureStream is an unbounded blocking queue, but other types
     * will work fine for a subset of the tasks (e.g. an unbonunded non-blocking queue).
     *
     * <pre>
     * {@code
     * List<Collection<String>> collected = LazyFutureStream
                .react(data)
                .withQueueFactory(QueueFactories.boundedQueue(1))
                .onePer(1, TimeUnit.SECONDS)
                .batchByTime(10, TimeUnit.SECONDS)
                .limit(15)
                .toList();
     * }
     * </pre>
     *	@param queue Queue factory to use for subsequent stages
     *	@return Stream
     * @see com.aol.cyclops.react.stream.traits.ConfigurableStream#withQueueFactory(com.aol.simple.react.async.QueueFactory)
     * @see com.aol.cyclops.react.stream.traits.LazyFutureStream#unboundedWaitFree()
     * @see com.aol.cyclops.react.stream.traits.LazyFutureStream#boundedWaitFree(int size)
     */
    LazyFutureStream<U> withQueueFactory(QueueFactory<U> queue);

    /*
     *	@param sub Queue Subscription to use for this Stream
     * @see com.aol.cyclops.react.stream.traits.LazySimpleReactStream#withSubscription(com.aol.simple.react.async.subscription.Continueable)
     */
    LazyFutureStream<U> withSubscription(Continueable sub);

    /*
     * Convert this stream into an async / sync stream
     *
     *	@param async true if aysnc stream
     *	@return
     * @see com.aol.cyclops.react.stream.traits.ConfigurableStream#withAsync(boolean)
     */
    LazyFutureStream<U> withAsync(boolean async);

    /*
     * @see com.aol.cyclops.react.stream.traits.LazyStream#forEach(java.util.function.Consumer)
     */
    default void forEach(Consumer<? super U> c) {
        LazyStream.super.forEach(c);

    }

    /*  Transfer data in this Stream asyncrhonously to a Queue
     * <pre>
     * {@code
     *  Queue<String> q = new LazyReact().reactInfinitely(() -> "100")
                       .limit(100)
                        .withQueueFactory(QueueFactories.boundedQueue(10))
                        .toQueue();
         q.stream().collect(Collectors.toList())
                   .size()
    
        //100
     * }</pre>
     *	@return Queue
     * @see com.aol.cyclops.react.stream.traits.ToQueue#toQueue()
     */
    default Queue<U> toQueue() {

        return LazyToQueue.super.toQueue();
    }

    default <T> T reduce(T identity, BiFunction<T, ? super U, T> accumulator) {
        return LazyStream.super.reduce(identity, accumulator, (a, b) -> a);
    }

    /*
     * (non-Javadoc)
     *
     * @see java.util.stream.Stream#reduce(java.lang.Object,
     * java.util.function.BinaryOperator)
     */
    @Override
    default U reduce(U identity, BinaryOperator<U> accumulator) {
        return LazyStream.super.reduce(identity, accumulator);
    }

    /*
     * @see com.aol.cyclops.react.stream.traits.LazyStream#reduce(java.lang.Object, java.util.function.BiFunction, java.util.function.BinaryOperator)
     */
    @Override
    default <T> T reduce(T identity, BiFunction<T, ? super U, T> accumulator, BinaryOperator<T> combiner) {
        return LazyStream.super.reduce(identity, accumulator, combiner);
    }

    /*
     * @see com.aol.cyclops.react.stream.traits.LazyStream#reduce(java.util.function.BinaryOperator)
     */
    @Override
    default Optional<U> reduce(BinaryOperator<U> accumulator) {
        return LazyStream.super.reduce(accumulator);
    }

    /*
     * @see com.aol.cyclops.react.stream.traits.LazyStream#collect(java.util.function.Supplier, java.util.function.BiConsumer, java.util.function.BiConsumer)
     */
    @Override
    default <R> R collect(Supplier<R> supplier, BiConsumer<R, ? super U> accumulator, BiConsumer<R, R> combiner) {
        return LazyStream.super.collect(supplier, accumulator, combiner);
    }

    /*
     * Execute subsequent stages on the completing thread (until async called)
     * 10X faster than async execution.
     * Use async for blocking IO or distributing work across threads or cores.
     * Switch to sync for non-blocking tasks when desired thread utlisation reached
     * <pre>
     * {@code
     *      new LazyReact().of(1,2,3)
                            .sync() //synchronous mode
                            .flatMapToCompletableFuture(i->CompletableFuture.completedFuture(i))
                            .block()
     *
     * }
     * </pre>
     *	@return Version of FutureStream that will use sync CompletableFuture methods
     * @see com.aol.cyclops.react.stream.traits.SimpleReactStream#sync()
     */
    default LazyFutureStream<U> sync() {
        return this.withAsync(false);
    }

    /*
     * Execute subsequent stages by submission to an Executor for async execution
     * 10X slower than sync execution.
     * Use async for blocking IO or distributing work across threads or cores.
     * Switch to sync for non-blocking tasks when desired thread utlisation reached
     * <pre>
     * {@code
     * LazyFutureStream stream = of(1,2,3,4).async()
     *                                      .map(this::doWorkOnSeparateThreads)
     *                                      .map(this::resubmitTaskForDistribution)
     *                                      .forEach(System.out::println);
     *
     * }</pre>
     *
     *	@return Version of FutureStream that will use async CompletableFuture methods
     * @see com.aol.cyclops.react.stream.traits.SimpleReactStream#async()
     */
    default LazyFutureStream<U> async() {
        return this.withAsync(true);
    }

    /**
     * This is the default setting, internal queues are backed by a ConcurrentLinkedQueue
     * This operator will return the next stage to using this Queue type if it has been changed
     *
     * @return LazyFutureStream backed by a ConcurrentLinkedQueue
     */
    default LazyFutureStream<U> unboundedWaitFree() {
        return this.withQueueFactory(QueueFactories.unboundedNonBlockingQueue());
    }

    /**
     * Use an Agrona ManyToOneConcurrentArrayQueue for the next operations (wait-free, mechanical sympathy).
     * Note Queued data will be somewhat limited by configured concurrency level, but that flatMap operations
     * can increase the amount of data to be buffered significantly.
     *
     * <pre>
     * {@code
     *     LazyFutureStream.of(col)
     *                     .boundedWaitFree(110)
     *                     .flatMap(Collection::stream)
     *                     .toList();
     * }
     * </pre>
     *
     * @param size Buffer size
     * @return LazyFutureStream backed by an Agrona ManyToOneConcurrentArrayQueue
     */
    default LazyFutureStream<U> boundedWaitFree(int size) {
        return this.withQueueFactory(QueueFactories.boundedNonBlockingQueue(size));
    }

    /**
     * Configure the max active concurrent tasks. The last set value wins, this can't be set per stage.
     *
     * <pre>
     *    {@code
     *    	List<String> data = new LazyReact().react(urlFile)
     *    										.maxActive(100)
     *    										.flatMap(this::loadUrls)
     *    										.map(this::callUrls)
     *    										.block();
     *    }
     * </pre>
     *
     * @param concurrentTasks Maximum number of active task chains
     * @return LazyFutureStream with new limits set
     */
    public LazyFutureStream<U> maxActive(int concurrentTasks);

    /*
     * Equivalent functionally to map / then but always applied on the completing thread (from the previous stage)
     *
     * When autoOptimize functionality is enabled, thenSync is the default behaviour for then / map operations
     *
     * <pre>
     * {@code
     *  new LazyReact().withAutoOptimize(false)
     *                 .react(()->1,()->2,()->3)
     *                  .thenSync(it->it+100) //add 100
                        .toList();
     * }
     * //results in [100,200,300]
     * </pre>
     *
     * @see com.aol.cyclops.react.stream.traits.LazySimpleReactStream#thenSync(java.util.function.Function)
     */
    default <R> LazyFutureStream<R> thenSync(final Function<? super U, ? extends R> fn) {
        return (LazyFutureStream<R>) LazySimpleReactStream.super.thenSync(fn);
    }

    /*
     *  Equivalent functionally to peek but always applied on the completing thread (from the previous stage)
     *  When autoOptimize functionality is enabled, peekSync is the default behaviour for peek operations
     * <pre>
     * {@code
     * new LazyReact().withAutoOptimize(false)
     *                .of(1,2,3,4)
                      .map(this::performIO)
                      .peekSync(this::cpuBoundTaskNoResult)
                      .run();
     *
     * }</pre>
     * @see com.aol.cyclops.react.stream.traits.LazySimpleReactStream#peekSync(java.util.function.Consumer)
     */
    default LazyFutureStream<U> peekSync(final Consumer<? super U> consumer) {
        return (LazyFutureStream<U>) LazySimpleReactStream.super.peekSync(consumer);
    }

    /**
     * closes all open queues.
     */
    default void closeAll() {
        getSubscription().closeAll();
    }

    /**
     * Turns this LazyFutureStream into a HotStream, a connectable Stream, being executed on a thread on the
     * in it's current task executor, that is producing data
     * <pre>
     * {@code
     *  HotStream<Integer> ints = new LazyReact().range(0,Integer.MAX_VALUE)
                                                .hotStream()
        ints.connect().forEach(System.out::println);
     *  //print out all the ints
     *  //multiple consumers are possible, so other Streams can connect on different Threads
     *
     * }
     * </pre>
     * @return a Connectable HotStream
     */
    default HotStream<U> hotStream() {
        return StreamUtils.hotStream(this, this.getTaskExecutor());

    }

    /* 
     * @see com.aol.cyclops.control.ReactiveSeq#findFirst()
     */
    default Optional<U> findFirst() {
        List<U> results = this.run(Collectors.toList());
        if (results.size() == 0)
            return Optional.empty();
        return Optional.of(results.get(0));
    }

    /**
     * Convert between an Lazy and Eager SimpleReact Stream, can be used to take
     * advantages of each approach during a single Stream
     *
     * Allows callers to take advantage of functionality only available in
     * SimpleReactStreams such as allOf
     *
     * <pre>
     * {@code
     * LazyReact.parallelCommonBuilder()
     * 						.react(()->slow(),()->1,()->2)
     * 						.peek(System.out::println)
     * 						.convertToSimpleReact()
     * 						.allOf(list->list)
     * 						.block()
     * }
     * </pre>
     *
     * @return An SimpleReactStream from this LazyFutureStream, will use the
     *         same executors
     */
    default SimpleReactStream<U> convertToSimpleReact() {
        return new SimpleReact(
                               getTaskExecutor()).withRetrier(getRetrier())
                                                 .fromStream((Stream) getLastActive().injectFutures()
                                                                                     .map(f -> {
                                                                                         try {
                                                                                             return CompletableFuture.completedFuture(f.join());
                                                                                         } catch (Throwable t) {
                                                                                             return new CompletableFuture().completeExceptionally(t);
                                                                                         }
                                                                                     }));

    }

    /*
     * Apply a function to all items in the stream.
     * <pre>
     * {@code
     *  LazyReact.sequentialBuilder().react(()->1,()->2,()->3)
                                             .map(it->it+100) //add 100
                                             .toList();
     * }
     * //results in [100,200,300]
     * </pre>
     *	@param mapper Function to be applied to all items in the Stream
     *	@return
     * @see com.aol.cyclops.react.stream.traits.FutureStream#map(java.util.function.Function)
     */
    default <R> LazyFutureStream<R> map(Function<? super U, ? extends R> mapper) {

        return (LazyFutureStream<R>) LazySimpleReactStream.super.then((Function) mapper);
    }

    /**
     * Break a stream into multiple Streams based of some characteristic of the
     * elements of the Stream
     *
     * e.g.
     *
     * <pre>
     * <code>
     *
     * LazyFutureStream.of(10,20,25,30,41,43).shard(ImmutableMap.of("even",new
     * 															Queue(),"odd",new Queue(),element-&gt; element%2==0? "even" : "odd");
     *
     * </code>
     * </pre>
     *
     * results in 2 Streams "even": 10,20,30 "odd" : 25,41,43
     *
     * @param shards
     *            Map of Queue's keyed by shard identifier
     * @param sharder
     *            Function to split split incoming elements into shards
     * @return Map of new sharded Streams
     */
    default <K> Map<K, LazyFutureStream<U>> shard(Map<K, Queue<U>> shards, Function<? super U, ? extends K> sharder) {

        toQueue(shards, sharder);
        Map res = shards.entrySet()
                        .stream()
                        .collect(Collectors.toMap(e -> e.getKey(), e -> fromStream(e.getValue()
                                                                                    .stream(getSubscription()))));
        return res;
    }

    /**
     * Can be used to debounce (accept a single data point from a unit of time)
     * data. This drops data. For a method that slows emissions and keeps data
     * #see#onePer
     *
     * <pre>
     * {@code
     * LazyFutureStream.of(1,2,3,4,5,6)
     *          .debounce(1000,TimeUnit.SECONDS).toList();
     *
     * // [1]
     * }
     * </pre>
     *
     * @param time
     *            Time from which to accept only one element
     * @param unit
     *            Time unit for specified time
     * @return Next stage of stream, with only 1 element per specified time
     *         windows
     */
    default LazyFutureStream<U> debounce(long time, TimeUnit unit) {
        return fromStream(ReactiveSeq.fromStream(toQueue().stream(getSubscription()))
                                     .debounce(time, unit));

    }

    /**
     * Allows clients to control the emission of data for the next phase of the
     * Stream. The user specified function can delay, drop, or change elements
     *
     * @param fn
     *            Function takes a supplier, which can be used repeatedly to get
     *            the next value from the Stream. If there are no more values, a
     *            ClosedQueueException will be thrown. This function should
     *            return a Supplier which returns the desired result for the
     *            next element (or just the next element).
     * @return Next stage in Stream
     */
    default LazyFutureStream<U> control(Function<Supplier<U>, Supplier<U>> fn) {
        Queue queue = toQueue();
        return fromStream(queue.streamControl(getSubscription(), fn));
    }

    /**
     * Batch elements into a Stream of collections with user defined function
     *
     * @param fn
     *            Function takes a supplier, which can be used repeatedly to get
     *            the next value from the Stream. If there are no more values, a
     *            ClosedQueueException will be thrown. This function should
     *            return a Supplier which creates a collection of the batched
     *            values
     * @return Stream of batched values
     */
    default <C extends Collection<U>> LazyFutureStream<C> group(Function<Supplier<U>, Supplier<C>> fn) {
        Queue queue = toQueue();
        return fromStream(queue.streamBatchNoTimeout(getSubscription(), fn));
    }

    /*
     * Batch the elements in the Stream by a combination of Size and Time
     * If batch exceeds max size it will be split
     * If batch exceeds max time it will be split
     * Excludes Null values (neccessary for timeout handling)
     *
     * <pre>
     * {@code
     * assertThat(react(()->1,()->2,()->3,()->4,()->5,()->{sleep(100);return 6;})
                        .batchBySizeAndTime(30,60,TimeUnit.MILLISECONDS)
                        .toList()
                        .get(0)
                        ,not(hasItem(6)));
        }
     * </pre>
     *
     * <pre>
     * {@code
     *
        assertThat(of(1,2,3,4,5,6).batchBySizeAndTime(3,10,TimeUnit.SECONDS).toList().get(0).size(),is(3));
    
     * }</pre>
     *
     *	@param size Max batch size
     *	@param time Max time length
     *	@param unit time unit
     *	@return batched stream
     * @see com.aol.cyclops.react.stream.traits.FutureStream#batchBySizeAndTime(int, long, java.util.concurrent.TimeUnit)
     */
    default LazyFutureStream<ListX<U>> groupedBySizeAndTime(int size, long time, TimeUnit unit) {
        return fromStream(ReactiveSeq.fromStream(toQueue().stream(getSubscription()))
                                     .groupedBySizeAndTime(size, time, unit));
        /**      Queue<U> queue = toQueue();
        Function<BiFunction<Long,TimeUnit,U>, Supplier<Collection<U>>> fn = new BatchByTimeAndSize<>(size,time,unit,()->new ListXImpl<>());
        return (LazyFutureStream)fromStream(queue.streamBatch(getSubscription(), (Function)fn)).filter(c->!((Collection)c).isEmpty());**/
    }

    /**
     * Batch the elements in this stream into Collections of specified size The
     * type of Collection is determined by the specified supplier
     *
     * <pre>
     * {@code
     * 		LazyFutureStream.of(1,1,1,1,1,1)
     * 						.batchBySize(3,()->new TreeSet<>())
     * 						.toList()
     *
     *   //[[1],[1]]
     * }
     *
     * </pre>
     *
     * @param size
     *            Size of batch
     * @param supplier
     *            Create the batch holding collection
     * @return Stream of Collections
     */
    default <C extends Collection<? super U>> LazyFutureStream<C> grouped(int size, Supplier<C> supplier) {
        return fromStream(ReactiveSeq.fromStream(toQueue().stream(getSubscription()))
                                     .grouped(size, supplier));

    }

    /**
     * Introduce a random delay between events in a stream Can be used to
     * prevent behaviour synchronizing within a system
     *
     * <pre>
     * {@code
     *
     * LazyFutureStream.parallelCommonBuilder()
     * 						.of(IntStream.range(0, 100))
     * 						.map(it -> it*100)
     * 						.jitter(10l)
     * 						.peek(System.out::println)
     * 						.block();
     *
     * }
     *
     * </pre>
     *
     * @param jitterInNanos
     *            Max number of nanos for jitter (random number less than this
     *            will be selected)/
     * @return Next stage in Stream with jitter applied
     */
    default LazyFutureStream<U> jitter(long jitterInNanos) {
        return fromStream(ReactiveSeq.fromStream(toQueue().stream(getSubscription()))
                                     .jitter(jitterInNanos));
    }

    /**
     * Apply a fixed delay before emitting elements to the next phase of the
     * Stream. Note this doesn't neccessarily imply a fixed delay between
     * element creation (although it may do). e.g.
     *
     * <pre>
     * {@code
     *    LazyFutureStream.of(1,2,3,4)
     *                    .fixedDelay(1,TimeUnit.hours);
     * }
     * </pre>
     *
     * Will emit 1 on start, then 2 after an hour, 3 after 2 hours and so on.
     *
     * However all 4 numbers will be populated in the Stream immediately.
     *
     * <pre>
     * {@code
     * LazyFutureStream.of(1,2,3,4)
     *                 .withQueueFactories(QueueFactories.boundedQueue(1))
     *                 .fixedDelay(1,TimeUnit.hours);
     *
     * }
     * </pre>
     *
     * Will populate each number in the Stream an hour apart.
     *
     * @param time
     *            amount of time between emissions
     * @param unit
     *            TimeUnit for emissions
     * @return Next Stage of the Stream
     */
    default LazyFutureStream<U> fixedDelay(long time, TimeUnit unit) {
        return fromStream(ReactiveSeq.fromStream(toQueue().stream(getSubscription()))
                                     .fixedDelay(time, unit));
    }

    /**
     * Slow emissions down, emiting one element per specified time period
     *
     * <pre>
     * {@code
     * 		LazyFutureStream.of(1,2,3,4,5,6)
     * 						 .onePer(1000,TimeUnit.NANOSECONDS)
     * 						 .toList();
     *
     * }
     *
     * </pre>
     *
     * @param time
     *            Frequency period of element emission
     * @param unit
     *            Time unit for frequency period
     * @return Stream with emissions slowed down by specified emission frequency
     */
    default LazyFutureStream<U> onePer(long time, TimeUnit unit) {
        return fromStream(ReactiveSeq.fromStream(toQueue().stream(getSubscription()))
                                     .onePer(time, unit));

    }

    /**
     * Allows x (specified number of) emissions with a time period before
     * stopping emmissions until specified time has elapsed since last emission
     *
     * <pre>
     * {@code
     *    LazyFutureStream.of(1,2,3,4,5,6)
     *    				   .xPer(6,100000000,TimeUnit.NANOSECONDS)
     *    				   .toList();
     *
     * }
     *
     * </pre>
     *
     * @param x
     *            Number of allowable emissions per time period
     * @param time
     *            Frequency time period
     * @param unit
     *            Frequency time unit
     * @return Stream with emissions slowed down by specified emission frequency
     */
    default LazyFutureStream<U> xPer(int x, long time, TimeUnit unit) {
        return fromStream(ReactiveSeq.fromStream(toQueue().stream(getSubscription()))
                                     .xPer(x, time, unit));
    }

    /**
     * Organise elements in a Stream into a Collections based on the time period
     * they pass through this stage
     *
     * <pre>
     * {@code
     * 	LazyFutureStream.react(()->load1(),()->load2(),()->load3(),()->load4(),()->load5(),()->load6())
     * 					.batchByTime(15000,TimeUnit.MICROSECONDS);
     *
     * }
     *
     * </pre>
     *
     * @param time
     *            Time period during which all elements should be collected
     * @param unit
     *            Time unit during which all elements should be collected
     * @return Stream of Lists
     */
    default LazyFutureStream<ListX<U>> groupedByTime(long time, TimeUnit unit) {
        return fromStream(ReactiveSeq.fromStream(toQueue().stream(getSubscription()))
                                     .groupedByTime(time, unit));

    }

    /**
     * Organise elements in a Stream into a Collections based on the time period
     * they pass through this stage
     *
     * <pre>
     * {@code
     * List <TreeSet<Integer>> set = LazyFutureStream.ofThread(1,1,1,1,1,1)
     *                                               .batchByTime(1500,TimeUnit.MICROSECONDS,()-> new TreeSet<>())
     *                                               .block();
    
            assertThat(set.get(0).size(),is(1));
     *
     * }
     * </pre>
     *
     *
     * @param time
     *            Time period during which all elements should be collected
     * @param unit
     *            Time unit during which all elements should be collected
     * @param factory
     *            Instantiates the collections used in the batching
     * @return Stream of collections
     */
    default <C extends Collection<? super U>> LazyFutureStream<C> groupedByTime(long time, TimeUnit unit, Supplier<C> factory) {
        return fromStream(ReactiveSeq.fromStream(toQueue().stream(getSubscription()))
                                     .groupedByTime(time, unit, factory));

    }

    /*
     *
     * React to new events with the supplied function on the supplied
     * Executor
     *
     * @param fn Apply to incoming events
     *
     * @param service Service to execute function on
     *
     * @return next stage in the Stream
     */
    default <R> LazyFutureStream<R> then(final Function<? super U, ? extends R> fn, Executor service) {
        return (LazyFutureStream<R>) LazySimpleReactStream.super.then(fn, service);
    }

    /*
     * Non-blocking asyncrhonous application of the supplied function.
     * Equivalent to map from Streams / Seq apis.
     *
     * @param fn Function to be applied asynchronously
     *
     * @return Next stage in stream
     *
     * @see
     * com.aol.simple.react.stream.traits.FutureStream#then(java.util.function
     * .Function)
     */
    default <R> LazyFutureStream<R> then(final Function<? super U, ? extends R> fn) {
        return (LazyFutureStream) LazySimpleReactStream.super.then(fn);
    }

    /**
     * Copy this Stream the specified number of times
     *
     * <pre>
     * {@code
     * LazyFutureStream.of(1,2,3,4,5,6)
                .map(i->i+2)
                .copy(5)
                .forEach(s -> System.out.println(s.toList()));
     *
     * }</pre>
     *
     * @param times to copy this Stream
     * @return List with specified number of copies
     */
    default List<LazyFutureStream<U>> copy(final int times) {
        return (List) LazySimpleReactStream.super.copySimpleReactStream(times);

    }

    /**
     * Merges this stream and the supplied Streams into a single Stream where the next value
     * is the next returned across any of the involved Streams. Suitable for merging infinite streams
     *
     * <pre>
     * {@code
     * 	LazyFutureStream<Integer> fast =  ... //  [1,2,3,4,5,6,7..]
     * 	LazyFutureStream<Integer> slow =  ... //  [100,200,300,400,500,600..]
     *
     *  LazyFutureStream<Integer> merged = fast.switchOnNextValue(Stream.of(slow));  //[1,2,3,4,5,6,7,8,100,9,10,11,12,13,14,15,16,200..]
     * }
     * </pre>
     *
     * @param streams
     * @return
     */
    default <R> LazyFutureStream<R> switchOnNextValue(Stream<LazyFutureStream> streams) {
        Queue queue = Queue.createMergeQueue();
        addToQueue(queue);
        streams.forEach(s -> s.addToQueue(queue));

        return fromStream(queue.stream(this.getSubscription()));
    }

    /**
     * Merges this stream and the supplied Streams into a single Stream where the next value
     * is the next returned across any of the involved Streams. Suitable for merging infinite streams
     *
     * <pre>
     * {@code
     * 	LazyFutureStream<Integer> fast =  ... //  [1,2,3,4,5,6,7..]
     * 	LazyFutureStream<Integer> slow =  ... //  [100,200,300,400,500,600..]
     *
     *  LazyFutureStream<Integer> merged = fast.mergeLatest(slow);  //[1,2,3,4,5,6,7,8,100,9,10,11,12,13,14,15,16,200..]
     * }
     * </pre>
     *
     * @param streams
     * @return
     */
    default <R> LazyFutureStream<R> mergeLatest(LazyFutureStream<?>... streams) {
        Queue queue = Queue.createMergeQueue();
        addToQueue(queue);
        Seq.of(streams)
           .forEach(s -> s.addToQueue(queue));

        return fromStream(queue.stream(this.getSubscription()));
    }

    /*
     * Define failure handling for this stage in a stream. Recovery function
     * will be called after an exception Will be passed a
     * SimpleReactFailedStageException which contains both the cause, and the
     * input value.
     *
     * <pre>
     * {@code
     * List<String> results = LazyReact.sequentialCommonBuilder()
                                        .withRetrier(retrier)
                                        .react(() -> "new event1", () -> "new event2")
                                        .retry(this::unreliable)
                                        .onFail(e -> "default")
                                        .peek(System.out::println)
                                        .capture(Throwable::printStackTrace)
                                        .block();
     *
     * }
     * </pre>
     *
     * @param fn Recovery function
     *
     * @return Next stage in stream
     *
     * @see
     * com.aol.simple.react.stream.traits.FutureStream#onFail(java.util.function
     * .Function)
     */
    @Override
    default LazyFutureStream<U> onFail(final Function<? super SimpleReactFailedStageException, ? extends U> fn) {
        return (LazyFutureStream) LazySimpleReactStream.super.onFail(fn);
    }

    /*
     * Handle failure for a particular class of exceptions only
     *
     * @param exceptionClass Class of exceptions to handle
     *
     * @param fn recovery function
     *
     * @return recovered value
     *
     * @see
     * com.aol.simple.react.stream.traits.FutureStream#onFail(java.lang.Class,
     * java.util.function.Function)
     */
    @Override
    default LazyFutureStream<U> onFail(Class<? extends Throwable> exceptionClass,
            final Function<? super SimpleReactFailedStageException, ? extends U> fn) {
        return (LazyFutureStream) LazySimpleReactStream.super.onFail(exceptionClass, fn);
    }

    /*
     * Capture non-recoverable exception
     *
     * <pre>
     * {@code
     *  LazyFutureStream.of(1, "a", 2, "b", 3, null)
     *                  .capture(e-> e.printStackTrace())
                        .peek(it ->System.out.println(it))
                        .cast(Integer.class)
                        .peek(it ->System.out.println(it))
                        .toList();
    
     *  //prints the ClasCastException for failed Casts
     * }
     * </pre>
     * @param errorHandler Consumer that captures the exception
     *
     * @return Next stage in stream
     *
     * @see
     * com.aol.simple.react.stream.traits.FutureStream#capture(java.util.function
     * .Consumer)
     */
    @Override
    default LazyFutureStream<U> capture(final Consumer<Throwable> errorHandler) {
        return (LazyFutureStream) LazySimpleReactStream.super.capture(errorHandler);
    }

    /*
     * @see
     * com.aol.simple.react.stream.traits.FutureStream#peek(java.util.function
     * .Consumer)
     */
    @Override
    default LazyFutureStream<U> peek(final Consumer<? super U> consumer) {
        return (LazyFutureStream) LazySimpleReactStream.super.peek(consumer);
    }

    /*
     * @see
     * com.aol.simple.react.stream.traits.FutureStream#filter(java.util.function
     * .Predicate)
     */
    default LazyFutureStream<U> filter(final Predicate<? super U> p) {
        return (LazyFutureStream) LazySimpleReactStream.super.filter(p);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.aol.simple.react.stream.FutureStreamImpl#flatMap(java.util.function
     * .Function)
     */
    @Override
    default <R> LazyFutureStream<R> flatMap(Function<? super U, ? extends Stream<? extends R>> flatFn) {
	return map(flatFn).flatten();
    }

    @Override
    default <R> LazyFutureStream<R> flatMapAnyM(Function<? super U, AnyM<? extends R>> flatFn) {

        return (LazyFutureStream) LazySimpleReactStream.super.flatMap(flatFn.andThen(anyM -> anyM.stream()));
    }

    /**
     * Perform a flatMap operation where the CompletableFuture type returned is flattened from the resulting Stream
     * If in async mode this operation is performed asyncrhonously
     * If in sync mode this operation is performed synchronously
     *
     * <pre>
     * {@code
     * assertThat( new LazyReact()
                                        .of(1,2,3)
                                        .flatMapCompletableFuture(i->CompletableFuture.completedFuture(i))
                                        .block(),equalTo(Arrays.asList(1,2,3)));
     * }
     * </pre>
     * In this example the result of the flatMapCompletableFuture is 'flattened' to the raw integer values
     *
     *
     * @param flatFn flatMap function
     * @return Flatten Stream with flatFn applied
     */
    default <R> LazyFutureStream<R> flatMapCompletableFuture(Function<? super U, CompletableFuture<? extends R>> flatFn) {
        return fromStream(StreamUtils.flatMapCompletableFuture(toQueue().stream(getSubscription()), flatFn));
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.aol.simple.react.stream.FutureStreamImpl#retry(java.util.function
     * .Function)
     */
    @Override
    default <R> LazyFutureStream<R> retry(Function<? super U, ? extends R> fn) {

        return (LazyFutureStream) LazySimpleReactStream.super.retry(fn);
    }

    /*
     * Convert the specified Stream to a LazyFutureStream, using the configuration
     * of this LazyFutureStream (task executors, current config settings)
     *
     * @see com.aol.cyclops.react.stream.traits.SimpleReactStream#fromStream(java.util.stream.Stream)
     */
    @Override
    default <R> LazyFutureStream<R> fromStream(Stream<R> stream) {

        return (LazyFutureStream) this.withLastActive(getLastActive().withNewStream(stream, this.getSimpleReact()));
    }

    /*
     * Convert the specified Stream to a LazyFutureStream, using the configuration
     * of this LazyFutureStream (task executors, current config settings)
     *
     * (non-Javadoc)
     *
     * @see
     * com.aol.simple.react.stream.FutureStreamImpl#fromStreamCompletableFuture
     * (java.util.stream.Stream)
     */

    default <R> LazyFutureStream<R> fromStreamOfFutures(Stream<FastFuture<R>> stream) {

        return (LazyFutureStream) this.withLastActive(getLastActive().withNewStreamFutures(stream.map(f -> f.toCompletableFuture())));
    }

    /**
     * Concatenate two streams.
     *
     *
     * // (1, 2, 3, 4, 5, 6) LazyFutureStream.of(1, 2,
     * 3).concat(LazyFutureStream.of(4, 5, 6))
     *
     *
     * @see #concat(Stream[])
     */
    @SuppressWarnings({ "unchecked" })
    @Override
    default LazyFutureStream<U> concat(Stream<? extends U> other) {
        return fromStream(Stream.concat(StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator(), Spliterator.ORDERED), false),
                                        StreamSupport.stream(Spliterators.spliteratorUnknownSize(other.iterator(), Spliterator.ORDERED), false)));

    }

    /**
    * Concatenate two streams.
    *
    * <pre>
    * {@code
    * // (1, 2, 3, 4)
    * LazyFutureStream.of(1, 2, 3).concat(4)
    * }
    * </pre>
    *
    * @see #concat(Stream[])
    */
    default LazyFutureStream<U> concat(U other) {
        return concat(Stream.of(other));
    }

    /**
     * Concatenate two streams.
     *
     * <pre>
     * {@code
     * // (1, 2, 3, 4, 5, 6)
     * LazyFutureStream.of(1, 2, 3).concat(4, 5, 6)
     * }
     * </pre>
     *
     * @see #concat(Stream[])
     */
    @SuppressWarnings({ "unchecked" })
    default LazyFutureStream<U> concat(U... other) {
        return concat(Stream.of(other));
    }

    /*
     * Cast all elements in this stream to specified type. May throw {@link
     * ClassCastException}.
     *
     * LazyFutureStream.of(1, "a", 2, "b", 3).cast(Integer.class)
     *
     * will throw a ClassCastException
     *
     * @param type Type to cast to
     *
     * @return LazyFutureStream
     *
     * @see
     * com.aol.simple.react.stream.traits.FutureStream#cast(java.lang.Class)
     */
    @Override
    default <U> LazyFutureStream<U> cast(Class<? extends U> type) {
        return (LazyFutureStream) LazySimpleReactStream.super.cast(type);

    }

    /**
     * Keep only those elements in a stream that are of a given type.
     *
     *
     *
     * LazyFutureStream.of(1, "a", 2, "b", 3).ofType(Integer.class)
     *
     * gives a Stream of (1,2,3)
     *
     * LazyFutureStream.of(1, "a", 2, "b", 3).ofType(String.class)
     *
     * gives a Stream of ("a","b")
     *
     */
    @Override
    default <U> LazyFutureStream<U> ofType(Class<? extends U> type) {
        return (LazyFutureStream) LazySimpleReactStream.super.ofType(type);
    }

    /**
     * Returns a stream with a given value interspersed between any two values
     * of this stream.
     *
     * <code>
     *
     * // (1, 0, 2, 0, 3, 0, 4)
     *
     * LazyFutureStream.of(1, 2, 3, 4).intersperse(0)
     *
     * </code>
     *
     * @see #intersperse(Stream, Object)
     */
    @Override
    default LazyFutureStream<U> intersperse(U value) {
        return (LazyFutureStream) LazySimpleReactStream.super.intersperse(value);
    }

    /*
     *
     * LazyFutureStream.of(1,2,3,4).limit(2)
     *
     * Will result in a Stream of (1,2). Only the first two elements are used.
     *
     * @param maxSize number of elements to take
     *
     * @return Limited LazyFutureStream
     *
     * @see org.jooq.lambda.Seq#limit(long)
     */
    @Override
    default LazyFutureStream<U> limit(long maxSize) {
        Continueable sub = this.getSubscription();
        sub.registerLimit(maxSize);
        return fromStream(ReactiveSeq.fromStream(toQueue().stream(sub))
                                     .limit(maxSize));

    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.ReactiveSeq#take(long)
     */
    @Override
    default LazyFutureStream<U> drop(long drop) {
        return skip(drop);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.ReactiveSeq#take(long)
     */
    @Override
    default LazyFutureStream<U> take(long take) {
        return limit(take);
    }

    @Override
    default LazyFutureStream<U> takeWhile(Predicate<? super U> p) {

        return this.limitWhile(p);
    }

    @Override
    default LazyFutureStream<U> dropWhile(Predicate<? super U> p) {

        return this.skipWhile(p);
    }

    @Override
    default LazyFutureStream<U> takeUntil(Predicate<? super U> p) {

        return this.limitUntil(p);
    }

    @Override
    default LazyFutureStream<U> dropUntil(Predicate<? super U> p) {

        return this.skipUntil(p);
    }

    @Override
    default LazyFutureStream<U> dropRight(int num) {

        return this.skipLast(num);
    }

    @Override
    default LazyFutureStream<U> takeRight(int num) {

        return this.limitLast(num);
    }

    /*
     * LazyFutureStream.of(1,2,3,4).skip(2)
     *
     * Will result in a stream of (3,4). The first two elements are skipped.
     *
     * @param n Number of elements to skip
     *
     * @return LazyFutureStream missing skipped elements
     *
     * @see org.jooq.lambda.Seq#skip(long)
     */
    @Override
    default LazyFutureStream<U> skip(long n) {
        Continueable sub = this.getSubscription();
        sub.registerSkip(n);
        return fromStream(ReactiveSeq.fromStream(toQueue().stream(sub))
                                     .skip(n));

    }

    /*
     * @return distinct elements in this Stream (must be a finite stream!)
     *
     * @see org.jooq.lambda.Seq#distinct()
     */
    @Override
    default LazyFutureStream<U> distinct() {

        return fromStream(toQueue().stream(getSubscription())
                                   .distinct());
    }

    /**
     * Create a sliding view over this Stream
     *
     * <pre>
     * {@code
     * //futureStream of [1,2,3,4,5,6]
     *
     * List<List<Integer>> list = futureStream.sliding(2)
                                    .collect(Collectors.toList());
    
    
        assertThat(list.get(0),hasItems(1,2));
        assertThat(list.get(1),hasItems(2,3));
     * }
     * </pre>
     * @param size
     *            Size of sliding window
     * @return Stream with sliding view over data in this stream
     */
    @Override
    default LazyFutureStream<ListX<U>> sliding(int size) {
        //    return this.fromStream(SlidingWindow.sliding(this,size, 1));
        return (LazyFutureStream) fromStream(ReactiveSeq.fromStream(toQueue().stream(getSubscription()))
                                                        .sliding(size));
    }

    /**
     * Create a sliding view over this Stream
     *
     * <pre>
     * {@code
     * //futureStream of [1,2,3,4,5,6,7,8]
     *
     * List<List<Integer>> list = futureStream.sliding(3,2)
                                    .collect(Collectors.toList());
    
    
        assertThat(list.get(0),hasItems(1,2,3));
        assertThat(list.get(1),hasItems(3,4,5));
     * }
     * </pre>
     * @param size
     *            Size of sliding window
     * @return Stream with sliding view over data in this stream
     */
    @Override
    default LazyFutureStream<ListX<U>> sliding(int size, int increment) {
        //return this.fromStream(SlidingWindow.sliding(this,size, increment));
        return (LazyFutureStream) fromStream(ReactiveSeq.fromStream(toQueue().stream(getSubscription()))
                                                        .sliding(size, increment));

    }

    /**
     * Duplicate a LazyFutureStream into two equivalent Streams.
     * Two LazyFutureStreams are
     * returned but Seq interface specifies return type is Seq. See duplicateFutureStream to
     * see an alternative which returns LazyFutureStream
     *
     * <pre>
     * {@code
     *
     * // tuple((1, 2, 3), (1, 2, 3))
     *
     * LazyFutureStream.of(1, 2, 3).duplicate()
     * }
     * </pre>
     *
     * @see LazyFutureStream#copy(int)
     *
     * @see #duplicate(Stream)
     */
    @Override
    default Tuple2<Seq<U>, Seq<U>> duplicate() {
        return ReactiveSeq.super.duplicate();

    }

    /*
     * <pre>
     * {@code
     *
     * // tuple((1, 2, 3), (1, 2, 3))
     *
     * LazyFutureStream.of(1, 2, 3).duplicate()
     * }
     * </pre>
     *
     * @see LazyFutureStream#copy(int)
     *
     * @see #duplicate(Stream)
     */
    default Tuple2<LazyFutureStream<U>, LazyFutureStream<U>> duplicateFutureStream() {
        Tuple2<Seq<U>, Seq<U>> duplicated = this.duplicate();
        return new Tuple2(
                          fromStream(duplicated.v1), fromStream(duplicated.v2));
    }

    /**
     * Partition a stream in two given a predicate. Two LazyFutureStreams are
     * returned but Seq interface specifies return type is Seq. See partitionFutureStream to
     * see an alternative which returns LazyFutureStream
     *
     * <code>
     *
     * // tuple((1, 3, 5), (2, 4, 6))
     *
     * LazyFutureStream.of(1, 2, 3, 4, 5,6).partition(i -&gt; i % 2 != 0)
     *
     * </code>
     *
     * @see #partitionFutureStream(Predicate)
     * @see #partition(Stream, Predicate)
     */
    @Override
    default Tuple2<Seq<U>, Seq<U>> partition(Predicate<? super U> predicate) {
        return ReactiveSeq.super.partition(predicate);
    }

    /**
     * Partition an LazyFutureStream into two LazyFutureStreams given a
     * predicate.
     * <pre>
     * {@code
     * LazyFutureStream.of(1, 2, 3, 4, 5, 6).partition(i -> i % 2 != 0)
     *
     * results in
     *
     * tuple((1, 3, 5), (2, 4, 6))
     * }</pre>
     * @param predicate
     *            Predicate to split Stream
     * @return LazyFutureStream
     * @see #partition(Predicate)
     */
    default Tuple2<LazyFutureStream<U>, LazyFutureStream<U>> partitionFutureStream(Predicate<? super U> predicate) {
        Tuple2<Seq<U>, Seq<U>> partition = partition(predicate);
        return new Tuple2(
                          fromStream(partition.v1), fromStream(partition.v2));
    }

    /*
     * @see com.aol.cyclops.control.ReactiveSeq#slice(long, long)
     */
    @Override
    default LazyFutureStream<U> slice(long from, long to) {

        return fromStream(ReactiveSeq.fromStream(toQueue().stream(getSubscription())
                                                          .slice(from, to)));
    }

    /**
     * Zip a Stream with a corresponding Stream of indexes.
     *
     * <code>
     *
     * // (tuple("a", 0), tuple("b", 1), tuple("c", 2))
     *
     * LazyFutureStream.of("a","b", "c").zipWithIndex()
     *
     *</code>
     *
     * @see #zipWithIndex(Stream)
     *
     *
     */
    default LazyFutureStream<Tuple2<U, Long>> zipWithIndex() {
        return fromStream(ReactiveSeq.fromStream(toQueue().stream(getSubscription()))
                                     .zipWithIndex());
    }

    /**
     * Zip two streams into one.
     *
     * <pre>
     * {@code
     *  // (tuple(1, "a"), tuple(2, "b"), tuple(3, "c"))
     *  LazyFutureStream.of(1, 2, 3)
     *                  .zip(Seq.of("a", "b", "c"))
     * }
     * </pre>
     *
     * @see #zip(Stream, Stream)
     */
    @Override
    default <T> LazyFutureStream<Tuple2<U, T>> zip(Seq<? extends T> other) {
        return fromStream(LazyFutureStreamFunctions.zip(this, other));
    }

    @Override
    default <T> LazyFutureStream<Tuple2<U, T>> zip(Stream<? extends T> other) {
        return fromStream(LazyFutureStreamFunctions.zip(this, other));
    }

    @Override
    default <T> LazyFutureStream<Tuple2<U, T>> zip(Iterable<? extends T> other) {
        return fromStream(LazyFutureStreamFunctions.zip(this, ReactiveSeq.fromIterable(other)));
    }

    /**
     * Zip two streams into one using a {@link BiFunction} to produce resulting
     * values.
     * <p>
     * <code>
     * // ("1:a", "2:b", "3:c")
     * LazyFutureStream.of(1, 2, 3).zip(Seq.of("a", "b", "c"), (i, s) -&gt; i + ":" + s)
     * </code>
     *
     * @see #zip(Seq, BiFunction)
     */
    @Override
    default <T, R> LazyFutureStream<R> zip(Seq<? extends T> other, BiFunction<? super U, ? super T, ? extends R> zipper) {
        return fromStream(LazyFutureStreamFunctions.zip(this, other, zipper));
    }

    @Override
    default <T, R> LazyFutureStream<R> zip(Stream<? extends T> other, BiFunction<? super U, ? super T, ? extends R> zipper) {
        return fromStream(LazyFutureStreamFunctions.zip(this, ReactiveSeq.fromStream(other), zipper));
    }

    @Override
    default <T, R> LazyFutureStream<R> zip(Iterable<? extends T> other, BiFunction<? super U, ? super T, ? extends R> zipper) {
        return fromStream(LazyFutureStreamFunctions.zip(this, ReactiveSeq.fromIterable(other), zipper));
    }

    /**
     * Scan a stream to the left.
     *
     *
     * // ("", "a", "ab", "abc") LazyFutureStream.of("a", "b", "c").scanLeft("",
     * (u, t) &gt; u + t)
     *
     */
    @Override
    default <T> LazyFutureStream<T> scanLeft(T seed, BiFunction<? super T, ? super U, ? extends T> function) {
        return fromStream(ReactiveSeq.fromStream(toQueue().stream(getSubscription()))
                                     .scanLeft(seed, function));

    }

    /**
     * Scan a stream to the right. - careful with infinite streams!
     *
     *
     * // ("", "c", "cb", "cba") LazyFutureStream.of("a", "b",
     * "c").scanRight("", (t, u) &gt; u + t)
     *
     */
    @Override
    default <R> LazyFutureStream<R> scanRight(R seed, BiFunction<? super U, ? super R, ? extends R> function) {
        return fromStream(ReactiveSeq.fromStream(toQueue().stream(getSubscription()))
                                     .scanRight(seed, function));

    }

    @Override
    default LazyFutureStream<U> scanRight(Monoid<U> monoid) {
        return fromStream(ReactiveSeq.fromStream(toQueue().stream(getSubscription()))
                                     .scanRight(monoid));

    }

    /**
     * Reverse a stream. - eager operation that materializes the Stream into a list - careful with infinite streams!
     *
     *
     * // (3, 2, 1) LazyFutureStream.of(1, 2, 3).reverse()
     *
     */
    @Override
    default LazyFutureStream<U> reverse() {
        //reverse using LazyFutureStream semantics to ensure concurrency / parallelism
        return fromStream(fromStream(toQueue().stream(getSubscription())).block()
                                                                         .reverse()
                                                                         .stream());

    }

    /**
     * Shuffle a stream
     *
     *
     * // e.g. (2, 3, 1) LazyFutureStream.of(1, 2, 3).shuffle()
     *
     */
    @Override
    default LazyFutureStream<U> shuffle() {
        return fromStream(ReactiveSeq.fromStream(toQueue().stream(getSubscription()))
                                     .shuffle());
    }

    /**
     * Shuffle a stream using specified source of randomness
     *
     *
     * // e.g. (2, 3, 1) LazyFutureStream.of(1, 2, 3).shuffle(new Random())
     *
     */
    @Override
    default LazyFutureStream<U> shuffle(Random random) {
        return fromStream(ReactiveSeq.fromStream(toQueue().stream(getSubscription()))
                                     .shuffle(random));
    }

    /**
     * Returns a stream with all elements skipped for which a predicate
     * evaluates to true.
     *
     *
     * // (3, 4, 5) LazyFutureStream.of(1, 2, 3, 4, 5).skipWhile(i &gt; i &lt;
     * 3)
     *
     *
     * @see #skipWhile(Stream, Predicate)
     */
    @Override
    default LazyFutureStream<U> skipWhile(Predicate<? super U> predicate) {
        return fromStream(ReactiveSeq.fromStream(toQueue().stream(getSubscription()))
                                     .skipWhile(predicate));
    }

    /**
     * Returns a stream with all elements skipped for which a predicate
     * evaluates to false.
     *
     *
     * // (3, 4, 5) LazyFutureStream.of(1, 2, 3, 4, 5).skipUntil(i &gt; i == 3)
     *
     *
     * @see #skipUntil(Stream, Predicate)
     */
    @Override
    default LazyFutureStream<U> skipUntil(Predicate<? super U> predicate) {
        return fromStream(ReactiveSeq.fromStream(toQueue().stream(getSubscription()))
                                     .skipUntil(predicate));
    }

    /**
     * Returns a stream limited to all elements for which a predicate evaluates
     * to true.
     *
     *
     * // (1, 2) LazyFutureStream.of(1, 2, 3, 4, 5).limitWhile(i -&gt; i &lt; 3)
     *
     *
     * @see #limitWhile(Stream, Predicate)
     */
    @Override
    default LazyFutureStream<U> limitWhile(Predicate<? super U> predicate) {
        return fromStream(ReactiveSeq.fromStream(toQueue().stream(getSubscription()))
                                     .limitWhile(predicate));
    }

    /**
     * Returns a stream limited to all elements for which a predicate evaluates
     * to false.
     *
     *
     * // (1, 2) LazyFutureStream.of(1, 2, 3, 4, 5).limitUntil(i &gt; i == 3)
     *
     *
     * @see #limitUntil(Stream, Predicate)
     */
    @Override
    default LazyFutureStream<U> limitUntil(Predicate<? super U> predicate) {
        return fromStream(LazyFutureStreamFunctions.limitUntil(this, predicate));
    }

    /**
     * Cross join 2 streams into one.
     * <p>
     * <pre>{@code
     * // (tuple(1, "a"), tuple(1, "b"), tuple(2, "a"), tuple(2, "b"))
     * LazyFutureStream.of(1, 2).crossJoin(LazyFutureStream.of("a", "b"))
     * }</pre>
     */
    default <T> LazyFutureStream<Tuple2<U, T>> crossJoin(Stream<? extends T> other) {
        return fromStream(ReactiveSeq.fromStream(toQueue().stream(getSubscription()))
                                     .crossJoin(other));
    }

    /**
     * Produce this stream, or an alternative stream from the
     * {@code value}, in case this stream is empty.
     * <pre>
     * {@code
     *    LazyFutureStream.of().onEmpty(1)
     *
     *     //1
     * }</pre>
     *
     *
     */
    default LazyFutureStream<U> onEmpty(U value) {

        return fromStream(ReactiveSeq.fromStream(toQueue().stream(getSubscription()))
                                     .onEmpty(value));
    }

    /**
     * Produce this stream, or an alternative stream from the
     * {@code supplier}, in case this stream is empty.
     *
     * <pre>
     * {@code
     *    LazyFutureStream.of().onEmptyGet(() -> 1)
     *
     *
     *  //1
     * }
     * </pre>
     *
     */
    default LazyFutureStream<U> onEmptyGet(Supplier<? extends U> supplier) {
        return fromStream(ReactiveSeq.fromStream(toQueue().stream(getSubscription()))
                                     .onEmptyGet(supplier));
    }

    /**
     * Produce this stream, or an alternative stream from the
     * {@code supplier}, in case this stream is empty.
     *
     * <pre>
     * {@code
     *   LazyFutureStream.of().capture(e -> ex = e).onEmptyThrow(() -> new RuntimeException()).toList();
     *
     *   //throws RuntimeException
     * }
     * </pre>
     *
     */
    default <X extends Throwable> LazyFutureStream<U> onEmptyThrow(Supplier<? extends X> supplier) {
        return fromStream(ReactiveSeq.super.onEmptyThrow(supplier));
    }

    /**
     * Inner join 2 streams into one.
     *
     * <pre>
     * {@code
     * // (tuple(1, 1), tuple(2, 2))
     * LazyFutureStream.of(1, 2, 3).innerJoin(Seq.of(1, 2), t -> Objects.equals(t.v1, t.v2))
     * }</pre>
     */
    default <T> LazyFutureStream<Tuple2<U, T>> innerJoin(Stream<? extends T> other, BiPredicate<? super U, ? super T> predicate) {
        return fromStream(ReactiveSeq.fromStream(toQueue().stream(getSubscription()))
                                     .innerJoin(other, predicate));

    }

    /**
     * Left outer join 2 streams into one.
     * <p>
     * <pre>
     * {@code
     * // (tuple(1, 1), tuple(2, 2), tuple(3, null))
     * LazyFutureStream.of(1, 2, 3).leftOuterJoin(Seq.of(1, 2), t -> Objects.equals(t.v1, t.v2))
     * }</pre>
     */
    default <T> LazyFutureStream<Tuple2<U, T>> leftOuterJoin(Stream<? extends T> other, BiPredicate<? super U, ? super T> predicate) {
        return fromStream(ReactiveSeq.fromStream(toQueue().stream(getSubscription()))
                                     .leftOuterJoin(other, predicate));
    }

    /**
     * Right outer join 2 streams into one.
     * <p>
     * <pre>
     * {@code
     * // (tuple(1, 1), tuple(2, 2), tuple(null, 3))
     * LazyFutureStream.of(1, 2).rightOuterJoin(Seq.of(1, 2, 3), t -> Objects.equals(t.v1, t.v2))
     * }</pre>
     */
    default <T> LazyFutureStream<Tuple2<U, T>> rightOuterJoin(Stream<? extends T> other, BiPredicate<? super U, ? super T> predicate) {
        return fromStream(ReactiveSeq.fromStream(toQueue().stream(getSubscription()))
                                     .rightOuterJoin(other, predicate));
    }

    /**
     * Create a Stream that infinitely cycles this Stream
     *
     * <pre>
     * {@code
     * assertThat(LazyFutureStream.of(1,2,2).cycle().limit(6)
                                .collect(Collectors.toList()),
                                    equalTo(Arrays.asList(1,2,2,1,2,2));
     * }
     * </pre>
     * @return New cycling stream
     */
    @Override
    default LazyFutureStream<U> cycle() {
        return fromStream(StreamUtils.cycle(this));

    }

    /**
     * Create a Stream that finitely cycles this Stream, provided number of times
     *
     * <pre>
     * {@code
     * assertThat(LazyFutureStream.of(1,2,2).cycle(3)
                                .collect(Collectors.toList()),
                                    equalTo(Arrays.asList(1,2,2,1,2,2,1,2,2)));
     * }
     * </pre>
     * @return New cycling stream
     */
    default LazyFutureStream<U> cycle(int times) {
        return fromStream(StreamUtils.cycle(times, Streamable.fromStream(this)));

    }

    /**
     * Repeat in a Stream while specified predicate holds
     * <pre>
     * {@code
     *  int count =0;
     *
        assertThat(LazyFutureStream.of(1,2,2).cycleWhile(next -> count++<6 )
                                            .collect(Collectors.toList()),equalTo(Arrays.asList(1,2,2,1,2,2)));
     * }
     * </pre>
     * @param predicate
     *            repeat while true
     * @return Repeating Stream
     */
    default LazyFutureStream<U> cycleWhile(Predicate<? super U> predicate) {
        return cycle().limitWhile(predicate);
    }

    /**
     * Repeat in a Stream until specified predicate holds
     *
     * <pre>
     * {@code
     * 	count =0;
        assertThat(LazyFutureStream.of(1,2,2,3).cycleUntil(next -> count++>10 )
                                            .collect(Collectors.toList()),equalTo(Arrays.asList(1, 2, 2, 3, 1, 2, 2, 3, 1, 2, 2)));
    
     * }
     * </pre>
     * @param predicate
     *            repeat while true
     * @return Repeating Stream
     */
    default LazyFutureStream<U> cycleUntil(Predicate<? super U> predicate) {

        return cycle().limitUntil(predicate);
    }

    default IterableFoldable<U> foldable() {
        return this;
    }

    /*
     *	@return Convert to standard JDK 8 Stream
     * @see com.aol.cyclops.react.stream.traits.FutureStream#stream()
     */
    @Override
    default ReactiveSeq<U> stream() {
        return toQueue().stream(getSubscription());
    }

    /*
     *	@return New version of this stream converted to execute asynchronously and in parallel
     * @see com.aol.cyclops.react.stream.traits.FutureStream#parallel()
     */
    default LazyFutureStream<U> parallel() {
        return this.withAsync(true)
                   .withTaskExecutor(LazyReact.parallelBuilder()
                                              .getExecutor());
    }

    /*
     *	@return New version of this stream  converted to execute synchronously and sequentially
     * @see com.aol.cyclops.react.stream.traits.FutureStream#sequential()
     */
    default LazyFutureStream<U> sequential() {
        return this.withAsync(false)
                   .withTaskExecutor(LazyReact.sequentialBuilder()
                                              .getExecutor());
    }

    /*
     * (non-Javadoc)
     *
     * @see org.jooq.lambda.Seq#unordered()
     */
    @Override
    default LazyFutureStream<U> unordered() {
        return this;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.jooq.lambda.Seq#onClose(java.lang.Runnable)
     */
    @Override
    default LazyFutureStream<U> onClose(Runnable closeHandler) {
        getLastActive().stream()
                       .onClose(closeHandler);
        return this;

    }

    /*
     * (non-Javadoc)
     *
     * @see org.jooq.lambda.Seq#sorted()
     */
    @Override
    default LazyFutureStream<U> sorted() {
        return fromStream(ReactiveSeq.fromStream(toQueue().stream(getSubscription()))
                                     .sorted());
    }

    /*
     * (non-Javadoc)
     *
     * @see org.jooq.lambda.Seq#sorted(java.util.Comparator)
     */
    @Override
    default LazyFutureStream<U> sorted(Comparator<? super U> comparator) {
        return fromStream(ReactiveSeq.fromStream(toQueue().stream(getSubscription()))
                                     .sorted(comparator));
    }

    /**
     * Give a consumer access to this Stream
     *
     * @param consumer
     *            Consumer that will recieve current stage
     * @return Self (current stage)
     */
    default LazyFutureStream<U> self(Consumer<LazyFutureStream<U>> consumer) {
        return then((t) -> {
            consumer.accept(this);
            return (U) t;
        });

    }

    /** START SEQUENCEM **/
    /*
     *	@return This Stream with a different type (can be used to narrow or widen)
     * @see com.aol.cyclops.control.ReactiveSeq#unwrap()
     */
    @Override
    default <R> R unwrap() {
        return (R) this;
    }

    /*
     * Flatten this stream one level
     * * <pre>
     * {@code
     *  LazyFutureStream.of(Arrays.asList(1,2))
     *                  .flatten();
     *
     *    // stream of 1,2
     *  }
     *  </pre>
     *
     * @see com.aol.cyclops.control.ReactiveSeq#flatten()
     */
    @Override
    default <T1> LazyFutureStream<T1> flatten() {
        return fromStream(ReactiveSeq.fromStream(toQueue().stream(getSubscription()))
                                     .flatten());

    }

    /* Optional empty, if empty Stream. Otherwise collects to a List
     *	@return this Stream as an Optional
     * @see com.aol.cyclops.control.ReactiveSeq#toOptional()
     */
    @Override
    default Optional<ListX<U>> toOptional() {
        return Optional.of(block())
                       .flatMap(list -> list.size() == 0 ? Optional.<ListX<U>> empty() : Optional.of(list));
    }

    /*
     * <pre>
     * {@code
     * LazyFutureStream.of(1,2,3,4)
                        .toCompletableFuture()
    
        //Future of	[1,2,3,4]
     *
     * }
     * </pre>
     * Future is populated asynchronously using current Streams task executor
     * @return This Stream as a CompletableFuture
     * @see com.aol.cyclops.control.ReactiveSeq#toCompletableFuture()
     */
    @Override
    default CompletableFuture<ListX<U>> toCompletableFuture() {
        return CompletableFuture.completedFuture(this)
                                .thenApplyAsync(s -> s.block(), this.getTaskExecutor());
    }

    /*
     * @see java.util.stream.BaseStream#spliterator()
     */
    @Override
    default Spliterator<U> spliterator() {
        return stream().spliterator();
    }

    /*
     * @see java.util.stream.BaseStream#isParallel()
     */
    @Override
    default boolean isParallel() {
        return false;
    }

    /*
     * @see java.util.stream.Stream#mapToInt(java.util.function.ToIntFunction)
     */
    @Override
    default IntStream mapToInt(ToIntFunction<? super U> mapper) {
        return stream().mapToInt(mapper);
    }

    /*
     * @see java.util.stream.Stream#mapToLong(java.util.function.ToLongFunction)
     */
    @Override
    default LongStream mapToLong(ToLongFunction<? super U> mapper) {
        return stream().mapToLong(mapper);
    }

    /*
     * @see java.util.stream.Stream#mapToDouble(java.util.function.ToDoubleFunction)
     */
    @Override
    default DoubleStream mapToDouble(ToDoubleFunction<? super U> mapper) {
        return stream().mapToDouble(mapper);
    }

    /*
     * @see java.util.stream.Stream#flatMapToInt(java.util.function.Function)
     */
    @Override
    default IntStream flatMapToInt(Function<? super U, ? extends IntStream> mapper) {
        return stream().flatMapToInt(mapper);
    }

    /*
     * @see java.util.stream.Stream#flatMapToLong(java.util.function.Function)
     */
    @Override
    default LongStream flatMapToLong(Function<? super U, ? extends LongStream> mapper) {
        return stream().flatMapToLong(mapper);
    }

    /*
     * @see java.util.stream.Stream#flatMapToDouble(java.util.function.Function)
     */
    @Override
    default DoubleStream flatMapToDouble(Function<? super U, ? extends DoubleStream> mapper) {
        return stream().flatMapToDouble(mapper);
    }

    /*
     * @see java.util.stream.Stream#forEachOrdered(java.util.function.Consumer)
     */
    @Override
    default void forEachOrdered(Consumer<? super U> action) {
        stream().forEachOrdered(action);

    }

    /*
     * @see java.util.stream.Stream#toArray()
     */
    @Override
    default Object[] toArray() {
        return stream().toArray();
    }

    /*
     * @see java.util.stream.Stream#toArray(java.util.function.IntFunction)
     */
    @Override
    default <A> A[] toArray(IntFunction<A[]> generator) {
        return stream().toArray(generator);
    }

    /*
     * @see com.aol.cyclops.control.ReactiveSeq#findAny()
     */
    @Override
    default Optional<U> findAny() {
        return ReactiveSeq.fromStream(stream())
                          .findAny();
    }

    /*
     * @see com.aol.cyclops.control.ReactiveSeq#toSet()
     */
    @Override
    default Set<U> toSet() {
        return collect(Collectors.toSet());
    }

    /*
     * @see com.aol.cyclops.control.ReactiveSeq#toList()
     */
    @Override
    default List<U> toList() {
        return collect(Collectors.toList());
    }

    /*
     * @see com.aol.cyclops.control.ReactiveSeq#toCollection(java.util.function.Supplier)
     */
    @Override
    default <C extends Collection<U>> C toCollection(Supplier<C> collectionFactory) {
        return collect(Collectors.toCollection(collectionFactory));
    }

    /*
     * @see com.aol.cyclops.control.ReactiveSeq#distinct(java.util.function.Function)
     */
    @Override
    default <R> ReactiveSeq<U> distinct(Function<? super U, ? extends R> keyExtractor) {
        return ReactiveSeq.fromStream(stream())
                          .distinct();
    }

    /*
     *	Duplicate the data in this Stream. To duplicate into 2 LazyFutureStreams use actOnFutures#duplicate
     * @see com.aol.cyclops.control.ReactiveSeq#duplicateSequence()
     */
    @Override
    default Tuple2<ReactiveSeq<U>, ReactiveSeq<U>> duplicateSequence() {
        return ReactiveSeq.fromStream(toQueue().stream(getSubscription()))
                          .duplicateSequence();
    }

    /*
     * Triplicate the data in this Stream. To triplicate into 3 LazyFutureStreams use actOnFutures#triplicate
     *
     * @see com.aol.cyclops.control.ReactiveSeq#triplicate()
     */
    @Override
    default Tuple3<ReactiveSeq<U>, ReactiveSeq<U>, ReactiveSeq<U>> triplicate() {
        return ReactiveSeq.fromStream(toQueue().stream(getSubscription()))
                          .triplicate();

    }

    /*
     * Quadruplicate the data in this Stream. To quadruplicate into 3 LazyFutureStreams use actOnFutures#quadruplicate
     * @see com.aol.cyclops.control.ReactiveSeq#quadruplicate()
     */
    @Override
    default Tuple4<ReactiveSeq<U>, ReactiveSeq<U>, ReactiveSeq<U>, ReactiveSeq<U>> quadruplicate() {
        return ReactiveSeq.fromStream(toQueue().stream(getSubscription()))
                          .quadruplicate();

    }

    /*
     * @see com.aol.cyclops.control.ReactiveSeq#splitSequenceAtHead()
     */
    @Override
    default Tuple2<Optional<U>, ReactiveSeq<U>> splitSequenceAtHead() {
        return ReactiveSeq.fromStream(toQueue().stream(getSubscription()))
                          .splitSequenceAtHead();

    }

    /*
     * @see com.aol.cyclops.control.ReactiveSeq#splitAt(int)
     */
    @Override
    default Tuple2<ReactiveSeq<U>, ReactiveSeq<U>> splitAt(int where) {
        return ReactiveSeq.fromStream(toQueue().stream(getSubscription()))
                          .splitAt(where);

    }

    /*
     * @see com.aol.cyclops.control.ReactiveSeq#splitBy(java.util.function.Predicate)
     */
    @Override
    default Tuple2<ReactiveSeq<U>, ReactiveSeq<U>> splitBy(Predicate<U> splitter) {
        return ReactiveSeq.fromStream(toQueue().stream(getSubscription()))
                          .splitBy(splitter);

    }

    /*
     * @see com.aol.cyclops.control.ReactiveSeq#partitionSequence(java.util.function.Predicate)
     */
    @Override
    default Tuple2<ReactiveSeq<U>, ReactiveSeq<U>> partitionSequence(Predicate<U> splitter) {
        return ReactiveSeq.fromStream(toQueue().stream(getSubscription()))
                          .partitionSequence(splitter);

    }

    /*
     * @see com.aol.cyclops.control.ReactiveSeq#cycle(com.aol.cyclops.sequence.Monoid, int)
     */
    @Override
    default LazyFutureStream<U> cycle(Monoid<U> m, int times) {
        return fromStream(ReactiveSeq.fromStream(toQueue().stream(getSubscription()))
                                     .cycle(m, times));

    }

    /*
     * @see com.aol.cyclops.control.ReactiveSeq#zip3(java.util.stream.Stream, java.util.stream.Stream)
     */
    @Override
    default <S, R> LazyFutureStream<Tuple3<U, S, R>> zip3(Stream<? extends S> second, Stream<? extends R> third) {
        return (LazyFutureStream) fromStream(ReactiveSeq.fromStream(toQueue().stream(getSubscription()))
                                                        .zip3(second, third));

    }

    /*
     * @see com.aol.cyclops.control.ReactiveSeq#zip4(java.util.stream.Stream, java.util.stream.Stream, java.util.stream.Stream)
     */
    @Override
    default <T2, T3, T4> LazyFutureStream<Tuple4<U, T2, T3, T4>> zip4(Stream<? extends T2> second, Stream<? extends T3> third,
            Stream<? extends T4> fourth) {
        return fromStream(ReactiveSeq.fromStream(toQueue().stream(getSubscription()))
                                     .zip4(second, third, fourth));

    }

    /*
     * @see com.aol.cyclops.control.ReactiveSeq#zipStream(java.util.stream.BaseStream, java.util.function.BiFunction)
     */
    @Override
    default <S, R> LazyFutureStream<R> zipStream(BaseStream<? extends S, ? extends BaseStream<? extends S, ?>> second,
            BiFunction<? super U, ? super S, ? extends R> zipper) {
        return fromStream(ReactiveSeq.fromStream(toQueue().stream(getSubscription()))
                                     .zipStream(second, zipper));

    }

    /*
     * @see com.aol.cyclops.control.ReactiveSeq#grouped(int)
     */
    @Override
    default LazyFutureStream<ListX<U>> grouped(int groupSize) {
        return fromStream(ReactiveSeq.fromStream(toQueue().stream(getSubscription()))
                                     .grouped(groupSize));
    }

    /*
     * @see com.aol.cyclops.control.ReactiveSeq#scanLeft(com.aol.cyclops.sequence.Monoid)
     */
    @Override
    default LazyFutureStream<U> scanLeft(Monoid<U> monoid) {
        return fromStream(ReactiveSeq.fromStream(toQueue().stream(getSubscription()))
                                     .scanLeft(monoid));
    }

    /*
     * @see com.aol.cyclops.control.ReactiveSeq#toStreamable()
     */
    @Override
    default Streamable<U> toStreamable() {
        return ReactiveSeq.fromStream(toQueue().stream(getSubscription()))
                          .toStreamable();
    }

    /*
     * @see com.aol.cyclops.control.ReactiveSeq#toStream()
     */
    @Override
    default <U> Stream<U> toStream() {
        return (Stream<U>) toQueue().stream(getSubscription());
    }

    /*
     * @see com.aol.cyclops.control.ReactiveSeq#startsWith(java.lang.Iterable)
     */
    @Override
    default boolean startsWithIterable(Iterable<U> iterable) {
        return ReactiveSeq.fromStream(toQueue().stream(getSubscription()))
                          .startsWithIterable(iterable);
    }

    /*
     * @see com.aol.cyclops.control.ReactiveSeq#startsWith(java.util.Iterator)
     */
    @Override
    default boolean startsWith(Stream<U> iterator) {
        return ReactiveSeq.fromStream(toQueue().stream(getSubscription()))
                          .startsWith(iterator);
    }

    /*
     * @see com.aol.cyclops.control.ReactiveSeq#endsWith(java.lang.Iterable)
     */
    @Override
    default boolean endsWithIterable(Iterable<U> iterable) {
        return ReactiveSeq.fromStream(toQueue().stream(getSubscription()))
                          .endsWithIterable(iterable);
    }

    /*
     * @see com.aol.cyclops.control.ReactiveSeq#endsWith(java.util.stream.Stream)
     */
    @Override
    default boolean endsWith(Stream<U> stream) {
        return ReactiveSeq.fromStream(toQueue().stream(getSubscription()))
                          .endsWith(stream);
    }

    /*
     * @see com.aol.cyclops.control.ReactiveSeq#anyM()
     */
    @Override
    default AnyMSeq<U> anyM() {
        return AnyM.fromStream(this);
    }

    /*
     * @see com.aol.cyclops.control.ReactiveSeq#flatMapCollection(java.util.function.Function)
     */
    @Override
    default <R> LazyFutureStream<R> flatMapIterable(Function<? super U, ? extends Iterable<? extends R>> fn) {
        return fromStream(ReactiveSeq.fromStream(toQueue().stream(getSubscription()))
                                     .flatMapIterable(fn));
    }

    /*
     * @see com.aol.cyclops.control.ReactiveSeq#flatMapStream(java.util.function.Function)
     */
    @Override
    default <R> LazyFutureStream<R> flatMapStream(Function<? super U, BaseStream<? extends R, ?>> fn) {
        return fromStream(ReactiveSeq.fromStream(toQueue().stream(getSubscription()))
                                     .flatMapStream(fn));
    }

    /*
     * @see com.aol.cyclops.control.ReactiveSeq#toLazyCollection()
     */
    @Override
    default CollectionX<U> toLazyCollection() {
        return ReactiveSeq.fromStream(toQueue().stream(getSubscription()))
                          .toLazyCollection();
    }

    /*
     * @see com.aol.cyclops.control.ReactiveSeq#toConcurrentLazyCollection()
     */
    @Override
    default CollectionX<U> toConcurrentLazyCollection() {
        return ReactiveSeq.fromStream(toQueue().stream(getSubscription()))
                          .toConcurrentLazyCollection();
    }

    /*
     * @see com.aol.cyclops.control.ReactiveSeq#toConcurrentLazyStreamable()
     */
    @Override
    default Streamable<U> toConcurrentLazyStreamable() {
        return ReactiveSeq.fromStream(toQueue().stream(getSubscription()))
                          .toConcurrentLazyStreamable();
    }

    /*
     * @see com.aol.cyclops.control.ReactiveSeq#appendStream(java.util.stream.Stream)
     */
    @Override
    default LazyFutureStream<U> appendStream(Stream<U> stream) {
        return fromStream(ReactiveSeq.fromStream(toQueue().stream(getSubscription()))
                                     .appendStream(stream));
    }

    /*
     * @see com.aol.cyclops.control.ReactiveSeq#prependStream(java.util.stream.Stream)
     */
    @Override
    default LazyFutureStream<U> prependStream(Stream<U> stream) {
        return fromStream(ReactiveSeq.fromStream(toQueue().stream(getSubscription()))
                                     .prependStream(stream));
    }

    /*
     * @see com.aol.cyclops.control.ReactiveSeq#append(java.lang.Object[])
     */
    @Override
    default LazyFutureStream<U> append(U... values) {
        return fromStream(ReactiveSeq.fromStream(toQueue().stream(getSubscription()))
                                     .append(values));
    }

    /*
     * @see com.aol.cyclops.control.ReactiveSeq#prepend(java.lang.Object[])
     */
    @Override
    default LazyFutureStream<U> prepend(U... values) {
        return fromStream(ReactiveSeq.fromStream(toQueue().stream(getSubscription()))
                                     .prepend(values));
    }

    /*
     * @see com.aol.cyclops.control.ReactiveSeq#insertAt(int, java.lang.Object[])
     */
    @Override
    default LazyFutureStream<U> insertAt(int pos, U... values) {
        return fromStream(ReactiveSeq.fromStream(toQueue().stream(getSubscription()))
                                     .insertAt(pos, values));
    }

    /*
     * @see com.aol.cyclops.control.ReactiveSeq#deleteBetween(int, int)
     */
    @Override
    default LazyFutureStream<U> deleteBetween(int start, int end) {
        return fromStream(ReactiveSeq.fromStream(toQueue().stream(getSubscription()))
                                     .deleteBetween(start, end));
    }

    /*
     * @see com.aol.cyclops.control.ReactiveSeq#insertStreamAt(int, java.util.stream.Stream)
     */
    @Override
    default LazyFutureStream<U> insertStreamAt(int pos, Stream<U> stream) {
        return fromStream(ReactiveSeq.fromStream(toQueue().stream(getSubscription()))
                                     .insertStreamAt(pos, stream));
    }

    /**
     * @return access to asynchronous terminal operations
     */
    default FutureOperations<U> futureOperations() {
        return new LazyFutureStreamFutureOpterationsImpl<>(
                                                           getTaskExecutor(), this);

    }

    /*
     * @see com.aol.cyclops.control.ReactiveSeq#futureOperations(java.util.concurrent.Executor)
     */
    @Override
    default FutureOperations<U> futureOperations(Executor exec) {
        return new LazyFutureStreamFutureOpterationsImpl<>(
                                                           exec, this);

    }

    /*
     * @see com.aol.cyclops.control.ReactiveSeq#skip(long, java.util.concurrent.TimeUnit)
     */
    @Override
    default LazyFutureStream<U> skip(long time, TimeUnit unit) {
        return fromStream(ReactiveSeq.fromStream(toQueue().stream(getSubscription()))
                                     .skip(time, unit));
    }

    /*
     * @see com.aol.cyclops.control.ReactiveSeq#limit(long, java.util.concurrent.TimeUnit)
     */
    @Override
    default LazyFutureStream<U> limit(long time, TimeUnit unit) {
        getSubscription().registerTimeLimit(unit.toNanos(time));
        return fromStream(toQueue().stream(getSubscription())
                                   .limit(time, unit));
    }

    /*
     * @see com.aol.cyclops.control.ReactiveSeq#skipLast(int)
     */
    @Override
    default LazyFutureStream<U> skipLast(int num) {
        return fromStream(ReactiveSeq.fromStream(toQueue().stream(getSubscription()))
                                     .skipLast(num));
    }

    /*
     * @see com.aol.cyclops.control.ReactiveSeq#limitLast(int)
     */
    @Override
    default LazyFutureStream<U> limitLast(int num) {
        return fromStream(ReactiveSeq.fromStream(toQueue().stream(getSubscription()))
                                     .limitLast(num));
    }

    /*
     * @see com.aol.cyclops.control.ReactiveSeq#firstValue()
     */
    @Override
    default U firstValue() {

        return ReactiveSeq.fromStream(toQueue().stream(getSubscription()))
                          .firstValue();
    }

    /*
     * Batch the elements in the Stream by a combination of Size and Time
     * If batch exceeds max size it will be split
     * If batch exceeds max time it will be split
     * Excludes Null values (neccessary for timeout handling)
     *
     * @see com.aol.cyclops.control.ReactiveSeq#batchBySizeAndTime(int, long, java.util.concurrent.TimeUnit, java.util.function.Supplier)
     */
    @Override
    default <C extends Collection<? super U>> LazyFutureStream<C> groupedBySizeAndTime(int size, long time, TimeUnit unit, Supplier<C> factory) {
        return fromStream(ReactiveSeq.fromStream(toQueue().stream(getSubscription()))
                                     .groupedBySizeAndTime(size, time, unit, factory));
        /**         Queue<U> queue = toQueue();
            Function<BiFunction<Long,TimeUnit,U>, Supplier<Collection<U>>> fn = new BatchByTimeAndSize(size,time,unit,factory);
            return (LazyFutureStream)fromStream(queue.streamBatch(getSubscription(), (Function)fn));**/
    }

    /*
     * @see com.aol.cyclops.control.ReactiveSeq#groupedStatefullyWhile(java.util.function.BiPredicate)
     */
    @Override
    default LazyFutureStream<ListX<U>> groupedStatefullyWhile(BiPredicate<ListX<? super U>, ? super U> predicate) {
        return fromStream(ReactiveSeq.fromStream(toQueue().stream(getSubscription()))
                                     .groupedStatefullyWhile(predicate));
    }

    /*
     * @see com.aol.cyclops.control.ReactiveSeq#batchUntil(java.util.function.Predicate)
     */
    @Override
    default LazyFutureStream<ListX<U>> groupedUntil(Predicate<? super U> predicate) {
        return fromStream(ReactiveSeq.fromStream(toQueue().stream(getSubscription()))
                                     .groupedUntil(predicate));
    }

    /*
     * @see com.aol.cyclops.control.ReactiveSeq#batchWhile(java.util.function.Predicate)
     */
    @Override
    default LazyFutureStream<ListX<U>> groupedWhile(Predicate<? super U> predicate) {
        return fromStream(ReactiveSeq.fromStream(toQueue().stream(getSubscription()))
                                     .groupedWhile(predicate));
    }

    /*
     * @see com.aol.cyclops.control.ReactiveSeq#batchWhile(java.util.function.Predicate, java.util.function.Supplier)
     */
    @Override
    default <C extends Collection<? super U>> LazyFutureStream<C> groupedWhile(Predicate<? super U> predicate, Supplier<C> factory) {
        return fromStream(ReactiveSeq.fromStream(toQueue().stream(getSubscription()))
                                     .groupedWhile(predicate, factory));
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Seq#sorted(java.util.function.Function)
     */
    @Override
    default <R extends Comparable<? super R>> LazyFutureStream<U> sorted(Function<? super U, ? extends R> function) {
        return fromStream(ReactiveSeq.fromStream(toQueue().stream(getSubscription()))
                                     .sorted(function));
    }

    /*
     * @see com.aol.cyclops.control.ReactiveSeq#batchUntil(java.util.function.Predicate, java.util.function.Supplier)
     */
    @Override
    default <C extends Collection<? super U>> LazyFutureStream<C> groupedUntil(Predicate<? super U> predicate, Supplier<C> factory) {

        return fromStream(ReactiveSeq.fromStream(toQueue().stream(getSubscription()))
                                     .groupedUntil(predicate, factory));
    }

    /*
     * @see com.aol.cyclops.control.ReactiveSeq#recover(java.util.function.Function)
     */
    @Override
    default LazyFutureStream<U> recover(Function<Throwable, ? extends U> fn) {
        return this.onFail(e -> fn.apply(e.getCause()));

    }

    /*
     * @see com.aol.cyclops.control.ReactiveSeq#recover(java.lang.Class, java.util.function.Function)
     */
    @Override
    default <EX extends Throwable> LazyFutureStream<U> recover(Class<EX> exceptionClass, Function<EX, ? extends U> fn) {
        return this.onFail(exceptionClass, e -> fn.apply((EX) e.getCause()));
    }

    /**
     * Perform a forEach operation over the Stream, without closing it, consuming only the specified number of elements from
     * the Stream, at this time. More elements can be consumed later, by called request on the returned Subscription
     * 
     * e.g.
     * <pre>
     * {@code
     *     Subscription next = LazyFutureStream.of(1,2,3,4)
     *          					    .forEachX(2,System.out::println);
     *          
     *     System.out.println("First batch processed!");
     *     
     *     next.request(2);
     *     
     *      System.out.println("Second batch processed!");
     *      
     *     //prints
     *     1
     *     2
     *     First batch processed!
     *     3
     *     4 
     *     Second batch processed!
     * }
     * </pre>
     * 
     * 
     * @param numberOfElements To consume from the Stream at this time
     * @param consumer To accept incoming events from the Stream
     * @return Subscription so that further processing can be continued or cancelled.
     */
    default <X extends Throwable> Subscription forEachX(long numberOfElements, Consumer<? super U> consumer) {
        val t2 = LazyFutureStreamUtils.forEachX(this, numberOfElements, consumer);
        t2.v2.run();
        return t2.v1.join();
    }

    /**
     * Perform a forEach operation over the Stream  without closing it,  capturing any elements and errors in the supplied consumers, but only consuming 
     * the specified number of elements from the Stream, at this time. More elements can be consumed later, by called request on the returned Subscription 
     * <pre>
     * {@code
     *     Subscription next = LazyFutureStream.of(()->1,()->2,()->{throw new RuntimeException()},()->4)
     *                                  .map(Supplier::get)
     *          					    .forEachXWithError(2,System.out::println, e->e.printStackTrace());
     *          
     *     System.out.println("First batch processed!");
     *     
     *     next.request(2);
     *     
     *      System.out.println("Second batch processed!");
     *      
     *     //prints
     *     1
     *     2
     *     First batch processed!
     *     
     *     RuntimeException Stack Trace on System.err
     *     
     *     4 
     *     Second batch processed!
     * }
     * </pre>	 
     * 
     * 
     * @param numberOfElements To consume from the Stream at this time
     * @param consumer To accept incoming elements from the Stream
     * @param consumerError To accept incoming processing errors from the Stream
     * @return Subscription so that further processing can be continued or cancelled.
     */
    default <X extends Throwable> Subscription forEachXWithError(long numberOfElements, Consumer<? super U> consumer,
            Consumer<? super Throwable> consumerError) {
        val t2 = LazyFutureStreamUtils.forEachXWithError(this, numberOfElements, consumer, consumerError);
        t2.v2.run();
        return t2.v1.join();
    }

    /**
     * Perform a forEach operation over the Stream  without closing it,  capturing any elements and errors in the supplied consumers, but only consuming 
     * the specified number of elements from the Stream, at this time. More elements can be consumed later, by called request on the returned Subscription,
     * when the entire Stream has been processed an onComplete event will be recieved.
     * 
     * <pre>
     * {@code
     *     Subscription next = LazyFurtureStream.of(()->1,()->2,()->{throw new RuntimeException()},()->4)
     *                                  .map(Supplier::get)
     *          					    .forEachXEvents(2,System.out::println, e->e.printStackTrace(),()->System.out.println("the end!"));
     *          
     *     System.out.println("First batch processed!");
     *     
     *     next.request(2);
     *     
     *      System.out.println("Second batch processed!");
     *      
     *     //prints
     *     1
     *     2
     *     First batch processed!
     *     
     *     RuntimeException Stack Trace on System.err
     *     
     *     4 
     *     Second batch processed!
     *     The end!
     * }
     * </pre>	 
     * @param numberOfElements To consume from the Stream at this time
     * @param consumer To accept incoming elements from the Stream
     * @param consumerError To accept incoming processing errors from the Stream
     * @param onComplete To run after an onComplete event
     * @return Subscription so that further processing can be continued or cancelled.
     */
    default <X extends Throwable> Subscription forEachXEvents(long numberOfElements, Consumer<? super U> consumer,
            Consumer<? super Throwable> consumerError, Runnable onComplete) {
        val t2 = LazyFutureStreamUtils.forEachXEvents(this, numberOfElements, consumer, consumerError, onComplete);
        t2.v2.run();
        return t2.v1.join();
    }

    /**
     *  Perform a forEach operation over the Stream    capturing any elements and errors in the supplied consumers,  
     * <pre>
     * {@code
     *     Subscription next = LazyFutureStream.of(()->1,()->2,()->{throw new RuntimeException()},()->4)
     *                                  .map(Supplier::get)
     *          					    .forEachWithError(System.out::println, e->e.printStackTrace());
     *          
     *     System.out.println("processed!");
     *     
     *    
     *      
     *     //prints
     *     1
     *     2
     *     RuntimeException Stack Trace on System.err
     *     4
     *     processed!
     *     
     * }
     * </pre>	 
     * @param consumerElement To accept incoming elements from the Stream
     * @param consumerError To accept incoming processing errors from the Stream
     */
    default <X extends Throwable> void forEachWithError(Consumer<? super U> consumerElement, 
                                                Consumer<? super Throwable> consumerError) {
        val t2 = LazyFutureStreamUtils.forEachWithError(this, consumerElement, consumerError);
        t2.v2.run();
    }

    /**
     * Perform a forEach operation over the Stream  capturing any elements and errors in the supplied consumers
     * when the entire Stream has been processed an onComplete event will be recieved.
     * 
     * <pre>
     * {@code
     *     Subscription next = LazyFutureStream.of(()->1,()->2,()->{throw new RuntimeException()},()->4)
     *                                  .map(Supplier::get)
     *          					    .forEachEvents(System.out::println, e->e.printStackTrace(),()->System.out.println("the end!"));
     *          
     *     System.out.println("processed!");
     *     
     *      
     *     //prints
     *     1
     *     2
     *     RuntimeException Stack Trace on System.err
     *      4 
     *     processed!
     *     
     *     
     * }
     * </pre>	
     * @param consumerElement To accept incoming elements from the Stream
     * @param consumerError To accept incoming processing errors from the Stream
     * @param onComplete To run after an onComplete event
     */
    default <X extends Throwable> void forEachEvent(Consumer<? super U> consumerElement, Consumer<? super Throwable> consumerError,
            Runnable onComplete) {
        val t2 = LazyFutureStreamUtils.forEachEvent(this, consumerElement, consumerError, onComplete);
        t2.v2.run();
    }

    /** END REACTIVESEQ **/

    /**
     * Construct an parallel LazyFutureStream from specified array, using the configured
     * standard parallel thread pool. By default this is the Common ForkJoinPool.
     * To use a different thread pool, the recommended approach is to construct your own LazyReact builder
     *
     * @see ThreadPools#getStandard()
     * @see ThreadPools#setUseCommon(boolean)
     *
     * @param array
     *            Values to react to
     * @return Next SimpleReact stage
     */
    public static <U> LazyFutureStream<U> parallel(U... array) {
        return LazyReact.parallelCommonBuilder()
                        .of(array);
    }

    /**
     *  Create a 'free threaded' asynchronous stream that runs on the supplied CompletableFutures executor service (unless async operator invoked
     *  , in which it will switch to the common 'free' thread executor)
     *  Subsequent tasks will be executed synchronously unless the async() operator is invoked.
     *
     *
     */
    static <T> LazyFutureStream<T> lazyFutureStreamFrom(Stream<CompletableFuture<T>> stream) {
        return new LazyReact(
                             ThreadPools.getSequential()).withRetrier(new AsyncRetryExecutor(
                                                                                             ThreadPools.getSequentialRetry()))
                                                         .withAsync(false)
                                                         .fromStreamFutures(stream);
    }

    /**
     *  Create a 'free threaded' asynchronous stream that runs on the supplied CompletableFutures executor service (unless async operator invoked
     *  , in which it will switch to the common 'free' thread executor)
     *  Subsequent tasks will be executed synchronously unless the async() operator is invoked.
     *
     *
     */
    static <T> LazyFutureStream<T> lazyFutureStream(CompletableFuture<T> value) {
        return new LazyReact(
                             ThreadPools.getSequential()).withRetrier(new AsyncRetryExecutor(
                                                                                             ThreadPools.getSequentialRetry()))
                                                         .withAsync(false)
                                                         .fromStreamFutures(Stream.of(value));
    }

    /**
     *  Create a 'free threaded' asynchronous stream that runs on a single thread (not current)
     *  The supplier will be executed asyncrhonously, subsequent tasks will be executed synchronously unless the async() operator
     *  is invoked.
     *
     *
     */
    static <T> LazyFutureStream<T> lazyFutureStream(CompletableFuture<T>... values) {
        return new LazyReact(
                             ThreadPools.getSequential()).withRetrier(new AsyncRetryExecutor(
                                                                                             ThreadPools.getSequentialRetry()))
                                                         .withAsync(false)
                                                         .fromStreamFutures(Stream.of(values));
    }

    /**
     *  Create a 'free threaded' asynchronous stream that runs on a single thread (not current)
     *  The supplier will be executed asyncrhonously, subsequent tasks will be executed synchronously unless the async() operator
     *  is invoked.
     *
     * @see Stream#of(Object)
     */
    static <T> LazyFutureStream<T> react(Supplier<T> value) {
        return new LazyReact(
                             ThreadPools.getSequential()).withRetrier(new AsyncRetryExecutor(
                                                                                             ThreadPools.getSequentialRetry()))
                                                         .withAsync(false)
                                                         .ofAsync(value);
    }

    /**
     Create a 'free threaded' asynchronous stream that runs on a single thread (not current)
     * The supplier will be executed asyncrhonously, subsequent tasks will be executed synchronously unless the async() operator is invoked.
    
     */
    @SafeVarargs
    static <T> LazyFutureStream<T> react(Supplier<T>... values) {
        return new LazyReact(
                             ThreadPools.getSequential()).withRetrier(new AsyncRetryExecutor(
                                                                                             ThreadPools.getSequentialRetry()))
                                                         .withAsync(false)
                                                         .ofAsync(values);
    }

    /**
     * Create a sequential synchronous stream that runs on the current thread
     *
     */
    static <T> LazyFutureStream<T> of(T value) {
        return lazyFutureStream((Stream) Seq.of(value));
    }

    /**
     * Create a sequential synchronous stream that runs on the current thread
     *
     */
    @SafeVarargs
    static <T> LazyFutureStream<T> of(T... values) {
        return lazyFutureStream((Stream) Seq.of(values));
    }

    /**
     * Create a sequential synchronous stream that runs on a free thread (commonFreeThread executor by default, shared
     * across any instances created in this manner.
     * @see com.aol.cyclops.react.ThreadPools#setUseCommon(boolean)
     *
     */
    static <T> LazyFutureStream<T> freeThread(T value) {
        return (LazyFutureStream) freeThread(new Object[] { value });
    }

    /**
     * Create a sequential synchronous stream that runs on a free thread (commonFreeThread executor by default, shared
     * across any instances created in this manner.
     * @see com.aol.cyclops.react.ThreadPools#setUseCommon(boolean)
     *
     */
    @SafeVarargs
    static <T> LazyFutureStream<T> freeThread(T... values) {
        LazyReact react = new LazyReact(
                                        ThreadPools.getSequential(), RetryBuilder.getDefaultInstance()
                                                                                 .withScheduler(ThreadPools.getSequentialRetry()),
                                        false, new MaxActive(
                                                             1, 1));
        return new LazyFutureStreamImpl<T>(
                                           react, Stream.of(values));

    }

    /**
     * Create a sequential synchronous stream that runs on the current thread
     */
    static <T> LazyFutureStream<T> empty() {
        return lazyFutureStream((Stream) Seq.empty());
    }

    /**
     * @see Stream#iterate(Object, UnaryOperator)
     */
    static <T> LazyFutureStream<T> iterate(final T seed, final UnaryOperator<T> f) {
        return lazyFutureStream((Stream) Seq.iterate(seed, f));
    }

    /**
     * Generate an infinite Stream of null values that runs on the current thread
     * @see Stream#generate(Supplier)
     */
    static LazyFutureStream<Void> generate() {
        return generate(() -> null);
    }

    /**
     * Generate an infinite Stream of given value that runs on the current thread
     * @see Stream#generate(Supplier)
     */
    static <T> LazyFutureStream<T> generate(T value) {
        return generate(() -> value);
    }

    /**
     * Generate an infinite Stream of value returned from Supplier that runs on the current thread
     * @see Stream#generate(Supplier)
     */
    static <T> LazyFutureStream<T> generate(Supplier<T> s) {
        return lazyFutureStream(Stream.generate(s));
    }

    /**
     * Wrap a Stream into a FutureStream that runs on the current thread
     */
    static <T> LazyFutureStream<T> lazyFutureStream(Stream<T> stream) {
        if (stream instanceof LazyFutureStream)
            return (LazyFutureStream<T>) stream;
        if (stream instanceof LazyFutureStream)
            stream = ((LazyFutureStream) stream).toQueue()
                                                .stream(((LazyFutureStream) stream).getSubscription());
        LazyReact react = new LazyReact(
                                        ThreadPools.getCurrentThreadExecutor(), RetryBuilder.getDefaultInstance()
                                                                                            .withScheduler(ThreadPools.getSequentialRetry()),
                                        false, new MaxActive(
                                                             1, 1));
        return new LazyFutureStreamImpl<T>(
                                           react, stream);

    }

    /**
     * Wrap an Iterable into a FutureStream that runs on the current thread
     */
    static <T> LazyFutureStream<T> lazyFutureStreamFromIterable(Iterable<T> iterable) {
        return lazyFutureStream(iterable.iterator());
    }

    /**
     * Wrap an Iterator into a FutureStream that runs on the current thread
     */
    static <T> LazyFutureStream<T> lazyFutureStream(Iterator<T> iterator) {
        return lazyFutureStream(StreamSupport.stream(spliteratorUnknownSize(iterator, ORDERED), false));
    }

}
