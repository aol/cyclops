package com.aol.simple.react.lazy;

import static org.junit.Assert.*;

import org.junit.Test;

import com.aol.simple.react.stream.lazy.LazyReact;
import com.aol.simple.react.util.SimpleTimer;

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
}
