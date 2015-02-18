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
public class LazyCollector<T> implements LazyResultConsumer<T>{

	
	private final Collection<T> results;
	private final List<CompletableFuture<T>> active = new ArrayList<>();
	private final MaxActive maxActive;
	
	public LazyCollector(MaxActive maxActive){
		this.maxActive = maxActive;
		this.results =null;
	}
	public LazyCollector(){
		this.maxActive = MaxActive.defaultValue.factory.getInstance();
		this.results =null;
	}
	
	@Override
	public void accept(CompletableFuture<T> t) {
		active.add(t);
		
		if(active.size()>maxActive.getMaxActive()){
			
			while(active.size()>maxActive.getReduceTo()){
				LockSupport.parkNanos(0l);
				List<CompletableFuture> toRemove = active.stream().filter(cf -> cf.isDone()).collect(Collectors.toList());
				active.removeAll(toRemove);
				results.addAll((Collection<? extends T>) toRemove.stream().map(cf -> cf.join()).collect(Collectors.toList()));
				
			}
		}
		
		
		
	}
	public Collection<T> getResults(){
		moveBatchToResult();
		return results;
	}

	private void moveBatchToResult() {
		results.addAll(active.stream().map(cf -> cf.join()).collect(Collectors.toList()));
	}
	
	
}
