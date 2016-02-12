package com.aol.cyclops.streams.lazy;

import static org.jooq.lambda.Agg.count;
import static org.jooq.lambda.Agg.max;
import static org.jooq.lambda.Agg.min;
import static org.jooq.lambda.tuple.Tuple.tuple;
import static org.junit.Assert.assertEquals;

import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.junit.Test;

import com.aol.cyclops.control.ReactiveSeq;

public class LazyOpsTest {
		Executor exec = Executors.newFixedThreadPool(1);
		@Test
	    public void testCount() {
		
	        assertEquals(new Long(0l), ReactiveSeq.of().lazyOperations().count());
	        assertEquals((Long)0L, ReactiveSeq.of().lazyOperations().countDistinct());
	        assertEquals((Long)0L, ReactiveSeq.<Integer>of().lazyOperations().countDistinctBy(l -> l % 3));

	        assertEquals((Long)1L, ReactiveSeq.of(1).lazyOperations().count());
	        assertEquals((Long)1L, ReactiveSeq.of(1).lazyOperations().countDistinct());
	        assertEquals((Long)1L, ReactiveSeq.of(1).lazyOperations().countDistinctBy(l -> l % 3L));

	        assertEquals((Long)2L, ReactiveSeq.of(1, 2).lazyOperations().count());
	        assertEquals((Long)2L, ReactiveSeq.of(1, 2).lazyOperations().countDistinct());
	        assertEquals((Long)2L, ReactiveSeq.of(1, 2).lazyOperations().countDistinctBy(l -> l % 3L));

	        assertEquals((Long)3L, ReactiveSeq.of(1, 2, 2).lazyOperations().count());
	        assertEquals((Long)2L, ReactiveSeq.of(1, 2, 2).lazyOperations().countDistinct());
	        assertEquals((Long)2L, ReactiveSeq.of(1, 2, 2).lazyOperations().countDistinctBy(l -> l % 3L));

	        assertEquals((Long)4L, ReactiveSeq.of(1, 2, 2, 4).lazyOperations().count());
	        assertEquals((Long)3L, ReactiveSeq.of(1, 2, 2, 4).lazyOperations().countDistinct());
	        assertEquals((Long)2L, ReactiveSeq.of(1, 2, 2, 4).lazyOperations().countDistinctBy(l -> l % 3L));
	    }
	    
	    @Test
	    public void testSum() {
	        assertEquals(Optional.empty(), ReactiveSeq.of().lazyOperations().sum());
	        
	        assertEquals(Optional.of(1), ReactiveSeq.of(1).lazyOperations().sum());
	        assertEquals(Optional.of(3), ReactiveSeq.of(1, 2).lazyOperations().sum());
	        assertEquals(Optional.of(6), ReactiveSeq.of(1, 2, 3).lazyOperations().sum());
	        
	        assertEquals(Optional.of(1.0), ReactiveSeq.of(1.0).lazyOperations().sum());
	        assertEquals(Optional.of(3.0), ReactiveSeq.of(1.0, 2.0).lazyOperations().sum());
	        assertEquals(Optional.of(6.0), ReactiveSeq.of(1.0, 2.0, 3.0).lazyOperations().sum());
	    }
	    
	    @Test
	    public void testAvg() {
	        assertEquals(Optional.empty(), ReactiveSeq.of().lazyOperations().avg());
	        
	        assertEquals(Optional.of(1), ReactiveSeq.of(1).lazyOperations().avg());
	        assertEquals(Optional.of(1), ReactiveSeq.of(1, 2).lazyOperations().avg());
	        assertEquals(Optional.of(2), ReactiveSeq.of(1, 2, 3).lazyOperations().avg());
	        
	        assertEquals(Optional.of(1.0), ReactiveSeq.of(1.0).lazyOperations().avg());
	        assertEquals(Optional.of(1.5), ReactiveSeq.of(1.0, 2.0).lazyOperations().avg());
	        assertEquals(Optional.of(2.0), ReactiveSeq.of(1.0, 2.0, 3.0).lazyOperations().avg());
	    }

	    @Test
	    public void testCollect() {
	        assertEquals(
	            tuple(0L, Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty()),
	            ReactiveSeq.<Integer>of().lazyOperations().collect(count(), min(), min(i -> -i), max(), max(i -> -i))
	        );

	        assertEquals(
	            tuple(1L, Optional.of(1), Optional.of(-1), Optional.of(1), Optional.of(-1)),
	            ReactiveSeq.of(1).lazyOperations().collect(count(), min(), min(i -> -i), max(), max(i -> -i))
	        );

	        assertEquals(
	            tuple(2L, Optional.of(1), Optional.of(-2), Optional.of(2), Optional.of(-1)),
	            ReactiveSeq.of(1, 2).lazyOperations().collect(count(), min(), min(i -> -i), max(), max(i -> -i))
	        );

	        assertEquals(
	            tuple(3L, Optional.of(1), Optional.of(-3), Optional.of(3), Optional.of(-1)),
	            ReactiveSeq.of(1, 2, 3).lazyOperations().collect(count(), min(), min(i -> -i), max(), max(i -> -i))
	        );

	        assertEquals(
	            tuple(4L, Optional.of(1), Optional.of(-4), Optional.of(4), Optional.of(-1)),
	            ReactiveSeq.of(1, 2, 3, 4).lazyOperations().collect(count(), min(), min(i -> -i), max(), max(i -> -i))
	        );
	    }
}
