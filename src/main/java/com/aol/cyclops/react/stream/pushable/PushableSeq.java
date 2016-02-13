package com.aol.cyclops.react.stream.pushable;

import java.util.stream.Stream;

import org.jooq.lambda.Seq;
import org.jooq.lambda.tuple.Tuple2;

import com.aol.cyclops.react.async.Queue;

/**
 * A more concrete Tuple2 impl
 * v1 is Queue&lt;T&gt;
 * v2 is Seq&lt;T&gt;
 * 
 * @author johnmcclean
 *
 * @param <T> data type
 */
public class PushableSeq<T> extends AbstractPushableStream<T,Queue<T>,Seq<T>> {

	
	public PushableSeq(Queue<T> v1, Seq<T> v2) {
		super(v1, v2);
		
	}
	

	private static final long serialVersionUID = 1L;

}
