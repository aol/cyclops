package com.aol.cyclops.streams.streamable;


import static com.aol.cyclops.sequence.streamable.Streamable.of;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;

import com.aol.cyclops.collections.extensions.standard.ListX;
import com.aol.cyclops.sequence.SequenceM;
import com.aol.cyclops.sequence.streamable.Streamable;

public class WindowingTest {
	Streamable<Integer> empty;
	Streamable<Integer> nonEmpty;

	@Before
	public void setup(){
		empty = of();
		nonEmpty = Streamable.of(1);
	}
	
	@Test
	public void windowWhile(){
		assertThat(Streamable.of(1,2,3,4,5,6)
				.windowWhile(i->i%3!=0)
				.toList().size(),equalTo(2));
		assertThat(Streamable.of(1,2,3,4,5,6)
				.windowWhile(i->i%3!=0)
				.toList().get(0).sequenceM().toList(),equalTo(Arrays.asList(1,2,3)));
	}
	@Test
	public void windowUntil(){
		assertThat(Streamable.of(1,2,3,4,5,6)
				.windowUntil(i->i%3==0)
				.toList().size(),equalTo(2));
		assertThat(Streamable.of(1,2,3,4,5,6)
				.windowUntil(i->i%3==0)
				.toList().get(0).sequenceM().toList(),equalTo(Arrays.asList(1,2,3)));
	}
	@Test
	public void windowUntilEmpty(){
		assertThat(SequenceM.<Integer>of()
				.windowUntil(i->i%3==0)
				.toList().size(),equalTo(0));
	}
	@Test
	public void windowStatefullyWhile(){
		System.out.println(Streamable.of(1,2,3,4,5,6)
				.windowStatefullyWhile((s,i)->s.sequenceM().toList().contains(4) ? true : false)
				.toList());
		assertThat(Streamable.of(1,2,3,4,5,6)
				.windowStatefullyWhile((s,i)->s.sequenceM().toList().contains(4) ? true : false)
				.toList().size(),equalTo(5));
		
	}
	@Test
	public void windowStatefullyWhileEmpty(){
		
		assertThat(Streamable.of()
				.windowStatefullyWhile((s,i)->s.contains(4) ? true : false)
				.toList().size(),equalTo(0));
		
	}
	@Test
	public void sliding() {
		List<List<Integer>> list = Streamable.of(1, 2, 3, 4, 5, 6).sliding(2).collect(Collectors.toList());

		assertThat(list.get(0), hasItems(1, 2));
		assertThat(list.get(1), hasItems(2, 3));
	}

	@Test
	public void slidingIncrement() {
		List<List<Integer>> list = Streamable.of(1, 2, 3, 4, 5, 6).sliding(3, 2).collect(Collectors.toList());

		System.out.println(list);
		assertThat(list.get(0), hasItems(1, 2, 3));
		assertThat(list.get(1), hasItems(3, 4, 5));
	}

	@Test
	public void grouped() {

		List<List<Integer>> list = Streamable.of(1, 2, 3, 4, 5, 6).grouped(3).collect(Collectors.toList());
		System.out.println(list);
		assertThat(list.get(0), hasItems(1, 2, 3));
		assertThat(list.get(1), hasItems(4, 5, 6));

	}

	@Test
	public void sliding2() {
		

		List<ListX<Integer>> sliding = Streamable.of(1, 2, 3, 4, 5).sliding(2).toList();

		assertThat(sliding, contains(asList(1, 2), asList(2, 3), asList(3, 4), asList(4, 5)));
	}

	@Test
	public void slidingOverlap() {
		
		List<ListX<Integer>> sliding = Streamable.of(1, 2, 3, 4, 5).sliding(3,2).toList();

		assertThat(sliding, contains(asList(1, 2, 3), asList(3, 4, 5)));
	}

	@Test
	public void slidingEmpty() {
		

		assertThat(Streamable.of().sliding(1).toList().size(),equalTo(0));
	}

	@Test
	public void slidingWithSmallWindowAtEnd() {
		

		List<ListX<Integer>> sliding = Streamable.of(1, 2, 3, 4, 5).sliding(2,2).toList();

		assertThat(sliding, contains(asList(1, 2), asList(3, 4), asList(5)));
	}

	@Test
	public void groupedOnEmpty() throws Exception {
			assertThat( empty.grouped(10).count(),equalTo(0l));
	}

	@Test(expected=IllegalArgumentException.class)
	public void groupedEmpty0() throws Exception {
		empty.grouped(0).toList();
		
	}
	@Test(expected=IllegalArgumentException.class)
	public void grouped0() throws Exception {
		nonEmpty.grouped(0).toList();
		
	}


	@Test
	public void groupedShorter() throws Exception {
		final Streamable<Integer> fixed = Streamable.fromStream(Stream.of(5, 7, 9));
		assertThat(fixed.sequenceM().grouped(4).elementAt(0).v1,equalTo(Arrays.asList(5,7,9)));
		assertThat(fixed.sequenceM().grouped(4).count(),equalTo(1l));

		
	}

	@Test
	public void groupedEqualSize() throws Exception {
		final Streamable<Integer> fixed = Streamable.fromStream(Stream.of(5, 7, 9));
		assertThat(fixed.sequenceM().grouped(3).get(0).get(),equalTo(Arrays.asList(5,7,9)));
		assertThat(fixed.sequenceM().grouped(3).count(),equalTo(1l));
	}

	@Test
	public void multipleGrouped() throws Exception {
		final Streamable<Integer> fixed = Streamable.fromStream(Stream.of(5, 7, 9,10));
		assertThat(fixed.sequenceM().grouped(3).get(0).get(),equalTo(Arrays.asList(5,7,9)));
		assertThat(fixed.sequenceM().grouped(3).count(),equalTo(2l));
		
	}

	
	@Test
	public void return1() throws Exception {
		final Streamable<Integer> fixed = Streamable.fromStream(Stream.of(5));
		assertThat(fixed.sequenceM().grouped(3).get(0).get(),equalTo(Arrays.asList(5)));
		assertThat(fixed.sequenceM().grouped(3).count(),equalTo(1l));
	}

	@Test
	public void groupedEmpty() throws Exception {
		
		assertThat(empty.grouped(1).count(),equalTo(0l));
	}

	@Test
	public void groupedInfinite() {
		SequenceM<Integer> infinite = SequenceM.iterate(1, i->i+1);
		
		final SequenceM<ListX<Integer>> grouped = infinite.grouped(3);
		assertThat(grouped.get(0).get(),equalTo(Arrays.asList(1,2,3)));
	
	}

}


