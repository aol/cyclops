package com.aol.cyclops.functions.collections;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;

import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.data.collections.POrderedSets;

public class POrderedSetsTest {

	@Test
	public void testOf() {
		assertThat(POrderedSets.of("a","b","c")
							.stream()
							.collect(Collectors.toList()),hasItems("a","b","c"));
	}

	@Test
	public void testEmpty() {
		assertThat(POrderedSets.empty().stream()
				.collect(Collectors.toList()),equalTo(Arrays.asList()));
	}

	@Test
	public void testSingleton() {
		assertThat(POrderedSets.of("a").stream()
				.collect(Collectors.toList()),equalTo(Arrays.asList("a")));
	}
	@Test
	public void testFromCollection() {
		assertThat(POrderedSets.fromCollection(Arrays.asList("a","b","c")).stream()
				.collect(Collectors.toList()),hasItems("a","b","c"));
	}
	@Test
	public void testToPOrderedSetstreamOfT() {
		assertThat(POrderedSets.fromStream(Stream.of("a","b","c")).stream()
				.collect(Collectors.toList()),
						hasItems("a","b","c"));
	}

	@Test
	public void testToPOrderedSets() {
		assertThat(ReactiveSeq.of("a","b","c").mapReduce(POrderedSets.toPOrderedSet()).stream()
				.collect(Collectors.toList()),
				hasItems("a","b","c"));
	}
	

}
