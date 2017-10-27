package com.oath.cyclops.internal.stream.spliterators.push.mergelatest;


import cyclops.reactive.Spouts;
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
		return Spouts.mergeLatest(Spouts.iterate(0l, i->i+1l).limit(elements));

	}

	@Override
	public Publisher<Long> createFailedPublisher() {
		return null; //not possible to forEachAsync to failed Stream

	}
    @Override @Test
    public void required_spec102_maySignalLessThanRequestedAndTerminateSubscription() throws Throwable {
	    super.required_spec102_maySignalLessThanRequestedAndTerminateSubscription();
    }
	@Test
	public void required_spec101_subscriptionRequestMustResultInTheCorrectNumberOfProducedElements() throws Throwable {
		super.required_spec101_subscriptionRequestMustResultInTheCorrectNumberOfProducedElements();
	}

	@Test
	public void required_spec302_mustAllowSynchronousRequestCallsFromOnNextAndOnSubscribe() throws Throwable {
		super.required_spec302_mustAllowSynchronousRequestCallsFromOnNextAndOnSubscribe();
	}
}
