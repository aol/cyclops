package com.aol.simple.react.stream;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;

import lombok.Getter;

public class ThreadPools {
	@Getter
	private static final ExecutorService commonFreeThread = new ForkJoinPool(1);

	public static ExecutorService getStandard() {
		return new ForkJoinPool(Runtime.getRuntime().availableProcessors());
	}
}
