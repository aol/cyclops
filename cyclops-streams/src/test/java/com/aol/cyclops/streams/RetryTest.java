package com.aol.cyclops.streams;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyInt;

import java.io.IOException;
import java.net.SocketException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import lombok.SneakyThrows;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.aol.cyclops.invokedynamic.ExceptionSoftener;
import com.aol.cyclops.sequence.SequenceM;


public class RetryTest {

	
	@Mock
	Function<Integer, String> serviceMock;
	
	Throwable error;
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		
		
		error = null;
	}

	@Test
	public void recover(){
		assertThat(SequenceM.of(1,2,3,4)
					.map(u->{throw new RuntimeException();})
					.recover(e->"hello")
					.firstValue(),equalTo("hello"));
	}

	@Test
	public void recover2(){
		assertThat(SequenceM.of(1,2,3,4)
					.map(i->i+2)
					.map(u->{throw new RuntimeException();})
					.recover(e->"hello")
					.firstValue(),equalTo("hello"));
	}
	@Test
	public void recover3(){
		assertThat(SequenceM.of(1,2,3,4)
					.map(i->i+2)
					.map(u->{throw new RuntimeException();})
					.map(i->"x!"+i)
					.recover(e->"hello")
					.firstValue(),equalTo("hello"));
	}
	@Test
	public void recoverIO(){
		assertThat(SequenceM.of(1,2,3,4)
					.map(u->{ExceptionSoftener.throwSoftenedException( new IOException()); return null;})
					.recover(e->"hello")
					.firstValue(),equalTo("hello"));
	}

	@Test
	public void recover2IO(){
		assertThat(SequenceM.of(1,2,3,4)
					.map(i->i+2)
					.map(u->{ExceptionSoftener.throwSoftenedException( new IOException()); return null;})
					.recover(IOException.class,e->"hello")
					.firstValue(),equalTo("hello"));
	}
	@Test(expected=IOException.class)
	
	public void recoverIOUnhandledThrown(){
		assertThat(SequenceM.of(1,2,3,4)
					.map(i->i+2)
					.map(u->{ExceptionSoftener.throwSoftenedException( new IOException()); return null;})
					.map(i->"x!"+i)
					.recover(IllegalStateException.class,e->"hello")
					.firstValue(),equalTo("hello"));
	}

	@Test
	public void shouldSucceedAfterFewAsynchronousRetries() throws Exception {

		given(serviceMock.apply(anyInt())).willThrow(
				new RuntimeException(new SocketException("First")),
				new RuntimeException(new IOException("Second"))).willReturn(
				"42");

	
		String result = SequenceM.of( 1,  2, 3)
				.retry(serviceMock)
				.firstValue();

		assertThat(result, is("42"));
	}

	private CompletableFuture<String> failedAsync(Throwable throwable) {
		final CompletableFuture<String> future = new CompletableFuture<>();
		future.completeExceptionally(throwable);
		return future;
	}


	

	@Test @Ignore
	public void shouldRethrowOriginalExceptionFromUserFutureCompletion()
			throws Exception {
		
		
		
			
		given(serviceMock.apply(anyInt())).willThrow(
				new RuntimeException("DONT PANIC"));

		
		List<String> result = SequenceM.of(1)
				
				.retry(serviceMock).toList();

		
		assertThat(result.size(), is(0));
		assertThat((error).getMessage(), is("DONT PANIC"));

	}

	

	@Test @Ignore
	public void shouldRethrowExceptionThatWasThrownFromUserTaskBeforeReturningFuture()
			throws Exception {
		error = null;
		
		given(serviceMock.apply(anyInt())).willThrow(
				new IllegalArgumentException("DONT PANIC"));

		
		List<String> result = SequenceM.of(1).retry(serviceMock).toList();
		
		assertThat(result.size(), is(0));

		error.printStackTrace();
		assertThat(error.getCause(), instanceOf(IllegalArgumentException.class));
		assertThat(error.getCause().getMessage(), is("DONT PANIC"));
	}

	

}