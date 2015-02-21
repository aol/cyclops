package com.aol.simple.react.stream;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;

import com.aol.simple.react.RetryBuilder;
import com.nurkiewicz.asyncretry.RetryExecutor;

/**
 * Lazy Stream Factory methods
 * 
 * @author johnmcclean
 *
 */
public interface LazyFutureStream<U>{

	/**
	 * Construct a SimpleReact Stage from a supplied array
	 * 
	 * @param array Array of value to form the reactive stream / sequence
	 * @return SimpleReact Stage
	 */
	public static <U> FutureStream<U> parallel(U... array){
		return new SimpleReact(false).reactToCollection(Arrays.asList(array));
	}
	/**
	 * @return Lazy SimpleReact for handling infinite streams
	 */
	public static SimpleReact parallel(){
		return new SimpleReact(false);
	}
	
	public static SimpleReact parallel(int parallelism){
		return  SimpleReact.builder().executor(new ForkJoinPool(parallelism)).retrier(new RetryBuilder().parallelism( parallelism)).eager(false).build();
	}
		
	public static SimpleReact parallelCommon(){
		return new SimpleReact(ForkJoinPool.commonPool(),false);
	}
	
	public static SimpleReact sequential(){
		return lazy(new ForkJoinPool(1));
	}
	public static SimpleReact sequentialCommon(){
		return lazy(ThreadPools.getCommonFreeThread());
	}
	
	/**
	 * @param executor Executor this SimpleReact instance will use to execute concurrent tasks.
	 * @return Lazy SimpleReact for handling infinite streams
	 */
	public static SimpleReact lazy(ExecutorService executor){
		return new SimpleReact(executor,false);
	}
	
	/**
	 * @param retry RetryExecutor this SimpleReact instance will use to retry concurrent tasks.
	 * @return Lazy SimpleReact for handling infinite streams
	 */
	public static SimpleReact lazy(RetryExecutor retry){
		return SimpleReact.builder().eager(false).retrier(retry).build();
	}
	/**
	 *  @param executor Executor this SimpleReact instance will use to execute concurrent tasks.
	 * @param retry RetryExecutor this SimpleReact instance will use to retry concurrent tasks.
	 * @return Lazy SimpleReact for handling infinite streams
	 */
	public static SimpleReact lazy(ExecutorService executor, RetryExecutor retry){
		return SimpleReact.builder().eager(false).executor(executor).retrier(retry).build();
	}
	
}
