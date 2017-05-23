package com.aol.cyclops2.internal.stream.spliterators.push.flatMap.iterable;


import cyclops.stream.Spouts;
import org.reactivestreams.Publisher;
import org.reactivestreams.tck.PublisherVerification;
import org.reactivestreams.tck.TestEnvironment;
import org.testng.annotations.Test;

@Test
public class IterableFlatMap2TckPublisherTest extends PublisherVerification<Long>{

	public IterableFlatMap2TckPublisherTest(){
		  super(new TestEnvironment(300L));
	}
	

	@Override
	public Publisher<Long> createPublisher(long elements) {
		return Spouts.iterate(0l, i->i+1l).flatMapI(i->Spouts.of(0l,i)).limit(elements);
		
	}

	@Override
	public Publisher<Long> createFailedPublisher() {
		return null; //not possible toNested forEachAsync toNested failed Stream
		
	}
	

}
