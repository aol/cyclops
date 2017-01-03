package com.aol.cyclops2.react.lazy;

import java.util.function.Supplier;

import cyclops.async.LazyReact;
import com.aol.cyclops2.react.base.BaseLazyNumberOperationsTest;
import cyclops.stream.FutureStream;

public class LazyNumberOperationsTest extends BaseLazyNumberOperationsTest{
	@Override
	protected <U> FutureStream<U> of(U... array) {
		return FutureStream.parallel(array);
	}
	@Override
	protected <U> FutureStream<U> ofThread(U... array) {
		return FutureStream.freeThread(array);
	}
	
	@Override
	protected <U> FutureStream<U> react(Supplier<U>... array) {
		return LazyReact.parallelBuilder().ofAsync(array);
		
	}
	  

}
