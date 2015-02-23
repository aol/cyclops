package com.aol.simple.react.stream;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ScheduledExecutorService;

import lombok.Getter;
import lombok.Setter;

public class ThreadPools {
	@Getter
	private static final ExecutorService commonFreeThread = new ForkJoinPool(1);
	
	@Getter
	private static final ExecutorService commonLazyExecutor = new ForkJoinPool(1);
	
	@Getter
	private static final ScheduledExecutorService commonFreeThreadRetry = Executors.newScheduledThreadPool(1);

	
	@Setter
	private static volatile boolean useCommon = true;
	public static ExecutorService getStandard() {
		if(useCommon)
			return ForkJoinPool.commonPool();
		return new ForkJoinPool(Runtime.getRuntime().availableProcessors());
	}
	public static ExecutorService getSequential() {
		if(useCommon)
			return commonFreeThread;
		else
		return new ForkJoinPool(1);
	}
	public static ScheduledExecutorService getSequentialRetry() {
		if(useCommon)
			return commonFreeThreadRetry;
		else
			return Executors.newScheduledThreadPool(1);
	}
	public static ExecutorService getLazyExecutor() {
		if(useCommon)
			return commonLazyExecutor;
		else
			return  new ForkJoinPool(1); 
	}
}