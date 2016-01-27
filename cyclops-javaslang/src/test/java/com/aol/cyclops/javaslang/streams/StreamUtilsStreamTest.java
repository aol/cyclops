package com.aol.cyclops.javaslang.streams;


import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.assertThat;
import javaslang.collection.LazyStream;

import org.junit.Test;


public class StreamUtilsStreamTest {

	 public static <U> LazyStream<U> of(U... array){
		 return LazyStream.of(array);
	 }

	
	@Test
	public void testNoneMatch(){
		assertThat(StreamUtils.noneMatch(of(1,2,3,4,5),it-> it==5000),equalTo(true));
	}
	
	
	
	@Test
	public void testFlatMap(){

		assertThat(StreamUtils.flatMapStream(LazyStream.of( asList("1","10"), asList("2"),asList("3"),asList("4")), list -> list.stream() ).toJavaList(),hasItem("10"));

	}
	
	
	
	
	
	

	
	

}