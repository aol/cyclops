
package cyclops.futurestream.react.lazy.futures;

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

import cyclops.data.tuple.Tuple;
import cyclops.futurestream.react.lazy.DuplicationTest;
import cyclops.data.tuple.Tuple2;
import cyclops.data.tuple.Tuple3;
import cyclops.data.tuple.Tuple4;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import cyclops.futurestream.FutureStream;

public class ZippingFuturesTest {
	FutureStream<Integer> empty;
	FutureStream<Integer> nonEmpty;

	@Before
	public void setup(){
		empty = DuplicationTest.of();
		nonEmpty = DuplicationTest.of(1);
	}


	@Test
	public void zip(){
		List<Tuple2<Integer,Integer>> list =
				DuplicationTest.of(1,2,3,4,5,6).actOnFutures().zip(DuplicationTest.of(100,200,300,400))
												.peek(it -> System.out.println(it))

												.collect(Collectors.toList());

		List<Integer> right = list.stream().map(t -> t._2()).collect(Collectors.toList());

		assertThat(right,hasItem(100));
		assertThat(right,hasItem(200));
		assertThat(right,hasItem(300));
		assertThat(right,hasItem(400));

		List<Integer> left = list.stream().map(t -> t._1()).collect(Collectors.toList());
		assertThat(Arrays.asList(1,2,3,4,5,6),hasItem(left.get(0)));


	}
	@Test
	public void zip3(){
		List<Tuple3<Integer,Integer,Character>> list =
				DuplicationTest.of(1,2,3,4,5,6).actOnFutures().zip3(DuplicationTest.of(100,200,300,400), DuplicationTest.of('a','b','c'))
												.peek(it -> System.out.println(it))

												.collect(Collectors.toList());

		List<Integer> right = list.stream().map(t -> t._2()).collect(Collectors.toList());
		assertThat(right,hasItem(100));
		assertThat(right,hasItem(200));
		assertThat(right,hasItem(300));
		assertThat(right,not(hasItem(400)));

		List<Integer> left = list.stream().map(t -> t._1()).collect(Collectors.toList());
		assertThat(Arrays.asList(1,2,3,4,5,6),hasItem(left.get(0)));

		List<Character> three = list.stream().map(t -> t._3()).collect(Collectors.toList());
		assertThat(Arrays.asList('a','b','c'),hasItem(three.get(0)));


	}
	@Test
	public void zip3Lfs(){
		List<Tuple3<Integer,Integer,Character>> list =
				DuplicationTest.of(1,2,3,4,5,6).actOnFutures().zip3WithFutureStream(DuplicationTest.of(100,200,300,400), DuplicationTest.of('a','b','c'))
												.peek(it -> System.out.println(it))

												.collect(Collectors.toList());

		List<Integer> right = list.stream().map(t -> t._2()).collect(Collectors.toList());
		assertThat(right,hasItem(100));
		assertThat(right,hasItem(200));
		assertThat(right,hasItem(300));
		assertThat(right,not(hasItem(400)));

		List<Integer> left = list.stream().map(t -> t._1()).collect(Collectors.toList());
		assertThat(Arrays.asList(1,2,3,4,5,6),hasItem(left.get(0)));

		List<Character> three = list.stream().map(t -> t._3()).collect(Collectors.toList());
		assertThat(Arrays.asList('a','b','c'),hasItem(three.get(0)));


	}
	@Test
	public void zip4(){
		List<Tuple4<Integer,Integer,Character,String>> list =
				DuplicationTest.of(1,2,3,4,5,6).actOnFutures().zip4(DuplicationTest.of(100,200,300,400), DuplicationTest.of('a','b','c'), DuplicationTest.of("hello","world"))
												.peek(it -> System.out.println(it))

												.collect(Collectors.toList());
		System.out.println(list);
		List<Integer> right = list.stream().map(t -> t._2()).collect(Collectors.toList());
		assertThat(right,hasItem(100));
		assertThat(right,hasItem(200));
		assertThat(right,not(hasItem(300)));
		assertThat(right,not(hasItem(400)));

		List<Integer> left = list.stream().map(t -> t._1()).collect(Collectors.toList());
		assertThat(Arrays.asList(1,2,3,4,5,6),hasItem(left.get(0)));

		List<Character> three = list.stream().map(t -> t._3()).collect(Collectors.toList());
		assertThat(Arrays.asList('a','b','c'),hasItem(three.get(0)));

		List<String> four = list.stream().map(t -> t._4()).collect(Collectors.toList());
		assertThat(Arrays.asList("hello","world"),hasItem(four.get(0)));


	}
	@Test
	public void zip4Lfs(){
		List<Tuple4<Integer,Integer,Character,String>> list =
				DuplicationTest.of(1,2,3,4,5,6).actOnFutures().zip4WithFutureStream(DuplicationTest.of(100,200,300,400), DuplicationTest.of('a','b','c'), DuplicationTest.of("hello","world"))
												.peek(it -> System.out.println(it))

												.collect(Collectors.toList());
		System.out.println(list);
		List<Integer> right = list.stream().map(t -> t._2()).collect(Collectors.toList());
		assertThat(right,hasItem(100));
		assertThat(right,hasItem(200));
		assertThat(right,not(hasItem(300)));
		assertThat(right,not(hasItem(400)));

		List<Integer> left = list.stream().map(t -> t._1()).collect(Collectors.toList());
		assertThat(Arrays.asList(1,2,3,4,5,6),hasItem(left.get(0)));

		List<Character> three = list.stream().map(t -> t._3()).collect(Collectors.toList());
		assertThat(Arrays.asList('a','b','c'),hasItem(three.get(0)));

		List<String> four = list.stream().map(t -> t._4()).collect(Collectors.toList());
		assertThat(Arrays.asList("hello","world"),hasItem(four.get(0)));


	}

	@Test
	public void zip2of(){

		List<Tuple2<Integer,Integer>> list = DuplicationTest.of(1,2,3,4,5,6).actOnFutures()
											.zip(DuplicationTest.of(100,200,300,400))
											.peek(it -> System.out.println(it))
											.collect(Collectors.toList());


		List<Integer> right = list.stream().map(t -> t._2()).collect(Collectors.toList());
		assertThat(right,hasItem(100));
		assertThat(right,hasItem(200));
		assertThat(right,hasItem(300));
		assertThat(right,hasItem(400));

		List<Integer> left = list.stream().map(t -> t._1()).collect(Collectors.toList());
		assertThat(Arrays.asList(1,2,3,4,5,6),hasItem(left.get(0)));

	}
	@Test
	public void zipInOrder(){

		List<Tuple2<Integer,Integer>> list =  DuplicationTest.of(1,2,3,4,5,6).actOnFutures()
													.zip( DuplicationTest.of(100,200,300,400))
													.collect(Collectors.toList());

		assertThat(asList(1,2,3,4,5,6),hasItem(list.get(0)._1()));
		assertThat(asList(100,200,300,400),hasItem(list.get(0)._2()));



	}




	@Test
	public void testZipDifferingLength() {
		List<Tuple2<Integer, String>> list = DuplicationTest.of(1, 2).actOnFutures().zip(DuplicationTest.of("a", "b", "c", "d")).toList();

		assertEquals(2, list.size());
		assertTrue(asList(1, 2).contains(list.get(0)._1()));
		assertTrue("" + list.get(1)._2(), asList(1, 2).contains(list.get(1)._1()));
		assertTrue(asList("a", "b", "c", "d").contains(list.get(0)._2()));
		assertTrue(asList("a", "b", "c", "d").contains(list.get(1)._2()));

	}




	@Test
	public void testZipDifferingLengthStream() {
		List<Tuple2<Integer, String>> list = DuplicationTest.of(1, 2).actOnFutures().zipWithFutureStream(DuplicationTest.of("a", "b", "c", "d")).toList();

		assertEquals(2, list.size());
		assertTrue(asList(1, 2).contains(list.get(0)._1()));
		assertTrue("" + list.get(1)._2(), asList(1, 2).contains(list.get(1)._1()));
		assertTrue(asList("a", "b", "c", "d").contains(list.get(0)._2()));
		assertTrue(asList("a", "b", "c", "d").contains(list.get(1)._2()));

	}



	@Test
	public void testZipWithIndex() {
		Assert.assertEquals(asList(), DuplicationTest.of().actOnFutures().zipWithIndex().toList());

		assertThat(DuplicationTest.of("a").zipWithIndex().map(t -> t._2()).findFirst().get(), is(0l));
		Assert.assertEquals(asList(Tuple.tuple("a", 0L)), DuplicationTest.of("a").zipWithIndex().toList());

	}


}
