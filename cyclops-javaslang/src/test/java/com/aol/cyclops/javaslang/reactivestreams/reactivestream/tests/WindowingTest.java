package com.aol.cyclops.javaslang.reactivestreams.reactivestream.tests;


import static com.aol.cyclops.javaslang.reactivestreams.ReactiveStream.of;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javaslang.collection.LazyStream;

import org.junit.Before;
import org.junit.Test;

import com.aol.cyclops.javaslang.reactivestreams.ReactiveStream;
import com.aol.cyclops.sequence.SequenceM;
import com.aol.cyclops.sequence.streamable.Streamable;

public class WindowingTest {
	ReactiveStream<Integer> empty;
	ReactiveStream<Integer> nonEmpty;

	@Before
	public void setup(){
		empty = of();
		nonEmpty = of(1);
	}
	
	@Test
	public void windowWhile(){
		assertThat(ReactiveStream.of(1,2,3,4,5,6)
				.windowWhile(i->i%3!=0)
				.toList().length(),equalTo(2));
		assertThat(ReactiveStream.of(1,2,3,4,5,6)
				.windowWhile(i->i%3!=0)
				.toList().get(0).sequenceM().toList(),equalTo(Arrays.asList(1,2,3)));
	}
	@Test
	public void windowUntil(){
		assertThat(ReactiveStream.of(1,2,3,4,5,6)
				.windowUntil(i->i%3==0)
				.toJavaList().size(),equalTo(2));
		assertThat(ReactiveStream.of(1,2,3,4,5,6)
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
		System.out.println(ReactiveStream.of(1,2,3,4,5,6)
				.windowStatefullyWhile((s,i)->s.sequenceM().toList().contains(4) ? true : false)
				.toList());
		assertThat(ReactiveStream.of(1,2,3,4,5,6)
				.windowStatefullyWhile((s,i)->s.sequenceM().toList().contains(4) ? true : false)
				.toList().length(),equalTo(5));
		
	}
	@Test
	public void windowStatefullyWhileEmpty(){
		
		assertThat(ReactiveStream.of()
				.windowStatefullyWhile((s,i)->s.sequenceM().toList().contains(4) ? true : false)
				.toList().length(),equalTo(0));
		
	}
	@Test
	public void sliding() {
		List<LazyStream<Integer>> list = ReactiveStream.of(1, 2, 3, 4, 5, 6).slidingWindow(2).collect(Collectors.toList());

		assertThat(list.get(0), hasItems(1, 2));
		assertThat(list.get(1), hasItems(2, 3));
	}

	@Test
	public void slidingIncrement() {
		List<LazyStream<Integer>> list = ReactiveStream.of(1, 2, 3, 4, 5, 6).slidingWindow(3, 2).collect(Collectors.toList());

		System.out.println(list);
		assertThat(list.get(0), hasItems(1, 2, 3));
		assertThat(list.get(1), hasItems(3, 4, 5));
	}

	@Test
	public void grouped() {

		List<List<Integer>> list = ReactiveStream.of(1, 2, 3, 4, 5, 6).windowBySize(3).map(s->s.toJavaList()).collect(Collectors.toList());
		System.out.println(list);
		assertThat(list.get(0), hasItems(1, 2, 3));
		assertThat(list.get(1), hasItems(4, 5, 6));

	}

	@Test
	public void sliding2() {
		

		List<ReactiveStream<Integer>> sliding = ReactiveStream.of(1, 2, 3, 4, 5).slidingWindow(2).toJavaList();

		assertThat(sliding, contains(ReactiveStream.of(1, 2), ReactiveStream.of(2, 3), ReactiveStream.of(3, 4), ReactiveStream.of(4, 5)));
	}

	@Test
	public void slidingOverlap() {
		
		List<java.util.List<Integer>> sliding = ReactiveStream.of(1, 2, 3, 4, 5).slidingWindow(3,2).map(s->s.toJavaList()).toJavaList();

		assertThat(sliding, contains(asList(1, 2, 3), asList(3, 4, 5)));
	}

	@Test
	public void slidingEmpty() {
		

		assertThat(ReactiveStream.of().sliding(1).toList().length(),equalTo(0));
	}

	@Test
	public void slidingWithSmallWindowAtEnd() {
		

		List<ReactiveStream<Integer>> sliding = ReactiveStream.of(1, 2, 3, 4, 5).slidingWindow(2,2).toJavaList();

		assertThat(sliding, contains(ReactiveStream.of(1, 2), ReactiveStream.of(3, 4), ReactiveStream.of(5)));
	}

	@Test
	public void groupedOnEmpty() throws Exception {
			assertThat( empty.windowBySize(10).length(),equalTo(0));
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
	public void groupedEmpty() throws Exception {
		
		assertThat(empty.grouped(1).length(),equalTo(0));
	}

	@Test
	public void groupedInfinite() {
		ReactiveStream<Integer> infinite = ReactiveStream.iterate(1, i->i+1);
		
		final ReactiveStream<ReactiveStream<Integer>> grouped = infinite.windowBySize(3);
		assertThat(grouped.get(),equalTo(ReactiveStream.of(1,2,3)));
	
	}

}


