package com.aol.simple.react.threads;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.aol.simple.react.async.factories.QueueFactories;
import com.aol.simple.react.async.factories.QueueFactory;
import com.aol.simple.react.stream.lazy.LazyReact;
import com.aol.simple.react.util.SimpleTimer;

public class ReactToList {
	static List res;
	public static void main(String[] args){
		List<Integer> values = new ArrayList();
		for(int i=0;i<400000;i++)
			values.add(i);
	
		
		LazyReact lazy = LazyReact.sequentialCurrentBuilder().withAsync(false);
		SimpleTimer t = new SimpleTimer();
		
		for(int x=0;x<100000;x++){
			res = lazy.from(values)
				.map(i->i+2)
				.map(i->i*3)
				.collect(Collectors.toList());
		}
			System.out.println(t.getElapsedNanoseconds());
		
		
			
	}
}
