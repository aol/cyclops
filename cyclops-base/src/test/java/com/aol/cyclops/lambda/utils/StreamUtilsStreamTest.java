package com.aol.cyclops.lambda.utils;


import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.assertThat;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;

import com.aol.cyclops.streams.StreamUtils;
public class StreamUtilsStreamTest {
	 public static <U> Stream<U> of(U... array){
		 return Stream.of(array);
	 }

	
	@Test
	public void testNoneMatch(){
		assertThat(StreamUtils.noneMatch(of(1,2,3,4,5),it-> it==5000),equalTo(true));
	}
	
	
	
	@Test
	public void testFlatMap(){
		assertThat(StreamUtils.flatMapStream(Stream.of( asList("1","10"), asList("2"),asList("3"),asList("4")), list -> list.stream() ).collect(Collectors.toList() 
						),hasItem("10"));
	}
	
	
	
	
	
	

	
	

}