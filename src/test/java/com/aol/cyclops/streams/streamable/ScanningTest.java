package com.aol.cyclops.streams.streamable;


import static com.aol.cyclops.util.stream.Streamable.of;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.aol.cyclops.Reducers;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.util.stream.Streamable;

public class ScanningTest {
	@Test
	public void testScanLeftStringConcat() {
		assertThat(of("a", "b", "c").scanLeft("", String::concat).toList(), is(asList("", "a", "ab", "abc")));
	}

	@Test
	public void testScanLeftSum() {
		assertThat(of("a", "ab", "abc").map(str -> str.length()).scanLeft(0, (u, t) -> u + t).toList(), is(asList(0, 1, 3, 6)));
	}

	@Test
	public void testScanLeftStringConcatMonoid() {
		assertThat(of("a", "b", "c").scanLeft(Reducers.toString("")).toList(), is(asList("", "a", "ab", "abc")));
	}

	@Test
	public void testScanLeftSumMonoid() {
		assertThat(of("a", "ab", "abc").map(str -> str.length()).scanLeft(Reducers.toTotalInt()).toList(), is(asList(0, 1, 3, 6)));
	}

	@Test
	public void testScanRightStringConcat() {
		assertThat(of("a", "b", "c").scanRight("", String::concat).toList(), is(asList("", "c", "bc", "abc")));
	}

	@Test
	public void testScanRightSum() {
		assertThat(of("a", "ab", "abc").map(str -> str.length()).scanRight(0, (t, u) -> u + t).toList(), is(asList(0, 3, 5, 6)));

	}
	@Test
	public void testScanRightStringConcatMonoid() {
		assertThat(of("a", "b", "c").scanRight(Reducers.toString("")).toList(), is(asList("", "c", "bc", "abc")));
	}

	@Test
	public void testScanRightSumMonoid() {
		assertThat(of("a", "ab", "abc").map(str -> str.length()).scanRight(Reducers.toTotalInt()).toList(), is(asList(0, 3, 5, 6)));

	}
}
