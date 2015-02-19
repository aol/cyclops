package com.aol.simple.react.collectors.lazy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.LockSupport;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Wither;

import com.aol.simple.react.config.MaxActive;
@Wither
@AllArgsConstructor
public class EmptyCollector<T> implements LazyResultConsumer<T> {
	

	private final List<CompletableFuture<T>> active = new ArrayList<>();
	@Getter
	private final MaxActive maxActive;
	
	public EmptyCollector(){
		maxActive = MaxActive.defaultValue.factory.getInstance();
	}
	
	@Override
	public void accept(CompletableFuture<T> t) {
		active.add(t);
		
		if(active.size()>maxActive.getMaxActive()){
			
			while(active.size()>maxActive.getReduceTo()){
				LockSupport.parkNanos(0l);
				List<CompletableFuture> toRemove = active.stream().filter(cf -> cf.isDone()).collect(Collectors.toList());
				active.removeAll(toRemove);
					
			}
		}
		
		
		
	}

	@Override
	public LazyResultConsumer<T> withResults(Collection<T> t) {
		
		return this.withMaxActive(maxActive);
	}

	@Override
	public Collection<T> getResults() {
		active.stream().forEach(cf -> cf.join());
		active.clear();
		return new ArrayList<>();
	}
	
}
