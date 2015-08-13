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
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.jooq.lambda.Seq;

import com.aol.simple.react.RetryBuilder;
import com.aol.simple.react.async.Queue;
import com.aol.simple.react.async.factories.QueueFactory;
import com.aol.simple.react.async.subscription.Continueable;
import com.aol.simple.react.exceptions.FilteredExecutionPathException;
import com.aol.simple.react.exceptions.SimpleReactFailedStageException;
import com.aol.simple.react.stream.StageWithResults;
import com.aol.simple.react.stream.StreamWrapper;
import com.aol.simple.react.stream.ThreadPools;
import com.aol.simple.react.stream.simple.SimpleReact;
import com.aol.simple.react.stream.simple.SimpleReactStreamImpl;
import com.aol.simple.react.stream.traits.ConfigurableStream.SimpleReactConfigurableStream;
import com.nurkiewicz.asyncretry.policy.AbortRetryException;


public interface SimpleReactStream<U> extends  
				BlockingStream<U>, 
				SimpleReactConfigurableStream<U>, 
				ToQueue<U>{
	

	
	Continueable getSubscription();
	
	/* 
	 * React to new events with the supplied function on the supplied Executor
	 * 
	 *	@param fn Apply to incoming events
	 *	@param service Service to execute function on 
	 *	@return next stage in the Stream
	 */
	default <R> SimpleReactStream<R> then(final Function<U, R> fn, Executor service) {
		

		
		return (SimpleReactStream<R>) this.withLastActive(
				getLastActive().stream(s -> s.map(
						(ft) -> ft.thenApplyAsync(SimpleReactStream.<U,R>handleExceptions(fn)))));
	}
	/* 
	 * React to new events with the supplied function on the supplied Executor
	 * 
	 *	@param fn Apply to incoming events
	 *	@param service Service to execute function on 
	 *	@return next stage in the Stream
	 */
	default <R> SimpleReactStream<R> thenSync(final Function<U, R> fn) {
		
		
		return (SimpleReactStream<R>) this.withLastActive(
				getLastActive().stream(s -> s.map(
						(ft) -> ft.thenApply(SimpleReactStream.<U,R>handleExceptions(fn)))));
	}
	
	
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
			BlockingStreamHelper.capture(e,getErrorHandler());
			return BlockingStreamHelper.block(this,Collectors.toList(),
					new StreamWrapper(Stream.of(array), true));
		};
		CompletableFuture onFail = cf.exceptionally(f);
		CompletableFuture onSuccess = onFail.thenApplyAsync((result) -> {
			return new StageWithResults(this.getTaskExecutor(),null, result).submit(() -> (R)fn
					.apply(BlockingStreamHelper.aggregateResults(collector, Stream.of(array)
							.collect(Collectors.toList()),getErrorHandler())));
		}, getTaskExecutor());
		return (SimpleReactStream<R>) withLastActive(new StreamWrapper(onSuccess,
				isEager()));

	}
	/**
	 * React to the completion of any of the events in the previous stage. Will not work reliably with Streams
	 * where filter has been applied in earlier stages. (As Filter completes the Stream for events that are filtered out, they
	 * potentially shortcircuit the completion of the stage).
	 * 
	 * @param fn Function to apply when any of the previous events complete
	 * @return Next stage in the stream
	 */
	default <R> SimpleReactStream<R> anyOf(
			final Function<U, R> fn) {
		CompletableFuture[] array = lastActiveArray(getLastActive());
		CompletableFuture cf = CompletableFuture.anyOf(array);
		CompletableFuture onSuccess = cf.thenApplyAsync(fn,getTaskExecutor());
		
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
		Function<Stream<CompletableFuture>,Stream<CompletableFuture>>  mapper = stream -> stream.map(
				(ft) -> ft.thenApplyAsync(res -> 
				getRetrier().getWithRetry( ()->SimpleReactStream.<U,R>handleExceptions(fn).apply((U)res)).join(),getTaskExecutor() ));

		return (SimpleReactStream<R>) this.withLastActive(getLastActive().stream(mapper));
	}
	
	
	
	default <R> SimpleReactStream<R> fromStream(Stream<R> stream) {
		
		
		return (SimpleReactStream<R>) this.withLastActive(getLastActive()
				.withNewStream(stream.map(CompletableFuture::completedFuture)));
	}
	

	/**
	 * Construct a SimpleReactStream from provided Stream of CompletableFutures
	 * 
	 * @param stream JDK Stream to construct new SimpleReactStream from
	 * @return SimpleReactStream
	 */
	default <R> SimpleReactStream<R> fromStreamOfFutures(
			Stream<CompletableFuture<R>> stream) {
		Stream noType = stream;
		return (SimpleReactStream<R>) this.withLastActive(getLastActive()
				.withNewStream(noType));
	}
	default <R> SimpleReactStream<R> fromStreamCompletableFutureReplace(
			Stream<CompletableFuture<R>> stream) {
		Stream noType = stream;
		return (SimpleReactStream<R>) this.withLastActive(getLastActive()
				.withStream(noType));
	}

	default <R> SimpleReactStream<R> fromListCompletableFuture(
			List<CompletableFuture<R>> list) {
		List noType = list;
		return (SimpleReactStream<R>) this.withLastActive(getLastActive()
				.withList(noType));
	}
	
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
	default  <R> SimpleReactStream<R> then(final Function<U,R> fn) {
		if(!this.isAsync())
			return thenSync(fn);
		Function<Stream<CompletableFuture>,Stream<CompletableFuture>> streamMapper = s ->s.map(ft -> ft.thenApplyAsync(SimpleReactStream.<U,R>handleExceptions(fn),getTaskExecutor()));
		return (SimpleReactStream<R>) this.withLastActive(getLastActive().stream(streamMapper));
	}
	
	/**
	 * 
	 * Applies a function to this phase independent on the main flow.
	 * Convenience over taking a reference to this phase and splitting it.
	 * 
	 * @param fn Function to be applied to each completablefuture on completion
	 * @return This phase in Stream
	 */
	default   SimpleReactStream<U> doOnEach(final Function<U, U> fn) {
		if(!isAsync())
			return doOnEachSync(fn);
		getLastActive().stream(s ->s.peek(
						(ft) -> ft.thenApplyAsync(SimpleReactStream.<U,U>handleExceptions(fn),
								getTaskExecutor())));
		return this;
	}
	/**
	 * 
	 * Applies a function to this phase independent on the main flow, continues on the currently executing thread.
	 * Convenience over taking a reference to this phase and splitting it.
	 * 
	 * @param fn Function to be applied to each completablefuture on completion
	 * @return This phase in Stream
	 */
	default   SimpleReactStream<U> doOnEachSync(final Function<U, U> fn) {
		
		getLastActive().stream(s ->s.peek(
						(ft) -> ft.thenApply(SimpleReactStream.<U,U>handleExceptions(fn))));
		return this;
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
		if(!isAsync())
			return peekSync(consumer);
		return (SimpleReactStream<U>) then((t) -> {
			consumer.accept(t);
			return (U) t;
		});
	}
	
	
	
	/**
	 * Synchronous peek operator
	 * 
	 * @param consumer Peek consumer
	 * @return Next stage
	 */
	default SimpleReactStream<U> peekSync(final Consumer<? super U> consumer) {
		return (SimpleReactStream<U>) thenSync((t) -> {
			consumer.accept(t);
			return (U) t;
		});
	}

	static <U,R> Function<U, R> handleExceptions(Function<U, R> fn) {
		return (input) -> {
			try {
				return fn.apply(input);
			} catch (Throwable t) {
				if(t instanceof AbortRetryException)//special case for retry
					throw t;
				throw new SimpleReactFailedStageException(input, t);

			}
		};
	}
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
	default <R> SimpleReactStream<R> flatMapCompletableFuture(
			Function<U, CompletableFuture<R>> flatFn) {
		if(!this.isAsync())
			return flatMapCompletableFutureSync(flatFn);
		Function<Stream<CompletableFuture>,Stream<CompletableFuture>> streamMapper = s ->(Stream)s.map(ft -> ft.thenComposeAsync(SimpleReactStream.handleExceptions(flatFn),getTaskExecutor()));
		return (SimpleReactStream<R>) this.withLastActive(getLastActive().stream(streamMapper));
	}
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
	default <R> SimpleReactStream<R> flatMapCompletableFutureSync(
			Function<U, CompletableFuture<R>> flatFn) {
		
		Function<Stream<CompletableFuture>,Stream<CompletableFuture>> streamMapper = s ->(Stream)s.map(ft -> ft.thenCompose(SimpleReactStream.handleExceptions(flatFn)));
		return (SimpleReactStream<R>) this.withLastActive(getLastActive().stream(streamMapper));
	}

	/**
	 * Allows aggregate values in a Stream to be flatten into a single Stream.
	 * flatMap functions turn each aggregate value into it's own Stream, and SimpleReact aggregates those Streams
	 * into a single flattened stream
	 * 
	 * @param flatFn Function that coverts a value (e.g. a Collection) into a Stream
	 * @return SimpleReactStream
	 */
	default <R> SimpleReactStream<R> flatMap(
			Function<? super U, ? extends Stream<? extends R>> flatFn) {

		//need to pass in a builder in the constructor and build using it
		return (SimpleReactStream)getSimpleReact().construct( Stream.of(), getOriginalFutures()).withSubscription(getSubscription()).withQueueFactory((QueueFactory<Object>) getQueueFactory())	
				.fromStream(
						toQueue()
								.stream(getSubscription())
								.flatMap(flatFn));
	}
	
	
	/**
	 * 
	 * flatMap / bind implementation that returns the correct type (SimpleReactStream)
	 * 
	 * @param stream Stream to flatMap
	 * @param flatFn flatMap function
	 * @return
	 */
	static <U,R> SimpleReactStream<R> bind(SimpleReactStream<U> stream,
			Function< U, SimpleReactStream<R>> flatFn) {

		return join(stream.then(flatFn));
		
	}
	
	/**
	 * flatten nested SimpleReactStreams
	 * 
	 * @param stream Stream to flatten
	 * @return flattened Stream
	 */
	static <U,R> SimpleReactStream<R> join(SimpleReactStream<SimpleReactStream<U>> stream){
		Queue queue =  stream.getQueueFactory().build();
		stream.then(it -> it.sync().then(queue::offer)).allOf(it ->queue.close());
		 return stream.fromStream(queue.stream(stream.getSubscription()));
	
	}
	
	

	List getOriginalFutures();

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
	default <R> List<CompletableFuture<R>> with(final Function<U, R> fn) {

		return getLastActive()
				.stream()
				.map(future -> (CompletableFuture<R>) future.thenApplyAsync(fn,
						getTaskExecutor())).collect(Collectors.toList());
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
		if(!isAsync())
			return filterSync(p);
	Function<Stream<CompletableFuture>,Stream<CompletableFuture>> fn = s -> s.map(ft -> ft.thenApplyAsync((in) -> {
			if (!p.test((U) in)) {
				throw new FilteredExecutionPathException();
			}
			return in;
		}));
		return (SimpleReactStream<U>) this.withLastActive(getLastActive()
				.stream(fn));

	}
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
	default SimpleReactStream<U> filterSync(final Predicate<? super U> p) {
		Function<Stream<CompletableFuture>,Stream<CompletableFuture>> fn = s -> s.map(ft -> ft.thenApply((in) -> {
				if (!p.test((U) in)) {
					throw new FilteredExecutionPathException();
				}
				return in;
			}));
			return (SimpleReactStream<U>) this.withLastActive(getLastActive()
					.stream(fn));

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
	default SimpleReactStream<U> onFail(final Function<? extends SimpleReactFailedStageException, U> fn) {
		return onFail(Throwable.class,fn);
	}

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
	default SimpleReactStream<U> onFail(Class<? extends Throwable> exceptionClass, final Function<? extends SimpleReactFailedStageException, U> fn){
		
		Function<Stream<CompletableFuture>,Stream<CompletableFuture>> mapper = s -> s.map((ft) -> ft.exceptionally((t) -> {
			if (t instanceof FilteredExecutionPathException)
				throw (FilteredExecutionPathException) t;
			Throwable throwable =(Throwable)t;
			if(t instanceof CompletionException)
				throwable = ((Exception)t).getCause();
			
			SimpleReactFailedStageException simpleReactException = assureSimpleReactException( throwable);//exceptions from initial supplier won't be wrapper in SimpleReactFailedStageException
			if(exceptionClass.isAssignableFrom(simpleReactException.getCause().getClass()))
		    	return ((Function) fn).apply(simpleReactException);
		    throw simpleReactException;
				
		}));
		return (SimpleReactStream<U>) this.withLastActive(getLastActive()
				.stream(mapper));
	}
	

	static SimpleReactFailedStageException assureSimpleReactException(
			Throwable throwable){
		if(throwable instanceof SimpleReactFailedStageException)
			return (SimpleReactFailedStageException)throwable;
		return new SimpleReactFailedStageException(null,(throwable));
	}
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
		return new SimpleReact().of(Arrays.asList(array));
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
	public static SimpleReact parallelBuilder(int parallelism) {
		return SimpleReact.builder().executor(new ForkJoinPool(parallelism))
				.retrier(new RetryBuilder().parallelism(parallelism)).build();
	}

	/**
	 * @return new eager SimpleReact builder configured with standard parallel executor
	 * By default this is the ForkJoinPool common instance but is configurable in the ThreadPools class
	 * 
	 * @see ThreadPools#getStandard()
	 * see RetryBuilder#getDefaultInstance()
	 */
	public static SimpleReact parallelCommonBuilder() {
		return SimpleReact.builder().executor(ThreadPools.getStandard()).async(true)
		.retrier(RetryBuilder.getDefaultInstance().withScheduler(ThreadPools.getCommonFreeThreadRetry())).build();
		
	}

	/**
	 * @return new eager SimpleReact builder configured to run on a separate thread (non-blocking current thread), sequentially
	 * New ForkJoinPool will be created
	 */
	public static SimpleReact sequentialBuilder() {
		return SimpleReact.builder().async(false).executor(new ForkJoinPool(1))
				.retrier(RetryBuilder.getDefaultInstance().withScheduler(Executors.newScheduledThreadPool(1))).build();
	}

	/**
	 * @return new eager SimpleReact builder configured to run on a separate thread (non-blocking current thread), sequentially
	 * Common free thread Executor from
	 */
	public static SimpleReact sequentialCommonBuilder() {
		return SimpleReact.builder().async(false).executor(ThreadPools.getCommonFreeThread())
				.retrier(RetryBuilder.getDefaultInstance().withScheduler(ThreadPools.getCommonFreeThreadRetry())).build();
	}

	/**
	 *  Create a parallel asynchronous stream
	 * @see Stream#of(Object)
	 */
	static <T> SimpleReactStream<T> react(Supplier<T> value) {
		return  new SimpleReact().react(value);
	}

	/**
	 * Create a parallel asynchronous stream
	 * @see Stream#of(Object[])
	 */
	@SafeVarargs
	static <T> SimpleReactStream<T> react(Supplier<T>... values) {
		return  new SimpleReact().react(values);
	}
	
	/**
	 * @see Stream#of(Object)
	 */
	static <T> SimpleReactStream<T> of(T value) {
		return of((Stream) Seq.of(value));
	}

	/**
	 * @see Stream#of(Object[])
	 */
	@SafeVarargs
	static <T> SimpleReactStream<T> of(T... values) {
		return of((Stream) Seq.of(values));
	}

	/**
	 * @see Stream#empty()
	 */
	static <T> SimpleReactStream<T> empty() {
		return of((Stream) Seq.empty());
	}




	
	
	
	/**
	 * Wrap a Stream into a SimpleReactStream.
	 */
	static <T> SimpleReactStream<T> of(Stream<T> stream) {
		
		if (stream instanceof FutureStream)
			stream = ((FutureStream) stream).toQueue().stream(((FutureStream) stream).getSubscription());

		SimpleReact sr = new SimpleReact(ThreadPools.getSequential(), RetryBuilder
				.getDefaultInstance().withScheduler(
						ThreadPools.getSequentialRetry()),false);
		return new SimpleReactStreamImpl<T>(sr,
				stream.map(CompletableFuture::completedFuture),null);
	}

	/**
	 * Wrap an Iterable into a FutureStream.
	 */
	static <T> SimpleReactStream<T> ofIterable(Iterable<T> iterable) {
		return of(iterable.iterator());
	}

	/**
	 * Wrap an Iterator into a FutureStream.
	 */
	static <T> SimpleReactStream<T> of(Iterator<T> iterator) {
		return of(StreamSupport.stream(
				spliteratorUnknownSize(iterator, ORDERED), false));
	}

	/* 
	 * Execute subsequent stages on the completing thread (until async called)
	 * 10X faster than async execution.
	 * Use async for blocking IO or distributing work across threads or cores.
	 * Switch to sync for non-blocking tasks when desired thread utlisation reached
	 * 
	 *	@return Version of FutureStream that will use sync CompletableFuture methods
	 * 
	 */
	default SimpleReactStream<U> sync(){
		return this.withAsync(false);
	}
	
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
	default SimpleReactStream<U> async(){
		return this.withAsync(true);
	}

	
}
