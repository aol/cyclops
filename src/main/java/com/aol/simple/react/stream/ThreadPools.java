package com.aol.simple.react.stream;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;

import lombok.AccessLevel;
import lombok.Getter;

public class ThreadPools {
	@Getter(AccessLevel.PACKAGE)
	private static final ExecutorService commonFreeThread = new ForkJoinPool(1);
}
