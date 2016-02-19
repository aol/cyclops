package com.aol.cyclops.react.base;

import static java.util.Arrays.asList;
import static org.jooq.lambda.tuple.Tuple.tuple;
import static org.junit.Assert.assertEquals;
import java.util.function.BiPredicate;
import java.util.function.Supplier;

import org.junit.Test;

import com.aol.cyclops.types.futurestream.LazyFutureStream;

public abstract class BaseSequentialSQLTest {
	abstract protected  <U> LazyFutureStream<U> of(U... array);
	abstract protected  <U> LazyFutureStream<U> ofThread(U... array);
	abstract protected <U> LazyFutureStream<U> react(Supplier<U>... array);
	
	 BiPredicate TRUE = (t, u) -> true;
	 @Test
	    public void testCrossJoin() {

	        // {A} x {B}
	        // ---------------------------------------------------------------------
	        assertEquals(asList(),
	            of().crossJoin(of()).toList());
	        assertEquals(asList(),
	            of().crossJoin(of(1)).toList());
	        assertEquals(asList(),
	            of().crossJoin(of(1, 2)).toList());

	        assertEquals(asList(),
	            of("A").crossJoin(of()).toList());
	        assertEquals(asList(
	            tuple("A", 1)),
	            of("A").crossJoin(of(1)).toList());
	        assertEquals(asList(
	            tuple("A", 1),
	            tuple("A", 2)),
	            of("A").crossJoin(of(1, 2)).toList());

	        assertEquals(asList(),
	            of("A", "B").crossJoin(of()).toList());
	        assertEquals(asList(
	            tuple("A", 1),
	            tuple("B", 1)),
	            of("A", "B").crossJoin(of(1)).toList());
	        assertEquals(asList(
	            tuple("A", 1),
	            tuple("A", 2),
	            tuple("B", 1),
	            tuple("B", 2)),
	            of("A", "B").crossJoin(of(1, 2)).toList());

	        assertEquals(asList(),
	            of("A", "B", "C").crossJoin(of()).toList());
	        assertEquals(asList(
	            tuple("A", 1),
	            tuple("B", 1),
	            tuple("C", 1)),
	            of("A", "B", "C").crossJoin(of(1)).toList());
	        assertEquals(asList(
	            tuple("A", 1),
	            tuple("A", 2),
	            tuple("B", 1),
	            tuple("B", 2),
	            tuple("C", 1),
	            tuple("C", 2)),
	            of("A", "B", "C").crossJoin(of(1, 2)).toList());


	        



	       
	    }

	    @Test
	    public void testInnerJoin() {
	      

	        assertEquals(asList(),
	            of().innerJoin(of(), TRUE).toList());
	        assertEquals(asList(),
	            of().innerJoin(of(1), TRUE).toList());
	        assertEquals(asList(),
	            of().innerJoin(of(1, 2), TRUE).toList());

	        assertEquals(asList(),
	        		of(1).innerJoin(of(), TRUE).toList());
	        assertEquals(asList(),
	            of(1).innerJoin(of(2), (t, u) -> t == u).toList());
	        assertEquals(asList(
	            tuple(1, 2)),
	            of(1).innerJoin(of(2), (t, u) -> t * 2 == u).toList());
	        assertEquals(asList(
	            tuple(1, 1)),
	            of(1).innerJoin(of(1, 2), (t, u) -> t == u).toList());
	        assertEquals(asList(
	            tuple(1, 2)),
	            of(1).innerJoin(of(1, 2), (t, u) -> t * 2 == u).toList());
	        assertEquals(asList(
	            tuple(1, 1),
	            tuple(1, 2)),
	            of(1).innerJoin(of(1, 2), TRUE).toList());
	    }

	    @Test
	    public void testLeftOuterJoin() {
	       

	        assertEquals(asList(),
	            of().leftOuterJoin(of(), TRUE).toList());
	        assertEquals(asList(),
	            of().leftOuterJoin(of(1), TRUE).toList());
	        assertEquals(asList(),
	            of().leftOuterJoin(of(1, 2), TRUE).toList());

	        assertEquals(asList(
	            tuple(1, null)),
	            of(1).leftOuterJoin(of(), TRUE).toList());
	        assertEquals(asList(
	            tuple(1, null)),
	            of(1).leftOuterJoin(of(2), (t, u) -> t == u).toList());
	        assertEquals(asList(
	            tuple(1, 2)),
	            of(1).leftOuterJoin(of(2), (t, u) -> t * 2 == u).toList());
	        assertEquals(asList(
	            tuple(1, 1)),
	            of(1).leftOuterJoin(of(1, 2), (t, u) -> t == u).toList());
	        assertEquals(asList(
	            tuple(1, 2)),
	            of(1).leftOuterJoin(of(1, 2), (t, u) -> t * 2 == u).toList());
	        assertEquals(asList(
	            tuple(1, 1),
	            tuple(1, 2)),
	            of(1).leftOuterJoin(of(1, 2), TRUE).toList());
	    }

	    @Test
	    public void testRightOuterJoin() {
	       

	        assertEquals(asList(),
	            of().rightOuterJoin(of(), TRUE).toList());
	        assertEquals(asList(
	            tuple(null, 1)),
	            of().rightOuterJoin(of(1), TRUE).toList());
	        assertEquals(asList(
	            tuple(null, 1),
	            tuple(null, 2)),
	            of().rightOuterJoin(of(1, 2), TRUE).toList());

	        assertEquals(asList(),
	        	of(1).rightOuterJoin(of(), TRUE).toList());
	        assertEquals(asList(
	            tuple(null, 2)),
	            of(1).rightOuterJoin(of(2), (t, u) -> t == u).toList());
	        assertEquals(asList(
	            tuple(1, 2)),
	            of(1).rightOuterJoin(of(2), (t, u) -> t * 2 == u).toList());
	        assertEquals(asList(
	            tuple(1, 1),
	            tuple(null, 2)),
	            of(1).rightOuterJoin(of(1, 2), (t, u) -> t == u).toList());
	        assertEquals(asList(
	            tuple(null, 1),
	            tuple(1, 2)),
	            of(1).rightOuterJoin(of(1, 2), (t, u) -> t * 2 == u).toList());
	        assertEquals(asList(
	            tuple(1, 1),
	            tuple(1, 2)),
	            of(1).rightOuterJoin(of(1, 2), TRUE).toList());
	    }

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
