package com.aol.simple.react.lazy;

import java.util.function.Supplier;

import org.junit.Test;

import com.aol.simple.react.base.BaseSequentialSQLTest;
import com.aol.simple.react.base.BaseSequentialSQLTest.X;
import com.aol.simple.react.stream.traits.FutureStream;
import com.aol.simple.react.stream.traits.LazyFutureStream;

public class LazySequentialSQLTest extends BaseSequentialSQLTest {

	@Override
	protected <U> LazyFutureStream<U> of(U... array) {
		return LazyFutureStream.of(array);
	}
	@Override
	protected <U> LazyFutureStream<U> ofThread(U... array) {
		return LazyFutureStream.ofThread(array);
	}

	@Override
	protected <U> FutureStream<U> react(Supplier<U>... array) {
		return LazyFutureStream.react(array);
	}
	 @Test(expected=X.class)
	 public void testOnEmptyThrows(){
	    	of().onEmptyThrow(() -> new X()).toList();
	  }
}
