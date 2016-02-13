package com.aol.cyclops.react.lazy.sequenceM.reactivestreams;

import java.util.Arrays;

import org.junit.Test;

import com.aol.cyclops.sequence.SequenceM;
import com.aol.cyclops.sequence.reactivestreams.CyclopsSubscriber;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class ReactiveStreamsTest {

	@Test
	public void publishAndSubscribe(){
		
		CyclopsSubscriber<Integer> sub = SequenceM.subscriber();
	
		SequenceM.of(1,2,3).subscribe(sub);
		
		assertThat(sub.sequenceM().toList(),equalTo(
				Arrays.asList(1,2,3)));
		
	}
	@Test
	public void publishAndSubscribeEmpty(){
		CyclopsSubscriber<Integer> sub = SequenceM.subscriber();
		SequenceM.<Integer>of().subscribe(sub);
		assertThat(sub.sequenceM().toList(),equalTo(
				Arrays.asList()));
	}
}
