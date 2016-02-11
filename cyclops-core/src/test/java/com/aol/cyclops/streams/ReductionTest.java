package com.aol.cyclops.streams;


import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;

import com.aol.cyclops.Reducers;
import com.aol.cyclops.sequence.SequenceM;


public class ReductionTest {

	@Test
	public void reduceWithMonoid(){
		
		assertThat(SequenceM.of("hello","2","world","4").mapReduce(Reducers.toCountInt()),equalTo(4));
	}
	@Test
	public void reduceWithMonoid2(){
		
		assertThat(SequenceM.of("one","two","three","four").mapReduce(this::toInt,Reducers.toTotalInt()),
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
		SequenceM.of("hello","2","world","4").join(",");
		assertThat(SequenceM.of("hello","2","world","4").reduce(Reducers.toString(",")),
				equalTo(",hello,2,world,4"));
	}
	@Test
    public void testCollectors() {
		List result = SequenceM.of(1,2,3)
							.collectStream(Stream.of(Collectors.toList(),Collectors.summingInt(Integer::intValue),Collectors.averagingInt(Integer::intValue)));
		
		assertThat(result.get(0),equalTo(Arrays.asList(1,2,3)));
		assertThat(result.get(1),equalTo(6));
		assertThat(result.get(2),equalTo(2.0));
    }
	@Test
    public void testCollectorsIterable() {
		List result = SequenceM.of(1,2,3)
							.collectIterable(Arrays.asList(Collectors.toList(),Collectors.summingInt(Integer::intValue),Collectors.averagingInt(Integer::intValue)));
		
		assertThat(result.get(0),equalTo(Arrays.asList(1,2,3)));
		assertThat(result.get(1),equalTo(6));
		assertThat(result.get(2),equalTo(2.0));
    }
	
}
