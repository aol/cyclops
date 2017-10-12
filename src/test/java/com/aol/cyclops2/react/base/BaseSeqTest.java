package com.aol.cyclops2.react.base;

import static java.util.Arrays.asList;
import static java.util.Comparator.comparing;
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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import cyclops.reactive.FutureStream;
import cyclops.reactive.ReactiveSeq;
import cyclops.data.tuple.Tuple;
import cyclops.data.tuple.Tuple2;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.pcollections.HashTreePMap;

import cyclops.async.adapters.Queue;
import cyclops.collectionx.mutable.ListX;
import com.aol.cyclops2.util.SimpleTimer;
//see BaseSequentialSeqTest for in order tests
public abstract class BaseSeqTest {
	abstract protected <U> FutureStream<U> of(U... array);
	abstract protected <U> FutureStream<U> ofThread(U... array);
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
	public void batchBySize(){
		System.out.println(of(1,2,3,4,5,6).grouped(3).collect(Collectors.toList()));
		for(int i=0;i<1000;i++)
			assertThat(of(1,2,3,4,5,6).grouped(3).collect(Collectors.toList()).size(),is(2));
	}
	@Test
	public void batchBySizeAndTimeSize(){
		
		assertThat(of(1,2,3,4,5,6).groupedBySizeAndTime(3,10,TimeUnit.SECONDS).toList().get(0).size(),is(3));
	}
	@Test
	public void batchBySizeAndTimeTime(){
		
		for(int i=0;i<5;i++){
			
			List<ListX<Integer>> list = react(()->1,()->2,()->3,()->4,()->5,()->{sleep(150);return 6;})
					.groupedBySizeAndTime(30,1,TimeUnit.MICROSECONDS)
					.toList();
			
			assertThat(list
							.get(0)
							,not(hasItem(6)));
		}
	}
	
	@Test
	public void batchBySizeSet(){
		
		
		assertThat(of(1,1,1,1,1,1).grouped(3,()->new TreeSet<Integer>()).block().get(0).size(),is(1));
		
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
		
		for(int i=0;i<100;i++)
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
		assertThat(of(1,2,3,4,5,6).groupedByTime(15000,TimeUnit.MICROSECONDS).collect(Collectors.toList()).size(),is(1));
	}
	@Test
	public void batchByTimeSet(){
		for(int i=0;i<5000;i++){
			List <TreeSet<Integer>> set = ofThread(1,1,1,1,1,1).groupedByTime(1500,TimeUnit.MICROSECONDS,()-> new TreeSet<>()).block();
			
			assertThat(set.get(0).size(),is(1));
			
			
			
		}
	}
	@Test
	public void batchByTimeInternalSize(){
		assertThat(of(1,2,3,4,5,6).groupedByTime(1,TimeUnit.NANOSECONDS).collect(Collectors.toList()).size(),greaterThan(5));
	}
	@Test
	public void shard(){
		Map<Integer,Queue<Integer>> shards = new HashMap<>();
		shards.put(1,new Queue());
		shards.put(2,new Queue());
		shards.put(3,new Queue());
		shards.put(4,new Queue());
		shards.put(5,new Queue());
		shards.put(6,new Queue());
		for(int i=0;i<100;i++)
			assertThat(of(1,2,3,4,5,6).shard(HashTreePMap.from(shards),Function.identity()).size(),is(6));
	}
	@Test
	public void shardStreams(){
		
		//for(int index=0;index<100;index++)
		{
		
			Map<Integer,Queue<Integer>> shards = HashTreePMap.singleton(0,new Queue<Integer>()).plus(1,new Queue());
					
			Map<Integer, ? extends FutureStream<Integer>> sharded = of(1,2,3,4,5,6).shard(shards, i -> i%2);
			sharded.get(0).forEach(next ->{
				System.out.println ("next is " + next);
			});
			//assertThat(sharded.get(0).collect(CyclopsCollectors.toList()),hasItem(6));
		}
	}
	@Test
    public void testSorted() {
        FutureStream<Tuple2<Integer, String>> t1 = of(tuple(2, "two"), tuple(1, "replaceWith"));
        List<Tuple2<Integer, String>> s1 = t1.sorted().toList();
        assertEquals(tuple(1, "replaceWith"), s1.get(0));
        assertEquals(tuple(2, "two"), s1.get(1));

        FutureStream<Tuple2<Integer, String>> t2 = of(tuple(2, "two"), tuple(1, "replaceWith"));
        List<Tuple2<Integer, String>> s2 = t2.sorted(comparing(t -> t._1())).toList();
        assertEquals(tuple(1, "replaceWith"), s2.get(0));
        assertEquals(tuple(2, "two"), s2.get(1));

        FutureStream<Tuple2<Integer, String>> t3 = of(tuple(2, "two"), tuple(1, "replaceWith"));
        List<Tuple2<Integer, String>> s3 = t3.sorted(t -> t._1()).toList();
        assertEquals(tuple(1, "replaceWith"), s3.get(0));
        assertEquals(tuple(2, "two"), s3.get(1));
    }

	@Test
	public void zip(){
		List<Tuple2<Integer,Integer>> list =
				of(1,2,3,4,5,6).zip(of(100,200,300,400))
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
	public void zip2of(){
		
		List<Tuple2<Integer,Integer>> list =of(1,2,3,4,5,6).zip(of(100,200,300,400))
						.peek(it -> System.out.println(it)).collect(Collectors.toList());
				//		.collect(CyclopsCollectors.toList());
	
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
		
		List<Tuple2<Integer,Integer>> list =  of(1,2,3,4,5,6)
													.zip( of(100,200,300,400))
													.collect(Collectors.toList());
		
		assertThat(asList(1,2,3,4,5,6),hasItem(list.get(0)._1()));
		assertThat(asList(100,200,300,400),hasItem(list.get(0)._2()));
		
		
		
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

		
		assertThat(zipped.collect(Collectors.toList()).size(),is(3));
	}

	

	@Test
	public void shouldTrimSecondFixedSeqIfLonger() throws Exception {
		final FutureStream<String> first = of("A", "B", "C");
		final FutureStream<Integer> second = of(1, 2, 3, 4);

		
		final ReactiveSeq<String> zipped = first.zip(second, (a, b) -> a + b);

		assertThat(zipped.collect(Collectors.toList()).size(),is(3));
	}

	@Test
	public void shouldTrimFirstFixedSeqIfLonger() throws Exception {
		final FutureStream<String> first = of("A", "B", "C","D");
		final FutureStream<Integer> second = of(1, 2, 3);
		final ReactiveSeq<String> zipped = first.zip(second, (a, b) -> a + b);

		
		assertThat(zipped.collect(Collectors.toList()).size(),equalTo(3));
	}

	

	
	@Test
	public void limitWhileTest(){
		
		List<Integer> list = new ArrayList<>();
		while(list.size()==0){
			list = of(1,2,3,4,5,6).limitWhile(it -> it<4)
						.peek(it -> System.out.println(it)).collect(Collectors.toList());
	
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
    	assertThat(of("a", "ab", "abc").scanLeft(0, (u, t) -> u + t.length()).toList().size(), 
    			is(asList(0, 1, 3, 6).size()));
    }

    @Test
    public void testScanRightStringConcat() {
        assertThat(of("a", "b", "c").scanRight("", String::concat).toList().size(),
            is(asList("", "c", "bc", "abc").size()));
    }
    @Test
    public void testScanRightSum() {
    	assertThat(of("a", "ab", "abc").scanRight(0, (t, u) -> u + t.length()).toList().size(),
            is(asList(0, 3, 5, 6).size()));

        
    }

    

    @Test
    public void testReverse() {
        assertThat( of(1, 2, 3).reverse().toList().size(), is(asList(3, 2, 1).size()));
    }

    @Test
    public void testShuffle() {
        Supplier<ReactiveSeq<Integer>> s = () ->of(1, 2, 3);

        assertEquals(3, s.get().shuffle().toList().size());
        assertThat(s.get().shuffle().toList(), hasItems(1, 2, 3));

        
    }
    @Test
    public void testShuffleRandom() {
    	Random r = new Random();
        Supplier<ReactiveSeq<Integer>> s = () ->of(1, 2, 3);

        assertEquals(3, s.get().shuffle(r).toList().size());
        assertThat(s.get().shuffle(r).toList(), hasItems(1, 2, 3));

        
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
	    			.peek(it ->System.out.println(it))
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
	        assertEquals(asList(),of().zipWithIndex().toList());
	     //   System.out.println( of("a").zipWithIndex().toList().get(0));
	       
	      assertThat( of("a").zipWithIndex().map(t->t._2()).findFirst().get(),is(0l));
	      assertEquals(asList(tuple("a", 0L)), of("a").zipWithIndex().toList());
	     //   assertEquals(asList(tuple("a", 0L), tuple("b", 1L)), of("a", "b").zipWithIndex().toList());
	       //assertThat(asList(tuple("a", 0L), tuple("b", 1L), tuple("c", 2L)), is(of("a", "b", "c").zipWithIndex().toList()));
	    }

	   
	    @Test
	    public void testSkipWhile() {
	        Supplier<ReactiveSeq<Integer>> s = () -> of(1, 2, 3, 4, 5);

	        assertTrue(s.get().skipWhile(i -> false).toList().containsAll(asList(1, 2, 3, 4, 5)));
	      
	        assertEquals(asList(), s.get().skipWhile(i -> true).toList());
	    }

	    @Test
	    public void testSkipUntil() {
	        Supplier<ReactiveSeq<Integer>> s = () -> of(1, 2, 3, 4, 5);

	        assertEquals(asList(), s.get().skipUntil(i -> false).toList());
	        assertTrue(s.get().skipUntil(i -> true).toList().containsAll(asList(1, 2, 3, 4, 5)));
		  }

	    @Test
	    public void testSkipUntilWithNulls() {
	        Supplier<FutureStream<Integer>> s = () -> of(1, 2, null, 3, 4, 5);
	       
	        assertTrue(s.get().skipUntil(i -> true).toList().containsAll(asList(1, 2, null, 3, 4, 5)));
	    }

	    @Test
	    public void testLimitWhile() {
	        Supplier<ReactiveSeq<Integer>> s = () -> of(1, 2, 3, 4, 5);

	        assertEquals(asList(), s.get().limitWhile(i -> false).toList());
	        assertTrue( s.get().limitWhile(i -> i < 3).toList().size()!=5);       
	        assertTrue(s.get().limitWhile(i -> true).toList().containsAll(asList(1, 2, 3, 4, 5)));
	    }

	    @Test
	    public void testLimitUntil() {
	        

	        assertTrue(of(1, 2, 3, 4, 5).limitUntil(i -> false).toList().containsAll(asList(1, 2, 3, 4, 5)));
	        assertFalse(of(1, 2, 3, 4, 5).limitUntil(i -> i % 3 == 0).toList().size()==5);
	        
	        assertEquals(asList(), of(1, 2, 3, 4, 5).limitUntil(i -> true).toList());
	    }

	    @Test
	    public void testLimitUntilWithNulls() {
	       

	        assertTrue(of(1, 2, null, 3, 4, 5).limitUntil(i -> false).toList().containsAll(asList(1, 2, null, 3, 4, 5)));
	    }

	    @Test
	    public void testPartition() {
	        Supplier<ReactiveSeq<Integer>> s = () -> of(1, 2, 3, 4, 5, 6);

	        assertEquals(6, s.get().partition(i -> i % 2 != 0)._1().toList().size() + s.get().partition(i -> i % 2 != 0)._2().toList().size());
	        
	        assertTrue(s.get().partition(i -> true)._1().toList().containsAll(asList(1, 2, 3, 4, 5, 6)));
	        assertEquals(asList(), s.get().partition(i -> true)._2().toList());

	        assertEquals(asList(), s.get().partition(i -> false)._1().toList());
	        assertTrue(s.get().partition(i -> false)._2().toList().containsAll(asList(1, 2, 3, 4, 5, 6)));
	    }

	    @Test
	    public void testPartition100(){
	    	
	    	for(int i=0;i<0;i++){
		    	Supplier<ReactiveSeq<Tuple2<Integer,Integer>>> s = ()-> of(tuple(1, 0),tuple(2, 1), tuple(3, 2), tuple(4, 3), tuple(5, 4), tuple(6, 5));
		    	
		    	Tuple2<ReactiveSeq<Tuple2<Integer, Integer>>, ReactiveSeq<Tuple2<Integer, Integer>>> tuple = s.get().partition(t -> t._2() < 3);
		    	
		    	Tuple2<ReactiveSeq<Integer>, ReactiveSeq<Integer>> t2 = tuple.transform((v1, v2) -> Tuple.<ReactiveSeq<Integer>, ReactiveSeq<Integer>>tuple(
		                v1.map(t -> t._1()),
		                v2.map(t -> t._1())
		            ));
		            
		    	List l1 = t2._1().toList();
		    	System.out.println("l1:" +l1);
		    	List l2 = t2._2().toList();
		    	 System.out.println("l2:"+l2);
		    	assertThat(l1.size(),is(3));
		    	assertThat(l2.size(),is(3));
	    	}
	    	
	    }
	
	@Test
	public void testSplitAt() {
		for (int i = 0; i < 1000; i++) {
			Supplier<ReactiveSeq<Integer>> s = () -> of(1, 2, 3, 4, 5, 6);

			assertEquals(asList(), s.get().splitAt(0)._1().toList());
			assertTrue(s.get().splitAt(0)._2().toList().containsAll(
					asList(1, 2, 3, 4, 5, 6)));

			assertEquals(1, s.get().splitAt(1)._1().toList().size());
			assertEquals(s.get().splitAt(1)._2().toList().size(), 5);

			assertEquals(3, s.get().splitAt(3)._1().toList().size());

			assertEquals(3, s.get().splitAt(3)._2().count());

			assertEquals(6, s.get().splitAt(6)._1().toList().size());
			assertEquals(asList(), s.get().splitAt(6)._2().toList());

			assertThat(s.get().splitAt(7)._1().toList().size(), is(6));
			assertEquals(asList(), s.get().splitAt(7)._2().toList());

		}
	}

	    @Test
	    public void testSplitAtHead() {
	        assertEquals(Optional.empty(), of().splitAtHead()._1());
	        assertEquals(asList(), of().splitAtHead()._2().toList());

	        assertEquals(Optional.of(1), of(1).splitAtHead()._1());
	        assertEquals(asList(), of(1).splitAtHead()._2().toList());

	        assertEquals(1, of(1, 2).splitAtHead()._2().count());


			assertEquals(asList(2, 3).size(), of(1, 2, 3).splitAtHead()._2().toList().size());
	        assertEquals(1, of(1, 2, 3).splitAtHead()._2().splitAtHead()._2().toList().size());
	        assertEquals(0, of(1, 2, 3).splitAtHead()._2().splitAtHead()._2().splitAtHead()._2().toList().size());
	    }

	    @Test
	    public void testMinByMaxBy() {
	        Supplier<ReactiveSeq<Integer>> s = () -> of(1, 2, 3, 4, 5, 6);

	        assertEquals(1, (int) s.get().maxBy(t -> Math.abs(t - 5)).get());
	        assertEquals(5, (int) s.get().minBy(t -> Math.abs(t - 5)).get());

	        assertEquals(6, (int) s.get().maxBy(t -> "" + t).get());
	        assertEquals(1, (int) s.get().minBy(t -> "" + t).get());
	    }

	   
	   

	    @Test
	    public void testFoldLeft() {
	    	for(int i=0;i<100;i++){
		        Supplier<ReactiveSeq<String>> s = () -> of("a", "b", "c");
	
		        assertTrue(s.get().foldLeft("", String::concat).contains("a"));
		        assertTrue(s.get().foldLeft("", String::concat).contains("b"));
		        assertTrue(s.get().foldLeft("", String::concat).contains("c"));
		       
		        assertEquals(3, (int) s.get().foldLeft(0, (u, t) -> u + t.length()));
	
		        
		        assertEquals(3, (int) s.get().foldRight(0, (t, u) -> u + t.length()));
	    	}
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
	        
	           
	    }
	    //tests converted from maybe-seq suite
	    @Test
		public void flattenEmpty() throws Exception {
				assertTrue(this.<Integer>of().flatMap(i -> asList(i, -i).stream()).block().isEmpty());
		}

		@Test
		public void flatten() throws Exception {
			assertThat(this.<Integer>of(1,2).flatMap(i -> asList(i, -i).stream()).block().size(),equalTo(asList(1, -1, 2, -2).size()));		
		}

		

		@Test
		public void flattenEmptyStream() throws Exception {
			
			assertThat(this.<Integer>of(1,2,3,4,5,5,6,8,9,10).flatMap(BaseSeqTest::flatMapFun).limit(10).collect(Collectors.toList()).size(),
											equalTo(asList(2, 3, 4, 5, 6, 7, 0, 0, 0, 0).size()));
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
