package com.aol.cyclops2.streams;

import static cyclops.stream.ReactiveSeq.of;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import cyclops.collectionx.immutable.VectorX;
import org.junit.Before;
import org.junit.Test;

import cyclops.stream.ReactiveSeq;
import cyclops.stream.Streamable;
import cyclops.collectionx.mutable.ListX;

public class WindowingTest {
	ReactiveSeq<Integer> empty;
	ReactiveSeq<Integer> nonEmpty;

	@Before
	public void setup(){
		empty = of();
		nonEmpty = of(1);
	}


	
	@Test
	public void windowWhile(){
		assertThat(ReactiveSeq.of(1,2,3,4,5,6)
				.groupedWhile(i->i%3!=0)
				.toList().size(),equalTo(2));
		assertThat(ReactiveSeq.of(1,2,3,4,5,6)
				.groupedWhile(i->i%3!=0)
				.toList().get(0),equalTo(Arrays.asList(1,2,3)));
	}
	@Test
	public void windowUntil(){
		assertThat(ReactiveSeq.of(1,2,3,4,5,6)
				.groupedUntil(i->i%3==0)
				.toList().size(),equalTo(2));
		assertThat(ReactiveSeq.of(1,2,3,4,5,6)
				.groupedUntil(i->i%3==0)
				.toList().get(0),equalTo(Arrays.asList(1,2,3)));
	}
	@Test
	public void windowUntilEmpty(){
		assertThat(ReactiveSeq.<Integer>of()
				.groupedUntil(i->i%3==0)
				.toList().size(),equalTo(0));
	}
	@Test
	public void windowStatefullyUntil(){
		System.out.println(ReactiveSeq.of(1,2,3,4,5,6)
				.groupedStatefullyUntil((s,i)->s.contains(4) ? true : false)
				.toList());

		assertThat(ReactiveSeq.of(1,2,3,4,5,6)
				.groupedStatefullyUntil((s,i)->s.contains(4) ? true : false)
				.toList().size(),equalTo(2));
		
	}
	@Test
	public void windowStatefullyUntilEmpty(){
		
		assertThat(ReactiveSeq.of()
				.groupedStatefullyUntil((s,i)->s.contains(4) ? true : false)
				.toList().size(),equalTo(0));
		
	}
	@Test
	public void windowStatefullyWhile(){
		System.out.println(ReactiveSeq.of(1,2,3,4,5,6)
				.groupedStatefullyWhile((s,i)->s.contains(4) ? true : false)
				.toList());

		assertThat(ReactiveSeq.of(1,2,3,4,5,6)
				.groupedStatefullyWhile((s,i)->s.contains(4) ? true : false)
				.toList().size(),equalTo(4));

	}
	@Test
	public void windowStatefullyWhileEmpty(){

		assertThat(ReactiveSeq.of()
				.groupedStatefullyWhile((s,i)->s.contains(4) ? true : false)
				.toList().size(),equalTo(0));

	}
	@Test
	public void sliding() {
		List<List<Integer>> list = ReactiveSeq.of(1, 2, 3, 4, 5, 6).sliding(2).collect(Collectors.toList());

		assertThat(list.get(0), hasItems(1, 2));
		assertThat(list.get(1), hasItems(2, 3));
	}

	@Test
	public void slidingIncrement() {
		List<List<Integer>> list = ReactiveSeq.of(1, 2, 3, 4, 5, 6).sliding(3, 2).collect(Collectors.toList());

		System.out.println(list);
		assertThat(list.get(0), hasItems(1, 2, 3));
		assertThat(list.get(1), hasItems(3, 4, 5));
	}

	@Test
	public void grouped() {

		List<List<Integer>> list = ReactiveSeq.of(1, 2, 3, 4, 5, 6).grouped(3).collect(Collectors.toList());
		System.out.println(list);
		assertThat(list.get(0), hasItems(1, 2, 3));
		assertThat(list.get(1), hasItems(4, 5, 6));

	}

	@Test
	public void sliding2() {
		

		List<VectorX<Integer>> sliding = ReactiveSeq.of(1, 2, 3, 4, 5).sliding(2).toList();

		assertThat(sliding, contains(asList(1, 2), asList(2, 3), asList(3, 4), asList(4, 5)));
	}

	@Test
	public void slidingOverlap() {
		
		List<VectorX<Integer>> sliding = ReactiveSeq.of(1, 2, 3, 4, 5).sliding(3,2).toList();

		assertThat(sliding, contains(asList(1, 2, 3), asList(3, 4, 5)));
	}

	@Test
	public void slidingEmpty() {

		System.out.println("List " + ReactiveSeq.of().sliding(1).toList());
		assertThat(ReactiveSeq.of().sliding(1).toList().size(),equalTo(0));
	}

	@Test
	public void slidingWithSmallWindowAtEnd() {
		

		List<VectorX<Integer>> sliding = ReactiveSeq.of(1, 2, 3, 4, 5).sliding(2,2).toList();

		assertThat(sliding, contains(asList(1, 2), asList(3, 4), asList(5)));
	}
	@Test
	public void slidingWithSmallWindowAtEndIterative() {


		Iterator<VectorX<Integer>> it =  ReactiveSeq.of(1, 2, 3, 4, 5).sliding(2,2).iterator();
		List<VectorX<Integer>> sliding = ReactiveSeq.fromIterator(it).toList();
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
		final Streamable<Integer> fixed = Streamable.fromStream(of(5, 7, 9));
		assertThat(fixed.reactiveSeq().grouped(4).elementAt(0)._1(),equalTo(Arrays.asList(5,7,9)));
		assertThat(fixed.reactiveSeq().grouped(4).count(),equalTo(1l));

		
	}

	@Test
	public void groupedEqualSize() throws Exception {
		final Streamable<Integer> fixed = Streamable.fromStream(of(5, 7, 9));
		assertThat(fixed.reactiveSeq().grouped(3).get(0).toOptional().get(),equalTo(Arrays.asList(5,7,9)));
		assertThat(fixed.reactiveSeq().grouped(3).count(),equalTo(1l));
	}

	@Test
	public void multipleGrouped() throws Exception {
		final Streamable<Integer> fixed = Streamable.fromStream(of(5, 7, 9,10));
		assertThat(fixed.reactiveSeq().grouped(3).get(0).toOptional().get(),equalTo(Arrays.asList(5,7,9)));
		fixed.reactiveSeq().grouped(3).printOut();
		assertThat(fixed.reactiveSeq().grouped(3).count(),equalTo(2l));
		
	}


	
	@Test
	public void return1() throws Exception {
		final Streamable<Integer> fixed = Streamable.fromStream(of(5));
		assertThat(fixed.reactiveSeq().grouped(3).get(0).toOptional().get(),equalTo(Arrays.asList(5)));
		assertThat(fixed.reactiveSeq().grouped(3).count(),equalTo(1l));
	}

	@Test
	public void groupedEmpty() throws Exception {
		
		assertThat(empty.grouped(1).count(),equalTo(0l));
	}

	@Test
	public void groupedInfinite() {
		ReactiveSeq<Integer> infinite = ReactiveSeq.iterate(1, i->i+1);
		
		final ReactiveSeq<ListX<Integer>> grouped = infinite.grouped(3);
		assertThat(grouped.get(0).toOptional().get(),equalTo(Arrays.asList(1,2,3)));
	
	}

}


