package cyclops.streams.push.reactivestreamspath;

import cyclops.control.Option;
import cyclops.reactive.ReactiveSeq;
import cyclops.reactive.Spouts;
import org.junit.Test;

import java.util.Optional;
import java.util.function.Supplier;

import static cyclops.reactive.Spouts.of;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

public class PartitionAndSplittingRSTest {
	@Test
	public void testSplitBy() {
		Supplier<ReactiveSeq<Integer>> s = () -> of(1, 2, 3, 4, 5, 6);

		assertEquals(6, s.get().splitBy(i -> i % 2 != 0)._1().toList().size() + s.get().splitBy(i -> i % 2 != 0)._2().toList().size());

		assertTrue(s.get().splitBy(i -> true)._1().toList().containsAll(asList(1, 2, 3, 4, 5, 6)));
		assertEquals(asList(), s.get().splitBy(i -> true)._2().toList());

		assertEquals(asList(), s.get().splitBy(i -> false)._1().toList());
		assertTrue(s.get().splitBy(i -> false)._2().toList().containsAll(asList(1, 2, 3, 4, 5, 6)));
	}

	@Test
	public void testPartition() {
		Supplier<ReactiveSeq<Integer>> s = () -> Spouts.of(1, 2, 3, 4, 5, 6);

		assertEquals(asList(1, 3, 5), s.get().partition(i -> i % 2 != 0)._1().toList());
		assertEquals(asList(2, 4, 6), s.get().partition(i -> i % 2 != 0)._2().toList());

		assertEquals(asList(2, 4, 6), s.get().partition(i -> i % 2 == 0)._1().toList());
		assertEquals(asList(1, 3, 5), s.get().partition(i -> i % 2 == 0)._2().toList());

		assertEquals(asList(1, 2, 3), s.get().partition(i -> i <= 3)._1().toList());
		assertEquals(asList(4, 5, 6), s.get().partition(i -> i <= 3)._2().toList());

		assertEquals(asList(1, 2, 3, 4, 5, 6), s.get().partition(i -> true)._1().toList());
		assertEquals(asList(), s.get().partition(i -> true)._2().toList());

		assertEquals(asList(), s.get().partition(i -> false)._1().toList());
		assertEquals(asList(1, 2, 3, 4, 5, 6), s.get().partition(i -> false)._2().toList());
	}
	@Test
	public void testPartitionSequence() {
		Supplier<ReactiveSeq<Integer>> s = () -> Spouts.of(1, 2, 3, 4, 5, 6);

		assertEquals(asList(1, 3, 5), s.get().partition(i -> i % 2 != 0)._1().toList());
		assertEquals(asList(2, 4, 6), s.get().partition(i -> i % 2 != 0)._2().toList());

		assertEquals(asList(2, 4, 6), s.get().partition(i -> i % 2 == 0)._1().toList());
		assertEquals(asList(1, 3, 5), s.get().partition(i -> i % 2 == 0)._2().toList());

		assertEquals(asList(1, 2, 3), s.get().partition(i -> i <= 3)._1().toList());
		assertEquals(asList(4, 5, 6), s.get().partition(i -> i <= 3)._2().toList());

		assertEquals(asList(1, 2, 3, 4, 5, 6), s.get().partition(i -> true)._1().toList());
		assertEquals(asList(), s.get().partition(i -> true)._2().toList());

		assertEquals(asList(), s.get().partition(i -> false)._1().toList());
		assertEquals(asList(1, 2, 3, 4, 5, 6), s.get().partition(i -> false)._2().toList());
	}

	@Test
	public void testSplitAt() {
		for (int i = 0; i < 1000; i++) {
			Supplier<ReactiveSeq<Integer>> s = () -> of(1, 2, 3, 4, 5, 6);

			assertEquals(asList(), s.get().splitAt(0)._1().toList());
			assertTrue(s.get().splitAt(0)._2().toList().containsAll(asList(1, 2, 3, 4, 5, 6)));

			assertEquals(1, s.get().splitAt(1)._1().toList().size());
			assertEquals(s.get().splitAt(1)._2().toList().size(), 5);

			assertEquals(3, s.get().splitAt(3)._1().toList().size());

			assertEquals(3, s.get().splitAt(3)._2().count());

			assertEquals(6, s.get().splitAt(6)._1().toList().size());
			assertEquals(asList(), s.get().splitAt(6)._2().toList());

			assertThat(s.get().splitAt(7)._1().toList().size(), is(6));
			assertEquals(asList(), s.get().splitAt(7)._2().toList());

		}
	}

	@Test
	public void testSplitAtHead() {

		assertEquals(asList(), Spouts.of(1).splitAtHead()._2().toList());

		assertEquals(Option.none(), of().splitAtHead()._1());
		assertEquals(asList(), Spouts.of().splitAtHead()._2().toList());

		assertEquals(Option.of(1), Spouts.of(1).splitAtHead()._1());

		assertEquals(Option.of(1), Spouts.of(1, 2).splitAtHead()._1());
		assertEquals(asList(2), Spouts.of(1, 2).splitAtHead()._2().toList());

		assertEquals(Option.of(1), Spouts.of(1, 2, 3).splitAtHead()._1());
		assertEquals(Option.of(2), Spouts.of(1, 2, 3).splitAtHead()._2().splitAtHead()._1());
		assertEquals(Option.of(3), Spouts.of(1, 2, 3).splitAtHead()._2().splitAtHead()._2().splitAtHead()._1());
		assertEquals(asList(2, 3), Spouts.of(1, 2, 3).splitAtHead()._2().toList());
		assertEquals(asList(3), Spouts.of(1, 2, 3).splitAtHead()._2().splitAtHead()._2().toList());
		assertEquals(asList(), Spouts.of(1, 2, 3).splitAtHead()._2().splitAtHead()._2().splitAtHead()._2().toList());
	}

	@Test
	public void testSplitSequenceAtHead() {

		assertEquals(asList(), Spouts.of(1).splitAtHead()._2().toList());

		assertEquals(Option.none(), of().splitAtHead()._1());
		assertEquals(asList(), Spouts.of().splitAtHead()._2().toList());

		assertEquals(Option.of(1), Spouts.of(1).splitAtHead()._1());

		assertEquals(Option.of(1), Spouts.of(1, 2).splitAtHead()._1());
		assertEquals(asList(2), Spouts.of(1, 2).splitAtHead()._2().toList());

		assertEquals(Option.of(1), Spouts.of(1, 2, 3).splitAtHead()._1());
		assertEquals(Option.of(2), Spouts.of(1, 2, 3).splitAtHead()._2().splitAtHead()._1());
		assertEquals(Option.of(3), Spouts.of(1, 2, 3).splitAtHead()._2().splitAtHead()._2().splitAtHead()._1());
		assertEquals(asList(2, 3), Spouts.of(1, 2, 3).splitAtHead()._2().toList());
		assertEquals(asList(3), Spouts.of(1, 2, 3).splitAtHead()._2().splitAtHead()._2().toList());
		assertEquals(asList(), Spouts.of(1, 2, 3).splitAtHead()._2().splitAtHead()._2().splitAtHead()._2().toList());
	}

}
