package com.aol.simple.react.stream.pushable;

import java.util.stream.Stream;

import lombok.AllArgsConstructor;

import org.jooq.lambda.tuple.Tuple2;

import com.aol.simple.react.async.Queue;


/**
 * A more concrete Tuple2 impl
 * v1 is Queue<T>
 * v2 is Stream<T>
 * 
 * @author johnmcclean
 *
 * @param <T> data type
 */
public class PushableStream<T> extends AbstractPushableStream<T,Queue<T>,Stream<T>> {

	
	public PushableStream(Queue<T> v1, Stream<T> v2) {
		super(v1, v2);
	}
	

	private static final long serialVersionUID = 1L;

}
