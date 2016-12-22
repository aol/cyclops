package com.aol.cyclops.data.collections;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.stream.Stream;

import org.junit.Test;

import cyclops.Reducers;
import cyclops.stream.ReactiveSeq;
import cyclops.collections.immutable.PVectorX;

public class PVectorsTest {

	@Test
	public void testOf() {
		assertThat(PVectorX.of("a","b","c"),equalTo(Arrays.asList("a","b","c")));
	}

	@Test
	public void testEmpty() {
		assertThat(PVectorX.empty(),equalTo(Arrays.asList()));
	}

	@Test
	public void testSingleton() {
		assertThat(PVectorX.of("a"),equalTo(Arrays.asList("a")));
	}
	@Test
	public void testFromCollection() {
		assertThat(PVectorX.fromCollection(Arrays.asList("a","b","c")),equalTo(Arrays.asList("a","b","c")));
	}
	@Test
	public void testToPVectorStreamOfT() {
		assertThat(PVectorX.fromStream(Stream.of("a","b","c")),
						equalTo(Arrays.asList("a","b","c")));
	}

	@Test
	public void testToPVector() {
		assertThat(ReactiveSeq.of("a","b","c").mapReduce(Reducers.toPVector()),
				equalTo(Arrays.asList("a","b","c")));
	}

}
