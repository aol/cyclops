package com.oath.cyclops.streams;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import cyclops.reactive.ReactiveSeq;
import cyclops.collections.mutable.ListX;
import com.oath.cyclops.types.stream.HeadAndTail;

public class HeadTailTest {



	@Test
	public void empty(){

		assertFalse(ListX.empty().headAndTail().headMaybe().isPresent());
		assertFalse(ListX.empty().headAndTail().headOptional().isPresent());
		assertTrue(ListX.empty().headAndTail().headStream().size()==0);


	}
}
