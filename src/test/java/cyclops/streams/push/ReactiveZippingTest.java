package cyclops.streams.push;

import cyclops.stream.ReactiveSeq;
import cyclops.stream.Spouts;
import org.jooq.lambda.Seq;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.jooq.lambda.tuple.Tuple4;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static cyclops.stream.Spouts.of;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.*;
import static org.jooq.lambda.tuple.Tuple.tuple;
import static org.junit.Assert.*;

public class ReactiveZippingTest {
	ReactiveSeq<Integer> empty;
	ReactiveSeq<Integer> nonEmpty;

	@Before
	public void setup(){
		empty = Spouts.of();
		nonEmpty = Spouts.of(1);
	}

	@Test
	public void zipInOrderNoLimit(){

		List<Tuple2<Integer,Integer>> list =  of(1,2,3,4,5,6)
				.zip( of(100,200,300,400))
				.collect(Collectors.toList());
		/**
		 List<Tuple2<Integer,Integer>> list =  of(1,2,3,4,5,6).limit(6)
		 .zip( of(100,200,300,400).limit(4))
		 .collect(Collectors.toList());**/

		assertThat(list.get(0).v1,is(1));
		assertThat(list.get(0).v2,is(100));
		assertThat(list.get(1).v1,is(2));
		assertThat(list.get(1).v2,is(200));
		assertThat(list.get(2).v1,is(3));
		assertThat(list.get(2).v2,is(300));
		assertThat(list.get(3).v1,is(4));
		assertThat(list.get(3).v2,is(400));



	}
	@Test
	public void zipUnevenRight(){
		assertEquals(asList("a"), of("a").toList());
		assertEquals(asList(tuple("a", 0L)), of("a").zip(of(0L,1L,2L)).toList());
		assertEquals(asList(tuple("a", 0L),tuple("b",1L)), of("a","b").zip(of(0L,1L,2L,3L)).toList());
		assertEquals(asList(tuple("a", 0L),tuple("b",1L)), of("a","b").zip(of(0L,1L,2L,3L,4L)).toList());
		assertEquals(asList(tuple("a", 0L),tuple("b",1L)), of("a","b").zip(of(0L,1L,2L,3L,4L,5L)).toList());
		assertEquals(asList(tuple( 0L,"a"),tuple(1L,"b")), of(0L,1L).zip(of("a","b","c","d")).toList());
	}

	@Test
	public void unevenTest(){
		assertEquals(asList(tuple("a", 0L)), of("a","b","c").zip(of(0L)).toList());
	}
	@Test
	public void zipUnevenLeft(){

		assertEquals(asList(tuple("a", 0L)), of("a","b").zip(of(0L)).toList());
		assertEquals(asList(tuple("a", 0L),tuple("b",1L)), of("a","b","c").zip(of(0L,1L)).toList());
		assertEquals(asList(tuple("a", 0L),tuple("b",1L)), of("a","b","c").zip(of(0L,1L)).collect(Collectors.toList()));
		assertEquals(asList(tuple("a", 0L),tuple("b",1L)), of("a","b","c","d").zip(of(0L,1L)).toList());

	}

	@Test
	public void zip1(){
		assertEquals(asList("a"), of("a").toList());
		assertEquals(asList(tuple("a", 0L)), of("a").zip(of(0L)).toList());
	}
	@Test
	public void zip(){
		List<Tuple2<Integer,Integer>> list =
				of(1,2,3,4,5,6).zip(of(100,200,300,400))
												.peek(it -> System.out.println(it))
												
												.collect(Collectors.toList());
		System.out.println(list);
		
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
				of(1,2,3,4,5,6).zip3(of(100,200,300,400),of('a','b','c'))
												.peek(it -> System.out.println(it))
												
												.collect(Collectors.toList());
		
		System.out.println(list);
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
				of(1,2,3,4,5,6).zip4(of(100,200,300,400),of('a','b','c'),of("hello","world"))
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
		
		List<Tuple2<Integer,Integer>> list =of(1,2,3,4,5,6)
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
		
		List<Tuple2<Integer,Integer>> list =  of(1,2,3,4,5,6)
													.zip( of(100,200,300,400))
													.collect(Collectors.toList());
		
		assertThat(asList(1,2,3,4,5,6),hasItem(list.get(0).v1));
		assertThat(asList(100,200,300,400),hasItem(list.get(0).v2));
		
		
		
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
		
		final ReactiveSeq<String> first = of("A", "B", "C");
		final ReactiveSeq<Integer> second = of(1, 2, 3);

		
		final ReactiveSeq<String> zipped = first.zip(second, (a, b) -> a + b);

		
		assertThat(zipped.collect(Collectors.toList()).size(),is(3));
	}

	

	@Test
	public void shouldTrimSecondFixedSeqIfLonger() throws Exception {
		final ReactiveSeq<String> first = of("A", "B", "C");
		final ReactiveSeq<Integer> second = of(1, 2, 3, 4);

		
		final ReactiveSeq<String> zipped = first.zip(second, (a, b) -> a + b);

		assertThat(zipped.collect(Collectors.toList()).size(),is(3));
	}

	@Test
	public void shouldTrimFirstFixedSeqIfLonger() throws Exception {
		final ReactiveSeq<String> first = of("A", "B", "C","D");
		final ReactiveSeq<Integer> second = of(1, 2, 3);
		final ReactiveSeq<String> zipped = first.zip(second, (a, b) -> a + b);

		
		assertThat(zipped.collect(Collectors.toList()).size(),equalTo(3));
	}

	@Test
	public void testZipDifferingLength() {
		List<Tuple2<Integer, String>> list = of(1, 2).zip(of("a", "b", "c", "d")).toList();

		assertEquals(2, list.size());
		assertTrue(asList(1, 2).contains(list.get(0).v1));
		assertTrue("" + list.get(1).v2, asList(1, 2).contains(list.get(1).v1));
		assertTrue(asList("a", "b", "c", "d").contains(list.get(0).v2));
		assertTrue(asList("a", "b", "c", "d").contains(list.get(1).v2));

	}

	
	@Test
	public void shouldTrimSecondFixedSeqIfLongerStream() throws Exception {
		final ReactiveSeq<String> first = of("A", "B", "C");
		final ReactiveSeq<Integer> second = of(1, 2, 3, 4);

		
		final ReactiveSeq<String> zipped = first.zipS(second, (a, b) -> a + b);

		assertThat(zipped.collect(Collectors.toList()).size(),is(3));
	}

	@Test
	public void shouldTrimFirstFixedSeqIfLongerStream() throws Exception {
		final ReactiveSeq<String> first = of("A", "B", "C","D");
		final ReactiveSeq<Integer> second = of(1, 2, 3);
		final ReactiveSeq<String> zipped = first.zipS(second, (a, b) -> a + b);

		
		assertThat(zipped.collect(Collectors.toList()).size(),equalTo(3));
	}

	@Test
	public void testZipDifferingLengthStream() {
		List<Tuple2<Integer, String>> list = of(1, 2).zip(of("a", "b", "c", "d")).toList();

		assertEquals(2, list.size());
		assertTrue(asList(1, 2).contains(list.get(0).v1));
		assertTrue("" + list.get(1).v2, asList(1, 2).contains(list.get(1).v1));
		assertTrue(asList("a", "b", "c", "d").contains(list.get(0).v2));
		assertTrue(asList("a", "b", "c", "d").contains(list.get(1).v2));

	}

	@Test
	public void shouldTrimSecondFixedSeqIfLongerSequence() throws Exception {
		final ReactiveSeq<String> first = of("A", "B", "C");
		final ReactiveSeq<Integer> second = of(1, 2, 3, 4);

		
		final ReactiveSeq<String> zipped = first.zip(second, (a, b) -> a + b);

		assertThat(zipped.collect(Collectors.toList()).size(),is(3));
	}

	@Test
	public void shouldTrimFirstFixedSeqIfLongerSequence() throws Exception {
		final ReactiveSeq<String> first = of("A", "B", "C","D");
		final ReactiveSeq<Integer> second = of(1, 2, 3);
		final ReactiveSeq<String> zipped = first.zip(second, (a, b) -> a + b);

		
		assertThat(zipped.collect(Collectors.toList()).size(),equalTo(3));
	}

	
	@Test
	public void testZipWithIndex() {
		assertEquals(asList(), of().zipWithIndex().toList());

		assertThat(of("a").zipWithIndex().map(t -> t.v2).findFirst().get(), is(0l));
		assertEquals(asList(new Tuple2("a", 0L)), of("a").zipWithIndex().toList());

	}

	@Test
	public void testUnzip() {

		Supplier<ReactiveSeq<Tuple2<Integer, String>>> s = () -> of(new Tuple2(1, "a"), new Tuple2(2, "b"), new Tuple2(3, "c"));

		Tuple2<ReactiveSeq<Integer>, ReactiveSeq<String>> u1 = ReactiveSeq.unzip(s.get());

		assertTrue(u1.v1.toList().containsAll(Arrays.asList(1, 2, 3)));

		assertTrue(u1.v2.toList().containsAll(asList("a", "b", "c")));

	}

	@Test
	public void testUnzipWithLimits() {

		Supplier<ReactiveSeq<Tuple2<Integer, String>>> s = () -> of(new Tuple2(1, "a"), new Tuple2(2, "b"), new Tuple2(3, "c"));

		Tuple2<ReactiveSeq<Integer>, ReactiveSeq<String>> u1 = ReactiveSeq.unzip(s.get());

		assertTrue(u1.v1.limit(2).toList().containsAll(Arrays.asList(1, 2)));

		assertTrue(u1.v2.toList().containsAll(asList("a", "b", "c")));

	}

	@Test
	public void testUnzip3WithLimits() {

		Supplier<ReactiveSeq<Tuple3<Integer, String, Long>>> s = () -> of(new Tuple3(1, "a", 2l), new Tuple3(2, "b", 3l), new Tuple3(3, "c", 4l));

		Tuple3<ReactiveSeq<Integer>, ReactiveSeq<String>, ReactiveSeq<Long>> u1 = ReactiveSeq.unzip3(s.get());

		assertTrue(u1.v1.limit(1).toList().containsAll(Arrays.asList(1)));

		assertTrue(u1.v2.limit(2).toList().containsAll(asList("a", "b")));
		assertTrue(u1.v3.toList().containsAll(asList(2l, 3l, 4l)));

	}

	@Test
	public void testUnzip3() {

		Supplier<ReactiveSeq<Tuple3<Integer, String, Long>>> s = () -> of(new Tuple3(1, "a", 2l), new Tuple3(2, "b", 3l), new Tuple3(3, "c", 4l));

		Tuple3<ReactiveSeq<Integer>, ReactiveSeq<String>, ReactiveSeq<Long>> u1 = ReactiveSeq.unzip3(s.get());

		assertTrue(u1.v1.toList().containsAll(Arrays.asList(1, 2, 3)));

		assertTrue(u1.v2.toList().containsAll(asList("a", "b", "c")));
		assertTrue(u1.v3.toList().containsAll(asList(2l, 3l, 4l)));

	}

	@Test
	public void testUnzip4() {

		Supplier<ReactiveSeq<Tuple4<Integer, String, Long, Character>>> s = () -> of(new Tuple4(1, "a", 2l, 'z'), new Tuple4(2, "b", 3l, 'y'), new Tuple4(3, "c",
				4l, 'x'));

		Tuple4<ReactiveSeq<Integer>, ReactiveSeq<String>, ReactiveSeq<Long>, ReactiveSeq<Character>> u1 = ReactiveSeq.unzip4(s.get());

		assertTrue(u1.v1.toList().containsAll(Arrays.asList(1, 2, 3)));

		assertTrue(u1.v2.toList().containsAll(asList("a", "b", "c")));

		assertTrue(u1.v3.toList().containsAll(asList(2l, 3l, 4l)));
		assertTrue(u1.v4.toList().containsAll(asList('z', 'y', 'x')));

	}

	@Test
	public void testUnzip4WithLimits() {

		Supplier<ReactiveSeq<Tuple4<Integer, String, Long, Character>>> s = () -> of(new Tuple4(1, "a", 2l, 'z'), new Tuple4(2, "b", 3l, 'y'), new Tuple4(3, "c",
				4l, 'x'));

		Tuple4<ReactiveSeq<Integer>, ReactiveSeq<String>, ReactiveSeq<Long>, ReactiveSeq<Character>> u1 = ReactiveSeq.unzip4(s.get());

		assertTrue(u1.v1.limit(1).toList().containsAll(Arrays.asList(1)));

		assertTrue(u1.v2.limit(2).toList().containsAll(asList("a", "b")));

		assertTrue(u1.v3.limit(3).toList().containsAll(asList(2l, 3l, 4l)));
		assertTrue(u1.v4.limit(4).toList().containsAll(asList('z', 'y', 'x')));

	}

}
