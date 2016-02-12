package com.aol.cyclops.javaslang.reactivestreams.reactivestream.tests;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyInt;

import java.io.IOException;
import java.net.SocketException;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.aol.cyclops.invokedynamic.ExceptionSoftener;
import com.aol.cyclops.javaslang.reactivestreams.ReactiveStream;
import com.aol.cyclops.control.SequenceM;


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
		assertThat(ReactiveStream.of(1,2,3,4)
					.map(u->{throw new RuntimeException();})
					.recover(e->"hello")
					.get(),equalTo("hello"));
	}

	@Test 
	public void recover2(){
		ReactiveStream.of(1,2,3,4)
		.map(i->i+2)
		.map(u->{ if(u==3) return "i"; else throw new RuntimeException();})
		.recover(e->"hello")
		.forEach(System.out::println);
		assertThat(ReactiveStream.of(1,2,3,4)
					.map(i->i+2)
					.map(u->{ if(u==3) return "i"; else throw new RuntimeException();})
					.recover(e->"hello")
					.toList().get(1),equalTo("hello"));
	}
	@Test @Ignore
	public void recover3(){
		assertThat(ReactiveStream.of(1,2,3,4)
					.map(i->i+2)
					.map(u->{ if(u==3) return "i"; else throw new RuntimeException();})
					.map(i->"x!"+i)
					.recover(e->"hello")
					.get(),equalTo("hello"));
	}
	@Test @Ignore
	public void recoverIO(){
		assertThat(ReactiveStream.of(1,2,3,4)
					.map(u->{ExceptionSoftener.throwSoftenedException( new IOException()); return null;})
					.recover(e->"hello")
					.get(),equalTo("hello"));
	}
	
	@Test
	public void recover2IO(){
		assertThat(ReactiveStream.of(1,2,3,4)
					.map(i->i+2)
					.map(u->{ExceptionSoftener.throwSoftenedException( new IOException()); return null;})
					.recover(IOException.class,e->"hello")
					.get(),equalTo("hello"));
	}
	@Test(expected=IOException.class)
	
	public void recoverIOUnhandledThrown(){
		assertThat(ReactiveStream.of(1,2,3,4)
					.map(i->i+2)
					.map(u->{ExceptionSoftener.throwSoftenedException( new IOException()); return null;})
					.map(i->"x!"+i)
					.recover(IllegalStateException.class,e->"hello")
					.get(),equalTo("hello"));
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


	

	

	

	

	

}