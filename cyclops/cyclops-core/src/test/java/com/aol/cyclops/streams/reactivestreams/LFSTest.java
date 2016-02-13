package com.aol.cyclops.streams.reactivestreams;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.junit.Ignore;
import org.junit.Test;

import com.aol.cyclops.react.stream.traits.LazyFutureStream;
import com.nurkiewicz.asyncretry.AsyncRetryExecutor;

public class LFSTest {
/**
	@Test @Ignore
	public void lfs() throws InterruptedException{
		AsyncRetryExecutor retry =new AsyncRetryExecutor(Executors.newScheduledThreadPool(
		           5)).withExponentialBackoff(1000, 5).withMaxDelay(10_000).withMaxRetries(3);

		Executor exec = Executors.newFixedThreadPool(5);
		LazyFutureStream.of(1,2,3)
		.withRetrier(retry)
		.withTaskExecutor(exec)
		.async()
        .peek(t -> System.err.println("AuditTrail 1 " + t) )
       
        .retry(s -> {
        	   System.out.println("retry");
          throw new RuntimeException("boo!");
         
        })
        .onFail(t -> {
          System.out.println("fail");
          return "hello";
        })
        .run();
		
		Thread.sleep(60_000);
	}
	**/
}
