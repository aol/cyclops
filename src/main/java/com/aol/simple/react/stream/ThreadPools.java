package com.aol.simple.react.stream;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;

import lombok.Getter;
import lombok.Setter;

public class ThreadPools {
	@Getter
	private static final ExecutorService commonFreeThread = new ForkJoinPool(1);

	@Setter
	private static volatile boolean useCommon = true;
	public static ExecutorService getStandard() {
		if(useCommon)
			return ForkJoinPool.commonPool();
		return new ForkJoinPool(Runtime.getRuntime().availableProcessors());
	}
}
