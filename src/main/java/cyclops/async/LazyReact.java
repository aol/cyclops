package cyclops.async;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import cyclops.stream.FutureStream;
import cyclops.stream.ReactiveSeq;
import cyclops.stream.Spouts;
import org.reactivestreams.Publisher;

import com.aol.cyclops2.internal.react.FutureStreamImpl;
import com.aol.cyclops2.internal.react.stream.InfiniteClosingSpliteratorFromSupplier;
import com.aol.cyclops2.internal.react.stream.ReactBuilder;
import com.aol.cyclops2.react.RetryBuilder;
import com.aol.cyclops2.react.ThreadPools;
import com.aol.cyclops2.react.async.subscription.Subscription;
import com.aol.cyclops2.react.collectors.lazy.MaxActive;
import cyclops.function.Cacheable;
import com.nurkiewicz.asyncretry.AsyncRetryExecutor;
import com.nurkiewicz.asyncretry.RetryExecutor;

import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Builder;
import lombok.experimental.Wither;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * Builder class for LazyFutureStreams
 *
 * Confgure 
 *      Executors
 *      Parallelism / concurrent tasks
 *      Caching
 *      Object pooling
 *
 *<pre>
 *{@code 
 * new LazyReact(Executors.newFixedThreadPool(4)).of(6,5,2,1)
                                                      .map(this::loadData)
                                                      .map(e->e*100)
                                                      .filter(e->e<551)
                                                      .peek(e->{
                                                          System.out.println("e is " + e 
                                                                              + " on thread " 
                                                                              + Thread.currentThread().getId());
                                                      })
                                                      .runOnCurrent();
 *
 *}
 *</pre>
 *
 *  
 * 
 * @author johnmcclean
 *
 */

@Builder
@Wither
@ToString
public class LazyReact implements ReactBuilder {

    @Getter
    private final Executor executor;
    @Getter
    private final RetryExecutor retrier;

    private final Boolean async;
    @Getter
    private final MaxActive maxActive;

    @Getter
    private final boolean streamOfFutures;
    @Getter
    private final boolean poolingActive;
    @Getter
    private final boolean autoOptimize;
    @Getter
    private final boolean autoMemoize;
    @Getter
    private final Cacheable<?> memoizeCache;



    /**
     * Turn automatic caching of values on for the FutureStream to be generated
     * by this Stream builder
     * 
     * <pre>
     * {@code 
     *  Map cache = new ConcurrentHashMap<>();
        LazyReact builder = new LazyReact().autoMemoizeOn((key,fn)-> cache.computeIfAbsent(key,fn));
        Set<Integer> result = builder.of(1,1,1,1)
                                     .capture(e->e.printStackTrace())
                                     .map(i->calc(i))
                                     .peek(System.out::println)
                                     .toSet();
     * 
     * 
     * }</pre>
     * 
     * 
     * @param memoizeCache Cacheable instance that controls memoization (Caching)
     * @return LazyReact Stream builder
     */
    public LazyReact autoMemoizeOn(final Cacheable<?> memoizeCache) {
        return withAutoMemoize(true).withMemoizeCache(memoizeCache);
    }

    /* 
     * The async flag determines whether, on completion, a Future executes the next task
     * synchronously on it's current thread or redistributes it back to a task executor
     * to handle execution.  
     * E.g. if async is false, subsequent tasks will be executed on the calling thread.
     *      if async is true, a task executor will be used to determine the executing thread.
     * 
     * 
     *  {@see LazyReact#autoOptimize}
     *  
     *	@return true if async
     *  
     */
    public boolean isAsync() {
        return async;
    }

    /**
     * Construct a LazyReact builder using standard thread pool.
     * By default, unless ThreadPools is configured otherwise this will be sized
     * to the available processors
     * 
     * ThreadPools#getStandard is used to determine task executor for parallel task execution
     * 
     * @see ThreadPools#getStandard()
     */
    public LazyReact() {

        this(ThreadPools.getStandard());

    }

    /**
     * Construct a LazyReact builder with provided Executor
     * 
     * @param executor Executor to use
     */
    public LazyReact(final Executor executor) {

        this.executor = executor;
        retrier = null;
        async = true;
        maxActive = MaxActive.IO;

        streamOfFutures = false;
        poolingActive = false;
        autoOptimize = true;
        autoMemoize = false;
        memoizeCache = null;

    }

    /**
     * @param maxActive Max active Future Tasks
     * @param executor  Executor to use
     */
    public LazyReact(final int maxActive, final Executor executor) {

        this.executor = executor;
        retrier = null;
        async = true;
        this.maxActive = MaxActive.IO;

        streamOfFutures = false;
        poolingActive = false;
        autoOptimize = true;
        autoMemoize = false;
        memoizeCache = null;

    }

    /**
     * LazyReact builder with a new TaskExecutor with threads determined by threadPoolSize
     * Max concurrent tasks is determined by concurrency
     * 
     * @param threadPoolSize
     * @param maxActiveTasks
     */
    public LazyReact(final int threadPoolSize, final int maxActiveTasks) {

        executor = Executors.newFixedThreadPool(threadPoolSize);
        retrier = new RetryBuilder().parallelism(threadPoolSize);
        async = true;
        maxActive = new MaxActive(
                maxActiveTasks, threadPoolSize);

        streamOfFutures = false;
        poolingActive = false;
        autoOptimize = true;
        autoMemoize = false;
        memoizeCache = null;
    }

    /**
     * Construct a FutureStream containing a single Future
     * 
     * @param cf CompletableFuture to create Stream from
     * @return FutureStream of a single value
     */
    public <U> FutureStream<U> from(final CompletableFuture<U> cf) {

        return this.constructFutures(Stream.of(cf));

    }

    /**
     * Construct a FutureStream from an array of CompletableFutures
     * 
     * <pre>
     * {@code 
     *  CompletableFuture<String> future1 = CompletableFuture.supplyAsync(()->"hello");
        CompletableFuture<String> future2 = CompletableFuture.supplyAsync(()->"hello2");
        new LazyReact().from(future1,future2)
                       .peek(System.out::println)
                       .transform(action->"result")
                       .toList()
                       .size()
        //2               
     * 
     * }
     * </pre>
     * 
     * @param cf Array of Futures to construct Stream from
     * @return FutureStream from array of Futures
     */
    public <U> FutureStream<U> from(final CompletableFuture<U>... cf) {
        return this.constructFutures(Stream.of(cf));

    }

    /* 
     * Construct a new Stream from another Stream
     * 
     *	@param s Stream to copy
     *	@param org ignored for LazyFutureStreams
     *	@return
     * @see com.aol.cyclops2.react.reactiveStream.BaseSimpleReact#construct(java.util.reactiveStream.Stream, java.util.List)
     */
    public <U> FutureStream<U> construct(final Stream<U> s) {

        return new FutureStreamImpl<U>(
                                           this, s);

    }

    /**
     * Construct a FutureStream from a Stream of CompletableFutures
     * 
     * @param s Stream of CompletableFutures
     * @return FutureStream
     */
    public <U> FutureStream<U> constructFutures(final Stream<CompletableFuture<U>> s) {
        final LazyReact toUse = withStreamOfFutures(true);
        return toUse.construct((Stream<U>) s);
    }

    /**
     * Turn objectPooling on for any Streams created by the returned LazyReact builder
     * This improves performance for Streams with very large numbers of elements, by reusing
     * Future instances. By default Object Pooling is Off.
     * <pre>
     * {@code 
     *  return new LazyReact()
    					.objectPoolingOn()
    					.range(0,5_000_000_000)
    					.map(this::process)
    					.forEach(System.out::println);
       }
       </pre>
     * @return New LazyReact builder with Object pooling on.
     */
    public LazyReact objectPoolingOn() {
        return withPoolingActive(true);
    }

    /**
     * Turn objectPooling off for any Streams created by the returned LazyReact builder. By default Object Pooling is Off.
     * 
     * <pre>
     * {@code 
     * 	LazyReact react; 
     *  
     *    react.objectPoolingOff()
    					.range(0,5_000)
    					.map(this::process)
    					.forEach(System.out::println);
     * }
     * </pre>
     * 
     * @return New LazyReact builder with Object pooling off.
     */
    public LazyReact objectPoolingOff() {
        return withPoolingActive(false);
    }

    /**
     * Turn on automatic threading optimization. Tasks will be 'fanned' out across threads initially
     * and subsequent task completion events will trigger further processing on the same thread. Where
     * operations require working on the results of multiple tasks, data will be forwarded to a Queue, data
     * read from the queue will transform also be 'fanned' out for processing across threads (with subsequent events
     *  again occuring on the same thread). This is equivalent to optimal use of the async() and sync() operators
     * on a Stream. autoOptimize overrides direct calls to sync() and async() on the Stream.
     * By default autoOptimize is On.
     * 
     * <pre>
     * {@code 
     * new LazyReact().autoOptimizeOn()
     *                  .range(0, 1_000_000)
    					.map(i->i+2)
    					.map(i->Thread.currentThread().getId())
    					.peek(System.out::println)
    					.runOnCurrent();
     * }
     * </pre>
     * @return
     */
    public LazyReact autoOptimizeOn() {
        return withAutoOptimize(true);
    }

    /**
     * Turn off automatic threading management. This allows use async() and sync() to control fan out directly in a FutureStream
     * By default autoOptimize is On.
     * 
     *  <pre>
     * {@code 
     * 	LazyReact react; 
     *  
     *    react.autoOptimizeOff()
    				    .range(0, 1_000_000)
    					.map(i->i+2)
    					.map(i->Thread.currentThread().getId())
    					.peek(System.out::println)
    					.runOnCurrent();
     * }
     * </pre>
     * 
     * @return
     */
    public LazyReact autoOptimizeOff() {
        return withAutoOptimize(false);
    }

    /**
     * Start any created Streams in asyncrhonous mode - that is tasks will be submited to an Executor to be run.
     * 
     * <pre>
     * {@code 
     * Turn async on
     * LazyReact.sequentialBuilder()
     *          .withMaxActive(MaxActive.IO)
     *          .async()
                .generateAsync(()->1)
                .limit(1_000_000);
                
                
     * 
     * }
     * </pre>
     * 
     * @return LazyReact that creates Streams in async mode
     */
    public LazyReact async() {
        return withAsync(true);
    }

    /**
     * Start any created Streams in syncrhonous mode - that is tasks will be executed on the calling thread
     * 
     * @return LazyReact that creates Streams in sync mode
     */
    public LazyReact sync() {
        return withAsync(false);
    }

    /**
     * Construct a FutureStream from an Publisher
     * 
     * <pre>
     * {@code 
     *    new LazyReact().fromPublisher(Flux.just(1,2,3)).toList();
     * 
     * }
     * </pre>
     * 
     * @param publisher
     *            to construct FutureStream from
     * @return FutureStream
     */
    public <T> FutureStream<T> fromPublisher(final Publisher<? extends T> publisher) {
        Objects.requireNonNull(publisher);
        Publisher<T> narrowed = (Publisher<T>)publisher;
        return Spouts.from(narrowed).toFutureStream(this);
    }

    /* 
     * Generate an FutureStream that is a range of Integers
     * 
     * <pre>
     * {@code 
     * new LazyReact().range(0,Integer.MAX_VALUE)
                    .limit(100)
                    .peek(v->value=v)
                    .peek(v->latch.countDown())
                    .peek(System.out::println)
                    .hotStream(exec)
                    .connect()
                    .limit(100)
                    .futureOperations(ForkJoinPool.commonPool())
                    .forEach(System.out::println);
     * 
     * }
     * </pre>
     * 
     *	@param startInclusive Start of range 
     *	@param endExclusive End of range
     *	@return FutureStream that is a range of Integers
     * @see com.aol.cyclops2.react.reactiveStream.BaseSimpleReact#range(int, int)
     */
    public FutureStream<Integer> range(final int startInclusive, final int endExclusive) {
        return fromStream(ReactiveSeq.range(startInclusive, endExclusive));
    }

    /* 
     * Construct a FutureStream from the provided Stream of completableFutures
     * 
     * <pre>
     * {@code 
     * 
     * CompletableFuture<List<String>> query(String string);
     * 
     * List<String> titles = new LazyReact().fromStreamFutures(Stream.of(query("Hello, world!")))
                                            .flatMap(Collection::reactiveStream)
                                            .peek(System.out::println)
                                            .<String>transform(url -> getTitle(url))
                                            .filter(Objects::nonNull)
                                            .limit(5)
                                            .peek(title -> saveTitle(title) )
                                            .peek(System.out::println)
                                            .block();
     * 
     * 
     * }
     * </pre>
     * 
     * 
     *	@param reactiveStream Stream that serves as input to FutureStream
     *	@return FutureStream
     * @see com.aol.cyclops2.react.reactiveStream.BaseSimpleReact#fromStream(java.util.reactiveStream.Stream)
     */
    public <U> FutureStream<U> fromStreamFutures(final Stream<CompletableFuture<U>> stream) {

        return constructFutures(stream);
    }

    /* 
     * Create a steam from provided Suppliers, e.g. Supplier will be executed asynchronously
     * 
     * <pre>
     * {@code 
     * 
     *  LazyReact.parallelCommonBuilder()
     *           .ofAsync(() -> loadFromDb(),() -> loadFromService1(),
                                                        () -> loadFromService2())
                 .map(this::convertToStandardFormat)
                 .peek(System.out::println)
                 .map(this::saveData)
                 .block();
     * 
     * }
     * </pre>
     * 
     * 
     *	@param actions Supplier Actions
     *	@return
     * @see com.aol.cyclops2.react.reactiveStream.BaseSimpleReact#react(java.util.function.Supplier[])
     */
    @SafeVarargs
    public final <U> FutureStream<U> ofAsync(final Supplier<U>... actions) {

        return reactI(actions);

    }

    /* 
     *  Construct a FutureStream from the provided Stream, Stream will be mapped to a Stream of CompeltableFutures internally
     * 
     * <pre>
     * {@code 
     * new LazyReact(100,110).fromStream(Files.walk(Paths.get(".")))
                             .map(d->{ throw new RuntimeException("hello");})
                             .map(Object::toString)
                             .recover(e->"hello world")
                             .forEach(System.out::println);
     * 
     * }
     * </pre>
     * 
     * 
     *	@param reactiveStream Stream that serves as input to FutureStream
     *	@return FutureStream
     * @see com.aol.cyclops2.react.reactiveStream.BaseSimpleReact#fromStreamWithoutFutures(java.util.reactiveStream.Stream)
     */
    public <U> FutureStream<U> fromStream(final Stream<U> stream) {

        return construct(stream);
    }



    /* 
     * 
     * Construct a FutureStream from specified Suppliers. Each Supplier is executed asyncrhonously,
     * and it's results provided to next phase of the Stream
     * 
     * <pre>
     * {@code 
     *   LazyReact.parallelBuilder()
     *            .react(asList(this::load)
                  .map(list -> 1 + 2)
                  .block();
     * 
     * }
     * </pre>
     * 
     *	@param actions Suppliers to execute
     *	@return FutureStream
     * @see com.aol.cyclops2.react.reactiveStream.BaseSimpleReact#react(java.util.List)
     */
    public <U> FutureStream<U> react(final Collection<Supplier<U>> actions) {

        final ReactiveSeq<Supplier<U>> seq = actions instanceof List ? ReactiveSeq.fromList((List) actions) : ReactiveSeq.fromIterable(actions);
        return fromStreamAsync(seq);
    }

    @SafeVarargs
    private final <U> FutureStream<U> reactI(final Supplier<U>... actions) {

        return constructFutures(Stream.of(actions)
                                      .map(next -> CompletableFuture.supplyAsync(next, getExecutor())));
    }

    /**
     * @param executor Task Executor for concurrent tasks
     * @param retrier Async Retrier
     * @param async If true each task will be submitted to an executor service
     */
    public LazyReact(final Executor executor, final RetryExecutor retrier, final Boolean async, final MaxActive maxActive,
            final boolean streamOfFutures, final boolean objectPoolingActive, final boolean autoOptimize, final boolean autoMemoize,
            final Cacheable memoizeCache) {
        super();
        this.executor = executor;
        this.retrier = retrier;
        this.async = Optional.ofNullable(async)
                             .orElse(true);
        this.maxActive = Optional.ofNullable(maxActive)
                                 .orElse(MaxActive.IO);
        this.streamOfFutures = streamOfFutures;

        poolingActive = objectPoolingActive;
        this.autoOptimize = autoOptimize;
        this.autoMemoize = autoMemoize;
        this.memoizeCache = memoizeCache;

    }

    /**
     * @param executor Task Executor for concurrent tasks
     * @param retrier Async Retrier
     * @param async If true each task will be submitted to an executor service
     * @param maxActive2 Max Active Future Tasks
     */
    public LazyReact(final Executor executor, final AsyncRetryExecutor retrier, final boolean async, final MaxActive maxActive2) {
        this(executor, retrier, async, maxActive2, false, false, async, false, null);
    }

    /* 
     * Build an FutureStream from the supplied iterable
     * 
     * <pre>
     * {@code 
     * List<Integer> list= new ArrayList<>();
        for(int i=0;i<1000;i++)
            list.add(i);
            
       new LazyReact().fromIterable(list)
                      .limit(100)
                      .peek(System.out::println)
                      .count()
     * 
     * }
     * </pre>
     * 
     *	@param iter Iterable
     *	@return FutureStream
     * @see com.aol.cyclops2.react.reactiveStream.BaseSimpleReact#ofIterable(java.lang.Iterable)
     */
    public <U> FutureStream<U> fromIterable(final Iterable<U> iter) {
        final ReactiveSeq<U> seq = iter instanceof List ? ReactiveSeq.fromList((List) iter) : ReactiveSeq.fromIterable(iter);
        return this.fromStream(seq);
    }

    /* 
     * Build an FutureStream that reacts Asynchronously to the Suppliers within the
     * specified Stream
     * 
     * <pre>
     * {@code 
     * Stream<Supplier<Data>> reactiveStream = Stream.of(this::load1,this::looad2,this::load3);
     * 
     * LazyReact().fromStreamAsync(reactiveStream)
     *            .map(this::process)
     *            .forEach(this::save)
     * }
     * </pre>
     * 
     *	@param actions Stream to react to
     *	@return FutureStream
     * @see com.aol.cyclops2.react.reactiveStream.BaseSimpleReact#react(java.util.reactiveStream.Stream)
     */
    public <U> FutureStream<U> fromStreamAsync(final Stream<? extends Supplier<U>> actions) {

        return constructFutures(actions.map(next -> CompletableFuture.supplyAsync(next, getExecutor())));
    }

    /* 
     * Build an FutureStream that reacts Asynchronously to the Suppliers within the
     * specified Iterator 
     * 
     * <pre>
     * {@code 
     *  List<Supplier<Data>> list = Arrays.asList(this::load1,this::looad2,this::load3);
     * 
     * LazyReact().fromIteratorAsync(list.iterator())
     *            .map(this::process)
     *            .forEach(this::save)
     *
     * 
     * }
     * </pre>
     * 
     * 
     * 
     *	@param actions Iterator to react to
     *	@return FutureStream
     * @see com.aol.cyclops2.react.reactiveStream.BaseSimpleReact#react(java.util.Iterator)
     */
    public <U> FutureStream<U> fromIteratorAsync(final Iterator<? extends Supplier<U>> actions) {

        return this.<U> constructFutures(StreamSupport.<Supplier<U>> stream(Spliterators.<Supplier<U>> spliteratorUnknownSize(actions,
                                                                                                                              Spliterator.ORDERED),
                                                                            false)
                                                      .map(next -> CompletableFuture.supplyAsync(next, getExecutor())));
    }

    /*
     * Build an FutureStream that reacts Asynchronously to the Suppliers within the
     * specified Iterator 
     *   
     * <pre>
     * {@code 
     *  List<Supplier<Data>> list = Arrays.asList(this::load1,this::looad2,this::load3);
     * 
     * LazyReact().fromIterableAsync(list)
     *            .map(this::process)
     *            .forEach(this::save)
     *
     * 
     * }
     * </pre>   
     *   
     *	@param actions
     *	@return
     * @see com.aol.cyclops2.react.reactiveStream.BaseSimpleReact#reactIterable(java.lang.Iterable)
     */
    public <U> FutureStream<U> fromIterableAsync(final Iterable<? extends Supplier<U>> actions) {
        final ReactiveSeq<? extends Supplier<U>> seq = actions instanceof List ? ReactiveSeq.fromList((List) actions)
                : ReactiveSeq.fromIterable(actions);
        return this.<U> constructFutures(seq.map(next -> CompletableFuture.supplyAsync(next, getExecutor())));
    }

    /**
     * * Construct a LazyReact builder using standard thread pool.
     * By default, unless ThreadPools is configured otherwise this will be sized
     * to the available processors
     * 
     * ThreadPools#getStandard is used to determine task executor for parallel task execution
     * 
     * @see ThreadPools#getStandard()
     * 
     * @return LazyReact for building infinite, parallel streams
     */
    public static LazyReact parallelBuilder() {
        return new LazyReact();
    }

    /**
     * Construct a new LazyReact builder, with a new task executor and retry
     * executor with configured number of threads
     * 
     * @param parallelism
     *            Number of threads task executor should have
     * @return LazyReact instance
     */
    public static LazyReact parallelBuilder(final int parallelism) {
        return LazyReact.builder()
                        .executor(Executors.newFixedThreadPool(parallelism))
                        .retrier(new RetryBuilder().parallelism(parallelism))
                        .build();
    }

    /**
     * @return new LazyReact builder configured with standard parallel executor
     *         By default this is the ForkJoinPool common instance but is
     *         configurable in the ThreadPools class
     * 
     * @see ThreadPools#getStandard() see RetryBuilder#getDefaultInstance()
     */
    public static LazyReact parallelCommonBuilder() {
        return LazyReact.builder()
                        .executor(ThreadPools.getStandard())
                        .retrier(RetryBuilder.getDefaultInstance()
                                             .withScheduler(ThreadPools.getCommonFreeThreadRetry()))
                        .build();
    }

    /**
     * @return new LazyReact builder configured to run on a separate thread
     *         (non-blocking current thread), sequentially New ForkJoinPool will
     *         be created
     */
    public static LazyReact sequentialBuilder() {
        return LazyReact.builder()
                        .maxActive(MaxActive.CPU)
                        .async(false)
                        .executor(Executors.newFixedThreadPool(1))
                        .retrier(RetryBuilder.getDefaultInstance()
                                             .withScheduler(Executors.newScheduledThreadPool(2)))
                        .build();
    }

    /**
     * @return LazyReact builder configured to run on a separate thread
     *         (non-blocking current thread), sequentially Common free thread
     *         Executor from
     */
    public static LazyReact sequentialCommonBuilder() {
        return LazyReact.builder()
                        .async(false)
                        .executor(ThreadPools.getCommonFreeThread())
                        .retrier(RetryBuilder.getDefaultInstance()
                                             .withScheduler(ThreadPools.getCommonFreeThreadRetry()))
                        .build();
    }

    /**
     * @return LazyReact builder configured to run on a separate thread
     *         (non-blocking current thread), sequentially Common free thread
     *         Executor from
     */
    public static LazyReact sequentialCurrentBuilder() {
        return LazyReact.builder()
                        .async(false)
                        .maxActive(new MaxActive(1,1))
                        .executor(ThreadPools.getCurrentThreadExecutor())
                        .retrier(RetryBuilder.getDefaultInstance()
                                             .withScheduler(ThreadPools.getCommonFreeThreadRetry()))
                        .build();
    }

    private static final Object NONE = new Object();

    /**
     * Iterate infinitely using the supplied seed and function
     * Iteration is synchronized to support multiple threads using the same iterator.
     * 
     * <pre>
     * {@code 
     *  new LazyReact().objectPoolingOn()
                       .iterate(1,i->i+1)
                       .limit(1_000_000)
                       .map(this::process)
                       .forEach(this::save);
     * 
     * }
     * </pre>
     * 
     * 
     * @see FutureStream#iterate for an alternative which does not synchronize iteration
     * @param seed Initial value
     * @param f Function that performs the iteration
     * @return FutureStream
     */
    public <U> FutureStream<U> iterate(final U seed, final UnaryOperator<U> f) {

        final Subscription sub = new Subscription();
        final Supplier<U> supplier = new Supplier<U>() {
            @SuppressWarnings("unchecked")
            U t = (U) NONE;

            @Override
            public U get() {
                return t = t == NONE ? seed : f.apply(t);
            }
        };
        return construct(StreamSupport.<U> stream(new InfiniteClosingSpliteratorFromSupplier<U>(
                                                                                                Long.MAX_VALUE, supplier, sub),
                                                  false));

    }

    /**
     * Generate a FutureStream from the data flowing into the prodiced Adapter
     * <pre>
     * {@code 
     *    
     *    Topic<Integer> topic = new Topic<>();
     *    
     *    new LazyReact(10,10).fromAdapter(topic)
     *                        .forEach(this::process);
     *    
     *    //on anther thread
     *    topic.offer(100);
     *    topic.offer(200);
     *   
     * }
     * </pre>
     * 
     * 
     * @param adapter Adapter to construct FutureStream from
     * @return FutureStream
     */
    public <U> FutureStream<U> fromAdapter(final Adapter<U> adapter) {
        final Subscription sub = new Subscription();
        return new FutureStreamImpl(this,()->adapter.stream(sub)){
            @Override
            public ReactiveSeq<U> stream() {
                return (ReactiveSeq<U>)adapter.stream(sub);
            }
        };


    }

    /**
     * Generate an infinite Stream
     * 
     * <pre>
     * {@code 
     *  new LazyReact().generate(()->"hello")
                       .limit(5)
                       .reduce(Semigroups.stringConcat);
                       
        //Optional[hellohellohellohellohello]         
     * 
     * }</pre>
     * 
     * @param generate Supplier that generates reactiveStream input
     * @return Infinite FutureStream
     */
    public <U> FutureStream<U> generate(final Supplier<U> generate) {

        return construct(StreamSupport.<U> stream(new InfiniteClosingSpliteratorFromSupplier<U>(
                                                                                                Long.MAX_VALUE, generate, new Subscription()),
                                                  false));
    }

    /**
     * Generate an infinite FutureStream executing the provided Supplier continually and asynhcronously
     * 
     * <pre>
     * {@code 
     *  new LazyReact().generate(this::load)
                       .limit(5)
                       .reduce(Semigroups.stringConcat);
                       
        //Optional["data1data2data3data4data5"]         
     * 
     * }</pre>
     * @param s Supplier to execute asynchronously to create an infinite Stream
     * @return Infinite FutureStream
     */
    public <U> FutureStream<U> generateAsync(final Supplier<U> s) {
        return this.constructFutures(ReactiveSeq.generate(() -> 1)
                                                .map(n -> CompletableFuture.supplyAsync(s, getExecutor())));

    }

    /**
     * Start a reactive flow from a JDK Iterator
     * <pre>
     * {@code 
     *  Iterator<Integer> iterator;
     *  new LazyReact(10,10).from(iterator)
     *                       .map(this::process);
     * 
     * }
     * </pre>
     * @param iterator SimpleReact will iterate over this iterator concurrently to skip the reactive dataflow
     * @return  FutureStream
     */
    @SuppressWarnings("unchecked")
    public <U> FutureStream<U> from(final Iterator<U> iterator) {
        return fromStream(StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED), false));

    }

    /**
     * Start a reactive flow from a Collection using an Iterator
     * 
     * 
     * <pre>
     * {@code 
     * 
     *  new LazyReact(10,10).from(myList)
     *                       .map(this::process);
     * 
     * }
     * </pre>
     * 
     * 
     * @param collection - Collection SimpleReact will iterate over at the skip of the flow
     *
     * @return  FutureStream
     */
    @SuppressWarnings("unchecked")
    public <R> FutureStream<R> from(final Collection<R> collection) {
        return fromStream(collection.stream());
    }

    /**
     * Start a reactive dataflow from a reactiveStream.
     * 
     * @param stream that will be used to drive the reactive dataflow
     * @return  FutureStream
     */
    public FutureStream<Integer> from(final IntStream stream) {

        return fromStream(stream.boxed());

    }

    /**
     * Start a reactive dataflow from a reactiveStream.
     * 
     * @param stream that will be used to drive the reactive dataflow
     * @return  FutureStream
     */
    public FutureStream<Double> from(final DoubleStream stream) {
        return fromStream(stream.boxed());
    }

    /**
     * Start a reactive dataflow from a reactiveStream.
     * 
     * @param stream that will be used to drive the reactive dataflow
     * @return FutureStream
     */
    public FutureStream<Long> from(final LongStream stream) {
        return fromStream(stream.boxed());
    }

    /**
     * Construct a LazyFurureStream from the values in the supplied array
     * <pre>
     * {@code 
     * 
     *  new LazyReact(10,10).of(1,2,3,4)
     *                      .map(this::process);
     * 
     * }
     * </pre>
     * @param array Array to construct FutureStream from
     * @return  FutureStream
     */
    @SafeVarargs
    public final <U> FutureStream<U> of(final U... array) {
        return fromStream(ReactiveSeq.of(array));
    }

}
