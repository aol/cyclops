package com.aol.cyclops.streams.reactivestreams;

import org.reactivestreams.Subscription;

import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.types.stream.reactive.CyclopsSubscriber;
import com.aol.cyclops.types.stream.reactive.ReactiveStreamsSubscriber;
import com.aol.cyclops.react.reactivestreams.JDKReactiveStreamsSubscriber;

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
			public ReactiveSeq<T> sequenceM() {
				return ReactiveSeq.fromStream(sub.getStream());
			}
		};
		
	}

}
