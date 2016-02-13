package com.aol.cyclops.streams.future;

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

public class FutureCollectableTest {
		Executor exec = Executors.newFixedThreadPool(1);
		@Test
	    public void testCount() {
		
	        assertEquals(new Long(0l), ReactiveSeq.of().futureOperations(exec).count().join());
	        assertEquals((Long)0L, ReactiveSeq.of().futureOperations(exec).countDistinct().join());
	        assertEquals((Long)0L, ReactiveSeq.<Integer>of().futureOperations(exec).countDistinctBy(l -> l % 3).join());

	        assertEquals((Long)1L, ReactiveSeq.of(1).futureOperations(exec).count().join());
	        assertEquals((Long)1L, ReactiveSeq.of(1).futureOperations(exec).countDistinct().join());
	        assertEquals((Long)1L, ReactiveSeq.of(1).futureOperations(exec).countDistinctBy(l -> l % 3L).join());

	        assertEquals((Long)2L, ReactiveSeq.of(1, 2).futureOperations(exec).count().join());
	        assertEquals((Long)2L, ReactiveSeq.of(1, 2).futureOperations(exec).countDistinct().join());
	        assertEquals((Long)2L, ReactiveSeq.of(1, 2).futureOperations(exec).countDistinctBy(l -> l % 3L).join());

	        assertEquals((Long)3L, ReactiveSeq.of(1, 2, 2).futureOperations(exec).count().join());
	        assertEquals((Long)2L, ReactiveSeq.of(1, 2, 2).futureOperations(exec).countDistinct().join());
	        assertEquals((Long)2L, ReactiveSeq.of(1, 2, 2).futureOperations(exec).countDistinctBy(l -> l % 3L).join());

	        assertEquals((Long)4L, ReactiveSeq.of(1, 2, 2, 4).futureOperations(exec).count().join());
	        assertEquals((Long)3L, ReactiveSeq.of(1, 2, 2, 4).futureOperations(exec).countDistinct().join());
	        assertEquals((Long)2L, ReactiveSeq.of(1, 2, 2, 4).futureOperations(exec).countDistinctBy(l -> l % 3L).join());
	    }
	    
	    @Test
	    public void testSum() {
	        assertEquals(Optional.empty(), ReactiveSeq.of().futureOperations(exec).sum().join());
	        
	        assertEquals(Optional.of(1), ReactiveSeq.of(1).futureOperations(exec).sum().join());
	        assertEquals(Optional.of(3), ReactiveSeq.of(1, 2).futureOperations(exec).sum().join());
	        assertEquals(Optional.of(6), ReactiveSeq.of(1, 2, 3).futureOperations(exec).sum().join());
	        
	        assertEquals(Optional.of(1.0), ReactiveSeq.of(1.0).futureOperations(exec).sum().join());
	        assertEquals(Optional.of(3.0), ReactiveSeq.of(1.0, 2.0).futureOperations(exec).sum().join());
	        assertEquals(Optional.of(6.0), ReactiveSeq.of(1.0, 2.0, 3.0).futureOperations(exec).sum().join());
	    }
	    
	    @Test
	    public void testAvg() {
	        assertEquals(Optional.empty(), ReactiveSeq.of().futureOperations(exec).avg().join());
	        
	        assertEquals(Optional.of(1), ReactiveSeq.of(1).futureOperations(exec).avg().join());
	        assertEquals(Optional.of(1), ReactiveSeq.of(1, 2).futureOperations(exec).avg().join());
	        assertEquals(Optional.of(2), ReactiveSeq.of(1, 2, 3).futureOperations(exec).avg().join());
	        
	        assertEquals(Optional.of(1.0), ReactiveSeq.of(1.0).futureOperations(exec).avg().join());
	        assertEquals(Optional.of(1.5), ReactiveSeq.of(1.0, 2.0).futureOperations(exec).avg().join());
	        assertEquals(Optional.of(2.0), ReactiveSeq.of(1.0, 2.0, 3.0).futureOperations(exec).avg().join());
	    }

	    @Test
	    public void testCollect() {
	        assertEquals(
	            tuple(0L, Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty()),
	            ReactiveSeq.<Integer>of().futureOperations(exec).collect(count(), min(), min(i -> -i), max(), max(i -> -i)).join()
	        );

	        assertEquals(
	            tuple(1L, Optional.of(1), Optional.of(-1), Optional.of(1), Optional.of(-1)),
	            ReactiveSeq.of(1).futureOperations(exec).collect(count(), min(), min(i -> -i), max(), max(i -> -i)).join()
	        );

	        assertEquals(
	            tuple(2L, Optional.of(1), Optional.of(-2), Optional.of(2), Optional.of(-1)),
	            ReactiveSeq.of(1, 2).futureOperations(exec).collect(count(), min(), min(i -> -i), max(), max(i -> -i)).join()
	        );

	        assertEquals(
	            tuple(3L, Optional.of(1), Optional.of(-3), Optional.of(3), Optional.of(-1)),
	            ReactiveSeq.of(1, 2, 3).futureOperations(exec).collect(count(), min(), min(i -> -i), max(), max(i -> -i)).join()
	        );

	        assertEquals(
	            tuple(4L, Optional.of(1), Optional.of(-4), Optional.of(4), Optional.of(-1)),
	            ReactiveSeq.of(1, 2, 3, 4).futureOperations(exec).collect(count(), min(), min(i -> -i), max(), max(i -> -i)).join()
	        );
	    }
}
