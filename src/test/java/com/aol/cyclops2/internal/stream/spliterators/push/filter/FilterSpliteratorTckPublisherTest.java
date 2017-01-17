package com.aol.cyclops2.internal.stream.spliterators.push.filter;


import cyclops.collections.ListX;
import cyclops.stream.ReactiveSeq;
import cyclops.stream.Spouts;
import org.reactivestreams.Publisher;
import org.reactivestreams.tck.PublisherVerification;
import org.reactivestreams.tck.TestEnvironment;
import org.testng.annotations.Test;

@Test
public class FilterSpliteratorTckPublisherTest extends PublisherVerification<Long>{

	public FilterSpliteratorTckPublisherTest(){
		  super(new TestEnvironment(300L));
	}
	

	@Override
	public Publisher<Long> createPublisher(long elements) {
		return Spouts.fromSpliterator(ReactiveSeq.iterate(0l,i->i+1l).limit(Math.max(100_000,elements)).spliterator())
				.filter(i->true);
		
	}

	@Override
	public Publisher<Long> createFailedPublisher() {
		return null; //not possible to subscribeAll to failed Stream
		
	}
	

}
