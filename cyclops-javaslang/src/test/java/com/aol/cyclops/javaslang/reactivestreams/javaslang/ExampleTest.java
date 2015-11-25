package com.aol.cyclops.javaslang.reactivestreams.javaslang;

import javaslang.collection.Stream;

import org.junit.Test;

import com.aol.cyclops.javaslang.reactivestreams.JavaslangReactiveStreamsPublisher;
import com.aol.cyclops.javaslang.reactivestreams.JavaslangReactiveStreamsSubscriber;
import com.aol.cyclops.sequence.SequenceM;
import com.aol.cyclops.sequence.reactivestreams.CyclopsSubscriber;

public class ExampleTest {

	@Test
	public void subscribe(){
		CyclopsSubscriber<Integer> subscriber =SequenceM.subscriber();
		
		Stream<Integer> stream = Stream.ofAll(1,2,3);
		
		JavaslangReactiveStreamsPublisher.ofSync(stream)
										.subscribe(subscriber);
		
		subscriber.sequenceM()
				.forEach(System.out::println);
	}
	@Test
	public void publish(){
		
		SequenceM<Integer> publisher =SequenceM.of(1,2,3);
		
		JavaslangReactiveStreamsSubscriber<Integer> subscriber = new JavaslangReactiveStreamsSubscriber<>();
		publisher.subscribe(subscriber);
		
		Stream<Integer> stream = subscriber.getStream();
		
		
		
		stream.forEach(System.out::println);
	}
}
