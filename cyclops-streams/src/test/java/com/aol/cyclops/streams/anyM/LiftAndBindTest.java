package com.aol.cyclops.streams.anyM;

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

public class LiftAndBindTest {
	@Test
	public void testLiftAndBindFile(){
		
		
		List<String> result = anyM("input.file")
								.map(getClass().getClassLoader()::getResource)
								.peek(System.out::println)
								.map(URL::getFile)
								.liftAndBindFile(File::new)
								.asSequence()
								.toList();
		
		assertThat(result,equalTo(Arrays.asList("hello","world")));
	}
	@Test
	public void testLiftAndBindURL(){
		
		
		List<String> result = anyM("input.file")
								.liftAndBindURL(getClass().getClassLoader()::getResource)
								.asSequence()
								.toList();
		
		assertThat(result,equalTo(Arrays.asList("hello","world")));
	}
	@Test
	public void testLiftAndBindString(){
		
		
		List<Character> result = anyM("input.file")
								.liftAndBindCharSequence(i->"hello world")
								.asSequence()
								.toList();
		
		assertThat(result,equalTo(Arrays.asList('h','e','l','l','o',' ','w','o','r','l','d')));
	}
	@Test
	public void testLiftAndBindBufferedReader(){
		
		
		List<String> result = anyM("input.file")
								.map(getClass().getClassLoader()::getResourceAsStream)
								.map(InputStreamReader::new)
								.liftAndBindBufferedReader(BufferedReader::new)
								.asSequence()
								.toList();
		
		assertThat(result,equalTo(Arrays.asList("hello","world")));
	}
}
