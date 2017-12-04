package com.oath.cyclops.internal.stream.spliterators.push.flatMap.stream;


import cyclops.reactive.Spouts;
import org.reactivestreams.Publisher;
import org.reactivestreams.tck.PublisherVerification;
import org.reactivestreams.tck.TestEnvironment;
import org.testng.annotations.Test;

@Test
public class FlatMap2TckPublisherTest extends PublisherVerification<Long>{

	public FlatMap2TckPublisherTest(){
		  super(new TestEnvironment(300L));
	}


	@Override
	public Publisher<Long> createPublisher(long elements) {
		return Spouts.iterate(0l, i->i+1l).flatMap(i->Spouts.of(0l,i)).limit(elements);

	}

	@Override
	public Publisher<Long> createFailedPublisher() {
		return null; //not possible to forEachAsync to failed Stream

	}


}
