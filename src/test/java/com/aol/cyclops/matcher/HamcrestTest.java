package com.aol.cyclops.matcher;

import static com.aol.cyclops.matcher.Extractors._;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;

public class HamcrestTest {

	@Test
	public void hamcrest1(){
		Matching.caseOf(hasItem("hello2"),System.out::println)
							.match(Arrays.asList("hello","world"));
	}
	@Test
	public void hasAnyOrderTest(){
		assertTrue(Matching.inCaseOf(containsInAnyOrder("world","hello"),list -> true)
							.apply(Arrays.asList("hello","world")));
	}
	@Test
	public void hamcrestWithExtractor(){
		assertTrue(Matching.inCaseOf(Extractors._(1),is("world"),list -> true)
							.apply(Arrays.asList("hello","world")));
	}
	@Test
	public void hamcrestWithPostExtractor(){
		assertEquals("world",Matching.inCaseOfThenExtract(containsInAnyOrder("world","hello"),value -> value
									,Extractors._(1))
							.apply(Arrays.asList("hello","world")));
	}
	@Test
	public void hamcrestWithPostExtractor2(){
		assertEquals("world",new PatternMatcher().inCaseOfThenExtract(containsInAnyOrder("world","hello"),value -> value
									,Extractors.<String>_(1))
							.apply(Arrays.asList("hello","world")));
	}
}
