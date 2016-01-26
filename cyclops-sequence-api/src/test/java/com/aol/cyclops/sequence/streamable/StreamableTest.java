package com.aol.cyclops.sequence.streamable;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class StreamableTest {
	@Test
	public void testContains() {
		assertThat(Streamable.of(1, 2, 3, 4, 5).contains(3), equalTo(true));
		assertThat(Streamable.of(1, 2, 3, 4, 5).contains(6), equalTo(false));
	}
	
	@Test
	public void testParallelContains() {
		assertThat(Streamable.of(1, 2, 3, 4, 5).parallelContains(3), equalTo(true));
		assertThat(Streamable.of(1, 2, 3, 4, 5).parallelContains(6), equalTo(false));
	}
}
