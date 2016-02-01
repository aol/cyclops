package com.aol.cyclops.sequence.reactivestreams;


public interface ReactiveStreamsSubscriber<T> {

	public CyclopsSubscriber<T> subscribe();
}
