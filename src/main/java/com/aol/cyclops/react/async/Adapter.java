package com.aol.cyclops.react.async;

import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.react.async.subscription.Continueable;

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
	 * Offer a single datapoint to this adapter
	 * 
	 * @param data data to add
	 * @return self
	 */
	public boolean offer(T data);
	
	/**
	 * @param stream Input data from provided Stream
	 */
	public boolean fromStream(Stream<T> stream);
	
	/**
	 * @return Stream of data
	 */
	public ReactiveSeq<T> stream();
	/**
	 * @return Stream of data
	 */
	public ReactiveSeq<T> stream(Continueable s);
	/**
	 * @return Stream of CompletableFutures that can be used as input into a SimpleReact concurrent dataflow
	 */
	public ReactiveSeq<CompletableFuture<T>> streamCompletableFutures();
	
	/**
	 * Close this adapter
	 * 
	 * @return true if closed
	 */
	public boolean close();
}
