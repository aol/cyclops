package com.aol.simple.react.async;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.*;

import org.junit.Test;

import com.aol.simple.react.async.wait.NoWaitRetry;
import com.aol.simple.react.async.wait.WaitStrategy.Offerable;
import com.aol.simple.react.async.wait.WaitStrategy.Takeable;

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

}
