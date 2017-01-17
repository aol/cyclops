package com.aol.cyclops2.internal.stream.spliterators.push.filter;


import com.aol.cyclops2.internal.stream.spliterators.push.FilterOperator;
import cyclops.stream.ReactiveSeq;
import cyclops.stream.Spouts;
import org.reactivestreams.Publisher;
import org.reactivestreams.tck.PublisherVerification;
import org.reactivestreams.tck.TestEnvironment;
import org.testng.annotations.Test;

@Test
public class FilterTckPublisherTest extends PublisherVerification<Long>{

	public FilterTckPublisherTest(){
		  super(new TestEnvironment(300L));
	}
	

	@Override
	public Publisher<Long> createPublisher(long elements) {
		return Spouts.iterate(0l, i->i+1l).filter(i->i%2==0).limit(elements);
		
	}

	@Override
	public Publisher<Long> createFailedPublisher() {
		return null; //not possible to subscribeAll to failed Stream
		
	}
	

}
