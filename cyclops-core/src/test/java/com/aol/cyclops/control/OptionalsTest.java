package com.aol.cyclops.control;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Optional;

import org.junit.Test;

import com.aol.cyclops.collections.extensions.standard.ListX;

public class OptionalsTest {
	@Test
	public void testSequence() {
		Optional<ListX<Integer>> maybes =Optionals.sequence(ListX.of(Optional.of(10),Optional.empty(),Optional.of(1)));
		assertThat(maybes,equalTo(Optional.of(ListX.of(10,1))));
	}
}
