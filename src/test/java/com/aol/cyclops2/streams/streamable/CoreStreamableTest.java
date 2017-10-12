package com.aol.cyclops2.streams.streamable;


import static cyclops.reactive.Streamable.of;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import cyclops.collectionx.mutable.ListX;
import cyclops.data.tuple.Tuple2;
import cyclops.data.tuple.Tuple3;
import cyclops.data.tuple.Tuple4;
import org.junit.Before;
import org.junit.Test;

import cyclops.function.Monoid;
import cyclops.reactive.ReactiveSeq;
import cyclops.reactive.Streamable;



//see BaseSequentialSeqTest for in order tests
public  class CoreStreamableTest {
	
	
	Streamable<Integer> empty;
	Streamable<Integer> nonEmpty;

	@Before
	public void setup(){
		empty = Streamable.of();
		nonEmpty = of(1);
	}
	@Test
	public void coflatMapTest(){
		Streamable<Streamable<Integer>> list = Streamable.of(1, 2, 3)
				                                        .coflatMap(s -> s);

		Streamable<Integer> stream2 = list.flatMap(s -> s).map(i -> i * 10);
		Streamable<Integer> stream3 = list.flatMap(s -> s).map(i -> i * 100);

		assertThat(stream2.toListX(),equalTo(ListX.of(10,20,30)));
		assertThat(stream3.toListX(),equalTo(ListX.of(100,200,300)));

	}

	@Test
    public void testFlatMap(){
	    assertThat(Streamable.of(1,2,3).flatMap(i->Streamable.of(i))
                .toList(),equalTo(ListX.of(1,2,3)));
    }
	@Test
    public void testContains() {
        assertThat(Streamable.of(1, 2, 3, 4, 5).contains(3), equalTo(true));
        assertThat(Streamable.of(1, 2, 3, 4, 5).contains(6), equalTo(false));
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
    public void testReverse() {

        assertThat( of(1, 2, 3).reverse().toList(), equalTo(asList(3, 2, 1)));
    }

   
    @Test
    public void testCycle() {
    	
    	   assertEquals(asList(1, 1, 1, 1, 1,1),of(1).cycle().limit(6).toList());
      
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
		 Tuple2<Streamable<Integer>,Streamable<Integer>> copies =of(1,2,3,4,5,6).duplicate();
		 assertTrue(copies._1().anyMatch(i->i==2));
		 assertTrue(copies._2().anyMatch(i->i==2));
	}
	@Test
	public void testTriplicate(){
		 Tuple3<Streamable<Integer>, Streamable<Integer>, Streamable<Integer>> copies =of(1,2,3,4,5,6).triplicate();
		 assertTrue(copies._1().anyMatch(i->i==2));
		 assertTrue(copies._2().anyMatch(i->i==2));
		 assertTrue(copies._3().anyMatch(i->i==2));
	}
	
	@Test
	public void testQuadriplicate(){
		 Tuple4<Streamable<Integer>, Streamable<Integer>, Streamable<Integer>,Streamable<Integer>> copies =of(1,2,3,4,5,6).quadruplicate();
		 assertTrue(copies._1().anyMatch(i->i==2));
		 assertTrue(copies._2().anyMatch(i->i==2));
		 assertTrue(copies._3().anyMatch(i->i==2));
		 assertTrue(copies._4().anyMatch(i->i==2));
	}

	@Test
	public void testDuplicateFilter(){
		 Tuple2<Streamable<Integer>, Streamable<Integer>> copies =of(1,2,3,4,5,6).duplicate();
		 assertTrue(copies._1().filter(i->i%2==0).toList().size()==3);
		 assertTrue(copies._2().filter(i->i%2==0).toList().size()==3);
	} 
	@Test
	public void testTriplicateFilter(){
		Tuple3<Streamable<Integer>, Streamable<Integer>, Streamable<Integer>> copies =of(1,2,3,4,5,6).triplicate();
		 assertTrue(copies._1().filter(i->i%2==0).toList().size()==3);
		 assertTrue(copies._2().filter(i->i%2==0).toList().size()==3);
		 assertTrue(copies._3().filter(i->i%2==0).toList().size()==3);
	} 
	@Test
	public void testQuadriplicateFilter(){
		 Tuple4<Streamable<Integer>, Streamable<Integer>, Streamable<Integer>,Streamable<Integer>> copies =of(1,2,3,4,5,6).quadruplicate();
		 assertTrue(copies._1().filter(i->i%2==0).toList().size()==3);
		 assertTrue(copies._2().filter(i->i%2==0).toList().size()==3);
		 assertTrue(copies._3().filter(i->i%2==0).toList().size()==3);
		 assertTrue(copies._4().filter(i->i%2==0).toList().size()==3);
	}
	@Test
	public void testDuplicateLimit(){
		 Tuple2<Streamable<Integer>, Streamable<Integer>> copies =of(1,2,3,4,5,6).duplicate();
		 assertTrue(copies._1().limit(3).toList().size()==3);
		 assertTrue(copies._2().limit(3).toList().size()==3);
	} 
	@Test
	public void testTriplicateLimit(){
		Tuple3<Streamable<Integer>, Streamable<Integer>, Streamable<Integer>> copies =of(1,2,3,4,5,6).triplicate();
		 assertTrue(copies._1().limit(3).toList().size()==3);
		 assertTrue(copies._2().limit(3).toList().size()==3);
		 assertTrue(copies._3().limit(3).toList().size()==3);
	} 
	@Test
	public void testQuadriplicateLimit(){
		 Tuple4<Streamable<Integer>, Streamable<Integer>, Streamable<Integer>,Streamable<Integer>> copies =of(1,2,3,4,5,6).quadruplicate();
		 assertTrue(copies._1().limit(3).toList().size()==3);
		 assertTrue(copies._2().limit(3).toList().size()==3);
		 assertTrue(copies._3().limit(3).toList().size()==3);
		 assertTrue(copies._4().limit(3).toList().size()==3);
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
	@Test
	public void skipUntil(){
		assertEquals(asList(3, 4, 5), Streamable.of(1, 2, 3, 4, 5).skipUntil(i -> i % 3 == 0).toList());
	}
    @Test
    public void zip2of(){

        List<Tuple2<Integer,Integer>> list =of(1,2,3,4,5,6)
                .zip(of(100,200,300,400).stream())
                .toListX();


        List<Integer> right = list.stream().map(t -> t._2()).collect(Collectors.toList());
        assertThat(right,hasItem(100));
        assertThat(right,hasItem(200));
        assertThat(right,hasItem(300));
        assertThat(right,hasItem(400));

        List<Integer> left = list.stream().map(t -> t._1()).collect(Collectors.toList());
        System.out.println(left);
        assertThat(Arrays.asList(1,2,3,4,5,6),hasItem(left.get(0)));

    }
	    @Test(expected=ClassCastException.class)
    public void cast(){
        of(1,2,3).cast(String.class).toList();
    }

	    @Test
    public void dropRight(){
        assertThat(of(1,2,3).dropRight(1).toList(),hasItems(1,2));
    }
    @Test
    public void skipLast1(){
        System.out.println("List " + Streamable.of(1,2,3,4).skipLast(1).toList());
      //  System.out.println(of(1,2,3,4).skipLast(1).toList());
      //  assertThat(of(1,2,3).skipLast(1).toList(),hasItems(1,2));
    }
    @Test
    public void testSkipLastForEach(){
        List<Integer> list = new ArrayList();
        ReactiveSeq.of(1,2,3,4,5).skipLast(2)
                .forEach(n->{list.add(n);});
        assertThat(list,equalTo(Arrays.asList(1,2,3)));
    }
    @Test
    public void testSkipLast(){
        assertThat(of(1,2,3,4,5)
                .skipLast(2)
                .toListX(),equalTo(Arrays.asList(1,2,3)));
    }
	   
	  
	    @Test
		public void testCycleTimes(){
            assertEquals(asList(1, 2, 1, 2, 1, 2),of(1, 2).cycle(3).toListX());
        }
	   
	    @Test
	    public void testSkipWhile() {
	        Streamable<Integer> s = of(1, 2, 3, 4, 5);

	        System.out.println(s.skipWhile(i -> false).toList());
	      //  assertTrue(s.skipWhile(i -> false).toList().containsAll(asList(1, 2, 3, 4, 5)));
	      
	        //assertEquals(asList(), s.skipWhile(i -> true).toList());
	    }

	    @Test
	    public void testSkipUntil() {
	        Supplier<Streamable<Integer>> s = () -> of(1, 2, 3, 4, 5);

	        assertEquals(asList(), s.get().skipUntil(i -> false).toList());
	        assertTrue(s.get().skipUntil(i -> true).toList().containsAll(asList(1, 2, 3, 4, 5)));
		  }

	    @Test
	    public void testSkipUntilWithNulls() {
	        Supplier<Streamable<Integer>> s = () -> of(1, 2, null, 3, 4, 5);
	       
	        assertTrue(s.get().skipUntil(i -> true).toList().containsAll(asList(1, 2, null, 3, 4, 5)));
	    }

	    @Test
	    public void testLimitWhile() {
	        Supplier<Streamable<Integer>> s = () -> of(1, 2, 3, 4, 5);

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
	       
	    	System.out.println(of(1, 2, null, 3, 4, 5).limitUntil(i -> false).toList());
	        assertTrue(of(1, 2, null, 3, 4, 5).limitUntil(i -> false).toList().containsAll(asList(1, 2, null, 3, 4, 5)));
	    }

	    

	    @Test
	    public void testMinByMaxBy() {
	        Supplier<Streamable<Integer>> s = () -> of(1, 2, 3, 4, 5, 6);

	        assertEquals(1, (int) s.get().maxBy(t -> Math.abs(t - 5)).get());
	        assertEquals(5, (int) s.get().minBy(t -> Math.abs(t - 5)).get());

	        assertEquals(6, (int) s.get().maxBy(t -> "" + t).get());
	        assertEquals(1, (int) s.get().minBy(t -> "" + t).get());
	    }

	   
	   

	    @Test
	    public void testFoldLeft() {
	    	for(int i=0;i<100;i++){
		        Supplier<Streamable<String>> s = () -> of("a", "b", "c");
	
		        assertTrue(s.get().reduce("", String::concat).contains("a"));
		        assertTrue(s.get().reduce("", String::concat).contains("b"));
		        assertTrue(s.get().reduce("", String::concat).contains("c"));
		       
		        assertEquals(3, (int) s.get().map(str->str.length()).foldLeft(0, (u, t) -> u + t));
	
		        
		        assertEquals(3, (int) s.get().map(str->str.length()).foldRight(0, (t, u) -> u + t));
	    	}
	    }
	    
	    @Test
	    public void testFoldRight(){
	    	 	Supplier<Streamable<String>> s = () -> of("a", "b", "c");

		        assertTrue(s.get().foldRight("", String::concat).contains("a"));
		        assertTrue(s.get().foldRight("", String::concat).contains("b"));
		        assertTrue(s.get().foldRight("", String::concat).contains("c"));
		        assertEquals(3, (int) s.get().map(str->str.length())
		        					.foldRight(0, (t, u) -> u + t));
	    }
	    
	   

	    @Test
		public void flattenOptional() throws Exception {
	    	
				assertTrue(ReactiveSeq.of(Optional.of(1)).to(ReactiveSeq::flattenO).toList().get(0).equals(new Integer(1)));
		}

		@Test
		public void flatten() throws Exception {
			assertThat(ReactiveSeq.of(Arrays.asList(1,2)).to(ReactiveSeq::flattenI).toList().size(),equalTo(asList(1,  2).size()));
		}

		

		@Test
		public void flattenEmptyStream() throws Exception {
			
			assertThat(ReactiveSeq.<Integer>of(1,2,3,4,5,5,6,8,9,10).limit(10).collect(Collectors.toList()).size(),
											equalTo(asList(2, 3, 4, 5, 6, 7, 0, 0, 0, 0).size()));
		}
		
		
	
}