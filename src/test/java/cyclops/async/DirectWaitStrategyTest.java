package cyclops.async;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.agrona.concurrent.ManyToOneConcurrentArrayQueue;
import org.junit.Test;

import cyclops.async.wait.DirectWaitStrategy;
import cyclops.async.wait.WaitStrategy.Offerable;
import cyclops.async.wait.WaitStrategy.Takeable;
public class DirectWaitStrategyTest {
	int called = 0;
	Takeable<String> takeable = ()->{ 
		called++;
		return null;
	};
	Offerable offerable = ()->{ 
		called++;
		return false;
	};
	@Test
	public void testTakeable() throws InterruptedException {
		called =0;
		String result = new DirectWaitStrategy<String>().take(takeable);
		assertTrue(result==null);
		assertThat(called,equalTo(1));
	}
	@Test
	public void testOfferable() throws InterruptedException {
		called =0;
		boolean result = new DirectWaitStrategy<String>().offer(offerable);
		assertThat(result,equalTo(false));
		assertThat(called,equalTo(1));
	}
	
	@Test
	public void testwithQueue(){
		Queue<String> q = new Queue<>(new ManyToOneConcurrentArrayQueue<String>(100),
									new DirectWaitStrategy<>(),
									new DirectWaitStrategy<>());
		
		q.offer("hello");
		assertThat(q.get(),equalTo("hello"));
	}

}
