package com.aol.cyclops2.util.stream.reactivestreams;

import org.reactivestreams.Subscriber;
import org.reactivestreams.tck.SubscriberBlackboxVerification;
import org.reactivestreams.tck.TestEnvironment;
import org.testng.annotations.Test;

import com.aol.cyclops2.types.reactive.QueueBasedSubscriber;
import com.aol.cyclops2.types.reactive.QueueBasedSubscriber.Counter;

@Test
public class TckBlackBoxSubscriberTest extends SubscriberBlackboxVerification<Long>{
	public TckBlackBoxSubscriberTest() {
        super(new TestEnvironment(300L));
    }

	@Override
	public Subscriber<Long> createSubscriber() {
		return new QueueBasedSubscriber(new Counter(),500);
		
	}

	@Override
	public Long createElement(int element) {
		return new Long(element);
	}


}
