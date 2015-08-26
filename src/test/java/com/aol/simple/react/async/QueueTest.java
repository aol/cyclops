package com.aol.simple.react.async;

import static com.aol.simple.react.stream.traits.EagerFutureStream.parallel;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.aol.simple.react.async.factories.QueueFactories;
import com.aol.simple.react.stream.lazy.LazyReact;
import com.aol.simple.react.stream.simple.SimpleReact;
import com.aol.simple.react.stream.traits.LazyFutureStream;
import com.aol.simple.react.stream.traits.EagerSimpleReactStream;

public class QueueTest {

	@Before
	public void setup() {
		found.set(0);
	}

	private final AtomicInteger found = new AtomicInteger(0);

	
	@Test
	public void backPressureTest() {
		
		
		
		Queue<Integer> q = new Queue<>(new LinkedBlockingQueue<>(2));
		new SimpleReact().react(() -> {
			q.offer(1);
			return found.getAndAdd(1);
		}, () -> {
			q.offer(1);
			return found.getAndAdd(1);
		}, () -> {
			q.offer(6);
			return found.getAndAdd(1);
		}, () -> {
			q.offer(5);
			return found.getAndAdd(1);
		});

		sleep(10);
		assertThat(found.get(), is(2));
		assertThat(q.stream().limit(2).collect(Collectors.toList()).size(),
				is(2));

		assertThat(q.stream().limit(2).collect(Collectors.toList()).size(),
				is(2));
		sleep(10);
		assertThat(found.get(), is(4));
	}

	@Test
	public void backPressureJDKTest() {
		Queue<String> q = new Queue<>(new LinkedBlockingQueue<>(2));
		new SimpleReact().react(() -> {
			Stream.of("1", "2", "3", "4").forEach(it -> {
				q.offer(it);
				found.getAndAdd(1);
			});
			return 1;
		});

		sleep(10);
		assertThat(found.get(), is(2));
		assertThat(q.stream().limit(2).collect(Collectors.toList()).size(),
				is(2));

		assertThat(q.stream().limit(2).collect(Collectors.toList()).size(),
				is(2));
		sleep(10);
		assertThat(found.get(), is(4));
	}

	@Test
	public void backPressureTimeoutTestVeryLow() {
		Queue<Integer> q = new Queue<Integer>(new LinkedBlockingQueue<>(2))
				.withOfferTimeout(1).withOfferTimeUnit(TimeUnit.MICROSECONDS);
		Set<Boolean> results = new SimpleReact().react(
				() -> offerAndIncrementFound(q),
				() -> offerAndIncrementFound(q),
				() -> offerAndIncrementFound(q),
				() -> offerAndIncrementFound(q)).block(Collectors.toSet());

		sleep(10);
		assertThat(found.get(), is(4));

		assertThat(results.size(), is(2));
		assertThat(results, hasItem(false)); // some offers failed.

	}

	@Test
	public void backPressureTimeoutTestVeryHigh() {
		Queue<Integer> q = new Queue<Integer>(new LinkedBlockingQueue<>(2))
				.withOfferTimeout(1).withOfferTimeUnit(TimeUnit.DAYS);
		EagerSimpleReactStream<Boolean> s = new SimpleReact().react(
				() -> offerAndIncrementFound(q),
				() -> offerAndIncrementFound(q),
				() -> offerAndIncrementFound(q),
				() -> offerAndIncrementFound(q));

		sleep(10);
		assertThat(found.get(), is(2));
		assertThat(q.stream().limit(4).collect(Collectors.toList()).size(),
				is(4));

		Set<Boolean> results = s.block(Collectors.toSet());

		assertThat(found.get(), is(4));
		assertThat(results.size(), is(1));
		assertThat(results, not(hasItem(false))); // some offers failed.

	}

	private Boolean offerAndIncrementFound(Queue<Integer> q) {
		boolean ret = q.offer(1);
		found.getAndAdd(1);
		return ret;
	}

	@Test
	public void testSizeSignal() {
		System.out.println("hello");
		Queue<Integer> q = new Queue<Integer>();
		Signal<Integer> s = q.getSizeSignal();

		q.add(1);
		q.add(1);
		q.add(1);
		q.stream().limit(3).forEach(it -> System.out.println(it)); // drain the
																	// queue
		q.add(1); // queue size is 1
		sleep(50);
		List<Integer> sizes = s.getDiscrete().stream().limit(7)
				.collect(Collectors.toList());
		assertThat(sizes.get(0), is(1));
		assertThat(sizes.get(1), is(2));
		assertThat(sizes.get(2), is(3));
		assertThat(sizes.get(3), is(2));
		assertThat(sizes.get(4), is(1));
		assertThat(sizes.get(5), is(0));
		assertThat(sizes.get(6), is(1));

	}

	@Test
	public void testAdd() {
		for(int i=0;i<1000;i++){
			found.set(0);
			Queue<Integer> q = new Queue<>(new LinkedBlockingQueue<>(2));
			new SimpleReact().react(() -> {
				q.add(1);
				return found.getAndAdd(1);
			}, () -> {
				q.add(1);
				return found.getAndAdd(1);
			}, () -> {
				q.add(6);
				return found.getAndAdd(1);
			}, () -> {
				q.add(5);
				return found.getAndAdd(1);
			}).block();
	
		//	sleep(10);
			
			assertThat(found.get(), is(4));
		}

	}

	@Test
	public void testAddFull() {
		Queue<Integer> q = new Queue<>(new LinkedBlockingQueue<>(2));

		assertTrue(q.add(1));
		assertTrue(q.add(2));
		assertFalse(q.add(3));

	}

	@Test
	public void enqueueTest() {
		Stream<String> stream = Stream.of("1", "2", "3");
		Queue<String> q = new Queue(new LinkedBlockingQueue());
		q.fromStream(stream);

		Integer dequeued = q.stream().limit(3).map(it -> Integer.valueOf(it))
				.reduce(0, (acc, next) -> acc + next);

		assertThat(dequeued, is(6));
	}

	volatile int count = 0;
	volatile int count1 = 10000;

	@Test
	public void simpleMergingTestLazyIndividualMerge() {
		Queue<Integer> q = new Queue(new LinkedBlockingQueue());
		q.offer(0);
		q.offer(100000);

		List<Integer> result = q.stream().limit(2)
				.peek(it -> System.out.println(it))
				.collect(Collectors.toList());
		assertThat(result, hasItem(100000));
		assertThat(result, hasItem(0));

	}

	@Test
	@Ignore
	// too non-deterministic to run regularly - relying on population from
	// competing threads
	public void mergingTestLazyIndividualMerge() {
		count = 0;
		count1 = 100000;

		Queue<Integer> q = new Queue(new LinkedBlockingQueue());
		LazyReact.parallelBuilder().reactInfinitely(() -> count++)
				.then(it -> q.offer(it)).run(new ForkJoinPool(1));
		LazyReact.parallelBuilder().reactInfinitely(() -> count1++)
				.then(it -> q.offer(it)).run(new ForkJoinPool(1));

		List<Integer> result = q.stream().limit(1000)
				.peek(it -> System.out.println(it))
				.collect(Collectors.toList());
		assertThat(result, hasItem(100000));
		assertThat(result, hasItem(0));

	}

	@Test
	public void simpleMergingTestEagerStreamMerge() {

		Queue<Integer> q = new Queue(new LinkedBlockingQueue());

		q.offer(0);
		q.offer(100000);

		List<Integer> result = q.stream().limit(2)
				.peek(it -> System.out.println(it))
				.collect(Collectors.toList());
		assertThat(result, hasItem(100000));
		assertThat(result, hasItem(0));

	}

	@Test
	@Ignore
	// too non-deterministic to run regularly - relying on population from
	// competing threads
	public void mergingTestEagerStreamMerge() {
		count = 0;
		count1 = 100000;

		Queue<Integer> q = new Queue(new LinkedBlockingQueue());

		new SimpleReact().react(() -> q.fromStream(Stream
				.generate(() -> count++)));
		new SimpleReact().react(() -> q.fromStream(Stream
				.generate(() -> count1++)));

		List<Integer> result = q.stream().limit(1000)
				.peek(it -> System.out.println(it))
				.collect(Collectors.toList());
		assertThat(result, hasItem(100000));
		assertThat(result, hasItem(0));

	}

	@Test
	public void queueTestBlock() {

		try {
			Queue q = new Queue<>(new LinkedBlockingQueue<>());

			new SimpleReact().react(() -> q.offer(1), () -> q.offer(2), () -> {
				sleep(50);
				return q.offer(4);
			}, () -> {
				sleep(400);
				q.close();
				return 1;
			});

			parallel().fromStream(q.streamCompletableFutures())
					.then(it -> "*" + it).peek(it -> found.getAndAdd(1))
					.peek(it -> System.out.println(it)).block();

		} finally {
			assertThat(found.get(), is(3));
		}

	}

	@Test
	public void queueTestTimeout() {

		
		Queue<Integer> q = new Queue<>(new LinkedBlockingQueue<Integer>())
				.withTimeout(1).withTimeUnit(TimeUnit.MILLISECONDS);

		new SimpleReact().react(() -> q.offer(1), () -> q.offer(2), () -> {
			sleep(500);
			return q.offer(4);
		}, () -> q.offer(5));

		Collection<String> results = parallel().fromStream(q.stream())
				.then(it -> "*" + it).collect(Collectors.toList());

		assertThat(results.size(), lessThan(4));
		assertThat(results, not(hasItem("*4")));
		//assertThat(results, hasItem("*5"));
		

	}

	@Test
	public void queueTestRun() {
		try {
			Queue<Integer> q = new Queue<>(new LinkedBlockingQueue<>());

			new SimpleReact().react(() -> q.offer(1), () -> q.offer(2), () -> {
				sleep(200);
				return q.offer(4);
			}, () -> {
				sleep(400);
				q.close();
				return 1;
			});

			List<String> result = parallel().fromStream(q.stream())
					.then(it -> "*" + it).peek(it -> found.getAndAdd(1))
					.peek(it -> System.out.println(it))
					.collect(Collectors.toList());

			assertThat(result, hasItem("*1"));

		} finally {
			assertThat(found.get(), is(3));
		}

	}

	boolean called = false;
	@Test
	public void stackOverflowQuestion() {
		called = false;
		Queue<String> queue = QueueFactories.<String> unboundedQueue().build();

		new Thread(() -> {
			for(int i=0;i<10;i++) {
				queue.add("New message " + System.currentTimeMillis());
			}
			queue.close();
		}).start();
		queue.stream().peek(this::called).forEach(e -> System.out.println(e));
		assertTrue(called);
	}

	private void called(String message){
		called=  true;
	}
	private int sleep(int i) {
		try {
			Thread.sleep(i);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return i;

	}
}
