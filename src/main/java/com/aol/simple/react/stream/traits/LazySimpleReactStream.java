package com.aol.simple.react.stream.traits;

import static java.util.Spliterator.ORDERED;
import static java.util.Spliterators.spliteratorUnknownSize;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
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
import com.aol.simple.react.async.future.FastFuture;
import com.aol.simple.react.async.subscription.Continueable;
import com.aol.simple.react.collectors.lazy.LazyResultConsumer;
import com.aol.simple.react.exceptions.FilteredExecutionPathException;
import com.aol.simple.react.exceptions.SimpleReactFailedStageException;
import com.aol.simple.react.stream.EagerStreamWrapper;
import com.aol.simple.react.stream.LazyStreamWrapper;
import com.aol.simple.react.stream.StageWithResults;
import com.aol.simple.react.stream.ThreadPools;
import com.aol.simple.react.stream.eager.EagerReact;
import com.aol.simple.react.stream.simple.SimpleReact;
import com.aol.simple.react.stream.simple.SimpleReactStreamImpl;
import com.aol.simple.react.stream.traits.operators.StreamCopier;
import com.nurkiewicz.asyncretry.AsyncRetryExecutor;
import com.nurkiewicz.asyncretry.RetryExecutor;
import com.nurkiewicz.asyncretry.policy.AbortRetryException;


public interface LazySimpleReactStream<U> extends  
				BlockingStream<U,FastFuture<U>>, 
				ConfigurableStream<U,FastFuture<U>>, 
				ToQueue<U>,
				SimpleReactStreamInterface<U>{
	

	
	LazySimpleReactStream<U> withTaskExecutor(Executor e);
	LazySimpleReactStream<U> withRetrier(RetryExecutor retry);
//	LazySimpleReactStream<U> withWaitStrategy(Consumer<FastFuture<U>> c);
	LazySimpleReactStream<U> withLazyCollector(LazyResultConsumer<U> lazy);
	LazySimpleReactStream<U> withQueueFactory(QueueFactory<U> queue);
	
	LazySimpleReactStream<U>  withErrorHandler(Optional<Consumer<Throwable>> errorHandler);
	LazySimpleReactStream<U> withSubscription(Continueable sub);
	LazySimpleReactStream<U> withAsync(boolean b);
	
	Continueable getSubscription();
	
	LazySimpleReactStream<U> withLastActive(LazyStreamWrapper streamWrapper);
	abstract LazyStreamWrapper getLastActive();
	
	/* 
	 * React to new events with the supplied function on the supplied Executor
	 * 
	 *	@param fn Apply to incoming events
	 *	@param service Service to execute function on 
	 *	@return next stage in the Stream
	 */
	default <R> LazySimpleReactStream<R> then(final Function<U, R> fn, Executor service) {
		

		
		return (LazySimpleReactStream<R>) this.withLastActive(
				getLastActive().stream(s -> s.map(
						(ft) -> ft.thenApplyAsync(LazySimpleReactStream.<U,R>handleExceptions(fn),getTaskExecutor()))));
	}
	/* 
	 * React to new events with the supplied function on the supplied Executor
	 * 
	 *	@param fn Apply to incoming events
	 *	@param service Service to execute function on 
	 *	@return next stage in the Stream
	 */
	default <R> LazySimpleReactStream<R> thenSync(final Function<U, R> fn) {
		
		
		return (LazySimpleReactStream<R>) this.withLastActive(
				getLastActive().stream(s -> s.map(
						(ft) -> ft.thenApply(LazySimpleReactStream.<U,R>handleExceptions(fn)))));
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
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	default <T, R> LazySimpleReactStream<R> allOf(final Collector collector,
			final Function<T, R> fn) {
		FastFuture[] array = lastActiveArray(getLastActive());
		FastFuture cf = FastFuture.allOf(array);
		Function<Exception, T> f = (Exception e) -> {
			BlockingStreamHelper.capture(e,getErrorHandler());
			return BlockingStreamHelper.block(this,Collectors.toList(),
					new EagerStreamWrapper(Stream.of(array), true));
		};
		FastFuture onFail = cf.exceptionally(f);
		FastFuture onSuccess = onFail.thenApplyAsync((result) -> {
			return new StageWithResults(this.getTaskExecutor(),null, result).submit(() -> (R)fn
					.apply(BlockingStreamHelper.aggregateResults(collector, Stream.of(array)
							.collect(Collectors.toList()),getErrorHandler())));
		}, getTaskExecutor());
		return (LazySimpleReactStream<R>) withLastActive(new EagerStreamWrapper(onSuccess,
				isEager()));

	} */
	/**
	 * React to the completion of any of the events in the previous stage. Will not work reliably with Streams
	 * where filter has been applied in earlier stages. (As Filter completes the Stream for events that are filtered out, they
	 * potentially shortcircuit the completion of the stage).
	 * 
	 * @param fn Function to apply when any of the previous events complete
	 * @return Next stage in the stream
	
	default <R> LazySimpleReactStream<R> anyOf(
			final Function<U, R> fn) {
		FastFuture[] array = lastActiveArray(getLastActive());
		FastFuture cf = FastFuture.anyOf(array);
		FastFuture onSuccess = cf.thenApplyAsync(fn,getTaskExecutor());
		
		return (LazySimpleReactStream<R>) withLastActive(new LazyStreamWrapper(onSuccess,
				isEager()));

	}
	

	

	@SuppressWarnings("rawtypes")
	static FastFuture[] lastActiveArray(LazyStreamWrapper lastActive) {
		List<FastFuture> list = lastActive.stream()
					.collect(Collectors.toList());
			return		list.toArray(new FastFuture[0]);
	}
	 */
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
	default <R> LazySimpleReactStream<R> retry(final Function<U, R> fn) {
		Function<Stream<FastFuture>,Stream<FastFuture>>  mapper = stream -> stream.map(
				(ft) -> ft.thenApplyAsync(res -> 
				getRetrier().getWithRetry( ()->LazySimpleReactStream.<U,R>handleExceptions(fn).apply((U)res)).join(),getTaskExecutor() ));

		return (LazySimpleReactStream<R>) this.withLastActive(getLastActive().stream(mapper));
	}
	
	
	
	default <R> LazySimpleReactStream<R> fromStream(Stream<R> stream) {
		
		
		return (LazySimpleReactStream<R>) this.withLastActive(getLastActive()
				.withNewStream(stream.map(FastFuture::completedFuture),this.getSimpleReact()));
	}
	

	/**
	 * Construct a SimpleReactStream from provided Stream of CompletableFutures
	 * 
	 * @param stream JDK Stream to construct new SimpleReactStream from
	 * @return SimpleReactStream
	
	default <R> LazySimpleReactStream<R> fromStreamOfFutures(
			Stream<FastFuture<R>> stream) {
		Stream noType = stream;
		return (LazySimpleReactStream<R>) this.withLastActive(getLastActive()
				.withNewStream(noType,this.getSimpleReact()));
	}
	default <R> LazySimpleReactStream<R> fromStreamCompletableFutureReplace(
			Stream<CompletableFuture<R>> stream) {
		Stream noType = stream;
		return (LazySimpleReactStream<R>) this.withLastActive(getLastActive()
				.withStream(noType));
	}

	default <R> LazySimpleReactStream<R> fromListCompletableFuture(
			List<CompletableFuture<R>> list) {
		List noType = list;
		return (LazySimpleReactStream<R>) this.withLastActive(getLastActive()
				.withList(noType));
	} */
	
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
	default  <R> LazySimpleReactStream<R> then(final Function<U,R> fn) {
		if(!this.isAsync())
			return thenSync(fn);
		Function<Stream<FastFuture>,Stream<FastFuture>> streamMapper = s ->s.map(ft -> ft.thenApplyAsync(LazySimpleReactStream.<U,R>handleExceptions(fn),getTaskExecutor()));
		return (LazySimpleReactStream<R>) this.withLastActive(getLastActive().stream(streamMapper));
	}
	
	/**
	default List<LazySimpleReactStream<U>> copySimpleReactStream(final int times){
		Stream.of(1,2,3,4).forEach(System.out::println);
		return (List)StreamCopier.toBufferingCopier(getLastActive().stream().iterator(), times)
				.stream()
				.map(it->StreamSupport.stream(Spliterators.spliteratorUnknownSize((Iterator)it, Spliterator.ORDERED), false))
				.<LazySimpleReactStream<U>>map(fs-> this.getSimpleReact().construct((Stream)fs, this.getOriginalFutures()))
				.collect(Collectors.toList());
	}**/
	/**
	 * 
	 * Applies a function to this phase independent on the main flow.
	 * Convenience over taking a reference to this phase and splitting it.
	 * 
	 * @param fn Function to be applied to each completablefuture on completion
	 * @return This phase in Stream
	 */
	default   LazySimpleReactStream<U> doOnEach(final Function<U, U> fn) {
		if(!isAsync())
			return doOnEachSync(fn);
		getLastActive().stream(s ->s.peek(
						(ft) -> ft.thenApplyAsync(LazySimpleReactStream.<U,U>handleExceptions(fn),
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
	default   LazySimpleReactStream<U> doOnEachSync(final Function<U, U> fn) {
		
		getLastActive().stream(s ->s.peek(
						(ft) -> ft.thenApply(LazySimpleReactStream.<U,U>handleExceptions(fn))));
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
	default LazySimpleReactStream<U> peek(final Consumer<? super U> consumer) {
		if(!isAsync())
			return peekSync(consumer);
		return (LazySimpleReactStream<U>) then((t) -> {
			System.out.println("Peeking " + t);
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
	default LazySimpleReactStream<U> peekSync(final Consumer<? super U> consumer) {
		return (LazySimpleReactStream<U>) thenSync((t) -> {
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
	default <R> LazySimpleReactStream<R> flatMapCompletableFuture(
			Function<U, CompletableFuture<R>> flatFn) {
		if(!this.isAsync())
			return flatMapCompletableFutureSync(flatFn);
		Function<Stream<FastFuture>,Stream<FastFuture>> streamMapper = s ->(Stream)s.map(ft -> ft.thenComposeAsync(LazySimpleReactStream.handleExceptions(flatFn),getTaskExecutor()));
		return (LazySimpleReactStream<R>) this.withLastActive(getLastActive().stream(streamMapper));
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
	default <R> LazySimpleReactStream<R> flatMapCompletableFutureSync(
			Function<U, CompletableFuture<R>> flatFn) {
		
		Function<Stream<FastFuture>,Stream<FastFuture>> streamMapper = s ->(Stream)s.map(ft -> ft.thenCompose(LazySimpleReactStream.handleExceptions(flatFn)));
		return (LazySimpleReactStream<R>) this.withLastActive(getLastActive().stream(streamMapper));
	}

	/**
	 * Allows aggregate values in a Stream to be flatten into a single Stream.
	 * flatMap functions turn each aggregate value into it's own Stream, and SimpleReact aggregates those Streams
	 * into a single flattened stream
	 * 
	 * @param flatFn Function that coverts a value (e.g. a Collection) into a Stream
	 * @return SimpleReactStream
	 */
	default <R> LazySimpleReactStream<R> flatMap(
			Function<? super U, ? extends Stream<? extends R>> flatFn) {

		//need to pass in a builder in the constructor and build using it
		return (LazySimpleReactStream)getSimpleReact().construct( Stream.of(), getOriginalFutures()).withSubscription(getSubscription()).withQueueFactory((QueueFactory<Object>) getQueueFactory())	
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
	static <U,R> LazySimpleReactStream<R> bind(LazySimpleReactStream<U> stream,
			Function< U, LazySimpleReactStream<R>> flatFn) {

		return join(stream.then(flatFn));
		
	}
	
	/**
	 * flatten nested SimpleReactStreams
	 * 
	 * @param stream Stream to flatten
	 * @return flattened Stream
	 */
	static <U,R> LazySimpleReactStream<R> join(LazySimpleReactStream<LazySimpleReactStream<U>> stream){
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

		return null;
	/**	return getLastActive()
				.stream()
				.map(FastFuture::toCompletableFuture)
				.map(future -> (CompletableFuture<R>) future.thenApplyAsync(fn,
						getTaskExecutor())).collect(Collectors.toList());**/
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
	default LazySimpleReactStream<U> filter(final Predicate<? super U> p) {
		
		if(!isAsync())
			return filterSync(p);
		Function<Stream<FastFuture>,Stream<FastFuture>> fn = s -> s.map(ft -> ft.thenApplyAsync((in) -> {
			if (!p.test((U) in)) {
				throw new FilteredExecutionPathException();
			}
			return in;
		},getTaskExecutor()));
		return (LazySimpleReactStream<U>) this.withLastActive(getLastActive()
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
	default LazySimpleReactStream<U> filterSync(final Predicate<? super U> p) {
		Function<Stream<FastFuture>,Stream<FastFuture>> fn = s -> s.map(ft -> ft.thenApply((in) -> {
				if (!p.test((U) in)) {
					throw new FilteredExecutionPathException();
				}
				return in;
			}));
			return (LazySimpleReactStream<U>) this.withLastActive(getLastActive()
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
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	default LazySimpleReactStream<U> merge(LazySimpleReactStream<U>... s) {
		List merged = Stream.concat(Stream.of(this),Stream.of(s)).map(stream -> stream.getLastActive().list())
				.flatMap(Collection::stream).collect(Collectors.toList());
		return (LazySimpleReactStream<U>) this.withLastActive(new EagerStreamWrapper(merged));
	} */
	
	 
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
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <R> LazySimpleReactStream<R> merge(LazySimpleReactStream s1, LazySimpleReactStream s2) {
		List merged = Stream.of(s1.getLastActive().list(), s2.getLastActive().list())
				.flatMap(Collection::stream).collect(Collectors.toList());
		return (LazySimpleReactStream<R>) s1.withLastActive(new EagerStreamWrapper(merged));
	}
	 */
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
	default LazySimpleReactStream<U> onFail(final Function<? extends SimpleReactFailedStageException, U> fn) {
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
	default LazySimpleReactStream<U> onFail(Class<? extends Throwable> exceptionClass, final Function<? extends SimpleReactFailedStageException, U> fn){
		
		Function<Stream<FastFuture>,Stream<FastFuture>> mapper = s -> s.map((ft) -> ft.exceptionally((t) -> {
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
		return (LazySimpleReactStream<U>) this.withLastActive(getLastActive()
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
	default LazySimpleReactStream<U> capture(final Consumer<? extends Throwable> errorHandler) {
		return (LazySimpleReactStream)this.withErrorHandler(Optional
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
	default <T, R> LazySimpleReactStream<R> allOf(final Function<List<T>, R> fn) {

		return (LazySimpleReactStream<R>) allOf(Collectors.toList(), (Function<R, U>) fn);

	}

	/**
	 * Convert between an Lazy and Eager future stream,
	 * can be used to take advantages of each approach during a single Stream
	 * 
	 * @return An EagerFutureStream from this LazyFutureStream, will use the same executors
	 */
	default EagerFutureStream<U> convertToEagerStream(){
		return new EagerReact(getTaskExecutor()).withRetrier(getRetrier()).fromStream((Stream)getLastActive().stream());
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
	default LazySimpleReactStream<U> sync(){
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
	default LazySimpleReactStream<U> async(){
		return this.withAsync(true);
	}

	
	
}
