package com.aol.cyclops.sequence;

import java.util.Iterator;
import java.util.stream.Stream;

/**
 * Represents something that can generate a Stream, repeatedly
 * 
 * @author johnmcclean
 *
 * @param <T> Data type for Stream
 */
public interface Streamable<T> extends Iterable<T>{

	Iterator<T> iterator();
	Object getStreamable();
	
	/**
	 * @return SequenceM from this Streamable
	 */
	SequenceM<T> sequenceM();
	/**
	 * @return New Stream
	 */
	Stream<T> stream();
	
	
}
