package com.aol.cyclops.react.lazy.sequenceM.reactivestreams;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Arrays;

import org.junit.Test;

import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.types.stream.reactive.CyclopsSubscriber;

public class ReactiveStreamsTest {

	@Test
	public void publishAndSubscribe(){
		
		CyclopsSubscriber<Integer> sub = ReactiveSeq.subscriber();
	
		ReactiveSeq.of(1,2,3).subscribe(sub);
		
		assertThat(sub.sequenceM().toList(),equalTo(
				Arrays.asList(1,2,3)));
		
	}
	@Test
	public void publishAndSubscribeEmpty(){
		CyclopsSubscriber<Integer> sub = ReactiveSeq.subscriber();
		ReactiveSeq.<Integer>of().subscribe(sub);
		assertThat(sub.sequenceM().toList(),equalTo(
				Arrays.asList()));
	}
}
