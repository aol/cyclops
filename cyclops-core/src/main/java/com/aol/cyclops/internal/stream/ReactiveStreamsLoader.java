package com.aol.cyclops.internal.stream;

import java.util.Iterator;
import java.util.Optional;
import java.util.ServiceLoader;

import com.aol.cyclops.types.stream.reactive.ReactiveStreamsSubscriber;

public class ReactiveStreamsLoader {
	
	public final static Optional<ReactiveStreamsSubscriber> subscriber = loadSubscriber();
	
	public static Optional<ReactiveStreamsSubscriber> loadSubscriber(){
		ServiceLoader<ReactiveStreamsSubscriber>  sub = ServiceLoader.load(ReactiveStreamsSubscriber.class);
		Iterator<ReactiveStreamsSubscriber> it = sub.iterator();
		if(it.hasNext())
			return Optional.of(it.next());
		return Optional.empty();
	}

	
}
