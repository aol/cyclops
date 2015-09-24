package com.aol.simple.react.lazy;

import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;

import java.util.function.Supplier;

import org.junit.Test;

import com.aol.simple.react.base.BaseSequentialSQLTest;
import com.aol.simple.react.stream.traits.LazyFutureStream;

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

	@Test
	public void testOnEmptyThrows() {
		ex = null;
		of().capture(e -> ex = e).onEmptyThrow(() -> new X()).toList();

		assertThat(ex, instanceOf(X.class));
	}
}
