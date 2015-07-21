package com.aol.cyclops.lambda.utils;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.val;

import org.junit.Test;

import com.aol.cyclops.lambda.api.AsStreamable;
import com.aol.cyclops.lambda.api.Monoid;
import com.aol.cyclops.streams.StreamUtils;

import static java.util.stream.Collectors.averagingInt;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.mapping;
public class StreamUtilsTest {

	@Test
	public void testReverse() {
		
		assertThat(StreamUtils.reverse(Stream.of(1,2,3)).collect(Collectors.toList())
				,equalTo(Arrays.asList(3,2,1)));
	}

	@Test
	public void testReversedStream() {
		
		assertThat(StreamUtils.reversedStream(Arrays.asList(1,2,3)).collect(Collectors.toList())
				,equalTo(Arrays.asList(3,2,1)));
		
		
	}

	@Test
	public void testCycleStreamOfU() {
		assertThat(StreamUtils.cycle(Stream.of(1,2,3)).limit(6).collect(Collectors.toList()),equalTo(Arrays.asList(1,2,3,1,2,3)));
	}

	@Test
	public void testCycleStreamableOfU() {
		assertThat(StreamUtils.cycle(AsStreamable.asStreamable(Stream.of(1,2,3))).limit(6).collect(Collectors.toList()),equalTo(Arrays.asList(1,2,3,1,2,3)));
	}

	@Test
	public void testStreamIterableOfU() {
		assertThat(StreamUtils.stream(Arrays.asList(1,2,3)).collect(Collectors.toList()),equalTo(Arrays.asList(1,2,3)));
	}

	@Test
	public void testStreamIteratorOfU() {
		assertThat(StreamUtils.stream(Arrays.asList(1,2,3).iterator()).collect(Collectors.toList()),equalTo(Arrays.asList(1,2,3)));
	}

	@Test
	public void testStreamMapOfKV() {
		Map<String,String> map = new HashMap<>();
		map.put("hello","world");
		assertThat(StreamUtils.stream(map).collect(Collectors.toList()),equalTo(Arrays.asList(new AbstractMap.SimpleEntry("hello","world"))));
	}


	@Test
	public void reducer(){
		Monoid<String> concat = Monoid.of("",(a,b)->a+b);
		Monoid<String> join = Monoid.of("",(a,b)->a+","+b);
		
		
		 assertThat(StreamUtils.reduce(Stream.of("hello", "world", "woo!"),Stream.of(concat,join))
		                 
		                  ,equalTo(Arrays.asList("helloworldwoo!",",hello,world,woo!")));
	}
	@Test
	public void reducer2(){
		Monoid<Integer> sum = Monoid.of(0,(a,b)->a+b);
		Monoid<Integer> mult = Monoid.of(1,(a,b)->a*b);
		val result = StreamUtils.reduce(Stream.of(1,2,3,4),Arrays.asList(sum,mult));
				
		 
		assertThat(result,equalTo(Arrays.asList(10,24)));
	}
	@Test
    public void testCollectors() {
		List result = StreamUtils.collect(Stream.of(1,2,3),Arrays.asList(Collectors.toList(),Collectors.summingInt(Integer::intValue),Collectors.averagingInt(Integer::intValue)));
		
		assertThat(result.get(0),equalTo(Arrays.asList(1,2,3)));
		assertThat(result.get(1),equalTo(6));
		assertThat(result.get(2),equalTo(2.0));
    }

	@Test
	public void testSkipUntil(){
		
		assertThat(StreamUtils.skipUntil(Stream.of(4,3,6,7),i->i==6).collect(Collectors.toList()),
				equalTo(Arrays.asList(6,7)));
		
		
	}
	@Test
	public void testSkipWhile(){
		assertThat(StreamUtils.skipWhile(Stream.of(4,3,6,7).sorted(),i->i<6).collect(Collectors.toList()),
				equalTo(Arrays.asList(6,7)));
	}
	
	@Test
	public void testLimitWhile(){
		assertThat(StreamUtils.limitWhile(Stream.of(4,3,6,7).sorted(),i->i<6).collect(Collectors.toList()),
				equalTo(Arrays.asList(3,4)));
	}
	@Test
	public void testLimitUntil(){
		assertThat(StreamUtils.limitUntil(Stream.of(4,3,6,7),i->i==6).collect(Collectors.toList()),
				equalTo(Arrays.asList(4,3)));
	}
}
