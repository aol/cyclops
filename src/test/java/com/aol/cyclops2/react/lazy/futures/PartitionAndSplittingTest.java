package com.aol.cyclops2.react.lazy.futures;

import static com.aol.cyclops2.react.lazy.DuplicationTest.of;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Optional;
import java.util.function.Supplier;

import cyclops.control.Option;
import cyclops.reactive.FutureStream;
import org.junit.Test;

public class PartitionAndSplittingTest {
	
	@Test
	public void testSplitAt() {
		for (int i = 0; i < 1000; i++) {
			Supplier<FutureStream<Integer>> s = () -> of(1, 2, 3, 4, 5, 6);

			assertEquals(asList(), s.get().actOnFutures().splitAt(0)._1().toList());
			assertTrue(s.get().actOnFutures().splitAt(0)._2().toList().containsAll(asList(1, 2, 3, 4, 5, 6)));

			assertEquals(1, s.get().actOnFutures().splitAt(1)._1().toList().size());
			assertEquals(s.get().actOnFutures().splitAt(1)._2().toList().size(), 5);

			assertEquals(3, s.get().actOnFutures().splitAt(3)._1().toList().size());

			assertEquals(3, s.get().actOnFutures().splitAt(3)._2().count());

			assertEquals(6, s.get().actOnFutures().splitAt(6)._1().toList().size());
			assertEquals(asList(), s.get().actOnFutures().splitAt(6)._2().toList());

			assertThat(s.get().actOnFutures().splitAt(7)._1().toList().size(), is(6));
			assertEquals(asList(), s.get().actOnFutures().splitAt(7)._2().toList());

		}
	}

	@Test
	public void testSplitAtHead() {

		assertEquals(asList(), of(1).actOnFutures().splitAtHead()._2().toList());

		assertEquals(Option.none(), of().actOnFutures().splitAtHead()._1());
		assertEquals(asList(), of().actOnFutures().splitAtHead()._2().toList());

		assertEquals(Option.of(1), of(1).actOnFutures().splitAtHead()._1());

		assertEquals(Option.of(1), of(1, 2).actOnFutures().splitAtHead()._1());
		assertEquals(asList(2), of(1, 2).actOnFutures().splitAtHead()._2().toList());

		assertEquals(Option.of(1), of(1, 2, 3).actOnFutures().splitAtHead()._1());
		assertEquals(Option.of(2), of(1, 2, 3).actOnFutures().splitAtHead()._2().splitAtHead()._1());
		assertEquals(Option.of(3), of(1, 2, 3).actOnFutures().splitAtHead()._2().splitAtHead()._2().splitAtHead()._1());
		assertEquals(asList(2, 3), of(1, 2, 3).splitAtHead()._2().toList());
		assertEquals(asList(3), of(1, 2, 3).actOnFutures().splitAtHead()._2().splitAtHead()._2().toList());
		assertEquals(asList(), of(1, 2, 3).actOnFutures().splitAtHead()._2().splitAtHead()._2().splitAtHead()._2().toList());
	}


}
