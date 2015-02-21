package com.aol.simple.react.stream;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;

import com.aol.simple.react.RetryBuilder;
import com.nurkiewicz.asyncretry.RetryExecutor;

/**
 * 
 * Static Factory methods for Simple React Flows
 * 
 * 
 * @author johnmcclean
 *
 */
public interface EagerFutureStream{
	
	
	/**
	 * Construct an Eager SimpleReact Stream from specified array
	 * 
	 * @param array Values to react to
	 * @return Next SimpleReact stage
	 */
	public static <U> FutureStream<U> parallel(U... array){
		return new SimpleReact().reactToCollection(Arrays.asList(array));
	}
	
	
	/**
	 * @return Eager SimpleReact for handling finite streams
	 */
	public static SimpleReact parallel(){
		return new SimpleReact(true);
	}
	public static SimpleReact parallel(int parallelism){
		return eagerBuilder(new ForkJoinPool(parallelism), new RetryBuilder().parallelism( parallelism));
	}
	public static SimpleReact paraellelCommon(){
		return new SimpleReact(ForkJoinPool.commonPool(),true);
	}
	
	public static SimpleReact sequential(){
		return eagerBuilder(new ForkJoinPool(1), new RetryBuilder().parallelism(1));
	}
	public static SimpleReact sequentialCommon(){
		return eagerBuilder(ThreadPools.getCommonFreeThread());
	}
	
	
	/**
	 * @param executor Executor this SimpleReact instance will use to execute concurrent tasks.
	 * @return Eager SimpleReact for handling finite streams
	 */
	public static SimpleReact eagerBuilder(ExecutorService executor){
		return new SimpleReact(executor,true);
	}
	/**
	 * @param retry RetryExecutor this SimpleReact instance will use to retry concurrent tasks.
	 * @return Eager SimpleReact for handling finite streams
	 */
	public static SimpleReact eagerBuilder(RetryExecutor retry){
		return SimpleReact.builder().retrier(retry).build();
	}
	/**
	 *  @param executor Executor this SimpleReact instance will use to execute concurrent tasks.
	 * @param retry RetryExecutor this SimpleReact instance will use to retry concurrent tasks.
	 * @return Eager SimpleReact for handling finite streams
	 */
	public static SimpleReact eagerBuilder(ExecutorService executor, RetryExecutor retry){
		return SimpleReact.builder().executor(executor).retrier(retry).build();
	}
	
}
