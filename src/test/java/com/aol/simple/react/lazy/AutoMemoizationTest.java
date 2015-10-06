package com.aol.simple.react.lazy;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import java.util.List;
import java.util.Map;
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
							  .map(i->calc(i))
							  .peek(System.out::println)
							  .toList();
						
	    System.out.println(result);
		assertThat(called,equalTo(1));
		assertThat(result.size(),equalTo(4));
	  }
	private int calc(int in){
		called++;
		return in*2;
	}
}
