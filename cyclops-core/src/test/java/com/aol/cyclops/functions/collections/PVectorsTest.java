package com.aol.cyclops.functions.collections;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.stream.Stream;

import org.junit.Test;

import com.aol.cyclops.data.collections.PVectors;
import com.aol.cyclops.control.SequenceM;

public class PVectorsTest {

	@Test
	public void testOf() {
		assertThat(PVectors.of("a","b","c"),equalTo(Arrays.asList("a","b","c")));
	}

	@Test
	public void testEmpty() {
		assertThat(PVectors.empty(),equalTo(Arrays.asList()));
	}

	@Test
	public void testSingleton() {
		assertThat(PVectors.of("a"),equalTo(Arrays.asList("a")));
	}
	@Test
	public void testFromCollection() {
		assertThat(PVectors.fromCollection(Arrays.asList("a","b","c")),equalTo(Arrays.asList("a","b","c")));
	}
	@Test
	public void testToPVectorStreamOfT() {
		assertThat(PVectors.fromStream(Stream.of("a","b","c")),
						equalTo(Arrays.asList("a","b","c")));
	}

	@Test
	public void testToPVector() {
		assertThat(SequenceM.of("a","b","c").mapReduce(PVectors.toPVector()),
				equalTo(Arrays.asList("a","b","c")));
	}

}
