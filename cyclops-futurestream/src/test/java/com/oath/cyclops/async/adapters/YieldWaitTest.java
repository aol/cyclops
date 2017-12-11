package com.oath.cyclops.async.adapters;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.agrona.concurrent.ManyToOneConcurrentArrayQueue;
import org.junit.Test;

import com.oath.cyclops.async.wait.WaitStrategy.Offerable;
import com.oath.cyclops.async.wait.WaitStrategy.Takeable;
import com.oath.cyclops.async.wait.YieldWait;

public class YieldWaitTest {
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
		String result = new YieldWait<String>().take(takeable);
		assertThat(result,equalTo("hello"));
		assertThat(called,equalTo(100));
	}
	@Test
	public void testOfferable() throws InterruptedException {
		called =0;
		boolean result = new YieldWait<String>().offer(offerable);
		assertThat(result,equalTo(true));
		assertThat(called,equalTo(100));
	}
	@Test
	public void testwithQueue(){
		Queue<String> q = new Queue<>(new ManyToOneConcurrentArrayQueue<String>(100),
									new YieldWait<>(),
									new YieldWait<>());

		q.offer("hello");
		assertThat(q.get(),equalTo("hello"));
	}

}
