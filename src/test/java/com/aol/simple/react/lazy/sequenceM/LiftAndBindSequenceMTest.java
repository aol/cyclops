package com.aol.simple.react.lazy.sequenceM;

import static com.aol.cyclops.lambda.api.AsAnyM.*;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.junit.Test;

import com.aol.simple.react.stream.traits.LazyFutureStream;

public class LiftAndBindSequenceMTest {
	@Test
	public void testLiftAndBindFile(){
		
		
		List<String> result = LazyFutureStream.of("input.file")
								.map(getClass().getClassLoader()::getResource)
								.peek(System.out::println)
								.map(URL::getFile)
								.flatMapFile(File::new)
								.toList();
		
		assertThat(result,equalTo(Arrays.asList("hello","world")));
	}
	@Test
	public void testLiftAndBindURL(){
		
		
		List<String> result = LazyFutureStream.of("input.file")
								.flatMapURL(getClass().getClassLoader()::getResource)
								.toList();
		
		assertThat(result,equalTo(Arrays.asList("hello","world")));
	}
	@Test
	public void testLiftAndBindString(){
		
		
		List<Character> result = LazyFutureStream.of("input.file")
									.flatMapCharSequence(i->"hello world")
									.toList();
		
		assertThat(result,equalTo(Arrays.asList('h','e','l','l','o',' ','w','o','r','l','d')));
	}
	@Test
	public void testLiftAndBindBufferedReader(){
		
		
		List<String> result = LazyFutureStream.of("input.file")
								.map(getClass().getClassLoader()::getResourceAsStream)
								.map(InputStreamReader::new)
								.flatMapBufferedReader(BufferedReader::new)
								.toList();
		
		assertThat(result,equalTo(Arrays.asList("hello","world")));
	}
}
