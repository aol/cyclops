package com.aol.cyclops.lambda.tuple;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;
public class TuplesStreamTest {
	
	@Test
	public void streamOfStrings(){
		Date d  = new Date();
		List<String> result = Tuples.tuple("hello",d,20).asStreamOfStrings().collect(Collectors.toList());
		
		assertThat(Arrays.asList("hello",d.toString(),"20"),equalTo(result));
	}
	@Test
	public void streamOfStreamsNoStreamable(){
		Date d  = new Date();
		List<Stream> result = Tuples.tuple(100,d,20).asStreams().collect(Collectors.toList());
		
		assertThat(3,equalTo(result.size()));
	}
	@Test
	public void streamOfStreamsStringAndArray(){
		Date d  = new Date();
		List<Stream> result = Tuples.tuple("hello",Arrays.asList(d,20)).asStreams().collect(Collectors.toList());
		
		assertThat(2,equalTo(result.size()));
	}
	@Test
	public void streamOfStreamsStringAndArrayFlattened(){
		Date d  = new Date();
		List result = Tuples.tuple("hello",Arrays.asList(d,20)).asFlattenedStream().collect(Collectors.toList());
		
		assertThat(7,equalTo(result.size()));
	}
	
	@Test
	public void streamOfStreamsFileFlattened(){
		URL url =this.getClass().getClassLoader().getResource("input.file");
		File file = new File(url.getFile());
		List result = Tuples.tuple(file).asFlattenedStream().collect(Collectors.toList());
		
		assertThat(2,equalTo(result.size()));
	}
}
