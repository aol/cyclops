package com.aol.cyclops.react.collectors.lazy;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

import com.aol.cyclops.react.async.future.FastFuture;
import com.aol.cyclops.react.config.MaxActive;
import com.aol.cyclops.react.stream.traits.ConfigurableStream;

/**
 * Interface that defines the rules for Collecting results from Infinite SimpleReact Streams
 * 
 * @author johnmcclean
 *
 * @param <T>
 */
public interface LazyResultConsumer<T> extends Consumer<FastFuture<T>>{

	/**
	 * Used to generate a new instance for result collection - populates the supplied Collection
	 * 
	 * @param t Collection to be populated
	 * @return Consumer that will populate the collection
	 */
	public LazyResultConsumer<T> withResults(Collection<FastFuture<T>> t);

	/**
	 * @return Completed results
	 */
	public Collection<FastFuture<T>> getResults();
	/**
	 * @return Completed  and active results
	 */
	public Collection<FastFuture<T>> getAllResults();

	public void block(Function<FastFuture<T>,T> safeJoin);

	
	

	
}