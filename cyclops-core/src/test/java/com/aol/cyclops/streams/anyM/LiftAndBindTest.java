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

import com.aol.cyclops.monad.AnyM;

public class LiftAndBindTest {
	@Test
	public void testLiftAndBindFile(){
		
		
		List<String> result = AnyM.streamOf("input.file")
								.map(getClass().getClassLoader()::getResource)
								.peek(System.out::println)
								.map(URL::getFile)
								.flatMapFile(File::new)
								.asSequence()
								.toList();
		
		assertThat(result,equalTo(Arrays.asList("hello","world")));
	}
	@Test
	public void testLiftAndBindURL(){
		
		
		List<String> result = AnyM.streamOf("input.file")
								.flatMapURL(getClass().getClassLoader()::getResource)
								.asSequence()
								.toList();
		
		assertThat(result,equalTo(Arrays.asList("hello","world")));
	}
	@Test
	public void testLiftAndBindString(){
		
		
		List<Character> result = AnyM.streamOf("input.file")
								.flatMapCharSequence(i->"hello world")
								.asSequence()
								.toList();
		
		assertThat(result,equalTo(Arrays.asList('h','e','l','l','o',' ','w','o','r','l','d')));
	}
	@Test
	public void testLiftAndBindBufferedReader(){
		
		
		List<String> result = AnyM.streamOf("input.file")
								.map(getClass().getClassLoader()::getResourceAsStream)
								.map(InputStreamReader::new)
								.flatMapBufferedReader(BufferedReader::new)
								.asSequence()
								.toList();
		
		assertThat(result,equalTo(Arrays.asList("hello","world")));
	}
}
