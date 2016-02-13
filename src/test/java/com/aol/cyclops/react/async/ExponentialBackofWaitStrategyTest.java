package com.aol.cyclops.react.async;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import uk.co.real_logic.agrona.concurrent.ManyToOneConcurrentArrayQueue;

import com.aol.cyclops.react.async.wait.DirectWaitStrategy;
import com.aol.cyclops.react.async.wait.ExponentialBackofWaitStrategy;
import com.aol.cyclops.react.async.wait.WaitStrategy.Offerable;
import com.aol.cyclops.react.async.wait.WaitStrategy.Takeable;
import com.aol.cyclops.react.util.SimpleTimer;

public class ExponentialBackofWaitStrategyTest {
	int called = 0;
	Takeable<String> takeable = ()->{ 
		called++;
		if(called<150)
			return null;
		return "hello";
	};
	Offerable offerable = ()->{ 
		called++;
		if(called<150)
			return false;
		return true;
	};
	@Test
	public void testTakeable() throws InterruptedException {
		SimpleTimer timer = new SimpleTimer();
		called =0;
		String result = new ExponentialBackofWaitStrategy<String>().take(takeable);
		assertThat(result,equalTo("hello"));
		assertThat(called,equalTo(150));
		assertThat(timer.getElapsedNanoseconds(),greaterThan(10000000l));
	}
	@Test
	public void testOfferable() throws InterruptedException {
		SimpleTimer timer = new SimpleTimer();
		called =0;
		boolean result = new ExponentialBackofWaitStrategy<String>().offer(offerable);
		assertThat(result,equalTo(true));
		assertThat(called,equalTo(150));
		assertThat(timer.getElapsedNanoseconds(),greaterThan(10000000l));
	}
	@Test
	public void testwithQueue(){
		Queue<String> q = new Queue<>(new ManyToOneConcurrentArrayQueue<String>(100),
									new ExponentialBackofWaitStrategy<>(),
									new ExponentialBackofWaitStrategy<>());
		
		q.offer("hello");
		assertThat(q.get(),equalTo("hello"));
	}

}
