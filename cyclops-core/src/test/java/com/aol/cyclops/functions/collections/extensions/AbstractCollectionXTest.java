package com.aol.cyclops.functions.collections.extensions;


import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.jooq.lambda.tuple.Tuple4;
import org.junit.Before;
import org.junit.Test;

import com.aol.cyclops.Monoid;
import com.aol.cyclops.Reducers;
import com.aol.cyclops.data.collections.extensions.CollectionX;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.monad.AnyM;
import com.aol.cyclops.sequence.SequenceM;
import com.aol.cyclops.types.Decomposable;
import com.aol.cyclops.types.Traversable;
import com.aol.cyclops.util.StreamUtils;
import com.aol.cyclops.util.Streamable;

import lombok.AllArgsConstructor;

public abstract class AbstractCollectionXTest {
	public abstract <T> CollectionX<T> empty();
	public abstract <T> CollectionX<T> of(T... values);
	
	private int addOne(Integer i){
		return i+1;
	}
	private int add(Integer a, Integer b){
		return a+b;
	}
	private String concat(String a, String b, String c){
		return a+b+c;
	}
	private String concat4(String a, String b, String c,String d){
		return a+b+c+d;
	}
	private String concat5(String a, String b, String c,String d,String e){
		return a+b+c+d+e;
	}
	@Test
	public void zap1(){
		assertThat(of(1,2,3).ap1(this::addOne)
				  .toListX(),equalTo(Arrays.asList(2,3,4)));
		
	}
	@Test
	public void zap2(){
		assertThat(of(1,2,3).ap2(this::add)
				  .ap(of(3,4,5))
				  .toListX(),equalTo(Arrays.asList(4,6,8)));
		
	}
	@Test
	public void zap3(){
		assertThat(of("a","b","c")
				  .ap3(this::concat)
				  .ap(of("1","2","3"))
				  .ap(of(".","?","!"))
				  .toListX(),equalTo(Arrays.asList("a1.","b2?","c3!")));
	}
	@Test
	public void zap4(){
		assertThat(of("a","b","c")
				  .ap4(this::concat4)
				  .ap(of("1","2","3"))
				  .ap(of(".","?","!"))
				  .ap(of("R","R","R"))
				  .toListX(),equalTo(Arrays.asList("a1.R","b2?R","c3!R")));
	}
	@Test
	public void zap5(){
		assertThat(of("a","b","c")
				  .ap5(this::concat5)
				  .ap(of("1","2","3"))
				  .ap(of(".","?","!"))
				  .ap(of("R","R","R"))
				  .ap(of("Z","Z","Z"))
				  .toListX(),equalTo(Arrays.asList("a1.RZ","b2?RZ","c3!RZ")));
	}
	@Test
	public void when(){
		
		String res=	of(1,2,3).visit((x,xs)->
								xs.join(x.visit(some-> (int)some>2? "hello" : "world",()->"boo!"))
					);
		assertThat(res,equalTo("2world3"));
	}
	@Test
	public void whenGreaterThan2(){
		String res=	of(5,2,3).visit((x,xs)->
								xs.join(x.visit(some-> (int)some>2? "hello" : "world",()->"boo!"))
					);
		assertThat(res,equalTo("2hello3"));
	}
	@Test
	public void when2(){
		
		Integer res =	of(1,2,3).visit((x,xs)->{
						
								System.out.println(x.isPresent());
								System.out.println(x.get());
								return x.get();
								});
		System.out.println(res);
	}
	@Test
	public void whenNilOrNot(){
		String res1=	ListX.of(1,2,3).visit((x,xs)-> x.visit(some-> (int)some>2? "hello" : "world",()->"EMPTY"));
	}
	@Test
	public void whenNilOrNotJoinWithFirstElement(){
		
		
		String res=	ListX.of(1,2,3).visit((x,xs)-> x.visit(some-> xs.join((int)some>2? "hello" : "world"),()->"EMPTY"));
		assertThat(res,equalTo("2world3"));
	}
	
	@Test
	public void testCollectable(){
		assertThat(of(1,2,3).collectable().anyMatch(i->i==2),equalTo(true));
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
	
	
	
	
	CollectionX<Integer> empty;
	CollectionX<Integer> nonEmpty;

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
		System.out.println(of(1,2,3,4,5,6).grouped(3).collect(Collectors.toList()));
		assertThat(of(1,2,3,4,5,6).grouped(3).collect(Collectors.toList()).size(),is(2));
	}
	

	

	
	@Test
	public void limitWhileTest(){
		
		List<Integer> list = new ArrayList<>();
		while(list.size()==0){
			list = of(1,2,3,4,5,6).takeWhile(it -> it<4)
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
        List<Integer> list = of(1, 2, 3).toCollection(LinkedList::new);

        for (Integer i :of(1, 2, 3)) {
            assertThat(list,hasItem(i));
        }
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

	    
	   
	  

	   
	    @Test
	    public void testSkipWhile() {
	        Supplier<CollectionX<Integer>> s = () -> of(1, 2, 3, 4, 5);

	        assertTrue(s.get().dropWhile(i -> false).toList().containsAll(asList(1, 2, 3, 4, 5)));
	      
	        assertEquals(asList(), s.get().dropWhile(i -> true).toList());
	    }

	    @Test
	    public void testSkipUntil() {
	        Supplier<CollectionX<Integer>> s = () -> of(1, 2, 3, 4, 5);

	        assertEquals(asList(), s.get().dropUntil(i -> false).toList());
	        assertTrue(s.get().dropUntil(i -> true).toList().containsAll(asList(1, 2, 3, 4, 5)));
		  }

	   

	    @Test
	    public void testLimitWhile() {
	        Supplier<CollectionX<Integer>> s = () -> of(1, 2, 3, 4, 5);

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
	        Supplier<CollectionX<Integer>> s = () -> of(1, 2, 3, 4, 5, 6);

	        assertEquals(1, (int) s.get().maxBy(t -> Math.abs(t - 5)).get());
	        assertEquals(5, (int) s.get().minBy(t -> Math.abs(t - 5)).get());

	        assertEquals(6, (int) s.get().maxBy(t -> "" + t).get());
	        assertEquals(1, (int) s.get().minBy(t -> "" + t).get());
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
	public void forEach2() {

		assertThat(of(1, 2, 3).forEach2(a -> Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9), a -> b -> a + b).toList(),
				equalTo(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 3, 4, 5, 6, 7, 8,
						9, 10, 11, 12)));
	}

	@Test
	public void forEach2Filter() {

		assertThat(of(1, 2, 3).forEach2(a -> Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10), a -> b -> a > 2 && b < 8,
				a -> b -> a + b).toList(), equalTo(Arrays.asList(3, 4, 5, 6, 7, 8, 9, 10)));
	}
	
	
	
	
	
	
	
	
	
	
	@Test
    public void emptyPermutations() {
        assertThat(of().permutations().map(s->s.toList()).toList(),equalTo(Arrays.asList()));
    }

    @Test
    public void permuations3() {
    	System.out.println(of(1, 2, 3).permutations().map(s->s.toList()).toList());
        assertThat(of(1, 2, 3).permutations().map(s->s.toList()).toList(),
        		equalTo(of(of(1, 2, 3),
        		of(1, 3, 2), of(2, 1, 3), of(2, 3, 1), of(3, 1, 2), of(3, 2, 1)).peek(i->System.out.println("peek - " + i)).map(s->s.toList()).toList()));
    }
    
    @Test
    public void emptyAllCombinations() {
        assertThat(of().combinations().map(s->s.toList()).toList(),equalTo(Arrays.asList(Arrays.asList())));
    }

    @Test
    public void allCombinations3() {
        assertThat(of(1, 2, 3).combinations().map(s->s.toList()).toList(),equalTo(Arrays.asList(Arrays.asList(), Arrays.asList(1), Arrays.asList(2),
        		Arrays.asList(3), Arrays.asList(1, 2), Arrays.asList(1, 3), Arrays.asList(2, 3), Arrays.asList(1, 2, 3))));
    }

  

    @Test
    public void emptyCombinations() {
        assertThat(of().combinations(2).toList(),equalTo(Arrays.asList()));
    }

    @Test
    public void combinations2() {
        assertThat(of(1, 2, 3).combinations(2).map(s->s.toList()).toList(),
                equalTo(Arrays.asList(Arrays.asList(1, 2), Arrays.asList(1, 3), Arrays.asList(2, 3))));
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
		
		
		assertTrue(of(1,2,3,4,5).stream().elapsed().noneMatch(t->t.v2<0));
	}
	@Test
	public void timeStamp(){
		
		
		assertTrue(of(1,2,3,4,5)
							.stream()
							.timestamp()
							.allMatch(t-> t.v2 <= System.currentTimeMillis()));
		

	}
	@Test
	public void elementAt0(){
		assertThat(of(1).stream().elementAt(0).v1,equalTo(1));
	}
	@Test
	public void getMultple(){
		assertThat(of(1,2,3,4,5).stream().elementAt(2).v1,equalTo(3));
	}
	@Test
	public void getMultpleStream(){
		assertThat(of(1,2,3,4,5).stream().elementAt(2).v2.toList(),equalTo(Arrays.asList(1,2,3,4,5)));
	}
	@Test(expected=NoSuchElementException.class)
	public void getMultiple1(){
		of(1).stream().elementAt(1);
	}
	@Test(expected=NoSuchElementException.class)
	public void getEmpty(){
		of().stream().elementAt(0);
	}
	@Test
	public void get0(){
		assertTrue(of(1).get(0).isPresent());
	}
	@Test
	public void getAtMultple(){
		assertThat(of(1,2,3,4,5).get(2).get(),equalTo(3));
	}
	@Test
	public void getAt1(){
		assertFalse(of(1).get(1).isPresent());
	}
	@Test
	public void elementAtEmpty(){
		assertFalse(of().get(0).isPresent());
	}
	@Test
	public void singleTest(){
		assertThat(of(1).single(),equalTo(1));
	}
	@Test(expected=UnsupportedOperationException.class)
	public void singleEmpty(){
		of().single();
	}
	@Test(expected=UnsupportedOperationException.class)
	public void single2(){
		of(1,2).single();
	}
	@Test
	public void singleOptionalTest(){
		assertThat(of(1).singleOptional().get(),equalTo(1));
	}
	@Test
	public void singleOptionalEmpty(){
		assertFalse(of().singleOptional().isPresent());
	}
	@Test
	public void singleOptonal2(){
		assertFalse(of(1,2).singleOptional().isPresent());
	}
	
	@Test
	public void limitTimeEmpty(){
		List<Integer> result = SequenceM.<Integer>of()
										.peek(i->sleep(i*100))
										.limit(1000,TimeUnit.MILLISECONDS)
										.toList();
		
		
		assertThat(result,equalTo(Arrays.asList()));
	}
	
	@Test
	public void skipTimeEmpty(){
		List<Integer> result = SequenceM.<Integer>of()
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
							.toListX(),equalTo(Arrays.asList(1,2,3)));
	}
	@Test
	public void testSkipLastEmpty(){
		assertThat(of()
							.skipLast(2)
							.stream().collect(Collectors.toList()),equalTo(Arrays.asList()));
	}
	@Test
	public void testLimitLast(){
		assertThat(of(1,2,3,4,5)
							.limitLast(2)
							.stream().collect(Collectors.toList()),equalTo(Arrays.asList(4,5)));
	}
	@Test
	public void testLimitLastEmpty(){
		assertThat(of()
							.limitLast(2)
							.stream().collect(Collectors.toList()),equalTo(Arrays.asList()));
	}
	@Test
	public void endsWith(){
		assertTrue(of(1,2,3,4,5,6)
				.endsWith(Arrays.asList(5,6)));
	}
	@Test
	public void endsWithFalse(){
		assertFalse(of(1,2,3,4,5,6)
				.endsWith(Arrays.asList(5,6,7)));
	}
	@Test
	public void endsWithToLong(){
		assertFalse(of(1,2,3,4,5,6)
				.endsWith(Arrays.asList(0,1,2,3,4,5,6)));
	}
	@Test
	public void endsWithEmpty(){
		assertTrue(of(1,2,3,4,5,6)
				.endsWith(Arrays.asList()));
	}
	@Test
	public void endsWithWhenEmpty(){
		assertFalse(of()
				.endsWith(Arrays.asList(1,2,3,4,5,6)));
	}
	@Test
	public void endsWithBothEmpty(){
		assertTrue(SequenceM.<Integer>of()
				.endsWith(Arrays.asList()));
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
		assertTrue(SequenceM.<Integer>of()
				.endsWith(Stream.of()));
	}
	
	@Test
	public void streamable(){
		Streamable<Integer> repeat = ((Traversable)of(1,2,3,4,5,6)
												.map(i->i*2)
												)
												.toStreamable();
		
		assertThat(repeat.sequenceM().toList(),equalTo(Arrays.asList(2,4,6,8,10,12)));
		assertThat(repeat.sequenceM().toList(),equalTo(Arrays.asList(2,4,6,8,10,12)));
	}
	
	@Test
	public void concurrentLazyStreamable(){
		Streamable<Integer> repeat = of(1,2,3,4,5,6)
												.map(i->i*2)
												.toConcurrentLazyStreamable();
		
		assertThat(repeat.sequenceM().toList(),equalTo(Arrays.asList(2,4,6,8,10,12)));
		assertThat(repeat.sequenceM().toList(),equalTo(Arrays.asList(2,4,6,8,10,12)));
	}
	/**
	@Test
	public void splitBy(){
		assertThat( of(1, 2, 3, 4, 5, 6).stream().splitBy(i->i<4).v1.toList(),equalTo(Arrays.asList(1,2,3)));
		assertThat( of(1, 2, 3, 4, 5, 6).splitBy(i->i<4).v2.toList(),equalTo(Arrays.asList(4,5,6)));
	}
	**/
	@Test
	public void testLazy(){
		Collection<Integer> col = of(1,2,3,4,5)
											.peek(System.out::println)
											.toLazyCollection();
		System.out.println("first!");
		col.forEach(System.out::println);
		assertThat(col.size(),equalTo(5));
	}
	@Test
	public void testLazyCollection(){
		Collection<Integer> col = of(1,2,3,4,5)
											.peek(System.out::println)
											.toConcurrentLazyCollection();
		System.out.println("first!");
		col.forEach(System.out::println);
		assertThat(col.size(),equalTo(5));
	}
	int peek = 0;
	@Test
	public void testPeek() {
		peek = 0 ;
		   AnyM.fromStream(Stream.of(asList(1,3)))
				  				.flatMap(c->AnyM.fromStream(c.stream()))
				  				.asSequence()
				  				.map(i->i*2)
				  				.peek(i-> peek=i)
				  				.collect(Collectors.toList());
		assertThat(peek,equalTo(6));
	}
	@Test
	public void testMap() {
		  List<Integer> list = AnyM.fromStream(Stream.of(asList(1,3)))
				  				.flatMap(c->AnyM.fromStream(c.stream()))
				  				.asSequence()
				  				.map(i->i*2)
				  				.peek(System.out::println)
				  				.collect(Collectors.toList());
		assertThat(Arrays.asList(2,6),equalTo(list));
	}
	@Test
	public void headAndTailTest(){
		Stream<String> s = Stream.of("hello","world");
		Iterator<String> it = s.iterator();
		String head = it.next();
		Stream<String> tail = StreamUtils.stream(it);
		tail.forEach(System.out::println);
	}
	@Test
	public void testOfType() {

		

		assertThat((((Traversable<Serializable>)of(1, "a", 2, "b", 3, null).ofType(Integer.class))).toListX(),containsInAnyOrder(1, 2, 3));

		assertThat((((Traversable<Serializable>)of(1, "a", 2, "b", 3, null).ofType(Integer.class))).toListX(),not(containsInAnyOrder("a", "b",null)));

		assertThat(((Traversable<Serializable>)of(1, "a", 2, "b", 3, null)

				.ofType(Serializable.class)).toListX(),containsInAnyOrder(1, "a", 2, "b", 3));

	}

	@Test
	public void testCastPast() {
		of(1, "a", 2, "b", 3, null).cast(Date.class).map(d -> d.getTime());
	



	}
	
	@Test
	public void flatMapCompletableFuture(){
		assertThat(of(1,2,3).stream().flatMapCompletableFuture(i->CompletableFuture.completedFuture(i+2))
				  								.collect(Collectors.toList()),
				  								equalTo(Arrays.asList(3,4,5)));
	}
	@Test
	public void flatMapOptional(){
		assertThat(of(1,2,3,null).stream().flatMapOptional(Optional::ofNullable)
			      										.collect(Collectors.toList()),
			      										equalTo(Arrays.asList(1,2,3)));
	}
	@Test
	public void testIntersperse() {
		
		assertThat(((Traversable<Integer>)of(1,2,3).intersperse(0)).toListX(),equalTo(Arrays.asList(1,0,2,0,3)));
	



	}
	@Test(expected=ClassCastException.class)
	public void cast(){
		of(1,2,3).cast(String.class).toListX();
	}
	@Test
	public void xMatch(){
		assertTrue(of(1,2,3,5,6,7).xMatch(3, i-> i>4 ));
	}
	
	@Test
	public void zip3(){
		List<Tuple3<Integer,Integer,Character>> list =
				of(1,2,3,4,5,6).zip3(of(100,200,300,400).stream(),of('a','b','c').stream())
												.toListX();
		
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
				of(1,2,3,4,5,6).zip4(of(100,200,300,400).stream(),of('a','b','c').stream(),of("hello","world").stream())
												.toListX();
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
											.zip(of(100,200,300,400).stream())
											.toListX();
				
	
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
													.zip( of(100,200,300,400).stream())
													.toListX();
		
		assertThat(asList(1,2,3,4,5,6),hasItem(list.get(0).v1));
		assertThat(asList(100,200,300,400),hasItem(list.get(0).v2));
		
		
		
	}

	@Test
	public void zipEmpty() throws Exception {
		
		
		final CollectionX<Integer> zipped = this.<Integer>empty().zip(SequenceM.<Integer>of(), (a, b) -> a + b);
		assertTrue(zipped.collect(Collectors.toList()).isEmpty());
	}

	@Test
	public void shouldReturnEmptySeqWhenZipEmptyWithNonEmpty() throws Exception {
		
		
		
		final CollectionX<Integer> zipped = this.<Integer>empty().zip(of(1,2), (a, b) -> a + b);
		assertTrue(zipped.collect(Collectors.toList()).isEmpty());
	}

	@Test
	public void shouldReturnEmptySeqWhenZipNonEmptyWithEmpty() throws Exception {
		
		
		final CollectionX<Integer> zipped = of(1,2,3).zip(this.<Integer>empty(), (a, b) -> a + b);

		
		assertTrue(zipped.collect(Collectors.toList()).isEmpty());
	}

	@Test
	public void shouldZipTwoFiniteSequencesOfSameSize() throws Exception {
		
		final CollectionX<String> first = of("A", "B", "C");
		final CollectionX<Integer> second = of(1, 2, 3);

		
		final CollectionX<String> zipped = first.zip(second, (a, b) -> a + b);

		
		assertThat(zipped.collect(Collectors.toList()).size(),is(3));
	}

	

	@Test
	public void shouldTrimSecondFixedSeqIfLonger() throws Exception {
		final CollectionX<String> first = of("A", "B", "C");
		final CollectionX<Integer> second = of(1, 2, 3, 4);

		
		final CollectionX<String> zipped = first.zip(second, (a, b) -> a + b);

		assertThat(zipped.collect(Collectors.toList()).size(),is(3));
	}

	@Test
	public void shouldTrimFirstFixedSeqIfLonger() throws Exception {
		final CollectionX<String> first = of("A", "B", "C","D");
		final CollectionX<Integer> second = of(1, 2, 3);
		final CollectionX<String> zipped = first.zip(second, (a, b) -> a + b);

		
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
		final CollectionX<String> first = of("A", "B", "C");
		final CollectionX<Integer> second = of(1, 2, 3, 4);

		
		final CollectionX<String> zipped = first.zip(second, (a, b) -> a + b);

		assertThat(zipped.collect(Collectors.toList()).size(),is(3));
	}

	@Test
	public void shouldTrimFirstFixedSeqIfLongerStream() throws Exception {
		final CollectionX<String> first = of("A", "B", "C","D");
		final CollectionX<Integer> second = of(1, 2, 3);
		
		final CollectionX<String> zipped = first.zip(second, (a, b) -> a + b);

		
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
		final CollectionX<String> first = of("A", "B", "C");
		final CollectionX<Integer> second = of(1, 2, 3, 4);

		
		final CollectionX<String> zipped = first.zip(second, (a, b) -> a + b);

		assertThat(zipped.collect(Collectors.toList()).size(),is(3));
	}

	@Test
	public void shouldTrimFirstFixedSeqIfLongerSequence() throws Exception {
		final CollectionX<String> first = of("A", "B", "C","D");
		final CollectionX<Integer> second = of(1, 2, 3);
		final CollectionX<String> zipped = first.zip(second, (a, b) -> a + b);

		
		assertThat(zipped.collect(Collectors.toList()).size(),equalTo(3));
	}

	
	@Test
	public void testZipWithIndex() {
		assertEquals(asList(), of().zipWithIndex().toListX());

		assertThat(of("a").zipWithIndex().map(t -> t.v2).findFirst().get(), is(0l));
		assertEquals(asList(new Tuple2("a", 0L)), of("a").zipWithIndex().toListX());

	}
	
	@Test
	public void patternTest1(){
		List<String> result = of(1,2,3,4)
								         .patternMatch("",
													  	c->c.where(i->"even", (Integer i)->i%2==0 )
													  	    .where( i->"odd",(Integer i)->i%2!=0)
													  )
											  .toListX();
		assertThat(result,equalTo(Arrays.asList("odd","even","odd","even")));
	}
	@Test
	public void patternTest2(){
		List<String> result = of(1,2,3,4)
										.patternMatch("n/a",c->c.just(i->"one",1))
											 .toListX();
		assertThat(result,equalTo(Arrays.asList("one","n/a","n/a","n/a")));
	}
	@Test
	public void patternTestDecomposable(){
		List<String> result = of(new MyCase(1,2),new MyCase(3,4))
											
											  .patternMatch("n/a",
													  c->c.values(i->"one",1,2)
													      .values(i->"two",3,4)
													      .values(i->"three",1,4)
													      .values(i->"four",2,3)
													  
													  
													  )
											  .toListX();
		assertThat(result,equalTo(Arrays.asList("one","two")));
	}
	@Test
	public void patternTestPojo(){
		
		List<String> result = of(new MyCase2(1,2),new MyCase2(3,4))
											  .patternMatch("n/a",
													  c->c.values(i->"one",1,2)
													      .values(i->"two",3,4)
													      .values(i->"three",5,6)
													  )
											  .toListX();
		assertThat(result,equalTo(Arrays.asList("one","two")));
	}
	@AllArgsConstructor
	static class MyCase implements Decomposable{
		int first;
		int second;
	}
	@AllArgsConstructor
	static class MyCase2 {
		int first;
		int second;
	}
	
	@Test
	public void emptyConvert(){
		assertFalse(empty().toMaybe().isPresent());
		assertFalse(empty().toOptional().isPresent());
		assertFalse(empty().toListX().size()>0);
		assertFalse(empty().toDequeX().size()>0);
		assertFalse(empty().toPStackX().size()>0);
		assertFalse(empty().toQueueX().size()>0);
		assertFalse(empty().toPVectorX().size()>0);
		assertFalse(empty().toPQueueX().size()>0);
		assertFalse(empty().toSetX().size()>0);
		assertFalse(empty().toSortedSetX().size()>0);
		assertFalse(empty().toPOrderedSetX().size()>0);
		assertFalse(empty().toPBagX().size()>0);
		assertFalse(empty().toPMapX(t->t,t->t).size()>0);
		assertFalse(empty().toMapX(t->t,t->t).size()>0);
		assertFalse(empty().toXor().get().size()>0);
		assertFalse(empty().toIor().get().size()>0);
		assertTrue(empty().toXor().isPrimary());
		assertTrue(empty().toIor().isPrimary());

		assertFalse(empty().toXorSecondary().isPrimary());
		assertFalse(empty().toIorSecondary().isPrimary());
		assertFalse(empty().toTry().isSuccess());
		assertFalse(empty().toEvalNow().get().size()>0);
		assertFalse(empty().toEvalLater().get().size()>0);
		assertFalse(empty().toEvalAlways().get().size()>0);
		assertFalse(empty().toCompletableFuture().join().size()>0);
		assertFalse(empty().toSet().size()>0);
		assertFalse(empty().toList().size()>0);
		assertFalse(empty().toStreamable().size()>0);
		
		
	}
	@Test
	public void presentConvert(){
		assertTrue(of(1).toMaybe().isPresent());
		assertTrue(of(1).toOptional().isPresent());
		assertTrue(of(1).toListX().size()>0);
		assertTrue(of(1).toDequeX().size()>0);
		assertTrue(of(1).toPStackX().size()>0);
		assertTrue(of(1).toQueueX().size()>0);
		assertTrue(of(1).toPVectorX().size()>0);
		assertTrue(of(1).toPQueueX().size()>0);
		assertTrue(of(1).toSetX().size()>0);
		assertTrue(of(1).toSortedSetX().size()>0);
		assertTrue(of(1).toPOrderedSetX().size()>0);
		assertTrue(of(1).toPBagX().size()>0);
		assertTrue(of(1).toPMapX(t->t,t->t).size()>0);
		assertTrue(of(1).toMapX(t->t,t->t).size()>0);
		assertTrue(of(1).toXor().get().size()>0);
		assertTrue(of(1).toIor().get().size()>0);
		assertTrue(of(1).toXor().isPrimary());
		assertTrue(of(1).toIor().isPrimary());
		assertFalse(of(1).toXorSecondary().isPrimary());
		assertFalse(of(1).toIorSecondary().isPrimary());
		assertTrue(of(1).toTry().isSuccess());
		assertTrue(of(1).toEvalNow().get().size()>0);
		assertTrue(of(1).toEvalLater().get().size()>0);
		assertTrue(of(1).toEvalAlways().get().size()>0);
		assertTrue(of(1).toCompletableFuture().join().size()>0);
		assertTrue(of(1).toSet().size()>0);
		assertTrue(of(1).toList().size()>0);
		assertTrue(of(1).toStreamable().size()>0);
		
		
	}


			
}
