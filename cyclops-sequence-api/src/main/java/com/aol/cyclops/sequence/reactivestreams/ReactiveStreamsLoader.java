package com.aol.cyclops.sequence.reactivestreams;

import java.util.Iterator;
import java.util.Optional;
import java.util.ServiceLoader;

public class ReactiveStreamsLoader {
	
	public final static Optional<ReactiveStreamsSubscriber> subscriber = loadSubscriber();
	public static final Optional<ReactiveStreamsPublisher> publisher = loadPublisher();
	
	public static Optional<ReactiveStreamsSubscriber> loadSubscriber(){
		ServiceLoader<ReactiveStreamsSubscriber>  sub = ServiceLoader.load(ReactiveStreamsSubscriber.class);
		Iterator<ReactiveStreamsSubscriber> it = sub.iterator();
		if(it.hasNext())
			return Optional.of(it.next());
		return Optional.empty();
	}

	private static Optional<ReactiveStreamsPublisher> loadPublisher() {
		ServiceLoader<ReactiveStreamsPublisher>  sub = ServiceLoader.load(ReactiveStreamsPublisher.class);
		Iterator<ReactiveStreamsPublisher> it = sub.iterator();
		if(it.hasNext())
			return Optional.of(it.next());
		return Optional.empty();
	}
}
