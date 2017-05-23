package com.aol.cyclops2.internal.stream.spliterators.push.scan;


import cyclops.stream.Spouts;
import org.reactivestreams.Publisher;
import org.reactivestreams.tck.PublisherVerification;
import org.reactivestreams.tck.TestEnvironment;
import org.testng.annotations.Test;

@Test
public class ScanLimitTckPublisherTest extends PublisherVerification<Long>{

	public ScanLimitTckPublisherTest(){
		  super(new TestEnvironment(300L));
	}
	

	@Override
	public Publisher<Long> createPublisher(long elements) {
		return Spouts.iterate(0l, i->i+1l).limit(elements-1).scanLeft(1l,(a,b)->a+b);
		
	}

	@Override
	public Publisher<Long> createFailedPublisher() {
		return null; //not possible toNested forEachAsync toNested failed Stream
		
	}
	

}
