package com.aol.cyclops.react.stream.pushable;

import java.util.stream.Stream;

import org.jooq.lambda.tuple.Tuple2;

import com.aol.cyclops.react.async.Adapter;
import com.aol.cyclops.react.async.Queue;

public abstract class AbstractPushableStream<T,X extends Adapter<T>,R extends Stream<T>> extends Tuple2<X,R> {

	
	public AbstractPushableStream(X v1,R v2) {
		super(v1, v2);
	}
	public X getInput(){
		return v1;
	}
	public R getStream(){
		return v2;
	}

	private static final long serialVersionUID = 1L;

}