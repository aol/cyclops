package com.aol.simple.react.threads;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

import com.aol.simple.react.config.MaxActive;
import com.aol.simple.react.stream.lazy.LazyReact;
import com.aol.simple.react.util.SimpleTimer;

public class ReactToList {
	static List res;
	public static void main(String[] args){
		List<Integer> values = new ArrayList();
		for(int i=0;i<4000;i++)
			values.add(i);
	
		
		LazyReact lazy = LazyReact.sequentialCurrentBuilder()
									.withAsync(false)
									.withMaxActive(new MaxActive(4010,1000));
		SimpleTimer t = new SimpleTimer();
		
		for(int x=0;x<1000;x++){
			res = lazy.from(values)
				.map(i->i+2)
				.map(i->i*3)
				.collect(Collectors.toList());
		}
		System.out.println(t.getElapsedNanoseconds());
		
		
			
	}
	@Test
	public void parallel(){
		List<Integer> values = new ArrayList();
		for(int i=0;i<4000;i++)
			values.add(i);
	
		
		LazyReact lazy = LazyReact.parallelBuilder(8).autoOptimiseOn()
				.withMaxActive(MaxActive.CPU);
		SimpleTimer t = new SimpleTimer();
		
		for(int x=0;x<1000;x++){
			res = lazy.from(values)
				.map(i->i+2)
				.map(i->i*3)
				.collect(Collectors.toList());
		}
			System.out.println(t.getElapsedNanoseconds());
	}
}
