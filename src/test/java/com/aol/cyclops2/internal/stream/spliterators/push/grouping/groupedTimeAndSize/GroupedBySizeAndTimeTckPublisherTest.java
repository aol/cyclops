package com.aol.cyclops2.internal.stream.spliterators.push.grouping.groupedTimeAndSize;


import cyclops.stream.Spouts;
import org.reactivestreams.Publisher;
import org.reactivestreams.tck.PublisherVerification;
import org.reactivestreams.tck.TestEnvironment;
import org.testng.annotations.Test;

import java.util.concurrent.TimeUnit;

@Test
public class GroupedBySizeAndTimeTckPublisherTest extends PublisherVerification<Long>{

	public GroupedBySizeAndTimeTckPublisherTest(){
		  super(new TestEnvironment(300L));
	}
	

	@Override
	public Publisher<Long> createPublisher(long elements) {
		return Spouts.iterate(0l, i->i+1l).groupedBySizeAndTime(1,1, TimeUnit.SECONDS).map(l->l.get(0)).limit(elements);
		
	}

	@Override
	public Publisher<Long> createFailedPublisher() {
		return null; //not possible to forEachAsync to failed Stream
		
	}
	

}
