package com.aol.cyclops.react.lazy;

import static org.junit.Assert.*;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import org.junit.Test;

import com.aol.cyclops.control.LazyReact;
import com.aol.cyclops.react.collectors.lazy.MaxActive;
import com.aol.cyclops.types.futurestream.LazyFutureStream;
import com.aol.cyclops.util.SimpleTimer;

public class ParallelTest {

	Object value =null;
	@Test
	public void runOnCurrent(){
		LazyReact lazy = LazyReact.parallelBuilder().autoOptimizeOn();
		System.out.println("Starting");
		
		
		
		lazy.range(0,100)
				.map(i->i+2)
				.thenSync(i-> {
				try{ 
					Thread.sleep(500);
				} catch (Exception e) {
					
				} return i;})
				.thenSync(i->"hello"+i)
				//.peekSync(System.out::println)
				.peekSync(val-> value=val)
				.runOnCurrent();
		
		assertNotNull(value);
	}
	
	@Test
	public void runThread(){
		CompletableFuture cf = new CompletableFuture();
			LazyFutureStream s = LazyReact.sequentialBuilder().withMaxActive(MaxActive.IO).async()
				.reactInfinitely(()->1).limit(1_000_000);
				
				for (int x = 0; x < 60; x++) {
					s = s.then(Function.identity());
				}
				
				//s.runOnCurrent();
				s.runThread(()->cf.complete(true));
				
				cf.join();
	}
}
