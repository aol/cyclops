package com.aol.cyclops2.streams.streamable;



import static cyclops.stream.Streamable.of;
import static java.util.Arrays.asList;
import static cyclops.collections.tuple.Tuple.tuple;
import static org.junit.Assert.assertEquals;

import java.util.function.BiPredicate;

import org.junit.Test;

public class SQLTest  {
		 
		 BiPredicate TRUE = (t, u) -> true;


		    @Test
		    public void testOnEmpty() throws X {
		        assertEquals(asList(1), of().onEmpty(1).toList());
		        assertEquals(asList(1), of().onEmptyGet(() -> 1).toList());
		        

		        assertEquals(asList(2), of(2).onEmpty(1).toList());
		        assertEquals(asList(2), of(2).onEmptyGet(() -> 1).toList());
		        assertEquals(asList(2), of(2).onEmptyThrow(() -> new X()).toList());

		        assertEquals(asList(2, 3), of(2, 3).onEmpty(1).toList());
		        assertEquals(asList(2, 3), of(2, 3).onEmptyGet(() -> 1).toList());
		        assertEquals(asList(2, 3), of(2, 3).onEmptyThrow(() -> new X()).toList());
		    }
		   

		    @SuppressWarnings("serial")
			public class X extends Exception {}

		    @Test
		    public void testConcat() {
		        assertEquals(asList(1, 2, 3, 4), of(1).concat(of(2, 3, 4)).toList());
		        assertEquals(asList(1, 2, 3, 4), of(1, 2).concat(of(3, 4)).toList());
		        assertEquals(asList(1, 2, 3, 4), of(1).concat(2, 3, 4).toList());
		        assertEquals(asList(1, 2, 3, 4), of(1, 2).concat(3, 4).toList());
		    }

	}