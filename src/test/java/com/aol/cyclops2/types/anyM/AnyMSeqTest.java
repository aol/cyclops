package com.aol.cyclops2.types.anyM;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.Test;
import org.testng.Assert;

import static org.junit.Assert.*;

import com.aol.cyclops2.internal.comprehensions.comprehenders.OptionalAdapter;
import com.aol.cyclops2.internal.monads.AnyMSeqImpl;

import cyclops.async.QueueFactories;
import cyclops.collections.ListX;
import cyclops.stream.FutureStream;

public class AnyMSeqTest {

	@Test
	public void testMergeP() {
		List<Integer> expected = Arrays.asList(new Integer[] { 10, 40, 50, 60 });
		AnyMSeq seq = new AnyMSeqImpl(Optional.of(10), OptionalAdapter.optional);
		AnyMSeq merged = seq.mergeP(QueueFactories.unboundedQueue(), FutureStream.builder().from(ListX.of(40, 50, 60)));
		assertEquals(4, merged.count());
		merged.traversable().forEach(x -> {
			System.out.println(x);
			Assert.assertTrue(expected.contains(x));
		});
	}
}
