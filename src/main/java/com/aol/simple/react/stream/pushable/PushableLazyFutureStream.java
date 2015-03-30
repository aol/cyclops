package com.aol.simple.react.stream.pushable;

import java.util.stream.Stream;

import org.jooq.lambda.tuple.Tuple2;

import com.aol.simple.react.async.Queue;
import com.aol.simple.react.stream.lazy.LazyFutureStream;

/**
 * A more concrete Tuple2 impl
 * v1 is Queue&lt;T&gt;
 * v2 is LazyFutureStream&lt;T&gt;
 * 
 * @author johnmcclean
 *
 * @param <T> data type
 */
public class PushableLazyFutureStream<T> extends AbstractPushableStream<T,Queue<T>,LazyFutureStream<T>> {

	
	public PushableLazyFutureStream(Queue<T> v1, LazyFutureStream<T> v2) {
		super(v1, v2);
		
	}

	private static final long serialVersionUID = 1L;

}