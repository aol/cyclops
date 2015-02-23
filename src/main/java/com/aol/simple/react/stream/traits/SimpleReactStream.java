package com.aol.simple.react.stream.traits;

import static java.util.Spliterator.ORDERED;
import static java.util.Spliterators.spliteratorUnknownSize;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.jooq.lambda.Seq;

import com.aol.simple.react.RetryBuilder;
import com.aol.simple.react.async.Queue;
import com.aol.simple.react.exceptions.FilteredExecutionPathException;
import com.aol.simple.react.exceptions.SimpleReactFailedStageException;
import com.aol.simple.react.stream.StageWithResults;
import com.aol.simple.react.stream.StreamWrapper;
import com.aol.simple.react.stream.ThreadPools;
import com.aol.simple.react.stream.lazy.LazyReact;
import com.aol.simple.react.stream.simple.SimpleReact;
import com.aol.simple.react.stream.simple.SimpleReactStreamImpl;
import com.nurkiewicz.asyncretry.RetryExecutor;


public interface SimpleReactStream<U> extends LazyStream<U>, 
				BlockingStream<U>, 
				ConfigurableStream<U>, 
				EagerOrLazyToQueue<U>{
	

	
	
	/**
	 * @param collector
	 *            to perform aggregation / reduction operation on the results
	 *            from active stage (e.g. to Collect into a List or String)
	 * @param fn
	 *            Function that receives the results of all currently active
	 *            tasks as input
	 * @return A new builder object that can be used to define the next stage in
	 *         the dataflow
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	default <T, R> SimpleReactStream<R> allOf(final Collector collector,
			final Function<T, R> fn) {
		CompletableFuture[] array = lastActiveArray(getLastActive());
		CompletableFuture cf = CompletableFuture.allOf(array);
		Function<Exception, T> f = (Exception e) -> {
			BlockingStream.capture(e,getErrorHandler());
			return block(Collectors.toList(),
					new StreamWrapper(Stream.of(array), true));
		};
		CompletableFuture onFail = cf.exceptionally(f);
		CompletableFuture onSuccess = onFail.thenApplyAsync((result) -> {
			return new StageWithResults(this.getTaskExecutor(),null, result).submit(() -> (R)fn
					.apply(BlockingStream.aggregateResults(collector, Stream.of(array)
							.collect(Collectors.toList()),getErrorHandler())));
		}, getTaskExecutor());
		return (SimpleReactStream<R>) withLastActive(new StreamWrapper(onSuccess,
				isEager()));

	}

	

	@SuppressWarnings("rawtypes")
	static CompletableFuture[] lastActiveArray(StreamWrapper lastActive) {
		return lastActive.list().toArray(new CompletableFuture[0]);
	}
	
	/**
	 * Will execute this phase on the RetryExecutor (default or user supplied).
	 * The RetryExecutor can be changed via withRetrier.
	 * 
	 * This stage will be retried according to the configured rules. See
	 * https://github.com/nurkiewicz/async-retry for detailed advice on how to
	 * conifugre
	 * 
	 * 
	 * @param fn
	 *            Function that will be executed and retried on failure
	 * @return Next Stage in the Strea,
	 */
	@SuppressWarnings("unchecked")
	default <R> SimpleReactStream<R> retry(final Function<U, R> fn) {

		return (SimpleReactStream<R>) this.withLastActive(getLastActive().permutate(
				getLastActive().stream().map(
						(ft) -> ft.thenApplyAsync((res) -> BlockingStream.getSafe(getRetrier()
								.getWithRetry(() -> fn.apply((U) res)),getErrorHandler()),
								getTaskExecutor())), Collectors.toList()));
	}
	
	default <R> SimpleReactStream<R> fromStream(Stream<R> stream) {
		return (SimpleReactStream<R>) this.withLastActive(getLastActive()
				.withNewStream(stream.map(CompletableFuture::completedFuture)));
	}

	default <R> SimpleReactStream<R> fromStreamCompletableFuture(
			Stream<CompletableFuture<R>> stream) {
		Stream noType = stream;
		return (SimpleReactStream<R>) this.withLastActive(getLastActive()
				.withStream(noType));
	}

	
	/**
	 * React <b>then</b>
	 * 
	 * 
	 * 
	 * Unlike 'with' this method is fluent, and returns another Stage Builder
	 * that can represent the next stage in the dataflow.
	 * 
	 * <code>
	  new SimpleReact().&lt;Integer, Integer&gt; react(() -&gt; 1, () -&gt; 2, () -&gt; 3)
				.then((it) -&gt; it * 100)
				.then((it) -&gt; "*" + it)
	</code>
	 *
	 * React then allows event reactors to be chained. Unlike React with, which
	 * returns a collection of Future references, React then is a fluent
	 * interface that returns the React builder - allowing further reactors to
	 * be added to the chain.
	 * 
	 * React then does not block.
	 * 
	 * React with can be called after React then which gives access to the full
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
	default  <R> SimpleReactStream<R> then(final Function<U, R> fn) {
		return (SimpleReactStream<R>) this.withLastActive(getLastActive().permutate(
				getLastActive().stream().map(
						(ft) -> ft.thenApplyAsync(SimpleReactStream.<U,R>handleExceptions(fn),
								getTaskExecutor())), Collectors.toList()));
	}

	static <U,R> Function<U, R> handleExceptions(Function<U, R> fn) {
		return (input) -> {
			try {
				return fn.apply(input);
			} catch (Throwable t) {
				throw new SimpleReactFailedStageException(input, t);

			}
		};
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jooq.lambda.Seq#flatMap(java.util.function.Function)
	 */
	default <R> SimpleReactStream<R> flatMap(
			Function<? super U, ? extends Stream<? extends R>> flatFn) {

		//need to pass in a builder in the constructor and build using it
		return (SimpleReactStream)getSimpleReact().construct(Stream.of(), this.getTaskExecutor(),
				this.getRetrier(), isEager())	
				.fromStream(
						toQueue()
								.stream()
								.flatMap(flatFn));
	}
	
	/**
	 * 
	 * React <b>with</b>
	 * 
	 * Asynchronously apply the function supplied to the currently active event
	 * tasks in the dataflow.
	 * 
	 * While most methods in this class are fluent, and return a reference to a
	 * SimpleReact Stage builder, this method can be used this method to access
	 * the underlying CompletableFutures.
	 * 
	 * <code>
	 	List&lt;CompletableFuture&lt;Integer&gt;&gt; futures = new SimpleReact().&lt;Integer, Integer&gt; react(() -&gt; 1, () -&gt; 2, () -&gt; 3)
				.with((it) -&gt; it * 100);
			</code>
	 * 
	 * In this instance, 3 suppliers generate 3 numbers. These may be executed
	 * in parallel, when they complete each number will be multiplied by 100 -
	 * as a separate parrellel task (handled by a ForkJoinPool or configurable
	 * task executor). A List of Future objects will be returned immediately
	 * from Simple React and the tasks will be executed asynchronously.
	 * 
	 * React with does not block.
	 * 
	 * @param fn
	 *            Function to be applied to the results of the currently active
	 *            event tasks
	 * @return A list of CompletableFutures which will contain the result of the
	 *         application of the supplied function
	 */
	@SuppressWarnings("unchecked")
	default <R> List<CompletableFuture<R>> with(final Function<U, R> fn) {

		return getLastActive()
				.stream()
				.map(future -> (CompletableFuture<R>) future.thenApplyAsync(fn,
						getTaskExecutor())).collect(Collectors.toList());
	}
	
	
	/**
	 * Peek asynchronously at the results in the current stage. Current results
	 * are passed through to the next stage.
	 * 
	 * @param consumer
	 *            That will recieve current results
	 * @return A new builder object that can be used to define the next stage in
	 *         the dataflow
	 */
	default SimpleReactStream<U> peek(final Consumer<? super U> consumer) {
		return (SimpleReactStream<U>) then((t) -> {
			consumer.accept(t);
			return (U) t;
		});
	}
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
	default SimpleReactStream<U> filter(final Predicate<? super U> p) {

		return (SimpleReactStream<U>) this.withLastActive(getLastActive().permutate(getLastActive()
				.stream().map(ft -> ft.thenApplyAsync((in) -> {
					if (!p.test((U) in)) {
						throw new FilteredExecutionPathException();
					}
					return in;
				})), Collectors.toList()));

	}
	/**
	 * @return A Stream of CompletableFutures that represent this stage in the
	 *         dataflow
	 */
	@SuppressWarnings({ "unchecked" })
	default <T> Stream<CompletableFuture<T>> streamCompletableFutures() {
		Stream s = this.getLastActive().stream();
		return s;

	}
	
	/**
	 * Merge this reactive dataflow with another of the same type. To merge
	 * flows of different types use the static method merge and merge to a
	 * common ancestor.
	 * 
	 * @param s
	 *            Reactive stage builder to merge with
	 * @return Merged dataflow
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	default SimpleReactStream<U> merge(SimpleReactStream<U> s) {
		List merged = Stream.of(this.getLastActive().list(), s.getLastActive().list())
				.flatMap(Collection::stream).collect(Collectors.toList());
		return (SimpleReactStream<U>) this.withLastActive(new StreamWrapper(merged));
	}
	
	/**
	 * Merge this reactive dataflow with another - recommended for merging
	 * different types. To merge flows of the same type the instance method
	 * merge is more appropriate.
	 * 
	 * @param s1
	 *            Reactive stage builder to merge
	 * @param s2
	 *            Reactive stage builder to merge
	 * @return Merged dataflow
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <R> SimpleReactStream<R> merge(SimpleReactStream s1, SimpleReactStream s2) {
		List merged = Stream.of(s1.getLastActive().list(), s2.getLastActive().list())
				.flatMap(Collection::stream).collect(Collectors.toList());
		return (SimpleReactStream<R>) s1.withLastActive(new StreamWrapper(merged));
	}
	
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
	 * <code>
	  	List&lt;String&gt; strings = new SimpleReact().&lt;Integer, Integer&gt; react(() -&gt; 100, () -&gt; 2, () -&gt; 3)
					.then(it -&gt; {
						if (it == 100)
							throw new RuntimeException("boo!");
			
						return it;
					})
					.onFail(e -&gt; 1)
					.then(it -&gt; "*" + it)
					.block();
		  </code>
	 * 
	 * 
	 * In this example onFail recovers from the RuntimeException thrown when the
	 * input to the first 'then' stage is 100.
	 * 
	 * @param fn
	 *            Recovery function, the exception is input, and the recovery
	 *            value is output
	 * @return A new builder object that can be used to define the next stage in
	 *         the dataflow
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	default <U> SimpleReactStream<U> onFail(final Function<? extends SimpleReactFailedStageException, U> fn) {
		return (SimpleReactStream<U>) this.withLastActive(getLastActive().permutate(getLastActive()
				.stream().map((ft) -> ft.exceptionally((t) -> {
					if (t instanceof FilteredExecutionPathException)
						throw (FilteredExecutionPathException) t;
					Throwable throwable =(Throwable)t;
					if(t instanceof CompletionException)
						throwable = ((Exception)t).getCause();
					
					SimpleReactFailedStageException simpleReactException = assureSimpleReactException( throwable);//exceptions from initial supplier won't be wrapper in SimpleReactFailedStageException
						return ((Function) fn).apply(simpleReactException);
						
				})), Collectors.toList()));
	}

	static SimpleReactFailedStageException assureSimpleReactException(
			Throwable throwable){
		return new SimpleReactFailedStageException(null,(throwable));
	}
	/**
	 * React <b>capture</b>
	 * 
	 * While onFail is used for disaster recovery (when it is possible to
	 * recover) - capture is used to capture those occasions where the full
	 * pipeline has failed and is unrecoverable.
	 * 
	 * <code>
	 * List&lt;String&gt; strings = new SimpleReact().&lt;Integer, Integer&gt; react(() -&gt; 1, () -&gt; 2, () -&gt; 3)
			.then(it -&gt; it * 100)
			.then(it -&gt; {
				if (it == 100)
					throw new RuntimeException("boo!");
	
				return it;
			})
			.onFail(e -&gt; 1)
			.then(it -&gt; "*" + it)
			.then(it -&gt; {
				
				if ("*200".equals(it))
					throw new RuntimeException("boo!");
	
				return it;
			})
			.capture(e -&gt; logger.error(e.getMessage(),e))
			.block();
		</code>
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
	default SimpleReactStream<U> capture(final Consumer<? extends Throwable> errorHandler) {
		return (SimpleReactStream)this.withErrorHandler(Optional
				.of((Consumer<Throwable>) errorHandler));
	}
	
	

	/**
	 * React and <b>allOf</b>
	 * 
	 * allOf is a non-blocking equivalent of block. The current thread is not
	 * impacted by the calculations, but the reactive chain does not continue
	 * until all currently alloted tasks complete. The allOf task is then
	 * provided with a list of the results from the previous tasks in the chain.
	 * 
	 * <code>
	  boolean blocked[] = {false};
		new SimpleReact().&lt;Integer, Integer&gt; react(() -&gt; 1, () -&gt; 2, () -&gt; 3)
				
				.then(it -&gt; {
					try {
						Thread.sleep(50000);
					} catch (Exception e) {
						
					}
					blocked[0] =true;
					return 10;
				})
				.allOf( it -&gt; it.size());

		
		assertThat(blocked[0],is(false));
		</code>
	 * 
	 * In this example, the current thread will continue and assert that it is
	 * not blocked, allOf could continue and be executed in a separate thread.
	 * 
	 * @param fn
	 *            Function that recieves the results of all currently active
	 *            tasks as input
	 * @return A new builder object that can be used to define the next stage in
	 *         the dataflow
	 */
	@SuppressWarnings("unchecked")
	default <T, R> SimpleReactStream<R> allOf(final Function<List<T>, R> fn) {

		return (SimpleReactStream<R>) allOf(Collectors.toList(), (Function<R, U>) fn);

	}

	
	
	
	/**
	 * Construct a SimpleReact Stage from a supplied array
	 * 
	 * @param array
	 *            Array of value to form the reactive stream / sequence
	 * @return SimpleReact Stage
	 */
	public static <U> SimpleReactStream<U> parallelOf(U... array) {
		return new SimpleReact().reactToCollection(Arrays.asList(array));
	}

	/**
	 * @return Lazy SimpleReact for handling infinite streams
	 */
	public static SimpleReact parallelBuilder() {
		return new SimpleReact();
	}

	public static SimpleReact parallelBuilder(int parallelism) {
		return SimpleReact.builder().executor(new ForkJoinPool(parallelism))
				.retrier(new RetryBuilder().parallelism(parallelism)).build();
	}

	public static SimpleReact parallelCommonBuilder() {
		return new SimpleReact(ForkJoinPool.commonPool());
	}

	public static SimpleReact sequentialBuilder() {
		return simple(new ForkJoinPool(1));
	}

	public static SimpleReact sequentialCommonBuilder() {
		return simple(ThreadPools.getCommonFreeThread());
	}

	/**
	 * @param executor
	 *            Executor this SimpleReact instance will use to execute
	 *            concurrent tasks.
	 * @return Lazy SimpleReact for handling infinite streams
	 */
	public static SimpleReact simple(ExecutorService executor) {
		return new SimpleReact(executor);
	}

	/**
	 * @param retry
	 *            RetryExecutor this SimpleReact instance will use to retry
	 *            concurrent tasks.
	 * @return Lazy SimpleReact for handling infinite streams
	 */
	public static SimpleReact simple(RetryExecutor retry) {
		return SimpleReact.builder().retrier(retry).build();
	}

	/**
	 * @param executor
	 *            Executor this SimpleReact instance will use to execute
	 *            concurrent tasks.
	 * @param retry
	 *            RetryExecutor this SimpleReact instance will use to retry
	 *            concurrent tasks.
	 * @return Lazy SimpleReact for handling infinite streams
	 */
	public static SimpleReact simple(ExecutorService executor, RetryExecutor retry) {
		return SimpleReact.builder().executor(executor).retrier(retry).build();
	}

	/**
	 * @see Stream#of(Object)
	 */
	static <T> SimpleReactStream<T> of(T value) {
		return futureStream((Stream) Seq.of(value));
	}

	/**
	 * @see Stream#of(Object[])
	 */
	@SafeVarargs
	static <T> SimpleReactStream<T> of(T... values) {
		return futureStream((Stream) Seq.of(values),true);
	}

	/**
	 * @see Stream#empty()
	 */
	static <T> SimpleReactStream<T> empty() {
		return futureStream((Stream) Seq.empty());
	}




	
	static <T> SimpleReactStream<T> futureStream(Stream<T> stream){
		return futureStream(stream,true);
	}
	
	/**
	 * Wrap a Stream into a SimpleReactStream.
	 */
	static <T> SimpleReactStream<T> futureStream(Stream<T> stream,boolean eager) {
		if (stream instanceof SimpleReactStream)
			return (SimpleReactStream<T>) stream;
		if (stream instanceof FutureStream)
			stream = ((FutureStream) stream).toQueue().stream();

		return new SimpleReactStreamImpl<T>(
				stream.map(CompletableFuture::completedFuture),
				ThreadPools.getSequential(), RetryBuilder
						.getDefaultInstance().withScheduler(
								ThreadPools.getSequentialRetry()),eager);
	}

	/**
	 * Wrap an Iterable into a FutureStream.
	 */
	static <T> SimpleReactStream<T> futureStream(Iterable<T> iterable) {
		return futureStream(iterable.iterator());
	}

	/**
	 * Wrap an Iterator into a FutureStream.
	 */
	static <T> SimpleReactStream<T> futureStream(Iterator<T> iterator) {
		return futureStream(StreamSupport.stream(
				spliteratorUnknownSize(iterator, ORDERED), false));
	}

	
}
