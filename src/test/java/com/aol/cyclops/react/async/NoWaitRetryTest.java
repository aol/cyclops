package com.aol.cyclops.react.async;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.*;

import org.junit.Test;

import uk.co.real_logic.agrona.concurrent.ManyToOneConcurrentArrayQueue;

import com.aol.cyclops.react.async.wait.ExponentialBackofWaitStrategy;
import com.aol.cyclops.react.async.wait.NoWaitRetry;
import com.aol.cyclops.react.async.wait.WaitStrategy.Offerable;
import com.aol.cyclops.react.async.wait.WaitStrategy.Takeable;

public class NoWaitRetryTest {
	int called = 0;
	Takeable<String> takeable = ()->{ 
		called++;
		if(called<100)
			return null;
		return "hello";
	};
	Offerable offerable = ()->{ 
		called++;
		if(called<100)
			return false;
		return true;
	};
	@Test
	public void testTakeable() throws InterruptedException {
		called =0;
		String result = new NoWaitRetry<String>().take(takeable);
		assertThat(result,equalTo("hello"));
		assertThat(called,equalTo(100));
	}
	@Test
	public void testOfferable() throws InterruptedException {
		called =0;
		boolean result = new NoWaitRetry<String>().offer(offerable);
		assertThat(result,equalTo(true));
		assertThat(called,equalTo(100));
	}

	@Test
	public void testwithQueue(){
		Queue<String> q = new Queue<>(new ManyToOneConcurrentArrayQueue<String>(100),
									new NoWaitRetry<>(),
									new NoWaitRetry<>());
		
		q.offer("hello");
		assertThat(q.get(),equalTo("hello"));
	}
}
