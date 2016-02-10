package com.aol.cyclops.javaslang.reactivestreams.javaslang;



import javaslang.collection.LazyStream;

import org.reactivestreams.Publisher;
import org.reactivestreams.tck.PublisherVerification;
import org.reactivestreams.tck.TestEnvironment;
import org.testng.annotations.Test;

import com.aol.cyclops.javaslang.reactivestreams.JavaslangReactiveStreamsPublisher;
@Test
public class TckSynchronousPublisherTest extends PublisherVerification<Long>{

	public TckSynchronousPublisherTest(){
		  super(new TestEnvironment(300L));
	}
	

	@Override
	public Publisher<Long> createPublisher(long elements) {
		return JavaslangReactiveStreamsPublisher.ofSync(LazyStream.gen(0l, i->i+1l).take((int)elements));
		
	}

	@Override
	public Publisher<Long> createFailedPublisher() {
		return null; //not possible to subscribe to failed Stream
		
	}
	

}
