package com.aol.simple.react.async.wait;




/**
 * Repeatedly retry to take or offer element to Queue if full or data unavailable
 * 
 * @author johnmcclean
 *
 * @param <T>
 */
public class NoWaitRetry<T> implements WaitStrategy<T> {

	@Override
	public T take(com.aol.simple.react.async.wait.WaitStrategy.Takeable<T> t) throws InterruptedException {
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
