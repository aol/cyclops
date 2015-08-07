package com.aol.simple.react.async;

import com.aol.cyclops.lambda.utils.ExceptionSoftener;


public class NoWaitRetry<T> implements WaitStrategy<T> {

	@Override
	public T take(com.aol.simple.react.async.WaitStrategy.Takeable<T> t) throws InterruptedException {
		T result;
		
			while((result = t.take())==null){
				
			}
		
		return result;
	}

	@Override
	public boolean offer(WaitStrategy.Offerable o) throws InterruptedException {
		while(!o.offer()){
			
		}
		return true;
	}
	
}
