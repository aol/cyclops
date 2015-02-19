package com.aol.simple.react;

import static com.nurkiewicz.asyncretry.backoff.FixedIntervalBackoff.DEFAULT_PERIOD_MILLIS;
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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.nurkiewicz.asyncretry.AsyncRetryContext;
import com.nurkiewicz.asyncretry.AsyncRetryExecutor;
import com.nurkiewicz.asyncretry.RetryContext;
import com.nurkiewicz.asyncretry.RetryExecutor;
import com.nurkiewicz.asyncretry.policy.AbortRetryException;
import com.nurkiewicz.asyncretry.policy.RetryPolicy;
//async retry test repurposed for simple react
public class RetryTest {
	

	@Mock
	protected ScheduledExecutorService schedulerMock;

	

	@Before
	public void injectMocks() {
		MockitoAnnotations.initMocks(this);
		setupMocks();
	}
	
	
	private Function<Integer,String> serviceMock = Mockito.mock(Function.class);
		
	

		@Test
		public void shouldSucceedAfterFewAsynchronousRetries() throws Exception {
			//given
			final RetryExecutor executor = new AsyncRetryExecutor(schedulerMock);
			given(serviceMock.apply(anyInt())).willThrow(
					new RuntimeException(new SocketException("First")),
					new RuntimeException(new IOException("Second"))).willReturn("42");

			
			//when
			String result = new SimpleReact().react(()->1,()->2,()->3).withRetrier(executor).retry(serviceMock).collectResults().first().getResults();

			//then
			assertThat(result,is("42"));
		}
		
		private CompletableFuture<String> failedAsync(Throwable throwable) {
			final CompletableFuture<String> future = new CompletableFuture<>();
			future.completeExceptionally(throwable);
			return future;
		}
		
		@Test
		public void shouldScheduleTwoTimesWhenRetries() throws Exception {
			//given
			final RetryExecutor executor = new AsyncRetryExecutor(schedulerMock);
			given(serviceMock.apply(anyInt())).willThrow(
					new RuntimeException(new SocketException("First")),
					new RuntimeException(new IOException("Second"))).willReturn("42");

			//when
			String result = new SimpleReact().react(()->1).withRetrier(executor).retry(serviceMock).collectResults().first().getResults();

			//then
			

			final InOrder order = inOrder(schedulerMock);
			order.verify(schedulerMock,times(1)).schedule(notNullRunnable(), eq(0L), millis());
			order.verify(schedulerMock, times(2)).schedule(notNullRunnable(), eq(DEFAULT_PERIOD_MILLIS), millis());
		}

		
		Throwable error;
		@Test
		public void shouldRethrowOriginalExceptionFromUserFutureCompletion() throws Exception {
			error = null;
			//given
			final RetryExecutor executor = new AsyncRetryExecutor(schedulerMock).
					abortOn(RuntimeException.class);
			given(serviceMock.apply(anyInt())).willThrow(new RuntimeException("DONT PANIC"));
			

			//when
			List<String> result = new SimpleReact().react(()->1).withRetrier(executor).capture(e-> error=e).retry(serviceMock).block();

			//then
			assertThat(result.size(),is(0));
			assertThat(error.getMessage(),is("DONT PANIC"));
			
		}
		
		@Test
		public void shouldAbortWhenTargetFutureWantsToAbort() throws Exception {
			error = null;
			//given
			final RetryExecutor executor = new AsyncRetryExecutor(schedulerMock);
			given(serviceMock.apply(anyInt())).willThrow(new AbortRetryException());
			

			//when
			List<String> result = new SimpleReact().react(()->1).withRetrier(executor).capture(e-> error=e).retry(serviceMock).block();

			//then
			assertThat(result.size(),is(0));

			assertThat(error,is(AbortRetryException.class));
		}

		@Test
		public void shouldRethrowExceptionThatWasThrownFromUserTaskBeforeReturningFuture() throws Exception {
			error = null;
			//given
			final RetryExecutor executor = new AsyncRetryExecutor(schedulerMock).
					abortOn(IllegalArgumentException.class);
			given(serviceMock.apply(anyInt())).willThrow(new IllegalArgumentException("DONT PANIC"));
			

			//when
			List<String> result = new SimpleReact().react(()->1).withRetrier(executor).capture(e-> error=e).retry(serviceMock).block();
			//then
			assertThat(result.size(),is(0));

			assertThat(error,is(IllegalArgumentException.class));
			assertThat(error.getMessage(),is("DONT PANIC"));
		}

		
		private void setupMocks() {
			given(schedulerMock.schedule(notNullRunnable(), anyLong(), eq(TimeUnit.MILLISECONDS))).willAnswer(invocation -> {
				((Runnable) invocation.getArguments()[0]).run();
				return null;
			});
		}
		protected Runnable notNullRunnable() {
			return (Runnable) notNull();
		}

		protected RetryContext notNullRetryContext() {
			return (RetryContext) notNull();
		}

		protected TimeUnit millis() {
			return eq(TimeUnit.MILLISECONDS);
		}

		protected RetryContext anyRetry() {
			return retry(1);
		}

		protected RetryContext retry(int ret) {
			return new AsyncRetryContext(RetryPolicy.DEFAULT, ret, new Exception());
		}
		
	}
