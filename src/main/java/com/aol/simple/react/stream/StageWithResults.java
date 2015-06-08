package com.aol.simple.react.stream;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Function;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import com.aol.simple.react.exceptions.ExceptionSoftener;
import com.aol.simple.react.stream.traits.ConfigurableStream;

@AllArgsConstructor
@Slf4j
public class StageWithResults<RS,U> {
	private final ExceptionSoftener exceptionSoftener = ExceptionSoftener.singleton.factory.getInstance();
	private final Executor taskExecutor;
	
	private final ConfigurableStream<U> stage;
	@Getter
	private final RS results;
	

	public StageWithResults(ConfigurableStream<U> stage, RS results) {
		
		this.taskExecutor = stage.getTaskExecutor();
		this.stage = stage;
		this.results = results;
	}
	
	
	

	
	/**
	 * This method allows the SimpleReact Executor to be reused by JDK parallel streams. It is best used when
	 * collectResults and block are called explicitly for finer grained control over the blocking conditions.
	 * 
	 * @param fn Function that contains parallelStream code to be executed by the SimpleReact ForkJoinPool (if configured)
	 */
	public <R> R submit(Function <RS,R> fn){
		return submit (() -> fn.apply(this.results));
	}
	
	
	
	/**
	 * This method allows the SimpleReact Executor to be reused by JDK parallel streams
	 * 
	 * @param callable that contains code
	 */
	public <T> T submit(Callable<T> callable){
		if(taskExecutor instanceof ForkJoinPool){
			log.debug("Submited callable to SimpleReact ForkJoinPool. JDK ParallelStreams will reuse SimpleReact ForkJoinPool.");
			try {
				
				return ((ForkJoinPool) taskExecutor).submit(callable).get();
			} catch (ExecutionException e) {
				exceptionSoftener.throwSoftenedException(e);
				
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				exceptionSoftener.throwSoftenedException(e);
				
			}
		}
		try {
			log.debug("Submited callable but do not have a ForkJoinPool. JDK ParallelStreams will use Common ForkJoinPool not SimpleReact Executor.");
			return callable.call();
		} catch (Exception e) {
			throw new RuntimeException(e); 
		}
	}

}
