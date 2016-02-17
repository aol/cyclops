package com.aol.cyclops.functions.collections.extensions;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertTrue;

import java.util.function.Supplier;

import org.junit.Test;

import com.aol.cyclops.types.anyM.AnyMSeq;

public abstract class AbstractAnyMSeqTestsWithNulls extends AbstractAnyMSeqOrderedDependentTest {
	public abstract <T> AnyMSeq<T> of(T... values);

	@Test
	public void testSkipUntilWithNulls() {
		Supplier<AnyMSeq<Integer>> s = () -> of(1, 2, null, 3, 4, 5);

		assertTrue(s.get().dropUntil(i -> true).toList().containsAll(asList(1, 2, null, 3, 4, 5)));
	}

	@Test
	public void testLimitUntilWithNulls() {

		System.out.println(of(1, 2, null, 3, 4, 5).takeUntil(i -> false).toList());
		assertTrue(of(1, 2, null, 3, 4, 5).takeUntil(i -> false).toList().containsAll(asList(1, 2, null, 3, 4, 5)));
	}
}
