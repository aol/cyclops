package cyclops.futurestream.react.simple;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Before;
import org.junit.Test;

import com.oath.cyclops.async.SimpleReact;
import com.oath.cyclops.async.adapters.Queue.ClosedQueueException;

public class OnFailTest {

	AtomicInteger shouldNeverBeCalled;
	AtomicInteger shouldBeCalled;
	AtomicInteger shouldNeverBeReached;

	@Before
	public void setup(){
		shouldNeverBeCalled = new AtomicInteger();
		shouldBeCalled = new AtomicInteger();
		shouldNeverBeReached = new AtomicInteger();
	}
	@Test
	public void chained(){
		new SimpleReact().ofAsync(()->1,()->2)
			.then(this::throwException)
			.onFail(IOException.class, e-> shouldNeverBeCalled.incrementAndGet())
			.onFail(RuntimeException.class, e-> shouldBeCalled.incrementAndGet())
			.onFail(ClosedQueueException.class, e-> shouldNeverBeReached.incrementAndGet())
			.block();

		assertThat(shouldNeverBeCalled.get(),equalTo(0));
		assertThat(shouldBeCalled.get(),equalTo(2));
		assertThat(shouldNeverBeReached.get(),equalTo(0));

	}
	@Test
	public void chained2(){
		new SimpleReact().ofAsync(()->1,()->2)
			.then(this::throwException)
			.then(i->i+2)
			.onFail(IOException.class, e-> shouldNeverBeCalled.incrementAndGet())
			.onFail(RuntimeException.class, e-> shouldBeCalled.incrementAndGet())
			.onFail(ClosedQueueException.class, e-> shouldNeverBeReached.incrementAndGet())
			.block();

		assertThat(shouldNeverBeCalled.get(),equalTo(0));
		assertThat(shouldBeCalled.get(),equalTo(2));
		assertThat(shouldNeverBeReached.get(),equalTo(0));

	}
	private int throwException(int num) {
		throw new MyRuntimeTimeException();
	}

	static class MyRuntimeTimeException extends RuntimeException {}
}
