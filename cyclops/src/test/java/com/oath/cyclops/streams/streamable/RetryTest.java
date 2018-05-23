package com.oath.cyclops.streams.streamable;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
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

import com.oath.cyclops.util.ExceptionSoftener;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import cyclops.reactive.Streamable;


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
		assertThat(Streamable.of(1,2,3,4)
					.map(u->{throw new RuntimeException();})
					.recover(e->"hello")
					.firstValue(null),equalTo("hello"));
	}

	@Test
	public void recover2(){
		assertThat(Streamable.of(1,2,3,4)
					.map(i->i+2)
					.map(u->{throw new RuntimeException();})
					.recover(e->"hello")
					.firstValue(null),equalTo("hello"));
	}
	@Test
	public void recover3(){
		assertThat(Streamable.of(1,2,3,4)
					.map(i->i+2)
					.map(u->{throw new RuntimeException();})
					.map(i->"x!"+i)
					.recover(e->"hello")
					.firstValue(null),equalTo("hello"));
	}
	@Test
	public void recoverIO(){
		assertThat(Streamable.of(1,2,3,4)
					.map(u->{
            ExceptionSoftener.throwSoftenedException( new IOException()); return null;})
					.recover(e->"hello")
					.firstValue(null),equalTo("hello"));
	}

	@Test
	public void recover2IO(){
		assertThat(Streamable.of(1,2,3,4)
					.map(i->i+2)
					.map(u->{ExceptionSoftener.throwSoftenedException( new IOException()); return null;})
					.recover(IOException.class,e->"hello")
					.firstValue(null),equalTo("hello"));
	}
	@Test(expected=IOException.class)

	public void recoverIOUnhandledThrown(){
		assertThat(Streamable.of(1,2,3,4)
					.map(i->i+2)
					.map(u->{ExceptionSoftener.throwSoftenedException( new IOException()); return null;})
					.map(i->"x!"+i)
					.recover(IllegalStateException.class,e->"hello")
					.firstValue(null),equalTo("hello"));
	}


	private CompletableFuture<String> failedAsync(Throwable throwable) {
		final CompletableFuture<String> future = new CompletableFuture<>();
		future.completeExceptionally(throwable);
		return future;
	}









}
