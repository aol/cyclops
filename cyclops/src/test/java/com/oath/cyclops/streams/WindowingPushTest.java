package com.oath.cyclops.streams;

import cyclops.companion.Streamable;
import cyclops.data.Seq;
import cyclops.data.Vector;
import cyclops.reactive.ReactiveSeq;
import cyclops.reactive.Spouts;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import static cyclops.reactive.Spouts.of;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class WindowingPushTest {
	ReactiveSeq<Integer> empty;
	ReactiveSeq<Integer> nonEmpty;

	@Before
	public void setup(){
		empty = of();
		nonEmpty = of(1);
	}



	@Test
	public void windowWhile(){
		assertThat(of(1,2,3,4,5,6)
				.groupedWhile(i->i%3!=0)
				.toList().size(),equalTo(2));
		assertThat(of(1,2,3,4,5,6)
				.groupedWhile(i->i%3!=0)
				.toList().get(0),equalTo(Seq.of(1,2,3)));
	}
	@Test
	public void windowUntil(){
		assertThat(of(1,2,3,4,5,6)
				.groupedUntil(i->i%3==0)
				.toList().size(),equalTo(2));
		assertThat(of(1,2,3,4,5,6)
				.groupedUntil(i->i%3==0)
				.toList().get(0),equalTo(Seq.of(1,2,3)));
	}
	@Test
	public void windowUntilEmpty(){
		assertThat(Spouts.<Integer>of()
				.groupedUntil(i->i%3==0)
				.toList().size(),equalTo(0));
	}
	@Test
	public void windowStatefullyUntil(){
		System.out.println(of(1,2,3,4,5,6)
				.groupedUntil((s, i)->s.containsValue(4) ? true : false)
				.toList());

		assertThat(of(1,2,3,4,5,6)
				.groupedUntil((s, i)->s.containsValue(4) ? true : false)
				.toList().size(),equalTo(2));

	}
	@Test
	public void windowStatefullyUntilEmpty(){

		assertThat(of()
				.groupedUntil((s, i)->s.contains(4) ? true : false)
				.toList().size(),equalTo(0));

	}
	@Test
	public void windowStatefullyWhile(){
		System.out.println(of(1,2,3,4,5,6)
				.groupedWhile((s, i)->s.containsValue(4) ? true : false)
				.toList());

		assertThat(of(1,2,3,4,5,6)
				.groupedWhile((s, i)->s.containsValue(4) ? true : false)
				.toList().size(),equalTo(4));

	}
	@Test
	public void windowStatefullyWhileEmpty(){

		assertThat(of()
				.groupedWhile((s, i)->s.contains(4) ? true : false)
				.toList().size(),equalTo(0));

	}
	@Test
	public void sliding() {
		List<Seq<Integer>> list = of(1, 2, 3, 4, 5, 6).sliding(2).collect(Collectors.toList());

		assertThat(list.get(0), hasItems(1, 2));
		assertThat(list.get(1), hasItems(2, 3));
	}

	@Test
	public void slidingIncrement() {
		List<Seq<Integer>> list = of(1, 2, 3, 4, 5, 6).sliding(3, 2).collect(Collectors.toList());

		System.out.println(list);
		assertThat(list.get(0), hasItems(1, 2, 3));
		assertThat(list.get(1), hasItems(3, 4, 5));
	}

	@Test
	public void grouped() {

		List<Vector<Integer>> list = of(1, 2, 3, 4, 5, 6).grouped(3).collect(Collectors.toList());
		System.out.println(list);
		assertThat(list.get(0), hasItems(1, 2, 3));
		assertThat(list.get(1), hasItems(4, 5, 6));

	}

	@Test
	public void sliding2() {


		List<Seq<Integer>> sliding = of(1, 2, 3, 4, 5).sliding(2).toList();

		assertThat(sliding, contains(Seq.of(1, 2), Seq.of(2, 3), Seq.of(3, 4), Seq.of(4, 5)));
	}

	@Test
	public void slidingOverlap() {

		List<Seq<Integer>> sliding = of(1, 2, 3, 4, 5).sliding(3,2).toList();

		assertThat(sliding, contains(Seq.of(1, 2, 3), Seq.of(3, 4, 5)));
	}

	@Test
	public void slidingEmpty() {

		System.out.println("List " + of().sliding(1).toList());
		assertThat(of().sliding(1).toList().size(),equalTo(0));
	}

	@Test
	public void slidingWithSmallWindowAtEnd() {


		List<Seq<Integer>> sliding = of(1, 2, 3, 4, 5).sliding(2,2).toList();

		assertThat(sliding, contains(Seq.of(1, 2), Seq.of(3, 4), Seq.of(5)));
	}
	@Test
	public void slidingWithSmallWindowAtEndIterative() {


		Iterator<Seq<Integer>> it =  of(1, 2, 3, 4, 5).sliding(2,2).iterator();
		List<Seq<Integer>> sliding = ReactiveSeq.fromIterator(it).toList();
		assertThat(sliding, contains(Seq.of(1, 2), Seq.of(3, 4), Seq.of(5)));
	}

	@Test
	public void groupedOnEmpty() throws Exception {
			assertThat( empty.grouped(10).count(),equalTo(0l));
	}

	@Test
	public void groupedEmpty0() throws Exception {
		empty.grouped(0).toList();
		assertThat(of(1,2,3).grouped(0).toList(),equalTo(Arrays.asList(Vector.of(1,2,3))));

	}
	@Test
	public void grouped0() throws Exception {
		nonEmpty.grouped(0).toList();

	}




	@Test
	public void groupedEmpty() throws Exception {

		assertThat(empty.grouped(1).count(),equalTo(0l));
	}

	@Test
	public void groupedInfinite() {
		ReactiveSeq<Integer> infinite = Spouts.iterate(1, i->i+1);

		final ReactiveSeq<Vector<Integer>> grouped = infinite.grouped(3);
		assertThat(grouped.elementAt(0).toOptional().get(),equalTo(Vector.of(1,2,3)));

	}

}


