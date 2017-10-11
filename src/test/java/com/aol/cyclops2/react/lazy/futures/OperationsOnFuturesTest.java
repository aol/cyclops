package com.aol.cyclops2.react.lazy.futures;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static cyclops.collections.tuple.Tuple.tuple;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import cyclops.async.LazyReact;
import cyclops.stream.FutureStream;
import cyclops.collections.tuple.Tuple;
import cyclops.collections.tuple.Tuple2;
import org.junit.Before;
import org.junit.Test;

public class OperationsOnFuturesTest {

	
	protected <U> FutureStream<U> of(U... array) {

		return LazyReact.sequentialCurrentBuilder()
						.of(array);
	}
	
	protected <U> FutureStream<U> ofThread(U... array) {
		return LazyReact.sequentialCommonBuilder().of(array);
	}


	protected <U> FutureStream<U> react(Supplier<U>... array) {
		return LazyReact.sequentialCommonBuilder().react(Arrays.asList(array));
	}
		
		FutureStream<Integer> empty;
		FutureStream<Integer> nonEmpty;

		@Before
		public void setup(){
			empty = of();
			nonEmpty = of(1);
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
		public void testIntersperse() {
			
			assertThat(LazyReact.sequentialBuilder().of(1,2,3)
							.actOnFutures()
							.intersperse(0)
							.toList(),
							equalTo(Arrays.asList(1,0,2,0,3)));
		



		}
		@Test
		public void testIntersperseCf() {
			
			assertThat(LazyReact.sequentialBuilder().of(1,2,3)
							.actOnFutures()
							.intersperse(CompletableFuture.completedFuture(0))
							.toList(),
							equalTo(Arrays.asList(1,0,2,0,3)));
		



		}
		@Test
		public void prepend(){
		List<String> result = 	of(1,2,3).actOnFutures().prepend(100,200,300)
				.map(it ->it+"!!").collect(Collectors.toList());

			assertThat(result,equalTo(Arrays.asList("100!!","200!!","300!!","1!!","2!!","3!!")));
		}	
		@Test
		public void append(){
			List<String> result = 	of(1,2,3).actOnFutures()
											 .append(100,200,300)
											 .map(it ->it+"!!")
											 .collect(Collectors.toList());

			assertThat(result,equalTo(Arrays.asList("1!!","2!!","3!!","100!!","200!!","300!!")));
		}
		@Test
		public void appendFutures(){
			List<String> result = 	of(1,2,3).actOnFutures()
											 .appendFutures(CompletableFuture.completedFuture(100),CompletableFuture.completedFuture(200),CompletableFuture.completedFuture(300))
											 .map(it ->it+"!!")
											 .collect(Collectors.toList());

			assertThat(result,equalTo(Arrays.asList("1!!","2!!","3!!","100!!","200!!","300!!")));
		}
		@Test
		public void appendStreamFutures(){
			List<String> result = 	of(1,2,3).actOnFutures()
											 .appendStreamFutures(Stream.of(CompletableFuture.completedFuture(100),CompletableFuture.completedFuture(200),CompletableFuture.completedFuture(300)))
											 .map(it ->it+"!!")
											 .collect(Collectors.toList());

			assertThat(result,equalTo(Arrays.asList("1!!","2!!","3!!","100!!","200!!","300!!")));
		}
		@Test
		public void prependStreams(){
			List<String> result = 	of(1,2,3).actOnFutures()
											.prependStream(of(100,200,300))
											.map(it ->it+"!!")
											.collect(Collectors.toList());

			assertThat(result,equalTo(Arrays.asList("100!!","200!!","300!!","1!!","2!!","3!!")));
		}
		@Test
		public void prependStreamsFutures(){
			Stream<CompletableFuture<Integer>> streamOfFutures = Stream.of(CompletableFuture.completedFuture(100),CompletableFuture.completedFuture(200),CompletableFuture.completedFuture(300));
			List<String> result = 	of(1,2,3).actOnFutures()
											.prependStreamFutures(streamOfFutures)
											.map(it ->it+"!!")
											.collect(Collectors.toList());

			assertThat(result,equalTo(Arrays.asList("100!!","200!!","300!!","1!!","2!!","3!!")));
		}
		@Test
		public void prependFutures(){
			
			List<String> result = 	of(1,2,3).actOnFutures()
											.prependFutures(CompletableFuture.completedFuture(100),CompletableFuture.completedFuture(200),CompletableFuture.completedFuture(300))
											.map(it ->it+"!!")
											.collect(Collectors.toList());

			assertThat(result,equalTo(Arrays.asList("100!!","200!!","300!!","1!!","2!!","3!!")));
		}
		@Test
		public void concatStreams(){
			List<String> result = 	of(1,2,3).actOnFutures()
										.appendStream(of(100,200,300))
										.map(it ->it+"!!")
										.collect(Collectors.toList());

			assertThat(result,equalTo(Arrays.asList("1!!","2!!","3!!","100!!","200!!","300!!")));
		}
		@Test
		public void insertAt(){
		List<String> result = 	of(1,2,3).actOnFutures()
										 .insertAt(1,100,200,300)
										 .map(it ->it+"!!")
										 .collect(Collectors.toList());

			assertThat(result,equalTo(Arrays.asList("1!!","100!!","200!!","300!!","2!!","3!!")));
		}
		@Test
		public void thenCombine(){
			assertThat(of(1,2,3,4).actOnFutures().thenCombine((a,b)->a+b).toList(),equalTo(Arrays.asList(3,7)));
		}
		@Test
		public void insertAtStream(){
		List<String> result = 	of(1,2,3).actOnFutures()
										.insertStreamAt(1,of(100,200,300))
										.map(it ->it+"!!")
										.collect(Collectors.toList());

			assertThat(result,equalTo(Arrays.asList("1!!","100!!","200!!","300!!","2!!","3!!")));
		}
		@Test
		public void insertAtStreamFutures(){
		Stream<CompletableFuture<Integer>> streamOfFutures = Stream.of(CompletableFuture.completedFuture(100),CompletableFuture.completedFuture(200),CompletableFuture.completedFuture(300));

		List<String> result = 	of(1,2,3).actOnFutures()
										.insertStreamFuturesAt(1,streamOfFutures)
										.map(it ->it+"!!")
										.collect(Collectors.toList());

			assertThat(result,equalTo(Arrays.asList("1!!","100!!","200!!","300!!","2!!","3!!")));
		}
		@Test
		public void deleteBetween(){
			List<String> result = 	of(1,2,3,4,5,6).actOnFutures()
												   .deleteBetween(2,4)
												   .map(it ->it+"!!")
												   .collect(Collectors.toList());

			assertThat(result,equalTo(Arrays.asList("1!!","2!!","5!!","6!!")));
		}
		@Test
		public void testSliceFutures(){
			assertThat(of(1,2,3,4,5).actOnFutures().slice(3,4).block().size(),is(1));
		}
		@Test
		public void testShuffleRandom() {
			Random r = new Random();
			Supplier<FutureStream<Integer>> s = () -> of(1, 2, 3);

			assertEquals(3, s.get().actOnFutures().shuffle(r).toList().size());
			assertThat(s.get().actOnFutures().shuffle(r).toList(),
				hasItems(1, 2, 3));
		        
		}
		@Test
		public void testLimit(){
			assertThat(of(1,2,3,4,5).actOnFutures().limit(2).collect(Collectors.toList()).size(),is(2));
		}
		@Test
		public void testSkip(){
			assertThat(of(1,2,3,4,5).actOnFutures().skip(2).collect(Collectors.toList()).size(),is(3));
		}
		@Test
	    public void testCycle() {
			for(int i=0;i<1000;i++)
	    	   assertEquals(asList(1, 1, 1, 1, 1,1),of(1).actOnFutures().cycle().limit(6).toList());
	      
	    }
		@Test
	    public void testCycleTimes() {
	        assertEquals(asList(1, 2, 1, 2, 1, 2),of(1, 2).actOnFutures().cycle(3).toList());
	       
	    }
		
		@Test
		public void sliding(){
			List<List<Integer>> list = of(1,2,3,4,5,6).actOnFutures().sliding(2)
										.collect(Collectors.toList());
			
		
			assertThat(list.get(0),hasItems(1,2));
			assertThat(list.get(1),hasItems(2,3));
		}
		@Test
		public void slidingInc(){
			List<List<Integer>> list = of(1,2,3,4,5,6).actOnFutures().sliding(3,2)
										.collect(Collectors.toList());
			
			
			System.out.println(list.get(0));
			assertThat(list.get(0),hasItems(1,2,3));
			assertThat(list.get(1),hasItems(3,4,5));
		}
		
		@Test
		public void batchBySize(){
			
			assertThat(of(1,2,3,4,5,6).actOnFutures().grouped(3).collect(Collectors.toList()).size(),is(2));
			
			assertThat(of(1,2,3,4,5,6).actOnFutures().grouped(3).collect(Collectors.toList()).get(0),hasItems(1, 2, 3));
			assertThat(of(1,2,3,4,5,6).actOnFutures().grouped(3).collect(Collectors.toList()).get(1),hasItems(4, 5, 6));

		}
		
		
		
		
		
		
		@Test
		public void zipLfs(){
			List<Tuple2<Integer,Integer>> list =
					of(1,2,3,4,5,6).actOnFutures().zipLfs(of(100,200,300,400))
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
		public void zipLfsCombiner(){
			 BiFunction<CompletableFuture<Integer>,CompletableFuture<Integer>,CompletableFuture<Tuple2<Integer,Integer>>> combiner = 
					 			(cf1,cf2)->cf1.<Integer,Tuple2<Integer,Integer>>thenCombine(cf2, (v1,v2)->Tuple.tuple(v1,v2));
			List<Tuple2<Integer,Integer>> list =
					of(1,2,3,4,5,6).actOnFutures().zipLfs(of(100,200,300,400), combiner)
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
		public void zip(){
			List<Tuple2<Integer,Integer>> list =
					of(1,2,3,4,5,6).actOnFutures().zip(of(100,200,300,400))
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
			List<Tuple2<Integer,Integer>> list =of(1,2,3,4,5,6).actOnFutures().zip(of(100,200,300,400)).peek(it -> System.out.println(it)).collect(Collectors.toList());
		
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
			
			//this is not 100% reliable for EagerFutureStream use zipFutures instead
				List<Tuple2<Integer,Integer>> list =  of(1,2,3,4,5,6).limit(6)
															.actOnFutures().zip( of(100,200,300,400).limit(4))
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
			
			
			final FutureStream<Tuple2<Integer,Integer>> zipped = empty.actOnFutures().zip(this.<Integer>of());
			assertTrue(zipped.collect(Collectors.toList()).isEmpty());
		}

		@Test
		public void shouldReturnEmptySeqWhenZipEmptyWithNonEmpty() throws Exception {
			
			
			
			final FutureStream<Tuple2<Integer,Integer>> zipped = empty.actOnFutures().zip(nonEmpty);
			assertTrue(zipped.collect(Collectors.toList()).isEmpty());
		}

		@Test
		public void shouldReturnEmptySeqWhenZipNonEmptyWithEmpty() throws Exception {
			
			
			final FutureStream<Tuple2<Integer,Integer>> zipped = nonEmpty.actOnFutures().zip(empty);

			
			assertTrue(zipped.collect(Collectors.toList()).isEmpty());
		}

		@Test
		public void shouldZipTwoFiniteSequencesOfSameSize() throws Exception {
			
			final FutureStream<String> first = of("A", "B", "C");
			final FutureStream<Integer> second = of(1, 2, 3);

			
			FutureStream<Tuple2<String, Integer>> zipped = first.actOnFutures().zip(second);

			
			assertThat(zipped.collect(Collectors.toList()).size(),equalTo(3));
		}

		

		@Test
		public void shouldTrimSecondFixedSeqIfLonger() throws Exception {
			final FutureStream<String> first = of("A", "B", "C");
			final FutureStream<Integer> second = of(1, 2, 3, 4);

			
			FutureStream<Tuple2<String, Integer>> zipped = first.actOnFutures().zip(second);

			assertThat(zipped.collect(Collectors.toList()).size(),equalTo(3));
		}

		@Test
		public void shouldTrimFirstFixedSeqIfLonger() throws Exception {
			final FutureStream<String> first = of("A", "B", "C","D");
			final FutureStream<Integer> second = of(1, 2, 3);
			FutureStream<Tuple2<String, Integer>> zipped = first.actOnFutures().zip(second);

			assertThat(zipped.collect(Collectors.toList()).size(),equalTo(3));
		}

		
	    @Test
	    public void testReverse() {
	        assertThat( of(1, 2, 3).actOnFutures()
	        						.reverse()
	        						.toList(), is(asList(3, 2, 1)));
	    }

	    @Test
	    public void testShuffle() {
	        Supplier<FutureStream<Integer>> s = () ->of(1, 2, 3);

	        assertEquals(3, s.get().actOnFutures().shuffle().toList().size());
	        assertThat(s.get().actOnFutures().shuffle().toList(), hasItems(1, 2, 3));

	        
	    }

	    @Test
	    public void testToSet() {
	        Set<CompletableFuture<Integer>> list = of(1, 2, 3).actOnFutures()
	        							.toSet();

	        List<Integer> joined = list.stream().map(f->f.join()).collect(Collectors.toList());
	        for (Integer i :of(1, 2, 3)) {
	            assertThat(joined,hasItem(i));
	        }
	    } 
	    @Test
	    public void testReduce() {
	    	
	       CompletableFuture<Integer> sum = LazyReact.sequentialBuilder().of(1, 2, 3)
	    		   											.actOnFutures()
	    		   											.reduce((cf1,cf2)-> cf1.thenCombine(cf2, (a,b)->a+b)).get();

	        assertThat(sum.join(),equalTo(6));
	        
	        
	    }
	    @Test
	    public void testReduceIdentity() {
	       CompletableFuture<Integer> sum = of(1, 2, 3).actOnFutures()
	        							.reduce(CompletableFuture.completedFuture(0),(cf1,cf2)-> cf1.thenCombine(cf2, (a,b)->a+b));

	        assertThat(sum.join(),equalTo(6));
	    } 
	    @Test
	    public void testToCollect() {
	        List<CompletableFuture<Integer>> list = of(1, 2, 3).actOnFutures()
	        							.collect(Collectors.toList());

	        List<Integer> joined = list.stream().map(f->f.join()).collect(Collectors.toList());
	        for (Integer i :of(1, 2, 3)) {
	            assertThat(joined,hasItem(i));
	        }
	    } 
	    @Test
	    public void testToList() {
	        List<CompletableFuture<Integer>> list = of(1, 2, 3).actOnFutures()
	        							.toList();

	        List<Integer> joined = list.stream().map(f->f.join()).collect(Collectors.toList());
	        for (Integer i :of(1, 2, 3)) {
	            assertThat(joined,hasItem(i));
	        }
	    } 
	    @Test
	    public void testIterable() {
	        List<CompletableFuture<Integer>> list = of(1, 2, 3).actOnFutures()
	        							.toCollection(LinkedList::new);

	        List<Integer> joined = list.stream().map(f->f.join()).collect(Collectors.toList());
	        for (Integer i :of(1, 2, 3)) {
	            assertThat(joined,hasItem(i));
	        }
	    }
		
		@Test
		public void testDuplicate(){
			 Tuple2<FutureStream<Integer>, FutureStream<Integer>> copies =of(1,2,3,4,5,6).actOnFutures().duplicate();
			 assertTrue(copies._1().anyMatch(i->i==2));
			 assertTrue(copies._2().anyMatch(i->i==2));
		}
		
	Throwable ex;

	@Test
	public void testCastException() {
		ex = null;
		of(1, "a", 2, "b", 3, null).capture(e -> ex = e)
				.peek(it -> System.out.println(it)).cast(Integer.class)
				.peek(it -> System.out.println(it)).actOnFutures().toList();

		assertNull(ex); //simple-react no longer handling the futures
	}

	@Test
	public void testCastExceptionOnFail() {
		ex = null;
		of(1, "a", 2, "b", 3, null)// .capture(e-> {e.printStackTrace();ex =e;})
				// .peek(it ->System.out.println(it))
				.cast(Integer.class).onFail(e -> {
					System.out.println("**" + e.getValue());
					return 1;

				}).peek(it -> System.out.println(it)).actOnFutures().toList();

		assertThat(ex, is(nullValue()));
	}

	

	
	@Test
	public void testZipDifferingLength() {
		List<Tuple2<Integer, String>> list = of(1, 2).actOnFutures().zip(
				of("a", "b", "c", "d")).toList();

		assertEquals(2, list.size());
		assertTrue(asList(1, 2).contains(list.get(0)._1()));
		assertTrue("" + list.get(1)._2(), asList(1, 2).contains(list.get(1)._1()));
		assertTrue(asList("a", "b", "c", "d").contains(list.get(0)._2()));
		assertTrue(asList("a", "b", "c", "d").contains(list.get(1)._2()));

	}

	@Test
	public void testZipWithIndex() {
		// assertEquals(asList(), of().zipWithIndex().toList());
		// assertEquals(asList(tuple("a", 0L)), of("a").zip(of(0L)).toList());
		// assertEquals(asList(tuple("a", 0L)),
		// of("a").zipWithIndex().toList());
		assertEquals(asList(tuple("a", 0L), tuple("b", 1L)), of("a", "b")
				.zipWithIndex().toList());
		assertEquals(asList(tuple("a", 0L), tuple("b", 1L), tuple("c", 2L)),
				of("a", "b", "c").actOnFutures().zipWithIndex().toList());
	}

	
	
	

	
	

	

	

	

	@Test
	public void testFoldLeft() {
		Supplier<FutureStream<String>> s = () -> of("a", "b", "c");

		CompletableFuture<String> identity = CompletableFuture.completedFuture("");
		BinaryOperator<CompletableFuture<String>> concat = (cf1,cf2)-> cf1.thenCombine(cf2, String::concat);
		assertTrue(s.get().actOnFutures().foldLeft(identity, concat).join().contains("a"));
		assertTrue(s.get().actOnFutures().foldLeft(identity, concat).join().contains("b"));
		assertTrue(s.get().actOnFutures().foldLeft(identity, concat).join().contains("c"));

		assertEquals(3, (int) s.get().foldLeft(0, (u, t) -> u + t.length()));

		assertEquals(3, (int) s.get().foldRight(0, (t, u) -> u + t.length()));
		assertEquals(
				"-a-b-c",
				s.get()
						.foldLeft(new StringBuilder(),
								(u, t) -> u.append("-").append(t)).toString());
	}

	@Test
	public void testFoldRight() {
		Supplier<FutureStream<String>> s = () -> of("a", "b", "c");
		CompletableFuture<String> identity = CompletableFuture.completedFuture("");
		BinaryOperator<CompletableFuture<String>> concat = (cf1,cf2)-> cf1.thenCombine(cf2, String::concat);

		assertTrue(s.get().actOnFutures().foldRight(identity, concat).join().contains("a"));
		assertTrue(s.get().actOnFutures().foldRight(identity, concat).join().contains("b"));
		assertTrue(s.get().actOnFutures().foldRight(identity, concat).join().contains("c"));
		assertEquals(3, (int) s.get().foldRight(0, (t, u) -> u + t.length()));

	}

	

	

}
