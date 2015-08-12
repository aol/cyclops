package com.aol.simple.react.base;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.jooq.lambda.tuple.Tuple.tuple;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jooq.lambda.Seq;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.pcollections.HashTreePMap;

import com.aol.simple.react.async.Queue;
import com.aol.simple.react.stream.CloseableIterator;
import com.aol.simple.react.stream.traits.FutureStream;
import com.aol.simple.react.stream.traits.LazyFutureStream;
import com.aol.simple.react.util.SimpleTimer;


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
	
	
	@Test @Ignore
	public void firstOf(){
		
		assertTrue(FutureStream.firstOf(of(1,2,3,4),react(()->value()),
				react(()->value())).anyMatch(it-> it.equals(1)));
		assertTrue(FutureStream.firstOf(of(1,2,3,4),react(()->value()),
				react(()->value())).anyMatch(it-> it.equals(2)));
		assertTrue(FutureStream.firstOf(of(1,2,3,4),react(()->value()),
				react(()->value())).anyMatch(it-> it.equals(3)));
		assertTrue(FutureStream.firstOf(of(1,2,3,4),react(()->value()),
				react(()->value())).anyMatch(it-> it.equals(4)));
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
	private int value2() {
		try {
			Thread.sleep(250);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 200;
	}
	@Test
	public void combine(){
		
		assertThat(of(1,2,3,4,5,6).combineLatest(of(3)).collect(Collectors.toList()).size(),greaterThan(5));
	}
	@Test
	public void combineLatest(){
		
		assertThat(of(1,2,3,4,5,6).combineLatest(react(()->3,()->value())).collect(Collectors.toList()).size(),greaterThan(5));
	}
	@Test
	public void combineValues(){
		for(int i=0;i<1000;i++){
			Stream<Tuple2<Integer,Integer>> s = of(1,2,3,4,5,6).combineLatest(of(3));
			List<Tuple2<Integer,Integer>> list = s.collect(Collectors.toList());
			System.out.println(i + " : " +  list);
			
		//	assertTrue(of(1,2,3,4,5,6).combineLatest(of(3)).anyMatch(it-> it.v2==null));
			
			assertTrue(of(1,2,3,4,5,6).combineLatest(of(3)).anyMatch(it-> (it.v1 == null ? -1 : it.v1)==1));
			assertTrue(list.stream().anyMatch(it-> (it.v1 == null ? -1 : it.v1)==2));
			assertTrue(of(1,2,3,4,5,6).combineLatest(of(3)).anyMatch(it-> (it.v1 == null ? -1 : it.v1)==3));
			assertTrue(of(1,2,3,4,5,6).combineLatest(of(3)).anyMatch(it-> (it.v1 == null ? -1 : it.v1)==4));
			assertTrue(of(1,2,3,4,5,6).combineLatest(of(3)).anyMatch(it-> (it.v1 == null ? -1 : it.v1)==5));
			assertTrue(of(1,2,3,4,5,6).combineLatest(of(3)).anyMatch(it-> (it.v1 == null ? -1 : it.v1)==6));
		}
	}
	@Test
	public void withLatest(){
		
		assertThat(of(1,2,3,4,5,6).withLatest(of(30,40,50,60,70,80,90,100,110,120,140))
				.collect(Collectors.toList()).size(),is(6));
		
	}
	@Test
	public void withLatestValues(){
		assertTrue(of(1,2,3,4,5,6).withLatest(of(30,40,50,60,70,80,90,100,110,120,140)).anyMatch(it-> it.v2==null));
		//assertTrue(of(1,2,3,4,5,6).combine(of(3)).oneMatch(it-> it.v2==3));
		assertTrue(of(1,2,3,4,5,6).withLatest(of(3)).anyMatch(it-> it.v1==1));
		assertTrue(of(1,2,3,4,5,6).withLatest(of(3)).anyMatch(it-> it.v1==2));
		assertTrue(of(1,2,3,4,5,6).withLatest(of(3)).anyMatch(it-> it.v1==3));
		assertTrue(of(1,2,3,4,5,6).withLatest(of(3)).anyMatch(it-> it.v1==4));
		assertTrue(of(1,2,3,4,5,6).withLatest(of(3)).anyMatch(it-> it.v1==5));
		assertTrue(of(1,2,3,4,5,6).withLatest(of(3)).anyMatch(it-> it.v1==6));
	}
	@Test
	public void skipUntil(){
		System.out.println(react(()->1,()->2,()->3,()->4,()->value2())
				.skipUntil(react(()->value())).collect(Collectors.toList()));
		assertTrue(react(()->1,()->2,()->3,()->4,()->value2()).skipUntil(react(()->value())).allMatch(it-> it==200));
		assertThat(react(()->1,()->2,()->3,()->4,()->value2()).skipUntil(react(()->value())).count(),is(1l));
	}
	@Test
	public void takeUntil(){
		System.out.println(react(()->1,()->2,()->3,()->4,()->value2())
				.takeUntil(react(()->value())).collect(Collectors.toList()));
		assertTrue(react(()->1,()->2,()->3,()->4,()->value2()).takeUntil(react(()->value())).noneMatch(it-> it==200));
		assertTrue(react(()->1,()->2,()->3,()->4,()->value2()).takeUntil(react(()->value())).anyMatch(it-> it==1));
	}
	@Test
	public void batchBySize(){
		System.out.println(of(1,2,3,4,5,6).batchBySize(3).collect(Collectors.toList()));
		for(int i=0;i<1000;i++)
			assertThat(of(1,2,3,4,5,6).batchBySize(3).collect(Collectors.toList()).size(),is(2));
	}
	@Test
	public void batchBySizeAndTimeSize(){
		
		assertThat(of(1,2,3,4,5,6).batchBySizeAndTime(3,10,TimeUnit.SECONDS).toList().get(0).size(),is(3));
	}
	@Test
	public void batchBySizeAndTimeTime(){
		
		for(int i=0;i<5;i++){
			
			List<List<Integer>> list = react(()->1,()->2,()->3,()->4,()->5,()->{sleep(150);return 6;})
					.batchBySizeAndTime(30,10,TimeUnit.MICROSECONDS)
					.toList();
			
			assertThat(list
							.get(0)
							,not(hasItem(6)));
		}
	}
	
	@Test
	public void batchBySizeSet(){
		
		
		assertThat(of(1,1,1,1,1,1).batchBySize(3,()->new TreeSet<Integer>()).block().get(0).size(),is(1));
		
		assertThat(of(1,1,1,1,1,1).batchBySize(3,()->new TreeSet<>()).block().get(1).size(),is(1));
	}
	@Test
	public void batchBySizeInternalSize(){
		assertThat(of(1,2,3,4,5,6).batchBySize(3).collect(Collectors.toList()).get(0).size(),is(3));
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
		assertThat(of(1,2,3,4,5,6).batchByTime(15000,TimeUnit.MICROSECONDS).collect(Collectors.toList()).size(),is(1));
	}
	@Test
	public void batchByTimeSet(){
		for(int i=0;i<5000;i++){
			List <Collection<Integer>> set = ofThread(1,1,1,1,1,1).batchByTime(1500,TimeUnit.MICROSECONDS,()-> new TreeSet<>()).block();
			System.out.println(set);
			assertThat(set.get(0).size(),is(1));
			}
	}
	@Test
	public void batchByTimeInternalSize(){
		assertThat(of(1,2,3,4,5,6).batchByTime(1,TimeUnit.NANOSECONDS).collect(Collectors.toList()).size(),greaterThan(5));
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
					
			Map<Integer, ? extends FutureStream<Integer>> sharded = of(1,2,3,4,5,6).shard(shards,i -> i%2);
			sharded.get(0).forEach(next ->{
				System.out.println ("next is " + next);
			});
			//assertThat(sharded.get(0).collect(Collectors.toList()),hasItem(6));
		}
	}
	@Test
	public void zip(){
		List<Tuple2<Integer,Integer>> list =
				of(1,2,3,4,5,6).zip(of(100,200,300,400))
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
	public void zip2of(){
		
		List<Tuple2<Integer,Integer>> list =of(1,2,3,4,5,6).zip(of(100,200,300,400))
						.peek(it -> System.out.println(it)).collect(Collectors.toList());
				//		.collect(Collectors.toList());
	
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
		
		
		final Seq<Integer> zipped = empty.zip(this.<Integer>of(), (a, b) -> a + b);
		assertTrue(zipped.collect(Collectors.toList()).isEmpty());
	}

	@Test
	public void shouldReturnEmptySeqWhenZipEmptyWithNonEmpty() throws Exception {
		
		
		
		final Seq<Integer> zipped = empty.zip(nonEmpty, (a, b) -> a + b);
		assertTrue(zipped.collect(Collectors.toList()).isEmpty());
	}

	@Test
	public void shouldReturnEmptySeqWhenZipNonEmptyWithEmpty() throws Exception {
		
		
		final Seq<Integer> zipped = nonEmpty.zip(empty, (a, b) -> a + b);

		
		assertTrue(zipped.collect(Collectors.toList()).isEmpty());
	}

	@Test
	public void shouldZipTwoFiniteSequencesOfSameSize() throws Exception {
		
		final FutureStream<String> first = of("A", "B", "C");
		final FutureStream<Integer> second = of(1, 2, 3);

		
		final Seq<String> zipped = first.zip(second, (a, b) -> a + b);

		
		assertThat(zipped.collect(Collectors.toList()).size(),is(3));
	}

	

	@Test
	public void shouldTrimSecondFixedSeqIfLonger() throws Exception {
		final FutureStream<String> first = of("A", "B", "C");
		final FutureStream<Integer> second = of(1, 2, 3, 4);

		
		final Seq<String> zipped = first.zip(second, (a, b) -> a + b);

		assertThat(zipped.collect(Collectors.toList()).size(),is(3));
	}

	@Test
	public void shouldTrimFirstFixedSeqIfLonger() throws Exception {
		final FutureStream<String> first = of("A", "B", "C","D");
		final FutureStream<Integer> second = of(1, 2, 3);
		final Seq<String> zipped = first.zip(second, (a, b) -> a + b);

		
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
        Supplier<Seq<Integer>> s = () ->of(1, 2, 3);

        assertEquals(3, s.get().shuffle().toList().size());
        assertThat(s.get().shuffle().toList(), hasItems(1, 2, 3));

        
    }

    @Test
    public void testCycle() {
    	   assertEquals(asList(1, 1, 1, 1, 1,1),of(1).cycle().limit(6).toList());
      
    }
    
    @Test
    public void testIterable() {
        List<Integer> list = of(1, 2, 3).toCollection(LinkedList::new);

        for (Integer i :of(1, 2, 3)) {
            assertThat(list,hasItem(i));
        }
    }
	
	@Test
	public void testDuplicate(){
		 Tuple2<Seq<Integer>, Seq<Integer>> copies =of(1,2,3,4,5,6).duplicate();
		 assertTrue(copies.v1.anyMatch(i->i==2));
		 assertTrue(copies.v2.anyMatch(i->i==2));
	}
	
	

	 Throwable ex;
	    @Test
	    public void testCastException() {
	    	ex = null;
	    	of(1, "a", 2, "b", 3, null).capture(e-> ex =e)
	    			.peek(it ->System.out.println(it))
	    			.cast(Integer.class)
	    				.peek(it ->System.out.println(it)).toList();
	    	
	    	assertThat(ex.getCause().getClass(),equalTo(ClassCastException.class));
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
	        Map<Integer, List<Integer>> map1 =of(1, 2, 3, 4).groupBy(i -> i % 2);
	       
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
	        assertTrue(asList(1,2).contains( list.get(0).v1));
	        assertTrue(""+list.get(1).v2,asList(1,2).contains( list.get(1).v1)); 
	        assertTrue(asList("a", "b", "c", "d").contains( list.get(0).v2));
	        assertTrue(asList("a", "b", "c", "d").contains( list.get(1).v2));
	       
	        
	    }

	    @Test
	    public void testZipWithIndex() {
	        assertEquals(asList(),of().zipWithIndex().toList());
	     //   System.out.println( of("a").zipWithIndex().toList().get(0));
	       
	      assertThat( of("a").zipWithIndex().map(t->t.v2).findFirst().get(),is(0l));
	      assertEquals(asList(tuple("a", 0L)), of("a").zipWithIndex().toList());
	     //   assertEquals(asList(tuple("a", 0L), tuple("b", 1L)), of("a", "b").zipWithIndex().toList());
	       //assertThat(asList(tuple("a", 0L), tuple("b", 1L), tuple("c", 2L)), is(of("a", "b", "c").zipWithIndex().toList()));
	    }

	   
	    @Test
	    public void testSkipWhile() {
	        Supplier<Seq<Integer>> s = () -> of(1, 2, 3, 4, 5);

	        assertTrue(s.get().skipWhile(i -> false).toList().containsAll(asList(1, 2, 3, 4, 5)));
	      
	        assertEquals(asList(), s.get().skipWhile(i -> true).toList());
	    }

	    @Test
	    public void testSkipUntil() {
	        Supplier<Seq<Integer>> s = () -> of(1, 2, 3, 4, 5);

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
	        Supplier<Seq<Integer>> s = () -> of(1, 2, 3, 4, 5);

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
	        Supplier<Seq<Integer>> s = () -> of(1, 2, 3, 4, 5, 6);

	        assertEquals(6, s.get().partition(i -> i % 2 != 0).v1.toList().size() + s.get().partition(i -> i % 2 != 0).v2.toList().size());
	        
	        assertTrue(s.get().partition(i -> true).v1.toList().containsAll(asList(1, 2, 3, 4, 5, 6)));
	        assertEquals(asList(), s.get().partition(i -> true).v2.toList());

	        assertEquals(asList(), s.get().partition(i -> false).v1.toList());
	        assertTrue(s.get().partition(i -> false).v2.toList().containsAll(asList(1, 2, 3, 4, 5, 6)));
	    }

	    @Test
	    public void testPartition100(){
	    	
	    	for(int i=0;i<0;i++){
		    	Supplier<Seq<Tuple2<Integer,Integer>>> s = ()-> of(tuple(1, 0),tuple(2, 1), tuple(3, 2), tuple(4, 3), tuple(5, 4), tuple(6, 5));
		    	
		    	Tuple2<Seq<Tuple2<Integer, Integer>>, Seq<Tuple2<Integer, Integer>>> tuple = s.get().partition(t -> t.v2 < 3);
		    	
		    	Tuple2<Seq<Integer>, Seq<Integer>> t2 = tuple.map((v1, v2) -> Tuple.<Seq<Integer>, Seq<Integer>>tuple(
		                v1.map(t -> t.v1),
		                v2.map(t -> t.v1)
		            ));
		            
		    	List l1 = t2.v1.toList();
		    	System.out.println("l1:" +l1);
		    	List l2 = t2.v2.toList();
		    	 System.out.println("l2:"+l2);
		    	assertThat(l1.size(),is(3));
		    	assertThat(l2.size(),is(3));
	    	}
	    	
	    }
	
	@Test
	public void testSplitAt() {
		for (int i = 0; i < 1000; i++) {
			Supplier<Seq<Integer>> s = () -> of(1, 2, 3, 4, 5, 6);

			assertEquals(asList(), s.get().splitAt(0).v1.toList());
			assertTrue(s.get().splitAt(0).v2.toList().containsAll(
					asList(1, 2, 3, 4, 5, 6)));

			assertEquals(1, s.get().splitAt(1).v1.toList().size());
			assertEquals(s.get().splitAt(1).v2.toList().size(), 5);

			assertEquals(3, s.get().splitAt(3).v1.toList().size());

			assertEquals(3, s.get().splitAt(3).v2.count());

			assertEquals(6, s.get().splitAt(6).v1.toList().size());
			assertEquals(asList(), s.get().splitAt(6).v2.toList());

			assertThat(s.get().splitAt(7).v1.toList().size(), is(6));
			assertEquals(asList(), s.get().splitAt(7).v2.toList());

		}
	}

	    @Test
	    public void testSplitAtHead() {
	        assertEquals(Optional.empty(), of().splitAtHead().v1);
	        assertEquals(asList(), Seq.of().splitAtHead().v2.toList());

	        assertEquals(Optional.of(1), Seq.of(1).splitAtHead().v1);
	        assertEquals(asList(), Seq.of(1).splitAtHead().v2.toList());

	        assertEquals(Optional.of(1), Seq.of(1, 2).splitAtHead().v1);
	        assertEquals(asList(2), Seq.of(1, 2).splitAtHead().v2.toList());

	        assertEquals(Optional.of(1), Seq.of(1, 2, 3).splitAtHead().v1);
	        assertEquals(Optional.of(2), Seq.of(1, 2, 3).splitAtHead().v2.splitAtHead().v1);
	        assertEquals(Optional.of(3), Seq.of(1, 2, 3).splitAtHead().v2.splitAtHead().v2.splitAtHead().v1);
	        assertEquals(asList(2, 3), Seq.of(1, 2, 3).splitAtHead().v2.toList());
	        assertEquals(asList(3), Seq.of(1, 2, 3).splitAtHead().v2.splitAtHead().v2.toList());
	        assertEquals(asList(), Seq.of(1, 2, 3).splitAtHead().v2.splitAtHead().v2.splitAtHead().v2.toList());
	    }

	    @Test
	    public void testMinByMaxBy() {
	        Supplier<Seq<Integer>> s = () -> of(1, 2, 3, 4, 5, 6);

	        assertEquals(1, (int) s.get().maxBy(t -> Math.abs(t - 5)).get());
	        assertEquals(5, (int) s.get().minBy(t -> Math.abs(t - 5)).get());

	        assertEquals(6, (int) s.get().maxBy(t -> "" + t).get());
	        assertEquals(1, (int) s.get().minBy(t -> "" + t).get());
	    }

	    @Test
	    public void testUnzip() {
	    	//order is not guaranteed as populated asynchronously
	        Supplier<Seq<Tuple2<Integer, String>>> s = () -> of(tuple(1, "a"), tuple(2, "b"), tuple(3, "c"));

	        for(int i=0;i<1000;i++){
	        Tuple2<Seq<Integer>, Seq<String>> u1 = Seq.unzip(s.get());
	      
	        assertTrue(u1.v1.toList().containsAll(Arrays.asList(1, 2, 3)));
	       
	       
	        assertTrue(u1.v2.toList().containsAll(asList("a", "b", "c")));

	        Tuple2<Seq<Integer>, Seq<String>> u2 = Seq.unzip(s.get(), v1 -> -v1, v2 -> v2 + "!");
	        assertTrue(u2.v1.toList().containsAll(asList(-1, -2, -3)));
	        assertTrue(u2.v2.toList().containsAll(asList("a!", "b!", "c!")));

	        Tuple2<Seq<Integer>, Seq<String>> u3 = Seq.unzip(s.get(), t -> tuple(-t.v1, t.v2 + "!"));
	        assertTrue(u3.v1.toList().containsAll(asList(-1, -2, -3)));
	        assertTrue(u3.v2.toList().containsAll(asList("a!", "b!", "c!")));

	        Tuple2<Seq<Integer>, Seq<String>> u4 = Seq.unzip(s.get(), (t1, t2) -> tuple(-t1, t2 + "!"));
	        assertTrue(u4.v1.toList().containsAll(asList(-1, -2, -3)));
	        assertTrue(u4.v2.toList().containsAll(asList("a!", "b!", "c!")));
	        }
	    }
	   

	    @Test
	    public void testFoldLeft() {
	    	for(int i=0;i<100;i++){
		        Supplier<Seq<String>> s = () -> of("a", "b", "c");
	
		        assertTrue(s.get().foldLeft("", String::concat).contains("a"));
		        assertTrue(s.get().foldLeft("", String::concat).contains("b"));
		        assertTrue(s.get().foldLeft("", String::concat).contains("c"));
		       
		        assertEquals(3, (int) s.get().foldLeft(0, (u, t) -> u + t.length()));
	
		        
		        assertEquals(3, (int) s.get().foldRight(0, (t, u) -> u + t.length()));
	    	}
	    }
	    
	    @Test
	    public void testFoldRight(){
	    	 	Supplier<Seq<String>> s = () -> of("a", "b", "c");

		        assertTrue(s.get().foldRight("", String::concat).contains("a"));
		        assertTrue(s.get().foldRight("", String::concat).contains("b"));
		        assertTrue(s.get().foldRight("", String::concat).contains("c"));
		        assertEquals(3, (int) s.get().foldRight(0, (t, u) -> u + t.length()));
	    }
	    
	    @Test
	    public void testFoldLeftStringBuilder() {
	        Supplier<Seq<String>> s = () -> of("a", "b", "c");

	        
	        assertTrue(s.get().foldLeft(new StringBuilder(), (u, t) -> u.append("-").append(t)).toString().contains("a"));
	        assertTrue(s.get().foldLeft(new StringBuilder(), (u, t) -> u.append("-").append(t)).toString().contains("b"));
	        assertTrue(s.get().foldLeft(new StringBuilder(), (u, t) -> u.append("-").append(t)).toString().contains("c"));
	        assertTrue(s.get().foldLeft(new StringBuilder(), (u, t) -> u.append("-").append(t)).toString().contains("-"));
	        
	        
	        assertEquals(3, (int) s.get().foldLeft(0, (u, t) -> u + t.length()));

	       
	    }

	    @Test
	    public void testFoldRighttringBuilder() {
	        Supplier<Seq<String>> s = () -> of("a", "b", "c");

	        
	        assertTrue(s.get().foldRight(new StringBuilder(), (t, u) -> u.append("-").append(t)).toString().contains("a"));
	        assertTrue(s.get().foldRight(new StringBuilder(), (t, u) -> u.append("-").append(t)).toString().contains("b"));
	        assertTrue(s.get().foldRight(new StringBuilder(), (t, u) -> u.append("-").append(t)).toString().contains("c"));
	        assertTrue(s.get().foldRight(new StringBuilder(), (t, u) -> u.append("-").append(t)).toString().contains("-"));
	        
	           
	    }
	    //tests converted from lazy-seq suite
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
