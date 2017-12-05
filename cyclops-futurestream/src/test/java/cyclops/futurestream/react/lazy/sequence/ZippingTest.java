package cyclops.futurestream.react.lazy.sequence;


import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import cyclops.futurestream.react.lazy.DuplicationTest;
import cyclops.reactive.FutureStream;
import cyclops.data.tuple.Tuple2;
import cyclops.data.tuple.Tuple3;
import cyclops.data.tuple.Tuple4;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import cyclops.reactive.ReactiveSeq;

public class ZippingTest {
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
				DuplicationTest.of(1,2,3,4,5,6).zip(DuplicationTest.of(100,200,300,400))
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
				DuplicationTest.of(1,2,3,4,5,6).zip3(DuplicationTest.of(100,200,300,400), DuplicationTest.of('a','b','c'))
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
				DuplicationTest.of(1,2,3,4,5,6).zip4(DuplicationTest.of(100,200,300,400), DuplicationTest.of('a','b','c'), DuplicationTest.of("hello","world"))
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

		List<Tuple2<Integer,Integer>> list = DuplicationTest.of(1,2,3,4,5,6)
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

		List<Tuple2<Integer,Integer>> list =  DuplicationTest.of(1,2,3,4,5,6)
													.zip( DuplicationTest.of(100,200,300,400))
													.collect(Collectors.toList());

		assertThat(asList(1,2,3,4,5,6),hasItem(list.get(0)._1()));
		assertThat(asList(100,200,300,400),hasItem(list.get(0)._2()));



	}

	@Test
	public void zipEmpty() throws Exception {


		final ReactiveSeq<Integer> zipped = empty.zip(ReactiveSeq.<Integer>of(), (a, b) -> a + b);
		assertTrue(zipped.collect(Collectors.toList()).isEmpty());
	}

	@Test
	public void shouldReturnEmptySeqWhenZipEmptyWithNonEmpty() throws Exception {



		final ReactiveSeq<Integer> zipped = empty.zip(nonEmpty, (a, b) -> a + b);
		assertTrue(zipped.collect(Collectors.toList()).isEmpty());
	}

	@Test
	public void shouldReturnEmptySeqWhenZipNonEmptyWithEmpty() throws Exception {


		final ReactiveSeq<Integer> zipped = nonEmpty.zip(empty, (a, b) -> a + b);


		assertTrue(zipped.collect(Collectors.toList()).isEmpty());
	}

	@Test
	public void shouldZipTwoFiniteSequencesOfSameSize() throws Exception {

		final ReactiveSeq<String> first = DuplicationTest.of("A", "B", "C");
		final ReactiveSeq<Integer> second = DuplicationTest.of(1, 2, 3);


		final ReactiveSeq<String> zipped = first.zip(second, (a, b) -> a + b);


		assertThat(zipped.collect(Collectors.toList()).size(),is(3));
	}



	@Test
	public void shouldTrimSecondFixedSeqIfLonger() throws Exception {
		final ReactiveSeq<String> first = DuplicationTest.of("A", "B", "C");
		final ReactiveSeq<Integer> second = DuplicationTest.of(1, 2, 3, 4);


		final ReactiveSeq<String> zipped = first.zip(second, (a, b) -> a + b);

		assertThat(zipped.collect(Collectors.toList()).size(),is(3));
	}

	@Test
	public void shouldTrimFirstFixedSeqIfLonger() throws Exception {
		final ReactiveSeq<String> first = DuplicationTest.of("A", "B", "C","D");
		final ReactiveSeq<Integer> second = DuplicationTest.of(1, 2, 3);
		final ReactiveSeq<String> zipped = first.zip(second, (a, b) -> a + b);


		assertThat(zipped.collect(Collectors.toList()).size(),equalTo(3));
	}

	@Test
	public void testZipDifferingLength() {
		List<Tuple2<Integer, String>> list = DuplicationTest.of(1, 2).zip(DuplicationTest.of("a", "b", "c", "d")).toList();

		assertEquals(2, list.size());
		assertTrue(asList(1, 2).contains(list.get(0)._1()));
		assertTrue("" + list.get(1)._2(), asList(1, 2).contains(list.get(1)._1()));
		assertTrue(asList("a", "b", "c", "d").contains(list.get(0)._2()));
		assertTrue(asList("a", "b", "c", "d").contains(list.get(1)._2()));

	}


	@Test
	public void shouldTrimSecondFixedSeqIfLongerStream() throws Exception {
		final ReactiveSeq<String> first = DuplicationTest.of("A", "B", "C");
		final ReactiveSeq<Integer> second = DuplicationTest.of(1, 2, 3, 4);


		final ReactiveSeq<String> zipped = first.zipWithStream(second, (a, b) -> a + b);

		assertThat(zipped.collect(Collectors.toList()).size(),is(3));
	}

	@Test
	public void shouldTrimFirstFixedSeqIfLongerStream() throws Exception {
		final ReactiveSeq<String> first = DuplicationTest.of("A", "B", "C","D");
		final ReactiveSeq<Integer> second = DuplicationTest.of(1, 2, 3);
		final ReactiveSeq<String> zipped = first.zipWithStream(second, (a, b) -> a + b);


		assertThat(zipped.collect(Collectors.toList()).size(),equalTo(3));
	}

	@Test
	public void testZipDifferingLengthStream() {
		List<Tuple2<Integer, String>> list = DuplicationTest.of(1, 2).zip(DuplicationTest.of("a", "b", "c", "d")).toList();

		assertEquals(2, list.size());
		assertTrue(asList(1, 2).contains(list.get(0)._1()));
		assertTrue("" + list.get(1)._2(), asList(1, 2).contains(list.get(1)._1()));
		assertTrue(asList("a", "b", "c", "d").contains(list.get(0)._2()));
		assertTrue(asList("a", "b", "c", "d").contains(list.get(1)._2()));

	}

	@Test
	public void shouldTrimSecondFixedSeqIfLongerSequence() throws Exception {
		final ReactiveSeq<String> first = DuplicationTest.of("A", "B", "C");
		final ReactiveSeq<Integer> second = DuplicationTest.of(1, 2, 3, 4);


		final ReactiveSeq<String> zipped = first.zip(second, (a, b) -> a + b);

		assertThat(zipped.collect(Collectors.toList()).size(),is(3));
	}

	@Test
	public void shouldTrimFirstFixedSeqIfLongerSequence() throws Exception {
		final ReactiveSeq<String> first = DuplicationTest.of("A", "B", "C","D");
		final ReactiveSeq<Integer> second = DuplicationTest.of(1, 2, 3);
		final ReactiveSeq<String> zipped = first.zip(second, (a, b) -> a + b);


		assertThat(zipped.collect(Collectors.toList()).size(),equalTo(3));
	}


	@Test
	public void testZipWithIndex() {
		Assert.assertEquals(asList(), DuplicationTest.of().zipWithIndex().toList());

		assertThat(DuplicationTest.of("a").zipWithIndex().map(t -> t._2()).findFirst().get(), is(0l));
		Assert.assertEquals(asList(new Tuple2("a", 0L)), DuplicationTest.of("a").zipWithIndex().toList());

	}

	@Test
	public void testUnzip() {

		Supplier<ReactiveSeq<Tuple2<Integer, String>>> s = () -> DuplicationTest.of(new Tuple2(1, "a"), new Tuple2(2, "b"), new Tuple2(3, "c"));

		Tuple2<ReactiveSeq<Integer>, ReactiveSeq<String>> u1 = ReactiveSeq.unzip(s.get());

		assertTrue(u1._1().toList().containsAll(Arrays.asList(1, 2, 3)));

		assertTrue(u1._2().toList().containsAll(asList("a", "b", "c")));

	}

	@Test
	public void testUnzipWithLimits() {

		Supplier<ReactiveSeq<Tuple2<Integer, String>>> s = () -> DuplicationTest.of(new Tuple2(1, "a"), new Tuple2(2, "b"), new Tuple2(3, "c"));

		Tuple2<ReactiveSeq<Integer>, ReactiveSeq<String>> u1 = ReactiveSeq.unzip(s.get());

		assertTrue(u1._1().limit(2).toList().containsAll(Arrays.asList(1, 2)));

		assertTrue(u1._2().toList().containsAll(asList("a", "b", "c")));

	}

	@Test
	public void testUnzip3WithLimits() {

		Supplier<ReactiveSeq<Tuple3<Integer, String, Long>>> s = () -> DuplicationTest.of(new Tuple3(1, "a", 2l), new Tuple3(2, "b", 3l), new Tuple3(3, "c", 4l));

		Tuple3<ReactiveSeq<Integer>, ReactiveSeq<String>, ReactiveSeq<Long>> u1 = ReactiveSeq.unzip3(s.get());

		assertTrue(u1._1().limit(1).toList().containsAll(Arrays.asList(1)));

		assertTrue(u1._2().limit(2).toList().containsAll(asList("a", "b")));
		assertTrue(u1._3().toList().containsAll(asList(2l, 3l, 4l)));

	}

	@Test
	public void testUnzip3() {

		Supplier<ReactiveSeq<Tuple3<Integer, String, Long>>> s = () -> DuplicationTest.of(new Tuple3(1, "a", 2l), new Tuple3(2, "b", 3l), new Tuple3(3, "c", 4l));

		Tuple3<ReactiveSeq<Integer>, ReactiveSeq<String>, ReactiveSeq<Long>> u1 = ReactiveSeq.unzip3(s.get());

		assertTrue(u1._1().toList().containsAll(Arrays.asList(1, 2, 3)));

		assertTrue(u1._2().toList().containsAll(asList("a", "b", "c")));
		assertTrue(u1._3().toList().containsAll(asList(2l, 3l, 4l)));

	}

	@Test
	public void testUnzip4() {

		Supplier<ReactiveSeq<Tuple4<Integer, String, Long, Character>>> s = () -> DuplicationTest.of(new Tuple4(1, "a", 2l, 'z'), new Tuple4(2, "b", 3l, 'y'), new Tuple4(3, "c",
				4l, 'x'));

		Tuple4<ReactiveSeq<Integer>, ReactiveSeq<String>, ReactiveSeq<Long>, ReactiveSeq<Character>> u1 = ReactiveSeq.unzip4(s.get());

		assertTrue(u1._1().toList().containsAll(Arrays.asList(1, 2, 3)));

		assertTrue(u1._2().toList().containsAll(asList("a", "b", "c")));

		assertTrue(u1._3().toList().containsAll(asList(2l, 3l, 4l)));
		assertTrue(u1._4().toList().containsAll(asList('z', 'y', 'x')));

	}

	@Test
	public void testUnzip4WithLimits() {

		Supplier<ReactiveSeq<Tuple4<Integer, String, Long, Character>>> s = () -> DuplicationTest.of(new Tuple4(1, "a", 2l, 'z'), new Tuple4(2, "b", 3l, 'y'), new Tuple4(3, "c",
				4l, 'x'));

		Tuple4<ReactiveSeq<Integer>, ReactiveSeq<String>, ReactiveSeq<Long>, ReactiveSeq<Character>> u1 = ReactiveSeq.unzip4(s.get());

		assertTrue(u1._1().limit(1).toList().containsAll(Arrays.asList(1)));

		assertTrue(u1._2().limit(2).toList().containsAll(asList("a", "b")));

		assertTrue(u1._3().limit(3).toList().containsAll(asList(2l, 3l, 4l)));
		assertTrue(u1._4().limit(4).toList().containsAll(asList('z', 'y', 'x')));

	}

}
