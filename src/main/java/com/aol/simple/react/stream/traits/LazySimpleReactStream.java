package com.aol.simple.react.stream.traits;

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
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.aol.simple.react.async.Queue;
import com.aol.simple.react.async.factories.QueueFactory;
import com.aol.simple.react.async.future.FastFuture;
import com.aol.simple.react.async.future.PipelineBuilder;
import com.aol.simple.react.async.subscription.Continueable;
import com.aol.simple.react.collectors.lazy.LazyResultConsumer;
import com.aol.simple.react.exceptions.FilteredExecutionPathException;
import com.aol.simple.react.exceptions.SimpleReactFailedStageException;
import com.aol.simple.react.stream.EagerStreamWrapper;
import com.aol.simple.react.stream.LazyStreamWrapper;
import com.aol.simple.react.stream.eager.EagerReact;
import com.aol.simple.react.stream.traits.operators.StreamCopier;
import com.nurkiewicz.asyncretry.RetryExecutor;
import com.nurkiewicz.asyncretry.policy.AbortRetryException;


public interface LazySimpleReactStream<U> extends  
				BlockingStream<U>, 
				ConfigurableStream<U,FastFuture<U>>, 
				ToQueue<U>,
				SimpleReactStream<U>{
	

	
	LazySimpleReactStream<U> withTaskExecutor(Executor e);
	LazySimpleReactStream<U> withRetrier(RetryExecutor retry);
	
	LazySimpleReactStream<U> withQueueFactory(QueueFactory<U> queue);
	
	LazySimpleReactStream<U>  withErrorHandler(Optional<Consumer<Throwable>> errorHandler);
	LazySimpleReactStream<U> withSubscription(Continueable sub);
	LazySimpleReactStream<U> withAsync(boolean b);
	
	Continueable getSubscription();
	
	<R> LazySimpleReactStream<R> withLastActive(LazyStreamWrapper<R> streamWrapper);
	abstract LazyStreamWrapper<U> getLastActive();
	
	
	
	/* 
	 * React to new events with the supplied function on the supplied Executor
	 * 
	 *	@param fn Apply to incoming events
	 *	@param service Service to execute function on 
	 *	@return next stage in the Stream
	 */
	default <R> LazySimpleReactStream<R> then(final Function<U, R> fn, Executor service) {
		

		
		return this.withLastActive(
				getLastActive().operation(
						(ft) -> ft.thenApplyAsync(LazySimpleReactStream.<U,R>handleExceptions(fn),service)));
	}
	/* 
	 * React to new events with the supplied function on the supplied Executor
	 * 
	 *	@param fn Apply to incoming events
	 *	@param service Service to execute function on 
	 *	@return next stage in the Stream
	 */
	@SuppressWarnings("unchecked")
	default <R> LazySimpleReactStream<R> thenSync(final Function<U, R> fn) {
		
		
		return  this.withLastActive(
				getLastActive().operation(
						(ft) -> ft.thenApply(LazySimpleReactStream.<U,R>handleExceptions(fn))));
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
	default <R> LazySimpleReactStream<R> retry(final Function<U, R> fn) {
		Function<PipelineBuilder,PipelineBuilder> mapper =
				(ft) -> ft.thenApplyAsync(res -> 
				getRetrier().getWithRetry( ()->LazySimpleReactStream.<U,R>handleExceptions(fn)
						.apply((U)res)).join(),getTaskExecutor() );

		return  this.withLastActive(getLastActive().operation(mapper));
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
	default  <R> LazySimpleReactStream<R> then(final Function<U,R> fn) {
		if(!this.isAsync())
			return thenSync(fn);
		Function<PipelineBuilder,PipelineBuilder> streamMapper = ft -> ft.thenApplyAsync(LazySimpleReactStream.<U,R>handleExceptions(fn),getTaskExecutor());
		return (LazySimpleReactStream<R>) this.withLastActive(getLastActive().operation(streamMapper));
	}
	
	
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
		getLastActive().operation(
						(ft) -> ft.thenApplyAsync(LazySimpleReactStream.<U,U>handleExceptions(fn),
								getTaskExecutor()));
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
		
		getLastActive().operation(
						(ft) -> ft.thenApply(LazySimpleReactStream.<U,U>handleExceptions(fn)));
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
		Function<PipelineBuilder,PipelineBuilder> streamMapper = ft -> ft.thenComposeAsync(LazySimpleReactStream.handleExceptions(flatFn),getTaskExecutor());
		return  this.withLastActive(getLastActive().operation(streamMapper));
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
		
		Function<PipelineBuilder,PipelineBuilder> streamMapper = ft -> ft.thenCompose(LazySimpleReactStream.handleExceptions(flatFn));
		return  this.withLastActive(getLastActive().operation(streamMapper));
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
		return (LazySimpleReactStream)getSimpleReact().construct( Stream.of()).withSubscription(getSubscription()).withQueueFactory((QueueFactory<Object>) getQueueFactory())	
				.fromStream(
						toQueue()
								.stream(getSubscription())
								.flatMap(flatFn));
	}
	
	default List<SimpleReactStream<U>> copySimpleReactStream(final int times){
		
		return (List)StreamCopier.toBufferingCopier(iterator(), times)
				.stream()
				.map(it->StreamSupport.stream(Spliterators.spliteratorUnknownSize((Iterator)it, Spliterator.ORDERED), false))
				.<SimpleReactStream<U>>map(fs-> (SimpleReactStream)this.getSimpleReact().construct((Stream)fs))
				.collect(Collectors.toList());
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
		Function<PipelineBuilder,PipelineBuilder> fn = ft -> ft.thenApplyAsync((in) -> {
			if (!p.test((U) in)) {
				throw new FilteredExecutionPathException();
			}
			return in;
		},getTaskExecutor());
		return  this.withLastActive(getLastActive()
				.operation(fn));

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
		Function<PipelineBuilder,PipelineBuilder> fn =  ft -> ft.thenApply((in) -> {
				if (!p.test((U) in)) {
					throw new FilteredExecutionPathException();
				}
				return in;
			});
			return  this.withLastActive(getLastActive()
					.operation(fn));

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
	default LazySimpleReactStream<U> onFail(final Function<SimpleReactFailedStageException, U> fn) {
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
	default LazySimpleReactStream<U> onFail(Class<? extends Throwable> exceptionClass, final Function<SimpleReactFailedStageException, U> fn){
		
		Function<PipelineBuilder,PipelineBuilder> mapper =(ft) -> ft.exceptionally((t) -> {
			if (t instanceof FilteredExecutionPathException)
				throw (FilteredExecutionPathException) t;
			Throwable throwable =(Throwable)t;
			if(t instanceof CompletionException)
				throwable = ((Exception)t).getCause();
			
			SimpleReactFailedStageException simpleReactException = assureSimpleReactException( throwable);//exceptions from initial supplier won't be wrapper in SimpleReactFailedStageException
			if(exceptionClass.isAssignableFrom(simpleReactException.getCause().getClass()))
		    	return  fn.apply((SimpleReactFailedStageException)simpleReactException);
		    throw simpleReactException;
				
		});
		return this.withLastActive(getLastActive()
				.operation(mapper));
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
		return this.withErrorHandler(Optional
				.of((Consumer<Throwable>) errorHandler));
	}
	
	




	
}
