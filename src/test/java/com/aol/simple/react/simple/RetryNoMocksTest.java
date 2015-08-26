package com.aol.simple.react.simple;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import com.aol.simple.react.stream.eager.EagerReact;
import com.aol.simple.react.stream.simple.SimpleReact;
import com.nurkiewicz.asyncretry.AsyncRetryExecutor;
import com.nurkiewicz.asyncretry.RetryExecutor;
import com.nurkiewicz.asyncretry.policy.AbortRetryException;

public class RetryNoMocksTest {

	Collection<Throwable> errors;
	AtomicInteger count;
	@Test
	public void testRetry() {
		count = new AtomicInteger(0);
		errors = Collections.synchronizedCollection(new ArrayList<>());
		final RetryExecutor executor = new AsyncRetryExecutor(Executors.newSingleThreadScheduledExecutor());
		List<Integer> result = new EagerReact().react(()->1,()->2,()->3)
												.withRetrier(executor)
												.capture(e -> errors.add(e))
												.retry(it -> throwOrReturn(it))
												.block();
		 
		assertThat(result.size(),is(3));
		assertThat(errors.size(),is(0));
	}
	@Test
	public void testAbort() {
		count = new AtomicInteger(0);
		errors = Collections.synchronizedCollection(new ArrayList<>());
		final RetryExecutor executor = new AsyncRetryExecutor(Executors.newSingleThreadScheduledExecutor());
		List<Integer> result = new SimpleReact().react(()->1,()->2,()->3)
												.withRetrier(executor)
												.capture(e -> errors.add(e))
												.retry(it -> abortOrReturn(it))
												.block();
		 
		assertThat(result.size(),is(2));
		assertThat(errors.iterator().next(),instanceOf(AbortRetryException.class));
	}
	@Test
	public void testRetryEnds() {
		count = new AtomicInteger(0);
		errors = Collections.synchronizedCollection(new ArrayList<>());
		final RetryExecutor executor = new AsyncRetryExecutor(Executors.newSingleThreadScheduledExecutor())
												.retryOn(Throwable.class)
												.withExponentialBackoff(5, 2)     //500ms times 2 after each retry
												.withMaxDelay(0_10)               //10 miliseconds
												.withUniformJitter()                //add between +/- 100 ms randomly
												.withMaxRetries(20);
		List result = SimpleReact.builder().retrier(executor).build().react(()->1,()->2,()->3)
												.capture(e -> errors.add(e))
												.retry((it) -> { throw new RuntimeException("failed"); })
												.block();
		 
		assertThat(result.size(),is(0));
		assertThat(errors.size(),is(3));
	}

	private Integer abortOrReturn(Integer it) {
		if(count.incrementAndGet()%2==0)
			throw new AbortRetryException();
		return it;
	}
	private Integer throwOrReturn(Integer it) {
		if(count.incrementAndGet()%2==0)
			throw new RuntimeException("failed");
		return it;
	}

}
