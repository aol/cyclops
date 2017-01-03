package com.aol.cyclops2.react.reactivestreams.jdk;

import org.reactivestreams.Subscriber;
import org.reactivestreams.tck.SubscriberBlackboxVerification;
import org.reactivestreams.tck.TestEnvironment;
import org.testng.annotations.Test;

import com.aol.cyclops2.types.stream.reactive.ValueSubscriber;

@Test
public class ValueSubscriberTckBlackBoxSubscriberTest2 extends SubscriberBlackboxVerification<Long>{
	public ValueSubscriberTckBlackBoxSubscriberTest2() {
        super(new TestEnvironment(300L));
    }

	@Override
	public Subscriber<Long> createSubscriber() {
		return ValueSubscriber.subscriber();
		
	}

	@Override
	public Long createElement(int element) {
		return new Long(element);
	}


}
