package com.aol.cyclops2.internal.stream.spliterators.push.grouping.sliding;


import cyclops.stream.Spouts;
import org.reactivestreams.Publisher;
import org.reactivestreams.tck.PublisherVerification;
import org.reactivestreams.tck.TestEnvironment;
import org.testng.annotations.Test;

@Test
public class SlidingTckPublisherTest extends PublisherVerification<Long>{

	public SlidingTckPublisherTest(){
		  super(new TestEnvironment(300L));
	}
	

	@Override
	public Publisher<Long> createPublisher(long elements) {
		return Spouts.concat(Spouts.of(1l,2l,3l,4l,5l),Spouts.iterate(0l, i->i+1l).sliding(2,1).skip(4).map(l->l.get(0))).limit(elements);
		
	}

	@Override
	public Publisher<Long> createFailedPublisher() {
		return null; //not possible to subscribeAll to failed Stream
		
	}
	

}
