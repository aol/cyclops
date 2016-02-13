package com.aol.cyclops.javaslang.reactivestreams.javaslang;


import java.util.concurrent.Executors;

import javaslang.collection.LazyStream;

import org.reactivestreams.Publisher;
import org.reactivestreams.tck.PublisherVerification;
import org.reactivestreams.tck.TestEnvironment;
import org.testng.annotations.Test;

import com.aol.cyclops.javaslang.reactivestreams.JavaslangReactiveStreamsPublisher;
@Test
public class TckAsynchronousPublisherTest extends PublisherVerification<Long>{

	public TckAsynchronousPublisherTest(){
		  super(new TestEnvironment(300L));
	}
	

	@Override
	public Publisher<Long> createPublisher(long elements) {
		return JavaslangReactiveStreamsPublisher.ofAsync(LazyStream.gen(0l, i->i+1l).take((int)elements), 
				Executors.newFixedThreadPool(1));
		
	}

	@Override
	public Publisher<Long> createFailedPublisher() {
		return null; //not possible to subscribe to failed Stream
		
	}
	

}
