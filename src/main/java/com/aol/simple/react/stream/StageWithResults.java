package com.aol.simple.react.stream;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Function;

import com.aol.simple.react.exceptions.ExceptionSoftener;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@AllArgsConstructor
@Slf4j
public class StageWithResults<RS,U> {
	private final ExceptionSoftener exceptionSoftener = ExceptionSoftener.singleton.factory.getInstance();
	private final ExecutorService taskExecutor;
	
	private final FutureStream<U> stage;
	@Getter
	private final RS results;
	

	public StageWithResults(FutureStreamImpl<U> stage, RS results) {
		
		this.taskExecutor = stage.getTaskExecutor();
		this.stage = stage;
		this.results = results;
	}
	
	/**
	 * Continue building the reactive dataflow
	 * 
	 * @return Builder for the current Stage in the reactive flow
	 */
	public FutureStream<U> proceed(){
		return stage;
	}
	

	
	/**
	 * This method allows the SimpleReact ExecutorService to be reused by JDK parallel streams. It is best used when
	 * collectResults and block are called explicitly for finer grained control over the blocking conditions.
	 * 
	 * @param fn Function that contains parallelStream code to be executed by the SimpleReact ForkJoinPool (if configured)
	 */
	public <R> R submit(Function <RS,R> fn){
		return submit (() -> fn.apply(this.results));
	}
	
	
	
	/**
	 * This method allows the SimpleReact ExecutorService to be reused by JDK parallel streams
	 * 
	 * @param callable that contains code
	 */
	<T> T submit(Callable<T> callable){
		if(taskExecutor instanceof ForkJoinPool){
			log.debug("Submited callable to SimpleReact ForkJoinPool. JDK ParallelStreams will reuse SimpleReact ForkJoinPool.");
			try {
				return taskExecutor.submit(callable).get();
			} catch (ExecutionException e) {
				exceptionSoftener.throwSoftenedException(e);
				
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				exceptionSoftener.throwSoftenedException(e);
				
			}
		}
		try {
			log.debug("Submited callable but do not have a ForkJoinPool. JDK ParallelStreams will use Common ForkJoinPool not SimpleReact ExecutorService.");
			return callable.call();
		} catch (Exception e) {
			throw new RuntimeException(e); 
		}
	}

}
