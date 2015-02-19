package com.aol.simple.react;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;

import org.jooq.lambda.Seq;

import com.nurkiewicz.asyncretry.RetryExecutor;

/**
 * 
 * Static Factory methods for Simple React Flows
 * 
 * 
 * @author johnmcclean
 *
 */
public class ReactStream {
	/**
	 * Construct a SimpleReact Stage from a supplied array
	 * 
	 * @param array Array of value to form the reactive stream / sequence
	 * @return SimpleReact Stage
	 */
	public static <U> Stage<U> lazy(U... array){
		return new SimpleReact(false).reactToCollection(Arrays.asList(array));
	}
	/**
	 * @return Lazy SimpleReact for handling infinite streams
	 */
	public static SimpleReact lazy(){
		return new SimpleReact(false);
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
	
	/**
	 * Construct an Eager SimpleReact Stream from specified array
	 * 
	 * @param array Values to react to
	 * @return Next SimpleReact stage
	 */
	public static <U> Stage<U> eager(U... array){
		return new SimpleReact().reactToCollection(Arrays.asList(array));
	}
	/**
	 * @return Eager SimpleReact for handling finite streams
	 */
	public static SimpleReact eager(){
		return new SimpleReact(true);
	}
	/**
	 * @param executor Executor this SimpleReact instance will use to execute concurrent tasks.
	 * @return Eager SimpleReact for handling finite streams
	 */
	public static SimpleReact eager(ExecutorService executor){
		return new SimpleReact(executor,true);
	}
	/**
	 * @param retry RetryExecutor this SimpleReact instance will use to retry concurrent tasks.
	 * @return Eager SimpleReact for handling finite streams
	 */
	public static SimpleReact eager(RetryExecutor retry){
		return SimpleReact.builder().retrier(retry).build();
	}
	/**
	 *  @param executor Executor this SimpleReact instance will use to execute concurrent tasks.
	 * @param retry RetryExecutor this SimpleReact instance will use to retry concurrent tasks.
	 * @return Eager SimpleReact for handling finite streams
	 */
	public static SimpleReact eager(ExecutorService executor, RetryExecutor retry){
		return SimpleReact.builder().executor(executor).retrier(retry).build();
	}
	
}
