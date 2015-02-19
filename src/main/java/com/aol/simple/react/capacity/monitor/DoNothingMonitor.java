package com.aol.simple.react.capacity.monitor;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * 
 * Use this class to switch off capacity monitoring
 * 
 * @author johnmcclean
 *
 */
public class DoNothingMonitor implements Consumer<CompletableFuture> {

	/* (non-Javadoc)
	 * @see java.util.function.Consumer#accept(java.lang.Object)
	 */
	@Override
	public void accept(CompletableFuture t) {
		
		
	}

}
