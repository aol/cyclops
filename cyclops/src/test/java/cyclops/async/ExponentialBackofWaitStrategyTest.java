package cyclops.async;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;

import cyclops.async.adapters.Queue;
import org.agrona.concurrent.ManyToOneConcurrentArrayQueue;
import org.junit.Test;

import cyclops.async.wait.ExponentialBackofWaitStrategy;
import cyclops.async.wait.WaitStrategy.Offerable;
import cyclops.async.wait.WaitStrategy.Takeable;
import com.oath.cyclops.util.SimpleTimer;

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
	@Test
	public void testwithQueue(){
		Queue<String> q = new Queue<>(new ManyToOneConcurrentArrayQueue<String>(100),
									new ExponentialBackofWaitStrategy<>(),
									new ExponentialBackofWaitStrategy<>());

		q.offer("hello");
		assertThat(q.get(),equalTo("hello"));
	}

}
