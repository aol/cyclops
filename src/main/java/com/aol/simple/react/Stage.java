package com.aol.simple.react;

import static java.util.Arrays.asList;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Wither;
import sun.misc.Unsafe;

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
@Wither
@AllArgsConstructor
public class Stage<T, U> {

	private final static Logger logger = Logger
			.getLogger(Stage.class.getName());
	private final Stream<CompletableFuture<T>> stream;
	private final Executor taskExecutor;

	@SuppressWarnings("rawtypes")
	private final List<CompletableFuture> lastActive;
	private final Optional<Consumer<Throwable>> errorHandler;

	/**
	 * 
	 * Construct a SimpleReact stage - this acts as a fluent SimpleReact builder
	 * 
	 * @param stream
	 *            Stream that will generate the events that will be reacted to.
	 * @param executor
	 *            The next stage's tasks will be submitted to this executor
	 */
	public Stage(Stream<CompletableFuture<T>> stream, Executor executor) {
		this.stream = stream;
		this.taskExecutor = executor;
		lastActive = null;
		errorHandler = Optional.empty();
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
	public List<CompletableFuture<U>> with(Function<T, ? extends Object> fn) {

		return stream.map(
				future -> (CompletableFuture<U>) future.thenApplyAsync(fn,
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
	 * 'http://www.nurkiewicz.com/2013/12/promises-and-completablefuture.html'>http://www.nurkiewicz.com/2013/12/promises-and-completablefuture.htm
	 * l < / a >
	 * 
	 * @param fn
	 *            Function to be applied to the results of the currently active
	 *            event tasks
	 * @return A new builder object that can be used to define the next stage in
	 *         the dataflow
	 */
	@SuppressWarnings("unchecked")
	public <R> Stage<U, R> then(Function<T, U> fn) {
		if (lastActive != null) {
			return (Stage<U, R>) this.withLastActive(lastActive.stream()
					.map((ft) -> ft.thenApplyAsync(fn, taskExecutor))
					.collect(Collectors.toList()));
		} else
			return (Stage<U, R>) this.withLastActive(stream.map(
					future -> future.thenApplyAsync(fn, taskExecutor)).collect(
					Collectors.toList()));

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
	public <R> Stage<T, R> onFail(Function<? extends Throwable, T> fn) {
		if (lastActive != null) {
			return (Stage<T, R>) this.withLastActive(lastActive.stream()
					.map((ft) -> ft.exceptionally(fn))
					.collect(Collectors.toList()));
		} else
			return (Stage<T, R>) this
					.withLastActive(stream
							.map(future -> future
									.exceptionally((Function<Throwable, ? extends T>) fn))
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
	public Stage<T, U> capture(Consumer<? extends Throwable> errorHandler) {
		return this.withErrorHandler(Optional
				.of((Consumer<Throwable>) errorHandler));
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
	 * @return Results of currently active stage
	 */
	@SuppressWarnings("hiding")
	public <U> List<U> block() {
		return block(lastActive);
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
	 * @param breakout Predicate that determines whether the block should be continued or remvoed
	 * @return  Competed results of currently active stage at full completion point or when breakout triggered (which ever comes first).
	 */
	@SuppressWarnings("hiding")
	public <U> List<U> block(final Predicate<Status> breakout) {
		return new Blocker<U>(lastActive, errorHandler).block(breakout);
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
	public <R> Stage<U, R> allOf(Function<List<T>, U> fn) {

		@SuppressWarnings("rawtypes")
		List<CompletableFuture> completedFutures = lastActive;

		return (Stage<U, R>) withLastActive(asList(CompletableFuture.allOf(
				lastActiveArray()).thenApplyAsync((result) -> {

			return fn.apply(aggregateResults(completedFutures));

		}, taskExecutor)));

	}
	
	@SuppressWarnings({ "rawtypes", "unchecked", "hiding" })
	private <U> List<U> block(List<CompletableFuture> lastActive) {
		return lastActive.stream().map((future) -> {

			try {
				return (U) future.get();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				capture(e);
				return null;
			} catch (Exception e) {

				capture(e);
				return null;
			}

		}).filter(v -> v != null).collect(Collectors.toList());
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private List<T> aggregateResults(List<CompletableFuture> completedFutures) {
		return (List<T>) completedFutures.stream().map(next -> getSafe(next))
				.collect(Collectors.toList());
	}

	@SuppressWarnings("rawtypes")
	private CompletableFuture[] lastActiveArray() {
		return lastActive.toArray(new CompletableFuture[0]);
	}

	private void capture(final Exception e) {
		errorHandler.ifPresent((handler) -> handler.accept(e.getCause()));
	}

	@SuppressWarnings("rawtypes")
	private Object getSafe(CompletableFuture next) {
		try {
			return next.get();
		} catch (InterruptedException | ExecutionException e) {
			Thread.currentThread().interrupt();
			errorHandler.ifPresent((handler) -> handler.accept(e.getCause()));
		}
		return null;
	}

	@AllArgsConstructor
	private static class Blocker<U> {

		private final Optional<Unsafe> unsafe = getUnsafe();
		@SuppressWarnings("rawtypes")
		private final List<CompletableFuture> lastActive;
		private final Optional<Consumer<Throwable>> errorHandler;
		private final CompletableFuture<List<U>> promise = new CompletableFuture<>();

		private final SimpleTimer timer = new SimpleTimer();
		private final AtomicInteger completed = new AtomicInteger();
		private final AtomicInteger errors = new AtomicInteger();

		private final Queue<U> currentResults = new ConcurrentLinkedQueue<U>();

		@SuppressWarnings("unchecked")
		public List<U> block(final Predicate<Status> breakout) {

			lastActive.forEach(f -> f.whenComplete((result, ex) -> {
				testBreakoutConditionsBeforeUnblockingCurrentThread(breakout,
						result, ex);

			}));

			try {
				return promise.get();
			} catch (ExecutionException e) {
				throwSoftenedException(e);
				throw new RuntimeException(e);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throwSoftenedException(e);
				throw new RuntimeException(e);
			}

		}

		private void throwSoftenedException(Exception e) {
			unsafe.ifPresent(u -> u.throwException(e));
		}

		private void testBreakoutConditionsBeforeUnblockingCurrentThread(
				final Predicate<Status> breakout, Object result, Object ex) {
			if (result != null)
				currentResults.add((U) result);
			int localComplete = completed.incrementAndGet();
			int localErrors = errors.get();
			if (ex != null) {
				localErrors = errors.incrementAndGet();
				errorHandler.ifPresent((handler) -> handler
						.accept(((Exception) ex).getCause()));
			}
			Status status = new Status(localComplete, localErrors,
					lastActive.size(), timer.getElapsedMilliseconds());
			if (breakoutConditionsMet(breakout, status)
					|| allResultsReturned(localComplete)) {
				promise.complete(new LinkedList<U>(currentResults));
			}
		}

		private boolean allResultsReturned(int localComplete) {
			return localComplete == lastActive.size();
		}

		private boolean breakoutConditionsMet(Predicate<Status> breakout,
				Status status) {
			return breakout.test(status);
		}

		private static Optional<Unsafe> getUnsafe() {

			try {

				Field field = Unsafe.class.getDeclaredField("theUnsafe");

				field.setAccessible(true);

				return Optional.of((Unsafe) field.get(null));

			} catch (Exception ex) {

				logger.log(Level.SEVERE, ex.getMessage());

			}
			return Optional.empty();

		}
	}

	@AllArgsConstructor
	@Getter
	public static class Status {
		private final int completed;
		private final int errors;
		private final int total;
		private final long elapsedMillis;
	}

}
