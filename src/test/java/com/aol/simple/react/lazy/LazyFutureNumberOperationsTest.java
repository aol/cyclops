package com.aol.simple.react.lazy;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.is;
import static org.jooq.lambda.tuple.Tuple.tuple;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.function.Supplier;

import org.jooq.lambda.Seq;
import org.jooq.lambda.tuple.Tuple2;
import org.junit.Test;

import com.aol.simple.react.base.BaseNumberOperationsTest;
import com.aol.simple.react.base.BaseSeqFutureTest;
import com.aol.simple.react.stream.eager.EagerReact;
import com.aol.simple.react.stream.traits.EagerFutureStream;
import com.aol.simple.react.stream.traits.FutureStream;

public class LazyFutureNumberOperationsTest extends BaseNumberOperationsTest{
	@Override
	protected <U> EagerFutureStream<U> of(U... array) {
		return EagerFutureStream.parallel(array);
	}
	@Override
	protected <U> EagerFutureStream<U> ofThread(U... array) {
		return EagerFutureStream.ofThread(array);
	}
	
	@Override
	protected <U> EagerFutureStream<U> react(Supplier<U>... array) {
		return EagerReact.parallelBuilder().react(array);
		
	}
	  

}
