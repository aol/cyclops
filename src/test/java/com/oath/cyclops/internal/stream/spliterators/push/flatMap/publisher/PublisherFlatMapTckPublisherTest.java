package com.oath.cyclops.internal.stream.spliterators.push.flatMap.publisher;


import cyclops.reactive.Spouts;
import org.reactivestreams.Publisher;
import org.reactivestreams.tck.PublisherVerification;
import org.reactivestreams.tck.TestEnvironment;
import org.testng.annotations.Test;

@Test(enabled=false)
public class PublisherFlatMapTckPublisherTest extends PublisherVerification<Long>{

	public PublisherFlatMapTckPublisherTest(){
		  super(new TestEnvironment(300L));
	}


	@Override
	public Publisher<Long> createPublisher(long elements) {
		return Spouts.iterate(0l, i->i+1l).limit(elements).flatMapP(i->Spouts.of(i));

	}

	@Override
	public Publisher<Long> createFailedPublisher() {
		return null; //not possible to forEachAsync to failed Stream

	}

	@Test
	public void required_spec302_mustAllowSynchronousRequestCallsFromOnNextAndOnSubscribe() throws Throwable {
		super.required_spec302_mustAllowSynchronousRequestCallsFromOnNextAndOnSubscribe();
	}
	@Override @Test
	public void optional_spec111_maySupportMultiSubscribe() throws Throwable {
		super.optional_spec111_maySupportMultiSubscribe();
	}
}
