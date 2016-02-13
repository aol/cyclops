package com.aol.cyclops.streams.anyM;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.aol.cyclops.control.AnyM;

public class LiftAndBindTest {
	@Test
	public void testLiftAndBindFile(){
		
	
		List<String> result = AnyM.streamOf("input.file")
								.map(getClass().getClassLoader()::getResource)
								.peek(System.out::println)
								.map(URL::getFile)
								.asSequence()
								.flatMapFile(File::new)
								.toList();
		
		assertThat(result,equalTo(Arrays.asList("hello","world")));
	}
	@Test
	public void testLiftAndBindURL(){
		
		
		List<String> result = AnyM.streamOf("input.file")
								.asSequence()
								.flatMapURL(getClass().getClassLoader()::getResource)
								.toList();
		
		assertThat(result,equalTo(Arrays.asList("hello","world")));
	}
	@Test
	public void testLiftAndBindString(){
		
		
		List<Character> result = AnyM.streamOf("input.file")
								.asSequence()
								.flatMapCharSequence(i->"hello world")
								.toList();
		
		assertThat(result,equalTo(Arrays.asList('h','e','l','l','o',' ','w','o','r','l','d')));
	}
	@Test
	public void testLiftAndBindBufferedReader(){
		
		
		List<String> result = AnyM.streamOf("input.file")
								.map(getClass().getClassLoader()::getResourceAsStream)
								.map(InputStreamReader::new)
								.asSequence()
								.flatMapBufferedReader(BufferedReader::new)
								
								.toList();
		
		assertThat(result,equalTo(Arrays.asList("hello","world")));
	}
}
