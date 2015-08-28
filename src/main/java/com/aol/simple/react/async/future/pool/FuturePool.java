package com.aol.simple.react.async.future.pool;

import java.util.Deque;
import java.util.function.Supplier;

import lombok.AllArgsConstructor;

import com.aol.simple.react.async.future.FastFuture;
import com.aol.simple.react.async.future.FinalPipeline;
@AllArgsConstructor
public class FuturePool {

	private final Deque<FastFuture> pool;
	private final int max;
	
	
	public<T> FastFuture<T> next(Supplier<FastFuture<T>> factory){
		if(pool.size()>0){
			
			FastFuture next = pool.pop();
			next.clearFast();
			return next;
		}
		
		return factory.get();
	}
	
	public <T> void done(FastFuture<T> f){
		if(pool.size()<max){
			
			pool.push(f);
		}
	}	
}
