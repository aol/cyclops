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

@Wither
@AllArgsConstructor
@Builder
public class ActiveSpinWaiter implements Consumer<CompletableFuture>{

	private final List<CompletableFuture> active = new ArrayList<>(1000);
	private final int maxActive;
	private final int reduceTo;

	public ActiveSpinWaiter(){
		maxActive = 70;
		reduceTo = 30;
	}
	
	@Override
	public void accept(CompletableFuture n) {
		
		active.add(n);
			
		
		
		if(active.size()>maxActive){
			
			while(active.size()>reduceTo){
				LockSupport.parkNanos(0l);
				List<CompletableFuture> toRemove = active.stream().filter(cf -> cf.isDone()).collect(Collectors.toList());
				active.removeAll(toRemove);
				
			}
		}
		
	}
	
	
}
