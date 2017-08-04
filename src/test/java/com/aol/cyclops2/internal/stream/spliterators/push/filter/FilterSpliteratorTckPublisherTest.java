package com.aol.cyclops2.internal.stream.spliterators.push.filter;


import cyclops.stream.ReactiveSeq;
import cyclops.stream.Spouts;
import org.reactivestreams.Publisher;
import org.reactivestreams.tck.PublisherVerification;
import org.reactivestreams.tck.TestEnvironment;
import org.reactivestreams.tck.support.PublisherVerificationRules;
import org.testng.annotations.Test;

@Test
public class FilterSpliteratorTckPublisherTest extends PublisherVerification<Long> implements PublisherVerificationRules{

	public FilterSpliteratorTckPublisherTest(){
		  super(new TestEnvironment(300L));
	}
	

	@Override
	public Publisher<Long> createPublisher(long elements) {
		return Spouts.fromSpliterator(ReactiveSeq.iterate(0l,i->i+1l).spliterator())
                .limit(elements)
				.filter(i->true);
		
	}

	@Override
	public Publisher<Long> createFailedPublisher() {
		return null; //not possible to forEachAsync to failed Stream
		
	}



}
