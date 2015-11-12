package com.aol.cyclops.javaslang.reactivestreams.javaslang;

import org.reactivestreams.Subscriber;
import org.reactivestreams.tck.SubscriberBlackboxVerification;
import org.reactivestreams.tck.TestEnvironment;
import org.testng.annotations.Test;

import com.aol.cyclops.javaslang.reactivestreams.JavaslangReactiveStreamsSubscriber;

@Test
public class TckBlackBoxSubscriberTest extends SubscriberBlackboxVerification<Long>{
	public TckBlackBoxSubscriberTest() {
        super(new TestEnvironment(300L));
    }

	@Override
	public Subscriber<Long> createSubscriber() {
		return new JavaslangReactiveStreamsSubscriber();
		
	}

	@Override
	public Long createElement(int element) {
		return new Long(element);
	}


}
