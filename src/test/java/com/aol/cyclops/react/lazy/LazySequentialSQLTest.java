package com.aol.cyclops.react.lazy;

import static org.junit.Assert.fail;

import java.util.function.Supplier;

import org.junit.Test;

import com.aol.cyclops.react.base.BaseSequentialSQLTest;
import com.aol.cyclops.react.stream.traits.LazyFutureStream;

public class LazySequentialSQLTest extends BaseSequentialSQLTest {

	@Override
	protected <U> LazyFutureStream<U> of(U... array) {
		return LazyFutureStream.of(array);
	}

	@Override
	protected <U> LazyFutureStream<U> ofThread(U... array) {
		return LazyFutureStream.freeThread(array);
	}

	@Override
	protected <U> LazyFutureStream<U> react(Supplier<U>... array) {
		return LazyFutureStream.react(array);
	}

	Throwable ex;

	@Test(expected=X.class)
	public void testOnEmptyThrows() {
		ex = null;
		of().capture(e -> ex = e).onEmptyThrow(() -> new X()).toList();

		fail("Exception expected");
	}
}
