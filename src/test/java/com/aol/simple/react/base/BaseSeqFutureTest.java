package com.aol.simple.react.base;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;

import com.aol.simple.react.stream.traits.FutureStream;


//see BaseSequentialSeqTest for in order tests
public abstract class BaseSeqFutureTest {
	abstract protected <U> FutureStream<U> of(U... array);
	abstract protected <U> FutureStream<U> ofThread(U... array);
	abstract protected <U> FutureStream<U> react(Supplier<U>... array);
	FutureStream<Integer> empty;
	FutureStream<Integer> nonEmpty;

	@Before
	public void setup(){
		empty = of();
		nonEmpty = of(1);
		
	}
	@Test
	public void testMax(){
		assertThat(of(1,2,3,4,5).futureOperations().max((t1,t2) -> t1-t2)
				.join().get(),is(5));
	}
	@Test
	public void testMin(){
		assertThat(of(1,2,3,4,5).futureOperations().min((t1,t2) -> t1-t2)
				.join().get(),is(1));
	}
	@Test
	public void testMapReduce(){
		assertThat(of(1,2,3,4,5).map(it -> it*100).futureOperations()
					.reduce( (acc,next) -> acc+next)
					.join()
					.get(),is(1500));
	}
	@Test
	public void testMapReduceSeed(){
		assertThat(of(1,2,3,4,5).map(it -> it*100)
				.futureOperations()
				.reduce( 50,(acc,next) -> acc+next)
				.join()
				,is(1550));
	}
	
	
	@Test
	public void testMapReduceCombiner(){
		assertThat(of(1,2,3,4,5).map(it -> it*100)
				.futureOperations().reduce( 0,
                (acc, next) -> acc+next,
                Integer::sum).join(),is(1500));
	}
	@Test
	public void testFindFirst(){
		assertThat(Arrays.asList(1,2,3),hasItem(of(1,2,3,4,5).filter(it -> it <3).futureOperations()
				.findFirst().join().get()));
	}
	@Test
	public void testFindAny(){
		assertThat(Arrays.asList(1,2,3),hasItem(of(1,2,3,4,5).filter(it -> it <3).findAny().get()));
	}
	@Test
	public void testAnyMatch(){
		assertThat(of(1,2,3,4,5).futureOperations().anyMatch(it-> it.equals(3)).join(),is(true));
	}
	@Test
	public void testAllMatch(){
		assertThat(of(1,2,3,4,5).futureOperations().allMatch(it-> it>0 && it <6).join(),is(true));
	}
	@Test
	public void testNoneMatch(){
		assertThat(of(1,2,3,4,5).futureOperations().noneMatch(it-> it==5000).join(),is(true));
	}
	
	
	@Test
	public void testAnyMatchFalse(){
		assertThat(of(1,2,3,4,5).futureOperations().anyMatch(it-> it.equals(8)).join(),is(false));
	}
	@Test
	public void testAllMatchFalse(){
		assertThat(of(1,2,3,4,5).futureOperations().allMatch(it-> it<0 && it >6).join(),is(false));
	}
	@Test
	public void testToArray() {
		assertThat( Arrays.asList(1,2,3,4,5),hasItem(of(1,5,3,4,2).futureOperations()
							.toArray().join()[0]));
	}
	@Test
	public void testToArrayGenerator() {
		assertThat( Arrays.asList(1,2,3,4,5),hasItem(of(1,5,3,4,2).futureOperations()
				.toArray(it->new Integer[it]).join()[0]));
	}

	@Test
	public void testCount(){
		assertThat(of(1,5,3,4,2).futureOperations().count().join(),is(5L));
	}

	@Test
	public void collectSBB(){
		
		
		CompletableFuture<List<Integer>> cf = of(1,2,3,4,5).futureOperations()
				.collect(ArrayList::new,(i,j)-> i.add(j), (i,j)-> i.addAll(j));
		List<Integer> list = cf.join();
		assertThat(list.size(),is(5));
	}
	@Test
	public void collect(){
		assertThat(of(1,2,3,4,5).futureOperations().collect(Collectors.toList()).join().size(),is(5));
		assertThat(of(1,1,1,2).futureOperations().collect(Collectors.toSet()).join().size(),is(2));
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
	    public void testGroupByEager() {
	        Map<Integer, List<Integer>> map1 =of(1, 2, 3, 4)
	        										.futureOperations()
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
	        assertEquals("123".length(),of(1, 2, 3).futureOperations().join().join().length());
	        assertEquals("1, 2, 3".length(), of(1, 2, 3).futureOperations()
	        				.join(", ").join()
	        				.length());
	        assertEquals("^1|2|3$".length(), of(1, 2, 3).futureOperations()
	        		.join("|", "^", "$").join()
	        		.length());
	    }

	    
	   
	  
	    @Test
	    public void testMinByMaxBy() {
	        Supplier<FutureStream<Integer>> s = () -> of(1, 2, 3, 4, 5, 6);

	        assertEquals(1, (int) s.get().futureOperations().maxBy(t -> Math.abs(t - 5)).join().get());
	        assertEquals(5, (int) s.get().futureOperations().minBy(t -> Math.abs(t - 5)).join().get());

	        assertEquals(6, (int) s.get().futureOperations().maxBy(t -> "" + t).join().get());
	        assertEquals(1, (int) s.get().futureOperations().minBy(t -> "" + t).join().get());
	    }

	  
	   

	    @Test
	    public void testFoldLeft() {
	    	for(int i=0;i<100;i++){
		        Supplier<FutureStream<String>> s = () -> of("a", "b", "c");
	
		        assertTrue(s.get().futureOperations().foldLeft("", String::concat).join().contains("a"));
		        assertTrue(s.get().futureOperations().foldLeft("", String::concat).join().contains("b"));
		        assertTrue(s.get().futureOperations().foldLeft("", String::concat).join().contains("c"));
		       
		        assertEquals(3, (int) s.get().futureOperations().foldLeft(0, (u, t) -> u + t.length()).join());
	
		        
		        assertEquals(3, (int) s.get().futureOperations().foldRight(0, (t, u) -> u + t.length()).join());
	    	}
	    }
	    
	    @Test
	    public void testFoldRight(){
	    	 	Supplier<FutureStream<String>> s = () -> of("a", "b", "c");

		        assertTrue(s.get().futureOperations().foldRight("", String::concat).join().contains("a"));
		        assertTrue(s.get().futureOperations().foldRight("", String::concat).join().contains("b"));
		        assertTrue(s.get().futureOperations().foldRight("", String::concat).join().contains("c"));
		        assertEquals(3, (int) s.get().futureOperations().foldRight(0, (t, u) -> u + t.length()).join());
	    }
	    
	    @Test
	    public void testFoldLeftStringBuilder() {
	        Supplier<FutureStream<String>> s = () -> of("a", "b", "c");

	        
	        assertTrue(s.get().futureOperations().foldLeft(new StringBuilder(), (u, t) -> u.append("-").append(t)).join().toString().contains("a"));
	        assertTrue(s.get().futureOperations().foldLeft(new StringBuilder(), (u, t) -> u.append("-").append(t)).join().toString().contains("b"));
	        assertTrue(s.get().futureOperations().foldLeft(new StringBuilder(), (u, t) -> u.append("-").append(t)).join().toString().contains("c"));
	        assertTrue(s.get().futureOperations().foldLeft(new StringBuilder(), (u, t) -> u.append("-").append(t)).join().toString().contains("-"));
	        
	        
	        assertEquals(3, (int) s.get().futureOperations().foldLeft(0, (u, t) -> u + t.length()).join());

	       
	    }

	    @Test
	    public void testFoldRighttringBuilder() {
	        Supplier<FutureStream<String>> s = () -> of("a", "b", "c");

	        
	        assertTrue(s.get().futureOperations().foldRight(new StringBuilder(), (t, u) -> u.append("-").append(t)).join().toString().contains("a"));
	        assertTrue(s.get().futureOperations().foldRight(new StringBuilder(), (t, u) -> u.append("-").append(t)).join().toString().contains("b"));
	        assertTrue(s.get().futureOperations().foldRight(new StringBuilder(), (t, u) -> u.append("-").append(t)).join().toString().contains("c"));
	        assertTrue(s.get().futureOperations().foldRight(new StringBuilder(), (t, u) -> u.append("-").append(t)).join().toString().contains("-"));
	        
	           
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
