package com.aol.cyclops.react.reactivestreams.jdk;

import org.reactivestreams.Subscriber;
import org.reactivestreams.tck.SubscriberBlackboxVerification;
import org.reactivestreams.tck.TestEnvironment;
import org.testng.annotations.Test;

import com.aol.cyclops.util.stream.reactivestreams.JDKReactiveStreamsSubscriber;

@Test
public class TckBlackBoxSubscriberTest extends SubscriberBlackboxVerification<Long>{
	public TckBlackBoxSubscriberTest() {
        super(new TestEnvironment(300L));
    }

	@Override
	public Subscriber<Long> createSubscriber() {
		return new JDKReactiveStreamsSubscriber();
		
	}

	@Override
	public Long createElement(int element) {
		return new Long(element);
	}


}
