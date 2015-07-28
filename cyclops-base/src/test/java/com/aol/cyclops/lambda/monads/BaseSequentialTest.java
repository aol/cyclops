package com.aol.cyclops.lambda.monads;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.aol.cyclops.streams.Pair;
public class BaseSequentialTest {

	<U> SequenceM<U> of(U... array){
			  return SequenceM.of(array);
	}
	
		
		SequenceM<Integer> empty;
		SequenceM<Integer> nonEmpty;

		@Before
		public void setup(){
			empty = of();
			nonEmpty = of(1);
		}
		
	Integer value2() {
		return 5;
	}
		
		
		@Test
		public void batchBySize(){
			System.out.println(of(1,2,3,4,5,6).grouped(3).collect(Collectors.toList()));
			assertThat(of(1,2,3,4,5,6).grouped(3).collect(Collectors.toList()).size(),is(2));
		}
		
		
		@Test
		public void prepend(){
		List<String> result = 	of(1,2,3).prepend(100,200,300)
				.map(it ->it+"!!").collect(Collectors.toList());

			assertThat(result,equalTo(Arrays.asList("100!!","200!!","300!!","1!!","2!!","3!!")));
		}	
		@Test
		public void append(){
		List<String> result = 	of(1,2,3).append(100,200,300)
				.map(it ->it+"!!").collect(Collectors.toList());

			assertThat(result,equalTo(Arrays.asList("1!!","2!!","3!!","100!!","200!!","300!!")));
		}
		@Test
		public void concatStreams(){
		List<String> result = 	of(1,2,3).appendStream(of(100,200,300))
				.map(it ->it+"!!").collect(Collectors.toList());

			assertThat(result,equalTo(Arrays.asList("1!!","2!!","3!!","100!!","200!!","300!!")));
		}
		@Test
		public void prependStreams(){
		List<String> result = 	of(1,2,3).prependStream(of(100,200,300))
				.map(it ->it+"!!").collect(Collectors.toList());

			assertThat(result,equalTo(Arrays.asList("100!!","200!!","300!!","1!!","2!!","3!!")));
		}
		@Test
		public void insertAt(){
		List<String> result = 	of(1,2,3).insertAt(1,100,200,300)
				.map(it ->it+"!!").collect(Collectors.toList());

			assertThat(result,equalTo(Arrays.asList("1!!","100!!","200!!","300!!","2!!","3!!")));
		}
		@Test
		public void insertAtStream(){
		List<String> result = 	of(1,2,3).insertStreamAt(1,of(100,200,300))
				.map(it ->it+"!!").collect(Collectors.toList());

			assertThat(result,equalTo(Arrays.asList("1!!","100!!","200!!","300!!","2!!","3!!")));
		}
		@Test
		public void deleteBetween(){
			List<String> result = 	of(1,2,3,4,5,6).deleteBetween(2,4)
				.map(it ->it+"!!").collect(Collectors.toList());

			assertThat(result,equalTo(Arrays.asList("1!!","2!!","5!!","6!!")));
		}
		
		@Test
		public void zip(){
			List<Pair<Integer,Integer>> list =
					of(1,2,3,4,5,6).zip(of(100,200,300,400))
													.peek(it -> System.out.println(it)).collect(Collectors.toList());
			
			List<Integer> right = list.stream().map(t -> t.v2).collect(Collectors.toList());
			assertThat(right,hasItem(100));
			assertThat(right,hasItem(200));
			assertThat(right,hasItem(300));
			assertThat(right,hasItem(400));
			
			List<Integer> left = list.stream().map(t -> t.v1).collect(Collectors.toList());
			assertThat(asList(1,2,3,4),equalTo(left));
			
			
		}
		
		@Test
		public void zip2of(){
			List<Pair<Integer,Integer>> list =of(1,2,3,4,5,6).zip(of(100,200,300,400)).peek(it -> System.out.println(it)).collect(Collectors.toList());
		
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
			
			//this is not 100% reliable for EagerSequenceM use zipFutures instead
				List<Pair<Integer,Integer>> list =  of(1,2,3,4,5,6).limit(6)
															.zip( of(100,200,300,400).limit(4))
															.collect(Collectors.toList());
				
				assertThat(list.get(0).v1,is(1));
				assertThat(list.get(0).v2,is(100));
				assertThat(list.get(1).v1,is(2));
				assertThat(list.get(1).v2,is(200));
				assertThat(list.get(2).v1,is(3));
				assertThat(list.get(2).v2,is(300));
				assertThat(list.get(3).v1,is(4));
				assertThat(list.get(3).v2,is(400));
			
			
			
		}

		@Test
		public void zipEmpty() throws Exception {
			
			
			final SequenceM<Integer> zipped = empty.zip(this.<Integer>of(), (a, b) -> a + b);
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

			
			assertThat(zipped.collect(Collectors.toList()),equalTo(asList("A1", "B2", "C3")));
		}

		

		@Test
		public void shouldTrimSecondFixedSeqIfLonger() throws Exception {
			final SequenceM<String> first = of("A", "B", "C");
			final SequenceM<Integer> second = of(1, 2, 3, 4);

			
			final SequenceM<String> zipped = first.zip(second, (a, b) -> a + b);

			assertThat(zipped.collect(Collectors.toList()),equalTo(asList("A1", "B2", "C3")));
		}

		@Test
		public void shouldTrimFirstFixedSeqIfLonger() throws Exception {
			final SequenceM<String> first = of("A", "B", "C","D");
			final SequenceM<Integer> second = of(1, 2, 3);
			final SequenceM<String> zipped = first.zip(second, (a, b) -> a + b);

			
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
	    	assertThat(of("a", "ab", "abc").map(str->str.length()).scanLeft(0, (u, t) -> u + t).toList(), 
	    			is(asList(0, 1, 3, 6)));
	    }

	    @Test
	    public void testScanRightStringConcat() {
	        assertThat(of("a", "b", "c").scanRight("", String::concat).toList(),
	            is(asList("", "c", "bc", "abc")));
	    }
	    @Test
	    public void testScanRightSum() {
	    	assertThat(of("a", "ab", "abc").map(str-> str.length()).scanRight(0, (t, u) -> u + t).toList(),
	            is(asList(0, 3, 5, 6)));

	        
	    }

	    

	    @Test
	    public void testReverse() {
	        assertThat( of(1, 2, 3).reverse().toList(), is(asList(3, 2, 1)));
	    }

	    @Test
	    public void testShuffle() {
	        Supplier<SequenceM<Integer>> s = () ->of(1, 2, 3);

	        assertEquals(3, s.get().shuffle().toList().size());
	        assertThat(s.get().shuffle().toList(), hasItems(1, 2, 3));

	        
	    }

	    @Test
	    public void testCycle() {
	        assertEquals(asList(1, 2, 1, 2, 1, 2),of(1, 2).cycle().limit(6).toList());
	        assertEquals(asList(1, 2, 3, 1, 2, 3), of(1, 2, 3).cycle().limit(6).toList());
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
		
		

		
		   

			
		    @Test
		    public void testGroupByEager() {
		        Map<Integer, List<Integer>> map1 =of(1, 2, 3, 4).groupBy(i -> i % 2);
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
		        List<Pair<Integer, String>> list = of(1, 2).zip(of("a", "b", "c", "d")).toList();

		        assertEquals(2, list.size());
		        assertTrue(asList(1,2).contains( list.get(0).v1));
		        assertTrue(""+list.get(1).v2,asList(1,2).contains( list.get(1).v1)); 
		        assertTrue(asList("a", "b", "c", "d").contains( list.get(0).v2));
		        assertTrue(asList("a", "b", "c", "d").contains( list.get(1).v2));
		       
		        
		    }

		    @Test
		    public void testZipWithIndex() {
		    	//assertEquals(asList(), of().zipWithIndex().toList());
		       // assertEquals(asList(tuple("a", 0L)), of("a").zip(of(0L)).toList());
		        //assertEquals(asList(tuple("a", 0L)), of("a").zipWithIndex().toList());
		    	assertEquals(asList(new Pair("a", 0L), new Pair("b", 1L)), of("a", "b").zipWithIndex().toList());
		        assertEquals(asList(new Pair("a", 0L), new Pair("b", 1L), new Pair("c", 2L)), of("a", "b", "c").zipWithIndex().toList());
		    }

		   
		    @Test
		    public void testSkipWhile() {
		    	 Supplier<SequenceM<Integer>> s = () -> SequenceM.of(1, 2, 3, 4, 5);

		         assertEquals(asList(1, 2, 3, 4, 5), s.get().skipWhile(i -> false).toList());
		         assertEquals(asList(3, 4, 5), s.get().skipWhile(i -> i % 3 != 0).toList());
		         assertEquals(asList(3, 4, 5), s.get().skipWhile(i -> i < 3).toList());
		         assertEquals(asList(4, 5), s.get().skipWhile(i -> i < 4).toList());
		         assertEquals(asList(), s.get().skipWhile(i -> true).toList());
		    }

		    @Test
		    public void testSkipUntil() {
		    	Supplier<SequenceM<Integer>> s = () -> SequenceM.of(1, 2, 3, 4, 5);

		        assertEquals(asList(), s.get().skipUntil(i -> false).toList());
		        assertEquals(asList(3, 4, 5), s.get().skipUntil(i -> i % 3 == 0).toList());
		        assertEquals(asList(3, 4, 5), s.get().skipUntil(i -> i == 3).toList());
		        assertEquals(asList(4, 5), s.get().skipUntil(i -> i == 4).toList());
		        assertEquals(asList(1, 2, 3, 4, 5), s.get().skipUntil(i -> true).toList());
			  }

		    @Test
		    public void testSkipUntilWithNulls() {
		    	 Supplier<SequenceM<Integer>> s = () -> SequenceM.of(1, 2, null, 3, 4, 5);

		         assertEquals(asList(1, 2, null, 3, 4, 5), s.get().skipUntil(i -> true).toList());
		    }

		    @Test
		    public void testLimitWhile() {
		    	 Supplier<SequenceM<Integer>> s = () -> SequenceM.of(1, 2, 3, 4, 5);

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
		        Supplier<SequenceM<Integer>> s = () -> of(1, 2, 3, 4, 5, 6);

		        assertEquals(asList(1, 3, 5), s.get().partition(i -> i % 2 != 0).v1.toList());
		        assertEquals(asList(2, 4, 6), s.get().partition(i -> i % 2 != 0).v2.toList());

		        assertEquals(asList(2, 4, 6), s.get().partition(i -> i % 2 == 0).v1.toList());
		        assertEquals(asList(1, 3, 5), s.get().partition(i -> i % 2 == 0).v2.toList());

		        assertEquals(asList(1, 2, 3), s.get().partition(i -> i <= 3).v1.toList());
		        assertEquals(asList(4, 5, 6), s.get().partition(i -> i <= 3).v2.toList());

		        assertEquals(asList(1, 2, 3, 4, 5, 6), s.get().partition(i -> true).v1.toList());
		        assertEquals(asList(), s.get().partition(i -> true).v2.toList());

		        assertEquals(asList(), s.get().partition(i -> false).v1.toList());
		        assertEquals(asList(1, 2, 3, 4, 5, 6), s.get().splitBy(i -> false).v2.toList());
		    }

		    @Test
		    public void testSplitAt() {
		    	for(int i=0;i<20;i++){
			        Supplier<SequenceM<Integer>> s = () -> of(1, 2, 3, 4, 5, 6);
		
			   
			        assertEquals(asList(4, 5, 6), s.get().splitAt(3).v2.toList());
		
			  
		    	}
		    	for(int i=0;i<20;i++){
			        Supplier<SequenceM<Integer>> s = () -> of(1, 2, 3, 4, 5, 6);
		
			     
			        assertEquals(asList(1, 2, 3), s.get().splitAt(3).v1.toList());
			       
		    	}
		    	for(int i=0;i<20;i++){
			        Supplier<SequenceM<Integer>> s = () -> of(1, 2, 3, 4, 5, 6);
		
			   
			       assertEquals(asList(1, 2, 3, 4, 5, 6), s.get().splitAt(6).v1.toList());
			      	}
		    	for(int i=0;i<20;i++){
			        Supplier<SequenceM<Integer>> s = () -> of(1, 2, 3, 4, 5, 6);
		
			   
			        assertEquals(asList(1, 2, 3, 4, 5, 6), s.get().splitAt(7).v1.toList());
			      	}
		    }

		    @Test
		    public void testSplitAtHead() {
		        assertEquals(Optional.empty(), of().splitAtHead().v1);
		        assertEquals(asList(), of().splitAtHead().v2.toList());

		        assertEquals(Optional.of(1), of(1).splitAtHead().v1);
		        assertEquals(asList(), of(1).splitAtHead().v2.toList());

		        assertEquals(Optional.of(1), of(1, 2).splitAtHead().v1);
		        assertEquals(asList(2), of(1, 2).splitAtHead().v2.toList());

		        assertEquals(Optional.of(1), of(1, 2, 3).splitAtHead().v1);
		        assertEquals(Optional.of(2), of(1, 2, 3).splitAtHead().v2.splitAtHead().v1);
		        assertEquals(Optional.of(3), of(1, 2, 3).splitAtHead().v2.splitAtHead().v2.splitAtHead().v1);
		        assertEquals(asList(2, 3), of(1, 2, 3).splitAtHead().v2.toList());
		        assertEquals(asList(3), of(1, 2, 3).splitAtHead().v2.splitAtHead().v2.toList());
		        assertEquals(asList(), of(1, 2, 3).splitAtHead().v2.splitAtHead().v2.splitAtHead().v2.toList());
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
		    public void testUnzip() {
		        Supplier<SequenceM<Pair<Integer, String>>> s = () -> of(new Pair(1, "a"), new Pair(2, "b"), new Pair(3, "c"));

		        Pair<SequenceM<Integer>, SequenceM<String>> u1 = SequenceM.unzip(s.get());
		        assertThat(u1.v1.toList(),equalTo(asList(1, 2, 3)));
		        assertThat(u1.v2.toList(),equalTo(asList("a", "b", "c")));

		        
		    }
		   

		    @Test
		    public void testFoldLeft() {
		        Supplier<SequenceM<String>> s = () -> of("a", "b", "c");

		        assertTrue(s.get().foldLeft("", String::concat).contains("a"));
		        assertTrue(s.get().foldLeft("", String::concat).contains("b"));
		        assertTrue(s.get().foldLeft("", String::concat).contains("c"));
		       
		        assertEquals(3, (int) s.get().map(str->str.length()).foldLeft(0, (u, t) -> u + t));

		        
		        assertEquals(3, (int) s.get().map(str->str.length()).foldRight(0, (t, u) -> u + t));
		        assertEquals("-a-b-c", s.get().map(str->new StringBuilder(str)).foldLeft(new StringBuilder(), (u, t) -> u.append("-").append(t)).toString());
		    }
		    
		    @Test
		    public void testFoldRight(){
		    	 	Supplier<SequenceM<String>> s = () -> of("a", "b", "c");

			        assertTrue(s.get().foldRight("", String::concat).equals("cba"));
			        assertTrue(s.get().foldRight("", String::concat).contains("b"));
			        assertTrue(s.get().foldRight("", String::concat).contains("c"));
			        assertEquals(3, (int) s.get().map(str->str.length()).foldRight(0, (t, u) -> u + t));
			        
		    }
		    
		   
		  //tests converted from lazy-seq suite
		    @Test
			public void flattenEmpty() throws Exception {
					assertTrue(this.<Integer>of().flatMap(i -> asList(i, -i).stream()).toList().isEmpty());
			}

			@Test
			public void flatten() throws Exception {
				assertThat(this.<Integer>of(1,2).flatMap(i -> asList(i, -i).stream()).toList(),equalTo(asList(1, -1, 2, -2)));		
			}

			

			@Test
			public void flattenEmptyStream() throws Exception {
				
				assertThat(this.<Integer>of(1,2,3,4,5,5,6,8,9,10).flatMap(this::flatMapFun).limit(10).collect(Collectors.toList()),
												equalTo(asList(2, 3, 4, 5, 6, 7, 0, 0, 0, 0)));
			}

			private  Stream<Integer> flatMapFun(int i) {
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

		    
		
		
		
	}
