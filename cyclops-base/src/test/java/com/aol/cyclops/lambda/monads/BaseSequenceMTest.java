package com.aol.cyclops.lambda.monads;

import static com.aol.cyclops.lambda.monads.SequenceM.of;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
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

import org.junit.Before;
import org.junit.Test;

import com.aol.cyclops.streams.Pair;
import com.aol.cyclops.streams.Quadruple;
import com.aol.cyclops.streams.Triple;


//see BaseSequentialSeqTest for in order tests
public  class BaseSequenceMTest {
	
	
	SequenceM<Integer> empty;
	SequenceM<Integer> nonEmpty;

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
	public void zip(){
		List<Pair<Integer,Integer>> list =
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
	public void zip3(){
		List<Triple<Integer,Integer,Character>> list =
				of(1,2,3,4,5,6).zip3(of(100,200,300,400),of('a','b','c'))
												.peek(it -> System.out.println(it))
												
												.collect(Collectors.toList());
		
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
		List<Quadruple<Integer,Integer,Character,String>> list =
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
		
		List<Pair<Integer,Integer>> list =of(1,2,3,4,5,6)
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
		
		List<Pair<Integer,Integer>> list =  of(1,2,3,4,5,6)
													.zip( of(100,200,300,400))
													.collect(Collectors.toList());
		
		assertThat(asList(1,2,3,4,5,6),hasItem(list.get(0).v1));
		assertThat(asList(100,200,300,400),hasItem(list.get(0).v2));
		
		
		
	}

	@Test
	public void zipEmpty() throws Exception {
		
		
		final SequenceM<Integer> zipped = empty.zip(SequenceM.<Integer>of(), (a, b) -> a + b);
		assertTrue(zipped.collect(Collectors.toList()).isEmpty());
	}

	@Test
	public void shouldReturnEmptySeqWhenZipEmptyWithNonEmpty() throws Exception {
		
		
		
		final SequenceM<Integer> zipped = empty.zip(nonEmpty, (a, b) -> a + b);
		assertTrue(zipped.collect(Collectors.toList()).isEmpty());
	}

	@Test
	public void shouldReturnEmptySeqWhenZipNonEmptyWithEmpty() throws Exception {
		
		
		final SequenceM<Integer> zipped = nonEmpty.zip(empty, (a, b) -> a + b);

		
		assertTrue(zipped.collect(Collectors.toList()).isEmpty());
	}

	@Test
	public void shouldZipTwoFiniteSequencesOfSameSize() throws Exception {
		
		final SequenceM<String> first = of("A", "B", "C");
		final SequenceM<Integer> second = of(1, 2, 3);

		
		final SequenceM<String> zipped = first.zip(second, (a, b) -> a + b);

		
		assertThat(zipped.collect(Collectors.toList()).size(),is(3));
	}

	

	@Test
	public void shouldTrimSecondFixedSeqIfLonger() throws Exception {
		final SequenceM<String> first = of("A", "B", "C");
		final SequenceM<Integer> second = of(1, 2, 3, 4);

		
		final SequenceM<String> zipped = first.zip(second, (a, b) -> a + b);

		assertThat(zipped.collect(Collectors.toList()).size(),is(3));
	}

	@Test
	public void shouldTrimFirstFixedSeqIfLonger() throws Exception {
		final SequenceM<String> first = of("A", "B", "C","D");
		final SequenceM<Integer> second = of(1, 2, 3);
		final SequenceM<String> zipped = first.zip(second, (a, b) -> a + b);

		
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
    	assertThat(of("a", "ab", "abc").map(str->str.length()).scanLeft(0, (u, t) -> u + t).toList().size(), 
    			is(asList(0, 1, 3, 6).size()));
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
        assertThat( of(1, 2, 3).reverse().toList().size(), is(asList(3, 2, 1).size()));
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
		 Pair<SequenceM<Integer>, SequenceM<Integer>> copies =of(1,2,3,4,5,6).duplicate();
		 assertTrue(copies.v1.anyMatch(i->i==2));
		 assertTrue(copies.v2.anyMatch(i->i==2));
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
	    public void testZipDifferingLength() {
	        List<Pair<Integer, String>> list = of(1, 2).zip(of("a", "b", "c", "d")).toList();

	        assertEquals(2, list.size());
	        assertTrue(asList(1,2).contains( list.get(0).v1));
	        assertTrue(""+list.get(1).v2,asList(1,2).contains( list.get(1).v1)); 
	        assertTrue(asList("a", "b", "c", "d").contains( list.get(0).v2));
	        assertTrue(asList("a", "b", "c", "d").contains( list.get(1).v2));
	       
	        
	    }

	    @Test
	    public void testZipWithIndex() {
	        assertEquals(asList(),of().zipWithIndex().toList());
	     
	       
	      assertThat( of("a").zipWithIndex().map(t->t.v2).findFirst().get(),is(0l));
	      assertEquals(asList(new Pair("a", 0L)), of("a").zipWithIndex().toList());
	     
	    }

	   
	    @Test
	    public void testSkipWhile() {
	        Supplier<SequenceM<Integer>> s = () -> of(1, 2, 3, 4, 5);

	        assertTrue(s.get().skipWhile(i -> false).toList().containsAll(asList(1, 2, 3, 4, 5)));
	      
	        assertEquals(asList(), s.get().skipWhile(i -> true).toList());
	    }

	    @Test
	    public void testSkipUntil() {
	        Supplier<SequenceM<Integer>> s = () -> of(1, 2, 3, 4, 5);

	        assertEquals(asList(), s.get().skipUntil(i -> false).toList());
	        assertTrue(s.get().skipUntil(i -> true).toList().containsAll(asList(1, 2, 3, 4, 5)));
		  }

	    @Test
	    public void testSkipUntilWithNulls() {
	        Supplier<SequenceM<Integer>> s = () -> of(1, 2, null, 3, 4, 5);
	       
	        assertTrue(s.get().skipUntil(i -> true).toList().containsAll(asList(1, 2, null, 3, 4, 5)));
	    }

	    @Test
	    public void testLimitWhile() {
	        Supplier<SequenceM<Integer>> s = () -> of(1, 2, 3, 4, 5);

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
	        Supplier<SequenceM<Integer>> s = () -> of(1, 2, 3, 4, 5, 6);

	        assertEquals(1, (int) s.get().maxBy(t -> Math.abs(t - 5)).get());
	        assertEquals(5, (int) s.get().minBy(t -> Math.abs(t - 5)).get());

	        assertEquals(6, (int) s.get().maxBy(t -> "" + t).get());
	        assertEquals(1, (int) s.get().minBy(t -> "" + t).get());
	    }

	   
	   

	    @Test
	    public void testFoldLeft() {
	    	for(int i=0;i<100;i++){
		        Supplier<SequenceM<String>> s = () -> of("a", "b", "c");
	
		        assertTrue(s.get().reduce("", String::concat).contains("a"));
		        assertTrue(s.get().reduce("", String::concat).contains("b"));
		        assertTrue(s.get().reduce("", String::concat).contains("c"));
		       
		        assertEquals(3, (int) s.get().map(str->str.length()).foldLeft(0, (u, t) -> u + t));
	
		        
		        assertEquals(3, (int) s.get().map(str->str.length()).foldRight(0, (t, u) -> u + t));
	    	}
	    }
	    
	    @Test
	    public void testFoldRight(){
	    	 	Supplier<SequenceM<String>> s = () -> of("a", "b", "c");

		        assertTrue(s.get().foldRight("", String::concat).contains("a"));
		        assertTrue(s.get().foldRight("", String::concat).contains("b"));
		        assertTrue(s.get().foldRight("", String::concat).contains("c"));
		        assertEquals(3, (int) s.get().map(str->str.length())
		        					.foldRight(0, (t, u) -> u + t));
	    }
	    
	   
	    //tests converted from lazy-seq suite
	    @Test
		public void flattenEmpty() throws Exception {
				assertTrue(SequenceM.<Integer>of().flatten().toList().isEmpty());
		}

		@Test
		public void flatten() throws Exception {
			assertThat(SequenceM.of(Arrays.asList(1,2)).flatten().toList().size(),equalTo(asList(1,  2).size()));		
		}

		 @Test
		public void testSplitBy() {
		        Supplier<SequenceM<Integer>> s = () -> of(1, 2, 3, 4, 5, 6);
		        

		        assertEquals(6, s.get().splitBy(i -> i % 2 != 0).v1.toList().size() + s.get().splitBy(i -> i % 2 != 0).v2.toList().size());
		        
		        assertTrue(s.get().splitBy(i -> true).v1.toList().containsAll(asList(1, 2, 3, 4, 5, 6)));
		        assertEquals(asList(), s.get().splitBy(i -> true).v2.toList());

		        assertEquals(asList(), s.get().splitBy(i -> false).v1.toList());
		        assertTrue(s.get().splitBy(i -> false).v2.toList().containsAll(asList(1, 2, 3, 4, 5, 6)));
		   }

		@Test
		public void flattenEmptyStream() throws Exception {
			
			assertThat(SequenceM.<Integer>of(1,2,3,4,5,5,6,8,9,10).flatten().limit(10).collect(Collectors.toList()).size(),
											equalTo(asList(2, 3, 4, 5, 6, 7, 0, 0, 0, 0).size()));
		}
		@Test
		public void testSplitAt() {
			for (int i = 0; i < 1000; i++) {
				Supplier<SequenceM<Integer>> s = () -> of(1, 2, 3, 4, 5, 6);

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
		    	
		    	 assertEquals(asList(), SequenceM.of(1).splitAtHead().v2.toList());
		    	 
		    	assertEquals(Optional.empty(), of().splitAtHead().v1);
		        assertEquals(asList(), SequenceM.of().splitAtHead().v2.toList());

		        assertEquals(Optional.of(1), SequenceM.of(1).splitAtHead().v1);
		       

		        assertEquals(Optional.of(1), SequenceM.of(1, 2).splitAtHead().v1);
		        assertEquals(asList(2), SequenceM.of(1, 2).splitAtHead().v2.toList());

		        assertEquals(Optional.of(1), SequenceM.of(1, 2, 3).splitAtHead().v1);
		        assertEquals(Optional.of(2), SequenceM.of(1, 2, 3).splitAtHead().v2.splitAtHead().v1);
		        assertEquals(Optional.of(3), SequenceM.of(1, 2, 3).splitAtHead().v2.splitAtHead().v2.splitAtHead().v1);
		        assertEquals(asList(2, 3), SequenceM.of(1, 2, 3).splitAtHead().v2.toList());
		        assertEquals(asList(3), SequenceM.of(1, 2, 3).splitAtHead().v2.splitAtHead().v2.toList());
		        assertEquals(asList(), SequenceM.of(1, 2, 3).splitAtHead().v2.splitAtHead().v2.splitAtHead().v2.toList());
		    }

		 @Test
		    public void testUnzip() {
		    	//order is not guaranteed as populated asynchronously
		        Supplier<SequenceM<Pair<Integer, String>>> s = () -> of(new Pair(1, "a"), new Pair(2, "b"), new Pair(3, "c"));

		       
		        Pair<SequenceM<Integer>, SequenceM<String>> u1 = SequenceM.unzip(s.get());
		      
		      
		        assertTrue(u1.v1.toList().containsAll(Arrays.asList(1, 2, 3)));
		       
		       
		       
		        assertTrue(u1.v2.toList().containsAll(asList("a", "b", "c")));

		       
		        
		    }

	@Test
	public void testUnzip3() {
		// order is not guaranteed as populated asynchronously
		Supplier<SequenceM<Triple<Integer, String, Long>>> s = () -> of(
				new Triple(1, "a", 2l), new Triple(2, "b", 3l), new Triple(3,
						"c", 4l));

		Triple<SequenceM<Integer>, SequenceM<String>, SequenceM<Long>> u1 = SequenceM
				.unzip3(s.get());

		assertTrue(u1.v1.toList().containsAll(Arrays.asList(1, 2, 3)));

		assertTrue(u1.v2.toList().containsAll(asList("a", "b", "c")));
		assertTrue(u1.v3.toList().containsAll(asList(2l, 3l, 4l)));

	}
	@Test
	public void testUnzip4() {
		// order is not guaranteed as populated asynchronously
		Supplier<SequenceM<Quadruple<Integer, String, Long,Character>>> s = () -> of(
				new Quadruple(1, "a", 2l,'z'), new Quadruple(2, "b", 3l,'y'), new Quadruple(3,
						"c", 4l,'x'));

		Quadruple<SequenceM<Integer>, SequenceM<String>, SequenceM<Long>,SequenceM<Character>> u1 = SequenceM
				.unzip4(s.get());

		assertTrue(u1.v1.toList().containsAll(Arrays.asList(1, 2, 3)));

		assertTrue(u1.v2.toList().containsAll(asList("a", "b", "c")));
		
		assertTrue(u1.v3.toList().containsAll(asList(2l, 3l, 4l)));
		assertTrue(u1.v4.toList().containsAll(asList('z', 'y', 'x')));

	}
		   
	
	
}