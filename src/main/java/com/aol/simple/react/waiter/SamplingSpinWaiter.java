package com.aol.simple.react.waiter;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Consumer;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class SamplingSpinWaiter implements Consumer<CompletableFuture>{

	private final int sampleRate;

	private volatile int count =0;
	
	public SamplingSpinWaiter(){
		
		sampleRate =1000;
	}
	
	@Override
	public void accept(CompletableFuture n) {
		
		if(count++%sampleRate==0){
			LockSupport.parkNanos(0l);
		}
		
	}
	
}
