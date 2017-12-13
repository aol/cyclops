package cyclops.futurestream.react.base;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static cyclops.data.tuple.Tuple.tuple;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import cyclops.collections.immutable.VectorX;
import cyclops.collections.mutable.MapX;
import cyclops.companion.MapXs;
import cyclops.control.Option;
import cyclops.futurestream.FutureStream;
import cyclops.reactive.ReactiveSeq;
import cyclops.data.tuple.Tuple2;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.oath.cyclops.async.adapters.Queue;
import cyclops.collections.mutable.ListX;
import com.oath.cyclops.util.SimpleTimer;

public abstract class BaseSequentialSeqTest {

	abstract protected  <U> FutureStream<U> of(U... array);
	abstract protected  <U> FutureStream<U> ofThread(U... array);
	abstract protected <U> FutureStream<U> react(Supplier<U>... array);

	FutureStream<Integer> empty;
	FutureStream<Integer> nonEmpty;

	@Before
	public void setup(){
		empty = of();
		nonEmpty = of(1);
	}

	@Test
	public void syncTest(){
		FutureStream stream = of(1,2,3,4).sync();
		assertThat(stream.isAsync(),is(false));
	}
	@Test
	public void asyncTest(){
		FutureStream stream = of(1,2,3,4).async();
		assertThat(stream.isAsync(),is(true));
	}
	@Test
	public void syncAndAsyncTest(){
		FutureStream stream = of(1,2,3,4).sync().async();
		assertThat(stream.isAsync(),is(true));
	}
	@Test
	public void asyncSyncTest(){
		FutureStream stream = of(1,2,3,4).async().sync();
		assertThat(stream.isAsync(),is(false));
	}


	protected Object value() {
		try {
			Thread.sleep(150);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "jello";
	}
	protected int value2() {
		try {
			Thread.sleep(250);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 200;
	}


	@Test
	public void sliding(){
		List<VectorX<Integer>> list = of(1,2,3,4,5,6).sliding(2)
									.collect(Collectors.toList());


		assertThat(list.get(0),hasItems(1,2));
		assertThat(list.get(1),hasItems(2,3));
	}
	@Test
	public void slidingInc(){
		List<VectorX<Integer>> list = of(1,2,3,4,5,6).sliding(3,2)
									.collect(Collectors.toList());


		assertThat(list.get(0),hasItems(1,2,3));
		assertThat(list.get(1),hasItems(3,4,5));
	}

	@Test
	public void batchBySize(){
		System.out.println(of(1,2,3,4,5,6).grouped(3).collect(Collectors.toList()));
		assertThat(of(1,2,3,4,5,6).grouped(3).collect(Collectors.toList()).size(),is(2));
	}
	@Test
	public void batchBySizeAndTimeSize(){

		assertThat(of(1,2,3,4,5,6).groupedBySizeAndTime(3,10,TimeUnit.SECONDS).toList().get(0).size(),is(3));
	}
	@Test
	public void batchBySizeAndTimeTime(){

		for(int i=0;i<10;i++){
			System.out.println(i);
			List<ListX<Integer>> list = react(()->1,()->2,()->3,()->4,()->5,()->{sleep(150);return 6;})
					.groupedBySizeAndTime(10,1,TimeUnit.MICROSECONDS)
					.toList();

			assertThat(list
							.get(0)
							,not(hasItem(6)));
		}
	}


	@Test
	public void batchBySizeSet(){
		System.out.println(of(1,1,1,1,1,1).grouped(3,()->new TreeSet<>()).block());
		assertThat(of(1,1,1,1,1,1).grouped(3,()->new TreeSet<>()).block().get(0).size(),is(1));
		assertThat(of(1,1,1,1,1,1).grouped(3,()->new TreeSet<>()).block().size(),is(1));
	}
	@Test
	public void batchBySizeInternalSize(){
		assertThat(of(1,2,3,4,5,6).grouped(3).collect(Collectors.toList()).get(0).size(),is(3));
	}
	@Test
	public void fixedDelay(){
		SimpleTimer timer = new SimpleTimer();

		assertThat(of(1,2,3,4,5,6).fixedDelay(10000,TimeUnit.NANOSECONDS).collect(Collectors.toList()).size(),is(6));
		assertThat(timer.getElapsedNanoseconds(),greaterThan(60000l));
	}
	@Test
	public void judder(){
		SimpleTimer timer = new SimpleTimer();

		assertThat(of(1,2,3,4,5,6).jitter(10000).collect(Collectors.toList()).size(),is(6));
		assertThat(timer.getElapsedNanoseconds(),greaterThan(20000l));
	}
	@Test
	public void debounce(){
		SimpleTimer timer = new SimpleTimer();


		assertThat(of(1,2,3,4,5,6).debounce(1000,TimeUnit.SECONDS).collect(Collectors.toList()).size(),is(1));

	}
	@Test
	public void debounceOk(){
		System.out.println(of(1,2,3,4,5,6).debounce(1,TimeUnit.NANOSECONDS).toList());
		assertThat(of(1,2,3,4,5,6).debounce(1,TimeUnit.NANOSECONDS).collect(Collectors.toList()).size(),is(6));

	}
	@Test
	public void onePer(){
		SimpleTimer timer = new SimpleTimer();
		System.out.println(of(1,2,3,4,5,6).onePer(1000,TimeUnit.NANOSECONDS).collect(Collectors.toList()));
		assertThat(of(1,2,3,4,5,6).onePer(1000,TimeUnit.NANOSECONDS).collect(Collectors.toList()).size(),is(6));
		assertThat(timer.getElapsedNanoseconds(),greaterThan(600l));
	}
	@Test
	public void xPer(){
		SimpleTimer timer = new SimpleTimer();
		System.out.println(of(1,2,3,4,5,6).xPer(6,1000,TimeUnit.NANOSECONDS).collect(Collectors.toList()));
		assertThat(of(1,2,3,4,5,6).xPer(6,100000000,TimeUnit.NANOSECONDS).collect(Collectors.toList()).size(),is(6));
		assertThat(timer.getElapsedNanoseconds(),lessThan(60000000l));
	}
	@Test
	public void batchByTime(){
		assertThat(of(1,2,3,4,5,6).groupedByTime(1,TimeUnit.SECONDS).collect(Collectors.toList()).size(),is(1));
	}
	@Test
	public void batchByTimeSet(){

		assertThat(of(1,1,1,1,1,1).groupedByTime(1500,TimeUnit.MICROSECONDS,()-> new TreeSet<>()).block().get(0).size(),is(1));
	}
	@Test
	public void batchByTimeInternalSize(){
		assertThat(of(1,2,3,4,5,6).groupedByTime(1,TimeUnit.NANOSECONDS).collect(Collectors.toList()).size(),greaterThan(5));
	}

	@Test
	public void shard(){

		for(int i=0;i<100;i++){
			Map<Integer,Queue<Integer>> shards = new HashMap<>();
			shards.put(1,new Queue());
			shards.put(2,new Queue());
			shards.put(3,new Queue());
			shards.put(4,new Queue());
			shards.put(5,new Queue());
			shards.put(6,new Queue());
			assertThat(of(1,2,3,4,5,6).shard(MapX.fromMap(shards),Function.identity()).size(),is(6));
		}
	}
	@Test
	public void shardStreams(){

		for(int index=0;index<100;index++){
			Map<Integer,Queue<Integer>> shards = MapXs.of(0,new Queue<Integer>()).plus(1,new Queue());

			assertThat(of(1,2,3,4,5,6).shard(shards,i -> 0).get(0).collect(Collectors.toList()),hasItem(6));
		}
	}


	@Test
	public void zip(){
		List<Tuple2<Integer,Integer>> list =
				of(1,2,3,4,5,6).zip(of(100,200,300,400))
												.peek(it -> System.out.println(it)).collect(Collectors.toList());

		List<Integer> right = list.stream().map(t -> t._2()).collect(Collectors.toList());
		assertThat(right,hasItem(100));
		assertThat(right,hasItem(200));
		assertThat(right,hasItem(300));
		assertThat(right,hasItem(400));

		List<Integer> left = list.stream().map(t -> t._1()).collect(Collectors.toList());
		assertThat(asList(1,2,3,4),equalTo(left));


	}

	@Test
	public void zip2of(){
		List<Tuple2<Integer,Integer>> list =of(1,2,3,4,5,6).zip(of(100,200,300,400)).peek(it -> System.out.println(it)).collect(Collectors.toList());

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

		//this is not 100% reliable for EagerFutureStream use zipFutures instead
			List<Tuple2<Integer,Integer>> list =  of(1,2,3,4,5,6).limit(6)
														.zip( of(100,200,300,400).limit(4))
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
	public void zipEmpty() throws Exception {


		final ReactiveSeq<Integer> zipped = empty.zip(this.<Integer>of(), (a, b) -> a + b);
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

		final FutureStream<String> first = of("A", "B", "C");
		final FutureStream<Integer> second = of(1, 2, 3);


		final ReactiveSeq<String> zipped = first.zip(second, (a, b) -> a + b);


		assertThat(zipped.collect(Collectors.toList()),equalTo(asList("A1", "B2", "C3")));
	}



	@Test
	public void shouldTrimSecondFixedSeqIfLonger() throws Exception {
		final FutureStream<String> first = of("A", "B", "C");
		final FutureStream<Integer> second = of(1, 2, 3, 4);


		final ReactiveSeq<String> zipped = first.zip(second, (a, b) -> a + b);

		assertThat(zipped.collect(Collectors.toList()),equalTo(asList("A1", "B2", "C3")));
	}

	@Test
	public void shouldTrimFirstFixedSeqIfLonger() throws Exception {
		final FutureStream<String> first = of("A", "B", "C","D");
		final FutureStream<Integer> second = of(1, 2, 3);
		final ReactiveSeq<String> zipped = first.zip(second, (a, b) -> a + b);


		assertThat(zipped.collect(Collectors.toList()),equalTo(asList("A1", "B2", "C3")));
	}

	@Test
	public void limitWhileTest(){
		List<Integer> list = of(1,2,3,4,5,6).limitWhile(it -> it<4).peek(it -> System.out.println(it)).collect(Collectors.toList());

		assertThat(list,hasItem(1));
		assertThat(list,hasItem(2));
		assertThat(list,hasItem(3));



	}

    @Test
    public void testScanLeftStringConcat() {
        assertThat(of("a", "b", "c").scanLeft("", String::concat).toList(),
        		is(asList("", "a", "ab", "abc")));
    }
    @Test
    public void testScanLeftSum() {
    	assertThat(of("a", "ab", "abc").scanLeft(0, (u, t) -> u + t.length()).toList(),
    			is(asList(0, 1, 3, 6)));
    }

    @Test
    public void testScanRightStringConcat() {
        assertThat(of("a", "b", "c").scanRight("", String::concat).toList(),
            is(asList("", "c", "bc", "abc")));
    }
    @Test
    public void testScanRightSum() {
    	assertThat(of("a", "ab", "abc").scanRight(0, (t, u) -> u + t.length()).toList(),
            is(asList(0, 3, 5, 6)));


    }



    @Test
    public void testReverse() {
        assertThat( of(1, 2, 3).reverse().toList(), is(asList(3, 2, 1)));
    }

    @Test
    public void testShuffle() {
        Supplier<ReactiveSeq<Integer>> s = () ->of(1, 2, 3);

        assertEquals(3, s.get().shuffle().toList().size());
        assertThat(s.get().shuffle().toList(), hasItems(1, 2, 3));


    }



    @Test
    public void testIterable() {
        List<Integer> list = of(1, 2, 3).to().collection(LinkedList::new);

        for (Integer i :of(1, 2, 3)) {
            assertThat(list,hasItem(i));
        }
    }

	@Test
	public void testDuplicate(){
		 Tuple2<ReactiveSeq<Integer>, ReactiveSeq<Integer>> copies =of(1,2,3,4,5,6).duplicate();
		 assertTrue(copies._1().anyMatch(i->i==2));
		 assertTrue(copies._2().anyMatch(i->i==2));
	}



	 Throwable ex;
	    @Test
	    public void testCastException() {
	    	ex = null;
	    	of(1, "a", 2, "b", 3, null).capture(e-> ex =e)
	    			.peek(it ->System.out.println(it))
	    			.cast(Integer.class)
	    				.peek(it ->System.out.println(it)).toList();

	    	assertThat(ex.getClass(),equalTo(ClassCastException.class));
	    }
	    @Test
	    public void testCastExceptionOnFail() {
	    	ex = null;
	    	of(1, "a", 2, "b", 3, null)//.capture(e-> {e.printStackTrace();ex =e;})
	    			//.peek(it ->System.out.println(it))
	    			.cast(Integer.class)
	    			.onFail(e -> {
	    				System.out.println("**"+e.getValue());
	    				return 1;

	    			})
	    			.peek(it ->System.out.println(it)).toList();

	    	assertThat(ex,is(nullValue()));
	    }




	    @Test
	    public void testGroupByEager() {
	        Map<Integer, ListX<Integer>> map1 =of(1, 2, 3, 4).groupBy(i -> i % 2);
	        assertEquals(asList(2, 4), map1.get(0));
	        assertEquals(asList(1, 3), map1.get(1));
	        assertEquals(2, map1.size());


	    }


	    @Test
	    public void testJoin() {
	        assertEquals("123",of(1, 2, 3).join());
	        assertEquals("1, 2, 3", of(1, 2, 3).join(", "));
	        assertEquals("^1|2|3$", of(1, 2, 3).join("|", "^", "$"));
	    }


	    @Test @Ignore //failing!
	    public void testOptional() {
	        assertEquals(asList(1),of(Optional.of(1)).toList());
	        assertEquals(asList(), of(Optional.empty()).toList());
	    }
	    @Test
	    public void testZipDifferingLength() {
	        List<Tuple2<Integer, String>> list = of(1, 2).zip(of("a", "b", "c", "d")).toList();

	        assertEquals(2, list.size());
	        assertTrue(asList(1,2).contains( list.get(0)._1()));
	        assertTrue(""+list.get(1)._2(),asList(1,2).contains( list.get(1)._1()));
	        assertTrue(asList("a", "b", "c", "d").contains( list.get(0)._2()));
	        assertTrue(asList("a", "b", "c", "d").contains( list.get(1)._2()));


	    }

	    @Test
	    public void testZipWithIndex() {
	    	//assertEquals(asList(), of().zipWithIndex().toList());
	       // assertEquals(asList(tuple("a", 0L)), of("a").zip(of(0L)).toList());
	        //assertEquals(asList(tuple("a", 0L)), of("a").zipWithIndex().toList());
	    	assertEquals(asList(tuple("a", 0L), tuple("b", 1L)), of("a", "b").zipWithIndex().toList());
	        assertEquals(asList(tuple("a", 0L), tuple("b", 1L), tuple("c", 2L)), of("a", "b", "c").zipWithIndex().toList());
	    }


	    @Test
	    public void testSkipWhile() {
	    	 Supplier<ReactiveSeq<Integer>> s = () -> of(1, 2, 3, 4, 5);

	         assertEquals(asList(1, 2, 3, 4, 5), s.get().skipWhile(i -> false).toList());
	         assertEquals(asList(3, 4, 5), s.get().skipWhile(i -> i % 3 != 0).toList());
	         assertEquals(asList(3, 4, 5), s.get().skipWhile(i -> i < 3).toList());
	         assertEquals(asList(4, 5), s.get().skipWhile(i -> i < 4).toList());
	         assertEquals(asList(), s.get().skipWhile(i -> true).toList());
	    }

	    @Test
	    public void testSkipUntil() {
	    	Supplier<ReactiveSeq<Integer>> s = () -> of(1, 2, 3, 4, 5);

	        assertEquals(asList(), s.get().skipUntil(i -> false).toList());
	        assertEquals(asList(3, 4, 5), s.get().skipUntil(i -> i % 3 == 0).toList());
	        assertEquals(asList(3, 4, 5), s.get().skipUntil(i -> i == 3).toList());
	        assertEquals(asList(4, 5), s.get().skipUntil(i -> i == 4).toList());
	        assertEquals(asList(1, 2, 3, 4, 5), s.get().skipUntil(i -> true).toList());
		  }

	    @Test
	    public void testSkipUntilWithNulls() {
	    	 Supplier<ReactiveSeq<Integer>> s = () -> of(1, 2, null, 3, 4, 5);

	         assertEquals(asList(1, 2, null, 3, 4, 5), s.get().skipUntil(i -> true).toList());
	    }

	    @Test
	    public void testLimitWhile() {
	    	 Supplier<ReactiveSeq<Integer>> s = () -> of(1, 2, 3, 4, 5);

	         assertEquals(asList(), s.get().limitWhile(i -> false).toList());
	         assertEquals(asList(1, 2), s.get().limitWhile(i -> i % 3 != 0).toList());
	         assertEquals(asList(1, 2), s.get().limitWhile(i -> i < 3).toList());
	         assertEquals(asList(1, 2, 3), s.get().limitWhile(i -> i < 4).toList());
	         assertEquals(asList(1, 2, 3, 4, 5), s.get().limitWhile(i -> true).toList());
	    }

	    @Test
	    public void testLimitUntil() {
	    	 assertEquals(asList(1, 2, 3, 4, 5),of(1, 2, 3, 4, 5).limitUntil(i -> false).toList());
	         assertEquals(asList(1, 2), of(1, 2, 3, 4, 5).limitUntil(i -> i % 3 == 0).toList());
	         assertEquals(asList(1, 2), of(1, 2, 3, 4, 5).limitUntil(i -> i == 3).toList());
	         assertEquals(asList(1, 2, 3), of(1, 2, 3, 4, 5).limitUntil(i -> i == 4).toList());
	         assertEquals(asList(), of(1, 2, 3, 4, 5).limitUntil(i -> true).toList());



	        assertEquals(asList(), of(1, 2, 3, 4, 5).limitUntil(i -> true).toList());
	    }

	    @Test
	    public void testLimitUntilWithNulls() {


	        assertThat(of(1, 2, null, 3, 4, 5).limitUntil(i -> false).toList(),equalTo(asList(1, 2, null, 3, 4, 5)));
	    }

	    @Test
	    public void testPartition() {
	        Supplier<ReactiveSeq<Integer>> s = () -> of(1, 2, 3, 4, 5, 6);

	        assertEquals(asList(1, 3, 5), s.get().partition(i -> i % 2 != 0)._1().toList());
	        assertEquals(asList(2, 4, 6), s.get().partition(i -> i % 2 != 0)._2().toList());

	        assertEquals(asList(2, 4, 6), s.get().partition(i -> i % 2 == 0)._1().toList());
	        assertEquals(asList(1, 3, 5), s.get().partition(i -> i % 2 == 0)._2().toList());

	        assertEquals(asList(1, 2, 3), s.get().partition(i -> i <= 3)._1().toList());
	        assertEquals(asList(4, 5, 6), s.get().partition(i -> i <= 3)._2().toList());

	        assertEquals(asList(1, 2, 3, 4, 5, 6), s.get().partition(i -> true)._1().toList());
	        assertEquals(asList(), s.get().partition(i -> true)._2().toList());

	        assertEquals(asList(), s.get().partition(i -> false)._1().toList());
	        assertEquals(asList(1, 2, 3, 4, 5, 6), s.get().partition(i -> false)._2().toList());
	    }

	    @Test
	    public void testSplitAt() {
	    	for(int i=0;i<20;i++){
		        Supplier<ReactiveSeq<Integer>> s = () -> of(1, 2, 3, 4, 5, 6);


		    //    assertEquals(asList(), s.getValue().splitAt(0).v1.toList());
		     //   assertEquals(asList(1, 2, 3, 4, 5, 6), s.getValue().splitAt(0)._2.toList());

		    //    assertEquals(asList(1), s.getValue().splitAt(1).v1.toList());
		     //   assertEquals(asList(2, 3, 4, 5, 6), s.getValue().splitAt(1)._2.toList());

		  //     assertEquals(asList(1, 2, 3), s.getValue().splitAt(3).v1.toList());
		        assertEquals(asList(4, 5, 6), s.get().splitAt(3)._2().toList());

		    //   assertEquals(asList(1, 2, 3, 4, 5, 6), s.getValue().splitAt(6).v1.toList());
		     //   assertEquals(asList(), s.getValue().splitAt(6)._2.toList());

		       // assertEquals(asList(1, 2, 3, 4, 5, 6), s.getValue().splitAt(7).v1.toList());
		       // assertEquals(asList(), s.getValue().splitAt(7)._2.toList());
	    	}
	    	for(int i=0;i<20;i++){
		        Supplier<ReactiveSeq<Integer>> s = () -> of(1, 2, 3, 4, 5, 6);


		        assertEquals(asList(1, 2, 3), s.get().splitAt(3)._1().toList());

	    	}
	    	for(int i=0;i<20;i++){
		        Supplier<ReactiveSeq<Integer>> s = () -> of(1, 2, 3, 4, 5, 6);


		       assertEquals(asList(1, 2, 3, 4, 5, 6), s.get().splitAt(6)._1().toList());
		      	}
	    	for(int i=0;i<20;i++){
		        Supplier<ReactiveSeq<Integer>> s = () -> of(1, 2, 3, 4, 5, 6);


		        assertEquals(asList(1, 2, 3, 4, 5, 6), s.get().splitAt(7)._1().toList());
		      	}
	    }
	@Test
	public void splitAtHeadInvestigate(){
		System.out.println("0" + of(1, 2, 3).splitAt(0)._2().toListX());
		System.out.println("head " + of(1, 2, 3).splitAtHead()._1());
		System.out.println("tail "+of(1, 2, 3).splitAtHead()._2().toListX());
		System.out.println(of(1, 2, 3).splitAtHead()._2().splitAtHead()._1());
		System.out.println(of(1, 2, 3).splitAtHead()._2().splitAtHead()._2().toListX());
		assertThat(of(1,2,3).splitAtHead()._2().toListX(),equalTo(ListX.of(2,3)));
	}
	    @Test
	    public void testSplitAtHead() {
	        assertEquals(Option.none(), of().splitAtHead()._1());
	        assertEquals(asList(), of().splitAtHead()._2().toList());

	        assertEquals(Option.of(1), of(1).splitAtHead()._1());
	        assertEquals(asList(), of(1).splitAtHead()._2().toList());

	        assertEquals(Option.of(1), of(1, 2).splitAtHead()._1());
	        assertEquals(asList(2), of(1, 2).splitAtHead()._2().toList());

	        assertEquals(Option.of(1), of(1, 2, 3).splitAtHead()._1());
	        assertEquals(Option.of(2), of(1, 2, 3).splitAtHead()._2().splitAtHead()._1());
	        assertEquals(Option.of(3), of(1, 2, 3).splitAtHead()._2().splitAtHead()._2().splitAtHead()._1());
	        assertEquals(asList(2, 3), of(1, 2, 3).splitAtHead()._2().toList());
	        assertEquals(asList(3), of(1, 2, 3).splitAtHead()._2().splitAtHead()._2().toList());
	        assertEquals(asList(), of(1, 2, 3).splitAtHead()._2().splitAtHead()._2().splitAtHead()._2().toList());
	    }

	    @Test
	    public void testMinByMaxBy() {
	        Supplier<ReactiveSeq<Integer>> s = () -> of(1, 2, 3, 4, 5, 6);

	        assertEquals(1, (int) s.get().maxBy(t -> Math.abs(t - 5)).orElse(-1));
	        assertEquals(5, (int) s.get().minBy(t -> Math.abs(t - 5)).orElse(-1));

	        assertEquals(6, (int) s.get().maxBy(t -> "" + t).orElse(-1));
	        assertEquals(1, (int) s.get().minBy(t -> "" + t).orElse(-1));
	    }

	    @Test
	    public void testFoldLeft() {
	        Supplier<ReactiveSeq<String>> s = () -> of("a", "b", "c");

	        assertTrue(s.get().foldLeft("", String::concat).contains("a"));
	        assertTrue(s.get().foldLeft("", String::concat).contains("b"));
	        assertTrue(s.get().foldLeft("", String::concat).contains("c"));

	        assertEquals(3, (int) s.get().foldLeft(0, (u, t) -> u + t.length()));


	        assertEquals(3, (int) s.get().foldRight(0, (t, u) -> u + t.length()));
	        assertEquals("-a-b-c", s.get().foldLeft(new StringBuilder(), (u, t) -> u.append("-").append(t)).toString());
	    }

	    @Test
	    public void testFoldRight(){
	    	 	Supplier<ReactiveSeq<String>> s = () -> of("a", "b", "c");

		        assertTrue(s.get().foldRight("", String::concat).contains("a"));
		        assertTrue(s.get().foldRight("", String::concat).contains("b"));
		        assertTrue(s.get().foldRight("", String::concat).contains("c"));
		        assertEquals(3, (int) s.get().foldRight(0, (t, u) -> u + t.length()));

	    }

	    @Test
	    public void testFoldLeftStringBuilder() {
	        Supplier<ReactiveSeq<String>> s = () -> of("a", "b", "c");


	        assertTrue(s.get().foldLeft(new StringBuilder(), (u, t) -> u.append("-").append(t)).toString().contains("a"));
	        assertTrue(s.get().foldLeft(new StringBuilder(), (u, t) -> u.append("-").append(t)).toString().contains("b"));
	        assertTrue(s.get().foldLeft(new StringBuilder(), (u, t) -> u.append("-").append(t)).toString().contains("c"));
	        assertTrue(s.get().foldLeft(new StringBuilder(), (u, t) -> u.append("-").append(t)).toString().contains("-"));


	        assertEquals(3, (int) s.get().foldLeft(0, (u, t) -> u + t.length()));


	    }

	    @Test
	    public void testFoldRighttringBuilder() {
	        Supplier<ReactiveSeq<String>> s = () -> of("a", "b", "c");


	        assertTrue(s.get().foldRight(new StringBuilder(), (t, u) -> u.append("-").append(t)).toString().contains("a"));
	        assertTrue(s.get().foldRight(new StringBuilder(), (t, u) -> u.append("-").append(t)).toString().contains("b"));
	        assertTrue(s.get().foldRight(new StringBuilder(), (t, u) -> u.append("-").append(t)).toString().contains("c"));
	        assertTrue(s.get().foldRight(new StringBuilder(), (t, u) -> u.append("-").append(t)).toString().contains("-"));
	        assertEquals("-c-b-a", s.get().foldRight(new StringBuilder(), (t, u) -> u.append("-").append(t)).toString());

	    }

	  //tests converted from lazy-seq suite
	    @Test
		public void flattenEmpty() throws Exception {
				assertTrue(this.<Integer>of().flatMap(i -> asList(i, -i).stream()).block().isEmpty());
		}

		@Test
		public void flatten() throws Exception {
			assertThat(this.<Integer>of(1,2).flatMap(i -> asList(i, -i).stream()).block(),equalTo(asList(1, -1, 2, -2)));
		}



		@Test
		public void flattenEmptyStream() throws Exception {

			assertThat(this.<Integer>of(1,2,3,4,5,5,6,8,9,10).flatMap(BaseSequentialSeqTest::flatMapFun).limit(10).collect(Collectors.toList()),
											equalTo(asList(2, 3, 4, 5, 6, 7, 0, 0, 0, 0)));
		}

		private static Stream<Integer> flatMapFun(int i) {
			if (i <= 0) {
				return Arrays.<Integer>asList().stream();
			}
			switch (i) {
				case 1:
					return asList(2).stream();
				case 2:
					return asList(3, 4).stream();
				case 3:
					return asList(5, 6, 7).stream();
				default:
					return asList(0, 0).stream();
			}
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

}
