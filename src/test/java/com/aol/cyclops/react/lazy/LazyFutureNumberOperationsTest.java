package com.aol.cyclops.react.lazy;

import java.util.function.Supplier;

import com.aol.cyclops.control.LazyReact;
import com.aol.cyclops.react.base.BaseNumberOperationsTest;
import com.aol.cyclops.types.futurestream.LazyFutureStream;

public class LazyFutureNumberOperationsTest extends BaseNumberOperationsTest{
	@Override
	protected <U> LazyFutureStream<U> of(U... array) {
		return LazyFutureStream.parallel(array);
	}
	@Override
	protected <U> LazyFutureStream<U> ofThread(U... array) {
		return LazyFutureStream.freeThread(array);
	}
	
	@Override
	protected <U> LazyFutureStream<U> react(Supplier<U>... array) {
		return LazyReact.parallelBuilder().react(array);
		
	}
	  

}
