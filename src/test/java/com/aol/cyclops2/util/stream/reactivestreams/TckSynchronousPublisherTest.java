package com.aol.cyclops2.util.stream.reactivestreams;


import com.aol.cyclops2.react.ThreadPools;
import cyclops.async.LazyReact;
import org.reactivestreams.Publisher;
import org.reactivestreams.tck.PublisherVerification;
import org.reactivestreams.tck.TestEnvironment;
import org.testng.annotations.Test;

import cyclops.stream.FutureStream;
@Test
public class TckSynchronousPublisherTest extends PublisherVerification<Long>{

	public TckSynchronousPublisherTest(){
		  super(new TestEnvironment(300L));
	}
	

	@Override
	public Publisher<Long> createPublisher(long elements) {
		return new LazyReact(ThreadPools.getCommonFreeThread()).iterate(0l, i->i+1l).sync().limit(elements);
		
	}

	@Override
	public Publisher<Long> createFailedPublisher() {
		return null; //not possible to subscribeAll to failed Stream
		
	}
	

}
