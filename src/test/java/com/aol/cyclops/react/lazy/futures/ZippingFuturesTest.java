
package com.aol.cyclops.react.lazy.futures;


import static com.aol.cyclops.react.stream.traits.LazyFutureStream.of;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.jooq.lambda.tuple.Tuple4;
import org.junit.Before;
import org.junit.Test;

import com.aol.cyclops.react.stream.traits.LazyFutureStream;

public class ZippingFuturesTest {
	LazyFutureStream<Integer> empty;
	LazyFutureStream<Integer> nonEmpty;

	@Before
	public void setup(){
		empty = of();
		nonEmpty = of(1);
	}

	
	@Test
	public void zip(){
		List<Tuple2<Integer,Integer>> list =
				of(1,2,3,4,5,6).actOnFutures().zip(of(100,200,300,400))
												.peek(it -> System.out.println(it))
												
												.collect(Collectors.toList());
		
		List<Integer> right = list.stream().map(t -> t.v2).collect(Collectors.toList());
		
		assertThat(right,hasItem(100));
		assertThat(right,hasItem(200));
		assertThat(right,hasItem(300));
		assertThat(right,hasItem(400));
		
		List<Integer> left = list.stream().map(t -> t.v1).collect(Collectors.toList());
		assertThat(Arrays.asList(1,2,3,4,5,6),hasItem(left.get(0)));
		
		
	}
	@Test
	public void zip3(){
		List<Tuple3<Integer,Integer,Character>> list =
				of(1,2,3,4,5,6).actOnFutures().zip3(of(100,200,300,400),of('a','b','c'))
												.peek(it -> System.out.println(it))
												
												.collect(Collectors.toList());
		
		List<Integer> right = list.stream().map(t -> t.v2).collect(Collectors.toList());
		assertThat(right,hasItem(100));
		assertThat(right,hasItem(200));
		assertThat(right,hasItem(300));
		assertThat(right,not(hasItem(400)));
		
		List<Integer> left = list.stream().map(t -> t.v1).collect(Collectors.toList());
		assertThat(Arrays.asList(1,2,3,4,5,6),hasItem(left.get(0)));
		
		List<Character> three = list.stream().map(t -> t.v3).collect(Collectors.toList());
		assertThat(Arrays.asList('a','b','c'),hasItem(three.get(0)));
		
		
	}
	@Test
	public void zip3Lfs(){
		List<Tuple3<Integer,Integer,Character>> list =
				of(1,2,3,4,5,6).actOnFutures().zip3Lfs(of(100,200,300,400),of('a','b','c'))
												.peek(it -> System.out.println(it))
												
												.collect(Collectors.toList());
		
		List<Integer> right = list.stream().map(t -> t.v2).collect(Collectors.toList());
		assertThat(right,hasItem(100));
		assertThat(right,hasItem(200));
		assertThat(right,hasItem(300));
		assertThat(right,not(hasItem(400)));
		
		List<Integer> left = list.stream().map(t -> t.v1).collect(Collectors.toList());
		assertThat(Arrays.asList(1,2,3,4,5,6),hasItem(left.get(0)));
		
		List<Character> three = list.stream().map(t -> t.v3).collect(Collectors.toList());
		assertThat(Arrays.asList('a','b','c'),hasItem(three.get(0)));
		
		
	}
	@Test
	public void zip4(){
		List<Tuple4<Integer,Integer,Character,String>> list =
				of(1,2,3,4,5,6).actOnFutures().zip4(of(100,200,300,400),of('a','b','c'),of("hello","world"))
												.peek(it -> System.out.println(it))
												
												.collect(Collectors.toList());
		System.out.println(list);
		List<Integer> right = list.stream().map(t -> t.v2).collect(Collectors.toList());
		assertThat(right,hasItem(100));
		assertThat(right,hasItem(200));
		assertThat(right,not(hasItem(300)));
		assertThat(right,not(hasItem(400)));
		
		List<Integer> left = list.stream().map(t -> t.v1).collect(Collectors.toList());
		assertThat(Arrays.asList(1,2,3,4,5,6),hasItem(left.get(0)));
		
		List<Character> three = list.stream().map(t -> t.v3).collect(Collectors.toList());
		assertThat(Arrays.asList('a','b','c'),hasItem(three.get(0)));
	
		List<String> four = list.stream().map(t -> t.v4).collect(Collectors.toList());
		assertThat(Arrays.asList("hello","world"),hasItem(four.get(0)));
		
		
	}
	@Test
	public void zip4Lfs(){
		List<Tuple4<Integer,Integer,Character,String>> list =
				of(1,2,3,4,5,6).actOnFutures().zip4Lfs(of(100,200,300,400),of('a','b','c'),of("hello","world"))
												.peek(it -> System.out.println(it))
												
												.collect(Collectors.toList());
		System.out.println(list);
		List<Integer> right = list.stream().map(t -> t.v2).collect(Collectors.toList());
		assertThat(right,hasItem(100));
		assertThat(right,hasItem(200));
		assertThat(right,not(hasItem(300)));
		assertThat(right,not(hasItem(400)));
		
		List<Integer> left = list.stream().map(t -> t.v1).collect(Collectors.toList());
		assertThat(Arrays.asList(1,2,3,4,5,6),hasItem(left.get(0)));
		
		List<Character> three = list.stream().map(t -> t.v3).collect(Collectors.toList());
		assertThat(Arrays.asList('a','b','c'),hasItem(three.get(0)));
	
		List<String> four = list.stream().map(t -> t.v4).collect(Collectors.toList());
		assertThat(Arrays.asList("hello","world"),hasItem(four.get(0)));
		
		
	}
	
	@Test
	public void zip2of(){
		
		List<Tuple2<Integer,Integer>> list =of(1,2,3,4,5,6).actOnFutures()
											.zip(of(100,200,300,400))
											.peek(it -> System.out.println(it))
											.collect(Collectors.toList());
				
	
		List<Integer> right = list.stream().map(t -> t.v2).collect(Collectors.toList());
		assertThat(right,hasItem(100));
		assertThat(right,hasItem(200));
		assertThat(right,hasItem(300));
		assertThat(right,hasItem(400));
		
		List<Integer> left = list.stream().map(t -> t.v1).collect(Collectors.toList());
		assertThat(Arrays.asList(1,2,3,4,5,6),hasItem(left.get(0)));

	}
	@Test
	public void zipInOrder(){
		
		List<Tuple2<Integer,Integer>> list =  of(1,2,3,4,5,6).actOnFutures()
													.zip( of(100,200,300,400))
													.collect(Collectors.toList());
		
		assertThat(asList(1,2,3,4,5,6),hasItem(list.get(0).v1));
		assertThat(asList(100,200,300,400),hasItem(list.get(0).v2));
		
		
		
	}

	
	

	@Test
	public void testZipDifferingLength() {
		List<Tuple2<Integer, String>> list = of(1, 2).actOnFutures().zip(of("a", "b", "c", "d")).toList();

		assertEquals(2, list.size());
		assertTrue(asList(1, 2).contains(list.get(0).v1));
		assertTrue("" + list.get(1).v2, asList(1, 2).contains(list.get(1).v1));
		assertTrue(asList("a", "b", "c", "d").contains(list.get(0).v2));
		assertTrue(asList("a", "b", "c", "d").contains(list.get(1).v2));

	}

	
	

	@Test
	public void testZipDifferingLengthStream() {
		List<Tuple2<Integer, String>> list = of(1, 2).actOnFutures().zipLfs(of("a", "b", "c", "d")).toList();

		assertEquals(2, list.size());
		assertTrue(asList(1, 2).contains(list.get(0).v1));
		assertTrue("" + list.get(1).v2, asList(1, 2).contains(list.get(1).v1));
		assertTrue(asList("a", "b", "c", "d").contains(list.get(0).v2));
		assertTrue(asList("a", "b", "c", "d").contains(list.get(1).v2));

	}

	
	
	@Test
	public void testZipWithIndex() {
		assertEquals(asList(), of().actOnFutures().zipWithIndex().toList());

		assertThat(of("a").zipWithIndex().map(t -> t.v2).findFirst().get(), is(0l));
		assertEquals(asList(new Tuple2("a", 0L)), of("a").zipWithIndex().toList());

	}


}
