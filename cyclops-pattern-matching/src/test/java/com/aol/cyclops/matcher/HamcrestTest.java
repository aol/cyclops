package com.aol.cyclops.matcher;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.hamcrest.Matcher;
import org.junit.Test;

public class HamcrestTest {

	@Test
	public void hamcrest1(){
		Matching.matchOf(hasItem("hello2"),System.out::println)
							.match(Arrays.asList("hello","world"));
	}
	@Test
	public void hasAnyOrderTest(){
		assertTrue(Matching.inMatchOf(containsInAnyOrder("world","hello"),list -> true)
							.apply(Arrays.asList("hello","world")).get());
	}
	@Test
	public void hamcrestWithExtractor(){
		assertTrue(Matching.inMatchOf(Extractors.at(1),is("world"),list -> true)
							.apply(Arrays.asList("hello","world")).get());
	}
	@Test
	public void hamcrestWithPostExtractor(){
		assertEquals("world",Matching.inMatchOfThenExtract((Matcher)containsInAnyOrder("world","hello"),value -> value
									,Extractors.at(1))
							.apply(Arrays.asList("hello","world")).get());
	}
	@Test
	public void hamcrestWithPostExtractor2(){
		assertEquals("world",new PatternMatcher().<String,List<String>,String>inMatchOfThenExtract(
				(Matcher)containsInAnyOrder("world","hello"),
										(String value) -> value
									,(List<String> list)-> list.get(1))
							.apply(Arrays.asList("hello","world")).get());
	}
}
