package com.aol.cyclops.matcher;

import static com.aol.cyclops.matcher.Extractors.get;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.hamcrest.Matcher;
import org.junit.Test;

import com.aol.cyclops.matcher.builders.Matching;
import com.aol.cyclops.matcher.builders.PatternMatcher;

public class HamcrestTest {

	@Test
	public void hamcrest1() {
		Matching.whenValues(c -> c.isMatch(hasItem("hello2")).thenConsume(System.out::println)).match(Arrays.asList("hello", "world"));
	}

	@Test
	public void multipleHamcrestMatchers() {
		assertThat(
				Matching.when().isMatch(containsString("hello"), containsString("world")).thenApply(message -> message).match("hello world").get(),
				is("hello world"));

		assertThat(Matching.when().isMatch(containsString("hello"), containsString("universe")).thenApply(message -> message).match("hello world"),
				is(Optional.empty()));
	}

	@Test
	public void hamcrestViaNew() {
		Matching.when().isMatch(hasItem("hello2")).thenConsume(System.out::println).when().isMatch(contains("world", "hello", "unexpected"))
				.thenConsume(System.err::println).match(Arrays.asList("hello", "world"));
	}

	@Test
	public void hasAnyOrderTest() {
		assertTrue(Matching.<Boolean> whenValues(c -> c.isMatch(containsInAnyOrder("world", "hello")).thenApply(list -> true))
				.apply(Arrays.asList("hello", "world")).get());
	}

	@Test
	public void hamcrestWithExtractor() {
		assertTrue(Matching.when().extract(Extractors.at(1)).isMatch(is("world")).thenApply(list -> true).match(Arrays.asList("hello", "world"))
				.get());
	}

	@Test
	public void postExtractorDoc() {
		Matching.when().isMatch(is(not(empty()))).thenExtract(get(1)).thenConsume(System.out::println)
				.match(Arrays.asList(20303, "20303 is passing", true));
	}

	@Test
	public void hamcrestWithPostExtractor() {
		assertEquals("world",
				Matching.whenValues(c -> c.isMatch(containsInAnyOrder("world", "hello")).thenExtract(Extractors.at(1)).thenApply(value -> value))

				.apply(Arrays.asList("hello", "world")).get());
	}

	@Test
	public void hamcrestWithPostExtractor2() {
		assertEquals(
				"world",
				new PatternMatcher()
						.<String, List<String>, String> inMatchOfThenExtract((Matcher) containsInAnyOrder("world", "hello"), (String value) -> value,
								(List<String> list) -> list.get(1)).apply(Arrays.asList("hello", "world")).get());
	}
}
