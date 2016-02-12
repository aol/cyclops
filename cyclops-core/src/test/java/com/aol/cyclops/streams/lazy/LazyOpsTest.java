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
		
	        assertEquals(new Long(0l), ReactiveSeq.of().lazyOperations().count().get());
	        assertEquals((Long)0L, ReactiveSeq.of().lazyOperations().countDistinct().get());
	        assertEquals((Long)0L, ReactiveSeq.<Integer>of().lazyOperations().countDistinctBy(l -> l % 3).get());

	        assertEquals((Long)1L, ReactiveSeq.of(1).lazyOperations().count().get());
	        assertEquals((Long)1L, ReactiveSeq.of(1).lazyOperations().countDistinct().get());
	        assertEquals((Long)1L, ReactiveSeq.of(1).lazyOperations().countDistinctBy(l -> l % 3L).get());

	        assertEquals((Long)2L, ReactiveSeq.of(1, 2).lazyOperations().count().get());
	        assertEquals((Long)2L, ReactiveSeq.of(1, 2).lazyOperations().countDistinct().get());
	        assertEquals((Long)2L, ReactiveSeq.of(1, 2).lazyOperations().countDistinctBy(l -> l % 3L).get());

	        assertEquals((Long)3L, ReactiveSeq.of(1, 2, 2).lazyOperations().count().get());
	        assertEquals((Long)2L, ReactiveSeq.of(1, 2, 2).lazyOperations().countDistinct().get());
	        assertEquals((Long)2L, ReactiveSeq.of(1, 2, 2).lazyOperations().countDistinctBy(l -> l % 3L).get());

	        assertEquals((Long)4L, ReactiveSeq.of(1, 2, 2, 4).lazyOperations().count().get());
	        assertEquals((Long)3L, ReactiveSeq.of(1, 2, 2, 4).lazyOperations().countDistinct().get());
	        assertEquals((Long)2L, ReactiveSeq.of(1, 2, 2, 4).lazyOperations().countDistinctBy(l -> l % 3L).get());
	    }
	    
	    @Test
	    public void testSum() {
	        assertEquals(Optional.empty(), ReactiveSeq.of().lazyOperations().sum().get());
	        
	        assertEquals(Optional.of(1), ReactiveSeq.of(1).lazyOperations().sum().get());
	        assertEquals(Optional.of(3), ReactiveSeq.of(1, 2).lazyOperations().sum().get());
	        assertEquals(Optional.of(6), ReactiveSeq.of(1, 2, 3).lazyOperations().sum().get());
	        
	        assertEquals(Optional.of(1.0), ReactiveSeq.of(1.0).lazyOperations().sum().get());
	        assertEquals(Optional.of(3.0), ReactiveSeq.of(1.0, 2.0).lazyOperations().sum().get());
	        assertEquals(Optional.of(6.0), ReactiveSeq.of(1.0, 2.0, 3.0).lazyOperations().sum().get());
	    }
	    
	    @Test
	    public void testAvg() {
	        assertEquals(Optional.empty(), ReactiveSeq.of().lazyOperations().avg().get());
	        
	        assertEquals(Optional.of(1), ReactiveSeq.of(1).lazyOperations().avg().get());
	        assertEquals(Optional.of(1), ReactiveSeq.of(1, 2).lazyOperations().avg().get());
	        assertEquals(Optional.of(2), ReactiveSeq.of(1, 2, 3).lazyOperations().avg().get());
	        
	        assertEquals(Optional.of(1.0), ReactiveSeq.of(1.0).lazyOperations().avg().get());
	        assertEquals(Optional.of(1.5), ReactiveSeq.of(1.0, 2.0).lazyOperations().avg().get());
	        assertEquals(Optional.of(2.0), ReactiveSeq.of(1.0, 2.0, 3.0).lazyOperations().avg().get());
	    }

	    @Test
	    public void testCollect() {
	        assertEquals(
	            tuple(0L, Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty()),
	            ReactiveSeq.<Integer>of().lazyOperations().collect(count(), min(), min(i -> -i), max(), max(i -> -i)).get()
	        );

	        assertEquals(
	            tuple(1L, Optional.of(1), Optional.of(-1), Optional.of(1), Optional.of(-1)),
	            ReactiveSeq.of(1).lazyOperations().collect(count(), min(), min(i -> -i), max(), max(i -> -i)).get()
	        );

	        assertEquals(
	            tuple(2L, Optional.of(1), Optional.of(-2), Optional.of(2), Optional.of(-1)),
	            ReactiveSeq.of(1, 2).lazyOperations().collect(count(), min(), min(i -> -i), max(), max(i -> -i)).get()
	        );

	        assertEquals(
	            tuple(3L, Optional.of(1), Optional.of(-3), Optional.of(3), Optional.of(-1)),
	            ReactiveSeq.of(1, 2, 3).lazyOperations().collect(count(), min(), min(i -> -i), max(), max(i -> -i)).get()
	        );

	        assertEquals(
	            tuple(4L, Optional.of(1), Optional.of(-4), Optional.of(4), Optional.of(-1)),
	            ReactiveSeq.of(1, 2, 3, 4).lazyOperations().collect(count(), min(), min(i -> -i), max(), max(i -> -i)).get()
	        );
	    }
}
