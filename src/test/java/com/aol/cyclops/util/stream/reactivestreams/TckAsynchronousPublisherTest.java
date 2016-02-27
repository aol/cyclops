package com.aol.cyclops.util.stream.reactivestreams;


import java.util.concurrent.Executors;

import org.reactivestreams.Publisher;
import org.reactivestreams.tck.PublisherVerification;
import org.reactivestreams.tck.TestEnvironment;
import org.testng.annotations.Test;

import com.aol.cyclops.control.LazyReact;
import com.aol.cyclops.types.futurestream.LazyFutureStream;
@Test
public class TckAsynchronousPublisherTest extends PublisherVerification<Long>{

	public TckAsynchronousPublisherTest(){
		  super(new TestEnvironment(300L));
	}
	

	@Override
	public Publisher<Long> createPublisher(long elements) {
		return new LazyReact().withPublisherExecutor(Executors.newFixedThreadPool(1))
		                     .reactInfinitely(()->100l).limit(elements);
		
	}

	@Override
	public Publisher<Long> createFailedPublisher() {
		return null; //not possible to subscribe to failed Stream
		
	}
	

}
