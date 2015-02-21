package com.aol.simple.react.stream;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import com.aol.simple.react.RetryBuilder;
import com.aol.simple.react.async.QueueFactories;
import com.aol.simple.react.async.QueueFactory;
import com.aol.simple.react.capacity.monitor.LimitingMonitor;
import com.aol.simple.react.collectors.ReactCollector;
import com.aol.simple.react.collectors.lazy.BatchingCollector;
import com.aol.simple.react.collectors.lazy.LazyResultConsumer;
import com.aol.simple.react.exceptions.ExceptionSoftener;
import com.aol.simple.react.exceptions.FilteredExecutionPathException;
import com.aol.simple.react.exceptions.SimpleReactFailedStageException;
import com.aol.simple.react.exceptions.ThrowsSoftened;
import com.aol.simple.react.stream.api.EagerToQueue;
import com.aol.simple.react.stream.api.Blockable;
import com.aol.simple.react.stream.api.Configuration;
import com.aol.simple.react.stream.api.FutureStream;
import com.aol.simple.react.stream.api.SimpleReactStream;
import com.aol.simple.react.stream.eager.EagerReact;
import com.nurkiewicz.asyncretry.RetryExecutor;

/**
 * 
 * An Immutable Builder Class that represents a stage in a SimpleReact Dataflow.
 * Chain stages together to build complext reactive dataflows.
 * 
 * Access the underlying CompletableFutures via the 'with' method. Return to
 * using JDK Collections and (parrellel) Streams via 'allOf' or 'block'
 * 
 * 
 * 
 * @author johnmcclean
 *
 * @param <U>
 *            Return parameter for this stage
 */

// lombok annotations to aid Immutability (Wither and AllArgsConstructor)

@AllArgsConstructor
@Slf4j
//@Wither
public abstract class FutureStreamImpl<U> implements SimpleReactStream<U>, 
										Configuration<U>,Blockable<U>,
										EagerToQueue<U>{

	private final ExceptionSoftener exceptionSoftener = ExceptionSoftener.singleton.factory
			.getInstance();

	

	

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
	public <R> SimpleReactStream<R> then(final Function<U, R> fn) {
		return (FutureStream<R>) this.withLastActive(getLastActive().permutate(
				getLastActive().stream().map(
						(ft) -> ft.thenApplyAsync(handleExceptions(fn),
								getTaskExecutor())), Collectors.toList()));
	}

	private <R> Function<U, R> handleExceptions(Function<U, R> fn) {
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
	@Override
	public <R> SimpleReactStream<R> flatMap(
			Function<? super U, ? extends Stream<? extends R>> flatFn) {

		//need to pass in a builder in the constructor and build using it
		return EagerReact
				.builder()
				//.eager(eager)
				.executor(getTaskExecutor())
				.retrier(getRetrier())
				.build()
				.fromStream(
						toQueue()
								.stream()
								.flatMap(flatFn)
								.map(it -> CompletableFuture
										.completedFuture(it)));
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
	public <R> SimpleReactStream<R> retry(final Function<U, R> fn) {

		return (SimpleReactStream<R>) this.withLastActive(getLastActive().permutate(
				getLastActive().stream().map(
						(ft) -> ft.thenApplyAsync((res) -> getSafe(getRetrier()
								.getWithRetry(() -> fn.apply((U) res))),
								getTaskExecutor())), Collectors.toList()));
	}

	/**
	 * This provides a mechanism to collect all of the results of active tasks
	 * inside a dataflow stage. This can then be used to provide those results
	 * to a function. Inside that function client code can leverage JDK 8
	 * parallel Streams that will be executed within the SimpleReact
	 * ExecutorService if that service is an instance of ForkJoinPool (the
	 * default setting).
	 * 
	 * Example : <code>
	 * Integer result = new SimpleReact()
				.&lt;Integer, Integer&gt; react(() -&gt; 1, () -&gt; 2, () -&gt; 3)
				.then((it) -&gt; { it * 200)
				.collectResults()
				.&lt;List&lt;Integer&gt;&gt;block()
				.submit( 
						it -&gt; it.orElse(new ArrayList())
								.parallelStream()
								.filter(f -&gt; f &gt; 300)
								.map(m -&gt; m - 5)
								.reduce(0, (acc, next) -&gt; acc + next));
								
	 * </code>
	 * 
	 * @return A builder that allows the blocking mechanism for results
	 *         collection to be set
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public ReactCollector<U> collectResults() {
		return new ReactCollector(this);
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
	public <T, R> SimpleReactStream<R> allOf(final Collector collector,
			final Function<T, R> fn) {
		CompletableFuture[] array = lastActiveArray();
		CompletableFuture cf = CompletableFuture.allOf(array);
		Function<Exception, T> f = (Exception e) -> {
			capture(e);
			return block(Collectors.toList(),
					new StreamWrapper(Stream.of(array), true));
		};
		CompletableFuture onFail = cf.exceptionally(f);
		CompletableFuture onSuccess = onFail.thenApplyAsync((result) -> {
			return new StageWithResults(this, result).submit(() -> fn
					.apply(aggregateResults(collector, Stream.of(array)
							.collect(Collectors.toList()))));
		}, getTaskExecutor());
		return (FutureStream<R>) withLastActive(new StreamWrapper(onSuccess,
				isEager()));

	}

	/**
	 * React and <b>block</b>
	 * 
	 * <code>
	 	List&lt;String&gt; strings = new SimpleReact().&lt;Integer, Integer&gt; react(() -&gt; 1, () -&gt; 2, () -&gt; 3)
				.then((it) -&gt; it * 100)
				.then((it) -&gt; "*" + it)
				.block();
	  </code>
	 * 
	 * In this example, once the current thread of execution meets the React
	 * block method, it will block until all tasks have been completed. The
	 * result will be returned as a List. The Reactive tasks triggered by the
	 * Suppliers are non-blocking, and are not impacted by the block method
	 * until they are complete. Block, only blocks the current thread.
	 * 
	 * 
	 * @return Results of currently active stage aggregated in a List throws
	 *         InterruptedException,ExecutionException
	 */
	@ThrowsSoftened({ InterruptedException.class, ExecutionException.class })
	public List<U> block() {
		return block(Collectors.toList(), getLastActive());
	}

	/**
	 * @param collector
	 *            to perform aggregation / reduction operation on the results
	 *            (e.g. to Collect into a List or String)
	 * @return Results of currently active stage in aggregated in form
	 *         determined by collector throws
	 *         InterruptedException,ExecutionException
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@ThrowsSoftened({ InterruptedException.class, ExecutionException.class })
	public <R> R block(final Collector collector) {
		return (R) block(collector, getLastActive());
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private <R> R block(final Collector collector,
			final StreamWrapper lastActive) {
		return (R) lastActive.stream().map((future) -> {
			return (U) getSafe(future);
		}).filter(v -> v != MISSING_VALUE).collect(collector);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private <R> R aggregateResults(final Collector collector,
			final List<CompletableFuture> completedFutures) {
		return (R) completedFutures.stream().map(next -> getSafe(next))
				.filter(v -> v != MISSING_VALUE).collect(collector);
	}

	@SuppressWarnings("rawtypes")
	private CompletableFuture[] lastActiveArray() {
		return getLastActive().list().toArray(new CompletableFuture[0]);
	}

	private void capture(final Exception e) {
		getErrorHandler().ifPresent((handler) -> {
			if (!(e.getCause() instanceof FilteredExecutionPathException)) {
				handler.accept(e.getCause());
			}
		});
	}

	@SuppressWarnings("rawtypes")
	private Object getSafe(final CompletableFuture next) {
		try {
			return next.get();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			capture(e);
			exceptionSoftener.throwSoftenedException(e);
		} catch (RuntimeException e) {
			capture(e);
		} catch (Exception e) {
			capture(e);
		}

		return MISSING_VALUE;
	}

	public final static MissingValue MISSING_VALUE = new MissingValue();

	private static class MissingValue {

	}


	

	public <R> SimpleReactStream<R> fromStream(Stream<R> stream) {
		return (SimpleReactStream<R>) this.withLastActive(getLastActive()
				.withNewStream(stream.map(CompletableFuture::completedFuture)));
	}

	public <R> SimpleReactStream<R> fromStreamCompletableFuture(
			Stream<CompletableFuture<R>> stream) {
		Stream noType = stream;
		return (FutureStream<R>) this.withLastActive(getLastActive()
				.withStream(noType));
	}
	public abstract SimpleReactStream<U> withTaskExecutor(ExecutorService e);
	public abstract SimpleReactStream<U> withRetrier(RetryExecutor retry);
	public abstract SimpleReactStream<U> withWaitStrategy(Consumer<CompletableFuture> c);
	public abstract SimpleReactStream<U> withEager(boolean eager);
	public abstract SimpleReactStream<U> withLazyCollector(LazyResultConsumer<U> lazy);
	public abstract SimpleReactStream<U> withQueueFactory(QueueFactory<U> queue);
	public abstract SimpleReactStream<U>  withErrorHandler(Optional<Consumer<Throwable>> errorHandler);
	public abstract SimpleReactStream<U> withLastActive(StreamWrapper streamWrapper);

	abstract public ExecutorService getTaskExecutor() ;

	abstract public boolean isEager() ;

	abstract public RetryExecutor getRetrier() ;

	abstract public Optional<Consumer<Throwable>> getErrorHandler();

	abstract public StreamWrapper getLastActive();

	abstract  public Consumer<CompletableFuture> getWaitStrategy();

	abstract public LazyResultConsumer<U> getLazyCollector() ;

	abstract public QueueFactory<U> getQueueFactory() ;

}
