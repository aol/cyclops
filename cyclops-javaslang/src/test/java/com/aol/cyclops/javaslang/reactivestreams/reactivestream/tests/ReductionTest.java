package com.aol.cyclops.javaslang.reactivestreams.reactivestream.tests;


import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;

import com.aol.cyclops.javaslang.reactivestreams.ReactiveStream;
import com.aol.cyclops.sequence.Reducers;
import com.aol.cyclops.sequence.SequenceM;


public class ReductionTest {

	@Test
	public void reduceWithMonoid(){
		
		assertThat(ReactiveStream.of("hello","2","world","4").mapReduce(Reducers.toCountInt()),equalTo(4));
	}
	@Test
	public void reduceWithMonoid2(){
		
		assertThat(ReactiveStream.of("one","two","three","four").mapReduce(this::toInt,Reducers.toTotalInt()),
						equalTo(10));
	}
	
	int toInt(String s){
		if("one".equals(s))
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
		ReactiveStream.of("hello","2","world","4").mkString(",");
		assertThat(ReactiveStream.of("hello","2","world","4").reduce(Reducers.toString(",")),
				equalTo(",hello,2,world,4"));
	}
	
	
}
