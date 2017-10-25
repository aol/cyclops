package com.aol.cyclops2.streams.streamable;

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

import cyclops.collections.mutable.ListX;
import cyclops.control.Option;
import cyclops.data.tuple.Tuple2;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import cyclops.reactive.Streamable;
public class StreamableSeqOpertationsTest {

	<U> Streamable<U> of(U... array){
			  return Streamable.of(array);
	}
	
		
	Streamable<Integer> empty;
	Streamable<Integer> nonEmpty;

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
		List<String> result = 	of(1,2,3).prependAll(100,200,300)
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
		List<String> result = 	of(1,2,3).appendStreamable(of(100,200,300))
				.map(it ->it+"!!").collect(Collectors.toList());

			assertThat(result,equalTo(Arrays.asList("1!!","2!!","3!!","100!!","200!!","300!!")));
		}
		
		@Test
		public void prependStreams(){
		List<String> result = 	of(1,2,3).prependStreamable(of(100,200,300))
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
		List<String> result = 	of(1,2,3).insertStreamableAt(1,of(100,200,300))
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
			
			//this is not 100% reliable for EagerSequenceM use zipFutures instead
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
			
			
			final Streamable<Integer> zipped = empty.zip(this.<Integer>of(), (a, b) -> a + b);
			assertTrue(zipped.collect(Collectors.toList()).isEmpty());
		}

		@Test
		public void shouldReturnEmptySeqWhenZipEmptyWithNonEmpty() throws Exception {
			
			
			
			final Streamable<Integer> zipped = empty.zip(nonEmpty, (a, b) -> a + b);
			assertTrue(zipped.collect(Collectors.toList()).isEmpty());
		}

		@Test
		public void shouldReturnEmptySeqWhenZipNonEmptyWithEmpty() throws Exception {
			
			
			final Streamable<Integer> zipped = nonEmpty.zip(empty, (a, b) -> a + b);

			
			assertTrue(zipped.collect(Collectors.toList()).isEmpty());
		}

		@Test
		public void shouldZipTwoFiniteSequencesOfSameSize() throws Exception {
			
			final Streamable<String> first = of("A", "B", "C");
			final Streamable<Integer> second = of(1, 2, 3);

			
			final Streamable<String> zipped = first.zip(second, (a, b) -> a + b);

			
			assertThat(zipped.collect(Collectors.toList()),equalTo(asList("A1", "B2", "C3")));
		}

		

		@Test
		public void shouldTrimSecondFixedSeqIfLonger() throws Exception {
			final Streamable<String> first = of("A", "B", "C");
			final Streamable<Integer> second = of(1, 2, 3, 4);

			
			final Streamable<String> zipped = first.zip(second, (a, b) -> a + b);

			assertThat(zipped.collect(Collectors.toList()),equalTo(asList("A1", "B2", "C3")));
		}

		@Test
		public void shouldTrimFirstFixedSeqIfLonger() throws Exception {
			final Streamable<String> first = of("A", "B", "C","D");
			final Streamable<Integer> second = of(1, 2, 3);
			final Streamable<String> zipped = first.zip(second, (a, b) -> a + b);

			
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
	    public void testReverse() {
	        assertThat( of(1, 2, 3).reverse().toList(), is(asList(3, 2, 1)));
	    }

	    @Test
	    public void testShuffle() {
	        Supplier<Streamable<Integer>> s = () ->of(1, 2, 3);

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
	        List<Integer> list = of(1, 2, 3).to().collection(LinkedList::new);

	        for (Integer i :of(1, 2, 3)) {
	            assertThat(list,hasItem(i));
	        }
	    }
		
		@Test
		public void testDuplicate(){
			 Tuple2<Streamable<Integer>, Streamable<Integer>> copies =of(1,2,3,4,5,6).duplicate();
			 assertTrue(copies._1().anyMatch(i->i==2));
			 assertTrue(copies._2().anyMatch(i->i==2));
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
		    	assertEquals(asList(new Tuple2("a", 0L), new Tuple2("b", 1L)), of("a", "b").zipWithIndex().toList());
		        assertEquals(asList(new Tuple2("a", 0L), new Tuple2("b", 1L), new Tuple2("c", 2L)), of("a", "b", "c").zipWithIndex().toList());
		    }

		   
		    @Test
		    public void testSkipWhile() {
		    	 Supplier<Streamable<Integer>> s = () -> Streamable.of(1, 2, 3, 4, 5);

		         assertEquals(asList(1, 2, 3, 4, 5), s.get().skipWhile(i -> false).toList());
		         assertEquals(asList(3, 4, 5), s.get().skipWhile(i -> i % 3 != 0).toList());
		         assertEquals(asList(3, 4, 5), s.get().skipWhile(i -> i < 3).toList());
		         assertEquals(asList(4, 5), s.get().skipWhile(i -> i < 4).toList());
		         assertEquals(asList(), s.get().skipWhile(i -> true).toList());
		    }

		    @Test
		    public void testSkipUntil() {
		    	Supplier<Streamable<Integer>> s = () -> Streamable.of(1, 2, 3, 4, 5);

		        assertEquals(asList(), s.get().skipUntil(i -> false).toList());
		        assertEquals(asList(3, 4, 5), s.get().skipUntil(i -> i % 3 == 0).toList());
		        assertEquals(asList(3, 4, 5), s.get().skipUntil(i -> i == 3).toList());
		        assertEquals(asList(4, 5), s.get().skipUntil(i -> i == 4).toList());
		        assertEquals(asList(1, 2, 3, 4, 5), s.get().skipUntil(i -> true).toList());
			  }

		    @Test
		    public void testSkipUntilWithNulls() {
		    	 Supplier<Streamable<Integer>> s = () -> Streamable.of(1, 2, null, 3, 4, 5);

		         assertEquals(asList(1, 2, null, 3, 4, 5), s.get().skipUntil(i -> true).toList());
		    }

		    @Test
		    public void testLimitWhile() {
		    	 Supplier<Streamable<Integer>> s = () -> Streamable.of(1, 2, 3, 4, 5);

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
		        Supplier<Streamable<Integer>> s = () -> of(1, 2, 3, 4, 5, 6);

		        assertEquals(asList(1, 3, 5), s.get().partition(i -> i % 2 != 0)._1().toList());
		        assertEquals(asList(2, 4, 6), s.get().partition(i -> i % 2 != 0)._2().toList());

		        assertEquals(asList(2, 4, 6), s.get().partition(i -> i % 2 == 0)._1().toList());
		        assertEquals(asList(1, 3, 5), s.get().partition(i -> i % 2 == 0)._2().toList());

		        assertEquals(asList(1, 2, 3), s.get().partition(i -> i <= 3)._1().toList());
		        assertEquals(asList(4, 5, 6), s.get().partition(i -> i <= 3)._2().toList());

		        assertEquals(asList(1, 2, 3, 4, 5, 6), s.get().partition(i -> true)._1().toList());
		        assertEquals(asList(), s.get().partition(i -> true)._2().toList());

		        assertEquals(asList(), s.get().partition(i -> false)._1().toList());
		        assertEquals(asList(1, 2, 3, 4, 5, 6), s.get().splitBy(i -> false)._2().toList());
		    }

		    @Test
		    public void testSplitAt() {
		    	for(int i=0;i<20;i++){
			        Supplier<Streamable<Integer>> s = () -> of(1, 2, 3, 4, 5, 6);
		
			   
			        assertEquals(asList(4, 5, 6), s.get().splitAt(3)._2().toList());
		
			  
		    	}
		    	for(int i=0;i<20;i++){
			        Supplier<Streamable<Integer>> s = () -> of(1, 2, 3, 4, 5, 6);
		
			     
			        assertEquals(asList(1, 2, 3), s.get().splitAt(3)._1().toList());
			       
		    	}
		    	for(int i=0;i<20;i++){
			        Supplier<Streamable<Integer>> s = () -> of(1, 2, 3, 4, 5, 6);
		
			   
			       assertEquals(asList(1, 2, 3, 4, 5, 6), s.get().splitAt(6)._1().toList());
			      	}
		    	for(int i=0;i<20;i++){
			        Supplier<Streamable<Integer>> s = () -> of(1, 2, 3, 4, 5, 6);
		
			   
			        assertEquals(asList(1, 2, 3, 4, 5, 6), s.get().splitAt(7)._1().toList());
			      	}
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
		        Supplier<Streamable<Integer>> s = () -> of(1, 2, 3, 4, 5, 6);

		        assertEquals(1, (int) s.get().maxBy(t -> Math.abs(t - 5)).orElse(-1));
		        assertEquals(5, (int) s.get().minBy(t -> Math.abs(t - 5)).orElse(-1));

		        assertEquals(6, (int) s.get().maxBy(t -> "" + t).orElse(-1));
		        assertEquals(1, (int) s.get().minBy(t -> "" + t).orElse(-1));
		    }

		    @Test
		    public void testUnzip() {
		        Supplier<Streamable<Tuple2<Integer, String>>> s = () -> of(new Tuple2(1, "a"), new Tuple2(2, "b"), new Tuple2(3, "c"));

		        Tuple2<Streamable<Integer>, Streamable<String>> u1 = Streamable.unzip(s.get());
		        assertThat(u1._1().toList(),equalTo(asList(1, 2, 3)));
		        assertThat(u1._2().toList(),equalTo(asList("a", "b", "c")));

		        
		    }
		   

		    @Test
		    public void testFoldLeft() {
		        Supplier<Streamable<String>> s = () -> of("a", "b", "c");

		        assertTrue(s.get().foldLeft("", String::concat).contains("a"));
		        assertTrue(s.get().foldLeft("", String::concat).contains("b"));
		        assertTrue(s.get().foldLeft("", String::concat).contains("c"));
		       
		        assertEquals(3, (int) s.get().map(str->str.length()).foldLeft(0, (u, t) -> u + t));

		        
		        assertEquals(3, (int) s.get().map(str->str.length()).foldRight(0, (t, u) -> u + t));
		        assertEquals("-a-b-c", s.get().map(str->new StringBuilder(str)).foldLeft(new StringBuilder(), (u, t) -> u.append("-").append(t)).toString());
		    }
		    
		    @Test
		    public void testFoldRight(){
		    	 	Supplier<Streamable<String>> s = () -> of("a", "b", "c");

			        assertTrue(s.get().foldRight("", String::concat).equals("cba"));
			        assertTrue(s.get().foldRight("", String::concat).contains("b"));
			        assertTrue(s.get().foldRight("", String::concat).contains("c"));
			        assertEquals(3, (int) s.get().map(str->str.length()).foldRight(0, (t, u) -> u + t));
			        
		    }
		    
		   
		  //tests converted from lazy-seq suite
		    @Test
			public void flattenEmpty() throws Exception {
					assertTrue(this.<Integer>of().flatMap(i -> Streamable.fromStream(asList(i, -i).stream())).toList().isEmpty());
			}

			@Test
			public void flatten() throws Exception {
				assertThat(this.<Integer>of(1,2).flatMap(i -> Streamable.fromStream(asList(i, -i).stream())).toList(),equalTo(asList(1, -1, 2, -2)));		
			}

			

			@Test
			public void flattenEmptyStream() throws Exception {
				
				assertThat(this.<Integer>of(1,2,3,4,5,5,6,8,9,10).flatMapStream(this::flatMapFun).limit(10).collect(Collectors.toList()),
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
