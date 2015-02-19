package com.aol.simple.react.collectors.lazy;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import com.aol.simple.react.config.MaxActive;

/**
 * Interface that defines the rules for Collecting results from Infinite SimpleReact Streams
 * 
 * @author johnmcclean
 *
 * @param <T>
 */
public interface LazyResultConsumer<T> extends Consumer<CompletableFuture<T>>{

	/**
	 * Used to generate a new instance for result collection - populates the supplied Collection
	 * 
	 * @param t Collection to be populated
	 * @return Consumer that will populate the collection
	 */
	public LazyResultConsumer<T> withResults(Collection<T> t);

	/**
	 * @return Completed results
	 */
	public Collection<T> getResults();

	public MaxActive getMaxActive();
}
