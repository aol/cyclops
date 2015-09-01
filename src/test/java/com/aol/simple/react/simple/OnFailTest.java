package com.aol.simple.react.simple;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;

import com.aol.simple.react.async.Queue.ClosedQueueException;
import com.aol.simple.react.stream.simple.SimpleReact;

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
		new SimpleReact().react(()->1,()->2)
			.then(this::throwException)
			.onFail(IOException.class, e-> shouldNeverBeCalled.incrementAndGet())
			.onFail(RuntimeException.class, e-> shouldBeCalled.incrementAndGet())
			.onFail(ClosedQueueException.class, e-> shouldNeverBeReached.incrementAndGet())
			.block();
		
		assertThat(shouldNeverBeCalled.get(),equalTo(0));
		assertThat(shouldBeCalled.get(),equalTo(2));
		assertThat(shouldNeverBeReached.get(),equalTo(0));
		
	}
	@Test
	public void chained2(){
		new SimpleReact().react(()->1,()->2)
			.then(this::throwException)
			.then(i->i+2)
			.onFail(IOException.class, e-> shouldNeverBeCalled.incrementAndGet())
			.onFail(RuntimeException.class, e-> shouldBeCalled.incrementAndGet())
			.onFail(ClosedQueueException.class, e-> shouldNeverBeReached.incrementAndGet())
			.block();
		
		assertThat(shouldNeverBeCalled.get(),equalTo(0));
		assertThat(shouldBeCalled.get(),equalTo(2));
		assertThat(shouldNeverBeReached.get(),equalTo(0));
		
	}
	private int throwException(int num) {
		throw new MyRuntimeTimeException();
	}
	
	static class MyRuntimeTimeException extends RuntimeException {}
}
