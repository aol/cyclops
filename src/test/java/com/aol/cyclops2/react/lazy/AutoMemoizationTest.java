package com.aol.cyclops2.react.lazy;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import cyclops.async.LazyReact;

public class AutoMemoizationTest {
   AtomicInteger called = new AtomicInteger(0);
	@Test
	public void autoMemoize(){
		
		called.set(0);
		Map cache = new ConcurrentHashMap<>();
		LazyReact react = new LazyReact().autoMemoizeOn((key,fn)-> cache.computeIfAbsent(key,fn));
		List result = react.of(1,1,1,1)
							  .capture(e->e.printStackTrace())
							  .map(i->calc(i))
							  .peek(System.out::println)
							
							  .toList();
						
	    
		System.out.println(result);
		assertThat(called.get(),equalTo(1));
		assertThat(result.size(),equalTo(4));
		
		
	  }
	@Test
	public void autoMemoizeOff(){
		called.set(0);
		Map cache = new ConcurrentHashMap<>();
		LazyReact react = new LazyReact();
		List result = react.of(1,1,1,1)
							  .capture(e->e.printStackTrace())
							  .map(i->calc(i))
							  .peek(System.out::println)
							  .toList();
						
	    
		System.out.println(result);
		
		assertThat(result.size(),equalTo(4));
		assertThat(called.get(),greaterThan(3));
	  }
	
	@Test
	public void autoMemoizeSet(){
		called.set(0);
		Map cache = new ConcurrentHashMap<>();
		LazyReact react = new LazyReact().autoMemoizeOn((key,fn)-> cache.computeIfAbsent(key,fn));
		Set<Integer> result = react.of(1,1,1,1)
							  .capture(e->e.printStackTrace())
							  .map(i->calc(i))
							  .peek(System.out::println)
							   .toSet();
						
	    
		System.out.println(result);
		assertThat(called.get(),equalTo(1));
		assertThat(result.size(),equalTo(1));
	  }
	private int calc(int in){
		called.incrementAndGet();
		return in*2;
	}
}
