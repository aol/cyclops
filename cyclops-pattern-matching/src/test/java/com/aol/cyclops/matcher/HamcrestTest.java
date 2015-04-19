package com.aol.cyclops.matcher;

import static com.aol.cyclops.matcher.Extractors.get;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.hamcrest.Matcher;
import org.junit.Test;

import com.aol.cyclops.matcher.builders.Matching;

public class HamcrestTest {

	@Test
	public void hamcrest1(){
		Matching.newCase(c->c.isMatch(hasItem("hello2")).thenConsume(System.out::println))
							.match(Arrays.asList("hello","world"));
	}
	@Test
	public void hamcrestViaNew(){
		Matching.newCase().isMatch(hasItem("hello2")).thenConsume(System.out::println)
							.match(Arrays.asList("hello","world"));
	}
	@Test
	public void hasAnyOrderTest(){
		assertTrue(Matching.<Boolean>newCase(c->c.isMatch(containsInAnyOrder("world","hello"))
											.thenApply(list -> true))
							.apply(Arrays.asList("hello","world")).get());
	}
	@Test
	public void hamcrestWithExtractor(){
		assertTrue(Matching.newCase().extract(Extractors.at(1))
										.isMatch(is("world"))
										.thenApply(list -> true)
							.match(Arrays.asList("hello","world")).get());
	}
	@Test
	public void postExtractorDoc(){
		Matching.newCase().isMatch(is(not(empty()))).thenExtract(get(1))
					.thenConsume(System.out::println)
				.match(Arrays.asList(20303,"20303 is passing",true));
	}
	@Test
	public void hamcrestWithPostExtractor(){
		assertEquals("world",Matching.newCase(c->c.isMatch(containsInAnyOrder("world","hello"))
													.thenExtract(Extractors.at(1))
													.thenApply(value -> value))
					
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
