package com.aol.cyclops2.data.collections;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.stream.Stream;

import org.junit.Test;

import cyclops.companion.Reducers;
import cyclops.stream.ReactiveSeq;
import cyclops.collections.immutable.LinkedListX;

public class PStacksTest {

	@Test
	public void testOf() {
		assertThat(LinkedListX.of("a","b","c"),equalTo(Arrays.asList("a","b","c")));
	}

	@Test
	public void testEmpty() {
		assertThat(LinkedListX.empty(),equalTo(Arrays.asList()));
	}

	@Test
	public void testSingleton() {
		assertThat(LinkedListX.of("a"),equalTo(Arrays.asList("a")));
	}
	@Test
	public void testFromCollection() {
		assertThat(LinkedListX.fromIterable(Arrays.asList("a","b","c")),equalTo(Arrays.asList("a","b","c")));
	}
	@Test
	public void testToPStackstreamOfTReveresed() {
		assertThat(LinkedListX.linkedListX(ReactiveSeq.of("a","b","c")),
						equalTo(Arrays.asList("c","b","a")));
	}

	@Test
	public void testToPStackReversed() {
		assertThat(ReactiveSeq.of("a","b","c").mapReduce(Reducers.toPStackReversed()),
				equalTo(Arrays.asList("c","b","a")));
	}
	@Test
	public void testToPStackstreamOf() {
		assertThat(LinkedListX.linkedListX(ReactiveSeq.of("a","b","c")),
						equalTo(Arrays.asList("c","b","a")));
	}

	@Test
	public void testToPStack() {
		assertThat(ReactiveSeq.of("a","b","c").mapReduce(Reducers.toPStack()),
				equalTo(Arrays.asList("a","b","c")));
	}
	

}
