package com.aol.cyclops2.internal.stream.spliterators.push.mergelatest;


import cyclops.stream.Spouts;
import org.reactivestreams.Publisher;
import org.reactivestreams.tck.PublisherVerification;
import org.reactivestreams.tck.TestEnvironment;
import org.testng.annotations.Test;

@Test
public class MergeLatestTckPublisherTest extends PublisherVerification<Long>{

	public MergeLatestTckPublisherTest(){
		  super(new TestEnvironment(300L));
	}
	

	@Override
	public Publisher<Long> createPublisher(long elements) {
		return Spouts.mergeLatest(Spouts.iterate(0l, i->i+1l)).limit(elements);
		
	}

	@Override
	public Publisher<Long> createFailedPublisher() {
		return null; //not possible to subscribeAll to failed Stream
		
	}

	@Test
	public void required_spec302_mustAllowSynchronousRequestCallsFromOnNextAndOnSubscribe() throws Throwable {
		super.required_spec302_mustAllowSynchronousRequestCallsFromOnNextAndOnSubscribe();
	}
}
