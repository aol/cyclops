package com.aol.cyclops.react.reactivestreams.reactiveseq;

import java.util.stream.Stream;

import org.reactivestreams.Subscriber;

import com.aol.cyclops.react.reactivestreams.JDKReactiveStreamsPublisher;
import com.aol.cyclops.types.stream.reactive.ReactiveStreamsPublisher;

public class PublisherForCyclops<T> implements ReactiveStreamsPublisher<T> {

	@Override
	public void subscribe(Stream<T> stream, Subscriber<? super T> s) {
		JDKReactiveStreamsPublisher pub =  JDKReactiveStreamsPublisher.ofSync(stream);
		pub.subscribe(s);
	}

}
