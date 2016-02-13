package com.aol.cyclops.guava;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Arrays;

import org.junit.Test;

import com.aol.cyclops.control.AnyM;
import com.google.common.base.Optional;
import com.google.common.collect.FluentIterable;

public class AnyGuavaMTest {

	

	private String success(){
		return "hello world";
		
	}
	private String exceptional(){
		
		throw new RuntimeException();
	}
	
	
	@Test
	public void optionalTest(){
		assertThat(Guava.anyM(Optional.of("hello world"))
				.map(String::toUpperCase)
				.toSequence()
				.toList(),equalTo(Arrays.asList("HELLO WORLD")));
	}
	@Test
	public void optionFlatMapTest(){
		assertThat(Guava.anyM(Optional.of("hello world"))
				.map(String::toUpperCase)
				.toSequence()
				.flatMapOptional(java.util.Optional::of)
				.toList(),equalTo(Arrays.asList("HELLO WORLD")));
	}
	@Test
	public void optionEmptyTest(){
		assertThat(Guava.anyM(Optional.<String>absent())
				.map(String::toUpperCase)
				.toSequence()
				.toList(),equalTo(Arrays.asList()));
	}
	@Test
	public void streamTest(){
		assertThat(Guava.anyM(FluentIterable.of(new String[]{"hello world"}))
				.map(String::toUpperCase)
				.toSequence()
				.toList(),equalTo(Arrays.asList("HELLO WORLD")));
	}
	
	@Test
	public void streamFlatMapTestJDK(){
		assertThat(Guava.anyM(FluentIterable.of(new String[]{"hello world"}))
				.map(String::toUpperCase)
				.flatMap(i->AnyM.fromStream(java.util.stream.Stream.of(i)))
				.toSequence()
				.toList(),equalTo(Arrays.asList("HELLO WORLD")));
	}
}
