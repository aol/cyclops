package com.aol.cyclops.javaslang.reactivestreams.reactivestream.tests;


import static com.aol.cyclops.javaslang.reactivestreams.ReactiveStream.of;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javaslang.collection.List;
import javaslang.collection.Map;
import javaslang.collection.Stream;

import org.junit.Before;
import org.junit.Test;

import com.aol.cyclops.javaslang.reactivestreams.ReactiveStream;
import com.aol.cyclops.sequence.Monoid;



//see BaseSequentialSeqTest for in order tests
public  class BaseSequenceMTest {
	
	
	ReactiveStream<Integer> empty;
	ReactiveStream<Integer> nonEmpty;

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
		System.out.println(of(1,2,3,4,5,6).windowBySize(3).collect(Collectors.toList()));
		assertThat(of(1,2,3,4,5,6).windowBySize(3).collect(Collectors.toList()).size(),is(2));
	}
	

	

	
	@Test
	public void takeWhileTest(){
		
		java.util.List<Integer> list = new ArrayList<>();
		while(list.size()==0){
			list = of(1,2,3,4,5,6).takeWhile(it -> it<4)
						.peek(it -> System.out.println(it)).collect(Collectors.toList());
	
		}
		assertThat(Arrays.asList(1,2,3,4,5,6),hasItem(list.get(0)));
		
		
		
		
	}

    @Test
    public void testScanLeftStringConcat() {
        assertThat(of("a", "b", "c").scanLeft("", String::concat).toList().length(),
        		is(4));
    }
    @Test
    public void testScanLeftSum() {
    	assertThat(of("a", "ab", "abc").map(str->str.length()).scanLeft(0, (u, t) -> u + t).toList().length(), 
    			is(asList(0, 1, 3, 6).size()));
    }
    @Test
    public void testScanRightStringConcatMonoid() {
        assertThat(of("a", "b", "c").scanRight(Monoid.of("", String::concat)).toList().length(),
            is(asList("", "c", "bc", "abc").size()));
    }
    @Test
    public void testScanRightStringConcat() {
        assertThat(of("a", "b", "c").scanRight("", String::concat).toList().length(),
            is(asList("", "c", "bc", "abc").size()));
    }
    @Test
    public void testScanRightSum() {
    	assertThat(of("a", "ab", "abc").map(str->str.length()).scanRight(0, (t, u) -> u + t).toList().length(),
            is(asList(0, 3, 5, 6).size()));

        
    }

    

    @Test
    public void testReverse() {

        assertThat( of(1, 2, 3).reverse().toList(), equalTo(asList(3, 2, 1)));
    }
    @Test
    public void testReverseList() {
    	
        assertThat( ReactiveStream.fromIterable(Arrays.asList(10,400,2,-1))
        				.reverse().toList(), equalTo(asList(-1, 2, 400,10)));
    }
    @Test
    public void testReverseListtake() {
    	
        assertThat( ReactiveStream.fromIterable(Arrays.asList(10,400,2,-1)).take(2)
        				.reverse().toList(), equalTo(asList(-1, 2)));
    }
    @Test
    public void testReverseRange() {
    	
        assertThat( ReactiveStream.range(0,10)
        				.reverse().toList(), equalTo(asList(9,8,7,6,5,4,3,2,1,0)));
    }

   
    @Test
    public void testCycle() {
    	
    	   assertEquals(asList(1, 1, 1, 1, 1,1),of(1).cycle().take(6).toList());
      
    }
    
    @Test
    public void testIterable() {
        java.util.List<Integer> list = of(1, 2, 3).toCollection(LinkedList::new);

        for (Integer i :of(1, 2, 3)) {
            assertThat(list,hasItem(i));
        }
    }
	
	
	    @Test(expected=ClassCastException.class)
	    public void testCastException() {
	    	of(1, "a", 2, "b", 3, null)
	    			.peek(it ->System.out.println(it))
	    			.cast(Integer.class)
	    				.peek(it ->System.out.println(it)).toList();
	    		
	    }
	   
	   

		
	    @Test
	    public void testGroupByEager() {
	        Map<Integer, Stream<Integer>> map1 =of(1, 2, 3, 4).<Integer>groupBy(i -> i % 2);
	       
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
	        Supplier<ReactiveStream<Integer>> s = () -> of(1, 2, 3, 4, 5);

	        assertTrue(s.get().dropWhile(i -> false).toList().containsAll(asList(1, 2, 3, 4, 5)));
	      
	        assertEquals(asList(), s.get().dropWhile(i -> true).toList());
	    }

	    @Test
	    public void testSkipUntil() {
	        Supplier<ReactiveStream<Integer>> s = () -> of(1, 2, 3, 4, 5);

	        assertEquals(asList(), s.get().dropUntil(i -> false).toList());
	        assertTrue(s.get().dropUntil(i -> true).toList().containsAll(asList(1, 2, 3, 4, 5)));
		  }

	    @Test
	    public void testSkipUntilWithNulls() {
	        Supplier<ReactiveStream<Integer>> s = () -> of(1, 2, null, 3, 4, 5);
	       
	        assertTrue(s.get().dropUntil(i -> true).toList().containsAll(asList(1, 2, null, 3, 4, 5)));
	    }

	    @Test
	    public void testtakeWhile() {
	        Supplier<ReactiveStream<Integer>> s = () -> of(1, 2, 3, 4, 5);

	        assertEquals(asList(), s.get().takeWhile(i -> false).toList());
	        assertTrue( s.get().takeWhile(i -> i < 3).toList().length()!=5);       
	        assertTrue(s.get().takeWhile(i -> true).toList().containsAll(asList(1, 2, 3, 4, 5)));
	    }

	    @Test
	    public void testtakeUntil() {
	        

	        assertTrue(of(1, 2, 3, 4, 5).takeUntil(i -> false).toList().containsAll(asList(1, 2, 3, 4, 5)));
	        assertFalse(of(1, 2, 3, 4, 5).takeUntil(i -> i % 3 == 0).toList().length()==5);
	        
	        assertEquals(asList(), of(1, 2, 3, 4, 5).takeUntil(i -> true).toList());
	    }

	    @Test
	    public void testtakeUntilWithNulls() {
	       
	    	System.out.println(of(1, 2, null, 3, 4, 5).takeUntil(i -> false).toList());
	        assertTrue(of(1, 2, null, 3, 4, 5).takeUntil(i -> false).toList().containsAll(asList(1, 2, null, 3, 4, 5)));
	    }

	    

	    @Test
	    public void testMinByMaxBy() {
	        Supplier<ReactiveStream<Integer>> s = () -> of(1, 2, 3, 4, 5, 6);

	        assertEquals(1, (int) s.get().maxBy(t -> Math.abs(t - 5)).get());
	        assertEquals(5, (int) s.get().minBy(t -> Math.abs(t - 5)).get());

	        assertEquals(6, (int) s.get().maxBy(t -> "" + t).get());
	        assertEquals(1, (int) s.get().minBy(t -> "" + t).get());
	    }

	   
	   

	    @Test
	    public void testFoldLeft() {
	    	for(int i=0;i<100;i++){
		        Supplier<ReactiveStream<String>> s = () -> of("a", "b", "c");
	
		        assertTrue(s.get().foldLeft("", String::concat).contains("a"));
		        assertTrue(s.get().foldLeft("", String::concat).contains("b"));
		        assertTrue(s.get().foldLeft("", String::concat).contains("c"));
		       
		        assertEquals(3, (int) s.get().map(str->str.length()).foldLeft(0, (u, t) -> u + t));
	
		        
		        assertEquals(3, (int) s.get().map(str->str.length()).foldRight(0, (t, u) -> u + t));
	    	}
	    }
	    
	    @Test
	    public void testFoldRight(){
	    	 	Supplier<ReactiveStream<String>> s = () -> of("a", "b", "c");

		        assertTrue(s.get().foldRight("", String::concat).contains("a"));
		        assertTrue(s.get().foldRight("", String::concat).contains("b"));
		        assertTrue(s.get().foldRight("", String::concat).contains("c"));
		        assertEquals(3, (int) s.get().map(str->str.length())
		        					.foldRight(0, (t, u) -> u + t));
	    }
	    
	   
	    //tests converted from lazy-seq suite
	    @Test
		public void flattenEmpty() throws Exception {
				assertTrue(ReactiveStream.<Integer>of().flatten().toList().isEmpty());
		}

		@Test
		public void flatten() throws Exception {
			assertThat(ReactiveStream.of(Arrays.asList(1,2)).flatten().toList().length(),equalTo(asList(1,  2).size()));		
		}

		

		@Test
		public void flattenEmptyStream() throws Exception {
			
			assertThat(ReactiveStream.<Integer>of(1,2,3,4,5,5,6,8,9,10).flatten().take(10).collect(Collectors.toList()).size(),
											equalTo(asList(2, 3, 4, 5, 6, 7, 0, 0, 0, 0).size()));
		}
		
		
	
}