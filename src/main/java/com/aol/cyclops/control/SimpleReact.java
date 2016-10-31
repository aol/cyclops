package com.aol.cyclops.control;

import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Supplier;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.reactivestreams.Publisher;

import com.aol.cyclops.internal.react.SimpleReactStreamImpl;
import com.aol.cyclops.internal.react.stream.ReactBuilder;
import com.aol.cyclops.react.RetryBuilder;
import com.aol.cyclops.react.ThreadPools;
import com.aol.cyclops.types.futurestream.SimpleReactStream;
import com.aol.cyclops.types.stream.reactive.SeqSubscriber;
import com.nurkiewicz.asyncretry.RetryExecutor;

import lombok.Getter;
import lombok.experimental.Builder;
import lombok.experimental.Wither;

/**
 * 
 * Builder class for SimpleReact Stream types
 * 
 * SimpleReact streams are finite eager parallel Streams with a concise API.
 * Awesome for loading discrete batches of files or remote resources.
 * Useful for doing shortcircuiting querying operations against remote services.
 *
 * Confgure 
 *      Executors
 *      Parallelism / concurrent tasks
 *      Caching
 *      Object pooling
 *
 
   E.g implementing a Quorum
 * <pre>
 * {@code 
 *   new SimpleReact().react(this:query,this:query,this:query,this:query)
                .then(this:process)
                .block(status -> status.getAllCompleted() >2 && status.getElapsedMillis()>200);

     //short circuit if 2 results after 200ms 
 * 
 * 
 * }
 * </pre>
 * 
 * E.g. loading files
 * <pre>
 * {@code 
 *   List<File> files;
 *   
 *   new SimpleReact().from(files)
 *                    .thenAsync(FileUtils::load)
                      .then(this:process)
                      .block();

 * 
 * 
 * }
 * </pre>
 *  
 *  In general if you have a small discrete data sets SimpleReact may be a fit.
 *  If you need infinite  / continuous Stream processing & more advanced features use LazyReact
 *  (Even flatMap is relatively limited in SimpleReact Stream - for advanced operations @see LazyReact)
 * 
 * @author johnmcclean
 *
 */
@Builder
@Wither
public class SimpleReact implements ReactBuilder {
    @Getter
    private final Executor queueService;
    @Getter
    private final Executor executor;
    @Getter
    private final RetryExecutor retrier;

    private final Boolean async;

    public <U> SimpleReactStream<U> construct(final Stream s) {
        return new SimpleReactStreamImpl<U>(
                                            this, s);
    }

    /**
     * Construct a SimpleReact builder using standard thread pool.
     * By default, unless ThreadPools is configured otherwise this will be sized
     * to the available processors
     * 
     * @see ThreadPools#getStandard()
     */
    public SimpleReact() {
        this(ThreadPools.getStandard());
    }

    /**
     * Construct a SimpleReact builder from the provided Executor, Retrier.
     * 
     * @param executor Task executor to execute tasks on
     * @param retrier Retrier to use for asyncrhonous retry
     * @param async If false, subsequent tasks are executed on the completing thread
     *              If true each subsequent task is resubmitted to a task executor,
     */
    public SimpleReact(final Executor executor, final RetryExecutor retrier, final Boolean async) {
        queueService = ThreadPools.getQueueCopyExecutor();
        this.executor = Optional.ofNullable(executor)
                                .orElse(new ForkJoinPool(
                                                         Runtime.getRuntime()
                                                                .availableProcessors()));
        this.retrier = retrier;

        this.async = Optional.ofNullable(async)
                             .orElse(true);
    }

    /**
     * @param executor Executor this SimpleReact instance will use to execute concurrent tasks.
     */
    public SimpleReact(final Executor executor) {
        queueService = ThreadPools.getQueueCopyExecutor();
        this.executor = executor;
        retrier = null;

        async = true;
    }

    /**
     * Construct a SimpleReact builder from the provided Executor, Retrier.
     * 
     * @param executor Task executor to execute tasks on
     * @param retrier Retrier to use for asyncrhonous retry
     */
    public SimpleReact(final Executor executor, final RetryExecutor retrier) {
        queueService = ThreadPools.getQueueCopyExecutor();
        this.executor = executor;
        this.retrier = retrier;

        async = true;
    }

    /**
     * 
     * @param executor Task executor to execute tasks on
     * @param retrier
     * @param queueCopier Task executor to transfer results during flatMap operations
     */
    public SimpleReact(final Executor executor, final RetryExecutor retrier, final Executor queueCopier) {
        queueService = ThreadPools.getQueueCopyExecutor();
        this.executor = executor;
        this.retrier = retrier;

        async = true;
    }

    public SimpleReact withQueueCopyExecutor(final Executor queueCopyExecutor) {
        return new SimpleReact(
                               executor, retrier, queueCopyExecutor);
    }

    /**
     * 
     * Start a reactive dataflow with a list of one-off-suppliers
     * 
     * @param actions
     *           Stream of Suppliers to provide data (and thus events) that
     *            downstream jobs will react too
     * @return Next stage in the reactive flow
     */
    public <U> SimpleReactStream<U> fromStreamAsync(final Stream<? extends Supplier<U>> actions) {

        return new SimpleReactStreamImpl<U>(
                                            this, actions.map(next -> CompletableFuture.supplyAsync(next, executor)));

    }

    /**
     * Construct a SimpleReactStream from an Publisher
     * 
     * @param publisher
     *            to construct SimpleReactStream from
     * @return SimpleReactStream
     */
    public <T> SimpleReactStream<T> fromPublisher(final Publisher<? extends T> publisher) {
        Objects.requireNonNull(publisher);
        final SeqSubscriber<T> sub = SeqSubscriber.subscriber();
        publisher.subscribe(sub);
        return sub.toSimpleReact(this);
    }

    /**
     * 
     * Start a reactive dataflow with a list of one-off-suppliers
     * 
     * @param actions
     *           Iterator over Suppliers to provide data (and thus events) that
     *            downstream jobs will react too
     * @return Next stage in the reactive flow
     */
    public <U> SimpleReactStream<U> fromIteratorAsync(final Iterator<? extends Supplier<U>> actions) {

        return new SimpleReactStreamImpl<U>(
                                            this, StreamSupport.stream(Spliterators.spliteratorUnknownSize(actions, Spliterator.ORDERED), false)
                                                               .map(next -> CompletableFuture.supplyAsync(next, executor)));

    }

    /**
     * 
     * Start a reactive dataflow with a list of one-off-suppliers
     * 
     * @param actions
     *           Stream of Suppliers to provide data (and thus events) that
     *            downstream jobs will react too
     * @return Next stage in the reactive flow
     */
    public <U> SimpleReactStream<U> fromIterableAsync(final Iterable<? extends Supplier<U>> actions) {

        return new SimpleReactStreamImpl<U>(
                                            this,
                                            StreamSupport.stream(Spliterators.spliteratorUnknownSize(actions.iterator(), Spliterator.ORDERED), false)
                                                         .map(next -> CompletableFuture.supplyAsync(next, executor)));

    }

    /**
     * 
     * Start a reactive dataflow with an array of one-off-suppliers
     * 
     * @param actions Array of Suppliers to provide data (and thus events) that
     *            downstream jobs will react too
     * @return Next stage in the reactive flow
     */
    @SafeVarargs
    public final <U> SimpleReactStream<U> ofAsync(final Supplier<U>... actions) {

        return reactI(actions);

    }

    /**
     * This internal method has been left protected, so it can be mocked / stubbed as some of the entry points are final
     * 
     */
    @SafeVarargs
    private final <U> SimpleReactStream<U> reactI(final Supplier<U>... actions) {

        return new SimpleReactStreamImpl<U>(
                                            this, Stream.of(actions)
                                                        .map(next -> CompletableFuture.supplyAsync(next, executor)));

    }

    /**
     * Start a reactive dataflow from a stream.
     * 
     * @param stream that will be used to drive the reactive dataflow
     * @return Next stage in the reactive flow
     */
    public <U> SimpleReactStream<U> from(final Stream<U> stream) {

        final Stream s = stream.map(it -> CompletableFuture.completedFuture(it));
        return construct(s);
    }

    /**
     * Start a reactive flow from a Collection using an Iterator
     * 
     * @param collection - Collection SimpleReact will iterate over at the start of the flow
     *
     * @return Next stage in the reactive flow
     */
    @SuppressWarnings("unchecked")
    public <R> SimpleReactStream<R> from(final Collection<R> collection) {
        return from(collection.stream());
    }

    public boolean isAsync() {
        return async;
    }

    /**
     * @return  An Eager SimpleReact instance 
     *  @see SimpleReact#SimpleReact()
     */
    public static SimpleReact parallelBuilder() {
        return new SimpleReact();
    }

    /**
     * Construct a new SimpleReact builder, with a new task executor and retry executor
     * with configured number of threads 
     * 
     * @param parallelism Number of threads task executor should have
     * @return eager SimpleReact instance
     */
    public static SimpleReact parallelBuilder(final int parallelism) {
        return SimpleReact.builder()
                          .executor(new ForkJoinPool(
                                                     parallelism))
                          .async(true)
                          .retrier(new RetryBuilder().parallelism(parallelism))
                          .build();
    }

    /**
     * @return new eager SimpleReact builder configured with standard parallel executor
     * By default this is the ForkJoinPool common instance but is configurable in the ThreadPools class
     * 
     * @see ThreadPools#getStandard()
     * see RetryBuilder#getDefaultInstance()
     */
    public static SimpleReact parallelCommonBuilder() {
        return SimpleReact.builder()
                          .executor(ThreadPools.getStandard())
                          .async(true)
                          .retrier(RetryBuilder.getDefaultInstance()
                                               .withScheduler(ThreadPools.getCommonFreeThreadRetry()))
                          .build();

    }

    /**
     * @return new eager SimpleReact builder configured to run on a separate thread (non-blocking current thread), sequentially
     * New ForkJoinPool will be created
     */
    public static SimpleReact sequentialBuilder() {
        return SimpleReact.builder()
                          .async(false)
                          .executor(new ForkJoinPool(
                                                     1))
                          .retrier(RetryBuilder.getDefaultInstance()
                                               .withScheduler(Executors.newScheduledThreadPool(1)))
                          .build();
    }

    /**
     * @return new eager SimpleReact builder configured to run on a separate thread (non-blocking current thread), sequentially
     * Common free thread Executor from
     */
    public static SimpleReact sequentialCommonBuilder() {
        return SimpleReact.builder()
                          .async(false)
                          .executor(ThreadPools.getCommonFreeThread())
                          .retrier(RetryBuilder.getDefaultInstance()
                                               .withScheduler(ThreadPools.getCommonFreeThreadRetry()))
                          .build();
    }

    public SimpleReactStream<Integer> range(final int startInclusive, final int endExclusive) {
        return from(IntStream.range(startInclusive, endExclusive));
    }

    /**
     * Start a reactive flow from a JDK Iterator
     * 
     * @param iterator SimpleReact will iterate over this iterator concurrently to start the reactive dataflow
     * @return Next stage in the reactive flow
     */
    @SuppressWarnings("unchecked")
    public <U> SimpleReactStream<U> from(final Iterator<U> iterator) {
        return from(StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED), false));

    }

    /**
     * Start a reactive flow from a JDK Iterator
     * 
     * @param iter SimpleReact will iterate over this iterator concurrently to start the reactive dataflow
     * @return Next stage in the reactive flow
     */
    @SuppressWarnings("unchecked")
    public <U> SimpleReactStream<U> fromIterable(final Iterable<U> iter) {
        return this.from(StreamSupport.stream(Spliterators.spliteratorUnknownSize(iter.iterator(), Spliterator.ORDERED), false));

    }

    /**
     * Start a reactive dataflow from a stream of CompletableFutures.
     * 
     * @param stream of CompletableFutures that will be used to drive the reactive dataflow
     * @return Next stage in the reactive flow
     */
    public <U> SimpleReactStream<U> fromStream(final Stream<CompletableFuture<U>> stream) {

        final Stream s = stream;
        return construct(s);
    }

    /**
     * Start a reactive dataflow from a stream.
     * 
     * @param stream that will be used to drive the reactive dataflow
     * @return Next stage in the reactive flow
     */
    public <U> SimpleReactStream<Integer> from(final IntStream stream) {

        return from(stream.boxed());

    }

    /**
     * Start a reactive dataflow from a stream.
     * 
     * @param stream that will be used to drive the reactive dataflow
     * @return Next stage in the reactive flow
     */
    public <U> SimpleReactStream<Double> from(final DoubleStream stream) {

        return from(stream.boxed());

    }

    /**
     * Start a reactive dataflow from a stream.
     * 
     * @param stream that will be used to drive the reactive dataflow
     * @return Next stage in the reactive flow
     */
    public <U> SimpleReactStream<Long> from(final LongStream stream) {

        return from(stream.boxed());

    }

    public <U> SimpleReactStream<U> of(final U... array) {
        return from(Stream.of(array));
    }

    public <U> SimpleReactStream<U> from(final CompletableFuture<U> cf) {
        return this.construct(Stream.of(cf));
    }

    /**
     * Construct a simpleReactStream from an Array of CompletableFutures
     * 
     * @param cf CompletableFutures to turn into a Stream
     * @return SimpleReactStream from an Array of CompletableFutures
     */
    public <U> SimpleReactStream<U> from(final CompletableFuture<U>... cf) {
        return this.construct(Stream.of(cf));
    }

    public SimpleReact(final Executor queueService, final Executor executor, final RetryExecutor retrier, final Boolean async) {
        super();
        this.queueService = Optional.ofNullable(queueService)
                                    .orElse(ThreadPools.getQueueCopyExecutor());
        this.executor = Optional.ofNullable(executor)
                                .orElse(ThreadPools.getCurrentThreadExecutor());
        this.retrier = retrier;
        this.async = Optional.ofNullable(async)
                             .orElse(true);
    }

}