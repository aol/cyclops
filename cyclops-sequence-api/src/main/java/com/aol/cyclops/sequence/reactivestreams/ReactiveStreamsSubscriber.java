package com.aol.cyclops.sequence.reactivestreams;

import org.reactivestreams.Subscriber;

public interface ReactiveStreamsSubscriber<T> {

	public Subscriber<T> subscribe();
}
