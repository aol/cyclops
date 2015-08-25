package com.aol.simple.react.lazy;

import java.util.function.Supplier;

import com.aol.simple.react.base.BaseNumberOperationsTest;
import com.aol.simple.react.stream.lazy.LazyReact;
import com.aol.simple.react.stream.traits.LazyFutureStream;

public class LazyFutureNumberOperationsTest extends BaseNumberOperationsTest{
	@Override
	protected <U> LazyFutureStream<U> of(U... array) {
		return LazyFutureStream.parallel(array);
	}
	@Override
	protected <U> LazyFutureStream<U> ofThread(U... array) {
		return LazyFutureStream.ofThread(array);
	}
	
	@Override
	protected <U> LazyFutureStream<U> react(Supplier<U>... array) {
		return LazyReact.parallelBuilder().react(array);
		
	}
	  

}
