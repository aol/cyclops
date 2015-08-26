package com.aol.simple.react.capacity.monitor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.experimental.Wither;

import com.aol.simple.react.async.future.FastFuture;
import com.aol.simple.react.config.MaxActive;

/**
 * Class to be used to limit the number of active CompletableFuture Streams, in an infinite SimpleReact stream.
 * Can be used alongside Garbage Collector configuration (e.g. G1) to assure constant throughput rates.
 * 
 * @author johnmcclean
 *
 */
@Wither
@AllArgsConstructor
public class LimitingMonitor<U> implements Consumer<FastFuture<U>>{

	private final List<FastFuture<U>> active = new ArrayList<>(1000);
	private final MaxActive maxActive;
	

	/**
	 * Limiting Monitor with default capacity settings
	 */
	public LimitingMonitor(){
		maxActive = MaxActive.defaultValue.factory.getInstance();
		
	}
	
	
	/* 
	 *	@param n CompletableFuture will be batched and if active futures above MaxActive,
	 *            will block until reduced to acceptable level
	 *            
	 * @see java.util.function.Consumer#accept(java.lang.Object)
	 */
	@Override
	public void accept(FastFuture n) {
		if(n.isDone())
			return;
		active.add(n);
			
		
		
		if(active.size()>maxActive.getMaxActive()){
			
			while(active.size()>maxActive.getReduceTo()){
				LockSupport.parkNanos(0l);
				List<FastFuture> toRemove = active.stream().filter(cf -> cf.isDone()).collect(Collectors.toList());
				active.removeAll(toRemove);
				
			}
		}
		
	}
	
	
}
