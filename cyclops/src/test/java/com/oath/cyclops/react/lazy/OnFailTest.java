package com.oath.cyclops.react.lazy;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;

import cyclops.async.LazyReact;
import cyclops.async.adapters.Queue.ClosedQueueException;

public class OnFailTest {

	AtomicInteger shouldNeverBeCalled;
	AtomicInteger shouldBeCalled;
	AtomicInteger shouldNeverBeReached;

	@Before
	public void setup(){
		shouldNeverBeCalled = new AtomicInteger();
		shouldBeCalled = new AtomicInteger();
		shouldNeverBeReached = new AtomicInteger();
	}
	@Test
	public void chained(){
		new LazyReact().ofAsync(()->1,()->2)
			.then(this::throwException)
			.onFail(IOException.class, e-> shouldNeverBeCalled.incrementAndGet())
			.onFail(RuntimeException.class, e-> shouldBeCalled.incrementAndGet())
			.onFail(ClosedQueueException.class, e-> shouldNeverBeReached.incrementAndGet())
			.collect(Collectors.toList());

		assertThat(shouldNeverBeCalled.get(),equalTo(0));
		assertThat(shouldBeCalled.get(),equalTo(2));
		assertThat(shouldNeverBeReached.get(),equalTo(0));

	}
	private int throwException(int num) {
		throw new MyRuntimeTimeException();
	}
	static class MyRuntimeTimeException extends RuntimeException {}
}
