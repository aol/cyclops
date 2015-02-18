package com.aol.simple.react.collectors.lazy;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import lombok.AllArgsConstructor;
import lombok.experimental.Wither;

@AllArgsConstructor
@Wither
public class SamplingCollector<T> implements LazyResultConsumer<T>{

	private final int sampleRate;
	private final LazyResultConsumer<T> next;

	private long count = 0;
	
	public SamplingCollector(int sampleRate, LazyResultConsumer<T> next) {
		this.sampleRate = sampleRate;
		this.next = next;
	}
	
	@Override
	public void accept(CompletableFuture<T> t) {
		if(count%sampleRate ==0)
			next.accept(t);
		
	}

	@Override
	public LazyResultConsumer<T> withResults(Collection<T> t) {
		return this.withNext(next.withResults(t));
	}

	@Override
	public Collection<T> getResults() {
		return next.getResults();
	}

	

	


	
}
