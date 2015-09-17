package com.aol.simple.react.eager;

import java.util.function.Supplier;

import com.aol.simple.react.base.BaseNumberOperationsTest;
import com.aol.simple.react.stream.eager.EagerReact;
import com.aol.simple.react.stream.traits.EagerFutureStream;

public class EagerFutureNumberOperationsTest extends BaseNumberOperationsTest{
	@Override
	protected <U> EagerFutureStream<U> of(U... array) {
		return EagerFutureStream.parallel(array);
	}
	@Override
	protected <U> EagerFutureStream<U> ofThread(U... array) {
		return EagerFutureStream.freeThread(array);
	}
	
	@Override
	protected <U> EagerFutureStream<U> react(Supplier<U>... array) {
		return EagerReact.parallelBuilder().react(array);
		
	}
	  

}
