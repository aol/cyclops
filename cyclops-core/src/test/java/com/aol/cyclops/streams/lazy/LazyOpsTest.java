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

import com.aol.cyclops.control.SequenceM;

public class LazyOpsTest {
		Executor exec = Executors.newFixedThreadPool(1);
		@Test
	    public void testCount() {
		
	        assertEquals(new Long(0l), SequenceM.of().lazyOperations().count());
	        assertEquals((Long)0L, SequenceM.of().lazyOperations().countDistinct());
	        assertEquals((Long)0L, SequenceM.<Integer>of().lazyOperations().countDistinctBy(l -> l % 3));

	        assertEquals((Long)1L, SequenceM.of(1).lazyOperations().count());
	        assertEquals((Long)1L, SequenceM.of(1).lazyOperations().countDistinct());
	        assertEquals((Long)1L, SequenceM.of(1).lazyOperations().countDistinctBy(l -> l % 3L));

	        assertEquals((Long)2L, SequenceM.of(1, 2).lazyOperations().count());
	        assertEquals((Long)2L, SequenceM.of(1, 2).lazyOperations().countDistinct());
	        assertEquals((Long)2L, SequenceM.of(1, 2).lazyOperations().countDistinctBy(l -> l % 3L));

	        assertEquals((Long)3L, SequenceM.of(1, 2, 2).lazyOperations().count());
	        assertEquals((Long)2L, SequenceM.of(1, 2, 2).lazyOperations().countDistinct());
	        assertEquals((Long)2L, SequenceM.of(1, 2, 2).lazyOperations().countDistinctBy(l -> l % 3L));

	        assertEquals((Long)4L, SequenceM.of(1, 2, 2, 4).lazyOperations().count());
	        assertEquals((Long)3L, SequenceM.of(1, 2, 2, 4).lazyOperations().countDistinct());
	        assertEquals((Long)2L, SequenceM.of(1, 2, 2, 4).lazyOperations().countDistinctBy(l -> l % 3L));
	    }
	    
	    @Test
	    public void testSum() {
	        assertEquals(Optional.empty(), SequenceM.of().lazyOperations().sum());
	        
	        assertEquals(Optional.of(1), SequenceM.of(1).lazyOperations().sum());
	        assertEquals(Optional.of(3), SequenceM.of(1, 2).lazyOperations().sum());
	        assertEquals(Optional.of(6), SequenceM.of(1, 2, 3).lazyOperations().sum());
	        
	        assertEquals(Optional.of(1.0), SequenceM.of(1.0).lazyOperations().sum());
	        assertEquals(Optional.of(3.0), SequenceM.of(1.0, 2.0).lazyOperations().sum());
	        assertEquals(Optional.of(6.0), SequenceM.of(1.0, 2.0, 3.0).lazyOperations().sum());
	    }
	    
	    @Test
	    public void testAvg() {
	        assertEquals(Optional.empty(), SequenceM.of().lazyOperations().avg());
	        
	        assertEquals(Optional.of(1), SequenceM.of(1).lazyOperations().avg());
	        assertEquals(Optional.of(1), SequenceM.of(1, 2).lazyOperations().avg());
	        assertEquals(Optional.of(2), SequenceM.of(1, 2, 3).lazyOperations().avg());
	        
	        assertEquals(Optional.of(1.0), SequenceM.of(1.0).lazyOperations().avg());
	        assertEquals(Optional.of(1.5), SequenceM.of(1.0, 2.0).lazyOperations().avg());
	        assertEquals(Optional.of(2.0), SequenceM.of(1.0, 2.0, 3.0).lazyOperations().avg());
	    }

	    @Test
	    public void testCollect() {
	        assertEquals(
	            tuple(0L, Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty()),
	            SequenceM.<Integer>of().lazyOperations().collect(count(), min(), min(i -> -i), max(), max(i -> -i))
	        );

	        assertEquals(
	            tuple(1L, Optional.of(1), Optional.of(-1), Optional.of(1), Optional.of(-1)),
	            SequenceM.of(1).lazyOperations().collect(count(), min(), min(i -> -i), max(), max(i -> -i))
	        );

	        assertEquals(
	            tuple(2L, Optional.of(1), Optional.of(-2), Optional.of(2), Optional.of(-1)),
	            SequenceM.of(1, 2).lazyOperations().collect(count(), min(), min(i -> -i), max(), max(i -> -i))
	        );

	        assertEquals(
	            tuple(3L, Optional.of(1), Optional.of(-3), Optional.of(3), Optional.of(-1)),
	            SequenceM.of(1, 2, 3).lazyOperations().collect(count(), min(), min(i -> -i), max(), max(i -> -i))
	        );

	        assertEquals(
	            tuple(4L, Optional.of(1), Optional.of(-4), Optional.of(4), Optional.of(-1)),
	            SequenceM.of(1, 2, 3, 4).lazyOperations().collect(count(), min(), min(i -> -i), max(), max(i -> -i))
	        );
	    }
}
