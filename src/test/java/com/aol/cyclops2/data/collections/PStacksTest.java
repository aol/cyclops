package com.aol.cyclops2.data.collections;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.stream.Stream;

import org.junit.Test;

import cyclops.companion.Reducers;
import cyclops.stream.ReactiveSeq;
import cyclops.collections.immutable.PStackX;

public class PStacksTest {

	@Test
	public void testOf() {
		assertThat(PStackX.of("a","b","c"),equalTo(Arrays.asList("a","b","c")));
	}

	@Test
	public void testEmpty() {
		assertThat(PStackX.empty(),equalTo(Arrays.asList()));
	}

	@Test
	public void testSingleton() {
		assertThat(PStackX.of("a"),equalTo(Arrays.asList("a")));
	}
	@Test
	public void testFromCollection() {
		assertThat(PStackX.fromCollection(Arrays.asList("a","b","c")),equalTo(Arrays.asList("a","b","c")));
	}
	@Test
	public void testToPStackstreamOfTReveresed() {
		assertThat(PStackX.fromStream(Stream.of("a","b","c")),
						equalTo(Arrays.asList("c","b","a")));
	}

	@Test
	public void testToPStackReversed() {
		assertThat(ReactiveSeq.of("a","b","c").mapReduce(Reducers.toPStackReversed()),
				equalTo(Arrays.asList("c","b","a")));
	}
	@Test
	public void testToPStackstreamOf() {
		assertThat(PStackX.fromStream(Stream.of("a","b","c")),
						equalTo(Arrays.asList("c","b","a")));
	}

	@Test
	public void testToPStack() {
		assertThat(ReactiveSeq.of("a","b","c").mapReduce(Reducers.toPStack()),
				equalTo(Arrays.asList("a","b","c")));
	}
	

}
