package com.oath.cyclops.util.stream.reactivestreams;

import com.oath.cyclops.types.reactive.QueueBasedSubscriber;
import org.reactivestreams.Subscriber;
import org.reactivestreams.tck.SubscriberBlackboxVerification;
import org.reactivestreams.tck.TestEnvironment;
import org.testng.annotations.Test;

@Test
public class TckBlackBoxSubscriberTest extends SubscriberBlackboxVerification<Long>{
	public TckBlackBoxSubscriberTest() {
        super(new TestEnvironment(300L));
    }

	@Override
	public Subscriber<Long> createSubscriber() {
		return new QueueBasedSubscriber(new QueueBasedSubscriber.Counter(),500);

	}

	@Override
	public Long createElement(int element) {
		return new Long(element);
	}


}
