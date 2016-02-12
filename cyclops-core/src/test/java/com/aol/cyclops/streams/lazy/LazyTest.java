package com.aol.cyclops.streams.lazy;
import static com.aol.cyclops.sequence.SequenceM.of;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.mapping;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.jooq.lambda.tuple.Tuple.collectors;
import static org.jooq.lambda.tuple.Tuple.tuple;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.jooq.lambda.tuple.Tuple2;
import org.junit.Before;
import org.junit.Test;

import com.aol.cyclops.control.Eval;
import com.aol.cyclops.control.ReactiveSeq;



//see BaseSequentialSeqTest for in order tests
public  class LazyTest {
	
	ReactiveSeq<Integer> empty;
	ReactiveSeq<Integer> nonEmpty;
	
	@Before
	public void setup(){
		empty = of();
		nonEmpty = of(1);
		
	}
	@Test
	public void testMax(){
		assertThat(of(1,2,3,4,5).lazyOperations().max((t1,t2) -> t1-t2)
				.get().get(),is(5));
	}
	@Test
	public void testMin(){
		assertThat(of(1,2,3,4,5).lazyOperations().min((t1,t2) -> t1-t2)
				.get().get(),is(1));
	}
	@Test
	public void testMapReduce(){
		assertThat(of(1,2,3,4,5).map(it -> it*100).lazyOperations()
					.reduce( (acc,next) -> acc+next)
					.get()
					.get(),is(1500));
	}
	@Test
	public void testMapReduceSeed(){
		assertThat(of(1,2,3,4,5).map(it -> it*100)
				.lazyOperations()
				.reduce( 50,(acc,next) -> acc+next)
				.get()
				,is(1550));
	}
	
	
	@Test
	public void testMapReduceCombiner(){
		assertThat(of(1,2,3,4,5).map(it -> it*100)
				.lazyOperations().reduce( 0,
                (acc, next) -> acc+next,
                Integer::sum).get(),is(1500));
	}
	
	@Test
	public void testFindFirst(){
		assertThat(Arrays.asList(1,2,3),hasItem(of(1,2,3,4,5).filter(it -> it <3).lazyOperations()
				.findFirst().get().get()));
	}
	@Test
	public void testFindAny(){
		assertThat(Arrays.asList(1,2,3),hasItem(of(1,2,3,4,5).filter(it -> it <3)
							.lazyOperations().findAny().get().get()));
	}
	@Test
	public void testAnyMatch(){
		assertThat(of(1,2,3,4,5).lazyOperations().anyMatch(it-> it.equals(3)).get(),is(true));
	}
	@Test
	public void testAllMatch(){
		assertThat(of(1,2,3,4,5).lazyOperations().allMatch(it-> it>0 && it <6).get(),is(true));
	}
	@Test
	public void testNoneMatch(){
		assertThat(of(1,2,3,4,5).lazyOperations().noneMatch(it-> it==5000).get(),is(true));
	}
	
	
	@Test
	public void testAnyMatchFalse(){
		assertThat(of(1,2,3,4,5).lazyOperations().anyMatch(it-> it.equals(8)).get(),is(false));
	}
	@Test
	public void testAllMatchFalse(){
		assertThat(of(1,2,3,4,5).lazyOperations().allMatch(it-> it<0 && it >6).get(),is(false));
	}
	@Test
	public void testToArray() {
		assertThat( Arrays.asList(1,2,3,4,5),hasItem(of(1,5,3,4,2).lazyOperations()
							.toArray().get()[0]));
	}
	@Test
	public void testToArrayGenerator() {
		assertThat( Arrays.asList(1,2,3,4,5),hasItem(of(1,5,3,4,2).lazyOperations()
				.toArray(it->new Integer[it]).get()[0]));
	}
	@Test
	public void testToCollection() {
		assertThat( Arrays.asList(1,2,3,4,5),equalTo(of(1,2,3,4,5).lazyOperations()
				.toCollection(()->new ArrayList()).get()));
	}

	@Test
	public void testCount(){
		assertThat(of(1,5,3,4,2).lazyOperations().count().get(),is(5L));
	}

	@Test
	public void collectSBB(){
		
		
		Eval<List<Integer>> cf = of(1,2,3,4,5).lazyOperations()
				.collect(ArrayList::new,(i,j)-> i.add(j), (i,j)-> i.addAll(j));
		List<Integer> list = cf.get();
		assertThat(list.size(),is(5));
	}
	@Test
	public void collect(){
		assertThat(of(1,2,3,4,5).lazyOperations().collect(Collectors.toList()).get().size(),is(5));
		assertThat(of(1,1,1,2).lazyOperations().collect(Collectors.toSet()).get().size(),is(2));
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
		public void groupBySupplierAndCollector(){
			Map<Integer, Tuple2<Long, String>> map4 =
			        of(tuple(1, 1), tuple(1, 2), tuple(1, 3), tuple(2, 4), tuple(2, 5))
			        	.lazyOperations()
			           .groupBy(t -> t.v1, collectors(counting(), 
			        		   mapping(t -> ((Tuple2<Integer,Integer>)t).map2(Object::toString).v2,
			        				   joining(", ")))).get();
			        assertEquals(3L, (long) map4.get(1).v1);
			        assertEquals(2L, (long) map4.get(2).v1);
			        assertEquals("1, 2, 3", map4.get(1).v2);
			        assertEquals("4, 5", map4.get(2).v2);
		}
		@Test
		public void groupByCollector(){
			 Map<Integer, Long> map3 =
				       of(tuple(1, 1), tuple(1, 2), tuple(1, 3), tuple(2, 1), tuple(2, 2))
				           .lazyOperations()
				       		.groupBy(t -> t.v1, counting()).get();
				        assertEquals(3L, (long) map3.get(1));
				        assertEquals(2L, (long) map3.get(2));
		}
	    @Test
	    public void testGroupByEager() {
	        Map<Integer, List<Integer>> map1 =of(1, 2, 3, 4)
	        										.lazyOperations()
	        										.groupBy(i -> i % 2)
	        										.get();
	       
	        assertThat(map1.get(0),hasItem(2));
	        assertThat(map1.get(0),hasItem(4));
	        assertThat(map1.get(1),hasItem(1));
	        assertThat(map1.get(1),hasItem(3));
	        
	        assertEquals(2, map1.size());

	     
	    }
	    

	    @Test
	    public void testJoin() {
	        assertEquals("123".length(),of(1, 2, 3).lazyOperations().join().get().length());
	        assertEquals("1, 2, 3".length(), of(1, 2, 3).lazyOperations()
	        				.join(", ").get()
	        				.length());
	        assertEquals("^1|2|3$".length(), of(1, 2, 3).lazyOperations()
	        		.join("|", "^", "$").get()
	        		.length());
	    }

	    
	   
	  
	    @Test
	    public void testMinByMaxBy() {
	        Supplier<ReactiveSeq<Integer>> s = () -> of(1, 2, 3, 4, 5, 6);

	        assertEquals(1, (int) s.get().lazyOperations().maxBy(t -> Math.abs(t - 5)).get().get());
	        assertEquals(5, (int) s.get().lazyOperations().minBy(t -> Math.abs(t - 5)).get().get());

	        assertEquals(6, (int) s.get().lazyOperations().maxBy(t -> "" + t).get().get());
	        assertEquals(1, (int) s.get().lazyOperations().minBy(t -> "" + t).get().get());
	    }

	  
	   

	    @Test
	    public void testFoldLeft() {
	    	for(int i=0;i<100;i++){
		        Supplier<ReactiveSeq<String>> s = () -> of("a", "b", "c");
	
		        assertTrue(s.get().lazyOperations().foldLeft("", String::concat).get().contains("a"));
		        assertTrue(s.get().lazyOperations().foldLeft("", String::concat).get().contains("b"));
		        assertTrue(s.get().lazyOperations().foldLeft("", String::concat).get().contains("c"));
		       
		        assertEquals(3, (int) s.get().lazyOperations().foldLeft(0, (u, t) -> u + t.length()).get());
	
		        
		        assertEquals(3, (int) s.get().lazyOperations().foldRight(0, (t, u) -> u + t.length()).get());
	    	}
	    }
	    
	    @Test
	    public void testFoldRight(){
	    	 	Supplier<ReactiveSeq<String>> s = () -> of("a", "b", "c");

		        assertTrue(s.get().lazyOperations().foldRight("", String::concat).get().contains("a"));
		        assertTrue(s.get().lazyOperations().foldRight("", String::concat).get().contains("b"));
		        assertTrue(s.get().lazyOperations().foldRight("", String::concat).get().contains("c"));
		        assertEquals(3, (int) s.get().lazyOperations().foldRight(0, (t, u) -> u + t.length()).get());
	    }
	    
	    @Test
	    public void testFoldLeftStringBuilder() {
	        Supplier<ReactiveSeq<String>> s = () -> of("a", "b", "c");

	        
	        assertTrue(s.get().lazyOperations().foldLeft(new StringBuilder(), (u, t) -> u.append("-").append(t)).get().toString().contains("a"));
	        assertTrue(s.get().lazyOperations().foldLeft(new StringBuilder(), (u, t) -> u.append("-").append(t)).get().toString().contains("b"));
	        assertTrue(s.get().lazyOperations().foldLeft(new StringBuilder(), (u, t) -> u.append("-").append(t)).get().toString().contains("c"));
	        assertTrue(s.get().lazyOperations().foldLeft(new StringBuilder(), (u, t) -> u.append("-").append(t)).get().toString().contains("-"));
	        
	        
	        assertEquals(3, (int) s.get().lazyOperations().foldLeft(0, (u, t) -> u + t.length()).get());

	       
	    }

	    @Test
	    public void testFoldRighttringBuilder() {
	        Supplier<ReactiveSeq<String>> s = () -> of("a", "b", "c");

	        
	        assertTrue(s.get().lazyOperations().foldRight(new StringBuilder(), (t, u) -> u.append("-").append(t)).get().toString().contains("a"));
	        assertTrue(s.get().lazyOperations().foldRight(new StringBuilder(), (t, u) -> u.append("-").append(t)).get().toString().contains("b"));
	        assertTrue(s.get().lazyOperations().foldRight(new StringBuilder(), (t, u) -> u.append("-").append(t)).get().toString().contains("c"));
	        assertTrue(s.get().lazyOperations().foldRight(new StringBuilder(), (t, u) -> u.append("-").append(t)).get().toString().contains("-"));
	        
	           
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