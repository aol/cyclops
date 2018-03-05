package cyclops.streams.asyncreactivestreams;

import com.oath.cyclops.util.ExceptionSoftener;
import cyclops.companion.reactor.Fluxs;
import cyclops.reactive.ReactiveSeq;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.net.SocketException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyInt;


public class AsyncRSRetryTest {


	@Mock
	Function<Integer, String> serviceMock;

	Throwable error;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);


		error = null;
	}

	protected <U> ReactiveSeq<U> of(U... array){

		return Fluxs.reactiveSeq(Flux.just(array).subscribeOn(Schedulers.fromExecutor(ForkJoinPool.commonPool())));

	}

	@Test
	public void recover(){
		assertThat(of(1,2,3,4)
					.map(u->{throw new RuntimeException();})
					.recover(e->"hello")
					.firstValue(null),equalTo("hello"));
	}

	@Test
	public void recover2(){
		assertThat(of(1,2,3,4)
					.map(i->i+2)
					.map(u->{throw new RuntimeException();})
					.recover(e->"hello")
					.firstValue(null),equalTo("hello"));
	}
	@Test
	public void recover3(){
		assertThat(of(1,2,3,4)
					.map(i->i+2)
					.map(u->{throw new RuntimeException();})
					.map(i->"x!"+i)
					.recover(e->"hello")
					.firstValue(null),equalTo("hello"));
	}
	@Test
	public void recoverIO(){
		assertThat(of(1,2,3,4)
					.map(u->{
                        ExceptionSoftener.throwSoftenedException( new IOException()); return null;})
					.recover(e->"hello")
					.firstValue(null),equalTo("hello"));
	}

	@Test
	public void recover2IO(){
		assertThat(of(1,2,3,4)
					.map(i->i+2)
					.map(u->{
                        ExceptionSoftener.throwSoftenedException( new IOException()); return null;})
					.recover(IOException.class,e->"hello")
					.firstValue(null),equalTo("hello"));
	}
	@Test
	public void recoverIOUnhandledThrown(){
		assertThat(of(1,2,3,4)
					.map(i->i+2)
					.map(u->{
                        ExceptionSoftener.throwSoftenedException( new IOException()); return null;})
					.map(i->"x!"+i)
					.recover(IllegalStateException.class,e->"hello")
					.firstValue("boo1"),equalTo("boo1"));
	}

	@Test
	public void shouldSucceedAfterFewAsynchronousRetries() throws Exception {

		given(serviceMock.apply(anyInt())).willThrow(
				new RuntimeException(new SocketException("First")),
				new RuntimeException(new IOException("Second"))).willReturn(
				"42");

		long time = System.currentTimeMillis();
		String result = of( 1,  2, 3)
				.retry(serviceMock,7,200,TimeUnit.MILLISECONDS)
				.firstValue(null);
		assertThat(System.currentTimeMillis()-time,greaterThan(200l));
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


		List<String> result = of(1)

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


		List<String> result = of(1).retry(serviceMock).toList();

		assertThat(result.size(), is(0));

		error.printStackTrace();
		assertThat(error.getCause(), instanceOf(IllegalArgumentException.class));
		assertThat(error.getCause().getMessage(), is("DONT PANIC"));
	}



}
