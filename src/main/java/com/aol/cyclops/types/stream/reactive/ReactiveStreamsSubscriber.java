package com.aol.cyclops.types.stream.reactive;

public interface ReactiveStreamsSubscriber<T> {

	public CyclopsSubscriber<T> subscribe();
}
