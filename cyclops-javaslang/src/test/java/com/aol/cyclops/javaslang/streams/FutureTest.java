package com.aol.cyclops.javaslang.streams;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javaslang.collection.LazyStream;
import lombok.experimental.ExtensionMethod;

import org.jooq.lambda.tuple.Tuple2;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.aol.cyclops.sequence.SequenceM;



//see BaseSequentialSeqTest for in order tests
@ExtensionMethod(StreamUtils.class)
public  class FutureTest {
	/**
	Stream<Integer> empty;
	Stream<Integer> nonEmpty;
	static final Executor exec = Executors.newFixedThreadPool(1);
	@Before
	public void setup(){
		empty = Stream.empty();
		nonEmpty = Stream.of(1);
		
	}
	@Test
	public void testMax(){
		assertThat(of(1,2,3,4,5).futureOperations(exec).max((t1,t2) -> t1-t2)
				.join().get(),is(5));
	}
	@Test
	public void testMin(){
		assertThat(of(1,2,3,4,5).futureOperations(exec).min((t1,t2) -> t1-t2)
				.join().get(),is(1));
	}
	@Test
	public void testMapReduce(){
		assertThat(of(1,2,3,4,5).map(it -> it*100).futureOperations(exec)
					.reduce( (acc,next) -> acc+next)
					.join()
					.get(),is(1500));
	}
	@Test
	public void testMapReduceSeed(){
		assertThat(of(1,2,3,4,5).map(it -> it*100)
				.futureOperations(exec)
				.reduce( 50,(acc,next) -> acc+next)
				.join()
				,is(1550));
	}
	
	
	@Test
	public void testMapReduceCombiner(){
		assertThat(of(1,2,3,4,5).map(it -> it*100)
				.futureOperations(exec).reduce( 0,
                (acc, next) -> acc+next,
                Integer::sum).join(),is(1500));
	}
	
	@Test
	public void testFindFirst(){
		assertThat(Arrays.asList(1,2,3),hasItem(of(1,2,3,4,5).filter(it -> it <3).futureOperations(exec)
				.findFirst().join().get()));
	}
	@Test
	public void testFindAny(){
		assertThat(Arrays.asList(1,2,3),hasItem(of(1,2,3,4,5).filter(it -> it <3)
							.futureOperations(exec).findAny().join().get()));
	}
	@Test
	public void testAnyMatch(){
		assertThat(of(1,2,3,4,5).futureOperations(exec).anyMatch(it-> it.equals(3)).join(),is(true));
	}
	@Test
	public void testAllMatch(){
		assertThat(of(1,2,3,4,5).futureOperations(exec).allMatch(it-> it>0 && it <6).join(),is(true));
	}
	@Test
	public void testNoneMatch(){
		assertThat(of(1,2,3,4,5).futureOperations(exec).noneMatch(it-> it==5000).join(),is(true));
	}
	
	
	@Test
	public void testAnyMatchFalse(){
		assertThat(of(1,2,3,4,5).futureOperations(exec).anyMatch(it-> it.equals(8)).join(),is(false));
	}
	@Test
	public void testAllMatchFalse(){
		assertThat(of(1,2,3,4,5).futureOperations(exec).allMatch(it-> it<0 && it >6).join(),is(false));
	}
	@Test
	public void testToArray() {
		assertThat( Arrays.asList(1,2,3,4,5),hasItem(of(1,5,3,4,2).futureOperations(exec)
							.toArray().join()[0]));
	}
	@Test
	public void testToArrayGenerator() {
		assertThat( Arrays.asList(1,2,3,4,5),hasItem(of(1,5,3,4,2).futureOperations(exec)
				.toArray(it->new Integer[it]).join()[0]));
	}
	@Test
	public void testToCollection() {
		assertThat( Arrays.asList(1,2,3,4,5),equalTo(of(1,2,3,4,5).futureOperations(exec)
				.toCollection(()->new ArrayList()).join()));
	}

	@Test
	public void testCount(){
		assertThat(of(1,5,3,4,2).futureOperations(exec).count().join(),is(5L));
	}

	@Test
	public void collectSBB(){
		
		
		CompletableFuture<List<Integer>> cf = of(1,2,3,4,5).futureOperations(exec)
				.collect(ArrayList::new,(i,j)-> i.add(j), (i,j)-> i.addAll(j));
		List<Integer> list = cf.join();
		assertThat(list.size(),is(5));
	}
	@Test
	public void collect(){
		assertThat(of(1,2,3,4,5).futureOperations(exec).collect(Collectors.toList()).join().size(),is(5));
		assertThat(of(1,1,1,2).futureOperations(exec).collect(Collectors.toSet()).join().size(),is(2));
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
			        	.futureOperations(exec)
			           .groupBy(t -> t.v1, collectors(counting(), 
			        		   mapping(t -> ((Tuple2<Integer,Integer>)t).map2(Object::toString).v2,
			        				   joining(", ")))).join();
			        assertEquals(3L, (long) map4.get(1).v1);
			        assertEquals(2L, (long) map4.get(2).v1);
			        assertEquals("1, 2, 3", map4.get(1).v2);
			        assertEquals("4, 5", map4.get(2).v2);
		}
		@Test
		public void groupByCollector(){
			 Map<Integer, Long> map3 =
				       of(tuple(1, 1), tuple(1, 2), tuple(1, 3), tuple(2, 1), tuple(2, 2))
				           .futureOperations(exec)
				       		.groupBy(t -> t.v1, counting()).join();
				        assertEquals(3L, (long) map3.get(1));
				        assertEquals(2L, (long) map3.get(2));
		}
	    @Test
	    public void testGroupByEager() {
	        Map<Integer, List<Integer>> map1 =of(1, 2, 3, 4)
	        										.futureOperations(exec)
	        										.groupBy(i -> i % 2)
	        										.join();
	       
	        assertThat(map1.get(0),hasItem(2));
	        assertThat(map1.get(0),hasItem(4));
	        assertThat(map1.get(1),hasItem(1));
	        assertThat(map1.get(1),hasItem(3));
	        
	        assertEquals(2, map1.size());

	     
	    }
	    

	    @Test
	    public void testJoin() {
	        assertEquals("123".length(),of(1, 2, 3).futureOperations(exec).join().join().length());
	        assertEquals("1, 2, 3".length(), of(1, 2, 3).futureOperations(exec)
	        				.join(", ").join()
	        				.length());
	        assertEquals("^1|2|3$".length(), of(1, 2, 3).futureOperations(exec)
	        		.join("|", "^", "$").join()
	        		.length());
	    }

	    
	   
	  
	    @Test
	    public void testMinByMaxBy() {
	        Supplier<SequenceM<Integer>> s = () -> of(1, 2, 3, 4, 5, 6);

	        assertEquals(1, (int) s.get().futureOperations(exec).maxBy(t -> Math.abs(t - 5)).join().get());
	        assertEquals(5, (int) s.get().futureOperations(exec).minBy(t -> Math.abs(t - 5)).join().get());

	        assertEquals(6, (int) s.get().futureOperations(exec).maxBy(t -> "" + t).join().get());
	        assertEquals(1, (int) s.get().futureOperations(exec).minBy(t -> "" + t).join().get());
	    }

	  
	   

	    @Test
	    public void testFoldLeft() {
	    	for(int i=0;i<100;i++){
		        Supplier<SequenceM<String>> s = () -> of("a", "b", "c");
	
		        assertTrue(s.get().futureOperations(exec).foldLeft("", String::concat).join().contains("a"));
		        assertTrue(s.get().futureOperations(exec).foldLeft("", String::concat).join().contains("b"));
		        assertTrue(s.get().futureOperations(exec).foldLeft("", String::concat).join().contains("c"));
		       
		        assertEquals(3, (int) s.get().futureOperations(exec).foldLeft(0, (u, t) -> u + t.length()).join());
	
		        
		        assertEquals(3, (int) s.get().futureOperations(exec).foldRight(0, (t, u) -> u + t.length()).join());
	    	}
	    }
	    
	    @Test
	    public void testFoldRight(){
	    	 	Supplier<SequenceM<String>> s = () -> of("a", "b", "c");

		        assertTrue(s.get().futureOperations(exec).foldRight("", String::concat).join().contains("a"));
		        assertTrue(s.get().futureOperations(exec).foldRight("", String::concat).join().contains("b"));
		        assertTrue(s.get().futureOperations(exec).foldRight("", String::concat).join().contains("c"));
		        assertEquals(3, (int) s.get().futureOperations(exec).foldRight(0, (t, u) -> u + t.length()).join());
	    }
	    
	    @Test
	    public void testFoldLeftStringBuilder() {
	        Supplier<SequenceM<String>> s = () -> of("a", "b", "c");

	        
	        assertTrue(s.get().futureOperations(exec).foldLeft(new StringBuilder(), (u, t) -> u.append("-").append(t)).join().toString().contains("a"));
	        assertTrue(s.get().futureOperations(exec).foldLeft(new StringBuilder(), (u, t) -> u.append("-").append(t)).join().toString().contains("b"));
	        assertTrue(s.get().futureOperations(exec).foldLeft(new StringBuilder(), (u, t) -> u.append("-").append(t)).join().toString().contains("c"));
	        assertTrue(s.get().futureOperations(exec).foldLeft(new StringBuilder(), (u, t) -> u.append("-").append(t)).join().toString().contains("-"));
	        
	        
	        assertEquals(3, (int) s.get().futureOperations(exec).foldLeft(0, (u, t) -> u + t.length()).join());

	       
	    }

	    @Test
	    public void testFoldRighttringBuilder() {
	        Supplier<SequenceM<String>> s = () -> of("a", "b", "c");

	        
	        assertTrue(s.get().futureOperations(exec).foldRight(new StringBuilder(), (t, u) -> u.append("-").append(t)).join().toString().contains("a"));
	        assertTrue(s.get().futureOperations(exec).foldRight(new StringBuilder(), (t, u) -> u.append("-").append(t)).join().toString().contains("b"));
	        assertTrue(s.get().futureOperations(exec).foldRight(new StringBuilder(), (t, u) -> u.append("-").append(t)).join().toString().contains("c"));
	        assertTrue(s.get().futureOperations(exec).foldRight(new StringBuilder(), (t, u) -> u.append("-").append(t)).join().toString().contains("-"));
	        
	           
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
	**/
}