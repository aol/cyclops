package com.aol.simple.react.reactivestreams.jdk;


import java.util.stream.Stream;

import org.reactivestreams.Publisher;
import org.reactivestreams.tck.PublisherVerification;
import org.reactivestreams.tck.TestEnvironment;
import org.testng.annotations.Test;

import com.aol.simple.react.reactivestreams.JDKReactiveStreamsPublisher;
import com.aol.simple.react.stream.lazy.LazyReact;
import com.aol.simple.react.stream.traits.LazyFutureStream;
@Test
public class TckAsynchronousPublisherTest extends PublisherVerification<Long>{

	public TckAsynchronousPublisherTest(){
		  super(new TestEnvironment(300L));
	}
	

	@Override
	public Publisher<Long> createPublisher(long elements) {
		return JDKReactiveStreamsPublisher.ofAsync(Stream.iterate(0l, i->i+1l).limit(elements));
		
	}

	@Override
	public Publisher<Long> createFailedPublisher() {
		return null; //not possible to subscribe to failed Stream
		
	}
	

}
