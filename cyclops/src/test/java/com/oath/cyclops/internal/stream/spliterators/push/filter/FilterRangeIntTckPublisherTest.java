package com.oath.cyclops.internal.stream.spliterators.push.filter;


import cyclops.reactive.Spouts;
import org.reactivestreams.Publisher;
import org.reactivestreams.tck.PublisherVerification;
import org.reactivestreams.tck.TestEnvironment;
import org.reactivestreams.tck.flow.support.PublisherVerificationRules;
import org.testng.annotations.Test;

@Test
public class FilterRangeIntTckPublisherTest extends PublisherVerification<Long> implements PublisherVerificationRules {

	public FilterRangeIntTckPublisherTest(){
		  super(new TestEnvironment(300L));
	}


	@Override
	public Publisher<Long> createPublisher(long elements) {
		return Spouts.range(0,(int)elements).map(Long::new)
                     .filter(i->true);
	}

	@Override
	public Publisher<Long> createFailedPublisher() {
		return null; //not possible to forEachAsync to failed Stream

	}



}
