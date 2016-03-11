package com.aol.cyclops.util.stream.pushable;

import org.jooq.lambda.Seq;

import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.data.async.Queue;

/**
 * A more concrete Tuple2 impl
 * v1 is Queue&lt;T&gt;
 * v2 is Seq&lt;T&gt;
 * 
 * @author johnmcclean
 *
 * @param <T> data type
 */
public class PushableReactiveSeq<T> extends AbstractPushableStream<T,Queue<T>,ReactiveSeq<T>> {

	
	public PushableReactiveSeq(Queue<T> v1, ReactiveSeq<T> v2) {
		super(v1, v2);
		
	}
	

	private static final long serialVersionUID = 1L;

}
