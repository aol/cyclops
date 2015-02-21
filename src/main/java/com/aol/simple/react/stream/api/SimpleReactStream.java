package com.aol.simple.react.stream.api;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.aol.simple.react.exceptions.FilteredExecutionPathException;
import com.aol.simple.react.exceptions.SimpleReactFailedStageException;
import com.aol.simple.react.stream.StreamWrapper;


public interface SimpleReactStream <U>{
	
	
	abstract StreamWrapper getLastActive();
	abstract FutureStream<U> withLastActive(StreamWrapper streamWrapper);
	SimpleReactStream<U>  withErrorHandler(Optional<Consumer<Throwable>> errorHandler);
	abstract ExecutorService getTaskExecutor();
	
	
	public <T, R> FutureStream<R> allOf(final Collector collector,
			final Function<T, R> fn);
		
	
	public <R> FutureStream<R> then(final Function<U, R> fn);
	public <R> FutureStream<R> flatMap(
			Function<? super U, ? extends Stream<? extends R>> flatFn);
	public <R> FutureStream<R> retry(final Function<U, R> fn);
	

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
	default FutureStream<U> peek(final Consumer<? super U> consumer) {
		return (FutureStream<U>) then((t) -> {
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
	default FutureStream<U> filter(final Predicate<? super U> p) {

		return (FutureStream<U>) this.withLastActive(getLastActive().permutate(getLastActive()
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
	default FutureStream<U> merge(FutureStream<U> s) {
		List merged = Stream.of(this.getLastActive().list(), s.getLastActive().list())
				.flatMap(Collection::stream).collect(Collectors.toList());
		return (FutureStream<U>) this.withLastActive(new StreamWrapper(merged));
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
	default <U> FutureStream<U> onFail(final Function<? extends SimpleReactFailedStageException, U> fn) {
		return (FutureStream<U>) this.withLastActive(getLastActive().permutate(getLastActive()
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
	default FutureStream<U> capture(final Consumer<? extends Throwable> errorHandler) {
		return (FutureStream)this.withErrorHandler(Optional
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
	default <T, R> FutureStream<R> allOf(final Function<List<T>, R> fn) {

		return (FutureStream<R>) allOf(Collectors.toList(), (Function<R, U>) fn);

	}


	
}
