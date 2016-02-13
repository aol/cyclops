package com.aol.cyclops.react.extractors;

/**
 * 
 * Functional interface for extracting results from a blocking call.
 * 
 * Useful when block is called after allOf, to return the value that resulted from the allOf stage unwrapped in a collection.
 * 
 * @author johnmcclean
 *
 * @param <T> Input type
 * @param <R> Return type
 */
@FunctionalInterface
public interface Extractor<T,R> {

	public R extract(T u);
}
