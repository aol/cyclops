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
import java.util.stream.Stream;

import com.aol.simple.react.blockers.Blocker;
import com.aol.simple.react.exceptions.ExceptionSoftener;
import com.aol.simple.react.exceptions.FilteredExecutionPathException;
import com.aol.simple.react.exceptions.ThrowsSoftened;
import com.aol.simple.react.extractors.Extractor;
import com.aol.simple.react.extractors.Extractors;
import com.aol.simple.react.stream.MissingValue;
import com.aol.simple.react.stream.Status;
import com.aol.simple.react.stream.StreamWrapper;

public interface BlockingStream<U> extends ConfigurableStream<U>{

	
	
	

	
	


	/**
	 * React and <b>block</b>
	 * 
	 * <pre>
	 * {@code 
		List<String> strings = new SimpleReact().<Integer, Integer> react(() -> 1, () -> 2, () -> 3)
				.then((it) -> it * 100)
				.then((it) -> "*" + it)
				.block();
		}
				
	  </pre>
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
		return BlockingStreamHelper.block(this,Collectors.toList(), getLastActive());
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
		return (R) BlockingStreamHelper.block(this,collector, getLastActive());
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

	

	 
}
