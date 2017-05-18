package com.aol.cyclops2.react.stream.pushable;

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

import cyclops.stream.FutureStream;
import org.jooq.lambda.tuple.Tuple2;
import org.junit.Test;
import org.mockito.internal.util.collections.Sets;

import cyclops.async.LazyReact;
import cyclops.stream.ReactiveSeq;
import cyclops.stream.StreamSource;
import cyclops.async.adapters.Queue;
import cyclops.async.QueueFactories;
import cyclops.async.adapters.Signal;
import cyclops.collections.immutable.PersistentSetX;
import cyclops.collections.immutable.LinkedListX;
import cyclops.collections.mutable.SetX;
import com.aol.cyclops2.react.threads.SequentialElasticPools;
import com.aol.cyclops2.util.stream.pushable.MultipleStreamSource;
import com.aol.cyclops2.util.stream.pushable.PushableFutureStream;
import com.aol.cyclops2.util.stream.pushable.PushableReactiveSeq;
import com.aol.cyclops2.util.stream.pushable.PushableStream;

import reactor.core.publisher.Flux;


public class PushableStreamTest {
    
    @Test
    public void pipes() throws InterruptedException{
        
        Flux.from(LinkedListX.of(10,20,30));
        
        SetX.fromPublisher(Flux.just(10,20,30));
        
        PersistentSetX.of(1,2,3)
             .flatMapP(i->Flux.just(i,i*10))
             .toPVectorX();
        
        /**
        Pipes<String, Integer> bus = Pipes.of();
        bus.register("reactor", QueueFactories.<Integer>boundedNonBlockingQueue(1000)
                                              .build());
        //bus.publishTo("reactor",Flux.just(10,20,30));
        bus.publishTo("reactor",ReactiveSeq.of(10,20,30));
        
        System.out.println(Thread.currentThread().getId());
       System.out.println(bus.futureStream("reactor", new LazyReact(50,50))
            .get()
           .map(i->"fan-out to handle blocking I/O:" + Thread.currentThread().getId() + ":"+i)
           .toList());//.forEach(System.out::println);
        
        Thread.sleep(1500);
        **/
    }

	@Test
	public void testLazyFutureStream() {
	    
	    
	    
	   
	    
		PushableFutureStream<Integer> pushable = StreamSource.ofUnbounded()
				                                                 .futureStream(new LazyReact());
		pushable.getInput().add(100);
		pushable.getInput().close();
		assertThat(pushable.getStream().collect(Collectors.toList()),
				hasItem(100));
	}

	@Test
	public void testReactPool() {
		PushableFutureStream<Integer> pushable = StreamSource.ofUnbounded()
		                                                        .futureStream(SequentialElasticPools.lazyReact.nextReactor());
		pushable.getInput().add(100);
		pushable.getInput().close();
		assertThat(pushable.getStream().collect(Collectors.toList()),
				hasItem(100));
	}

	@Test
	public void testStreamTuple() {

		Tuple2<Queue<Integer>, Stream<Integer>> pushable = StreamSource.ofUnbounded()
				                                                        .stream();
		pushable.v1.add(10);
		pushable.v1.close();
		assertThat(pushable.v2.collect(Collectors.toList()), hasItem(10));
	}

	@Test
	public void testStream() {
		PushableStream<Integer> pushable = StreamSource.ofUnbounded()
				                                        .stream();
		pushable.getInput().add(10);
		pushable.getInput().close();
		assertThat(pushable.getStream().collect(Collectors.toList()),
				hasItem(10));
	}

	@Test
	public void testStreamBackPressure1() throws InterruptedException {

		PushableStream<Integer> pushable = StreamSource.of(1)
            			                               .stream();
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
		Tuple2<Queue<Integer>, ReactiveSeq<Integer>> pushable = StreamSource.ofUnbounded()
				                                                            .reactiveSeq();
		pushable.v1.add(10);
		pushable.v1.close();
		assertThat(pushable.v2.collect(Collectors.toList()), hasItem(10));
	}

	@Test
	public void testSeq() {
	    
		PushableReactiveSeq<Integer> pushable = StreamSource.ofUnbounded()
				                                            .reactiveSeq();
		pushable.getInput().add(10);
		pushable.getInput().close();
		
		
		
		assertThat(pushable.getStream().collect(Collectors.toList()),
				hasItem(10));
	}

	@Test
	public void testLazyFutureStreamAdapter() {
		Signal<Integer> signal = Signal.queueBackedSignal();
		FutureStream<Integer> pushable = StreamSource
				                                .futureStream(signal.getDiscrete(),new LazyReact());
		signal.set(100);
		signal.close();
		assertThat(pushable.collect(Collectors.toList()), hasItem(100));
	}

	@Test
	public void testSeqAdapter() {
		Signal<Integer> signal = Signal.queueBackedSignal();
		ReactiveSeq<Integer> pushable = StreamSource.reactiveSeq(signal
				.getDiscrete());
		signal.set(100);
		signal.close();
		assertThat(pushable.collect(Collectors.toList()), hasItem(100));
	}

	@Test
	public void testStreamAdapter() {
		Signal<Integer> signal = Signal.queueBackedSignal();
		Stream<Integer> pushable = StreamSource
				                        .stream(signal.getDiscrete());
		signal.set(100);
		signal.close();
		assertThat(pushable.collect(Collectors.toList()), hasItem(100));
	}
	
	@Test
	public void testLazyFutureStreamTopic() {
		MultipleStreamSource<Integer> multi = StreamSource
		                                        .ofMultiple();
		FutureStream<Integer> pushable = multi
				.futureStream(new LazyReact());
		multi.getInput().offer(100);
		multi.getInput().close();
		assertThat(pushable.collect(Collectors.toList()),
				hasItem(100));
	}
	@Test
    public void testLazyFutureStreamTopicBackPressure() {
        MultipleStreamSource<Integer> multi = StreamSource
                                                .ofMultiple(2);
        FutureStream<Integer> pushable = multi
                .futureStream(new LazyReact());
        multi.getInput().offer(100);
        multi.getInput().close();
        assertThat(pushable.collect(Collectors.toList()),
                hasItem(100));
    }
	@Test
    public void testLazyFutureStreamTopicQueueFactory() {
        MultipleStreamSource<Integer> multi = StreamSource
                                                .ofMultiple(QueueFactories.boundedQueue(100));
        FutureStream<Integer> pushable = multi
                .futureStream(new LazyReact());
        multi.getInput().offer(100);
        multi.getInput().close();
        assertThat(pushable.collect(Collectors.toList()),
                hasItem(100));
    }
	@Test
	public void testReactPoolTopic() {
		MultipleStreamSource<Integer> multi =  StreamSource
		                                            .ofMultiple();
		FutureStream<Integer> pushable = multi
										.futureStream(SequentialElasticPools.lazyReact.nextReactor());
		multi.getInput().offer(100);
		multi.getInput().close();
		assertThat(pushable.collect(Collectors.toList()),
				hasItem(100));
	}
	@Test
	public void testStreamTopic() {
		MultipleStreamSource<Integer> multi = StreamSource
		                                                .ofMultiple();
		Stream<Integer> pushable = multi.stream();
		multi.getInput().offer(10);
		multi.getInput().close();
		assertThat(pushable.collect(Collectors.toList()),
				hasItem(10));
	}
	@Test
	public void testSeqTopic() {
		PushableReactiveSeq<Integer> pushable = StreamSource.ofUnbounded()
				                                    .reactiveSeq();
		pushable.getInput().offer(10);
		pushable.getInput().close();
		assertThat(pushable.getStream().collect(Collectors.toList()),
				hasItem(10));
	}
	
	@Test
	public void testMultiple() {
		MultipleStreamSource<Integer> multi = StreamSource
												.ofMultiple();
		FutureStream<Integer> pushable = multi
				.futureStream(new LazyReact());
		ReactiveSeq<Integer> seq = multi.reactiveSeq();
		Stream<Integer> stream = multi.stream();
		multi.getInput().offer(100);
		multi.getInput().close();
		
		Set<Integer> vals = new TreeSet<>();
		pushable.forEach(vals::add);
		seq.forEach(vals::add);
		stream.forEach(vals::add);
		
		assertThat(Sets.newSet(100),is(vals));
	}
	
	
	@Test(expected=IllegalArgumentException.class)
	public void testWithBackPressureNegativeAfterButOn() {
		PushableFutureStream<Integer> pushable = StreamSource.of(-10)
		                                                          .futureStream(new LazyReact());
		
		pushable.getInput().add(100);
		pushable.getInput().close();
		assertThat(pushable.getStream().collect(Collectors.toList()),
				hasItem(100));
	}
}
