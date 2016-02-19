package com.aol.cyclops.functions.collections;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;

import com.aol.cyclops.data.collections.PQueues;
import com.aol.cyclops.control.ReactiveSeq;

public class PQueuesTest {

	@Test
	public void testOf() {
		assertThat(PQueues.of("a","b","c")
							.stream()
							.collect(Collectors.toList()),equalTo(Arrays.asList("a","b","c")));
	}

	@Test
	public void testEmpty() {
		assertThat(PQueues.empty().stream()
				.collect(Collectors.toList()),equalTo(Arrays.asList()));
	}

	@Test
	public void testSingleton() {
		assertThat(PQueues.of("a").stream()
				.collect(Collectors.toList()),equalTo(Arrays.asList("a")));
	}
	@Test
	public void testFromCollection() {
		assertThat(PQueues.fromCollection(Arrays.asList("a","b","c")).stream()
				.collect(Collectors.toList()),equalTo(Arrays.asList("a","b","c")));
	}
	@Test
	public void testToPStackstreamOfT() {
		assertThat(PQueues.fromStream(Stream.of("a","b","c")).stream()
				.collect(Collectors.toList()),
						equalTo(Arrays.asList("a","b","c")));
	}

	@Test
	public void testToPStack() {
		assertThat(ReactiveSeq.of("a","b","c").mapReduce(PQueues.toPQueue()).stream()
				.collect(Collectors.toList()),
				equalTo(Arrays.asList("a","b","c")));
	}
	

}
