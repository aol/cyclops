package com.aol.cyclops.streams;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.util.stream.StreamUtils;

public class LiftAndBindSequenceMTest {
	@Test
	public void testLiftAndBindFile(){
		
		
		List<String> result = AnyM.streamOf("input.file")
								.asSequence()
								.map(getClass().getClassLoader()::getResource)
								.peek(System.out::println)
								.map(URL::getFile)
								.map(File::new)
								.<String>flatMapAnyM(AnyM::ofConvertable)
								.toList();
		
		assertThat(result,equalTo(Arrays.asList("hello","world")));
	}
	@Test
	public void testLiftAndBindURL(){
		
		
		List<String> result = AnyM.streamOf("input.file")
								.asSequence()
								.map(getClass().getClassLoader()::getResource)
								.<String>flatMapAnyM(AnyM::ofConvertable)
								.toList();
		
		assertThat(result,equalTo(Arrays.asList("hello","world")));
	}
	@Test
	public void testLiftAndBindString(){
		
		
		List<Character> result = StreamUtils.flatMapCharSequence(AnyM.streamOf("input.file")
									.asSequence(),i->"hello world")
									.collect(Collectors.toList());
		
		assertThat(result,equalTo(Arrays.asList('h','e','l','l','o',' ','w','o','r','l','d')));
	}
	@Test
	public void testLiftAndBindBufferedReader(){
		
		
		List<String> result = StreamUtils.flatMapBufferedReader(AnyM.streamOf("input.file")
								.asSequence()
								.map(getClass().getClassLoader()::getResourceAsStream)
								.map(InputStreamReader::new)
								,r-> new BufferedReader(r))
								.collect(Collectors.toList());
		
		assertThat(result,equalTo(Arrays.asList("hello","world")));
	}
}
