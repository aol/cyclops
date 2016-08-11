package com.aol.cyclops.react.lazy.futures;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Optional;
import java.util.function.Supplier;

import org.junit.Test;

import com.aol.cyclops.types.futurestream.LazyFutureStream;

public class PartitionAndSplittingTest {
	
	@Test
	public void testSplitAt() {
		for (int i = 0; i < 1000; i++) {
			Supplier<LazyFutureStream<Integer>> s = () -> LazyFutureStream.of(1, 2, 3, 4, 5, 6);

			assertEquals(asList(), s.get().actOnFutures().splitAt(0).v1.toList());
			assertTrue(s.get().actOnFutures().splitAt(0).v2.toList().containsAll(asList(1, 2, 3, 4, 5, 6)));

			assertEquals(1, s.get().actOnFutures().splitAt(1).v1.toList().size());
			assertEquals(s.get().actOnFutures().splitAt(1).v2.toList().size(), 5);

			assertEquals(3, s.get().actOnFutures().splitAt(3).v1.toList().size());

			assertEquals(3, s.get().actOnFutures().splitAt(3).v2.count());

			assertEquals(6, s.get().actOnFutures().splitAt(6).v1.toList().size());
			assertEquals(asList(), s.get().actOnFutures().splitAt(6).v2.toList());

			assertThat(s.get().actOnFutures().splitAt(7).v1.toList().size(), is(6));
			assertEquals(asList(), s.get().actOnFutures().splitAt(7).v2.toList());

		}
	}

	@Test
	public void testSplitAtHead() {

		assertEquals(asList(), LazyFutureStream.of(1).actOnFutures().splitAtHead().v2.toList());

		assertEquals(Optional.empty(),LazyFutureStream.of().actOnFutures().splitAtHead().v1);
		assertEquals(asList(), LazyFutureStream.of().actOnFutures().splitAtHead().v2.toList());

		assertEquals(Optional.of(1), LazyFutureStream.of(1).actOnFutures().splitAtHead().v1);

		assertEquals(Optional.of(1), LazyFutureStream.of(1, 2).actOnFutures().splitAtHead().v1);
		assertEquals(asList(2), LazyFutureStream.of(1, 2).actOnFutures().splitAtHead().v2.toList());

		assertEquals(Optional.of(1), LazyFutureStream.of(1, 2, 3).actOnFutures().splitAtHead().v1);
		assertEquals(Optional.of(2), LazyFutureStream.of(1, 2, 3).actOnFutures().splitAtHead().v2.splitAtHead().v1);
		assertEquals(Optional.of(3), LazyFutureStream.of(1, 2, 3).actOnFutures().splitAtHead().v2.splitAtHead().v2.splitAtHead().v1);
		assertEquals(asList(2, 3), LazyFutureStream.of(1, 2, 3).splitAtHead().v2.toList());
		assertEquals(asList(3), LazyFutureStream.of(1, 2, 3).actOnFutures().splitAtHead().v2.splitAtHead().v2.toList());
		assertEquals(asList(), LazyFutureStream.of(1, 2, 3).actOnFutures().splitAtHead().v2.splitAtHead().v2.splitAtHead().v2.toList());
	}


}
