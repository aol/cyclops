package com.aol.cyclops.guava;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Arrays;

import org.junit.Test;

import com.aol.cyclops.guava.Guava;
import com.aol.cyclops.lambda.monads.AnyMonads;
import com.google.common.base.Optional;
import com.google.common.collect.FluentIterable;

import fj.data.Option;

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
				.flatMapOptional(java.util.Optional::of)
				.toSequence()
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
				.flatMap(i->AnyMonads.anyM(java.util.stream.Stream.of(i)))
				.toSequence()
				.toList(),equalTo(Arrays.asList("HELLO WORLD")));
	}
}
