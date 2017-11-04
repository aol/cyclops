package com.oath.cyclops.react.lazy.futures;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.function.Supplier;

import com.oath.cyclops.react.lazy.DuplicationTest;
import cyclops.control.Option;
import cyclops.reactive.FutureStream;
import org.junit.Assert;
import org.junit.Test;

public class PartitionAndSplittingTest {

	@Test
	public void testSplitAt() {
		for (int i = 0; i < 1000; i++) {
			Supplier<FutureStream<Integer>> s = () -> DuplicationTest.of(1, 2, 3, 4, 5, 6);

			Assert.assertEquals(asList(), s.get().actOnFutures().splitAt(0)._1().toList());
			assertTrue(s.get().actOnFutures().splitAt(0)._2().toList().containsAll(asList(1, 2, 3, 4, 5, 6)));

			Assert.assertEquals(1, s.get().actOnFutures().splitAt(1)._1().toList().size());
			Assert.assertEquals(s.get().actOnFutures().splitAt(1)._2().toList().size(), 5);

			Assert.assertEquals(3, s.get().actOnFutures().splitAt(3)._1().toList().size());

			Assert.assertEquals(3, s.get().actOnFutures().splitAt(3)._2().count());

			Assert.assertEquals(6, s.get().actOnFutures().splitAt(6)._1().toList().size());
			Assert.assertEquals(asList(), s.get().actOnFutures().splitAt(6)._2().toList());

			assertThat(s.get().actOnFutures().splitAt(7)._1().toList().size(), is(6));
			Assert.assertEquals(asList(), s.get().actOnFutures().splitAt(7)._2().toList());

		}
	}

	@Test
	public void testSplitAtHead() {

		Assert.assertEquals(asList(), DuplicationTest.of(1).actOnFutures().splitAtHead()._2().toList());

		Assert.assertEquals(Option.none(), DuplicationTest.of().actOnFutures().splitAtHead()._1());
		Assert.assertEquals(asList(), DuplicationTest.of().actOnFutures().splitAtHead()._2().toList());

		Assert.assertEquals(Option.of(1), DuplicationTest.of(1).actOnFutures().splitAtHead()._1());

		Assert.assertEquals(Option.of(1), DuplicationTest.of(1, 2).actOnFutures().splitAtHead()._1());
		Assert.assertEquals(asList(2), DuplicationTest.of(1, 2).actOnFutures().splitAtHead()._2().toList());

		Assert.assertEquals(Option.of(1), DuplicationTest.of(1, 2, 3).actOnFutures().splitAtHead()._1());
		Assert.assertEquals(Option.of(2), DuplicationTest.of(1, 2, 3).actOnFutures().splitAtHead()._2().splitAtHead()._1());
		Assert.assertEquals(Option.of(3), DuplicationTest.of(1, 2, 3).actOnFutures().splitAtHead()._2().splitAtHead()._2().splitAtHead()._1());
		Assert.assertEquals(asList(2, 3), DuplicationTest.of(1, 2, 3).splitAtHead()._2().toList());
		Assert.assertEquals(asList(3), DuplicationTest.of(1, 2, 3).actOnFutures().splitAtHead()._2().splitAtHead()._2().toList());
		Assert.assertEquals(asList(), DuplicationTest.of(1, 2, 3).actOnFutures().splitAtHead()._2().splitAtHead()._2().splitAtHead()._2().toList());
	}


}
