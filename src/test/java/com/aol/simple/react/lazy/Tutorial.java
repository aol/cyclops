package com.aol.simple.react.lazy;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import org.junit.Ignore;
import org.junit.Test;

import com.aol.simple.react.async.QueueFactories;
import com.aol.simple.react.stream.lazy.LazyFutureStream;
import com.aol.simple.react.threads.SequentialElasticPools;

public class Tutorial {

	String status="ok";
	/**
	 * check status every second, bath every 10 secs
	 */
	@Test @Ignore
	public void onePerSecondAndBatch(){
		List<Collection<String>> collected = LazyFutureStream.sequentialCommonBuilder().reactInfinitely(()->status)
													.withQueueFactory(QueueFactories.boundedQueue(1))
													.onePer(1, TimeUnit.SECONDS)
													.batchByTime(10, TimeUnit.SECONDS)
													.limit(15)
													.block();
		System.out.println(collected);
	}
	/**
	 * create a stream of time intervals in seconds
	 */
	@Test @Ignore
	public void secondsTimeInterval(){
		List<Collection<Integer>> collected = LazyFutureStream.sequentialCommonBuilder().iterateInfinitely(0, it -> it+1)
													.withQueueFactory(QueueFactories.boundedQueue(1))
													.onePer(1, TimeUnit.SECONDS)
													.batchByTime(10, TimeUnit.SECONDS)
													.limit(15)
													.block();
		System.out.println(collected);
	}
	@Test @Ignore
	public void range(){
		List<Collection<Integer>> collected = LazyFutureStream.sequentialCommonBuilder()
														.fromPrimitiveStream(IntStream.range(0, 10))
														.batchBySize(5)
														.block();
		System.out.println(collected);
	}
	
	@Test @Ignore
	public void executeRestCallInPool(){
		boolean success  = SequentialElasticPools.eagerReact.react( er-> er.react(()->restGet())
														.map(Tutorial::transformData)
														.then(Tutorial::saveToDb)
														.first());
	}
	private static boolean saveToDb(Object o){
		return true;
	}
	private Object restGet() {
		// TODO Auto-generated method stub
		return null;
	}
	private static Object transformData(Object o) {
		// TODO Auto-generated method stub
		return null;
	}
}
