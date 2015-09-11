package com.aol.cyclops.streams.reactivestreams;

import org.junit.Test;

import com.aol.cyclops.sequence.SequenceM;
import com.aol.cyclops.sequence.reactivestreams.CyclopsSubscriber;

public class ReactiveStreamsTest {

	@Test
	public void publish(){
		CyclopsSubscriber<Integer> sub = SequenceM.subscriber();
		SequenceM.of(1,2,3).subscribe(sub);
		sub.sequenceM().forEach(System.out::println);
	}
}
