package com.aol.cyclops.react.simple;

import static com.nurkiewicz.asyncretry.backoff.FixedIntervalBackoff.DEFAULT_PERIOD_MILLIS;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.notNull;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;

import java.io.IOException;
import java.net.SocketException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.aol.cyclops.control.SimpleReact;
import com.aol.cyclops.react.SimpleReactFailedStageException;
import com.nurkiewicz.asyncretry.AsyncRetryExecutor;
import com.nurkiewicz.asyncretry.RetryExecutor;
import com.nurkiewicz.asyncretry.policy.AbortRetryException;


//async retry test specs running against SimpleReact

public class RetryTest {

	@Mock
	ScheduledExecutorService schedulerMock;
	@Mock
	Function<Integer, String> serviceMock;
	RetryExecutor executor;
	Throwable error;
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		given(
				schedulerMock.schedule((Runnable)notNull(), anyLong(),
						eq(TimeUnit.MILLISECONDS))).willAnswer(invocation -> {
			((Runnable) invocation.getArguments()[0]).run();
			return null;
		});
		executor = new AsyncRetryExecutor(schedulerMock);
		error = null;
	}

	

	@Test
	public void shouldSucceedAfterFewAsynchronousRetries() throws Exception {

		given(serviceMock.apply(anyInt())).willThrow(
				new RuntimeException(new SocketException("First")),
				new RuntimeException(new IOException("Second"))).willReturn(
				"42");

	
		String result = new SimpleReact().react(() -> 1, () -> 2, () -> 3)
				.withRetrier(executor).retry(serviceMock)
				.block().firstValue();

		assertThat(result, is("42"));
	}

	private CompletableFuture<String> failedAsync(Throwable throwable) {
		final CompletableFuture<String> future = new CompletableFuture<>();
		future.completeExceptionally(throwable);
		return future;
	}

	@Test
	public void shouldScheduleTwoTimesWhenRetries() throws Exception {
		
		given(serviceMock.apply(anyInt())).willThrow(
				new RuntimeException(new SocketException("First")),
				new RuntimeException(new IOException("Second"))).willReturn(
				"42");

		
		String result = new SimpleReact().react(() -> 1).withRetrier(executor)
				.retry(serviceMock).block().firstValue();

	

		final InOrder order = inOrder(schedulerMock);
		order.verify(schedulerMock, times(1)).schedule((Runnable)notNull(),
				eq(0L),  eq(TimeUnit.MILLISECONDS));
		order.verify(schedulerMock, times(2)).schedule((Runnable)notNull(),
				eq(DEFAULT_PERIOD_MILLIS),  eq(TimeUnit.MILLISECONDS));
	}

	

	@Test
	public void shouldRethrowOriginalExceptionFromUserFutureCompletion()
			throws Exception {
		
		
		final RetryExecutor executor = new AsyncRetryExecutor(schedulerMock)
				.abortOn(RuntimeException.class);
		given(serviceMock.apply(anyInt())).willThrow(
				new RuntimeException("DONT PANIC"));

		
		List<String> result = new SimpleReact().react(() -> 1)
				.withRetrier(executor).capture(e -> error = e)
				.retry(serviceMock).block();

		
		assertThat(result.size(), is(0));
		assertThat(((SimpleReactFailedStageException)error).getCause().getMessage(), is("DONT PANIC"));

	}

	@Test
	public void shouldAbortWhenTargetFutureWantsToAbort() throws Exception {
		error = null;
		
		
		given(serviceMock.apply(anyInt())).willThrow(new AbortRetryException());

		
		List<String> result = new SimpleReact().react(() -> 1)
				.withRetrier(executor)
				.capture(e -> error = e)
				.retry(serviceMock).block();

		
		assertThat(result.size(), is(0));
		error.printStackTrace();
		assertThat(error, instanceOf(AbortRetryException.class));
	}

	@Test
	public void shouldRethrowExceptionThatWasThrownFromUserTaskBeforeReturningFuture()
			throws Exception {
		error = null;
		
		final RetryExecutor executor = new AsyncRetryExecutor(schedulerMock)
				.abortIf(t->   t.getCause().getClass().isAssignableFrom(IllegalArgumentException.class));
		given(serviceMock.apply(anyInt())).willThrow(
				new IllegalArgumentException("DONT PANIC"));

		
		List<String> result = new SimpleReact().react(() -> 1)
				.withRetrier(executor).capture(e -> error = e)
				.retry(serviceMock).block();
		
		assertThat(result.size(), is(0));

		error.printStackTrace();
		assertThat(error.getCause(), instanceOf(IllegalArgumentException.class));
		assertThat(error.getCause().getMessage(), is("DONT PANIC"));
	}

	

}
