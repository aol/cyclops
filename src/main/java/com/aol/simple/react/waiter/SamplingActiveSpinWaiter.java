package com.aol.simple.react.waiter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.experimental.Builder;
import lombok.experimental.Wither;

import com.aol.simple.react.config.MaxActive;

@Wither
@AllArgsConstructor
@Builder
public class SamplingActiveSpinWaiter implements Consumer<CompletableFuture>{

	private final List<CompletableFuture> active = new ArrayList<>(1000);
	private final MaxActive maxActive;
	private final int sampleRate;

	private volatile int count =0;
	
	public SamplingActiveSpinWaiter(){
		maxActive = MaxActive.defaultValue.factory.getInstance();
		sampleRate =3;
	}
	
	@Override
	public void accept(CompletableFuture n) {
		
		if(count++%sampleRate==0){
			active.add(n);
			
		
		
			if(active.size()>maxActive.getMaxActive()){
				
				while(active.size()>maxActive.getReduceTo()){
					LockSupport.parkNanos(0l);
					List<CompletableFuture> toRemove = active.stream().filter(cf -> cf.isDone()).collect(Collectors.toList());
					active.removeAll(toRemove);
					
				}
			}
		}
		
	}
	
	
}

