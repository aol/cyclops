package com.oath.cyclops.streams.anyM;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import cyclops.companion.Streams;
import org.junit.Test;

import cyclops.monads.AnyM;

public class LiftAndBindTest {
	@Test
	public void testLiftAndBindFile(){


		List<String> result = Streams.flatMapFile(AnyM.streamOf("input.file")
								.map(getClass().getClassLoader()::getResource)
								.peek(System.out::println)
								.map(URL::getFile)
								.stream(),
								 File::new)
								.collect(Collectors.toList());

		assertThat(result,equalTo(Arrays.asList("hello","world")));
	}
	@Test
	public void testLiftAndBindURL(){


		List<String> result = Streams.flatMapURL(AnyM.streamOf("input.file")
								.stream()
								,getClass().getClassLoader()::getResource)
								.collect(Collectors.toList());

		assertThat(result,equalTo(Arrays.asList("hello","world")));
	}
	@Test
	public void testLiftAndBindString(){


		List<Character> result = Streams.flatMapCharSequence(AnyM.streamOf("input.file")
								.stream()
								,i->"hello world")
								.collect(Collectors.toList());

		assertThat(result,equalTo(Arrays.asList('h','e','l','l','o',' ','w','o','r','l','d')));
	}
	@Test
	public void testLiftAndBindBufferedReader(){


		List<String> result = Streams.flatMapBufferedReader(AnyM.streamOf("input.file")
								.map(getClass().getClassLoader()::getResourceAsStream)
								.map(InputStreamReader::new)
								.stream(),
								BufferedReader::new)
								.collect(Collectors.toList());


		assertThat(result,equalTo(Arrays.asList("hello","world")));
	}
}
