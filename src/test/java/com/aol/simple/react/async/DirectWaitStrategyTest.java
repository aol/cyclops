package com.aol.simple.react.async;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

import org.junit.Test;

import uk.co.real_logic.agrona.concurrent.ManyToOneConcurrentArrayQueue;

import com.aol.simple.react.async.factories.QueueFactories;
import com.aol.simple.react.async.wait.DirectWaitStrategy;
import com.aol.simple.react.async.wait.NoWaitRetry;
import com.aol.simple.react.async.wait.WaitStrategy.Offerable;
import com.aol.simple.react.async.wait.WaitStrategy.Takeable;

import static org.junit.Assert.assertThat;
public class DirectWaitStrategyTest {
	int called = 0;
	Takeable<String> takeable = ()->{ 
		called++;
		return null;
	};
	Offerable offerable = ()->{ 
		called++;
		return false;
	};
	@Test
	public void testTakeable() throws InterruptedException {
		called =0;
		String result = new DirectWaitStrategy<String>().take(takeable);
		assertTrue(result==null);
		assertThat(called,equalTo(1));
	}
	@Test
	public void testOfferable() throws InterruptedException {
		called =0;
		boolean result = new DirectWaitStrategy<String>().offer(offerable);
		assertThat(result,equalTo(false));
		assertThat(called,equalTo(1));
	}
	
	@Test
	public void testwithQueue(){
		Queue<String> q = new Queue<>(new ManyToOneConcurrentArrayQueue<String>(100),
									new DirectWaitStrategy<>(),
									new DirectWaitStrategy<>());
		
		q.offer("hello");
		assertThat(q.get(),equalTo("hello"));
	}

}
