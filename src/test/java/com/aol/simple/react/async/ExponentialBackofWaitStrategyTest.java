package com.aol.simple.react.async;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.aol.simple.react.async.WaitStrategy.Offerable;
import com.aol.simple.react.async.WaitStrategy.Takeable;
import com.aol.simple.react.util.SimpleTimer;

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

}
