package com.aol.simple.react.stream.pushable;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jooq.lambda.Seq;
import org.jooq.lambda.tuple.Tuple2;
import org.junit.Test;
import org.mockito.internal.util.collections.Sets;

import com.aol.simple.react.async.Queue;
import com.aol.simple.react.async.Signal;
import com.aol.simple.react.stream.traits.LazyFutureStream;
import com.aol.simple.react.threads.SequentialElasticPools;


public class PushableStreamTest {

	@Test
	public void testLazyFutureStream() {
		PushableLazyFutureStream<Integer> pushable = new PushableStreamBuilder()
				.pushableLazyFutureStream();
		pushable.getInput().add(100);
		pushable.getInput().close();
		assertThat(pushable.getStream().collect(Collectors.toList()),
				hasItem(100));
	}

	@Test
	public void testReactPool() {
		PushableLazyFutureStream<Integer> pushable = new PushableStreamBuilder()
				.pushable(SequentialElasticPools.lazyReact);
		pushable.getInput().add(100);
		pushable.getInput().close();
		assertThat(pushable.getStream().collect(Collectors.toList()),
				hasItem(100));
	}

	@Test
	public void testStreamTuple() {

		Tuple2<Queue<Integer>, Stream<Integer>> pushable = new PushableStreamBuilder()
				.pushableStream();
		pushable.v1.add(10);
		pushable.v1.close();
		assertThat(pushable.v2.collect(Collectors.toList()), hasItem(10));
	}

	@Test
	public void testStream() {
		PushableStream<Integer> pushable = new PushableStreamBuilder()
				.pushableStream();
		pushable.getInput().add(10);
		pushable.getInput().close();
		assertThat(pushable.getStream().collect(Collectors.toList()),
				hasItem(10));
	}

	@Test
	public void testStreamBackPressure1() throws InterruptedException {

		PushableStream<Integer> pushable = new PushableStreamBuilder()
				.withBackPressureAfter(1).withBackPressureOn(true)
				.pushableStream();
		List events = Collections.synchronizedList(new ArrayList<>());
		new Thread(() -> pushable.getStream().forEach(events::add)).start();
		pushable.getInput().offer(10);
		events.add("here!");
		pushable.getInput().offer(20);
		events.add("there!");
		pushable.getInput().offer(30);
		events.add("there2!");
		pushable.getInput().close();

		System.out.println(events);
		/**
		 * non-deterministics assertThat(events.get(0),is("here!"));
		 * assertThat(events.get(1),is(10));
		 * assertThat(events.get(2),is("there!"));
		 * assertThat(events.get(3),is(20));
		 * assertThat(events.get(4),is("there2!"));
		 * assertThat(events.get(5),is(30));
		 **/

	}

	@Test
	public void testSeqTuple() {
		Tuple2<Queue<Integer>, Seq<Integer>> pushable = new PushableStreamBuilder()
				.pushableSeq();
		pushable.v1.add(10);
		pushable.v1.close();
		assertThat(pushable.v2.collect(Collectors.toList()), hasItem(10));
	}

	@Test
	public void testSeq() {
		PushableSeq<Integer> pushable = new PushableStreamBuilder()
				.pushableSeq();
		pushable.getInput().add(10);
		pushable.getInput().close();
		assertThat(pushable.getStream().collect(Collectors.toList()),
				hasItem(10));
	}

	@Test
	public void testLazyFutureStreamAdapter() {
		Signal<Integer> signal = Signal.queueBackedSignal();
		LazyFutureStream<Integer> pushable = new PushableStreamBuilder()
				.pushableLazyFutureStream(signal.getDiscrete());
		signal.set(100);
		signal.close();
		assertThat(pushable.collect(Collectors.toList()), hasItem(100));
	}

	@Test
	public void testSeqAdapter() {
		Signal<Integer> signal = Signal.queueBackedSignal();
		Seq<Integer> pushable = new PushableStreamBuilder().pushableSeq(signal
				.getDiscrete());
		signal.set(100);
		signal.close();
		assertThat(pushable.collect(Collectors.toList()), hasItem(100));
	}

	@Test
	public void testStreamAdapter() {
		Signal<Integer> signal = Signal.queueBackedSignal();
		Stream<Integer> pushable = new PushableStreamBuilder()
				.pushableStream(signal.getDiscrete());
		signal.set(100);
		signal.close();
		assertThat(pushable.collect(Collectors.toList()), hasItem(100));
	}
	
	@Test
	public void testLazyFutureStreamTopic() {
		MultiplePushableStreamsBuilder<Integer> multi = new PushableStreamBuilder()
		.multiple();
		LazyFutureStream<Integer> pushable = multi
				.pushableLazyFutureStream();
		multi.getInput().offer(100);
		multi.getInput().close();
		assertThat(pushable.collect(Collectors.toList()),
				hasItem(100));
	}
	@Test
	public void testReactPoolTopic() {
		MultiplePushableStreamsBuilder<Integer> multi = new PushableStreamBuilder()
		.multiple();
		LazyFutureStream<Integer> pushable = multi
										.pushable(SequentialElasticPools.lazyReact);
		multi.getInput().offer(100);
		multi.getInput().close();
		assertThat(pushable.collect(Collectors.toList()),
				hasItem(100));
	}
	@Test
	public void testStreamTopic() {
		MultiplePushableStreamsBuilder<Integer> multi = new PushableStreamBuilder()
		.multiple();
		Stream<Integer> pushable = multi.pushableStream();
		multi.getInput().offer(10);
		multi.getInput().close();
		assertThat(pushable.collect(Collectors.toList()),
				hasItem(10));
	}
	@Test
	public void testSeqTopic() {
		PushableSeq<Integer> pushable = new PushableStreamBuilder()
				.pushableSeq();
		pushable.getInput().offer(10);
		pushable.getInput().close();
		assertThat(pushable.getStream().collect(Collectors.toList()),
				hasItem(10));
	}
	
	@Test
	public void testMultiple() {
		MultiplePushableStreamsBuilder<Integer> multi = new PushableStreamBuilder()
												.multiple();
		LazyFutureStream<Integer> pushable = multi
				.pushableLazyFutureStream();
		Seq<Integer> seq = multi.pushableSeq();
		Stream<Integer> stream = multi.pushableStream();
		multi.getInput().offer(100);
		multi.getInput().close();
		
		Set<Integer> vals = new TreeSet<>();
		pushable.forEach(vals::add);
		seq.forEach(vals::add);
		stream.forEach(vals::add);
		
		assertThat(Sets.newSet(100),is(vals));
	}
	
	@Test
	public void testWithBackPressureAfterButOff() {
		PushableLazyFutureStream<Integer> pushable = new PushableStreamBuilder().withBackPressureAfter(10)
				.withBackPressureOn(false)
				.pushableLazyFutureStream();
		
		pushable.getInput().add(100);
		pushable.getInput().close();
		assertThat(pushable.getStream().collect(Collectors.toList()),
				hasItem(100));
	}
	@Test(expected=IllegalArgumentException.class)
	public void testWithBackPressureNegativeAfterButOn() {
		PushableLazyFutureStream<Integer> pushable = new PushableStreamBuilder().withBackPressureAfter(-10)
				.withBackPressureOn(true)
				.pushableLazyFutureStream();
		
		pushable.getInput().add(100);
		pushable.getInput().close();
		assertThat(pushable.getStream().collect(Collectors.toList()),
				hasItem(100));
	}
}
