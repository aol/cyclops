package com.aol.simple.react.lazy.sequenceM.reactivestreams;

import org.reactivestreams.Subscription;

import com.aol.cyclops.sequence.SequenceM;
import com.aol.cyclops.sequence.reactivestreams.CyclopsSubscriber;
import com.aol.cyclops.sequence.reactivestreams.ReactiveStreamsSubscriber;
import com.aol.simple.react.reactivestreams.JDKReactiveStreamsSubscriber;

public class MockSubscriber<T> implements ReactiveStreamsSubscriber<T> {

	@Override
	public CyclopsSubscriber<T> subscribe() {
		JDKReactiveStreamsSubscriber<T> sub= new JDKReactiveStreamsSubscriber<>();
		return new CyclopsSubscriber<T>() {

			@Override
			public void onSubscribe(Subscription s) {
				sub.onSubscribe(s);
				
			}

			@Override
			public void onNext(T t) {
				sub.onNext(t);
				
			}

			@Override
			public void onError(Throwable t) {
				sub.onError(t);
				
			}

			@Override
			public void onComplete() {
				sub.onComplete();
				
			}

			@Override
			public SequenceM<T> sequenceM() {
				return SequenceM.fromStream(sub.getStream());
			}
		};
		
	}

}
