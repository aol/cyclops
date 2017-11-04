package com.oath.cyclops.streams.reactivestreams;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import org.junit.Test;

import cyclops.async.LazyReact;

public class LFSTest {

        static Supplier<Integer> countGen(AtomicInteger i) {
            return (()-> i.getAndIncrement());
        }
        @Test
        public void test(){
            final Supplier<Integer> count = countGen(new AtomicInteger(1));
            final Optional<Integer> sum = new LazyReact(100,100).generate(count).limit(10).reduce((a, b) -> a + b);
            assertThat(sum.get(),equalTo(55));
        }

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
