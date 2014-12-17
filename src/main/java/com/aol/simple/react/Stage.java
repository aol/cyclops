package com.aol.simple.react;

import static java.util.Arrays.asList;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Wither;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * An Immutable Builder Class that represents a stage in a SimpleReact Dataflow.
 * Chain stages together to build complext reactive dataflows.
 * 
 * Access the underlying CompletableFutures via the 'with' method. Return to
 * using JDK Collections and (parrellel) Streams via 'allOf' or 'block'
 * 
 * @author johnmcclean
 *
 * @param <T>
 *            Input parameter for this stage
 * @param <U>
 *            Return parameter for this stage
 */

//lombok annotations to aid Immutability (Wither and AllArgsConstructor)
@Wither
@AllArgsConstructor(access=AccessLevel.PACKAGE)
@Slf4j
public class Stage<T, U> {

	private final ExceptionSoftener exceptionSoftener = ExceptionSoftener.singleton.factory.getInstance();
	private final ExecutorService taskExecutor;
	@SuppressWarnings("rawtypes")
	private final List<CompletableFuture> lastActive;
	private final Optional<Consumer<Throwable>> errorHandler;
	@Getter
	private final Optional<U> results;

	/**
	 * 
	 * Construct a SimpleReact stage - this acts as a fluent SimpleReact builder
	 * 
	 * @param stream
	 *            Stream that will generate the events that will be reacted to.
	 * @param executor
	 *            The next stage's tasks will be submitted to this executor
	 */
	Stage(final Stream<CompletableFuture<T>> stream,
			final ExecutorService executor) {

		this.taskExecutor = executor;
		this.lastActive = stream.collect(Collectors.toList());
		this.errorHandler = Optional.of( (e)-> log.error(e.getMessage(),e));
		this.results = Optional.empty();
	}
	
	/**
	 * @return Results for this stage in the dataflow
	 */
	public <U> Optional<U> getResults(){
		return (Optional<U>)results;
	}
	
	/**
	 * @return Unwrapped results for this stage in the dataflow - may be null
	 */
	public <U> U extractResults(){
		if(!results.isPresent())
			return null;
		return (U)results.get();
	}

	
	/**
	 * This method allows the SimpleReact ExecutorService to be reused by JDK parallel streams
	 * 
	 * @param callable that contains code
	 */
	public <R> R submit(Function <Optional<U>,R> fn){
		if(taskExecutor instanceof ForkJoinPool){
			log.debug("Submited callable to SimpleReact ForkJoinPool. JDK ParallelStreams will reuse SimpleReact ForkJoinPool.");
			try {
				return (R)taskExecutor.submit(()-> (R)fn.apply(this.results)).get();
			} catch (ExecutionException e) {
				exceptionSoftener.throwSoftenedException(e);
				throw new RuntimeException(e);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				exceptionSoftener.throwSoftenedException(e);
				throw new RuntimeException(e);
			}
		}
		try {
			log.debug("Submited callable but do not have a ForkJoinPool. JDK ParallelStreams will use Common ForkJoinPool not SimpleReact ExecutorService.");
			return (R)fn.apply(this.results);
		} catch (Exception e) {
			throw new RuntimeException(e); 
		}
	}
	
	/**
	 * This method allows the SimpleReact ExecutorService to be reused by JDK parallel streams
	 * 
	 * @param callable that contains code
	 */
	public <T> T submit(Callable<T> callable){
		if(taskExecutor instanceof ForkJoinPool){
			log.debug("Submited callable to SimpleReact ForkJoinPool. JDK ParallelStreams will reuse SimpleReact ForkJoinPool.");
			try {
				return taskExecutor.submit(callable).get();
			} catch (ExecutionException e) {
				exceptionSoftener.throwSoftenedException(e);
				throw new RuntimeException(e);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				exceptionSoftener.throwSoftenedException(e);
				throw new RuntimeException(e);
			}
		}
		try {
			log.debug("Submited callable but do not have a ForkJoinPool. JDK ParallelStreams will use Common ForkJoinPool not SimpleReact ExecutorService.");
			return callable.call();
		} catch (Exception e) {
			throw new RuntimeException(e); 
		}
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
	 	List<CompletableFuture<Integer>> futures = SimpleReact.<Integer, Integer> react(() -> 1, () -> 2, () -> 3)
				.with((it) -> it * 100);
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
	public List<CompletableFuture<U>> with(
			final Function<T, ? extends Object> fn) {

		return lastActive
				.stream()
				.map(future -> (CompletableFuture<U>) future.thenApplyAsync(fn,
						taskExecutor)).collect(Collectors.toList());
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
	  SimpleReact.<Integer, Integer> react(() -> 1, () -> 2, () -> 3)
				.then((it) -> it * 100)
				.then((it) -> "*" + it)
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
	 * 'http://www.nurkiewicz.com/2013/12/promises-and-completablefuture.html'>http://www.nurkiewicz.com/2013/12/promises-and-completablefuture.h
	 * t m l < / a >
	 * 
	 * @param fn
	 *            Function to be applied to the results of the currently active
	 *            event tasks
	 * @return A new builder object that can be used to define the next stage in
	 *         the dataflow
	 */
	@SuppressWarnings("unchecked")
	public <R> Stage<U, R> then(final Function<T, U> fn) {
		return (Stage<U, R>) this.withLastActive(lastActive.stream()
				.map((ft) -> ft.thenApplyAsync(fn, taskExecutor))
				.collect(Collectors.toList()));
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
	  	List<String> strings = SimpleReact.<Integer, Integer> react(() -> 100, () -> 2, () -> 3)
					.then(it -> {
						if (it == 100)
							throw new RuntimeException("boo!");
			
						return it;
					})
					.onFail(e -> 1)
					.then(it -> "*" + it)
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
	@SuppressWarnings("unchecked")
	public <R> Stage<T, R> onFail(final Function<? extends Throwable, T> fn) {
		return (Stage<T, R>) this
				.withLastActive(lastActive.stream()
						.map((ft) -> ft.exceptionally(fn))
						.collect(Collectors.toList()));
	}

	/**
	 * React <b>capture</b>
	 * 
	 * While onFail is used for disaster recovery (when it is possible to
	 * recover) - capture is used to capture those occasions where the full
	 * pipeline has failed and is unrecoverable.
	 * 
	 * <code>
	 * List<String> strings = SimpleReact.<Integer, Integer> react(() -> 1, () -> 2, () -> 3)
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
		</code>
	 * 
	 * In this case, strings will only contain the two successful results (for
	 * ()->1 and ()->3), an exception for the chain starting from Supplier ()->2
	 * will be logged by capture. Capture will not capture the exception thrown
	 * when an Integer value of 100 is found, but will catch the exception when
	 * the String value "*200" is passed along the chain.
	 * 
	 * @param errorHandler
	 *            A consumer that recieves and deals with an unrecoverable error
	 *            in the dataflow
	 * @return A new builder object that can be used to define the next stage in
	 *         the dataflow
	 */
	@SuppressWarnings("unchecked")
	public Stage<T, U> capture(final Consumer<? extends Throwable> errorHandler) {
		return this.withErrorHandler(Optional
				.of((Consumer<Throwable>) errorHandler));
	}
	
	public ReactCollector<T,U> collectResults(){
		return new ReactCollector(this);
	}

	/**
	 * React and <b>block</b>
	 * 
	 * <code>
	 	List<String> strings = SimpleReact.<Integer, Integer> react(() -> 1, () -> 2, () -> 3)
				.then((it) -> it * 100)
				.then((it) -> "*" + it)
				.block();
	  </code>
	 * 
	 * In this example, once the current thread of execution meets the React
	 * block method, it will block until all tasks have been completed. The
	 * result will be returned as a List. The Reactive tasks triggered by the
	 * Suppliers are non-blocking, and are not impacted by the block method
	 * until they are complete. Block, only blocks the current thread.
	 * 
	 * @return Results of currently active stage aggregated in a List
	 */
	@SuppressWarnings("hiding")
	public <U> List<U> block() {
		return block(Collectors.toList(),lastActive);
	}
	
	/**
	 * @param collector to perform aggregation / reduction operation on the results (e.g. to Collect into a List or String)
	 * @return Results of currently active stage in aggregated in form determined by collector
	 */
	@SuppressWarnings({ "hiding", "unchecked","rawtypes"})
	public <U,R> R block(final Collector collector) {
		return (R)block(collector,lastActive);
	}
	/**
	 * Block until first result recieved
	 * 
	 * @return  first result.
	 */
	@SuppressWarnings({ "hiding", "unchecked","rawtypes"})
	public <U,R> R first() {
		return blockAndExtract(Extractors.first(),status -> status.getCompleted() > 1);
	}
	/**
	 * Block until all results recieved.
	 * 
	 * @return  last result
	 */
	@SuppressWarnings({ "hiding", "unchecked","rawtypes"})
	public <U,R> R last() {
		return blockAndExtract(Extractors.last());
	}
	
	/**
	 * Block until tasks complete and return a value determined by the extractor supplied.
	 * 
	 * @param extractor used to determine which value should be returned, recieves current collected input and extracts a return value
	 * @return Value determined by the supplied extractor
	 */
	@SuppressWarnings({ "hiding", "unchecked","rawtypes"})
	public <U,R> R blockAndExtract(final Extractor extractor) {
		return blockAndExtract(extractor, status -> false);
	}
	/**
	 *  Block until tasks complete, or breakout conditions met and return a value determined by the extractor supplied.
	 * 
	 * @param extractor used to determine which value should be returned, recieves current collected input and extracts a return value 
	 * @param breakout Predicate that determines whether the block should be
	 *            continued or removed
	 * @return Value determined by the supplied extractor
	 */
	@SuppressWarnings({ "hiding", "unchecked","rawtypes"})
	public <U,R> R blockAndExtract(final Extractor extractor,final Predicate<Status> breakout) {
		return (R)extractor.extract(block());
	}

	/**
	 * React and <b>block</b> with <b>breakout</b>
	 * 
	 * Sometimes you may not need to block until all the work is complete, one
	 * result or a subset may be enough. To faciliate this, block can accept a
	 * Predicate functional interface that will allow SimpleReact to stop
	 * blocking the current thread when the Predicate has been fulfilled. E.g.
	 * 
	 * <code>
	  	List<String> strings = SimpleReact.<Integer, Integer> react(() -> 1, () -> 2, () -> 3)
				.then(it -> it * 100)
				.then(it -> "*" + it)
				.block(status -> status.getCompleted()>1);
	  </code>
	 * 
	 * In this example the current thread will unblock once more than one result
	 * has been returned.
	 * 
	 * @param breakout
	 *            Predicate that determines whether the block should be
	 *            continued or removed
	 * @return List of Completed results of currently active stage at full completion
	 *         point or when breakout triggered (which ever comes first).
	 */
	@SuppressWarnings("hiding")
	public <X> List<X> block(final Predicate<Status> breakout) {
		return new Blocker<X>(lastActive, errorHandler).block(breakout);
	}
	
	
	/**
	 * @param collector to perform aggregation / reduction operation on the results (e.g. to Collect into a List or String)
	 * @param breakout  Predicate that determines whether the block should be
	 *            continued or removed
	 * @return Completed results of currently active stage at full completion
	 *         point or when breakout triggered (which ever comes first), in aggregated in form determined by collector
	 */
	@SuppressWarnings({ "hiding", "unchecked","rawtypes" })
	public <U,R> R block(final Collector collector,final Predicate<Status> breakout) {
		return (R)block(breakout).stream().collect(collector);
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
		SimpleReact.<Integer, Integer> react(() -> 1, () -> 2, () -> 3)
				
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
	public <R> Stage<U, R> allOf(final Function<List<T>, U> fn) {

		return allOf(Collectors.toList(), (Function<R, U>)fn);

	}
	/**
	 * @param collector to perform aggregation / reduction operation on the results from active stage (e.g. to Collect into a List or String)
	 * @param fn  Function that recieves the results of all currently active
	 *            tasks as input
	 * @return A new builder object that can be used to define the next stage in
	 *         the dataflow
	 */
	@SuppressWarnings({"unchecked","rawtypes"})
	public <R> Stage<U, R> allOf(final Collector collector,final Function<R, U> fn) {

		return (Stage<U, R>) withLastActive(asList( CompletableFuture.allOf(
				lastActiveArray()).thenApplyAsync((result) -> {
						return submit( () -> fn.apply(aggregateResults(collector, lastActive)));
					}, taskExecutor)));

	}

	@SuppressWarnings({ "rawtypes", "unchecked", "hiding" })
	private <U,R> R block(final Collector collector,final List<CompletableFuture> lastActive) {
		return (R)lastActive.stream().map((future) -> {
			return (U) getSafe(future);
		}).filter(v -> v != MISSING_VALUE).collect(collector);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private<R> R aggregateResults(final  Collector collector,
			final List<CompletableFuture> completedFutures) {
		return (R) completedFutures.stream().map(next -> getSafe(next)).filter(v -> v != MISSING_VALUE)
				.collect(collector);
	}

	@SuppressWarnings("rawtypes")
	private CompletableFuture[] lastActiveArray() {
		return lastActive.toArray(new CompletableFuture[0]);
	}

	private void capture(final Exception e) {
		errorHandler.ifPresent((handler) -> handler.accept(e.getCause()));
	}

	@SuppressWarnings("rawtypes")
	private Object getSafe(final CompletableFuture next) {
		try {
			return next.get();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			capture(e);
		} catch (Exception e) {
			capture(e);
		}
		return MISSING_VALUE;
	}
	
	private final static MissingValue MISSING_VALUE =new MissingValue();
	private static class MissingValue {
		
	}
	

}
