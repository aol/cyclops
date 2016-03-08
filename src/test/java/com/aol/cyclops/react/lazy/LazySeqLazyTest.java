package com.aol.cyclops.react.lazy;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.is;
import static org.jooq.lambda.tuple.Tuple.tuple;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.function.Supplier;

import org.jooq.lambda.tuple.Tuple2;
import org.junit.Test;

import com.aol.cyclops.react.base.BaseSeqFutureTest;
import com.aol.cyclops.react.base.BaseSeqLazyTest;
import com.aol.cyclops.types.futurestream.LazyFutureStream;

public class LazySeqLazyTest extends BaseSeqLazyTest{
	
	  @Test
	    public void testZipDifferingLength() {
	        List<Tuple2<Integer, String>> list = of(1, 2).zip(of("a", "b", "c", "d"))
	        		.lazyOperations()
	        		.toList()
	        		.get();

	        assertEquals(2, list.size());
	        assertTrue(asList(1,2).contains( list.get(0).v1));
	        assertTrue(""+list.get(1).v2,asList(1,2).contains( list.get(1).v1)); 
	        assertTrue(asList("a", "b", "c", "d").contains( list.get(0).v2));
	        assertTrue(asList("a", "b", "c", "d").contains( list.get(1).v2));
	       
	        
	    }

	    @Test
	    public void testZipWithIndex() {
	        assertEquals(asList(),of().zipWithIndex().lazyOperations().toList().get());
	   
	       
	      assertThat( of("a").zipWithIndex().map(t->t.v2).lazyOperations().findFirst().get().get(),is(0l));
	      assertEquals(asList(tuple("a", 0L)), of("a").zipWithIndex().lazyOperations().toList().get());
	    }

	   
	    @Test
	    public void testSkipWhile() {
	        Supplier<LazyFutureStream<Integer>> s = () -> of(1, 2, 3, 4, 5);

	        assertTrue(s.get().skipWhile(i -> false).lazyOperations().toList().get().containsAll(asList(1, 2, 3, 4, 5)));
	      
	        assertEquals(asList(), s.get().skipWhile(i -> true).toList());
	    }

	    @Test
	    public void testSkipUntil() {
	        Supplier<LazyFutureStream<Integer>> s = () -> of(1, 2, 3, 4, 5);

	        assertEquals(asList(), s.get().skipUntil(i -> false).lazyOperations().toList().get());
	        assertTrue(s.get().skipUntil(i -> true).toList().containsAll(asList(1, 2, 3, 4, 5)));
		  }

	    @Test
	    public void testSkipUntilWithNulls() {
	        Supplier<LazyFutureStream<Integer>> s = () -> of(1, 2, null, 3, 4, 5);
	       
	        assertTrue(s.get().skipUntil(i -> true).lazyOperations()
	        				.toList().get().containsAll(asList(1, 2, null, 3, 4, 5)));
	    }

	    @Test
	    public void testLimitWhile() {
	        Supplier<LazyFutureStream<Integer>> s = () -> of(1, 2, 3, 4, 5);

	        assertEquals(asList(), s.get().limitWhile(i -> false).lazyOperations().toList().get());
	        assertTrue( s.get().limitWhile(i -> i < 3).lazyOperations().toList().get().size()!=5);       
	        assertTrue(s.get().limitWhile(i -> true).lazyOperations().toList().get().containsAll(asList(1, 2, 3, 4, 5)));
	    }

	    @Test
	    public void testLimitUntil() {
	        

	        assertTrue(of(1, 2, 3, 4, 5).limitUntil(i -> false).lazyOperations().toList()
	        		.get().containsAll(asList(1, 2, 3, 4, 5)));
	       
	    }

	    @Test
	    public void testLimitUntilWithNulls() {
	       

	        assertTrue(of(1, 2, null, 3, 4, 5).limitUntil(i -> false).lazyOperations()
	        				.toSet().get().containsAll(asList(1, 2, null, 3, 4, 5)));
	    }

		@Override
		protected <U> LazyFutureStream<U> of(U... array) {
			return LazyFutureStream.of(array);
		}

		@Override
		protected <U> LazyFutureStream<U> ofThread(U... array) {
			return LazyFutureStream.freeThread(array);
		}

		@Override
		protected <U> LazyFutureStream<U> react(Supplier<U>... array) {
			return LazyFutureStream.react(array);
		}

	   
	
	

}
