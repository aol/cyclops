package com.aol.cyclops.functions.collections;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.stream.Stream;

import org.junit.Test;

import com.aol.cyclops.collections.PStacks;
import com.aol.cyclops.sequence.SequenceM;

public class PStacksTest {

	@Test
	public void testOf() {
		assertThat(PStacks.of("a","b","c"),equalTo(Arrays.asList("a","b","c")));
	}

	@Test
	public void testEmpty() {
		assertThat(PStacks.empty(),equalTo(Arrays.asList()));
	}

	@Test
	public void testSingleton() {
		assertThat(PStacks.of("a"),equalTo(Arrays.asList("a")));
	}
	@Test
	public void testFromCollection() {
		assertThat(PStacks.fromCollection(Arrays.asList("a","b","c")),equalTo(Arrays.asList("a","b","c")));
	}
	@Test
	public void testToPStackstreamOfTReveresed() {
		assertThat(PStacks.fromStreamReversed(Stream.of("a","b","c")),
						equalTo(Arrays.asList("c","b","a")));
	}

	@Test
	public void testToPStackReversed() {
		assertThat(SequenceM.of("a","b","c").mapReduce(PStacks.toPStackReverse()),
				equalTo(Arrays.asList("c","b","a")));
	}
	@Test
	public void testToPStackstreamOf() {
		assertThat(PStacks.fromStream(Stream.of("a","b","c")),
						equalTo(Arrays.asList("a","b","c")));
	}

	@Test
	public void testToPStack() {
		assertThat(SequenceM.of("a","b","c").mapReduce(PStacks.toPStack()),
				equalTo(Arrays.asList("a","b","c")));
	}
	

}
