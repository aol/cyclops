package com.aol.simple.react.reactivestreams;


import org.reactivestreams.Publisher;
import org.reactivestreams.tck.PublisherVerification;
import org.reactivestreams.tck.TestEnvironment;
import org.testng.annotations.Test;

import com.aol.simple.react.stream.traits.LazyFutureStream;
@Test
public class TckAsynchronousPublisherTest extends PublisherVerification<Long>{

	public TckAsynchronousPublisherTest(){
		  super(new TestEnvironment(300L));
	}
	

	@Override
	public Publisher<Long> createPublisher(long elements) {
		return LazyFutureStream.iterate(0l, i->i+1l).limit(elements);
		
	}

	@Override
	public Publisher<Long> createFailedPublisher() {
		// TODO Auto-generated method stub
		return null;
	}
	

}
