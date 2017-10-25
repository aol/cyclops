package com.aol.cyclops2.data.collections;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.stream.Collectors;

import cyclops.collections.immutable.OrderedSetX;
import org.junit.Test;

import cyclops.companion.Reducers;
import cyclops.reactive.ReactiveSeq;

public class POrderedSetsTest {

	@Test
	public void testOf() {
		assertThat(OrderedSetX.of("a","b","c")
							.stream()
							.collect(Collectors.toList()),hasItems("a","b","c"));
	}

	@Test
	public void testEmpty() {
		assertThat(OrderedSetX.empty().stream()
				.collect(Collectors.toList()),equalTo(Arrays.asList()));
	}

	@Test
	public void testSingleton() {
		assertThat(OrderedSetX.of("a").stream()
				.collect(Collectors.toList()),equalTo(Arrays.asList("a")));
	}
	@Test
	public void testFromCollection() {
		assertThat(OrderedSetX.fromIterable(Arrays.asList("a","b","c")).stream()
				.collect(Collectors.toList()),hasItems("a","b","c"));
	}
	@Test
	public void testToPOrderedSetstreamOfT() {
		assertThat(OrderedSetX.orderedSetX(ReactiveSeq.of("a","b","c")).stream()
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
