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

import com.aol.cyclops.react.base.BaseSeqLazyTest;
import com.aol.cyclops.types.futurestream.LazyFutureStream;

public class LazySeqLazyTest extends BaseSeqLazyTest{
	
	  @Test
	    public void testZipDifferingLength() {
	        List<Tuple2<Integer, String>> list = of(1, 2).zip(of("a", "b", "c", "d"))
	        		.foldLazy(s->s.toList())
	        		.get();

	        assertEquals(2, list.size());
	        assertTrue(asList(1,2).contains( list.get(0).v1));
	        assertTrue(""+list.get(1).v2,asList(1,2).contains( list.get(1).v1)); 
	        assertTrue(asList("a", "b", "c", "d").contains( list.get(0).v2));
	        assertTrue(asList("a", "b", "c", "d").contains( list.get(1).v2));
	       
	        
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
