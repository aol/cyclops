package com.aol.cyclops.data.collections;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;

import cyclops.Reducers;
import cyclops.stream.ReactiveSeq;
import cyclops.collections.immutable.PQueueX;

public class PQueuesTest {

	@Test
	public void testOf() {
		assertThat(PQueueX.of("a","b","c")
							.stream()
							.collect(Collectors.toList()),equalTo(Arrays.asList("a","b","c")));
	}

	@Test
	public void testEmpty() {
		assertThat(PQueueX.empty().stream()
				.collect(Collectors.toList()),equalTo(Arrays.asList()));
	}

	@Test
	public void testSingleton() {
		assertThat(PQueueX.of("a").stream()
				.collect(Collectors.toList()),equalTo(Arrays.asList("a")));
	}
	@Test
	public void testFromCollection() {
		assertThat(PQueueX.fromCollection(Arrays.asList("a","b","c")).stream()
				.collect(Collectors.toList()),equalTo(Arrays.asList("a","b","c")));
	}
	@Test
	public void testToPStackstreamOfT() {
		assertThat(PQueueX.fromStream(Stream.of("a","b","c")).stream()
				.collect(Collectors.toList()),
						equalTo(Arrays.asList("a","b","c")));
	}

	@Test
	public void testToPStack() {
		assertThat(ReactiveSeq.of("a","b","c").mapReduce(Reducers.toPQueue()).stream()
				.collect(Collectors.toList()),
				equalTo(Arrays.asList("a","b","c")));
	}
	

}
