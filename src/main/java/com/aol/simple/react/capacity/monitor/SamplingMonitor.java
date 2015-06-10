package com.aol.simple.react.capacity.monitor;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Consumer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.experimental.Wither;

/**
 * 
 * Use this class to sample the CompletableFuture chains in an infinite flow as they are created
 * Can be used to with a LimitingMonitor to reduce touch points
 * 
 * @author johnmcclean
 *
 */
@AllArgsConstructor
@Builder
@Wither
public class SamplingMonitor implements Consumer<CompletableFuture>{

	private final int sampleRate;

	private volatile int count =0;
	private final Consumer<CompletableFuture> monitor;

	/**
	 * Sampling monitor that will pass control to supplied monitor when sampling triggered.
	 * Default rate is one in every 3 events.
	 * 
	 * @param monitor Events passed to monitor when triggered by sampling.
	 */
	public SamplingMonitor(Consumer<CompletableFuture> monitor){
		
		sampleRate = 3;
		this.monitor = monitor;
	}
	
	
	/* 
	 * Will pass control to another Monitor every n futures.
	 *	@param n CompletableFuture
	 * @see java.util.function.Consumer#accept(java.lang.Object)
	 */
	@Override
	public void accept(CompletableFuture n) {
		
		if(count++%sampleRate==0){
			monitor.accept(n);
		}
		
	}
	
}
