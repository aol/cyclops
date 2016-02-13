package com.aol.cyclops.react.reactivestreams;


import org.reactivestreams.Publisher;
import org.reactivestreams.tck.PublisherVerification;
import org.reactivestreams.tck.TestEnvironment;
import org.testng.annotations.Test;

import com.aol.cyclops.control.LazyReact;
import com.aol.cyclops.react.stream.traits.LazyFutureStream;
@Test
public class TckSynchronousPublisherTest extends PublisherVerification<Long>{

	public TckSynchronousPublisherTest(){
		  super(new TestEnvironment(300L));
	}
	

	@Override
	public Publisher<Long> createPublisher(long elements) {
		return LazyFutureStream.iterate(0l, i->i+1l).sync().limit(elements);
		
	}

	@Override
	public Publisher<Long> createFailedPublisher() {
		return null; //not possible to subscribe to failed Stream
		
	}
	

}
