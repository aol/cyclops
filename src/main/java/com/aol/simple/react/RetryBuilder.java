package com.aol.simple.react;

import java.util.concurrent.Executors;

import lombok.Getter;
import lombok.Setter;

import com.nurkiewicz.asyncretry.AsyncRetryExecutor;

public class RetryBuilder {

	@Getter @Setter
	private static volatile AsyncRetryExecutor defaultInstance = factory.defaultInstance.getRetryExecutor();
	public enum factory {
		defaultInstance(new AsyncRetryExecutor(Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors())).
			    retryOn(Throwable.class).
			    withExponentialBackoff(500, 2).     //500ms times 2 after each retry
			    withMaxDelay(10_000).               //10 seconds
			    withUniformJitter().                //add between +/- 100 ms randomly
			    withMaxRetries(20));
		@Getter
		private final AsyncRetryExecutor retryExecutor;
		
		private factory(AsyncRetryExecutor retryExecutor){
			this.retryExecutor = retryExecutor;	
	}
	}
	
	public AsyncRetryExecutor parallelism(int parallelism){
		return defaultInstance.withScheduler(Executors.newScheduledThreadPool(parallelism));
	}
}
