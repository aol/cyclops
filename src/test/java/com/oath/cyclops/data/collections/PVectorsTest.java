package com.oath.cyclops.data.collections;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Arrays;

import cyclops.collections.immutable.VectorX;
import cyclops.data.Vector;
import org.junit.Test;

import cyclops.companion.Reducers;
import cyclops.reactive.ReactiveSeq;

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
		assertThat(VectorX.fromIterable(Arrays.asList("a","b","c")),equalTo(Arrays.asList("a","b","c")));
	}
	@Test
	public void testToPVectorStreamOfT() {
		assertThat(VectorX.vectorX(ReactiveSeq.of("a","b","c")),
						equalTo(Arrays.asList("a","b","c")));
	}

	@Test
	public void testToPVector() {
		assertThat(ReactiveSeq.of("a","b","c").mapReduce(Reducers.toPVector()),
				equalTo(Vector.of("a","b","c")));
	}

}
