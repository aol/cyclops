package com.oath.cyclops.react.lazy.sequence;


import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import com.oath.cyclops.react.lazy.DuplicationTest;
import org.junit.Test;

import cyclops.companion.Reducers;

public class ScanningTest {
	@Test
	public void testScanLeftStringConcat() {

		assertThat(DuplicationTest.of("a", "b", "c").scanLeft("", String::concat).toList(), is(asList("", "a", "ab", "abc")));
	}

	@Test
	public void testScanLeftSum() {
		assertThat(DuplicationTest.of("a", "ab", "abc").map(str -> str.length()).scanLeft(0, (u, t) -> u + t).toList(), is(asList(0, 1, 3, 6)));
	}

	@Test
	public void testScanLeftStringConcatMonoid() {
		assertThat(DuplicationTest.of("a", "b", "c").scanLeft(Reducers.toString("")).toList(), is(asList("", "a", "ab", "abc")));
	}

	@Test
	public void testScanLeftSumMonoid() {
		assertThat(DuplicationTest.of("a", "ab", "abc").map(str -> str.length()).scanLeft(Reducers.toTotalInt()).toList(), is(asList(0, 1, 3, 6)));
	}

	@Test
	public void testScanRightStringConcat() {
		assertThat(DuplicationTest.of("a", "b", "c").scanRight("", String::concat).toList(), is(asList("", "c", "bc", "abc")));
	}

	@Test
	public void testScanRightSum() {
		assertThat(DuplicationTest.of("a", "ab", "abc").map(str -> str.length()).scanRight(0, (t, u) -> u + t).toList(), is(asList(0, 3, 5, 6)));

	}
	@Test
	public void testScanRightStringConcatMonoid() {
		assertThat(DuplicationTest.of("a", "b", "c").scanRight(Reducers.toString("")).toList(), is(asList("", "c", "bc", "abc")));
	}

	@Test
	public void testScanRightSumMonoid() {
		assertThat(DuplicationTest.of("a", "ab", "abc").map(str -> str.length()).scanRight(Reducers.toTotalInt()).toList(), is(asList(0, 3, 5, 6)));

	}
}
