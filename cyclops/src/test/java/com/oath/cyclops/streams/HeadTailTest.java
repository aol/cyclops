package com.oath.cyclops.streams;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import cyclops.reactive.collections.mutable.ListX;

public class HeadTailTest {



	@Test
	public void empty(){

		assertFalse(ListX.empty().headAndTail().headMaybe().isPresent());
		assertFalse(ListX.empty().headAndTail().headOptional().isPresent());
		assertTrue(ListX.empty().headAndTail().headStream().size()==0);


	}
}
