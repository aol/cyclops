package com.aol.cyclops.functions.collections.extensions;


import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.jooq.lambda.tuple.Tuple2;
import org.junit.Before;
import org.junit.Test;

import com.aol.cyclops.collections.extensions.CollectionX;
import com.aol.cyclops.sequence.Monoid;
import com.aol.cyclops.sequence.Reducers;


public abstract class AbstractCollectionXTest {

	public abstract <T> CollectionX<T> of(T... values);
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
		
}
