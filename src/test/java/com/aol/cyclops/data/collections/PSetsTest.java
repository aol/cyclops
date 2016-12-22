package com.aol.cyclops.data.collections;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;

import cyclops.Reducers;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.data.collections.extensions.persistent.PSetX;

public class PSetsTest {

	@Test
	public void testOf() {
		assertThat(PSetX.of("a","b","c")
							.stream()
							.collect(Collectors.toList()),hasItems("a","b","c"));
	}

	@Test
	public void testEmpty() {
		assertThat(PSetX.empty().stream()
				.collect(Collectors.toList()),equalTo(Arrays.asList()));
	}

	@Test
	public void testSingleton() {
		assertThat(PSetX.of("a").stream()
				.collect(Collectors.toList()),equalTo(Arrays.asList("a")));
	}
	@Test
	public void testFromCollection() {
		assertThat(PSetX.fromCollection(Arrays.asList("a","b","c")).stream()
				.collect(Collectors.toList()),hasItems("a","b","c"));
	}
	@Test
	public void testToPSetstreamOfT() {
		assertThat(PSetX.fromStream(Stream.of("a","b","c")).stream()
				.collect(Collectors.toList()),
						hasItems("a","b","c"));
	}

	@Test
	public void testToPSets() {
		assertThat(ReactiveSeq.of("a","b","c").mapReduce(Reducers.toPSet()).stream()
				.collect(Collectors.toList()),
				hasItems("a","b","c"));
	}
	

}
