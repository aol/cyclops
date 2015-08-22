package com.aol.simple.react.eager;

import java.util.function.Supplier;

import com.aol.simple.react.base.BaseSequentialSQLTest;
import com.aol.simple.react.stream.traits.FutureStream;
import com.aol.simple.react.stream.traits.LazyFutureStream;

public class EagerSequentialSQLTest extends BaseSequentialSQLTest {

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
	
}
