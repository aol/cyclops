package com.aol.cyclops2.data.collections;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;

import cyclops.Reducers;
import cyclops.stream.ReactiveSeq;
import cyclops.collections.immutable.POrderedSetX;

public class POrderedSetsTest {

	@Test
	public void testOf() {
		assertThat(POrderedSetX.of("a","b","c")
							.stream()
							.collect(Collectors.toList()),hasItems("a","b","c"));
	}

	@Test
	public void testEmpty() {
		assertThat(POrderedSetX.empty().stream()
				.collect(Collectors.toList()),equalTo(Arrays.asList()));
	}

	@Test
	public void testSingleton() {
		assertThat(POrderedSetX.of("a").stream()
				.collect(Collectors.toList()),equalTo(Arrays.asList("a")));
	}
	@Test
	public void testFromCollection() {
		assertThat(POrderedSetX.fromCollection(Arrays.asList("a","b","c")).stream()
				.collect(Collectors.toList()),hasItems("a","b","c"));
	}
	@Test
	public void testToPOrderedSetstreamOfT() {
		assertThat(POrderedSetX.fromStream(Stream.of("a","b","c")).stream()
				.collect(Collectors.toList()),
						hasItems("a","b","c"));
	}

	@Test
	public void testToPOrderedSets() {
		assertThat(ReactiveSeq.of("a","b","c").mapReduce(Reducers.toPOrderedSet()).stream()
				.collect(Collectors.toList()),
				hasItems("a","b","c"));
	}
	

}
