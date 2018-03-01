package cyclops.monads.collections;

import static java.util.Arrays.asList;
import static java.util.Comparator.comparing;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static cyclops.data.tuple.Tuple.tuple;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.oath.anym.AnyMSeq;
import com.oath.cyclops.ReactiveConvertableSequence;
import cyclops.data.HashMap;
import cyclops.data.Seq;
import cyclops.data.Vector;
import cyclops.data.tuple.Tuple;
import cyclops.reactive.collections.immutable.VectorX;
import cyclops.companion.*;
import cyclops.futurestream.LazyReact;
import cyclops.control.Maybe;
import cyclops.control.Trampoline;

import cyclops.monads.AnyM;
import cyclops.monads.WitnessType;
import cyclops.reactive.ReactiveSeq;
import cyclops.reactive.Streamable;
import cyclops.data.tuple.Tuple2;
import cyclops.data.tuple.Tuple3;
import cyclops.data.tuple.Tuple4;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import cyclops.function.Monoid;
import cyclops.reactive.collections.mutable.ListX;

import com.oath.cyclops.util.SimpleTimer;

public abstract class AbstractAnyMSeqTest<W extends WitnessType<W>> {//@TODO extends AbstractIterableXTest{
	public abstract <T> AnyMSeq<W,T> empty();
	public abstract <T> AnyMSeq<W,T> of(T... values);

    public static final LazyReact r = new LazyReact(10,10);
   // prependS, append,append,prependAll,prependAll,insertAt,deleteBetween,insertAtS,recover
    @Test
    public void prependAppend(){
               assertThat(of(1).prependStream(Stream.of(2)).appendAll(3).prepend(4).appendAll(5,6).prependAll(7,8).insertAt(4,9).deleteBetween(1,2)
                .insertStreamAt(5,Stream.of(11,12)).stream().count(),equalTo(10L));
    }
    @Test
    public void prependAppendUpdate(){


        assertThat(of(1).prependStream(Stream.of(2)).appendAll(3).prepend(4).appendAll(5,6).prependAll(7,8)
                .insertAt(4,9)
                    .updateAt(4,10)
                .deleteBetween(1,2)
                .insertStreamAt(5,Stream.of(11,12)).stream().count(),equalTo(10L));
    }
    @Test
    public void prependAppendInsertAt(){

        assertThat(of(1).prependStream(Stream.of(2)).appendAll(3).prepend(4).appendAll(5,6).prependAll(7,8)
                    .insertAt(4,9,10)
                    .deleteBetween(1,2)
                .insertStreamAt(5,Stream.of(11,12)).stream().count(),equalTo(11L));
    }
    @Test
    public void prependAppendInsertAtAll(){

        assertThat(of(1).prependStream(Stream.of(2)).appendAll(3).prepend(4).appendAll(5,6).prependAll(7,8).insertAt(4,Arrays.asList(9,10)).deleteBetween(1,2)
                .insertStreamAt(5,Stream.of(11,12)).stream().count(),equalTo(11L));
    }
    @Test
    public void testRecover1(){
        assertThat(of(1,2,3).map(e->{throw new RuntimeException();}).recover(e->"hello").join(" "),equalTo("hello hello hello"));
    }
    @Test
    public void testRecover2(){
        assertThat(of(1,2,3).map(e->{throw new RuntimeException();}).recover(RuntimeException.class,e->"hello").join(" "),equalTo("hello hello hello"));
    }

    @Test
    public void testRetry(){
        of(1,2,3).retry(i->i+2).printOut();
    }
    @Test
    public void testRetryTime(){
        of(1,2,3).retry(i->i+2,3,1,TimeUnit.DAYS).printOut();
    }

    @Test
    public void stream(){

        assertThat(of(1,2,3).stream().collect(java.util.stream.Collectors.toList()),hasItems(1,2,3));
    }



    private void sleep2(int time){
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Test
    public void visit(){

        String res= of(1,2,3).visit((x,xs)-> xs.join(x>2? "hello" : "world"),
                                                              ()->"boo!");

        assertThat(res,equalTo("2world3"));
    }
    @Test
    public void whenGreaterThan2(){
        String res= of(5,2,3).visit((x,xs)->
                                xs.join(x>2? "hello" : "world"),()->"boo!");

        assertThat(res,equalTo("2hello3"));
    }
    @Test
    public void when2(){

        Integer res =   of(1,2,3).visit((x,xs)->x,()->10);
        System.out.println(res);
    }
    @Test
    public void whenNilOrNot(){
        String res1=    of(1,2,3).visit((x,xs)-> x>2? "hello" : "world",()->"EMPTY");
    }
    @Test
    public void whenNilOrNotJoinWithFirstElement(){


        String res= of(1,2,3).visit((x,xs)-> xs.join(x>2? "hello" : "world"),()->"EMPTY");
        assertThat(res,equalTo("2world3"));
    }

	@Test
	public void testCollectable(){
		assertThat(of(1,2,3).anyMatch(i->i==2),equalTo(true));
	}
	@Test
	public void dropRight(){
		assertThat(of(1,2,3).dropRight(1).toList(),hasItems(1,2));
	}
	@Test
	public void dropRightEmpty(){
		assertThat(of().dropRight(1).toList(),equalTo(Arrays.asList()));
	}

	@Test
	public void dropUntil(){
		assertThat(of(1,2,3,4,5).dropUntil(p->p==2).toList().size(),lessThan(5));
	}
	@Test
	public void dropUntilEmpty(){
		assertThat(of().dropUntil(p->true).toList(),equalTo(Arrays.asList()));
	}
	@Test
	public void dropWhile(){
		assertThat(of(1,2,3,4,5).dropWhile(p->p<6).toList().size(),lessThan(1));
	}
	@Test
	public void dropWhileEmpty(){
		assertThat(of().dropWhile(p->true).toList(),equalTo(Arrays.asList()));
	}
	@Test
	public void filter(){
	   // System.out.println( (Object) of(1,2,3,4,5).filter(i->i<3).unwrap());
		assertThat(of(1,2,3,4,5).filter(i->i<3).toList(),hasItems(1,2));
	}
	@Test
	public void findAny(){
		assertThat(of(1,2,3,4,5).findAny().get(),lessThan(6));
	}
	@Test
	public void findFirst(){
		assertThat(of(1,2,3,4,5).findFirst().get(),lessThan(6));
	}




	AnyMSeq<W,Integer> empty;
	AnyMSeq<W,Integer> nonEmpty;

	@Before
	public void setup(){
		empty = of();
		nonEmpty = of(1);
	}


	protected Object value() {

		return "jello";
	}
	private int value2() {

		return 200;
	}


	@Test
	public void batchBySize(){
		System.out.println(of(1,2,3,4,5,6).grouped(3).collect(java.util.stream.Collectors.toList()));
		assertThat(of(1,2,3,4,5,6).grouped(3).collect(java.util.stream.Collectors.toList()).size(),is(2));
	}





	@Test
	public void limitWhileTest(){

		List<Integer> list = new ArrayList<>();
		while(list.size()==0){
			list = of(1,2,3,4,5,6).takeWhile(it -> it<4)
						.peek(it -> System.out.println(it)).collect(java.util.stream.Collectors.toList());

		}
		assertThat(Arrays.asList(1,2,3,4,5,6),hasItem(list.get(0)));




	}

    @Test
    public void testScanLeftStringConcat() {
        assertThat(of("a", "b", "c").scanLeft("", String::concat).toList().size(),
        		is(4));
    }
    @Test
    public void testScanLeftSum() {
    	assertThat(of("a", "ab", "abc").map(str->str.length()).scanLeft(0, (u, t) -> u + t).toList().size(),
    			is(asList(0, 1, 3, 6).size()));
    }
    @Test
    public void testScanRightStringConcatMonoid() {
        assertThat(of("a", "b", "c").scanRight(Monoid.of("", String::concat)).toList().size(),
            is(asList("", "c", "bc", "abc").size()));
    }
    @Test
    public void testScanRightStringConcat() {
        assertThat(of("a", "b", "c").scanRight("", String::concat).toList().size(),
            is(asList("", "c", "bc", "abc").size()));
    }
    @Test
    public void testScanRightSum() {
    	assertThat(of("a", "ab", "abc").map(str->str.length()).scanRight(0, (t, u) -> u + t).toList().size(),
            is(asList(0, 3, 5, 6).size()));


    }









    @Test
    public void testIterable() {
        List<Integer> list = of(1, 2, 3).to().collection(LinkedList::new);

        for (Integer i :of(1, 2, 3)) {
            assertThat(list,hasItem(i));
        }
    }







	    @Test
	    public void testGroupByEager() {
	        HashMap<Integer, ListX<Integer>> mapA =of(1, 2, 3, 4).groupBy(i -> i % 2).map(ListX::fromIterable);
            Map<Integer, ListX<Integer>> map1 = mapA.javaMap();

	        assertThat(map1.get(0),hasItem(2));
	        assertThat(map1.get(0),hasItem(4));
	        assertThat(map1.get(1),hasItem(1));
	        assertThat(map1.get(1),hasItem(3));

	        assertEquals(2, map1.size());


	    }


	    @Test
	    public void testJoin() {
	        assertEquals("123".length(),of(1, 2, 3).join().length());
	        assertEquals("1, 2, 3".length(), of(1, 2, 3).join(", ").length());
	        assertEquals("^1|2|3$".length(), of(1, 2, 3).join("|", "^", "$").length());


	    }






	    @Test
	    public void testSkipWhile() {
	        Supplier<AnyMSeq<W,Integer>> s = () -> of(1, 2, 3, 4, 5);

	        assertTrue(s.get().dropWhile(i -> false).toList().containsAll(asList(1, 2, 3, 4, 5)));

	        assertEquals(asList(), s.get().dropWhile(i -> true).toList());
	    }

	    @Test
	    public void testSkipUntil() {
	        Supplier<AnyMSeq<W,Integer>> s = () -> of(1, 2, 3, 4, 5);

	        assertEquals(asList(), s.get().dropUntil(i -> false).toList());
	        assertTrue(s.get().dropUntil(i -> true).toList().containsAll(asList(1, 2, 3, 4, 5)));
		  }



	    @Test
	    public void testLimitWhile() {
	        Supplier<AnyMSeq<W,Integer>> s = () -> of(1, 2, 3, 4, 5);

	        assertEquals(asList(), s.get().takeWhile(i -> false).toList());
	        assertTrue( s.get().takeWhile(i -> i < 3).toList().size()!=5);
	        assertTrue(s.get().takeWhile(i -> true).toList().containsAll(asList(1, 2, 3, 4, 5)));
	    }

	    @Test
	    public void testLimitUntil() {


	        assertTrue(of(1, 2, 3, 4, 5).takeUntil(i -> false).toList().containsAll(asList(1, 2, 3, 4, 5)));
	        assertFalse(of(1, 2, 3, 4, 5).takeUntil(i -> i % 3 == 0).toList().size()==5);

	        assertEquals(asList(), of(1, 2, 3, 4, 5).takeUntil(i -> true).toList());
	    }





	    @Test
	    public void testMinByMaxBy() {
	        Supplier<AnyMSeq<W,Integer>> s = () -> of(1, 2, 3, 4, 5, 6);

	        assertEquals(1, (int) s.get().maxBy(t -> Math.abs(t - 5)).orElse(-1));
	        assertEquals(5, (int) s.get().minBy(t -> Math.abs(t - 5)).orElse(-1));

	        assertEquals(6, (int) s.get().maxBy(t -> "" + t).orElse(-1));
	        assertEquals(1, (int) s.get().minBy(t -> "" + t).orElse(-1));
	    }




		@Test
		public void onePer(){
			SimpleTimer timer = new SimpleTimer();
			System.out.println(of(1,2,3,4,5,6).onePer(1000,TimeUnit.NANOSECONDS).collect(java.util.stream.Collectors.toList()));
			assertThat(of(1,2,3,4,5,6).onePer(1000,TimeUnit.NANOSECONDS).collect(java.util.stream.Collectors.toList()).size(),is(6));
			assertThat(timer.getElapsedNanoseconds(),greaterThan(600l));
		}
		@Test
		public void xPer(){
			SimpleTimer timer = new SimpleTimer();
			System.out.println(of(1,2,3,4,5,6).xPer(6,1000,TimeUnit.NANOSECONDS).collect(java.util.stream.Collectors.toList()));
			assertThat(of(1,2,3,4,5,6).xPer(6,100000000,TimeUnit.NANOSECONDS).collect(java.util.stream.Collectors.toList()).size(),is(6));
			assertThat(timer.getElapsedNanoseconds(),lessThan(60000000l));
		}


		@Test
		public void zip(){
			List<Tuple2<Integer,Integer>> list =
					of(1,2,3,4,5,6).zip(of(100,200,300,400).stream())
													.peek(it -> System.out.println(it))
													.collect(java.util.stream.Collectors.toList());
			System.out.println("list = " +list);

			List<Integer> right = list.stream().map(t -> t._2()).collect(java.util.stream.Collectors.toList());

			assertThat(right,hasItem(100));
			assertThat(right,hasItem(200));
			assertThat(right,hasItem(300));
			assertThat(right,hasItem(400));

			List<Integer> left = list.stream().map(t -> t._1()).collect(java.util.stream.Collectors.toList());
			assertThat(Arrays.asList(1,2,3,4,5,6),hasItem(left.get(0)));


		}


		@Test
		public void testScanLeftStringConcatMonoid() {
			assertThat(of("a", "b", "c").scanLeft(Reducers.toString("")).toList(), is(asList("", "a", "ab", "abc")));
		}

		@Test
		public void testScanLeftSumMonoid() {

			assertThat(of("a", "ab", "abc").map(str -> str.length()).
								peek(System.out::println).scanLeft(Reducers.toTotalInt()).toList(), is(asList(0, 1, 3, 6)));
		}



		@Test
		public void testScanRightSumMonoid() {
			assertThat(of("a", "ab", "abc").peek(System.out::println)
										.map(str -> str.length())
										.peek(System.out::println)
										.scanRight(Reducers.toTotalInt()).toList(), is(asList(0, 3, 5, 6)));

		}


	@Test
	public void onEmptySwitchEmpty(){
		assertThat(of().stream()
							.onEmptySwitch(()->Stream.of(1,2,3))
							.toList(),
							equalTo(Arrays.asList(1,2,3)));

	}
	@Test
	public void onEmptySwitch(){
		assertThat(of(4,5,6).stream()
							.onEmptySwitch(()->Stream.of(1,2,3))
							.toList(),
							equalTo(Arrays.asList(4,5,6)));

	}

	@Test
	public void elapsedIsPositive(){


		assertTrue(of(1,2,3,4,5).stream().elapsed().noneMatch(t->t._2()<0));
	}
	@Test
	public void timeStamp(){


		assertTrue(of(1,2,3,4,5)
							.stream()
							.timestamp()
							.allMatch(t-> t._2() <= System.currentTimeMillis()));


	}
	@Test
	public void elementAt0(){
		assertThat(of(1).stream().elementAtAndStream(0)._1(),equalTo(1));
	}
	@Test
	public void getMultple(){
		assertThat(of(1,2,3,4,5).stream().elementAtAndStream(2)._1(),equalTo(3));
	}
	@Test
	public void getMultpleStream(){
		assertThat(of(1,2,3,4,5).stream().elementAtAndStream(2)._2().toList(),equalTo(Arrays.asList(1,2,3,4,5)));
	}
	@Test(expected=NoSuchElementException.class)
	public void getMultiple1(){
		of(1).stream().elementAtAndStream(1);
	}
	@Test(expected=NoSuchElementException.class)
	public void getEmpty(){
		of().stream().elementAtAndStream(0);
	}
	@Test
	public void get0(){
		assertTrue(of(1).elementAt(0).isPresent());
	}
	@Test
	public void getAtMultple(){
		assertThat(of(1,2,3,4,5).elementAt(2).toOptional().get(),equalTo(3));
	}
	@Test
	public void getAt1(){
		assertFalse(of(1).elementAt(1).isPresent());
	}
	@Test
	public void elementAtEmpty(){
		assertFalse(of().elementAt(0).isPresent());
	}
	@Test
	public void singleTest(){
		assertThat(of(1).singleOrElse(null),equalTo(1));
	}
	@Test
	public void singleEmpty(){
		Assert.assertNull(of().singleOrElse(null));
	}
	@Test
	public void single2(){
		Assert.assertNull(of(1,2).singleOrElse(null));
	}
	@Test
	public void singleOptionalTest(){
		assertThat(of(1).single().toOptional().get(),equalTo(1));
	}
	@Test
	public void singleOptionalEmpty(){
		assertFalse(of().single().isPresent());
	}
	@Test
	public void singleOptonal2(){
		assertFalse(of(1,2).single().isPresent());
	}

	@Test
	public void limitTimeEmpty(){
		List<Integer> result = ReactiveSeq.<Integer>of()
										.peek(i->sleep(i*100))
										.limit(1000,TimeUnit.MILLISECONDS)
										.toList();


		assertThat(result,equalTo(Arrays.asList()));
	}

	@Test
	public void skipTimeEmpty(){
		List<Integer> result = ReactiveSeq.<Integer>of()
										.peek(i->sleep(i*100))
										.skip(1000,TimeUnit.MILLISECONDS)
										.toList();


		assertThat(result,equalTo(Arrays.asList()));
	}
	private int sleep(Integer i) {
		try {
			Thread.currentThread().sleep(i);
		} catch (InterruptedException e) {

		}
		return i;
	}
	@Test
	public void testSkipLast(){
		assertThat(of(1,2,3,4,5)
							.skipLast(2)
							.to(ReactiveConvertableSequence::converter)
                             .listX(),equalTo(Arrays.asList(1,2,3)));
	}
	@Test
	public void testSkipLastEmpty(){
		assertThat(of()
							.skipLast(2)
							.stream().collect(java.util.stream.Collectors.toList()),equalTo(Arrays.asList()));
	}
	@Test
	public void testLimitLast(){
		assertThat(of(1,2,3,4,5)
							.limitLast(2)
							.stream().collect(java.util.stream.Collectors.toList()),equalTo(Arrays.asList(4,5)));
	}
	@Test
	public void testLimitLastEmpty(){
		assertThat(of()
							.limitLast(2)
							.stream().collect(java.util.stream.Collectors.toList()),equalTo(Arrays.asList()));
	}
	@Test
	public void endsWith(){
		assertTrue(of(1,2,3,4,5,6)
				.endsWithIterable(Arrays.asList(5,6)));
	}
	@Test
	public void endsWithFalse(){
		assertFalse(of(1,2,3,4,5,6)
				.endsWithIterable(Arrays.asList(5,6,7)));
	}
	@Test
	public void endsWithToLong(){
		assertFalse(of(1,2,3,4,5,6)
				.endsWithIterable(Arrays.asList(0,1,2,3,4,5,6)));
	}
	@Test
	public void endsWithEmpty(){
		assertTrue(of(1,2,3,4,5,6)
				.endsWithIterable(Arrays.asList()));
	}
	@Test
	public void endsWithWhenEmpty(){
		assertFalse(of()
				.endsWithIterable(Arrays.asList(1,2,3,4,5,6)));
	}
	@Test
	public void endsWithBothEmpty(){
		assertTrue(ReactiveSeq.<Integer>of()
				.endsWithIterable(Arrays.asList()));
	}
	@Test
	public void endsWithStream(){
		assertTrue(of(1,2,3,4,5,6)
				.endsWith(Stream.of(5,6)));
	}
	@Test
	public void endsWithFalseStream(){
		assertFalse(of(1,2,3,4,5,6)
				.endsWith(Stream.of(5,6,7)));
	}
	@Test
	public void endsWithToLongStream(){
		assertFalse(of(1,2,3,4,5,6)
				.endsWith(Stream.of(0,1,2,3,4,5,6)));
	}
	@Test
	public void endsWithEmptyStream(){
		assertTrue(of(1,2,3,4,5,6)
				.endsWith(Stream.of()));
	}
	@Test
	public void endsWithWhenEmptyStream(){
		assertFalse(of()
				.endsWith(Stream.of(1,2,3,4,5,6)));
	}
	@Test
	public void endsWithBothEmptyStream(){
		assertTrue(ReactiveSeq.<Integer>of()
				.endsWith(Stream.of()));
	}

	@Test
	public void streamable(){
		Streamable<Integer> repeat = (of(1,2,3,4,5,6)
												.map(i->i*2)
												).to()
												.streamable();

		assertThat(repeat.stream().toList(),equalTo(Arrays.asList(2,4,6,8,10,12)));
		assertThat(repeat.stream().toList(),equalTo(Arrays.asList(2,4,6,8,10,12)));
	}



	@Test
	public void testLazy(){
		Collection<Integer> col = of(1,2,3,4,5)
											.peek(System.out::println).to()
											.lazyCollection();
		System.out.println("takeOne!");
		col.forEach(System.out::println);
		assertThat(col.size(),equalTo(5));
	}

	int peek = 0;
	@Test
	public void testPeek() {
		peek = 0 ;
		   AnyM.fromStream(Stream.of(asList(1,3)))
				  				.flatMap(c-> AnyM.fromStream(c.stream()))
				  				.stream()
				  				.map(i->i*2)
				  				.peek(i-> peek=i)
				  				.collect(java.util.stream.Collectors.toList());
		assertThat(peek,equalTo(6));
	}
	@Test
	public void testMap() {
		  List<Integer> list = AnyM.fromStream(Stream.of(asList(1,3)))
				  				.flatMap(c->AnyM.fromStream(c.stream()))
				  				.stream()
				  				.map(i->i*2)
				  				.peek(System.out::println)
				  				.collect(java.util.stream.Collectors.toList());
		assertThat(Arrays.asList(2,6),equalTo(list));
	}
	@Test
	public void headAndTailTest(){
		Stream<String> s = Stream.of("hello","world");
		Iterator<String> it = s.iterator();
		String head = it.next();
		Stream<String> tail = Streams.stream(it);
		tail.forEach(System.out::println);
	}


	@Test
	public void xMatch(){
		assertTrue(of(1,2,3,5,6,7).xMatch(3, i-> i>4 ));
	}



	@Test
	public void zip2of(){

		List<Tuple2<Integer,Integer>> list = of(1,2,3,4,5,6)
											.zip(of(100,200,300,400))
											.to(ReactiveConvertableSequence::converter)
                                             .listX()
                      .materialize();



		List<Integer> right = list.stream().map(t -> t._2()).collect(java.util.stream.Collectors.toList());
		assertThat(right,hasItem(100));
		assertThat(right,hasItem(200));
		assertThat(right,hasItem(300));
		assertThat(right,hasItem(400));

		List<Integer> left = list.stream().map(t -> t._1()).collect(java.util.stream.Collectors.toList());
		System.out.println(left);
		assertThat(Arrays.asList(1,2,3,4,5,6),hasItem(left.get(0)));

	}
	@Test
	public void zipInOrder(){

		List<Tuple2<Integer,Integer>> list =  of(1,2,3,4,5,6)
													.zip( of(100,200,300,400).stream())
													.to(ReactiveConvertableSequence::converter)
                                                    .listX();

		assertThat(asList(1,2,3,4,5,6),hasItem(list.get(0)._1()));
		assertThat(asList(100,200,300,400),hasItem(list.get(0)._2()));



	}

	@Test
	public void zipEmpty() throws Exception {


		final AnyMSeq<W,Integer> zipped = this.<Integer>empty().zip(ReactiveSeq.<Integer>of(), (a, b) -> a + b);
		assertTrue(zipped.collect(java.util.stream.Collectors.toList()).isEmpty());
	}

	@Test
	public void shouldReturnEmptySeqWhenZipEmptyWithNonEmpty() throws Exception {



		final AnyMSeq<W,Integer> zipped = this.<Integer>empty().zip(of(1,2), (a, b) -> a + b);
		assertTrue(zipped.collect(java.util.stream.Collectors.toList()).isEmpty());
	}

	@Test
	public void shouldReturnEmptySeqWhenZipNonEmptyWithEmpty() throws Exception {


		final AnyMSeq<W,Integer> zipped = of(1,2,3).zip(this.<Integer>empty(), (a, b) -> a + b);


		assertTrue(zipped.collect(java.util.stream.Collectors.toList()).isEmpty());
	}

	@Test
	public void shouldZipTwoFiniteSequencesOfSameSize() throws Exception {

		final AnyMSeq<W,String> first = of("A", "B", "C");
		final AnyMSeq<W,Integer> second = of(1, 2, 3);


		final AnyMSeq<W,String> zipped = first.zip(second, (a, b) -> a + b);


		assertThat(zipped.collect(java.util.stream.Collectors.toList()).size(),is(3));
	}



	@Test
	public void shouldTrimSecondFixedSeqIfLonger() throws Exception {
		final AnyMSeq<W,String> first = of("A", "B", "C");
		final AnyMSeq<W,Integer> second = of(1, 2, 3, 4);


		final AnyMSeq<W,String> zipped = first.zip(second, (a, b) -> a + b);

		assertThat(zipped.collect(java.util.stream.Collectors.toList()).size(),is(3));
	}

	@Test
	public void shouldTrimFirstFixedSeqIfLonger() throws Exception {
		final AnyMSeq<W,String> first = of("A", "B", "C","D");
		final AnyMSeq<W,Integer> second = of(1, 2, 3);
		final AnyMSeq<W,String> zipped = first.zip(second, (a, b) -> a + b);


		assertThat(zipped.collect(java.util.stream.Collectors.toList()).size(),equalTo(3));
	}

	@Test
	public void testZipDifferingLength() {
		List<Tuple2<Integer, String>> list = of(1, 2).zip(of("a", "b", "c", "d").stream()).toList();

		assertEquals(2, list.size());
		assertTrue(asList(1, 2).contains(list.get(0)._1()));
		assertTrue("" + list.get(1)._2(), asList(1, 2).contains(list.get(1)._1()));
		assertTrue(asList("a", "b", "c", "d").contains(list.get(0)._2()));
		assertTrue(asList("a", "b", "c", "d").contains(list.get(1)._2()));

	}


	@Test
	public void shouldTrimSecondFixedSeqIfLongerStream() throws Exception {
		final AnyMSeq<W,String> first = of("A", "B", "C");
		final AnyMSeq<W,Integer> second = of(1, 2, 3, 4);


		final AnyMSeq<W,String> zipped = first.zip(second, (a, b) -> a + b);

		assertThat(zipped.collect(java.util.stream.Collectors.toList()).size(),is(3));
	}

	@Test
	public void shouldTrimFirstFixedSeqIfLongerStream() throws Exception {
		final AnyMSeq<W,String> first = of("A", "B", "C","D");
		final AnyMSeq<W,Integer> second = of(1, 2, 3);

		final AnyMSeq<W,String> zipped = first.zip(second, (a, b) -> a + b);


		assertThat(zipped.collect(java.util.stream.Collectors.toList()).size(),equalTo(3));
	}

	@Test
	public void testZipDifferingLengthStream() {
		List<Tuple2<Integer, String>> list = of(1, 2).zip(of("a", "b", "c", "d").stream()).toList();

		assertEquals(2, list.size());
		assertTrue(asList(1, 2).contains(list.get(0)._1()));
		assertTrue("" + list.get(1)._2(), asList(1, 2).contains(list.get(1)._1()));
		assertTrue(asList("a", "b", "c", "d").contains(list.get(0)._2()));
		assertTrue(asList("a", "b", "c", "d").contains(list.get(1)._2()));

	}

	@Test
	public void shouldTrimSecondFixedSeqIfLongerSequence() throws Exception {
		final AnyMSeq<W,String> first = of("A", "B", "C");
		final AnyMSeq<W,Integer> second = of(1, 2, 3, 4);


		final AnyMSeq<W,String> zipped = first.zip(second, (a, b) -> a + b);

		assertThat(zipped.collect(java.util.stream.Collectors.toList()).size(),is(3));
	}

	@Test
	public void shouldTrimFirstFixedSeqIfLongerSequence() throws Exception {
		final AnyMSeq<W,String> first = of("A", "B", "C","D");
		final AnyMSeq<W,Integer> second = of(1, 2, 3);
		final AnyMSeq<W,String> zipped = first.zip(second, (a, b) -> a + b);


		assertThat(zipped.collect(java.util.stream.Collectors.toList()).size(),equalTo(3));
	}


	@Test
	public void testZipWithIndex() {
		assertEquals(asList(), of().zipWithIndex().to(ReactiveConvertableSequence::converter).listX());

		assertThat(of("a").zipWithIndex().map(t -> t._2()).findFirst().get(), is(0l));
		assertEquals(asList(Tuple.tuple("a", 0L)), of("a").zipWithIndex().to(ReactiveConvertableSequence::converter).listX());

	}




	@Test
	public void emptyConvert(){

		assertFalse(empty().to(ReactiveConvertableSequence::converter).optional().isPresent());
		assertFalse(empty().to(ReactiveConvertableSequence::converter).listX().size()>0);
		assertFalse(empty().to(ReactiveConvertableSequence::converter).dequeX().size()>0);
		assertFalse(empty().to(ReactiveConvertableSequence::converter).linkedListX().size()>0);
		assertFalse(empty().to(ReactiveConvertableSequence::converter).queueX().size()>0);
		assertFalse(empty().to(ReactiveConvertableSequence::converter).vectorX().size()>0);
		assertFalse(empty().to(ReactiveConvertableSequence::converter).persistentQueueX().size()>0);
		assertFalse(empty().to(ReactiveConvertableSequence::converter).setX().size()>0);
		assertFalse(empty().to(ReactiveConvertableSequence::converter).sortedSetX().size()>0);
		assertFalse(empty().to(ReactiveConvertableSequence::converter).orderedSetX().size()>0);
		assertFalse(empty().to(ReactiveConvertableSequence::converter).bagX().size()>0);
		assertFalse(empty().to(ReactiveConvertableSequence::converter).mapX(t->t,t->t).size()>0);

		assertFalse(empty().toSet().size()>0);
		assertFalse(empty().toList().size()>0);
		assertFalse(empty().to().streamable().size()>0);


	}
	@Test
	public void presentConvert(){

		assertTrue(of(1).to(ReactiveConvertableSequence::converter).optional().isPresent());
		assertTrue(of(1).to(ReactiveConvertableSequence::converter).listX().size()>0);
		assertTrue(of(1).to(ReactiveConvertableSequence::converter).dequeX().size()>0);
		assertTrue(of(1).to(ReactiveConvertableSequence::converter).linkedListX().size()>0);
		assertTrue(of(1).to(ReactiveConvertableSequence::converter).queueX().size()>0);
		assertTrue(of(1).to(ReactiveConvertableSequence::converter).vectorX().size()>0);
		assertTrue(of(1).to(ReactiveConvertableSequence::converter).persistentQueueX().size()>0);
		assertTrue(of(1).to(ReactiveConvertableSequence::converter).setX().size()>0);
		assertTrue(of(1).to(ReactiveConvertableSequence::converter).sortedSetX().size()>0);
		assertTrue(of(1).to(ReactiveConvertableSequence::converter).orderedSetX().size()>0);
		assertTrue(of(1).to(ReactiveConvertableSequence::converter).bagX().size()>0);
		assertTrue(of(1).to(ReactiveConvertableSequence::converter).mapX(t->t,t->t).size()>0);

		assertTrue(of(1).toSet().size()>0);
		assertTrue(of(1).toList().size()>0);
		assertTrue(of(1).to().streamable().size()>0);


	}





        @Test
        public void batchBySizeCollection(){
            assertThat(of(1,2,3,4,5,6).grouped(3,()->Vector.empty()).elementAt(0).toOptional().get().size(),is(3));

        }
        @Test
        public void batchBySizeInternalSize(){
            assertThat(of(1,2,3,4,5,6).grouped(3).collect(java.util.stream.Collectors.toList()).get(0).size(),is(3));
        }
        @Test
        public void fixedDelay(){
            SimpleTimer timer = new SimpleTimer();

            assertThat(of(1,2,3,4,5,6).fixedDelay(10000,TimeUnit.NANOSECONDS).collect(java.util.stream.Collectors.toList()).size(),is(6));
            assertThat(timer.getElapsedNanoseconds(),greaterThan(60000l));
        }





        @Test
        public void testSorted() {


            AnyMSeq<W,Tuple2<Integer, Integer>> t1 = of(tuple(2, 2), tuple(1, 1));

            List<Tuple2<Integer, Integer>> s1 = t1.sorted().toList();
            assertEquals(tuple(1, 1), s1.get(0));
            assertEquals(tuple(2, 2), s1.get(1));

            AnyMSeq<W,Tuple2<Integer, String>> t2 = of(tuple(2, "two"), tuple(1, "replaceWith"));
            List<Tuple2<Integer, String>> s2 = t2.sorted(comparing(t -> t._1())).toList();
            assertEquals(tuple(1, "replaceWith"), s2.get(0));
            assertEquals(tuple(2, "two"), s2.get(1));

            AnyMSeq<W,Tuple2<Integer, String>> t3 = of(tuple(2, "two"), tuple(1, "replaceWith"));
            List<Tuple2<Integer, String>> s3 = t3.sorted(t -> t._1()).toList();
            assertEquals(tuple(1, "replaceWith"), s3.get(0));
            assertEquals(tuple(2, "two"), s3.get(1));
        }

	@Test
	public void testSortedDuplicate() {
	    AnyMSeq<W,Tuple2<Integer, String>> t3 = of(tuple(1, "two"), tuple(1, "replaceWith"));
	    List<Tuple2<Integer, String>> s3 = t3.sorted(t -> t._1()).toList();
	    assertEquals(2, s3.size());
	}

        @Test
        public void zip2(){
            List<Tuple2<Integer,Integer>> list =
                    of(1,2,3,4,5,6).zipWithStream(Stream.of(100,200,300,400))
                                                    .peek(it -> System.out.println(it))

                                                    .collect(java.util.stream.Collectors.toList());

            List<Integer> right = list.stream().map(t -> t._2()).collect(java.util.stream.Collectors.toList());
            assertThat(right,hasItem(100));
            assertThat(right,hasItem(200));
            assertThat(right,hasItem(300));
            assertThat(right,hasItem(400));

            List<Integer> left = list.stream().map(t -> t._1()).collect(java.util.stream.Collectors.toList());
            assertThat(Arrays.asList(1,2,3,4,5,6),hasItem(left.get(0)));


        }



        @Test
        public void testReverse() {
            assertThat( of(1, 2, 3).reverse().toList().size(), is(asList(3, 2, 1).size()));
        }

        @Test
        public void testShuffle() {
            Supplier<AnyMSeq<W,Integer>> s = () ->of(1, 2, 3);

            assertEquals(3, s.get().shuffle().to(ReactiveConvertableSequence::converter).listX().size());
            assertThat(s.get().shuffle().to(ReactiveConvertableSequence::converter).listX(), hasItems(1, 2, 3));


        }
        @Test
        public void testShuffleRandom() {
            Random r = new Random();
            Supplier<AnyMSeq<W,Integer>> s = () ->of(1, 2, 3);

            assertEquals(3, s.get().shuffle(r).to(ReactiveConvertableSequence::converter).listX().size());
            assertThat(s.get().shuffle(r).to(ReactiveConvertableSequence::converter).listX(), hasItems(1, 2, 3));


        }

            @Test
            public void testSplitAtHead() {
                assertEquals(Optional.empty(), of().headAndTail().headOptional());
                assertEquals(asList(), of().headAndTail().tail().toList());

                assertEquals(Optional.of(1), of(1).headAndTail().headOptional());
                assertEquals(asList(), of(1).headAndTail().tail().toList());

                assertEquals(Maybe.of(1), of(1, 2).headAndTail().headMaybe());
                assertEquals(asList(2), of(1, 2).headAndTail().tail().toList());

                assertEquals(Arrays.asList(1), of(1, 2, 3).headAndTail().headStream().toList());
                assertEquals((Integer)2, of(1, 2, 3).headAndTail().tail().headAndTail().head());
                assertEquals(Optional.of(3), of(1, 2, 3).headAndTail().tail().headAndTail().tail().headAndTail().headOptional());
                assertEquals(asList(2, 3), of(1, 2, 3).headAndTail().tail().toList());
                assertEquals(asList(3), of(1, 2, 3).headAndTail().tail().headAndTail().tail().toList());
                assertEquals(asList(), of(1, 2, 3).headAndTail().tail().headAndTail().tail().headAndTail().tail().toList());
            }

            @Test
            public void testMinByMaxBy2() {
                Supplier<AnyMSeq<W,Integer>> s = () -> of(1, 2, 3, 4, 5, 6);

                assertEquals(1, (int) s.get().maxBy(t -> Math.abs(t - 5)).orElse(-1));
                assertEquals(5, (int) s.get().minBy(t -> Math.abs(t - 5)).orElse(-1));

                assertEquals(6, (int) s.get().maxBy(t -> "" + t).orElse(-1));
                assertEquals(1, (int) s.get().minBy(t -> "" + t).orElse(-1));
            }




            @Test
            public void testFoldLeft() {
                for(int i=0;i<100;i++){
                    Supplier<AnyMSeq<W,String>> s = () -> of("a", "b", "c");

                    assertTrue(s.get().reduce("", String::concat).contains("a"));
                    assertTrue(s.get().reduce("", String::concat).contains("b"));
                    assertTrue(s.get().reduce("", String::concat).contains("c"));

                    assertEquals(3, (int) s.get().reduce(0, (u, t) -> u + t.length()));


                    assertEquals(3, (int) s.get().foldRight(0, (t, u) -> u + t.length()));
                }
            }

            @Test
            public void testFoldRight(){
                    Supplier<AnyMSeq<W,String>> s = () -> of("a", "b", "c");

                    assertTrue(s.get().foldRight("", String::concat).contains("a"));
                    assertTrue(s.get().foldRight("", String::concat).contains("b"));
                    assertTrue(s.get().foldRight("", String::concat).contains("c"));
                    assertEquals(3, (int) s.get().foldRight(0, (t, u) -> u + t.length()));
            }

            @Test
            public void testFoldLeftStringBuilder() {
                Supplier<AnyMSeq<W,String>> s = () -> of("a", "b", "c");


                assertTrue(s.get().reduce(new StringBuilder(), (u, t) -> u.append("-").append(t)).toString().contains("a"));
                assertTrue(s.get().reduce(new StringBuilder(), (u, t) -> u.append("-").append(t)).toString().contains("b"));
                assertTrue(s.get().reduce(new StringBuilder(), (u, t) -> u.append("-").append(t)).toString().contains("c"));
                assertTrue(s.get().reduce(new StringBuilder(), (u, t) -> u.append("-").append(t)).toString().contains("-"));


                assertEquals(3, (int) s.get().reduce(0, (u, t) -> u + t.length()));


            }

            @Test
            public void testFoldRighttringBuilder() {
                Supplier<AnyMSeq<W,String>> s = () -> of("a", "b", "c");


                assertTrue(s.get().foldRight(new StringBuilder(), (t, u) -> u.append("-").append(t)).toString().contains("a"));
                assertTrue(s.get().foldRight(new StringBuilder(), (t, u) -> u.append("-").append(t)).toString().contains("b"));
                assertTrue(s.get().foldRight(new StringBuilder(), (t, u) -> u.append("-").append(t)).toString().contains("c"));
                assertTrue(s.get().foldRight(new StringBuilder(), (t, u) -> u.append("-").append(t)).toString().contains("-"));


            }

            @Test
            public void batchUntil(){
                assertThat(of(1,2,3,4,5,6)
                        .groupedUntil(i->false)
                        .to(ReactiveConvertableSequence::converter).listX().size(),equalTo(1));

            }
            @Test
            public void batchWhile(){
                assertThat(of(1,2,3,4,5,6)
                        .groupedWhile(i->true)
                        .to(ReactiveConvertableSequence::converter).listX()
                        .size(),anyOf(equalTo(1),equalTo(6)));

            }
            @Test
            public void batchUntilSupplier(){
                assertThat(of(1,2,3,4,5,6)
                        .groupedUntil(i->false,()->Vector.empty())
                        .to(ReactiveConvertableSequence::converter).listX().size(),equalTo(1));

            }
            @Test
            public void batchWhileSupplier(){
                assertThat(of(1,2,3,4,5,6)
                        .groupedWhile(i->true,()->Vector.empty())
                        .to(ReactiveConvertableSequence::converter).listX()
                        .size(),equalTo(1));

            }

            @Test
            public void slidingNoOrder() {
                ListX<Seq<Integer>> list = of(1, 2, 3, 4, 5, 6).sliding(2).to(ReactiveConvertableSequence::converter).listX();

                System.out.println(list);
                assertThat(list.get(0).size(), equalTo(2));
                assertThat(list.get(1).size(), equalTo(2));
            }

            @Test
            public void slidingIncrementNoOrder() {
                List<Seq<Integer>> list = of(1, 2, 3, 4, 5, 6).sliding(3, 2).collect(java.util.stream.Collectors.toList());

                System.out.println(list);

                assertThat(list.get(1).size(), greaterThan(1));
            }

            @Test
            public void combineNoOrder(){
                assertThat(of(1,2,3)
                           .combine((a, b)->a.equals(b), Semigroups.intSum)
                           .to(ReactiveConvertableSequence::converter).listX(),equalTo(ListX.of(1,2,3)));

            }
    @Test
    public void combineNoOrderMonoid(){
        assertThat(of(1,2,3)
                .combine(Monoids.intSum,(a, b)->a.equals(b))
                .to(ReactiveConvertableSequence::converter).listX(),equalTo(ListX.of(1,2,3)));

    }

            @Test
            public void zip3NoOrder(){
                List<Tuple3<Integer,Integer,Character>> list =
                        of(1,2,3,4).zip3(of(100,200,300,400).stream(),of('a','b','c','d').stream())
                                                        .to(ReactiveConvertableSequence::converter).listX();

                System.out.println(list);
                List<Integer> right = list.stream().map(t -> t._2()).collect(java.util.stream.Collectors.toList());
                assertThat(right,hasItem(100));
                assertThat(right,hasItem(200));
                assertThat(right,hasItem(300));
                assertThat(right,hasItem(400));

                List<Integer> left = list.stream().map(t -> t._1()).collect(java.util.stream.Collectors.toList());
                assertThat(Arrays.asList(1,2,3,4),hasItem(left.get(0)));

                List<Character> three = list.stream().map(t -> t._3()).collect(java.util.stream.Collectors.toList());
                assertThat(Arrays.asList('a','b','c','d'),hasItem(three.get(0)));


            }
            @Test
            public void zip4NoOrder(){
                List<Tuple4<Integer,Integer,Character,String>> list =
                        of(1,2,3,4).zip4(of(100,200,300,400).stream(),of('a','b','c','d').stream(),of("hello","world","boo!","2").stream())
                                                        .to(ReactiveConvertableSequence::converter).listX();
                System.out.println(list);
                List<Integer> right = list.stream().map(t -> t._2()).collect(java.util.stream.Collectors.toList());
                assertThat(right,hasItem(100));
                assertThat(right,hasItem(200));
                assertThat(right,hasItem(300));
                assertThat(right,hasItem(400));

                List<Integer> left = list.stream().map(t -> t._1()).collect(java.util.stream.Collectors.toList());
                assertThat(Arrays.asList(1,2,3,4),hasItem(left.get(0)));

                List<Character> three = list.stream().map(t -> t._3()).collect(java.util.stream.Collectors.toList());
                assertThat(Arrays.asList('a','b','c','d'),hasItem(three.get(0)));

                List<String> four = list.stream().map(t -> t._4()).collect(java.util.stream.Collectors.toList());
                assertThat(Arrays.asList("hello","world","boo!","2"),hasItem(four.get(0)));


            }

            @Test
            public void testIntersperseNoOrder() {

                assertThat((of(1,2,3).intersperse(0)).to(ReactiveConvertableSequence::converter).listX(),hasItem(0));




            }



            @Test @Ignore
            public void testOfTypeNoOrder() {



                assertThat(of(1,  0.2, 2, 0.3, 3)

                        .ofType(Serializable.class).to(ReactiveConvertableSequence::converter).listX(),containsInAnyOrder(1, 0.2, 2,0.3, 3));

            }

            @Test
            public void allCombinations3NoOrder() {
                System.out.println(of(1, 2, 3).combinations().map(s->s.to(ReactiveConvertableSequence::converter).listX()).to(ReactiveConvertableSequence::converter).listX());
                assertThat(of(1, 2, 3).combinations().map(s->s.to(ReactiveConvertableSequence::converter).listX()).to(ReactiveConvertableSequence::converter).listX().size(),equalTo(8));
            }

            @Test
            public void emptyAllCombinationsNoOrder() {
                assertThat(of().combinations().map(s -> s.to(ReactiveConvertableSequence::converter).listX()).to(ReactiveConvertableSequence::converter).listX(), equalTo(Arrays.asList(Arrays.asList())));
            }

            @Test
            public void emptyPermutationsNoOrder() {
                assertThat(of().permutations().map(s->s.toList()).toList(),equalTo(Arrays.asList()));
            }

            @Test
            public void permuations3NoOrder() {
                System.out.println(of(1, 2, 3).permutations().map(s->s.to(ReactiveConvertableSequence::converter).listX()).to(ReactiveConvertableSequence::converter).listX());
                assertThat(of(1, 2, 3).permutations().map(s->s.to(ReactiveConvertableSequence::converter).listX()).to(ReactiveConvertableSequence::converter).listX().get(0).size(),
                        equalTo(3));
            }

            @Test
            public void emptyCombinationsNoOrder() {
                assertThat(of().combinations(2).map(s -> s.to(ReactiveConvertableSequence::converter).listX()).to(ReactiveConvertableSequence::converter).listX(), equalTo(Arrays.asList()));
            }

             @Test
            public void combinations2NoOrder() {

                    assertThat(of(1, 2, 3).combinations(2).map(s->s.to(ReactiveConvertableSequence::converter).listX()).to(ReactiveConvertableSequence::converter).listX().get(0).size(),
                            equalTo(2));
                }
        protected Object sleep(int i) {
            try {
                Thread.currentThread().sleep(i);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return i;
        }
        @Test
        public void trampoline2Test(){
            of(10,20,30,40)
                     .trampoline(i-> fibonacci(i))
                     .forEach(System.out::println);
        }
        @Test
        public void trampolineTest(){
            of(10_000,200_000,3_000_000,40_000_000)
                     .trampoline(i-> fibonacci(i))
                     .forEach(System.out::println);
        }
        Trampoline<Long> fibonacci(int i){
            return fibonacci(i,1,0);
        }
        Trampoline<Long> fibonacci(int n, long a, long b) {
            return n == 0 ? Trampoline.done(b) : Trampoline.more( ()->fibonacci(n-1, a+b, a));
        }

}
