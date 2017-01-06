package com.aol.cyclops2.react.lazy.sequence.reactivestreams;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Arrays;

import org.junit.Test;

import cyclops.stream.ReactiveSeq;
import com.aol.cyclops2.types.stream.reactive.SeqSubscriber;

public class ReactiveStreamsTest {

	@Test
	public void publishAndSubscribe(){
		
		SeqSubscriber<Integer> sub = ReactiveSeq.subscriber();
	
		ReactiveSeq.of(1,2,3).subscribe(sub);
		
		assertThat(sub.stream().toList(),equalTo(
				Arrays.asList(1,2,3)));
		
	}
	@Test
	public void publishAndSubscribeEmpty(){
		SeqSubscriber<Integer> sub = ReactiveSeq.subscriber();
		ReactiveSeq.<Integer>of().subscribe(sub);
		assertThat(sub.stream().toList(),equalTo(
				Arrays.asList()));
	}
}
