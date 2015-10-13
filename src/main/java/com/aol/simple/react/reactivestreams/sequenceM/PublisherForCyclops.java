package com.aol.simple.react.reactivestreams.sequenceM;

import java.util.stream.Stream;

import org.reactivestreams.Subscriber;

import com.aol.cyclops.sequence.reactivestreams.ReactiveStreamsPublisher;
import com.aol.simple.react.reactivestreams.JDKReactiveStreamsPublisher;

public class PublisherForCyclops<T> implements ReactiveStreamsPublisher<T> {

	@Override
	public void subscribe(Stream<T> stream, Subscriber<? super T> s) {
		JDKReactiveStreamsPublisher pub =  JDKReactiveStreamsPublisher.ofSync(stream);
		pub.subscribe(s);
	}

}
