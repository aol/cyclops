package com.aol.simple.react.async.pipes;

import com.aol.simple.react.stream.eager.EagerReact;

public class EagerReactors {
	private static volatile int IOThreadPoolSize=100;
	private static volatile int CPUBoundThreadPoolSize=Runtime.getRuntime().availableProcessors();
	private static void setIOThreadPoolSize(int size){
		IOThreadPoolSize=size;
	}
	private static void setCPUBoundThreadPoolSize(int size){
		CPUBoundThreadPoolSize=size;
	}
	public final static EagerReact ioReact = EagerReact.parallelBuilder(IOThreadPoolSize);
	public final static EagerReact cpuReact = EagerReact.parallelBuilder(CPUBoundThreadPoolSize);
	
}
