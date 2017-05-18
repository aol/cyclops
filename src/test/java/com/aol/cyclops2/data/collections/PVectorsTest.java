package com.aol.cyclops2.data.collections;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.stream.Stream;

import cyclops.collections.immutable.VectorX;
import org.junit.Test;

import cyclops.companion.Reducers;
import cyclops.stream.ReactiveSeq;

public class PVectorsTest {

	@Test
	public void testOf() {
		assertThat(VectorX.of("a","b","c"),equalTo(Arrays.asList("a","b","c")));
	}

	@Test
	public void testEmpty() {
		assertThat(VectorX.empty(),equalTo(Arrays.asList()));
	}

	@Test
	public void testSingleton() {
		assertThat(VectorX.of("a"),equalTo(Arrays.asList("a")));
	}
	@Test
	public void testFromCollection() {
		assertThat(VectorX.fromCollection(Arrays.asList("a","b","c")),equalTo(Arrays.asList("a","b","c")));
	}
	@Test
	public void testToPVectorStreamOfT() {
		assertThat(VectorX.fromStream(Stream.of("a","b","c")),
						equalTo(Arrays.asList("a","b","c")));
	}

	@Test
	public void testToPVector() {
		assertThat(ReactiveSeq.of("a","b","c").mapReduce(Reducers.toPVector()),
				equalTo(Arrays.asList("a","b","c")));
	}

}
