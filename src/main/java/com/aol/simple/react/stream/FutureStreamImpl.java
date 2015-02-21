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
import lombok.experimental.Wither;
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
import com.aol.simple.react.stream.api.AsyncToQueue;
import com.aol.simple.react.stream.api.Configuration;
import com.aol.simple.react.stream.api.FutureStream;
import com.aol.simple.react.stream.api.SimpleReactStream;
import com.aol.simple.react.stream.simple.SimpleReact;
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
public  class FutureStreamImpl<U> implements SimpleReactStream<U>, Configuration<U>,AsyncToQueue<U>{

	private final ExceptionSoftener exceptionSoftener = ExceptionSoftener.singleton.factory
			.getInstance();
	@Getter
//	@Wither(value = AccessLevel.PUBLIC)
	private final ExecutorService taskExecutor;
//	@Wither(value = AccessLevel.PUBLIC)
	private final RetryExecutor retrier;

	@Getter
	private final Optional<Consumer<Throwable>> errorHandler;

	@Getter
	private final StreamWrapper lastActive;
	@Getter
	private final boolean eager;
//	@Wither(value = AccessLevel.PUBLIC)
	@Getter
	private final Consumer<CompletableFuture> waitStrategy;
	@Getter
	private final LazyResultConsumer<U> lazyCollector;
	@Getter
	private final QueueFactory<U> queueFactory;

	/**
	 * 
	 * Construct a SimpleReact stage - this acts as a fluent SimpleReact builder
	 * 
	 * @param stream
	 *            Stream that will generate the events that will be reacted to.
	 * @param executor
	 *            The next stage's tasks will be submitted to this executor
	 */
	protected FutureStreamImpl(final Stream<CompletableFuture<U>> stream,
			final ExecutorService executor, final RetryExecutor retrier,
			final boolean eager) {

		this.taskExecutor = Optional.ofNullable(executor).orElse(
				new ForkJoinPool(Runtime.getRuntime().availableProcessors()));
		Stream s = stream;
		this.lastActive = new StreamWrapper(s, Optional.ofNullable(eager)
				.orElse(true));
		this.errorHandler = Optional.of((e) -> log.error(e.getMessage(), e));
		this.eager = eager;
		this.retrier = Optional.ofNullable(retrier).orElse(
				RetryBuilder.getDefaultInstance());
		this.waitStrategy = new LimitingMonitor();
		this.lazyCollector = new BatchingCollector<>();
		this.queueFactory = QueueFactories.boundedQueue(1000);
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
	public <R> FutureStream<R> then(final Function<U, R> fn) {
		return (FutureStream<R>) this.withLastActive(lastActive.permutate(
				lastActive.stream().map(
						(ft) -> ft.thenApplyAsync(handleExceptions(fn),
								taskExecutor)), Collectors.toList()));
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
	public <R> FutureStream<R> flatMap(
			Function<? super U, ? extends Stream<? extends R>> flatFn) {

		//need to pass in a builder in the constructor and build using it
		return SimpleReact
				.builder()
				.eager(eager)
				.executor(taskExecutor)
				.retrier(retrier)
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
	public <R> FutureStream<R> retry(final Function<U, R> fn) {

		return (FutureStream<R>) this.withLastActive(lastActive.permutate(
				lastActive.stream().map(
						(ft) -> ft.thenApplyAsync((res) -> getSafe(retrier
								.getWithRetry(() -> fn.apply((U) res))),
								taskExecutor)), Collectors.toList()));
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
	public <T, R> FutureStream<R> allOf(final Collector collector,
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
		}, taskExecutor);
		return (FutureStream<R>) withLastActive(new StreamWrapper(onSuccess,
				eager));

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
		return lastActive.list().toArray(new CompletableFuture[0]);
	}

	private void capture(final Exception e) {
		errorHandler.ifPresent((handler) -> {
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


	

	public <R> FutureStream<R> fromStream(Stream<R> stream) {
		return (FutureStream<R>) this.withLastActive(lastActive
				.withNewStream(stream.map(CompletableFuture::completedFuture)));
	}

	public <R> FutureStream<R> fromStreamCompletableFuture(
			Stream<CompletableFuture<R>> stream) {
		Stream noType = stream;
		return (FutureStream<R>) this.withLastActive(lastActive
				.withStream(noType));
	}
	public SimpleReactStream<U> withTaskExecutor(ExecutorService e){
		return null;
	}
	public SimpleReactStream<U> withRetrier(RetryExecutor retry){
		return null;
	}
	public SimpleReactStream<U> withWaitStrategy(Consumer<CompletableFuture> c){
		return null;
	}
	public SimpleReactStream<U> withEager(boolean eager){
		return null;
	}
	public SimpleReactStream<U> withLazyCollector(LazyResultConsumer<U> lazy){
		return null;
	}
	public SimpleReactStream<U> withQueueFactory(QueueFactory<U> queue){
		return null;
	}
	public SimpleReactStream<U>  withErrorHandler(Optional<Consumer<Throwable>> errorHandler){
		return null;
	}
	public SimpleReactStream<U> withLastActive(StreamWrapper streamWrapper){
		return null;
	}

}
