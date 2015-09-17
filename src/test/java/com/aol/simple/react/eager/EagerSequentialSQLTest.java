package com.aol.simple.react.eager;

import java.util.concurrent.CompletionException;
import java.util.function.Supplier;

import org.junit.Test;

import com.aol.simple.react.base.BaseSequentialSQLTest;
import com.aol.simple.react.base.BaseSequentialSQLTest.X;
import com.aol.simple.react.stream.traits.EagerFutureStream;

public class EagerSequentialSQLTest extends BaseSequentialSQLTest {

	@Override
	protected <U> EagerFutureStream<U> of(U... array) {
		return EagerFutureStream.of(array);
	}
	@Override
	protected <U> EagerFutureStream<U> ofThread(U... array) {
		return EagerFutureStream.freeThread(array);
	}

	@Override
	protected <U> EagerFutureStream<U> react(Supplier<U>... array) {
		return EagerFutureStream.react(array);
	}
	 @Test(expected=CompletionException.class)
	  public void testOnEmptyThrows(){
	    	of().onEmptyThrow(() -> new X()).toList();
	  }
	
}
