package com.aol.simple.react.lazy;

import static com.aol.simple.react.stream.lazy.LazyFutureStream.*;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.io.Serializable;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

import org.jooq.lambda.Seq;
import org.junit.Ignore;
import org.junit.Test;

import com.aol.simple.react.async.Queue;
import com.aol.simple.react.async.QueueFactories;
import com.aol.simple.react.base.BaseSeqTest;
import com.aol.simple.react.stream.eager.EagerFutureStreamImpl;
import com.aol.simple.react.stream.lazy.LazyFutureStream;
import com.aol.simple.react.stream.simple.SimpleReact;
import com.aol.simple.react.stream.traits.FutureStream;

public class LazySeqTest extends BaseSeqTest {
	@Test
	public void zipFastSlow() {
		Queue q = new Queue();
		LazyFutureStream.parallelBuilder().reactInfinitely(() -> sleep(100))
				.then(it -> q.add("100")).run(new ForkJoinPool(1));
		parallel(1, 2, 3, 4, 5, 6).zip(q.stream())
				.peek(it -> System.out.println(it))
				.collect(Collectors.toList());

	}

	@Test
	public void testBackPressureWhenZippingUnevenStreams() throws InterruptedException {

		Queue fast = parallelBuilder().reactInfinitely(() -> "100")
				.withQueueFactory(QueueFactories.boundedQueue(2)).toQueue();

		Thread t = new Thread(() -> {
			parallelBuilder().react(() -> 1, SimpleReact.times(10)).peek(c -> sleep(10))
					.zip(fast.stream()).forEach(it -> {
					});
		});
		t.start();

		int max = fast.getSizeSignal().getDiscrete().stream()
				.mapToInt(it -> (int) it).limit(5).max().getAsInt();
		assertThat(max, is(2));
		t.join();
	
	}

	@Test 
	public void testBackPressureWhenZippingUnevenStreams2() {

		Queue fast = parallelBuilder().reactInfinitely(() -> "100")
				.withQueueFactory(QueueFactories.boundedQueue(10)).toQueue();

		new Thread(() -> {
			parallelBuilder().react(() -> 1, SimpleReact.times(1000)).peek(c -> sleep(10))
					.zip(fast.stream()).forEach(it -> {
					});
		}).start();
		;

		int max = fast.getSizeSignal().getDiscrete().stream()
				.mapToInt(it -> (int) it).limit(50).max().getAsInt();
		assertThat(max, is(10));
	}

	@Test
	public void testOfType() {
		assertEquals(asList(1, 2, 3),
				of(1, "a", 2, "b", 3, null).ofType(Integer.class).toList());
		assertEquals(asList(1, "a", 2, "b", 3), of(1, "a", 2, "b", 3, null)
				.ofType(Serializable.class).toList());
	}

	@Test @Ignore
	public void shouldZipTwoInfiniteSequences() throws Exception {
		
		final FutureStream<Integer> units = LazyFutureStream.iterate(1,n -> n+1);
		final FutureStream<Integer> hundreds = LazyFutureStream.iterate(100,n-> n+100);
		final Seq<String> zipped = units.zip(hundreds, (n, p) -> n + ": " + p);

		
		assertThat(zipped.limit(5).join(),equalTo(of("1: 100", "2: 200", "3: 300", "4: 400", "5: 500").join()));
	}

	@Test
	public void shouldZipFiniteWithInfiniteSeq() throws Exception {
		
		final Seq<Integer> units = LazyFutureStream.iterate(1,n -> n+1).limit(5);
		final FutureStream<Integer> hundreds = LazyFutureStream.iterate(100,n-> n+100);
		final Seq<String> zipped = units.zip(hundreds, (n, p) -> n + ": " + p);
		
		assertThat(zipped.limit(5).join(),equalTo(of("1: 100", "2: 200", "3: 300", "4: 400", "5: 500").join()));
}

	@Test
	public void shouldZipInfiniteWithFiniteSeq() throws Exception {
		final FutureStream<Integer> units = LazyFutureStream.iterate(1,n -> n+1);
		final Seq<Integer> hundreds = LazyFutureStream.iterate(100,n-> n+100).limit(5);
		final Seq<String> zipped = units.zip(hundreds, (n, p) -> n + ": " + p);
		assertThat(zipped.limit(5).join(),equalTo(of("1: 100", "2: 200", "3: 300", "4: 400", "5: 500").join()));
	}

	
	@Test
	public void testCastPast() {
		assertEquals(asList(1, "a", 2, "b", 3, null),
				of(1, "a", 2, "b", 3, null).capture(e -> e.printStackTrace())
						.cast(Serializable.class).toList());

	}

	@Override
	protected <U> FutureStream<U> of(U... array) {

		return parallel(array);
	}
	protected Object sleep(int i) {
		try {
			Thread.currentThread().sleep(i);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return i;
	}

}
