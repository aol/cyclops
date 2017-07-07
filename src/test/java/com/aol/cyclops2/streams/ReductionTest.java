package com.aol.cyclops2.streams;


import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import cyclops.companion.Reducers;
import cyclops.stream.ReactiveSeq;


public class ReductionTest {

	@Test
	public void reduceWithMonoid(){
		
		assertThat(ReactiveSeq.of("hello","2","world","4").mapReduce(Reducers.toCountInt()),equalTo(4));
	}
	@Test
	public void reduceWithMonoid2(){
		
		assertThat(ReactiveSeq.of("replaceWith","two","three","four").mapReduce(this::toInt,Reducers.toTotalInt()),
						equalTo(10));
	}
	
	int toInt(String s){
		if("replaceWith".equals(s))
			return 1;
		if("two".equals(s))
			return 2;
		if("three".equals(s))
			return 3;
		if("four".equals(s))
			return 4;
		return -1;
	}
	@Test
	public void reduceWithMonoidJoin(){
		ReactiveSeq.of("hello","2","world","4").join(",");
		assertThat(ReactiveSeq.of("hello","2","world","4").reduce(Reducers.toString(",")),
				equalTo(",hello,2,world,4"));
	}
	
}
