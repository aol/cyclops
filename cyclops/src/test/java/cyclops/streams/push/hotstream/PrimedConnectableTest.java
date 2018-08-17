package cyclops.streams.push.hotstream;

import com.oath.cyclops.types.stream.PausableConnectable;
import cyclops.reactive.Spouts;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.locks.LockSupport;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class PrimedConnectableTest {
	static final Executor exec = Executors.newFixedThreadPool(1);
	volatile Object value;

	@Test
	public void hotStream() throws InterruptedException{
		value= null;
		CountDownLatch latch = new CountDownLatch(1);
		Spouts.of(1,2,3)
				.peek(v->value=v)
				.peek(v->latch.countDown())
				.primedHotStream(exec)
				.connect().forEach(System.out::println);

		latch.await();
		assertTrue(value!=null);
	}
	@Test
	public void hotStreamConnect() throws InterruptedException{


		for(int i=0;i<1_000;i++)
		{
			System.out.println(i);
			value= null;
			CountDownLatch latch = new CountDownLatch(1);
			Spouts.range(0,Integer.MAX_VALUE)
					.limit(100)
					.peek(v->value=v)
					.peek(v->latch.countDown())
					.peek(System.out::println)
					.primedHotStream(exec)
					.connect()
					.limit(100)
					.runFuture(ForkJoinPool.commonPool(),s->s.forEach(System.out::println));

			latch.await();
			assertTrue(value!=null);
		}
	}

	@Test
	public void hotStreamConnectBlockingQueue() throws InterruptedException{
		value= null;
		CountDownLatch latch = new CountDownLatch(1);
		Spouts.range(0,Integer.MAX_VALUE)
				.limit(1000)
				.peek(v->value=v)
				.peek(v->latch.countDown())
				.primedHotStream(exec)
				.connect(new LinkedBlockingQueue<>())
				.limit(100)
				.runFuture(ForkJoinPool.commonPool(),s->s.forEach(System.out::println));

		latch.await();
		assertTrue(value!=null);
	}
	@Test
	public void hotStreamCapture() throws InterruptedException{


		List<Integer> list = Spouts.range(0,Integer.MAX_VALUE)
									 .limit(1000)
									 .primedHotStream(exec)
									 .connect()
									 .limit(2)
									 .toList();

		assertThat(list,equalTo(Arrays.asList(0,1)));

	}
	@Test
	public void hotStreamCaptureLong() throws InterruptedException{

		List<Long> list = Spouts.rangeLong(0,Long.MAX_VALUE)
				.limit(1000)
				.primedHotStream(exec)
				.connect()
				.limit(2)
				.toList();

		assertThat(list,equalTo(Arrays.asList(0l,1l)));

	}
	@Test
	public void hotStreamCaptureReversed() throws InterruptedException{


		List<Integer> list = Spouts.range(0,Integer.MAX_VALUE)
				.limit(1000)
				.reverse()
				.primedHotStream(exec)
				.connect()
				.limit(2)
				.toList();

		assertThat(list,equalTo(Arrays.asList(999,998)));

	}
	volatile boolean active;
	@Test
	public void hotStreamConnectPausable() throws InterruptedException{
		value= null;
		active=true;
		CountDownLatch latch = new CountDownLatch(1);
		PausableConnectable<Integer> s = Spouts.range(0,Integer.MAX_VALUE)
				.takeWhile(i->active)
				.peek(v->value=v)
				.peek(v->latch.countDown())
				.primedPausableHotStream(exec);
		s.connect(new LinkedBlockingQueue<>())
				.limit(100)
				.runFuture(ForkJoinPool.commonPool(),st->st.forEach(System.out::println));

		Object oldValue = value;


		try{
			s.pause();
			s.unpause();
			Thread.sleep(1000);
			s.pause();
			assertTrue(value!=oldValue);
			s.unpause();
			latch.await();
			assertTrue(value!=null);
		}finally{
		    active=false;
			s.unpause();
		}
	}
	@Test
	public void hotStreamConnectPausableConnect() throws InterruptedException{
		value= null;
		CountDownLatch latch = new CountDownLatch(1);
		PausableConnectable<Integer> s = Spouts.range(0,Integer.MAX_VALUE)
				.limit(50000)
				.peek(v->value=v)
				.peek(v->latch.countDown())
				.primedPausableHotStream(exec);
		s.connect()
				.limit(100)
				.runFuture(ForkJoinPool.commonPool(),st->st.forEach(System.out::println));


		Object oldValue = value;
		try{
			s.pause();
			s.unpause();
			LockSupport.parkNanos(1000l);
			s.pause();
			System.out.println(value);
			assertTrue("value =" + value!=oldValue);
			s.unpause();
			latch.await();
			assertTrue(value!=null);
		}finally{
			s.unpause();
		}
	}
}
