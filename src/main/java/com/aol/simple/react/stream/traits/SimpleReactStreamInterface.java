package com.aol.simple.react.stream.traits;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Stream;

import com.aol.simple.react.async.Queue;
import com.aol.simple.react.exceptions.SimpleReactFailedStageException;


public interface SimpleReactStreamInterface<U>{

	
	
	
	
	
	/* 
	 * React to new events with the supplied function on the supplied Executor
	 * 
	 *	@param fn Apply to incoming events
	 *	@param service Service to execute function on 
	 *	@return next stage in the Stream
	 */
	<R> SimpleReactStreamInterface<R> then(final Function<U, R> fn, Executor service);
	/* 
	 * React to new events with the supplied function on the supplied Executor
	 * 
	 *	@param fn Apply to incoming events
	 *	@param service Service to execute function on 
	 *	@return next stage in the Stream
	 */
	<R> SimpleReactStreamInterface<R> thenSync(final Function<U, R> fn);
	
	
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
	 <T, R> SimpleReactStreamInterface<R> allOf(final Collector collector,
			final Function<T, R> fn) ;
	/**
	 * React to the completion of any of the events in the previous stage. Will not work reliably with Streams
	 * where filter has been applied in earlier stages. (As Filter completes the Stream for events that are filtered out, they
	 * potentially shortcircuit the completion of the stage).
	 * 
	 * @param fn Function to apply when any of the previous events complete
	 * @return Next stage in the stream
	 */
	 <R> SimpleReactStreamInterface<R> anyOf(
			final Function<U, R> fn) ;

	

	
	
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
	<R> SimpleReactStreamInterface<R> retry(final Function<U, R> fn);
	
	
	
	<R> SimpleReactStreamInterface<R> fromStream(Stream<R> stream);
	

	/**
	 * Construct a SimpleReactStream from provided Stream of CompletableFutures
	 * 
	 * @param stream JDK Stream to construct new SimpleReactStream from
	 * @return SimpleReactStream
	 */
	<R> SimpleReactStreamInterface<R> fromStreamOfFutures(
			Stream<CompletableFuture<R>> stream);
	<R> SimpleReactStreamInterface<R> fromStreamCompletableFutureReplace(
			Stream<CompletableFuture<R>> stream);

	<R> SimpleReactStreamInterface<R> fromListCompletableFuture(
			List<CompletableFuture<R>> list);
	
	/**
	 * React <b>then</b>
	 * 
	 * 
	 * 
	 * Unlike 'with' this method is fluent, and returns another Stage Builder
	 * that can represent the next stage in the dataflow.
	 * 
	 * <pre>
	 * {@code 
	 	new SimpleReact().<Integer, Integer> react(() -> 1, () -> 2, () -> 3)
				.then((it) -> it * 100)
				.then((it) -> "*" + it)
				
				}
	</pre>
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
	<R> SimpleReactStreamInterface<R> then(final Function<U,R> fn) ;
	
	List<SimpleReactStreamInterface<U>> copySimpleReactStream(final int times);
	/**
	 * 
	 * Applies a function to this phase independent on the main flow.
	 * Convenience over taking a reference to this phase and splitting it.
	 * 
	 * @param fn Function to be applied to each completablefuture on completion
	 * @return This phase in Stream
	 */
	SimpleReactStreamInterface<U> doOnEach(final Function<U, U> fn) ;
	/**
	 * 
	 * Applies a function to this phase independent on the main flow, continues on the currently executing thread.
	 * Convenience over taking a reference to this phase and splitting it.
	 * 
	 * @param fn Function to be applied to each completablefuture on completion
	 * @return This phase in Stream
	 */
	SimpleReactStreamInterface<U> doOnEachSync(final Function<U, U> fn);
	/**
	 * Peek asynchronously at the results in the current stage. Current results
	 * are passed through to the next stage.
	 * 
	 * @param consumer
	 *            That will recieve current results
	 * @return A new builder object that can be used to define the next stage in
	 *         the dataflow
	 */
	SimpleReactStreamInterface<U> peek(final Consumer<? super U> consumer);
	
	
	
	/**
	 * Synchronous peek operator
	 * 
	 * @param consumer Peek consumer
	 * @return Next stage
	 */
	 SimpleReactStreamInterface<U> peekSync(final Consumer<? super U> consumer);

	
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
	<R> SimpleReactStreamInterface<R> flatMapCompletableFuture(
			Function<U, CompletableFuture<R>> flatFn);
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
	<R> SimpleReactStreamInterface<R> flatMapCompletableFutureSync(
			Function<U, CompletableFuture<R>> flatFn) ;

	/**
	 * Allows aggregate values in a Stream to be flatten into a single Stream.
	 * flatMap functions turn each aggregate value into it's own Stream, and SimpleReact aggregates those Streams
	 * into a single flattened stream
	 * 
	 * @param flatFn Function that coverts a value (e.g. a Collection) into a Stream
	 * @return SimpleReactStream
	 */
	 <R> SimpleReactStreamInterface<R> flatMap(
			Function<? super U, ? extends Stream<? extends R>> flatFn);
	
	
	
	
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
	 * <pre>
	 	{@code 
	 	List<CompletableFuture<Integer>> futures = new SimpleReact().<Integer, Integer> react(() -> 1, () -> 2, () -> 3)
									.with((it) -> it * 100);
		}
		</pre>
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
	<R> List<CompletableFuture<R>> with(final Function<U, R> fn);
	
	
	
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
	SimpleReactStreamInterface<U> filter(final Predicate<? super U> p);
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
	SimpleReactStreamInterface<U> filterSync(final Predicate<? super U> p) ;
	/**
	 * @return A Stream of CompletableFutures that represent this stage in the
	 *         dataflow
	 */
	@SuppressWarnings({ "unchecked" })
	<T> Stream<CompletableFuture<T>> streamCompletableFutures();
	
	/*
	  * Merge two simple-react Streams, by merging the Stream of underlying
	 * futures - not suitable for merging infinite Streams - use   
	 * see LazyFutureStream#switchOnNext for infinite Streams
	 * 
	 * <pre>
	 * {@code 
	 * List<String> result = 	SimpleReactStream.of(1,2,3)
	 * 											 .merge(LazyFutureStream.of(100,200,300))
												  .map(it ->it+"!!")
												  .toList();
		assertThat(result,equalTo(Arrays.asList("1!!","2!!","3!!","100!!","200!!","300!!")));
	 * 
	 * }
	 * </pre>
	 * 
	 * @param s Stream to merge
	 * 
	 * @return Next stage in stream
	 * 
	 * @see
	 * com.aol.simple.react.stream.traits.FutureStream#merge(com.aol.simple.
	 * react.stream.traits.SimpleReactStream)
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	SimpleReactStreamInterface<U> merge(SimpleReactStreamInterface<U>... s);
	
	 
	
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
					.then(it -> {
						if (it == 100)
							throw new RuntimeException("boo!");
			
						return it;
					})
					.onFail(e -> 1)
					.then(it -> "*" + it)
					.block();	  
	  
	  
	  
	  }
	  
		  </pre>
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
	SimpleReactStreamInterface<U> onFail(final Function<? extends SimpleReactFailedStageException, U> fn) ;

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
	SimpleReactStreamInterface<U> onFail(Class<? extends Throwable> exceptionClass, final Function<? extends SimpleReactFailedStageException, U> fn);
	

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
			.then(it -> it * 100)
			.then(it -> {
				if (it == 100)
					throw new RuntimeException("boo!");
	
				return it;
			})
			.onFail(e -> 1)
			.then(it -> "*" + it)
			.then(it -> {
				
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
	SimpleReactStreamInterface<U> capture(final Consumer<? extends Throwable> errorHandler);
	
	

	/**
	 * React and <b>allOf</b>
	 * 
	 * allOf is a non-blocking equivalent of block. The current thread is not
	 * impacted by the calculations, but the reactive chain does not continue
	 * until all currently alloted tasks complete. The allOf task is then
	 * provided with a list of the results from the previous tasks in the chain.
	 * 
	 * <pre>
	  {@code
	  boolean blocked[] = {false};
		new SimpleReact().<Integer, Integer> react(() -> 1, () -> 2, () -> 3)
				
				.then(it -> {
					try {
						Thread.sleep(50000);
					} catch (Exception e) {
						
					}
					blocked[0] =true;
					return 10;
				})
				.allOf( it -> it.size());
		
		assertThat(blocked[0],is(false));
	  
	  }
		</pre>
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
	<T, R> SimpleReactStreamInterface<R> allOf(final Function<List<T>, R> fn);

	/**
	 * Convert between an Lazy and Eager future stream,
	 * can be used to take advantages of each approach during a single Stream
	 * 
	 * @return An EagerFutureStream from this LazyFutureStream, will use the same executors
	 */
	EagerFutureStream<U> convertToEagerStream();
	
	
	/* 
	 * Execute subsequent stages on the completing thread (until async called)
	 * 10X faster than async execution.
	 * Use async for blocking IO or distributing work across threads or cores.
	 * Switch to sync for non-blocking tasks when desired thread utlisation reached
	 * 
	 *	@return Version of FutureStream that will use sync CompletableFuture methods
	 * 
	 */
	 SimpleReactStreamInterface<U> sync();
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
	SimpleReactStreamInterface<U> async();
	Queue<U> toQueue();

	
	
}