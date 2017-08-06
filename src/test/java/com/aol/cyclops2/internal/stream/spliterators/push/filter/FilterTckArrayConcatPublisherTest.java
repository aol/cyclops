package com.aol.cyclops2.internal.stream.spliterators.push.filter;


import cyclops.stream.Spouts;
import org.reactivestreams.Publisher;
import org.reactivestreams.tck.PublisherVerification;
import org.reactivestreams.tck.TestEnvironment;
import org.testng.annotations.Test;

@Test
public class FilterTckArrayConcatPublisherTest extends PublisherVerification<Long>{

	public FilterTckArrayConcatPublisherTest(){
		  super(new TestEnvironment(300L));
	}
	

	@Override
	public Publisher<Long> createPublisher(long elements) {
		return Spouts.concat(Spouts.of(),Spouts.iterate(0l, i->i+1l)).filter(i->true).limit(elements);
		
	}

	@Override
	public Publisher<Long> createFailedPublisher() {
		return null; //not possible to forEachAsync to failed Stream
		
	}
	

}
