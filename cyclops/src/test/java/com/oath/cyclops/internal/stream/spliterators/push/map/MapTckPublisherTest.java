package com.oath.cyclops.internal.stream.spliterators.push.map;



import cyclops.data.LazySeq;
import cyclops.reactive.Spouts;
import org.reactivestreams.Publisher;
import org.reactivestreams.tck.PublisherVerification;
import org.reactivestreams.tck.TestEnvironment;
import org.testng.annotations.Test;

@Test
public class MapTckPublisherTest extends PublisherVerification<Long>{

	public MapTckPublisherTest(){
		  super(new TestEnvironment(300L));
	}


	@Override
	public Publisher<Long> createPublisher(long elements) {

		return Spouts.fromIterable(LazySeq.fill(Math.min(elements,10_000),10l)).map(i->i*2);

	}

	@Override
	public Publisher<Long> createFailedPublisher() {
		return null; //not possible to forEachAsync to failed Stream

	}


}
