package com.aol.cyclops.react.lazy;

import static org.junit.Assert.fail;

import java.util.function.Supplier;

import cyclops.stream.FutureStream;
import org.junit.Test;

import com.aol.cyclops.react.base.BaseSequentialSQLTest;

public class LazySequentialSQLTest extends BaseSequentialSQLTest {

	@Override
	protected <U> FutureStream<U> of(U... array) {
		return FutureStream.of(array);
	}

	@Override
	protected <U> FutureStream<U> ofThread(U... array) {
		return FutureStream.freeThread(array);
	}

	@Override
	protected <U> FutureStream<U> react(Supplier<U>... array) {
		return FutureStream.react(array);
	}

	Throwable ex;

	@Test(expected=X.class)
	public void testOnEmptyThrows() {
		ex = null;
		of().capture(e -> ex = e).onEmptyThrow(() -> new X()).toList();

		fail("Exception expected");
	}
}
