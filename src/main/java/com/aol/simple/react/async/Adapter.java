package com.aol.simple.react.async;

import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

/**
 * 
 * Interface for an Adapter that inputs data from 1 or more input Streams and sends it to 1 or more output Streams
 * 
 * @author johnmcclean
 *
 * @param <T> Data type
 */
public interface Adapter<T> {

	/**
	 * Add a single datapoint to this adapter
	 * 
	 * @param data data to add
	 * @return self
	 */
	public T add(T data);
	
	/**
	 * @param stream Input data from provided Stream
	 */
	public boolean fromStream(Stream<T> stream);
	
	/**
	 * @return Stream of data
	 */
	public Stream<T> stream();
	/**
	 * @return Stream of CompletableFutures that can be used as input into a SimpleReact concurrent dataflow
	 */
	public Stream<CompletableFuture<T>> streamCompletableFutures();
	
	/**
	 * Close this adapter
	 * 
	 * @return true if closed
	 */
	public boolean close();
}
