package com.aol.simple.react.lazy;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.Test;

import com.aol.simple.react.stream.lazy.LazyReact;

public class AutoMemoizationTest {
   volatile int called = 0;
	@Test
	public void autoMemoize(){
		called=0;
		Map cache = new ConcurrentHashMap<>();
		LazyReact react = new LazyReact().autoMemoizeOn((key,fn)-> cache.computeIfAbsent(key,fn));
		List result = react.of(1,1,1,1)
							  .capture(e->e.printStackTrace())
							  .map(i->calc(i))
							  .peek(System.out::println)
							
							  .toList();
						
	    
		System.out.println(result);
		assertThat(called,equalTo(1));
		assertThat(result.size(),equalTo(4));
	  }
	@Test
	public void autoMemoizeOff(){
		called=0;
		Map cache = new ConcurrentHashMap<>();
		LazyReact react = new LazyReact();
		List result = react.of(1,1,1,1)
							  .capture(e->e.printStackTrace())
							  .map(i->calc(i))
							  .peek(System.out::println)
							  .toList();
						
	    
		System.out.println(result);
		assertThat(called,greaterThan(4));
		assertThat(result.size(),equalTo(4));
	  }
	
	@Test
	public void autoMemoizeSet(){
		called=0;
		Map cache = new ConcurrentHashMap<>();
		LazyReact react = new LazyReact().autoMemoizeOn((key,fn)-> cache.computeIfAbsent(key,fn));
		Set<Integer> result = react.of(1,1,1,1)
							  .capture(e->e.printStackTrace())
							  .map(i->calc(i))
							  .peek(System.out::println)
							   .toSet();
						
	    
		System.out.println(result);
		assertThat(called,equalTo(1));
		assertThat(result.size(),equalTo(1));
	  }
	private int calc(int in){
		called++;
		return in*2;
	}
}
