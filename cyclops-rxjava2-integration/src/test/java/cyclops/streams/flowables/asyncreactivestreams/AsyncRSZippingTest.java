package cyclops.streams.flowables.asyncreactivestreams;


import cyclops.companion.rx2.Flowables;
import cyclops.reactive.FlowableReactiveSeq;
import cyclops.reactive.ReactiveSeq;
import cyclops.reactive.Spouts;
import cyclops.data.tuple.Tuple;
import cyclops.data.tuple.Tuple2;
import cyclops.data.tuple.Tuple3;
import cyclops.data.tuple.Tuple4;
import org.junit.Before;
import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.*;
import static cyclops.data.tuple.Tuple.tuple;
import static org.junit.Assert.*;

public class AsyncRSZippingTest {
	ReactiveSeq<Integer> empty;
	ReactiveSeq<Integer> nonEmpty;

	@Before
	public void setup(){
		empty = of();
		nonEmpty = of(1);
	}

	protected <U> ReactiveSeq<U> of(U... array){

		return FlowableReactiveSeq.reactiveSeq(Flux.just(array).subscribeOn(Schedulers.fromExecutor(ForkJoinPool.commonPool())));

	}
    protected <U> Flux<U> flux(U... array){

        return Flux.just(array).subscribeOn(Schedulers.fromExecutor(ForkJoinPool.commonPool()));

    }
    protected <U> ReactiveSeq<U> withSleep(U... array){

        return Spouts.from(Flux.just(array).subscribeOn(Schedulers.fromExecutor(ForkJoinPool.commonPool()))).peek(c->{
            try {
                System.out.println("Sleeping " + c  + "T" + Thread.currentThread().getId());
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

    }



	@Test
	public void zipLatest(){

	    for(int i=0;i<10_000;i++) {
            List<Tuple2<Integer, Integer>> list = of(1, 2, 3, 4, 5, 6)
                    .zipLatest(of(100, 200, 300, 400), Tuple::tuple)
                    .collect(Collectors.toList());

            System.out.println(list);
            assertThat(list.size(),greaterThan(3));
            if(list.size()==4){
                list.forEach(t->{
                    assertThat(t._1(),equalTo(6));
                });
            }
        }



	}
	@Test
	public void zipInOrderNoLimit(){

		List<Tuple2<Integer,Integer>> list =  of(1,2,3,4,5,6)
				.zip( of(100,200,300,400))
				.collect(Collectors.toList());

		assertThat(list.get(0)._1(),is(1));
		assertThat(list.get(0)._2(),is(100));
		assertThat(list.get(1)._1(),is(2));
		assertThat(list.get(1)._2(),is(200));
		assertThat(list.get(2)._1(),is(3));
		assertThat(list.get(2)._2(),is(300));
		assertThat(list.get(3)._1(),is(4));
		assertThat(list.get(3)._2(),is(400));



	}
	@Test
	public void zipUnevenRight(){
		for(int i=0;i<20;i++) {
			System.out.println(i);
			assertEquals(asList("a"), of("a").toList());
			assertEquals(asList(tuple("a", 0L)), of("a").zip(of(0L, 1L, 2L)).toList());
			assertEquals(asList(tuple("a", 0L), tuple("b", 1L)), of("a", "b").zip(of(0L, 1L, 2L, 3L)).toList());
			assertEquals(asList(tuple("a", 0L), tuple("b", 1L)), of("a", "b").zip(of(0L, 1L, 2L, 3L, 4L)).toList());
			assertEquals(asList(tuple("a", 0L), tuple("b", 1L)), of("a", "b").zip(of(0L, 1L, 2L, 3L, 4L, 5L)).toList());
			assertEquals(asList(tuple(0L, "a"), tuple(1L, "b")), of(0L, 1L).zip(of("a", "b", "c", "d")).toList());
		}
	}

	@Test
	public void unevenTest(){
		assertEquals(asList(tuple("a", 0L)), of("a","b","c").zip(of(0L)).toList());
	}
	@Test
	public void zipUnevenLeft(){
		for(int i=0;i<20;i++) {
			System.out.println(i);
			assertEquals(asList(tuple("a", 0L)), of("a", "b").zip(of(0L)).toList());
			assertEquals(asList(tuple("a", 0L), tuple("b", 1L)), of("a", "b", "c").zip(of(0L, 1L)).toList());
			assertEquals(asList(tuple("a", 0L), tuple("b", 1L)), of("a", "b", "c").zip(of(0L, 1L)).collect(Collectors.toList()));
			assertEquals(asList(tuple("a", 0L), tuple("b", 1L)), of("a", "b", "c", "d").zip(of(0L, 1L)).toList());
		}
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
				of(1,2,3,4,5,6).zip3(of(100,200,300,400),of('a','b','c'))
												.peek(it -> System.out.println(it))

												.collect(Collectors.toList());

		System.out.println(list);
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
				of(1,2,3,4,5,6).zip4(of(100,200,300,400),of('a','b','c'),of("hello","world"))
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

	    for(int i=0;i<10;i++) {
	        System.out.println("i is " + i);
            List<Tuple2<Integer, Integer>> list = of(1, 2, 3, 4, 5, 6)
                    .zip(of(100, 200, 300, 400))
                    .peek(it -> System.out.println("Peeking " + it))
                    .collect(Collectors.toList());


            List<Integer> right = list.stream().map(t -> t._2()).collect(Collectors.toList());
            System.out.println("Right is " + right + " list is " +  list);
            assertThat(right, hasItem(100));
            assertThat(right, hasItem(200));
            assertThat(right, hasItem(300));
            assertThat(right, hasItem(400));

            List<Integer> left = list.stream().map(t -> t._1()).collect(Collectors.toList());
            assertThat(Arrays.asList(1, 2, 3, 4, 5, 6), hasItem(left.get(0)));
        }

	}
	@Test
	public void zipInOrder(){

		List<Tuple2<Integer,Integer>> list =  of(1,2,3,4,5,6)
													.zip( of(100,200,300,400))
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
		assertTrue(asList(1, 2).contains(list.get(0)._1()));
		assertTrue("" + list.get(1)._2(), asList(1, 2).contains(list.get(1)._1()));
		assertTrue(asList("a", "b", "c", "d").contains(list.get(0)._2()));
		assertTrue(asList("a", "b", "c", "d").contains(list.get(1)._2()));

	}


	@Test
	public void shouldTrimSecondFixedSeqIfLongerStream() throws Exception {
		final ReactiveSeq<String> first = of("A", "B", "C");
		final ReactiveSeq<Integer> second = of(1, 2, 3, 4);


		final ReactiveSeq<String> zipped = first.zipWithStream(second, (a, b) -> a + b);

		assertThat(zipped.collect(Collectors.toList()).size(),is(3));
	}

	@Test
	public void shouldTrimFirstFixedSeqIfLongerStream() throws Exception {
		final ReactiveSeq<String> first = of("A", "B", "C","D");
		final ReactiveSeq<Integer> second = of(1, 2, 3);
		final ReactiveSeq<String> zipped = first.zipWithStream(second, (a, b) -> a + b);


		assertThat(zipped.collect(Collectors.toList()).size(),equalTo(3));
	}

	@Test
	public void testZipDifferingLengthStream() {
		List<Tuple2<Integer, String>> list = of(1, 2).zip(of("a", "b", "c", "d")).toList();

		assertEquals(2, list.size());
		assertTrue(asList(1, 2).contains(list.get(0)._1()));
		assertTrue("" + list.get(1)._2(), asList(1, 2).contains(list.get(1)._1()));
		assertTrue(asList("a", "b", "c", "d").contains(list.get(0)._2()));
		assertTrue(asList("a", "b", "c", "d").contains(list.get(1)._2()));

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
	public void zipWithIndexToList(){
		of("a").zipWithIndex().toList();
	}
	@Test
	public void testZipWithIndex() {
		assertEquals(asList(), of().zipWithIndex().toList());

		assertThat(of("a").zipWithIndex().map(t -> t._2()).findFirst().get(), is(0l));
		assertEquals(asList(new Tuple2("a", 0L)), of("a").zipWithIndex().toList());

	}

	@Test
	public void testUnzip() {

		Supplier<ReactiveSeq<Tuple2<Integer, String>>> s = () -> of(new Tuple2(1, "a"), new Tuple2(2, "b"), new Tuple2(3, "c"));

		Tuple2<ReactiveSeq<Integer>, ReactiveSeq<String>> u1 = ReactiveSeq.unzip(s.get());

		assertTrue(u1._1().toList().containsAll(Arrays.asList(1, 2, 3)));

		assertTrue(u1._2().toList().containsAll(asList("a", "b", "c")));

	}

	@Test
	public void testUnzipWithLimits() {

		Supplier<ReactiveSeq<Tuple2<Integer, String>>> s = () -> of(new Tuple2(1, "a"), new Tuple2(2, "b"), new Tuple2(3, "c"));

		Tuple2<ReactiveSeq<Integer>, ReactiveSeq<String>> u1 = ReactiveSeq.unzip(s.get());

		assertTrue(u1._1().limit(2).toList().containsAll(Arrays.asList(1, 2)));

		assertTrue(u1._2().toList().containsAll(asList("a", "b", "c")));

	}

	@Test
	public void testUnzip3WithLimits() {

		Supplier<ReactiveSeq<Tuple3<Integer, String, Long>>> s = () -> of(new Tuple3(1, "a", 2l), new Tuple3(2, "b", 3l), new Tuple3(3, "c", 4l));

		Tuple3<ReactiveSeq<Integer>, ReactiveSeq<String>, ReactiveSeq<Long>> u1 = ReactiveSeq.unzip3(s.get());

		assertTrue(u1._1().limit(1).toList().containsAll(Arrays.asList(1)));

		assertTrue(u1._2().limit(2).toList().containsAll(asList("a", "b")));
		assertTrue(u1._3().toList().containsAll(asList(2l, 3l, 4l)));

	}

	@Test
	public void testUnzip3() {

		Supplier<ReactiveSeq<Tuple3<Integer, String, Long>>> s = () -> of(new Tuple3(1, "a", 2l), new Tuple3(2, "b", 3l), new Tuple3(3, "c", 4l));

		Tuple3<ReactiveSeq<Integer>, ReactiveSeq<String>, ReactiveSeq<Long>> u1 = ReactiveSeq.unzip3(s.get());

		assertTrue(u1._1().toList().containsAll(Arrays.asList(1, 2, 3)));

		assertTrue(u1._2().toList().containsAll(asList("a", "b", "c")));
		assertTrue(u1._3().toList().containsAll(asList(2l, 3l, 4l)));

	}

	@Test
	public void testUnzip4() {

		Supplier<ReactiveSeq<Tuple4<Integer, String, Long, Character>>> s = () -> of(new Tuple4(1, "a", 2l, 'z'), new Tuple4(2, "b", 3l, 'y'), new Tuple4(3, "c",
				4l, 'x'));

		Tuple4<ReactiveSeq<Integer>, ReactiveSeq<String>, ReactiveSeq<Long>, ReactiveSeq<Character>> u1 = ReactiveSeq.unzip4(s.get());

		assertTrue(u1._1().toList().containsAll(Arrays.asList(1, 2, 3)));

		assertTrue(u1._2().toList().containsAll(asList("a", "b", "c")));

		assertTrue(u1._3().toList().containsAll(asList(2l, 3l, 4l)));
		assertTrue(u1._4().toList().containsAll(asList('z', 'y', 'x')));

	}

	@Test
	public void testUnzip4WithLimits() {

		Supplier<ReactiveSeq<Tuple4<Integer, String, Long, Character>>> s = () -> of(new Tuple4(1, "a", 2l, 'z'), new Tuple4(2, "b", 3l, 'y'), new Tuple4(3, "c",
				4l, 'x'));

		Tuple4<ReactiveSeq<Integer>, ReactiveSeq<String>, ReactiveSeq<Long>, ReactiveSeq<Character>> u1 = ReactiveSeq.unzip4(s.get());

		assertTrue(u1._1().limit(1).toList().containsAll(Arrays.asList(1)));

		assertTrue(u1._2().limit(2).toList().containsAll(asList("a", "b")));

		assertTrue(u1._3().limit(3).toList().containsAll(asList(2l, 3l, 4l)));
		assertTrue(u1._4().limit(4).toList().containsAll(asList('z', 'y', 'x')));

	}

}
