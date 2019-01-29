package com.oath.cyclops.streams.streamable;


import static cyclops.companion.Streamable.of;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


import cyclops.data.Seq;
import cyclops.data.Vector;
import org.junit.Before;
import org.junit.Test;

import cyclops.reactive.ReactiveSeq;
import cyclops.companion.Streamable;


public class WindowingTest {
	Streamable<Integer> empty;
	Streamable<Integer> nonEmpty;

	@Before
	public void setup(){
		empty = of();
		nonEmpty = Streamable.of(1);
	}
	@Test
    public void takeWhile(){
        assertThat(of(1,2,3,4,5).takeWhile(p->p<6).toList().size(),greaterThan(1));
    }
    @Test
    public void takeWhileEmpty(){
        assertThat(of().takeWhile(p->true).toList(),equalTo(Arrays.asList()));
    }
	@Test
	public void windowWhile(){
		assertThat(Streamable.of(1,2,3,4,5,6)
				.groupedWhile(i->i%3!=0)
				.toList().size(),equalTo(2));
		assertThat(Streamable.of(1,2,3,4,5,6)
				.groupedWhile(i->i%3!=0)
				.toList().get(0),equalTo(Vector.of(1,2,3)));
	}
	@Test
	public void windowUntil(){
		assertThat(Streamable.of(1,2,3,4,5,6)
				.groupedUntil(i->i%3==0)
				.toList().size(),equalTo(2));
		assertThat(Streamable.of(1,2,3,4,5,6)
				.groupedUntil(i->i%3==0)
				.toList().get(0),equalTo(Vector.of(1,2,3)));
	}
	@Test
	public void windowUntilEmpty(){
		assertThat(ReactiveSeq.<Integer>of()
				.groupedUntil(i->i%3==0)
				.toList().size(),equalTo(0));
	}

	@Test
	public void windowStatefullyWhile(){

		System.out.println(Streamable.of(1,2,3,4,5,6)
				.groupedUntil((s, i)->s.containsValue(4) ? true : false)
				);
		assertThat(Streamable.of(1,2,3,4,5,6)
				.groupedUntil((s, i)->s.containsValue(4) ? true : false)

				.toList().size(),equalTo(2));

	}
	@Test
	public void windowStatefullyWhileEmpty(){

		assertThat(Streamable.of()
				.groupedUntil((s, i)->s.contains(4) ? true : false)
				.toList().size(),equalTo(0));

	}
	@Test
	public void sliding() {
		List<Seq<Integer>> list = Streamable.of(1, 2, 3, 4, 5, 6).sliding(2).collect(Collectors.toList());

		assertThat(list.get(0), hasItems(1, 2));
		assertThat(list.get(1), hasItems(2, 3));
	}

	@Test
	public void slidingIncrement() {
		List<Seq<Integer>> list = Streamable.of(1, 2, 3, 4, 5, 6).sliding(3, 2).collect(Collectors.toList());

		System.out.println(list);
		assertThat(list.get(0), hasItems(1, 2, 3));
		assertThat(list.get(1), hasItems(3, 4, 5));
	}

	@Test
	public void grouped() {

		List<Vector<Integer>> list = Streamable.of(1, 2, 3, 4, 5, 6).grouped(3).collect(Collectors.toList());
		System.out.println(list);
		assertThat(list.get(0), hasItems(1, 2, 3));
		assertThat(list.get(1), hasItems(4, 5, 6));

	}

	@Test
	public void sliding2() {


		List<Seq<Integer>> sliding = Streamable.of(1, 2, 3, 4, 5).sliding(2).toList();
		System.out.println("Sliding " + sliding);
		assertThat(sliding, contains(Vector.of(1, 2), Vector.of(2, 3), Vector.of(3, 4), Vector.of(4, 5)));
	}

	@Test
	public void slidingOverlap() {

		List<Seq<Integer>> sliding = Streamable.of(1, 2, 3, 4, 5).sliding(3,2).toList();

		assertThat(sliding, contains(Vector.of(1, 2, 3), Vector.of(3, 4, 5)));
	}

	@Test
	public void slidingEmpty() {


		assertThat(Streamable.of().sliding(1).toList().size(),equalTo(0));
	}

	@Test
	public void slidingWithSmallWindowAtEnd() {


		List<Seq<Integer>> sliding = Streamable.of(1, 2, 3, 4, 5).sliding(2,2).toList();

		assertThat(sliding, contains(Vector.of(1, 2), Vector.of(3, 4), Vector.of(5)));
	}

	@Test
	public void groupedOnEmpty() throws Exception {
			assertThat( empty.grouped(10).count(),equalTo(0l));
	}

	@Test
	public void groupedEmpty0() throws Exception {
		empty.grouped(0).toList();

	}
	@Test
	public void grouped0() throws Exception {
		nonEmpty.grouped(0).toList();

	}


	@Test
	public void groupedShorter() throws Exception {
		final Streamable<Integer> fixed = Streamable.fromStream(Stream.of(5, 7, 9));
		assertThat(fixed.stream().grouped(4).elementAtAndStream(0)._1(),equalTo(Vector.of(5,7,9)));
		assertThat(fixed.stream().grouped(4).count(),equalTo(1l));


	}

	@Test
	public void groupedEqualSize() throws Exception {
		final Streamable<Integer> fixed = Streamable.fromStream(Stream.of(5, 7, 9));
		assertThat(fixed.stream().grouped(3).elementAt(0).toOptional().get(),equalTo(Vector.of(5,7,9)));
		assertThat(fixed.stream().grouped(3).count(),equalTo(1l));
	}

	@Test
	public void multipleGrouped() throws Exception {
		final Streamable<Integer> fixed = Streamable.fromStream(Stream.of(5, 7, 9,10));
		assertThat(fixed.stream().grouped(3).elementAt(0).toOptional().get(),equalTo(Vector.of(5,7,9)));
		assertThat(fixed.stream().grouped(3).count(),equalTo(2l));

	}


	@Test
	public void return1() throws Exception {
		final Streamable<Integer> fixed = Streamable.fromStream(Stream.of(5));
		assertThat(fixed.stream().grouped(3).elementAt(0).toOptional().get(),equalTo(Vector.of(5)));
		assertThat(fixed.stream().grouped(3).count(),equalTo(1l));
	}

	@Test
	public void groupedEmpty() throws Exception {

		assertThat(empty.grouped(1).count(),equalTo(0l));
	}

	@Test
	public void groupedInfinite() {
		ReactiveSeq<Integer> infinite = ReactiveSeq.iterate(1, i->i+1);

		final ReactiveSeq<Vector<Integer>> grouped = infinite.grouped(3);
		assertThat(grouped.elementAt(0).toOptional().get(),equalTo(Vector.of(1,2,3)));

	}

}


