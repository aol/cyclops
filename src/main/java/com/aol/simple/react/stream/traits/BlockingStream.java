package com.aol.simple.react.stream.traits;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import com.aol.simple.react.blockers.Blocker;
import com.aol.simple.react.collectors.ReactCollector;
import com.aol.simple.react.exceptions.ExceptionSoftener;
import com.aol.simple.react.exceptions.FilteredExecutionPathException;
import com.aol.simple.react.exceptions.ThrowsSoftened;
import com.aol.simple.react.extractors.Extractor;
import com.aol.simple.react.extractors.Extractors;
import com.aol.simple.react.stream.MissingValue;
import com.aol.simple.react.stream.Status;
import com.aol.simple.react.stream.StreamWrapper;

public interface BlockingStream<U> extends ConfigurableStream<U>{

	
	final static ExceptionSoftener exceptionSoftener = ExceptionSoftener.singleton.factory
			.getInstance();
	

	
	
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
	default ReactCollector<U> collectResults() {
		return new ReactCollector(this);
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
	default List<U> block() {
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
	default <R> R block(final Collector collector) {
		return (R) block(collector, getLastActive());
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	default <R> R block(final Collector collector,
			final StreamWrapper lastActive) {
		return (R) lastActive.stream().map((future) -> {
			return (U) getSafe(future,getErrorHandler());
		}).filter(v -> v != MissingValue.MISSING_VALUE).collect(collector);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	static <R> R aggregateResults(final Collector collector,
			final List<CompletableFuture> completedFutures, Optional<Consumer<Throwable>> errorHandler) {
		return (R) completedFutures.stream().map(next -> getSafe(next,errorHandler))
				.filter(v -> v != MissingValue.MISSING_VALUE).collect(collector);
	}
	
	static void capture(final Exception e,Optional<Consumer<Throwable>> errorHandler) {
		errorHandler.ifPresent((handler) -> {
			if (!(e.getCause() instanceof FilteredExecutionPathException)) {
				handler.accept(e.getCause());
			}
		});
	}
	@SuppressWarnings("rawtypes")
	static Object getSafe(final CompletableFuture next,Optional<Consumer<Throwable>> errorHandler) {
		try {
			return next.get();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			capture(e,errorHandler);
			exceptionSoftener.throwSoftenedException(e);
		} catch (RuntimeException e) {
			capture(e,errorHandler);
		} catch (Exception e) {
			capture(e,errorHandler);
		}

		return MissingValue.MISSING_VALUE;
	}

	
	/**
	 * Block until first result received
	 * 
	 * @return first result. throws InterruptedException,ExecutionException
	 */

	@ThrowsSoftened({ InterruptedException.class, ExecutionException.class })
	default  U first() {
		return blockAndExtract(Extractors.first(),
				status -> status.getCompleted() > 0);
	}

	/**
	 * Block until all results received.
	 * 
	 * @return last result throws InterruptedException,ExecutionException
	 */
	@ThrowsSoftened({ InterruptedException.class, ExecutionException.class })
	default  U last() {
		return blockAndExtract(Extractors.last());
	}

	/**
	 * Block until tasks complete and return a value determined by the extractor
	 * supplied.
	 * 
	 * @param extractor
	 *            used to determine which value should be returned, recieves
	 *            current collected input and extracts a return value
	 * @return Value determined by the supplied extractor throws
	 *         InterruptedException,ExecutionException
	 */
	@ThrowsSoftened({ InterruptedException.class, ExecutionException.class })
	default <R> R blockAndExtract(
			@SuppressWarnings("rawtypes") final Extractor extractor) {
		return blockAndExtract(extractor, status -> false);
	}

	/**
	 * Block until tasks complete, or breakout conditions met and return a value
	 * determined by the extractor supplied.
	 * 
	 * @param extractor
	 *            used to determine which value should be returned, recieves
	 *            current collected input and extracts a return value
	 * @param breakout
	 *            Predicate that determines whether the block should be
	 *            continued or removed
	 * @return Value determined by the supplied extractor throws
	 *         InterruptedException,ExecutionException
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@ThrowsSoftened({ InterruptedException.class, ExecutionException.class })
	default  <R> R blockAndExtract(final Extractor extractor,
			final Predicate<Status> breakout) {
		return (R) extractor.extract(block());
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
	  	List&lt;String&gt; strings = new SimpleReact().&lt;Integer, Integer&gt; react(() -&gt; 1, () -&gt; 2, () -&gt; 3)
				.then(it -&gt; it * 100)
				.then(it -&gt; "*" + it)
				.block(status -&gt; status.getCompleted()&gt;1);
	  </code>
	 * 
	 * In this example the current thread will unblock once more than one result
	 * has been returned.
	 * 
	 * @param breakout
	 *            Predicate that determines whether the block should be
	 *            continued or removed
	 * @return List of Completed results of currently active stage at full
	 *         completion point or when breakout triggered (which ever comes
	 *         first). throws InterruptedException,ExecutionException
	 */
	@ThrowsSoftened({ InterruptedException.class, ExecutionException.class })
	default List<U> block(final Predicate<Status> breakout) {
		return new Blocker<U>(getLastActive().list(), getErrorHandler()).block(breakout);
	}

	/**
	 * @param collector
	 *            to perform aggregation / reduction operation on the results
	 *            (e.g. to Collect into a List or String)
	 * @param breakout
	 *            Predicate that determines whether the block should be
	 *            continued or removed
	 * @return Completed results of currently active stage at full completion
	 *         point or when breakout triggered (which ever comes first), in
	 *         aggregated in form determined by collector throws
	 *         InterruptedException,ExecutionException
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@ThrowsSoftened({ InterruptedException.class, ExecutionException.class })
	default  <R> R block(final Collector collector,
			final Predicate<Status> breakout) {
		return (R) block(breakout).stream().collect(collector);
	}

	/**
	 * This method allows the SimpleReact ExecutorService to be reused by JDK
	 * parallel streams. This offers less control over blocking than raw submit,
	 * with the parameterless block() method called.
	 * 
	 * @param fn
	 *            Function that contains parallelStream code to be executed by
	 *            the SimpleReact ForkJoinPool (if configured)
	 */
	default <R> R submitAndBlock(Function<List<U>, R> fn) {
		return collectResults().block().submit(r -> fn.apply(r));
	}
}
