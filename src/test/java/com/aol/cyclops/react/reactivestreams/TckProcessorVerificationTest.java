package com.aol.cyclops.react.reactivestreams;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.reactivestreams.Processor;
import org.reactivestreams.Publisher;
import org.reactivestreams.tck.IdentityProcessorVerification;
import org.reactivestreams.tck.TestEnvironment;
import org.testng.annotations.Test;

@Test
public class TckProcessorVerificationTest extends IdentityProcessorVerification<Long> {
	ExecutorService exec = Executors.newFixedThreadPool(3);
	public TckProcessorVerificationTest(){
		  //super(new TestEnvironment(300L));
		super(new TestEnvironment(250, true), 3500);
	}
	@Override
	public Processor<Long, Long> createIdentityProcessor(int bufferSize) {
			return new FutureStreamProcessor();
	}

	@Override
	public Publisher<Long> createFailedPublisher() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ExecutorService publisherExecutorService() {
		// TODO Auto-generated method stub
		return exec;
	}

	@Override
	public Long createElement(int element) {
		return new Long(5);
	}

}
