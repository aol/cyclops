package com.aol.cyclops2.internal.stream.spliterators.push.filter;


import cyclops.reactive.Spouts;
import org.reactivestreams.Publisher;
import org.reactivestreams.tck.PublisherVerification;
import org.reactivestreams.tck.TestEnvironment;
import org.testng.annotations.Test;

@Test
public class FilterGenerateTckPublisherTest extends PublisherVerification<Long>{

	public FilterGenerateTckPublisherTest(){
		  super(new TestEnvironment(300L));
	}
	

	@Override
	public Publisher<Long> createPublisher(long elements) {
		return Spouts.generate(()->2l).filter(i->i%2==0).limit(elements);
		
	}

	@Override
	public Publisher<Long> createFailedPublisher() {
		return null; //not possible to forEachAsync to failed Stream
		
	}
	

}
