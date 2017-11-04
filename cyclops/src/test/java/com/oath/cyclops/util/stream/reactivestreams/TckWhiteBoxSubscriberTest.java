package com.oath.cyclops.util.stream.reactivestreams;

import com.oath.cyclops.types.reactive.QueueBasedSubscriber;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.reactivestreams.tck.SubscriberWhiteboxVerification;
import org.reactivestreams.tck.TestEnvironment;
import org.testng.annotations.Test;

@Test
public class TckWhiteBoxSubscriberTest extends SubscriberWhiteboxVerification<Long>{
	public TckWhiteBoxSubscriberTest() {
        super(new TestEnvironment(300L));
    }



	@Override
	public Long createElement(int element) {
		return new Long(element);
	}

	@Override
	public Subscriber<Long> createSubscriber(
			org.reactivestreams.tck.SubscriberWhiteboxVerification.WhiteboxSubscriberProbe<Long> probe) {

		 return new QueueBasedSubscriber<Long>(new QueueBasedSubscriber.Counter(),500) {
	            @Override
	            public void onSubscribe(final Subscription rsSubscription) {
	               probe.registerOnSubscribe(new SubscriberPuppet() {
                       @Override
                       public void triggerRequest(long elements) {

                           rsSubscription.request(elements);
                       }

                       @Override
                       public void signalCancel() {
                           rsSubscription.cancel();
                       }
                   });
	               super.onSubscribe(rsSubscription);
	            }

	            @Override
	            public void onNext(Long aLong) {
	                probe.registerOnNext(aLong);
	                super.onNext(aLong);
	            }

	            @Override
	            public void onError(Throwable t) {
	                probe.registerOnError(t);
	                super.onError(t);
	            }

	            @Override
	            public void onComplete() {
	                probe.registerOnComplete();
	                super.onComplete();
	            }
	        };
	    }



}
