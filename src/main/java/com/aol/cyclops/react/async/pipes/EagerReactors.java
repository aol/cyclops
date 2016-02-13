package com.aol.cyclops.react.async.pipes;

import com.aol.cyclops.control.SimpleReact;

public class EagerReactors {
	private static volatile int IOThreadPoolSize=100;
	private static volatile int CPUBoundThreadPoolSize=Runtime.getRuntime().availableProcessors();
	private static void setIOThreadPoolSize(int size){
		IOThreadPoolSize=size;
	}
	private static void setCPUBoundThreadPoolSize(int size){
		CPUBoundThreadPoolSize=size;
	}
	public final static SimpleReact ioReact = SimpleReact.parallelBuilder(IOThreadPoolSize);
	public final static SimpleReact cpuReact = SimpleReact.parallelBuilder(CPUBoundThreadPoolSize);
	
}
