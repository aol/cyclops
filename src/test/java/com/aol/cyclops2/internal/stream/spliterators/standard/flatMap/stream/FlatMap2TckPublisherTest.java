package com.aol.cyclops2.internal.stream.spliterators.standard.flatMap.stream;


import cyclops.stream.ReactiveSeq;
import cyclops.stream.Spouts;
import org.reactivestreams.Publisher;
import org.reactivestreams.tck.PublisherVerification;
import org.reactivestreams.tck.TestEnvironment;
import org.testng.annotations.Test;

@Test
public class FlatMap2TckPublisherTest extends PublisherVerification<Long>{

	public FlatMap2TckPublisherTest(){
		  super(new TestEnvironment(300L));
	}
	

	@Override
	public Publisher<Long> createPublisher(long elements) {
		return ReactiveSeq.iterate(0l, i->i+1l).flatMap(i->Spouts.of(0l,i)).limit(elements);
		
	}

	@Override
	public Publisher<Long> createFailedPublisher() {
		return null; //not possible to subscribeAll to failed Stream
		
	}
	

}
