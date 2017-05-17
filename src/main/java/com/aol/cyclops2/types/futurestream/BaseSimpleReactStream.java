package com.aol.cyclops2.types.futurestream;

import static java.util.Spliterator.ORDERED;
import static java.util.Spliterators.spliteratorUnknownSize;

import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import cyclops.stream.FutureStream;
import org.jooq.lambda.Seq;

import cyclops.async.SimpleReact;
import cyclops.async.adapters.Queue;
import cyclops.async.adapters.QueueFactory;
import com.aol.cyclops2.internal.react.SimpleReactStreamImpl;
import com.aol.cyclops2.internal.react.stream.CloseableIterator;
import com.aol.cyclops2.internal.react.stream.ReactBuilder;
import com.aol.cyclops2.internal.react.stream.StreamWrapper;
import com.aol.cyclops2.react.SimpleReactFailedStageException;
import com.aol.cyclops2.react.ThreadPools;
import com.aol.cyclops2.react.async.subscription.Continueable;

public interface BaseSimpleReactStream<U> extends BlockingStream<U> {

    ReactBuilder getSimpleReact();

    /**
     * Keep only those elements in a reactiveStream that are of a given type.
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
    default <U> BaseSimpleReactStream<U> ofType(final Class<? extends U> type) {
        return filterSync(type::isInstance).thenSync(t -> (U) t);
    }

    /*
     * Cast all elements in this reactiveStream to specified type. May throw {@link
     * ClassCastException}.
     * 
     * SimpleReactStream.of(1, "a", 2, "b", 3).cast(Integer.class)
     * 
     * will throw a ClassCastException
     * 
     * @param type Type to cast to
     * 
     * @return SimpleReactStream
     * 
     */
    default <U> BaseSimpleReactStream<U> cast(final Class<? extends U> type) {
        return this.thenSync(type::cast);

    }

    /**
     * Returns a reactiveStream with a given value interspersed between any two values
     * of this reactiveStream.
     * 
     * <code>
     * 
     * // (1, 0, 2, 0, 3, 0, 4) 
     * 
     * SimpleReactStream.of(1, 2, 3, 4).intersperse(0)
     * 
     * </code>
     *
     */
    default BaseSimpleReactStream<U> intersperse(final U value) {
        return flatMap(t -> Stream.of(value, t)).skip(1);
    }

    public BaseSimpleReactStream<U> skip(long n);

    /*
     * (non-Javadoc)
     * 
     */
    default Iterator<U> iterator() {

        final Queue<U> q = toQueue();
        if (getSubscription().closed())
            return new CloseableIterator<>(
                                           Arrays.<U> asList()
                                                 .iterator(),
                                           getSubscription(), null);

        return new CloseableIterator<>(
                                       q.stream(getSubscription())
                                        .iterator(),
                                       getSubscription(), q);
    }

    @Override
    StreamWrapper<U> getLastActive();

    /* 
     * React to new events with the supplied function on the supplied Executor
     * 
     *	@param fn Apply to incoming events
     *	@param service Service to execute function on 
     *	@return next stage in the Stream
     */

    <R> BaseSimpleReactStream<R> then(final Function<? super U, ? extends R> fn, Executor service);

    /* 
     * React to new events with the supplied function on the supplied Executor
     * 
     *	@param fn Apply to incoming events
     *	@param service Service to execute function on 
     *	@return next stage in the Stream
     */
    <R> BaseSimpleReactStream<R> thenSync(final Function<? super U, ? extends R> fn);



    <R> BaseSimpleReactStream<R> fromStream(Stream<R> stream);

    /**
     * React <b>transform</b>
     * 
     * 
     * 
     * Unlike 'with' this method is fluent, and returns another Stage Builder
     * that can represent the next stage in the dataflow.
     * 
     * <pre>
     * {@code 
     	new SimpleReact().<Integer, Integer> react(() -> 1, () -> 2, () -> 3)
    			.transform((it) -> it * 100)
    			.transform((it) -> "*" + it)
    			
    			}
    </pre>
     *
     * React transform allows event reactors to be chained. Unlike React with, which
     * returns a collection of Future references, React transform is a fluent
     * interface that returns the React builder - allowing further reactors to
     * be added to the chain.
     * 
     * React transform does not block.
     * 
     * React with can be called after React transform which gives access to the full
     * CompleteableFuture api. CompleteableFutures can be passed back into
     * SimpleReact via SimpleReact.react(streamOfCompleteableFutures);
     * 
     * See this blog post for examples of what can be achieved via
     * CompleteableFuture :- <a href=
     * 'http://www.nurkiewicz.com/2013/12/promises-and-completablefuture.html'>http://www.nurkiewicz.com/2013/12/promises-and-completablefuture.htm
     * l </a>
     * 
     * @param fn
     *            Function to be applied to the results of the currently active
     *            event tasks
     * @return A new builder object that can be used to define the next stage in
     *         the dataflow
     */
    @SuppressWarnings("unchecked")
    <R> BaseSimpleReactStream<R> then(final Function<? super U, ? extends R> fn);

    /**
     * Peek asynchronously at the results in the current stage. Current results
     * are passed through to the next stage.
     * 
     * @param consumer
     *            That will recieve current results
     * @return A new builder object that can be used to define the next stage in
     *         the dataflow
     */
    BaseSimpleReactStream<U> peek(final Consumer<? super U> consumer);

    /**
     * Synchronous peek operator
     * 
     * @param consumer Peek consumer
     * @return Next stage
     */
    BaseSimpleReactStream<U> peekSync(final Consumer<? super U> consumer);

    /**
     * Perform a flatMap operation where the CompletableFuture type returned is flattened from the resulting Stream
     * If in async mode this operation is performed asyncrhonously
     * If in sync mode this operation is performed synchronously
     * 
     * <pre>
     * {@code 
     * assertThat( new SimpleReact()
    									.of(1,2,3)
    									.flatMapCompletableFuture(i->CompletableFuture.completedFuture(i))
    									.block(),equalTo(Arrays.asList(1,2,3)));
     * }
     * </pre>
     *
     * In this example the result of the flatMapCompletableFuture is 'flattened' to the raw integer values
     * 
     * 
     * @param flatFn flatMap function
     * @return Flatten Stream with flatFn applied
     */
    <R> BaseSimpleReactStream<R> flatMapToCompletableFuture(Function<? super U, CompletableFuture<? extends R>> flatFn);

    /**
     * Perform a flatMap operation where the CompletableFuture type returned is flattened from the resulting Stream
     * This operation is performed synchronously
     * 
     * <pre>
     * {@code 
     * assertThat( new SimpleReact()
    									.of(1,2,3)
    									.flatMapCompletableFutureSync(i->CompletableFuture.completedFuture(i))
    									.block(),equalTo(Arrays.asList(1,2,3)));
     * }
     *</pre>
     * In this example the result of the flatMapCompletableFuture is 'flattened' to the raw integer values
     * 
     * 
     * @param flatFn flatMap function
     * @return Flatten Stream with flatFn applied
     */
    <R> BaseSimpleReactStream<R> flatMapToCompletableFutureSync(Function<? super U, CompletableFuture<? extends R>> flatFn);

    /**
     * Allows aggregate values in a Stream to be flatten into a single Stream.
     * flatMap function turn each aggregate value into it's own Stream, and SimpleReact aggregates those Streams
     * into a single flattened reactiveStream
     * 
     * @param flatFn Function that coverts a value (e.g. a Collection) into a Stream
     * @return SimpleReactStream
     */
    <R> BaseSimpleReactStream<R> flatMap(Function<? super U, ? extends Stream<? extends R>> flatFn);

    /**
     * Removes elements that do not match the supplied predicate from the
     * dataflow
     * 
     * @param p
     *            Predicate that will be used to filter elements from the
     *            dataflow
     * @return A new builder object that can be used to define the next stage in
     *         the dataflow
     */
    @SuppressWarnings("unchecked")
    BaseSimpleReactStream<U> filter(final Predicate<? super U> p);

    /**
     * Synchronous filtering operation
     * 
     * Removes elements that do not match the supplied predicate from the
     * dataflow
     * 
     * @param p
     *            Predicate that will be used to filter elements from the
     *            dataflow
     * @return A new builder object that can be used to define the next stage in
     *         the dataflow
     */
    BaseSimpleReactStream<U> filterSync(final Predicate<? super U> p);

    /**
     * @return A Stream of CompletableFutures that represent this stage in the
     *         dataflow
     */
    @SuppressWarnings({ "unchecked" })
    <T> Stream<CompletableFuture<T>> streamCompletableFutures();

    /**
     * React <b>onFail</b>
     * 
     * 
     * Define a function that can be used to recover from exceptions during the
     * preceeding stage of the dataflow. e.g.
     * 
     * 
     * 
     * onFail allows disaster recovery for each task (a separate onFail should
     * be configured for each react phase that can fail). E.g. if reading data
     * from an external service fails, but default value is acceptable - onFail
     * is a suitable mechanism to set the default value. Asynchronously apply
     * the function supplied to the currently active event tasks in the
     * dataflow.
     * 
     * <pre>
      {@code
    List<String> strings = new SimpleReact().<Integer, Integer> react(() -> 100, () -> 2, () -> 3)
    				.transform(it -> {
    					if (it == 100)
    						throw new RuntimeException("boo!");
    		
    					return it;
    				})
    				.onFail(e -> 1)
    				.transform(it -> "*" + it)
    				.block();	  
      
      
      
      }
      
    	  </pre>
     * 
     * 
     * In this example onFail recovers from the RuntimeException thrown when the
     * input to the first 'transform' stage is 100.
     * 
     * @param fn
     *            Recovery function, the exception is input, and the recovery
     *            value is emitted
     * @return A new builder object that can be used to define the next stage in
     *         the dataflow
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    BaseSimpleReactStream<U> onFail(final Function<? super SimpleReactFailedStageException, ? extends U> fn);

    /**
     * Recover for a particular class of exceptions only. Chain onFail methods from specific Exception classes
     * to general, as Exceptions will be caught and handled in order. 
     * e.g.
     * <pre>
     * {@code
      			onFail(IOException.class, recoveryFunction1)
      			.onFail(Throwable.class,recovertyFunction2)
     *  }
     * </pre>
     * For an IOException recoveryFunction1 will be executed
     * 
     * but with the definitions reveresed 
     * <pre>
      {@code
      	onFail(Throwable.class,recovertyFunction2)
      		.onFail(IOException.class, recoveryFunction1)
     	}
     	</pre>
     
     * recoveryFunction1 will not be called
     * 
     * 
     * @param exceptionClass Class of exceptions to recover from
     * @param fn Recovery function
     * @return recovery value
     */
    BaseSimpleReactStream<U> onFail(Class<? extends Throwable> exceptionClass,
            final Function<? super SimpleReactFailedStageException, ? extends U> fn);

    /**
     * React <b>capture</b>
     * 
     * While onFail is used for disaster recovery (when it is possible to
     * recover) - capture is used to capture those occasions where the full
     * pipeline has failed and is unrecoverable.
     * 
     * <pre>
     	{@code
    	List<String> strings = new SimpleReact().<Integer, Integer> react(() -> 1, () -> 2, () -> 3)
    		.transform(it -> it * 100)
    		.transform(it -> {
    			if (it == 100)
    				throw new RuntimeException("boo!");
    
    			return it;
    		})
    		.onFail(e -> 1)
    		.transform(it -> "*" + it)
    		.transform(it -> {
    			
    			if ("*200".equals(it))
    				throw new RuntimeException("boo!");
    
    			return it;
    		})
    		.capture(e -> logger.error(e.getMessage(),e))
    		.block();
    		}
    	</pre>
     * 
     * In this case, strings will only contain the two successful results (for
     * ()-&gt;1 and ()-&gt;3), an exception for the chain starting from Supplier
     * ()-&gt;2 will be logged by capture. Capture will not capture the
     * exception thrown when an Integer value of 100 is found, but will catch
     * the exception when the String value "*200" is passed along the chain.
     * 
     * @param errorHandler
     *            A consumer that recieves and deals with an unrecoverable error
     *            in the dataflow
     * @return A new builder object that can be used to define the next stage in
     *         the dataflow
     */
    @SuppressWarnings("unchecked")
    BaseSimpleReactStream<U> capture(final Consumer<Throwable> errorHandler);

    /* 
     * Execute subsequent stages on the completing thread (until async called)
     * 10X faster than async execution.
     * Use async for blocking IO or distributing work across threads or cores.
     * Switch to sync for non-blocking tasks when desired thread utlisation reached
     * 
     *	@return Version of FutureStream that will use sync CompletableFuture methods
     * 
     */
    BaseSimpleReactStream<U> sync();

    /* 
     * Execute subsequent stages by submission to an Executor for async execution
     * 10X slower than sync execution.
     * Use async for blocking IO or distributing work across threads or cores.
     * Switch to sync for non-blocking tasks when desired thread utlisation reached
     *
     * 
     *	@return Version of FutureStream that will use async CompletableFuture methods
     *
     */
    BaseSimpleReactStream<U> async();

    Queue<U> toQueue();

    /**
     * Create a 'free threaded' asynchronous reactiveStream that runs on a single thread (not current)
     * The supplier will be executed asyncrhonously, subsequent tasks will be executed synchronously unless the async() operator is invoked.
     * 
     */
    static <T> SimpleReactStream<T> react(final Supplier<T> value) {
        return new SimpleReact(
                               ThreadPools.getStandard(),
                               false).ofAsync(value);
    }

    /**
     * Create a 'free threaded' asynchronous reactiveStream that runs on a single thread (not current)
     * The supplier will be executed asyncrhonously, subsequent tasks will be executed synchronously unless the async() operator is invoked.
     * 
     */
    @SafeVarargs
    static <T> SimpleReactStream<T> react(final Supplier<T>... values) {
        return new SimpleReact(
                               ThreadPools.getStandard(),
                               false).ofAsync(values);
    }

    /**
     *  Create a sequential synchronous reactiveStream that runs on the current thread
     * 
     * 
     */
    static <T> SimpleReactStream<T> of(final T value) {
        return simpleReactStream((Stream) Seq.of(value));
    }

    /**
     *  Create a sequential synchronous reactiveStream that runs on the current thread
     * 
     * 
     */
    @SafeVarargs
    static <T> SimpleReactStream<T> of(final T... values) {
        return simpleReactStream((Stream) Seq.of(values));
    }

    /**
     *  Create a sequential synchronous reactiveStream that runs on the current thread
     * @see Stream#of(Object)
     * 
     */
    static <T> SimpleReactStream<T> freeThread(final T value) {
        return new SimpleReact(
                               ThreadPools.getSequential(),
                               false).of(value);
    }

    /**
     * Create a sequential synchronous reactiveStream that runs on the current thread
     * @see Stream#of(Object[])
     * 
     */
    @SafeVarargs
    static <T> SimpleReactStream<T> freeThread(final T... values) {
        return new SimpleReact(
                               ThreadPools.getSequential(),
                               false).of(values);
    }

    /**
     *  Create an empty sequential synchronous reactiveStream that runs on the current thread
     * 
     * 
     */
    static <T> SimpleReactStream<T> empty() {
        return simpleReactStream((Stream) Seq.empty());
    }

    /**
     * Construct an SimpleReactStream  from specified array, that will run in parallel
     * on the common Parallel executor service (by default the Common ForkJoinPool) see ThreadPools#setUseCommon 
     * to change to a different pool
     * 
     * @param array
     *            Array of value to form the reactive reactiveStream / sequence
     * @return SimpleReact Stage
     */
    public static <U> SimpleReactStream<U> parallel(final U... array) {
        return SimpleReact.parallelCommonBuilder()
                          .from(Arrays.asList(array));
    }

    /**
     *  Create a 'free threaded' asynchronous reactiveStream that runs on the supplied CompletableFutures executor service (unless async operator invoked
     *  , in which it will switch to the common 'free' thread executor)
     *  Subsequent tasks will be executed synchronously unless the async() operator is invoked.
     *  
     * @see Stream#of(Object)
     */
    static <T> BaseSimpleReactStream<T> simpleReactStreamFrom(final Stream<CompletableFuture<T>> stream) {
        return new SimpleReact(
                               ThreadPools.getSequential(),
                               false).fromStream(stream);
    }

    /**
     *  Create a 'free threaded' asynchronous reactiveStream that runs on the supplied CompletableFutures executor service (unless async operator invoked
     *  , in which it will switch to the common 'free' thread executor)
     *  Subsequent tasks will be executed synchronously unless the async() operator is invoked.
     *  
     * @see Stream#of(Object)
     */
    static <T> BaseSimpleReactStream<T> simpleReactStream(final CompletableFuture<T> value) {
        return new SimpleReact(
                               ThreadPools.getSequential(),
                               false).fromStream(Stream.of(value));
    }

    /**
     *  Create a 'free threaded' asynchronous reactiveStream that runs on a single thread (not current)
     *  The supplier will be executed asyncrhonously, subsequent tasks will be executed synchronously unless the async() operator
     *  is invoked.
     *  
     * @see Stream#of(Object)
     */
    static <T> SimpleReactStream<T> simpleReactStream(final CompletableFuture<T>... values) {
        return new SimpleReact(
                               ThreadPools.getSequential(),
                               false).fromStream(Stream.of(values));
    }

    /**
     * Wrap a Stream into a SimpleReactStream.
     */
    static <T> SimpleReactStream<T> simpleReactStream(Stream<T> stream) {

        if (stream instanceof FutureStream)
            stream = ((FutureStream) stream).toQueue()
                                                .stream(((FutureStream) stream).getSubscription());

        final SimpleReact sr = new SimpleReact(
                                               ThreadPools.getCurrentThreadExecutor(),
                                               false);
        return new SimpleReactStreamImpl<T>(
                                            sr, stream.map(CompletableFuture::completedFuture));
    }

    /**
     * Wrap an Iterable into a FutureStream.
     */
    static <T> SimpleReactStream<T> simpleReactStreamFromIterable(final Iterable<T> iterable) {
        return simpleReactStream(iterable.iterator());
    }

    /**
     * Wrap an Iterator into a FutureStream.
     */
    static <T> SimpleReactStream<T> simpleReactStream(final Iterator<T> iterator) {
        return simpleReactStream(StreamSupport.stream(spliteratorUnknownSize(iterator, ORDERED), false));
    }

    Continueable getSubscription();

    QueueFactory<U> getQueueFactory();

    BaseSimpleReactStream<U> withSubscription(Continueable subscription);

    BaseSimpleReactStream<U> withQueueFactory(QueueFactory<U> queueFactory);



    Executor getTaskExecutor();

    boolean isAsync();

}