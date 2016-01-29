package com.aol.cyclops.javaslang.reactivestreams.reactivestream.tests;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javaslang.collection.Map;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.aol.cyclops.javaslang.reactivestreams.ReactiveStream;


public class BaseSequentialTest {

	<U> ReactiveStream<U> of(U... array){
			  return ReactiveStream.of(array);
	}
	
		
		ReactiveStream<Integer> empty;
		ReactiveStream<Integer> nonEmpty;

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
			System.out.println(of(1,2,3,4,5,6).windowBySize(3).collect(Collectors.toList()));
			assertThat(of(1,2,3,4,5,6).windowBySize(3).collect(Collectors.toList()).size(),is(2));
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
		List<String> result = 	of(1,2,3).appendAll(of(100,200,300))
				.map(it ->it+"!!").collect(Collectors.toList());

			assertThat(result,equalTo(Arrays.asList("1!!","2!!","3!!","100!!","200!!","300!!")));
		}
		
		@Test
		public void prependStreams(){
		List<String> result = 	of(1,2,3).prependAll(of(100,200,300))
				.map(it ->it+"!!").collect(Collectors.toList());

			assertThat(result,equalTo(Arrays.asList("100!!","200!!","300!!","1!!","2!!","3!!")));
		}
		@Test
		public void insertAt(){
		List<String> result = 	of(1,2,3).insertAll(1,Arrays.asList(100,200,300))
				.map(it ->it+"!!").collect(Collectors.toList());

			assertThat(result,equalTo(Arrays.asList("1!!","100!!","200!!","300!!","2!!","3!!")));
		}
		@Test
		public void insertAtStream(){
		List<String> result = 	of(1,2,3).insertAll(1,of(100,200,300))
				.map(it ->it+"!!").collect(Collectors.toList());

			assertThat(result,equalTo(Arrays.asList("1!!","100!!","200!!","300!!","2!!","3!!")));
		}
		@Test
		public void deleteBetween(){
			List<String> result = 	of(1,2,3,4,5,6).removeBetween(2,4)
				.map(it ->it+"!!").collect(Collectors.toList());

			assertThat(result,equalTo(Arrays.asList("1!!","2!!","5!!","6!!")));
		}
		
		
		
		@Test
		public void limitWhileTest(){
			List<Integer> list = of(1,2,3,4,5,6).takeWhile(it -> it<4).peek(it -> System.out.println(it)).collect(Collectors.toList());
		
			assertThat(list,hasItem(1));
			assertThat(list,hasItem(2));
			assertThat(list,hasItem(3));
			
			
			
		}

	 

	    

	    @Test
	    public void testReverse() {
	        assertThat( of(1, 2, 3).reverse().toJavaList(), is(asList(3, 2, 1)));
	    }

	   

	    @Test
	    public void testCycle() {
	        assertEquals(asList(1, 2, 1, 2, 1, 2),of(1, 2).cycle().take(6).toJavaList());
	        assertEquals(asList(1, 2, 3, 1, 2, 3), of(1, 2, 3).cycle().take(6).toJavaList());
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
		        Map<Integer, javaslang.collection.Stream<Integer>> map1 =of(1, 2, 3, 4).groupBy(i -> i % 2);
		       
		        assertEquals(ReactiveStream.of(2, 4), map1.get(0).get());
		        assertEquals(ReactiveStream.of(1, 3), map1.get(1).get());
		        assertEquals(2, map1.size());

		     
		    }
		    

		    @Test
		    public void testJoin() {
		        assertEquals("123",of(1, 2, 3).mkString());
		        assertEquals("1, 2, 3", of(1, 2, 3).mkString(", "));
		        assertEquals("|1^2^3$", of(1, 2, 3).mkString("|", "^", "$"));
		    }

		    
		   
		  
		    @Test
		    public void testSkipWhile() {
		    	 Supplier<ReactiveStream<Integer>> s = () -> ReactiveStream.of(1, 2, 3, 4, 5);

		         assertEquals(asList(1, 2, 3, 4, 5), s.get().dropWhile(i -> false).toJavaList());
		         assertEquals(asList(3, 4, 5), s.get().dropWhile(i -> i % 3 != 0).toJavaList());
		         assertEquals(asList(3, 4, 5), s.get().dropWhile(i -> i < 3).toJavaList());
		         assertEquals(asList(4, 5), s.get().dropWhile(i -> i < 4).toJavaList());
		         assertEquals(asList(), s.get().dropWhile(i -> true).toJavaList());
		    }

		    @Test
		    public void testSkipUntil() {
		    	Supplier<ReactiveStream<Integer>> s = () -> ReactiveStream.of(1, 2, 3, 4, 5);

		        assertEquals(asList(), s.get().dropUntil(i -> false).toJavaList());
		        assertEquals(asList(3, 4, 5), s.get().dropUntil(i -> i % 3 == 0).toJavaList());
		        assertEquals(asList(3, 4, 5), s.get().dropUntil(i -> i == 3).toJavaList());
		        assertEquals(asList(4, 5), s.get().dropUntil(i -> i == 4).toJavaList());
		        assertEquals(asList(1, 2, 3, 4, 5), s.get().dropUntil(i -> true).toJavaList());
			  }

		    @Test @Ignore //doesn't work
		    public void testSkipUntilWithNulls() {
		    	
		    	 Supplier<ReactiveStream<Integer>> s = () -> ReactiveStream.of(1, 2, null, 3, 4, 5);

		         assertEquals(asList(1, 2, null, 3, 4, 5), s.get().dropUntil(i -> true).toJavaList());
		    }

		    @Test
		    public void testLimitWhile() {
		    	 Supplier<ReactiveStream<Integer>> s = () -> ReactiveStream.of(1, 2, 3, 4, 5);

		         assertEquals(asList(), s.get().takeWhile(i -> false).toJavaList());
		         assertEquals(asList(1, 2), s.get().takeWhile(i -> i % 3 != 0).toJavaList());
		         assertEquals(asList(1, 2), s.get().takeWhile(i -> i < 3).toJavaList());
		         assertEquals(asList(1, 2, 3), s.get().takeWhile(i -> i < 4).toJavaList());
		         assertEquals(asList(1, 2, 3, 4, 5), s.get().takeWhile(i -> true).toJavaList());
		    }

		    @Test
		    public void testLimitUntil() {
		    	 assertEquals(asList(1, 2, 3, 4, 5),of(1, 2, 3, 4, 5).takeUntil(i -> false).toJavaList());
		         assertEquals(asList(1, 2), of(1, 2, 3, 4, 5).takeUntil(i -> i % 3 == 0).toJavaList());
		         assertEquals(asList(1, 2), of(1, 2, 3, 4, 5).takeUntil(i -> i == 3).toJavaList());
		         assertEquals(asList(1, 2, 3), of(1, 2, 3, 4, 5).takeUntil(i -> i == 4).toJavaList());
		         assertEquals(asList(), of(1, 2, 3, 4, 5).takeUntil(i -> true).toJavaList());

		        
		        
		        assertEquals(asList(), of(1, 2, 3, 4, 5).takeUntil(i -> true).toJavaList());
		    }

		    @Test @Ignore
		    public void testLimitUntilWithNulls() {
		       

		        assertThat(of(1, 2, null, 3, 4, 5).takeUntil(i -> false).toList(),equalTo(asList(1, 2, null, 3, 4, 5)));
		    }

		    

		    @Test
		    public void testSplitAt() {
		    	for(int i=0;i<20;i++){
			        Supplier<ReactiveStream<Integer>> s = () -> of(1, 2, 3, 4, 5, 6);
		
			   
			        assertEquals(asList(4, 5, 6), s.get().splitAt(3)._2().toJavaList());
		
			  
		    	}
		    	for(int i=0;i<20;i++){
			        Supplier<ReactiveStream<Integer>> s = () -> of(1, 2, 3, 4, 5, 6);
		
			     
			        assertEquals(asList(1, 2, 3), s.get().splitAt(3)._1.toJavaList());
			       
		    	}
		    	for(int i=0;i<20;i++){
			        Supplier<ReactiveStream<Integer>> s = () -> of(1, 2, 3, 4, 5, 6);
		
			   
			       assertEquals(asList(1, 2, 3, 4, 5, 6), s.get().splitAt(6)._1.toJavaList());
			      	}
		    	for(int i=0;i<20;i++){
			        Supplier<ReactiveStream<Integer>> s = () -> of(1, 2, 3, 4, 5, 6);
		
			   
			        assertEquals(asList(1, 2, 3, 4, 5, 6), s.get().splitAt(7)._1.toJavaList());
			      	}
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
		        Supplier<ReactiveStream<String>> s = () -> of("a", "b", "c");

		        assertTrue(s.get().foldLeft("", String::concat).contains("a"));
		        assertTrue(s.get().foldLeft("", String::concat).contains("b"));
		        assertTrue(s.get().foldLeft("", String::concat).contains("c"));
		       
		        assertEquals(3, (int) s.get().map(str->str.length()).foldLeft(0, (u, t) -> u + t));

		        
		        assertEquals(3, (int) s.get().map(str->str.length()).foldRight(0, (t, u) -> u + t));
		        assertEquals("-a-b-c", s.get().map(str->new StringBuilder(str)).foldLeft(new StringBuilder(), (u, t) -> u.append("-").append(t)).toString());
		    }
		    
		    @Test
		    public void testFoldRight(){
		    	 	Supplier<ReactiveStream<String>> s = () -> of("a", "b", "c");
		    	 	
		    	 	
		    	 	
			        assertTrue(s.get().foldRight("", (a,b)->b+a).equals("cba"));
			        assertTrue(s.get().foldRight("", (a,b)->b+a).contains("b"));
			        assertTrue(s.get().foldRight("", (a,b)->b+a).contains("c"));
			        assertEquals(3, (int) s.get().map(str->str.length()).foldRight(0, (t, u) -> u + t));
			        
		    }
		    
		   
		  //tests converted from lazy-seq suite
		    @Test
			public void flattenEmpty() throws Exception {
					assertTrue(this.<Integer>of().flatMap(i -> ReactiveStream.of(i, -i)).toList().isEmpty());
			}

			@Test
			public void flatten() throws Exception {
				assertThat(this.<Integer>of(1,2).flatMap(i -> ReactiveStream.of(i, -i)).toJavaList(),equalTo(asList(1, -1, 2, -2)));		
			}

			

			@Test
			public void flattenEmptyStream() throws Exception {
				
				assertThat(this.<Integer>of(1,2,3,4,5,5,6,8,9,10).flatMap(this::flatMapFun).take(10).collect(Collectors.toList()),
												equalTo(asList(2, 3, 4, 5, 6, 7, 0, 0, 0, 0)));
			}

			private  ReactiveStream<Integer> flatMapFun(int i) {
				if (i <= 0) {
					return ReactiveStream.empty();
				}
				switch (i) {
					case 1:
						return ReactiveStream.fromIterable(asList(2));
					case 2:
						return ReactiveStream.fromIterable(asList(3, 4));
					case 3:
						return ReactiveStream.fromIterable(asList(5, 6, 7));
					default:
						return ReactiveStream.fromIterable(asList(0, 0));
				}
			}

		    
		
		
		
	}
