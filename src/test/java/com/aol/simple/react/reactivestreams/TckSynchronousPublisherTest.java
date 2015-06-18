package com.aol.simple.react.reactivestreams;


import org.reactivestreams.Publisher;
import org.reactivestreams.tck.PublisherVerification;
import org.reactivestreams.tck.TestEnvironment;
import org.testng.annotations.Test;

import com.aol.simple.react.stream.lazy.LazyReact;
import com.aol.simple.react.stream.traits.LazyFutureStream;
@Test
public class TckSynchronousPublisherTest extends PublisherVerification<Long>{

	public TckSynchronousPublisherTest(){
		  super(new TestEnvironment(300L));
	}
	

	@Override
	public Publisher<Long> createPublisher(long elements) {
		return LazyFutureStream.iterate(0l, i->i+1l).limit(elements);
		
	}

	@Override
	public Publisher<Long> createFailedPublisher() {
		return null;
		
	}
	

}
