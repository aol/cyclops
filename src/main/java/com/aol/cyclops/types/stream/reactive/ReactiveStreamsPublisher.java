package com.aol.cyclops.types.stream.reactive;

import java.util.stream.Stream;

import org.reactivestreams.Subscriber;

public interface ReactiveStreamsPublisher<T> {
	void  subscribe(Stream<T> stream,Subscriber<? super T> s);
}
