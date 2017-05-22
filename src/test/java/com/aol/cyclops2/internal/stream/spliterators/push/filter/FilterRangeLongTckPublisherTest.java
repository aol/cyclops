package com.aol.cyclops2.internal.stream.spliterators.push.filter;


import cyclops.stream.Spouts;
import org.reactivestreams.Publisher;
import org.reactivestreams.tck.PublisherVerification;
import org.reactivestreams.tck.TestEnvironment;
import org.reactivestreams.tck.support.PublisherVerificationRules;
import org.testng.annotations.Test;

@Test
public class FilterRangeLongTckPublisherTest extends PublisherVerification<Long> implements PublisherVerificationRules{

	public FilterRangeLongTckPublisherTest(){
		  super(new TestEnvironment(300L));
	}
	

	@Override
	public Publisher<Long> createPublisher(long elements) {
		return Spouts.rangeLong(0,elements)
                     .filter(i->true);
	}

	@Override
	public Publisher<Long> createFailedPublisher() {
		return null; //not possible toNested forEachAsync toNested failed Stream
		
	}



}
