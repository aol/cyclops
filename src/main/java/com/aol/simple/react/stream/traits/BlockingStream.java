package com.aol.simple.react.stream.traits;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import com.aol.simple.react.blockers.Blocker;
import com.aol.simple.react.exceptions.ThrowsSoftened;
import com.aol.simple.react.extractors.Extractor;
import com.aol.simple.react.extractors.Extractors;
import com.aol.simple.react.stream.EagerStreamWrapper;
import com.aol.simple.react.stream.LazyStreamWrapper;
import com.aol.simple.react.stream.Status;

public interface BlockingStream<U,F> extends ConfigurableStream<U,F>{


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
		Object lastActive = getLastActive();
		if(lastActive instanceof EagerStreamWrapper){
			EagerStreamWrapper last = (EagerStreamWrapper)lastActive;
			return BlockingStreamHelper.block(this,Collectors.toList(), last);
		}else{
			LazyStreamWrapper last = (LazyStreamWrapper)lastActive;
			return BlockingStreamHelper.block(this,Collectors.toList(), last);
		}
	}

	 Object getLastActive();

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
		Object lastActive = getLastActive();
		if(lastActive instanceof EagerStreamWrapper){
			EagerStreamWrapper last = (EagerStreamWrapper)lastActive;
			return (R) BlockingStreamHelper.block(this,collector, last);
		}else{
			LazyStreamWrapper last = (LazyStreamWrapper)lastActive;
			return (R) BlockingStreamHelper.block(this,collector,last);
		}
		
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

	

	

	

	 
}
